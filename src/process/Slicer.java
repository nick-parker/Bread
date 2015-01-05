package process;

import java.io.IOException;
import java.util.ArrayList;

import math.geom2d.line.LineSegment2D;
import math.geom3d.Box3D;
import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.Surface3D;

/**
 * Stores info on a slice job and the layers which make it up. Eventually this class should have every
 * parameter set by its constructor, but for now non-final values are set manually in source code for testing. TODO
 */
public class Slicer {
	public final Model3D part;
	public final Surface3D shape;
	public final double layerHeight;
	public final double filD;
	public final double nozzleD;
	public final double extrusionWidth;
	public final int printTemp;
	public final int xySpeed;
	public final int zSpeed;
	public final int numShells;
	public final double infillWidth;
	public final double infillDir;	//Direction of infill on layer 0;
	public final double infillAngle;	//Amount to change infill direction each layer, radians CW.
	public final double lift;	//Amount to lift for travel moves.
	public final LineSegment2D[] topo;
	public final double EperL;	//E increase per unit L increase.
	public final double retraction;
	//Inputs below are optional, above are mandatory.
	public double shellSpeedMult = 1;	//unused
	public double bottomSpeedMult = 1;	//Currently unused.
	public int bottomLayerCount = 5;
	public double infillInsetMultiple = 0;	//Number of extrusion widths to inset infill beyond innermost shell
	public Slicer(Model3D part, Surface3D shape, double layerHeight, double filD, double nozzleD, double extrusionWidth,
			int printTemp, int xySpeed, int zSpeed, int numShells, double infillWidth, double infillDir, double infillAngle, 
			double lift, double retraction) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.extrusionWidth = extrusionWidth;
		this.printTemp = printTemp;
		this.xySpeed = xySpeed;
		this.zSpeed = zSpeed;
		this.numShells = numShells;
		this.infillWidth = infillWidth;
		this.layerHeight = layerHeight;
		this.infillDir = infillDir;
		this.infillAngle = infillAngle;
		this.part = part;
		this.shape = shape;
		this.topo = shape.topology();
		this.lift = lift;
		this.retraction = retraction;
		//Cross sectional area of the extrusion is the ratio of plastic volume/XYZ distance, units mm^2
		//volume rate * filament distance/unit volume = filament rate. filament distance/unit volume is cx area of filament.
		this.EperL = (((extrusionWidth-layerHeight)*extrusionWidth+3.14*Math.pow(layerHeight,2)/4))/Math.pow(filD,2);
	}
	/**
	 * Position shape so that its highest point is layerHeight/2 above the part's lowest point.
	 */
	private void PositionShape(){
		Box3D b1 = shape.boundingBox();
		Box3D b2 = part.boundingBox();
		Vector3D mv = new Vector3D(0,0,b2.getMinZ()-b1.getMaxZ()+layerHeight/2);
		shape.move(mv);
	}
	private int layerCount(){
		Box3D b1 = shape.boundingBox();
		Box3D b2 = part.boundingBox();
		//Distance between highest point on part and the lowest point on a properly positioned shape.
		double distance = b1.getDepth()+b2.getDepth()-layerHeight/2;
		return (int) Math.ceil(distance/layerHeight);
	}
	public void slice(String fileLoc){
		PositionShape();
		int lc = layerCount();
		GcodeExport g = new GcodeExport(fileLoc, this);
		g.writeFromFile("start.gcode");
		g.setTempAndWait(this.printTemp);
		for(int n=0;n<lc;n++){
//			if(n<bottomLayerCount) g.SetSpeed((int)Math.round(this.Speed*this.bottomSpeedMult));
//			else g.SetSpeed(this.Speed);
			Layer l = new Layer(this,n);
			System.out.println("Offset: "+l.offset);
			Reproject r = new Reproject(l.offset,this);
			ArrayList<Extrusion2D> p = l.getPath();
			if(p==null) continue;
			ArrayList<Extrusion3D> path = r.Proj(p);
			g.addLayer(path);			
		}
		g.writeFromFile("end.gcode");
		g.close();
	}
}

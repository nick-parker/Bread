package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import process.GcodeExport;
import process.Reproject;
import structs.Extrusion2D;
import structs.Extrusion3D;
import structs.Layer;
import tests.IntersectTest;
import math.geom2d.Point2D;
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
	public Model3D part;
	public Surface3D shape;
	public double layerHeight;
	public double filD;
	public double nozzleD;
	public double extrusionWidth;
	public int printTemp;
	public int xySpeed;
	public int zSpeed;
	public int numShells;
	public double infillWidth;
	public double infillDir;	//Direction of infill on layer 0;
	public double infillAngle;	//Amount to change infill direction each layer, radians CW.
	public double lift;	//Amount to lift for travel moves.
	public LineSegment2D[] topo;
	public double EperL;	//E increase per unit L increase.
	public double retraction;
	public double retractSpeed;
	public double retractThreshold;
	public double zMin = 0.25;
	public boolean FirmwareRetract = false;
	public int topLayerStart;
	public int botLayers;
	public int layerCount;
	//Inputs below are optional, above are mandatory.
	public boolean cross = true;
	public double infillFlowMultiple = 1;
	public double infillInsetMultiple = -0.25;	//Number of extrusion widths to inset infill beyond innermost shell, or neg value to overlap.
	public double minInfillLength = 0.25;
	public Slicer(Model3D part, Surface3D shape, double layerHeight, double filD, double nozzleD, double extrusionWidth,
			int printTemp, int xySpeed, int zSpeed, int numShells, double infillWidth, int infillDir, int infillAngle, 
			double lift, double retraction, double retractSpeed, double retractThreshold, int topLayers, int botLayers) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.extrusionWidth = extrusionWidth;
		this.printTemp = printTemp;
		this.xySpeed = xySpeed;
		this.zSpeed = zSpeed;
		this.numShells = numShells;
		this.infillWidth = infillWidth;
		this.layerHeight = layerHeight;
		this.infillDir = infillDir*Math.PI/180.0;
		this.infillAngle = infillAngle*Math.PI/180.0;
		this.part = part;
		this.shape = shape;
		this.topo = shape.topology();
		this.lift = lift;
		this.retraction = retraction;
		this.retractSpeed = retractSpeed;
		this.retractThreshold = retractThreshold;
		this.layerCount = layerCount();
		this.topLayerStart = layerCount-topLayers;
		this.botLayers = botLayers;
		//Cross sectional area of the extrusion is the ratio of plastic volume/XYZ distance, units mm^2
		//volume rate * filament distance/unit volume = filament rate. filament distance/unit volume is cx area of filament.
		this.EperL = (((extrusionWidth-layerHeight)*extrusionWidth+3.14*Math.pow(layerHeight,2)/4))/Math.pow(filD,2);
	}
	/**
	 * Load a slicer config for the given meshes.
	 * @param config
	 */
	public Slicer(Model3D part, Surface3D shape, String config){
		this.part = part;
		this.shape = shape;
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(config));
			while(f.ready()){
				String[] line = f.readLine().split(" ");
				switch(line[0]){
				case "layerHeight":
					this.layerHeight = Double.parseDouble(line[1]);
					break;
				case "filD":
					this.filD = Double.parseDouble(line[1]);
					break;
				case "nozzleD":
					this.nozzleD = Double.parseDouble(line[1]);
					break;
				case "extrusionWidth":
					this.extrusionWidth = Double.parseDouble(line[1]);
					break;
				case "printTemp":
					this.printTemp = Integer.parseInt(line[1]);
					break;
				case "xySpeed":
					this.xySpeed = Integer.parseInt(line[1]);
					break;
				case "zSpeed":
					this.zSpeed = Integer.parseInt(line[1]);
					break;
				case "numShells":
					this.numShells = Integer.parseInt(line[1]);
					break;
				case "infillWidth":
					this.infillWidth = Double.parseDouble(line[1]);
					break;
				case "infillDir":
					this.infillDir = Double.parseDouble(line[1])*Math.PI/180;
					break;
				case "infillAngle":
					this.infillAngle = Double.parseDouble(line[1])*Math.PI/180;
					break;
				case "lift":
					this.lift = Double.parseDouble(line[1]);
					break;
				case "retraction":
					this.retraction = Double.parseDouble(line[1]);
					break;
				case "retractSpeed":
					this.retractSpeed = Double.parseDouble(line[1]);
					break;
				case "retractThreshold":
					this.retractThreshold = Double.parseDouble(line[1]);
					break;
				case "topLayers":
					this.topLayerStart = Integer.parseInt(line[1]);
				case "botLayers":
					this.botLayers = Integer.parseInt(line[1]);
				}
			}
			this.EperL = (((extrusionWidth-layerHeight)*extrusionWidth+3.14*Math.pow(layerHeight,2)/4))/Math.pow(filD,2);
			this.topo = shape.topology();
			this.layerCount = layerCount();
			this.topLayerStart = layerCount-topLayerStart;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
		shape.move(new Vector3D(0,0,zMin));
		GcodeExport g = new GcodeExport(fileLoc, this);
		g.writeFromFile("start.gcode");
		g.setTempAndWait(this.printTemp);
		Point2D last = new Point2D(0,0);
		for(int n=0;n<layerCount;n++){
			last = doLayer(n,g,last);
		}
		g.writeFromFile("end.gcode");
		g.close();
	}
	private Point2D doLayer(int n, GcodeExport g, Point2D last){
		Layer l = new Layer(this,n);
		System.out.println("Offset: "+l.offset);
		shape.setOffset(l.offset);
		ArrayList<Extrusion2D> p = l.getPath(last);
		if(p==null||p.size()==0) return last;
		last = p.get(p.size()-1).lastPoint();
		Reproject r = new Reproject(l.offset,this);
		ArrayList<Extrusion3D> path = r.Proj(p);
		if(path.size()==0) System.out.println(p);
		g.addLayer(path,n);	
		return last;
	}
	public void debug(String fileLoc){
		PositionShape();
		shape.move(new Vector3D(0,0,zMin));
		int lc = layerCount;
		IntersectTest t = new IntersectTest(fileLoc);
		for(int n=0;n<lc;n++){
			Layer l = new Layer(this,n);
			shape.setOffset(l.offset);
			t.export(shape.overlap(part));
		}
		t.close();
	}
}

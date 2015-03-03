package main;

import io.GcodeExport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import process.Brim;
import process.NozzleOffset;
import process.Reproject;
import structs.Extrusion2D;
import structs.Extrusion3D;
import structs.Layer;
import structs.SmartLayer;
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
	public double zMin = 0;
	public boolean FirmwareRetract = false;
	public int topLayerStart;
	public int botLayers;
	public int layerCount;
	//Inputs below are optional, above are mandatory.
	public boolean cross = true;
	public boolean allSolid = false;
	public int brimCount = 8;
	public double TipRadius = 0.1; //Radius of the flat tip of the nozzle, NOT the hole itself. 
	public double infillFlowMultiple = 1;
	public double infillInsetMultiple = 0.05;	//Number of extrusion widths to inset infill beyond innermost shell, or neg value to overlap.
	public double minInfillLength = 1.25;
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
		Vector3D mv = new Vector3D(0,0,b2.getMinZ()-b1.getMaxZ()+layerHeight);
		shape.move(mv);
		b1 = shape.boundingBox();
		System.out.println("New max Z: " + b1.getMaxZ());
		Vector3D layerUp = new Vector3D(0,0,layerHeight);
		Vector3D layerDown = new Vector3D(0,0,-layerHeight);
		//TODO Figure out why the hell this is necessary... Bounding Box implementation is probably broken junk?
		while(!checkEmpty(0)){
			System.out.println("Moving Down");
			shape.move(layerDown);
		}
		while(checkEmpty(0)){
			System.out.println("Moving up");
			shape.move(layerUp);
		}
	}
	private int layerCount(){
		PositionShape();
		Box3D b1 = shape.boundingBox();
		Box3D b2 = part.boundingBox();
		//Distance between highest point on part and the lowest point on a properly positioned shape.
		double distance = b1.getDepth()+b2.getDepth()-layerHeight/2;
		int max = 1+(int) Math.ceil(distance/layerHeight);
		int min = 0;
		while(!checkEmpty(min)&&checkEmpty(max)&&min<max-1){
			int mid = (min+max)/2;
			if(checkEmpty(mid)) max=mid;
			else min=mid;
		}
		shape.setOffset(0);
		return max;
		
		
	}
	/**
	 * Check if a layer with the given number would be empty.
	 * @param layerNum
	 * @return
	 */
	private boolean checkEmpty(int layerNum){
		shape.setOffset(layerNum*layerHeight);
//		LineSegment3D[] overlap = shape.overlap(part);
		return !shape.intersect(part);
	}
	public void slice(String fileLoc){
//		PositionShape();
		part.move(new Vector3D(0,0,zMin));
		GcodeExport g = new GcodeExport(fileLoc, this);
		g.writeFromFile("start.gcode");
		g.setTempAndWait(this.printTemp);
		Point2D last = new Point2D(0,0);
		ArrayList<Extrusion3D> br = Brim.brim(this, brimCount, last);
		if(brimCount!=0) g.addLayer(br, -1);
		last = new Point2D(br.get(br.size()-1).lastPoint().getX(),br.get(br.size()-1).lastPoint().getY());
		for(int n=0;n<layerCount;n++){
			last = doLayer(n,g,last);
		}
		g.writeFromFile("end.gcode");
		g.close();
	}
	private Point2D doLayer(int n, GcodeExport g, Point2D last){
		SmartLayer l = new SmartLayer(this,n);
		l.makeChunks();
//		Layer l = new Layer(this,n);
		System.out.println("Offset: "+l.offset);
		shape.setOffset(l.offset);
		ArrayList<Extrusion2D> p = l.getPath(last);
		if(p==null||p.size()==0) return last;
		last = p.get(p.size()-1).lastPoint();
		Reproject r = new Reproject(l.offset,this);
		ArrayList<Extrusion3D> path = NozzleOffset.offset(r.Proj(p),TipRadius);
//		ArrayList<Extrusion3D> path =  r.Proj(p);
		g.addLayer(path,n);	
		return last;
	}
	public void debug(String fileLoc){
		PositionShape();
		part.move(new Vector3D(0,0,zMin));
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

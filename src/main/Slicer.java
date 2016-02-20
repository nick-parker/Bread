package main;

import io.GcodeExport;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
import math.geom3d.Box3D;
import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.Surface3D;

/**
 * Stores info on a slice job and the layers which make it up. Eventually this class should have every
 * parameter set by its constructor, but for now non-final values are set manually in source code for testing.
 */
public class Slicer {
	public Model3D part; // The mesh of the part being printed
	public Surface3D shape; // A mesh representing the shape of the layers
	public Model3D support; //A mesh representing all the support structures for this part.
	public double layerHeight;
	public double filD;
	public double nozzleD;
	public double extrusionWidth;
	public int printTemp;
	public int xySpeed;
	public int zSpeed;
	public int numShells;
	public double lift;	//Amount to lift for travel moves.
	public double EperL;	//E increase per unit L increase.
	public double retraction;
	public double retractSpeed;
	public double retractThreshold;
	public int topLayerStart;
	public int botLayers;
	public int layerCount;
	//Inputs below are optional, above are mandatory.
	public boolean cross = true;
	public boolean allSolid = false;
	public boolean OuterFirst = false;
	public boolean EnableSupport = false;
	public boolean FirmwareRetract = false;
	public double infillWidth = 3;
	public double infillDir = 45*Math.PI/180.0;	//Direction of infill on layer 0;
	public double infillAngle = 90*Math.PI/180.0;	//Amount to change infill direction each layer, radians CW.
	public int brimCount = 0;
	public double TipRadius = 0.1; //Radius of the flat tip of the nozzle, NOT the hole itself. 
	public double infillFlowMultiple = 1;
	public double infillInsetMultiple = 0;	//Number of extrusion widths to inset infill beyond innermost shell, or neg value to overlap.
	public double minInfillLength = 0.5;
	public double xMax = 0;
    public double yMax = 0;
    public double zMax = 0;
    public double xMin = 0;
    public double yMin = 0;
    public double zMin = 0;
    public Vector3D bedMin = new Vector3D(0,0,0);
    public Vector3D bedMax = new Vector3D(0,0,0);
    public Vector3D bedCenter = new Vector3D(0,0,0);
	public double SupportDist = 2;
	/**
	 * Load a slicer config for the given meshes.
	 * @param config file to pull slicer settings from.
	 */
	public Slicer(Model3D part, Surface3D shape, String config){
		this.part = part;
		this.shape = shape;
		try {
			configure(new FileReader(config));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

    public Slicer(Model3D part, Surface3D shape, Reader r){
		this.part = part;
		this.shape = shape;
		configure(r);
	}
    private void configure(Reader r){
		BufferedReader f;
		try {
			f = new BufferedReader(r);
			String lineRead;
			while((lineRead = f.readLine())!=null){
				String[] line = lineRead.split("[ \t]");
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
					break;
				case "AllSolid":
					this.allSolid = Boolean.parseBoolean(line[1]);
					break;
				case "FirmwareRetract":
					this.FirmwareRetract = Boolean.parseBoolean(line[1]);
					break;
				case "TipRadius":
					this.TipRadius = Double.parseDouble(line[1]);
					break;
				case "brimCount":
					this.brimCount = Integer.parseInt(line[1]);
					break;
				case "OuterFirst":
					this.OuterFirst = Boolean.parseBoolean(line[1]);
					break;
                case "XMax":
                    this.xMax = Double.parseDouble(line[1]);
                    break;
                case "YMax":
                    this.yMax = Double.parseDouble(line[1]);
                    break;
                case "ZMax":
                    this.zMax = Double.parseDouble(line[1]);
                    break;
                case "XMin":
                    this.xMin = Double.parseDouble(line[1]);
                    break;
                case "YMin":
                    this.yMin = Double.parseDouble(line[1]);
                    break;
                case "ZMin":
                    this.zMin = Double.parseDouble(line[1]);
                    break;
				case "EnableSupport":
					this.EnableSupport = Boolean.parseBoolean(line[1]);
					break;
			
				case "SupportDist":
					this.SupportDist = Double.parseDouble(line[1]);
					break;
				}
			}
            this.setBedDimensions(new Vector3D(xMin, yMin, zMin), new Vector3D(xMax, yMax, zMax));

			this.EperL = (((extrusionWidth-layerHeight)*extrusionWidth+3.14*Math.pow(layerHeight,2)/4))/Math.pow(filD,2);
			shape.generateTopology();
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
		Box3D shapeBb = shape.boundingBox();
		Box3D partBb = part.boundingBox();

        double xOffset = bedCenter.getX() - ((partBb.getMaxX() - partBb.getMinX()) * 0.5);
        double yOffset = bedCenter.getY() - ((partBb.getMaxY() - partBb.getMinY()) * 0.5);

        Vector3D mv = new Vector3D(xOffset,yOffset,partBb.getMinZ()-shapeBb.getMaxZ()+layerHeight);
		shape.move(mv);
        part.move(new Vector3D(xOffset, yOffset,0));
		shapeBb = shape.boundingBox();
		System.out.println("New max Z: " + shapeBb.getMaxZ());
		Vector3D layerUp = new Vector3D(0,0,layerHeight);
		Vector3D layerDown = new Vector3D(0,0,-layerHeight);
		//Moving Down should never be printed unless boundingBox is broken.
		while(!checkEmpty(-1)){
			System.out.println("Moving Down");
			shape.move(layerDown);
		}
		//This is necessary because contacting bounding boxes does not imply contacting meshes.
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
		//binary search for the last layer.
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
	 * Warning: Changes shape offset. Change back after call if necessary.
	 * @param layerNum
	 * @return
	 */
	private boolean checkEmpty(int layerNum){
		shape.setOffset(layerNum * layerHeight);
//		LineSegment3D[] overlap = shape.overlap(part);
		return !shape.intersect(part);
	}
    public void slice(Writer w){
//		PositionShape();
		part.move(new Vector3D(0, 0, zMin));
		GcodeExport g = new GcodeExport(w, this);
		g.writeFromFile("start.gcode");
		g.setTempAndWait(this.printTemp);
//		Point2D last = new Point2D(shape.boundingBox().getMinX(),shape.boundingBox().getMinY());
		Point2D last = new Point2D(0,0);
		ArrayList<Extrusion3D> br = Brim.brim(this, brimCount, last);
		if(brimCount!=0){
			g.addLayer(br, -1);
			last = new Point2D(br.get(br.size()-1).lastPoint().getX(),br.get(br.size()-1).lastPoint().getY());
		}
		for(int n=0;n<layerCount;n++){
			last = doLayer(n,g,last);
		}
		g.writeFromFile("end.gcode");
		g.close();
	}
    public void slice(String fileLoc){
		try {
			slice(new FileWriter(fileLoc));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		part.move(new Vector3D(0, 0, zMin));
		int lc = layerCount;
		IntersectTest t = new IntersectTest(fileLoc);
		for(int n=0;n<lc;n++){
			Layer l = new Layer(this,n);
			shape.setOffset(l.offset);
			t.export(shape.overlap(part));
		}
		t.close();
	}
    public void setBedDimensions(Vector3D min, Vector3D max) {
        this.setBedMin(min);
        this.setBedMax(max);
        this.setBedCenter();
    }

    public void setBedMin(Vector3D vec){
        this.xMin = vec.getX();
        this.yMin = vec.getY();
        this.zMin =vec.getZ();
        this.bedMin = vec;
    }

    public void setBedMin(double x,double y,double z){
        this.xMin = x;
        this.yMin = y;
        this.zMin = z;
        this.bedMin = new Vector3D(x,y,z);
    }


    public void setBedMax(double x,double y,double z){
        this.xMax = x;
        this.yMax = y;
        this.zMax = z;
        this.bedMax = new Vector3D(x,y,z);
    }

    public void setBedMax(Vector3D vec){
        this.xMax = vec.getX();
        this.yMax = vec.getY();
        this.zMax =vec.getZ();
        this.bedMax = vec;
    }

    public void setBedCenter(){
        this.bedCenter = bedMax.minus(bedMin).times(0.5);
    }
    public void AddSupport(Model3D supp){
    	this.support = supp;
    	Box3D partBb = part.boundingBox();
    	double xOffset = bedCenter.getX() - ((partBb.getMaxX() - partBb.getMinX()) * 0.5);
        double yOffset = bedCenter.getY() - ((partBb.getMaxY() - partBb.getMinY()) * 0.5);
        this.support.move(new Vector3D(xOffset,yOffset,0));
    }
}

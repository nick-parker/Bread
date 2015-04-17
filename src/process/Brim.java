package process;

import java.util.ArrayList;

import main.Slicer;
import math.geom2d.Point2D;
import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.SimplePlane;
import mesh3d.Surface3D;
import structs.Extrusion2D;
import structs.Extrusion3D;
import structs.Extrusion2D.ET;
import structs.Loop;

public class Brim {
	public static ArrayList<Extrusion3D> brim(Slicer s, int count, Point2D lst){
		if(count==0) return new ArrayList<Extrusion3D>();
		double z = s.layerHeight*.75;
		Box3D bb = s.part.boundingBox();
		Surface3D plane = SimplePlane.MakePlane(bb.getMinX()-5, bb.getMinY()-5, bb.getMaxX()+5, bb.getMaxY()+5, z);
		LineSegment3D[] overlap = plane.overlap(s.part);
		ArrayList<Loop> loops = Order.ListOrder(Flatten.FlattenZ(overlap));
		ArrayList<ArrayList<Extrusion2D>> ls = new ArrayList<ArrayList<Extrusion2D>>();
		for(int i=count-1;i>=0;i--){
			ls.addAll(NativeInset.insetLines(loops, -(i+0.25)*s.extrusionWidth, ET.shell)); //Negative inset is an outset.
		}
		ArrayList<Extrusion3D> output = new ArrayList<Extrusion3D>();
		Point3D last = new Point3D(lst.getX(),lst.getY(),z);
		for(ArrayList<Extrusion2D> br : ls){
			Extrusion3D first = lift(br.get(0),z);
			output.add(new Extrusion3D(last,first.firstPoint(),ET.nonretracting));
			for(Extrusion2D ex : br){
				output.add(lift(ex,z));
			}
			last =  output.get(output.size()-1).lastPoint();
		}
		return output;
	}
	private static Extrusion3D lift(Extrusion2D e, double z){
		Point3D p1 = new Point3D(e.firstPoint().getX(),e.firstPoint().getY(),z);
		Point3D p2 = new Point3D(e.lastPoint().getX(),e.lastPoint().getY(),z);
		return new Extrusion3D(p1,p2, e.ExtrusionType);
	}
}

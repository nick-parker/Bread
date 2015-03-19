package process;

import java.util.ArrayList;

import main.Slicer;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.SimplePlane;
import mesh3d.Surface3D;
import structs.Extrusion2D;
import structs.Extrusion3D;
import structs.Extrusion2D.ET;
import structs.Loop;

//TODO Make this use a planar slice not a simple box.

public class Brim {
	public static ArrayList<Extrusion3D> brim(Slicer s, int count, Point2D lst){
		double z = s.layerHeight*.75;
		Box3D b = s.part.boundingBox();
//		Point2D p0 = new Point2D(b.getMinX(),b.getMinY());
//		Point2D p1 = new Point2D(b.getMaxX(),b.getMinY());
//		Point2D p2 = new Point2D(b.getMaxX(),b.getMaxY());
//		Point2D p3 = new Point2D(b.getMinX(),b.getMaxY());
//		Loop l = new Loop(new LineSegment2D(p0, p1));
//		l.AttemptAdd(new LineSegment2D(p1,p2));
//		l.AttemptAdd(new LineSegment2D(p2,p3));
//		l.AttemptAdd(new LineSegment2D(p3,p0));
//		ArrayList<Loop> loops = new ArrayList<Loop>();
//		loops.add(l);
//		Surface3D plane = SimplePlane.MakePlane(b.getMinX()-1, b.getMinY()-1, b.getMaxX()+1, b.getMaxY()+1, z);
		Surface3D plane = SimplePlane.MakePlane(-100, -100, 100, 100, z);
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

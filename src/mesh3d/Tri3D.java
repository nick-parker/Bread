package mesh3d;

import math.geom2d.Point2D;
import math.geom2d.Angle2D;
import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.Shape3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.line.StraightLine3D;
import math.geom3d.plane.Plane3D;

public class Tri3D{
	private Point3D[] ps;
	private LineSegment3D[] es;
	private Vector3D[] vs;	//Vectors describing u,v coordinate system of triangle.
	private Plane3D pl;
	/**
	 * Create a Tri3D with CCW normal.
	 */
	public Tri3D(Point3D p0, Point3D p1, Point3D p2){
		this.ps = new Point3D[]{p0,p1,p2};
		Vector3D v1 = new Vector3D(p0,p1);
		Vector3D v2 = new Vector3D(p0,p2);
		this.vs = new Vector3D[]{v1,v2};
		this.pl = new Plane3D(p0,v1,v2);
		LineSegment3D e0 = new LineSegment3D(p0, p1);
		LineSegment3D e1 = new LineSegment3D(p1, p2);
		LineSegment3D e2 = new LineSegment3D(p2, p0);
		this.es = new LineSegment3D[]{e0,e1,e2};
	}
	public Tri3D move(Vector3D v) {
		Point3D pA = ps[0].plus(v);
		Point3D pB = ps[1].plus(v);
		Point3D pC = ps[2].plus(v);
		return new Tri3D(pA,pB,pC);
	}
	/**
	 * @return a new Triangle with opposite normal to this triangle.
	 */
	public Tri3D flip(){
		return new Tri3D(Mesh3D.copyPoint(ps[0]),Mesh3D.copyPoint(ps[2]),Mesh3D.copyPoint(ps[1]));
	}
	/**
	 * @param l Line to intersect with tri.
	 * @return Intersection of l with this triangle, or null if there is no intersection.
	 */
	public Point3D lineIntersection(StraightLine3D l){
		Point3D p = pl.lineIntersection(l);
		return this.contains(p) ? p : null;
	}
	/**
	 * @param l Line segment to intersect with tri.
	 * @return Intersection of l with this triangle, or null if there is no intersection.
	 */
	public Point3D SegmentIntersection(LineSegment3D l){
		Point3D p = lineIntersection(l.supportingLine());
		if(p==null) return null;
		return l.contains(p) ? p : null;
	}
	/**
	 * @param p Point to test
	 * @return Whether this contains point p, edges are included.
	 */
	public boolean contains(Point3D p){
		if(p==null) return false;
		Vector3D v = new Vector3D(ps[0],p);
		return Vector3D.dotProduct(v,vs[0])<=vs[0].norm()&&Vector3D.dotProduct(v,vs[1])<=vs[1].norm();
	}
	public Point3D getMax(Vector3D v) {
		double d1 = Vector3D.dotProduct(vs[0], v);
		double d2 = Vector3D.dotProduct(vs[1], v);
		double d3 = Vector3D.dotProduct(vs[0].opposite(), v);
		if(d3>d1 &&d3>d2) return Mesh3D.copyPoint(ps[0]);
		if(d1>d2) return Mesh3D.copyPoint(ps[1]);
		return Mesh3D.copyPoint(ps[2]);
	}
	/**
	 * Find the line segment representing the intersection of this triangle with t.
	 * @param t Other triangle to intersect.
	 * @return A point3D[2] representing the segment, or null if the Tris don't intersect.
	 */
	public Point3D[] overlap(Tri3D t){
		//Get all 6 intersections of the edges with the two planes.
		Point3D[] hs = new Point3D[]{SegmentPlaneIntersection(t.es[0]),
				SegmentPlaneIntersection(t.es[1]),
				SegmentPlaneIntersection(t.es[2]),
				t.SegmentPlaneIntersection(es[0]),
				t.SegmentPlaneIntersection(es[1]),
				t.SegmentPlaneIntersection(es[2])};
		int i = 0;
		Point3D[] hits = new Point3D[2];
		for(Point3D h:hs){
			if(h!=null&&this.contains(h)&&t.contains(h)) hits[i++]=h;
		}
		return i==2 ? hits : null;	
	}
	/**
	 * @param l Line segment to intersect with the plane of this triangle.
	 * @return Intersection or null if it does not exist.
	 */
	public Point3D SegmentPlaneIntersection(LineSegment3D l) {
		Point3D p = pl.lineIntersection(l.supportingLine());
		return l.contains(p) ? p : null;
	}
	/**
	 * @return Normal vector for this triangle.
	 */
	public Vector3D normal() {
		return pl.normal();
	}
	/**
	 * @return the points of this triangle, breaking abstraction barrier. Do not modify these.
	 */
	public Point2D[] getPoints2D(){
		return new Point2D[]{new Point2D(ps[0].getX(),ps[0].getY()),new Point2D(ps[1].getX(),ps[1].getY()),new Point2D(ps[2].getX(),ps[2].getY())}; 
	}
}

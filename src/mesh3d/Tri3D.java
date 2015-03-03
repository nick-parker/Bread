package mesh3d;

import main.Constants;
import math.geom2d.Point2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.line.StraightLine3D;
import math.geom3d.plane.Plane3D;

public class Tri3D{
	private Point3D[] ps;
	private LineSegment3D[] es;
	private Vector3D u;
	private Vector3D v;
	private double udotv;
	private double denom; //Denominatoor of contains equations, baked in because it's a property of the triangle.
	private double originDot; //baked in value for testing which side of the plane a point is on.
	public final double radius;	//Distance from p0 to the furthest point on this triangle.
	public final double xyradius; //XY distance from p0 to the furthest point on this triangle.
	public final double zradius;
	private Plane3D pl;
	/**
	 * Create a Tri3D with CCW normal.
	 */
	public Tri3D(Point3D p0, Point3D p1, Point3D p2){
		this.ps = new Point3D[]{p0,p1,p2};
		Vector3D v1 = new Vector3D(p0,p1);
		Vector3D v2 = new Vector3D(p0,p2);
		this.u = v1;
		this.v = v2;
		this.udotv = Vector3D.dotProduct(u,v);
		this.denom = Math.pow(Vector3D.dotProduct(u,v),2)-u.normSq()*v.normSq();
		this.pl = new Plane3D(p0,u,v);
		LineSegment3D e0 = new LineSegment3D(p0, p1);
		LineSegment3D e1 = new LineSegment3D(p1, p2);
		LineSegment3D e2 = new LineSegment3D(p2, p0);
		this.es = new LineSegment3D[]{e0,e1,e2};
		this.originDot = PointDot(pl.normal(), pl.origin());
		this.radius = Math.max(length(e0),length(e2));
		this.xyradius= Math.sqrt(Math.max(Math.pow(p1.getX()-p0.getX(), 2)+Math.pow(p1.getY()-p0.getY(), 2),
				Math.pow(p2.getX()-p0.getX(), 2)+Math.pow(p2.getY()-p0.getY(), 2)));
		this.zradius = Math.max(Math.abs(p0.getZ()-p1.getZ()), Math.abs(p0.getZ()-p2.getZ()));
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
//		System.out.println(PointToStr(p));
		return this.TolContains(p,1e-3) ? p : null;
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
		return TolContains(p,Constants.tol);
		//TODO Figure out if adding tolerances here hurts us.
	}
	/**
	 * Contains with a tolerance.
	 * @param p
	 * @param tol Tolerance to account for floating point issues.
	 * @return
	 */
	private boolean TolContains(Point3D p, double tol) {
		if(p==null) return false;
		if(!pl.contains(p)) return false;
		//http://geomalgorithms.com/a06-_intersect-2.html
		Vector3D w = new Vector3D(ps[0],p);
		double[] p2d = new double[]{(udotv*Vector3D.dotProduct(w,v)-v.normSq()*Vector3D.dotProduct(w, u))/denom,
				(udotv*Vector3D.dotProduct(w,u)-u.normSq()*Vector3D.dotProduct(w, v))/denom};
		return p2d[0]>=-1*tol&&p2d[1]>=-1*tol&&p2d[0]<=1+tol&&p2d[1]<=1+tol&&p2d[0]+p2d[1]<=1+tol;
//		return p2d[0]>=0&&p2d[1]>=0&&p2d[0]<=1&&p2d[1]<=1&&p2d[0]+p2d[1]<=1;
	}
	public Point3D getMax(Vector3D vec) {
		double d1 = Vector3D.dotProduct(u, vec);
		double d2 = Vector3D.dotProduct(v, vec);
		if(d1<0&&d1<d2) return Mesh3D.copyPoint(ps[0]);
		if(d1>d2) return Mesh3D.copyPoint(ps[1]);
		return Mesh3D.copyPoint(ps[2]);
	}
	/**
	 * Find the line segment representing the intersection of this triangle with t.
	 * @param t Other triangle to intersect.
	 * @return A point3D[2] representing the segment, or null if the Tris don't intersect.
	 */
	public Point3D[] overlap(Tri3D t){
//		1. Compute plane equation of triangle 2.
		//Already baked in other triangle
//		2. Reject as trivial if all points of triangle 1 are on same side.
		//Dot them all with the normal and test against the origin of the plane
		Vector3D n2 = t.normal();
		double[] dots1 = new double[]{PointDot(n2,ps[0]),PointDot(n2,ps[1]),PointDot(n2,ps[2])};
		if(allSameSide(dots1,t.originDot)) return null;	//This triangle doesn't intersect the other's plane.
//		3. Compute plane equation of triangle 1.
		//Baked in this triangle
//		4. Reject as trivial if all points of triangle 2 are on same side.
		Vector3D n = normal();
		double[] dots2 = new double[]{PointDot(n,t.ps[0]),PointDot(n,t.ps[1]),PointDot(n,t.ps[2])};
		if(allSameSide(dots2,originDot)) return null; //The other doesn't intersect this triangle's plane.
		//Check they aren't coplanar
		if(Vector3D.isColinear(n,n2)) return null;
//		5. Compute intersection line and project onto largest axis.
//		6. Compute the intervals for each triangle.
//		7. Intersect the intervals.
		/* I'm deviating here, at the risk of writing something inefficient or wrong,
		 * because I want to understand my code. Also, the method described by Thomas Moller
		 * is a boolean intersection test, and doesn't yield the intersection itself.
		 * Steps below this are my own. */
//		5. Calculate the intersections of the edges with the opposite plane for each triangle
		Point3D[] hitsA = PlaneTriangleIntersection(this.pl,t.es);
		Point3D[] hitsB = PlaneTriangleIntersection(t.pl, this.es);
		if(hitsA==null||hitsB==null) return null;
//		6. Dot the intersections with normalized n1 x n2 to get their positions along the intersection line
		Vector3D dir = Vector3D.crossProduct(n, n2).normalize();
		double[] dotsA = new double[]{PointDot(dir,hitsA[0]), PointDot(dir,hitsA[1])};
		double lineOrigin = dotsA[0];
		double[] dotsB = new double[]{PointDot(dir,hitsB[0]), PointDot(dir,hitsB[1])};
		if(dotsA[0]>dotsA[1]) flipVals(dotsA,0,1);
		if(dotsB[0]>dotsB[1]) flipVals(dotsB,0,1);
//		7. Find the overlap between the two intervals on the intersection line
		double[] overlap = new double[]{Math.max(dotsA[0], dotsB[0]),Math.min(dotsA[1], dotsB[1])};
		if(overlap[0]>=overlap[1]) return null;	//Ranges don't overlap
//		8. Using one intersection as an origin, add multiples of normalized n1 x n2 to get the two points
		Vector3D add0 = dir.times(overlap[0]-lineOrigin);
		Vector3D add1 = dir.times(overlap[1]-lineOrigin);
//		9. Return the two points representing the intersection as an ordered pair along n1 x n2.
		return new Point3D[]{hitsA[0].plus(add0),hitsA[0].plus(add1)};
	}
	/**
	 * @param pl Plane to intersect
	 * @param lines Triangle of line segments to intersect
	 * @return The pair of points where the triangle intersects the plane. 
	 */
	private static Point3D[] PlaneTriangleIntersection(Plane3D pl, LineSegment3D[] lines){
		Point3D[] output = new Point3D[2];
		int k = 0;
		for(LineSegment3D ls:lines){
			Point3D p = SegmentPlaneIntersection(pl, ls);
			if(p!=null&&notIn(output,p)){
//				if(k==2) break;
				output[k] = p;
				k++;
			}
		}
		if(output[0]==null||output[1]==null) return null;
		return output;
	}
	private static boolean notIn(Point3D[] ps,Point3D p){
		for(Point3D p2:ps){
			if(p2!=null&&p.distance(p2)<Constants.tol) return false;
		}
		return true;
	}
	/**
	 * @param l Line segment to intersect with the plane of this triangle.
	 * @return Intersection or null if it does not exist.
	 */
	public static Point3D SegmentPlaneIntersection(Plane3D plane, LineSegment3D l) {
		Point3D p = plane.lineIntersection(l.supportingLine());
		return l.contains(p) ? p : null;
	}
	/**
	 * @return Normal vector for this triangle.
	 */
	public Vector3D normal() {
		return pl.normal();
	}
	/**
	 * @return the points of this triangle projected into XY.
	 */
	public Point2D[] getPoints2D(){
		return new Point2D[]{getPoint(0),getPoint(1),getPoint(2)}; 
	}
	public Point2D getPoint(int i){
		return new Point2D(ps[i].getX(),ps[i].getY());
	}
	public Point3D getPoint3D(int i){
		return ps[i];
	}
	@Override
	public String toString(){
		return "Tri3D: "+PointToStr(ps[0])+" " + PointToStr(ps[1]) + " " + PointToStr(ps[2]);
	}
	public static String PointToStr(Point3D p){
		return "["+ Constants.xyz.format(p.getX()) + " " + Constants.xyz.format(p.getY()) + " " +Constants.xyz.format(p.getZ()) +"]";
	}
	public static String VectorToStr(Vector3D v){
		return "{"+ v.getX() + " " + v.getY() + " " +v.getZ() +"}";
	}
	public static <T> void flipVals(T[] arr, int a, int b){
		T tmp = arr[a];
		arr[a]=arr[b];
		arr[b]=tmp;
	}
	public static void flipVals(double[] arr, int a, int b){
		double tmp = arr[a];
		arr[a]=arr[b];
		arr[b]=tmp;
	}
	public static double PointDot(Vector3D v, Point3D p){
		Vector3D v2 = new Vector3D(p);
		return Vector3D.dotProduct(v, v2);
	}
	public static double length(LineSegment3D l){
		return l.firstPoint().distance(l.lastPoint());
	}
	/**
	 * Check that either all values of a are > than v or all
	 * values of a are <= v.
	 * @return whether all values are on the same side.
	 */
	public static boolean allSameSide(double[] a, double v){
		boolean great = true;
		for(double i:a){
			if(i<=v) great=false; 
		}
		if(great) return true;
		for(double i:a){
			if(i>v) return false;
		}
		return true;
	}
	/**
	 * @return Backing plane, breaking abstraction barrier. Should be fine since planes are immutable?
	 */
	public Plane3D plane() {
		return pl;
	}
	static public boolean equiv(Point3D a, Point3D b){return a.distance(b)<Constants.tol;}
	
}

package mesh3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import utils2D.Utils2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.line.StraightLine3D;

public class Surface3D extends Mesh3D{
	double offset;	//Z offset applied to this surface.
	private Hashtable<Point2D,Tri3D> TriMap;
	private ArrayList<Point2D> ps;
	private double MaxRad;
	public Surface3D(Tri3D[] ts) {
		this.tris = ts;
		offset = 0;
		makeBB();
		makeMaps();
	}
	/**
	 * Generate the list and point->Tri hash table used to
	 * quickly find the nearest triangles in 2d.
	 */
	private void makeMaps() {
		TriMap = new Hashtable<Point2D,Tri3D>();
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		for(Tri3D t: this.tris){
			Point2D p = t.getPoint(0);
			TriMap.put(p, t);
			points.add(p);
		}
		ps = points;
		MaxRad = 0;
		for(Tri3D t: this.tris){
			if(t.radius>MaxRad) MaxRad=t.radius;
		}
	}

	@Override
	public boolean closed(){return false;};
	
	public LineSegment3D[] overlap(Mesh3D m){
		LineSegment3D[] output = new LineSegment3D[m.triCount()*this.triCount()];
		int j=0;
		for(Tri3D tS:tris){
//			System.out.println("Testing Surface triangle " + tS.toString());
			for(Tri3D tM:m.getTris()){
				Point3D[] hits = tS.overlap(tM);
//				if(Math.abs(tM.normal().getZ())<Constants.tol){
//					System.out.println("Found a side face, should intersect.");
//				}
				if(hits!=null){
					Vector3D edge = new Vector3D(hits[0],hits[1]);
					Vector3D cross = Vector3D.crossProduct(tS.normal(), tM.normal());
					//Orient line segments so that the inside of the model is CCW from them, viewed facing the surface normal.
					output[j]=Vector3D.dotProduct(edge, cross)>0 ? new LineSegment3D(hits[0],hits[1]) :
						new LineSegment3D(hits[1],hits[0]);
					j++;
//					System.out.println("Face with normal "+Tri3D.VectorToStr(tM.normal())+" intersects " +tS.toString()+" on line segment "+Tri3D.PointToStr(hits[0])+" to " +Tri3D.PointToStr(hits[1]));
				}
			}
		}
		return Arrays.copyOf(output,j);
	}
	/**
	 * @return A 2d projection of this Surface's topology.
	 */
	public LineSegment2D[] topology(){
		ArrayList<LineSegment2D> output = new ArrayList<LineSegment2D>();;
		for(Tri3D t:tris){
			Point2D[] ps = t.getPoints2D();
			//Generate the 2d projection of each triangle
			LineSegment2D[] ls = new LineSegment2D[]{new LineSegment2D(ps[0],ps[1]),
					new LineSegment2D(ps[1],ps[2]),
					new LineSegment2D(ps[2],ps[0])};
			//Throw away any lines which we already have in the topology.
			boolean[] checks = new boolean[]{true,true,true};
			for(LineSegment2D l:output){
				for(int i=0;i<3;i++){
					if(checks[i]&&(l.almostEquals(ls[i], Constants.tol)||l.almostEquals(ls[i].reverse(), Constants.tol))){
						checks[i]=false;
					}
				}				
			}
			//Add the lines we want to keep to our topology.
			for(int i=0;i<3;i++){
				if(checks[i])output.add(ls[i]);
			}
		}
		return output.toArray(new LineSegment2D[output.size()]);
	}
	/**
	 * Set the amount by which this surface is offset in the Z axis.
	 * @param z Amount to offset from original position.
	 */
	public void setOffset(double z){
		if(offset!=z){
			this.move(new Vector3D(0,0,z-offset));
			offset = z;
		}
	}
	/**
	 * Project point P onto this surface in the Z axis.
	 * @param p Point2D to project
	 * @return The projected point as a Point3D object, or null if p is not in the domain of
	 * this surface.
	 */
	public Point3D project(Point2D p){
		StraightLine3D l = new StraightLine3D(new Point3D(p.getX(),p.getY(),0),new Vector3D(0,0,1));
		for(Point2D tp : ps){
			//If the largest triangle in the mesh could include both these points
			if(Utils2D.within(p,tp,MaxRad)){
				Tri3D t = TriMap.get(tp);
				Point3D hit = t.lineIntersection(l);
				if(hit!=null) return hit;
			}
		}
		return null;
	}
//	/**
//	 * Returns the tri whose origin point is the i'th closest to point p in 2D.
//	 * @param p
//	 * @return A tri3D from this surface's set of tris.
//	 */
//	private Tri3D getNearest(Point2D p, int i){
//		int index = ps.getIndex(p);
//		Point2D op = ps.get(index + i%2==0 ? i/2 : -i/2);
//		if(op==null) return null;
//		return TriMap.get(op);
//	}
}

package mesh3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import utils2D.Utils2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.line.StraightLine3D;

public class Surface3D extends Mesh3D{
	double offset;	//Z offset applied to this surface.
	private Hashtable<Point2D,Tri3D> TriMap;
	private ArrayList<Point2D> ps;
	private double MaxRad;	//Length of the longest edge in this mesh, useful for reprojection.
	public Surface3D(Tri3D[] ts) {
		this.tris = ts;
		offset = 0;
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
			if(t.xyradius>MaxRad) MaxRad=t.xyradius;
		}
		MaxRad+=Constants.tol;
	}

	@Override
	public boolean closed(){return false;};
	
	public LineSegment3D[] overlap(Mesh3D m){
		LineSegment3D[] output = new LineSegment3D[m.triCount()*this.triCount()];
		int j=0;
		for(Tri3D tS:tris){
			for(Tri3D tM:m.tris){
				//Skip any Tris that can't possibly reach each other.
				if(Math.abs(tS.getPoint3D(0).getZ()-tM.getPoint3D(0).getZ())>tS.zradius+tM.zradius) continue;
				if(tS.getPoint(0).distance(tM.getPoint(0))>tS.radius+tM.radius) continue;
//				System.out.println("Passed both prechecks.");
				Point3D[] hits = tS.overlap(tM);
				if(hits!=null){
					Vector3D edge = new Vector3D(hits[0],hits[1]);
					Vector3D cross = Vector3D.crossProduct(tS.normal(), tM.normal());
					//Orient line segments so that the inside of the model is CCW from them, viewed facing the surface normal.
					output[j]=Vector3D.dotProduct(edge, cross)>0 ? new LineSegment3D(hits[0],hits[1]) :
						new LineSegment3D(hits[1],hits[0]);
					j++;
				}
			}
		}
		return Arrays.copyOf(output,j);
	}
	/**
	 * Project the edges which make up this surface into 2D to intersect them with paths which are
	 * to be projected onto this surface. Duplicate edges (from neighboring triangles) are removed, ie
	 * the output of this function is a set of 2D line segments.
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
			//Throw away any lines which we already have in the topology. Also, any line which has 0 length.
			boolean[] checks = new boolean[]{true,true,true};
			for(LineSegment2D l:output){
				for(int i=0;i<3;i++){
					if(checks[i]&&(l.almostEquals(ls[i], Constants.tol)||l.almostEquals(ls[i].reverse(), Constants.tol))){
						checks[i]=false;
					}
					if(l.length()<Constants.tol) checks[i]=false;
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
	 * Set the amount by which this surface is offset from its "true" position in the Z axis.
	 * @param z Amount to offset from original position.
	 */
	public void setOffset(double z){
		if(offset!=z){
			this.move(new Vector3D(0,0,z-offset));
			makeMaps();
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
		StraightLine3D l = new StraightLine3D(new Point3D(p.getX(),p.getY(),0),Constants.zplus);
		for(Point2D tp : ps){
			//If the largest edge in the mesh could connect these points
			if(Utils2D.within(p,tp,MaxRad)){
				//Test the triangle associated with this point.
				Tri3D t = TriMap.get(tp);
				Point3D hit = t.lineIntersection(l);
				if(hit!=null) return hit;
			}
			//Otherwise the associated triangle can't possibly reach this point.
		}
		Box3D b = this.boundingBox();
		System.out.println("Point " + p.getX()+" "+p.getY() + " Failed to project.");
		System.out.println("X bounds: "+ b.getMinX()+" : "+b.getMaxX()+" Y bounds: "+b.getMinY()+" : "+b.getMaxY());
//		return project(p);
		return null;
	}
	@Override
	public void move(Vector3D v){{
		Tri3D[] newTris = new Tri3D[tris.length];
		int i=0;
		for(Tri3D t:tris){
			newTris[i]=t.move(v);
			i++;
			}
		tris=newTris;
		};
		makeMaps();
	}
	public double getOffset() {
		return offset;
	}
}

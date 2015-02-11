package mesh3d;

import java.util.Arrays;

import main.Constants;
import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.Shape3D;
import math.geom3d.Vector3D;
import math.geom3d.transform.AffineTransform3D;

abstract class Mesh3D implements Shape3D{
	protected Tri3D[] tris;
	/**
	 * Generate the smallest possible axis aligned box which contains this mesh.
	 */
	@Override
	public Box3D boundingBox(){
		return new Box3D(getMax(Constants.xminus).getX(),getMax(Constants.xplus).getX(),
				getMax(Constants.yminus).getY(),getMax(Constants.yplus).getY(),
				getMax(Constants.zminus).getZ(),getMax(Constants.zplus).getZ());
	}
	
	/**
	 * Move the mesh by the specified vector. Equivalent cost to a copy operation because Tris and meshes are immutable.
	 * This should probably be changed to return a new Mesh to match the functionality of Tri3D.move, and to make multithreading
	 * step 1 easy/possible.
	 */
	public void move(Vector3D v){{
		Tri3D[] newTris = new Tri3D[tris.length];
		int i=0;
		for(Tri3D t:tris){
			newTris[i]=t.move(v);
			i++;
			}
		tris=newTris;
		};
	}
	/**
	 * @param v Direction to search
	 * @return The point on this mesh furthest in the v direction, or origin if this Mesh has no triangles.
	 */
	public Point3D getMax(Vector3D v){
		Point3D max = Constants.origin;
		double maxD = 0;
		boolean first = true;
		for(Tri3D t:tris){
			Point3D m = t.getMax(v);
			double d = Vector3D.dotProduct(v, new Vector3D(Constants.origin,m));
			if(d>maxD){
				maxD=d;
				max=m;
				continue;
			}
			if(first){
				first=false;
				maxD=d;
				max=m;
			}
		}
		return max;
	}
	/**
	 * @return A copy of the tri array backing this Mesh. 
	 */
	public Tri3D[] getTris(){ return Arrays.copyOf(tris, tris.length);}
	/**
	 * @return Whether this is a closed shape.
	 */
	abstract public boolean closed();

	/**
	 * @return The number of tris in this mesh.
	 */
	public int triCount(){return tris.length;}
	
	public boolean intersect(Mesh3D m){
		for(Tri3D tS : tris){
			for(Tri3D tM : m.tris){
				if(Math.abs(tS.getPoint3D(0).getZ()-tM.getPoint3D(0).getZ())>tS.zradius+tM.zradius) continue;
				if(tS.getPoint(0).distance(tM.getPoint(0))>tS.radius+tM.radius) continue;
				Point3D[] overlap = tS.overlap(tM);
				if(overlap!=null&&overlap.length>0) return true;
			}
		}
		return false;
	}
	
	//Static methods below this point.
	/**
	 * @param p Point to duplicate
	 * @return Return a duplicate of point p.
	 */
	static public Point3D copyPoint(Point3D p){return new Point3D(p.getX(),p.getY(),p.getZ());}
	//Unimplemented inherited methods below this point.	
	
	/**
	 * Not Implemented.
	 */
	@Override
	public Shape3D clip(Box3D arg0) {throw new UnsupportedOperationException();}
	/**
	 * Unimplemented. For Model3D, see encloses.
	 */
	@Override
	public boolean contains(Point3D p) {throw new UnsupportedOperationException();};
	/**
	 * Not Implemented.
	 */
	@Override
	public double distance(Point3D arg0) {throw new UnsupportedOperationException();}
	/**
	 * Not Implemented. Use move(Vector3D) instead.
	 */
	@Override
	public Shape3D transform(AffineTransform3D arg0) {throw new UnsupportedOperationException();}
	/**
	 * Not implemented.
	 */
	@Override
	public boolean isBounded() {throw new UnsupportedOperationException();}
	/**
	 * Not Implemented
	 */
	@Override
	public boolean isEmpty() {throw new UnsupportedOperationException();}

}

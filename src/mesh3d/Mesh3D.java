package mesh3d;

import math.geom3d.Box3D;
import math.geom3d.Point3D;
import math.geom3d.Shape3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.transform.AffineTransform3D;

abstract class Mesh3D implements Shape3D{
	protected Tri3D[] tris;
	protected Box3D bb;
	/**
	 * Generate the smallest possible box which contains this mesh.
	 */
	@Override
	public Box3D boundingBox(){return bb;};
	/**
	 * Move the mesh by the specified vector.
	 */
	public void move(Vector3D v){{
		for(Tri3D t:tris){t.move(v);}
		makeBB();
		};
	}
	protected void makeBB(){
		bb = new Box3D(getMax(Constants.xminus).getX(),getMax(Constants.xplus).getX(),
				getMax(Constants.yminus).getY(),getMax(Constants.yplus).getY(),
				getMax(Constants.zminus).getZ(),getMax(Constants.zplus).getZ());
	};
	/**
	 * @param v Direction to search
	 * @return The point on this mesh furthest in the v direction, or origin if this Mesh has no triangles.
	 */
	public Point3D getMax(Vector3D v){
		Point3D max = Constants.origin;
		double maxD = -1e99;
		for(Tri3D t:tris){
			Point3D m = t.getMax(v);
			double d = Vector3D.dotProduct(v, new Vector3D(Constants.origin,m));
			if(d>maxD){
				maxD=d;
				max=m;
			}
		}
		return max;
	}
	/**
	 * @return the list of tris composing this mesh.
	 */
	public Tri3D[] getTris(){ return tris;}
	/**
	 * @return Whether this is a closed shape.
	 */
	abstract public boolean closed();

	/**
	 * @return The number of tris in this mesh.
	 */
	public int triCount(){return tris.length;}
	
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

package mesh3d;

import math.geom3d.Point3D;
import math.geom3d.Vector3D;

public class Transform3D {
	double[] t;
	public Transform3D(double[] t){
		if(t.length!=9){
			System.out.println("Transforms must be 3x3.");
			throw new UnsupportedOperationException();
		}
		this.t = t;
	}
	/**
	 * Apply this transform to a 3d coordinate or vector.
	 * @param v Coordinate/Vector to transform
	 * @return A new double[3] representing the transformed coordinate/vector.
	 */
	public double[] apply(double[] v){
		return new double[]{
				t[0]*v[0]+t[1]*v[1]*t[2]+v[2],
				t[3]*v[0]+t[4]*v[1]+t[5]*v[2],
				t[6]*v[0]+t[7]*v[1]+t[8]*v[2]};
	}
	public Point3D apply(Point3D p){
		double[] out = apply(new double[]{p.getX(),p.getY(),p.getZ()});
		return new Point3D(out[0],out[1],out[2]);
	}
	public Tri3D apply(Tri3D t){
		return new Tri3D(apply(t.getPoint3D(0)),apply(t.getPoint3D(1)),apply(t.getPoint3D(2)));
	}
	public Surface3D apply(Surface3D t){
		Tri3D[] ts = new Tri3D[t.tris.length];
		for(int i=0;i<ts.length;i++){
			ts[i]=apply(t.tris[i]);
		}
		return new Surface3D(ts);
	}
	public Model3D apply(Model3D t){
		Tri3D[] ts = new Tri3D[t.tris.length];
		for(int i=0;i<ts.length;i++){
			ts[i]=apply(t.tris[i]);
		}
		return new Model3D(ts);
	}
	/**
	 * @param o Length 9 double array representing another transform.
	 * @return o*this
	 */
	public Transform3D mul(double[] o){
		return new Transform3D( new double[]{
		o[0]*t[0]+o[1]*t[3]+o[2]*t[6],
		o[0]*t[1]+o[1]*t[4]+o[2]*t[7],
		o[0]*t[2]+o[1]*t[5]+o[2]*t[8],
		o[3]*t[0]+o[4]*t[3]+o[5]*t[6],
		o[3]*t[1]+o[4]*t[4]+o[5]*t[7],
		o[3]*t[2]+o[4]*t[5]+o[5]*t[8],
		o[6]*t[0]+o[7]*t[3]+o[8]*t[6],
		o[6]*t[1]+o[7]*t[4]+o[8]*t[7],
		o[6]*t[2]+o[7]*t[5]+o[8]*t[8]});
	}
	public Transform3D mul(Transform3D o){
		return mul(o.t);
	}
	public static Transform3D rotate(double degrees, Vector3D axis){
		double a = degrees*Math.PI/180;
		Vector3D u = axis.normalize();
		double l = u.getX();
		double m = u.getY();
		double n = u.getZ();
		double cos = Math.cos(a);
		double sin = Math.sin(a);
		double k1 = (1-cos);
		return new Transform3D(new double[]{
				l*l*k1+cos,
				m*l*k1-n*sin,
				n*l*k1+m*sin,
				
				l*m*k1+n*sin,
				m*m*k1+cos,
				n*m*k1-l*sin,
				
				l*n*k1-m*sin,
				m*n*k1+l*sin,
				n*n*k1+cos});		
	}
	public static Transform3D scale(double x, double y, double z){
		return new Transform3D(new double[]{x,0,0,0,y,0,0,0,z});
	}
}

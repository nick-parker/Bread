package structs;

import math.geom3d.Point3D;
import math.geom3d.Vector3D;

public class Point6D extends Point3D{
	public Vector3D normal;
	public Point6D(double x, double y, double z, Vector3D n){
		super(x,y,z);
		this.normal = n;
	}
	public Point6D(Point3D p, Vector3D n){
		super(p.getX(),p.getY(),p.getZ());
		this.normal = n;
	}
}

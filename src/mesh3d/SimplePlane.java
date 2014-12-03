package mesh3d;

import math.geom3d.Point3D;

public class SimplePlane {
	public static Surface3D MakePlane(double minX, double minY, double maxX, double maxY, double z){
		Point3D p1 = new Point3D(maxX,maxY,z);
		Point3D p2 = new Point3D(minX,minY,z);
		Point3D p3 = new Point3D(maxX,minY,z);
		Tri3D t1 = new Tri3D(p1,p2,p3);
		Point3D q1 = new Point3D(minX,minY,z);
		Point3D q2 = new Point3D(maxX,maxY,z);
		Point3D q3 = new Point3D(minX,maxY,z);
		Tri3D t2 = new Tri3D(q1,q2,q3);
		return new Surface3D(new Tri3D[]{t1,t2});
	}
}

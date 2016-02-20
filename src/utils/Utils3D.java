package utils;

import process.Flatten;
import main.Constants;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Tri3D;

public class Utils3D {
	
	public static boolean equiv(LineSegment3D a, LineSegment3D b){
		return (equiv(a.firstPoint(),b.firstPoint())&&equiv(a.lastPoint(),b.lastPoint()))
				||
				(equiv(a.firstPoint(),b.lastPoint())&&equiv(a.lastPoint(),b.firstPoint()));
	}
	
	static public boolean equiv(Point3D a, Point3D b){return a.distance(b)<Constants.tol;}

	public static double PointDot(Vector3D v, Point3D p){
		Vector3D v2 = new Vector3D(p);
		return Vector3D.dotProduct(v, v2);
	}

	public static double length(LineSegment3D l){
		return l.firstPoint().distance(l.lastPoint());
	}

	public static String PointToStr(Point3D p){
		return "["+ Constants.xyz.format(p.getX()) + " " + Constants.xyz.format(p.getY()) + " " +Constants.xyz.format(p.getZ()) +"]";
	}

	public static String VectorToStr(Vector3D v){
		return "{"+ v.getX() + " " + v.getY() + " " +v.getZ() +"}";
	}
	/**
	 * @param p Point to duplicate
	 * @return Return a duplicate of point p.
	 */
	static public Point3D copyPoint(Point3D p){return new Point3D(p.getX(),p.getY(),p.getZ());}
	/**
	 * Calculate the slope of this line, delta(Z)/delta(XY)
	 * @param l Line to measure slope from
	 * @return Double representing slope, Z/sqrt(x^2+y^2)
	 */
	public static double slope(LineSegment3D l){
		return (l.firstPoint().getZ()-l.lastPoint().getZ())/Utils3D.length(l);
	}
}

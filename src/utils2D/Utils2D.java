package utils2D;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;

public class Utils2D {
	public static double PointDot(Vector2D v, Point2D p){
		return v.dot(new Vector2D(p));
	}
	/**
	 * @return A vector a radians CW from the +x axis.
	 */
	public static Vector2D AngleVector(double a){
		//.normalize()
		return (new Vector2D(Math.cos(a),-Math.sin(a)));
	}
}

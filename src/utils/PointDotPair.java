package utils;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;

public class PointDotPair{
	public final Point2D p;
	public final double dot;
	public PointDotPair(Point2D p, double dot){
		this.dot = dot;
		this.p = p;
	}
	public PointDotPair(Point2D p, Vector2D v){
		this.p = p;
		this.dot = Utils2D.PointDot(v,p);
	}
}
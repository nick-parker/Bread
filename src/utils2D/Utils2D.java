package utils2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import structs.Extrusion2D;
import structs.Extrusion2D.ET;
import main.Constants;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Point3D;

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
	public static ArrayList<Point2D> orderPoints(Vector2D v, Collection<Point2D> ps){
		ArrayList<Point2D> output = new ArrayList<Point2D>();
		ArrayList<PointDotPair> sortedPs = new ArrayList<PointDotPair>();
		for(Point2D p:ps){
			sortedPs.add(new PointDotPair(p,v));
		}
		Collections.sort(sortedPs,new PointDotPairComp());		
		for(int k=0;k<sortedPs.size();k++){
			output.add(sortedPs.get(k).p);
		}
		return output;
	}
	public static ArrayList<LineSegment2D> ConnectPoints(ArrayList<Point2D> ps){
		ArrayList<LineSegment2D> output = new ArrayList<LineSegment2D>();
		for(int k=0;k<ps.size()-1;k++){
			output.add(new LineSegment2D(ps.get(k),ps.get(k+1)));
		}
		return output;
	}
	public static ArrayList<Extrusion2D> ConnectPoints(ArrayList<Point2D> ps, ET extrusionType){
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		for(int k=0;k<ps.size()-1;k++){
			output.add(new Extrusion2D(ps.get(k),ps.get(k+1),extrusionType));
		}
		return output;
	}
	public static boolean equiv(Point2D p1, Point2D p2){
		return p1.distance(p2)<Constants.tol;
	}
	public static Point3D setZ(Point2D p, double z){
		return new Point3D(p.x(),p.y(),z);
	}
	static public boolean within(Point2D p, Point2D point2d, double d){return p.distance(point2d)<d;}
}

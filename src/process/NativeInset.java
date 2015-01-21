package process;

import java.util.ArrayList;

import process.Extrusion2D.ET;
import representation.ClipperJNA;
import representation.Domain;
import representation.IntPoint;
import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.MultiPolygon2D;
/**
 * Semifunctional wrapper for the CampSkeleton library. CampSkeleton performs straight skeleton based
 * polygon offsetting operations, but doesn't have support for certain skeleton features necessary to represent some
 * very common shapes (rectangular regions), and therefore isn't usable in this project. In the future NativeInset should be
 * replaced with a JNI interface to the Clipper library's C++ implementation.
 * @author Nick
 *
 */
public class NativeInset {
	/**
	 * Inset the provided paths by the given distance d.
	 * @param loops An ArrayList of Loop objects passed out of the Order function
	 * @param d A distance in mm to inset the loops.
	 * @return A new set of loops represented as an arraylist of arraylists of points.
	 */
	public static ArrayList<ArrayList<Point2D>> inset(ArrayList<Loop> loops, double d){
		ArrayList<ArrayList<Point2D>> points = new ArrayList<ArrayList<Point2D>>();
		for(Loop l:loops){
			if(!l.checkClosure()) System.out.println("Unclosed loop in NativeInset");
			points.add(l.getPointLoop());
		}
		return inset(d,points);
	}
	public static ArrayList<ArrayList<Point2D>> inset(double d, ArrayList<ArrayList<Point2D>> ps){
		Domain LoopDom = new Domain(ps);
		Domain output = Domain.parse(ClipperJNA.inset(LoopDom.toString(),(int)Math.round(d*-1*IntPoint.conv)));
//		System.out.println(output);
		return output==null ? null : output.conv();
	}
	public static MultiPolygon2D GetRegion(ArrayList<ArrayList<Point2D>> regionPs){
		ArrayList<LinearRing2D> rs = new ArrayList<LinearRing2D>();
		for(ArrayList<Point2D> r:regionPs){
			rs.add(new LinearRing2D(r));
		}
		return new MultiPolygon2D(rs);
	}
	public static ArrayList<ArrayList<Extrusion2D>> insetLines(ArrayList<Loop> loops, double d, ET type){
		ArrayList<ArrayList<Point2D>> ps = inset(loops,d);
		if(ps==null) return null;
		ArrayList<ArrayList<Extrusion2D>> output = new ArrayList<ArrayList<Extrusion2D>>();
		for(ArrayList<Point2D> a: ps){
			ArrayList<Extrusion2D> thisLoop = new ArrayList<Extrusion2D>();
			for(int i=0;i<a.size()-1;i++){
				thisLoop.add(new Extrusion2D(a.get(i),a.get(i+1),type));
			}
			thisLoop.add(new Extrusion2D(a.get(a.size()-1),a.get(0),type));
			output.add(thisLoop);
		}
		return output;
	}
}


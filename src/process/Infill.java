package process;

import java.util.ArrayList;
import java.util.Collection;

import utils2D.Utils2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.MultiPolygon2D;

public class Infill {
	/**
	 * 
	 * @param s The slicer to pull settings from.
	 * @param loops The set of loops representing the layer domain in 2D.
	 * @param distance Minimum distance to maintain between infill and loops.
	 * @param layerNumber The number of this layer, for infill orientation.
	 * @return A list of infill extrusions, ordered in a zig zag pattern across the part.
	 */
	public static ArrayList<Extrusion2D> getInfill(Slicer s, ArrayList<Loop> loops, double distance, int layerNumber){
		if(loops.size()==0) return null;
		ArrayList<ArrayList<Point2D>> regionPs;
		System.out.println(ToPoints(loops));
		if(distance!=0){
			//Generate the inset, as a set of rings of points. Currently nonfunctional.
			regionPs = NativeInset.inset(loops, distance);
		}
		else{
			regionPs = ToPoints(loops);
		}
		if(regionPs==null) return null;
		MultiPolygon2D region = NativeInset.GetRegion(regionPs);	//Convert to a multipolygon for some convenience.
		Collection<LineSegment2D> edges = region.edges();	//Get the edges of the multipolygon
		//Calculate the CW angle from +x to run infill on this layer.
		double a = (s.infillDir+layerNumber*s.infillAngle);	//CW angle infill lines make with x axis. %(2*Math.PI)
		//Calculate a perpendicular vector to the infill.
		Vector2D move = Utils2D.AngleVector(a+Math.PI/2);	//Direction perpendicular to infill to move intersection line.
		Vector2D dir = Utils2D.AngleVector(a);				//Direction parallel to infill.
		//Get the point furthest in the -move direction from the set of rings of points.
		Point2D firstP = getExtreme(move,regionPs,false);
		StraightLine2D l = new StraightLine2D(firstP, dir);
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		Box2D b = region.boundingBox();
		//Calculate the worst case scenario number of lines necessary, because calculating width in a given vector direction 
		//is implemented wrong.
		int lineCount = (int) (Math.sqrt(Math.pow(b.getHeight(),2)+Math.pow(b.getWidth(),2))/s.infillWidth);
		//Iterate through lines without checking that they actually intersect. getEdges returns an empty list for nonintersection,
		//so addAll and iterating through it both do nothing.
		for(int i=0;i<lineCount;i++){
			ArrayList<Extrusion2D> es = getEdges(l,edges,dir,1);
			if(i%2==0) output.addAll(es);
			else{	//Add them flipped and in reverse order. This way the lines form a zig zag pattern across part.
				for(int q=es.size()-1;q>=0;q--){
					Extrusion2D e = es.get(q);
					output.add(new Extrusion2D(e.lastPoint(),e.firstPoint(),1));
				}
			}
			//Move the line to the right.
			l = l.parallel(s.infillWidth);
		}
		return output;
	}
	private static ArrayList<ArrayList<Point2D>> ToPoints(ArrayList<Loop> loops) {
		ArrayList<ArrayList<Point2D>> output = new ArrayList<ArrayList<Point2D>>();
		for(Loop l: loops){
			output.add(l.getPointLoop());
		}
		return output;
	}
	/**
	 * 
	 * @param v
	 * @param ps
	 * @return The point in ps farthest in the given direction.
	 */
	public static Point2D getExtreme(Vector2D v, ArrayList<ArrayList<Point2D>> ps, boolean max){
//		System.out.println("Seeking max? "+max);
//		System.out.println("Vector: "+v);
		Point2D min = ps.get(0).get(0);
		double minDot = Utils2D.PointDot(v,min);
		for(ArrayList<Point2D> l : ps){
			for(Point2D p: l){
				double dot = Utils2D.PointDot(v, p);
//				System.out.println(p + "has dot of " + dot);
				if(!max&&dot<minDot){
					minDot = dot;
					min = p;
				}
				if(max&&dot>minDot){
					//Find max instead.
					minDot = dot;
					min = p;
				}
			}
		}
//		System.out.println("Found: "+min);
		return min;
	}
	/**
	 * @param v
	 * @param ps
	 * @return ps' width along v.
	 */
	public static double getWidth(Vector2D v, ArrayList<ArrayList<Point2D>> ps){
		Vector2D delta = new Vector2D(getExtreme(v,ps,false),getExtreme(v,ps,true));
		return v.dot(delta);
	}
	
	/**
	 * Intersect a line with a set of edges, then connect the pairs of intersections.
	 * 
	 * @param l Line to intersect
	 * @param v Direction of l, passing this in saves some calculations since it's the same for a whole layer.
	 * @param edges Set of edges to intersect with
	 * @return Segments of l inside the domain represented by edges. Empty list if edges and l don't intersect.
	 */
	public static ArrayList<Extrusion2D> getEdges(StraightLine2D l, Collection<LineSegment2D> edges, Vector2D v, int type){
		ArrayList<Point2D> ps = new ArrayList<Point2D>();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		//Generate the intersections of l with the edges
		for(LineSegment2D ls:edges){
			Point2D hit = l.intersection(ls);
			if(hit!=null){
				ps.add(hit);				
			}
		}
		ArrayList<Point2D> truePs = ps;
		//Check that there's an even number of intersections.
		if(truePs.size()%2!=0){
			System.out.println("Odd number of intersections.");
			return output;
		}
		//Sort the intersections along l
		ArrayList<Point2D> sorted = Utils2D.orderPoints(v, truePs);
		for(int k=0;k<sorted.size()-1;k+=2){
			if(Utils2D.equiv(sorted.get(k), sorted.get(k+1))) continue;	//Don't make 0 length edges.
			output.add(new Extrusion2D(sorted.get(k),sorted.get(k+1),type));
		}
		return output;
	}
}

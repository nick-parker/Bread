package process;

import java.util.ArrayList;

import straightskeleton.Corner;
import utils.LoopL;
import main.Inset;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.MultiPolygon2D;

public class NativeInset {
	public static ArrayList<ArrayList<Point2D>> inset(ArrayList<Loop> loops, double d){
		ArrayList<ArrayList<Point2D>> polies = new ArrayList<ArrayList<Point2D>>();
		for(Loop l:loops){
			if(!l.checkClosure()) System.out.println("Unclosed loop in NativeInset");
			polies.add(l.getPointLoop());
		}
		LoopL<Corner> Shell = Inset.inset(polies, d);
		ArrayList<ArrayList<Point2D>> output = new ArrayList<ArrayList<Point2D>>();
		for(utils.Loop<Corner> l:Shell){
			ArrayList<Point2D> ps = new ArrayList<Point2D>();
			for(Corner c:l){
				ps.add(new Point2D(c.x,c.y));
			}
			output.add(ps);
		}
		return output;		
	}
	public static MultiPolygon2D GetRegion(ArrayList<ArrayList<Point2D>> regionPs){
		ArrayList<LinearRing2D> rs = new ArrayList<LinearRing2D>();
		for(ArrayList<Point2D> r:regionPs){
			rs.add(new LinearRing2D(r));
		}
		return new MultiPolygon2D(rs);
	}
	public static ArrayList<ArrayList<Extrusion2D>> insetLines(ArrayList<Loop> loops, double d,int type){
		ArrayList<ArrayList<Point2D>> ps = inset(loops,d);
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


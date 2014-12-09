package process;

import java.util.ArrayList;

import straightskeleton.Corner;
import utils.LoopL;
import main.Inset;
import math.geom2d.Point2D;

public class NativeInset {
	public static ArrayList<ArrayList<Point2D>> inset(ArrayList<Loop> loops, int length, int d){
		ArrayList<ArrayList<Point2D>> polies = new ArrayList<ArrayList<Point2D>>();
		for(Loop l:loops) polies.add(l.getPointLoop());
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
}

package process;

import java.util.ArrayList;
import java.util.HashSet;

import math.geom2d.line.LineSegment2D;

public class Order {
	/**
	 * Separate an array of line segments into the continuous curves which
	 * make it up, and order those curves. Ends curves when they can no longer 
	 * be extended, with no regard to loop closure.
	 * @param ls Array of line segments to sort out and order.
	 * @return A set of continuous sets of line segments.
	 */
	static public ArrayList<Loop> ListOrder(LineSegment2D[] ls){
		ArrayList<Loop> output = new ArrayList<Loop>();
		HashSet<LineSegment2D> unsorted = new HashSet<LineSegment2D>();
		for(LineSegment2D l:ls) unsorted.add(l);
		while(!unsorted.isEmpty()){
			LineSegment2D active = unsorted.iterator().next();	//Grab a random unsorted seg.
			unsorted.remove(active);
			Loop activeLoop = new Loop(active);
			//Complete loops one at a time. This is inefficient but much easier to debug than
			//a more intelligent system where segments are built up and merged until no further
			//merges can be performed. TODO Make Order faster
			boolean changing = true;
			while(changing){
				changing = false;
				for(LineSegment2D l:unsorted){
					if(activeLoop.AttemptAdd(l)){
						unsorted.remove(l);
						changing = true;
						break;
					}
				}
			}
			output.add(activeLoop);
		}
		return output;
	}
}

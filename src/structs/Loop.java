package structs;

import java.util.ArrayList;
import java.util.Iterator;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import mesh3d.Constants;
/**
 * A loop of LineSegment2D objects which will hopefully be closed when finished, but is not guaranteed to be.
 * Used in the Order function.
 * @author Nick
 *
 */
public class Loop implements Iterable<LineSegment2D>{
	ArrayList<LineSegment2D> loop;
	LineSegment2D end;
	public Loop(LineSegment2D l){
		this.loop = new ArrayList<LineSegment2D>();
		loop.add(l);
		end = l;
	}
	/**
	 * @param l Line segment to append to the end of this loop.
	 */
	private void addEnd(LineSegment2D l){
		if(l.length()<Constants.tol) return;
		loop.add(l);
		end = l;
	}
	/**
	 * @param l Line Segment to append to the front of this loop.
	 */
	private void addFront(LineSegment2D l){
		if(l.length()<Constants.tol) return;
		loop.add(0, l);
	}
	/**
	 * Attempt to add the line segment l to this loop.
	 * @param l LineSegment2D to add.
	 * @return Whether l was added successfully.
	 */
	public boolean AttemptAdd(LineSegment2D l){
		if(equiv2D(end.lastPoint(),l.firstPoint())){
			addEnd(l);
			return true;
		}
		if(equiv2D(loop.get(0).firstPoint(),l.lastPoint())){
			addFront(l);
			return true;
		}
		return false;
	}
	/**
	 * @return Whether this loop is closed.
	 */
	public boolean checkClosure(){
		return equiv2D(loop.get(0).firstPoint(),end.lastPoint());
	}
	public void appendEnd(Loop other){
		loop.addAll(other.loop);
		end = other.end;
	}
	public void appendFront(Loop other){
		loop.addAll(0,other.loop);
	}
	public boolean AttemptAppend(Loop other){
//		System.out.println("Attempt Append:");
//		System.out.println(other.end.lastPoint().getX()+", "+other.end.lastPoint().getY() + " vs " + this.loop.get(0).firstPoint().getX()+", "+this.loop.get(0).firstPoint().getY());
		if(equiv2D(other.end.lastPoint(),loop.get(0).firstPoint())){
			appendFront(other);
			return true;
		}
//		System.out.println(this.end.lastPoint().getX()+", "+this.end.lastPoint().getY() + " vs " + other.loop.get(0).firstPoint().getX()+", "+other.loop.get(0).firstPoint().getY());
		if(equiv2D(this.end.lastPoint(),other.loop.get(0).firstPoint())){
			appendEnd(other);
			return true;
		}
		return false;
	}
	public int getLength() {
		return loop.size();
	}
	public ArrayList<Point2D> getPointLoop() {
		ArrayList<Point2D> output = new ArrayList<Point2D>();
		output.add(loop.get(0).firstPoint());
		for(LineSegment2D l:loop) output.add(l.lastPoint());
		return output;
	}
	public Iterator<LineSegment2D> iterator(){
		return loop.iterator();
	}
	
	static public boolean equiv2D(Point2D a, Point2D b){return a.distance(b)<Constants.tol;}
}

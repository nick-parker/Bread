package structs;

import java.util.ArrayList;
import java.util.Iterator;

import utils.Utils2D;
import main.Constants;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
/**
 * A loop of LineSegment2D objects which will hopefully be closed when finished, but is not guaranteed to be.
 * Used in the Order function.
 * @author Nick
 *
 */
public class Loop implements Iterable<LineSegment2D>{
	ArrayList<LineSegment2D> loop;
	LineSegment2D end;
	private boolean checked;
	private boolean hole;
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
		if(Utils2D.equiv(end.lastPoint(),l.firstPoint())){
			addEnd(l);
			return true;
		}
		if(Utils2D.equiv(loop.get(0).firstPoint(),l.lastPoint())){
			addFront(l);
			return true;
		}
		return false;
	}
	/**
	 * @return Whether this loop is closed.
	 */
	public boolean checkClosure(){
		return Utils2D.equiv(loop.get(0).firstPoint(),end.lastPoint());
	}
	public void appendEnd(Loop other){
		loop.addAll(other.loop);
		end = other.end;
	}
	public void appendFront(Loop other){
		loop.addAll(0,other.loop);
	}
	public boolean AttemptAppend(Loop other){
		if(Utils2D.equiv(other.end.lastPoint(),loop.get(0).firstPoint())){
			appendFront(other);
			return true;
		}
		if(Utils2D.equiv(this.end.lastPoint(),other.loop.get(0).firstPoint())){
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
	/**
	 * Calculate the signed area of this loop.
	 * @return The area of this loop calculated via cross products.
	 */
	public double area(){
		double sum = 0;
		for(LineSegment2D l: loop){
			Point2D p1 = l.firstPoint();
			Point2D p2 = l.lastPoint();
			sum += p1.getX()*p2.getY() - p2.getX()*p1.getY();
		}
		return sum/2;
	}
	/**
	 * Check whether this loop has a negative area, indicating that it is
	 * a hole and not a positive region.
	 * @return True if this loop has an area less than 0
	 */
	public boolean hole() {
		if(!checked){
			checked = true;
			hole = area()<0;
		}
		return hole;
	}
	/**
	 * @param i
	 * @return the element at the specified position in this loop.
	 */
	public LineSegment2D get(int i){
		return loop.get(i);
	}
	/**
	 * Check whether this loop contains point p within its area.
	 * Behavior on the border of this loop is rather undetermined.
	 * @param p Point to test
	 * @return True if p is within this loop.
	 */
	public boolean contains(Point2D p){
		if(loop.size()==0) return false;
		LineSegment2D test = new LineSegment2D(p,Constants.far2D);
		int hits = 0;
		for(LineSegment2D seg : loop){
			if(LineSegment2D.intersects(seg, test)) hits++;
		}
		return hits%2==1;
	}
	/**
	 * Checks whether this loop contains the beginning of l.
	 * Assuming loops are nonintersecting, this tests whether this loop encloses l.
	 * @param l Loop to test
	 * @return True if this loop contains l.
	 */
	public boolean contains(Loop l){
		return this.contains(l.get(0).firstPoint());
	}
}

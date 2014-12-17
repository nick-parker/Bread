package process;

import java.util.ArrayList;
import utils2D.Utils2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;

/**
 * ExtrusionType marks the type of extrusion this line represents.
 * 0 - travel
 * 1 - infill
 * 2 - shell
 * @author Nick
 *
 */
public class Extrusion2D extends LineSegment2D{
	public final int ExtrusionType;
	
	public Extrusion2D(Point2D point1, Point2D point2, int ExtrusionType) {
		super(point1, point2);
		this.ExtrusionType=ExtrusionType;
	}
	/**
	 * Return a set of Extrusion2Ds of the same type as this with the same endpoints,
	 * split anywhere this intersects the lines in topology.
	 * @param topology
	 * @return
	 */
	public ArrayList<Extrusion2D> splitExtrusion(LineSegment2D[] topology){
		ArrayList<Point2D> hits = new ArrayList<Point2D>();
		hits.add(this.firstPoint());
		for(LineSegment2D l: topology){
			if(l==null) continue;
			Point2D hit = this.intersection(l);
			if(hit!=null) hits.add(hit);
		}
		hits.add(this.lastPoint());
		ArrayList<Point2D> sorted = Utils2D.orderPoints(this.direction(), hits);
		return Utils2D.ConnectPoints(sorted,this.ExtrusionType);
	}
}

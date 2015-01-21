package process;

import java.util.ArrayList;

import process.Extrusion2D.ET;
import utils2D.Utils2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;

/**
 * Stores information on a single layer and outputs a continuous 2D path.
 */
public class Layer {
	Slicer s;	//Parent slicer object
	int layerNo;
	public double offset;
	ArrayList<Loop> loops;
	private boolean loopsMade = false;
	
	public Layer(Slicer s, int layerNo){
		this.s=s;
		this.layerNo=layerNo;
		this.offset = layerNo*s.layerHeight;
	}
	private void makeLoops(){
		s.shape.setOffset(offset);
		LineSegment2D[] ls = Flatten.FlattenZ(s.shape.overlap(s.part));
		this.loops = Order.ListOrder(ls);
	}
	/**
	 * Generate a set of line segments representing the infill for this layer.
	 * All lines are oriented in the same direction, ordered along that direction
	 * and to the right of that direction as though reading a book.
	 * @param distance Minimum distance between the model surface and the infill
	 * @return The set of line segments representing the infill.
	 */
	public ArrayList<Extrusion2D> getInfill(double distance){
		if(!loopsMade) makeLoops();
		return Infill.getInfill(s, loops, distance, layerNo);
	}
	/**
	 * Generate a set of line segments representing the shells for this layer.
	 * Ordered innermost shells first in a round robin fashion, so every
	 * region gets its first shell before any of them get their second.
	 * @return The described set in an ArrayList.
	 */
	public ArrayList<Extrusion2D> getShells(){
		if(!loopsMade) makeLoops();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		for(int i=s.numShells-1;i>=0;i--){
			double dist = (i+0.5)*s.extrusionWidth;
			ArrayList<ArrayList<Extrusion2D>> shells = NativeInset.insetLines(loops, dist,ET.shell);
			if(shells==null) continue;
			for(ArrayList<Extrusion2D> shell : shells){
				output.addAll(shell);
			}
		}
		return output;
	}
	public ArrayList<Extrusion2D> getPath(Point2D lastPoint){
		double infillDistance = (0.25+s.numShells+s.infillInsetMultiple)*s.extrusionWidth;
		ArrayList<Extrusion2D> infill = s.infillWidth==0 ? null : getInfill(infillDistance); //TODO Something smarter here
		ArrayList<Extrusion2D> shells = getShells();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		Point2D last = lastPoint;	//Last segment added to output.
		if(shells!=null){
			for(Extrusion2D e: shells){
				ConnectSplitAppend(last,e,output);
				last = e.lastPoint();
			}
		}
		if(infill!=null){
			for(Extrusion2D e: infill){
				ConnectSplitAppend(last,e,output);
				last = e.lastPoint();
			}
		}
//		if(shells!=null){
//			for(Extrusion2D e: shells){
//				ConnectSplitAppend(last,e,output);
//				last = e.lastPoint();
//			}
//		}
		if(output.size()==0) return null;
		return output;
	}
	/**
	 * Adds e, and a travel from the end of last to the beginning of e if necessary, to output. Both e and the travel
	 * are split based on the topology of the surface in this layer's slicer.
	 * @param lp Last position of the head.
	 * @param e Segment to add to output.
	 * @param output Continuous path composed of Extrusion2D objects.
	 */
	private void ConnectSplitAppend(Point2D lp, Extrusion2D e, ArrayList<Extrusion2D> output){
		//If last point isn't the start of e, add a travel between them and retract if necessary.
		Point2D np = e.firstPoint();
		if(!Utils2D.equiv(lp, np)){
			output.addAll((new Extrusion2D(
					lp,
					np,
					lp.distance(np)>s.retractThreshold ? ET.travel : ET.nonretracting)	//Retract is 0, nonretracting is 3.
					).splitExtrusion(s.topo));
		}
		output.addAll(e.splitExtrusion(s.topo));
	}
}

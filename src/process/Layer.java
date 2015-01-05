package process;

import java.util.ArrayList;

import utils2D.Utils2D;
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
			ArrayList<ArrayList<Extrusion2D>> shells = NativeInset.insetLines(loops, dist,2);
			for(ArrayList<Extrusion2D> shell : shells){
				output.addAll(shell);
			}
		}
		return output;
	}
	public ArrayList<Extrusion2D> getPath(){
//		double infillDistance = (0.5+s.numShells+s.infillInsetMultiple)*s.extrusionWidth;
		ArrayList<Extrusion2D> infill = getInfill(0);
		if(infill==null) return null;
//		ArrayList<Extrusion2D> shells = getShells();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		Extrusion2D last = null;	//Last segment added to output.
		for(Extrusion2D e: infill){
			ConnectSplitAppend(last,e,output);
			last = e;
		}
//		for(Extrusion2D e: shells){
//			ConnectSplitAppend(last,e,output);
//			last = e;
//		}
		return output;
	}
	/**
	 * Adds e, and a travel from the end of last to the beginning of e if necessary, to output. Both e and the travel
	 * are split based on the topology of the surface in this layer's slicer.
	 * @param last Last segment added to output.
	 * @param e Segment to add to output.
	 * @param output Continous path composed of Extrusion2D objects.
	 */
	private void ConnectSplitAppend(Extrusion2D last, Extrusion2D e, ArrayList<Extrusion2D> output){
		if(last!=null&&!Utils2D.equiv(last.lastPoint(), e.firstPoint())){
			output.addAll((new Extrusion2D(last.lastPoint(),e.firstPoint(),0)).splitExtrusion(s.topo));
		}
		output.addAll(e.splitExtrusion(s.topo));
	}
}

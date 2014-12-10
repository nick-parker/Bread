package process;

import java.util.ArrayList;

import math.geom2d.line.LineSegment2D;

/**
 * Stores information on a single layer and outputs a modular chunk of gcode to print
 * this layer.
 */
public class Layer {
	Slicer s;	//Parent slicer object
	int layerNo;
	ArrayList<Loop> loops;
	private boolean loopsMade = false;
	
	public Layer(Slicer s, int layerNo){
		this.s=s;
		this.layerNo=layerNo;
	}
	private void makeLoops(){
		s.shape.setOffset(layerNo*s.layerHeight);
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
	 * @param numShells Number of shells to generate.
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
		double infillDistance = (0.5+s.numShells+s.infillInsetMultiple)*s.extrusionWidth;
		ArrayList<Extrusion2D> infill = getInfill(infillDistance);
		ArrayList<Extrusion2D> shells = getShells();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		return null;
	}
}

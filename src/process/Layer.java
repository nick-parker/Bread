package process;

import java.util.ArrayList;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.MultiPolygon2D;

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
	public ArrayList<LineSegment2D> getInfill(double distance){
		if(!loopsMade) makeLoops();
		return Infill.getInfill(s, loops, distance, layerNo);
	}
}

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
	public Layer(Slicer s, int layerNo){
		this.s=s;
		this.layerNo=layerNo;
	}
	private ArrayList<Loop> getLoops(){
		s.shape.setOffset(layerNo*s.layerHeight);
		LineSegment2D[] ls = Flatten.FlattenZ(s.shape.overlap(s.part));
		return Order.ListOrder(ls);
	}
//	private ArrayList<LineSegment2D> UnroundCorners(CirculinearDomain2D c){
//		ArrayList<LineSegment2D> output = new ArrayList<LineSegment2D>();
//	}
}

package structs;

import java.util.ArrayList;

import process.Flatten;
import process.Order;
import main.Slicer;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;

/**
 * Implementation of Layer which makes use of LayerChunks.
 * @author Nick
 *
 */
public class SmartLayer {
	Slicer s;
	int layerNo;
	ArrayList<LayerChunk> chunks;
	public double offset;
	public SmartLayer(Slicer s, int layerNo){
		this.s=s;
		this.layerNo=layerNo;
		this.offset = layerNo*s.layerHeight;
		this.chunks = new ArrayList<LayerChunk>();
	}
	public void addChunk(LayerChunk lc){
		chunks.add(lc);
	}
	public void makeChunks(){
		s.shape.setOffset(offset);
		LineSegment2D[] intersect = Flatten.FlattenZ(s.shape.overlap(s.part));
		ArrayList<Loop> loops = Order.ListOrder(intersect);
		ArrayList<Loop> rechecks = new ArrayList<Loop>();
		for(Loop l : loops){
			if(l.hole())addHole(l);
			else chunks.add(new LayerChunk(s,layerNo,l));
		}
		for(Loop l : rechecks){
			if(!addHole(l)){
//				throw new MalformedLayerException();
				System.out.println("Error: Hole appears to be outside all chunks.");
			}
		}
	}
	private boolean addHole(Loop l){
		LayerChunk smallest = null;
		double minArea = 1e20;
		for(LayerChunk lc : chunks){
			if(lc.rim.contains(l)&&lc.rim.area()<minArea){
				smallest = lc;
				minArea = lc.rim.area();
			}
		}
		if(smallest!=null) smallest.addLoop(l);
		return smallest!=null;
	}
	public ArrayList<Extrusion2D> getPath(Point2D lastPoint){
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		Point2D p = lastPoint;
		for(LayerChunk lc : chunks){
			ArrayList<Extrusion2D>  path = lc.getPath(p);
			if(path!=null) output.addAll(path);
			int sz = output.size();
			if(sz>0) p = output.get(sz-1).lastPoint();
		}
		return output;
	}
	/**
	 * Indicates that something was wrong with the intersection which forms the very basis of a layer.
	 * @author Nick
	 *
	 */
	public class MalformedLayerException extends Exception{};
}

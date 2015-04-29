package structs;

import java.util.ArrayList;

import process.Flatten;
import process.Infill;
import process.NativeInset;
import process.Order;
import structs.Extrusion2D.ET;
import utils.Utils2D;
import main.Slicer;
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
	protected boolean loopsMade = false;
	
	public Layer(Slicer s, int layerNo){
		this.s=s;
		this.layerNo=layerNo;
		this.offset = layerNo*s.layerHeight;
	}
	protected void makeLoops(){
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
		boolean solid = s.allSolid||(layerNo<s.botLayers || layerNo>=s.topLayerStart);
		if(!s.cross||solid) return Infill.getInfill(s, loops, distance, layerNo,0,0);
		else{
			ArrayList<Extrusion2D> output = Infill.getInfill(s,loops,distance, layerNo, 0,0);
			if(output!=null) output.addAll(Infill.getInfill(s,loops,distance,layerNo,90,0));
			return output;
		}
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
		for(int i= s.OuterFirst ? 0 : s.numShells-1;
		i<s.numShells&&s.OuterFirst || i>=0&&!s.OuterFirst;
		i+= s.OuterFirst ? 1 : -1){
			double dist = (i+0.5)*s.extrusionWidth;
			ArrayList<ArrayList<Extrusion2D>> shells = NativeInset.insetLines(loops, dist,ET.shell);
			if(shells==null) continue;
			for(ArrayList<Extrusion2D> shell : shells){
				if(layerNo%2==0){
					output.addAll(shell);
//					System.out.println("Forward");
				}
				else{
//					System.out.println("Reverse");
					for(int j=shell.size()-1;j>=0;j--){
						output.add(shell.get(j).reverseE());
					}
				}
			}
		}
		return output;
	}
	public ArrayList<Extrusion2D> getPath(Point2D lastPoint){
		double infillDistance = (0.5+s.numShells+s.infillInsetMultiple)*s.extrusionWidth;
		ArrayList<Extrusion2D> infill = s.infillWidth==0 ? null : getInfill(infillDistance); //TODO Something smarter here
		ArrayList<Extrusion2D> shells = getShells();
		ArrayList<Extrusion2D> output = new ArrayList<Extrusion2D>();
		Point2D last = lastPoint;	//End of last segment added to output.
		if(shells!=null){
			for(Extrusion2D e: shells){
				ConnectSplitAppend(s,last,e,output);
				last = e.lastPoint();
			}
		}
		if(infill!=null){
			for(Extrusion2D e: infill){
				ConnectSplitAppend(s,last,e,output);
				last = e.lastPoint();
			}
		}
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
	public static void ConnectSplitAppend(Slicer s, Point2D lp, Extrusion2D e, ArrayList<Extrusion2D> output){
		//If last point isn't the start of e, add a travel between them and retract if necessary.
		Point2D np = e.firstPoint();
		if(!Utils2D.equiv(lp, np)){
			output.addAll((new Extrusion2D(
					lp,
					np,
					lp.distance(np)>s.retractThreshold ? ET.travel : ET.nonretracting)	//Retract is 0, nonretracting is 3.
					).splitExtrusion(s.shape.topo));
		}
		output.addAll(e.splitExtrusion(s.shape.topo));
	}
}

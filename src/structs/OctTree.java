package structs;

import java.util.ArrayList;

import utils.Utils3D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import mesh3d.Tri3D;
/**
 * An octree implementation where all three axes are bounded by min <=  content < max.
 * @author Nick
 *Layout of children in array:
 *4s bit signifies +Z, 2s bit signifies +Y, 1s bit signifies +X.
 * @param <T>
 */
public class OctTree<T> {
	private Locator<T> loc;
	private OctTree<T>[] children;
	private ArrayList<T> contents; //The contents of this tree which cannot be sorted into a subtree.
	private Vector3D min;
	private Vector3D center;
	private Vector3D max;
	public interface Locator<T>{
		/**
		 * Return the index inside ot where input belongs. -1 if input spans
		 * multiple octants. Note that this does not guarantee input is inside ot,
		 * merely that it only contacts 1 octant. eg a ray starting near the center
		 * and shooting off in the --- direction would receive index 0.
		 * @param input The input you intend to sort into your octree.
		 * @param ot The octTree you wish to locate input inside of.
		 * @return A Point3D representing input.
		 */
		public int locate(T input, OctTree<T> ot);
		/**
		 * Checks all 8 octants if !ot.contains(input).
		 * @param input An object of type T.
		 * @param ot The OctTree to work on.
		 * @return an 8 bit value, where the bits mark whether input intersects with
		 * that octant.
		 */
		public int intersectible(T input, OctTree<T> ot);
		/**
		 * Return whether ot fully contains input.
		 * @param input Object of type T
		 * @param ot OctTree instance to check input against.
		 * @return True if ot contains input.
		 */
		public boolean contains(T input, OctTree<T> ot);
	}
	public static class TriLocator implements Locator<Tri3D>{
		public int locate(Tri3D t, OctTree<Tri3D> ot){
			//This loop indexes all three points in the triangle, and returns either their
			//matched index or -1 if any do not match.
			int output = -100; //placeholder value for first loop.
			for(int i=0;i<3;i++){
				int idx = idx(t.getPoint3D(i),ot);
				if(idx==output||output<0) output=idx;	//Check that for the 2nd and 3rd loops, idx== the first loop's idx.
				else return -1;
			}
			return output;
		}
		private int idx(Point3D p,OctTree<Tri3D> ot){
			int idx = 0;
			if(ot.center.getZ()<=p.getZ()) idx = (idx|4);
			if(ot.center.getY()<=p.getY()) idx = (idx|2);
			if(ot.center.getX()<=p.getX()) idx = (idx|1);
			return idx;
			
		}
		public boolean contains(Tri3D t, OctTree<Tri3D> ot){
			//Check that all three points are contained by this OctTree.
			return ot.containsPoint(t.getPoint3D(0))&&ot.containsPoint(t.getPoint3D(1))&&ot.containsPoint(t.getPoint3D(2));
		}
		@Override
		public int intersectible(Tri3D t, OctTree<Tri3D> ot) {
			if(!this.contains(t, ot)) return 255;
			int maxs = 0; //Each bit repesents "is there a point on the + side of this axis?"
			int mins = 0;//Each bit repesents "is there a point on the - side of this axis?"
			//If one half of a given axis isn't hit at all, then all octants in that half can be ignored.
			for(int i=0;i<3;i++){
				int idx = idx(t.getPoint3D(i),ot);
				maxs = (maxs|idx);
				mins = (mins|(~idx&7)); //simple bit mask to get the inverse of just the 3 bit idx.
			}
			//check each octant based on axes info, and output it.
			/*
			 * 0: 000 if min is nonzero, check it.
			 * 1: 001 if min min max is 111, check it.
			 * 2: 010 if min max min is 111, check it.
			 * 3: 011 if min max max is 111, check it.
			 * etc.
			 * For each bit, (i_n AND maxs_n OR !i_n AND mins_n) means we have to check i.
			 * The logic equation below also evals true for i_n AND !mins_n, but this case will
			 * never occur when i_n AND maxs_n doesn't, as !mins_n guarantees maxs_n. Thus it's harmless.
			 * ((i&maxs)|(i^mins))
			 */
			int output=0;
			for(int i=0;i<8;i++){
				if(((i&maxs)|(i^mins))==7) output += 1 << i;
			}
			return output;
			/*
			 *This implementation doesn't allow us to ignore quarters (ie the +X +Y column), nor individual
			 *octants. However, these scenarios are extremely rare for triangles, especially the slice surface (ie, within ~40 deg of flat)
			 *triangles we'll be querying this structure with. 
			 */
		}
	}
	@SuppressWarnings("unchecked")
	public OctTree(Vector3D min, Vector3D max, Locator<T> loc){
//		System.out.println(min+" "+max);
		this.min = min;
		this.max = max;
		this.center = (min.plus(max)).times(0.5);
		this.loc = loc;
		this.children = new OctTree[8];
		this.contents = new ArrayList<T>();
	}
	/**
	 * Attempt to store input in this octtree, return whether input is
	 * within this tree's bounding box.
	 * @param input Content to store.
	 * @return True if input is in the bounding box of this tree.
	 */
	public boolean add(T input){
		if(!loc.contains(input,this)){
			System.out.println(input + " didn't fit inside "+ Utils3D.VectorToStr(min)+" - "+Utils3D.VectorToStr(max));
			return false;
		}
		int idx = loc.locate(input,this);
		//Store in this tree if it spans multiple octants.
		if(idx==-1){
//			System.out.println("Found the right level.");
			contents.add(input);
			return true;
		}
		//store in a child tree otherwise.
		if(children[idx]==null) children[idx]=NewTree(idx);
//		System.out.println("Trying to store in child "+idx);
		if(children[idx].add(input)){
//			System.out.println("Added one successfully!");
			return true;
		} else return false;
	}
	public void addAll(T[] input){
		for(T item: input) this.add(item);
	}
	/**
	 * Generate a new tree in the given octant of this tree.
	 * @param idx Octant index, as specified in class description.
	 * @return A new OctTree<T> instance with the same Locator, in the given octant.
	 */
	private OctTree<T> NewTree(int idx){
		//Checking the bits described in this class's description to figure out corners of the new subtree.
		Vector3D newMin = new Vector3D((idx&1)==1 ? center.getX() : min.getX(),
						(idx&2)==2 ? center.getY() : min.getY(),
						(idx&4)==4 ? center.getZ() : min.getZ());
		Vector3D newMax = newMin.plus(center).minus(min);
		return new OctTree<T>(newMin,newMax,loc);
	}
	/**
	 * Test whether this tree contains point p.
	 * @param p A Point3D
	 * @return True if min<=p<max for all 3 axes.
	 */
	public boolean containsPoint(Point3D p){
		//structured funny to make shortcircuiting handy.
		return this.min.getX()<=p.getX()&&this.max.getX()>p.getX()
				&&this.min.getY()<=p.getY()&&this.max.getY()>p.getY()
				&&this.min.getZ()<=p.getZ()&&this.max.getZ()>p.getZ();
	}
	/**
	 * Apply Locator.contains() to input.
	 * @param input Object to test against Locator.contains() method.
	 * @return Output of Locator.contains() method.
	 */
	public boolean contains(T input){
		return loc.contains(input,this);
	}
	/**
	 * Fetch all the contents of this tree which may intersect input.
	 * @param input An object of type T.
	 * @return An ArrayList of the contents of all subtrees which contain part of input.
	 */
	public ArrayList<T> getIntersectible(T input){
		int idx = loc.locate(input, this);
		ArrayList<T> output = new ArrayList<T>();
		output.addAll(contents);	//Always have to check the contents of this node.
		if(idx!=-1){	//if input fits in a single octant
			if(children[idx]!=null){
				output.addAll(children[idx].getIntersectible(input)); //Return this node's contents and that octant's.
				return output;
			}
			return output;	//Return just the contents of this node, because the octant is empty.
		}
		//if input spans multiple octants, check only the relevant child octants.
		for(int i=0;i<8;i++){
			int inter = loc.intersectible(input, this); //inter is 8 bits representing which octants input spans.
			if(children[i]!=null&&(inter&(1<<i))!=0) output.addAll(children[i].getIntersectible(input));
		}
		return output;
	}
	/**
	 * Move the octTree and its subtrees. DOES NOT AFFECT CONTENTS.
	 * This should only be used in conjunction with an identical move operation
	 * on the contents of this tree.
	 * @param v Vector to move the entire tree by.
	 */
	public void move(Vector3D v){
		this.min = this.min.plus(v);
		this.center = this.center.plus(v);
		this.max = this.max.plus(v);
		for(OctTree<T> o : children){
			if(o!=null) o.move(v);
		}
	}
}

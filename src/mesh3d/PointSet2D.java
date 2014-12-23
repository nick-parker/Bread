package mesh3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import math.geom2d.Point2D;

public class PointSet2D {
	private ArrayList<Point2D> ps;
	private PointCmp c = new PointCmp();
	public PointSet2D(){
		this.ps = new ArrayList<Point2D>();
	}
	private class PointCmp implements Comparator<Point2D>{
		@Override
		public int compare(Point2D o1, Point2D o2) {
			if(o1.getX()<o2.getX()) return -1;
			if(o1.getX()>o2.getX()) return 1;
			return 0;
		}
	}
	public void add(Point2D p){
		ps.add(p);
		Collections.sort(ps,c);
	}
	public void addAll(Collection<Point2D> pSet){
		ps.addAll(pSet);
		Collections.sort(ps,c);
	}
	/**
	 * Get the index which p would be placed at if it was added.
	 * @param p
	 * @return
	 */
	public int getIndex(Point2D p){
		int i=0;
		int j = ps.size()-1;
		while(i<=j){
			int pivot = (i+j)/2;
			if(c.compare(p, ps.get(pivot))<0){
				//p.x<pivot.x therefore p belongs to the left of pivot. 
				j=pivot;
			}
			else i=pivot;	//if pivot.x==p.x, i will get returned.
		}
		return i;
		
	}
	public Point2D get(int i) {
		try{
			return ps.get(i);
		} catch(IndexOutOfBoundsException e){
			return null;
		}
	}
}

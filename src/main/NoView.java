package main;

import java.util.ArrayList;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.Flatten;
import process.Loop;
import process.Order;
import straightskeleton.Corner;
import utils.LoopL;

public class NoView {

	public static void main(String[] args) {
		Model3D m1 = null;
		Surface3D m2 = null;		
		try {
			m1 = Stli.importModel("big.stl", true);
//			m2 = Stli.importSurface("crinkle3.stl", true);
			m2 = SimplePlane.MakePlane(-10, -10, 500, 500, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		m2.move(new Vector3D(0,0,-5.5));
		LineSegment3D[] over = m2.overlap(m1);
		LineSegment2D[] ls = Flatten.FlattenZ(over);
		ArrayList<Loop> loops = Order.ListOrder(ls);
		Point2D[] ps = new Point2D[ls.length];
		int i=0;
		for(Loop lp:loops){
			System.out.println(lp.checkClosure());
			for(LineSegment2D l:lp){
				ps[i]=l.lastPoint();
				i++;
			}
		}
		ArrayList<ArrayList<Point2D>> polies = new ArrayList<ArrayList<Point2D>>();
		for(Loop l:loops) polies.add(l.getPointLoop());
		LoopL<Corner> ssShell = Inset.inset(polies, -1);
		utils.Loop<Corner> l0 = ssShell.get(0);
		Point2D[] disp = new Point2D[16];
		i=0;
		for(Corner c:l0){
			disp[i]=new Point2D(c.x,c.y);
			i++;
		}
		System.out.println("Foo");

	}

}

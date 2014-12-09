package main;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import process.Flatten;
import process.Loop;
import process.NativeInset;
import process.Order;
import straightskeleton.Corner;
import utils.LoopL;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;

public class Main extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1311630393667993010L;
	public Main(){
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
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
//		Point2D[] ps = new Point2D[ls.length];
//		int i=0;
//		for(Loop lp:loops){
//			System.out.println(lp.checkClosure());
//			for(LineSegment2D l:lp){
//				ps[i]=l.lastPoint();
//				i++;
//			}
//		}
//		ArrayList<ArrayList<Point2D>> polies = new ArrayList<ArrayList<Point2D>>();
//		for(Loop l:loops) polies.add(l.getPointLoop());
//		LoopL<Corner> ssShell = Inset.inset(polies, 20);
//		utils.Loop<Corner> l0 = ssShell.get(0);
//		Point2D[] disp = new Point2D[l0.count()];
//		int i=0;
//		for(Corner c:l0){
//			disp[i]=new Point2D(c.x,c.y);
//			i++;
//		}
		ArrayList<ArrayList<Point2D>> output = NativeInset.inset(loops, ls.length, 20);
		Point2D[] disp = output.get(0).toArray(new Point2D[1]);
		ArrayList<ArrayList<Point2D>> output2 = NativeInset.inset(loops, ls.length, 10);
		Point2D[] disp2 = output2.get(0).toArray(new Point2D[1]);
		SimplePolygon2D poly =  new SimplePolygon2D(disp);
		SimplePolygon2D poly2 =  new SimplePolygon2D(disp2);
		poly2.draw(g2);
		poly.draw(g2);
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		JPanel panel = new Main();
		JFrame frame = new JFrame("Main File");
		frame.setContentPane(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}

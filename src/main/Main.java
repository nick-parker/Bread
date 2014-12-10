package main;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import process.Flatten;
import process.Layer;
import process.Loop;
import process.NativeInset;
import process.Order;
import process.Slicer;
import math.geom2d.Point2D;
import math.geom2d.ShapeArray2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.MultiPolygon2D;
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
		Slicer s = null;
		try {
			m1 = Stli.importModel("big.stl", true);
//			m2 = Stli.importSurface("crinkle3.stl", true);
			m2 = SimplePlane.MakePlane(-10, -10, 500, 500, 4);
			s = new Slicer(m1,m2,0.5,1.75,0.4,0.7,200,50,10,0,Math.PI/4);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		m2.move(new Vector3D(0,0,-5.5));
		LineSegment3D[] over = m2.overlap(m1);
		LineSegment2D[] ls = Flatten.FlattenZ(over);
		ArrayList<Loop> loops = Order.ListOrder(ls);
		ArrayList<ArrayList<Point2D>> output = NativeInset.inset(loops, 1);
		MultiPolygon2D m = NativeInset.GetRegion(output);
		m.draw(g2);
		Layer layer1 = new Layer(s,3);
		ArrayList<LineSegment2D> infill = layer1.getInfill(25);
		ShapeArray2D sa2d = new ShapeArray2D<LineSegment2D>(infill);
		sa2d.draw(g2);
		
		Point2D start = new Point2D(300,300);
		start.draw(g2);
		
//		ArrayList<ArrayList<Point2D>> output2 = NativeInset.inset(loops, 10);
//		Point2D[] disp = output.get(0).toArray(new Point2D[1]);
//		Point2D[] disp2 = output2.get(0).toArray(new Point2D[1]);
//		SimplePolygon2D poly =  new SimplePolygon2D(disp);
//		SimplePolygon2D poly2 =  new SimplePolygon2D(disp2);
//		poly2.draw(g2);
//		poly.draw(g2);
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		JPanel panel = new Main();
		JFrame frame = new JFrame("Main File");
		frame.setContentPane(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}

package main;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import process.Flatten;
import process.Loop;
import process.Order;
import math.geom2d.Point2D;
import math.geom2d.circulinear.CirculinearDomain2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Model3D;
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
			m1 = Stli.importModel("model.stl", true);
			m2 = Stli.importSurface("crinkle3.stl", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		m2.move(new Vector3D(0,0,-5.5));
		LineSegment3D[] over = m2.overlap(m1);
		LineSegment2D[] ls = Flatten.FlattenZ(over);
		ArrayList<Loop> loops = Order.ListOrder(ls);
		Point2D[] ps = new Point2D[ls.length];
		int i=0;
		for(Loop lp:loops){
			System.out.println(lp.checkClosure());
			for(LineSegment2D l:lp.getLoop()){
				ps[i]=l.lastPoint();
				i++;
			}
		}
		SimplePolygon2D poly =  new SimplePolygon2D(ps);
		CirculinearDomain2D poly2 = poly.buffer(0);
		poly.draw(g2);
		poly2.draw(g2);
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		JPanel panel = new Main();
		JFrame frame = new JFrame("Main File");
		frame.setContentPane(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}

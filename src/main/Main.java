package main;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import process.Extrusion2D;
import process.Layer;
import process.Slicer;
import math.geom2d.Point2D;
import math.geom2d.ShapeArray2D;
import math.geom2d.line.LineSegment2D;
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
			s = new Slicer(m1,m2,0.5,1.75,0.4,5,200,50,2,4,0,Math.PI/4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Layer layer1 = new Layer(s,2);
		ArrayList<Extrusion2D> infill = layer1.getInfill(20);
		ShapeArray2D<LineSegment2D> sa2d = new ShapeArray2D<LineSegment2D>(infill);
		sa2d.draw(g2);
		ArrayList<Extrusion2D> shells = layer1.getShells();
		ShapeArray2D<LineSegment2D> sa2d2 = new ShapeArray2D<LineSegment2D>(shells);
		sa2d2.draw(g2);
		Point2D start = new Point2D(300,300);
		start.draw(g2);
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		JPanel panel = new Main();
		JFrame frame = new JFrame("Main File");
		frame.setContentPane(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}

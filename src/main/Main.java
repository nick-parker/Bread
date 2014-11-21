package main;


import java.awt.Graphics;
import java.io.IOException;

import javax.swing.JPanel;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.line.StraightLine3D;
import math.geom3d.plane.Plane3D;
import mesh3d.Model3D;
import mesh3d.Stli;
import mesh3d.Surface3D;

public class Main extends JPanel {
	public Main(){
	}
	public void paint(Graphics g){
		
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		Model3D m1 = Stli.importModel("model.stl", true);
		Surface3D m2 = Stli.importSurface("crinkle3.stl", true);
		LineSegment2D[] ls = m2.topology();
		System.out.println("Foo");
	}
}

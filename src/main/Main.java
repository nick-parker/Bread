package main;


import java.awt.Graphics;
import java.io.IOException;

import javax.swing.JPanel;

import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
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
		LineSegment3D[] over = m2.overlap(m1);
		System.out.println("Foo");
	}
}

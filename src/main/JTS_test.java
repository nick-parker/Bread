package main;

import java.io.IOException;

import math.geom3d.Point3D;
import mesh3d.Model3D;
import mesh3d.Tri3D;
import io.Stli;

public class JTS_test {
	public static void main(String[] args) throws IOException{
		Model3D m = Stli.importModel("./Prints/wave.stl",false);
		System.out.println("Imported.");
		Point3D p0 = new Point3D(-5,-5,3);
		Point3D p1 = new Point3D(0,1,3);
		Point3D p2 = new Point3D(1,0,3);
		Tri3D t = new Tri3D(p0,p1,p2);
		System.out.println(t);
//		System.out.println(m.triTree.getIntersectible(t));
		System.out.println(m.intersect(t));
		
	}
}

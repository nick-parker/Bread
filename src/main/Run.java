package main;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import math.geom2d.Point2D;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.NativeInset;
import representation.ClipperJNA;
import structs.Layer;
import structs.Loop;

public class Run {

	public static void main(String[] args) {
		Model3D part = null;
		Surface3D surface = null;		
		Slicer s = null;
		try {
//			m1 = Stli.importModel("ASTM D638-10-1.stl", true);
			part = Stli.importModel("ASTM D638-10-1.stl", true);
//			m1.move(new Vector3D(0,0,0.25));
			surface = Stli.importSurface("20deg.stl", true);
//			m2 = SimplePlane.MakePlane(-200, -200, 200, 200, 4);
			part.move(new Vector3D(20,0,0));
			surface.move(new Vector3D(20,0,0));
			//(Model3D part, Surface3D shape, layerHeight, filD, nozzleD, 
			//extrusionWidth, PrintTemp, xySpeed, zSpeed, numShells, infillWidth, 
			//infillDir, infillAngle, lift, retraction)
			s = new Slicer(part, surface, "config1.txt");
//			s = new Slicer(m1, m2, 0.3, 1.75, 0.4, .55, 200, 40, 40, 0, 0.6, 0, Math.PI/4, 0.15, 0, 50);
		} catch (FileNotFoundException e){
			System.out.println(e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		s.slice("2015-2-4-1155.gcode");
//		surface.setOffset(5.7);
//		Point2D p = new Point2D(6.487060546875, 0.27490234375);
//		Point3D p3 = surface.project(p);
//		System.out.println(p3);
	}
}

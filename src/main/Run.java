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
//			part = Stli.importModel("ASTM D638-10-1.stl", true);
			part = Stli.importModel("squarering.stl", true);
			surface = Stli.importSurface("v.stl", true);
//			surface = SimplePlane.MakePlane(-5, -5, 50, 50, 0);
			part.move(new Vector3D(0,0.1,0));
//			surface.move(new Vector3D(20,0,0));
			s = new Slicer(part, surface, "config1.txt");
		} catch (FileNotFoundException e){
			System.out.println(e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		s.slice("dragon.g");
	}
}

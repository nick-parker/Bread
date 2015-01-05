package main;

import java.io.FileNotFoundException;

import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.Slicer;

public class NoView {

	public static void main(String[] args) {
		Model3D m1 = null;
		Surface3D m2 = null;		
		Slicer s = null;
		try {
			m1 = Stli.importModel("ring.stl", true);
			m1.move(new Vector3D(0,0,0.25));
			m2 = Stli.importSurface("v.stl", true);
//			m2 = SimplePlane.MakePlane(-200, -200, 200, 200, 4);
//			m2.move(new Vector3D(2.5,0,0));
			//(Model3D part, Surface3D shape, layerHeight, filD, nozzleD, 
			//extrusionWidth, PrintTemp, xySpeed, zSpeed, numShells, infillWidth, 
			//infillDir, infillAngle, lift, retraction)
			s = new Slicer(m1, m2, "config1.txt");
		} catch (FileNotFoundException e){
			System.out.println(e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		m2.move(new Vector3D(0,0,0));
		s.slice("yoda2.gcode");
	}
}

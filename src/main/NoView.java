package main;

import java.util.ArrayList;

import math.geom3d.Vector3D;
import mesh3d.Model3D;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.Extrusion2D;
import process.Extrusion3D;
import process.GcodeExport;
import process.Layer;
import process.Reproject;
import process.Slicer;

public class NoView {

	public static void main(String[] args) {
		Model3D m1 = null;
		Surface3D m2 = null;		
		Slicer s = null;
		try {
			m1 = Stli.importModel("model.stl", true);
			m2 = Stli.importSurface("crinkle3.stl", true);
//			m2 = SimplePlane.MakePlane(-10, -10, 500, 500, 4);
			//(Model3D part, Surface3D shape, layerHeight, filD, nozzleD, 
			//extrusionWidth, PrintTemp, speed, numShells, infillWidth, 
			//infillDir, infillAngle, lift)
			s = new Slicer(m1,m2,0.3,1.75,0.4,
					.55,200,30,0,1,
					0,Math.PI/4,0.3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		m2.move(new Vector3D(0.1,0,0));
		s.slice("output.gcode");
//		Layer l1 = new Layer(s,3);
//		Reproject r = new Reproject(l1.offset, s);
//		ArrayList<Extrusion2D> p2 = l1.getPath();
//		ArrayList<Extrusion3D> p3 = r.Proj(p2);
//		System.out.println(p2.size()+" "+p3.size());
//		GcodeExport.CheckContinuity(p3);
//		System.out.println(p3);
		
	}

}

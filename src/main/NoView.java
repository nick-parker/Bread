package main;

import java.util.ArrayList;

import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.Extrusion2D;
import process.Extrusion3D;
import process.Layer;
import process.Reproject;
import process.Slicer;

public class NoView {

	public static void main(String[] args) {
		Model3D m1 = null;
		Surface3D m2 = null;		
		Slicer s = null;
		try {
			m1 = Stli.importModel("big.stl", true);
//			m2 = Stli.importSurface("crinkle3.stl", true);
			m2 = SimplePlane.MakePlane(-10, -10, 500, 500, 4);
			s = new Slicer(m1,m2,0.5,1.75,0.4,5,200,50,2,4,0,Math.PI/4,0.3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		s.slice("output.gcode");
//		Layer l1 = new Layer(s,4);
//		Reproject r = new Reproject(l1.offset, s);
//		ArrayList<Extrusion2D> p2 = l1.getPath();
//		ArrayList<Extrusion3D> p3 = r.Proj(p2);
//		System.out.println(p2.size()+" "+p3.size());
//		System.out.println(p3);
	}

}

package main;

import java.util.ArrayList;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Model3D;
import mesh3d.SimplePlane;
import mesh3d.Stli;
import mesh3d.Surface3D;
import process.Flatten;
import process.Loop;
import process.Order;
import straightskeleton.Corner;
import utils.LoopL;

public class NoView {

	public static void main(String[] args) {
		Model3D m1 = null;
		Surface3D m2 = null;		
		try {
			m1 = Stli.importModel("big.stl", true);
//			m2 = Stli.importSurface("crinkle3.stl", true);
			m2 = SimplePlane.MakePlane(-10, -10, 500, 500, 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(m2.topology().length);

	}

}

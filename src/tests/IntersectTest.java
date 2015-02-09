package tests;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import main.Constants;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;

/**
 * A simple gcode exporter to look at 3D paths.
 * @author Nick
 *
 */
public class IntersectTest {
	PrintWriter w;
	public IntersectTest(String fileName){
		try {
			this.w = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void export(LineSegment3D[] profile){
		for(LineSegment3D l : profile ){
			G1(l.firstPoint(),false);
			G1(l.lastPoint(),true);
		}
	}
	private void G1(Point3D p,boolean extrude){
		if(!extrude){
			w.println("G1 X" + Constants.xyz.format(p.getX())+" Y"+Constants.xyz.format(p.getY())+" Z"+Constants.xyz.format(p.getZ()));
		}
		else{
			w.println("G92 E0");
			w.println("G1 X" + Constants.xyz.format(p.getX())+" Y"+Constants.xyz.format(p.getY())+" Z"+Constants.xyz.format(p.getZ())+" E5");
		}
	}
	public void close(){
		w.close();
	}
}

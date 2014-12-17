package process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import math.geom3d.Point3D;
import mesh3d.Tri3D;

public class GcodeExport {
	PrintWriter w;
	Slicer s;
	public GcodeExport(String fileLoc, Slicer s){
		this.s = s;
		try {
			this.w = new PrintWriter(new FileWriter(fileLoc));
			w.println("Testing the printwriter.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void addLayer(ArrayList<Extrusion3D> path){
		Point3D last = path.get(0).firstPoint();
		double currE = 0;
		zeroE();
		for(Extrusion3D e: path){
			if(!Tri3D.equiv(last,e.firstPoint())){
				System.out.println("Discontinuous path! Something's probably wrong!");
			}
			if(e.ExtrusionType==1||e.ExtrusionType==2){
				//Infill or shell.
				currE +=s.EperL*Tri3D.length(e);
			}
			G1(e.lastPoint(),currE);
		}
	}
	public void writeFromFile(String fileLoc){
		BufferedReader f = null;
		try {
			f = new BufferedReader(new FileReader(fileLoc));
			while(f.ready()){
				w.println(f.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void G1(Point3D p, double e){
		w.println("G1 X"+p.getX()+" Y"+p.getY()+" Z"+p.getZ()+" E"+e);
	}
	private void zeroE(){
		w.println("G92 E0");
	}
	public void close() {
		w.close();
		
	}
}

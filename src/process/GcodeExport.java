package process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import mesh3d.Tri3D;

public class GcodeExport {
	private Point3D last;
	private double currE;
	PrintWriter w;
	Slicer s;
	public GcodeExport(String fileLoc, Slicer s){
		this.s = s;
		this.last = new Point3D(0,0,0);
		try {
			this.w = new PrintWriter(new FileWriter(fileLoc));
//			w.println("Testing the printwriter.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void CheckContinuity(ArrayList<Extrusion3D> path){
		Point3D l = path.get(0).firstPoint();
		for(Extrusion3D e: path){
			if(!Tri3D.equiv(l,e.firstPoint())){
				System.out.println("Discontinuous path! Something's probably wrong!");
				System.out.println("Jump from "+Tri3D.PointToStr(l)+" to "+Tri3D.PointToStr(e.firstPoint()));
			}
			l = e.lastPoint();
		}
	}
	public void addLayer(ArrayList<Extrusion3D> path){
		w.println(";layer");
		last = path.get(0).firstPoint();
		zeroE();
		G1(last);
		for(Extrusion3D e: path){
			if(!Tri3D.equiv(last,e.firstPoint())){
				System.out.println("Discontinuous path! Something's probably wrong!");
				System.out.println("Jump from "+Tri3D.PointToStr(last)+" to "+Tri3D.PointToStr(e.firstPoint()));
			}
			if(e.ExtrusionType==1||e.ExtrusionType==2){
				//Infill or shell.
				currE +=s.EperL*Tri3D.length(e);
			}
			G1(e.lastPoint());
			last = e.lastPoint();
		}
		lift(2);
	}
	/**
	 * Lift the nozzle by i*s.lift. Used before travel moves are expected.
	 * @param i
	 */
	private void lift(int i) {
		last = last.plus(new Vector3D(0,0,i*s.lift));
		G1(last);
		
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
	private void G1(Point3D p){
		w.println("G1 X"+p.getX()+" Y"+p.getY()+" Z"+p.getZ()+" E"+currE);
	}
	private void zeroE(){
		w.println("G92 E0");
		currE = 0;
	}
	public void close() {
		w.close();
		
	}
	public void SetSpeed(double d) {
		w.println("G1 F"+d);
		
	}
	public void setTempAndWait(int printTemp) {
		w.println("M109 S"+printTemp);
		
	}
}

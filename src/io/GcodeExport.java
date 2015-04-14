package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import structs.Extrusion3D;
import structs.Extrusion2D.ET;
import utils.Utils3D;
import main.Constants;
import main.Slicer;
import math.geom3d.Point3D;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
/**
 * A Gcode Export function for use with absolute coordinate modes. The most recent position in XYZE coordinates is stored as
 * state in each instance of the exporter, as well as the location to write the .gcode file and the slicer object which
 * stores settings relevant to E coordinate calculations.
 * @author Nick
 *
 */
public class GcodeExport {
	private Point3D last;
	private double currE;
	private double CurrSpeed = 0;
	//Formatting just matches the conventions set by other slicers for precision in various axes.
	//Cura is compatible with all the popular firmwares, so its conventions are a safe bet.
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
	public GcodeExport(Writer w, Slicer s){
		this.s = s;
		this.last = new Point3D(0,0,0);
		this.w = new PrintWriter(w);
	}
	/**
	 * Debugging function to check that a series of LineSegment3D objects is continuous.
	 * @param path A list of LineSegment3D objects or a subtype.
	 */
	public static void CheckContinuity(ArrayList<? extends LineSegment3D> path){
		Point3D l = path.get(0).firstPoint();
		for(LineSegment3D e: path){
			if(!Utils3D.equiv(l,e.firstPoint())){
				System.out.println("Discontinuous path! Jump from "+Utils3D.PointToStr(l)+" to "+Utils3D.PointToStr(e.firstPoint()));
			}
			l = e.lastPoint();
		}
	}
	/**
	 * Convert the final Extrusion3D path of a layer object to gcode, and append it to the output file.
	 * @param path Layer path to convert
	 */
	public void addLayer(ArrayList<Extrusion3D> path, int layerNo){
		w.println(";layer "+layerNo);
		Extrusion3D prev = path.get(0);
//		System.out.println(Tri3D.PointToStr(last));
//		last = prev.firstPoint();
//		System.out.println(Tri3D.PointToStr(last));
		zeroE();	//Zero E
//		G1(last);	//Travel to the first point directly from wherever it is now.
		for(Extrusion3D e: path){
			if(Utils3D.length(e)<Constants.tol) continue;
			if(!Utils3D.equiv(last,e.firstPoint())){
				System.out.println("Discontinuous path! Jump from "+Utils3D.PointToStr(last)+" to "+Utils3D.PointToStr(e.firstPoint()));
			}
			if(e.ExtrusionType==ET.travel&&prev.ExtrusionType!=ET.travel||e.equals(prev)){
				w.println(";retract");
				retract(true); //pull back
				w.println(";travel");
			}
			if(e.ExtrusionType!=ET.travel&&prev.ExtrusionType==ET.travel){
				w.println(";unretract");
				retract(false); //push out
			}
			if(e.ExtrusionType==ET.infill||e.ExtrusionType==ET.shell){
				//Infill or shell.
				currE +=s.EperL*Utils3D.length(e);
				if(e.ExtrusionType==ET.infill&&prev.ExtrusionType!=ET.infill) w.println(";infill");
				else if(e.ExtrusionType==ET.shell&&prev.ExtrusionType!=ET.shell) w.println(";shell");
			}
			G1(e.lastPoint());
			prev = e;
			last = e.lastPoint();
		}
		lift(s.layerHeight); //Lift by one layer height.
	}
	/**
	 * Encapsulates retraction functionality.
	 * @param pull Whether to pull filament out or push it back in. True retracts.
	 */
	private void retract(boolean pull){
		if(pull){
			if(s.FirmwareRetract) w.println("G10");
			else{
				currE-=s.retraction;
				G1E(s.retractSpeed);
			}
		}
		else{
			if(s.FirmwareRetract) w.println("G11");
			else{
				currE+=s.retraction;
				G1E(s.retractSpeed);
			}
		}
	}
	/**
	 * Lift by distance d.
	 * @param d Distance to lift in +Z direction.
	 */
	private void lift(double d){
		last = last.plus(new Vector3D(0,0,d));
		G1(last);
	}
	/**
	 * Copy the contents of a file into the target file of this exporter. Used for pre-print and post-print commands like
	 * setting coordinate modes and turning off the heaters.
	 * @param fileLoc Location of the file to copy.
	 */
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
	/**
	 * Write a gcode command to move to an absolute position.
	 * @param p
	 */
	private void G1(Point3D p){
		Vector3D mv = new Vector3D(last,p);
		double cos = Vector3D.dotProduct(mv, Constants.zplus)/mv.norm();
		//Limit the speed based on the maximum z speed and XY speeds specified in the slicer object.
		if(mv.norm()==0) SetSpeed(s.zSpeed);
		else if(Math.abs(Math.abs(cos)-1)<Constants.tol) SetSpeed(s.zSpeed);
		else{
			SetSpeed(Math.min(Math.abs(s.zSpeed/cos),s.xySpeed));
		}
		w.println("G1 X"+Constants.xyz.format(p.getX())+" Y"+Constants.xyz.format(p.getY())+" Z"+Constants.xyz.format(p.getZ())+" E"+Constants.ext.format(currE));
	}
	/**
	 * Send a command to go to the current E position without moving XYZ, at the given speed.
	 * @param s Speed to retract, in mm/s
	 */
	private void G1E(double s){
		SetSpeed(s);
		w.println("G1 E"+Constants.ext.format(currE));
	}
	/**
	 * Zero the e value both in this exporter's internal state and in gcode.
	 */
	private void zeroE(){
		w.println("G92 E0");
		currE = 0;
	}
	public void close() {
		w.close();
		
	}
	/**
	 * Set the 3D movement speed in gcode.
	 * @param d Speed in mm/s
	 */
	public void SetSpeed(double d) {
		if(CurrSpeed!=d){
			CurrSpeed = d;
			w.println("G1 F"+Constants.xyz.format(d*60));
		}		
	}
	/**
	 * Set the temperature and wait until the temperature is reached before executing more
	 * commands.
	 * @param printTemp Temperature in degrees C
	 */
	public void setTempAndWait(int printTemp) {
		w.println("M109 S"+printTemp);
		
	}
}
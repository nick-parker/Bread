package process;

import java.util.ArrayList;

import structs.Extrusion3D;
import structs.Extrusion2D.ET;
import main.Constants;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;

/**
 * A method to compensate for the difference between uphill and downhill printing.
 * The trailing edge of the nozzle becomes the relevant point for compressing the material
 * onto the previous layer.
 * @author Nick
 *
 */
public class NozzleOffset {
	/**
	 * Calculate the tangent of the angle between e and the XY plane.
	 * @param e A LineSegment3D subclass.
	 * @return The tangent as a double.
	 */
	private static double getTan(LineSegment3D e){
		return del(e,2)/Math.sqrt(Math.pow(del(e,0),2)+Math.pow(del(e,1),2));
	}
	private static double del(LineSegment3D e, int axis){
		if(axis==0){
			return e.lastPoint().getX()-e.firstPoint().getX();
		}
		if(axis==1){
			return e.lastPoint().getY()-e.firstPoint().getY();
		}
		if(axis==2){
			return e.lastPoint().getZ()-e.firstPoint().getZ();
		}
		throw new UnsupportedOperationException();
	}
	/**
	 * Offset an Extrusion3D based on its direction and angle.
	 * @param e Extrusion3D to offset
	 * @param rad Radius of extruder nozzle + nozzle wall thickness
	 * @return A new Extrusion3D instance appropriately shifted.
	 */
	public static Extrusion3D offset(Extrusion3D e, double rad){
		if(Math.sqrt(Math.pow(del(e,0),2)+Math.pow(del(e,1),2))<Constants.tol) return e;
		return e.move(0, 0, -rad*getTan(e));
	}
	public static ArrayList<Extrusion3D> offset(ArrayList<Extrusion3D> es, double rad){
		ArrayList<Extrusion3D> output = new ArrayList<Extrusion3D>();
		Point3D last = es.get(0).firstPoint();
		for(Extrusion3D e : es){
			Extrusion3D off = offset(e,rad);
			output.add(new Extrusion3D(last,off.firstPoint(),ET.nonretracting));
			output.add(off);
			last = off.lastPoint();
		}
		output.add(new Extrusion3D(last,es.get(es.size()-1).lastPoint(),ET.nonretracting));
		return output;
	}
}

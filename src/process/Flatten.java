package process;

import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;

public class Flatten {
	/**
	 * @param input An array of LineSegment3D objects
	 * @return An array representing the XY projection of input.
	 */
	public static LineSegment2D[] FlattenZ(LineSegment3D[] input){
		LineSegment2D[] output = new LineSegment2D[input.length];
		for(int i=0;i<input.length;i++){
			output[i]=FlattenZ(input[i]);
		}
		return output;
	}
	/**
	 * 
	 * @param input A LineSegment3D object
	 * @return The projection of input into the XY plane
	 */
	public static LineSegment2D FlattenZ(LineSegment3D input){
		Point3D a = input.firstPoint();
		Point3D b = input.lastPoint();
		return new LineSegment2D(
				new Point2D(a.getX(),a.getY()),
				new Point2D(b.getX(),b.getY()));
	}
}

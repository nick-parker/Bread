package structs;

import structs.Extrusion2D.ET;
import utils.Utils3D;
import main.Constants;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;
import math.geom3d.Vector3D;

/**
 * ExtrusionType marks the type of extrusion this line represents.
 * 0 - travel
 * 1 - infill
 * 2 - shell
 * 3 - nonretracting travel
 * @author Nick
 *
 */
public class Extrusion3D extends LineSegment3D {
	public final ET ExtrusionType;
	public Vector3D n0;
	public Vector3D n1;
	public Extrusion3D(Point3D p0, Point3D p1, ET ExtrusionType) {
		super(p0, p1);
		this.ExtrusionType = ExtrusionType;
		this.n0 = Constants.zero;
		this.n1 = Constants.zero;
	}
	public Extrusion3D(Point6D p0, Point6D p1, ET ExtrusionType) {
		super(p0, p1);
		this.ExtrusionType = ExtrusionType;
		this.n0 = p0.normal;
		this.n1 = p1.normal;
	}
	public Extrusion3D reverse() {
		return new Extrusion3D(this.lastPoint(),this.firstPoint(),ExtrusionType);
	}
	public Extrusion3D move(double x, double y, double z){
		return new Extrusion3D(
				new Point6D(x1+x,y1+y,z1+z, n0),
				new Point6D(x2+x,y2+y,z2+z, n1),
				ExtrusionType);
	}
	@Override
	public String toString(){
		return "E3D("+Utils3D.PointToStr(this.firstPoint()) + ", " +
				Utils3D.PointToStr(this.lastPoint()) + " " +
				this.ExtrusionType + " " + Utils3D.VectorToStr(this.n0) + " " +
				Utils3D.VectorToStr(this.n1) + ")";
	}
	@Override
	public Point6D firstPoint() {
		return new Point6D(x1,y1,z1,n0);
	}
	@Override
	public Point6D lastPoint() {
		return new Point6D(x2,y2,z2,n1);
	}
	public Extrusion3D reverseE(){
		return new Extrusion3D(lastPoint(),firstPoint(),ExtrusionType);
	}
}

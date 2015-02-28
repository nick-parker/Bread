package structs;

import structs.Extrusion2D.ET;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.Tri3D;

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

	public Extrusion3D(Point3D p1, Point3D p2, ET ExtrusionType) {
		super(p1, p2);
		this.ExtrusionType = ExtrusionType;
	}

	public Extrusion3D reverse() {
		return new Extrusion3D(this.lastPoint(),this.firstPoint(),ExtrusionType);
	}
	public Extrusion3D move(double x, double y, double z){
		return new Extrusion3D(
				new Point3D(x1+x,y1+y,z1+z),
				new Point3D(this.x2+x,y2+y,z2+z),
				this.ExtrusionType);
	}
	@Override
	public String toString(){
		return "E3D("+Tri3D.PointToStr(this.firstPoint())+", "+Tri3D.PointToStr(this.lastPoint())+" "+this.ExtrusionType+")";
	}
	public Extrusion3D reverseE(){
		return new Extrusion3D(lastPoint(),firstPoint(),ExtrusionType);
	}
}

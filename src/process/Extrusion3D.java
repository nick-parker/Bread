package process;

import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;

public class Extrusion3D extends LineSegment3D {
	public final int ExtrusionType;

	public Extrusion3D(Point3D p1, Point3D p2, int ExtrusionType) {
		super(p1, p2);
		this.ExtrusionType = ExtrusionType;
	}

	public Extrusion3D reverse() {
		return new Extrusion3D(this.lastPoint(),this.firstPoint(),ExtrusionType);
	}
	
}

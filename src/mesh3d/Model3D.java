package mesh3d;

import main.Constants;
import math.geom3d.Point3D;
import math.geom3d.line.LineSegment3D;

public class Model3D extends Mesh3D{
	public Model3D(Tri3D[] ts) {
		this.tris = ts;
	}
	/**
	 * @param p Point3D to test
	 * @return Whether p is within or on the surface of this mesh.
	 */
	public boolean encloses(Point3D p) {
		int hits = 0;
		LineSegment3D test = new LineSegment3D(p,Constants.farPoint);
		for(Tri3D t:tris){
			if(t.SegmentIntersection(test)!=null){
				hits++;
			}
		}
		return hits%2==1;
	}
	@Override
	public boolean closed(){return false;};

}

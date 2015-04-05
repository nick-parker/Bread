package mesh3d;

import math.geom3d.Box3D;

/**
 * A Surface3D with Z thickness added, used in multilayershape printing.
 * @author Nick
 *
 */
public class CollisionMesh extends Mesh3D {
	private double min;
	private double max;
	public CollisionMesh(Tri3D[] ts) {
		this.tris = ts;
		this.min = boundingBox().getMinZ();
		this.max = boundingBox().getMaxZ();
	}
	@Override
	public boolean closed() {
		return true;
	}
	public boolean QuickIntersect(CollisionMesh cm){
		if(this.min>cm.max||this.max<cm.min) return false;
		return this.intersect(cm);
	}
	public boolean QuickIntersect(Mesh3D s){
		Box3D b = s.boundingBox();
		if(this.min>b.getMaxZ()||this.max<b.getMinZ()) return false;
		return this.intersect(s);
	}

}

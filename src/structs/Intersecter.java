package structs;

import java.util.ArrayList;

import utils.Utils3D;
import main.Constants;
import math.geom3d.Vector3D;
import math.geom3d.line.LineSegment3D;
import mesh3d.CollisionMesh;
import mesh3d.Model3D;
import mesh3d.Surface3D;
import mesh3d.Tri3D;

/**
 * Generates the final domains for multiple layer shapes using Clipper's boolean operations,
 * and priorities assigned to the layer shapes. This implementation assumes that no layer shapes
 * have open edges inside the part.
 * @author Nick
 *
 */
public class Intersecter {
	ArrayList<LayerShape> shapes;
	Model3D part;
	
	/**
	 * Initialize an Intersector with a single layer shape covering the entire part, then
	 * add more layer shapes afterwards. First shape must be a full part cover to
	 * ensure the whole part gets printed.
	 * 
	 * Assumes baseShape is lined up already.
	 * @param part Model to print.
	 * @param baseShape First layer shape, domain must encompass part.
	 */
	public Intersecter(Model3D part, Surface3D baseShape, Vector3D position, double ZRange){
		this.part = part;
		this.shapes = new ArrayList<LayerShape>();
		this.shapes.add(new LayerShape(0,baseShape,position,ZRange));
	}
	
	public void AddShape(int priority, Surface3D shape, Vector3D initialPos, double ZRange){
		shapes.add(new LayerShape(priority, shape,initialPos,ZRange));
	}
	
	private class LayerShape{
		int priority;
		Surface3D shape;
		Vector3D initialPos;
		double ZRange;
		CollisionMesh cm;
		private LayerShape(int priority, Surface3D shape, Vector3D initialPos, double ZRange){
			this.priority = priority;
			this.shape = shape;
			this.initialPos = initialPos;
			this.ZRange = ZRange;
			cm = MakeCM();
		}
		private CollisionMesh MakeCM(){
			ArrayList<Tri3D> ts = new ArrayList<Tri3D>();
			Tri3D[] tris = shape.getTris();
			for(Tri3D t: tris) ts.add(t);
			Vector3D extrude = new Vector3D(0,0,ZRange);
			ArrayList<LineSegment3D> perims = shape.getPerimeters();
			for(LineSegment3D l : perims){
				Tri3D[] pair = Utils3D.extrude(l,extrude);
				ts.add(pair[0]);
				ts.add(pair[1]);
			}
			return new CollisionMesh(ts.toArray(new Tri3D[ts.size()]));
		}
	}
}

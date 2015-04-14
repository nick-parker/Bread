package process;

import java.util.ArrayList;
import java.util.List;

import representation.ClipperJNA;
import representation.Domain;
import representation.IntPoint;
import structs.Extrusion2D;
import structs.Loop;
import structs.Extrusion2D.ET;
import utils.Utils2D;
import main.Constants;
import math.geom2d.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.MultiPolygon2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;
import com.vividsolutions.jts.operation.buffer.BufferOp;
/**
 * Wrapper for the C++ library Clipper, which I've packaged into ClipperLib2.dll with a pretty ugly little string interface.
 * @author Nick
 *
 */
public class NativeInset {
	private static GeometryFactory gf = new GeometryFactory();
//	static boolean UseClipperLib = false;
	/**
	 * Inset the provided paths by the given distance d.
	 * @param loops An ArrayList of Loop objects passed out of the Order function
	 * @param d A distance in mm to inset the loops.
	 * @return A new set of loops represented as an arraylist of arraylists of points.
	 */
	public static ArrayList<ArrayList<Point2D>> inset(ArrayList<Loop> loops, double d){
//		if(UseClipperLib){
//			ArrayList<ArrayList<Point2D>> points = new ArrayList<ArrayList<Point2D>>();
//			for(Loop l:loops){
//				if(!l.checkClosure()) System.out.println("Unclosed loop in NativeInset");
//				points.add(l.getPointLoop());
//			}
//			ArrayList<ArrayList<Point2D>> output = inset(d,points);
//			System.out.println(output);
//			return output;
//		}
		return jtsInset(-d,loops);
	}
	public static ArrayList<ArrayList<Point2D>> inset(double d, ArrayList<ArrayList<Point2D>> ps){
		Domain LoopDom = new Domain(ps);
		Domain output = Domain.parse(ClipperJNA.inset(LoopDom.toString(),(int)Math.round(d*-1*IntPoint.conv)));
//		System.out.println(output);
		return output==null ? null : output.conv();
	}
	public static ArrayList<ArrayList<Point2D>> jtsInset(double d, ArrayList<Loop> ls){
		MultiPolygon mp = ToMultiPoly(ls);
		BufferOp bf = new BufferOp(mp);
		bf.setQuadrantSegments(5);
		Geometry g = bf.getResultGeometry(d);
//		ArrayList<Polygon> polies = new ArrayList<Polygon>();
		@SuppressWarnings("unchecked")
		List<Polygon> polies = (List<Polygon>)PolygonExtracter.getPolygons(g);
		ArrayList<ArrayList<Point2D>> output = new  ArrayList<ArrayList<Point2D>>();
		for(Polygon p : polies){
			int num = p.getNumInteriorRing();
			output.add(conv(p.getExteriorRing()));
			for(int i=0;i<num;i++)output.add(conv(p.getInteriorRingN(i)));
		}
		if(output.size()==0) return null;
		for(ArrayList<Point2D> ps : output){
			if(ps.size()==0) continue;
			for(int i=0;i<ps.size()-1;i++){
				try{ while(Utils2D.equiv(ps.get(i), ps.get(i+1))) ps.remove(i);
				} catch(IndexOutOfBoundsException e){
					break;
				}
			}
		}
		return output;
	}
	private static ArrayList<Point2D> conv(LineString lr){
		ArrayList<Point2D> output = new ArrayList<Point2D>();
		for(Coordinate c : lr.getCoordinates()) output.add(new Point2D(c.x,c.y));
//		System.out.println(output);
		output.remove(output.size()-1);
		return output;
	}
	private static Coordinate conv(Point2D p){
		return new Coordinate(p.getX(),p.getY());
	}
	private static LinearRing conv(Loop l){
		if(l.getLength()<3) return null;
		Coordinate[] cs = new Coordinate[l.getLength()+1];
		for(int i=0;i<cs.length-1;i++) cs[i]=conv(l.get(i).firstPoint());
		cs[cs.length-1] = cs[0];
		return gf.createLinearRing(cs);
	}
	private static MultiPolygon ToMultiPoly(ArrayList<Loop> ls){
		ArrayList<Loop> outers = new ArrayList<Loop>();
		@SuppressWarnings("unchecked")
		ArrayList<Loop> inners = (ArrayList<Loop>) ls.clone();
		for(Loop l : ls) if(!l.hole()){
			outers.add(l);
			inners.remove(l);
		}
		ArrayList<ArrayList<LinearRing>> holes = new ArrayList<ArrayList<LinearRing>>();
		while(holes.size()<outers.size()) holes.add(new ArrayList<LinearRing>());
		for(Loop il : inners){
			double smallestArea = 1e10;
			int smallestIndex = 0;
			for(int i=0;i<outers.size();i++){
				Loop out = outers.get(i);
				if(out.contains(il)&&out.area()<smallestArea) smallestIndex=i;
			}
			holes.get(smallestIndex).add(conv(il));
		}
		Polygon[] ps = new Polygon[outers.size()];
		for(int i=0;i<ps.length;i++){
			ps[i] = gf.createPolygon(conv(outers.get(i)),null); //(holes.size()>i ? (LinearRing[])holes.get(i).toArray() : null)
		}
		return gf.createMultiPolygon(ps);
	}
	public static MultiPolygon2D GetRegion(ArrayList<ArrayList<Point2D>> regionPs){
		ArrayList<LinearRing2D> rs = new ArrayList<LinearRing2D>();
		for(ArrayList<Point2D> r:regionPs){
			rs.add(new LinearRing2D(r));
		}
		return new MultiPolygon2D(rs);
	}
	public static ArrayList<ArrayList<Extrusion2D>> insetLines(ArrayList<Loop> loops, double d, ET type){
		ArrayList<ArrayList<Point2D>> ps = inset(loops,d);
		if(ps==null) return null;
		ArrayList<ArrayList<Extrusion2D>> output = new ArrayList<ArrayList<Extrusion2D>>();
		for(ArrayList<Point2D> a: ps){
			ArrayList<Extrusion2D> thisLoop = new ArrayList<Extrusion2D>();
			for(int i=0;i<a.size()-1;i++){
				Extrusion2D add = new Extrusion2D(a.get(i),a.get(i+1),type);
				if(add.length()>Constants.tol) thisLoop.add(add);
			}
			Extrusion2D add = new Extrusion2D(a.get(a.size()-1),a.get(0),type);
			if(add.length()>Constants.tol) thisLoop.add(add);
			output.add(thisLoop);
		}
		return output;
	}
}


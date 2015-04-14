package main;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;

public class JTS_test {
	public static void main(String[] args){
		Coordinate c0 = new Coordinate(0.0,0.0);
		Coordinate c1 = new Coordinate(0.0,20.0);
		Coordinate c2 = new Coordinate(20.0,20.0);
		Coordinate c3 = new Coordinate(20.0,0.0);
		Coordinate[] cs = new Coordinate[]{c0,c1,c2,c3,c0};
		GeometryFactory gf = new GeometryFactory();
		System.out.println(gf.getPrecisionModel());
		LinearRing lr = gf.createLinearRing(cs);
		Coordinate h0 = new Coordinate(4,4);
		Coordinate h1 = new Coordinate(8,4);
		Coordinate h2 = new Coordinate(8,8);
		Coordinate h3 = new Coordinate(4,8);
		Coordinate[] hs = new Coordinate[]{h0,h1,h2,h3,h0};
		LinearRing[] holes = new LinearRing[]{gf.createLinearRing(hs)};
		Polygon p = gf.createPolygon(lr, holes);
		BufferOp bf = new BufferOp(p);
		bf.setQuadrantSegments(1);
		Geometry lr2 = bf.getResultGeometry(-1);
		System.out.println(lr2);
	}
}

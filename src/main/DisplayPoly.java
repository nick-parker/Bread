package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import math.geom2d.AffineTransform2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.circulinear.CirculinearShape2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.SimplePolygon2D;
import mesh3d.Model3D;
import mesh3d.Stli;
import mesh3d.Surface3D;

public class DisplayPoly extends JPanel{
	public DisplayPoly(){	
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		try {
			Model3D m1 = Stli.importModel("model.stl", true);
			Surface3D m2 = Stli.importSurface("crinkle4.stl", true);
			LineSegment2D[] ls = m2.topology();
			for(LineSegment2D l:ls){
				l.draw(g2);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public final static void main(String[] args){
		JPanel panel = new DisplayPoly();
		JFrame frame = new JFrame("JavaGeom Demo");
		frame.setContentPane(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}

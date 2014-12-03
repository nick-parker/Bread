package main;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import math.geom2d.line.LineSegment2D;
import mesh3d.Stli;
import mesh3d.Surface3D;

public class DisplayPoly extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 824956457560589150L;
	public DisplayPoly(){	
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D) g;
		
		try {
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

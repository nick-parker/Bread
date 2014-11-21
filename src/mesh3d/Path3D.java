package mesh3d;

import java.util.ArrayList;
import java.util.List;

import math.geom3d.line.LineSegment3D;

public class Path3D{
	List<LineSegment3D> path;
	public Path3D(){
		path = new ArrayList<LineSegment3D>();
	}
	/**
	 * @return Whether this path is closed.
	 */
	public boolean closed(){
		for(int i=0;i<path.size()-1;i++){
			if(path.get(i).lastPoint().distance(path.get(i+1).firstPoint())>Constants.tol){
				return false;
			}
		}
		if(path.get(path.size()-1).lastPoint().distance(path.get(0).firstPoint())>Constants.tol){
			return false;
		}
		return true;
	}
}

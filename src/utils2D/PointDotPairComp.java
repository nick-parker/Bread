package utils2D;

import java.util.Comparator;

public class PointDotPairComp implements Comparator<PointDotPair>{
	@Override
	public int compare(PointDotPair arg0, PointDotPair arg1) {
		if(arg0.dot<arg1.dot) return -1;
		if(arg0.dot>arg1.dot) return 1;
		return 0;
	}
	
}
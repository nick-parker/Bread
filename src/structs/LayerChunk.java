package structs;

import java.util.ArrayList;
import java.util.Collection;

import main.Slicer;

public class LayerChunk extends Layer{
	public final Loop rim;
	public LayerChunk(Slicer s, int layerNo, Loop rim) {
		super(s, layerNo);
		this.rim = rim;
		this.loops = new ArrayList<Loop>();
		this.loops.add(rim);
	}
	@Override
	protected void makeLoops(){
//		if(!loopsMade) System.out.println("Chunk lacks loops, things are about to break.");
	}
	public void addLoop(Loop l){
		if(l.hole()) loops.add(l);
	}
	public void addLoops(Collection<Loop> ls){
		for(Loop l : ls){
			addLoop(l);
		}
		loopsMade=true;
	}
}

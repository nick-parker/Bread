package process;

import java.io.IOException;

import mesh3d.Model3D;
import mesh3d.Stli;
import mesh3d.Surface3D;

/**
 * Stores info on a slice job and the layers which make it up.
 */
public class Slicer {
	public final Model3D part;
	public final Surface3D shape;
	public final double layerHeight;
	public final double filD;
	public final double nozzleD;
	public final double extrusionWidth;
	public final int printTemp;
	public final double Speed;
	public final double infillWidth;
	//Inputs below are optional, above are mandatory.
	public double shellSpeed;
	public double bottomSpeedMult;
	public int DegreesPerFacet; //Used for turning the circular corners of inset shells into linear ones.	
	public Slicer(String part, String shape, double layerHeight, double filD, double nozzleD, double extrusionWidth,
			int printTemp, double speed, double infillWidth) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.extrusionWidth = extrusionWidth;
		this.printTemp = printTemp;
		this.Speed = speed;
		this.infillWidth = infillWidth;
		this.layerHeight = layerHeight;
		this.part = Stli.importModel(part, true);
		this.shape = Stli.importSurface(shape, true);
	}
}

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
	public final int printTemp;
	public final double Speed;
	//Inputs below are optional, above are mandatory.
	double shellSpeed;
	double bottomSpeedMult;
	public Slicer(String part, String shape, double layerHeight, double filD, double nozzleD, int printTemp, double speed) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.printTemp = printTemp;
		this.Speed = speed;
		this.layerHeight = layerHeight;
		this.part = Stli.importModel(part, true);
		this.shape = Stli.importSurface(shape, true);
	}
}

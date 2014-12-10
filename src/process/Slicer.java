package process;

import java.io.IOException;

import mesh3d.Model3D;
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
	public final int numShells;
	public final double infillWidth;
	public final double infillDir;	//Direction of infill on layer 0;
	public final double infillAngle;	//Amount to change infill direction each layer, radians CW.
	//Inputs below are optional, above are mandatory.
	public double shellSpeedMult = 1;
	public double bottomSpeedMult = 1;
	public int bottomLayerCount = 0;
	public double infillInsetMultiple = 0;	//Number of extrusion widths to inset infill beyond innermost shell
	public Slicer(Model3D part, Surface3D shape, double layerHeight, double filD, double nozzleD, double extrusionWidth,
			int printTemp, double speed, int numShells, double infillWidth, double infillDir, double infillAngle) throws IOException{
		this.filD = filD;
		this.nozzleD = nozzleD;
		this.extrusionWidth = extrusionWidth;
		this.printTemp = printTemp;
		this.Speed = speed;
		this.numShells = numShells;
		this.infillWidth = infillWidth;
		this.layerHeight = layerHeight;
		this.infillDir = infillDir;
		this.infillAngle = infillAngle;
		this.part = part;
		this.shape = shape;
	}
}

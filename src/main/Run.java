package main;

import io.Stli;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import mesh3d.Model3D;
import mesh3d.Surface3D;

/**
 * The main class of TreeSlicer. Execute is probably the method you want to call.
 * @author Nick
 *
 */
public class Run {

	public static void main(String[] args) throws IOException {
		if(args.length==4){
			execute(args[0],args[1],args[2],args[3]);
		} else {
			System.out.println("Usage: [Part.stl] [Surface.stl] [config.txt] [output path.g]");
		}
	}
	private static void execute(String partStr, String surfaceStr, String configStr, String output){
		execute(partStr,surfaceStr,configStr,output,"");
	}
	private static void execute(String partStr, String surfaceStr, String configStr, String output, String suppStr){
		Model3D part = null;
		Model3D support = null;
		Surface3D surface = null;		
		Slicer s = null;
		try {
			part = Stli.importModel(partStr, false);
			surface = Stli.importSurface(surfaceStr, false);
			s = new Slicer(part, surface, configStr);
			if(s.EnableSupport){
				support = Stli.importModel(suppStr, false);
				s.AddSupport(support);
			}
		} catch (FileNotFoundException e){
			System.out.println(e.getMessage());
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		s.slice(output+".g");
	}
	/**
	 * Slice from three strings and output the resulting gcode to a string.
	 * @param partStr ASCII .stl file contents representing the print
	 * @param surfaceStr ASCII .stl file contents representing the layer shape
	 * @param configStr contents of config.txt file.
	 * @param target The target filename.
	 * @return What would be the contents of the .gcode file.
	 */
	public static void slice(String partStr, String surfaceStr, String configStr, String target){
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(target+".g"));
			Model3D part = null;
			Surface3D surface = null;		
			try{
				part = Stli.importModel(new StringReader(partStr));
				surface = Stli.importSurface(new StringReader(surfaceStr));			
			} catch(FileNotFoundException e){
				System.out.println(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			Slicer s = new Slicer(part, surface, new StringReader(configStr));
			s.slice(pw);
			pw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

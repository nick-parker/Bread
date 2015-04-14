package main;

import io.Stli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import mesh3d.Model3D;
import mesh3d.Surface3D;

/**
 * The main class of TreeSlicer. Slice is probably the method you want to call.
 * @author Nick
 *
 */
public class Run {

	public static void main(String[] args) throws IOException {
		execute("./Prints/DBZ.stl","./Prints/DBS.stl", "DBZNew");
//		BufferedReader brp = new BufferedReader(new FileReader(args[0]));
//		BufferedReader brs = new BufferedReader(new FileReader(args[1]));
//		BufferedReader brc = new BufferedReader(new FileReader(args[2]));
//		String part = "", config = "", surface = "";
//		String line;
//		while((line=brp.readLine())!=null){
//			part = part + line+'\n';
//		}
//		while((line=brs.readLine())!=null){
//			surface = surface + line+'\n';
//		}
//		while((line=brc.readLine())!=null){
//			config = config+line+'\n';
//		}
//		brp.close();
//		brs.close();
//		brc.close();
//		slice(part, surface, config,"output");
	}
	private static void execute(String partStr, String surfaceStr, String output){
		Model3D part = null;
		Surface3D surface = null;		
		Slicer s = null;
		try {
			part = Stli.importModel(partStr, false);
			surface = Stli.importSurface(surfaceStr, false);
			s = new Slicer(part, surface, "config1.txt");
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

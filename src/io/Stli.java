package io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import main.Constants;
import math.geom3d.Point3D;
import mesh3d.Model3D;
import mesh3d.Surface3D;
import mesh3d.Tri3D;

public class Stli {
	private static Tri3D[] importMesh(String fileLoc) throws IOException{
		return importMesh(new FileReader(fileLoc));
	}
	private static Tri3D[] importMesh(Reader r) throws IOException{
		try{
			BufferedReader f = new BufferedReader(r);
			ArrayList<Tri3D> tris = new ArrayList<Tri3D>();
			Point3D[] ps = new Point3D[]{Constants.origin,Constants.origin,Constants.origin};
			int i=0;
			boolean inFace = false;
			String line;
			while((line = f.readLine()) != null){
				String[] lineArray = line.trim().split(" ");
				switch(lineArray[0]){
				case "facet":
					if(inFace){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					inFace = true;
					break;
				case "vertex":
					if(!inFace){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					ps[i] = new Point3D(Double.parseDouble(lineArray[1]),Double.parseDouble(lineArray[2]),Double.parseDouble(lineArray[3]));
					i++;
					break;
				case "endfacet":
					if(!inFace||i!=3){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					Tri3D a = new Tri3D(ps[0],ps[1],ps[2]);
					if(!a.degenerate())tris.add(a);
					ps = new Point3D[]{Constants.origin,Constants.origin,Constants.origin};
					i=0;
					inFace = false;
					break;
				default:
					//Do nothing in other cases.
					break;
				}
			}
			f.close();
			return tris.toArray(new Tri3D[tris.size()]);
		} catch(NumberFormatException e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Import a binary .stl file
	 * @param fileName File to import
	 * @return An array of Tri3D objects representing the .stl
	 */
	private static Tri3D[] importBinMesh(String fileLoc){
		try{
			InputStream is = new FileInputStream(fileLoc);
			return importBinMesh(is);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	private static Tri3D[] importBinMesh(InputStream is){
		try{
			is.skip(84); //toss the header
			ArrayList<Tri3D> ts = new ArrayList<Tri3D>();
			boolean done=false;
			while(true){
				is.skip(12);
				double[] ps = new double[9];
				for(int j=0;j<9;j++){
					byte[] bytes = new byte[4];
					if(is.read(bytes)<=0){
						done = true;
						break;
					};
					double d = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN ).getFloat();
					ps[j]=d;
				}
				Tri3D a = t3d(ps);
				if(!a.degenerate())ts.add(a);
				is.skip(2);
				if(done) break;
			}
			is.close();
			return ts.toArray(new Tri3D[ts.size()]);
		} catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	private static Tri3D t3d(double[] coords){
		return new Tri3D(
				new Point3D(coords[0],coords[1],coords[2]),
				new Point3D(coords[3],coords[4],coords[5]),
				new Point3D(coords[6],coords[7],coords[8])
				);
	}
	public static Model3D importModel(String fileName, boolean ascii) throws IOException{
		if(ascii) return new Model3D(importMesh(fileName));
		return new Model3D(importBinMesh(fileName));
	}
	public static Surface3D importSurface(String fileName, boolean ascii) throws IOException{
		if(ascii) return new Surface3D(importMesh(fileName));
		return new Surface3D(importBinMesh(fileName));
	}
	/**
	 * Only supports ASCII right now.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Model3D importModel(Reader file) throws IOException{
		return new Model3D(importMesh(file));
	}
	/**
	 * Only supports ASCII right now.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Surface3D importSurface(Reader file) throws IOException{
		return new Surface3D(importMesh(file));
		
	}
	public static Model3D importModel(byte[] stl){
		InputStream is = new ByteArrayInputStream(stl);
		return new Model3D(importBinMesh(is));
	}
	public static Surface3D importSurface(byte[] stl){
		InputStream is = new ByteArrayInputStream(stl);
		return new Surface3D(importBinMesh(is));
		
	}
}

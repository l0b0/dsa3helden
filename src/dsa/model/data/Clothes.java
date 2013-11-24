package dsa.model.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Clothes {

	private Clothes() {
		
	}
	
	private HashMap<String, Cloth> allClothes = new HashMap<String, Cloth>();
	
	public Cloth getCloth(String name) {
		if (allClothes.containsKey(name)) {
			return allClothes.get(name);
		}
		return null;
	}
	
	public void addCloth(Cloth cloth) {
		if (Things.getInstance().getThing(cloth.getName()) == null) {
			throw new IllegalStateException("Create thing first!");
		}
		allClothes.put(cloth.getName(), cloth);
	}
	
	public void removeCloth(String name) {
		if (allClothes.containsKey(name)) {
			allClothes.remove(name);
		}
	}
	
	private static Clothes sInstance = null;
	
	public static Clothes getInstance() {
		if (sInstance == null) {
			sInstance = new Clothes();
		}
		return sInstance;
	}
	
	public void readFromFile(String fileName, boolean add) throws IOException {
		if (!add)
			allClothes.clear();
	    File file = new File(fileName);
	    if (!file.exists()) return;
		int lineNr = 0;
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
		try {
			++lineNr;
			String line = in.readLine();
			while (line != null) {
				Cloth cloth = Cloth.read(line, lineNr);
				if (Things.getInstance().getThing(cloth.getName()) == null) {
					throw new IOException("Zeile " + lineNr + ": Unbekannte Kleidung.");
				}
				addCloth(cloth);
				++lineNr;
				line = in.readLine();
			}
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public void writeUserDefinedCloths(String filename) throws IOException {
	    ArrayList<Cloth> userDefinedClothes = new ArrayList<Cloth>();
	    for (Cloth c : allClothes.values()) {
	      Thing t = Things.getInstance().getThing(c.getName());
	      if (t.isUserDefined()) {
	    	  userDefinedClothes.add(c);
	      }
	    }
	    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1")));
	    try {
	      for (Cloth c : userDefinedClothes) {
	    	  String line = c.write();
	    	  out.println(line);
	      }
	      out.flush();
	    }
	    finally {
	      out.close();
	    }		
	}
}

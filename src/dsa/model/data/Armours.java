/*
    Copyright (c) 2006 [Joerg Ruedenauer]
  
    This file is part of Heldenverwaltung.

    Heldenverwaltung is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Heldenverwaltung is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.model.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class Armours {

  private static Armours instance = new Armours();

  private final TreeMap<String, Armour> theArmours;

  public static Armours getInstance() {
    return instance;
  }

  public Armour getArmour(String name) {
    return theArmours.containsKey(name) ? theArmours.get(name) : null;
  }

  public void addArmour(Armour armour) {
    theArmours.put(armour.getName(), armour);
  }
  
  public void removeArmour(String name) {
    theArmours.remove(name);
  }

  public Armour[] getAllArmours() {
    Armour[] temp = new Armour[theArmours.size()];
    return theArmours.values().toArray(temp);
  }

  public void saveUserDefinedArmours(String filename) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    out.println("*** User-Defined Armours ***");
    out.println(2); // version    
    for (Armour armour : theArmours.values()) {
      if (armour.isUserDefined()) {
        out.println(armour.getName());
        out.println(armour.getRS());
        out.println(armour.getBE());
        out.println(armour.getWeight());
        out.println(armour.getWorth());
      }
    }
    out.flush();
    out.close();
  }
  
  public void loadUserDefinedArmours(String filename) throws IOException {
    File file = new File(filename);
    if (!file.exists()) return;
    internalLoadFile(filename, true);
  }

  public void loadFile(String filename) throws IOException {
    theArmours.clear();
    internalLoadFile(filename, false);
  }
  
  private void internalLoadFile(String filename, boolean userDefined) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    try {
      int lineNr = 0;
      String line = in.readLine();
      lineNr++;
      if (line == null) return;
      boolean hasVersion = line.equals("*** User-Defined Armours ***");
      int version = 1;
      if (hasVersion) {
        line = in.readLine();
        lineNr++;
        try {
          version = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException("Falsches Dateiformat in " + filename);
        }
        line = in.readLine();
        lineNr++;
      }
      while (line != null) {
        String name = line;
        line = in.readLine();
        lineNr++; // rs
        int rs = 1, be = 1;
        if (line == null) {
          throw new IOException("EOF statt RS!");
        }
        try {
          rs = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr + ": RS keine Zahl!");
        }
        if ((rs < 0) || (rs > 20)) {
          throw new IOException("Zeile " + lineNr + ": RS falsch!");
        }
        line = in.readLine();
        lineNr++;
        if (line == null) throw new IOException("EOF statt BE!");
        try {
          be = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr + ": BE keine Zahl!");
        }
        if ((be < 0) || (be > 20)) {
          throw new IOException("Zeile " + lineNr + ": BE falsch!");
        }
        line = in.readLine();
        lineNr++; // Gewicht
        if (line == null) throw new IOException("EOF statt Gewicht!");
        int weight = 0;
        try {
          weight = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr + ": Gewicht keine Zahl!");
        }
        int worth = 0;
        if ( (userDefined && hasVersion && version > 1) || (!userDefined)) {
          line = in.readLine();
          lineNr++;
          if (line == null) throw new IOException("EOF statt Wert!");
          try {
            worth = Integer.parseInt(line);
          }
          catch (NumberFormatException e) {
            throw new IOException("Zeile " + lineNr + ": Wert keine Zahl!");
          }
        }
        Armour armour = new Armour(name, rs, be, weight, worth, userDefined);
        theArmours.put(name, armour);
        line = in.readLine();
        lineNr++;
      }
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }

  private Armours() {
    theArmours = new TreeMap<String, Armour>();
  }

}

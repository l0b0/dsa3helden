/*
    Copyright (c) 2006-2008 [Joerg Ruedenauer]
  
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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;

public class Opponents {
  
  private HashMap<String, Opponent> opponents = new HashMap<String, Opponent>();
  
  private boolean changed = false;
  
  public Opponents() {
  }
  
  private static Opponents opponentsDB = new Opponents();
  
  private static boolean dbInitialized = false;
  
  public static Opponents getOpponentsDB() {
    if (!dbInitialized) {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
          + java.io.File.separator;
      String userDataPath = dsa.util.Directories.getUserDataPath();
      try {
        opponentsDB.readFromFile(dirPath + "Gegner.dat", false);
        opponentsDB.readFromFile(userDataPath + "Eigene_Gegner.dat", true);
        dbInitialized = true;
      }
      catch (java.io.IOException e) {
        javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
            "Fehler beim Laden der Gegnerdaten", javax.swing.JOptionPane.ERROR_MESSAGE);        
      }
    }
    return opponentsDB;
  }
  
  public boolean wasChanged() {
    if (changed) return true;
    for (Opponent o : opponents.values()) {
      if (o.wasChanged()) return true;
    }
    return false;
  }
  
  public int getNrOfOpponents() {
    return opponents.size();
  }
  
  public Set<String> getOpponentNames() {
    return opponents.keySet();
  }
  
  public Opponent getOpponent(String name) {
    if (opponents.containsKey(name)) {
      return opponents.get(name);
    }
    else return null;
  }
  
  public void addOpponent(Opponent opponent) {
    if (getOpponent(opponent.getName()) == null) {
      opponents.put(opponent.getName(), opponent);
      changed = true;
    }
  }
  
  public void removeOpponent(String name) {
    if (opponents.containsKey(name)) {
      opponents.remove(name);
      changed = true;
    }
  }
  
  public void readFromFile(String filename, boolean userDefined) throws IOException {
    if (userDefined) {
      File file = new File(filename);
      if (!file.exists()) return;
    }
    BufferedReader in = new BufferedReader(new FileReader(filename));
    try {
      readFromFile(in, filename, userDefined);
    }
    finally {
      if (in != null) in.close();
    }
  }
  
  public void readFromFile(BufferedReader in, String filename, boolean userDefined) throws IOException {
    String line = in.readLine();
    int version = 1;
    try {
      version = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Falsche Version in " + filename);
    }
    line = in.readLine();
    while (line != null && !line.equals("-- End Opponents --")) {
      try {
        Opponent o = Opponent.createOpponent(line, version, userDefined);
        if (opponents.get(o.getName()) == null)
          opponents.put(o.getName(), o);
      }
      catch (ParseException e) {
        throw new IOException("Formatfehler in " + filename + ":\n" + line + " falsch, Fehlernummer " + e.getErrorOffset());
      }
      line = in.readLine();
    }
    changed = false;    
  }
  
  public void writeToFile(String fileName) throws IOException {
    writeToFile(fileName, false);
  }
  
  public void writeToFile(String fileName, boolean onlyUserDefined) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    try {
      writeToFile(out, onlyUserDefined);
      out.flush();
    }
    finally {
      if (out != null) out.close();
    }    
  }
  
  public void writeToFile(PrintWriter out) throws IOException {
    writeToFile(out, false);
  }
  
  public void writeToFile(PrintWriter out, boolean onlyUserDefined) throws IOException {
    final int VERSION = 7;
    out.println(VERSION);
    for (Opponent o : opponents.values()) {
      if (!onlyUserDefined || o.isUserDefined()) {
        out.println(o.writeToLine(VERSION));
      }
    }
    out.println("-- End Opponents --");
  }

}

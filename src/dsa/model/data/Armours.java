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
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.model.data;

import java.util.TreeMap;

import java.io.*;

public class Armours {

  private static Armours instance;

  private TreeMap<String, Armour> armours;

  public static Armours getInstance() {
    if (instance == null) instance = new Armours();
    return instance;
  }

  public Armour getArmour(String name) {
    return armours.containsKey(name) ? armours.get(name) : null;
  }

  public void addArmour(Armour armour) {
    armours.put(armour.getName(), armour);
  }

  public Armour[] getAllArmours() {
    Armour[] temp = new Armour[armours.size()];
    return armours.values().toArray(temp);
  }

  public void saveUserDefinedArmours(String filename) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    for (Armour armour : armours.values()) {
      if (armour.isUserDefined()) {
        out.println(armour.getName());
        out.println(armour.getRS());
        out.println(armour.getBE());
        out.println(armour.getWeight());
      }
    }
    out.flush();
    out.close();
  }
  
  public void loadUserDefinedArmours(String filename) throws IOException {
    internalLoadFile(filename, true);
  }

  public void loadFile(String filename) throws IOException {
    armours.clear();
    internalLoadFile(filename, false);
  }
  
  private void internalLoadFile(String filename, boolean userDefined) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    int lineNr = 0;
    String line = in.readLine();
    lineNr++;
    while (line != null) {
      String name = line;
      line = in.readLine();
      lineNr++; // rs
      int rs = 1, be = 1;
      if (line == null) throw new IOException("EOF statt RS!");
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
      Armour armour = new Armour(name, rs, be, weight);
      armours.put(name, armour);
      line = in.readLine();
      lineNr++;
    }
  }

  private Armours() {
    armours = new TreeMap<String, Armour>();
  }

}

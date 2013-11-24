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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import java.io.*;

public class Shields {

  private HashMap<String, Shield> shields = new HashMap<String, Shield>();

  private ArrayList<String> names = new ArrayList<String>();

  private static Shields instance = null;

  public static Shields getInstance() {
    if (instance == null) {
      instance = new Shields();
    }
    return instance;
  }

  public List<String> getAvailableShields() {
    return Collections.unmodifiableList(names);
  }

  public Shield getShield(String name) {
    return shields.get(name);
  }

  public List<Shield> getAllShields() {
    ArrayList<Shield> s = new ArrayList<Shield>();
    for (String n : names)
      s.add(shields.get(n));
    return s;
  }

  private int parseInt(String s, int lineNr) throws IOException {
    try {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": Zahl erwartet!");
    }
  }

  public void readFile(String fileName) throws IOException {
    int lineNr = 0;
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    try {
      String line = in.readLine();
      lineNr++;
      while (line != null) {
        StringTokenizer t = new StringTokenizer(line, ";");
        if (t.countTokens() != 9)
          throw new IOException("Zeile" + lineNr + ": Falsches Format!");
        String name = t.nextToken();
        int paMod = parseInt(t.nextToken(), lineNr);
        int paMod2 = parseInt(t.nextToken(), lineNr);
        int atMod = parseInt(t.nextToken(), lineNr);
        int beMod = parseInt(t.nextToken(), lineNr);
        int fkMod = parseInt(t.nextToken(), lineNr);
        int bf = parseInt(t.nextToken(), lineNr);
        int weight = parseInt(t.nextToken(), lineNr);
        int worth = parseInt(t.nextToken(), lineNr);
        Shield shield = new Shield(name, atMod, paMod, paMod2, beMod, fkMod,
            bf, weight, worth);
        shields.put(name, shield);
        names.add(name);
        line = in.readLine();
        lineNr++;
      }
    }
    catch (IOException e) {
      throw new IOException(fileName + ", " + e.getMessage());
    }
    finally {
      in.close();
    }
  }

}

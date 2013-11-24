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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Region {

  private final String name;

  private final ArrayList<String> talents = new ArrayList<String>();

  private final ArrayList<Integer> mods = new ArrayList<Integer>();

  public Region(File file) throws IOException {
    int lineNr = 0;
    String fileName = file.getName();
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      lineNr++;
      String line = in.readLine();
      if (line == null)
        throw new IOException("Unerwartetes Dateiende in " + fileName + "!");
      name = line;
      line = in.readLine();
      lineNr++;
      while (line != null) {
        if (line.equals("")) {
          line = in.readLine();
          lineNr++;
          continue;
        }
        StringTokenizer st = new StringTokenizer(line, ":");
        if (st.countTokens() != 2) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Falsches Format!");
        }
        String talent = st.nextToken().trim();
        try {
          String m = st.nextToken().trim();
          if (m.charAt(0) == '+') m = m.substring(1);
          int mod = Integer.parseInt(m);
          talents.add(talent);
          mods.add(mod);
        }
        catch (NumberFormatException e) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Falsches Format!");
        }
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

  public String getName() {
    return name;
  }

  public ArrayList<String> getModifiedTalents() {
    ArrayList<String> result = new ArrayList<String>();
    result.addAll(talents);
    return result;
  }

  public ArrayList<Integer> getModifications() {
    ArrayList<Integer> result = new ArrayList<Integer>();
    result.addAll(mods);
    return result;
  }

}

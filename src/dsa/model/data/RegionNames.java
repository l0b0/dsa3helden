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

public class RegionNames {

  private String region;

  private String language;

  private final ArrayList<String> wFirstNames = new ArrayList<String>();

  private final ArrayList<String> lastNames = new ArrayList<String>();

  private final ArrayList<String> mFirstNames = new ArrayList<String>();

  private final ArrayList<String> mSecondNames = new ArrayList<String>();

  private final ArrayList<String> wSecondNames = new ArrayList<String>();

  private final ArrayList<String> nobleLastNames = new ArrayList<String>();

  public ArrayList<String> getLastNames() {
    return lastNames;
  }

  public ArrayList<String> getMFirstNames() {
    return mFirstNames;
  }

  public ArrayList<String> getMSecondNames() {
    return mSecondNames;
  }

  public ArrayList<String> getNobleLastNames() {
    return nobleLastNames;
  }

  public String getRegion() {
    return region;
  }

  public ArrayList<String> getWFirstNames() {
    return wFirstNames;
  }

  public ArrayList<String> getWSecondNames() {
    return wSecondNames;
  }

  public String getLanguage() {
    return language;
  }

  RegionNames() {
  }

  public void readFromFile(File file) throws IOException {
    lastNames.clear();
    mFirstNames.clear();
    wFirstNames.clear();
    mSecondNames.clear();
    wSecondNames.clear();
    nobleLastNames.clear();
    String fileName = file.getName();
    region = fileName.substring(0, fileName.indexOf('.'));
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      ArrayList<String> current = lastNames;
      String line = in.readLine();
      if (line == null)
        throw new IOException("Unexpected end of file " + file.getName() + "!");
      language = line;
      line = in.readLine();
      while (line != null) {
        if (line.startsWith("---Nachnamen"))
          current = lastNames;
        else if (line.startsWith("---MVornamen"))
          current = mFirstNames;
        else if (line.startsWith("---WVornamen"))
          current = wFirstNames;
        else if (line.startsWith("---MBeinamen"))
          current = mSecondNames;
        else if (line.startsWith("---WBeinamen"))
          current = wSecondNames;
        else if (line.startsWith("---Adelig"))
          current = nobleLastNames;
        else
          current.add(line);
        line = in.readLine();
      }
    }
    finally {
      in.close();
    }
  }
}

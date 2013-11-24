/*
    Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Regions {

  private static Regions instance = new Regions();
  
  private static boolean dataRead = false;

  public static Regions getInstance() {
    if (!dataRead) {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
        + java.io.File.separator;
      try {
        instance.readFiles(dirPath + "Regionen");
        dataRead = true;
      }
      catch (java.io.IOException e) {
        javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
            "Fehler beim Laden der Regionen", javax.swing.JOptionPane.ERROR_MESSAGE);        
      }
    }
    return instance;
  }

  private final ArrayList<String> regionNames = new ArrayList<String>();

  private final HashMap<String, Region> regions = new HashMap<String, Region>();

  public void readFiles(String directory) throws IOException {
    File dir = new File(directory);
    File[] files = dir.listFiles();
    for (File f : files) {
      if (f.getName().endsWith(".dat")) {
        Region region = new Region(f);
        regionNames.add(region.getName());
        regions.put(region.getName(), region);
      }
    }
    Collections.sort(regionNames);
  }

  public String[] getRegions() {
    String[] regionsArray = new String[regionNames.size()];
    return regionNames.toArray(regionsArray);
  }

  public Region getRegion(String name) {
    return regions.get(name);
  }
}

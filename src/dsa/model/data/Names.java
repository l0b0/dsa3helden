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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Names {

  private final HashMap<String, RegionNames> regionNames;

  public RegionNames getRegionNames(String region) {
    return regionNames.get(region);
  }

  private static Names instance = new Names();

  public static Names getInstance() {
    return instance;
  }

  private Names() {
    regionNames = new HashMap<String, RegionNames>();
  }

  public void readFiles(String directory) throws IOException {
    File dir = new File(directory);
    regionNames.clear();
    if (!dir.exists() || !dir.isDirectory()) return;
    for (File file : dir.listFiles()) {
      if (file.getName().endsWith(".dat")) {
        RegionNames r = new RegionNames();
        r.readFromFile(file);
        regionNames.put(r.getRegion(), r);
      }
    }
  }

}

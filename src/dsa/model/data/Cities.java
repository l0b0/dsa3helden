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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Cities {

  public static Cities getInstance() {
    return instance;
  }

  private static Cities instance = new Cities();

  private final ArrayList<String> regions;

  private final HashMap<String, ArrayList<String>> cities;

  private final HashMap<String, String> nameRegions;

  private Cities() {
    regions = new ArrayList<String>();
    cities = new HashMap<String, ArrayList<String>>();
    nameRegions = new HashMap<String, String>();
  }

  public List<String> getRegions() {
    return Collections.unmodifiableList(regions);
  }

  public List<String> getCities(String region) {
    if (cities.containsKey(region)) {
      return Collections.unmodifiableList(cities.get(region));
    }
    else
      return new ArrayList<String>();
  }

  public String getNameRegion(String city) {
    for (String region : cities.keySet()) {
      for (String c : cities.get(region)) {
        if (c.equals(city)) {
          return nameRegions.get(region);
        }
      }
    }
    return "Gareth";
  }

  public void readCities(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    try {
      String line = in.readLine();
      if (line == null || !line.startsWith("---"))
        throw new IOException("Falsches Format in " + fileName + "!");
      String region = line.substring(3);
      regions.add(region);
      ArrayList<String> temp = new ArrayList<String>();
      line = in.readLine();
      if (line == null)
        throw new IOException("Unexpected end of file " + fileName + "!");
      nameRegions.put(region, line);
      while (line != null) {
        if (line.startsWith("---")) {
          cities.put(region, temp);
          temp = new ArrayList<String>();
          region = line.substring(3);
          regions.add(region);
        }
        else {
          temp.add(line);
        }
        line = in.readLine();
      }
      cities.put(region, temp);
    }
    finally {
      in.close();
    }
  }

}

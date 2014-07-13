/*
 Copyright (c) 2006-2009 [Joerg Ruedenauer]
 
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Tradezones {

  public Tradezone[] getTradezones() {
    Tradezone[] result = new Tradezone[zones.size()];
    return zones.values().toArray(result);
  }
  
  public String[] getTradezoneIDs() {
    String[] result = new String[zones.size()];
    zones.keySet().toArray(result);
    return result;
  }
  
  public Tradezone getTradezone(String id) {
    return zones.get(id);
  }

  public static final class BorderInfo implements Comparable<BorderInfo> {
    int nrOfLandBorders;
    int nrOfSeaBorders;
    
    final String id;
    
    public String getSource() {
      return id;
    }
    
    public int getNrOfLandBorders() {
      return nrOfLandBorders;
    }
    
    public int getNrOfSeaBorders() {
      return nrOfSeaBorders;
    }
    
    public int getDistance() {
      return nrOfLandBorders * 3 + nrOfSeaBorders;
    }
    
    BorderInfo(String id) {
      this.id = id;
      nrOfLandBorders = 0;
      nrOfSeaBorders = 0;
    }
    
    public BorderInfo(String id, int nrOfLandBorders, int nrOfSeaBorders) {
      this.id = id;
      this.nrOfLandBorders = nrOfLandBorders;
      this.nrOfSeaBorders = nrOfSeaBorders;
    }

    public int compareTo(BorderInfo other) {
      int result = getDistance() - other.getDistance();
      if (result == 0) {
        // prefer the one with fewer borders
        result = nrOfLandBorders + nrOfSeaBorders - other.nrOfLandBorders - other.nrOfSeaBorders;
      }
      if (result == 0) {
        // prefer the one with fewer land borders
        result = nrOfLandBorders - other.nrOfLandBorders;
      }
      return result;
    }
    
  }
  

  public BorderInfo getTradeRoute(String source, String target) {
    if (source.equals(target)) {
      return new BorderInfo(target);
    }
    
    if (!zones.containsKey(source) || !zones.containsKey(target)) {
      throw new IllegalArgumentException();
    }
    
    // Dijkstra algorithm
    // distances: all borders, weighted by land / sea
    HashMap<String, BorderInfo> distances = new HashMap<String, BorderInfo>();
    // temporary set of candidate nodes
    HashMap<String, BorderInfo> candidates = new HashMap<String, BorderInfo>();
    // for fast / easy calculation of nearest candidate
    ArrayList<BorderInfo> candidates2 = new ArrayList<BorderInfo>();
    
    // initialization
    Tradezone sourceZone = zones.get(source);
    distances.put(source, new BorderInfo(source));
    for (String zone : sourceZone.getBorders()) {
      BorderInfo info = new BorderInfo(zone);
      if (sourceZone.isSeaBorder(zone)) {
        info.nrOfSeaBorders++;
      }
      else {
        info.nrOfLandBorders++;
      }
      candidates.put(zone, info);
      candidates2.add(info);
    }
    
    // loop until destination is reached
    do {
      if (candidates2.isEmpty()) {
        // no way from source to target
        return null;
      }
      // select smallest candidate
      java.util.Collections.sort(candidates2);
      BorderInfo nextEntry = candidates2.get(0);
      
      if (nextEntry.id.equals(target)) {
        // arrived at destination!
        return nextEntry;
      }
      
      // move from candidates to visited
      candidates.remove(nextEntry.id);
      candidates2.remove(0);
      distances.put(nextEntry.id, nextEntry);
      
      // for all zones bordering to the newly visited ...
      Tradezone nextZone = zones.get(nextEntry.id);
      for (String zone : nextZone.getBorders()) {
        boolean seaBorder = nextZone.isSeaBorder(zone);
        BorderInfo nextCandidate = new BorderInfo(zone);
        nextCandidate.nrOfLandBorders = nextEntry.nrOfLandBorders;
        nextCandidate.nrOfSeaBorders = nextEntry.nrOfSeaBorders;
        if (seaBorder) {
          nextCandidate.nrOfSeaBorders++;
        }
        else {
          nextCandidate.nrOfLandBorders++;
        }
        // if already visited
        if (distances.containsKey(nextCandidate.id)) {
          // fixed distance must be smaller than the new way
          assert(distances.get(nextCandidate.id).compareTo(nextCandidate) < 0);
          continue;
        }
        // if already in the candidates set
        BorderInfo oldCandidate = candidates.get(nextCandidate.id);
        if (oldCandidate != null) {
          if (nextCandidate.compareTo(oldCandidate) < 0) {
            // faster way found, switch
            candidates2.remove(oldCandidate);
            candidates.put(nextCandidate.id, nextCandidate);
            candidates2.add(nextCandidate);
          }
        }
        else {
          // not yet seen
          candidates.put(nextCandidate.id, nextCandidate);
          candidates2.add(nextCandidate);
        }
      } // end loop around neighbors of last picked
    } 
    while (true);
 }
  
  public void readFile(String filename) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "ISO-8859-1"));;
    int lineNr = 1;
    String line = in.readLine();
    testEmpty(line);
    while (!"---".equals(line)) {
      StringTokenizer tok = new StringTokenizer(line, ";");
      if (tok.countTokens() != 2) {
        throw new IOException("Zeile " + lineNr + ": Syntaxfehler");
      }
      String id = tok.nextToken();
      String name = tok.nextToken();
      zones.put(id, new Tradezone(id, name));
      ++lineNr;
      line = in.readLine();
      testEmpty(line);
    }
    do {
      ++lineNr;
      line = in.readLine();
      if (line == null) break;
      StringTokenizer tok = new StringTokenizer(line, ",");
      if (tok.countTokens() != 3) {
        throw new IOException("Zeile " + lineNr + ": Syntaxfehler");
      }
      String id1 = tok.nextToken();
      String id2 = tok.nextToken();
      boolean isSeaBorder = tok.nextToken().equals("1");
      if (!zones.containsKey(id1)) {
        throw new IOException("Zeile " + lineNr + ": Unbekannte Handelszone '" + id1 + "'");
      }
      if (!zones.containsKey(id2)) {
        throw new IOException("Zeile " + lineNr + ": Unbekannte Handelszone '" + id2 + "'");
      }
      if (id1.equals(id2)) {
        throw new IOException("Zeile " + lineNr + ": Grenze zwischen zwei gleichen Zonen");
      }
      zones.get(id1).addBorder(id2, isSeaBorder);
      zones.get(id2).addBorder(id1, isSeaBorder);
    }
    while (true);
  }
  
  private void testEmpty(String line) throws IOException {
    if (line == null || "".equals(line)) {
      throw new IOException("Unerwartetes Dateiende beim Lesen der Handelszonen");
    }
  }
  
  public static Tradezones getInstance() {
    if (sInstance == null) {
      sInstance = new Tradezones();
    }
    return sInstance;
  }
  
  private Tradezones() {
    zones = new HashMap<String, Tradezone>();
  }
  
  private static Tradezones sInstance = null;
  
  private HashMap<String, Tradezone> zones;
}

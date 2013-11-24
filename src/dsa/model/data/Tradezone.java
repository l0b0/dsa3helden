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

import java.util.HashMap;
import java.util.Set;

public class Tradezone {

  Tradezone(String id, String name) {
    this.id = id;
    this.name = name;
    this.borders = new HashMap<String, Boolean>();
  }
  
  public final String getID() { return id; }
  
  public final String getName() { return name; }
  
  void addBorder(String id, boolean isSeaBorder) {
    borders.put(id, isSeaBorder);
  }
  
  Set<String> getBorders() {
    return java.util.Collections.unmodifiableSet(borders.keySet());
  }
  
  boolean isSeaBorder(String id) {
    return borders.get(id);
  }
  
  private final String id;
  private final String name;
  
  private HashMap<String, Boolean> borders;
}

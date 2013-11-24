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
package dsa.model;

import java.io.IOException;
import java.util.StringTokenizer;

public class FarRangedFightParams implements Cloneable {

  public FarRangedFightParams() {
    distance = 20;
    size = 3;
    movement = 0;
    sight = 0;
    wind = 0;
    infights = 0;
    mode = 1;
    changed = false;
  }
  
  private int parseInt(String token) throws IOException {
    try {
      return Integer.parseInt(token);
    }
    catch (NumberFormatException e) {
      throw new IOException("Fernkampfparameter falsch: " + e.getMessage());
    }
  }
  
  public FarRangedFightParams(String serializedForm) throws IOException {
    StringTokenizer tokenizer = new StringTokenizer(serializedForm, "/");
    if (tokenizer.countTokens() < 7) throw new IOException("Fernkampfparameter falsch");
    distance = parseInt(tokenizer.nextToken());
    size = parseInt(tokenizer.nextToken());
    movement = parseInt(tokenizer.nextToken());
    sight = parseInt(tokenizer.nextToken());
    wind = parseInt(tokenizer.nextToken());
    infights = parseInt(tokenizer.nextToken());
    mode = parseInt(tokenizer.nextToken());
    changed = false;
  }
  
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  
  public String writeToString()
  {
    changed = false;
    return "" + distance + "/" + size + "/" + movement + "/" + sight
      + "/" + wind + "/" + infights + "/" + mode;
  }
  
  public boolean isChanged() { 
    return changed;
  }
  
  private boolean changed;
  
  private int distance;
  private int size;
  private int sight;
  private int movement;
  private int wind;
  private int infights;
  private int mode;
  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    if (this.distance == distance) return;
    this.distance = distance;
    changed = true;
  }

  public int getInfights() {
    return infights;
  }

  public void setInfights(int infights) {
    if (this.infights == infights) return;
    this.infights = infights;
    changed = true;
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    if (this.mode == mode) return;
    this.mode = mode;
    changed = true;
  }

  public int getMovement() {
    return movement;
  }

  public void setMovement(int movement) {
    if (this.movement == movement) return;
    this.movement = movement;
    changed = true;
  }

  public int getSight() {
    return sight;
  }

  public void setSight(int sight) {
    if (this.sight == sight) return;
    this.sight = sight;
    changed = true;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    if (this.size == size) return;
    this.size = size;
    changed = true;
  }

  public int getWind() {
    return wind;
  }

  public void setWind(int wind) {
    if (this.wind == wind) return;
    this.wind = wind;
    changed = true;
  }
}

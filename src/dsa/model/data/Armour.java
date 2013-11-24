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

/**
 */
public class Armour {

  String name;
  int RS, BE; int weight;
  boolean userDefined;

  public Armour(String aName, int aRS, int aBE, int aWeight) {
    this(aName, aRS, aBE, aWeight, true);
  }

  Armour(String aName, int aRS, int aBE, int aWeight, boolean ud) {
    name = aName;
    RS = aRS;
    BE = aBE;
    weight = aWeight;
    userDefined = ud;
  }
  
  boolean isUserDefined() { 
    return userDefined;
  }

  /**
   * @return
   */
  public int getBE() {
    return BE;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
  public int getRS() {
    return RS;
  }

  public int getWeight() {
    return weight;
  }

  /**
   * @param i
   */
  public void setBE(int i) {
    BE = i;
  }

  /**
   * @param string
   */
  public void setName(String string) {
    name = string;
  }

  /**
   * @param i
   */
  public void setRS(int i) {
    RS = i;
  }

}

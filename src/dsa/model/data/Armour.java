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

  int rs, be;

  int weight;
  
  int worth;

  boolean userDefined;

  public Armour(String aName, int aRS, int aBE, int aWeight, int aWorth) {
    this(aName, aRS, aBE, aWeight, aWorth, true);
  }

  Armour(String aName, int aRS, int aBE, int aWeight, int aWorth, boolean ud) {
    name = aName;
    rs = aRS;
    be = aBE;
    weight = aWeight;
    worth = aWorth;
    userDefined = ud;
  }

  public boolean isUserDefined() {
    return userDefined;
  }

  /**
   * @return
   */
  public int getBE() {
    return be;
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
    return rs;
  }

  public int getWeight() {
    return weight;
  }
  
  public int getWorth() {
    return worth;
  }

  /**
   * @param i
   */
  public void setBE(int i) {
    be = i;
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
    rs = i;
  }
  
  public void setWorth(int i) {
    worth = i;
  }
  
  public void setWeight(int i) {
    weight = i;
  }

}

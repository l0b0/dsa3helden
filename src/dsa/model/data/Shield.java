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

public class Shield {

  private String name;

  private int atMod;

  private int paMod;

  private int paMod2;

  private int beMod;

  private int fkMod;

  private int bf;

  private int weight;

  private int worth;

  private boolean userDefined;

  public Shield(String n, int a, int p, int p2, int be, int fk, int bf, int w,
      int m) {
    this(n, a, p, p2, be, fk, bf, w, m, true);
  }

  Shield(String n, int a, int p, int p2, int be, int fk, int bf, int w, int m,
      boolean ud) {
    name = n;
    atMod = a;
    paMod = p;
    paMod2 = p2;
    beMod = be;
    fkMod = fk;
    this.bf = bf;
    weight = w;
    worth = m;
    userDefined = ud;
  }

  public int getAtMod() {
    return atMod;
  }

  public int getBeMod() {
    return beMod;
  }

  public int getBF() {
    return bf;
  }

  public int getFkMod() {
    return fkMod;
  }

  public String getName() {
    return name;
  }

  public int getPaMod() {
    return paMod;
  }

  public int getPaMod2() {
    return paMod2;
  }

  public int getWeight() {
    return weight;
  }

  public int getWorth() {
    return worth;
  }

  public boolean isUserDefined() {
    return userDefined;
  }

  public void setAtMod(int atMod) {
    this.atMod = atMod;
  }

  public void setBeMod(int beMod) {
    this.beMod = beMod;
  }

  public void setBF(int abf) {
    this.bf = abf;
  }

  public void setFkMod(int fkMod) {
    this.fkMod = fkMod;
  }

  public void setPaMod(int paMod) {
    this.paMod = paMod;
  }

  public void setPaMod2(int paMod2) {
    this.paMod2 = paMod2;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public void setWorth(int worth) {
    this.worth = worth;
  }
}

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
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.model;

import java.util.StringTokenizer;

import dsa.control.Dice;

public class DiceSpecification implements Cloneable {

  private int nrOfDices;

  private int diceSize;

  private int fixedPoints;

  public String toString() {
    String ret = "";
    if (nrOfDices > 0) {
      ret += nrOfDices + "W" + diceSize;
    }
    if (fixedPoints > 0 && ret.length() > 0) {
      ret += "+";
    }
    if (fixedPoints != 0 || ret.length() == 0) ret += fixedPoints;
    return ret;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  private DiceSpecification() {
  }

  public static DiceSpecification parse(String text) /*throws NumberFormatException*/ {
    DiceSpecification ds = new DiceSpecification();
    ds.fixedPoints = 0;
    ds.diceSize = 6;
    ds.nrOfDices = 0;
    StringTokenizer t = new StringTokenizer(text, "+");
    while (t.hasMoreTokens()) {
      String subElement = t.nextToken();
      if (subElement.indexOf('W') != -1) {
        if (subElement.charAt(0) == 'W') {
          subElement = subElement.substring(1);
          ds.nrOfDices = 1;
          if (subElement.indexOf('-') != -1) {
            ds.fixedPoints += Integer.parseInt(subElement.substring(subElement
                .indexOf('-')));
            subElement = subElement.substring(0, subElement.indexOf('-'));
          }
          if (subElement.length() > 0) {
            ds.diceSize = Integer.parseInt(subElement);
          }
          else
            ds.diceSize = 6;
          if (ds.diceSize < 0) {
            throw new NumberFormatException("Würfelgröße negativ!");
          }
        }
        else {
          String nr = subElement.substring(0, subElement.indexOf('W'));
          ds.nrOfDices = Integer.parseInt(nr);
          subElement = subElement.substring(subElement.indexOf('W') + 1);
          if (subElement.indexOf('-') != -1) {
            ds.fixedPoints += Integer.parseInt(subElement.substring(subElement
                .indexOf('-')));
            subElement = subElement.substring(0, subElement.indexOf('-'));
          }
          if (subElement.length() > 0) {
            ds.diceSize = Integer.parseInt(subElement);
          }
          else
            ds.diceSize = 6;
          if (ds.diceSize < 0) {
            throw new NumberFormatException("Würfelgröße negativ!");
          }
        }
      }
      else {
        ds.fixedPoints += Integer.parseInt(subElement);
      }
    }
    return ds;
  }

  public int getDiceSize() {
    return diceSize;
  }

  public int getFixedPoints() {
    return fixedPoints;
  }

  public int getNrOfDices() {
    return nrOfDices;
  }

  public int calcValue() {
    int value = fixedPoints;
    for (int i = 0; i < nrOfDices; ++i)
      value += Dice.roll(diceSize);
    return value;
  }
}

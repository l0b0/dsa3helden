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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import dsa.model.DiceSpecification;

public class Opponent implements Cloneable {
  
  private String name;
  
  private String category;
  
  private int le;
  
  private int rs;
  
  private int mr;
  
  private ArrayList<String> weapons;
  
  private ArrayList<DiceSpecification> tps;
  
  private ArrayList<Integer> ats;
  
  private ArrayList<Integer> pas;
  
  private int nrOfAttacks;
  
  private int nrOfParades;
  
  private boolean changed;
  
  private boolean userDefined;
  
  public Object clone() {
    try {
      Opponent newObject = (Opponent) super.clone();
      newObject.weapons = new ArrayList<String>(weapons);
      newObject.ats = new ArrayList<Integer>(ats);
      newObject.pas = new ArrayList<Integer>(pas);
      newObject.tps = new ArrayList<DiceSpecification>(tps);
      newObject.changed = false;
      newObject.userDefined = true;
      return newObject;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  
  public Opponent makeClone() {
    return (Opponent) clone();
  }
  
  private static int parseInt(String text, int pos) throws ParseException {
    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException e) {
      throw new ParseException(text, pos);
    }
  }
  
  public static Opponent createOpponent(String inputLine, int version, boolean userDefined) throws ParseException {
    Opponent o = new Opponent(inputLine, version);
    o.userDefined = userDefined;
    return o;
  }
  
  private Opponent(String inputLine, int version) throws ParseException {
    parseLine(inputLine, version);
    changed = false;
  }
  
  public Opponent(String name, String category, int le, int rs, int mr, 
      ArrayList<String> weapons, ArrayList<Integer> ats, ArrayList<DiceSpecification> tps, 
      int nrOfAttacks, int nrOfParades, ArrayList<Integer> pas) {
    this.name = name;
    this.category = category;
    this.le = le;
    this.rs = rs;
    this.mr = mr;
    this.weapons = new ArrayList<String>(weapons);
    this.ats = new ArrayList<Integer>(ats);
    this.tps = new ArrayList<DiceSpecification>(tps);
    this.nrOfAttacks = nrOfAttacks;
    this.nrOfParades = nrOfParades;
    this.pas = new ArrayList<Integer>(pas);
    userDefined = true;
    changed = false;
  }
    
  private void parseLine(String inputLine, int version) throws ParseException {
    if (version < 1) throw new ParseException(inputLine, -2);
    String[] outerTokens = inputLine.split(";");
    if (outerTokens.length < 11) {
      throw new ParseException(inputLine, 0);
    }
    int i = 0;
    name = outerTokens[i++];
    category = outerTokens[i++];
    le = parseInt(outerTokens[i++], 1);
    rs = parseInt(outerTokens[i++], 2);
    mr = parseInt(outerTokens[i++], 3);
    int weaponCount = parseInt(outerTokens[i++], 4);
    String weaponNames = outerTokens[i++];
    StringTokenizer innerTokens = new StringTokenizer(weaponNames, "/");
    if (innerTokens.countTokens() != weaponCount) {
      throw new ParseException(inputLine, 5);
    }
    weapons = new ArrayList<String>();
    for (int j = 0; j < weaponCount; ++j) {
      weapons.add(innerTokens.nextToken());
    }
    nrOfAttacks = parseInt(outerTokens[i++], 6);
    if (nrOfAttacks > weaponCount) {
      throw new ParseException(inputLine, 14);
    }
    String atValues = outerTokens[i++];
    innerTokens = new StringTokenizer(atValues, "/");
    if (innerTokens.countTokens() != weaponCount) {
      throw new ParseException(inputLine, 7);
    }
    ats = new ArrayList<Integer>();
    for (int j = 0; j < weaponCount; ++j) {
      ats.add(parseInt(innerTokens.nextToken(), 8));
    }
    String tpValues = outerTokens[i++];
    innerTokens = new StringTokenizer(tpValues, "/");
    if (innerTokens.countTokens() != weaponCount) {
      throw new ParseException(inputLine, 9);
    }
    tps = new ArrayList<DiceSpecification>();
    for (int j = 0; j < weaponCount; ++j) {
      try {
        tps.add(DiceSpecification.parse(innerTokens.nextToken()));
      }
      catch (NumberFormatException e) {
        throw new ParseException(inputLine, 13);
      }
    }
    nrOfParades = parseInt(outerTokens[i++], 10);
    String paValues = outerTokens.length > 11 ? outerTokens[i++] : "";
    int neededValues = nrOfAttacks < weaponCount ? weaponCount : nrOfParades;
    innerTokens = new StringTokenizer(paValues, "/");
    if (innerTokens.countTokens() != neededValues) {
      throw new ParseException(inputLine, 11);
    }
    pas = new ArrayList<Integer>();
    for (int j = 0; j < neededValues; ++j) {
      pas.add(parseInt(innerTokens.nextToken(), 12));
    }
  }
  
  public String writeToLine(int version) {
    StringBuffer buffer = new StringBuffer(200);
    if (version > 0) {
      buffer.append(name);
      buffer.append(';');
      buffer.append(category);
      buffer.append(';');
      buffer.append(le);
      buffer.append(';');
      buffer.append(rs);
      buffer.append(';');
      buffer.append(mr);
      buffer.append(';');
      buffer.append(weapons.size());
      buffer.append(';');
      for (int i = 0; i < weapons.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(weapons.get(i));
      }
      buffer.append(';');
      buffer.append(nrOfAttacks);
      buffer.append(';');
      for (int i = 0; i < ats.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(ats.get(i));
      }
      buffer.append(';');
      for (int i = 0; i < tps.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(tps.get(i).toString());
      }
      buffer.append(';');
      buffer.append(nrOfParades);
      buffer.append(';');
      for (int i = 0; i < pas.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(pas.get(i));
      }
    }    
    return buffer.toString();
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    if (!this.category.equals(category)) {
      this.category = category;
      changed = true;
    }
  }

  public int getLE() {
    return le;
  }

  public void setLE(int le) {
    if (this.le != le) {
      this.le = le;
      changed = true;
    }
  }

  public int getMR() {
    return mr;
  }

  public void setMR(int mr) {
    if (this.mr != mr) {
      this.mr = mr;
      changed = true;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (!this.name.equals(name)) {
      this.name = name;
      changed = true;
    }
  }

  public int getNrOfAttacks() {
    return nrOfAttacks;
  }

  public void setNrOfAttacks(int nrOfAttacks) {
    if (this.nrOfAttacks != nrOfAttacks) {
      this.nrOfAttacks = nrOfAttacks;
      changed = true;
    }
  }

  public int getRS() {
    return rs;
  }

  public void setRS(int rs) {
    if (this.rs != rs) {
      this.rs = rs;
      changed = true;
    }
  }

  public ArrayList<String> getWeapons() {
    return new ArrayList<String>(weapons);
  }
  
  public void replaceWeapon(int index, String newWeapon) {
    if (index >=0 && index < weapons.size()) {
      if (!weapons.get(index).equals(newWeapon)) {
        weapons.set(index, newWeapon);
        changed = true;
      }
    }
  }
  
  public int getAT(int nr) {
    if (nr >= 0 && nr < ats.size()) {
      return ats.get(nr);
    }
    else return 0;
  }
  
  public int getPA(int nr) {
    if (nr >= 0 && nr < pas.size()) {
      return pas.get(nr);
    }
    else return 0;
  }

  public int getNrOfParades() {
    return nrOfParades;
  }

  public void setNrOfParades(int nrOfParades) {
    if (this.nrOfParades != nrOfParades) {
      this.nrOfParades = nrOfParades;
      changed = true;
    }
  }

  public DiceSpecification getTP(int nr) {
    if (nr >= 0 && nr < tps.size()) {
      return tps.get(nr);
    }
    else {
      return DiceSpecification.parse("W6");
    }
  }

  public void setTP(int nr, DiceSpecification tp) {
    if (nr >= 0 && nr < tps.size()) {
      tps.set(nr, tp);
      changed = true;
    }
  }
  
  public void setAT(int nr, int at) {
    if (nr >= 0 && nr < ats.size()) {
      ats.set(nr, at);
      changed = true;
    }
  }
  
  public void setPA(int nr, int pa) {
    if (nr >= 0 && nr < pas.size()) {
      pas.set(nr, pa);
      changed = true;
    }
  }

  public void addWeapon(String name, DiceSpecification tp, int at, int pa) {
    weapons.add(name);
    tps.add(tp);
    ats.add(at);
    pas.add(pa);
    changed = true;
  }
  
  public void removeWeapon(int index) {
    weapons.remove(index);
    tps.remove(index);
    ats.remove(index);
    pas.remove(index);
    changed = true;
  }
  
  public boolean isUserDefined() {
    return userDefined;
  }
  
  public boolean wasChanged() {
    return changed;
  }
}

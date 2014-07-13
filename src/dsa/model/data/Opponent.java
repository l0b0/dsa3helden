/*
    Copyright (c) 2006-2008 [Joerg Ruedenauer]
  
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
import java.util.List;
import java.util.StringTokenizer;

import dsa.model.DiceSpecification;
import dsa.model.FarRangedFightParams;
import dsa.model.Fighter;
import dsa.util.AbstractObservable;
import dsa.util.Observer;
import dsa.util.Optional;

public class Opponent extends AbstractObservable<Opponent.OpponentObserver> implements Cloneable, Fighter {
  
  public interface OpponentObserver extends Observer {
    void opponentChanged();
  }
  
  private String name;
  
  private String category;
  
  private int le;
  
  private int currentLE;
  
  private int rs;
  
  private int mr;
  
  private ArrayList<String> weapons;
  
  private ArrayList<DiceSpecification> tps;
  
  private ArrayList<Integer> ats;
  
  private ArrayList<Integer> pas;
  
  private ArrayList<String> usedWeapons;
  
  private int nrOfAttacks;
  
  private int nrOfParades;
  
  private boolean changed;
  
  private boolean userDefined;
  
  private ArrayList<String> targets;
  
  private boolean hasStumbled;
  
  private boolean isGrounded;
  
  private ArrayList<Integer> atBoni;
  
  private FarRangedFightParams farRangedFightParams;
  
  private ArrayList<String> opponentWeapons; 

  public Object clone() {
    try {
      Opponent newObject = (Opponent) super.clone();
      newObject.weapons = new ArrayList<String>(weapons);
      newObject.ats = new ArrayList<Integer>(ats);
      newObject.pas = new ArrayList<Integer>(pas);
      newObject.tps = new ArrayList<DiceSpecification>(tps);
      newObject.targets = new ArrayList<String>(targets);
      newObject.atBoni = new ArrayList<Integer>(atBoni);
      newObject.farRangedFightParams = (FarRangedFightParams) farRangedFightParams.clone();
      newObject.opponentWeapons = new ArrayList<String>(opponentWeapons);
      newObject.usedWeapons = new ArrayList<String>(usedWeapons);
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
    opponentWeapons = new ArrayList<String>();
    parseLine(inputLine, version);
    changed = false;
  }
  
  public Opponent(String name, String category, int le, int rs, int mr, 
      ArrayList<String> weapons, ArrayList<Integer> ats, ArrayList<DiceSpecification> tps, 
      int nrOfAttacks, int nrOfParades, ArrayList<Integer> pas) {
    this.name = name;
    this.category = category;
    this.le = le;
    this.currentLE = le;
    this.rs = rs;
    this.mr = mr;
    this.weapons = new ArrayList<String>(weapons);
    this.ats = new ArrayList<Integer>(ats);
    this.tps = new ArrayList<DiceSpecification>(tps);
    this.nrOfAttacks = nrOfAttacks;
    this.nrOfParades = nrOfParades;
    this.pas = new ArrayList<Integer>(pas);
    this.targets = new ArrayList<String>();
    this.atBoni = new ArrayList<Integer>();
    this.farRangedFightParams = new FarRangedFightParams();
    for (int i = 0; i < weapons.size(); ++i) {
      targets.add("");
      atBoni.add(0);
    }
    usedWeapons = new ArrayList<String>();
    for (int i = 0; i < nrOfAttacks; ++i) {
      int index = (i >= weapons.size()) ? 0 : i;
      usedWeapons.add(weapons.get(index));
    }
    opponentWeapons = new ArrayList<String>();
    userDefined = true;
    hasStumbled = false;
    isGrounded = false;
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
    if (innerTokens.countTokens() < neededValues) {
      throw new ParseException(inputLine, 11);
    }
    pas = new ArrayList<Integer>();
    for (int j = 0; j < neededValues; ++j) {
      pas.add(parseInt(innerTokens.nextToken(), 12));
    }
    if (version > 1) {
      if (outerTokens.length < 12) {
        throw new ParseException(inputLine, 0);
      }
      currentLE = parseInt(outerTokens[i++], 14);
    }
    else {
      currentLE = le;
    }
    targets = new ArrayList<String>();
    int index = 0;
    if (version > 2) {
      if (outerTokens.length > i) {
        innerTokens = new StringTokenizer(outerTokens[i++], "/");
        int tokenCount = innerTokens.countTokens();
        while (index < tokenCount) {
          targets.add(innerTokens.nextToken());
          ++index;
        }
      }
    }
    atBoni = new ArrayList<Integer>();
    int atBoniIndex = 0;
    if (version > 3) {
      if (outerTokens.length > i) {
        hasStumbled = (outerTokens[i++].equals("1"));
      }
      else hasStumbled = false;
      if (outerTokens.length > i) {
        isGrounded = (outerTokens[i++].equals("1"));
      }
      if (outerTokens.length > i) {
        innerTokens = new StringTokenizer(outerTokens[i++], "/");
        int tokenCount = innerTokens.countTokens();
        while (atBoniIndex < tokenCount) try {
          atBoni.add(Integer.parseInt(innerTokens.nextToken()));
          ++atBoniIndex;
        } catch (NumberFormatException e) {
          throw new ParseException(inputLine, 15);
        }
      }
      if (outerTokens.length > i) {
        try {
          farRangedFightParams = new FarRangedFightParams(outerTokens[i++]);
        }
        catch (java.io.IOException e) {
          throw new ParseException(inputLine, 16);
        }
      }
      else farRangedFightParams = new FarRangedFightParams();
    }
    else {
      hasStumbled = false;
      isGrounded = false;
      farRangedFightParams = new FarRangedFightParams();
    }
    while (index < weapons.size()) {
      targets.add("");
      ++index;
    }
    while (atBoniIndex < weapons.size()) {
      atBoni.add(0);
      ++atBoniIndex;
    }
    if (version > 4) {
      if (outerTokens.length > i) {
        isDazed = (outerTokens[i++].equals("1"));
      }
    }
    else isDazed = false;
    opponentWeapons.clear();
    if (version > 5) {
      if (outerTokens.length > i) {
        innerTokens = new StringTokenizer(outerTokens[i++], "/");
        int tokenCount = innerTokens.countTokens();
        for (int j = 0; j < tokenCount; ++j) {
          opponentWeapons.add(innerTokens.nextToken());
        }
      }
    }
    usedWeapons = new ArrayList<String>();
    if (version > 6 && outerTokens.length > i) {
      innerTokens = new StringTokenizer(outerTokens[i++], "/");
      int tokenCount = innerTokens.countTokens();
      for (int j = 0; j < tokenCount; ++j) {
        usedWeapons.add(innerTokens.nextToken());
      }
      for (int j = tokenCount; j < nrOfAttacks; ++j) {
        usedWeapons.add(weapons.get(0));
      }
    }
    else {
      for (int j = 0; j < nrOfAttacks; ++j) {
        int index2 = (j >= weapons.size()) ? 0 : j;
        usedWeapons.add(weapons.get(index2));
      }      
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
    if (version > 1) {
      buffer.append(';');
      buffer.append(currentLE);
    }
    if (version > 2) {
      buffer.append(';');
      for (int i = 0; i < targets.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(targets.get(i));
      }
    }
    if (version > 3) {
      buffer.append(';');
      buffer.append(hasStumbled ? '1' : '0');
      buffer.append(';');
      buffer.append(isGrounded ? '1' : '0');
      buffer.append(';');
      for (int i = 0; i < atBoni.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(atBoni.get(i));
      }
      buffer.append(';');
      buffer.append(farRangedFightParams.writeToString());
    }
    if (version > 4) {
      buffer.append(';');
      buffer.append(isDazed ? '1' : '0');
    }
    if (version > 5) {
      buffer.append(';');
      for (int i = 0; i < opponentWeapons.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(opponentWeapons.get(i));
      }
    }
    if (version > 6) {
      buffer.append(';');
      for (int i = 0; i < usedWeapons.size(); ++i) {
        if (i > 0) buffer.append('/');
        buffer.append(usedWeapons.get(i));
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
      if (currentLE > le) {
        currentLE = le;
      }
      changed = true;
      for (OpponentObserver o : observers) {
        o.opponentChanged();
      }
    }
  }

  public int getMR() {
    return mr;
  }

  public void setMR(int mr) {
    if (this.mr != mr) {
      this.mr = mr;
      changed = true;
      for (OpponentObserver o : observers) {
        o.opponentChanged();
      }
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
      ArrayList<String> newWeapons = new ArrayList<String>();
      if (usedWeapons.size() > 0) {
	      for (int i = 0; i < nrOfAttacks; ++i) {
	        int index = (i >= usedWeapons.size()) ? 0 : i;
	        newWeapons.add(usedWeapons.get(index));
	      }
      }
      if (weapons.size() > 0) {
    	  for (int i = usedWeapons.size(); i < nrOfAttacks; ++i) {
    		  int index = i >= weapons.size() ? 0 : i;
    		  newWeapons.add(weapons.get(index));
    	  }
      }
      usedWeapons = newWeapons;
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
      for (OpponentObserver o : observers) {
        o.opponentChanged();
      }
    }
  }

  public ArrayList<String> getWeapons() {
    return new ArrayList<String>(weapons);
  }
  
  public List<String> getPossibleWeapons(int attackNr) {
    return getWeapons();
  }
  
  public void setUsedWeapon(int attack, String weapon) {
    if (attack < 0 || attack >= usedWeapons.size()) return;
    if (usedWeapons.get(attack).equals(weapon)) return;
    usedWeapons.set(attack, weapon);
    changed = true;
  }
  
  public void replaceWeapon(int index, String newWeapon) {
    if (index >=0 && index < weapons.size()) {
      if (!weapons.get(index).equals(newWeapon)) {
        weapons.set(index, newWeapon);
        changed = true;
      }
    }
  }
  
  public Optional<Integer> getWeaponAT(int nr) {
    if (nr >= 0 && nr < ats.size()) {
      return new Optional<Integer>(ats.get(nr));
    }
    else return Optional.NULL_INT;
  }
  
  public Optional<Integer> getAT(int nr) {
    if (nr >= 0 && nr < usedWeapons.size()) {
      String weapon = usedWeapons.get(nr);
      for (int i = 0; i < weapons.size(); ++i) {
        if (weapons.get(i).equals(weapon)) {
          return getWeaponAT(i);
        }
      }
    }
    return Optional.NULL_INT;
  }
  
  public Optional<Integer> getWeaponPA(int nr) {
    if (nr >= 0 && nr < pas.size()) {
      return new Optional<Integer>(pas.get(nr));
    }
    else return Optional.NULL_INT;
  }
  
  public Optional<Integer> getPA(int nr) {
    if (nr >= 0 && nr < usedWeapons.size()) {
      String weapon = usedWeapons.get(nr);
      for (int i = 0; i < weapons.size(); ++i) {
        if (weapons.get(i).equals(weapon)) {
          return getWeaponPA(i);
        }
      }
    }
    return Optional.NULL_INT;    
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

  public DiceSpecification getWeaponTP(int nr) {
    if (nr >= 0 && nr < tps.size()) {
      return tps.get(nr);
    }
    else {
      return DiceSpecification.parse("W6");
    }
  }
  
  public DiceSpecification getTP(int nr) {
    if (nr >= 0 && nr < usedWeapons.size()) {
      String weapon = usedWeapons.get(nr);
      for (int i = 0; i < weapons.size(); ++i) {
        if (weapons.get(i).equals(weapon)) {
          return getWeaponTP(i);
        }
      }
    }
    return DiceSpecification.parse("W6");    
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
    targets.add("");
    if (usedWeapons.size() < nrOfAttacks) {
    	for (int i = usedWeapons.size(); i < nrOfAttacks; ++i) {
    		usedWeapons.add(name);
    	}
    }
    changed = true;
  }
  
  public void removeWeapon(int index) {
    String weapon = weapons.get(index);
    weapons.remove(index);
    tps.remove(index);
    ats.remove(index);
    pas.remove(index);
    targets.remove(index);
    for (int i = 0; i < usedWeapons.size(); ++i) {
      if (usedWeapons.get(i).equals(weapon)) {
        usedWeapons.set(i, weapons.isEmpty() ? "" : weapons.get(0));
      }
    }
    changed = true;
  }
  
  public boolean isUserDefined() {
    return userDefined;
  }
  
  public boolean wasChanged() {
    return changed || farRangedFightParams.isChanged();
  }

  public int getMaxLE() {
    return le;
  }
  
  public int getCurrentLE() {
    return currentLE;
  }

  public void setCurrentLE(int le) {
    if (currentLE != le) {
      currentLE = le;
      changed = true;
      for (OpponentObserver o : observers) {
        o.opponentChanged();
      }
    }
  }

  public List<String> getFightingWeapons() {
    return new ArrayList<String>(usedWeapons);
  }
  
  public void setTarget(int weaponIndex, String target) {
    if (weaponIndex >= 0 && weaponIndex < targets.size()) {
      if (targets.get(weaponIndex).equals(target)) return;
      targets.set(weaponIndex, target);
      changed = true;
    }
  }
  
  public String getTarget(int weaponIndex) {
    if (weaponIndex >= 0 && weaponIndex < targets.size()) {
      return targets.get(weaponIndex);
    }
    else return "";
  }

  public boolean hasStumbled() {
    return hasStumbled;
  }

  public void setHasStumbled(boolean hasStumbled) {
    if (hasStumbled != this.hasStumbled) {
      this.hasStumbled = hasStumbled;
      changed = true;
    }
  }
  
  public boolean isGrounded() {
    return isGrounded;
  }

  public void setGrounded(boolean grounded) {
    if (isGrounded != grounded) {
      isGrounded = grounded;
      changed = true;
    }
  }

  public int getATBonus(int nr) {
    if (nr < 0 || nr >= atBoni.size()) return 0;
    else return atBoni.get(nr);
  }

  public void setATBonus(int nr, int bonus) {
    if (nr >= 0 && nr < atBoni.size()) {
      int oldValue = atBoni.get(nr);
      if (bonus != oldValue) {
        atBoni.set(nr, bonus);
        changed = true;
      }
    }
  }

  public int getMarkers() {
    return 0;
  }

  public FarRangedFightParams getFarRangedFightParams() {
    return farRangedFightParams;
  }
  
  private boolean isDazed = false;
  
  public boolean isDazed() {
    return isDazed;
  }
  
  public void setDazed(boolean dazed) {
    if (dazed == isDazed) return;
    isDazed = dazed;
    changed = true;
  }

  public String getOpponentWeapon(int nr) {
    if (nr < 0 || nr >= opponentWeapons.size()) {
      return "Nichts";
    }
    else return opponentWeapons.get(nr);
  }

  public void setOpponentWeapon(int nr, String weapon) {
    if (nr < 0) return;
    while (nr >= opponentWeapons.size()) {
      opponentWeapons.add("Nichts");
    }
    if (!opponentWeapons.get(nr).equals(weapon)) {
      opponentWeapons.set(nr, weapon);
      changed = true;
    }
  }

}

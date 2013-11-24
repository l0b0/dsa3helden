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
package dsa.model.impl;

import java.util.HashMap;

import dsa.model.talents.Talent;

public class Language implements Talent, dsa.model.talents.Language {

  public Language(String name, int max, boolean isOld) {
    this.name = name;
    this.max.put("Default", max);
    this.isOld = isOld;
  }

  public boolean canBeTested() {
    return false;
  }

  public void setMax(String language, int value) {
    max.put(language, value);
  }

  public int getMax() {
    return max.get("Default");
  }

  public int getMax(String language) {
    if (max.containsKey(language))
      return max.get(language);
    else
      return getMax();
  }

  public boolean isOld() {
    return isOld;
  }

  public String getName() {
    return name;
  }

  public boolean isFightingTalent() {
    return false;
  }

  public boolean isSpell() {
    return false;
  }

  public boolean isLanguage() {
    return true;
  }

  public int getMaxIncreasePerStep() {
    return 0;
  }

  private String name;

  private java.util.HashMap<String, Integer> max = new HashMap<String, Integer>();

  private boolean isOld;

}

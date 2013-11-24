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
package dsa.model.impl;

/**
 * 
 */
public class FightingTalent implements dsa.model.talents.FightingTalent {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.FightingTalent#IsProjectileTalent()
   */
  public boolean isProjectileTalent() {
    return isProjectile;
  }

  public boolean canBeTested() {
    return false;
  }

  public boolean isSpell() {
    return false;
  }

  public boolean isLanguage() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#IsFightingTalent()
   */
  public boolean isFightingTalent() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetMaxIncreasePerStep()
   */
  public int getMaxIncreasePerStep() {
    return maxIncreasePerStep;
  }

  public int getBEMinus() {
    return beMinus;
  }

  public boolean isLefthandIndicator() {
    return "Linkshändig".equals(name);
  }

  boolean isProjectile;

  String name;

  int maxIncreasePerStep;

  int beMinus;

  FightingTalent(String name, boolean projectile, int beMinus,
      int maxIncreasePerStep) {
    this.name = name;
    this.isProjectile = projectile;
    this.beMinus = beMinus;
    this.maxIncreasePerStep = maxIncreasePerStep;
  }

}

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

import dsa.model.characters.Property;

/**
 * 
 */
public class NormalTalent implements dsa.model.talents.NormalTalent {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetFirstProperty()
   */
  public Property getFirstProperty() {
    return property1;
  }

  public boolean canBeTested() {
    return true;
  }

  public boolean isSpell() {
    return isSpell;
  }

  public boolean isLanguage() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetSecondProperty()
   */
  public Property getSecondProperty() {
    return property2;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetThirdProperty()
   */
  public Property getThirdProperty() {
    return property3;
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
   * @see dsa.data.Talent#GetMaxIncreasePerStep()
   */
  public int getMaxIncreasePerStep() {
    return maxIncreasePerStep;
  }

  public NormalTalent(String name, Property p1, Property p2, Property p3,
      int mIpS, boolean isSpell) {
    this.name = name;
    this.property1 = p1;
    this.property2 = p2;
    this.property3 = p3;
    this.maxIncreasePerStep = mIpS;
    this.isSpell = isSpell;
  }

  private Property property1;

  private Property property2;

  private Property property3;

  private String name;

  private int maxIncreasePerStep;

  private boolean isSpell;

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#IsFightingTalent()
   */
  public boolean isFightingTalent() {
    return false;
  }

}

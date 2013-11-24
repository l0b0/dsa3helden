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
package dsa.model.characters;

/**
 * 
 */
public abstract class CharacterAdapter implements CharacterObserver {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#TalentAdded(java.lang.String)
   */
  public void talentAdded(String talent) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#TalentRemoved(java.lang.String)
   */
  public void talentRemoved(String talent) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#DefaultTalentChanged(java.lang.String)
   */
  public void defaultTalentChanged(String talent) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#CurrentTalentChanged(java.lang.String)
   */
  public void currentTalentChanged(String talent) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#DefaultPropertyChanged(dsa.data.Property)
   */
  public void defaultPropertyChanged(Property property) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#CurrentPropertyChanged(dsa.data.Property)
   */
  public void currentPropertyChanged(Property property) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#DefaultEnergyChanged(dsa.data.Energy)
   */
  public void defaultEnergyChanged(Energy energy) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharacterObserver#CurrentEnergyChanged(dsa.data.Energy)
   */
  public void currentEnergyChanged(Energy energy) {
  }

  public void stepIncreased() {
  }

  public void increaseTriesChanged() {
  }

  public void weightChanged() {
  }

  public void atPADistributionChanged(String talent) {
  }

  public void derivedValueChanged(Hero.DerivedValue dv) {
  }

  public void thingRemoved(String thing) {
  }

  public void nameChanged(String oldName, String newName) {
  }

  public void weaponRemoved(String weapon) {
  }

  public void shieldRemoved(String shield) {
  }

  public void bfChanged(String item) {
  }

  public void beModificationChanged() {
  }
}

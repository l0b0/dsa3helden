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
package dsa.model.characters;

/**
 * 
 */
public interface CharacterObserver extends dsa.util.Observer {
  void talentAdded(String talent);

  void talentRemoved(String talent);

  void defaultTalentChanged(String talent);

  void currentTalentChanged(String talent);

  void defaultPropertyChanged(Property property);

  void currentPropertyChanged(Property property);

  void defaultEnergyChanged(Energy energy);

  void currentEnergyChanged(Energy energy);

  void stepIncreased();

  void increaseTriesChanged();

  void weightChanged();

  void atPADistributionChanged(String talent);

  void derivedValueChanged(Hero.DerivedValue dv);

  void thingRemoved(String thing, boolean fromWarehouse);

  void weaponRemoved(String weapon);
  
  void armourRemoved(String armour);

  void shieldRemoved(String name);

  void nameChanged(String oldName, String newName);

  void bfChanged(String item);

  void beModificationChanged();
  
  void thingsChanged();
  
  void activeWeaponsChanged();

  void fightingStateChanged();
  
  void opponentWeaponChanged(int weaponNr);
  
  void moneyChanged();
}

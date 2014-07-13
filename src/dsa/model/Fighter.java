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
package dsa.model;

import java.util.List;

import dsa.util.Optional;

public interface Fighter {
  
  int getCurrentLE();
  void setCurrentLE(int le);
  int getMaxLE();
  
  int getMR();
  int getRS();
  
  String getName();
  
  int getNrOfAttacks();
  int getNrOfParades();
  
  List<String> getFightingWeapons();
  List<String> getPossibleWeapons(int attackNr);
  void setUsedWeapon(int attack, String weapon);
  
  Optional<Integer> getAT(int nr);
  Optional<Integer> getPA(int nr);
  DiceSpecification getTP(int nr);
  
  void setTarget(int weaponIndex, String target);
  String getTarget(int weaponIndex);
  
  boolean hasStumbled();

  void setHasStumbled(boolean hasStumbled);
  
  boolean isGrounded();

  void setGrounded(boolean grounded);

  int getATBonus(int nr);
  void setATBonus(int nr, int bonus);

  int getMarkers();
  
  FarRangedFightParams getFarRangedFightParams();

  boolean isDazed();

  void setDazed(boolean dazed);

  String getOpponentWeapon(int nr);
  void setOpponentWeapon(int nr, String weapon);
}

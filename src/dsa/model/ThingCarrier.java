/*
 Copyright (c) 2006-2009 [Joerg Ruedenauer]
 
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

import dsa.model.characters.CharacterObserver;
import dsa.model.data.IExtraThingData;
import dsa.util.Observable;

public interface ThingCarrier extends Observable<CharacterObserver> {
  
  String getName();

  String[] getThings();
  
  String[] getThingsInContainer(String container);

  String addThing(String name, String container);
  
  String addThing(String name, IExtraThingData extraData, String container);
  
  boolean canMoveTo(String name, String newContainer);
  
  String moveThing(String name, String newContainer);

  IExtraThingData getExtraThingData(String thing, int thingNumber);

  void removeThing(String name, boolean removeContents);

  int getThingCount(String thing);
  
  void pay(int price, dsa.model.data.Thing.Currency currency);

  boolean isDifference();

  void fireWeightChanged();
  
  String getThingContainer(String thing);
}

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
package dsa.gui.dialogs.ItemProviders;

import dsa.gui.tables.ThingsTable;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.util.Optional;

public class ThingsProvider extends ItemProvider {
  
  public ThingsProvider(boolean allThings) {
    super(new ThingsTable(false, allThings));
    this.allThings = allThings;
  }
  
  private boolean allThings;

  /* (non-Javadoc)
   * @see dsa.gui.dialogs.ItemProviders.ItemProvider#fillTable(boolean)
   */
  @Override
  public void fillTable(boolean showSingularItems) {
    Things things = Things.getInstance();
    ThingsTable table = (ThingsTable) getTable();
    for (Thing thing : things.getAllWearableThings()) {
      if (!allThings) {
        if (!thing.getCategory().equals("Kleidung")) {
          continue;
        }
      }
      if (thing.isSingular() && !showSingularItems) continue;
      if (isDisplayed(thing.getName())) table.addThing(thing);
    }
  }

  public int getDefaultPrice(String item) {
    Optional<Integer> value = Things.getInstance().getThing(item).getValue();
    return value.hasValue() ? value.getValue().intValue() : 0;
  }
  
  public Thing.Currency getCurrency(String item) {
    return Things.getInstance().getThing(item).getCurrency();
  }
}

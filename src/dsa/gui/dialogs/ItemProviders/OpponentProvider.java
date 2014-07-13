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

import dsa.gui.tables.OpponentTable;
import dsa.model.data.Opponents;

public class OpponentProvider extends ItemProvider {

  public OpponentProvider() {
    super(new OpponentTable(false));
  }
  /* (non-Javadoc)
   * @see dsa.gui.dialogs.ItemProviders.ItemProvider#fillTable(boolean)
   */
  @Override
  public void fillTable(boolean showSingularItems) {
    Opponents opponents = Opponents.getOpponentsDB();
    OpponentTable table = (OpponentTable) getTable();
    for (String opponent : opponents.getOpponentNames()) {
      if (isDisplayed(opponent)) table.addOpponent(opponents.getOpponent(opponent));
    }
  }

}

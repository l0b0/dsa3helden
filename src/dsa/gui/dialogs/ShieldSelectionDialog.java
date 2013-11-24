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
package dsa.gui.dialogs;

import dsa.gui.tables.ShieldsTable;
import dsa.model.data.Shield;
import dsa.model.data.Shields;

public class ShieldSelectionDialog extends SelectionDialogBase {

  public ShieldSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Schild / Parierwaffe hinzufügen", new ShieldsTable(),
        "Parade");

    fillTable();
  }

  protected void fillTable() {
    Shields shields = Shields.getInstance();
    ShieldsTable table = (ShieldsTable) mTable;
    for (Shield shield : shields.getAllShields()) {
      if (isDisplayed(shield.getName())) table.addShield(shield);
    }
  }

}

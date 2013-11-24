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

import java.util.List;

import dsa.gui.tables.SpellTable;
import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;

public class SpellSelectionDialog extends SelectionDialogBase {

  public SpellSelectionDialog(javax.swing.JFrame owner, Hero character) {
    super(owner, "Zauber hinzufügen", new SpellTable(), "Zauber");
    mHero = character;
    fillTable();
  }

  private Hero mHero;

  public void updateTable() {
    mTable.clear();
    fillTable();
  }

  @Override
  protected void fillTable() {
    List<Talent> talents = Talents.getInstance().getTalentsInCategory("Zauber");
    SpellTable table = (SpellTable) mTable;
    for (Talent t : talents) {
      if (!mHero.hasTalent(t.getName()) && isDisplayed(t.getName())) {
        table.addSpell((Spell) t);
      }
    }

  }

}

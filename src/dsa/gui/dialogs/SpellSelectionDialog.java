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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.tables.SpellTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;

public final class SpellSelectionDialog extends AbstractSelectionDialog {

  public SpellSelectionDialog(javax.swing.JFrame owner, Hero character) {
    super(owner, "Zauber hinzufügen", new SpellTable(character.getInternalType()), "Zauber");
    mHero = character;
    initialize();
    fillTable();
  }

  private final Hero mHero;

  public void updateTable() {
    mTable.clear();
    fillTable();
  }

  @Override
  protected void fillTable() {
    List<Talent> talents = Talents.getInstance().getTalentsInCategory("Zauber");
    SpellTable table = (SpellTable) mTable;
    listen = false;
    for (Talent t : talents) {
      if (!mHero.hasTalent(t.getName()) && isDisplayed(t.getName())) {
        table.addSpell((Spell) t);
      }
    }
    listen = true;
    updateDeleteButton();
  }

  boolean listen = true;

  protected void addSubclassSpecificButtons(JPanel lowerPanel) {
    lowerPanel.add(getNewButton());
    lowerPanel.add(getDeleteButton());
    lowerPanel.add(getEditButton());
    mTable.addSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!listen) return;
        updateDeleteButton();
      }
    });
  }

  private JButton newButton;

  private JButton deleteButton;
  
  private JButton editButton;

  private JButton getNewButton() {
    if (newButton == null) {
      newButton = new JButton(ImageManager.getIcon("increase"));
      newButton.setToolTipText("Neuen Zauber anlegen");
      newButton.setBounds(315, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SpellDialog dialog = new SpellDialog(SpellSelectionDialog.this);
          dialog.setVisible(true);
          if (dialog.getCreatedSpell() != null) {
            Talents.getInstance().addUserSpell(dialog.getCreatedSpell());
            ((SpellTable) mTable).addSpell(dialog.getCreatedSpell());
          }
        }
      });
    }
    return newButton;
  }

  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Zauber bearbeiten");
      editButton.setBounds(405, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Spell spell = (Spell) Talents.getInstance().getTalent(mTable.getSelectedItem());
          if (spell == null) return;
          SpellDialog dialog = new SpellDialog(SpellSelectionDialog.this, spell);
          dialog.setVisible(true);
          if (dialog.getCreatedSpell() != null) {
            ((SpellTable) mTable).removeSelectedSpell();
            Talents.getInstance().addUserSpell(dialog.getCreatedSpell());
            ((SpellTable) mTable).addSpell(dialog.getCreatedSpell());
          }
        }
      });
    }
    return editButton;
  }

  private JButton getDeleteButton() {
    if (deleteButton == null) {
      deleteButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      deleteButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      deleteButton.setToolTipText("Zauber löschen");
      deleteButton.setBounds(360, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeSpell();
        }
      });
    }
    return deleteButton;
  }

  protected void removeSpell() {
    String spell = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane.showConfirmDialog(this,
        "Zauber wird bei allen geladenen Helden entfernt. Sicher?",
        "Zauber entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    for (dsa.model.characters.Hero c : Group.getInstance().getAllCharacters()) {
      c.removeTalent(spell);
    }
    ((SpellTable) mTable).removeSelectedSpell();
    Talents.getInstance().removeUserSpell(spell);
  }

  private void updateDeleteButton() {
    String spell = mTable.getSelectedItem();
    if (spell != null && spell.length() > 0) {
      Talent t = Talents.getInstance().getTalent(spell);
      if (t instanceof Spell) {
        getDeleteButton().setEnabled(((Spell)t).isUserDefined());
        getEditButton().setEnabled(((Spell) t).isUserDefined());
        return;
      }
    }
    getDeleteButton().setEnabled(false);
    getEditButton().setEnabled(false);
  }
}

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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.dialogs.ItemProviders.ClothesProvider;
import dsa.gui.tables.ClothesTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.data.Animal;
import dsa.model.data.Cloth;
import dsa.model.data.Clothes;
import dsa.model.data.Thing;
import dsa.model.data.Things;

public final class ClothSelectionDialog extends dsa.gui.dialogs.AbstractSelectionDialog {

  public ClothSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Kleidung hinzufügen", new ClothesProvider(),
        "KleidungSelector");
    initialize();
    fillTable();
  }

  protected boolean showSingularBox() {
    return true;
  }

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
      newButton.setToolTipText("Neue Kleidung anlegen");
      newButton.setBounds(365, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ClothDialog dialog = new ClothDialog(ClothSelectionDialog.this);
          dialog.setVisible(true);
          if (dialog.getThing() != null)
            if (showSingularItems() || !dialog.getThing().isSingular())
              ((ClothesTable) mTable).addThing(dialog.getThing(), dialog.getThing().getName(), true);
        }
      });
    }
    return newButton;
  }

  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Kleidung bearbeiten");
      editButton.setBounds(455, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Thing thing = Things.getInstance().getThing(mTable.getSelectedItem());
          if (thing == null) return;
          Cloth cloth = Clothes.getInstance().getCloth(thing.getName());
          if (cloth == null) return;
          ClothDialog dialog = new ClothDialog(ClothSelectionDialog.this, cloth);
          dialog.setVisible(true);
          if (dialog.getThing() != null) {
            ((ClothesTable) mTable).removeThing(thing.getName());
            if (showSingularItems() || !thing.isSingular())
              ((ClothesTable) mTable).addThing(thing, thing.getName(), true);
            if (getCallback() != null) {
              getCallback().itemChanged(thing.getName());
            }
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
      deleteButton.setToolTipText("Kleidung löschen");
      deleteButton.setBounds(410, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeThing();
        }
      });
    }
    return deleteButton;
  }

  protected void removeThing() {
    String thing = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane.showConfirmDialog(this,
        "Kleidung wird bei allen geladenen Helden entfernt. Sicher?",
        "Kleidung entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    for (dsa.model.characters.Hero c : Group.getInstance()
        .getAllCharacters()) {
      c.removeClothes(thing);
      while (c.getThingCount(thing) > 0)
        c.removeThing(thing, false);
      for (int i = 0; i < c.getNrOfAnimals(); ++i) {
        Animal a = c.getAnimal(i);
        while (a.getThingCount(thing) > 0) {
          a.removeThing(thing, false);
        }
      }
    }
    ((ClothesTable) mTable).removeSelectedThing();
    Things.getInstance().removeThing(thing);
    Clothes.getInstance().removeCloth(thing);
  }

  protected void updateDeleteButton() {
    String thing = mTable.getSelectedItem();
    boolean enabled = thing != null && thing.length() > 0
                    && Things.getInstance().getThing(thing).isUserDefined();
    getDeleteButton().setEnabled(enabled);
    getEditButton().setEnabled(enabled);
  }

}

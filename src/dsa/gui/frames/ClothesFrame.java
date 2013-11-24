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
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.ThingSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.tables.ThingsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.ExtraThingData;
import dsa.model.data.Thing;
import dsa.model.data.Things;

public final class ClothesFrame extends AbstractDnDFrame implements CharactersObserver, Things.ThingsListener {

  public ClothesFrame() {
    super(ThingTransfer.Flavors.Thing, "Kleidung");
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    Things.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Kleidung");
        Group.getInstance().removeObserver(ClothesFrame.this);
        Things.getInstance().removeObserver(ClothesFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Kleidung");
          Group.getInstance().removeObserver(ClothesFrame.this);
          Things.getInstance().removeObserver(ClothesFrame.this);
          done = true;
        }
      }
    });
    mTable = new ThingsTable(false, false);
    registerForDnD(mTable);
    JPanel panel = mTable.getPanelWithTable();

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getRemoveButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);

    updateData();
    mTable.restoreSortingState("Kleidung");
    mTable.setFirstSelectedRow();
  }

  public String getHelpPage() {
    return "Kleidung";
  }

  ThingsTable mTable;

  Hero currentHero;

  JButton addButton;

  JButton removeButton;

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText("Kleidung hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }

  protected void selectItem() {
    ThingSelectionDialog dialog = new ThingSelectionDialog(this, false);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        if (java.util.Arrays.asList(currentHero.getClothes()).contains(item)) {
          JOptionPane
              .showMessageDialog(
                  ClothesFrame.this,
                  "Jede Kleidungsart kann hier nur einmal hinzugefügt werden.\nDu kannst weitere Kleidungsstücke statt dessen zu den\nAusrüstungsgegenständen hinzufügen.",
                  "Fehler", JOptionPane.WARNING_MESSAGE);
          return;
        }
        currentHero.addClothes(item);
        Thing thing = Things.getInstance().getThing(item);
        if (thing != null)
          mTable.addThing(thing);
        else
          mTable.addUnknownThing(item);
        removeButton.setEnabled(true);
      }
      public void itemChanged(String item) {
        Things.getInstance().thingChanged(item);
      }
    });
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(5, 35, 60, 25);
      removeButton.setToolTipText("Kleidung entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          removeItem(name);
        }
      });
    }
    return removeButton;
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Things things = Things.getInstance();
      for (String name : currentHero.getClothes()) {
        Thing thing = things.getThing(name);
        if (thing != null) {
          mTable.addThing(thing);
        }
        else
          mTable.addUnknownThing(name);
      }
      removeButton.setEnabled(currentHero.getClothes().length > 0);
      addButton.setEnabled(true);
    }
    else {
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }

  @Override
  protected boolean addItem(String item, ExtraThingData extraData) {
    if (extraData.getType() != ExtraThingData.Type.Thing) {
      return false;
    }
    Thing thing = Things.getInstance().getThing(item);
    if (thing == null) {
      return false;
    }
    if (!thing.getCategory().equals("Kleidung")) {
      return false;
    }
    if (java.util.Arrays.asList(currentHero.getClothes()).contains(item)) {
      JOptionPane
          .showMessageDialog(
              ClothesFrame.this,
              "Jede Kleidungsart kann hier nur einmal hinzugefügt werden.\nDu kannst weitere Kleidungsstücke statt dessen zu den\nAusrüstungsgegenständen hinzufügen.",
              "Fehler", JOptionPane.WARNING_MESSAGE);
      return false;
    }
    currentHero.addClothes(item);
    mTable.addThing(thing);
    removeButton.setEnabled(true);
    return true;
  }

  @Override
  protected void removeItem(String item) {
    currentHero.removeClothes(item);
    mTable.removeSelectedThing();
    removeButton.setEnabled(currentHero.getClothes().length > 0);
  }

  @Override
  protected ExtraThingData getExtraDnDData(String item) {
    return new ExtraThingData(ExtraThingData.Type.Thing);
  }

  public void thingChanged(String thing) {
    if (mTable.containsItem(thing)) {
      mTable.removeThing(thing);
      mTable.addThing(Things.getInstance().getThing(thing));
    }
  }
}

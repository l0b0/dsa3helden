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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsa.gui.dialogs.ThingSelectionDialog;
import dsa.gui.dialogs.SelectionDialogBase.SelectionDialogCallback;
import dsa.gui.tables.ThingsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Thing;
import dsa.model.data.Things;

public class ThingsFrame extends SubFrame implements CharactersObserver {

  private class MyHeroObserver extends dsa.model.characters.CharacterAdapter {
    public void thingRemoved(String thing) {
      mTable.removeThing(thing);
    }
  }

  private MyHeroObserver myHeroObserver = new MyHeroObserver();

  public ThingsFrame() {
    super("Ausrüstung");
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    if (currentHero != null) currentHero.addHeroObserver(myHeroObserver);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Ausrüstung");
        Group.getInstance().removeObserver(ThingsFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myHeroObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Ausrüstung");
          Group.getInstance().removeObserver(ThingsFrame.this);
          done = true;
        }
      }
    });
    mTable = new ThingsTable(true);
    JPanel panel = mTable.getPanelWithTable();

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(150, 40));
    lowerPanel.add(getSumLabel(), null);
    panel.add(lowerPanel, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getRemoveButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);

    updateData();
    mTable.restoreSortingState("Ausrüstung");
    mTable.setFirstSelectedRow();
  }

  ThingsTable mTable;

  Hero currentHero;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel("");
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 440, 25);
    }
    return sumLabel;
  }

  JButton addButton;

  JButton removeButton;

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText("Gegenstand hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addThing();
        }
      });
    }
    return addButton;
  }

  protected void addThing() {
    ThingSelectionDialog dialog = new ThingSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void ItemSelected(String item) {
        currentHero.addThing(item);
        if (currentHero.getThingCount(item) == 1) {
          Thing thing = Things.getInstance().getThing(item);
          if (thing != null)
            mTable.addThing(thing);
          else
            mTable.addUnknownThing(item);
        }
        else
          mTable.setCount(item, currentHero.getThingCount(item));
        removeButton.setEnabled(true);
        calcSums();
      }
    });
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(5, 35, 60, 25);
      removeButton.setToolTipText("Gegenstand entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          int oldCount = currentHero.getThingCount(name);
          currentHero.removeThing(name);
          if (oldCount == 1) {
            // mTable.RemoveSelectedThing();
          }
          else
            mTable.setCount(name, oldCount - 1);
          removeButton.setEnabled(currentHero.getThings().length > 0);
          calcSums();
        }
      });
    }
    return removeButton;
  }

  private void calcSums() {
    long weight = 0;
    long value = 0;
    Things things = Things.getInstance();
    for (String name : currentHero.getThings()) {
      Thing thing = things.getThing(name);
      if (thing != null) {
        long count = currentHero.getThingCount(name);
        weight += count * (long) thing.getWeight();
        if (thing.getValue().hasValue()) {
          if (thing.getCurrency() == Thing.Currency.D)
            value += count * (long) thing.getValue().getValue() * 1000l;
          else if (thing.getCurrency() == Thing.Currency.S)
            value += count * (long) thing.getValue().getValue() * 100l;
          else if (thing.getCurrency() == Thing.Currency.K)
            value += count * (long) thing.getValue().getValue() * 10l;
          else if (thing.getCurrency() == Thing.Currency.H)
            value += count * (long) thing.getValue().getValue();
        }
      }
    }
    float weightStones = weight / 40.0f;
    float valueD = value / 1000.0f;

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);

    String text = "Gesamt: Wert ca. " + format.format(valueD)
        + " Dukaten, Gewicht ca. ";
    format.setMaximumFractionDigits(3);
    text += format.format(weightStones) + " Stein";
    sumLabel.setText(text);
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Things things = Things.getInstance();
      for (String name : currentHero.getThings()) {
        Thing thing = things.getThing(name);
        if (thing != null) {
          mTable.addThing(thing);
        }
        else
          mTable.addUnknownThing(name);
        mTable.setCount(name, currentHero.getThingCount(name));
      }
      calcSums();
      removeButton.setEnabled(currentHero.getThings().length > 0);
      addButton.setEnabled(true);
    }
    else {
      sumLabel
          .setText("Ausrüstung: 0 Stein,  Waffen: 0 Stein,  Rüstung: 0 Stein");
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (oldCharacter != null) oldCharacter.removeHeroObserver(myHeroObserver);
    if (newCharacter != null) newCharacter.addHeroObserver(myHeroObserver);
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
}

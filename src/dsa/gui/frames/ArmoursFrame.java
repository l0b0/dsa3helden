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
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.ArmourSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.tables.ArmoursTable;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.gui.util.OptionsChange.OptionsListener;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Armour;
import dsa.model.data.Armours;

public final class ArmoursFrame extends SubFrame implements CharactersObserver,
    OptionsListener {

  private class MyHeroObserver extends dsa.model.characters.CharacterAdapter {
    public void armourRemoved(String armour) {
      mTable.removeArmour(armour);
    }
  }

  private final MyHeroObserver myHeroObserver = new MyHeroObserver();

  public ArmoursFrame() {
    super("Rüstungen");
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    OptionsChange.addListener(ArmoursFrame.this);
    if (currentHero != null) currentHero.addHeroObserver(myHeroObserver);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Rüstungen");
        Group.getInstance().removeObserver(ArmoursFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myHeroObserver);
        OptionsChange.removeListener(ArmoursFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Rüstungen");
          Group.getInstance().removeObserver(ArmoursFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myHeroObserver);
          OptionsChange.removeListener(ArmoursFrame.this);
          done = true;
        }
      }
    });
    mTable = new ArmoursTable();
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
    mTable.restoreSortingState("Rüstungen");
    mTable.setFirstSelectedRow();
  }

  ArmoursTable mTable;

  Hero currentHero;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel("");
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 280, 25);
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
      addButton.setToolTipText("Rüstung hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addArmour();
        }
      });
    }
    return addButton;
  }

  protected void addArmour() {
    ArmourSelectionDialog dialog = new ArmourSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        if (Arrays.asList(currentHero.getArmours()).contains(item)) {
          JOptionPane
              .showMessageDialog(
                  ArmoursFrame.this,
                  "Ein Held kann jede Rüstung nur einfach tragen.\nDu kannst die Rüstung statt dessen zu den\nAusrüstungsgegenständen hinzufügen.",
                  "Fehler", JOptionPane.WARNING_MESSAGE);
          return;
        }
        currentHero.addArmour(item);
        Armour armour = Armours.getInstance().getArmour(item);
        if (armour != null)
          mTable.addArmour(armour);
        else
          mTable.addUnknownArmour(item);
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
      removeButton.setToolTipText("Rüstung entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          currentHero.removeArmour(name);
          mTable.removeSelectedArmour();
          removeButton.setEnabled(currentHero.getArmours().length > 0);
          calcSums();
        }
      });
    }
    return removeButton;
  }

  private void calcSums() {
    int weight = 0;
    Armours armours = Armours.getInstance();
    for (String name : currentHero.getArmours()) {
      Armour armour = armours.getArmour(name);
      if (armour != null) {
        weight += armour.getWeight();
      }
    }
    float weightStones = weight / 40.0f;
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(3);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);
    sumLabel.setText("Gesamt:  RS " + currentHero.getRS() + "  BE "
        + currentHero.getBE() + "  Gewicht " + format.format(weightStones)
        + " Stein");
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Armours armours = Armours.getInstance();
      for (String name : currentHero.getArmours()) {
        Armour armour = armours.getArmour(name);
        if (armour != null) {
          mTable.addArmour(armour);
        }
        else
          mTable.addUnknownArmour(name);
      }
      calcSums();
      removeButton.setEnabled(currentHero.getArmours().length > 0);
      addButton.setEnabled(true);
    }
    else {
      sumLabel.setText("Gesamt: RS 0; BE 0; Gewicht 0 Stein");
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
      if (currentHero != null) currentHero.removeHeroObserver(myHeroObserver);
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }

  public void optionsChanged() {
    updateData();
  }
}

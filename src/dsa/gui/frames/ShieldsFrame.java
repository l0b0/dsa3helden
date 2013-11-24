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
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.ShieldSelectionDialog;
import dsa.gui.dialogs.SelectionDialogBase.SelectionDialogCallback;
import dsa.gui.tables.ShieldsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Shield;
import dsa.model.data.Shields;

public class ShieldsFrame extends SubFrame implements CharactersObserver {

  private class MyObserver extends CharacterAdapter {
    public void bfChanged(String item) {
      updateData();
    }

    public void shieldRemoved(String item) {
      updateData();
    }
  }

  private MyObserver myObserver = new MyObserver();

  public ShieldsFrame() {
    super("Parade");
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Parade");
        Group.getInstance().removeObserver(ShieldsFrame.this);
        if (currentHero != null) currentHero.removeHeroObserver(myObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Parade");
          Group.getInstance().removeObserver(ShieldsFrame.this);
          if (currentHero != null) currentHero.removeHeroObserver(myObserver);
          done = true;
        }
      }
    });
    mTable = new ShieldsTable();
    JPanel panel = mTable.getPanelWithTable();

    // JPanel lowerPanel = new JPanel();
    // lowerPanel.setLayout(null);
    // lowerPanel.setPreferredSize(new java.awt.Dimension(150, 40));
    // lowerPanel.add(getSumLabel(), null);
    // panel.add(lowerPanel, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getRemoveButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);

    updateData();
    mTable.restoreSortingState("Parade");
    mTable.setFirstSelectedRow();
  }

  ShieldsTable mTable;

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
      addButton.setToolTipText("Schild / Parierwaffe hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addShield();
        }
      });
    }
    return addButton;
  }

  protected void addShield() {
    ShieldSelectionDialog dialog = new ShieldSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void ItemSelected(String item) {
        if (Arrays.asList(currentHero.getShields()).contains(item)) {
          JOptionPane
              .showMessageDialog(
                  ShieldsFrame.this,
                  "Ein Held kann jedes Paradewerkzeug nur einfach tragen.\nDu kannst es statt dessen zu den\nAusrüstungsgegenständen hinzufügen.",
                  "Fehler", JOptionPane.WARNING_MESSAGE);
          return;
        }
        currentHero.addShield(item);
        Shield Shield = Shields.getInstance().getShield(item);
        if (Shield != null)
          mTable.addShield(Shield);
        else
          mTable.addUnknownShield(item);
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
      removeButton.setToolTipText("Paradewerkzeug entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          currentHero.removeShield(name);
        }
      });
    }
    return removeButton;
  }

  private void calcSums() {
    int weight = 0;
    Shields shields = Shields.getInstance();
    for (String name : currentHero.getShields()) {
      Shield shield = shields.getShield(name);
      if (shield != null) {
        weight += shield.getWeight();
      }
    }
    // float weightStones = weight / 40.0f;
    // sumLabel.setText("Gesamt: RS " + currentHero.GetRS() + " BE " +
    // currentHero.GetBE()
    // + " Gewicht " + weightStones + " Stein");
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Shields shields = Shields.getInstance();
      for (String name : currentHero.getShields()) {
        Shield shield = shields.getShield(name);
        if (shield != null) {
          mTable.addShield(shield, currentHero.getBF(name));
        }
        else
          mTable.addUnknownShield(name);
      }
      calcSums();
      removeButton.setEnabled(currentHero.getShields().length > 0);
      addButton.setEnabled(true);
    }
    else {
      // sumLabel.setText("Gesamt: RS 0; BE 0; Gewicht 0 Stein");
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (currentHero != null) currentHero.removeHeroObserver(myObserver);
    currentHero = newCharacter;
    if (currentHero != null) currentHero.addHeroObserver(myObserver);
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      if (currentHero != null) currentHero.removeHeroObserver(myObserver);
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }
}

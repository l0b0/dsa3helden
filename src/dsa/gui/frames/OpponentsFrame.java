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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import dsa.gui.dialogs.OpponentDialog;
import dsa.gui.dialogs.OpponentSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.tables.OpponentTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.GroupObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Opponent;
import dsa.model.data.Opponents;

public final class OpponentsFrame extends SubFrame 
    implements GroupObserver, OpponentTable.ValueChanger, OpponentDialog.NameChecker {

  private final class OpponentSelectionCallback implements SelectionDialogCallback {
    public void itemSelected(String item) {
      Opponent o = Opponents.getOpponentsDB().getOpponent(item);
      if (o == null) return;
      Opponent newOpponent  = Group.getInstance().addOpponent(o);
      mTable.addOpponent(newOpponent);
      removeButton.setEnabled(true);
      editButton.setEnabled(true);
    }

    public void itemChanged(String item) {
    }
  }

  public String getHelpPage() {
    return "Gegner";
  }

  public void groupLoaded() {
    updateData();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
  }

  public void characterAdded(Hero character) {
  }

  public void characterRemoved(Hero character) {
  }

  public void globalLockChanged() {
  }
  
  public OpponentsFrame() {
    super("Gegner");
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Gegner");
        Group.getInstance().removeObserver(OpponentsFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Gegner");
          Group.getInstance().removeObserver(OpponentsFrame.this);
          done = true;
        }
      }
    });
    mTable = new OpponentTable(true);
    mTable.setValueChanger(this);
    JPanel panel = mTable.getPanelWithTable();

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getCreateButton(), null);
    rightPanel.add(getEditButton(), null);
    rightPanel.add(getRemoveButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);
    updateData();
    mTable.restoreSortingState("Gegner");
    mTable.setFirstSelectedRow();
  }
  
  OpponentTable mTable;

  JButton addButton;
  
  JButton createButton;

  JButton removeButton;
  
  JButton editButton;

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText("Gegner hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }
  
  JButton getCreateButton() {
    if (createButton == null) {
      createButton = new JButton(ImageManager.getIcon("tsa"));
      createButton.setBounds(5, 35, 60, 25);
      createButton.setToolTipText("Gegner erstellen");
      createButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OpponentDialog dialog = new OpponentDialog(OpponentsFrame.this, 
              OpponentsFrame.this, null);
          dialog.setVisible(true);
          Opponent newOpponent = dialog.getNewOpponent();
          if (newOpponent != null) {
            Group.getInstance().addOpponent(newOpponent);
            mTable.addOpponent(newOpponent);
            removeButton.setEnabled(true);
            editButton.setEnabled(true);
          }
        }
      });
    }
    return createButton;
  }

  protected void selectItem() {
    OpponentSelectionDialog dialog = new OpponentSelectionDialog(this);
    dialog.setCallback(new OpponentSelectionCallback());
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(5, 95, 60, 25);
      removeButton.setToolTipText("Gegner entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          Group.getInstance().removeOpponent(name);
          mTable.removeOpponent(name);
          removeButton.setEnabled(Group.getInstance().getOpponentNames().size() > 0);
          editButton.setEnabled(Group.getInstance().getOpponentNames().size() > 0);
        }
      });
    }
    return removeButton;
  }
  
  JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setBounds(5, 65, 60, 25);
      editButton.setToolTipText("Gegner bearbeiten");
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          Opponent o = Group.getInstance().getOpponent(name);
          OpponentDialog dialog = new OpponentDialog(OpponentsFrame.this, 
              OpponentsFrame.this, o);
          dialog.setVisible(true);
          if (dialog.getNewOpponent() != null) {
            Group.getInstance().replaceOpponent(name, dialog.getNewOpponent());
            updateData();
          }
        }
      });
    }
    return editButton;
  }

  private void updateData() {
    mTable.clear();
    for (String name : Group.getInstance().getOpponentNames()) {
      Opponent o = Group.getInstance().getOpponent(name);
      mTable.addOpponent(o);
    }
    mTable.setFirstSelectedRow();
    removeButton.setEnabled(Group.getInstance().getOpponentNames().size() > 0);
    editButton.setEnabled(Group.getInstance().getOpponentNames().size() > 0);
  }

  public void leChanged(String item, int le) {
    Opponent o = Group.getInstance().getOpponent(item);
    if (o != null) o.setLE(le);
  }

  public void mrChanged(String item, int mr) {
    Opponent o = Group.getInstance().getOpponent(item);
    if (o != null) o.setMR(mr);
  }

  public void rsChanged(String item, int rs) {
    Opponent o = Group.getInstance().getOpponent(item);
    if (o != null) o.setRS(rs);
  }

  public boolean canUseName(String newName) {
    Opponent o = Group.getInstance().getOpponent(newName);
    if (o != null) {
      javax.swing.JOptionPane.showMessageDialog(this, "Einen Gegner mit diesem Namen gibt es bereits.", "Heldenverwaltung", 
          javax.swing.JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    else return true;
  }

  public void categoryChanged(String item, String category) {
    Opponent o = Group.getInstance().getOpponent(item);
    if (o != null) o.setCategory(category);
  }

  public void nameChanged(String oldName, String newName) {
    Opponent o = Group.getInstance().getOpponent(oldName);
    if (o != null) o.setName(newName);
  }

  public void orderChanged() {
  }

}
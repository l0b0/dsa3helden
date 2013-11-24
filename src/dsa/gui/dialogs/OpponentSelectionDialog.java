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

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.tables.OpponentTable;
import dsa.gui.util.ImageManager;
import dsa.model.data.Opponent;
import dsa.model.data.Opponents;

public class OpponentSelectionDialog extends AbstractSelectionDialog 
    implements OpponentDialog.NameChecker {

  public OpponentSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Gegner hinzufügen", new OpponentTable(false), 
        "Gegner");
    initialize();
    fillTable();
  }

  public String getHelpPage() {
    return "Gegner_hinzufuegen";
  }

  protected void fillTable() {
    Opponents opponents = Opponents.getOpponentsDB();
    OpponentTable table = (OpponentTable) mTable;
    listen = false;
    for (String opponent : opponents.getOpponentNames()) {
      if (isDisplayed(opponent)) table.addOpponent(opponents.getOpponent(opponent));
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
      newButton.setToolTipText("Neuen Gegner anlegen");
      newButton.setBounds(315, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OpponentDialog dialog = new OpponentDialog(OpponentSelectionDialog.this, 
              OpponentSelectionDialog.this, null);
          dialog.setVisible(true);
          if (dialog.getNewOpponent() != null) {
            Opponents.getOpponentsDB().addOpponent(dialog.getNewOpponent());
            ((OpponentTable) mTable).addOpponent(dialog.getNewOpponent());
          }
        }
      });
    }
    return newButton;
  }

  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Gegner bearbeiten");
      editButton.setBounds(405, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Opponent opponent = Opponents.getOpponentsDB().getOpponent(mTable.getSelectedItem());
          if (opponent == null) return;
          OpponentDialog dialog = new OpponentDialog(OpponentSelectionDialog.this, 
              OpponentSelectionDialog.this, opponent);
          dialog.setVisible(true);
          if (dialog.getNewOpponent() != null) {
            ((OpponentTable) mTable).removeOpponent(opponent.getName());
            ((OpponentTable) mTable).addOpponent(dialog.getNewOpponent());
            Opponents.getOpponentsDB().removeOpponent(opponent.getName());
            Opponents.getOpponentsDB().addOpponent(dialog.getNewOpponent());
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
      deleteButton.setToolTipText("Gegner löschen");
      deleteButton.setBounds(360, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeOpponent();
        }
      });
    }
    return deleteButton;
  }

  protected void removeOpponent() {
    String opponent = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane
        .showConfirmDialog(
            this,
            "Gegner dieses Typs werden nicht aus der Gruppe entfernt. Sicher?",
            "Gegner entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    ((OpponentTable) mTable).removeSelectedOpponent();
    Opponents.getOpponentsDB().removeOpponent(opponent);
  }

  private void updateDeleteButton() {
    String opponent = mTable.getSelectedItem();
    boolean enabled = (opponent != null) && (opponent.length() > 0)
                    && Opponents.getOpponentsDB().getOpponent(opponent) != null 
                    && Opponents.getOpponentsDB().getOpponent(opponent).isUserDefined();
    getDeleteButton().setEnabled(enabled);
    getEditButton().setEnabled(enabled);
  }

  public boolean canUseName(String name) {
    if (Opponents.getOpponentsDB().getOpponent(name) != null) {
      JOptionPane.showMessageDialog(this, "Ein Gegner dieses Namens existiert bereits.", "Heldenverwaltung", JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    else return true;
  }
}

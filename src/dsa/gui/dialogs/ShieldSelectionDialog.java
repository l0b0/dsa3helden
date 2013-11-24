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
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.tables.ShieldsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.data.Shield;
import dsa.model.data.Shields;

public final class ShieldSelectionDialog extends AbstractSelectionDialog {

  public ShieldSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Schild / Parierwaffe hinzufügen", new ShieldsTable(),
        "Parade");
    initialize();
    fillTable();
  }

  protected void fillTable() {
    Shields shields = Shields.getInstance();
    ShieldsTable table = (ShieldsTable) mTable;
    listen = false;
    for (Shield shield : shields.getAllShields()) {
      if (isDisplayed(shield.getName())) table.addShield(shield);
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
      newButton.setToolTipText("Neues Schild / neue Parierwaffe anlegen");
      newButton.setBounds(315, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ShieldDialog dialog = new ShieldDialog(ShieldSelectionDialog.this);
          dialog.setVisible(true);
          if (dialog.getCreatedShield() != null) {
            Shields.getInstance().addShield(dialog.getCreatedShield());
            ((ShieldsTable) mTable).addShield(dialog.getCreatedShield());
          }
        }
      });
    }
    return newButton;
  }

  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Schild / Parierwaffe bearbeiten");
      editButton.setBounds(405, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Shield shield = Shields.getInstance().getShield(mTable.getSelectedItem());
          if (shield == null) return;
          ShieldDialog dialog = new ShieldDialog(ShieldSelectionDialog.this, shield);
          dialog.setVisible(true);
          if (dialog.getCreatedShield() != null) {
            ((ShieldsTable) mTable).removeShield(shield.getName());
            ((ShieldsTable) mTable).addShield(shield);
            if (getCallback() != null) {
              getCallback().itemChanged(shield.getName());
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
      deleteButton.setToolTipText("Schild / Parierwaffe löschen");
      deleteButton.setBounds(360, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeShield();
        }
      });
    }
    return deleteButton;
  }

  protected void removeShield() {
    String shield = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane
        .showConfirmDialog(
            this,
            "Schild / Parierwaffe wird bei allen geladenen Helden entfernt. Sicher?",
            "Paradehilfe entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    for (dsa.model.characters.Hero c : Group.getInstance().getAllCharacters()) {
      c.removeShield(shield);
    }
    ((ShieldsTable) mTable).removeSelectedShield();
    Shields.getInstance().removeShield(shield);
  }

  private void updateDeleteButton() {
    String shield = mTable.getSelectedItem();
    boolean enabled = shield != null && shield.length() > 0
                    && Shields.getInstance().getShield(shield).isUserDefined();
    getDeleteButton().setEnabled(enabled);
    getEditButton().setEnabled(enabled);
  }

}

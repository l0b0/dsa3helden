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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.tables.ArmoursTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.data.Armour;
import dsa.model.data.Armours;

public final class ArmourSelectionDialog extends AbstractSelectionDialog {

  public ArmourSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Rüstung hinzufügen", new ArmoursTable(), "Rüstungen");
    initialize();
    fillTable();
  }

  protected void fillTable() {
    Armours armours = Armours.getInstance();
    ArmoursTable table = (ArmoursTable) mTable;
    listen = false;
    for (Armour armour : armours.getAllArmours()) {
      if (isDisplayed(armour.getName())) table.addArmour(armour);
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
      newButton.setToolTipText("Neue Rüstung anlegen");
      newButton.setBounds(315, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ArmourDialog dialog = new ArmourDialog(ArmourSelectionDialog.this);
          dialog.setVisible(true);
          if (dialog.getArmour() != null) {
            Armours.getInstance().addArmour(dialog.getArmour());
            ((ArmoursTable) mTable).addArmour(dialog.getArmour());
          }
        }
      });
    }
    return newButton;
  }
  
  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Rüstung bearbeiten");
      editButton.setBounds(405, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Armour armour = Armours.getInstance().getArmour(mTable.getSelectedItem());
          if (armour == null) return;
          ArmourDialog dialog = new ArmourDialog(ArmourSelectionDialog.this, armour);
          dialog.setVisible(true);
          if (dialog.getArmour() != null) {
            ((ArmoursTable) mTable).removeArmour(armour.getName());
            ((ArmoursTable) mTable).addArmour(armour);
            if (getCallback() != null) {
              getCallback().itemChanged(armour.getName());
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
      deleteButton.setToolTipText("Rüstung löschen");
      deleteButton.setBounds(360, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeArmour();
        }
      });
    }
    return deleteButton;
  }

  protected void removeArmour() {
    String armour = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane.showConfirmDialog(this,
        "Rüstung wird bei allen geladenen Helden entfernt. Sicher?",
        "Rüstung entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    for (dsa.model.characters.Hero c : Group.getInstance().getAllCharacters()) {
      c.removeArmour(armour);
    }
    ((ArmoursTable) mTable).removeSelectedArmour();
    Armours.getInstance().removeArmour(armour);
  }

  private void updateDeleteButton() {
    String armour = mTable.getSelectedItem();
    boolean enabled = armour != null && armour.length() > 0
                    && Armours.getInstance().getArmour(armour).isUserDefined();
    getDeleteButton().setEnabled(enabled);
    getEditButton().setEnabled(enabled);
  }

}

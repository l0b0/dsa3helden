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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.tables.WeaponsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;

public final class WeaponsSelectionDialog extends AbstractSelectionDialog {

  public WeaponsSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Waffe hinzufügen", new WeaponsTable(false), "Waffen");
    initialize();
    fillTable();
  }

  protected void fillTable() {
    Weapons weapons = Weapons.getInstance();
    WeaponsTable table = (WeaponsTable) mTable;
    listen = false;
    for (Weapon weapon : weapons.getAllWeapons()) {
      if (isDisplayed(weapon.getName())) table.addWeapon(weapon, 1);
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
      newButton.setToolTipText("Neue Waffe anlegen");
      newButton.setBounds(315, 5, 40, 25);
      newButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          WeaponDialog dialog = new WeaponDialog(WeaponsSelectionDialog.this);
          dialog.setVisible(true);
          if (dialog.getCreatedWeapon() != null) {
            Weapons.getInstance().addWeapon(dialog.getCreatedWeapon());
            ((WeaponsTable) mTable).addWeapon(dialog.getCreatedWeapon(), 1);
          }
        }
      });
    }
    return newButton;
  }

  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setToolTipText("Waffe bearbeiten");
      editButton.setBounds(405, 5, 40, 25);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Weapon weapon = Weapons.getInstance().getWeapon(mTable.getSelectedItem());
          if (weapon == null) return;
          WeaponDialog dialog = new WeaponDialog(WeaponsSelectionDialog.this, weapon);
          dialog.setVisible(true);
          if (dialog.getCreatedWeapon() != null) {
            ((WeaponsTable) mTable).removeWeapon(weapon.getName());
            ((WeaponsTable) mTable).addWeapon(weapon, 1);
            if (getCallback() != null) {
              getCallback().itemChanged(weapon.getName());
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
      deleteButton.setToolTipText("Waffe löschen");
      deleteButton.setBounds(360, 5, 40, 25);
      deleteButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeWeapon();
        }
      });
    }
    return deleteButton;
  }

  protected void removeWeapon() {
    String weapon = mTable.getSelectedItem();
    int result = javax.swing.JOptionPane.showConfirmDialog(this,
        "Waffe wird bei allen geladenen Helden entfernt. Sicher?",
        "Waffe entfernen", javax.swing.JOptionPane.YES_NO_OPTION);
    if (result == javax.swing.JOptionPane.NO_OPTION) return;
    for (dsa.model.characters.Hero c : Group.getInstance()
        .getAllCharacters()) {
      while (c.getWeaponCount(weapon) > 0)
        c.removeWeapon(weapon);
    }
    ((WeaponsTable) mTable).removeSelectedWeapon();
    Weapons.getInstance().removeWeapon(weapon);
  }

  private void updateDeleteButton() {
    String weapon = mTable.getSelectedItem();
    boolean enabled = weapon != null && weapon.length() > 0
                    && Weapons.getInstance().getWeapon(weapon).isUserDefined();
    getDeleteButton().setEnabled(enabled);
    getEditButton().setEnabled(enabled);
  }

}

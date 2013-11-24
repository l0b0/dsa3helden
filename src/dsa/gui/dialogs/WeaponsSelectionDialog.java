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

public class WeaponsSelectionDialog extends SelectionDialogBase {

  public WeaponsSelectionDialog(javax.swing.JFrame owner) {
    super(owner, "Waffe hinzufügen", new WeaponsTable(false), "Waffen");

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
    mTable.addSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!listen) return;
        updateDeleteButton();
      }
    });
  }

  private JButton addButton;

  private JButton deleteButton;

  private JButton getNewButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setToolTipText("Neue Waffe anlegen");
      addButton.setBounds(315, 5, 40, 25);
      addButton.addActionListener(new ActionListener() {
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
    return addButton;
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
    getDeleteButton().setEnabled(
        weapon != null && weapon.length() > 0
            && Weapons.getInstance().getWeapon(weapon).isUserDefined());
  }

}

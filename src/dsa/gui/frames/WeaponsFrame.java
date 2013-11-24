/*
 Copyright (c) 2006-2008 [Joerg Ruedenauer]
 
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
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.WeaponsSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.tables.WeaponsTable;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.gui.util.OptionsChange.OptionsListener;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.ExtraThingData;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.data.Thing.Currency;
import dsa.util.Optional;

public final class WeaponsFrame extends AbstractDnDFrame implements
    CharactersObserver, OptionsListener, WeaponsTable.ValueChanger {

  private class MyHeroObserver extends dsa.model.characters.CharacterAdapter {
    public void weaponRemoved(String weapon) {
      mTable.removeWeapon(weapon);
    }

    public void bfChanged(String item) {
      updateData();
    }
    
    public void thingsChanged() {
      updateData();
    }
  }

  private final MyHeroObserver myHeroObserver = new MyHeroObserver();

  public WeaponsFrame() {
    super(ThingTransfer.Flavors.Weapon, "Waffen");
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    OptionsChange.addListener(this);
    if (currentHero != null) currentHero.addHeroObserver(myHeroObserver);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Waffen");
        Group.getInstance().removeObserver(WeaponsFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myHeroObserver);
        OptionsChange.removeListener(WeaponsFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Waffen");
          Group.getInstance().removeObserver(WeaponsFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myHeroObserver);
          OptionsChange.removeListener(WeaponsFrame.this);
          done = true;
        }
      }
    });
    createUI();
  }
  
  private void createUI() {
    mTable = new WeaponsTable(true, this);
    JPanel panel = mTable.getPanelWithTable();

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getRemoveButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);
    
    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(150, 40));
    lowerPanel.add(getSumLabel(), null);
    panel.add(lowerPanel, BorderLayout.SOUTH);

    this.setContentPane(panel);

    registerForDnD(mTable);

    updateData();
    mTable.restoreSortingState("Waffen");
    mTable.setFirstSelectedRow();    
  }

  public String getHelpPage() {
    return "Waffen";
  }

  WeaponsTable mTable;

  Hero currentHero;

  JButton addButton;

  JButton removeButton;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel("");
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 440, 25);
    }
    return sumLabel;
  }
  
  private void calcSums() {
    if (currentHero == null) {
      sumLabel.setText("Gesamt: Gewicht 0 Stein, Wert 0 S");
      return;
    }
    int weaponWeight = 0;
    int projectileWeight = 0;
    int weaponWorth = 0;
    int projectileWorth = 0;
    for (String n : currentHero.getWeapons()) {
      Weapon w = Weapons.getInstance().getWeapon(n);
      if (w == null) continue;
      weaponWeight += w.getWeight();
      if (w.getWorth().hasValue()) {
        weaponWorth += w.getWorth().getValue();
      }
      if (w.isProjectileWeapon()) {
        int nrOfProjectiles = currentHero.getNrOfProjectiles(n);
        if (w.getProjectileWeight().hasValue()) {
          projectileWeight += nrOfProjectiles * w.getProjectileWeight().getValue();
        }
        if (w.getProjectileWorth().hasValue()) {
          projectileWorth += nrOfProjectiles * w.getProjectileWorth().getValue();
        }
      }
    }
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);
    double overallWeight = (weaponWeight + projectileWeight) / 40.0;
    double overallWorth = (weaponWorth + projectileWorth / 100) / 10.0;
    String text = "Gesamt: Gewicht ca. " + format.format(overallWeight)
      + " Stein, Wert ca. " + format.format(overallWorth) + " Dukaten";
    sumLabel.setText(text);
  }
 
  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText("Waffe hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }

  protected void selectItem() {
    WeaponsSelectionDialog dialog = new WeaponsSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        weaponSelected(item, 1);
      }

      public void itemChanged(String item) {
        weaponChanged(item);
      }

      @Override
      public void itemsBought(String item, int count, int finalPrice,
          Currency currency) {
        weaponSelected(item, count);
        currentHero.pay(finalPrice, currency);
      }
    });
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(5, 35, 60, 25);
      removeButton.setToolTipText("Waffe entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          currentHero.removeWeapon(name);
          removeButton.setEnabled(currentHero.getWeapons().length > 0);
          updateData();
        }
      });
    }
    return removeButton;
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Weapons weapons = Weapons.getInstance();
      for (String name : currentHero.getWeapons()) {
        Weapon weapon = weapons.getWeapon(name);
        if (weapon != null) {
          Optional<Integer> projectiles = new Optional<Integer>();
          if (weapon.isProjectileWeapon()) {
            projectiles.setValue(currentHero.getNrOfProjectiles(name));
          }
          mTable.addWeapon(weapon, name, currentHero.getBF(name, 1),
              currentHero.getWeaponCount(name), projectiles);
        }
        else
          mTable.addUnknownWeapon(name);
      }
      removeButton.setEnabled(!currentHero.isDifference() && currentHero.getWeapons().length > 0);
      addButton.setEnabled(!currentHero.isDifference());
    }
    else {
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
    calcSums();
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
    createUI();
    validate();
  }

  private void weaponSelected(String item, int count) {
    Weapon weapon = Weapons.getInstance().getWeapon(item);
    if (weapon != null
        && Weapons.getCategoryName(weapon.getType()).equals("Schußwaffen")
        && weapon.getKKBonus().hasValue()
        && weapon.getKKBonus().getValue() > currentHero
            .getCurrentProperty(Property.KK)) {
      JOptionPane.showMessageDialog(WeaponsFrame.this, currentHero.getName()
          + " ist zu schwach, um diese Waffe zu benutzen!", "Fehler",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    for (int i = 0; i < count; ++i) {
      String name = currentHero.addWeapon(item);
      if (weapon != null && currentHero.getWeaponCount(item) == 1) {
        Optional<Integer> projectiles = new Optional<Integer>();
        if (weapon.isProjectileWeapon()) {
          projectiles.setValue(0);
        }
        mTable.addWeapon(weapon, name, currentHero.getBF(name, 1), currentHero
            .getWeaponCount(name), projectiles);
      }
      else if (weapon != null)
        mTable.setWeaponCount(item, currentHero.getWeaponCount(item));
      else
        mTable.addUnknownWeapon(item);
    }
    removeButton.setEnabled(true);
    calcSums();
  }

  private void weaponChanged(String item) {
    // for now, take the easy way ...
    updateData();
    currentHero.fireWeightChanged();
  }

  @Override
  protected boolean addItem(String item, ExtraThingData extraData) {
    Weapon weapon = Weapons.getInstance().getWeapon(item);
    if (weapon == null) {
      return false;
    }
    String name = currentHero.addWeapon(item);
    Optional<Integer> projectiles = new Optional<Integer>();
    int projNr = 0;
    int bf = 0;
    if (extraData.getType() == ExtraThingData.Type.Weapon) {
      try {
        bf = extraData.getPropertyInt("BF");
        projNr = extraData.getPropertyInt("Projectiles");
      }
      catch (ExtraThingData.PropertyException e) {
        e.printStackTrace();
      }
    }
    if (currentHero.getWeaponCount(item) == 1) {
      if (weapon.isProjectileWeapon()) {
        projectiles.setValue(projNr);
      }
      mTable.addWeapon(weapon, name, currentHero.getBF(name, 1), currentHero
          .getWeaponCount(name), projectiles);
    }
    else {
      mTable.setWeaponCount(item, currentHero.getWeaponCount(item));
    }
    if (extraData.getType() == ExtraThingData.Type.Weapon) {
      // setBF will update the table, so projectile number must
      // be set before!
      currentHero.setNrOfProjectiles(name, projNr);
      currentHero.setBF(name, 0, bf);
    }
    removeButton.setEnabled(true);
    calcSums();
    return true;
  }

  @Override
  protected void removeItem(String item) {
    currentHero.removeWeapon(item);
    removeButton.setEnabled(currentHero.getWeapons().length > 0);
    updateData();
  }

  @Override
  protected ExtraThingData getExtraDnDData(String item) {
    ExtraThingData data = new ExtraThingData(ExtraThingData.Type.Weapon);
    data.setProperty("BF", currentHero.getBF(item, 1));
    data.setProperty("Projectiles", currentHero.getNrOfProjectiles(item));
    Weapon weapon = Weapons.getInstance().getWeapon(item);
    if (weapon != null) {
      data.setProperty("Worth", weapon.getWorth().hasValue() ? weapon
          .getWorth().getValue() : 0);
      data.setProperty("Weight", weapon.getWeight());
      data.setProperty("Category", "Waffe");
    }
    return data;
  }

  public void bfChanged(String name, int bf) {
    currentHero.setBF(name, 0, bf);
  }
  
  public void projectilesChanged(String name, int nrOfProjectiles) {
    currentHero.setNrOfProjectiles(name, nrOfProjectiles);
    calcSums();
  }
}

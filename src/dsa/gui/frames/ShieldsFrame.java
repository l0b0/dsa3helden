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
import java.util.Arrays;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.ShieldSelectionDialog;
import dsa.gui.dialogs.ShopDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.dialogs.ItemProviders.ShieldsProvider;
import dsa.gui.tables.ShieldsTable;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.ExtraThingData;
import dsa.model.data.IExtraThingData;
import dsa.model.data.Shield;
import dsa.model.data.Shields;

public final class ShieldsFrame extends AbstractDnDFrame implements CharactersObserver, ShieldsTable.BFChanger {

  private final class ShieldSelectionCallback implements SelectionDialogCallback {
    public void itemSelected(String item) {
      if (Arrays.asList(currentHero.getShields()).contains(item)) {
        JOptionPane
            .showMessageDialog(
                ShieldsFrame.this,
                Localization.getString("Parade.ParadeNurEinmal"), //$NON-NLS-1$
                Localization.getString("Parade.Fehler"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return;
      }
      itemAdded(item);
    }

    public void itemChanged(String item) {
      if (mTable.containsItem(item)) {
        mTable.removeShield(item);
        mTable.addShield(Shields.getInstance().getShield(item));
        currentHero.fireWeightChanged();
      }
    }

    private void itemAdded(String item) {
      shieldAdded(item);
    }
  }
  
  private void shieldAdded(String item) {
    currentHero.addShield(item);
    Shield shield = Shields.getInstance().getShield(item);
    if (shield != null)
      mTable.addShield(shield);
    else
      mTable.addUnknownShield(item);
    removeButton.setEnabled(true);
    calcSums();          
  }
  
  private boolean shieldBought(String item, int count) {
    boolean alreadyThere = Arrays.asList(currentHero.getShields()).contains(item);
    if (alreadyThere || count > 1) {
      JOptionPane
      .showMessageDialog(
          ShieldsFrame.this,
          Localization.getString("Parade.ParadeNurEinmal"), //$NON-NLS-1$
          Localization.getString("Parade.Fehler"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  private class MyObserver extends CharacterAdapter {
    public void bfChanged(String item) {
      updateData();
    }

    public void shieldRemoved(String item) {
      updateData();
    }
    
    public void thingsChanged() {
      updateData();
    }
  }

  private final MyObserver myObserver = new MyObserver();

  public ShieldsFrame() {
    super(ThingTransfer.Flavors.Shield, Localization.getString("Parade.Parade")); //$NON-NLS-1$
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Parade"); //$NON-NLS-1$
        Group.getInstance().removeObserver(ShieldsFrame.this);
        if (currentHero != null) currentHero.removeHeroObserver(myObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Parade"); //$NON-NLS-1$
          Group.getInstance().removeObserver(ShieldsFrame.this);
          if (currentHero != null) currentHero.removeHeroObserver(myObserver);
          done = true;
        }
      }
    });
    mTable = new ShieldsTable(this);
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
    rightPanel.add(getBuyButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);
    
    registerForDnD(mTable);

    updateData();
    mTable.restoreSortingState("Parade"); //$NON-NLS-1$
    mTable.setFirstSelectedRow();
  }
  
  public String getHelpPage() {
    return "Parade"; //$NON-NLS-1$
  }

  ShieldsTable mTable;

  Hero currentHero;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel(""); //$NON-NLS-1$
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 280, 25);
    }
    return sumLabel;
  }

  JButton addButton;

  JButton removeButton;
  
  JButton buyButton;

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase")); //$NON-NLS-1$
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled")); //$NON-NLS-1$
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText(Localization.getString("Parade.SchildHinzufuegen")); //$NON-NLS-1$
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }

  JButton getBuyButton() {
    if (buyButton == null) {
      buyButton = new JButton(ImageManager.getIcon("money")); //$NON-NLS-1$
      buyButton.setBounds(5, 35, 60, 25);
      buyButton.setToolTipText(Localization.getString("Parade.GegenstandKaufen")); //$NON-NLS-1$
      buyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          buyItem();
        }
      });
    }
    return buyButton;
  }
  
  private void buyItem() {
    ShopDialog dialog = new ShopDialog(this, new ShieldsProvider());
    dialog.setVisible(true);
    if (dialog.wasClosedByOK()) {
      Map<String, Integer> cart = dialog.getBoughtItems();
      boolean allowed = true;
      for (String item : cart.keySet()) {
        if (!shieldBought(item, cart.get(item))) {
          allowed = false;
          break;
        }
      }
      if (allowed) {
        for (String item : cart.keySet()) {
          shieldAdded(item);
        }
        currentHero.pay(dialog.getFinalPrice(), dialog.getCurrency());
      }
    }
  }

  protected void selectItem() {
    ShieldSelectionDialog dialog = new ShieldSelectionDialog(this);
    dialog.setCallback(new ShieldSelectionCallback());
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled")); //$NON-NLS-1$
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease")); //$NON-NLS-1$
      removeButton.setBounds(5, 65, 60, 25);
      removeButton.setToolTipText(Localization.getString("Parade.SchildEntfernen")); //$NON-NLS-1$
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
//    int weight = 0;
//    Shields shields = Shields.getInstance();
//    for (String name : currentHero.getShields()) {
//      Shield shield = shields.getShield(name);
//      if (shield != null) {
//        weight += shield.getWeight();
//      }
//    }
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
          mTable.addShield(shield, name, currentHero.getBF(name));
        }
        else
          mTable.addUnknownShield(name);
      }
      calcSums();
      removeButton.setEnabled(!currentHero.isDifference() && currentHero.getShields().length > 0);
      addButton.setEnabled(!currentHero.isDifference());
      buyButton.setEnabled(!currentHero.isDifference());
    }
    else {
      // sumLabel.setText("Gesamt: RS 0; BE 0; Gewicht 0 Stein");
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
      buyButton.setEnabled(false);
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

  @Override
  protected boolean addItem(String item, IExtraThingData extraData) {
    if (Arrays.asList(currentHero.getShields()).contains(item)) {
      JOptionPane
          .showMessageDialog(
              ShieldsFrame.this,
              Localization.getString("Parade.ParadeNurEinmal"), //$NON-NLS-1$
              Localization.getString("Parade.Fehler"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    Shield shield = Shields.getInstance().getShield(item);
    if (shield != null)
      mTable.addShield(shield);
    else
      return false;
    currentHero.addShield(item);
    if (extraData.getType() == ExtraThingData.Type.Shield) {
      try {
        currentHero.setBF(item, ((ExtraThingData)extraData).getPropertyInt("BF")); //$NON-NLS-1$
      }
      catch (ExtraThingData.PropertyException e) {
        e.printStackTrace();
      }
    }
    removeButton.setEnabled(true);
    calcSums();
    return true;
  }

  @Override
  protected void removeItem(String item, boolean alwaysWithContents) {
    currentHero.removeShield(item);
  }

  @Override
  protected IExtraThingData getExtraDnDData(String item) {
    ExtraThingData data = new ExtraThingData(ExtraThingData.Type.Shield);
    data.setProperty("BF", currentHero.getBF(item)); //$NON-NLS-1$
    Shield shield = Shields.getInstance().getShield(item);
    if (shield != null) {
      data.setProperty("Worth", shield.getWorth()); //$NON-NLS-1$
      data.setProperty("Weight", shield.getWeight()); //$NON-NLS-1$
      data.setProperty("Category", shield.getFkMod() > 0 ? "Rüstung" : "Waffe"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      data.setProperty("Singular", shield.isSingular() ? 1 : 0); //$NON-NLS-1$
    }
    return data;
  }

  public void bfChanged(String name, int bf) {
    currentHero.setBF(name, bf);
  }
}

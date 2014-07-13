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
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.ClothSelectionDialog;
import dsa.gui.dialogs.ShopDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.dialogs.ItemProviders.ClothesProvider;
import dsa.gui.tables.ClothesTable;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.gui.util.OptionsChange.OptionsListener;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.GroupOptions;
import dsa.model.characters.Hero;
import dsa.model.data.Cloth;
import dsa.model.data.Clothes;
import dsa.model.data.ExtraThingData;
import dsa.model.data.IExtraThingData;
import dsa.model.data.Thing;
import dsa.model.data.Things;

public final class ClothesFrame extends AbstractDnDFrame implements CharactersObserver, Things.ThingsListener, OptionsListener {

  private CharacterAdapter myHeroObserver;
  
  public ClothesFrame() {
    super(ThingTransfer.Flavors.Thing, Localization.getString("Kleidung.Kleidung")); //$NON-NLS-1$
    currentHero = Group.getInstance().getActiveHero();
    myHeroObserver = new CharacterAdapter() {
      public void thingsChanged() {
        updateData();
      }
      public void weightChanged() {
    	  updateData();
      }
    };
    if (currentHero != null) {
      currentHero.addHeroObserver(myHeroObserver);
    }
    Group.getInstance().addObserver(this);
    Things.getInstance().addObserver(this);
    OptionsChange.addListener(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Kleidung"); //$NON-NLS-1$
        Group.getInstance().removeObserver(ClothesFrame.this);
        Things.getInstance().removeObserver(ClothesFrame.this);
        OptionsChange.removeListener(ClothesFrame.this);
        if (currentHero != null) {
          currentHero.removeHeroObserver(myHeroObserver);
        }
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Kleidung"); //$NON-NLS-1$
          Group.getInstance().removeObserver(ClothesFrame.this);
          Things.getInstance().removeObserver(ClothesFrame.this);
          OptionsChange.removeListener(ClothesFrame.this);
          if (currentHero != null) {
            currentHero.removeHeroObserver(myHeroObserver);
          }
          done = true;
        }
      }
    });
    Initialize();
    mTable.setFirstSelectedRow();
  }
  
  private void Initialize() {
    mTable = new ClothesTable();
    registerForDnD(mTable);
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
    rightPanel.add(getBuyButton(), null);
    panel.add(rightPanel, BorderLayout.EAST);

    this.setContentPane(panel);

    updateData();
    mTable.restoreSortingState("Kleidung"); //$NON-NLS-1$
  }

  public String getHelpPage() {
    return "Kleidung"; //$NON-NLS-1$
  }

  ClothesTable mTable;

  Hero currentHero;

  JButton addButton;

  JButton removeButton;
  
  JButton buyButton;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel(""); //$NON-NLS-1$
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 350, 25);
    }
    return sumLabel;
  }
  
  private void calcSums() {
	  int ks = 0; int be = 0;
	  int weight = 0; int worth = 0;
	  if (currentHero != null) {
	      for (String cName : currentHero.getClothes()) {
	    	  Cloth cloth = Clothes.getInstance().getCloth(cName);
	    	  if (cloth != null) {
	    		 ks += cloth.getKS();
	    		 be += cloth.getBE();
	    	  }
	    	  Thing thing = Things.getInstance().getThing(cName);
	    	  if (thing != null) {
	    		  weight += thing.getWeight();
	    		  worth += thing.getValue().hasValue() ? thing.getValue().getValue().intValue() : 0;
	    	  }
	      }
	  }
      float weightStones = weight / 40.0f;
      float worthD = worth / 10.0f;
      NumberFormat format = NumberFormat.getNumberInstance();
      format.setGroupingUsed(true);
      format.setMaximumFractionDigits(3);
      format.setMinimumFractionDigits(0);
      format.setMinimumIntegerDigits(1);
      String text = Localization.getString("Kleidung.GesamtKS") + ks; //$NON-NLS-1$
      if (Group.getInstance().getOptions().getClothesBE() == GroupOptions.ClothesBE.Items) {
    	  text += Localization.getString("Kleidung.BE") + be; //$NON-NLS-1$
      }
      text += Localization.getString("Kleidung.Gewicht") + format.format(weightStones) //$NON-NLS-1$
          + Localization.getString("Kleidung.Stein") //$NON-NLS-1$
          + Localization.getString("Kleidung.Wert") + format.format(worthD) + Localization.getString("Kleidung.Dukaten"); //$NON-NLS-1$ //$NON-NLS-2$
      sumLabel.setText(text);
  }

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase")); //$NON-NLS-1$
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled")); //$NON-NLS-1$
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText(Localization.getString("Kleidung.KleidungHinzufuegen")); //$NON-NLS-1$
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }

  protected void selectItem() {
    ClothSelectionDialog dialog = new ClothSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        if (java.util.Arrays.asList(currentHero.getClothes()).contains(item)) {
          JOptionPane
              .showMessageDialog(
                  ClothesFrame.this,
                  Localization.getString("Kleidung.KleidungsArtNurEinmal"), //$NON-NLS-1$
                  Localization.getString("Kleidung.Fehler"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
          return;
        }
        clothesAdded(item);
      }
      
      public void itemChanged(String item) {
        Things.getInstance().thingChanged(item);
      }
    });
    dialog.setVisible(true);
  }
  
  protected void clothesAdded(String item) {
    Thing thing = Things.getInstance().getThing(item);
    if (thing != null && thing.getCategory().equals(Localization.getString("Kleidung.KleidungsKategorie"))) { //$NON-NLS-1$
      mTable.addThing(thing);
      currentHero.addClothes(item);
      removeButton.setEnabled(true);
      calcSums();
    }
  }
  
  protected boolean clothesBought(String item, int count) {
    boolean alreadyThere = java.util.Arrays.asList(currentHero.getClothes()).contains(item);
    if (alreadyThere || count > 1) {
      JOptionPane
      .showMessageDialog(
          ClothesFrame.this,
          Localization.getString("Kleidung.KleidungsArtNurEinmal"), //$NON-NLS-1$
          Localization.getString("Kleidung.Fehler"), JOptionPane.WARNING_MESSAGE);        //$NON-NLS-1$
      return false;
    }
    return true;
  }

  JButton getBuyButton() {
    if (buyButton == null) {
      buyButton = new JButton(ImageManager.getIcon("money")); //$NON-NLS-1$
      buyButton.setBounds(5, 35, 60, 25);
      buyButton.setToolTipText(Localization.getString("Kleidung.GegenstandKaufen")); //$NON-NLS-1$
      buyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          buyItem();
        }
      });
    }
    return buyButton;
  }
  
  private void buyItem() {
    ShopDialog dialog = new ShopDialog(this, new ClothesProvider());
    dialog.setVisible(true);
    if (dialog.wasClosedByOK()) {
      Map<String, Integer> cart = dialog.getBoughtItems();
      boolean allowed = true;
      for (String item : cart.keySet()) {
        if (!clothesBought(item, cart.get(item))) {
          allowed = false;
          break;
        }
      }
      if (allowed) {
        for (String item : cart.keySet()) {
          clothesAdded(item);
        }
        currentHero.pay(dialog.getFinalPrice(), dialog.getCurrency());
      }
    }
  }
  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled")); //$NON-NLS-1$
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease")); //$NON-NLS-1$
      removeButton.setBounds(5, 65, 60, 25);
      removeButton.setToolTipText(Localization.getString("Kleidung.KleidungEntfernen")); //$NON-NLS-1$
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          removeItem(name, false);
        }
      });
    }
    return removeButton;
  }

  private void updateData() {
    mTable.clear();
    if (currentHero != null) {
      Things things = Things.getInstance();
      for (String name : currentHero.getClothes()) {
        String realName = name;
        if (currentHero.isDifference()) {
          realName = dsa.util.Strings.getStringWithoutChangeTag(name);
        }
        Thing thing = things.getThing(realName);
        if (thing != null) {
          mTable.addThing(thing, name);
        }
        else
          mTable.addUnknownThing(name);
      }
      removeButton.setEnabled(isChangeAllowed() && currentHero.getClothes().length > 0);
      addButton.setEnabled(isChangeAllowed());
      buyButton.setEnabled(isChangeAllowed());
    }
    else {
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
      buyButton.setEnabled(false);
    }
    calcSums();
    mTable.setFirstSelectedRow();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (oldCharacter != null) {
      oldCharacter.removeHeroObserver(myHeroObserver);
    }
    currentHero = newCharacter;
    if (currentHero != null) {
      currentHero.addHeroObserver(myHeroObserver);
    }
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
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
    if (extraData.getType() != ExtraThingData.Type.Thing) {
      return false;
    }
    Thing thing = Things.getInstance().getThing(item);
    if (thing == null) {
      return false;
    }
    if (!thing.getCategory().equals(Localization.getString("Kleidung.KleidungsKategorie"))) { //$NON-NLS-1$
      return false;
    }
    if (java.util.Arrays.asList(currentHero.getClothes()).contains(item)) {
      JOptionPane
          .showMessageDialog(
              ClothesFrame.this,
              Localization.getString("Kleidung.KleidungsArtNurEinmal"), //$NON-NLS-1$
              Localization.getString("Kleidung.Fehler"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    currentHero.addClothes(item);
    mTable.addThing(thing);
    removeButton.setEnabled(true);
    calcSums();
    return true;
  }

  @Override
  protected void removeItem(String item, boolean alwaysWithContents) {
    currentHero.removeClothes(item);
    mTable.removeSelectedThing();
    removeButton.setEnabled(currentHero.getClothes().length > 0);
    calcSums();
  }

  @Override
  protected IExtraThingData getExtraDnDData(String item) {
    return new ExtraThingData(ExtraThingData.Type.Thing);
  }

  public void thingChanged(String thing) {
    if (mTable.containsItem(thing)) {
      mTable.removeThing(thing);
      mTable.addThing(Things.getInstance().getThing(thing));
      calcSums();
    }
  }

	@Override
	public void optionsChanged() {
		String selectedItem = mTable.getSelectedItem();
		Initialize();
		mTable.setSelectedItem(selectedItem);
	}
}

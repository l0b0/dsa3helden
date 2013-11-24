/*
 Copyright (c) 2006-2009 [Joerg Ruedenauer]
 
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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.dialogs.ShopDialog;
import dsa.gui.dialogs.ThingSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.dialogs.ItemProviders.ThingsProvider;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.tables.ThingsTable;
import dsa.gui.util.ImageManager;
import dsa.model.ThingCarrier;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.data.ExtraThingData;
import dsa.model.data.IExtraThingData;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.util.Optional;

abstract class AbstractThingsFrame extends AbstractDnDFrame implements CharactersObserver, Things.ThingsListener  {

  private class MyHeroObserver extends dsa.model.characters.CharacterAdapter {
    public void thingRemoved(String thing, boolean fromWarehouse) {
      updateData();
    }
    
    public void thingsChanged() {
      updateData();
    }
  }

  private final MyHeroObserver myHeroObserver = new MyHeroObserver();
  
  protected AbstractThingsFrame(ThingCarrier carrier, String container) {
    super(ThingTransfer.Flavors.Thing, container);
    mCarrier = carrier;
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Group.getInstance().addObserver(AbstractThingsFrame.this);
      }
    });
    Things.getInstance().addObserver(this);
    if (carrier != null) carrier.addObserver(myHeroObserver);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        mTable.saveSortingState("Ausrüstung");
        Group.getInstance().removeObserver(AbstractThingsFrame.this);
        Things.getInstance().removeObserver(AbstractThingsFrame.this);
        framesOfContainers.closeAllFrames();
        if (mCarrier != null)
          mCarrier.removeObserver(myHeroObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          mTable.saveSortingState("Ausrüstung");
          Group.getInstance().removeObserver(AbstractThingsFrame.this);
          Things.getInstance().removeObserver(AbstractThingsFrame.this);
          framesOfContainers.closeAllFrames();
          if (mCarrier != null)
            mCarrier.removeObserver(myHeroObserver);
          done = true;
        }
      }
    });
    mTable = new ThingsTable(true);
    registerForDnD(mTable);
    mTable.addSelectionListener(new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent arg0) {
        String selected = mTable.getSelectedItem();
        if (selected == null) {
          getEditButton().setEnabled(false);
        }
        else {
          Thing thing = Things.getInstance().getThing(selected);
          getEditButton().setEnabled(thing != null && thing.isContainer());
        }
      }
    });
    mTable.setDoubleClickListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (getEditButton().isEnabled()) {
          openItem();
        }
      }
    });

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(150, 40));
    lowerPanel.add(getSumLabel(), null);
    JPanel panel = mTable.getPanelWithTable();
    panel.add(lowerPanel, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getBuyButton(), null);
    rightPanel.add(getRemoveButton(), null);
    rightPanel.add(getEditButton(), null);
     
    panel.add(rightPanel, BorderLayout.EAST);
    
    mTable.restoreSortingState("Ausrüstung");
    mTable.setFirstSelectedRow();
    
    thingsPanel = panel;
  }
  
  protected abstract Rectangle getAddButtonPos();
  protected abstract Rectangle getBuyButtonPos();
  protected abstract Rectangle getRemoveButtonPos();
  protected abstract Rectangle getEditButtonPos();
  protected abstract Rectangle getSumLabelPos();
  
  private JPanel thingsPanel;
  
  protected JPanel getThingsPanel() {
    return thingsPanel;
  }
  
  ThingsTable mTable;

  ThingCarrier mCarrier;

  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel("");
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(getSumLabelPos());
    }
    return sumLabel;
  }

  JButton addButton;

  JButton removeButton;
  
  JButton editButton;
  
  JButton buyButton;
  
  JButton getBuyButton() {
    if (buyButton == null) {
      buyButton = new JButton(ImageManager.getIcon("money"));
      buyButton.setBounds(getBuyButtonPos());
      buyButton.setToolTipText("Gegenstand kaufen");
      buyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          buyItem();
        }
      });
    }
    return buyButton;
  }
  
  private void buyItem() {
    ShopDialog dialog = new ShopDialog(this, new ThingsProvider(true));
    dialog.setVisible(true);
    if (dialog.wasClosedByOK()) {
      Map<String, Integer> cart = dialog.getBoughtItems();
      for (String item : cart.keySet()) {
        addItem(item, new ExtraThingData(ExtraThingData.Type.Thing), cart.get(item));
      }
      mCarrier.pay(dialog.getFinalPrice(), dialog.getCurrency());
    }
  }
  
  JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton(ImageManager.getIcon("edit"));
      editButton.setBounds(getEditButtonPos());
      editButton.setToolTipText("Inhalt ansehen");
      editButton.setEnabled(false);
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          openItem();
        }
      });
    }
    return editButton;
  }
  
  private static class FramesOfContainers {
    
    private HashMap<String, ThingsFrame> framesList = new HashMap<String, ThingsFrame>();
    
    public void openFrame(ThingCarrier carrier, String container) {
      if (framesList.containsKey(container)) {
         framesList.get(container).toFront();
      }
      else {
        addFrame(carrier, container);
      }
    }
    
    public void closeFrame(String container) {
      if (framesList.containsKey(container)) {
        framesList.get(container).dispose();
        framesList.remove(container);
      }
    }
    
    public void closeAllFrames() {
      Set<String> copy = new HashSet<String>(framesList.keySet());
      for (String thing : copy) {
        closeFrame(thing);
      }
    }
    
    private void addFrame(ThingCarrier carrier, String container) {
      ThingsFrame newFrame = new ThingsFrame(carrier, container);
      newFrame.addWindowListener(new WindowAdapter() {
        private void removeFrame(Object frame) {
          for (String key : framesList.keySet()) {
            if (framesList.get(key) == frame) {
              framesList.remove(key);
              break;
            }
          }
        }
        public void windowClosed(WindowEvent arg0) {
          removeFrame(arg0.getSource());
        }
        public void windowClosing(WindowEvent arg0) {
          removeFrame(arg0.getSource());
        }
      });
      framesList.put(container, newFrame);
      newFrame.setVisible(true);
    }
  }
  
  private FramesOfContainers framesOfContainers = new FramesOfContainers();
  
  protected void openItem() {
    String name = mTable.getSelectedItem();
    Thing thing = Things.getInstance().getThing(name);
    if (thing.isContainer()) {
      framesOfContainers.openFrame(mCarrier, name);
    }
  }

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(getAddButtonPos());
      addButton.setToolTipText("Gegenstand hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }
  
  protected void selectItem() {
    ThingSelectionDialog dialog = new ThingSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        addItem(item, new ExtraThingData(ExtraThingData.Type.Thing), 1);
      }
      public void itemChanged(String item) {
        Things.getInstance().thingChanged(item);
      }
    });
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(getRemoveButtonPos());
      removeButton.setToolTipText("Gegenstand entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          removeItem(name, false);
        }
      });
    }
    return removeButton;
  }
  
  protected void calcSums() {
    sumLabel.setText(calcSumLabelText());
  }

  protected String calcSumLabelText() {
    long weight = 0;
    long value = 0;
    Things things = Things.getInstance();
    String[] names = mCarrier.getThingsInContainer(getTitle());
    for (String name : names) {
      Thing thing = things.getThing(name);
      if (thing != null) {
        long count = mCarrier.getThingCount(name);
        weight += count * (long) thing.getWeight();
        if (thing.getValue().hasValue()) {
          if (thing.getCurrency() == Thing.Currency.D)
            value += count * (long) thing.getValue().getValue() * 1000L;
          else if (thing.getCurrency() == Thing.Currency.S)
            value += count * (long) thing.getValue().getValue() * 100L;
          else if (thing.getCurrency() == Thing.Currency.K)
            value += count * (long) thing.getValue().getValue() * 10L;
          else if (thing.getCurrency() == Thing.Currency.H)
            value += count * (long) thing.getValue().getValue();
        }
      }
    }
    float weightStones = weight / 40.0f;
    float valueD = value / 1000.0f;

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);

    String text = "Gesamt: Wert ca. " + format.format(valueD)
        + " Dukaten, Gewicht ca. ";
    format.setMaximumFractionDigits(3);
    text += format.format(weightStones) + " Stein";
    return text;
  }

  protected void updateData() {
    mTable.clear();
    if (mCarrier != null) {
      Things things = Things.getInstance();
      for (String name : mCarrier.getThingsInContainer(getTitle())) {
        Thing thing = things.getThing(name);
        if (thing != null) {
          mTable.addThing(thing, name);
        }
        else
          mTable.addUnknownThing(name);
        mTable.setCount(name, mCarrier.getThingCount(name));
      }
      sumLabel.setText(calcSumLabelText());
      removeButton.setEnabled(!mCarrier.isDifference() && mCarrier.getThings().length > 0);
      addButton.setEnabled(!mCarrier.isDifference());
      buyButton.setEnabled(!mCarrier.isDifference());
    }
    else {
      sumLabel
          .setText("Gesamt: Wert 0 Dukaten, Gewicht 0 Stein");
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
      buyButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    mCarrier = newCharacter;
    if (oldCharacter != null) oldCharacter.removeHeroObserver(myHeroObserver);
    if (newCharacter != null) newCharacter.addHeroObserver(myHeroObserver);
    framesOfContainers.closeAllFrames();
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == mCarrier) {
      mCarrier = null;
      framesOfContainers.closeAllFrames();
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }
  
  protected boolean addItem(String item, IExtraThingData extraData) {
    return addItem(item, extraData, 1);
  }
  
  protected boolean addMovedItem(String item, IExtraThingData extraData) {
    if (!mCarrier.canMoveTo(item, getTitle())) {
      return false;
    }
    String newName = mCarrier.moveThing(item, getTitle());
    if (!addToTable(item, extraData, newName)) {
      return false;
    }
    calcSums();
    return true;
  }

  protected boolean addItem(String item, IExtraThingData extraData, int count) {
    if (extraData.getType() == ExtraThingData.Type.Weapon) {
      dsa.model.data.Weapon w = dsa.model.data.Weapons.getInstance().getWeapon(item);
      item = w.getName();
    }
    for (int i = 0;  i < count; ++i) {
      String newName = mCarrier.addThing(item, extraData, getTitle());
      if (!addToTable(item, extraData, newName)) {
        return false;
      }
    }
    removeButton.setEnabled(true);
    calcSums();
    return true;
  }

  /**
   * @param item
   * @param extraData
   * @param newName
   */
  private boolean addToTable(String item, IExtraThingData extraData, String newName) {
    if (mCarrier.getThingCount(newName) == 1) {
      Thing thing = Things.getInstance().getThing(newName);
      if (thing != null) {
        mTable.addThing(thing, newName);
      }
      else if (extraData.getType() != ExtraThingData.Type.Thing) {
        try {
          ExtraThingData data = (ExtraThingData)extraData;
          String category = data.getProperty("Category");
          int value = data.getPropertyInt("Worth");
          int weight = data.getPropertyInt("Weight");
          boolean singular = data.getPropertyInt("Singular") == 1;
          thing = new Thing(newName, new Optional<Integer>(value), Thing.Currency.S, weight, category, true, singular, false);
          Things.getInstance().addThing(thing);
          mTable.addThing(thing);
        }
        catch (ExtraThingData.PropertyException e) {
          e.printStackTrace();
          return false;
        }
      }
      else {
        JOptionPane.showMessageDialog(this, "Unbekannter Gegenstand.", 
            "Gegenstand hinzufügen", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    mTable.setCount(newName, mCarrier.getThingCount(newName));
    mTable.invalidate();
    return true;
  }
  
  protected IExtraThingData getExtraDnDData(String item) {
    return mCarrier.getExtraThingData(item, mCarrier.getThingCount(item));
  }

  protected void removeItem(String name, boolean alwaysWithContents) {
    int oldCount = mCarrier.getThingCount(name);
    boolean removeContents = false;
    Thing thing = Things.getInstance().getThing(name);
    boolean needsUpdate = false;
    if (thing != null && thing.isContainer() && mCarrier.getThingsInContainer(name).length > 0) {
      if (alwaysWithContents) {
        removeContents = true;
      }
      else { 
        switch (JOptionPane.showConfirmDialog(this, "Inhalt ebenfalls entfernen?", "Heldenverwaltung", 
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        case JOptionPane.YES_OPTION:
          removeContents = true;
          break;
        case JOptionPane.NO_OPTION:
          needsUpdate = true;
          break;
        default:
          return;
        }
      }
    }
    mCarrier.removeThing(name, removeContents);
    framesOfContainers.closeFrame(name);
    if (oldCount != 1) {
      mTable.setCount(name, oldCount - 1);
    }
    // else
      // mTable.RemoveSelectedThing();
    removeButton.setEnabled(mCarrier.getThings().length > 0);
    if (needsUpdate) {
      updateData();
    }
    calcSums();
  }
  
  protected void removeMovedItem(String name) {
    framesOfContainers.closeFrame(name);
    updateData();
    calcSums();
  }
  
  protected final String getCarrier() {
    return mCarrier.getName();
  }

  public void thingChanged(String thing) {
    if (mTable.containsItem(thing)) {
      mTable.removeThing(thing);
      mTable.addThing(Things.getInstance().getThing(thing));
      mTable.setCount(thing, mCarrier.getThingCount(thing));
      calcSums();
      mCarrier.fireWeightChanged();
      if (!Things.getInstance().getThing(thing).isContainer()) {
        framesOfContainers.closeFrame(thing);
      }
    }
  }
  
  protected final String getTopLevelContainer() {
    String container = getTitle();
    if (mCarrier == null) return container;
    while (mCarrier.getThingCount(container) > 0) {
      container = mCarrier.getThingContainer(container);
    }
    return container;
  }
}

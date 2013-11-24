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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.text.NumberFormatter;

import dsa.control.HeroStepIncreaser;
import dsa.gui.dialogs.ProbeDialog;
import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;

/**
 * 
 */
public final class PropertyFrame extends SubFrame implements CharactersObserver {

  private javax.swing.JPanel jContentPane = null;

  private final boolean goodProperties;

  /**
   * This is the default constructor
   */
  public PropertyFrame(String title, boolean goodProperties) {
    super(title);
    setTitle(title);
    this.goodProperties = goodProperties;
    properties = new ArrayList<Property>();
    defaultValues = new ArrayList<JFormattedTextField>();
    currentValues = new ArrayList<JFormattedTextField>();
    locks = new ArrayList<JToggleButton>();
    tests = new ArrayList<JButton>();
    // testAlls = new Vector<JButton>();
    increases = new ArrayList<JButton>();
    propertyDescriptions = new ArrayList<JLabel>();
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(PropertyFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(PropertyFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myCharacterObserver);
          done = true;
        }
      }
    });
    initialize();
    // pack();
  }
  
  public String getHelpPage() {
    return "Eigenschaften";
  }

  private boolean disableChange = false;

  /**
   * 
   * 
   */
  private void updateData() {
    disableChange = true;
    int displayIndex = 0;
    for (int i = 0; i < properties.size(); ++i) {
      if (currentHero != null) {
        defaultValues.get(displayIndex).setValue(
            Integer.valueOf(currentHero.getDefaultProperty(properties.get(i))));
        int value = Integer.valueOf(currentHero.getCurrentProperty(properties.get(i)));
        currentValues.get(displayIndex).setValue(value);
        if (value == 0 || !currentHero.isDifference()) {
          currentValues.get(displayIndex).setForeground(Color.BLACK);
        }
        else if (value > 0) {
          currentValues.get(displayIndex).setForeground(Color.GREEN);
        }
        else {
          currentValues.get(displayIndex).setForeground(Color.RED);
        }
        locks.get(displayIndex).setEnabled(!currentHero.isDifference());
        // locks.get(i).setSelected(false);
        boolean locked = locks.get(displayIndex).isSelected();
        defaultValues.get(displayIndex).setEditable(!currentHero.isDifference() &&
            (locked || Group.getInstance().getGlobalUnlock()));
        currentValues.get(displayIndex).setEditable(!currentHero.isDifference());
        tests.get(displayIndex).setEnabled(!currentHero.isDifference());
        increases.get(displayIndex).setEnabled(!currentHero.isDifference() && 
            (currentHero.hasPropertyChangeTry(properties.get(i)) || locked
                || Group.getInstance().getGlobalUnlock()));
      }
      else {
        defaultValues.get(displayIndex).setEditable(false);
        defaultValues.get(displayIndex).setText("-");
        currentValues.get(displayIndex).setEditable(false);
        currentValues.get(displayIndex).setText("-");
        locks.get(displayIndex).setEnabled(false);
        tests.get(displayIndex).setEnabled(false);
        // testAlls.get(displayIndex).setEnabled(true);
        increases.get(displayIndex).setEnabled(false);
      }
      displayIndex++;
    }
    disableChange = false;
  }

  /**
   * 
   * 
   */
  protected void createUI() {
    getJContentPane().removeAll();
    JScrollPane scrollPane = new JScrollPane();
    getJContentPane().add(scrollPane, BorderLayout.CENTER);
    properties.clear();
    defaultValues.clear();
    currentValues.clear();
    locks.clear();
    tests.clear();
    // testAlls.clear();
    increases.clear();

    int lineHeight = 20;
    int btnWidth = 28;

    if (goodProperties) {
      properties.add(Property.MU);
      properties.add(Property.KL);
      properties.add(Property.IN);
      properties.add(Property.CH);
      properties.add(Property.FF);
      properties.add(Property.GE);
      properties.add(Property.KK);
    }
    else {
      properties.add(Property.AG);
      properties.add(Property.HA);
      properties.add(Property.RA);
      properties.add(Property.TA);
      properties.add(Property.NG);
      properties.add(Property.GG);
      properties.add(Property.JZ);
    }

    JPanel propertiesPanel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    propertiesPanel.setLayout(layout);
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(2, 2, 2, 2);
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    JLabel headerDesc = new JLabel("Eigenschaft");
    headerDesc.setPreferredSize(new Dimension(20, lineHeight));
    layout.setConstraints(headerDesc, c);
    propertiesPanel.add(headerDesc);
    c.weightx = 0.5;
    c.gridx = 1;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;
    JLabel headerDefault = new JLabel("Std");
    headerDefault.setPreferredSize(new Dimension(25, lineHeight));
    layout.setConstraints(headerDefault, c);
    propertiesPanel.add(headerDefault);
    c.gridx = 2;
    JLabel headerCurrent = new JLabel("Akt");
    headerCurrent.setPreferredSize(new Dimension(25, lineHeight));
    layout.setConstraints(headerCurrent, c);
    propertiesPanel.add(headerCurrent);
    c.gridx = 3;
    JLabel headerLock = new JLabel("");
    layout.setConstraints(headerLock, c);
    headerLock.setPreferredSize(new Dimension(btnWidth, lineHeight));
    propertiesPanel.add(headerLock);
    c.gridx = 4;
    JLabel headerTests = new JLabel("");
    layout.setConstraints(headerTests, c);
    headerTests.setPreferredSize(new Dimension(btnWidth, lineHeight));
    propertiesPanel.add(headerTests);
    c.gridx = 5;
    JLabel headerIncr = new JLabel("");
    headerIncr.setPreferredSize(new Dimension(btnWidth, lineHeight));
    layout.setConstraints(headerIncr, c);
    propertiesPanel.add(headerIncr);

    int nrOfProperties = 0;

    for (int i = 0; i < properties.size(); ++i) {
      ++nrOfProperties;
      String descr = "" + properties.get(i);
      JLabel descrLabel = new JLabel(descr);
      propertyDescriptions.add(descrLabel);
      c.gridx = 0;
      c.gridy = i + 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.EAST;
      c.weightx = 1.0;
      c.weighty = 0.5;
      descrLabel.setPreferredSize(new Dimension(20, lineHeight));
      layout.setConstraints(descrLabel, c);
      propertiesPanel.add(descrLabel);
      c.gridx = 1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 0.5;
      JFormattedTextField defaultValue = new JFormattedTextField(
          new NumberFormatter(NumberFormat.getIntegerInstance()));
      // defaultValue.setColumns(3);
      defaultValue.setPreferredSize(new Dimension(25, lineHeight));
      defaultValue.setValue(Integer.valueOf(0));
      defaultValues.add(defaultValue);
      layout.setConstraints(defaultValue, c);
      propertiesPanel.add(defaultValue);
      c.gridx = 2;
      JFormattedTextField currentValue = new JFormattedTextField(
          new NumberFormatter(NumberFormat.getIntegerInstance()));
      // currentValue.setColumns(3);
      currentValue.setPreferredSize(new Dimension(25, lineHeight));
      currentValue.setValue(Integer.valueOf(0));
      currentValues.add(currentValue);
      layout.setConstraints(currentValue, c);
      propertiesPanel.add(currentValue);
      c.gridx = 3;
      JToggleButton lock = new JToggleButton(ImageManager.getIcon("locked"));
      lock.setPressedIcon(ImageManager.getIcon("unlocked"));
      lock.setPreferredSize(new Dimension(btnWidth, lineHeight));
      lock.setToolTipText("Schützen / Freigeben");
      locks.add(lock);
      layout.setConstraints(lock, c);
      propertiesPanel.add(lock);
      c.gridx = 4;
      JButton test = null;
      test = new JButton(ImageManager.getIcon("probe"));
      test.setDisabledIcon(ImageManager.getIcon("probe_disabled"));
      test.setPreferredSize(new Dimension(btnWidth, lineHeight));
      test.setToolTipText("Probe");
      layout.setConstraints(test, c);
      tests.add(test);
      propertiesPanel.add(test);
      c.gridx = 5;
      JButton increase = new JButton(ImageManager
          .getIcon(goodProperties ? "increase" : "decrease_enabled"));
      increase.setDisabledIcon(ImageManager
          .getIcon(goodProperties ? "increase_disabled" : "decrease"));
      increase.setPreferredSize(new Dimension(btnWidth, lineHeight));
      increase.setToolTipText(goodProperties ? "Erhöhen" : "Senken");
      increases.add(increase);
      layout.setConstraints(increase, c);
      propertiesPanel.add(increase);

      lock.addActionListener(new Locker(properties.get(i), defaultValue,
          increase));
      test.addActionListener(new Tester(properties.get(i)));
      increase.addActionListener(new Increaser(properties.get(i), increase,
          lock));
      defaultValue.addPropertyChangeListener(new PropertyChanger(properties
          .get(i), false));
      currentValue.addPropertyChangeListener(new PropertyChanger(properties
          .get(i), true));
    }

    JPanel scrolledPanel = new JPanel(new BorderLayout());
    scrolledPanel.add(propertiesPanel, BorderLayout.NORTH);

    addSubclassSpecificComponents(scrolledPanel);

    scrollPane.setViewportView(scrolledPanel);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    // this.setSize(getBounds().width, (nrOfTalents + 1) * 27 +
    // getSubclassSpecificSizeOffset().height);

    updateData();
    // talentPane.invalidate();
    // scrolledPanel.invalidate();
    // scrollPane.invalidate();
    // this.invalidate();
    // talentPane.validate();
    // scrolledPanel.validate();
    // scrollPane.validate();
    this.validate();
  }

  protected void addSubclassSpecificComponents(java.awt.Container container) {
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 0);
  }

  private class Locker implements ActionListener {
    public Locker(Property property, JTextField talentValue, Component increaser) {
      c1 = talentValue;
      c2 = increaser;
      this.property = property;
    }

    public void actionPerformed(ActionEvent e) {
      JToggleButton button = (JToggleButton) e.getSource();
      c1.setEditable(button.isSelected()
          || Group.getInstance().getGlobalUnlock());
      c2
          .setEnabled(c1.isEditable()
              || (PropertyFrame.this.currentHero != null && PropertyFrame.this.currentHero
                  .hasPropertyChangeTry(property)));
      if (button.isSelected()) {
        button.setIcon(ImageManager.getIcon("unlocked"));
      }
      else {
        button.setIcon(ImageManager.getIcon("locked"));
      }
    }

    private final JTextField c1;

    private final Component c2;

    private final Property property;
  };

  private class PropertyChanger implements PropertyChangeListener {
    public PropertyChanger(Property property, boolean current) {
      this.property = property;
      this.current = current;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if (PropertyFrame.this.disableChange) return;
      if (!evt.getPropertyName().equals("value")) return;
      if (currentHero != null) {
        if (current)
          currentHero.setCurrentProperty(property,
              ((Number) ((JFormattedTextField) evt.getSource()).getValue())
                  .intValue());
        else
          currentHero.setDefaultProperty(property,
              ((Number) ((JFormattedTextField) evt.getSource()).getValue())
                  .intValue());
      }
    }

    private Property property;

    private boolean current;
  };

  private class Tester implements ActionListener {
    public Tester(Property property) {
      this.property = property;
    }

    public void actionPerformed(ActionEvent e) {
      ProbeDialog dialog = new ProbeDialog(PropertyFrame.this, currentHero,
          property);
      dialog.setLocationRelativeTo(PropertyFrame.this);
      dialog.setVisible(true);
    };

    private Property property;
  };

  private class Increaser implements ActionListener {
    public Increaser(Property property, JButton button, JToggleButton lock) {
      this.property = property;
      // this.button = button;
      this.lock = lock;
    }

    public void actionPerformed(ActionEvent e) {
      Hero hero = PropertyFrame.this.currentHero;
      if (hero == null) return;
      boolean goodProperty = property.ordinal() <= Property.KK.ordinal();
      HeroStepIncreaser increaser = new HeroStepIncreaser(hero);
      boolean success = false;
      if (goodProperty) {
        success = increaser.tryToIncreaseGoodProperty(property);
      }
      else {
        success = increaser.tryToDecreaseBadProperty(property);
      }
      if (!lock.isSelected() && !Group.getInstance().getGlobalUnlock()) {
        hero.removePropertyChangeTry(goodProperty);
      }
      
      int index = 0;
      while (index < properties.size() && properties.get(index) != property)
    	  ++index;
      if (index >= defaultValues.size())
    	  return;
      
      final int displayIndex = index;
      final Color oldColor = defaultValues.get(displayIndex).getBackground();
      Color color = success ? Color.GREEN : Color.RED;
      defaultValues.get(displayIndex).setBackground(color);
      currentValues.get(displayIndex).setBackground(color);
      Timer timer = new Timer(250, new ActionListener() {
    	  public void actionPerformed(ActionEvent e) {
    		 defaultValues.get(displayIndex).setBackground(oldColor);
    		 currentValues.get(displayIndex).setBackground(oldColor);
    	  }
      });
      timer.setRepeats(false);
      timer.start();
      updateData();
    }

    private Property property;

    // private JButton button;

    private JToggleButton lock;
  };

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setContentPane(getJContentPane());
    createUI();
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(new java.awt.BorderLayout());
    }
    return jContentPane;
  }

  private Hero currentHero = null;

  private ArrayList<Property> properties;

  private ArrayList<JLabel> propertyDescriptions;

  private ArrayList<JFormattedTextField> defaultValues;

  private ArrayList<JFormattedTextField> currentValues;

  private ArrayList<JToggleButton> locks;

  private ArrayList<JButton> tests;

  // private Vector<JButton> testAlls;
  private ArrayList<JButton> increases;

  private CharacterObserver myCharacterObserver = new MyCharacterObserver();

  private class MyCharacterObserver extends CharacterAdapter {
    /*
     * (non-Javadoc)
     * 
     * @see dsa.data.CharacterObserver#DefaultPropertyChanged(dsa.data.Property)
     */
    public void defaultPropertyChanged(Property property) {
      PropertyFrame.this.updateData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see dsa.data.CharacterObserver#CurrentPropertyChanged(dsa.data.Property)
     */
    public void currentPropertyChanged(Property property) {
      PropertyFrame.this.updateData();
    }

    public void stepIncreased() {
      PropertyFrame.this.updateData();
    }
    
    public void increaseTriesChanged() {
      PropertyFrame.this.updateData();
    }

  };

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#ActiveCharacterChanged(dsa.data.Hero,
   *      dsa.data.Hero)
   */
  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    updateData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterRemoved(dsa.data.Hero)
   */
  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterAdded(dsa.data.Hero)
   */
  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
    updateData();
  }
}

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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.text.NumberFormatter;

// import dsa.control.Dice;
import dsa.control.Dice;
import dsa.control.Markers;
import dsa.control.Probe;
import dsa.gui.dialogs.ProbeDialog;
import dsa.gui.dialogs.RedistributionDialog;
import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;

public final class EnergyFrame extends SubFrame implements CharactersObserver {

  private javax.swing.JPanel jContentPane = null;

  /**
   * This is the default constructor
   */
  public EnergyFrame(String title) {
    super(title);
    setTitle(title);
    energies = new ArrayList<Energy>();
    defaultValues = new ArrayList<JFormattedTextField>();
    currentValues = new ArrayList<JFormattedTextField>();
    locks = new ArrayList<JToggleButton>();
    // testAlls = new Vector<JButton>();
    increases = new ArrayList<JButton>();
    energyDescriptions = new ArrayList<JLabel>();
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(EnergyFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(EnergyFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myCharacterObserver);
          done = true;
        }
      }
    });
    initialize();
    // pack();
  }

  private boolean disableChange = false;

  /**
   * 
   * 
   */
  private void updateData() {
    disableChange = true;
    for (int i = 0; i < energies.size() - 2; ++i) {
      if ((currentHero != null) && currentHero.hasEnergy(energies.get(i))) {
        defaultValues.get(i).setValue(
            Integer.valueOf(currentHero.getDefaultEnergy(energies.get(i))));
        currentValues.get(i).setValue(
            Integer.valueOf(currentHero.getCurrentEnergy(energies.get(i))));
        locks.get(i).setEnabled(true);
        // locks.get(i).setSelected(false);
        boolean locked = locks.get(i).isSelected();
        defaultValues.get(i).setEnabled(true);
        defaultValues.get(i).setEditable(
            locked || Group.getInstance().getGlobalUnlock());
        currentValues.get(i).setEnabled(true);
        currentValues.get(i).setEditable(true);
      }
      else {
        defaultValues.get(i).setText("-");
        defaultValues.get(i).setEditable(false);
        defaultValues.get(i).setEnabled(false);
        currentValues.get(i).setEditable(false);
        currentValues.get(i).setText("-");
        currentValues.get(i).setEnabled(false);
        locks.get(i).setEnabled(false);
        if (i != energies.size() - 2) {
          increases.get(i).setEnabled(false);
        }
      }
    }
    if ((currentHero != null) && currentHero.hasEnergy(Energy.AU)) {
      defaultValues.get(Energy.AU.ordinal()).setValue(
          Integer.valueOf(currentHero.getDefaultEnergy(Energy.AU)));
      currentValues.get(Energy.AU.ordinal()).setValue(
          Integer.valueOf(currentHero.getCurrentEnergy(Energy.AU)));
      currentValues.get(Energy.AU.ordinal()).setEditable(true);
      defaultValues.get(Energy.KO.ordinal()).setValue(
          Integer.valueOf(currentHero.getDefaultEnergy(Energy.KO)));
      currentValues.get(Energy.KO.ordinal()).setValue(
          Integer.valueOf(currentHero.getCurrentEnergy(Energy.KO)));
    }
    else {
      defaultValues.get(Energy.AU.ordinal()).setText("-");
      currentValues.get(Energy.AU.ordinal()).setText("-");
      currentValues.get(Energy.AU.ordinal()).setEditable(false);
      defaultValues.get(Energy.KO.ordinal()).setText("-");
      currentValues.get(Energy.KO.ordinal()).setText("-");
    }

    if ((currentHero != null) && (currentHero.hasEnergy(Energy.LE))) {
      increases.get(Energy.LE.ordinal()).setEnabled(
          currentHero.hasLEIncreaseTry());
    }
    if ((currentHero != null) && (currentHero.hasEnergy(Energy.AE))) {
      increases.get(Energy.AE.ordinal()).setEnabled(
          currentHero.hasAEIncreaseTry());
    }
    int index = Energy.KE.ordinal();
    if ((currentHero != null) && (currentHero.hasEnergy(Energy.KE))) {
      boolean locked = locks.get(index).isSelected()
          || Group.getInstance().getGlobalUnlock();
      increases.get(index).setEnabled(locked);
    }
    else {
      increases.get(index).setEnabled(false);
    }
    if (currentHero != null) {
      meditationButton.setEnabled(currentHero.hasGreatMeditation());
      smallMedButton.setEnabled(currentHero.hasEnergy(Energy.AE)
          && currentHero.getCurrentEnergy(Energy.AE) < currentHero
              .getDefaultEnergy(Energy.AE)
          && currentHero.getCurrentEnergy(Energy.LE) > 4
          && currentHero.getCurrentEnergy(Energy.AE) > 0);
      regenButton
          .setEnabled(currentHero.getCurrentEnergy(Energy.LE) < currentHero
              .getDefaultEnergy(Energy.LE)
              || (currentHero.hasEnergy(Energy.AE) && currentHero
                  .getCurrentEnergy(Energy.AE) < currentHero
                  .getDefaultEnergy(Energy.AE)));
    }
    else {
      meditationButton.setEnabled(false);
      regenButton.setEnabled(false);
      smallMedButton.setEnabled(false);
    }
    koProbeButton.setEnabled(currentHero != null);

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
    energies.clear();
    defaultValues.clear();
    currentValues.clear();
    locks.clear();
    // testAlls.clear();
    increases.clear();

    int lineHeight = 20;
    int btnWidth = 28;

    for (Energy aEnergy : Energy.values()) {
      energies.add(aEnergy);
    }

    JPanel energiesPanel = new JPanel();
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    energiesPanel.setLayout(layout);
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(2, 2, 2, 2);
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;
    JLabel headerDesc = new JLabel("Energie");
    headerDesc.setPreferredSize(new Dimension(20, lineHeight));
    layout.setConstraints(headerDesc, c);
    energiesPanel.add(headerDesc);
    c.weightx = 0.5;
    c.gridx = 1;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;
    JLabel headerDefault = new JLabel("Std");
    headerDefault.setPreferredSize(new Dimension(25, lineHeight));
    layout.setConstraints(headerDefault, c);
    energiesPanel.add(headerDefault);
    c.gridx = 2;
    JLabel headerCurrent = new JLabel("Akt");
    headerCurrent.setPreferredSize(new Dimension(25, lineHeight));
    layout.setConstraints(headerCurrent, c);
    energiesPanel.add(headerCurrent);
    c.gridx = 3;
    JLabel headerLock = new JLabel("");
    layout.setConstraints(headerLock, c);
    headerLock.setPreferredSize(new Dimension(btnWidth, lineHeight));
    energiesPanel.add(headerLock);
    c.gridx = 4;
    JLabel headerIncr = new JLabel("");
    headerIncr.setPreferredSize(new Dimension(btnWidth, lineHeight));
    layout.setConstraints(headerIncr, c);
    energiesPanel.add(headerIncr);

    int nrOfEnergies = 0;

    for (int i = 0; i < energies.size() - 2; ++i) {
      ++nrOfEnergies;
      String descr = "" + energies.get(i);
      JLabel descrLabel = new JLabel(descr);
      energyDescriptions.add(descrLabel);
      c.gridx = 0;
      c.gridy = i + 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.EAST;
      c.weightx = 1.0;
      c.weighty = 0.5;
      descrLabel.setPreferredSize(new Dimension(20, lineHeight));
      layout.setConstraints(descrLabel, c);
      energiesPanel.add(descrLabel);
      c.gridx = 1;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.CENTER;
      c.weightx = 0.5;
      JFormattedTextField defaultValue = new JFormattedTextField(
          new NumberFormatter(NumberFormat.getIntegerInstance()));
      // defaultValue.setColumns(3);
      defaultValue.setPreferredSize(new Dimension(25, lineHeight));
      // defaultValue.setValue(new Integer(0));
      defaultValues.add(defaultValue);
      layout.setConstraints(defaultValue, c);
      energiesPanel.add(defaultValue);
      c.gridx = 2;
      JFormattedTextField currentValue = new JFormattedTextField(
          new NumberFormatter(NumberFormat.getIntegerInstance()));
      // currentValue.setColumns(3);
      currentValue.setPreferredSize(new Dimension(25, lineHeight));
      // currentValue.setValue(new Integer(0));
      currentValues.add(currentValue);
      layout.setConstraints(currentValue, c);
      energiesPanel.add(currentValue);
      c.gridx = 3;
      JToggleButton lock = new JToggleButton(ImageManager.getIcon("locked"));
      lock.setPressedIcon(ImageManager.getIcon("unlocked"));
      lock.setPreferredSize(new Dimension(btnWidth, lineHeight));
      lock.setToolTipText("Schützen / Freigeben");
      locks.add(lock);
      layout.setConstraints(lock, c);
      energiesPanel.add(lock);
      c.gridx = 4;
      if (i != energies.size() - 2) {
        JButton increase = new JButton(ImageManager.getIcon("increase"));
        increase.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
        increase.setPreferredSize(new Dimension(btnWidth, lineHeight));
        increase.setToolTipText("Erhöhen");
        increases.add(increase);
        layout.setConstraints(increase, c);
        energiesPanel.add(increase);
        increase.addActionListener(new Increaser(energies.get(i), increase,
            lock));
        lock.addActionListener(new Locker(energies.get(i), defaultValue,
            increase));
      }
      else {
        lock.addActionListener(new Locker(energies.get(i), defaultValue, null));
      }
      defaultValue.addPropertyChangeListener(new EnergyChanger(energies.get(i),
          false));
      currentValue.addPropertyChangeListener(new EnergyChanger(energies.get(i),
          true));
    }
    ++nrOfEnergies;
    JLabel descrLabel = new JLabel("" + Energy.AU);
    energyDescriptions.add(descrLabel);
    c.gridx = 0;
    c.gridy = energies.size() - 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 1.0;
    c.weighty = 0.5;
    descrLabel.setPreferredSize(new Dimension(20, lineHeight));
    layout.setConstraints(descrLabel, c);
    energiesPanel.add(descrLabel);
    c.gridx = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.5;
    JFormattedTextField defaultValue = new JFormattedTextField(
        new NumberFormatter(NumberFormat.getIntegerInstance()));
    // defaultValue.setColumns(3);
    defaultValue.setPreferredSize(new Dimension(25, lineHeight));
    // defaultValue.setValue(new Integer(0));
    defaultValue.setEditable(false);
    defaultValues.add(defaultValue);
    layout.setConstraints(defaultValue, c);
    energiesPanel.add(defaultValue);
    c.gridx = 2;
    JFormattedTextField currentValue = new JFormattedTextField(
        new NumberFormatter(NumberFormat.getIntegerInstance()));
    // currentValue.setColumns(3);
    currentValue.setPreferredSize(new Dimension(25, lineHeight));
    // currentValue.setValue(new Integer(0));
    currentValue.setEditable(false);
    currentValue.addPropertyChangeListener(new EnergyChanger(energies
        .get(Energy.AU.ordinal()), true));
    currentValues.add(currentValue);
    layout.setConstraints(currentValue, c);
    energiesPanel.add(currentValue);

    ++nrOfEnergies;
    JLabel descrLabel2 = new JLabel("" + Energy.KO);
    energyDescriptions.add(descrLabel);
    c.gridx = 0;
    c.gridy = energies.size();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.EAST;
    c.weightx = 1.0;
    c.weighty = 0.5;
    descrLabel2.setPreferredSize(new Dimension(20, lineHeight));
    layout.setConstraints(descrLabel2, c);
    energiesPanel.add(descrLabel2);
    c.gridx = 1;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.CENTER;
    c.weightx = 0.5;
    JFormattedTextField defaultValue2 = new JFormattedTextField(
        new NumberFormatter(NumberFormat.getIntegerInstance()));
    // defaultValue.setColumns(3);
    defaultValue2.setPreferredSize(new Dimension(25, lineHeight));
    // defaultValue.setValue(new Integer(0));
    defaultValue2.setEditable(false);
    defaultValues.add(defaultValue2);
    layout.setConstraints(defaultValue2, c);
    energiesPanel.add(defaultValue2);
    c.gridx = 2;
    JFormattedTextField currentValue2 = new JFormattedTextField(
        new NumberFormatter(NumberFormat.getIntegerInstance()));
    // currentValue.setColumns(3);
    currentValue2.setPreferredSize(new Dimension(25, lineHeight));
    // currentValue.setValue(new Integer(0));
    currentValue2.setEditable(false);
    currentValues.add(currentValue2);
    layout.setConstraints(currentValue2, c);
    energiesPanel.add(currentValue2);
    c.gridx = 3;
    koProbeButton = new JButton(ImageManager.getIcon("probe"));
    koProbeButton
        .setPreferredSize(new java.awt.Dimension(btnWidth, lineHeight));
    koProbeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doKOProbe();
      }
    });
    koProbeButton.setToolTipText("Probe");
    layout.setConstraints(koProbeButton, c);
    energiesPanel.add(koProbeButton);

    JPanel scrolledPanel = new JPanel(new BorderLayout());
    scrolledPanel.add(energiesPanel, BorderLayout.NORTH);

    addSubclassSpecificComponents(scrolledPanel);

    scrollPane.setViewportView(scrolledPanel);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setOpaque(false);

    JPanel bottomPanel = new JPanel(new java.awt.FlowLayout(
        java.awt.FlowLayout.CENTER, 12, 12));

    bottomPanel.add(getRegenButton());
    bottomPanel.add(getSmallMedButton());
    bottomPanel.add(getMeditationButton());

    getJContentPane().add(bottomPanel, BorderLayout.SOUTH);
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

  private JButton koProbeButton = null;

  private JButton meditationButton = null;

  protected void addSubclassSpecificComponents(java.awt.Container container) {
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 0);
  }

  private static class Locker implements ActionListener {
    public Locker(Energy energy, javax.swing.JTextField talentValue, Component increaser) {
      c1 = talentValue;
      c2 = increaser;
      this.energy = energy;
    }

    public void actionPerformed(ActionEvent e) {
      JToggleButton button = (JToggleButton) e.getSource();
      c1.setEditable(button.isSelected()
          || Group.getInstance().getGlobalUnlock());
      if (button.isSelected()) {
        button.setIcon(ImageManager.getIcon("unlocked"));
      }
      else {
        button.setIcon(ImageManager.getIcon("locked"));
      }
      if (energy == Energy.KE) {
        c2.setEnabled(button.isSelected()
            || Group.getInstance().getGlobalUnlock());
      }
    }

    private final javax.swing.JTextField c1;

    private final Component c2;

    private final Energy energy;
  };

  private class EnergyChanger implements PropertyChangeListener {
    public EnergyChanger(Energy energy, boolean current) {
      this.energy = energy;
      this.current = current;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if (EnergyFrame.this.disableChange) return;
      if (!evt.getPropertyName().equals("value")) return;
      if (currentHero != null) {
        if (current) {
          int value = ((Number) ((JFormattedTextField) evt.getSource())
              .getValue()).intValue();
          if (energy == Energy.AU) {
            int oldValue = currentHero.getCurrentEnergy(energy);
            int diff = value - oldValue;
            currentHero.changeAU(diff);
          }
          else {
            currentHero.setCurrentEnergy(energy, value);
          }
        }
        else
          currentHero.setDefaultEnergy(energy,
              ((Number) ((JFormattedTextField) evt.getSource()).getValue())
                  .intValue());
      }
    }

    private Energy energy;

    private boolean current;
  };

  private class Increaser implements ActionListener {
    public Increaser(Energy energy, JButton button, JToggleButton lock) {
      this.energy = energy;
      // this.button = button;
      // this.lock = lock;
    }

    public void actionPerformed(ActionEvent e) {
      Hero hero = EnergyFrame.this.currentHero;
      if (hero == null) return;
      if (energy == Energy.KE) {
        String pointsS = javax.swing.JOptionPane.showInputDialog(
            EnergyFrame.this, "Neu erworbene Karmaenergie:", "KE erhöhen",
            JOptionPane.PLAIN_MESSAGE);
        if (pointsS == null) return;
        int points = 0;
        try {
          points = Integer.parseInt(pointsS);
          if (points < 0) throw new NumberFormatException("");
        }
        catch (NumberFormatException ex) {
          JOptionPane.showMessageDialog(EnergyFrame.this,
              "Bitte eine positive ganze Zahl eingeben.", "Fehler",
              JOptionPane.ERROR_MESSAGE);
          return;
        }
        int oldKE = hero.getDefaultEnergy(Energy.KE);
        hero.setDefaultEnergy(Energy.KE, oldKE + points);
        return;
      }
      if (hero.hasExtraAEIncrease()) {
        if (energy == Energy.AE) {
          hero.increaseAE(Dice.roll(6) + hero.getFixedAEIncrease());
        }
        else {
          hero.increaseLE(Dice.roll(6) + hero.getFixedLEIncrease());
        }
      }
      else if (hero.hasEnergy(Energy.AE)) {
        int increase = Dice.roll(6) + hero.getFixedLEIncrease();
        RedistributionDialog dialog = new RedistributionDialog();
        dialog.setTitle("LE-AE-Verteilung");
        dialog.setDescription("Du hast " + increase
            + " Punkte zur Verfügung,\n"
            + "die du auf LE und AE verteilen kannst.\n"
            + "Wieviele Punkte sollen auf die AE gelegt werden?");
        dialog.setQuestion("Punkte auf AE:");
        dialog.setMinimum(0);
        dialog.setMaximum(increase);
        dialog.setDefault(increase / 2);
        dialog.setLocationRelativeTo(EnergyFrame.this);
        boolean goOn = false;
        do {
          goOn = false;
          dialog.setVisible(true);
          int aeIncrease = dialog.getValue();
          try {
            hero.increaseLEAndAE(increase - aeIncrease, aeIncrease);
          }
          catch (dsa.model.characters.LEAEIncreaseException ex) {
            javax.swing.JOptionPane.showMessageDialog(EnergyFrame.this, ex
                .getMessage(), "Fehler", javax.swing.JOptionPane.ERROR_MESSAGE);
            goOn = true;
          }
        } while (goOn);
      }
      else {
        hero.increaseLE(Dice.roll(6) + hero.getFixedLEIncrease());
      }
    }

    private Energy energy;

    // private JButton button;

    // private JToggleButton lock;
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

  private ArrayList<Energy> energies;

  private ArrayList<JLabel> energyDescriptions;

  private ArrayList<JFormattedTextField> defaultValues;

  private ArrayList<JFormattedTextField> currentValues;

  private ArrayList<JToggleButton> locks;

  private ArrayList<JButton> increases;

  private CharacterObserver myCharacterObserver = new MyCharacterObserver();

  private void doKOProbe() {
    ProbeDialog dialog = new ProbeDialog(this, currentHero);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  private class MyCharacterObserver extends CharacterAdapter {
    /*
     * (non-Javadoc)
     * 
     * @see dsa.data.CharacterObserver#DefaultEnergyChanged(dsa.data.Energy)
     */
    public void defaultEnergyChanged(Energy energy) {
      EnergyFrame.this.updateData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see dsa.data.CharacterObserver#CurrentEnergyChanged(dsa.data.Energy)
     */
    public void currentEnergyChanged(Energy energy) {
      EnergyFrame.this.updateData();
    }

    public void stepIncreased() {
      EnergyFrame.this.updateData();
    }

    public void increaseTriesChanged() {
      EnergyFrame.this.updateData();
    }

  };

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getMeditationButton() {
    if (meditationButton == null) {
      meditationButton = new JButton();
      meditationButton.setBounds(new java.awt.Rectangle(257, 167, 50, 22));
      meditationButton.setText("M");
      meditationButton.setToolTipText("Große Meditation");
      meditationButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          int choice = JOptionPane
              .showConfirmDialog(
                  EnergyFrame.this,
                  "Möchtest du eine Große Meditation durchführen?\n(10 Zauber-Steigerungen -> 1W6+2 ASP)",
                  "Große Meditation", JOptionPane.YES_NO_OPTION);
          if (choice == JOptionPane.YES_OPTION) {
            int newASP = currentHero.doGreatMeditation();
            updateData();
            JOptionPane.showMessageDialog(EnergyFrame.this, currentHero
                .getName()
                + " hat " + newASP + " ASP erhalten.");
          }
        }
      });
    }
    return meditationButton;
  }

  private JButton smallMedButton;

  private JButton regenButton;

  private JButton getSmallMedButton() {
    if (smallMedButton == null) {
      smallMedButton = new JButton();
      smallMedButton.setBounds(new Rectangle(257, 167, 50, 22));
      smallMedButton.setText("m");
      smallMedButton.setToolTipText("Meditieren");
      smallMedButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doSmallMeditation();
        }
      });
    }
    return smallMedButton;
  }

  void doSmallMeditation() {
    RedistributionDialog dialog = new RedistributionDialog(this);
    dialog.setTitle("Meditation");
    dialog
        .setDescription("Unter fixen Kosten von 1 ASP und 3 LeP kann\nLE in AE gewandelt werden.");
    dialog.setQuestion("Wieviel LE umwandeln?");
    dialog.setMinimum(0);
    dialog.setLocationRelativeTo(this);
    int maxLE = currentHero.getCurrentEnergy(Energy.LE) - 4;
    int maxAE = currentHero.getDefaultEnergy(Energy.AE)
        - currentHero.getCurrentEnergy(Energy.AE) + 1;
    dialog.setMaximum(Math.min(maxLE, maxAE));
    dialog.setDefault(Math.max(Math.min(maxLE, maxAE) / 2, 1));
    dialog.setVisible(true);
    int value = dialog.getValue();
    if (value == 0) return;
    Probe probe = new Probe();
    probe.setFirstProperty(currentHero.getCurrentProperty(Property.IN));
    probe.setSecondProperty(currentHero.getCurrentProperty(Property.CH));
    probe.setThirdProperty(currentHero.getCurrentProperty(Property.KK));
    probe.setModifier(Markers.getMarkers(currentHero));
    probe.setSkill(currentHero.getStep() / 2);
    if (probe.performTest(Dice.roll(20), Dice.roll(20), Dice.roll(20))) {
      currentHero.changeCurrentEnergy(Energy.LE, 0 - value - 3);
      currentHero.changeCurrentEnergy(Energy.AE, value - 1);
    }
    else {
      JOptionPane.showMessageDialog(this,
          "Leider hat die Probe nicht geklappt.", "Meditation",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private JButton getRegenButton() {
    if (regenButton == null) {
      regenButton = new JButton();
      regenButton.setBounds(new Rectangle(257, 167, 50, 22));
      regenButton.setText("R");
      regenButton.setToolTipText("Regenerieren");
      regenButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doRegeneration();
        }
      });
    }
    return regenButton;
  }

  void doRegeneration() {
    int missingLE = currentHero.getDefaultEnergy(Energy.LE)
        - currentHero.getCurrentEnergy(Energy.LE);
    if (currentHero.hasEnergy(Energy.AE)
        && currentHero.getCurrentEnergy(Energy.AE) < currentHero
            .getDefaultEnergy(Energy.AE)) {
      int missingAE = currentHero.getDefaultEnergy(Energy.AE)
          - currentHero.getCurrentEnergy(Energy.AE);
      boolean hasBonus = Group.getInstance().getOptions()
          .hasFastAERegeneration();
      int bonusRegeneration = hasBonus ? (int) Math
          .round((double) missingAE / 10.0) : 0;
      if (missingLE > 0) {
        int first = Dice.roll(6);
        int second = Dice.roll(6);
        if (first < second) {
          int temp = first;
          first = second;
          second = temp;
        }
        first = Math.min(first, missingLE);
        second = Math.min(second + bonusRegeneration, missingAE);
        currentHero.changeCurrentEnergy(Energy.LE, first);
        currentHero.changeCurrentEnergy(Energy.AE, second);
        JOptionPane.showMessageDialog(this,
            "LE: " + first + " Punkte regeneriert\n" + "AE: " + second
                + " Punkte regeneriert", "Regeneration",
            JOptionPane.INFORMATION_MESSAGE);
      }
      else {
        int value = Dice.roll(6) + bonusRegeneration;
        value = Math.min(value, missingAE);
        currentHero.changeCurrentEnergy(Energy.AE, value);
        JOptionPane.showMessageDialog(this, "AE: " + value
            + " Punkte regeneriert.", "Regeneration",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
    else {
      int value = Dice.roll(6);
      value = Math.min(value, missingLE);
      currentHero.changeCurrentEnergy(Energy.LE, value);
      JOptionPane.showMessageDialog(this, "LE: " + value
          + " Punkte regeneriert.", "Regeneration",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#ActiveCharacterChanged(dsa.data.Hero,
   *      dsa.data.Hero)
   */
  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
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

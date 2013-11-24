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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;

import dsa.gui.lf.BGDialog;
import dsa.model.characters.Group;
import dsa.model.data.Animals;

import javax.swing.JTextField;

public final class AnimalSelectionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JComboBox categoryCombo = null;

  private JLabel jLabel1 = null;

  private JComboBox raceCombo = null;

  private JLabel jLabel2 = null;

  private JComboBox stepCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  /**
   * This is the default constructor
   */
  public AnimalSelectionDialog(java.awt.Frame parent) {
    super(parent);
    initialize();
    setLocationRelativeTo(parent);
    setModal(true);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createAnimal();
      }
    });
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    fillCategoryCombo();
    categoryCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fillRaceAndStepCombo();
      }
    });
    raceCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (listenForRaces) fillStepCombo();
      }
    });
  }

  private void fillCategoryCombo() {
    dsa.model.characters.Hero hero = Group.getInstance().getActiveHero();
    boolean canHaveFamiliar = (hero.getType().startsWith("Hexe") || hero
        .getType().startsWith("Geode"));
    List<String> categories = Animals.getInstance().getCategories();
    for (String c : categories) {
      if (canHaveFamiliar || !c.startsWith("Vertraute")) {
        categoryCombo.addItem(c);
      }
    }
    if (categories.size() > 0) {
      categoryCombo.setSelectedIndex(0);
      fillRaceAndStepCombo();
    }
  }

  protected void fillRaceAndStepCombo() {
    listenForRaces = false;
    raceCombo.removeAllItems();
    String category = categoryCombo.getSelectedItem().toString();
    List<String> races = Animals.getInstance().getRaces(category);
    for (String r : races) {
      raceCombo.addItem(r);
    }
    if (races.size() > 0) {
      raceCombo.setSelectedIndex(0);
      fillStepCombo();
    }
    listenForRaces = true;
  }

  boolean listenForRaces = true;

  private static final String[] STEP_DESCRIPTIONS = { "ungearbeitet",
      "unerfahren", "erprobt", "geschult" };

  private void fillStepCombo() {
    String race = raceCombo.getSelectedItem().toString();
    String category = categoryCombo.getSelectedItem().toString();
    int steps = Animals.getInstance().getNrOfSteps(category, race);
    stepCombo.removeAllItems();
    if (steps <= 1) {
      stepCombo.addItem("-");
      stepCombo.setEnabled(false);
    }
    else {
      for (int i = 0; i < steps; ++i) {
        stepCombo.addItem(STEP_DESCRIPTIONS[i]);
      }
      stepCombo.setEnabled(true);
    }
    stepCombo.setSelectedIndex(0);
  }

  private dsa.model.data.Animal animal = null;

  private JLabel jLabel3 = null;

  private JTextField nameField = null;

  private void createAnimal() {
    String name = nameField.getText();
    if (name.length() == 0) {
      javax.swing.JOptionPane.showMessageDialog(this,
          "Bitte einen Namen eingeben!", "Fehler",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return;
    }
    String race = raceCombo.getSelectedItem().toString();
    String category = categoryCombo.getSelectedItem().toString();
    int step = stepCombo.getSelectedIndex();
    animal = Animals.getInstance().createAnimal(category, race, step);
    animal.setName(name);
    dispose();
  }

  public dsa.model.data.Animal getAnimal() {
    return animal;
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(337, 222);
    this.setTitle("Tier hinzufügen");
    this.setContentPane(getJContentPane());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(14, 121, 66, 19));
      jLabel3.setText("Name:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(14, 88, 67, 23));
      jLabel2.setText("Erfahrung:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(14, 51, 48, 21));
      jLabel1.setText("Sorte:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(15, 15, 46, 22));
      jLabel.setText("Typ:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getCategoryCombo(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getRaceCombo(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(getStepCombo(), null);
      jContentPane.add(getOKButton(), null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(getNameField(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getCategoryCombo() {
    if (categoryCombo == null) {
      categoryCombo = new JComboBox();
      categoryCombo.setBounds(new java.awt.Rectangle(91, 15, 225, 24));
    }
    return categoryCombo;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getRaceCombo() {
    if (raceCombo == null) {
      raceCombo = new JComboBox();
      raceCombo.setBounds(new java.awt.Rectangle(91, 51, 225, 24));
    }
    return raceCombo;
  }

  /**
   * This method initializes jComboBox2
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getStepCombo() {
    if (stepCombo == null) {
      stepCombo = new JComboBox();
      stepCombo.setBounds(new java.awt.Rectangle(91, 87, 225, 24));
    }
    return stepCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(36, 155, 105, 25));
      okButton.setText("OK");
    }
    return okButton;
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(179, 155, 105, 25));
      cancelButton.setText("Abbrechen");
    }
    return cancelButton;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(91, 119, 225, 24));
    }
    return nameField;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

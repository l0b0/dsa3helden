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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import dsa.gui.lf.BGDialog;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.util.Optional;

public final class ThingDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JTextField nameField = null;

  private JLabel jLabel1 = null;

  private JComboBox categoryCombo = null;

  private JSpinner valueSpinner = null;

  private JComboBox currencyCombo = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JSpinner weightSpinner = null;

  private JComboBox weightCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel = null;

  /**
   * This is the default constructor
   */
  public ThingDialog(JDialog parent) {
    super(parent);
    initialize();
    setModal(true);
    setLocationRelativeTo(parent);
    preFillComboBoxes();
  }
  
  public ThingDialog(JDialog parent, Thing thing) {
    super(parent);
    initialize();
    setModal(true);
    setLocationRelativeTo(parent);
    preFillComboBoxes();
    this.thing = thing;
    nameField.setText(thing.getName());
    nameField.setEnabled(false);
    categoryCombo.setSelectedItem(thing.getCategory());
    int weight = thing.getWeight();
    if (weight != 0 && weight % 40 == 0) {
      weightCombo.setSelectedIndex(1);
      weight /= 40;
    }
    weightSpinner.setValue(weight);
    currencyCombo.setSelectedIndex(thing.getCurrency().ordinal());
    valueSpinner.setValue(thing.getValue().hasValue() ? thing.getValue().getValue() : 0);
  }

  private void preFillComboBoxes() {
    java.util.HashSet<String> categories = Things.getInstance()
        .getKnownCategories();
    for (String c : categories) {
      if (!c.startsWith("Transport") && !c.startsWith("Gasthaus")
          && !c.startsWith("Dienstleistung")) {
        categoryCombo.addItem(c);
      }
    }
    currencyCombo.addItem("Dukaten");
    currencyCombo.addItem("Silbertaler");
    currencyCombo.addItem("Heller");
    currencyCombo.addItem("Kreuzer");
    weightCombo.addItem("Unzen");
    weightCombo.addItem("Stein");
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(374, 257);
    this.setTitle("Gegenstand hinzufügen");
    this.setContentPane(getJContentPane());
    this.getRootPane().setDefaultButton(getOkButton());
    setEscapeButton(getCancelButton());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(16, 135, 66, 15));
      jLabel3.setText("Gewicht:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(16, 97, 38, 15));
      jLabel2.setText("Wert:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(16, 59, 75, 15));
      jLabel1.setText("Kategorie:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(16, 21, 57, 15));
      jLabel.setText("Name:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getNameField(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getCategoryCombo(), null);
      jContentPane.add(getValueSpinner(), null);
      jContentPane.add(getCurrencyCombo(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(getWeightSpinner(), null);
      jContentPane.add(getWeightCombo(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getJPanel(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(105, 19, 239, 19));
    }
    return nameField;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getCategoryCombo() {
    if (categoryCombo == null) {
      categoryCombo = new JComboBox();
      categoryCombo.setEditable(true);
      categoryCombo.setBounds(new java.awt.Rectangle(105, 57, 239, 19));
    }
    return categoryCombo;
  }

  /**
   * This method initializes jTextField1
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getValueSpinner() {
    if (valueSpinner == null) {
      valueSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
      valueSpinner.setBounds(new java.awt.Rectangle(105, 95, 60, 19));
    }
    return valueSpinner;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getCurrencyCombo() {
    if (currencyCombo == null) {
      currencyCombo = new JComboBox();
      currencyCombo.setBounds(new java.awt.Rectangle(190, 95, 153, 19));
    }
    return currencyCombo;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getWeightSpinner() {
    if (weightSpinner == null) {
      weightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
      weightSpinner.setBounds(new java.awt.Rectangle(105, 133, 60, 19));
    }
    return weightSpinner;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getWeightCombo() {
    if (weightCombo == null) {
      weightCombo = new JComboBox();
      weightCombo.setBounds(new java.awt.Rectangle(190, 133, 153, 19));
    }
    return weightCombo;
  }

  private Thing thing = null;

  public Thing getThing() {
    return thing;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(53, 187, 100, 25));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          okClicked();
        }
      });
    }
    return okButton;
  }

  protected void okClicked() {
    String name = nameField.getText();
    boolean newThing = nameField.isEnabled();
    if (newThing) {
      if (name.length() == 0) {
        JOptionPane.showMessageDialog(this, "Bitte einen Namen angeben!",
            "Fehler", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (Things.getInstance().getThing(name) != null) {
        JOptionPane.showMessageDialog(this,
            "Ein Gegenstand dieses Namens existiert bereits.", "Fehler",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    String category = categoryCombo.getEditor().getItem().toString();
    int value = ((Number) valueSpinner.getValue()).intValue();
    Thing.Currency currency = Thing.Currency.values()[currencyCombo
        .getSelectedIndex()];
    int weight = ((Number) weightSpinner.getValue()).intValue();
    if (weightCombo.getSelectedIndex() == 1) {
      weight *= 40;
    }
    if (newThing) {
      thing = new Thing(name, new Optional<Integer>(value), currency,
          weight, category, true);
      Things.getInstance().addThing(thing);
    }
    else {
      thing.setCategory(category);
      thing.setValue(new Optional<Integer>(value));
      thing.setCurrency(currency);
      thing.setWeight(weight);
    }
    dispose();
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(207, 187, 100, 25));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ThingDialog.this.dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes jPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jPanel = new JPanel();
      jPanel.setBounds(new java.awt.Rectangle(5, 9, 348, 158));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
    }
    return jPanel;
  }

  public final String getHelpPage() {
    return "Gegenstand_hinzufuegen";
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

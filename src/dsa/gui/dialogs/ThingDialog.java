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
import java.awt.Rectangle;
import javax.swing.JCheckBox;

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
    singularBox.setSelected(thing.isSingular());
    containerBox.setSelected(thing.isContainer());
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
    this.setSize(374, 243);
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
      jLabel3.setBounds(new Rectangle(20, 110, 81, 21));
      jLabel3.setText("Gewicht:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(20, 80, 81, 21));
      jLabel2.setText("Wert:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(20, 50, 81, 21));
      jLabel1.setText("Kategorie:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(20, 20, 81, 21));
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
      jContentPane.add(getContainerBox(), null);
      jContentPane.add(getSingularBox(), null);
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
      nameField.setBounds(new Rectangle(110, 20, 231, 21));
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
      categoryCombo.setBounds(new Rectangle(110, 50, 231, 21));
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
      valueSpinner.setBounds(new Rectangle(110, 80, 61, 21));
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
      currencyCombo.setBounds(new Rectangle(180, 80, 161, 21));
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
      weightSpinner.setBounds(new Rectangle(110, 110, 61, 21));
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
      weightCombo.setBounds(new Rectangle(180, 110, 161, 21));
    }
    return weightCombo;
  }

  private Thing thing = null;

  private JCheckBox singularBox = null;

  private JCheckBox containerBox = null;

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
      okButton.setBounds(new Rectangle(50, 180, 111, 21));
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
    boolean singular = singularBox.isSelected();
    boolean container = containerBox.isSelected();
    if (newThing) {
      thing = new Thing(name, new Optional<Integer>(value), currency,
          weight, category, true, singular, container);
      Things.getInstance().addThing(thing);
    }
    else {
      thing.setCategory(category);
      thing.setValue(new Optional<Integer>(value));
      thing.setCurrency(currency);
      thing.setWeight(weight);
      thing.setIsSingular(singular);
      thing.setIsContainer(container);
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
      cancelButton.setBounds(new Rectangle(210, 180, 100, 21));
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
      jPanel.setLayout(null);
      jPanel.setBounds(new Rectangle(10, 10, 341, 161));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
    }
    return jPanel;
  }

  public final String getHelpPage() {
    return "Gegenstand_hinzufuegen";
  }

  /**
   * This method initializes singularBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getSingularBox() {
    if (singularBox == null) {
      singularBox = new JCheckBox();
      singularBox.setBounds(new Rectangle(20, 140, 151, 21));
      singularBox.setText("Einzelstück");
    }
    return singularBox;
  }

  /**
   * This method initializes containerBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getContainerBox() {
    if (containerBox == null) {
      containerBox = new JCheckBox();
      containerBox.setBounds(new Rectangle(180, 140, 161, 21));
      containerBox.setText("Behältnis");
    }
    return containerBox;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

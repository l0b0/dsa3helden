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

import dsa.gui.lf.BGDialog;
import dsa.model.data.Armour;
import dsa.model.data.Armours;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ArmourDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JTextField nameField = null;

  private JSpinner rsSpinner = null;

  private JLabel jLabel3 = null;

  private JSpinner beSpinner = null;

  private JSpinner weightSpinner = null;

  private JComboBox unitCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private Armour armour = null;

  /**
   * This method initializes
   * 
   */
  public ArmourDialog() {
    super();
    initialize();
  }

  public ArmourDialog(JDialog owner) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
  }

  public Armour getArmour() {
    return armour;
  }

  private boolean createArmour() {
    armour = null;
    String name = nameField.getText();
    if (name == null || name.equals("")) {
      JOptionPane.showMessageDialog(this,
          "Die Rüstung muss einen Namen haben.", "Rüstung hinzufügen",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (Armours.getInstance().getArmour(name) != null) {
      JOptionPane.showMessageDialog(this,
          "Eine Rüstung mit diesem Namen existiert bereits.",
          "Rüstung hinzufügen", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    int rs = ((Number) rsSpinner.getValue()).intValue();
    int be = ((Number) beSpinner.getValue()).intValue();
    int weight = ((Number) weightSpinner.getValue()).intValue();
    if (unitCombo.getSelectedIndex() == 1) {
      weight *= 40;
    }
    armour = new Armour(name, rs, be, weight);
    return true;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(352, 191));
    this.setContentPane(getJContentPane());
    this.setTitle("Rüstung hinzufügen");

  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getOKButton(), null);
      jContentPane.add(getCancelButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(160, 40, 61, 21));
      jLabel3.setText("BE:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(10, 70, 71, 21));
      jLabel2.setText("Gewicht:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 40, 71, 21));
      jLabel1.setText("RS:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 10, 71, 21));
      jLabel.setText("Name:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(10, 10, 321, 111));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(jLabel2, null);
      jPanel.add(getJTextField(), null);
      jPanel.add(getRSSpinner(), null);
      jPanel.add(jLabel3, null);
      jPanel.add(getBESpinner(), null);
      jPanel.add(getWeightSpinner(), null);
      jPanel.add(getUnitCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getJTextField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(90, 10, 191, 21));
    }
    return nameField;
  }

  /**
   * This method initializes jTextField1
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getRSSpinner() {
    if (rsSpinner == null) {
      rsSpinner = new JSpinner();
      rsSpinner.setBounds(new java.awt.Rectangle(90, 40, 51, 21));
      rsSpinner.setModel(new SpinnerNumberModel(0, 0, 14, 1));
    }
    return rsSpinner;
  }

  /**
   * This method initializes jTextField2
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getBESpinner() {
    if (beSpinner == null) {
      beSpinner = new JSpinner();
      beSpinner.setBounds(new java.awt.Rectangle(230, 40, 51, 21));
      beSpinner.setModel(new SpinnerNumberModel(1, 0, 14, 1));
    }
    return beSpinner;
  }

  /**
   * This method initializes jTextField3
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getWeightSpinner() {
    if (weightSpinner == null) {
      weightSpinner = new JSpinner();
      weightSpinner.setBounds(new java.awt.Rectangle(90, 70, 51, 21));
      weightSpinner.setModel(new SpinnerNumberModel(50, 0, 5000, 1));
    }
    return weightSpinner;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getUnitCombo() {
    if (unitCombo == null) {
      unitCombo = new JComboBox();
      unitCombo.setBounds(new java.awt.Rectangle(160, 70, 121, 21));
      unitCombo.addItem("Unzen");
      unitCombo.addItem("Stein");
      unitCombo.setSelectedIndex(0);
    }
    return unitCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(50, 130, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (createArmour()) {
            dispose();
          }
        }
      });
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
      cancelButton.setBounds(new java.awt.Rectangle(190, 130, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          armour = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

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

import dsa.gui.lf.BGDialog;
import dsa.model.data.Shield;
import dsa.model.data.Shields;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ShieldDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JTextField nameField = null;

  private JSpinner paSpinner = null;

  private JLabel jLabel2 = null;

  private JSpinner atSpinner = null;

  private JCheckBox daggerBox = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JSpinner pa2Spinner = null;

  private JLabel jLabel5 = null;

  private JSpinner beSpinner = null;

  private JSpinner bfSpinner = null;

  private JLabel jLabel6 = null;

  private JSpinner fkSpinner = null;

  private JLabel jLabel7 = null;

  private JSpinner weightSpinner = null;

  private JComboBox weightCombo = null;

  private JLabel jLabel8 = null;

  private JSpinner valueSpinner = null;

  private JComboBox valueCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private Shield shield = null;

  /**
   * This method initializes
   * 
   */
  public ShieldDialog() {
    super();
    initialize();
  }

  public ShieldDialog(JDialog owner) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
  }
  
  public ShieldDialog(JDialog owner, Shield shield) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
    this.shield = shield;
    nameField.setText(shield.getName());
    nameField.setEnabled(false);
    int weight = shield.getWeight();
    if (weight > 0 && weight % 40 == 0) {
      weightCombo.setSelectedIndex(1);
      weight /= 40;
    }
    weightSpinner.setValue(weight);
    int worth = shield.getWorth();
    if (worth > 0 && worth % 10 == 0) {
      valueCombo.setSelectedIndex(1);
      worth /= 10;
    }
    valueSpinner.setValue(worth);
    beSpinner.setValue(shield.getBeMod());
    atSpinner.setValue(shield.getAtMod());
    paSpinner.setValue(shield.getPaMod());
    pa2Spinner.setValue(shield.getPaMod2());
    daggerBox.setSelected(shield.getFkMod() == 0);
    fkSpinner.setValue(-shield.getFkMod());
    fkSpinner.setEnabled(shield.getFkMod() != 0);
    pa2Spinner.setEnabled(shield.getFkMod() == 0);
    bfSpinner.setValue(shield.getBF());
  }
  
  public final String getHelpPage() {
    return "Parade_hinzufuegen";
  }

  public Shield getCreatedShield() {
    return shield;
  }

  private boolean createShield() {
    String name = getNameField().getText();
    boolean createShield = getNameField().isEnabled();
    if (createShield) {
      shield = null;
      if (name == null || name.length() == 0) {
        JOptionPane.showMessageDialog(this,
            "Die Parierhilfe muss einen Namen haben.", "Fehler",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (Shields.getInstance().getShield(name) != null) {
        JOptionPane.showMessageDialog(this,
            "Eine Parierhilfe mit diesem Namen exisitiert bereits.", "Fehler",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    int pa = ((Number) getPASpinner().getValue()).intValue();
    int at = ((Number) getATSpinner().getValue()).intValue();
    boolean dagger = getDaggerBox().isSelected();
    int pa2 = pa;
    if (dagger) {
      pa2 = ((Number) getPA2Spinner().getValue()).intValue();
    }
    int be = ((Number) getBESpinner().getValue()).intValue();
    int fk = 0;
    if (!dagger) {
      fk = -((Number) getFKSpinner().getValue()).intValue();
    }
    int bf = ((Number) getBFSpinner().getValue()).intValue();
    int weight = ((Number) getWeightSpinner().getValue()).intValue();
    if (getWeightCombo().getSelectedIndex() == 1) {
      weight *= 40;
    }
    int value = ((Number) getValueSpinner().getValue()).intValue();
    if (getValueCombo().getSelectedIndex() == 1) {
      value *= 10;
    }
    if (createShield) {
      shield = new Shield(name, at, pa, pa2, be, fk, bf, weight, value);
    }
    else {
      shield.setAtMod(at);
      shield.setPaMod(pa);
      shield.setPaMod2(pa2);
      shield.setBeMod(be);
      shield.setFkMod(fk);
      shield.setBF(bf);
      shield.setWeight(weight);
      shield.setWorth(value);
    }
    return true;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(366, 317));
    this.setContentPane(getJContentPane());
    this.setTitle("Paradehilfe hinzufügen");
    this.getRootPane().setDefaultButton(getOKButton());
    setEscapeButton(getCancelButton());
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
      jLabel8 = new JLabel();
      jLabel8.setBounds(new java.awt.Rectangle(10, 190, 91, 21));
      jLabel8.setText("Wert:");
      jLabel7 = new JLabel();
      jLabel7.setBounds(new java.awt.Rectangle(10, 160, 91, 21));
      jLabel7.setText("Gewicht:");
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(10, 130, 91, 21));
      jLabel6.setText("FK-Bonus:");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(180, 100, 81, 21));
      jLabel5.setText("Bruchfaktor:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(10, 100, 91, 21));
      jLabel4.setText("Behinderung:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(180, 70, 81, 21));
      jLabel3.setText("PA-Bonus 2:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(180, 40, 81, 21));
      jLabel2.setText("AT-Malus:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 40, 91, 21));
      jLabel1.setText("PA-Bonus:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 10, 91, 21));
      jLabel.setText("Name:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(10, 10, 331, 230));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(getNameField(), null);
      jPanel.add(getPASpinner(), null);
      jPanel.add(jLabel2, null);
      jPanel.add(getATSpinner(), null);
      jPanel.add(getDaggerBox(), null);
      jPanel.add(jLabel3, null);
      jPanel.add(jLabel4, null);
      jPanel.add(getPA2Spinner(), null);
      jPanel.add(jLabel5, null);
      jPanel.add(getBESpinner(), null);
      jPanel.add(getBFSpinner(), null);
      jPanel.add(jLabel6, null);
      jPanel.add(getFKSpinner(), null);
      jPanel.add(jLabel7, null);
      jPanel.add(getWeightSpinner(), null);
      jPanel.add(getWeightCombo(), null);
      jPanel.add(jLabel8, null);
      jPanel.add(getValueSpinner(), null);
      jPanel.add(getValueCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(110, 10, 211, 21));
    }
    return nameField;
  }

  /**
   * This method initializes jTextField1
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getPASpinner() {
    if (paSpinner == null) {
      paSpinner = new JSpinner();
      paSpinner.setBounds(new java.awt.Rectangle(110, 40, 51, 21));
      paSpinner.setModel(new SpinnerNumberModel(1, 0, 7, 1));
      paSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          getPA2Spinner().setValue(getPASpinner().getValue());
        }
      });
    }
    return paSpinner;
  }

  /**
   * This method initializes jTextField2
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getATSpinner() {
    if (atSpinner == null) {
      atSpinner = new JSpinner();
      atSpinner.setBounds(new java.awt.Rectangle(270, 40, 51, 21));
      atSpinner.setModel(new SpinnerNumberModel(0, 0, 9, 1));
    }
    return atSpinner;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getDaggerBox() {
    if (daggerBox == null) {
      daggerBox = new JCheckBox();
      daggerBox.setBounds(new java.awt.Rectangle(10, 70, 151, 21));
      daggerBox.setText("Ist Parierdolch");
      daggerBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getPA2Spinner().setEnabled(daggerBox.isSelected());
          if (daggerBox.isSelected()) {
            getFKSpinner().setValue(Integer.valueOf(0));
          }
          getFKSpinner().setEnabled(!daggerBox.isSelected());
        }
      });
    }
    return daggerBox;
  }

  /**
   * This method initializes jTextField3
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getPA2Spinner() {
    if (pa2Spinner == null) {
      pa2Spinner = new JSpinner();
      pa2Spinner.setBounds(new java.awt.Rectangle(270, 70, 51, 21));
      pa2Spinner.setModel(new SpinnerNumberModel(1, 0, 9, 1));
      pa2Spinner.setEnabled(false);
    }
    return pa2Spinner;
  }

  /**
   * This method initializes jTextField4
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getBESpinner() {
    if (beSpinner == null) {
      beSpinner = new JSpinner();
      beSpinner.setBounds(new java.awt.Rectangle(110, 100, 51, 21));
      beSpinner.setModel(new SpinnerNumberModel(1, 0, 12, 1));
    }
    return beSpinner;
  }

  /**
   * This method initializes jTextField5
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getBFSpinner() {
    if (bfSpinner == null) {
      bfSpinner = new JSpinner();
      bfSpinner.setBounds(new java.awt.Rectangle(270, 100, 51, 21));
      bfSpinner.setModel(new SpinnerNumberModel(2, -10, 12, 1));
    }
    return bfSpinner;
  }

  /**
   * This method initializes jTextField6
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getFKSpinner() {
    if (fkSpinner == null) {
      fkSpinner = new JSpinner();
      fkSpinner.setBounds(new java.awt.Rectangle(110, 130, 51, 21));
      fkSpinner.setModel(new SpinnerNumberModel(0, 0, 3, 1));
    }
    return fkSpinner;
  }

  /**
   * This method initializes jTextField7
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getWeightSpinner() {
    if (weightSpinner == null) {
      weightSpinner = new JSpinner();
      weightSpinner.setBounds(new java.awt.Rectangle(110, 160, 51, 21));
      weightSpinner.setModel(new SpinnerNumberModel(50, 0, 5000, 1));
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
      weightCombo.setBounds(new java.awt.Rectangle(180, 160, 141, 21));
      weightCombo.addItem("Unzen");
      weightCombo.addItem("Stein");
      weightCombo.setSelectedIndex(0);
    }
    return weightCombo;
  }

  /**
   * This method initializes jTextField8
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getValueSpinner() {
    if (valueSpinner == null) {
      valueSpinner = new JSpinner();
      valueSpinner.setBounds(new java.awt.Rectangle(110, 190, 51, 21));
      valueSpinner.setModel(new SpinnerNumberModel(40, 0, 500, 1));
    }
    return valueSpinner;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getValueCombo() {
    if (valueCombo == null) {
      valueCombo = new JComboBox();
      valueCombo.setBounds(new java.awt.Rectangle(180, 190, 141, 21));
      valueCombo.addItem("Silbertaler");
      valueCombo.addItem("Dukaten");
      valueCombo.setSelectedIndex(0);
    }
    return valueCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(60, 250, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (createShield()) dispose();
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
      cancelButton.setBounds(new java.awt.Rectangle(190, 250, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          shield = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

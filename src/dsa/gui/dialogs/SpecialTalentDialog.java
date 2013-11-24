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

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import dsa.gui.lf.BGDialog;
import dsa.gui.util.ImageManager;
import dsa.model.DataFactory;
import dsa.model.characters.Group;
import dsa.model.characters.Property;
import dsa.model.data.Talents;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Talent;

public class SpecialTalentDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JComboBox talentBox = null;

  private JPanel jPanel = null;

  private JLabel jLabel1 = null;

  private JComboBox firstPropertyBox = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JComboBox secondPropertyBox = null;

  private JComboBox thirdPropertyBox = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private JCheckBox probeBox = null;

  /**
   * This is the default constructor
   */
  public SpecialTalentDialog() {
    super();
    initialize();
  }

  public SpecialTalentDialog(java.awt.Frame parent) {
    super(parent);
    initialize();
    initializeLogic();
  }

  private static String otherTalentString = "<Neu ...>";

  private JLabel jLabel4 = null;

  private JSpinner incrField = null;

  private JLabel jLabel5 = null;

  private JTextField nameField = null;

  private void initializeLogic() {
    for (Property property : Property.values()) {
      if (property == Property.HA) break;
      String text = property.name();
      getFirstPropertyBox().addItem(text);
      getSecondPropertyBox().addItem(text);
      getThirdPropertyBox().addItem(text);
    }
    JComboBox talentBox = getTalentBox();
    for (dsa.model.talents.Talent t : dsa.model.data.Talents.getInstance()
        .getTalentsInCategory("Berufe / Sonstiges")) {
      talentBox.addItem(t.getName());
    }
    talentBox.removeItem("Jagen (Falle)");
    talentBox.removeItem("Jagen (Pirsch)");
    talentBox.addItem(otherTalentString);
    talentBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        talentSelected();
      }
    });
    talentBox.setSelectedIndex(0);
    getProbeBox().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean withProbe = !getProbeBox().isSelected();
        getFirstPropertyBox().setEnabled(withProbe);
        getSecondPropertyBox().setEnabled(withProbe);
        getThirdPropertyBox().setEnabled(withProbe);
      }
    });
    getCancelButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closedByOK = false;
        talent = null;
        dispose();
      }
    });
    getOkButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String tName = getTalentBox().getSelectedItem().toString();
        if (tName.equals(otherTalentString)) {
          String name = nameField.getText();
          if (name.equals("")) {
            JOptionPane.showMessageDialog(SpecialTalentDialog.this,
                "Das Talent muss einen Namen haben.", "Fehler",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          if (Talents.getInstance().getTalent(name) != null) {
            JOptionPane.showMessageDialog(SpecialTalentDialog.this,
                "Ein Talent dieses Namens gibt es schon.", "Fehler",
                JOptionPane.ERROR_MESSAGE);
            return;
          }
          createTalent(name);
          Talents.getInstance().addUserTalent(talent);
        }
        else if (Talents.getInstance().isUserTalent(tName)) {
          Talents.getInstance().removeUserTalent(tName);
          createTalent(tName);
          Talents.getInstance().addUserTalent(talent);
        }
        else {
          talent = Talents.getInstance().getTalent(tName);
        }
        closedByOK = true;
        dispose();
      }
    });
    getRemoveButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(SpecialTalentDialog.this,
            "Dies entfernt das Talent von allen Helden. Sicher?", "Entfernen",
            JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
          return;
        }
        String tName = getTalentBox().getSelectedItem().toString();
        for (dsa.model.characters.Hero hero : Group.getInstance()
            .getAllCharacters()) {
          if (hero.hasTalent(tName)) {
            hero.removeTalent(tName);
          }
        }
        Talents.getInstance().removeUserTalent(tName);
        getTalentBox().removeItem(tName);
        getTalentBox().setSelectedIndex(0);
      }
    });
    talentSelected();
  }

  private boolean closedByOK;

  private dsa.model.talents.Talent talent;

  private JButton removeButton = null;

  public boolean WasClosedByOK() {
    return closedByOK;
  }

  public String GetTalent() {
    return talent.getName();
  }

  private void talentSelected() {
    String talentName = getTalentBox().getSelectedItem().toString();
    if (talentName.equals(otherTalentString)) {
      getNameField().setEnabled(true);
      getNameField().setText("");
      getFirstPropertyBox().setEnabled(true);
      getFirstPropertyBox().setSelectedIndex(0);
      getSecondPropertyBox().setEnabled(true);
      getSecondPropertyBox().setSelectedIndex(0);
      getThirdPropertyBox().setEnabled(true);
      getThirdPropertyBox().setSelectedIndex(0);
      getProbeBox().setEnabled(true);
      getProbeBox().setSelected(false);
      getIncrField().setEnabled(true);
      getIncrField().setValue(0);
      getRemoveButton().setEnabled(false);
    }
    else if (!Talents.getInstance().isUserTalent(talentName)) {
      NormalTalent t = (NormalTalent) Talents.getInstance().getTalent(
          talentName);
      getNameField().setEnabled(false);
      getNameField().setText(t.getName());
      getFirstPropertyBox().setEnabled(false);
      getFirstPropertyBox().setSelectedIndex(t.getFirstProperty().ordinal());
      getSecondPropertyBox().setEnabled(false);
      getSecondPropertyBox().setSelectedIndex(t.getSecondProperty().ordinal());
      getThirdPropertyBox().setEnabled(false);
      getThirdPropertyBox().setSelectedIndex(t.getThirdProperty().ordinal());
      getProbeBox().setEnabled(false);
      getProbeBox().setSelected(true);
      getIncrField().setEnabled(false);
      getIncrField().setValue(t.getMaxIncreasePerStep());
      getRemoveButton().setEnabled(false);
    }
    else {
      Talent t = Talents.getInstance().getTalent(talentName);
      getNameField().setEnabled(false);
      getNameField().setText(t.getName());
      boolean testable = t.canBeTested();
      getProbeBox().setEnabled(true);
      getProbeBox().setSelected(!testable);
      getIncrField().setEnabled(true);
      getIncrField().setValue(t.getMaxIncreasePerStep());
      getFirstPropertyBox().setEnabled(testable);
      getSecondPropertyBox().setEnabled(testable);
      getThirdPropertyBox().setEnabled(testable);
      if (testable) {
        NormalTalent nt = (NormalTalent) t;
        getFirstPropertyBox().setSelectedIndex(nt.getFirstProperty().ordinal());
        getSecondPropertyBox().setSelectedIndex(
            nt.getSecondProperty().ordinal());
        getThirdPropertyBox().setSelectedIndex(nt.getThirdProperty().ordinal());
      }
      getRemoveButton().setEnabled(true);
    }
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(315, 323);
    this.setTitle("Talent hinzufügen");
    this.setContentPane(getJContentPane());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(18, 20, 54, 18));
      jLabel.setText("Talent:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getTalentBox(), null);
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(getCancelButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getTalentBox() {
    if (talentBox == null) {
      talentBox = new JComboBox();
      talentBox.setBounds(new java.awt.Rectangle(84, 20, 195, 19));
    }
    return talentBox;
  }

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(15, 13, 45, 15));
      jLabel5.setText("Name:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(15, 160, 139, 15));
      jLabel4.setText("Steigerungen / Stufe:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(15, 106, 104, 15));
      jLabel3.setText("Eigenschaft 3:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(15, 77, 104, 15));
      jLabel2.setText("Eigenschaft 2:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(15, 46, 104, 15));
      jLabel1.setText("Eigenschaft 1:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(18, 50, 261, 193));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel1, null);
      jPanel.add(getFirstPropertyBox(), null);
      jPanel.add(jLabel2, null);
      jPanel.add(jLabel3, null);
      jPanel.add(getSecondPropertyBox(), null);
      jPanel.add(getThirdPropertyBox(), null);
      jPanel.add(getProbeBox(), null);
      jPanel.add(jLabel4, null);
      jPanel.add(getIncrField(), null);
      jPanel.add(jLabel5, null);
      jPanel.add(getNameField(), null);
      jPanel.add(getRemoveButton(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getFirstPropertyBox() {
    if (firstPropertyBox == null) {
      firstPropertyBox = new JComboBox();
      firstPropertyBox.setBounds(new java.awt.Rectangle(167, 46, 66, 17));
    }
    return firstPropertyBox;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getSecondPropertyBox() {
    if (secondPropertyBox == null) {
      secondPropertyBox = new JComboBox();
      secondPropertyBox.setBounds(new java.awt.Rectangle(167, 77, 66, 17));
    }
    return secondPropertyBox;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getThirdPropertyBox() {
    if (thirdPropertyBox == null) {
      thirdPropertyBox = new JComboBox();
      thirdPropertyBox.setBounds(new java.awt.Rectangle(167, 106, 66, 17));
    }
    return thirdPropertyBox;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(30, 261, 105, 20));
      okButton.setSelected(true);
      okButton.setText("OK");
    }
    return okButton;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(162, 261, 105, 20));
      cancelButton.setText("Abbrechen");
    }
    return cancelButton;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getProbeBox() {
    if (probeBox == null) {
      probeBox = new JCheckBox();
      probeBox.setBounds(new java.awt.Rectangle(13, 133, 180, 15));
      probeBox.setText("Keine Probe möglich");
    }
    return probeBox;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getIncrField() {
    if (incrField == null) {
      incrField = new JSpinner(new SpinnerNumberModel(0, 0, 2, 1));
      incrField.setBounds(new java.awt.Rectangle(167, 160, 66, 17));
    }
    return incrField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(69, 13, 134, 19));
    }
    return nameField;
  }

  private void createTalent(String name) {
    if (getProbeBox().isSelected()) {
      talent = DataFactory.getInstance().createOtherTalent(name,
          ((Number) getIncrField().getValue()).intValue());
    }
    else {
      talent = DataFactory.getInstance().createNormalTalent(
          name,
          dsa.model.characters.Property.values()[getFirstPropertyBox()
              .getSelectedIndex()],
          dsa.model.characters.Property.values()[getSecondPropertyBox()
              .getSelectedIndex()],
          dsa.model.characters.Property.values()[getThirdPropertyBox()
              .getSelectedIndex()],
          ((Number) getIncrField().getValue()).intValue());
    }
  }

  /**
   * This method initializes jButton1	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton();
      removeButton.setBounds(new java.awt.Rectangle(212, 13, 34, 19));
      removeButton.setIcon(ImageManager.getIcon("decrease_enabled"));
    }
    return removeButton;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

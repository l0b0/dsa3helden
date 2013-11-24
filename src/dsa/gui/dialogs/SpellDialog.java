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
 along with Heldenverwaltung; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import dsa.gui.lf.BGDialog;
import dsa.model.DataFactory;
import dsa.model.characters.Property;
import dsa.model.data.Talents;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;

public final class SpellDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JTextField nameField = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JLabel jLabel5 = null;

  private JComboBox property1Combo = null;

  private JComboBox property2Combo = null;

  private JComboBox property3Combo = null;

  private JComboBox categoryCombo = null;

  private JComboBox originCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private Spell spell = null;

  /**
   * This method initializes
   * 
   */
  public SpellDialog() {
    super();
    initialize();
  }

  public SpellDialog(JDialog owner) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
  }
  
  public SpellDialog(JDialog owner, Spell spell) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
    this.spell = spell;
    nameField.setText(spell.getName());
    nameField.setEnabled(false);
    originCombo.setSelectedItem(spell.getOrigin());
    property1Combo.setSelectedIndex(spell.getFirstProperty().ordinal());
    property2Combo.setSelectedIndex(spell.getSecondProperty().ordinal());
    property3Combo.setSelectedIndex(spell.getThirdProperty().ordinal());
    categoryCombo.setSelectedItem(spell.getCategory());
  }

  public Spell getCreatedSpell() {
    return spell;
  }
  
  public final String getHelpPage() {
    return "Zauber_hinzufuegen";
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setTitle("Zauber hinzufügen");
    this.setContentPane(getJContentPane());
    this.setSize(new java.awt.Dimension(376, 285));
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
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(10, 160, 101, 21));
      jLabel5.setText("Ursprung:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(10, 130, 101, 21));
      jLabel4.setText("Kategorie:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(10, 100, 101, 21));
      jLabel3.setText("Eigenschaft 3:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(10, 70, 101, 21));
      jLabel2.setText("Eigenschaft 2:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 40, 101, 21));
      jLabel1.setText("Eigenschaft 1:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 10, 101, 21));
      jLabel.setText("Name:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(10, 10, 341, 201));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(getNameField(), null);
      jPanel.add(jLabel1, null);
      jPanel.add(jLabel2, null);
      jPanel.add(jLabel3, null);
      jPanel.add(jLabel4, null);
      jPanel.add(jLabel5, null);
      jPanel.add(getProperty1Combo(), null);
      jPanel.add(getProperty2Combo(), null);
      jPanel.add(getProperty3Combo(), null);
      jPanel.add(getCategoryCombo(), null);
      jPanel.add(getOriginCombo(), null);
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
      nameField.setBounds(new java.awt.Rectangle(120, 10, 201, 21));
    }
    return nameField;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getProperty1Combo() {
    if (property1Combo == null) {
      property1Combo = new JComboBox();
      property1Combo.setBounds(new java.awt.Rectangle(120, 40, 81, 21));
      for (Property p : Property.values()) {
        property1Combo.addItem(p);
      }
    }
    return property1Combo;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getProperty2Combo() {
    if (property2Combo == null) {
      property2Combo = new JComboBox();
      property2Combo.setBounds(new java.awt.Rectangle(120, 70, 81, 21));
      for (Property p : Property.values()) {
        property2Combo.addItem(p);
      }
    }
    return property2Combo;
  }

  /**
   * This method initializes jComboBox2
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getProperty3Combo() {
    if (property3Combo == null) {
      property3Combo = new JComboBox();
      property3Combo.setBounds(new java.awt.Rectangle(120, 100, 81, 21));
      for (Property p : Property.values()) {
        property3Combo.addItem(p);
      }
    }
    return property3Combo;
  }

  /**
   * This method initializes jComboBox3
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getCategoryCombo() {
    if (categoryCombo == null) {
      categoryCombo = new JComboBox();
      categoryCombo.setBounds(new java.awt.Rectangle(120, 130, 201, 21));
      HashSet<String> categories = new HashSet<String>();
      for (Talent t : Talents.getInstance().getTalentsInCategory("Zauber")) {
        if (!(t instanceof Spell)) continue;
        Spell s = (Spell) t;
        if (!categories.contains(s.getCategory())) {
          categories.add(s.getCategory());
          categoryCombo.addItem(s.getCategory());
        }
      }
    }
    return categoryCombo;
  }

  /**
   * This method initializes jComboBox4
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getOriginCombo() {
    if (originCombo == null) {
      originCombo = new JComboBox();
      originCombo.setBounds(new java.awt.Rectangle(120, 160, 201, 21));
      HashSet<String> origins = new HashSet<String>();
      for (Talent t : Talents.getInstance().getTalentsInCategory("Zauber")) {
        if (!(t instanceof Spell)) continue;
        Spell s = (Spell) t;
        if (!origins.contains(s.getOrigin())) {
          origins.add(s.getOrigin());
          originCombo.addItem(s.getOrigin());
        }
      }
    }
    return originCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(60, 220, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (createSpell()) dispose();
        }
      });
    }
    return okButton;
  }

  protected boolean createSpell() {
    String name = nameField.getText();
    boolean createSpell = nameField.isEnabled();
    if (createSpell) {
      spell = null;
      if (name == null || name.length() == 0) {
        JOptionPane.showMessageDialog(this, "Der Zauber muss einen Namen haben.",
            "Fehler", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (Talents.getInstance().getTalent(name) != null) {
        JOptionPane.showMessageDialog(this,
            "Ein Talent oder Zauber mit diesem Namen existiert bereits.",
            "Fehler", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    Property p1 = (Property) property1Combo.getSelectedItem();
    Property p2 = (Property) property2Combo.getSelectedItem();
    Property p3 = (Property) property3Combo.getSelectedItem();
    String category = (String) categoryCombo.getSelectedItem();
    String origin = (String) originCombo.getSelectedItem();
    if (!createSpell) {
      Talents.getInstance().removeUserSpell(spell.getName());
    }
    spell = DataFactory.getInstance().createUserDefinedSpell(name, p1, p2, p3,
        category, origin);
    return true;
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(200, 220, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          spell = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

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

import java.util.StringTokenizer;

import dsa.gui.lf.BGDialog;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.util.Optional;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import java.awt.Rectangle;

public final class WeaponDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JSpinner wDamageSpinner = null;

  private JLabel jLabel5 = null;

  private JSpinner constDamageSpinner = null;

  private JSpinner kkSpinner = null;

  private JLabel jLabel6 = null;

  private JSpinner bfSpinner = null;

  private JSpinner weightSpinner = null;

  private JComboBox weightCombo = null;

  private JComboBox categoryCombo = null;

  private JTextField nameField = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private Weapon weapon = null;

  private JCheckBox twoHandedBox = null;

  private JLabel jLabel7 = null;

  private JTextField distancesField = null;

  private JLabel jLabel8 = null;

  private JTextField distModsField = null;

  private JLabel jLabel9 = null;

  private JSpinner worthSpinner = null;

  private JComboBox worthCombo = null;

  private JLabel jLabel10 = null;

  private JComboBox projectileBox = null;

  private JLabel jLabel11 = null;

  private JSpinner ptWeightSpinner = null;

  private JLabel jLabel12 = null;

  private JLabel jLabel13 = null;

  private JSpinner ptWorthSpinner = null;

  private JLabel jLabel14 = null;

  public Weapon getCreatedWeapon() {
    return weapon;
  }

  /**
   * This method initializes
   * 
   */
  public WeaponDialog(JDialog owner) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
  }
  
  public WeaponDialog(JDialog owner, Weapon weapon) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
    this.weapon = weapon;
    nameField.setText(weapon.getName());
    nameField.setEnabled(false);
    bfSpinner.setValue(weapon.getBF());
    categoryCombo.setSelectedItem(Weapons.getCategoryName(weapon.getType()));
    constDamageSpinner.setValue(weapon.getConstDamage());
    if (weapon.isFarRangedWeapon()) {
      distancesField.setEnabled(true);
      String distances = "";
      for (Weapon.Distance d : Weapon.Distance.values()) {
        distances += weapon.getDistancePaces(d);
        if (d != Weapon.Distance.ExtremelyFar) {
          distances += "/";
        }
      }
      distancesField.setText(distances);
      distModsField.setEnabled(true);
      String distMods = "";
      for (Weapon.Distance d : Weapon.Distance.values()) {
        distMods += weapon.getDistanceCategoryTPMod(d);
        if (d != Weapon.Distance.ExtremelyFar) {
          distMods += "/";
        }
      }
      distModsField.setText(distMods);
      twoHandedBox.setEnabled(false);
      twoHandedBox.setSelected(true);
    }
    else {
      distancesField.setEnabled(false);
      distModsField.setEnabled(false);
      twoHandedBox.setEnabled(true);
      twoHandedBox.setSelected(weapon.isTwoHanded());
    }
    if (weapon.isProjectileWeapon()) {
      projectileBox.setEnabled(true);
      projectileBox.setSelectedItem(weapon.getProjectileType());
      ptWeightSpinner.setEnabled(true);
      if (weapon.getProjectileWeight().hasValue()) {
        ptWeightSpinner.setValue(weapon.getProjectileWeight().getValue());
      }
      else {
        ptWeightSpinner.setValue(0);
      }
      ptWorthSpinner.setEnabled(true);
      if (weapon.getProjectileWorth().hasValue()) {
        ptWorthSpinner.setValue(weapon.getProjectileWorth().getValue());
      }
      else {
        ptWorthSpinner.setValue(0);
      }
    }
    else {
      projectileBox.setSelectedIndex(0);
      projectileBox.setEnabled(false);
      ptWeightSpinner.setEnabled(false);
      ptWorthSpinner.setEnabled(false);
    }
    kkSpinner.setValue(weapon.getKKBonus().hasValue() ? weapon.getKKBonus().getValue() : 21);
    wDamageSpinner.setValue(weapon.getW6damage());
    int weight = weapon.getWeight();
    if (weight > 0 && weight % 40 == 0) {
      weightCombo.setSelectedIndex(1);
      weight /= 40;
    }
    weightSpinner.setValue(weight);
    int worth = weapon.getWorth().hasValue() ? weapon.getWorth().getValue() : 0;
    if (worth > 0 && worth % 10 == 0) {
      worthCombo.setSelectedIndex(1);
      worth /= 10;
    }
    worthSpinner.setValue(worth);
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(339, 419));
    this.setContentPane(getJContentPane());
    this.setTitle("Waffe hinzufügen");

  }

  public final String getHelpPage() {
    return "Waffe_hinzufuegen";
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
      jContentPane.add(getOkButton(), null);
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
      jLabel14 = new JLabel();
      jLabel14.setBounds(new Rectangle(280, 310, 11, 21));
      jLabel14.setText("K");
      jLabel13 = new JLabel();
      jLabel13.setBounds(new Rectangle(180, 310, 36, 20));
      jLabel13.setText("Wert:");
      jLabel12 = new JLabel();
      jLabel12.setBounds(new Rectangle(160, 310, 11, 20));
      jLabel12.setText("U");
      jLabel11 = new JLabel();
      jLabel11.setBounds(new Rectangle(10, 310, 91, 21));
      jLabel11.setText("Gewicht:");
      jLabel10 = new JLabel();
      jLabel10.setBounds(new Rectangle(10, 280, 91, 21));
      jLabel10.setText("Geschosse:");
      jLabel9 = new JLabel();
      jLabel9.setBounds(new java.awt.Rectangle(10,160,91,21));
      jLabel9.setText("Wert:");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new Rectangle(10, 250, 91, 21));
      jLabel8.setText("TP-Boni:");
      jLabel7 = new JLabel();
      jLabel7.setBounds(new Rectangle(10, 220, 91, 21));
      jLabel7.setText("Reichweiten:");
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(210,100,31,21));
      jLabel6.setText("BF:");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(170,70,31,21));
      jLabel5.setText("W +");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(10, 130, 91, 21));
      jLabel4.setText("Gewicht:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(10, 100, 91, 21));
      jLabel3.setText("KK-Zuschlag:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(10, 70, 81, 21));
      jLabel2.setText("Schaden:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 40, 81, 21));
      jLabel1.setText("Kategorie:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 10, 81, 21));
      jLabel.setText("Name:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new Rectangle(8, 8, 309, 343));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(jLabel2, null);
      jPanel.add(jLabel3, null);
      jPanel.add(jLabel4, null);
      jPanel.add(getWDamageSpinner(), null);
      jPanel.add(jLabel5, null);
      jPanel.add(getConstDamageSpinner(), null);
      jPanel.add(getKkSpinner(), null);
      jPanel.add(jLabel6, null);
      jPanel.add(getBfSpinner(), null);
      jPanel.add(getWeightSpinner(), null);
      jPanel.add(getWeightCombo(), null);
      jPanel.add(getCategoryCombo(), null);
      jPanel.add(getNameField(), null);
      jPanel.add(getTwoHandedBox(), null);
      jPanel.add(jLabel7, null);
      jPanel.add(getDistancesField(), null);
      jPanel.add(jLabel8, null);
      jPanel.add(getDistModsField(), null);
      jPanel.add(jLabel9, null);
      jPanel.add(getWorthSpinner(), null);
      jPanel.add(getWorthCombo(), null);
      jPanel.add(jLabel10, null);
      jPanel.add(getProjectileBox(), null);
      jPanel.add(jLabel11, null);
      jPanel.add(getPtWeightSpinner(), null);
      jPanel.add(jLabel12, null);
      jPanel.add(jLabel13, null);
      jPanel.add(getPtWorthSpinner(), null);
      jPanel.add(jLabel14, null);
    }
    return jPanel;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getWDamageSpinner() {
    if (wDamageSpinner == null) {
      wDamageSpinner = new JSpinner();
      wDamageSpinner.setBounds(new java.awt.Rectangle(110,70,51,21));
      wDamageSpinner.setModel(new SpinnerNumberModel(1, 0, 7, 1));
    }
    return wDamageSpinner;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getConstDamageSpinner() {
    if (constDamageSpinner == null) {
      constDamageSpinner = new JSpinner();
      constDamageSpinner.setBounds(new java.awt.Rectangle(210,70,41,21));
      constDamageSpinner.setModel(new SpinnerNumberModel(0, 0, 20, 1));
    }
    return constDamageSpinner;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getKkSpinner() {
    if (kkSpinner == null) {
      kkSpinner = new JSpinner();
      kkSpinner.setBounds(new java.awt.Rectangle(110,100,51,21));
      kkSpinner.setModel(new SpinnerNumberModel(15, 12, 21, 1));
    }
    return kkSpinner;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getBfSpinner() {
    if (bfSpinner == null) {
      bfSpinner = new JSpinner();
      bfSpinner.setBounds(new java.awt.Rectangle(250, 100, 41, 21));
      bfSpinner.setModel(new SpinnerNumberModel(2, -10, 12, 1));
    }
    return bfSpinner;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getWeightSpinner() {
    if (weightSpinner == null) {
      weightSpinner = new JSpinner();
      weightSpinner.setBounds(new java.awt.Rectangle(110,130,51,21));
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
      weightCombo.setBounds(new java.awt.Rectangle(170,130,121,20));
      weightCombo.addItem("Unzen");
      weightCombo.addItem("Stein");
      weightCombo.setSelectedIndex(0);
    }
    return weightCombo;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getCategoryCombo() {
    if (categoryCombo == null) {
      categoryCombo = new JComboBox();
      categoryCombo.setBounds(new java.awt.Rectangle(110, 40, 181, 20));
      for (String category : Weapons.getAvailableCategories()) {
        categoryCombo.addItem(category);
      }
      categoryCombo.removeItem("Linkshändig");
      categoryCombo.setSelectedIndex(2);
      categoryCombo.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          boolean p = Weapons.isFarRangedCategory(categoryCombo
              .getSelectedItem().toString());
          twoHandedBox.setEnabled(!p);
          twoHandedBox.setSelected(p);
          distancesField.setEnabled(p);
          distModsField.setEnabled(p);
          if (Weapons.isProjectileCategory(categoryCombo.getSelectedItem().toString())) {
            projectileBox.setEnabled(true);
            ptWeightSpinner.setEnabled(true);
            ptWorthSpinner.setEnabled(true);
          }
          else {
            projectileBox.setSelectedIndex(0);
            projectileBox.setEnabled(false);
            ptWeightSpinner.setEnabled(false);
            ptWorthSpinner.setEnabled(false);
          }
        }
      });
    }
    return categoryCombo;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new java.awt.Rectangle(110, 10, 181, 21));
    }
    return nameField;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(50, 360, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (createWeapon()) {
            dispose();
          }
        }
      });
    }
    return okButton;
  }

  protected boolean createWeapon() {
    String name = nameField.getText();
    boolean createWeapon = nameField.isEnabled();
    if (createWeapon) {
      if (name == null || name.equals("")) {
        JOptionPane.showMessageDialog(this, "Bitte einen Namen eingeben!",
            "Waffe hinzufügen", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      if (Weapons.getInstance().getWeapon(name) != null) {
        JOptionPane.showMessageDialog(this,
            "Eine Waffe dieses Namens existiert bereits.", "Waffe hinzufügen",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    int category = Weapons.getCategoryIndex(categoryCombo.getSelectedItem()
        .toString());
    int w6damage = ((Number) wDamageSpinner.getValue()).intValue();
    int constDamage = ((Number) constDamageSpinner.getValue()).intValue();
    int bf = ((Number) bfSpinner.getValue()).intValue();
    Optional<Integer> kk = null;
    if (Weapons
        .isFarRangedCategory(categoryCombo.getSelectedItem().toString())) {
      kk = new Optional<Integer>();
    }
    else {
      kk = new Optional<Integer>(((Number) kkSpinner.getValue()).intValue());
    }
    int weight = ((Number) weightSpinner.getValue()).intValue();
    if (weightCombo.getSelectedIndex() == 1) {
      weight *= 40;
    }
    int worth = ((Number) worthSpinner.getValue()).intValue();
    if (worthCombo.getSelectedIndex() == 1) {
      worth *= 10;
    }
    boolean twoHanded = twoHandedBox.isSelected();
    boolean projectile = Weapons.isFarRangedCategory(category);
    int nrOfDists = Weapon.Distance.values().length;
    int[] dists = new int[nrOfDists];
    int[] distMods = new int[nrOfDists];
    String ptt = "Keine";
    Optional<Integer> ptw = new Optional<Integer>();
    Optional<Integer> ptm = new Optional<Integer>();
    if (projectile) {
      String distString = distancesField.getText();
      StringTokenizer st = new StringTokenizer(distString, "/");
      if (st.countTokens() != nrOfDists) {
        JOptionPane.showMessageDialog(this,
            "Reichweiten eingeben als \"x/x/x/x/x\".", "Fehler",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
      for (int i = 0; i < nrOfDists; ++i) {
        try {
          dists[i] = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this,
              "Reichweiten eingeben als \"x/x/x/x/x\".", "Fehler",
              JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
      distString = distModsField.getText();
      st = new StringTokenizer(distString, "/");
      if (st.countTokens() != nrOfDists) {
        JOptionPane.showMessageDialog(this,
            "TP-Boni eingeben als \"x/x/x/x/x\".", "Fehler",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
      for (int i = 0; i < nrOfDists; ++i) {
        try {
          distMods[i] = Integer.parseInt(st.nextToken());
        }
        catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this,
              "TP-Boni eingeben als \"x/x/x/x/x\".", "Fehler",
              JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
    }
    if (Weapons.isProjectileCategory(category)) {
      ptt = projectileBox.getSelectedItem().toString();
      ptw = new Optional<Integer>(((Number)ptWorthSpinner.getValue()).intValue());
      ptm = new Optional<Integer>(((Number)ptWeightSpinner.getValue()).intValue());
    }
    if (createWeapon) {
      weapon = new Weapon(w6damage, constDamage, category, name, bf, kk, weight,
          true, twoHanded, projectile, new Optional<Integer>(worth), ptt, ptw, ptm);
    }
    else {
      weapon.setW6damage(w6damage);
      weapon.setConstDamage(constDamage);
      weapon.setType(category);
      weapon.setBF(bf);
      weapon.setKKBonus(kk);
      weapon.setWeight(weight);
      weapon.setTwoHanded(twoHanded);
      weapon.setWorth(new Optional<Integer>(worth));
      weapon.setFarRanged(projectile);
      weapon.setProjectileType(ptt);
      weapon.setProjectileWeight(ptm);
      weapon.setProjectileWorth(ptw);
    }
    if (projectile) {
      weapon.setDistanceMods(distMods);
      weapon.setDistances(dists);
    }
    return true;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(180, 360, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          weapon = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getTwoHandedBox() {
    if (twoHandedBox == null) {
      twoHandedBox = new JCheckBox();
      twoHandedBox.setBounds(new java.awt.Rectangle(10,190,181,21));
      twoHandedBox.setText("Zweihändig geführt");
    }
    return twoHandedBox;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getDistancesField() {
    if (distancesField == null) {
      distancesField = new JTextField();
      distancesField.setBounds(new Rectangle(110, 220, 181, 21));
      distancesField.setEnabled(false);
    }
    return distancesField;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getDistModsField() {
    if (distModsField == null) {
      distModsField = new JTextField();
      distModsField.setBounds(new Rectangle(110, 250, 181, 21));
      distModsField.setEnabled(false);
    }
    return distModsField;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getWorthSpinner() {
    if (worthSpinner == null) {
      worthSpinner = new JSpinner();
      worthSpinner.setBounds(new java.awt.Rectangle(111,161,50,19));
      worthSpinner.setModel(new SpinnerNumberModel(100, 0, 5000, 1));
    }
    return worthSpinner;
  }

  /**
   * This method initializes jComboBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getWorthCombo() {
    if (worthCombo == null) {
      worthCombo = new JComboBox();
      worthCombo.setBounds(new java.awt.Rectangle(170,160,121,21));
      worthCombo.addItem("Silbertaler");
      worthCombo.addItem("Dukaten");
    }
    return worthCombo;
  }

  /**
   * This method initializes projectileBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getProjectileBox() {
    if (projectileBox == null) {
      projectileBox = new JComboBox();
      projectileBox.setBounds(new Rectangle(110, 280, 179, 20));
      projectileBox.setEditable(true);
      for (String t : Weapons.getInstance().getProjectileTypes()) {
        projectileBox.addItem(t);
      }
      projectileBox.setEnabled(false);
    }
    return projectileBox;
  }

  /**
   * This method initializes ptWeightSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getPtWeightSpinner() {
    if (ptWeightSpinner == null) {
      ptWeightSpinner = new JSpinner();
      ptWeightSpinner.setModel(new SpinnerNumberModel(3, 0, 100, 1));
      ptWeightSpinner.setBounds(new Rectangle(110, 310, 46, 21));
      ptWeightSpinner.setEnabled(false);
    }
    return ptWeightSpinner;
  }

  /**
   * This method initializes ptWorthSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getPtWorthSpinner() {
    if (ptWorthSpinner == null) {
      ptWorthSpinner = new JSpinner();
      ptWorthSpinner.setModel(new SpinnerNumberModel(40, 0, 1000, 1));
      ptWorthSpinner.setBounds(new Rectangle(225, 310, 51, 21));
      ptWorthSpinner.setEnabled(false);
    }
    return ptWorthSpinner;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

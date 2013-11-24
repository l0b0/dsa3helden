/*
    Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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

import dsa.gui.lf.BGDialog;
import dsa.gui.tables.OpponentWeaponTable;
import dsa.model.DiceSpecification;
import dsa.model.data.Opponent;
import dsa.util.Optional;

import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;

public class OpponentDialog extends BGDialog implements OpponentWeaponTable.ValueChanger {

  private JPanel jContentPane = null;
  private JPanel jPanel = null;
  private JLabel jLabel = null;
  private JLabel jLabel1 = null;
  private JLabel jLabel2 = null;
  private JSpinner leSpinner = null;
  private JLabel jLabel3 = null;
  private JSpinner mrSpinner = null;
  private JLabel jLabel4 = null;
  private JSpinner rsSpinner = null;
  private JTextField nameField = null;
  private JTextField categoryField = null;
  private JPanel jPanel1 = null;
  private JPanel jPanel2 = null;
  private JButton addWeaponButton = null;
  private JButton removeWeaponButton = null;
  private JPanel jPanel3 = null;
  private JButton okButton = null;
  private JButton cancelButton = null;
  private JPanel jPanel4 = null;
  private OpponentWeaponTable weaponTable = null;
  private JLabel jLabel5 = null;
  private JSpinner atSpinner = null;
  private JLabel jLabel6 = null;
  private JSpinner paSpinner = null;
  
  private Opponent opponent;
  
  private String oldName;
  
  public interface NameChecker {
    boolean canUseName(String name);
  }
  
  private NameChecker nameChecker;

  /**
   * This method initializes 
   * 
   */
  public OpponentDialog(JFrame parent, NameChecker checker, Opponent opponent) {
  	super(parent, "Gegner bearbeiten");
  	initialize();
    if (opponent != null) {
      this.opponent = opponent.makeClone();
    }
    else {
      this.opponent = new Opponent("", "", 30, 1, 0, 
          new ArrayList<String>(), new ArrayList<Integer>(), 
          new ArrayList<DiceSpecification>(), 0, 0, 
          new ArrayList<Integer>());
    }
    setData(this.opponent);
    nameChecker = checker;
  }
  
  public OpponentDialog(JDialog parent, NameChecker checker, Opponent opponent) {
    super(parent, "Gegner bearbeiten");
    initialize();
    if (opponent != null) {
      this.opponent = opponent.makeClone();
    }
    else {
      this.opponent = new Opponent("", "", 30, 1, 0, 
          new ArrayList<String>(), new ArrayList<Integer>(), 
          new ArrayList<DiceSpecification>(), 0, 0, 
          new ArrayList<Integer>());
    }
    setData(this.opponent);
    nameChecker = checker;
  }
  
  public Opponent getNewOpponent() {
    return opponent;
  }
  
  private void setData(Opponent o) {
    nameField.setText(o.getName());
    categoryField.setText(o.getCategory());
    leSpinner.setValue(o.getLE());
    mrSpinner.setValue(o.getMR());
    rsSpinner.setValue(o.getRS());
    atSpinner.setValue(o.getNrOfAttacks());
    paSpinner.setValue(o.getNrOfParades());
    ArrayList<String> weapons = o.getWeapons();
    for (int i = 0; i < weapons.size(); ++i) {
      DiceSpecification tp = o.getTP(i);
      int at = o.getAT(i).getValue();
      Optional<Integer> pa = o.getNrOfParades() > i ? 
          new Optional<Integer>(o.getPA(i).getValue()) : new Optional<Integer>(null);
      weaponTable.addWeapon(weapons.get(i), tp, at, pa);
    }
    weaponTable.setFirstSelectedRow();
    removeWeaponButton.setEnabled(weapons.size() > 0);
    oldName = o.getName();
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setModal(true);
    this.setSize(new Dimension(350, 350));
    this.setContentPane(getJContentPane());
    this.setLocationRelativeTo(getParent());
    this.getRootPane().setDefaultButton(getOkButton());
    setEscapeButton(getCancelButton());
  }

  public String getHelpPage() {
    return "Gegner_bearbeiten";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), BorderLayout.NORTH);
      jContentPane.add(getJPanel4(), BorderLayout.CENTER);
      jContentPane.add(getJPanel3(), BorderLayout.SOUTH);
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
      jLabel6 = new JLabel();
      jLabel6.setBounds(new Rectangle(170, 130, 71, 21));
      jLabel6.setText("# Paraden:");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new Rectangle(10, 130, 71, 21));
      jLabel5.setText("# Attacken:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new Rectangle(210, 100, 31, 21));
      jLabel4.setText("RS:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new Rectangle(50, 100, 31, 21));
      jLabel3.setText("MR:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(50, 70, 31, 21));
      jLabel2.setText("LE:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 40, 71, 21));
      jLabel1.setText("Kategorie:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 71, 21));
      jLabel.setText("Name:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setPreferredSize(new Dimension(100, 160));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(jLabel2, null);
      jPanel.add(getLeSpinner(), null);
      jPanel.add(jLabel3, null);
      jPanel.add(getMrSpinner(), null);
      jPanel.add(jLabel4, null);
      jPanel.add(getRsSpinner(), null);
      jPanel.add(getNameField(), null);
      jPanel.add(getJTextField(), null);
      jPanel.add(jLabel5, null);
      jPanel.add(getJTextField2(), null);
      jPanel.add(jLabel6, null);
      jPanel.add(getJTextField3(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes leSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getLeSpinner() {
    if (leSpinner == null) {
      leSpinner = new JSpinner();
      leSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
      leSpinner.setBounds(new Rectangle(90, 70, 51, 21));
    }
    return leSpinner;
  }

  /**
   * This method initializes mrSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getMrSpinner() {
    if (mrSpinner == null) {
      mrSpinner = new JSpinner();
      mrSpinner.setModel(new SpinnerNumberModel(0, -50, 100, 1));
      mrSpinner.setBounds(new Rectangle(90, 100, 51, 21));
    }
    return mrSpinner;
  }

  /**
   * This method initializes rsSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getRsSpinner() {
    if (rsSpinner == null) {
      rsSpinner = new JSpinner();
      rsSpinner.setModel(new SpinnerNumberModel(0, 0, 50, 1));
      rsSpinner.setBounds(new Rectangle(250, 100, 51, 21));
    }
    return rsSpinner;
  }

  /**
   * This method initializes nameField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new Rectangle(90, 10, 151, 21));
    }
    return nameField;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getJTextField() {
    if (categoryField == null) {
      categoryField = new JTextField();
      categoryField.setBounds(new Rectangle(90, 40, 151, 21));
    }
    return categoryField;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jPanel1 = new JPanel();
      jPanel1.setLayout(new BorderLayout());
      jPanel1.add(getJPanel2(), BorderLayout.EAST);
      jPanel1.add(getWeaponTable().getPanelWithTable(), BorderLayout.CENTER);
    }
    return jPanel1;
  }

  /**
   * This method initializes jPanel4  
   *  
   * @return javax.swing.JPanel 
   */
  private JPanel getJPanel4() {
    if (jPanel4 == null) {
      jPanel4 = new JPanel();
      jPanel4.setLayout(new BorderLayout());
      jPanel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Waffen", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
      jPanel4.add(getJPanel1(), BorderLayout.CENTER);
    }
    return jPanel4;
  }

  /**
   * This method initializes jPanel2	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel2() {
    if (jPanel2 == null) {
      jPanel2 = new JPanel();
      jPanel2.setLayout(null);
      jPanel2.setPreferredSize(new Dimension(70, 100));
      jPanel2.add(getAddWeaponButton(), null);
      jPanel2.add(getRemoveWeaponButton(), null);
    }
    return jPanel2;
  }

  /**
   * This method initializes addWeaponButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddWeaponButton() {
    if (addWeaponButton == null) {
      addWeaponButton = new JButton();
      addWeaponButton.setBounds(new Rectangle(10, 10, 51, 21));
      addWeaponButton.setToolTipText("Waffe hinzufügen");
      addWeaponButton.setIcon(dsa.gui.util.ImageManager.getIcon("increase"));
      addWeaponButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          addWeapon();
        }
      });
    }
    return addWeaponButton;
  }
  
  private void addWeapon() {
    String name = "Waffe";
    DiceSpecification tp = DiceSpecification.parse("1W6");
    int at = 10;
    int pa = 10;
    weaponTable.addWeapon(name, tp, at, new Optional<Integer>(pa));
    removeWeaponButton.setEnabled(true);
    opponent.addWeapon(name, tp, at, pa);
  }

  /**
   * This method initializes removeWeaponButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRemoveWeaponButton() {
    if (removeWeaponButton == null) {
      removeWeaponButton = new JButton();
      removeWeaponButton.setToolTipText("Waffe entfernen");
      removeWeaponButton.setIcon(dsa.gui.util.ImageManager.getIcon("decrease_enabled"));
      removeWeaponButton.setDisabledIcon(dsa.gui.util.ImageManager.getIcon("decrease"));
      removeWeaponButton.setBounds(new Rectangle(10, 40, 51, 21));
      removeWeaponButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          removeWeapon();
        }
      });
    }
    return removeWeaponButton;
  }
  
  private void removeWeapon() {
    int index = weaponTable.getSelectedIndex();
    if (index >= 0) {
      opponent.removeWeapon(index);
      weaponTable.removeSelectedWeapon();
      removeWeaponButton.setEnabled(opponent.getWeapons().size() > 0);
    }
  }

  /**
   * This method initializes jPanel3	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel3() {
    if (jPanel3 == null) {
      jPanel3 = new JPanel();
      jPanel3.setLayout(null);
      jPanel3.setPreferredSize(new Dimension(100, 40));
      jPanel3.add(getOkButton(), null);
      jPanel3.add(getCancelButton(), null);
    }
    return jPanel3;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(40, 10, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (commitData())
            dispose();
        }
      });
    }
    return okButton;
  }
  
  private boolean commitData() {
    String name = nameField.getText();
    if (name == null || name.length() == 0) {
      JOptionPane.showMessageDialog(this, "Der Gegner muss einen Namen haben!", "Heldenverwaltung", JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    if (!name.equals(oldName) && !nameChecker.canUseName(name)) {
      return false;
    }
    int nrOfATs = ((Number)atSpinner.getValue()).intValue();
    if (nrOfATs > opponent.getWeapons().size()) {
      JOptionPane.showMessageDialog(this, "Der Gegner kann nicht mehr Attacken als Waffen haben.", "Heldenverwaltung", JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    int nrOfPAs = ((Number)paSpinner.getValue()).intValue();
    if (nrOfPAs > nrOfATs) {
      JOptionPane.showMessageDialog(this, "Der Gegner kann nicht mehr Paraden als Attacken haben.", "Heldenverwaltung", JOptionPane.PLAIN_MESSAGE);
      return false;
    }
    int le = ((Number)leSpinner.getValue()).intValue();
    int mr = ((Number)mrSpinner.getValue()).intValue();
    int rs = ((Number)rsSpinner.getValue()).intValue();
    opponent.setName(name);
    opponent.setCategory(categoryField.getText());
    opponent.setLE(le);
    opponent.setMR(mr);
    opponent.setRS(rs);
    opponent.setNrOfAttacks(nrOfATs);
    opponent.setNrOfParades(nrOfPAs);
    return true;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(190, 10, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          opponent = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }
  
  private OpponentWeaponTable getWeaponTable() {
    if (weaponTable == null) {
      weaponTable = new OpponentWeaponTable();
      weaponTable.restoreSortingState("Gegnerwaffen");
      weaponTable.setValueChanger(this);
    }
    return weaponTable;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getJTextField2() {
    if (atSpinner == null) {
      atSpinner = new JSpinner();
      atSpinner.setModel(new SpinnerNumberModel(1, 0, 10, 1));
      atSpinner.setBounds(new Rectangle(90, 130, 51, 21));
    }
    return atSpinner;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getJTextField3() {
    if (paSpinner == null) {
      paSpinner = new JSpinner();
      paSpinner.setModel(new SpinnerNumberModel(1, 0, 10, 1));
      paSpinner.setBounds(new Rectangle(250, 130, 51, 21));
    }
    return paSpinner;
  }

  public void atChanged(int item, int at) {
    opponent.setAT(item, at);
  }

  public void nameChanged(int item, String newName) {
    opponent.replaceWeapon(item, newName);
  }

  public void paChanged(int item, int pa) {
    opponent.setPA(item, pa);
  }

  public void tpChanged(int item, DiceSpecification tp) {
    opponent.setTP(item, tp);
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

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

import dsa.control.GroupOperations.HeroFileFilter;
import dsa.gui.lf.BGDialog;
import dsa.util.Directories;

import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.Rectangle;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class HeroComparisonDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JPanel selectionPanel = null;
  private JLabel jLabel = null;
  private JTextField hero1Field = null;
  private JButton hero1Button = null;
  private JLabel jLabel1 = null;
  private JTextField hero2Field = null;
  private JButton hero2Button = null;
  private JButton cancelButton = null;
  private JButton okButton = null;
  
  /**
   * This method initializes 
   * 
   */
  public HeroComparisonDialog(javax.swing.JFrame parent) {
  	super(parent);
  	initialize();
  }
  
  public String getFirstHeroFile() {
    return getHero1Field().getText();
  }
  
  public String getSecondHeroFile() {
    return getHero2Field().getText();
  }
  
  private boolean closedByOK = false;
  
  public boolean wasClosedByOK() {
    return closedByOK;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(421, 228));
    this.setContentPane(getJContentPane());
    this.setTitle("Heldenvergleich");
    this.setModal(true);
    this.setEscapeButton(getCancelButton());
    this.getRootPane().setDefaultButton(getOkButton());
    this.setLocationRelativeTo(getParent());
  }

  public String getHelpPage() {
    return "Heldenvergleich";
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
      jContentPane.add(getSelectionPanel(), null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getOkButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes selectionPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getSelectionPanel() {
    if (selectionPanel == null) {
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 70, 211, 21));
      jLabel1.setText("Zweiter Held / höhere Stufe:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 211, 21));
      jLabel.setText("Erster Held / niedrigere Stufe:");
      selectionPanel = new JPanel();
      selectionPanel.setLayout(null);
      selectionPanel.setBounds(new Rectangle(10, 10, 391, 141));
      selectionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      selectionPanel.add(jLabel, null);
      selectionPanel.add(getHero1Field(), null);
      selectionPanel.add(getHero1Button(), null);
      selectionPanel.add(jLabel1, null);
      selectionPanel.add(getHero2Field(), null);
      selectionPanel.add(getHero2Button(), null);
    }
    return selectionPanel;
  }

  /**
   * This method initializes hero1Field	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getHero1Field() {
    if (hero1Field == null) {
      hero1Field = new JTextField();
      hero1Field.setBounds(new Rectangle(10, 40, 311, 21));
    }
    return hero1Field;
  }

  /**
   * This method initializes hero1Button	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getHero1Button() {
    if (hero1Button == null) {
      hero1Button = new JButton();
      hero1Button.setBounds(new Rectangle(330, 40, 41, 21));
      hero1Button.setText("...");
      hero1Button.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectFile(hero1Field);
        }
      });
    }
    return hero1Button;
  }
  

  /**
   * This method initializes hero2Field	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getHero2Field() {
    if (hero2Field == null) {
      hero2Field = new JTextField();
      hero2Field.setBounds(new Rectangle(10, 100, 311, 21));
    }
    return hero2Field;
  }

  /**
   * This method initializes hero2Button	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getHero2Button() {
    if (hero2Button == null) {
      hero2Button = new JButton();
      hero2Button.setBounds(new Rectangle(330, 100, 41, 21));
      hero2Button.setText("...");
      hero2Button.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectFile(hero2Field);
        }
      });
    }
    return hero2Button;
  }
  
  private void selectFile(JTextField textField) {
    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
    File f = Directories.getLastUsedDirectory(this.getParent(), "Heros");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new HeroFileFilter());
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      textField.setText(chooser.getSelectedFile().getAbsolutePath());
      Directories.setLastUsedDirectory(this.getParent(), "Heros", chooser.getSelectedFile());
    }
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(300, 170, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          closedByOK = false;
          HeroComparisonDialog.this.dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(180, 170, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          okClicked();
        }
      });
    }
    return okButton;
  }
  
  private void okClicked() {
    if (getHero1Field().getText().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(this, "Bitte wähle Helden zum Vergleich aus!", "Heldenverwaltung", 
          javax.swing.JOptionPane.ERROR_MESSAGE);
      getHero1Field().requestFocus();
    }
    else if (getHero2Field().getText().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(this, "Bitte wähle Helden zum Vergleich aus!", "Heldenverwaltung", 
          javax.swing.JOptionPane.ERROR_MESSAGE);
      getHero2Field().requestFocus();      
    }
    else {
      closedByOK = true;
      dispose();
    }
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

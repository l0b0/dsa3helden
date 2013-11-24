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
package dsa.gui.dialogs.fighting;

import dsa.control.Markers;
import dsa.gui.lf.BGDialog;
import dsa.model.Fighter;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

public class AtPaModDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JLabel jLabel = null;
  private JLabel jLabel1 = null;
  private JLabel jLabel2 = null;
  private JLabel jLabel3 = null;
  private JLabel jLabel4 = null;
  private JButton cancelButton = null;
  private JButton okButton = null;
  private JTextField groundedField = null;
  private JTextField stumbledField = null;
  private JTextField paradeField = null;
  private JTextField markerField = null;
  private JSpinner modSpinner = null;
  
  private int extraMod;
  private boolean isAT;

  /**
   * This method initializes 
   * 
   */
  public AtPaModDialog(JDialog owner, Fighter fighter, int nr, int extraMod, boolean at) {
  	super(owner, true);
  	initialize();
    this.extraMod = extraMod;
    isAT = at;
    setValues(fighter, nr);
    this.setEscapeButton(cancelButton);
    this.getRootPane().setDefaultButton(okButton);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        AtPaModDialog.this.extraMod = ((Number)modSpinner.getValue()).intValue();
        dispose();
      }
    });
    this.setLocationRelativeTo(owner);
  }
  
  public int getExtraMod() {
    return extraMod;
  }
  
  private void setValues(Fighter fighter, int nr) {
    groundedField.setText(fighter.isGrounded() ? (isAT ? "+7" : "+5") : "+0");
    stumbledField.setText(fighter.hasStumbled() ? "+5" : "+0");
    if (fighter.getATBonus(nr) > 0) {
      paradeField.setText("-" + fighter.getATBonus(nr));
    }
    else {
      paradeField.setText("+" + -fighter.getATBonus(nr));
    }
    markerField.setText("+" + Markers.getMarkers(fighter));
    modSpinner.setValue(extraMod);
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
        this.setSize(new Dimension(253, 239));
        this.setTitle("Modifikatoren");
        this.setContentPane(getJContentPane());
  		
  }

  public String getHelpPage() {
    return "Kampfmodifikatoren";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel4 = new JLabel();
      jLabel4.setBounds(new Rectangle(10, 140, 101, 21));
      jLabel4.setText("Marker:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new Rectangle(10, 110, 101, 21));
      jLabel3.setText("Aus letzter Aktion:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(10, 80, 101, 21));
      jLabel2.setText("Gestolpert:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 50, 101, 21));
      jLabel1.setText("Am Boden:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 101, 21));
      jLabel.setText("Zuschlag:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(getGroundedField(), null);
      jContentPane.add(getStumbledField(), null);
      jContentPane.add(getParadeField(), null);
      jContentPane.add(getMarkerField(), null);
      jContentPane.add(getModSpinner(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(130, 180, 101, 21));
      cancelButton.setText("Abbrechen");
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
      okButton.setBounds(new Rectangle(10, 180, 101, 21));
      okButton.setText("OK");
    }
    return okButton;
  }

  /**
   * This method initializes groundedField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getGroundedField() {
    if (groundedField == null) {
      groundedField = new JTextField();
      groundedField.setBounds(new Rectangle(130, 50, 51, 21));
      groundedField.setHorizontalAlignment(JTextField.RIGHT);
      groundedField.setEditable(false);
    }
    return groundedField;
  }

  /**
   * This method initializes stumbledField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getStumbledField() {
    if (stumbledField == null) {
      stumbledField = new JTextField();
      stumbledField.setBounds(new Rectangle(130, 80, 51, 21));
      stumbledField.setHorizontalAlignment(JTextField.RIGHT);
      stumbledField.setEditable(false);
    }
    return stumbledField;
  }

  /**
   * This method initializes paradeField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getParadeField() {
    if (paradeField == null) {
      paradeField = new JTextField();
      paradeField.setBounds(new Rectangle(130, 110, 51, 21));
      paradeField.setHorizontalAlignment(JTextField.RIGHT);
      paradeField.setEditable(false);
    }
    return paradeField;
  }

  /**
   * This method initializes markerField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getMarkerField() {
    if (markerField == null) {
      markerField = new JTextField();
      markerField.setBounds(new Rectangle(130, 140, 51, 21));
      markerField.setHorizontalAlignment(JTextField.RIGHT);
      markerField.setEditable(false);
    }
    return markerField;
  }

  /**
   * This method initializes modSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getModSpinner() {
    if (modSpinner == null) {
      modSpinner = new JSpinner(new SpinnerNumberModel(0, -20, 20, 1));
      modSpinner.setBounds(new Rectangle(130, 10, 51, 21));
    }
    return modSpinner;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

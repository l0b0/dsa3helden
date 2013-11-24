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

import javax.swing.JPanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

import dsa.gui.lf.BGDialog;
import dsa.model.Date;
import java.awt.Rectangle;

public class DateSelectionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JSpinner daySpinner = null;

  private JLabel jLabel1 = null;

  private JSpinner yearSpinner = null;

  private JComboBox monthCombo = null;

  private JLabel jLabel2 = null;

  private JComboBox eraCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private Date date = null;

  private JComboBox eventCombo = null;

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    daySpinner.setValue(date.getDay());
    monthCombo.setSelectedIndex(date.getMonth().ordinal());
    yearSpinner.setValue(date.getYear());
    eraCombo.setSelectedIndex(date.getEra().ordinal());
    eventCombo.setSelectedIndex(date.getEvent().ordinal());
  }

  public void setDefaultValues() {
    daySpinner.setValue(1);
    monthCombo.setSelectedIndex(0);
    yearSpinner.setValue(1);
    eraCombo.setSelectedIndex(0);
    eventCombo.setSelectedIndex(0);
  }

  public DateSelectionDialog() {
    super();
    initialize();
  }

  public DateSelectionDialog(Frame owner) {
    super(owner);
    initialize();
  }

  public DateSelectionDialog(Frame owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public DateSelectionDialog(Frame owner, String title) {
    super(owner, title);
    initialize();
  }

  public DateSelectionDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public DateSelectionDialog(Frame owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }

  public DateSelectionDialog(Dialog owner) {
    super(owner);
    initialize();
  }

  public DateSelectionDialog(Dialog owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public DateSelectionDialog(Dialog owner, String title) {
    super(owner, title);
    initialize();
  }

  public DateSelectionDialog(Dialog owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public DateSelectionDialog(Dialog owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }
  
  public String getHelpPage() {
    return "Geburtstag";
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(360, 170);
    this.setTitle("Geburtstag");
    this.setContentPane(getJContentPane());
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
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(10, 50, 51, 21));
      jLabel2.setText("Jahr:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(140, 10, 49, 21));
      jLabel1.setText("Monat:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 51, 21));
      jLabel.setText("Tag:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new Rectangle(10, 10, 331, 81));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(getDaySpinner(), null);
      jPanel.add(jLabel1, null);
      jPanel.add(getYearSpinner(), null);
      jPanel.add(getMonthCombo(), null);
      jPanel.add(jLabel2, null);
      jPanel.add(getEraCombo(), null);
      jPanel.add(getEventCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getDaySpinner() {
    if (daySpinner == null) {
      daySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
      daySpinner.setBounds(new Rectangle(70, 10, 51, 19));
    }
    return daySpinner;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getYearSpinner() {
    if (yearSpinner == null) {
      yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
      yearSpinner.setBounds(new Rectangle(70, 50, 51, 19));
    }
    return yearSpinner;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getMonthCombo() {
    if (monthCombo == null) {
      monthCombo = new JComboBox();
      monthCombo.setBounds(new Rectangle(200, 10, 121, 19));
      for (int i = 0; i < Date.Month.values().length - 1; ++i) {
        monthCombo.addItem(Date.Month.values()[i]);
      }
    }
    return monthCombo;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getEraCombo() {
    if (eraCombo == null) {
      eraCombo = new JComboBox();
      eraCombo.setBounds(new Rectangle(130, 50, 61, 19));
      for (Date.Era era : Date.Era.values()) {
        eraCombo.addItem(era);
      }
    }
    return eraCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(50, 100, 102, 23));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          date = calcDate();
          dispose();
        }
      });
    }
    return okButton;
  }

  private Date calcDate() {
    return new Date(
        ((Number) daySpinner.getValue()).intValue(), 
        Date.Month.values()[monthCombo.getSelectedIndex()], 
        ((Number) yearSpinner.getValue()).intValue(),
        Date.Era.values()[eraCombo.getSelectedIndex()],
        Date.Event.values()[eventCombo.getSelectedIndex()]);
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(190, 100, 102, 23));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          date = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes eventCombo	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getEventCombo() {
    if (eventCombo == null) {
      eventCombo = new JComboBox();
      eventCombo.setBounds(new Rectangle(200, 50, 121, 21));
      for (Date.Event event : Date.Event.values()) {
        eventCombo.addItem(event);
      }
    }
    return eventCombo;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

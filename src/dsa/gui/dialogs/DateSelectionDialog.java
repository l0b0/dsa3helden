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

import javax.swing.JPanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

import dsa.gui.lf.BGDialog;

public class DateSelectionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JSpinner daySpinner = null;

  private JLabel jLabel1 = null;

  private JSpinner yearSpinner = null;

  private JComboBox monthCombo = null;

  private JLabel jLabel2 = null;

  private JComboBox halCombo = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private String date = null;

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    StringTokenizer t = new StringTokenizer(date, " .");
    try {
      String dayS = t.nextToken();
      int day = Integer.parseInt(dayS);
      String month = t.nextToken();
      int monthIndex = 0;
      for (int i = 0; i < monthCombo.getItemCount(); ++i) {
        if (month.equals(monthCombo.getItemAt(i))) {
          monthIndex = i;
          break;
        }
      }
      String yearS = t.nextToken();
      int year = Integer.parseInt(yearS);
      int halIndex = t.nextToken().startsWith("v") ? 1 : 0;
      daySpinner.setValue(day);
      monthCombo.setSelectedIndex(monthIndex);
      yearSpinner.setValue(year);
      halCombo.setSelectedIndex(halIndex);
    }
    catch (NumberFormatException e) {
      setDefaultValues();
    }
    catch (NoSuchElementException e) {
      setDefaultValues();
    }
  }

  public void setDefaultValues() {
    daySpinner.setValue(1);
    monthCombo.setSelectedIndex(0);
    yearSpinner.setValue(1);
    halCombo.setSelectedIndex(0);
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
    this.setSize(303, 209);
    this.setTitle("Geburtstag");
    this.setContentPane(getJContentPane());
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
      jLabel2.setBounds(new java.awt.Rectangle(14, 75, 38, 15));
      jLabel2.setText("Jahr:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(14, 47, 66, 15));
      jLabel1.setText("Monat:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(14, 18, 57, 15));
      jLabel.setText("Tag:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(15, 16, 265, 107));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(getDaySpinner(), null);
      jPanel.add(jLabel1, null);
      jPanel.add(getYearSpinner(), null);
      jPanel.add(getMonthCombo(), null);
      jPanel.add(jLabel2, null);
      jPanel.add(getHalCombo(), null);
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
      daySpinner.setBounds(new java.awt.Rectangle(86, 16, 58, 19));
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
      yearSpinner.setBounds(new java.awt.Rectangle(86, 73, 58, 19));
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
      monthCombo.setBounds(new java.awt.Rectangle(86, 45, 165, 19));
      String[] months = { "Praios", "Rondra", "Efferd", "Travia", "Boron",
          "Hesinde", "Firun", "Tsa", "Phex", "Peraine", "Ingerimm", "Rhaja" };
      for (String m : months)
        monthCombo.addItem(m);
    }
    return monthCombo;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getHalCombo() {
    if (halCombo == null) {
      halCombo = new JComboBox();
      halCombo.setBounds(new java.awt.Rectangle(160, 73, 91, 19));
      halCombo.addItem("Hal");
      halCombo.addItem("vor Hal");
    }
    return halCombo;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(29, 143, 102, 23));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          date = getDateString();
          dispose();
        }
      });
    }
    return okButton;
  }

  private String getDateString() {
    return "" + ((Number) daySpinner.getValue()).intValue() + "." + " "
        + monthCombo.getSelectedItem() + " "
        + ((Number) yearSpinner.getValue()).intValue() + " "
        + halCombo.getSelectedItem();
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(160, 143, 102, 23));
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

} // @jve:decl-index=0:visual-constraint="10,10"

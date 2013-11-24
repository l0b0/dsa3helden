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

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import javax.swing.border.BevelBorder;

import dsa.gui.lf.BGDialog;

public class RedistributionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JButton okButton = null;

  private JTextArea descLabel = null;

  private JLabel questionLabel = null;

  private JSpinner valueSpinner = null;

  public void setDescription(String description) {
    descLabel.setText(description);
  }

  public void setQuestion(String question) {
    questionLabel.setText(question);
    java.awt.Dimension size = questionLabel.getPreferredSize();
    if (size.width > 230) size.setSize(230, size.height);
    questionLabel.setBounds(6, questionLabel.getY(), size.width, size.height);
    valueSpinner.setBounds(6 + size.width + 9, valueSpinner.getY(),
        valueSpinner.getWidth(), valueSpinner.getHeight());
  }

  private final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 10, 1);

  public void setMinimum(int minimum) {
    model.setMinimum(minimum);
  }

  public void setMaximum(int maximum) {
    model.setMaximum(maximum);
  }

  public void setDefault(int value) {
    model.setValue(value);
    theValue = value;
  }

  private int theValue = 0;

  public int getValue() {
    return theValue;
  }

  /**
   * This is the default constructor
   */
  public RedistributionDialog() {
    super();
    initialize();
  }

  public RedistributionDialog(javax.swing.JFrame parent) {
    super(parent);
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(326, 212);
    this.setContentPane(getJContentPane());
    this.setModal(true);
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
      questionLabel = new JLabel();
      questionLabel.setBounds(new java.awt.Rectangle(63, 81, 100, 22));
      questionLabel.setText("JLabel");
      descLabel = new JTextArea();
      descLabel.setText("JLabel");
      descLabel.setBounds(new java.awt.Rectangle(6, 5, 290, 68));
      descLabel.setEditable(false);
      jPanel = new JPanel();
      jPanel.setBounds(new java.awt.Rectangle(6, 7, 300, 119));
      jPanel.setLayout(null);
      jPanel.add(questionLabel, null);
      jPanel.add(getValueSpinner(), null);
      jPanel.add(descLabel, null);
      jPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }
    return jPanel;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(123, 140, 72, 24));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          theValue = model.getNumber().intValue();
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getValueSpinner() {
    if (valueSpinner == null) {
      valueSpinner = new JSpinner();
      valueSpinner.setBounds(new java.awt.Rectangle(181, 81, 51, 22));
      valueSpinner.setModel(model);
    }
    return valueSpinner;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

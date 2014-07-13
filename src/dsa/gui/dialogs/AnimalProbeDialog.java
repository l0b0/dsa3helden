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

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dsa.control.Dice;
import dsa.gui.lf.BGDialog;
import dsa.model.data.Animal;
import dsa.util.Strings;

/**
 * 
 */
public final class AnimalProbeDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JButton probeButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel = null;

  private JSpinner difficultySpinner = null;

  private JLabel jLabel = null;

  private Animal animal;

  private int property;

  private JLabel jLabel2 = null;

  public AnimalProbeDialog(Frame owner, Animal animal, int property) {
    super(owner, true);
    this.animal = animal;
    this.property = property;
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Probe");
    this.setSize(289, 168);
    this.setContentPane(getJContentPane());
    getDifficultySpinner().requestFocus();
    ((JSpinner.DefaultEditor) getDifficultySpinner().getEditor()).getTextField().select(
        0, 1);
    setProbability();
    this.getRootPane().setDefaultButton(getProbeButton());
    setEscapeButton(getCancelButton());
    this.getDifficultySpinner().requestFocusInWindow();
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) this.getDifficultySpinner().getEditor();
    editor.getTextField().setText("0");
    editor.getTextField().selectAll();
  }

  /**
   * 
   * 
   */
  private void setProbability() {
    int difficulty = ((Integer) getDifficultySpinner().getModel().getValue()).intValue();
    int value = ((Number) animal.getAttributeValue(property)).intValue()
        - difficulty;
    double successPercent = 0.0;
    if (value <= 1)
      successPercent = 5.0;
    else if (value >= 20)
      successPercent = 95.0;
    else
      successPercent = value * 5.0;
    String formatted = new java.text.DecimalFormat("#0.00#")
        .format(successPercent);
    jLabel2.setText("Erfolgswahrscheinlichkeit: " + formatted + "%");
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(getProbeButton(), null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getJPanel(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getProbeButton() {
    if (probeButton == null) {
      probeButton = new JButton();
      probeButton.setName("");
      probeButton.setText("Probe!");
      probeButton.setMnemonic(java.awt.event.KeyEvent.VK_P);
      probeButton.setPreferredSize(new java.awt.Dimension(90, 25));
      probeButton.setLocation(30, 100);
      probeButton.setSize(90, 25);
      probeButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          doProbe();
        }
      });
    }
    return probeButton;
  }

  protected void doProbe() {
    int difficulty = ((Integer) getDifficultySpinner().getModel().getValue()).intValue();
    java.awt.Container parent = getParent();
    dispose();
    String result = "";
    result = doProbe(animal, property, difficulty);
    JOptionPane
        .showMessageDialog(parent, result, "Probe für "
            + Strings.cutTo(animal.getName(), ' '),
            JOptionPane.INFORMATION_MESSAGE);
  }

  private static String doProbe(Animal animal, int property, int mod) {
    int hurdle = ((Number) animal.getAttributeValue(property)).intValue() - mod;
    int diceThrow = Dice.roll(20);
    if (diceThrow == 1) {
      return "Perfekt bestanden!";
    }
    else if (diceThrow == 20) {
      return "Patzer!";
    }
    else if (diceThrow <= hurdle) {
      return "Mit einer " + diceThrow + " bestanden.";
    }
    else
      return "Mit einer " + diceThrow + " nicht bestanden.";
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setPreferredSize(new java.awt.Dimension(90, 25));
      cancelButton.setText("Abbruch");
      cancelButton.setMnemonic(java.awt.event.KeyEvent.VK_A);
      cancelButton.setLocation(145, 100);
      cancelButton.setSize(90, 25);
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          AnimalProbeDialog.this.dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jLabel2 = new JLabel();
      jPanel = new JPanel();
      jLabel = new JLabel();
      jPanel.setLayout(null);
      jPanel.setBounds(17, 14, 247, 74);
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jLabel.setBounds(12, 12, 62, 19);
      jLabel.setText("Zuschlag:");
      // jLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
      jPanel.add(getDifficultySpinner(), null);
      jPanel.add(jLabel, null);
      jLabel.setLabelFor(getDifficultySpinner());
      jLabel2.setBounds(12, 42, 224, 20);
      jLabel2.setText("Erfolgswahrscheinlichkeit");
      jLabel2.setForeground(java.awt.Color.RED);
      jPanel.add(jLabel2, null);
    }
    return jPanel;
  }

  /**
   * This method initializes jSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getDifficultySpinner() {
    if (difficultySpinner == null) {
      difficultySpinner = new JSpinner();
      difficultySpinner.setSize(43, 20);
      difficultySpinner.setLocation(81, 12);
      difficultySpinner.setModel(new SpinnerNumberModel(0, -50, 80, 1));
      difficultySpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          setProbability();
        }
      });
    }
    return difficultySpinner;
  }

  public String getHelpPage() {
    return "Probe";
  }
} //  @jve:decl-index=0:visual-constraint="10,10"

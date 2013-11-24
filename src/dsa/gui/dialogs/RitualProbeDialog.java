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

import java.awt.Frame;

import javax.swing.JDialog;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dsa.control.Dice;
import dsa.control.Markers;
import dsa.control.Probe;
import dsa.gui.lf.BGDialog;
import dsa.model.characters.Hero;
import dsa.model.data.Ritual;
import dsa.util.Strings;

/**
 * 
 */
public final class RitualProbeDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JButton jButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel = null;

  private JSpinner difficultySpinner = null;

  private JLabel jLabel = null;

  private Hero hero;

  private Ritual.TestData testData;

  private JLabel jLabel2 = null;

  /**
   * This is the default constructor
   */
  public RitualProbeDialog(Frame owner, Hero hero, Ritual.TestData testData) {
    super(owner, true);
    this.hero = hero;
    this.testData = testData;
    initialize();
  }

  public RitualProbeDialog(JDialog owner, Hero hero, Ritual.TestData testData) {
    super(owner, true);
    this.hero = hero;
    this.testData = testData;
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Probe");
    this.setSize(289, 238);
    this.setContentPane(getJContentPane());

    String text = "Probe geht auf: " + testData.getP1() + "/" + testData.getP2() + "/"
        + testData.getP3();
    propertyLabel.setText(text);
    defaultAddLabel.setText("Normaler Zuschlag für Probe: "
        + testData.getDefaultModifier());
    int mod = testData.getDefaultModifier();
    if (testData.hasHalfStepLess()) {
      halfStepLabel.setText("Reduzierung um halbe Stufe: -"
          + (hero.getStep() / 2));
      mod -= hero.getStep() / 2;
    }
    else {
      halfStepLabel.setText("Reduzierung um halbe Stufe: nein");
    }
    getDifficultySpinner().setValue(Integer.valueOf(mod));

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
    Probe probe = new Probe();
    probe.setFirstProperty(hero.getCurrentProperty(testData.getP1()));
    probe.setSecondProperty(hero.getCurrentProperty(testData.getP2()));
    probe.setThirdProperty(hero.getCurrentProperty(testData.getP3()));
    probe.setSkill(0);
    probe.setModifier(difficulty + Markers.getMarkers(hero));
    int successCount = 0;
    for (int i = 1; i <= 20; i++)
      for (int j = 1; j <= 20; j++)
        for (int k = 1; k <= 20; k++) {
          if (probe.performTest(i, j, k)) successCount++;
        }
    double successPercent = (((double) successCount) / 8000.0) * 100.0;
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
    if (jButton == null) {
      jButton = new JButton();
      jButton.setName("");
      jButton.setText("Probe!");
      jButton.setMnemonic(java.awt.event.KeyEvent.VK_P);
      jButton.setPreferredSize(new java.awt.Dimension(90, 25));
      jButton.setLocation(29, 175);
      jButton.setSize(90, 25);
      jButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          doProbe();
        }
      });
    }
    return jButton;
  }

  protected void doProbe() {
    int difficulty = ((Integer) getDifficultySpinner().getModel().getValue()).intValue();
    java.awt.Container parent = getParent();
    dispose();
    String ret = "";
    ret = doProbe(hero, testData, difficulty);
    JOptionPane.showMessageDialog(parent, ret, "Ritual-Probe für "
        + Strings.cutTo(hero.getName(), ' '), JOptionPane.INFORMATION_MESSAGE);
  }

  public enum Result {
    Success, Failure, Canceled
  };

  private Result result = Result.Canceled;

  private JLabel defaultAddLabel = null;

  private JLabel halfStepLabel = null;

  private JLabel propertyLabel = null;

  public Result getResult() {
    return result;
  }

  private String doProbe(Hero character, Ritual.TestData test, int mod) {
    Probe probe = new Probe();
    probe.setFirstProperty(character.getCurrentProperty(test.getP1()));
    probe.setSecondProperty(character.getCurrentProperty(test.getP2()));
    probe.setThirdProperty(character.getCurrentProperty(test.getP3()));
    probe.setSkill(0);
    probe.setModifier(mod + Markers.getMarkers(character));
    int throw1 = Dice.roll(20);
    int throw2 = Dice.roll(20);
    int throw3 = Dice.roll(20);
    int ret = probe.performDetailedTest(throw1, throw2, throw3);
    String s = "\n (Wurf: " + throw1 + ", " + throw2 + ", " + throw3 + ")";
    if (ret == Probe.DAEMONENPECH) {
      this.result = Result.Failure;
      return "DÄMONISCHES PECH! 3x die 20!";
    }
    else if (ret == Probe.PATZER) {
      this.result = Result.Failure;
      return "Patzer (2 20er)!" + s;
    }
    else if (ret == Probe.PERFEKT) {
      this.result = Result.Success;
      return "Perfekt (2 1er)!" + s;
    }
    else if (ret == Probe.GOETTERGLUECK) {
      this.result = Result.Success;
      return "GÖTTLICHE GUNST! 3x die 1!";
    }
    else if (ret == Probe.FEHLSCHLAG) {
      this.result = Result.Failure;
      return "Nicht gelungen." + s;
    }
    else {
      this.result = Result.Success;
      return "Gelungen; " + ret + (ret == 1 ? " Punkt" : " Punkte")
          + " übrig." + s;
    }
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
      cancelButton.setLocation(144, 175);
      cancelButton.setSize(90, 25);
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          result = Result.Canceled;
          RitualProbeDialog.this.dispose();
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
      propertyLabel = new JLabel();
      propertyLabel.setBounds(new java.awt.Rectangle(12, 44, 222, 19));
      propertyLabel.setText("Probe geht auf:");
      halfStepLabel = new JLabel();
      halfStepLabel.setBounds(new java.awt.Rectangle(11, 125, 226, 18));
      halfStepLabel.setText("Reduzierung um halbe Stufe: ");
      defaultAddLabel = new JLabel();
      defaultAddLabel.setBounds(new java.awt.Rectangle(12, 97, 224, 19));
      defaultAddLabel.setText("Normaler Zuschlag für Probe: ");
      jLabel2 = new JLabel();
      jPanel = new JPanel();
      jLabel = new JLabel();
      jPanel.setLayout(null);
      jPanel.setBounds(17, 14, 247, 152);
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jLabel.setBounds(12, 12, 62, 19);
      jLabel.setText("Zuschlag:");
      // jLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
      jPanel.add(getDifficultySpinner(), null);
      jPanel.add(jLabel, null);
      jLabel.setLabelFor(getDifficultySpinner());
      jLabel2.setBounds(11, 69, 224, 20);
      jLabel2.setText("Erfolgswahrscheinlichkeit");
      jLabel2.setForeground(java.awt.Color.RED);
      jPanel.add(jLabel2, null);
      jPanel.add(defaultAddLabel, null);
      jPanel.add(halfStepLabel, null);
      jPanel.add(propertyLabel, null);
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

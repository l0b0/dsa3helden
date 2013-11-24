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

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dsa.control.Dice;
import dsa.control.Markers;
import dsa.control.Probe;
import dsa.gui.lf.BGDialog;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Talents;
import dsa.model.talents.NormalTalent;
import dsa.util.Strings;

/**
 * 
 */
public final class ProbeDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JButton probeButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel = null;

  private JSpinner difficultySpinner = null;

  private JLabel jLabel = null;

  private JCheckBox jCheckBox = null;

  private JLabel jLabel1 = null;

  private Hero hero;

  private String talentName;

  private Property property;

  private boolean isTalentProbe;

  private boolean isKOProbe;

  private JLabel jLabel2 = null;

  /**
   * This is the default constructor
   */
  public ProbeDialog(Frame owner, Hero hero, String talent) {
    super(owner, true);
    this.hero = hero;
    talentName = talent;
    isTalentProbe = true;
    isKOProbe = false;
    initialize();
  }

  public ProbeDialog(Frame owner, Hero hero, Property property) {
    super(owner, true);
    this.hero = hero;
    this.property = property;
    isTalentProbe = false;
    isKOProbe = false;
    initialize();
  }

  public ProbeDialog(Frame owner, Hero hero) {
    super(owner, true);
    this.hero = hero;
    isTalentProbe = false;
    isKOProbe = true;
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Probe");
    this.setSize(289, 189);
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
    if (isTalentProbe) {
      NormalTalent talent = (NormalTalent) Talents.getInstance().getTalent(
          talentName);
      Probe probe = new Probe();
      probe
          .setFirstProperty(hero.getCurrentProperty(talent.getFirstProperty()));
      probe.setSecondProperty(hero.getCurrentProperty(talent
          .getSecondProperty()));
      probe
          .setThirdProperty(hero.getCurrentProperty(talent.getThirdProperty()));
      probe.setSkill(hero.getCurrentTalentValue(talentName));
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
    else {
      int value = 0;
      difficulty += Markers.getMarkers(hero);
      if (!isKOProbe)
        value = hero.getCurrentProperty(property) - difficulty;
      else
        value = hero.getCurrentEnergy(dsa.model.characters.Energy.KO)
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
      probeButton.setLocation(33, 122);
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
    boolean all = getJCheckBox().isSelected();
    java.awt.Container parent = getParent();
    dispose();
    String result = "";
    if (all) {
      for (Hero aHero : Group.getInstance().getAllCharacters()) {
        result += Strings.cutTo(aHero.getName(), ' ') + ": ";
        if (isTalentProbe) {
          result += doProbe(aHero, talentName, difficulty) + "\n";
        }
        else if (isKOProbe) {
          result += doProbe(aHero, difficulty) + "\n";
        }
        else {
          result += doProbe(aHero, property, difficulty) + "\n";
        }
      }
    }
    else if (isTalentProbe) {
      result = doProbe(hero, talentName, difficulty);
    }
    else if (isKOProbe) {
      result = doProbe(hero, difficulty);
    }
    else
      result = doProbe(hero, property, difficulty);
    JOptionPane.showMessageDialog(parent, result, all ? "Proben" : "Probe für "
        + Strings.cutTo(hero.getName(), ' '), JOptionPane.INFORMATION_MESSAGE);
  }

  private static String doProbe(Hero character, String talentName, int mod) {
    NormalTalent talent = (NormalTalent) Talents.getInstance().getTalent(
        talentName);
    Probe probe = new Probe();
    probe.setFirstProperty(character.getCurrentProperty(talent
        .getFirstProperty()));
    probe.setSecondProperty(character.getCurrentProperty(talent
        .getSecondProperty()));
    probe.setThirdProperty(character.getCurrentProperty(talent
        .getThirdProperty()));
    probe.setSkill(character.getCurrentTalentValue(talentName));
    mod += Markers.getMarkers(character);
    probe.setModifier(mod);
    int throw1 = Dice.roll(20);
    int throw2 = Dice.roll(20);
    int throw3 = Dice.roll(20);
    int result = probe.performDetailedTest(throw1, throw2, throw3);
    String s = "\n (Wurf: " + throw1 + ", " + throw2 + ", " + throw3 + ")";
    if (result == Probe.DAEMONENPECH) {
      return "DÄMONISCHES PECH! 3x die 20!";
    }
    else if (result == Probe.PATZER) {
      return "Patzer (2 20er)!" + s;
    }
    else if (result == Probe.PERFEKT) {
      return "Perfekt (2 1er)!" + s;
    }
    else if (result == Probe.GOETTERGLUECK) {
      return "GÖTTLICHE GUNST! 3x die 1!";
    }
    else if (result == Probe.FEHLSCHLAG) {
      return "Nicht gelungen." + s;
    }
    else {
      return "Gelungen; " + result + (result == 1 ? " Punkt" : " Punkte")
          + " übrig." + s;
    }
  }

  private static String doProbe(Hero character, Property property, int mod) {
    mod += Markers.getMarkers(character);
    int hurdle = character.getCurrentProperty(property) - mod;
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

  private static String doProbe(Hero character, int mod) {
    mod += Markers.getMarkers(character);
    int hurdle = character.getCurrentEnergy(dsa.model.characters.Energy.KO)
        - mod;
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
      cancelButton.setLocation(148, 122);
      cancelButton.setSize(90, 25);
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          ProbeDialog.this.dispose();
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
      jLabel1 = new JLabel();
      jPanel.setLayout(null);
      jPanel.setBounds(17, 14, 247, 96);
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jLabel.setBounds(12, 12, 62, 19);
      jLabel.setText("Zuschlag:");
      // jLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
      jLabel1.setBounds(32, 69, 149, 19);
      jLabel1.setText("Probe aller Helden");
      jPanel.add(getDifficultySpinner(), null);
      jPanel.add(jLabel, null);
      jPanel.add(getJCheckBox(), null);
      jPanel.add(jLabel1, null);
      jLabel.setLabelFor(getDifficultySpinner());
      jLabel1.setLabelFor(getJCheckBox());
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

  /**
   * This method initializes jCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getJCheckBox() {
    if (jCheckBox == null) {
      jCheckBox = new JCheckBox();
      jCheckBox.setSize(23, 17);
      jCheckBox.setLocation(9, 69);
    }
    return jCheckBox;
  }

  public String getHelpPage() {
    return "Probe";
  }
} //  @jve:decl-index=0:visual-constraint="10,10"

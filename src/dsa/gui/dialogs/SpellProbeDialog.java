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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Spell;
import dsa.model.talents.Spell.Cost;
import dsa.util.Strings;

/**
 * 
 */
public final class SpellProbeDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JButton probeButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel = null;

  private JSpinner difficultySpinner = null;

  private JLabel jLabel = null;

  private Hero hero;

  private String talentName;

  private JLabel jLabel2 = null;

  /**
   * This is the default constructor
   */
  public SpellProbeDialog(Frame owner, Hero hero, String talent) {
    super(owner, true);
    this.hero = hero;
    talentName = talent;
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Probe");
    this.setContentPane(getJContentPane());
    pack();
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
    jLabel2.setForeground(successPercent >= 50.0 ? Color.GREEN : Color.RED);
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), BorderLayout.NORTH);
      jContentPane.add(getButtonPane(), BorderLayout.SOUTH);
      jContentPane.add(getASPPane(), BorderLayout.CENTER);
    }
    return jContentPane;
  }
  
  private JPanel costPane;
  private ArrayList<JSpinner> spinners = new ArrayList<JSpinner>();
  private JComboBox variantCombo;
  private JCheckBox checkBox;
  private JLabel costLabel;
  
  private JPanel getASPPane() {
    JPanel aspPane = new JPanel();
    aspPane.setLayout(new BorderLayout());
    Spell spell = (Spell) Talents.getInstance().getTalent(talentName);
    if (spell.getNrOfVariants() > 1) {
      JPanel variantPanel = new JPanel();
      variantPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      variantPanel.add(new JLabel("Variante: "));
      variantCombo = new JComboBox();
      for (int i = 0; i < spell.getNrOfVariants(); ++i) {
        variantCombo.addItem(spell.getVariantName(i));
      }
      variantCombo.setSelectedIndex(0);
      variantCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fillCostPane(variantCombo.getSelectedIndex()); 
          costPane.invalidate();
          SpellProbeDialog.this.pack();
        }
      });
      variantPanel.add(variantCombo);
      aspPane.add(variantPanel, BorderLayout.NORTH);
    }
    checkBox = new JCheckBox("4. Stabzauber benutzen");
    checkBox.setEnabled(false);
    if (hero.getInternalType().startsWith("Magier")) {
      List<String> rituals = hero.getRituals();
      for (int i = 0; i < rituals.size(); ++i) {
        if (rituals.get(i).equals("4. Stabzauber")) {
          checkBox.setEnabled(true);
          checkBox.setSelected(true);
          checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              calcCosts();
            }
          });
        }
      }
    }
    JPanel lowerPane = new JPanel(new GridLayout(2, 1));
    JPanel cbWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    cbWrapper.add(checkBox);
    lowerPane.add(cbWrapper);
    JPanel costWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    costLabel = new JLabel();
    costWrapper.add(costLabel);
    lowerPane.add(costWrapper);
    costPane = new JPanel();
    fillCostPane(0);
    aspPane.add(costPane, BorderLayout.CENTER);
    aspPane.add(lowerPane, BorderLayout.SOUTH);
    aspPane.setBorder(javax.swing.BorderFactory
        .createTitledBorder(javax.swing.BorderFactory
        .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), "Kosten"));
    return aspPane;
  }
  
  int aeSum = 0;
  int leSum = 0;
  int permanentSum = 0;

  private void calcCosts() {
    aeSum = 0;
    leSum = 0;
    permanentSum = 0;
    int variant = 0;
    Spell spell = (Spell) Talents.getInstance().getTalent(talentName);
    if (spell.getNrOfVariants() > 1) variant = variantCombo.getSelectedIndex();
    List<Cost> costs = spell.getCosts(variant);
    int spinnerIndex = 0;
    for (int i = 0; i < costs.size(); ++i) {
      Cost cost = costs.get(i);
      if (cost.getCostType() == Spell.CostType.Custom) {
        aeSum += getSpinnerValue(spinnerIndex); ++spinnerIndex;
      }
      else if (cost.getCostType() == Spell.CostType.Fixed) {
        aeSum += cost.getCost().calcValue();
      }
      else if (cost.getCostType() == Spell.CostType.LP) {
        leSum += cost.getCost().calcValue();
      }
      else if (cost.getCostType() == Spell.CostType.Multiplied) {
        aeSum += getSpinnerValue(spinnerIndex) * cost.getCost().calcValue(); ++spinnerIndex;
      }
      else if (cost.getCostType() == Spell.CostType.Permanent) {
        permanentSum += cost.getCost().calcValue();
      }
      else if (cost.getCostType() == Spell.CostType.Quadratic) {
        int value = getSpinnerValue(spinnerIndex); ++spinnerIndex;
        aeSum += value * value;
      }
      else if (cost.getCostType() == Spell.CostType.Special) {
        // nothing
      }
      else if (cost.getCostType() == Spell.CostType.Step) {
        aeSum += hero.getStep();
      }
      else if (cost.getCostType() == Spell.CostType.Variable) {
        int value = getSpinnerValue(spinnerIndex); ++spinnerIndex;
        for (int j = 0; j < value; ++j) {
          aeSum += cost.getCost().calcValue();
        }
      }
    }
    if (checkBox.isSelected()) aeSum -= 2;
    if (aeSum <= 0) aeSum = 1;
    String text = "Gesamtkosten: " + aeSum + " ASP";
    if (permanentSum > 0) {
      text = text + " und " + permanentSum + " permanente ASP";
    }
    if (leSum > 0) {
      text = text + " und " + leSum + " LP";
    }
    text = text + ".";
    costLabel.setText(text);
    boolean ok = true;
    if (aeSum + permanentSum > hero.getCurrentEnergy(Energy.AE)) ok = false;
    if (leSum > hero.getCurrentEnergy(Energy.LE)) ok = false;
    if (permanentSum > hero.getDefaultEnergy(Energy.AE)) ok = false;
    costLabel.setForeground(ok ? Color.GREEN : Color.RED);
    probeButton.setEnabled(ok);
  }
  
  private int getSpinnerValue(int index) {
    return ((SpinnerNumberModel)spinners.get(index).getModel()).getNumber().intValue();
  }
  
  private void fillCostPane(int variant) {
    costPane.removeAll();
    spinners.clear();
    Spell spell = (Spell) Talents.getInstance().getTalent(talentName);
    int nrOfRows = 0;
    List<Cost> costs = spell.getCosts(variant); 
    for (int i = 0; i < costs.size(); ++i) {
      Spell.CostType ct = costs.get(i).getCostType();
      if (ct == Spell.CostType.Custom || ct == Spell.CostType.Multiplied 
          || ct == Spell.CostType.Quadratic || ct == Spell.CostType.Variable) {
        nrOfRows += 2;
      }
      else nrOfRows += 1;
    }
    costPane.setLayout(new GridLayout(nrOfRows, 1));
    for (int i = 0; i < costs.size(); ++i) {
      Spell.CostType ct = costs.get(i).getCostType();
      if (ct == Spell.CostType.Fixed) {
        addText("Der Zauber hat feste Kosten von " + costs.get(i).getCost() + " ASP.");
      }
      else if (ct == Spell.CostType.Permanent) {
        addText("Der Zauber hat permanente Kosten von " + costs.get(i).getCost() + " ASP.");
      }
      else if (ct == Spell.CostType.LP) {
        addText("Der Zauber hat feste Kosten von " + costs.get(i).getCost() + " LP.");
      }
      else if (ct == Spell.CostType.Special) {
        addText("Der Zauber hat spezielle Kosten. Bitte manuell die Energien anpassen.");
      }
      else if (ct == Spell.CostType.Step) {
        addText("Der Zauber hat feste Kosten von " + hero.getStep() + " (Stufe) ASP.");
      }
      else if (ct == Spell.CostType.Custom) {
        addText("Der Zauber hat frei bestimmbare Kosten.");
        addVariablePane("Bitte Kosten angeben: ", costs.get(i).getCost().calcValue());
      }
      else if (ct == Spell.CostType.Multiplied) {
        addText("Der Zauber kostet " + costs.get(i).getCost() + " mal " + costs.get(i).getText() + " ASP.");
        addVariablePane(costs.get(i).getText() + ": ", 1);
      }
      else if (ct == Spell.CostType.Quadratic) {
        addText("Der Zauber kostet " + costs.get(i).getText() + " zum Quadrat ASP.");
        addVariablePane(costs.get(i).getText() + ": ", 1);        
      }
      else if (ct == Spell.CostType.Variable) {
        addText("Der Zauber kostet " + costs.get(i).getCost() + " ASP pro " + costs.get(i).getText() + ".");
        addVariablePane(costs.get(i).getText() + ": ", 0);
      }
    }
    calcCosts();
  }
  
  private void addText(String text) {
    JPanel pane = new JPanel();
    pane.setLayout(new FlowLayout(FlowLayout.LEFT));
    JLabel l = new JLabel();
    l.setText(text);
    pane.add(l);
    costPane.add(pane);
  }
  
  private void addVariablePane(String text, int minimum) {
    JPanel pane = new JPanel();
    pane.setLayout(new FlowLayout(FlowLayout.LEFT));
    JLabel l = new JLabel();
    l.setText(text);
    pane.add(l);
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(minimum == 0 ? 1 : minimum, minimum, 150, 1));
    spinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        calcCosts();
      }
    });
    spinners.add(spinner);
    pane.add(spinner);
    costPane.add(pane);
  }
  
  private JPanel getButtonPane() {
    JPanel buttonPane = new JPanel();
    buttonPane.setPreferredSize(new java.awt.Dimension(250, 55));
    buttonPane.setLayout(null);
    buttonPane.add(getProbeButton(), null);
    buttonPane.add(getCancelButton(), null);
    return buttonPane;
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
      probeButton.setLocation(33, 15);
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
    result = doProbe(hero, talentName, difficulty);
    ProbeResultDialog.showDialog(parent, result, "Probe für "
        + Strings.cutTo(hero.getName(), ' '));
  }

  private String doProbe(Hero character, String talentName, int mod) {
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
      removeCosts(false);
      return "DÄMONISCHES PECH! 3x die 20!";
    }
    else if (result == Probe.PATZER) {
      removeCosts(false);
      return "Patzer (2 20er)!" + s;
    }
    else if (result == Probe.PERFEKT) {
      removeCosts(true);
      return "Perfekt (2 1er)!" + s;
    }
    else if (result == Probe.GOETTERGLUECK) {
      removeCosts(true);
      return "GÖTTLICHE GUNST! 3x die 1!";
    }
    else if (result == Probe.FEHLSCHLAG) {
      removeCosts(false);
      return "Nicht gelungen." + s;
    }
    else {
      removeCosts(true);
      return "Gelungen; " + result + (result == 1 ? " Punkt" : " Punkte")
          + " übrig." + s;
    }
  }
  
  private void removeCosts(boolean success) {
    int aeCosts = success ? aeSum : (int) Math.floor(((double)aeSum) / 2.0);
    int leCosts = success ? leSum : (int) Math.floor(((double)leSum) / 2.0);
    if (aeCosts < 1) aeCosts = 1;
    int permanentCosts = success ? permanentSum : 0;
    hero.changeCurrentEnergy(Energy.AE, -aeCosts);
    if (leCosts > 0) hero.changeCurrentEnergy(Energy.LE, -leCosts);
    if (permanentCosts > 0) hero.changeDefaultEnergy(Energy.AE, -permanentCosts); 
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
      cancelButton.setLocation(148, 15);
      cancelButton.setSize(90, 25);
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          SpellProbeDialog.this.dispose();
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
      jPanel.setPreferredSize(new java.awt.Dimension(247, 70));
      jPanel.setBorder(javax.swing.BorderFactory
          .createTitledBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED), "Schwierigkeit"));
      jLabel.setBounds(12, 15, 62, 25);
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
      difficultySpinner.setLocation(81, 18);
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

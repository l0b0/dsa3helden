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
package dsa.gui.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import java.awt.Rectangle;
import javax.swing.JTextField;

public final class TypeMetaFrame extends SubFrame implements CharactersObserver {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JCheckBox aeBox = null;

  private JCheckBox keBox = null;

  private JPanel jPanel1 = null;

  private JLabel magicTypeLabel = null;

  private JComboBox magicTypeCombo = null;

  private JLabel jLabel3 = null;

  private JLabel spellIncreaseLabel = null;

  private JLabel incrMovesLabel = null;

  private JLabel fixedLELabel = null;

  private JLabel fixedAELabel = null;

  private JSpinner mrBonusSpinner = null;

  private JSpinner beBonusSpinner = null;

  private JSpinner talentIncreaseSpinner = null;

  private JSpinner spellIncreaseSpinner = null;

  private JSpinner increaseMovesSpinner = null;

  private JSpinner leIncreaseSpinner = null;

  private JSpinner aeIncreaseSpinner = null;

  public TypeMetaFrame() {
    super("Metadaten");
    this.setContentPane(getJContentPane());
    currentHero = dsa.model.characters.Group.getInstance().getActiveHero();
    dsa.model.characters.Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(TypeMetaFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(TypeMetaFrame.this);
          done = true;
        }
      }
    });
    updateData();
  }
  
  public String getHelpPage() {
    return "Metadaten";
  }

  private Hero currentHero;

  private boolean listenForChanges = true;

  private JLabel jLabel2 = null;

  private JTextField typeField = null;

  protected void updateData() {
    currentHero = Group.getInstance().getActiveHero();
    listenForChanges = false;
    mrBonusSpinner.setEnabled(currentHero != null);
    beBonusSpinner.setEnabled(currentHero != null);
    aeBox.setEnabled(currentHero != null);
    keBox.setEnabled(currentHero != null);
    magicTypeCombo.setEnabled(currentHero != null);
    talentIncreaseSpinner.setEnabled(currentHero != null);
    spellIncreaseSpinner.setEnabled(currentHero != null);
    increaseMovesSpinner.setEnabled(currentHero != null);
    leIncreaseSpinner.setEnabled(currentHero != null);
    aeIncreaseSpinner.setEnabled(currentHero != null);
    typeField.setEnabled(currentHero != null);
    if (currentHero != null) {
      mrBonusSpinner.setValue(currentHero.getMRBonus());
      beBonusSpinner.setValue(currentHero.getBEModification());
      aeBox.setSelected(currentHero.hasEnergy(Energy.AE));
      keBox.setSelected(currentHero.hasEnergy(Energy.KE));
      if (currentHero.canDoGreatMeditation()) {
        magicTypeCombo.setSelectedIndex(1);
      }
      else if (currentHero.hasExtraAEIncrease()) {
        magicTypeCombo.setSelectedIndex(2);
      }
      else {
        magicTypeCombo.setSelectedIndex(0);
      }
      talentIncreaseSpinner.setValue(currentHero.getTalentIncreasesPerStep());
      spellIncreaseSpinner.setValue(currentHero.getSpellIncreasesPerStep());
      increaseMovesSpinner.setValue(currentHero.getSpellToTalentMoves());
      leIncreaseSpinner.setValue(currentHero.getFixedLEIncrease());
      aeIncreaseSpinner.setValue(currentHero.getFixedAEIncrease());
      checkAEAvailability();
      typeField.setText(currentHero.getInternalType());
    }
    else {
      mrBonusSpinner.setValue(0);
      beBonusSpinner.setValue(0);
      aeBox.setSelected(false);
      keBox.setSelected(false);
      magicTypeCombo.setSelectedIndex(0);
      talentIncreaseSpinner.setValue(0);
      spellIncreaseSpinner.setValue(0);
      increaseMovesSpinner.setValue(0);
      leIncreaseSpinner.setValue(0);
      aeIncreaseSpinner.setValue(0);
      typeField.setText("");
    }
    listenForChanges = true;
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
      jContentPane.add(getJPanel1(), null);
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
      jLabel2.setBounds(new Rectangle(10, 30, 71, 21));
      jLabel2.setText("Name:");
      magicTypeLabel = new JLabel();
      magicTypeLabel.setBounds(new Rectangle(10, 150, 71, 21));
      magicTypeLabel.setText("Magietyp:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 90, 71, 21));
      jLabel1.setText("BE-Bonus:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 60, 71, 21));
      jLabel.setText("MR-Bonus:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(20, 20, 211, 191));
      jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
          javax.swing.BorderFactory
              .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
          "Typeigenschaften",
          javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
          javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(getAeBox(), null);
      jPanel.add(getKeBox(), null);
      jPanel.add(magicTypeLabel, null);
      jPanel.add(getMagicTypeCombo(), null);
      jPanel.add(getMrBonusSpinner(), null);
      jPanel.add(getBeBonusSpinner(), null);
      jPanel.add(jLabel2, null);
      jPanel.add(getTypeField(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getAeBox() {
    if (aeBox == null) {
      aeBox = new JCheckBox();
      aeBox.setBounds(new Rectangle(10, 120, 81, 21));
      aeBox.setText("Hat AE");
      aeBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!listenForChanges) return;
          currentHero.setHasEnergy(Energy.AE, aeBox.isSelected());
          checkAEAvailability();
        }
      });
    }
    return aeBox;
  }

  protected void checkFixedAEIncrAvailability() {
    boolean hasFixedAEIncr = aeBox.isSelected()
        && magicTypeCombo.getSelectedIndex() == 2;
    fixedAELabel.setEnabled(hasFixedAEIncr);
    aeIncreaseSpinner.setEnabled(hasFixedAEIncr);
  }

  /**
   * This method initializes jCheckBox1
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getKeBox() {
    if (keBox == null) {
      keBox = new JCheckBox();
      keBox.setBounds(new Rectangle(90, 120, 91, 21));
      keBox.setText("Hat KE");
      keBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!listenForChanges) return;
          currentHero.setHasEnergy(Energy.KE, keBox.isSelected());
        }
      });
    }
    return keBox;
  }

  /**
   * This method initializes jPanel1
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      fixedAELabel = new JLabel();
      fixedAELabel.setBounds(new java.awt.Rectangle(10, 150, 131, 21));
      fixedAELabel.setText("AE-Anstieg:  1W +");
      fixedLELabel = new JLabel();
      fixedLELabel.setBounds(new java.awt.Rectangle(10, 120, 131, 21));
      fixedLELabel.setText("LE-Anstieg:   1W +");
      incrMovesLabel = new JLabel();
      incrMovesLabel.setBounds(new java.awt.Rectangle(10, 90, 131, 21));
      incrMovesLabel.setText("Verschiebung:");
      incrMovesLabel.setToolTipText("Von Zauber- zu Talentsteigerungen");
      spellIncreaseLabel = new JLabel();
      spellIncreaseLabel.setBounds(new java.awt.Rectangle(10, 60, 131, 21));
      spellIncreaseLabel.setText("Zaubersteigerungen:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(10, 30, 131, 21));
      jLabel3.setText("Talentsteigerungen:");
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBounds(new java.awt.Rectangle(250, 20, 231, 191));
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(
          javax.swing.BorderFactory
              .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
          "Stufenanstieg",
          javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
          javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
      jPanel1.add(jLabel3, null);
      jPanel1.add(spellIncreaseLabel, null);
      jPanel1.add(incrMovesLabel, null);
      jPanel1.add(fixedLELabel, null);
      jPanel1.add(fixedAELabel, null);
      jPanel1.add(getTalentIncreaseSpinner(), null);
      jPanel1.add(getSpellIncreaseSpinner(), null);
      jPanel1.add(getIncreaseMovesSpinner(), null);
      jPanel1.add(getLeIncreaseSpinner(), null);
      jPanel1.add(getAeIncreaseSpinner(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getMagicTypeCombo() {
    if (magicTypeCombo == null) {
      magicTypeCombo = new JComboBox();
      magicTypeCombo.setBounds(new Rectangle(90, 150, 111, 21));
      magicTypeCombo.addItem("Normal");
      magicTypeCombo.addItem("Magier");
      magicTypeCombo.addItem("Geode");
      magicTypeLabel
          .setToolTipText("Magier: mit großer Meditation; Geode: mit seperater AE-Steigerung");
      magicTypeCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!listenForChanges) return;
          boolean gm = magicTypeCombo.getSelectedIndex() == 1;
          boolean ea = magicTypeCombo.getSelectedIndex() == 2;
          currentHero.setHasExtraAEIncrease(ea);
          currentHero.setCanDoGreatMeditation(gm);
          checkFixedAEIncrAvailability();
        }
      });
    }
    return magicTypeCombo;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getMrBonusSpinner() {
    if (mrBonusSpinner == null) {
      mrBonusSpinner = new JSpinner(new SpinnerNumberModel(0, -5, 5, 1));
      mrBonusSpinner.setBounds(new Rectangle(90, 60, 61, 21));
      mrBonusSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero.setMRBonus(((Number) mrBonusSpinner.getValue())
              .intValue());
        }
      });
    }
    return mrBonusSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getBeBonusSpinner() {
    if (beBonusSpinner == null) {
      beBonusSpinner = new JSpinner(new SpinnerNumberModel(0, -3, 3, 1));
      beBonusSpinner.setBounds(new Rectangle(90, 90, 61, 21));
      beBonusSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero.setBEModification(((Number) beBonusSpinner.getValue())
              .intValue());
        }
      });
    }
    return beBonusSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getTalentIncreaseSpinner() {
    if (talentIncreaseSpinner == null) {
      talentIncreaseSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 50, 1));
      talentIncreaseSpinner.setBounds(new java.awt.Rectangle(150, 30, 61, 21));
      talentIncreaseSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero.setTalentIncreasesPerStep(((Number) talentIncreaseSpinner
              .getValue()).intValue());
        }
      });
    }
    return talentIncreaseSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getSpellIncreaseSpinner() {
    if (spellIncreaseSpinner == null) {
      spellIncreaseSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 60, 1));
      spellIncreaseSpinner.setBounds(new java.awt.Rectangle(150, 60, 61, 21));
      spellIncreaseSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          int si = ((Number) spellIncreaseSpinner.getValue()).intValue();
          currentHero.setSpellIncreasesPerStep(si);
          if (((Number) increaseMovesSpinner.getValue()).intValue() > si) {
            increaseMovesSpinner.setValue(si);
          }
        }
      });
    }
    return spellIncreaseSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getIncreaseMovesSpinner() {
    if (increaseMovesSpinner == null) {
      increaseMovesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
      increaseMovesSpinner.setBounds(new java.awt.Rectangle(150, 90, 61, 21));
      increaseMovesSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero.setSpellToTalentMoves(((Number) increaseMovesSpinner
              .getValue()).intValue());
        }
      });
    }
    return increaseMovesSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getLeIncreaseSpinner() {
    if (leIncreaseSpinner == null) {
      leIncreaseSpinner = new JSpinner(new SpinnerNumberModel(0, -3, 3, 1));
      leIncreaseSpinner.setBounds(new java.awt.Rectangle(150, 120, 61, 21));
      leIncreaseSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero
              .setFixedLEIncrease(((Number) leIncreaseSpinner.getValue())
                  .intValue());
        }
      });
    }
    return leIncreaseSpinner;
  }

  /**
   * This method initializes JSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getAeIncreaseSpinner() {
    if (aeIncreaseSpinner == null) {
      aeIncreaseSpinner = new JSpinner(new SpinnerNumberModel(0, -3, 3, 1));
      aeIncreaseSpinner.setBounds(new java.awt.Rectangle(150, 150, 61, 21));
      aeIncreaseSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (!listenForChanges) return;
          currentHero
              .setFixedAEIncrease(((Number) aeIncreaseSpinner.getValue())
                  .intValue());
        }
      });
    }
    return aeIncreaseSpinner;
  }

  private void checkAEAvailability() {
    boolean hasAE = aeBox.isSelected();
    magicTypeLabel.setEnabled(hasAE);
    magicTypeCombo.setEnabled(hasAE);
    spellIncreaseLabel.setEnabled(hasAE);
    spellIncreaseSpinner.setEnabled(hasAE);
    incrMovesLabel.setEnabled(hasAE);
    increaseMovesSpinner.setEnabled(hasAE);
    checkFixedAEIncrAvailability();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    updateData();
  }

  public void characterRemoved(Hero character) {
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }

  /**
   * This method initializes typeField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getTypeField() {
    if (typeField == null) {
      typeField = new JTextField();
      typeField.setBounds(new Rectangle(90, 30, 111, 21));
      typeField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
          typeChanged();
        }
        public void insertUpdate(DocumentEvent e) {
          typeChanged();
        }
        public void removeUpdate(DocumentEvent e) {
          typeChanged();
        }
        private void typeChanged() {
          if (!listenForChanges) return;
          if (currentHero != null) currentHero.setInternalType(typeField.getText());
        }
      });
    }
    return typeField;
  }

} // @jve:decl-index=0:visual-constraint="11,12"

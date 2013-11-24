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
 along with Heldenverwaltung; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.frames;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JTextField;
import javax.swing.JPanel;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import javax.swing.text.NumberFormatter;

import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;

public final class StepFrame extends SubFrame implements CharactersObserver {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JTextField apField = null;

  private JToggleButton apLockBtn = null;

  private JButton apPlusBtn = null;

  private JLabel jLabel1 = null;

  private JTextField stepField = null;

  private JLabel jLabel2 = null;

  private JTextField nextStepField = null;

  private JLabel jLabel3 = null;

  private JTextField rufField = null;

  private JLabel missingLabel = null;
  
  private JLabel remainingStepsLabel = null;

  /**
   * This method initializes
   * 
   */
  public StepFrame() {
    super();
    initialize();
  }

  public StepFrame(String name) {
    super(name);
    Group.getInstance().addObserver(this);
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(StepFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(StepFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myCharacterObserver);
          done = true;
        }
      }
    });
    initialize();
    updateData();
  }
  
  public String getHelpPage() {
    return "Erfahrung";
  }

  private Hero currentHero;

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    // this.setSize(new java.awt.Dimension(319,227));
    this.setContentPane(getJContentPane());
    this.setTitle("Erfahrung");
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      freeTriesLabel = new JLabel();
      freeTriesLabel.setBounds(new java.awt.Rectangle(199, 197, 50, 22));
      freeTriesLabel.setText("");
      freeTriesLabel.setForeground(Color.RED);
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(7, 197, 181, 22));
      jLabel6.setText("Frei verteilbare Versuche:");
      spellTriesLabel = new JLabel();
      spellTriesLabel.setBounds(new java.awt.Rectangle(199, 167, 50, 22));
      spellTriesLabel.setText("");
      spellTriesLabel.setForeground(Color.RED);
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(7, 167, 181, 22));
      jLabel5.setText("Zaubersteigerungsversuche:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(7, 137, 181, 22));
      jLabel4.setText("Talentsteigerungsversuche:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(7, 102, 131, 22));
      jLabel3.setText("Ruf:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(7, 71, 127, 22));
      jLabel2.setText("Nächste Stufe bei:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(7, 40, 67, 22));
      jLabel1.setText("Stufe:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(7, 9, 124, 22));
      jLabel.setText("AbenteuerPunkte:");
      missingLabel = new JLabel("");
      missingLabel.setBounds(new java.awt.Rectangle(218, 71, 110, 22));
      remainingStepsLabel = new JLabel("");
      remainingStepsLabel.setBounds(new java.awt.Rectangle(218, 40, 110, 22));
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getApField(), null);
      jContentPane.add(getApLockBtn(), null);
      jContentPane.add(getApPlusBtn(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getStepField(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(getNextStepField(), null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(getRufField(), null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(getTalentTriesLabel(), null);
      jContentPane.add(jLabel5, null);
      jContentPane.add(spellTriesLabel, null);
      jContentPane.add(jLabel6, null);
      jContentPane.add(freeTriesLabel, null);
      jContentPane.add(getClearButton(), null);
      jContentPane.add(missingLabel, null);
      jContentPane.add(remainingStepsLabel, null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getApField() {
    if (apField == null) {
      apField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      apField.setBounds(new java.awt.Rectangle(152, 11, 56, 20));
      apField.setEditable(false);
      apField.addPropertyChangeListener(new PropertyChangeListener() {

        boolean listen = true;

        public void propertyChange(PropertyChangeEvent evt) {
          if (!listen) return;
          if (!evt.getPropertyName().equals("value")) return;
          if (currentHero != null) {
            listen = false;
            String sValue = apField.getText();
            int iValue = Integer.parseInt(sValue);
            currentHero.setAP(iValue);
            listen = true;
          }
        }

      });
    }
    return apField;
  }

  /**
   * This method initializes jToggleButton
   * 
   * @return javax.swing.JToggleButton
   */
  private JToggleButton getApLockBtn() {
    if (apLockBtn == null) {
      apLockBtn = new JToggleButton(ImageManager.getIcon("locked"));
      apLockBtn.setBounds(new java.awt.Rectangle(219, 11, 33, 18));
      apLockBtn.setToolTipText("Schützen / Freigeben");
      apLockBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          apField.setEditable(apLockBtn.isSelected()
              || Group.getInstance().getGlobalUnlock());
          apLockBtn.setIcon(ImageManager
              .getIcon(apLockBtn.isSelected() ? "unlocked" : "locked"));
        }
      });
    }
    return apLockBtn;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getApPlusBtn() {
    if (apPlusBtn == null) {
      apPlusBtn = new JButton(ImageManager.getIcon("increase"));
      apPlusBtn.setBounds(new java.awt.Rectangle(264, 11, 33, 18));
      apPlusBtn.setToolTipText("AP hinzufügen");
      apPlusBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          addAP();
        }
      });
    }
    return apPlusBtn;
  }

  protected void addAP() {
    String apS = javax.swing.JOptionPane.showInputDialog(this,
        "Im letzten Abenteuer verdiente AP:", "AP erhöhen",
        JOptionPane.PLAIN_MESSAGE);
    if (apS == null) return;
    int ap = 0;
    try {
      ap = Integer.parseInt(apS);
      if (ap < 0) throw new NumberFormatException("");
    }
    catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,
          "Bitte eine positive ganze Zahl eingeben.", "Fehler",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    int currentStep = currentHero.getStep();
    currentHero.changeAP(ap);
    updateData();
    if (currentHero.getStep() > currentStep) {
      JOptionPane.showMessageDialog(this, currentHero.getName()
          + " ist gerade auf Stufe " + currentHero.getStep() + " gestiegen!",
          "Stufenanstieg", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getStepField() {
    if (stepField == null) {
      stepField = new JTextField();
      stepField.setBounds(new java.awt.Rectangle(152, 42, 56, 20));
      stepField.setEditable(false);
    }
    return stepField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNextStepField() {
    if (nextStepField == null) {
      nextStepField = new JTextField();
      nextStepField.setBounds(new java.awt.Rectangle(152, 73, 56, 20));
      nextStepField.setEditable(false);
    }
    return nextStepField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getRufField() {
    if (rufField == null) {
      rufField = new JTextField();
      rufField.setBounds(new java.awt.Rectangle(152, 104, 144, 20));
      rufField.setEditable(false);
    }
    return rufField;
  }

  private class MyCharacterObserver extends CharacterAdapter {

    public void stepIncreased() {
      updateData();
    }

    public void increaseTriesChanged() {
      updateData();
    }

  }

  private final MyCharacterObserver myCharacterObserver = new MyCharacterObserver();

  private JLabel jLabel4 = null;

  private JLabel talentTriesLabel = null;

  private JLabel jLabel5 = null;

  private JLabel spellTriesLabel = null;

  private JLabel jLabel6 = null;

  private JLabel freeTriesLabel = null;

  private JButton clearButton = null;

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    currentHero = newCharacter;
    updateData();
  }

  public void characterRemoved(Hero character) {
    updateData();
  }

  public void characterAdded(Hero character) {
    updateData();
  }

  public void globalLockChanged() {
    updateData();
  }

  private void updateData() {
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) {
      apField.setText("" + currentHero.getAP());
      stepField.setText("" + currentHero.getStep());
      nextStepField.setText("" + currentHero.getStep()
          * (currentHero.getStep() + 1) * 50);
      rufField.setText(currentHero.getRuf());
      talentTriesLabel
          .setText("" + currentHero.getOverallTalentIncreaseTries());
      apPlusBtn.setEnabled(true);
      apLockBtn.setEnabled(true);
      spellTriesLabel.setText("" + currentHero.getOverallSpellIncreaseTries());
      freeTriesLabel.setText("" + currentHero.getSpellOrTalentIncreaseTries());
      apField.setEditable(apLockBtn.isSelected()
          || Group.getInstance().getGlobalUnlock());
      clearButton.setEnabled(currentHero.getOverallSpellIncreaseTries()
          + currentHero.getSpellOrTalentIncreaseTries()
          + currentHero.getOverallTalentIncreaseTries() > 0);
      missingLabel
          .setText("("
              + (currentHero.getStep() * (currentHero.getStep() + 1) * 50 - currentHero
                  .getAP()) + " AP fehlen)");
      if (currentHero.getRemainingStepIncreases() > 0) {
        remainingStepsLabel.setText("(" + currentHero.getRemainingStepIncreases() + " zu steigern)");
      }
      else {
        remainingStepsLabel.setText("");
      }
    }
    else {
      apField.setText("");
      stepField.setText("");
      nextStepField.setText("");
      rufField.setText("");
      talentTriesLabel.setText("");
      spellTriesLabel.setText("");
      freeTriesLabel.setText("");
      apPlusBtn.setEnabled(false);
      apLockBtn.setEnabled(false);
      clearButton.setEnabled(false);
      missingLabel.setText("");
    }

  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JLabel getTalentTriesLabel() {
    if (talentTriesLabel == null) {
      talentTriesLabel = new JLabel();
      talentTriesLabel.setBounds(new java.awt.Rectangle(199, 137, 50, 22));
      talentTriesLabel.setForeground(Color.RED);
    }
    return talentTriesLabel;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getClearButton() {
    if (clearButton == null) {
      clearButton = new JButton();
      clearButton.setBounds(new java.awt.Rectangle(261, 137, 37, 20));
      clearButton.setIcon(ImageManager.getIcon("decrease_enabled"));
      clearButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      clearButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clearIncreaseTries();
        }
      });
      clearButton.setToolTipText("Restliche Steigerungsversuche streichen");
    }
    return clearButton;
  }

  protected void clearIncreaseTries() {
    if (JOptionPane.showConfirmDialog(this,
        "Restliche Steigerungsversuche streichen?", "Heldenverwaltung",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      currentHero.removeRemainingIncreaseTries();
    }
  }

} // @jve:decl-index=0:visual-constraint="10,10"

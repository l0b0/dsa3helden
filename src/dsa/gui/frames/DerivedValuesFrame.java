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
package dsa.gui.frames;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import dsa.gui.util.OptionsChange;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;

public class DerivedValuesFrame extends SubFrame implements CharactersObserver,
    OptionsChange.OptionsListener {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JTextField atDField = null;

  private JLabel jLabel5 = null;

  private JTextField paDField = null;

  private JTextField fkDField = null;

  private JTextField awDField = null;

  private JTextField abDField = null;

  private JTextField atCField = null;

  private JLabel jLabel6 = null;

  private JTextField paCField = null;

  private JTextField fkCField = null;

  private JTextField awCField = null;

  private JTextField abCField = null;

  private JLabel jLabel7 = null;

  private JTextField tkDField = null;

  private JTextField tkCField = null;

  private JLabel jLabel8 = null;

  private JTextField mrDField = null;

  private JTextField mrCField = null;

  private JLabel atModLabel = null;

  private JLabel paModLabel = null;

  private JLabel fkModLabel = null;

  private JLabel awModLabel = null;

  private JLabel abModLabel = null;

  private JLabel tkModLabel = null;

  private JLabel mrModLabel = null;

  /**
   * This method initializes
   * 
   */
  public DerivedValuesFrame() {
    super();
    initialize();
  }
  
  public String getHelpPage() {
    return "Berechnete_Werte"; //$NON-NLS-1$
  }

  public DerivedValuesFrame(String title) {
    super(title);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(DerivedValuesFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        OptionsChange.removeListener(DerivedValuesFrame.this);
      }

      public void windowClosed(WindowEvent e) {
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        OptionsChange.removeListener(DerivedValuesFrame.this);
      }
    });
    initialize();
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    OptionsChange.addListener(this);
    updateData();
  }

  private Hero currentHero = null;

  private static String getModSign(int value) {
    return value > 0 ? "+" : ""; //$NON-NLS-1$ //$NON-NLS-2$
  }

  private static final Color DARKGREEN = new Color(0, 175, 0);

  private Color getColor(int value) {
    return value > 0 ? DARKGREEN : value < 0 ? Color.RED : Color.BLACK;
  }

  private void updateData() {
    if (currentHero != null) {
      atDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.AT));
      atCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.AT));
      paDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.PA));
      paCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.PA));
      fkDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.FK));
      fkCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.FK));
      awDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.AW));
      awCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.AW));
      abDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.AB));
      abCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.AB));
      tkDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.TK));
      tkCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.TK));
      mrDField.setText("" //$NON-NLS-1$
          + currentHero.getDefaultDerivedValue(Hero.DerivedValue.MR));
      mrCField.setText("" //$NON-NLS-1$
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.MR));
      
      boolean active = !currentHero.isDifference();
      atCField.setEditable(active);
      paCField.setEditable(active);
      fkCField.setEditable(active);
      awCField.setEditable(active);
      abCField.setEditable(active);
      tkCField.setEditable(active);
      mrCField.setEditable(active);
      
      int value = 0;
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.AT);
      atModLabel.setText(getModSign(value) + value);
      atModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.PA);
      paModLabel.setText(getModSign(value) + value);
      paModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.FK);
      fkModLabel.setText(getModSign(value) + value);
      fkModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.AW);
      awModLabel.setText(getModSign(value) + value);
      awModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.AB);
      abModLabel.setText(getModSign(value) + value);
      awModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.TK);
      tkModLabel.setText(getModSign(value) + value);
      tkModLabel.setForeground(getColor(value));
      value = currentHero.getCurrentDerivedValueChange(Hero.DerivedValue.MR);
      mrModLabel.setText(getModSign(value) + value);
      mrModLabel.setForeground(getColor(value));
    }
    else {
      atDField.setText("-"); //$NON-NLS-1$
      atCField.setText("-"); //$NON-NLS-1$
      atModLabel.setText("-"); //$NON-NLS-1$
      atModLabel.setForeground(Color.BLACK);
      paDField.setText("-"); //$NON-NLS-1$
      paCField.setText("-"); //$NON-NLS-1$
      paModLabel.setText("-"); //$NON-NLS-1$
      paModLabel.setForeground(Color.BLACK);
      fkDField.setText("-"); //$NON-NLS-1$
      fkCField.setText("-"); //$NON-NLS-1$
      fkModLabel.setText("-"); //$NON-NLS-1$
      fkModLabel.setForeground(Color.BLACK);
      awDField.setText("-"); //$NON-NLS-1$
      awCField.setText("-"); //$NON-NLS-1$
      awModLabel.setText("-"); //$NON-NLS-1$
      awModLabel.setForeground(Color.BLACK);
      abDField.setText("-"); //$NON-NLS-1$
      abCField.setText("-"); //$NON-NLS-1$
      abModLabel.setText("-"); //$NON-NLS-1$
      abModLabel.setForeground(Color.BLACK);
      tkDField.setText("-"); //$NON-NLS-1$
      tkCField.setText("-"); //$NON-NLS-1$
      tkModLabel.setText("-"); //$NON-NLS-1$
      tkModLabel.setForeground(Color.BLACK);
      mrDField.setText("-"); //$NON-NLS-1$
      mrCField.setText("-"); //$NON-NLS-1$
      mrModLabel.setText("-"); //$NON-NLS-1$
      mrModLabel.setForeground(Color.BLACK);
    }
  }

  private final CharacterObserver myCharacterObserver = new CharacterAdapter() {

    public void defaultPropertyChanged(Property property) {
      updateData();
    }

    public void currentPropertyChanged(Property property) {
      updateData();
    }

    public void stepIncreased() {
      updateData();
    }

    public void derivedValueChanged(Hero.DerivedValue dv) {
      updateData();
    }

  };

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    // this.setSize(new java.awt.Dimension(274,252));
    this.setContentPane(getJContentPane());
    this.setTitle(Localization.getString("BerechneteWerte.BerechneteWerte")); //$NON-NLS-1$
  }

  private boolean disableChange = false;

  private class ValueChanger implements PropertyChangeListener {
    public ValueChanger(Hero.DerivedValue dv, JLabel label) {
      this.dv = dv;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if (DerivedValuesFrame.this.disableChange) return;
      if (!evt.getPropertyName().equals("value")) return; //$NON-NLS-1$
      if (currentHero != null) {
        int value = ((Number) ((JFormattedTextField) evt.getSource())
            .getValue()).intValue();
        int oldValue = currentHero.getCurrentDerivedValue(dv);
        int oldChange = currentHero.getCurrentDerivedValueChange(dv);
        int newChange = value - (oldValue - oldChange);
        currentHero.setCurrentDerivedValueChange(dv, newChange);
      }
    }

    private final Hero.DerivedValue dv;

  };

  private JPanel getJContentPane() {
    if (jContentPane == null) {
      final int modWidth = 40;
      jLabel8 = new JLabel();
      jLabel8.setBounds(new java.awt.Rectangle(9, 27, 127, 19));
      jLabel8.setText(Localization.getString("BerechneteWerte.MagieResistenz")); //$NON-NLS-1$
      mrModLabel = new JLabel();
      mrModLabel.setBounds(255, 27, modWidth, 19);
      jLabel7 = new JLabel();
      jLabel7.setBounds(new java.awt.Rectangle(9, 189, 127, 19));
      jLabel7.setText(Localization.getString("BerechneteWerte.TragkraftUnzen")); //$NON-NLS-1$
      tkModLabel = new JLabel();
      tkModLabel.setBounds(255, 189, modWidth, 19);
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(207, 9, 37, 19));
      jLabel6.setText(Localization.getString("BerechneteWerte.Akt")); //$NON-NLS-1$
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(153, 9, 37, 19));
      jLabel5.setText(Localization.getString("BerechneteWerte.Std")); //$NON-NLS-1$
      JLabel modLabel = new JLabel();
      modLabel.setBounds(255, 9, 30, 19);
      modLabel.setText(Localization.getString("BerechneteWerte.Mod")); //$NON-NLS-1$
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(9, 162, 127, 19));
      jLabel4.setText(Localization.getString("BerechneteWerte.AusweichenBonus")); //$NON-NLS-1$
      abModLabel = new JLabel();
      abModLabel.setBounds(255, 162, modWidth, 19);
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(9, 135, 127, 19));
      jLabel3.setText(Localization.getString("BerechneteWerte.AusweichenBasis")); //$NON-NLS-1$
      awModLabel = new JLabel();
      awModLabel.setBounds(255, 135, modWidth, 19);
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(9, 108, 127, 19));
      jLabel2.setText(Localization.getString("BerechneteWerte.FernkampfBasis")); //$NON-NLS-1$
      fkModLabel = new JLabel();
      fkModLabel.setBounds(255, 108, modWidth, 19);
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(9, 81, 127, 19));
      jLabel1.setText(Localization.getString("BerechneteWerte.ParadeBasis")); //$NON-NLS-1$
      paModLabel = new JLabel();
      paModLabel.setBounds(255, 81, modWidth, 19);
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(9, 54, 127, 19));
      jLabel.setText(Localization.getString("BerechneteWerte.AttackeBasis")); //$NON-NLS-1$
      atModLabel = new JLabel();
      atModLabel.setBounds(255, 54, modWidth, 19);
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(getAtDField(), null);
      jContentPane.add(getPaDField(), null);
      jContentPane.add(getFkDField(), null);
      jContentPane.add(getAwDField(), null);
      jContentPane.add(getAbDField(), null);
      jContentPane.add(getAtCField(), null);
      jContentPane.add(jLabel6, null);
      jContentPane.add(getPaCField(), null);
      jContentPane.add(getFkCField(), null);
      jContentPane.add(getAwCField(), null);
      jContentPane.add(getAbCField(), null);
      jContentPane.add(jLabel7, null);
      jContentPane.add(getTkDField(), null);
      jContentPane.add(getTkCField(), null);
      jContentPane.add(jLabel8, null);
      jContentPane.add(getMrDField(), null);
      jContentPane.add(getMrCField(), null);
      jContentPane.add(jLabel5, null);
      jContentPane.add(atModLabel, null);
      jContentPane.add(paModLabel, null);
      jContentPane.add(fkModLabel, null);
      jContentPane.add(tkModLabel, null);
      jContentPane.add(awModLabel, null);
      jContentPane.add(abModLabel, null);
      jContentPane.add(mrModLabel, null);
      jContentPane.add(modLabel, null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAtDField() {
    if (atDField == null) {
      atDField = new JTextField();
      atDField.setBounds(new java.awt.Rectangle(144, 54, 46, 19));
      atDField.setEditable(false);
    }
    return atDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getPaDField() {
    if (paDField == null) {
      paDField = new JTextField();
      paDField.setBounds(new java.awt.Rectangle(144, 81, 46, 19));
      paDField.setEditable(false);
    }
    return paDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getFkDField() {
    if (fkDField == null) {
      fkDField = new JTextField();
      fkDField.setBounds(new java.awt.Rectangle(144, 108, 46, 19));
      fkDField.setEditable(false);
    }
    return fkDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAwDField() {
    if (awDField == null) {
      awDField = new JTextField();
      awDField.setBounds(new java.awt.Rectangle(144, 135, 46, 19));
      awDField.setEditable(false);
    }
    return awDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAbDField() {
    if (abDField == null) {
      abDField = new JTextField();
      abDField.setBounds(new java.awt.Rectangle(144, 162, 46, 19));
      abDField.setEditable(false);
    }
    return abDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAtCField() {
    if (atCField == null) {
      atCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      atCField.setBounds(new java.awt.Rectangle(198, 54, 46, 19));
      atCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.AT,
          atModLabel));
    }
    return atCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getPaCField() {
    if (paCField == null) {
      paCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      paCField.setBounds(new java.awt.Rectangle(198, 81, 46, 19));
      paCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.PA,
          paModLabel));
    }
    return paCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getFkCField() {
    if (fkCField == null) {
      fkCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      fkCField.setBounds(new java.awt.Rectangle(198, 108, 46, 19));
      fkCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.FK,
          fkModLabel));
    }
    return fkCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAwCField() {
    if (awCField == null) {
      awCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      awCField.setBounds(new java.awt.Rectangle(198, 135, 46, 19));
      awCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.AW,
          awModLabel));
    }
    return awCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getAbCField() {
    if (abCField == null) {
      abCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      abCField.setBounds(new java.awt.Rectangle(198, 162, 46, 19));
      abCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.AB,
          abModLabel));
    }
    return abCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getTkDField() {
    if (tkDField == null) {
      tkDField = new JTextField();
      tkDField.setBounds(new java.awt.Rectangle(144, 189, 46, 19));
      tkDField.setEditable(false);
    }
    return tkDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getTkCField() {
    if (tkCField == null) {
      tkCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      tkCField.setBounds(new java.awt.Rectangle(198, 189, 46, 19));
      tkCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.TK,
          tkModLabel));
    }
    return tkCField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getMrDField() {
    if (mrDField == null) {
      mrDField = new JTextField();
      mrDField.setBounds(new java.awt.Rectangle(144, 27, 46, 19));
      mrDField.setEditable(false);
    }
    return mrDField;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getMrCField() {
    if (mrCField == null) {
      mrCField = new JFormattedTextField(new NumberFormatter(NumberFormat
          .getIntegerInstance()));
      mrCField.setBounds(new java.awt.Rectangle(198, 27, 46, 19));
      mrCField.addPropertyChangeListener(new ValueChanger(Hero.DerivedValue.MR,
          mrModLabel));
    }
    return mrCField;
  }

  public void globalLockChanged() {
  }

  public void optionsChanged() {
    updateData();
  }

} // @jve:decl-index=0:visual-constraint="10,10"

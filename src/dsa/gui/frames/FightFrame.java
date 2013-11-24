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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.control.Dice;
import dsa.control.Fighting;
import dsa.control.Markers;
import dsa.gui.dialogs.ProbeResultDialog;
import dsa.gui.dialogs.WeaponsSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.dialogs.fighting.ProjectileAttackDialog;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.model.DiceSpecification;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.Talent;
import dsa.util.Optional;

import javax.swing.JCheckBox;
import java.awt.Rectangle;

public final class FightFrame extends SubFrame implements CharactersObserver,
    OptionsChange.OptionsListener {

  private JPanel jContentPane = null;

  private JLabel leLabel = null;

  private JTextField leField = null;

  private JLabel jLabel1 = null;

  private JLabel awLabel = null;

  private JLabel jLabel3 = null;

  private JLabel rsLabel = null;

  private JLabel jLabel2 = null;

  private JLabel beLabel = null;

  private JLabel jLabel4 = null;

  private JComboBox modeBox = null;

  private JLabel firstHandLabel = null;

  private JComboBox hand1Box = null;

  private JLabel jLabel6 = null;

  private JLabel at1Label = null;

  private JLabel jLabel7 = null;

  private JLabel pa1Label = null;

  private JLabel jLabel8 = null;

  private JLabel tp1Label = null;

  private JLabel jLabel9 = null;

  private JLabel bf1Label = null;

  private JLabel secondHandLabel = null;

  private JComboBox hand2Box = null;

  private JLabel jLabel5 = null;

  private JLabel at2Label = null;

  private JLabel jLabel10 = null;

  private JLabel pa2Label = null;

  private JLabel jLabel11 = null;

  private JLabel tp2Label = null;

  private JLabel jLabel12 = null;

  private JLabel bf2Label = null;

  private JButton attack1Button = null;

  private JButton parade1Button = null;

  private JButton attack2Button = null;

  private JButton parade2Button = null;

  private JCheckBox fromAUBox = null;

  private boolean listen = true;

  private boolean withDazed = false;

  private Hero currentHero;

  /**
   * This method initializes
   * 
   */
  public FightFrame() {
    super("Kampf (Spieler)");
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(FightFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        OptionsChange.removeListener(FightFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(FightFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myCharacterObserver);
          OptionsChange.removeListener(FightFrame.this);
          done = true;
        }
      }
    });
    initialize();
    OptionsChange.addListener(this);
  }
  
  public String getHelpPage() {
    return "Kampf_Spieler";
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setContentPane(getJContentPane());
    this.setTitle("Kampf (Spieler)");
    enableWVControls();
    updateData();
  }

  boolean boxContains(JComboBox box, String item) {
    for (int i = 0; i < box.getItemCount(); ++i) {
      if (box.getItemAt(i).equals(item)) return true;
    }
    return false;
  }

  boolean withMarkers;

  boolean useAU;

  private void updateData() {
    listen = false;
    withDazed = Group.getInstance().getOptions().hasQvatStunned();
    withMarkers = Markers.isUsingMarkers();
    if (currentHero != null && !currentHero.isDifference()) {
      useAU = currentHero.fightUsesAU();
      fromAUBox.setEnabled(true);
      fromAUBox.setSelected(useAU);
      leLabel.setText(useAU ? "AU" : "LE");
      leField.setText(""
          + currentHero.getCurrentEnergy(useAU ? Energy.AU : Energy.LE));
      leField.setEnabled(true);
      awLabel.setText(""
          + currentHero.getCurrentDerivedValue(Hero.DerivedValue.AB));
      rsLabel.setText("" + currentHero.getRS());
      modeBox.setEnabled(true);
      modeBox.removeAllItems();
      modeBox.addItem("Waffenlos");
      int nrOfCloseRangeWeapons = Fighting.getCloseRangeWeapons(currentHero).size();
      if (nrOfCloseRangeWeapons > 0) {
        modeBox.addItem("Eine Waffe");
        if (currentHero.getShields().length > 0) {
          modeBox.addItem("Waffe + Parade");
          if (currentHero.getCurrentTalentValue("Linkshändig") >= 9) {
            modeBox.addItem("Waffe + Parade, separat");
          }
        }
        if (nrOfCloseRangeWeapons > 1
            && currentHero.getCurrentTalentValue("Linkshändig") >= 6) {
          modeBox.addItem("Zwei Waffen");
        }
      }
      if (Fighting.getTwoHandedWeapons(currentHero).size() > 0) {
        modeBox.addItem("Zweihandwaffe");
      }
      if (Fighting.getProjectileWeapons(currentHero).size() > 0) {
        modeBox.addItem("Fernkampf");
      }
      if (boxContains(modeBox, currentHero.getFightMode())) {
        modeBox.setSelectedItem(currentHero.getFightMode());
      }
      else {
        modeBox.setSelectedIndex(0);
      }
      groundBox.setSelected(currentHero.isGrounded());
      groundBox.setEnabled(true);
      if (withDazed) {
        if (!Fighting.canDefend(currentHero)) currentHero.setDazed(true);
        dazedBox.setSelected(currentHero.isDazed());
        dazedBox.setEnabled(Fighting.canDefend(currentHero));
      }
      else {
        dazedBox.setSelected(false);
        currentHero.setDazed(false);
        dazedBox.setEnabled(false);
      }
      opponent1Box.setEditable(true);
      opponent1Box.setSelectedItem(currentHero.getOpponentWeapon(0));
      opponent1Box.setEditable(false);
      opponent2Box.setEditable(true);
      opponent2Box.setSelectedItem(currentHero.getOpponentWeapon(1));
      opponent2Box.setEditable(false);
      modeChanged(true);
      listen = true;
      if (boxContains(hand1Box, currentHero.getFirstHandWeapon())) {
        hand1Box.setSelectedItem(currentHero.getFirstHandWeapon());
      }
      else if (hand1Box.getItemCount() > 0) {
        hand1Box.setSelectedIndex(0);
      }
      if (hand2Box.isEnabled()) {
        if (boxContains(hand2Box, currentHero.getSecondHandItem())) {
          hand2Box.setSelectedItem(currentHero.getSecondHandItem());
        }
        else if (hand2Box.getItemCount() > 0) {
          hand2Box.setSelectedIndex(0);
        }
      }
      if (withMarkers) {
        markerSpinner.setEnabled(true);
        updateMarkerValues();
      }
      else {
        markerSpinner.setEnabled(false);
        markerSpinner.setValue(0);
      }
      // hand1Changed();
      // hand2Changed();
    }
    else {
      leLabel.setText("LE");
      awLabel.setText("-");
      at1Label.setText("-");
      at2Label.setText("-");
      beLabel.setText("-");
      bf1Label.setText("-");
      bf2Label.setText("-");
      firstHandLabel.setText("Erste Hand:");
      hand1Box.removeAllItems();
      hand1Box.setEnabled(false);
      hand2Box.removeAllItems();
      hand2Box.setEnabled(false);
      modeBox.removeAllItems();
      modeBox.setEnabled(false);
      pa1Label.setText("-");
      pa2Label.setText("-");
      parade1Button.setEnabled(false);
      parade2Button.setEnabled(false);
      rsLabel.setText("-");
      secondHandLabel.setText("Zweite Hand:");
      tp1Label.setText("-");
      tp2Label.setText("-");
      attack1Button.setEnabled(false);
      attack2Button.setEnabled(false);
      leField.setText("-");
      leField.setEnabled(false);
      groundBox.setSelected(false);
      groundBox.setEnabled(false);
      dazedBox.setSelected(false);
      dazedBox.setEnabled(false);
      markerSpinner.setEnabled(false);
      markerSpinner.setValue(0);
      fromAUBox.setEnabled(false);
      fromAUBox.setSelected(false);
    }
    setLEColor();
    evadeButton.setEnabled(currentHero != null && !currentHero.isDifference() && Fighting.canDefend(currentHero));
    hitButton.setEnabled(currentHero != null && !currentHero.isDifference());
    listen = true;
  }

  void updateMarkerValues() {
    int markers = currentHero.getMarkers();
    int extraMarkers = currentHero.getExtraMarkers();
    markerSpinnerModel.setMinimum(markers - extraMarkers);
    markerSpinnerModel.setMaximum(markers - extraMarkers + 11);
    markerSpinner.setValue(markers);
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      opponent1Label = new JLabel();
      opponent1Label.setBounds(new Rectangle(11, 98, 100, 23));
      opponent1Label.setText("Gegner:");
      opponent2Label = new JLabel();
      opponent2Label.setBounds(new Rectangle(10, 190, 101, 21));
      opponent2Label.setText("Gegner:");
      jLabel13 = new JLabel();
      jLabel13.setBounds(new Rectangle(10, 280, 101, 21));
      jLabel13.setText("Marker:");
      bf2Label = new JLabel();
      bf2Label.setBounds(new Rectangle(260, 220, 31, 21));
      bf2Label.setText("4");
      jLabel12 = new JLabel();
      jLabel12.setBounds(new Rectangle(230, 220, 21, 21));
      jLabel12.setText("BF:");
      tp2Label = new JLabel();
      tp2Label.setBounds(new Rectangle(170, 220, 51, 21));
      tp2Label.setText("2W+6");
      jLabel11 = new JLabel();
      jLabel11.setBounds(new Rectangle(140, 220, 21, 21));
      jLabel11.setText("TP:");
      pa2Label = new JLabel();
      pa2Label.setBounds(new Rectangle(110, 220, 21, 21));
      pa2Label.setText("13");
      jLabel10 = new JLabel();
      jLabel10.setBounds(new Rectangle(80, 220, 21, 21));
      jLabel10.setText("PA:");
      at2Label = new JLabel();
      at2Label.setBounds(new Rectangle(50, 220, 21, 21));
      at2Label.setText("12");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new Rectangle(20, 220, 21, 21));
      jLabel5.setText("AT:");
      secondHandLabel = new JLabel();
      secondHandLabel.setBounds(new Rectangle(10, 160, 101, 21));
      secondHandLabel.setText("Zweite Hand:");
      bf1Label = new JLabel();
      bf1Label.setBounds(new Rectangle(260, 130, 30, 21));
      bf1Label.setText("3");
      jLabel9 = new JLabel();
      jLabel9.setBounds(new Rectangle(229, 130, 22, 21));
      jLabel9.setText("BF:");
      tp1Label = new JLabel();
      tp1Label.setBounds(new Rectangle(170, 130, 48, 21));
      tp1Label.setText("3W+10");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new Rectangle(140, 130, 21, 21));
      jLabel8.setText("TP:");
      pa1Label = new JLabel();
      pa1Label.setBounds(new Rectangle(110, 130, 21, 21));
      pa1Label.setText("15");
      jLabel7 = new JLabel();
      jLabel7.setBounds(new Rectangle(80, 130, 25, 21));
      jLabel7.setText("PA:");
      at1Label = new JLabel();
      at1Label.setBounds(new Rectangle(50, 130, 21, 21));
      at1Label.setText("11");
      jLabel6 = new JLabel();
      jLabel6.setBounds(new Rectangle(20, 130, 23, 21));
      jLabel6.setText("AT:");
      firstHandLabel = new JLabel();
      firstHandLabel.setBounds(new java.awt.Rectangle(10, 70, 105, 21));
      firstHandLabel.setText("Erste Hand:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(10, 40, 103, 21));
      jLabel4.setText("Kampfmodus:");
      beLabel = new JLabel();
      beLabel.setBounds(new Rectangle(268, 10, 23, 19));
      beLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
      beLabel.setText("2");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(231, 10, 27, 19));
      jLabel2.setText("BE:");
      rsLabel = new JLabel();
      rsLabel.setBounds(new java.awt.Rectangle(194, 10, 27, 19));
      rsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
      rsLabel.setText("2");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(157, 10, 27, 19));
      jLabel3.setText("RS:");
      awLabel = new JLabel();
      awLabel.setBounds(new java.awt.Rectangle(120, 10, 27, 19));
      awLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
      awLabel.setText("100");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(83, 10, 27, 19));
      jLabel1.setText("AB:");
      leField = new JTextField();
      leField.setBounds(new java.awt.Rectangle(46, 10, 27, 19));
      leField.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
      leField.setText("100");
      leField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          leChanged();
        }

        public void removeUpdate(DocumentEvent e) {
          leChanged();
        }

        public void changedUpdate(DocumentEvent e) {
          leChanged();
        }
      });
      leLabel = new JLabel();
      leLabel.setBounds(new java.awt.Rectangle(9, 10, 27, 19));
      leLabel.setText("LE:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(leLabel, null);
      jContentPane.add(leField, null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(awLabel, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(rsLabel, null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(beLabel, null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(getModeBox(), null);
      jContentPane.add(firstHandLabel, null);
      jContentPane.add(getHand1Box(), null);
      jContentPane.add(jLabel6, null);
      jContentPane.add(at1Label, null);
      jContentPane.add(jLabel7, null);
      jContentPane.add(pa1Label, null);
      jContentPane.add(jLabel8, null);
      jContentPane.add(tp1Label, null);
      jContentPane.add(jLabel9, null);
      jContentPane.add(bf1Label, null);
      jContentPane.add(secondHandLabel, null);
      jContentPane.add(getHand2Box(), null);
      jContentPane.add(jLabel5, null);
      jContentPane.add(at2Label, null);
      jContentPane.add(jLabel10, null);
      jContentPane.add(pa2Label, null);
      jContentPane.add(jLabel11, null);
      jContentPane.add(tp2Label, null);
      jContentPane.add(jLabel12, null);
      jContentPane.add(bf2Label, null);
      jContentPane.add(getAttack1Button(), null);
      jContentPane.add(getParade1Button(), null);
      jContentPane.add(getAttack2Button(), null);
      jContentPane.add(getParade2Button(), null);
      jContentPane.add(getGroundBox(), null);
      jContentPane.add(getEvadeButton(), null);
      jContentPane.add(getHitButton(), null);
      jContentPane.add(getDazedBox(), null);
      jContentPane.add(jLabel13, null);
      jContentPane.add(getMarkerSpinner(), null);
      jContentPane.add(getAUBox(), null);
      jContentPane.add(opponent2Label, null);
      jContentPane.add(getOpponent2Box(), null);
      jContentPane.add(opponent1Label, null);
      jContentPane.add(getOpponent1Box(), null);
    }
    return jContentPane;
  }

  protected void leChanged() {
    if (!listen) return;
    if (currentHero == null) return;
    listen = false;
    String text = leField.getText();
    try {
      int value = Integer.parseInt(text);
      if (useAU) {
        int oldValue = currentHero.getCurrentEnergy(Energy.AU);
        int diff = value - oldValue;
        currentHero.changeAU(diff);
      }
      else {
        currentHero.setCurrentEnergy(Energy.LE, value);
      }
      setLEColor();
      if (value <= 5 && withDazed) {
        currentHero.setDazed(true);
        dazedBox.setSelected(true);
        dazedBox.setEnabled(false);
      }
      else if (withDazed) {
        dazedBox.setEnabled(true);
      }
      if (withMarkers && !useAU) {
        updateMarkerValues();
      }
      listen = true;
    }
    catch (NumberFormatException e) {
      listen = true;
    }
    modeChanged(true);
  }

  private void setLEColor() {
    Color color = null;
    if (currentHero == null) {
      color = Color.BLACK;
    }
    else {
      int value = currentHero.getCurrentEnergy(useAU ? Energy.AU : Energy.LE);
      if (value > 20)
        color = Color.GREEN;
      else if (value > 8)
        color = Color.ORANGE;
      else
        color = Color.RED;
    }
    leField.setForeground(color);
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getModeBox() {
    if (modeBox == null) {
      modeBox = new JComboBox();
      modeBox.setBounds(new Rectangle(120, 40, 171, 21));
      modeBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          modeChanged(false);
        }
      });
    }
    return modeBox;
  }

  private JCheckBox getAUBox() {
    if (fromAUBox == null) {
      fromAUBox = new JCheckBox();
      fromAUBox.setText("SP auf AU");
      fromAUBox.setToolTipText("SchadensPunkte von der AUsdauer abziehen");
      fromAUBox.setBounds(new Rectangle(160, 280, 130, 21));
      fromAUBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          currentHero.setFightUsesAU(fromAUBox.isSelected());
          updateData();
        }
      });
    }
    return fromAUBox;
  }

  private void disableSecondHand() {
    secondHandLabel.setEnabled(false);
    hand2Box.setEnabled(false);
    attack2Button.setEnabled(false);
    parade2Button.setEnabled(false);
    at2Label.setText("-");
    pa2Label.setText("-");
    tp2Label.setText("-");
    bf2Label.setText("-");
  }
  
  private boolean fireActiveWeaponChange = true;

  protected void modeChanged(boolean initializing) {
    if (!listen && !initializing) return;
    Object mode = modeBox.getSelectedItem();
    if (mode == null) return;
    fireActiveWeaponChange = false;
    attack1Button.setEnabled(Fighting.canAttack(currentHero));
    parade1Button.setEnabled(Fighting.canDefend(currentHero));
    evadeButton.setEnabled(Fighting.canDefend(currentHero));
    hand1Box.removeAllItems();
    hand2Box.removeAllItems();
    beLabel.setText("" + currentHero.getBE());
    currentHero.setFightMode(mode.toString());
    for (String s : Fighting.getPossibleItems(currentHero, mode.toString(), 0)) {
      hand1Box.addItem(s);
    }
    hand1Box.setEnabled(true);
    hand1Box.setSelectedIndex(0);
    for (String s : Fighting.getPossibleItems(currentHero, mode.toString(), 1)) {
      hand2Box.addItem(s);
    }
    if (mode.equals("Waffenlos")) {
      firstHandLabel.setText("Kampftechnik:");
      disableSecondHand();
    }
    else if (mode.equals("Eine Waffe")) {
      firstHandLabel.setText("Erste Hand:");
      disableSecondHand();
    }
    else if (mode.toString().startsWith("Waffe + Parade")) {
      firstHandLabel.setText("Erste Hand:");
      hand2Box.setSelectedIndex(0);
      secondHandLabel.setEnabled(true);
      hand2Box.setEnabled(true);
      attack2Button.setEnabled(false);
      parade2Button.setEnabled(mode.toString().endsWith("separat")
          && Fighting.canDefend(currentHero));
    }
    else if (mode.equals("Zwei Waffen")) {
      firstHandLabel.setText("Erste Hand:");
      hand2Box.setSelectedIndex(0);
      secondHandLabel.setEnabled(true);
      hand2Box.setEnabled(true);
      int skill = currentHero.getCurrentTalentValue("Linkshändig");
      boolean earlyLeftHand = Group.getInstance().getOptions()
          .isEarlyTwoHanded();
      int attackBorder = earlyLeftHand ? 6 : 14;
      int paradeBorder = earlyLeftHand ? 9 : 14;
      attack2Button.setEnabled(Fighting.canAttack(currentHero) && skill >= attackBorder);
      parade2Button.setEnabled(skill >= paradeBorder && Fighting.canDefend(currentHero));
    }
    else if (mode.equals("Zweihandwaffe")) {
      firstHandLabel.setText("Waffe:");
      disableSecondHand();
    }
    else { // "Fernkampf"
      firstHandLabel.setText("Waffe:");
      disableSecondHand();
      parade1Button.setEnabled(false);
    }
    fireActiveWeaponChange = true;
    enableWVControls();
    currentHero.fireActiveWeaponsChanged();
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getHand1Box() {
    if (hand1Box == null) {
      hand1Box = new JComboBox();
      hand1Box.setBounds(new Rectangle(120, 70, 171, 21));
      hand1Box.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hand1Changed();
        }
      });
    }
    return hand1Box;
  }

  private void setFirstAT() {
    int opponentModifier = 0;
    int ownModifier = 0;
    if (Fighting.canAttack(currentHero)) {
      Optional<Integer> at = currentHero.getAT(0);
      if (at.hasValue()) {
        int modAT = Fighting.getWVModifiedAT(at.getValue(), hand1Box.getSelectedItem().toString(), 
          opponent1Box.getSelectedItem().toString());
        at1Label.setText("" + modAT);
        opponentModifier = 20 - Fighting.getWVModifiedPA(20, 
            hand1Box.getSelectedItem().toString(), 
            opponent1Box.getSelectedItem().toString());
        ownModifier = at.getValue() - modAT;
      }
      else {
        at1Label.setText("" + at);
      }
    }
    else {
      at1Label.setText("-");
    }
    String text = null;
    if (Group.getInstance().getOptions().useWV()) {
      if (ownModifier != 0) {
        text = "<html>AT wird durch WV um " + ownModifier + " erniedrigt<br />";
      }
      else {
        text = "<html>AT wird durch WV nicht erniedrigt<br />";
      }
      if (opponentModifier != 0) {
        text += "PA des Gegners wird durch WV um " + opponentModifier + " erniedrigt</html>";
      }
      else {
        text += "PA des Gegners wird durch WV nicht erniedrigt</html>";
      }
    }
    at1Label.setToolTipText(text);
  }

  private void setFirstPA() {
    int opponentModifier = 0;
    int ownModifier = 0;
    Optional<Integer> pa = currentHero.getPA(0);
    if (pa.hasValue()) {
      int modPA = Fighting.getWVModifiedPA(pa.getValue(), opponent1Box.getSelectedItem().toString(), 
          hand1Box.getSelectedItem().toString());
      pa1Label.setText("" + modPA);
      opponentModifier = 20 - Fighting.getWVModifiedAT(20, 
          opponent1Box.getSelectedItem().toString(), 
          hand1Box.getSelectedItem().toString());
      ownModifier = pa.getValue() - modPA;
    }
    else {
      pa1Label.setText("" + currentHero.getPA(0));
    }
    String text = null;
    if (Group.getInstance().getOptions().useWV()) {
      if (ownModifier != 0) {
        text = "<html>PA wird durch WV um " + ownModifier + " erniedrigt<br />";
      }
      else {
        text = "<html>PA wird durch WV nicht erniedrigt<br />";
      }
      if (opponentModifier != 0) {
        text += "AT des Gegners wird durch WV um " + opponentModifier + " erniedrigt</html>";
      }
      else {
        text += "AT des Gegners wird durch WV nicht erniedrigt</html>";
      }
    }
    pa1Label.setToolTipText(text);
  }

  private void setSecondAT() {
    int opponentModifier = 0;
    int ownModifier = 0;
    if (Fighting.canAttack(currentHero)) {
      Optional<Integer> at = currentHero.getAT(1);
      if (at.hasValue()) {
        int modAT = Fighting.getWVModifiedAT(at.getValue(), hand2Box.getSelectedItem().toString(), 
          opponent2Box.getSelectedItem().toString());
        at2Label.setText("" + modAT);
        opponentModifier = 20 - Fighting.getWVModifiedPA(20, 
            hand2Box.getSelectedItem().toString(), 
            opponent2Box.getSelectedItem().toString());
        ownModifier = at.getValue() - modAT;
      }
      else {
        at2Label.setText("" + at);
      }
    }
    else {
      at2Label.setText("-");
    }
    String text = null;
    if (Group.getInstance().getOptions().useWV()) {
      if (ownModifier != 0) {
        text = "<html>AT wird durch WV um " + ownModifier + " erniedrigt<br />";
      }
      else {
        text = "<html>AT wird durch WV nicht erniedrigt<br />";
      }
      if (opponentModifier != 0) {
        text += "PA des Gegners wird durch WV um " + opponentModifier + " erniedrigt</html>";
      }
      else {
        text += "PA des Gegners wird durch WV nicht erniedrigt</html>";
      }
    }
    at2Label.setToolTipText(text);
  }

  private void setSecondPA() {
    int opponentModifier = 0;
    int ownModifier = 0;
    Optional<Integer> pa = currentHero.getPA(1);
    if (pa.hasValue()) {
      int modPA = Fighting.getWVModifiedPA(pa.getValue(), opponent2Box.getSelectedItem().toString(), 
          hand2Box.getSelectedItem().toString());
      ownModifier = pa.getValue() - modPA;
      pa2Label.setText("" + modPA);
      opponentModifier = 20 - Fighting.getWVModifiedAT(20, 
          opponent2Box.getSelectedItem().toString(), 
          hand2Box.getSelectedItem().toString());
    }
    else {
      pa2Label.setText("" + currentHero.getPA(1));
    }
    String text = null;
    if (Group.getInstance().getOptions().useWV()) {
      if (ownModifier != 0) {
        text = "<html>PA wird durch WV um " + ownModifier + " erniedrigt<br />";
      }
      else {
        text = "<html>PA wird durch WV nicht erniedrigt<br />";
      }
      if (opponentModifier != 0) {
        text += "AT des Gegners wird durch WV um " + opponentModifier + " erniedrigt</html>";
      }
      else {
        text += "AT des Gegners wird durch WV nicht erniedrigt</html>";
      }
    }
    pa2Label.setToolTipText(text);
  }

  private void setFirstWeaponData(String s) {
    setFirstAT();
    setFirstPA();
    tp1Label.setText(Fighting.getFirstTP(currentHero).toString());
    bf1Label.setText("" + currentHero.getBF(s, 1));
    tp1Label.setToolTipText("KK-Bonus: " + Fighting.getFirstKKBonus(currentHero));
  }

  protected void hand1Changed() {
    if (!listen) return;
    Object mode = modeBox.getSelectedItem();
    if (mode == null) return;
    Object o = hand1Box.getSelectedItem();
    if (o == null) return;
    String s = o.toString();
    currentHero.setFirstHandWeapon(s);
    if (mode.equals("Waffenlos")) {
      setFirstAT();
      setFirstPA();
      tp1Label.setText(Fighting.getFirstTP(currentHero).toString());
      bf1Label.setText("-");
    }
    else if (mode.equals("Fernkampf")) {
      Optional<Integer> skill = currentHero.getAT(0);
      if (skill.hasValue() && Fighting.canAttack(currentHero)) {
        at1Label.setText("" + skill.getValue());
      }
      else {
        at1Label.setText("-");
      }
      pa1Label.setText("-");
      tp1Label.setText(Fighting.getFirstTP(currentHero).toString());
      bf1Label.setText("" + currentHero.getBF(s, 1));
    }
    else {
      setFirstWeaponData(s);
    }
    if (fireActiveWeaponChange) currentHero.fireActiveWeaponsChanged();
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getHand2Box() {
    if (hand2Box == null) {
      hand2Box = new JComboBox();
      hand2Box.setBounds(new Rectangle(120, 160, 171, 21));
      hand2Box.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          hand2Changed();
        }
      });
    }
    return hand2Box;
  }

  protected void hand2Changed() {
    if (!listen) return;
    Object mode = modeBox.getSelectedItem();
    if (mode == null) return;
    Object o = hand2Box.getSelectedItem();
    if (o == null) return;
    String s = o.toString();
    currentHero.setSecondHandItem(s);
    if (mode.equals("Waffe + Parade")) {
      hand1Changed();
      Shield shield = Shields.getInstance().getShield(s);
      int beMod = shield.getBeMod();
      int be = currentHero.getBE() + beMod;
      beLabel.setText("" + be);
      at2Label.setText("-");
      pa2Label.setText("-");
      tp2Label.setText("-");
      tp2Label.setToolTipText(null);
    }
    else if (mode.equals("Waffe + Parade, separat")) {
      at2Label.setText("-");
      pa2Label.setText("" + currentHero.getPA(1));
      int be = 0;
      tp2Label.setText("-");
      tp2Label.setToolTipText(null);
      Shield shield = Shields.getInstance().getShield(s);
      bf2Label.setText("" + currentHero.getBF(s));
      int beMod = shield.getBeMod();
      be = currentHero.getBE() + beMod;
      beLabel.setText("" + be);
    }
    else { // "Zwei Waffen"
      setSecondAT();
      setSecondPA();
      tp2Label.setText("" + Fighting.getSecondTP(currentHero));
      bf2Label.setText("" + currentHero.getBF(s, 1));
      tp2Label.setToolTipText("KK-Bonus: " + Fighting.getSecondKKBonus(currentHero));
    }
    if (fireActiveWeaponChange) currentHero.fireActiveWeaponsChanged();
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getAttack1Button() {
    if (attack1Button == null) {
      attack1Button = new JButton();
      attack1Button.setBounds(new Rectangle(30, 310, 41, 21));
      attack1Button.setIcon(ImageManager.getIcon("attack1"));
      attack1Button.setToolTipText("Attacke mit erster Hand");
      attack1Button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doAttack1();
        }
      });
    }
    return attack1Button;
  }

  protected void doAttack1() {
    String mode = modeBox.getSelectedItem().toString();
    if (mode.equals("Fernkampf")) {
      doProjectileAttack();
    }
    else if (mode.equals("Waffenlos")) {
      doWeaponlessAttack();
    }
    else {
      doAttack(true);
    }
    currentHero.setAT1Bonus(0);
  }

  private void doAttack(boolean weapon1) {
    try {
      int atValue = Integer.parseInt(weapon1 ? at1Label.getText() : at2Label
          .getText());
      int at = atValue;
      if (currentHero.isGrounded()) at -= 7;
      boolean stumbled = currentHero.hasStumbled();
      if (stumbled) {
        at -= 5;
        currentHero.setHasStumbled(false);
      }
      String tpS = weapon1 ? tp1Label.getText() : tp2Label.getText();
      DiceSpecification ds = DiceSpecification.parse(tpS);
      int atbonus = weapon1 ? currentHero.getAT1Bonus() : currentHero
          .getAT2Bonus();
      at += atbonus;
      int atRoll = Dice.roll(20);
      if (atRoll == 20) {
        doFumble(weapon1);
      }
      else {
        String s = (atRoll == 1 ? "Perfekte Attacke!\n" : "");
        s += "AT-Wert: " + atValue + "\n";
        if (atbonus != 0) s += "Bonus aus letzter Runde: " + atbonus + "\n";
        if (currentHero.isGrounded()) s += "Am Boden: -7\n";
        if (stumbled) s += "Gestolpert: -5\n";
        s += "Würfelwurf: " + atRoll + "\n";
        if (withMarkers) {
          s += "Abzug durch Marker: " + Markers.getMarkers(currentHero) + "\n";
        }
        int q = 0;
        boolean hit = true;
        if (weapon1 || currentHero.getCurrentTalentValue("Linkshändig") >= 10) {
          q = (at - atRoll - Markers.getMarkers(currentHero)) / 2;
          s += "Qualität: " + q + "\n";
        }
        else {
          if (atRoll <= at - Markers.getMarkers(currentHero))
            s += "Getroffen!\n";
          else {
            s += "Nicht getroffen.\n";
            hit = false;
          }
        }
        if (hit) {
          int tp = ds.calcValue() + q;
          if (atRoll == 1) tp += Dice.roll(6);
          if (tp < 0) tp = 0;
          s += "Trefferpunkte: " + tp;
        }
        ImageIcon icon = ImageManager.getIcon(weapon1 ? "attack1" : "attack2");
        ProbeResultDialog.showDialog(this, s, "Attacke", icon);
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  private void doWeaponlessAttack() {
    try {
      int atValue = Integer.parseInt(at1Label.getText());
      int at = atValue;
      if (currentHero.isGrounded()) at -= 7;
      int atbonus = currentHero.getAT1Bonus();
      at += atbonus;
      boolean stumbled = currentHero.hasStumbled();
      if (stumbled) {
        at -=5;
        currentHero.setHasStumbled(false);
      }
      int atRoll = Dice.roll(20);
      if (atRoll == 20) {
        doFumble(true);
      }
      else {
        String s = (atRoll == 1 ? "Perfekte Attacke!\n" : "");
        s += "AT-Wert: " + atValue + "\n";
        if (atbonus != 0) s += "Bonus aus letzter Runde: " + atbonus + "\n";
        if (currentHero.isGrounded()) s += "Am Boden: -7\n";
        if (stumbled){
          s += "Gestolpert: -5\n";
        }
        if (withMarkers) {
          s += "Abzug durch Marker: " + currentHero.getMarkers() + "\n";
        }
        s += "Würfelwurf: " + atRoll + "\n";
        int q = 0;
        q = (at - atRoll - Markers.getMarkers(currentHero)) / 2;
        s += "Qualität: " + q + "\n";
        s += calcWeaponlessTP(atRoll == 1, q);
        ImageIcon icon = ImageManager.getIcon("attack1");
        ProbeResultDialog.showDialog(this, s, "Attacke", icon);
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  private String calcWeaponlessTP(boolean perfect, int q) {
    String mode = hand1Box.getSelectedItem().toString();
    String s = "";
    if (mode.equals("Raufen")) {
      int tp = Dice.roll(6);
      if (perfect) tp += Dice.roll(6);
      tp += q;
      if (tp < 0) tp = 0;
      s = "Trefferpunkte: " + tp;
    }
    else if (mode.equals("Boxen")) {
      int tp = Dice.roll(6);
      boolean again = false;
      if (tp == 6) again = true;
      if (perfect) {
        int tp2 = Dice.roll(6);
        if (tp2 == 6) again = true;
        tp += tp2;
      }
      if (again) {
        int tp2 = Dice.roll(6);
        if (tp2 == 6) {
          JOptionPane.showMessageDialog(this, "Lucky Punch! "
              + currentHero.getName() + " hat den Gegner k.o. geschlagen.",
              "Attacke", JOptionPane.PLAIN_MESSAGE);
        }
        tp += tp2;
      }
      tp += q;
      if (tp < 0) tp = 0;
      s = "Trefferpunkte: " + tp;
    }
    else if (mode.equals("Ringen")) {
      int tp = Dice.roll(6);
      if (perfect) tp += Dice.roll(6);
      tp += q;
      if (tp < 0) tp = 0;
      if (perfect) {
        s += "Der Gegner ist niedergerungen.\n";
      }
      s += "Trefferpunkte: " + tp;
    }
    else { // Hruruzat
      boolean canTurn = perfect;
      int tp1 = 0;
      int tp2 = 0;
      int tp = 0;
      do {
        tp1 = Dice.roll(6);
        tp2 = Dice.roll(6);
        if (canTurn && (tp2 == tp1 + 1 || tp2 == tp1 - 1)) {
          canTurn = false;
          if (tp2 == tp1 + 1)
            tp1++;
          else
            tp2++;
        }
        tp += tp1 + tp2;
        if (tp1 == tp2) {
          JOptionPane.showMessageDialog(this, "Zat! Zwei mal die " + tp1 + ".",
              "Attacke", JOptionPane.PLAIN_MESSAGE);
        }
      } while (tp1 == tp2);
      tp += q;
      if (tp < 0) tp = 0;
      s = "Trefferpunkte: " + tp;
    }
    return s;
  }

  private void doProjectileAttack() {
    Weapon w = Weapons.getInstance().getWeapon(
        hand1Box.getSelectedItem().toString());
    int at = 0;
    try {
      at = Integer.parseInt(at1Label.getText());
    }
    catch (NumberFormatException e) {
      at = 0;
    }
    ProjectileAttackDialog dialog = new ProjectileAttackDialog(
        this, at, w, currentHero.getFarRangedFightParams());
    dialog.setVisible(true);
    if (dialog.wasCanceled()) return;
    at = dialog.getATValue();
    int mod = dialog.getModifier();
    int roll = Dice.roll(20);
    String s = "AT-Wert: " + at + "\n";
    s += "Zuschlag: " + mod + "\n";
    if (withMarkers) {
      s += "Zuschlag durch Marker: " + Markers.getMarkers(currentHero) + "\n";
    }
    if (currentHero.hasStumbled()) {
      s += "Zuschlag durch Solpern: 5\n";
      currentHero.setHasStumbled(false);
    }
    s += "Wurf: " + roll + "\n";
    if (roll == 1 || roll <= at - mod - Markers.getMarkers(currentHero)) {
      if (roll == 1)
        s += "Perfekt g";
      else
        s += "G";
      s += "etroffen! -- Qualität ";
      int q = (at - mod - Markers.getMarkers(currentHero) - roll) / 2;
      s += q + "\n";
      String tps = tp1Label.getText();
      DiceSpecification ds = DiceSpecification.parse(tps);
      int tp = ds.calcValue();
      if (roll == 1) tp += Dice.roll(6);
      tp += w.getDistanceTPMod(dialog.getDistance());
      tp += q;
      s += "Trefferpunkte: " + tp;
    }
    else
      s += "Daneben.";
    ProbeResultDialog.showDialog(this, s, "Attacke");
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getParade1Button() {
    if (parade1Button == null) {
      parade1Button = new JButton();
      parade1Button.setBounds(new Rectangle(90, 310, 41, 21));
      parade1Button.setIcon(ImageManager.getIcon("defense1"));
      parade1Button.setToolTipText("Parade mit erster Hand");
      parade1Button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          parade1();
        }
      });
    }
    return parade1Button;
  }

  protected void parade1() {
    try {
      int paValue = Integer.parseInt(pa1Label.getText());
      int bonus = doParade(paValue, true);
      currentHero.setAT1Bonus(bonus);
    }
    catch (NumberFormatException e) {
      return;
    }
  }

  protected void parade2() {
    try {
      int paValue = Integer.parseInt(pa2Label.getText());
      int bonus = doParade(paValue, false);
      currentHero.setAT2Bonus(bonus);
    }
    catch (NumberFormatException e) {
      return;
    }
  }

  private int doParade(int paValue, boolean weapon1) {
    String iconName = weapon1 ? "defense1" : "defense2";
    ImageIcon icon = ImageManager.getIcon(iconName);
    Integer[] quals = new Integer[23];
    for (int i = -9; i < 13; ++i) {
      quals[i + 9] = i;
    }
    Integer q = (Integer) JOptionPane.showInputDialog(this, "AT-Qualität:",
        "Parade", JOptionPane.PLAIN_MESSAGE, icon, quals, quals[9]);
    if (q == null) return 0;
    if (currentHero.isGrounded()) q += 5;
    if (currentHero.hasStumbled()) {
      q += 5;
      currentHero.setHasStumbled(false);
    }
    int d = Dice.roll(20);
    if (d == 20) {
      doFumble(weapon1);
      return 0;
    }
    else if (d == 1) {
    	ProbeResultDialog.showDialog(this, "Mit einer 1 perfekt pariert!",
          "Parade", icon);
      return (paValue - q - Markers.getMarkers(currentHero)) / 2;
    }
    else if (d <= paValue - q - Markers.getMarkers(currentHero)) {
    	ProbeResultDialog.showDialog(this, "Mit einer " + d + " pariert.",
          "Parade", icon);
      return 0;
    }
    else {
      ProbeResultDialog.showDialog(this, "Mit einer " + d + " nicht pariert.",
			"Parade", icon);
      int tp = -1;
      while (tp < 0) {
        Object temp = JOptionPane.showInputDialog(this, "Trefferpunkte:", "Parade",
            JOptionPane.PLAIN_MESSAGE, icon, null, "");
        if (temp == null) return 0;
        String text = temp.toString();
        try {
          tp = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "Bitte eine Zahl >= 0 eingeben.",
              "Parade", JOptionPane.ERROR_MESSAGE);
        }
      }
      doHit(tp);
      return 0;
    }
  }

  private void doFumble(boolean weapon1) {
    String mode = modeBox.getSelectedItem().toString();
    boolean withWeapon = !mode.equals("Waffenlos");
    int d = 0;
    do {
      d = Dice.roll(6) + Dice.roll(6);
    } while (!withWeapon && d < 6);

    if (d == 2) {
      int r = Dice.roll(20);
      if (r <= currentHero.getCurrentProperty(Property.KK) - 7) {
        ProbeResultDialog
            .showDialog(
                this,
                "Patzer! Beinahe die Waffe verloren, aber der Rettungswurf ist geglückt.",
                "Patzer");
      }
      else {
    	  ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " verliert "
            + (currentHero.getSex().startsWith("m") ? "seine" : "ihre")
            + " Waffe!", "Patzer");
        int bfRoll = Dice.roll(6) + Dice.roll(6);
        String weapon = weapon1 ? hand1Box.getSelectedItem().toString()
            : hand2Box.getSelectedItem().toString();
        int bf = 0;
        if (weapon1 || !mode.equals("Waffe + Parade, separat")) {
          bf = currentHero.getBF(weapon, 1);
        }
        else {
          bf = currentHero.getBF(weapon);
        }
        if (bfRoll <= bf) {
          breakWeapon(weapon1);
        }
        else {
          if (bfRoll < 10) ++bf;
          bfRoll = Dice.roll(6) + Dice.roll(6);
          if (bfRoll <= bf) {
            breakWeapon(weapon1);
          }
          else {
            if (bfRoll < 10) ++bf;
            if (weapon1 || !mode.equals("Waffe + Parade, separat")) {
              currentHero.setBF(weapon, 1, bf);
            }
            else {
              currentHero.setBF(weapon, bf);
            }
            if (weapon1)
              bf1Label.setText("" + bf);
            else
              bf2Label.setText("" + bf);
            ProbeResultDialog.showDialog(this, "Die Waffe bleibt ganz.",
                "Patzer");
          }
        }
      }
    }
    else if (d == 3) {
      int r = Dice.roll(20);
      if (r <= currentHero.getCurrentProperty(Property.KK) - 7
          - Markers.getMarkers(currentHero)) {
    	  ProbeResultDialog.showDialog(
                this,
                "Patzer! Beinahe die Waffe verloren, aber der Rettungswurf ist geglückt.",
                "Patzer");
      }
      else {
    	  ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " verliert "
            + (currentHero.getSex().startsWith("m") ? "seine" : "ihre")
            + " Waffe!", "Patzer");
      }
    }
    else if (d <= 5) {
      int bfRoll = Dice.roll(6) + Dice.roll(6);
      String weapon = weapon1 ? hand1Box.getSelectedItem().toString()
          : hand2Box.getSelectedItem().toString();
      ProbeResultDialog.showDialog(this, "Patzer! Die Waffe ist beschädigt.",
          "Patzer");
      int bf = 0;
      if (weapon1 || !mode.equals("Waffe + Parade, separat")) {
        bf = currentHero.getBF(weapon, 1);
      }
      else {
        bf = currentHero.getBF(weapon);
      }
      if (bfRoll <= bf) {
        breakWeapon(weapon1);
      }
      else {
        if (bfRoll < 10) ++bf;
        if (weapon1 || !mode.equals("Waffe + Parade, separat")) {
          currentHero.setBF(weapon, 1, bf);
        }
        else {
          currentHero.setBF(weapon, bf);
        }
        if (weapon1)
          bf1Label.setText("" + bf);
        else
          bf2Label.setText("" + bf);
        ProbeResultDialog.showDialog(this, "Die Waffe bleibt ganz.", "Patzer");
      }
    }
    else if (d <= 8) {
      int r = Dice.roll(20);
      int be = 0;
      try {
        be = Integer.parseInt(beLabel.getText());
      }
      catch (NumberFormatException e) {
        be = 0;
      }
      if (r <= currentHero.getCurrentProperty(Property.GE) - be
          - Markers.getMarkers(currentHero)) {
    	  ProbeResultDialog.showDialog(this, "Patzer! Beinahe wäre "
            + currentHero.getName() + " gestolpert.", "Parade");
      }
      else {
    	  ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " stolpert!", "Patzer");
        currentHero.setHasStumbled(true);
      }
    }
    else if (d <= 10) {
      int r = Dice.roll(20);
      int be = 0;
      try {
        be = Integer.parseInt(beLabel.getText());
      }
      catch (NumberFormatException e) {
        be = 0;
      }
      if (r <= currentHero.getCurrentProperty(Property.GE) - be
          - Markers.getMarkers(currentHero)) {
    	  ProbeResultDialog.showDialog(this, "Patzer! Beinahe wäre "
            + currentHero.getName() + " gestürzt.", "Patzer");
      }
      else {
    	  ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " stürzt!", "Patzer");
        currentHero.setGrounded(true);
        groundBox.setSelected(true);
      }
    }
    else if (d == 11) {
      int r = Dice.roll(20);
      int be = 0;
      try {
        be = Integer.parseInt(beLabel.getText());
      }
      catch (NumberFormatException e) {
        be = 0;
      }
      if (r <= currentHero.getCurrentProperty(Property.GE) - be
          - Markers.getMarkers(currentHero)) {
    	  ProbeResultDialog.showDialog(this, "Patzer! Beinahe hätte "
            + currentHero.getName() + " sich selbst verletzt.", "Patzer");
      }
      else {
        ImageIcon icon = ImageManager.getIcon("hit");
        int damage = Dice.roll(6);
        ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " verletzt sich selbst!\n" + damage + " Schadenspunkte.",
            "Patzer", icon);
        doHit(damage + currentHero.getRS());
      }
    }
    else {
      int r = Dice.roll(20);
      int be = 0;
      try {
        be = Integer.parseInt(beLabel.getText());
      }
      catch (NumberFormatException e) {
        be = 0;
      }
      if (r <= currentHero.getCurrentProperty(Property.GE) - be
          - Markers.getMarkers(currentHero)) {
    	  ProbeResultDialog.showDialog(this, "Patzer! Beinahe hätte "
            + currentHero.getName() + " sich selbst schwer verletzt.",
            "Patzer");
      }
      else {
        ImageIcon icon = ImageManager.getIcon("hit");
        int damage = Dice.roll(6) + Dice.roll(6);
        ProbeResultDialog.showDialog(this, "Patzer! " + currentHero.getName()
            + " verletzt sich selbst schwer!\n" + damage + " Schadenspunkte.",
            "Patzer", icon);
        doHit(damage + currentHero.getRS());
      }
    }
  }

  private void breakWeapon(boolean weapon1) {
    String weapon = weapon1 ? hand1Box.getSelectedItem().toString() : hand2Box
        .getSelectedItem().toString();
    ProbeResultDialog.showDialog(this, "Die Waffe zerbricht!", "Patzer");
    String mode = modeBox.getSelectedItem().toString();
    if (!weapon1 && !mode.equals("Zwei Waffen")) {
      currentHero.removeShield(weapon);
    }
    else {
      currentHero.removeWeapon(weapon);
    }
  }

  private void doHit(int tp) {
    Fighting.doHit(currentHero, tp, useAU, true, this, new Fighting.UpdateCallbacks() {
      public void updateData() {
        FightFrame.this.updateData();
      }
    });
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getAttack2Button() {
    if (attack2Button == null) {
      attack2Button = new JButton();
      attack2Button.setBounds(new Rectangle(160, 310, 41, 21));
      attack2Button.setIcon(ImageManager.getIcon("attack2"));
      attack2Button.setToolTipText("Attacke mit zweiter Hand");
      attack2Button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doAttack(false);
          currentHero.setAT2Bonus(0);
        }
      });
    }
    return attack2Button;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getParade2Button() {
    if (parade2Button == null) {
      parade2Button = new JButton();
      parade2Button.setBounds(new Rectangle(220, 310, 41, 21));
      parade2Button.setIcon(ImageManager.getIcon("defense2"));
      parade2Button.setToolTipText("Parade mit zweiter Hand");
      parade2Button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          parade2();
        }
      });
    }
    return parade2Button;
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    currentHero = newCharacter;
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero)
      character.removeHeroObserver(myCharacterObserver);
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }

  private final CharacterObserver myCharacterObserver = new MyCharacterObserver();

  private JCheckBox groundBox = null;

  private JButton evadeButton = null;

  private JButton hitButton = null;

  private JCheckBox dazedBox = null;

  private JLabel jLabel13 = null;

  private JSpinner markerSpinner = null;

  private class MyCharacterObserver extends CharacterAdapter {
    public void weightChanged() {
      updateData();
    }

    public void atPADistributionChanged(String talent) {
      updateData();
    }

    public void derivedValueChanged(Hero.DerivedValue dv) {
      updateData();
    }

    public void currentPropertyChanged(Property property) {
      updateData();
    }

    public void currentEnergyChanged(Energy energy) {
      if (!listen) return;
      updateData();
    }

    public void currentTalentChanged(String talent) {
      Talent t = Talents.getInstance().getTalent(talent);
      if (t.isFightingTalent()) updateData();
    }

    public void beModificationChanged() {
      updateData();
    }
    
    public void thingsChanged() {
      updateData();
    }
    
    public void fightingStateChanged() {
      if (!listen) return;
      updateData();
    }
    
    public void activeWeaponsChanged() {
      if (!listen) return;
      updateData();
    }
    
    public void opponentWeaponChanged(int weaponNr) {
      if (weaponNr == 0) {
        opponent1Box.setEditable(true);
        opponent1Box.setSelectedItem(currentHero.getOpponentWeapon(weaponNr));
        opponent1Box.setEditable(false);
      }
      else if (weaponNr == 1) {
        opponent2Box.setEditable(true);
        opponent2Box.setSelectedItem(currentHero.getOpponentWeapon(weaponNr));
        opponent2Box.setEditable(false);        
      }
    }
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getGroundBox() {
    if (groundBox == null) {
      groundBox = new JCheckBox();
      groundBox.setBounds(new Rectangle(10, 250, 121, 21));
      groundBox.setText("am Boden");
      groundBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          groundedClicked();
        }
      });
    }
    return groundBox;
  }

  private void groundedClicked() {
    listen = false;
    if (groundBox.isSelected()) {
      currentHero.setGrounded(true);
    }
    else {
      int value = currentHero.getCurrentProperty(Property.GE);
      int mod = 0;
      try {
        mod = Integer.parseInt(beLabel.getText());
      }
      catch (NumberFormatException e) {
        mod = 0;
      }
      int roll = Dice.roll(20);
      if (roll <= value - mod - Markers.getMarkers(currentHero)) {
        JOptionPane.showMessageDialog(this, "Probe auf GE+BE mit einer " + roll
            + " bestanden!", "Aufstehen", JOptionPane.PLAIN_MESSAGE);
        currentHero.setGrounded(false);
      }
      else {
        if (JOptionPane.showConfirmDialog(this, "Probe auf GE+BE mit einer "
            + roll + " nicht bestanden.\n" + "Trotzdem ändern?", "Aufstehen",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          currentHero.setGrounded(false);
        }
        else {
          groundBox.setSelected(true);
        }
      }
    }
    listen = true;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getEvadeButton() {
    if (evadeButton == null) {
      evadeButton = new JButton();
      evadeButton.setBounds(new Rectangle(60, 340, 41, 21));
      evadeButton.setToolTipText("Ausweichen");
      evadeButton.setIcon(ImageManager.getIcon("evade"));
      evadeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doEvade();
        }
      });
    }
    return evadeButton;
  }

  protected void doEvade() {
    String iconName = "evade";
    ImageIcon icon = ImageManager.getIcon(iconName);
    Integer[] quals = new Integer[23];
    for (int i = -9; i < 13; ++i) {
      quals[i + 9] = i;
    }
    Integer q = (Integer) JOptionPane.showInputDialog(this, "AT-Qualität:",
        "Parade", JOptionPane.PLAIN_MESSAGE, icon, quals, quals[9]);
    if (q == null) return;
    if (currentHero.isGrounded()) q += 5;
    int maxBonus = currentHero.getCurrentDerivedValue(Hero.DerivedValue.AB);
    Integer[] boni = new Integer[maxBonus + 1];
    for (int i = 0; i <= maxBonus; ++i)
      boni[i] = i;
    Integer b = (Integer) JOptionPane.showInputDialog(this,
        "Ausweichen-Bonus:", "Ausweichen", JOptionPane.PLAIN_MESSAGE, icon,
        boni, boni[0]);
    if (b == null) return;
    int aw = currentHero.getCurrentDerivedValue(Hero.DerivedValue.PA);
    int be = Math.round((float) currentHero.getBE() / 2.0f);
    int d = Dice.roll(20);
    if (d == 20) {
      doFumble(true);
    }
    else if (d == 1) {
      ProbeResultDialog.showDialog(this, "Mit einer 1 perfekt ausgewichen!",
          "Ausweichen", icon);
    }
    else if (d <= aw + b - q - be - Markers.getMarkers(currentHero)) {
    	ProbeResultDialog.showDialog(this, "Mit einer " + d + " ausgewichen.",
          "Ausweichen", icon);
    }
    else {
      ProbeResultDialog.showDialog(this, "Mit einer " + d + " nicht ausgewichen.",
    		  "Ausweichen", icon);
      int tp = -1;
      while (tp < 0) {
        Object temp = JOptionPane.showInputDialog(this, "Trefferpunkte:", "Ausweichen",
            JOptionPane.PLAIN_MESSAGE, icon, null, "");
        if (temp == null) return;
        String text = temp.toString();
        try {
          tp = Integer.parseInt(text);
        }
        catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(this, "Bitte eine Zahl >= 0 eingeben.",
              "Parade", JOptionPane.ERROR_MESSAGE);
        }
      }
      doHit(tp);
    }
    currentHero.setAT1Bonus(-2 * b);
    currentHero.setAT2Bonus(-2 * b);
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getHitButton() {
    if (hitButton == null) {
      hitButton = new JButton();
      hitButton.setBounds(new Rectangle(190, 340, 41, 21));
      hitButton.setIcon(ImageManager.getIcon("hit"));
      hitButton.setToolTipText("Treffer hinnehmen (ohne Verteidigung)");
      hitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doHit();
        }
      });
    }
    return hitButton;
  }

  protected void doHit() {
    int tp = -1;
    ImageIcon icon = ImageManager.getIcon("hit");
    while (tp < 0) {
      Object temp = JOptionPane.showInputDialog(this, "Trefferpunkte:",
          "Treffer", JOptionPane.PLAIN_MESSAGE, icon, null, "");
      if (temp == null) return;
      String text = temp.toString();
      try {
        tp = Integer.parseInt(text);
      }
      catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Bitte eine Zahl >= 0 eingeben.",
            "Parade", JOptionPane.ERROR_MESSAGE);
        tp = -1;
      }
    }
    doHit(tp);
  }
  
  private void enableWVControls() {
    boolean withWV = currentHero != null && Group.getInstance().getOptions().useWV();
    String mode = modeBox.getSelectedItem() != null ? modeBox.getSelectedItem().toString() : "Eine Waffe";
    if ("Eine Waffe".equals(mode) || mode.startsWith("Waffe +") || 
        "Zwei Waffen".equals(mode) || "Waffenlos".equals(mode) ||
        "Zweihandwaffe".equals(mode)) {
      opponent1Label.setEnabled(withWV);
      opponent1Box.setEnabled(withWV);
    }
    else {
      opponent1Label.setEnabled(false);
      opponent1Box.setEnabled(false);
    }
    if ("Zwei Waffen".equals(modeBox.getSelectedItem())) {
      opponent2Label.setEnabled(withWV);
      opponent2Box.setEnabled(withWV);
    }
    else {
      opponent2Label.setEnabled(false);
      opponent2Box.setEnabled(false);
    }
  }

  public void optionsChanged() {
    updateData();
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getDazedBox() {
    if (dazedBox == null) {
      dazedBox = new JCheckBox();
      dazedBox.setBounds(new Rectangle(160, 250, 121, 21));
      dazedBox.setText("benommen");
      dazedBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dazedClicked();
        }
      });
    }
    return dazedBox;
  }

  private void dazedClicked() {
    boolean dazed = dazedBox.isSelected();
    if (dazed) {
      currentHero.setDazed(true);
      updateData();
    }
    else {
      int ko = currentHero.getCurrentEnergy(Energy.KO);
      int roll = Dice.roll(20);
      if (roll <= ko - Markers.getMarkers(currentHero)) {
        JOptionPane.showMessageDialog(this, "KO-Probe mit einer " + roll
            + " geglückt!", "Benommenheit", JOptionPane.PLAIN_MESSAGE);
        currentHero.setDazed(false);
        updateData();
      }
      else {
        if (JOptionPane.showConfirmDialog(this, "KO-Probe mit einer " + roll
            + " nicht bestanden.\nTrotzdem ändern?", "Benommenheit",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          currentHero.setDazed(false);
          updateData();
        }
        else {
          dazedBox.setSelected(true);
        }
      }
    }
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JSpinner getMarkerSpinner() {
    if (markerSpinner == null) {
      markerSpinner = new JSpinner();
      markerSpinner.setBounds(new Rectangle(90, 280, 51, 21));
      markerSpinner.setModel(getMarkerSpinnerModel());
      markerSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          changeMarkers();
        }
      });
    }
    return markerSpinner;
  }

  private void changeMarkers() {
    int leMarkers = currentHero.getMarkers() - currentHero.getExtraMarkers();
    int newMarkers = ((Number) markerSpinner.getValue()).intValue();
    currentHero.setExtraMarkers(newMarkers - leMarkers);
  }

  private SpinnerNumberModel markerSpinnerModel;

  private JLabel opponent2Label = null;

  private JComboBox opponent2Box = null;

  private JLabel opponent1Label = null;

  private JComboBox opponent1Box = null;

  private SpinnerNumberModel getMarkerSpinnerModel() {
    if (markerSpinnerModel == null) {
      markerSpinnerModel = new SpinnerNumberModel(0, 0, 0, 1);
    }
    return markerSpinnerModel;
  }

  /**
   * This method initializes opponent2Box	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getOpponent2Box() {
    if (opponent2Box == null) {
      opponent2Box = new JComboBox();
      opponent2Box.setBounds(new Rectangle(120, 190, 171, 21));
      opponent2Box.addItem("<Waffe ...>");
      opponent2Box.addItem("Nichts");
      opponent2Box.addItem("Raufen");
      opponent2Box.addItem("Boxen");
      opponent2Box.addItem("Ringen");
      opponent2Box.addItem("Hruruzat");
      opponent2Box.addItemListener(new ItemListener() {
        private boolean listenToCombo = true;
        public void itemStateChanged(ItemEvent e) {
          if (!listenToCombo || !listen) return;
          listenToCombo = false;
          if ("<Waffe ...>".equals(opponent2Box.getSelectedItem())) {
            selectOpponentWeapon(opponent2Box);
          }
          else {
            setSecondAT();
            setSecondPA();
            currentHero.setOpponentWeapon(1, opponent2Box.getSelectedItem().toString());
          }
          if ("<Waffe ...>".equals(opponent2Box.getSelectedItem())) {
            opponent2Box.setEditable(true);
            opponent2Box.setSelectedItem(currentHero.getOpponentWeapon(1));
            opponent2Box.setEditable(false);
          }
          listenToCombo = true;
        }
      });
    }
    return opponent2Box;
  }
  
  /**
   * This method initializes opponent1Box	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getOpponent1Box() {
    if (opponent1Box == null) {
      opponent1Box = new JComboBox();
      opponent1Box.setBounds(new Rectangle(120, 100, 171, 21));
      opponent1Box.addItem("<Waffe ...>");
      opponent1Box.addItem("Nichts");
      opponent1Box.addItem("Raufen");
      opponent1Box.addItem("Boxen");
      opponent1Box.addItem("Ringen");
      opponent1Box.addItem("Hruruzat");
      opponent1Box.addItemListener(new ItemListener() {
        private boolean listenToCombo = true;
        public void itemStateChanged(ItemEvent e) {
          if (!listenToCombo || !listen) return;
          listenToCombo = false;
          if ("<Waffe ...>".equals(opponent1Box.getSelectedItem())) {
            selectOpponentWeapon(opponent1Box);
          }
          else {
            setFirstAT();
            setFirstPA();
            currentHero.setOpponentWeapon(0, opponent1Box.getSelectedItem().toString());
          }
          if ("<Waffe ...>".equals(opponent1Box.getSelectedItem())) {
            opponent1Box.setEditable(true);
            opponent1Box.setSelectedItem(currentHero.getOpponentWeapon(0));
            opponent1Box.setEditable(false);
          }
          listenToCombo = true;
        }
      });
    }
    return opponent1Box;
  }
  
  
  private static class MySelectionDialogCallback implements SelectionDialogCallback
  {
    public void itemChanged(String item) {
    }
    public void itemSelected(String item) {
      mBox.setEditable(true);
      mBox.setSelectedItem(item);
      mBox.setEditable(false);
    }
    public MySelectionDialogCallback(final JComboBox box) {
      mBox = box;
    }
    
    private final JComboBox mBox;
  };

  private void selectOpponentWeapon(final JComboBox box) {
    
    WeaponsSelectionDialog dialog = new WeaponsSelectionDialog(this, true);
    dialog.setCallback(new MySelectionDialogCallback(box));
    dialog.setVisible(true);
  }

} // @jve:decl-index=0:visual-constraint="10,10"

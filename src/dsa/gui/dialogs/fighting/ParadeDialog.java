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
package dsa.gui.dialogs.fighting;

import dsa.control.Dice;
import dsa.control.Fighting;
import dsa.gui.lf.BGDialog;
import dsa.gui.util.ImageManager;
import dsa.model.Fighter;
import dsa.model.characters.Hero;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.remote.RemoteManager;
import dsa.util.Optional;
import dsa.util.Strings;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ParadeDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JRadioButton paradeButton = null;
  private JRadioButton evadeButton = null;
  private JLabel jLabel = null;
  private JSpinner evadeBonusSpinner = null;
  private JLabel jLabel1 = null;
  private JSpinner diceRollSpinner = null;
  private JButton rollParadeButton = null;
  private JButton modButton = null;
  private JLabel resultLabel = null;
  private JRadioButton hitButton = null;
  private JRadioButton noHitButton = null;
  private JRadioButton fumbleRadioButton = null;
  private JSpinner fumbleSpinner = null;
  private JButton fumbleButton = null;
  private JLabel fumbleLabel = null;
  private JLabel fumbleLabel2 = null;
  private JSpinner spSpinner = null;
  private JButton spButton = null;
  private JCheckBox sendToPlayerBox = null;
  private JCheckBox sendToAllBox = null;
  private JCheckBox copyBox = null;
  private JButton cancelButton = null;
  private JButton okButton = null;
  
  public static enum ParadeOutcome { Hit, NoHit, Fumble, Canceled };
  
  public static class ParadeResult {
    public ParadeResult(ParadeOutcome outcome, String weapon) {
      this.outcome = outcome;
      this.weapon = weapon;
      isEvasion = false;
    }
    
    public ParadeResult(int type, int sp, String weapon) {
      fumbleType = type;
      fumbleSP = sp;
      outcome = ParadeOutcome.Fumble;
      this.weapon = weapon;
      isEvasion = false;
    }
    
    public ParadeOutcome getOutcome() { return outcome; }
    public int getFumbleType() { return fumbleType; }
    public int getFumbleSP() { return fumbleSP; }
    public String getWeapon() { return weapon; }
    
    public boolean sendToPlayer() { return sendToPlayer; }
    public boolean sendToAll() { return sendToAll; }
    public boolean copy() { return copy; }
    public boolean isEvasion() { return isEvasion; }
    
    public void setSendToPlayer(boolean send) { sendToPlayer = send; }
    public void setSendToAll(boolean send) { sendToAll = send; }
    public void setCopy(boolean copy) { this.copy = copy; }
    public void setEvasion(boolean evasion) { isEvasion = evasion; }
    
    private ParadeOutcome outcome;
    private int fumbleType;
    private int fumbleSP;
    private String weapon;
    private boolean sendToPlayer;
    private boolean sendToAll;
    private boolean copy;
    private boolean isEvasion;
  }

  /**
   * This method initializes 
   * 
   */
  public ParadeDialog(JFrame parent, Fighter defender, int atQuality, String attack, String parade) {
  	super(parent, true);
    mDefender = defender;
    this.atQuality = atQuality;
    attackWeapon = attack;
  	initialize();
    initializeLogic();
    setLocationRelativeTo(parent);
    getRootPane().setDefaultButton(getOkButton());
    setEscapeButton(getCancelButton());
    this.setTitle("Parade von " + Strings.cutTo(mDefender.getName(), ' '));
    if (parade != null && !"Nichts".equals(parade)) {
      weaponBox.setSelectedItem(parade);
      weaponBox.setEnabled(false);
      canSelectWeapon = false;
      evadeButton.setEnabled(false);
      paradeButton.doClick();
    }
    else if ("Nichts".equals(parade)) {
      evadeButton.doClick();
      paradeButton.setEnabled(false);
      canSelectWeapon = false;
    }
    else {
      weaponBox.setEnabled(true);
      canSelectWeapon = true;
      paradeButton.doClick();
    }
  }
  
  public ParadeResult getResult() {
    return mResult;
  }
  
  private Fighter mDefender;
  private int extraMod = 0;
  private int atQuality;
  private int bonusForNextAction = 0;
  
  private ParadeResult mResult = new ParadeResult(ParadeOutcome.Canceled, "");
  private JLabel jLabel2 = null;
  private JComboBox weaponBox = null;
  
  private boolean canSelectWeapon;
  private String attackWeapon;

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
	boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
    this.setSize(new Dimension(289, !isConnected ? 366 : 406));
    this.setContentPane(getJContentPane());
  }
  
  private boolean listenForButtons = true;
  
  private void initializeLogic()
  {
    getParadeButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!listenForButtons) return;
        choiceSelected(paradeButton);
        paradeSelected();
      }
    });
    getEvadeButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!listenForButtons) return;
        choiceSelected(evadeButton);
        evadeSelected();
      }
    });
    getHitButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!listenForButtons) return;
        choiceSelected(hitButton);
        disableControls();
      }
    });
    getNoHitButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!listenForButtons) return;
        choiceSelected(noHitButton);
        disableControls();
      }
    });
    getFumbleRadioButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!listenForButtons) return;
        choiceSelected(fumbleRadioButton);
        fumbleSelected();
      }
    });
    getEvadeBonusSpinner().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        calcOutcome();
      }
    });
    getDiceRollSpinner().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        calcOutcome();
      }
    });
    getRollParadeButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rollParade();
      }
    });
    getModButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int weaponNr = 0;
        if (paradeButton.isSelected()) weaponNr = weaponBox.getSelectedIndex();
        AtPaModDialog dialog = new AtPaModDialog(ParadeDialog.this, mDefender, weaponNr, extraMod, false);
        dialog.setVisible(true);
        extraMod = dialog.getExtraMod();
        calcOutcome();
      }
    });
    getFumbleSpinner().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        fumbleTypeChanged();
      }
    });
    getFumbleButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcFumble();
      }
    });
    getSpButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcSP();
      }
    });
    getCancelButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    getOkButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcResult();
        int weaponNr = 0;
        if (paradeButton.isSelected()) weaponNr = weaponBox.getSelectedIndex();
        mDefender.setATBonus(weaponNr, bonusForNextAction);
        mDefender.setHasStumbled(false);
        if (RemoteManager.getInstance().isConnectedAsGM() && (mDefender instanceof Hero)) {
        	if (mResult.sendToPlayer()) {
        		RemoteManager.getInstance().informOfFightPropertyChange((Hero)mDefender, dsa.remote.IServer.FightProperty.stumbled, mResult.sendToAll());
        		if (weaponNr == 0)
        			RemoteManager.getInstance().informOfFightPropertyChange((Hero)mDefender, dsa.remote.IServer.FightProperty.atBonus1, mResult.sendToAll());
        		else
        			RemoteManager.getInstance().informOfFightPropertyChange((Hero)mDefender, dsa.remote.IServer.FightProperty.atBonus2, mResult.sendToAll());
        	}
        }
        dispose();
      }
    });
    weaponBox.removeAllItems();
    for (String weapon : mDefender.getFightingWeapons()) {
      Weapon w = Weapons.getInstance().getWeapon(weapon);
      if (w != null && w.isFarRangedWeapon()) continue;
      weaponBox.addItem(weapon);
    }
    paradeButton.setEnabled(weaponBox.getItemCount() > 0);
    weaponBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (paradeButton.isSelected()) calcOutcome();
      }
    });
    if (paradeButton.isEnabled()) {
      paradeButton.setSelected(true);
      paradeSelected();
    }
    else if (evadeButton.isEnabled()) {
      evadeButton.setSelected(true);
      evadeSelected();
    }
    else {
      hitButton.setSelected(true);
      disableControls();
    }
  }
  
  private void fumbleTypeChanged() {
    int value = ((Number)fumbleSpinner.getValue()).intValue();
    switch (value) {
    case 2:
      fumbleLabel.setText("Waffe verloren");
      fumbleLabel2.setText("Waffe schwer beschädigt");
      break;
    case 3:
      fumbleLabel.setText("Waffe verloren");
      break;
    case 4:
    case 5:
      fumbleLabel.setText("Waffe beschädigt");
      break;
    case 6:
    case 7:
    case 8:
      fumbleLabel.setText("Stolpern");
      break;
    case 9:
    case 10:
      fumbleLabel.setText("Sturz");
      break;
    case 11:
      fumbleLabel.setText("Selbst leicht verletzt");
      ((SpinnerNumberModel)spSpinner.getModel()).setMinimum(1);
      ((SpinnerNumberModel)spSpinner.getModel()).setMaximum(6);
      break;
    case 12:
      fumbleLabel.setText("Selbst schwer verletzt");
      ((SpinnerNumberModel)spSpinner.getModel()).setMinimum(2);
      ((SpinnerNumberModel)spSpinner.getModel()).setMaximum(12);
      break;
    default:
      fumbleLabel.setText("");
      break;
    }
    spSpinner.setEnabled(value >= 11);
    spSpinner.setVisible(value >= 11);
    spButton.setEnabled(value >= 11);
    spButton.setVisible(value >= 11);
    if (value >= 11) {
      fumbleLabel2.setText("SP:");
      calcSP();
    }
    else if (value != 2) {
      fumbleLabel2.setText("");
    }
  }
  
  private void calcSP() {
    int value = ((Number)fumbleSpinner.getValue()).intValue();
    int sp = 0;
    if (value >= 11) {
      sp += Dice.roll(6);
    }
    if (value == 12) {
      sp += Dice.roll(6);
    }
    spSpinner.setValue(Integer.valueOf(sp));
  }
  
  private void calcOutcome() {
    int roll = ((Number)diceRollSpinner.getValue()).intValue();
    if (roll == 20) {
      resultLabel.setText("Patzer!");
      fumbleRadioButton.setSelected(true);
      choiceSelected(fumbleRadioButton);
      fumbleSelected();
      return;
    }
    int toHit = 0;
    int weaponNr = 0;
    bonusForNextAction = 0;
    if (evadeButton.isSelected()) {
      Hero hero = (Hero) mDefender;
      toHit = hero.getCurrentDerivedValue(Hero.DerivedValue.AW);
      int awBonus = ((Number)evadeBonusSpinner.getValue()).intValue();
      toHit += awBonus;
      bonusForNextAction -= 2 * awBonus;
    }
    else {
      weaponNr = weaponBox.getSelectedIndex();
      Optional<Integer> pa = mDefender.getPA(weaponNr);
      if (pa.hasValue()) {
        toHit = pa.getValue().intValue();
        toHit = Fighting.getWVModifiedPA(toHit, attackWeapon, weaponBox.getSelectedItem().toString());
      }
    }
    toHit -= atQuality;
    toHit -= extraMod;
    toHit -= Fighting.getModifier(mDefender, false, weaponNr);
    String outcome = "";
    if (roll > toHit && roll != 1) {
      if (evadeButton.isSelected()) outcome = "Nicht ausgewichen.";
      else outcome = "Nicht pariert.";
    }
    else if (roll == 1) {
      if (evadeButton.isSelected()) outcome = "Perfekt ausgewichen!";
      else outcome = "Perfekt pariert!";
      bonusForNextAction += (toHit - 1) / 2;
    }
    else {
      if (evadeButton.isSelected()) outcome = "Ausgewichen!";
      else outcome = "Pariert!";
    }
    resultLabel.setText(outcome);
  }
  
  private void choiceSelected(JRadioButton button) {
    listenForButtons = false;
    paradeButton.setSelected(button == paradeButton);
    evadeButton.setSelected(button == evadeButton);
    hitButton.setSelected(button == hitButton);
    noHitButton.setSelected(button == noHitButton);
    fumbleRadioButton.setSelected(button == fumbleRadioButton);
    listenForButtons = true;
  }
  
  private void paradeSelected() {
    weaponBox.setEnabled(canSelectWeapon);
    evadeBonusSpinner.setEnabled(false);
    enableRoll();
    rollParade();
  }
  
  private void evadeSelected() {
    weaponBox.setEnabled(false);
    evadeBonusSpinner.setEnabled(true);
    enableRoll();
    rollParade();
  }
  
  private void rollParade() {
    int roll = Dice.roll(20);
    diceRollSpinner.setValue(Integer.valueOf(roll));
    if (roll == 20) {
      resultLabel.setText("Patzer!");
      fumbleRadioButton.setSelected(true);
      choiceSelected(fumbleRadioButton);
      fumbleSelected();
    }
    else calcOutcome();
  }
  
  private void enableRoll() {
    diceRollSpinner.setEnabled(true);
    rollParadeButton.setEnabled(true);
    modButton.setEnabled(true);
    hideFumble();
  }
  
  private void disableControls() {
    weaponBox.setEnabled(false);
    evadeBonusSpinner.setEnabled(false);
    diceRollSpinner.setEnabled(false);
    rollParadeButton.setEnabled(false);
    modButton.setEnabled(false);
    hideFumble();
  }
  
  private void hideFumble() {
    fumbleSpinner.setEnabled(false);
    fumbleButton.setEnabled(false);
    fumbleLabel.setText("");
    fumbleLabel2.setText("");
    spSpinner.setEnabled(false);
    spSpinner.setVisible(false);
    spButton.setEnabled(false);
    spButton.setVisible(false);    
  }
  
  private void fumbleSelected() {
    diceRollSpinner.setEnabled(false);
    rollParadeButton.setEnabled(false);
    modButton.setEnabled(false);
    fumbleSpinner.setEnabled(true);
    fumbleButton.setEnabled(true);
    calcFumble();
  }
  
  private void calcFumble() {
    int value = Dice.roll(6) + Dice.roll(6);
    if (!(mDefender instanceof dsa.model.characters.Hero)) 
      while (value < 6)
        value = Dice.roll(6) + Dice.roll(6);
    fumbleSpinner.setValue(value);
  }
  
  private void calcResult() {
    if (hitButton.isSelected()) {
      mResult = new ParadeResult(ParadeOutcome.Hit, "");
    }
    else if (noHitButton.isSelected()) {
      mResult = new ParadeResult(ParadeOutcome.NoHit, "");
    }
    else if (fumbleRadioButton.isSelected()) {
      int sp = 0;
      int fumbleType = ((Number)fumbleSpinner.getValue()).intValue();
      if (fumbleType >= 11) {
        sp = ((Number) spSpinner.getValue()).intValue();
      }
      String weapon = "";
      if (paradeButton.isSelected()) weapon = weaponBox.getSelectedItem().toString();
      mResult = new ParadeResult(fumbleType, sp, weapon);
    }
    else {
      boolean hit = resultLabel.getText().startsWith("Nicht");
      String weapon = "";
      if (paradeButton.isSelected()) weapon = weaponBox.getSelectedItem().toString();
      mResult = new ParadeResult(hit ? ParadeOutcome.Hit : ParadeOutcome.NoHit, weapon);
    }
    mResult.setCopy(copyBox.isSelected());
    mResult.setSendToPlayer(sendToPlayerBox.isSelected());
    mResult.setSendToAll(sendToAllBox.isSelected());
    mResult.setEvasion(evadeButton.isSelected());
  }

  public String getHelpPage() {
    return "Parade_Meister";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(90, 10, 51, 21));
      jLabel2.setText("Waffe:");
      fumbleLabel2 = new JLabel();
      fumbleLabel2.setBounds(new Rectangle(30, 250, 171, 21));
      fumbleLabel2.setText("SP:");
      fumbleLabel = new JLabel();
      fumbleLabel.setBounds(new Rectangle(30, 220, 231, 21));
      fumbleLabel.setText("");
      resultLabel = new JLabel();
      resultLabel.setBounds(new Rectangle(30, 100, 231, 21));
      resultLabel.setText("");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(30, 70, 51, 21));
      jLabel1.setText("Wurf:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(150, 40, 51, 21));
      jLabel.setText("Bonus:");
      boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
      copyBox = new JCheckBox("Kopieren");
      copyBox.setBounds(new Rectangle(10, 270, 161, 21));
      copyBox.setSelected(!isConnected);
      sendToPlayerBox = new JCheckBox("An Spieler senden");
      sendToPlayerBox.setBounds(new Rectangle(10, 290, 161, 21));
      sendToPlayerBox.setEnabled(isConnected);
      sendToPlayerBox.setSelected(isConnected);
      sendToPlayerBox.addItemListener(new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent arg0) {
			sendToAllBox.setSelected(sendToPlayerBox.isSelected());
			sendToAllBox.setEnabled(sendToPlayerBox.isSelected());
		}
      });
      sendToAllBox = new JCheckBox("An alle senden");
      sendToAllBox.setBounds(new Rectangle(10, 310, 161, 21));
      sendToAllBox.setEnabled(isConnected);
      sendToAllBox.setSelected(isConnected);
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(getParadeButton(), null);
      jContentPane.add(getEvadeButton(), null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getEvadeBonusSpinner(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getDiceRollSpinner(), null);
      jContentPane.add(getRollParadeButton(), null);
      jContentPane.add(getModButton(), null);
      jContentPane.add(resultLabel, null);
      jContentPane.add(getHitButton(), null);
      jContentPane.add(getNoHitButton(), null);
      jContentPane.add(getFumbleRadioButton(), null);
      jContentPane.add(getFumbleSpinner(), null);
      jContentPane.add(getFumbleButton(), null);
      jContentPane.add(fumbleLabel, null);
      jContentPane.add(fumbleLabel2, null);
      jContentPane.add(getSpSpinner(), null);
      jContentPane.add(getSpButton(), null);
      jContentPane.add(copyBox, null);
      if (isConnected) {
    	  jContentPane.add(sendToPlayerBox, null);
    	  jContentPane.add(sendToAllBox, null);
      }
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(getWeaponBox(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes paradeButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getParadeButton() {
    if (paradeButton == null) {
      paradeButton = new JRadioButton();
      paradeButton.setBounds(new Rectangle(10, 10, 72, 21));
      paradeButton.setText("Parade");
    }
    return paradeButton;
  }

  /**
   * This method initializes evadeButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getEvadeButton() {
    if (evadeButton == null) {
      evadeButton = new JRadioButton();
      evadeButton.setBounds(new Rectangle(10, 40, 121, 21));
      evadeButton.setText("Ausweichen");
      evadeButton.setEnabled(mDefender instanceof Hero);
    }
    return evadeButton;
  }

  /**
   * This method initializes evadeBonusSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getEvadeBonusSpinner() {
    if (evadeBonusSpinner == null) {
      evadeBonusSpinner = new JSpinner();
      evadeBonusSpinner.setBounds(new Rectangle(210, 40, 51, 21));
      evadeBonusSpinner.setEnabled(false);
      if (mDefender instanceof Hero) {
        int max = ((Hero) mDefender).getCurrentDerivedValue(Hero.DerivedValue.AB);
        evadeBonusSpinner.setModel(new SpinnerNumberModel(0, 0, max, 1));
      }
    }
    return evadeBonusSpinner;
  }

  /**
   * This method initializes diceRollSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getDiceRollSpinner() {
    if (diceRollSpinner == null) {
      diceRollSpinner = new JSpinner();
      diceRollSpinner.setBounds(new Rectangle(90, 70, 51, 21));
    }
    return diceRollSpinner;
  }

  /**
   * This method initializes rollParadeButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRollParadeButton() {
    if (rollParadeButton == null) {
      rollParadeButton = new JButton();
      rollParadeButton.setBounds(new Rectangle(150, 70, 51, 21));
      rollParadeButton.setToolTipText("Würfeln");
      rollParadeButton.setIcon(ImageManager.getIcon("probe"));
    }
    return rollParadeButton;
  }

  /**
   * This method initializes modButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getModButton() {
    if (modButton == null) {
      modButton = new JButton();
      modButton.setBounds(new Rectangle(210, 70, 51, 21));
      modButton.setToolTipText("Modifikatoren ...");
      modButton.setText("...");
    }
    return modButton;
  }

  /**
   * This method initializes hitButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getHitButton() {
    if (hitButton == null) {
      hitButton = new JRadioButton();
      hitButton.setBounds(new Rectangle(10, 130, 131, 21));
      hitButton.setText("Treffer");
    }
    return hitButton;
  }

  /**
   * This method initializes noHitButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getNoHitButton() {
    if (noHitButton == null) {
      noHitButton = new JRadioButton();
      noHitButton.setBounds(new Rectangle(10, 160, 131, 21));
      noHitButton.setText("Kein Treffer");
    }
    return noHitButton;
  }

  /**
   * This method initializes jRadioButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getFumbleRadioButton() {
    if (fumbleRadioButton == null) {
      fumbleRadioButton = new JRadioButton();
      fumbleRadioButton.setBounds(new Rectangle(10, 190, 131, 21));
      fumbleRadioButton.setText("Patzer:");
    }
    return fumbleRadioButton;
  }

  /**
   * This method initializes fumbleSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getFumbleSpinner() {
    if (fumbleSpinner == null) {
      fumbleSpinner = new JSpinner();
      fumbleSpinner.setBounds(new Rectangle(150, 190, 51, 21));
      int min = (mDefender instanceof Hero) ? 2 : 6;
      fumbleSpinner.setModel(new SpinnerNumberModel(min, min, 12, 1));
    }
    return fumbleSpinner;
  }

  /**
   * This method initializes fumbleButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getFumbleButton() {
    if (fumbleButton == null) {
      fumbleButton = new JButton();
      fumbleButton.setBounds(new Rectangle(210, 190, 51, 21));
      fumbleButton.setToolTipText("Auswürfeln");
      fumbleButton.setIcon(ImageManager.getIcon("probe"));
    }
    return fumbleButton;
  }

  /**
   * This method initializes spSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getSpSpinner() {
    if (spSpinner == null) {
      spSpinner = new JSpinner();
      spSpinner.setBounds(new Rectangle(150, 250, 51, 21));
    }
    return spSpinner;
  }

  /**
   * This method initializes spButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getSpButton() {
    if (spButton == null) {
      spButton = new JButton();
      spButton.setBounds(new Rectangle(210, 250, 51, 21));
      spButton.setIcon(ImageManager.getIcon("probe"));
    }
    return spButton;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
      cancelButton.setBounds(new Rectangle(160, !isConnected ? 310 : 350, 101, 21));
      cancelButton.setText("Abbrechen");
    }
    return cancelButton;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
      okButton.setBounds(new Rectangle(30, !isConnected ? 310 : 350, 101, 21));
      okButton.setText("OK");
    }
    return okButton;
  }

  /**
   * This method initializes weaponBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getWeaponBox() {
    if (weaponBox == null) {
      weaponBox = new JComboBox();
      weaponBox.setBounds(new Rectangle(150, 10, 111, 21));
    }
    return weaponBox;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

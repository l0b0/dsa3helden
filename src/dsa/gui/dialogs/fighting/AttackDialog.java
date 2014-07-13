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
import dsa.model.FarRangedFightParams;
import dsa.model.Fighter;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.remote.RemoteManager;
import dsa.util.Optional;
import dsa.util.Strings;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;

public class AttackDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JLabel jLabel = null;
  private JSpinner atThrowField = null;
  private JLabel jLabel1 = null;
  private JTextField qualityField = null;
  private JLabel jLabel2 = null;
  private JSpinner tpField = null;
  private JCheckBox fumbleBox = null;
  private JSpinner fumbleField = null;
  private JLabel fumbleLabel = null;
  private JLabel fumbleLabel1 = null;
  private JSpinner fumbleField2 = null;
  private JButton atRollButton = null;
  private JButton tpRollButton = null;
  private JButton fumbleButton = null;
  private JButton fumbleButton2 = null;
  private JButton cancelButton = null;
  private JButton okButton = null;
  private JButton modButton = null;
  private JCheckBox sendToPlayerBox = null;
  private JCheckBox sendToAllBox = null;
  private JCheckBox copyBox = null;
  private int extraMod = 0;
  private int farRangedMod = 0;
  
  private boolean farRanged = false;
  private int distance = 0;
  private int at;

  public static class AttackResult {
    public AttackResult(boolean fumble, boolean hit, int param, int tp, int pa) {
      this.fumble = fumble;
      if (!fumble) quality = param; else fumbleType = param;
      this.tp = tp;
      this.hit = hit;
      paradeIndex = pa;
    }
    public AttackResult(int fumbleType) {
      fumble = true;
      this.fumbleType = fumbleType; 
    }
    
    public boolean hasFumbled() { return fumble; }
    public boolean hasHit() { return hit; }
    public int getQuality() { return quality; }
    public int getTP() { return tp; }
    public int getFumbleType() { return fumbleType; }
    public int getParadeIndex() { return paradeIndex; }
    
    public boolean sendToPlayer() { return sendToPlayer; }
    public boolean sendToAll() { return sendToAll; }
    public boolean copy() { return copy; }
    
    public void setSendToPlayer(boolean send) { sendToPlayer = send; }
    public void setSendToAll(boolean send) { sendToAll = send; }
    public void setCopy(boolean copy) { this.copy = copy; }
    
    private boolean fumble;
    private boolean hit;
    private int quality;
    private int tp;
    private int fumbleType;
    private int paradeIndex;
    private boolean sendToPlayer;
    private boolean sendToAll;
    private boolean copy;
  }

  /**
   * This method initializes 
   * 
   */
  public AttackDialog(JFrame owner, Fighter attacker, int weaponNr, String[] possiblePAs) {
  	super(owner, true);
    this.attacker = attacker;
    this.weaponNr = weaponNr;
    String weaponName = attacker.getFightingWeapons().get(weaponNr);
    Weapon weapon = Weapons.getInstance().getWeapon(weaponName);
    farRanged = (weapon != null) && weapon.isFarRangedWeapon();
  	initialize();
    this.setLocationRelativeTo(owner);
    Optional<Integer> atValue = attacker.getAT(weaponNr);
    at = atValue.hasValue() ? atValue.getValue() : 7;
    if (farRanged) {
      FarRangedFightParams params = attacker.getFarRangedFightParams();
      at = ProjectileAttackDialog.calcAtValue(params, at);
      distance = params.getDistance();
      farRangedMod = ProjectileAttackDialog.calcModifier(params, weapon);
    }
    paradeBox.addItem("Nichts");
    if (!farRanged && dsa.model.characters.Group.getInstance().getOptions().useWV()) {
      for (String pa : possiblePAs) {
        paradeBox.addItem(pa);
      }
      paradeBox.setEditable(true);
      paradeBox.setSelectedItem(attacker.getOpponentWeapon(weaponNr));
      paradeBox.setEditable(false);
    }
    else {
      paradeLabel.setEnabled(false);
      paradeBox.setEnabled(false);
    }
    initializeLogic();
    rollAT();
    
    this.setEscapeButton(cancelButton);
    this.getRootPane().setDefaultButton(okButton);
  }
  
  private Fighter attacker;
  private int weaponNr;
  
  private AttackResult result = null;
  
  public AttackResult getResult() { return result; }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
	boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
    this.setSize(new Dimension(242, !isConnected ? 308 : 348));
    this.setTitle("Attacke von " + Strings.cutTo(attacker.getName(), ' '));
    this.setContentPane(getJContentPane());
    atThrowField.requestFocus();
  }
  
  private boolean listenForATChange = true;
  private JLabel paradeLabel = null;
  private JComboBox paradeBox = null;
  
  private void rollAT() {
    int newThrow = Dice.roll(20);
    atThrowField.setValue(newThrow);    
  }
  
  private void initializeLogic() {
    atRollButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        rollAT();
      }
    });
    tpRollButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcTP();
      }
    });
    fumbleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcFumble();
      }
    });
    fumbleButton2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcSP();
      }
    });
    atThrowField.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!listenForATChange) return;
        listenForATChange = false;
        calcQualityAndTP();
        listenForATChange = true;
      }
    });
    fumbleBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (fumbleBox.isSelected()) {
          listenForATChange = false;
          atThrowField.setValue(20);
          tpField.setValue(0);
          tpField.setEnabled(false);
          tpRollButton.setEnabled(false);
          qualityField.setText("--");
          fumbleField.setEnabled(true);
          fumbleButton.setEnabled(true);
          modButton.setEnabled(false);
          calcFumble();    
          listenForATChange = true;
        }
        else {
          fumbleLabel.setText("");
          fumbleLabel1.setText("");
          fumbleField2.setEnabled(false);
          fumbleField2.setVisible(false);
          fumbleButton2.setEnabled(false);
          fumbleButton2.setVisible(false);
          fumbleButton.setEnabled(false);
          modButton.setEnabled(true);
          atThrowField.setValue(10);
        }
      }
    });
    fumbleField.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
    	fumbleValueChanged();
      }
    });
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        result = null;
        dispose();
      }
    });
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int paWeapon = paradeBox.getSelectedIndex() - 1;
        if (fumbleBox.isSelected()) {
          int fumbleType = ((Number)fumbleField.getValue()).intValue();
          if (fumbleType >= 11) {
            int tp = ((Number)fumbleField2.getValue()).intValue();
            result = new AttackResult(true, false, fumbleType, tp, paWeapon);
          }
          else {
            result = new AttackResult(fumbleType);
          }
        }
        else {
          if (qualityField.getText().equals("--")) {
            result = new AttackResult(false, false, 0, 0, paWeapon);
          }
          else {
            int quality = Integer.parseInt(qualityField.getText());
            int tp = ((Number)tpField.getValue()).intValue();
            result = new AttackResult(false, true, quality, tp, paWeapon);
          }
        }
        result.setCopy(copyBox.isSelected());
        result.setSendToPlayer(sendToPlayerBox.isSelected());
        result.setSendToAll(sendToAllBox.isSelected());
        dispose();
      }
    });
    modButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (farRanged) {
          ProjectileAttackDialog dialog = new ProjectileAttackDialog(
              AttackDialog.this, weaponNr, attacker, extraMod);
          dialog.setVisible(true);
          if (!dialog.wasCanceled()) {
            at = dialog.getATValue();
            distance = dialog.getDistance();
            farRangedMod = dialog.getModifier();
            extraMod = dialog.getAdditionalModifier();
            calcQualityAndTP();
          }
        }
        else {
          AtPaModDialog dialog = new AtPaModDialog(AttackDialog.this, attacker, weaponNr, extraMod, true);
          dialog.setVisible(true);
          extraMod = dialog.getExtraMod();
          calcQualityAndTP();
        }
      }
    });
    paradeBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        attacker.setOpponentWeapon(weaponNr, paradeBox.getSelectedItem().toString());
        calcQualityAndTP();
      }
    });
    calcQualityAndTP();
  }
  
  private void fumbleValueChanged() {
      int value = ((Number)fumbleField.getValue()).intValue();
      if (!farRanged) {
	        switch (value) {
	        case 2:
	          fumbleLabel.setText("Waffe verloren");
	          fumbleLabel1.setText("Waffe schwer beschädigt");
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
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMinimum(1);
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMaximum(6);
	          break;
	        case 12:
	          fumbleLabel.setText("Selbst schwer verletzt");
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMinimum(2);
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMaximum(12);
	          break;
	        default:
	          fumbleLabel.setText("");
	          break;
	        }
	        fumbleField2.setEnabled(value >= 11);
	        fumbleField2.setVisible(value >= 11);
	        fumbleButton2.setEnabled(value >= 11);
	        fumbleButton2.setVisible(value >= 11);
	        if (value >= 11) {
	          fumbleLabel1.setText("SP:");
	          calcSP();
	        }
	        else if (value != 2) {
	          fumbleLabel1.setText("");
	        }
      }
      else {
      	fumbleLabel.setText(Fighting.getFKFumbleResult(value));
      	if (value >= 11) {
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMinimum(1);
	          ((SpinnerNumberModel)fumbleField2.getModel()).setMaximum(attacker.getTP(weaponNr).getDiceSize() * attacker.getTP(weaponNr).getNrOfDices());
	          fumbleLabel1.setText("TP:");
	          calcSP();
      	}
      	else {
	        	fumbleLabel1.setText("");
      	}
	        fumbleField2.setEnabled(value >= 11);
	        fumbleField2.setVisible(value >= 11);
	        fumbleButton2.setEnabled(value >= 11);
	        fumbleButton2.setVisible(value >= 11);
      }	  
  }

  public String getHelpPage() {
    return "Attacke_Meister";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      paradeLabel = new JLabel();
      paradeLabel.setBounds(new Rectangle(10, 40, 91, 21));
      paradeLabel.setText("Parade mit:");
      fumbleLabel1 = new JLabel();
      fumbleLabel1.setBounds(new Rectangle(30, 190, 191, 21));
      fumbleLabel1.setText("");
      fumbleLabel = new JLabel();
      fumbleLabel.setBounds(new Rectangle(30, 160, 191, 21));
      fumbleLabel.setText("");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(10, 100, 91, 21));
      jLabel2.setText("TrefferPunkte:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 70, 91, 21));
      jLabel1.setText("Qualität:");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 91, 21));
      jLabel.setText("Attacke-Wurf:");
      boolean isConnected = RemoteManager.getInstance().isConnectedAsGM();
      copyBox = new JCheckBox("Kopieren");
      copyBox.setBounds(new Rectangle(10, 220, 161, 21));
      copyBox.setSelected(!isConnected);
      sendToPlayerBox = new JCheckBox("An Spieler senden");
      sendToPlayerBox.setBounds(new Rectangle(10, 240, 161, 21));
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
      sendToAllBox.setBounds(new Rectangle(10, 260, 161, 21));
      sendToAllBox.setEnabled(isConnected);
      sendToAllBox.setSelected(isConnected);
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getAtThrowField(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getQualityField(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(getTpField(), null);
      jContentPane.add(getFumbleBox(), null);
      jContentPane.add(getFumbleField(), null);
      jContentPane.add(fumbleLabel, null);
      jContentPane.add(fumbleLabel1, null);
      jContentPane.add(getFumbleField2(), null);
      jContentPane.add(getAtRollButton(), null);
      jContentPane.add(getModButton(), null);
      jContentPane.add(getTpRollButton(), null);
      jContentPane.add(getFumbleButton(), null);
      jContentPane.add(getFumbleButton2(), null);
      jContentPane.add(copyBox, null);
      if (isConnected) {
    	  jContentPane.add(sendToPlayerBox, null);
    	  jContentPane.add(sendToAllBox, null);
      }
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(paradeLabel, null);
      jContentPane.add(getParadeBox(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes atThrowField	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getAtThrowField() {
    if (atThrowField == null) {
      atThrowField = new JSpinner(new SpinnerNumberModel(10, 1, 20, 1));
      atThrowField.setBounds(new Rectangle(110, 10, 51, 21));
    }
    return atThrowField;
  }

  /**
   * This method initializes qualityField	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JTextField getQualityField() {
    if (qualityField == null) {
      qualityField = new JTextField();
      qualityField.setBounds(new Rectangle(110, 70, 36, 21));
      qualityField.setEditable(false);
      qualityField.setHorizontalAlignment(JTextField.RIGHT);
    }
    return qualityField;
  }

  /**
   * This method initializes tpField	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getTpField() {
    if (tpField == null) {
      tpField = new JSpinner(new SpinnerNumberModel(1, -10, 80, 1));
      tpField.setBounds(new Rectangle(110, 100, 51, 21));
    }
    return tpField;
  }

  /**
   * This method initializes fumbleBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getFumbleBox() {
    if (fumbleBox == null) {
      fumbleBox = new JCheckBox();
      fumbleBox.setBounds(new Rectangle(10, 130, 101, 21));
      fumbleBox.setText("Patzer:");
    }
    return fumbleBox;
  }

  /**
   * This method initializes fumbleField	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getFumbleField() {
    if (fumbleField == null) {
      int min = (attacker instanceof dsa.model.characters.Hero) ? 2 : 6;
      if (farRanged) {
    	  min = 2;
      }
      
      fumbleField = new JSpinner(new SpinnerNumberModel(7, min, 12, 1));
      fumbleField.setBounds(new Rectangle(110, 130, 51, 21));
      fumbleField.setEnabled(false);
    }
    return fumbleField;
  }

  /**
   * This method initializes fumbleField2	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getFumbleField2() {
    if (fumbleField2 == null) {
      fumbleField2 = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));
      fumbleField2.setBounds(new Rectangle(120, 190, 51, 21));
      fumbleField2.setVisible(false);
    }
    return fumbleField2;
  }

  /**
   * This method initializes atRollButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAtRollButton() {
    if (atRollButton == null) {
      atRollButton = new JButton();
      atRollButton.setBounds(new Rectangle(180, 10, 41, 21));
      atRollButton.setIcon(ImageManager.getIcon("probe"));
    }
    return atRollButton;
  }
  
  private JButton getModButton() {
    if (modButton == null) {
      modButton = new JButton();
      modButton.setBounds(new Rectangle(180, 70, 41, 21));
      modButton.setText("...");
      modButton.setToolTipText("Modifikatoren");
    }
    return modButton;
  }

  /**
   * This method initializes tpRollButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getTpRollButton() {
    if (tpRollButton == null) {
      tpRollButton = new JButton();
      tpRollButton.setBounds(new Rectangle(180, 100, 41, 21));
      tpRollButton.setIcon(ImageManager.getIcon("probe"));
    }
    return tpRollButton;
  }

  /**
   * This method initializes fumbleButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getFumbleButton() {
    if (fumbleButton == null) {
      fumbleButton = new JButton();
      fumbleButton.setBounds(new Rectangle(180, 130, 41, 21));
      fumbleButton.setEnabled(false);
      fumbleButton.setIcon(ImageManager.getIcon("probe"));
    }
    return fumbleButton;
  }

  /**
   * This method initializes fumbleButton2	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getFumbleButton2() {
    if (fumbleButton2 == null) {
      fumbleButton2 = new JButton();
      fumbleButton2.setBounds(new Rectangle(180, 190, 41, 21));
      fumbleButton2.setEnabled(false);
      fumbleButton2.setIcon(ImageManager.getIcon("probe"));
    }
    return fumbleButton2;
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
      cancelButton.setBounds(new Rectangle(120, !isConnected ? 250 : 290, 101, 21));
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
      okButton.setBounds(new Rectangle(10, !isConnected ? 250 : 290, 101, 21));
      okButton.setText("OK");
      okButton.setDefaultCapable(true);
    }
    return okButton;
  }

  private void calcFumble() {
    int value = Dice.roll(6) + Dice.roll(6);
    if (!farRanged && !(attacker instanceof dsa.model.characters.Hero))  
      while (value < 6)
        value = Dice.roll(6) + Dice.roll(6);
    if (((Number)fumbleField.getValue()).intValue() != value) {
    	fumbleField.setValue(value);
    }
    else {
    	fumbleValueChanged();
    }
  }

  private void calcTP() {
    if (qualityField.getText().equals("--")) {
      tpField.setValue(0);
      tpField.setEnabled(false);
      tpRollButton.setEnabled(false);
    }
    else {
      int value = attacker.getTP(weaponNr).calcValue();
      int quality = Integer.parseInt(qualityField.getText());
      int atRoll = ((Number)atThrowField.getValue()).intValue();
      if (atRoll == 1) value += Dice.roll(6);
      if (farRanged) {
        String weaponName = attacker.getFightingWeapons().get(weaponNr);
        Weapon weapon = Weapons.getInstance().getWeapon(weaponName);
        value += weapon.getDistanceTPMod(distance);
      }
      tpField.setValue(value + quality);
      tpField.setEnabled(true);
      tpRollButton.setEnabled(true);
    }
  }

  private void calcSP() {
	if (!farRanged) {
	    int newValue = Dice.roll(6);
	    if (((Number)fumbleField.getValue()).intValue() == 12) {
	      newValue += Dice.roll(6);
	    }
	    fumbleField2.setValue(newValue);
	}
	else {
	      int tp = attacker.getTP(weaponNr).calcValue();
          String weaponName = attacker.getFightingWeapons().get(weaponNr);
	      Weapon weapon = Weapons.getInstance().getWeapon(weaponName);
	      tp += weapon.getDistanceTPMod(distance);
	      fumbleField2.setValue(tp);
	}
  }

  private void calcQualityAndTP() {
    int value = ((Number)atThrowField.getValue()).intValue();
    if (value == 20) {
      tpField.setValue(0);
      tpField.setEnabled(false);
      tpRollButton.setEnabled(false);
      qualityField.setText("--");
      fumbleBox.setSelected(true);
      fumbleField.setEnabled(true);
      fumbleButton.setEnabled(true);
      modButton.setEnabled(false);
      calcFumble();
    }
    else {
      fumbleBox.setSelected(false);
      fumbleButton2.setVisible(false);
      fumbleField.setEnabled(false);
      fumbleField2.setEnabled(false);
      fumbleField2.setVisible(false);
      fumbleLabel.setText("");
      fumbleLabel1.setText("");
      fumbleButton.setEnabled(false);
      tpRollButton.setEnabled(true);
      tpField.setEnabled(true);
      modButton.setEnabled(true);
      int toHit = at;
      toHit = Fighting.getWVModifiedAT(toHit, attacker.getFightingWeapons().get(weaponNr), 
          paradeBox.getSelectedItem().toString());
      toHit -= Fighting.getModifier(attacker, true, weaponNr);
      toHit -= extraMod;
      toHit -= farRangedMod;
      int quality = (toHit - value) / 2;
      if (farRanged) {
        if (value <= toHit) qualityField.setText("" + quality);
        else qualityField.setText("--");
      }
      else qualityField.setText("" + quality);
      calcTP();
    }
  }

  /**
   * This method initializes paradeBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getParadeBox() {
    if (paradeBox == null) {
      paradeBox = new JComboBox();
      paradeBox.setBounds(new Rectangle(110, 40, 111, 21));
    }
    return paradeBox;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

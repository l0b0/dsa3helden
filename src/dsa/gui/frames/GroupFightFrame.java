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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import dsa.control.Dice;
import dsa.control.Fighting;
import dsa.gui.dialogs.fighting.AttackDialog;
import dsa.gui.dialogs.fighting.ParadeDialog;
import dsa.gui.tables.FightTable;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.model.Fighter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.GroupObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.characters.Hero.DerivedValue;
import dsa.model.data.Opponent;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.remote.IServer.FightProperty;
import dsa.remote.RemoteFight;
import dsa.remote.RemoteManager;
import dsa.util.Strings;

public class GroupFightFrame extends SubFrame 
    implements GroupObserver, FightTable.Client, OptionsChange.OptionsListener {
  
  public GroupFightFrame() {
    super("Kampf (Meister)");
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    heroesTable = new FightTable(this);
    enemiesTable = new FightTable(this);
    JPanel heroesPanel = new JPanel(new BorderLayout());
    heroesPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "Helden"));
    heroesPanel.add(heroesTable.getPanelWithTable(), BorderLayout.CENTER);
    JPanel enemiesPanel = new JPanel(new BorderLayout());
    enemiesPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "Gegner"));
    enemiesPanel.add(enemiesTable.getPanelWithTable(), BorderLayout.CENTER);
    JPanel outerRoundsPanel = new JPanel(new BorderLayout());
    JPanel roundsPanel = new JPanel();
    roundsPanel.setLayout(new BoxLayout(roundsPanel, BoxLayout.X_AXIS));
    roundsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    roundsPanel.add(new JLabel("Kampfrunde:"));
    roundsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    roundsPanel.add(getRoundsSpinner());
    roundsPanel.add(Box.createHorizontalGlue());
    outerRoundsPanel.add(roundsPanel, BorderLayout.WEST);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    mainPanel.add(outerRoundsPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    mainPanel.add(heroesPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(enemiesPanel);
    this.setContentPane(mainPanel);
    myCharacterObserver = new MyCharacterObserver();
	for (Hero hero : Group.getInstance().getAllCharacters())  {
		hero.addHeroObserver(myCharacterObserver);
	}
    Group.getInstance().addObserver(this);
    OptionsChange.addListener(this);
    this.addWindowListener(new WindowAdapter() {
      
      boolean done = false;

      public void windowClosed(WindowEvent e) {
        if (!done) cleanup();
      }

      public void windowClosing(WindowEvent e) {
        cleanup();
      }
      
      private void cleanup() {
        Group.getInstance().removeObserver(GroupFightFrame.this);
        OptionsChange.removeListener(GroupFightFrame.this);
        for (Hero hero : Group.getInstance().getAllCharacters()) {
        	hero.removeObserver(myCharacterObserver);
        }
        heroesTable.saveSortingState("Helden");
        enemiesTable.saveSortingState("Gegner");
        done = true;
      }
    });
    heroesTable.restoreSortingState("Helden");
    enemiesTable.restoreSortingState("Gegner");
    updateData();
  }
  

  public String getHelpPage() {
    return "Kampf_Meister";
  }
  
  private MyCharacterObserver myCharacterObserver;
  
  private FightTable heroesTable;
  private FightTable enemiesTable;
  
  private JSpinner roundsSpinner;
  private boolean listen = true;
  
  private JSpinner getRoundsSpinner() {
	  if (roundsSpinner == null) {
		  roundsSpinner = new JSpinner();
		  roundsSpinner.setModel(new SpinnerNumberModel(1, 1, 999, 1));
	      roundsSpinner.addChangeListener(new ChangeListener() {
	          public void stateChanged(ChangeEvent evt) {
	        	  updateKr();
	          }
	      });
	      roundsSpinner.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						roundsSpinner.commitEdit();
						updateKr();
					}
					catch (ParseException e) {
						
					}
				}
			}
	    	  
	      });
	  }
	  return roundsSpinner;
  }
  
  private void updateKr() {
 	 if (!listen) 
		 return;
	 int kr = ((Integer)roundsSpinner.getModel().getValue()).intValue();
	 Group.getInstance().setKR(kr);
	 if (RemoteManager.getInstance().isConnectedAsGM()) {
		 RemoteManager.getInstance().informPlayersOfKRChange(kr);
	 }
  }
  
  private void updateData() {
  listen = false;
    heroesTable.clear();
    enemiesTable.clear();
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      if (!hero.isDifference()) heroesTable.addFighter(hero);
    }
    for (String enemyName : Group.getInstance().getOpponentNames()) {
      Opponent o = Group.getInstance().getOpponent(enemyName);
      enemiesTable.addFighter(o);
      o.addObserver(new MyOpponentObserver(o));
    }
    updateOpponentNames();
    getRoundsSpinner().setValue(Group.getInstance().getKR());
    listen = true;
  }
  
  private void updateOpponentNames() {
    ArrayList<Fighter> opponents1 = new ArrayList<Fighter>();
    ArrayList<Fighter> opponents2 = new ArrayList<Fighter>();
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      if (!hero.isDifference()) opponents2.add(hero);
    }
    for (String enemyName : Group.getInstance().getOpponentNames()) {
      Opponent opponent = Group.getInstance().getOpponent(enemyName);
      opponents1.add(opponent);
      opponents2.add(opponent);
    }
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      if (!hero.isDifference()) opponents1.add(hero);
    }
    heroesTable.setOpponents(opponents1);
    enemiesTable.setOpponents(opponents2);
  }
  
  private void updateData(Hero hero) {
    heroesTable.updateFighter(hero);
  }
  
  private void updateData(Opponent opponent) {
    enemiesTable.updateFighter(opponent);
  }

  public void groupLoaded() {
    updateData();
  }
  
  public void orderChanged() {
    updateData();
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    updateData();
  }

  public void characterAdded(Hero character) {
    if (!character.isDifference()) heroesTable.addFighter(character);
    character.addHeroObserver(myCharacterObserver);
  }

  public void characterRemoved(Hero character) {
	character.removeHeroObserver(myCharacterObserver);
    updateData();
  }
  
  public void opponentsChanged() {
    updateData();
  }
  
  public void characterReplaced(Hero oldHero, Hero newHero) {
	  oldHero.removeHeroObserver(myCharacterObserver);
	  newHero.addHeroObserver(myCharacterObserver);
	  updateData();
  }

  public void globalLockChanged() {
  }

/* 
  A. Held greift an
  
    1) Qualität und TP manuell eingeben
    2) Qualität und TP automatisch würfeln
    3) Patzer
  
  B. Gegner greift an
  
     -- wie A.
  
  C. Held wird angegriffen
  
    1) Treffer hinnehmen
    2) Kein Treffer (fix)
    3) Automatisch Ausweichen --> Bonus eingeben!
    4) Automatisch Parieren
    5) Patzer
  
    (Manuell Ausweichen / Parieren --> 1/2)
  
  D. Gegner wird angegriffen
  
     -- wie C, aber ohne Option 3
  
  Fernkampfangriff:
  
  - bei 1, wenn gewürfelt wird anders
  - 2, fällt weg
  
  Bei Gegnern folgende Patzertypen nicht: Waffe verloren, Waffe beschädigt   
  */
  
  public void doAttack(Fighter fighter, int weaponIndex, final Fighter opponent) {
    String weaponName = fighter.getFightingWeapons().get(weaponIndex);
    Weapon weapon = Weapons.getInstance().getWeapon(weaponName);
    boolean farRanged = (weapon != null) && weapon.isFarRangedWeapon();
    String[] opponentWeapons = new String[opponent.getFightingWeapons().size()];
    opponentWeapons = opponent.getFightingWeapons().toArray(opponentWeapons);
    
    RemoteFight.Attack receivedAt = null;
    if (fighter instanceof Hero && RemoteManager.getInstance().isConnectedAsGM()) {
    	receivedAt = RemoteManager.getInstance().getLastAttackByHero((Hero)fighter);
    	if (receivedAt != null) {
    		switch (JOptionPane.showConfirmDialog(this, "Empfangene AT verwenden?", "Heldenverwaltung", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
    		case JOptionPane.YES_OPTION:
    			break;
    		case JOptionPane.NO_OPTION:
    			receivedAt = null;
    			break;
    		case JOptionPane.CANCEL_OPTION:
			default:
    			return;
    		}
    		RemoteManager.getInstance().removeLastAttackByHero((Hero)fighter);
    	}
    }
    
    AttackDialog.AttackResult result = null;
    if (receivedAt == null) {
    	AttackDialog dialog = new AttackDialog(this, fighter, weaponIndex, opponentWeapons);
    	dialog.setVisible(true);
    	result = dialog.getResult();
    }
    else {
    	int paIndex = opponentWeapons.length > 0 ? 0 : -1;
    	result = new AttackDialog.AttackResult(false, true, receivedAt.getQuality(), receivedAt.getTP(), paIndex);
    	result.setCopy(false);
    	result.setSendToPlayer(false);
    	result.setSendToAll(false);
    	farRanged = receivedAt.isProjectile();
    }
    
    if (result == null) return; // user cancelled
    
    if ((fighter instanceof Hero) && Fighting.Flammenschwert1.equals(weaponName) && receivedAt == null) {
    	if (((Hero)fighter).getCurrentEnergy(Energy.AE) < 2) {
    		JOptionPane.showMessageDialog(this, "Nicht genügend ASP für die Attacke!", "Heldenverwaltung", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}
    	((Hero)fighter).changeCurrentEnergy(Energy.AE, -2);
    }
    else if ((fighter instanceof Hero) && Fighting.Flammenschwert2.equals(weaponName) && receivedAt == null) {
    	if (((Hero)fighter).getCurrentEnergy(Energy.AE) < 3) {
    		JOptionPane.showMessageDialog(this, "Nicht genügend ASP für die Attacke!", "Heldenverwaltung", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}
    	((Hero)fighter).changeCurrentEnergy(Energy.AE, -3);
    }
    
    fighter.setATBonus(weaponIndex, 0);
    fighter.setHasStumbled(false);
    if (RemoteManager.getInstance().isConnected() && (fighter instanceof Hero) && result.sendToPlayer()) {
    	RemoteManager.getInstance().informOfFightPropertyChange((Hero)fighter, dsa.remote.IServer.FightProperty.stumbled, result.sendToAll());
    	if (weaponIndex == 0) 
    		RemoteManager.getInstance().informOfFightPropertyChange((Hero)fighter, dsa.remote.IServer.FightProperty.atBonus1, result.sendToAll());
    	else
    		RemoteManager.getInstance().informOfFightPropertyChange((Hero)fighter, dsa.remote.IServer.FightProperty.atBonus2, result.sendToAll());
    }
    
    String text = "";
    
    if (result.hasFumbled()) {
      int fumbleType = result.getFumbleType();
      int tp = result.getTP();
      text += "Patzer! ";
      text += doFumble(fighter, weaponName, fumbleType, tp, result.sendToPlayer(), result.sendToAll());
    }
    else if (!result.hasHit()) {
    	text += " Nicht getroffen.";
    }
    else {
    	text += " Qualität " + result.getQuality() + ", " + result.getTP() + " TP";
    }
    if (result.copy()) {
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(new StringSelection(text), new ClipboardOwner() {
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
			}
		});    	
    }
    if (result.sendToPlayer()) {
    	if (fighter instanceof Hero) {
    		if (weapon != null && Weapons.isFarRangedCategory(weapon.getType())) {
    			RemoteManager.getInstance().informOfHeroProjectileAT((Hero)fighter, text, !result.hasFumbled() && result.hasHit(), 
    					result.getQuality(), result.getTP(), result.sendToAll());
    		}
    		else {
    			RemoteManager.getInstance().informOfHeroAt((Hero)fighter, text, result.getQuality(), !result.hasFumbled() && result.hasHit(), result.getTP(), 
    				weapon != null && Weapons.isAUCategory(weapon.getType()), result.sendToAll());
    		}
    	}
    	else if (opponent instanceof Hero) {
    		if (weapon != null && Weapons.isFarRangedCategory(weapon.getType())) {
    			RemoteManager.getInstance().informOfOpponentProjectileAT((Hero)opponent, (Opponent)fighter, text, !result.hasFumbled() && result.hasHit(), 
    					result.getQuality(), result.getTP(), result.sendToAll());
    		}
    		else {
    			RemoteManager.getInstance().informOfOpponentAt((Hero)opponent, (Opponent)fighter, text, result.getQuality(), !result.hasFumbled() && result.hasHit(), result.getTP(), 
    				weapon != null && Weapons.isAUCategory(weapon.getType()), result.sendToAll());
    		}
    	}
    }
    
    if (result.hasFumbled()) return;
    if (!result.hasHit()) return;
    
    if (farRanged && result.getTP() <= 0) return;
    
    if (!(fighter instanceof Hero)) {
    	if (JOptionPane.showConfirmDialog(this, "Auch Parade für Held würfeln?", "Heldenverwaltung", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
    		return;
    	}
    }
    
    String paText = "";
    ParadeDialog.ParadeResult paradeResult = null;
    if (Fighting.canDefend(opponent)) {
      String paradeWeapon = null;
      if (Group.getInstance().getOptions().useWV()) {
        if (result.getParadeIndex() == -1) paradeWeapon = "Nichts";
        else if (result.getParadeIndex() == -2) paradeWeapon = fighter.getOpponentWeapon(weaponIndex);
        else paradeWeapon = opponent.getFightingWeapons().get(result.getParadeIndex());
      }          
      String attackWeapon = weapon != null ? weapon.getName() : weaponName;
      ParadeDialog dialog2 = new ParadeDialog(this, opponent, result.getQuality(), 
          attackWeapon, paradeWeapon);
      dialog2.setVisible(true);
      paradeResult = dialog2.getResult();
      ParadeDialog.ParadeOutcome outcome = paradeResult.getOutcome();
      boolean success = true;
      if (outcome == ParadeDialog.ParadeOutcome.NoHit)
        paText = paradeResult.isEvasion() ? "Ausgewichen." : "Pariert.";
      else if (outcome == ParadeDialog.ParadeOutcome.Canceled)
        return;
      else if (outcome == ParadeDialog.ParadeOutcome.Fumble) {
        paText = "Patzer! " + doFumble(opponent, paradeResult.getWeapon(), paradeResult.getFumbleType(), 
            paradeResult.getFumbleSP(), paradeResult.sendToPlayer(), paradeResult.sendToAll());
        success = false;
      }
      else {
    	  paText = paradeResult.isEvasion() ? "Nicht ausgewichen." : "Nicht pariert.";
    	  success = false;
      }
      if (paradeResult.sendToPlayer()) {
    	  if (fighter instanceof Hero) {
    		  RemoteManager.getInstance().informOfOpponentPA((Hero)fighter, (Opponent)opponent, paText, paradeResult.sendToAll());
    	  }
    	  else {
    		  RemoteManager.getInstance().informOfHeroPA((Hero)opponent, paText, success, paradeResult.sendToAll());
    	  }
      }
      if (success)
    	  return;
    }
    else {
      JOptionPane.showMessageDialog(this, Strings.cutTo(opponent.getName(), ' ') + 
          " kann sich nicht verteidigen und wird getroffen!", "Kampf", 
          JOptionPane.PLAIN_MESSAGE, ImageManager.getIcon("hit"));
    }
    
    boolean useAU = weapon != null && Weapons.isAUCategory(weapon.getType());
    boolean hasShield = false;
    if (opponent instanceof Hero) {
    	Hero hero = (Hero) opponent;
    	if (hero.getFightMode().startsWith("Waffe + Parade")) {
    		Shield shield = Shields.getInstance().getShield(hero.getSecondHandItem());
    		if (shield != null && shield.getFkMod() != 0) {
    			hasShield = true;
    		}
    	}
    }
    try {
      RemoteManager.getInstance().setListenForHeroChanges(false);
      boolean sendToPlayer = (paradeResult == null && result.sendToPlayer()) || (paradeResult != null && paradeResult.sendToPlayer());
	  boolean sendToOthers = (paradeResult == null && result.sendToAll()) || (paradeResult != null && paradeResult.sendToAll());
      String hitResult = Fighting.doHit(opponent, result.getTP(), useAU, false, hasShield, this, new HitUpdate(sendToPlayer, sendToOthers));
      if ((opponent instanceof Hero) && sendToPlayer) {
    	  RemoteManager.getInstance().informOfHit((Hero)opponent, hitResult, sendToOthers);
      }
    }
    finally {
  	  RemoteManager.getInstance().setListenForHeroChanges(true);
    }
    
    if (opponent instanceof Hero) {
      heroesTable.updateFighter(opponent);
    }
    else {
      enemiesTable.updateFighter(opponent);
    }
  }
  
  public Component getMessageBoxParent() {
	  return this;
  }
  
  private static class HitUpdate implements Fighting.UpdateCallbacks {
	  private boolean sendToPlayer;
	  private boolean sendToOthers;
	  public HitUpdate(boolean sendToPlayer, boolean sendToOthers) {
		  this.sendToPlayer = sendToPlayer;
		  this.sendToOthers = sendToOthers;
	  }
    public void updateData() {}
	public void fightPropertyChanged(Fighter fighter, FightProperty fp) {
		if (sendToPlayer && (fighter instanceof Hero)) {
			RemoteManager.getInstance().informOfFightPropertyChange((Hero)fighter, fp, sendToOthers);
		}
	}
  }


  private String doFumble(Fighter fighter, String weaponName, int fumbleType, int sp, boolean sendToPlayer, boolean sendToAll) {
	String info = "";
    switch (fumbleType) {
    case 2:
      info = Strings.cutTo(fighter.getName(), ' ') + " verliert die Waffe "
              + weaponName + "!";
      JOptionPane.showMessageDialog(this, "Kampf", 
          info, JOptionPane.PLAIN_MESSAGE);
      if (fighter instanceof Hero) {
        int bfRoll = Dice.roll(6) + Dice.roll(6);
        Hero hero = (Hero) fighter;
        boolean breakWeapon = true;
        int bf = hero.getBF(weaponName, 1);
        if (bfRoll > bf) {
          if (bfRoll < 10) ++bf;
          bfRoll = Dice.roll(6) + Dice.roll(6);
          if (bfRoll > bf) {
            breakWeapon = false;
          }
          else if (bfRoll < 10) ++bf;
        }
        if (breakWeapon) {
          JOptionPane.showMessageDialog(this, "Kampf", 
              "Die Waffe zerbricht!", JOptionPane.PLAIN_MESSAGE);
          hero.removeWeapon(weaponName);
          info += "Die Waffe zerbricht!";
        }
        else {
          hero.setBF(weaponName, 1, bf);
        }
      }
      break;
    case 3:
    	info = Strings.cutTo(fighter.getName(), ' ') + " verliert die Waffe "
    	          + weaponName + "!"; 
      JOptionPane.showMessageDialog(this, "Kampf", 
          info, JOptionPane.PLAIN_MESSAGE);
      break;
    case 4:
    case 5:
      info = "Die Waffe ist beschädigt.";
      if (fighter instanceof Hero) {
        int bfRoll = Dice.roll(6) + Dice.roll(6);
        Hero hero = (Hero) fighter;
        boolean breakWeapon = true;
        int bf = hero.getBF(weaponName, 1);
        if (bfRoll > bf) {
          if (bfRoll < 10) ++bf;
          breakWeapon = false;
        }
        if (breakWeapon) {
          JOptionPane.showMessageDialog(this, "Kampf", 
              "Die Waffe zerbricht!", JOptionPane.PLAIN_MESSAGE);
          info += "Die Waffe zerbricht!";
          hero.removeWeapon(weaponName);
        }
        else {
          hero.setBF(weaponName, 1, bf);
        }
      }
      break;
    case 6:
    case 7:
    case 8:
      info = "Stolpern";
      fighter.setHasStumbled(true);
      if (RemoteManager.getInstance().isConnected() && (fighter instanceof Hero) && sendToPlayer)
      	RemoteManager.getInstance().informOfFightPropertyChange((Hero) fighter, dsa.remote.IServer.FightProperty.stumbled, sendToAll);
      break;
    case 9:
    case 10:
      info = "Sturz";
      fighter.setGrounded(true);
      if (fighter instanceof Hero) updateData((Hero)fighter);
      else updateData((Opponent)fighter);
      if (RemoteManager.getInstance().isConnected() && (fighter instanceof Hero) && sendToPlayer)
        	RemoteManager.getInstance().informOfFightPropertyChange((Hero) fighter, dsa.remote.IServer.FightProperty.grounded, sendToAll);
      break;
    case 11:
      info = "Selbst verletzt.";
    case 12:
      if (info.equals("")) info = "Selbst schwer verletzt."; 
      fighter.setCurrentLE(fighter.getCurrentLE() - sp);
      if (fighter instanceof Hero) updateData((Hero)fighter);
      else updateData((Opponent)fighter);
      break;
    default:
        break;
    }
    return info;
  }
  
  private class MyOpponentObserver implements Opponent.OpponentObserver {
    public void opponentChanged() {
      updateData(opponent);
    }
    public MyOpponentObserver(Opponent o) {
      opponent = o;
    }
    private Opponent opponent;
  }
  
  private class MyCharacterObserver implements CharacterObserver {
    public void armourRemoved(String armour) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void atPADistributionChanged(String talent) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void beModificationChanged() {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void bfChanged(String item) {
    }

    public void currentEnergyChanged(Energy energy) {
      if (energy == Energy.LE) {
      	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
      }
    }

    public void currentPropertyChanged(Property property) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void currentTalentChanged(String talent) {
      if (Talents.getInstance().getTalent(talent).isFightingTalent()) {
      	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
      }
    }

    public void defaultEnergyChanged(Energy energy) {
      if (energy == Energy.LE) {
      	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
      }
    }

    public void defaultPropertyChanged(Property property) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void defaultTalentChanged(String talent) {
      if (Talents.getInstance().getTalent(talent).isFightingTalent()) {
      	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
      }
    }

    public void derivedValueChanged(DerivedValue dv) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void increaseTriesChanged() {
    }

    public void nameChanged(String oldName, String newName) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void shieldRemoved(String name) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void stepIncreased() {
    }

    public void talentAdded(String talent) {
    }

    public void talentRemoved(String talent) {
    }

    public void thingRemoved(String thing, boolean fromWarehouse) {
    }

    public void thingsChanged() {
    }

    public void weaponRemoved(String weapon) {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }

    public void weightChanged() {
    }
    
    public void activeWeaponsChanged() {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }
    
    public void fightingStateChanged() {
    	for (Hero hero : Group.getInstance().getAllCharacters()) {
    		updateData(hero);
    	}
    }
    
    public void opponentWeaponChanged(int weaponNr) {
      // only used in the attack dialog
    }
    
    public void moneyChanged() {
    }
    
  }

  public void optionsChanged() {
    heroesTable.dazedOptionChanged();
    enemiesTable.dazedOptionChanged();
  }

}

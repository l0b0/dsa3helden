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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import dsa.model.data.Talents;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
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
    mainPanel.add(heroesPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(enemiesPanel);
    this.setContentPane(mainPanel);
    myCharacterObserver = new MyCharacterObserver();
    Hero currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) {
      currentHero.addHeroObserver(myCharacterObserver);
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
        done = true;
      }
    });
    updateData();
  }
  

  public String getHelpPage() {
    return "Kampf_Meister";
  }
  
  private MyCharacterObserver myCharacterObserver;
  
  private FightTable heroesTable;
  private FightTable enemiesTable;
  
  private void updateData() {
    heroesTable.clear();
    enemiesTable.clear();
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      heroesTable.addFighter(hero);
    }
    for (String enemyName : Group.getInstance().getOpponentNames()) {
      Opponent o = Group.getInstance().getOpponent(enemyName);
      enemiesTable.addFighter(o);
      o.addObserver(new MyOpponentObserver(o));
    }
    updateOpponentNames();
  }
  
  private void updateOpponentNames() {
    ArrayList<Fighter> opponents1 = new ArrayList<Fighter>();
    ArrayList<Fighter> opponents2 = new ArrayList<Fighter>();
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      opponents2.add(hero);
    }
    for (String enemyName : Group.getInstance().getOpponentNames()) {
      Opponent opponent = Group.getInstance().getOpponent(enemyName);
      opponents1.add(opponent);
      opponents2.add(opponent);
    }
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      opponents1.add(hero);
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
    if (oldCharacter != null) {
      oldCharacter.removeHeroObserver(myCharacterObserver);
    }
    if (newCharacter != null) {
      newCharacter.addHeroObserver(myCharacterObserver);
    }
  }

  public void characterAdded(Hero character) {
    heroesTable.addFighter(character);
  }

  public void characterRemoved(Hero character) {
    updateData();
  }
  
  public void opponentsChanged() {
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
    AttackDialog dialog = new AttackDialog(this, fighter, weaponIndex);
    dialog.setVisible(true);
    AttackDialog.AttackResult result = dialog.getResult();
    if (result == null) return; // user cancelled
    
    fighter.setATBonus(weaponIndex, 0);
    fighter.setHasStumbled(false);
    
    if (result.hasFumbled()) {
      int fumbleType = result.getFumbleType();
      int tp = result.getTP();
      doFumble(fighter, weaponName, fumbleType, tp);
      return;
    }
    
    if (farRanged && result.getTP() <= 0) return;
    
    if (!farRanged && Fighting.canDefend(opponent)) {
      ParadeDialog dialog2 = new ParadeDialog(this, opponent, result.getQuality());
      dialog2.setVisible(true);
      ParadeDialog.ParadeResult paradeResult = dialog2.getResult();
      ParadeDialog.ParadeOutcome outcome = paradeResult.getOutcome();
      if (outcome == ParadeDialog.ParadeOutcome.NoHit)
        return;
      else if (outcome == ParadeDialog.ParadeOutcome.Canceled)
        return;
      else if (outcome == ParadeDialog.ParadeOutcome.Fumble)
        doFumble(opponent, paradeResult.getWeapon(), paradeResult.getFumbleType(), 
            paradeResult.getFumbleSP());
    }
    else {
      JOptionPane.showMessageDialog(this, Strings.cutTo(opponent.getName(), ' ') + 
          " kann sich nicht verteidigen und wird getroffen!", "Kampf", 
          JOptionPane.PLAIN_MESSAGE, ImageManager.getIcon("hit"));
    }
    
    boolean useAU = weapon != null && Weapons.isAUCategory(weapon.getType());
    Fighting.doHit(opponent, result.getTP(), useAU, false, this, new NoUpdate());
    if (opponent instanceof Hero) {
      heroesTable.updateFighter(opponent);
    }
    else {
      enemiesTable.updateFighter(opponent);
    }
  }
  
  private static class NoUpdate implements Fighting.UpdateCallbacks {
    public void updateData() {}
  }


  private void doFumble(Fighter fighter, String weaponName, int fumbleType, int sp) {
    switch (fumbleType) {
    case 2:
      JOptionPane.showMessageDialog(this, "Kampf", 
          Strings.cutTo(fighter.getName(), ' ') + " verliert die Waffe "
          + weaponName + "!", JOptionPane.PLAIN_MESSAGE);
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
        }
        else {
          hero.setBF(weaponName, 1, bf);
        }
      }
      break;
    case 3:
      JOptionPane.showMessageDialog(this, "Kampf", 
          Strings.cutTo(fighter.getName(), ' ') + " verliert die Waffe "
          + weaponName + "!", JOptionPane.PLAIN_MESSAGE);
      break;
    case 4:
    case 5:
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
      fighter.setHasStumbled(true);
      break;
    case 9:
    case 10:
      fighter.setGrounded(true);
      if (fighter instanceof Hero) updateData((Hero)fighter);
      else updateData((Opponent)fighter);
      break;
    case 11:
    case 12:
      fighter.setCurrentLE(fighter.getCurrentLE() - sp);
      if (fighter instanceof Hero) updateData((Hero)fighter);
      else updateData((Opponent)fighter);
      break;
    default:
        break;
    }
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
      updateData(Group.getInstance().getActiveHero());
    }

    public void atPADistributionChanged(String talent) {
      updateData(Group.getInstance().getActiveHero());
    }

    public void beModificationChanged() {
      updateData(Group.getInstance().getActiveHero());
    }

    public void bfChanged(String item) {
    }

    public void currentEnergyChanged(Energy energy) {
      if (energy == Energy.LE) {
        updateData(Group.getInstance().getActiveHero());        
      }
    }

    public void currentPropertyChanged(Property property) {
      updateData(Group.getInstance().getActiveHero());
    }

    public void currentTalentChanged(String talent) {
      if (Talents.getInstance().getTalent(talent).isFightingTalent()) {
        updateData(Group.getInstance().getActiveHero());
      }
    }

    public void defaultEnergyChanged(Energy energy) {
      if (energy == Energy.LE) {
        updateData(Group.getInstance().getActiveHero());
      }
    }

    public void defaultPropertyChanged(Property property) {
      updateData(Group.getInstance().getActiveHero());
    }

    public void defaultTalentChanged(String talent) {
      if (Talents.getInstance().getTalent(talent).isFightingTalent()) {
        updateData(Group.getInstance().getActiveHero());
      }
    }

    public void derivedValueChanged(DerivedValue dv) {
      updateData(Group.getInstance().getActiveHero());
    }

    public void increaseTriesChanged() {
    }

    public void nameChanged(String oldName, String newName) {
      updateData(Group.getInstance().getActiveHero());
    }

    public void shieldRemoved(String name) {
      updateData(Group.getInstance().getActiveHero());
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
      updateData(Group.getInstance().getActiveHero());
    }

    public void weightChanged() {
    }
    
    public void activeWeaponsChanged() {
      updateData(Group.getInstance().getActiveHero());
    }
    
    public void fightingStateChanged() {
      updateData(Group.getInstance().getActiveHero());
    }
    
  }

  public void optionsChanged() {
    heroesTable.dazedOptionChanged();
    enemiesTable.dazedOptionChanged();
  }

}

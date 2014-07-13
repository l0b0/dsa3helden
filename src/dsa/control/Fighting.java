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
package dsa.control;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dsa.gui.util.ImageManager;
import dsa.model.DiceSpecification;
import dsa.model.Fighter;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.FightingTalent;
import dsa.util.Optional;

public final class Fighting {

  private Fighting() {}
  
  private static int getNormalATSkill(Hero hero, int category) {
    String talent = Weapons.getCategoryName(category);
    int skill = hero.getATPart(talent);
    if (skill < 0) skill = 0;
    skill += hero.getCurrentDerivedValue(Hero.DerivedValue.AT);
    int be = hero.getBE();
    be += ((FightingTalent) Talents.getInstance().getTalent(talent))
        .getBEMinus();
    if (be < 0) be = 0;
    skill -= be / 2;
    return skill;
  }

  private static int getNormalPASkill(Hero hero, int category) {
    String talent = Weapons.getCategoryName(category);
    int skill = hero.getPAPart(talent);
    if (skill < 0) skill = 0;
    skill += hero.getCurrentDerivedValue(Hero.DerivedValue.PA);
    int be = hero.getBE();
    be += ((FightingTalent) Talents.getInstance().getTalent(talent))
        .getBEMinus();
    if (be < 0) be = 0;
    skill -= Math.round(Math.ceil((double) be / 2.0));
    return skill;
  }

  public static boolean canAttack(Fighter hero) {
    boolean withDazed = Group.getInstance().getOptions().hasQvatStunned();
    return (!withDazed || !hero.isDazed()) && canDefend(hero);
  }

  public static boolean canDefend(Fighter hero) {
    boolean leOK = hero.getCurrentLE() > 5;
    if (hero instanceof Hero) {
      return leOK && ((Hero)hero).getCurrentEnergy(Energy.AU) > 0;
    }
    else return leOK;
  }
  
  public static int getModifier(Fighter fighter, boolean isAttack, int weaponNr) {
    int mod = 0;
    if (fighter.hasStumbled()) {
      mod += 5;
    }
    if (fighter.isGrounded()) {
      mod += isAttack ? 7 : 5;
    }
    mod -= fighter.getATBonus(weaponNr);
    mod += Markers.getMarkers(fighter);
    return mod;
  }
  
  private static Weapon.WV getWeaponWV(String weapon) {
    if ("Raufen".equals(weapon)) {
      return new Weapon.WV(2, 0);      
    }
    else if ("Ringen".equals(weapon)) {
      return new Weapon.WV(2, 0);      
    }
    else if ("Boxen".equals(weapon)) {
      return new Weapon.WV(2, 0);
    }
    else if ("Hruruzat".equals(weapon)) {
      return new Weapon.WV(3, 1);
    }
    else if (Flammenschwert1.equals(weapon)) {
    	return new Weapon.WV(0, 0);
    }
    else if (Flammenschwert2.equals(weapon)) {
    	return new Weapon.WV(0, 0);
    }
    else {
      Weapon w = Weapons.getInstance().getWeapon(weapon);
      if (w != null) return w.getWV();
      else return null;
    }
  }
  
  public static int getWVModifiedAT(int defaultAT, String atWeapon, String paWeapon) {
    if (atWeapon == null || paWeapon == null) {
      return defaultAT;
    }
    if (Group.getInstance().getOptions().useWV()) {
      Weapon.WV atWV = getWeaponWV(atWeapon);
      Weapon.WV paWV = getWeaponWV(paWeapon);
      if (atWV == null || paWV == null) return defaultAT;
      if (atWV.getAT() > paWV.getPA()) {
        return defaultAT; // wv changes the PA
      }
      else {
        int modifiedAT = defaultAT - (paWV.getPA() - atWV.getAT());
        return modifiedAT > 0 ? modifiedAT : 0;
      }
    }
    else return defaultAT;
  }
  
  public static int getWVModifiedPA(int defaultPA, String atWeapon, String paWeapon) {
    if (atWeapon == null || paWeapon == null) {
      return defaultPA;
    }
    if (Group.getInstance().getOptions().useWV()) {
      Weapon.WV atWV = getWeaponWV(atWeapon);
      Weapon.WV paWV = getWeaponWV(paWeapon);
      if (atWV == null || paWV == null) return defaultPA;
      if (atWV.getAT() < paWV.getPA()) {
        return defaultPA; // wv changes the AT
      }
      else {
        int modifiedPA = defaultPA - (atWV.getAT() - paWV.getPA());
        return modifiedPA > 0 ? modifiedPA : 0;
      }
    }    
    else return defaultPA;
  }
  
  public static Optional<Integer> getFirstATValue(Hero hero) {
    int atMod = 0;
    if (hero.getFightMode().equals("Fernkampf")) {
      String s = hero.getFirstHandWeapon();
      Weapon w = Weapons.getInstance().getWeapon(s);
      String talent = Weapons.getCategoryName(w.getType());
      int skill = hero.getCurrentTalentValue(talent);
      skill += hero.getCurrentDerivedValue(Hero.DerivedValue.FK);
      int be = hero.getBE();
      be += ((FightingTalent) Talents.getInstance().getTalent(talent))
          .getBEMinus();
      if (be < 0) be = 0;
      skill -= be;     
      return new Optional<Integer>(skill);
    }
    else if (hero.getFightMode().equals("Waffe + Parade")) {
      Object o2 = hero.getSecondHandItem();
      if (o2 != null) {
        Shield shield = Shields.getInstance().getShield(o2.toString());
        if (shield != null) atMod = shield.getAtMod();
      }
    }
    int category = 0;
    String s = hero.getFirstHandWeapon();
    if (hero.getFightMode().equals("Waffenlos")) {
       category = Weapons.getCategoryIndex(s);
    }
    else if (s == null) {
      category  = Weapons.getCategoryIndex("Raufen");
    }
    else if (s.equals(Flammenschwert1)) {
    	return new Optional<Integer>(15);
    }
    else if (s.equals(Flammenschwert2)) {
    	return new Optional<Integer>(15);
    }
    else {
      category = Weapons.getInstance().getWeapon(s).getType();
    }
    if (category == -1) 
      category = Weapons.getCategoryIndex("Raufen");
    return new Optional<Integer>(getNormalATSkill(hero, category) + atMod);    
  }

  public static Optional<Integer> getFirstPAValue(Hero hero) {
    if (canAttack(hero)) {
      int paMod = 0;
      if (hero.getFightMode().equals("Fernkampf")) {
        return new Optional<Integer>(hero.getCurrentDerivedValue(Hero.DerivedValue.PA));
      }
      if (hero.getFightMode().equals("Waffe + Parade")) {
        Object o2 = hero.getSecondHandItem();
        if (o2 != null) {
          Shield shield = Shields.getInstance().getShield(o2.toString());
          int leftSkill = hero.getCurrentTalentValue("Linkshändig");
          if (shield != null) paMod = leftSkill > 8 ? shield.getPaMod2() : shield.getPaMod();
        }
      }
      int category = 0;
      String s = hero.getFirstHandWeapon();
      if (hero.getFightMode().equals("Waffenlos")) {
         category = Weapons.getCategoryIndex(s);
      }
      else if (s == null) {
        category = Weapons.getCategoryIndex("Raufen");
      }
      else if (s.equals(Flammenschwert1)) {
    	  category = Weapons.getInstance().getWeapon("Langschwert").getType();
      }
      else if (s.equals(Flammenschwert2)) {
    	  category = Weapons.getCategoryIndex("Raufen");
    	  paMod -= 5;
      }
      else {
        category = Weapons.getInstance().getWeapon(s).getType();
      }
      if (category == -1)
        category = Weapons.getCategoryIndex("Raufen");
      return new Optional<Integer>(getNormalPASkill(hero, category) + paMod);
    }
    else {
      return new Optional<Integer>(hero.getCurrentDerivedValue(Hero.DerivedValue.PA));
    }    
  }
  
  public static Optional<Integer> getSecondATValue(Hero hero) {
    int value = hero.getCurrentTalentValue("Linkshändig");
    value = -7 + Math.round(value / 2.0f);
    if (value > 0) value = 0;
    String weapon = hero.getSecondHandItem();
    if (weapon != null) {
      int category = Weapons.getInstance().getWeapon(weapon).getType();
      return new Optional<Integer>(getNormalATSkill(hero, category) + value);
    }
    else return Optional.NULL_INT;
  }
  
  public static Optional<Integer> getSecondPAValue(Hero hero) {
    if (hero.getFightMode().equals("Waffe + Parade, separat")) {
      int be = 0;
      if (canAttack(hero)) {
        int leftSkill = hero.getCurrentTalentValue("Linkshändig");
        leftSkill += -7 + leftSkill - Math.round(leftSkill / 2.0f);
        leftSkill += hero.getCurrentDerivedValue(Hero.DerivedValue.PA);
        be = hero.getBE();
        if (be < 0) be = 0;
        leftSkill -= Math.round(Math.ceil((double) be / 2.0));
        return new Optional<Integer>(leftSkill);
      }
      else {
        return new Optional<Integer>(hero.getCurrentDerivedValue(Hero.DerivedValue.PA));
      }
    }
    else if (hero.getFightMode().equals("Zwei Waffen")){
      int value = hero.getCurrentTalentValue("Linkshändig");
      value = -7 + value - Math.round(value / 2.0f);
      if (value > 0) value = 0;
      if (canAttack(hero)) {
        String weapon = hero.getSecondHandItem();
        int category = Weapons.getInstance().getWeapon(weapon).getType();
        return new Optional<Integer>(getNormalPASkill(hero, category) + value);
      }
      else {
        return new Optional<Integer>(hero.getCurrentDerivedValue(Hero.DerivedValue.PA) + value);
      }
    }
    else return Optional.NULL_INT;
  }

  private static int getKKBonus(Hero hero, int border) {
    int kk = hero.getCurrentProperty(Property.KK);
    if (kk <= border)
      return 0;
    else
      return kk - border;
  }

  private static int getKKBonus(Hero hero, Weapon w) {
    if (Weapons.getCategoryName(w.getType()).equals("Schußwaffen")) {
      return 0;
    }
    if (w.getKKBonus().hasValue()) {
      return getKKBonus(hero, w.getKKBonus().getValue());
    }
    else
      return 0;
  }
  
  private static DiceSpecification getTP(Hero hero, String weapon) {
    if (weapon == null) return DiceSpecification.create(1, 6, 0);
    if (weapon.equals(Flammenschwert1)) {
    	return DiceSpecification.create(1, 6, hero.getStep());
    }
    else if (weapon.equals(Flammenschwert2)) {
    	return DiceSpecification.create(1, 6, hero.getStep());
    }
    else {
	    Weapon w = Weapons.getInstance().getWeapon(weapon);
	    int wDamage = w.getW6damage();
	    int constDamage = w.getConstDamage();
	    constDamage += getKKBonus(hero, w);    
	    return DiceSpecification.create(wDamage, 6, constDamage);
    }
  }
  
  public static int getFirstKKBonus(Hero hero) {
    if (hero.getFightMode().equals("Waffenlos")) {
      if (hero.getFirstHandWeapon().equals("Boxen")) {
        return getKKBonus(hero, 14);
      }
    }
    else {
      String w = hero.getFirstHandWeapon();
      if (Flammenschwert1.equals(w) || Flammenschwert2.equals(w)) {
    	  return 0;
      }
      Weapon weapon = Weapons.getInstance().getWeapon(w);
      if (weapon != null) {
        return getKKBonus(hero, weapon); 
      }
    }
    return 0;
  }
  
  public static DiceSpecification getFirstTP(Hero hero) {
	  String weapon = hero.getFirstHandWeapon();
    if (hero.getFightMode().equals("Waffenlos")) {
      int fixedTP = 0;
      if (weapon != null && weapon.equals("Boxen")) {
        fixedTP += getKKBonus(hero, 14);
      }
      return DiceSpecification.create(1, 6, fixedTP);
    }
    else {
      return getTP(hero, weapon);
    }
  }
  
  public static int getSecondKKBonus(Hero hero) {
    if (!hero.getFightMode().equals("Zwei Waffen")) return 0;
    String s = hero.getSecondHandItem();
    Weapon w = Weapons.getInstance().getWeapon(s);
    return w != null ? getKKBonus(hero, w) : 0;
  }
  
  public static DiceSpecification getSecondTP(Hero hero) {
    if (!hero.getFightMode().equals("Zwei Waffen")) return DiceSpecification.create(0, 6, 0);
    String s = hero.getSecondHandItem();
    Weapon w = Weapons.getInstance().getWeapon(s);
    int wDamage = w.getW6damage();
    int constDamage = w.getConstDamage();
    int leftSkill = hero.getCurrentTalentValue("Linkshändig");
    if (leftSkill >= 8) {
      constDamage += getKKBonus(hero, w);
    }    
    return DiceSpecification.create(wDamage, 6, constDamage);
  }
  
  public static interface UpdateCallbacks {
    void updateData();
    void fightPropertyChanged(Fighter fighter, dsa.remote.IServer.FightProperty fp);
  }
  
  public static String doHit(Fighter fighter, int tp, boolean useAU, boolean rollSpecials, boolean hasShield,
      JFrame parent, UpdateCallbacks callbacks) {
    ImageIcon icon = ImageManager.getIcon("hit");
    int sp = tp - fighter.getRS();
    if (sp <= 0) return ", aber die Rüstung fängt den Schaden ab.";
    int le = 0;
    String zoneText = "";
    if (Group.getInstance().getOptions().useHitZones()) {
    	zoneText = "Trefferzone:";
    	switch (Dice.roll(20)) {
    	case 1:
    	case 3:
    	case 5:
    		zoneText += " linkes Bein.";
    		break;
    	case 2:
    	case 4:
    	case 6:
    		zoneText += " rechtes Bein.";
    		break;
    	case 7:
    	case 8:
    		zoneText += " Bauch.";
    		break;
    	case 9:
    	case 11:
    		zoneText += hasShield ? " Waffenarm." : " zweiter Arm.";
    		break;
    	case 10:
    		zoneText += hasShield ? " Waffenarm." : " erster Arm.";
    		break;
    	case 12:
    		zoneText += hasShield ? " linkes Bein." : " zweiter Arm.";
    		break;
    	case 13:
    		zoneText += hasShield ? " Brust." : " erster Arm.";
    		break;
    	case 14:
    		zoneText += hasShield ? " Kopf." : " zweiter Arm.";
    		break;
    	case 15:
    	case 16:
    	case 17:
    	case 18:
    		zoneText += " Brust.";
    		break;
    	case 19:
    	case 20:
    		zoneText += " Kopf.";
    		break;
		default:
			zoneText += " <Fehler>.";
			break;
    	}
    }
    String zoneTextOnly = zoneText;
    if (!zoneText.isEmpty()) {
    	zoneText = "\n\n" + zoneText;
    }
    if (useAU && (fighter instanceof Hero)) {
      ((Hero)fighter).changeAU(-sp);
      le = ((Hero)fighter).getCurrentEnergy(Energy.AU);
    }
    else {
      fighter.setCurrentLE(fighter.getCurrentLE() - sp);
      le = fighter.getCurrentLE();
    }
    String additionalText = "";
    if (useAU) {
      if (le == 0) {
    	additionalText = " und wird bewusstlos.";
        JOptionPane.showMessageDialog(parent, fighter.getName()
            + " wird bewusstlos." + zoneText, "Treffer", JOptionPane.PLAIN_MESSAGE, icon);
      }
      else if (!"".equals(zoneTextOnly)) {
    	  JOptionPane.showMessageDialog(parent, zoneTextOnly, "Treffer", JOptionPane.PLAIN_MESSAGE, icon);
      }
      return additionalText + " " + zoneTextOnly;
    }
    if (le < 0) {
      JOptionPane.showMessageDialog(parent, fighter.getName()
          + " liegt im Sterben ..." + zoneText, "Verwundung", JOptionPane.PLAIN_MESSAGE,
          icon);
      additionalText += " und liegt im Sterben ...";
    }
    else if (le <= 5) {
      if (Group.getInstance().getOptions().hasQvatStunned()) {
        if (le + sp > 5) {
          JOptionPane
              .showMessageDialog(
                  parent,
                  fighter.getName()
                      + " ist benommen und braucht jede KR\neine KO-Probe, um nicht ohnmächtig zu werden." + zoneText,
                  "Verwundung", JOptionPane.PLAIN_MESSAGE, icon);
          fighter.setDazed(true);
          callbacks.fightPropertyChanged(fighter, dsa.remote.IServer.FightProperty.dazed);
          callbacks.updateData();
          additionalText += " und ist benommen.";
        }
        else if (!"".equals(zoneTextOnly)) {
      	  JOptionPane.showMessageDialog(parent, zoneTextOnly, "Treffer", JOptionPane.PLAIN_MESSAGE, icon);
        }
      }
      else {
        JOptionPane.showMessageDialog(parent, fighter.getName()
            + " wird bewusstlos." + zoneText, "Verwundung", JOptionPane.PLAIN_MESSAGE,
            icon);
        additionalText += " und wird bewusstlos.";
      }
    }
    else if (sp >= 15 && (fighter instanceof Hero)) {
      Hero currentHero = (Hero) fighter;
      if (Markers.isUsingMarkers()) {
        currentHero.setExtraMarkers(currentHero.getExtraMarkers() + 1);
        callbacks.fightPropertyChanged(fighter, dsa.remote.IServer.FightProperty.markers);
        callbacks.updateData();
        additionalText += " und bekommt einen Wundmarker.";
      }
      if (!rollSpecials) {
        if (JOptionPane.showConfirmDialog(parent, currentHero.getName() + " hat mehr als 15 SP erhalten. Effekt auswürfeln?", 
            "Treffer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
          rollSpecials = true;
        }
      }
      if (Group.getInstance().getOptions().hasQvatStunned() && rollSpecials) {
        int ko = currentHero.getCurrentEnergy(Energy.KO);
        int roll = Dice.roll(20);
        if (roll <= ko - Markers.getMarkers(currentHero)) {
          String koText = " hat die KO-Probe mit einer " + roll + " bestanden!"; 
          JOptionPane.showMessageDialog(parent, currentHero.getName()
              + koText + zoneText,
              "Verwundung", JOptionPane.PLAIN_MESSAGE, icon);
          additionalText += " und" + koText;
        }
        else {
          if (currentHero.isDazed()) {
        	String koText = " hat die KO-Probe mit einer " + roll + " nicht bestanden.\n"
                    + (currentHero.getSex().startsWith("m") ? "Er" : "Sie")
                    + " wird ohnmächtig.";
            JOptionPane.showMessageDialog(parent, currentHero.getName()
                + koText + zoneText, "Verwundung", JOptionPane.PLAIN_MESSAGE,
                icon);
            additionalText += " und" + koText;
          }
          else {
        	String koText = " hat die KO-Probe mit einer " + roll + " nicht bestanden.\n"
                    + (currentHero.getSex().startsWith("m") ? "Er" : "Sie")
                    + " ist jetzt benommen."; 
            JOptionPane.showMessageDialog(parent, currentHero.getName()
                + koText + zoneText, "Verwundung",
                JOptionPane.PLAIN_MESSAGE, icon);
            currentHero.setDazed(true);
            callbacks.updateData();
            additionalText += " und" + koText;
          }
        }

      }
      else if (rollSpecials) {
        Probe probe = new Probe();
        probe.setFirstProperty(currentHero.getCurrentProperty(Property.MU));
        probe.setSecondProperty(currentHero.getCurrentProperty(Property.KK));
        probe.setThirdProperty(currentHero.getCurrentProperty(Property.KK));
        probe.setModifier(sp - 15 + Markers.getMarkers(currentHero));
        probe.setSkill(currentHero.getCurrentTalentValue("Selbstbeherrschung"));
        int d1 = Dice.roll(20);
        int d2 = Dice.roll(20);
        int d3 = Dice.roll(20);
        int result = probe.performDetailedTest(d1, d2, d3);
        String specialText;
        if (result == Probe.DAEMONENPECH) {
          specialText = " hat die Selbstbeherrschung mit DREI 20ern verpatzt!";
        }
        else if (result == Probe.PATZER) {
          specialText = " hat die Selbstbeherrschung mit zwei 20ern verpatzt!";
        }
        else if (result == Probe.PERFEKT) {
          specialText = " hat die Selbstbeherrschung mit zwei 1ern perfekt bestanden!";
        }
        else if (result == Probe.GOETTERGLUECK) {
          specialText = " hat die Selbstbeherrschung mit DREI 1ern bestanden!";
        }
        else if (result == Probe.FEHLSCHLAG) {
          specialText = " hat die Selbstbeherrschung mit " + d1 + "," + d2 + "," + d3
                  + " nicht bestanden.\n"
                  + (currentHero.getSex().startsWith("m") ? "Er" : "Sie")
                  + " wird vor Schmerz ohnmächtig.";
        }
        else {
          specialText = " hat die Selbstbeherrschung mit " + d1 + "," + d2 + "," + d3
                  + " bestanden (" + result + " Punkte übrig).";
        }
        JOptionPane.showMessageDialog(parent, currentHero.getName()
                + specialText + zoneText,
                "Verwundung", JOptionPane.PLAIN_MESSAGE, icon);
        additionalText = " und " + specialText;
      }
      else if (!"".equals(zoneTextOnly)) {
    	  JOptionPane.showMessageDialog(parent, zoneTextOnly, "Treffer", JOptionPane.PLAIN_MESSAGE, icon);
      }
    }
    else if (!"".equals(zoneTextOnly)) {
  	  JOptionPane.showMessageDialog(parent, zoneTextOnly, "Treffer", JOptionPane.PLAIN_MESSAGE, icon);
    }    
    if (additionalText.isEmpty())
    	additionalText = ".";
    if (!zoneTextOnly.isEmpty())
    	additionalText += " " + zoneTextOnly;
    return additionalText;
  }

  private static boolean isProjectileWeapon(Weapon w) {
    return Weapons.isFarRangedCategory(w.getType());
  }

  public static ArrayList<String> getProjectileWeapons(Hero currentHero) {
    String[] weapons = currentHero.getWeapons();
    ArrayList<String> result = new ArrayList<String>();
    for (String s : weapons) {
      Weapon w = Weapons.getInstance().getWeapon(s);
      if (isProjectileWeapon(w)) result.add(s);
    }
    return result;
  }
  
  public static final String Flammenschwert1 = "Flammenschwert, i.d. Hand";
  public static final String Flammenschwert2 = "Flammenschwert, schwebend";

  public static ArrayList<String> getCloseRangeWeapons(Hero currentHero, int hand) {
    String[] weapons = currentHero.getWeapons();
    ArrayList<String> result = new ArrayList<String>();
    for (String s : weapons) {
      Weapon w = Weapons.getInstance().getWeapon(s);
      if (!isProjectileWeapon(w) && !w.isTwoHanded()) {
        for (int i = 0; i < currentHero.getWeaponCount(s); ++i)
          result.add(s);
      }
    }
    if (currentHero.getRituals().contains("5. Stabzauber") && hand == 0) {
    	result.add(Flammenschwert1);
    	result.add(Flammenschwert2);
    }
    return result;
  }

  public static ArrayList<String> getTwoHandedWeapons(Hero currentHero) {
    String[] weapons = currentHero.getWeapons();
    ArrayList<String> result = new ArrayList<String>();
    for (String s : weapons) {
      Weapon w = Weapons.getInstance().getWeapon(s);
      if (!isProjectileWeapon(w) && w.isTwoHanded()) result.add(s);
    }
    return result;
  }
  
  public static java.util.List<String> getPossibleItems(Hero hero, String fightMode, int hand) {
    if (hand == 0) {
      if (fightMode.equals("Eine Waffe")) {
        return getCloseRangeWeapons(hero, hand);
      }
      else if (fightMode.startsWith("Waffe + Parade")) {
        return getCloseRangeWeapons(hero, hand);
      }
      else if (fightMode.equals("Zwei Waffen")) {
        return getCloseRangeWeapons(hero, hand);
      }
      else if (fightMode.equals("Waffenlos")) {
        ArrayList<String> items = new ArrayList<String>();
        items.add("Raufen");
        items.add("Boxen");
        items.add("Ringen");
        items.add("Hruruzat");
        return items;
      }
      else if (fightMode.equals("Zweihandwaffe")) {
        return getTwoHandedWeapons(hero);
      }
      else {
        return getProjectileWeapons(hero);
      }
    }
    else {
      // left hand
      if (fightMode.startsWith("Waffe + Parade")) {
        return java.util.Arrays.asList(hero.getShields());
      }
      else if (fightMode.equals("Zwei Waffen")) {
        ArrayList<String> items = getCloseRangeWeapons(hero, 1);
        if (items.contains(hero.getFirstHandWeapon()))
          items.remove(hero.getFirstHandWeapon());
        return items;
      }
      else return new ArrayList<String>();
    }
  }

}

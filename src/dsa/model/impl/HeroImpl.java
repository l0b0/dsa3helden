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
package dsa.model.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;

import static dsa.model.characters.Energy.*;
import static dsa.model.characters.Property.*;
import dsa.control.Fighting;
import dsa.control.filetransforms.FileType;
import dsa.control.printing.Printer;
import dsa.model.Date;
import dsa.model.DiceSpecification;
import dsa.model.FarRangedFightParams;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.LEAEIncreaseException;
import dsa.model.characters.Property;
import dsa.model.data.Animal;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
import dsa.model.data.ExtraThingData;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.Talent;
import dsa.util.AbstractObservable;
import dsa.util.Directories;
import dsa.util.Optional;

/**
 * 
 */
public final class HeroImpl extends AbstractObservable<CharacterObserver>
    implements Hero, Cloneable {

  private boolean changed = false;

  private int talentIncreasesPerStep;

  public HeroImpl() {
    super();
    properties = new EnumMap<Property, CurrentAndDefaultData>(Property.class);
    properties.put(MU, new CurrentAndDefaultData());
    properties.put(KL, new CurrentAndDefaultData());
    properties.put(CH, new CurrentAndDefaultData());
    properties.put(IN, new CurrentAndDefaultData());
    properties.put(FF, new CurrentAndDefaultData());
    properties.put(GE, new CurrentAndDefaultData());
    properties.put(KK, new CurrentAndDefaultData());
    properties.put(HA, new CurrentAndDefaultData());
    properties.put(RA, new CurrentAndDefaultData());
    properties.put(TA, new CurrentAndDefaultData());
    properties.put(AG, new CurrentAndDefaultData());
    properties.put(GG, new CurrentAndDefaultData());
    properties.put(NG, new CurrentAndDefaultData());
    properties.put(JZ, new CurrentAndDefaultData());
    energies = new EnumMap<Energy, EnergyData>(Energy.class);
    energies.put(LE, new EnergyData(true));
    energies.put(AE, new EnergyData(false));
    energies.put(KE, new EnergyData(false));
    energies.put(AU, new EnergyData(true));
    energies.put(KO, new EnergyData(true));
    talents = new HashMap<String, TalentData>();
    name = "";
    type = "";
    ap = 0;
    changed = false;
    overallTalentIncreaseTries = 0;
    talentIncreasesPerStep = 30;
    mLEIncreaseTries = 0;
    mAEIncreaseTries = 0;
    mHasExtraAEIncrease = false;
    fixedLEIncrease = 0;
    fixedAEIncrease = 0;
    mrBonus = 0;
    overallSpellIncreaseTries = 0;
    spellIncreasesPerStep = 0;
    spellToTalentMoves = 0;
    mHasGreatMeditation = false;
    money.add(0);
    money.add(0);
    currencies.add(0);
    currencies.add(1);
    bankMoney.add(0);
    bankMoney.add(0);
    bankCurrencies.add(0);
    bankCurrencies.add(1);
    overallSpellOrTalentIncreaseTries = 0;
    canMeditate = false;
    bgFile = "";
    bgEditor = "";
    notes = "";
    picture = "";
    for (int i = 0; i < DerivedValue.values().length; ++i) {
      derivedValueChanges.add(0);
    }
    title = "";
    rituals.clear();
    printFile = "";
    skin = "";
    farRangedFightParams = new FarRangedFightParams();
    checkForMirakel();
  }

  int spellToTalentMoves;

  int spellIncreasesPerStep;

  boolean mHasGreatMeditation;

  private static final String HUNT1 = "Jagen (Falle)";

  private static final String HUNT2 = "Jagen (Pirsch)";

  public void addTalent(String talentName) {
    talents.put(talentName, new TalentData());
    talents.get(talentName).increasesPerStep = Talents.getInstance().getTalent(
        talentName).getMaxIncreasePerStep();
    Talent talent = Talents.getInstance().getTalent(talentName);
    if (talent != null && !talent.isLanguage() &&
        (   (talent.isSpell() && getOverallSpellIncreaseTries() > 0)
         || (!talent.isSpell() && getOverallTalentIncreaseTries() > 0)
         || getSpellOrTalentIncreaseTries() > 0)) {
      talents.get(talentName).remainingTries = 3;
      talents.get(talentName).remainingIncreases = talents.get(talentName).increasesPerStep;
    }
    for (CharacterObserver o : observers)
      o.talentAdded(talentName);
    checkForHuntingTalents();
    if (!talentName.equals(HUNT1) && !talentName.equals(HUNT2)) changed = true;
  }

  public void removeTalent(String talentName) {
    if (!talents.containsKey(talentName)) return;
    talents.remove(talentName);
    for (CharacterObserver o : observers)
      o.talentRemoved(talentName);
    changed = true;
  }

  public Object clone() throws CloneNotSupportedException {
    HeroImpl hero = (HeroImpl) super.clone();
    hero.properties = new EnumMap<Property, CurrentAndDefaultData>(
        Property.class);
    Iterator<Property> propIt = properties.keySet().iterator();
    while (propIt.hasNext()) {
      Property property = propIt.next();
      CurrentAndDefaultData newData = (CurrentAndDefaultData) properties.get(
          property).clone();
      hero.properties.put(property, newData);
    }
    hero.energies = new EnumMap<Energy, EnergyData>(Energy.class);
    Iterator<Energy> enerIt = energies.keySet().iterator();
    while (enerIt.hasNext()) {
      Energy energy = enerIt.next();
      EnergyData newData = (EnergyData) energies.get(energy).clone();
      hero.energies.put(energy, newData);
    }
    calcKO();
    hero.talents = new HashMap<String, TalentData>();
    Iterator<String> talentIt = talents.keySet().iterator();
    while (talentIt.hasNext()) {
      String talent = talentIt.next();
      TalentData newData = (TalentData) talents.get(talent).clone();
      hero.talents.put(talent, newData);
    }
    hero.armours = new TreeSet<String>();
    hero.armours.addAll(armours);
    hero.weapons = new HashMap<String, Integer>();
    hero.weapons.putAll(weapons);
    hero.currencies = new ArrayList<Integer>();
    hero.currencies.addAll(currencies);
    hero.money = new ArrayList<Integer>();
    hero.money.addAll(money);
    hero.fightingTalentsInDocument = new ArrayList<String>();
    hero.fightingTalentsInDocument.addAll(fightingTalentsInDocument);
    hero.rituals = new ArrayList<String>();
    hero.rituals.addAll(rituals);
    hero.derivedValueChanges = new ArrayList<Integer>();
    hero.derivedValueChanges.addAll(derivedValueChanges);
    hero.atParts = new HashMap<String, Integer>();
    hero.atParts.putAll(atParts);
    hero.things = new HashMap<String, Integer>();
    hero.things.putAll(things);
    hero.bankCurrencies = new ArrayList<Integer>();
    hero.bankCurrencies.addAll(bankCurrencies);
    hero.bankMoney = new ArrayList<Integer>();
    hero.bankMoney.addAll(bankMoney);
    hero.shields = new ArrayList<String>();
    hero.shields.addAll(shields);
    hero.thingsInWarehouse = new HashMap<String, Integer>();
    hero.thingsInWarehouse.putAll(thingsInWarehouse);
    hero.clothes = new ArrayList<String>();
    hero.clothes.addAll(clothes);
    hero.observers = new java.util.LinkedList<CharacterObserver>();
    hero.bfs = new HashMap<String, Integer>();
    hero.bfs.putAll(bfs);
    hero.shieldBFs = new HashMap<String, Integer>();
    hero.shieldBFs.putAll(shieldBFs);
    hero.extraThingData = new HashMap<String, ExtraThingData>();
    hero.extraThingData.putAll(extraThingData);
    hero.extraWarehouseData = new HashMap<String, ExtraThingData>();
    hero.extraWarehouseData.putAll(extraWarehouseData);
    hero.nrOfProjectiles = new HashMap<String, Integer>();
    hero.nrOfProjectiles.putAll(nrOfProjectiles);
    hero.loadedNewerVersion = false;
    hero.targets = new ArrayList<String>(targets);
    hero.farRangedFightParams = (FarRangedFightParams) farRangedFightParams.clone();
    return hero;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetDefaultProperty(dsa.data.Property)
   */
  public int getDefaultProperty(Property property) {
    return properties.get(property).defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetCurrentProperty(dsa.data.Property)
   */
  public int getCurrentProperty(Property property) {
    return properties.get(property).currentValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetDefaultEnergy(dsa.data.Energy)
   */
  public int getDefaultEnergy(Energy energy) {
    return energies.get(energy).defaultValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetCurrentEnergy(dsa.data.Energy)
   */
  public int getCurrentEnergy(Energy energy) {
    int value = energies.get(energy).currentValue;
    if (energy == Energy.AU) {
      value += auDifference;
    }
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#HasEnergy(dsa.data.Energy)
   */
  public boolean hasEnergy(Energy energy) {
    return energies.get(energy).available;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetDefaultProperty(dsa.data.Property, int)
   */
  public void setDefaultProperty(Property property, int value) {
    int delta = value - getDefaultProperty(property);
    properties.get(property).defaultValue = value;
    properties.get(property).currentValue += delta;
    if (property == KK) {
      changeDefaultEnergy(AU, delta);
      calcKO();
    }
    else if (property == MU) {
      calcKO();
    }
    for (CharacterObserver o : observers) {
      o.defaultPropertyChanged(property);
      o.currentPropertyChanged(property);
    }
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetCurrentProperty(dsa.data.Property, int)
   */
  public void setCurrentProperty(Property property, int value) {
    properties.get(property).currentValue = value;
    if (property == KK) {
      changeCurrentEnergy(AU, value - getCurrentProperty(KK));
      calcKO();
    }
    else if (property == MU) {
      calcKO();
    }
    for (CharacterObserver o : observers)
      o.currentPropertyChanged(property);
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetDefaultEnergy(dsa.data.Energy, int)
   */
  public void setDefaultEnergy(Energy energy, int value) {
    int delta = value - getDefaultEnergy(energy);
    energies.get(energy).defaultValue = value;
    energies.get(energy).currentValue += delta;
    if (energy == LE) {
      changeDefaultEnergy(AU, delta);
      calcKO();
    }
    for (CharacterObserver o : observers) {
      o.defaultEnergyChanged(energy);
      o.currentEnergyChanged(energy);
    }
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetCurrentEnergy(dsa.data.Property, int)
   */
  public void setCurrentEnergy(Energy energy, int value) {
    energies.get(energy).currentValue = value;
    if (energy == LE) {
      setCurrentEnergy(AU, value + getCurrentProperty(KK));
      calcKO();
    }
    else if (energy == AU
        && (energies.get(Energy.AU).currentValue + auDifference < 0)) {
      auDifference = -energies.get(Energy.AU).currentValue;
    }
    for (CharacterObserver o : observers)
      o.currentEnergyChanged(energy);
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeDefaultProperty(dsa.data.Property, int)
   */
  public void changeDefaultProperty(Property property, int mod) {
    properties.get(property).defaultValue += mod;
    properties.get(property).currentValue += mod;
    if (property == KK) {
      changeDefaultEnergy(AU, mod);
      calcKO();
    }
    else if (property == MU) {
      calcKO();
    }
    for (CharacterObserver o : observers)
      o.defaultPropertyChanged(property);
    for (CharacterObserver o : observers)
      o.currentPropertyChanged(property);
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeCurrentProperty(dsa.data.Property, int)
   */
  public void changeCurrentProperty(Property property, int mod) {
    properties.get(property).currentValue += mod;
    if (property == KK) {
      changeCurrentEnergy(AU, mod);
      calcKO();
    }
    else if (property == MU) {
      calcKO();
    }
    for (CharacterObserver o : observers)
      o.currentPropertyChanged(property);
    changed = true;
  }

  public void setProperty(int index, int newValue) {
    properties.get(Property.values()[index]).defaultValue = newValue;
    properties.get(Property.values()[index]).currentValue = newValue;
    if (index == 6) {
      energies.get(AU).currentValue = properties.get(KK).currentValue
          + energies.get(LE).currentValue;
      energies.get(AU).defaultValue = properties.get(KK).defaultValue
          + energies.get(LE).defaultValue;
      calcKO();
    }
    else if (index == 0) {
      calcKO();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeDefaultEnergy(dsa.data.Energy, int)
   */
  public void changeDefaultEnergy(Energy energy, int mod) {
    energies.get(energy).currentValue += mod;
    energies.get(energy).defaultValue += mod;
    if (energy == Energy.LE) {
      changeDefaultEnergy(Energy.AU, mod);
      calcKO();
    }
    for (CharacterObserver o : observers)
      o.defaultEnergyChanged(energy);
    for (CharacterObserver o : observers)
      o.currentEnergyChanged(energy);
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeCurrentEnergy(dsa.data.Energy, int)
   */
  public void changeCurrentEnergy(Energy energy, int mod) {
    energies.get(energy).currentValue += mod;
    if (energy == Energy.LE) {
      changeCurrentEnergy(Energy.AU, mod);
      calcKO();
    }
    else if (energy == Energy.AU
        && (energies.get(Energy.AU).currentValue + auDifference < 0)) {
      auDifference = -energies.get(Energy.AU).currentValue;
    }
    for (CharacterObserver o : observers)
      o.currentEnergyChanged(energy);
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetHasEnergy(dsa.data.Energy, boolean)
   */
  public void setHasEnergy(Energy energy, boolean hasIt) {
    if (energies.get(energy).available == hasIt) return;
    energies.get(energy).available = hasIt;
    for (CharacterObserver o : observers)
      o.defaultEnergyChanged(energy);
    for (CharacterObserver o : observers)
      o.currentEnergyChanged(energy);
    if (energy == Energy.KE) {
      checkForMirakel();
    }
    changed = true;
  }

  private static final String MIRAKEL = "Mirakel";

  void checkForMirakel() {
    if (hasEnergy(Energy.KE)) {
      if (!hasTalent(MIRAKEL)) {
        addTalent(MIRAKEL);
      }
      int value = (int) Math.round((float) getStep() / 2.0f);
      setDefaultTalentValue(MIRAKEL, value);
    }
    else {
      if (hasTalent(MIRAKEL)) {
        removeTalent(MIRAKEL);
      }
    }
  }

  void calcKO() {
    int oldDefault = getDefaultEnergy(KO);
    int oldCurrent = getCurrentEnergy(KO);
    energies.get(KO).defaultValue = (int) (Math.round(getDefaultEnergy(LE)
        / 10.0f + (getDefaultProperty(MU) + 2.0f * getDefaultProperty(KK))
        / 5.0f));
    energies.get(KO).currentValue = (int) (Math.round(getCurrentEnergy(LE)
        / 10.0f + (getCurrentProperty(MU) + 2.0f * getCurrentProperty(KK))
        / 5.0f));
    if (oldDefault != getDefaultEnergy(KO)) {
      for (CharacterObserver o : observers)
        o.defaultEnergyChanged(KO);
    }
    if (oldCurrent != getCurrentEnergy(KO)) {
      for (CharacterObserver o : observers)
        o.currentEnergyChanged(KO);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetAP()
   */
  public int getAP() {
    return ap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetStep()
   */
  public int getStep() {
    return (int) Math.floor(0.5 + 0.01 * Math.sqrt(2500 + 200 * ap));
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetAP(int)
   */
  public void setAP(int someAP) {
    ap = someAP;
    checkForMirakel();
    changed = true;
  }

  public void toStepOne(int talentReducement) {
    overallTalentIncreaseTries += talentIncreasesPerStep;
    overallTalentIncreaseTries -= talentReducement;
    for (TalentData data : talents.values()) {
      data.remainingIncreases = data.increasesPerStep;
      data.remainingTries = 3;
    }
    boolean fullStep = dsa.model.characters.Group.getInstance().getOptions()
        .hasFullFirstStep();
    if (fullStep) {
      goodPropertyChangeTries++;
      badPropertyChangeTries++;
      mLEIncreaseTries++;
      if (hasEnergy(Energy.AE)) {
        mAEIncreaseTries++;
        canMeditate = mHasGreatMeditation;
      }
    }
    overallSpellIncreaseTries += spellIncreasesPerStep;
    overallSpellOrTalentIncreaseTries += spellToTalentMoves;
    overallSpellIncreaseTries -= spellToTalentMoves;
    remainingStepIncreases++;
    checkForMirakel();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeAP(int)
   */
  public void changeAP(int mod) {
    int oldStep = getStep();
    ap += mod;
    if (getStep() != oldStep) {
      if (remainingStepIncreases == 0) {
        enableStepIncreases();
      }
      remainingStepIncreases += (getStep() - oldStep);
      checkForMirakel();
    }
    changed = true;
  }

  public void removeRemainingIncreaseTries() {
    overallSpellIncreaseTries = 0;
    overallSpellOrTalentIncreaseTries = 0;
    overallTalentIncreaseTries = 0;
    for (TalentData data : talents.values()) {
      data.remainingIncreases = 0;
      data.remainingTries = 0;
    }
    canMeditate = false;
    checkForNextStepIncrease();
    for (CharacterObserver o : observers)
      o.increaseTriesChanged();
    changed = true;
  }

  private void checkForNextStepIncrease() {
    if (overallSpellIncreaseTries > 0 || overallSpellOrTalentIncreaseTries > 0
        || overallTalentIncreaseTries > 0 || mLEIncreaseTries > 0
        || (hasEnergy(Energy.AE) && mAEIncreaseTries > 0) 
        || goodPropertyChangeTries > 0
        || badPropertyChangeTries > 0) return;
    if (remainingStepIncreases > 0) {
      --remainingStepIncreases;
    }
    if (remainingStepIncreases > 0) {
      enableStepIncreases();
    }
  }

  private void enableStepIncreases() {
    overallTalentIncreaseTries += talentIncreasesPerStep;
    for (TalentData data : talents.values()) {
      data.remainingIncreases = data.increasesPerStep;
      data.remainingTries = 3;
    }
    goodPropertyChangeTries++;
    badPropertyChangeTries++;
    mLEIncreaseTries++;
    if (hasEnergy(Energy.AE)) {
      mAEIncreaseTries++;
      canMeditate = mHasGreatMeditation;
    }
    overallSpellIncreaseTries += spellIncreasesPerStep;
    overallSpellOrTalentIncreaseTries += spellToTalentMoves;
    overallSpellIncreaseTries -= spellToTalentMoves;
    for (CharacterObserver o : observers)
      o.stepIncreased();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#HasTalent(java.lang.String)
   */
  public boolean hasTalent(String talent) {
    return talents.containsKey(talent);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetDefaultTalentValue(java.lang.String)
   */
  public int getDefaultTalentValue(String talent) {
    if (hasTalent(talent))
      return talents.get(talent).defaultValue;
    else
      return -20;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetCurrentTalentValue(java.lang.String)
   */
  public int getCurrentTalentValue(String talent) {
    if (hasTalent(talent))
      return talents.get(talent).currentValue;
    else
      return -20;
  }

  private static final String SEELE = "Heilkunde, Seele";

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetDefaultTalentValue(java.lang.String)
   */
  public void setDefaultTalentValue(String talent, int value) {
    if (hasTalent(talent)) {
      if (getDefaultTalentValue(talent) == value) return;
      int delta = value - getDefaultTalentValue(talent);
      talents.get(talent).defaultValue = value;
      talents.get(talent).currentValue += delta;
      if (!talent.equals(HUNT1) && !talent.equals(HUNT2))
        checkForHuntingTalents();
      for (CharacterObserver o : observers) {
        o.defaultTalentChanged(talent);
        o.currentTalentChanged(talent);
      }
      if (talent.equals(SEELE)) {
        for (CharacterObserver o : observers)
          o.derivedValueChanged(Hero.DerivedValue.MR);
      }
    }
    if (!talent.equals(HUNT1) && !talent.equals(HUNT2)) changed = true;
  }

  /**
   * 
   * 
   */
  private void checkForHuntingTalents() {
    boolean wasChanged = isChanged();
    if (hasTalent("Wildnisleben") && hasTalent("Fährtensuchen")) {
      // Falle
      if (hasTalent("Fallenstellen")) {
        if (!hasTalent(HUNT1)) addTalent(HUNT1);
        int defaultValue = getDefaultTalentValue("Wildnisleben")
            + getDefaultTalentValue("Fährtensuchen")
            + getDefaultTalentValue("Fallenstellen");
        defaultValue = (int) Math.floor(defaultValue / 4.0f);
        int currentValue = getCurrentTalentValue("Wildnisleben")
            + getCurrentTalentValue("Fährtensuchen")
            + getCurrentTalentValue("Fallenstellen");
        currentValue = (int) Math.floor(currentValue / 4.0f);
        if (hasTalent("Tierkunde")) {
          int tierkundeDefault = getDefaultTalentValue("Tierkunde");
          defaultValue += (tierkundeDefault > 10) ? (int) Math
              .floor((tierkundeDefault - 10) / 2.0f) : 0;
          int tierkundeCurrent = getCurrentTalentValue("Tierkunde");
          currentValue += (tierkundeCurrent > 10) ? (int) Math
              .floor((tierkundeCurrent - 10) / 2.0f) : 0;
        }
        setDefaultTalentValue(HUNT1, defaultValue);
        setCurrentTalentValue(HUNT1, currentValue);
      }
      // Pirsch
      if (hasTalent("Schleichen")) {
        if (!hasTalent(HUNT2)) addTalent(HUNT2);
        int defaultValue = getDefaultTalentValue("Wildnisleben")
            + getDefaultTalentValue("Fährtensuchen")
            + getDefaultTalentValue("Schleichen");
        defaultValue = (int) Math.floor(defaultValue / 4.0f);
        int currentValue = getCurrentTalentValue("Wildnisleben")
            + getCurrentTalentValue("Fährtensuchen")
            + getCurrentTalentValue("Schleichen");
        currentValue = (int) Math.floor(currentValue / 4.0f);
        int defaultBonus = 0;
        int currentBonus = 0;
        if (hasTalent("Schußwaffen")) {
          int swDefault = getDefaultTalentValue("Schußwaffen");
          defaultBonus = (swDefault > 10) ? (int) Math
              .floor((swDefault - 10) / 2.0f) : 0;
          int swCurrent = getCurrentTalentValue("Schu�waffen");
          currentBonus = (swCurrent > 10) ? (int) Math
              .floor((swCurrent - 10) / 2.0f) : 0;
        }
        if (hasTalent("Wurfwaffen")) {
          int wwDefault = getDefaultTalentValue("Wurfwaffen");
          defaultBonus = Math.max(defaultBonus, wwDefault > 10 ? (int) Math
              .floor((wwDefault - 10) / 3.0f) : 0);
          int wwCurrent = getCurrentTalentValue("Wurfwaffen");
          currentBonus = Math.max(currentBonus, wwCurrent > 10 ? (int) Math
              .floor((wwCurrent - 10) / 3.0f) : 0);
        }
        if (hasTalent("Speere und Stäbe")) {
          int ssDefault = getDefaultTalentValue("Speere und Stäbe");
          int ssCurrent = getCurrentTalentValue("Speere und Stäbe");
          defaultBonus = Math.max(defaultBonus, ssDefault > 10 ? (int) Math
              .floor((ssDefault - 10) / 3.0f) : 0);
          currentBonus = Math.max(currentBonus, ssCurrent > 10 ? (int) Math
              .floor((ssCurrent - 10) / 3.0f) : 0);
        }
        setDefaultTalentValue(HUNT2, defaultValue + defaultBonus);
        setCurrentTalentValue(HUNT2, currentValue + currentBonus);
      }
    }
    changed = wasChanged;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetCurrentTalentValue(java.lang.String)
   */
  public void setCurrentTalentValue(String talent, int value) {
    if (hasTalent(talent)) {
      if (getCurrentTalentValue(talent) == value) return;
      talents.get(talent).currentValue = value;
      if (!talent.equals("Jagen / Falle") && !talent.equals("Jagen / Pirsch"))
        checkForHuntingTalents();
      for (CharacterObserver o : observers)
        o.currentTalentChanged(talent);
      if (talent.equals(SEELE)) {
        for (CharacterObserver o : observers)
          o.derivedValueChanged(Hero.DerivedValue.MR);
      }
    }
    if (!talent.equals(HUNT1) && !talent.equals(HUNT2)) changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetTalentIncreaseTries(java.lang.String)
   */
  public int getTalentIncreaseTries(String talent) {
    Talent t = Talents.getInstance().getTalent(talent);
    if (t.isLanguage()) {
      if (getDefaultTalentValue(talent) >= ((Language) t).getMax(nativeTongue))
        return 0;
      return ((Language) t).isOld() ? getFreeOldLanguagePoints()
          : getFreeLanguagePoints();
    }
    boolean isSpell = Talents.getInstance().getTalent(talent).isSpell();
    if (isSpell && getOverallSpellIncreaseTries() == 0
        && getSpellOrTalentIncreaseTries() == 0)
      return 0;
    else if (!isSpell && getOverallTalentIncreaseTries() == 0
        && getSpellOrTalentIncreaseTries() == 0)
      return 0;
    else if (hasTalent(talent))
      return talents.get(talent).remainingIncreases;
    else if (isSpell)
      return 1; // to learn as yet unknown spells
    else
      return 0;
  }

  public int getOverallTalentIncreaseTries() {
    return overallTalentIncreaseTries;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetType()
   */
  public String getType() {
    return type;
  }

  public void setCompleteEnergy(Energy energy, int value) {
    setDefaultEnergy(energy, value);
    setCurrentEnergy(energy, value);
  }

  public void setCompleteProperty(Property property, int value) {
    setDefaultProperty(property, value);
    setCurrentProperty(property, value);
  }

  private String name;

  private String type;

  private int overallTalentIncreaseTries;

  private int overallSpellIncreaseTries;

  private int overallSpellOrTalentIncreaseTries;

  int mrBonus;

  private class CurrentAndDefaultData implements Cloneable {
    int currentValue;

    int defaultValue;

    public CurrentAndDefaultData() {
      currentValue = 0;
      defaultValue = 0;
    }

    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  };

  private Map<Property, CurrentAndDefaultData> properties;

  private int goodPropertyChangeTries = 0;

  private int badPropertyChangeTries = 0;

  private int ap;

  private class EnergyData extends CurrentAndDefaultData {
    boolean available;

    public EnergyData(boolean available) {
      this.available = available;
    }

    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  };

  private Map<Energy, EnergyData> energies;

  private class TalentData extends CurrentAndDefaultData {
    int remainingIncreases;

    int remainingTries;

    public TalentData() {
      remainingIncreases = 0;
      remainingTries = 3;
    }

    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }

    int increasesPerStep;
  };

  private Map<String, TalentData> talents;

  /**
   * 
   * @param tempname
   */
  public void setName(String tempname) {
    if (name.equals(tempname)) return;
    String oldName = name;
    name = tempname;
    changed = true;
    for (CharacterObserver o : observers)
      o.nameChanged(oldName, name);
  }

  public boolean hasPropertyChangeTry(Property property) {
    if (property.ordinal() > Property.KK.ordinal())
      return badPropertyChangeTries > 0;
    else
      return goodPropertyChangeTries > 0;
  }

  public void removePropertyChangeTry(boolean goodProperty) {
    if (goodProperty)
      goodPropertyChangeTries--;
    else
      badPropertyChangeTries--;
    checkForNextStepIncrease();
    for (CharacterObserver o : observers) {
      o.increaseTriesChanged();
    }
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetTalentIncreaseTries(java.lang.String, int)
   */
  public void removeTalentIncreaseTry(String talent, boolean succeeded) {
    if (!hasTalent(talent)) return;
    if (succeeded) {
      talents.get(talent).remainingIncreases--;
      talents.get(talent).remainingTries = 3;
      changeTalentValue(talent, 1);
    }
    else {
      talents.get(talent).remainingTries--;
      if (talents.get(talent).remainingTries == 0) {
        talents.get(talent).remainingIncreases = 0;
      }
    }
    if (Talents.getInstance().getTalent(talent).isSpell()) {
      if (overallSpellIncreaseTries > 0)
        overallSpellIncreaseTries--;
      else
        overallSpellOrTalentIncreaseTries--;
      if (overallSpellIncreaseTries == 0
          && overallSpellOrTalentIncreaseTries == 0) {
        for (String n : talents.keySet()) {
          if (Talents.getInstance().getTalent(n).isSpell()) {
            talents.get(n).remainingIncreases = 0;
          }
        }
      }
    }
    else {
      if (overallTalentIncreaseTries > 0)
        overallTalentIncreaseTries--;
      else
        overallSpellOrTalentIncreaseTries--;
      if (overallTalentIncreaseTries == 0
          && overallSpellOrTalentIncreaseTries == 0) {
        for (String n : talents.keySet()) {
          if (!Talents.getInstance().getTalent(n).isSpell()) {
            talents.get(n).remainingIncreases = 0;
          }
        }
      }
    }
    checkForNextStepIncrease();
    for (CharacterObserver observer : observers) {
      observer.increaseTriesChanged();
    }
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#AddHeroObserver(dsa.data.CharacterObserver)
   */
  public void addHeroObserver(CharacterObserver observer) {
    addObserver(observer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#RemoveHeroObserver(dsa.data.CharacterObserver)
   */
  public void removeHeroObserver(CharacterObserver observer) {
    removeObserver(observer);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#ChangeTalentValue(java.lang.String, int)
   */
  public void changeTalentValue(String talent, int mod) {
    if (!hasTalent(talent)) {
      javax.swing.JOptionPane.showMessageDialog(null,
          "Versuch, unbekanntes Talent '" + talent
              + "' zu ändern.\nVermutlich ein Fehler.", "Heldenverwaltung",
          javax.swing.JOptionPane.WARNING_MESSAGE);
      return;
    }
    TalentData data = talents.get(talent);
    data.currentValue += mod;
    data.defaultValue += mod;
    for (CharacterObserver o : observers)
      o.currentTalentChanged(talent);
    for (CharacterObserver o : observers)
      o.defaultTalentChanged(talent);
    if (talent.equals(SEELE)) {
      for (CharacterObserver o : observers)
        o.derivedValueChanged(Hero.DerivedValue.MR);
    }
    changed = true;
  }

  private static final int FILE_VERSION = 42;

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#StoreToFile(java.io.File)
   */
  public void storeToFile(File f, File realFile) throws IOException {
    PrintWriter file = new PrintWriter(new java.io.FileWriter(f));
    try {
      file.println(FILE_VERSION); // version
      file.println(name);
      file.println(type);
      file.println(ap);
      for (Property property : properties.keySet()) {
        CurrentAndDefaultData data = properties.get(property);
        file.println(property.name());
        file.println(data.currentValue);
        file.println(data.defaultValue);
      }
      file.println("--");
      for (Energy energy : energies.keySet()) {
        if (energy == KO) continue; // is not stored, transient
        EnergyData data = energies.get(energy);
        file.println(energy.name());
        file.println(data.currentValue);
        file.println(data.defaultValue);
        file.println(data.available ? 1 : 0);
      }
      file.println("--");
      for (String talent : talents.keySet()) {
        TalentData data = talents.get(talent);
        file.println(talent);
        file.println(data.currentValue);
        file.println(data.defaultValue);
        file.println(data.remainingIncreases);
        file.println(data.remainingTries);
        file.println(data.increasesPerStep);
      }
      file.println("--");
      // version 2
      file.println(Directories.getAbsolutePath(printingTemplateFile, f));
      // version 3
      file.println(birthPlace);
      file.println(eyeColor);
      file.println(hairColor);
      file.println(skinColor);
      file.println(height);
      file.println(weight);
      file.println(sex);
      file.println(birthday);
      file.println(stand);
      file.println(god);
      file.println(age);
      // version 4
      file.println(overallTalentIncreaseTries);
      file.println(talentIncreasesPerStep);
      file.println(mLEIncreaseTries);
      file.println(mHasExtraAEIncrease ? 1 : 0);
      file.println(fixedLEIncrease);
      file.println(fixedAEIncrease);
      file.println(mrBonus);
      file.println(mAEIncreaseTries);
      // version 5
      file.println(overallSpellIncreaseTries);
      file.println(spellIncreasesPerStep);
      file.println(spellToTalentMoves);
      file.println(mHasGreatMeditation ? 1 : 0);
      // version 6
      file.println(goodPropertyChangeTries);
      file.println(badPropertyChangeTries);
      // version 8
      file.println(money.size());
      for (int i = 0; i < money.size(); ++i) {
        file.println(currencies.get(i));
        file.println(money.get(i));
      }
      // version 9
      file.println(nativeTongue);
      // version 10
      file.println(overallSpellOrTalentIncreaseTries);
      file.println(canMeditate ? 1 : 0);
      file.println(beModification);
      printArmours(file);
      // version 11
      printWeapons(file);
      // version 12
      printThings(file);
      // version 13
      file.println(Directories.getRelativePath(bgFile, realFile));
      file.println(Directories.getAbsolutePath(bgEditor, realFile));
      file.println(1); // use external editor, now always true
      file.println(notes);
      file.println("-- End Notes --");
      // version 14
      file.println(Directories.getRelativePath(picture, realFile));
      // version 15
      file.println(atParts.size());
      for (String atName : atParts.keySet()) {
        file.println(atName);
        file.println(atParts.get(atName));
      }
      // version 16
      for (DerivedValue dv : DerivedValue.values()) {
        file.println(derivedValueChanges.get(dv.ordinal()));
      }
      // version 17
      file.println(fightingTalentsInDocument.size());
      for (String ft : fightingTalentsInDocument) {
        file.println(ft);
      }
      // version 18
      file.println(skin);
      file.println(title);
      // version 19
      file.println(rituals.size());
      for (String s : rituals)
        file.println(s);
      // version 20
      file.println(Directories.getRelativePath(printFile, realFile));
      // version 21
      file.println(animals.size());
      for (int i = 0; i < animals.size(); ++i) {
        animals.get(i).writeToFile(file);
      }
      // version 22
      file.println(bankMoney.size());
      for (int i = 0; i < bankMoney.size(); ++i) {
        file.println(bankCurrencies.get(i));
        file.println(bankMoney.get(i));
      }
      // version 23
      printShields(file);
      // version 24
      file.println(soulAnimal);
      file.println(element);
      file.println(academy);
      file.println(magicSpecialization);
      // version 25
      printWarehouse(file);
      // version 26
      printClothes(file);
      // version 27
      file.println(fightMode);
      file.println(firstHandWeapon);
      file.println(secondHandItem);
      // version 28
      file.println(at1Bonus);
      file.println(at2Bonus);
      file.println(mIsGrounded ? 1 : 0);
      // version 29
      printWeaponBFs(file);
      // version 30
      printShieldBFs(file);
      // version 31
      file.println(dazed ? "1" : "0");
      // version 32
      file.println(extraMarkers);
      // version 33
      file.println(useAUForFight ? "1" : "0");
      file.println(auDifference);
      // version 34
      file.println(remainingStepIncreases);
      // version 35
      printExtraThingData(file);
      printExtraWarehouseData(file);
      // version 36
      file.println(printingFileType.toString());
      // version 37
      file.println(printingZFW);
      // version 38
      printProjectiles(file);
      // version 39
      file.println(internalType);
      // version 40
      file.println(targets.size());
      for (int i = 0; i < targets.size(); ++i) {
        file.println(targets.get(i));
      }
      // version 41
      file.println(hasStumbled ? "1" : "0");
      // version 42
      file.println(farRangedFightParams.writeToString());
      file.println("-End Hero-");
      changed = false;
      file.flush();
    }
    finally {
      if (file != null) {
        file.close();
      }
    }
  }

  private void printProjectiles(PrintWriter file) throws IOException {
    file.println(nrOfProjectiles.size());
    for (String n : nrOfProjectiles.keySet()) {
      file.println(n);
      file.println(nrOfProjectiles.get(n));
    }
  }

  private void printShieldBFs(PrintWriter file) throws IOException {
    file.println(shieldBFs.size());
    for (String n : shieldBFs.keySet()) {
      file.println(n);
      file.println(shieldBFs.get(n));
    }
  }

  private void printWeaponBFs(PrintWriter file) throws IOException {
    file.println(bfs.size());
    for (String n : bfs.keySet()) {
      file.println(n);
      file.println(bfs.get(n));
    }
  }

  private void printExtraWarehouseData(PrintWriter file) throws IOException {
    file.println(extraWarehouseData.size());
    for (Map.Entry<String, ExtraThingData> entry : extraWarehouseData
        .entrySet()) {
      file.println(entry.getKey());
      entry.getValue().store(file);
    }
  }

  private void printExtraThingData(PrintWriter file) throws IOException {
    file.println(extraThingData.size());
    for (Map.Entry<String, ExtraThingData> entry : extraThingData.entrySet()) {
      file.println(entry.getKey());
      entry.getValue().store(file);
    }
  }

  private void printClothes(PrintWriter file) throws IOException {
    file.println(clothes.size());
    for (String clothesName : clothes) {
      Thing thing = Things.getInstance().getThing(clothesName);
      thing.writeToStream(file);
    }
  }

  private void printWarehouse(PrintWriter file) throws IOException {
    int nrOfThings2 = 0;
    for (Integer t : thingsInWarehouse.values()) {
      nrOfThings2 += t;
    }
    file.println(nrOfThings2);
    for (String thingName : thingsInWarehouse.keySet()) {
      Thing thing = Things.getInstance().getThing(thingName);
      for (int i = 0; i < thingsInWarehouse.get(thingName); ++i) {
        thing.writeToStream(file);
      }
    }
  }

  private void printShields(PrintWriter file) throws IOException {
    file.println(shields.size());
    for (int i = 0; i < shields.size(); ++i) {
      file.println(shields.get(i));
    }
  }

  private void printThings(PrintWriter file) throws IOException {
    int nrOfThings = 0;
    for (Integer t : things.values()) {
      nrOfThings += t;
    }
    file.println(nrOfThings);
    for (String thingName : things.keySet()) {
      Thing thing = Things.getInstance().getThing(thingName);
      for (int i = 0; i < things.get(thingName); ++i) {
        thing.writeToStream(file);
      }
    }
  }

  private void printWeapons(PrintWriter file) throws IOException {
    int nrOfWeapons = 0;
    for (Integer t : weapons.values())
      nrOfWeapons += t;
    file.println(nrOfWeapons);
    for (String weaponName : weapons.keySet()) {
      Weapon weapon = Weapons.getInstance().getWeapon(weaponName);
      if (weapon == null) {
        weapon = new Weapon(1, 0, 5, weaponName, 1,
            new dsa.util.Optional<Integer>(14), 50, true, false, false,
            dsa.util.Optional.NULL_INT);
      }
      for (int i = 0; i < weapons.get(weaponName); ++i) {
        weapon.writeToStream(file);
      }
    }
  }

  private void printArmours(PrintWriter file) throws IOException {
    file.println(armours.size());
    for (String armourName : armours) {
      Armour armour = Armours.getInstance().getArmour(armourName);
      file.println(armourName + ";" + armour.getRS() + ";" + armour.getBE()
          + ";" + armour.getWeight() + ";" + armour.getWorth());
    }
  }
  
  public void storeThingsToFile(File f) throws IOException  {
    PrintWriter file = new PrintWriter(new java.io.FileWriter(f));
    try {
      file.println(FILE_VERSION);
      file.println("___Ausruestung___");
      printThings(file);
      printExtraThingData(file);
      file.println("___Kleidung___");
      printClothes(file);
      file.println("___Waffen___");
      printWeapons(file);
      printWeaponBFs(file);
      printProjectiles(file);
      file.println("___Ruestungen___");
      printArmours(file);
      file.println("___Parade___");
      printShields(file);
      printShieldBFs(file);
      file.println("___Lager___");
      printWarehouse(file);
      printExtraWarehouseData(file);
      file.flush();
    }
    finally {
      if (file != null) {
        file.close();
      }
    }
  }
  
  public void readThingsFromFile(long thingTypes, File f) throws IOException {
    BufferedReader file = new BufferedReader(new java.io.FileReader(f));
    try {
      int lineNr = 0;
      String line = file.readLine();
      testEmpty(line);
      int version = parseInt(line, lineNr);
      line = file.readLine(); // "___Ausruestung___"
      if ((thingTypes & THINGS) != 0) {
        things.clear();
        lineNr = readThings(file, lineNr);
        extraThingData.clear();
        lineNr = readExtraThingData(file, lineNr, THINGS);
        line = file.readLine();
        lineNr++;
      }
      else {
        do {
          line = file.readLine();
          ++lineNr;
        }
        while (line != null && !line.equals("___Kleidung___"));
      }
      if ((thingTypes & CLOTHES) != 0) {
        clothes.clear();
        lineNr = readClothes(file, lineNr);
        line = file.readLine();
        lineNr++;
      }
      else {
        do { 
          line = file.readLine();
          ++lineNr;
        }
        while (line != null && !line.equals("___Waffen___"));
      }
      if ((thingTypes & WEAPONS) != 0) {
        lineNr = readWeapons(file, lineNr);
        bfs.clear();
        lineNr = readBFs(file, lineNr, version);
        nrOfProjectiles.clear();
        lineNr = readNrOfProjectiles(file, lineNr, version);
        line = file.readLine();
        lineNr++;      
      }
      else {
        do {
          line = file.readLine();
          ++lineNr;
        }
        while (line != null && !line.equals("___Ruestungen___"));
      }
      if ((thingTypes & ARMOURS) != 0) {
        lineNr = readArmours(file, lineNr);
        line = file.readLine();
        lineNr++;
      }
      else {
        do {
          line = file.readLine();
          ++lineNr;
        }
        while (line != null && !line.equals("___Parade___"));
      }
      if ((thingTypes & SHIELDS) != 0) {
        shields.clear();
        lineNr = readShields(file, lineNr);
        shieldBFs.clear();
        lineNr = readShieldBFs(file, lineNr, version);
        file.readLine();
        lineNr++;
      }
      else {
        do {
          line = file.readLine();
          ++lineNr;
        }
        while (line != null && !line.equals("___Lager___"));
      }
      if ((thingTypes & WAREHOUSE) != 0) {
        thingsInWarehouse.clear();
        lineNr = readWarehouse(file, lineNr);
        extraWarehouseData.clear();
        readExtraThingData(file, lineNr, WAREHOUSE);
      }
      changed = true;
      for (CharacterObserver o : observers) {
        o.thingsChanged();
      }
    }
    finally {
      if (file != null) {
        file.close();
      }
    }
  }

  private int parseInt(String line, int lineNr) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": " + line
          + " ist keine Zahl!");
    }
  }

  private void testEmpty(String s) throws IOException {
    if (s == null) throw new IOException("Unerwartetes Dateiende!");
  }

  public void readFromFile(File f) throws IOException {
    BufferedReader file = new BufferedReader(new java.io.FileReader(f));
    int lineNr = 0;
    String line = file.readLine();
    testEmpty(line);
    int version = parseInt(line, lineNr);
    loadedNewerVersion =  (version > FILE_VERSION);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    name = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    type = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    ap = parseInt(line, lineNr);
    lineNr = readProperties(file, lineNr);
    lineNr = readEnergies(file, lineNr);
    lineNr = readTalents(file, lineNr, version);
    if (version > 1) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      printingTemplateFile = Directories.getAbsolutePath(line, f);
    }
    if (version > 2) {
      lineNr = readVersion3Data(file, lineNr);
    }
    if (version > 3) {
      lineNr = readVersion4Data(file, lineNr);
    }
    if (version > 4) {
      lineNr = readVersion5Data(file, lineNr);
    }
    if (version > 6) {
      lineNr = readVersion7Data(file, lineNr);
    }
    if (version > 7) {
      lineNr = readMoney(file, lineNr);
    }
    if (version > 8) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      nativeTongue = line;
    }
    if (version > 9) {
      lineNr = readVersion10Data(file, lineNr);
    }
    if (version > 10) {
      lineNr = readWeapons(file, lineNr);
    }
    if (version > 11) {
      lineNr = readThings(file, lineNr);
    }
    if (version > 12) {
      lineNr = readVersion13Data(f, file, lineNr);
    }
    if (version > 13) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      picture = Directories.getAbsolutePath(line, f);
    }
    atParts.clear();
    if (version > 14) {
      lineNr = readATParts(file, lineNr);
    }
    lineNr = readDVValueChanges(file, lineNr, version);
    fightingTalentsInDocument.clear();
    if (version > 16) {
      lineNr = readFightingTalentsInDocument(file, lineNr);
    }
    if (version > 17) {
      lineNr = readVersion18Data(file, lineNr);
    }
    else
      skin = "";
    lineNr = readRituals(file, lineNr, version);
    if (version > 19) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      printFile = Directories.getAbsolutePath(line, f);
    }
    else
      printFile = "";
    animals.clear();
    if (version > 20) {
      lineNr = readAnimals(file, lineNr);
    }
    lineNr = readBank(file, lineNr, version);
    shields.clear();
    if (version > 22) {
      lineNr = readShields(file, lineNr);
    }
    if (version > 23) {
      lineNr = readVersion24Data(file, lineNr);
    }
    else {
      soulAnimal = "";
      element = "";
      academy = "";
      magicSpecialization = "";
    }
    thingsInWarehouse.clear();
    if (version > 24) {
      lineNr = readWarehouse(file, lineNr);
    }
    clothes.clear();
    if (version > 25) {
      lineNr = readClothes(file, lineNr);
    }
    if (version > 26) {
      lineNr = readFightData(file, lineNr);
    }
    if (version > 27) {
      lineNr = readFightData2(file, lineNr);
    }
    else {
      at1Bonus = 0;
      at2Bonus = 0;
      mIsGrounded = false;
    }
    lineNr = readBFs(file, lineNr, version);
    lineNr = readShieldBFs(file, lineNr, version);
    if (version > 30) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      dazed = "1".equals(line);
    }
    else
      dazed = false;
    if (version > 31) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      extraMarkers = parseInt(line, lineNr);
    }
    else
      extraMarkers = 0;
    if (version > 32) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      useAUForFight = parseInt(line, lineNr) == 1;
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      auDifference = parseInt(line, lineNr);
    }
    else {
      useAUForFight = false;
      auDifference = 0;
    }
    if (version > 33) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      remainingStepIncreases = parseInt(line, lineNr);
    }
    else {
      remainingStepIncreases = (overallSpellIncreaseTries > 0
          || overallSpellOrTalentIncreaseTries > 0 || overallTalentIncreaseTries > 0) ? 1
          : 0;
    }
    if (version > 34) {
      lineNr = readExtraThingData(file, lineNr, (THINGS | WAREHOUSE));
    }
    if (version > 35) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      try {
        printingFileType = FileType.valueOf(line);
      }
      catch (IllegalArgumentException e) {
        printingFileType = FileType.WordML;
      }
    }
    else {
      String printingFileName = getPrintingTemplateFile().toLowerCase(
          java.util.Locale.GERMAN);
      if (printingFileName.endsWith("xml")) {
        printingFileType = FileType.WordML;
      }
      else if (printingFileName.endsWith("rtf")) {
        printingFileType = FileType.RTF;
      }
      else if (printingFileName.endsWith("html")) {
        printingFileType = FileType.HTML;
      }
      else if (printingFileName.endsWith("htm")) {
        printingFileType = FileType.HTML;
      }
      else if (printingFileName.endsWith("odt")) {
        printingFileType = FileType.ODT;
      }
      else {
        printingFileType = FileType.Unknown;
      }
    }
    if (version > 36) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      printingZFW = parseInt(line, lineNr);
    }
    else {
      printingZFW = -6;
    }
    lineNr = readNrOfProjectiles(file, lineNr, version);
    if (version > 38) {
      lineNr++;
      line = file.readLine();
      internalType = line;
    }
    else {
      internalType = type;
    }
    targets = new ArrayList<String>();
    if (version > 39) {
      lineNr++;
      line = file.readLine();
      int nrOfTargets = parseInt(line, lineNr);
      for (int i = 0; i < nrOfTargets; ++i) {
        lineNr++;
        line = file.readLine();
        targets.add(line);
      }
    }
    if (version > 40) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      hasStumbled = "1".equals(line);
    }
    else hasStumbled = false;
    if (version > 41) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      farRangedFightParams = new FarRangedFightParams(line);
    }
    else farRangedFightParams = new FarRangedFightParams();
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    while (line != null && !line.equals("-End Hero-")) {
      line = file.readLine();
      testEmpty(line);
    }
    setDefaultEnergy(AU, getDefaultProperty(KK) + getDefaultEnergy(LE));
    setCurrentEnergy(AU, getCurrentProperty(KK) + getCurrentEnergy(LE));
    checkForHuntingTalents();
    checkForMirakel();
    calcKO();
    changed = false;
  }

  private int readExtraThingData(BufferedReader file, int lineNr, long types)
      throws IOException {
    if ((types & THINGS) != 0) {
      String line = file.readLine();
      lineNr++;
      testEmpty(line);
      int count = parseInt(line, lineNr);
      for (int i = 0; i < count; ++i) {
        String key = file.readLine();
        lineNr++;
        testEmpty(line);
        ExtraThingData extraData = new ExtraThingData();
        lineNr = extraData.read(file, lineNr);
        extraThingData.put(key, extraData);
      }
    }
    if ((types & WAREHOUSE) != 0) {
      String line = file.readLine();
      lineNr++;
      testEmpty(line);
      int count = parseInt(line, lineNr);
      for (int i = 0; i < count; ++i) {
        String key = file.readLine();
        lineNr++;
        testEmpty(line);
        ExtraThingData extraData = new ExtraThingData();
        lineNr = extraData.read(file, lineNr);
        extraWarehouseData.put(key, extraData);
      }
    }
    return lineNr;
  }

  private int readShieldBFs(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    if (version > 29) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int nrOfEntries = parseInt(line, lineNr);
      for (int i = 0; i < nrOfEntries; ++i) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        String shieldName = line;
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int bf = parseInt(line, lineNr);
        shieldBFs.put(shieldName, bf);
      }
    }
    else {
      for (String shieldName : shields) {
        Shield s = Shields.getInstance().getShield(shieldName);
        shieldBFs.put(shieldName, s.getBF());
      }
    }
    return lineNr;
  }

  private int readNrOfProjectiles(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    if (version > 37) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int nrOfEntries = parseInt(line, lineNr);
      for (int i = 0; i < nrOfEntries; ++i) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        String weaponName = line;
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int nrOfProjs = parseInt(line, lineNr);
        nrOfProjectiles.put(weaponName, nrOfProjs);
      }
    }
    else {
      for (String weaponName : weapons.keySet()) {
        for (int i = 0; i < weapons.get(weaponName); ++i) {
          nrOfProjectiles.put(weaponName + " " + (i + 1), 0);
        }
      }
    }
    return lineNr;
  }

  private int readBFs(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    if (version > 28) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int nrOfEntries = parseInt(line, lineNr);
      for (int i = 0; i < nrOfEntries; ++i) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        String weaponName = line;
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int bf = parseInt(line, lineNr);
        bfs.put(weaponName, bf);
      }
    }
    else {
      for (String weaponName : weapons.keySet()) {
        Weapon w = Weapons.getInstance().getWeapon(weaponName);
        int bf = w != null ? w.getBF() : 0;
        for (int i = 0; i < weapons.get(weaponName); ++i) {
          bfs.put(weaponName + " " + (i + 1), bf);
        }
      }
    }
    return lineNr;
  }

  private int readFightData2(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    at1Bonus = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    at2Bonus = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mIsGrounded = line != null && line.trim().equals("1");
    return lineNr;
  }

  private int readFightData(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    fightMode = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    firstHandWeapon = line;
    if (firstHandWeapon.equals("null")) firstHandWeapon = null;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    secondHandItem = line;
    if (secondHandItem.equals("null")) secondHandItem = null;
    return lineNr;
  }

  private int readClothes(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfClothes = parseInt(line, lineNr);
    for (int i = 0; i < nrOfClothes; ++i) {
      Thing thing = new Thing();
      lineNr = thing.readFromStream(file, lineNr);
      clothes.add(thing.getName());
    }
    return lineNr;
  }

  private int readWarehouse(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfThings = parseInt(line, lineNr);
    for (int i = 0; i < nrOfThings; ++i) {
      Thing thing = new Thing();
      lineNr = thing.readFromStream(file, lineNr);
      addThingToWarehouse(thing.getName());
    }
    return lineNr;
  }

  private int readVersion24Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    line = file.readLine();
    lineNr++;
    testEmpty(line);
    soulAnimal = line;
    line = file.readLine();
    lineNr++;
    testEmpty(line);
    element = line;
    line = file.readLine();
    lineNr++;
    testEmpty(line);
    academy = line;
    line = file.readLine();
    lineNr++;
    testEmpty(line);
    magicSpecialization = line;
    return lineNr;
  }

  private int readShields(BufferedReader file, int lineNr) throws IOException {
    String line;
    line = file.readLine();
    lineNr++;
    testEmpty(line);
    int nrOfShields = parseInt(line, lineNr);
    for (int i = 0; i < nrOfShields; ++i) {
      line = file.readLine();
      lineNr++;
      testEmpty(line);
      shields.add(line);
    }
    return lineNr;
  }

  private int readBank(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    if (version > 21) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int nrOfCurrencies = parseInt(line, lineNr);
      bankMoney.clear();
      bankCurrencies.clear();
      for (int i = 0; i < nrOfCurrencies; ++i) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int currency = parseInt(line, lineNr);
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int value = parseInt(line, lineNr);
        bankMoney.add(value);
        bankCurrencies.add(currency);
      }
    }
    else {
      bankMoney.clear();
      bankCurrencies.clear();
      bankMoney.add(0);
      bankMoney.add(0);
      bankCurrencies.add(0);
      bankCurrencies.add(1);
    }
    return lineNr;
  }

  private int readAnimals(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfAnimals = parseInt(line, lineNr);
    for (int i = 0; i < nrOfAnimals; ++i) {
      Animal animal = new Animal("");
      lineNr = animal.readFromFile(file, lineNr);
      animals.add(animal);
    }
    return lineNr;
  }

  private int readRituals(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    rituals.clear();
    if (version > 18) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int nrOfRituals = parseInt(line, lineNr);
      for (int i = 0; i < nrOfRituals; ++i) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        rituals.add(line);
      }
    }
    else {
      rituals
          .addAll(dsa.model.data.Rituals.getInstance().getStartRituals(type));
    }
    return lineNr;
  }

  private int readVersion18Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    skin = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    title = line;
    return lineNr;
  }

  private int readFightingTalentsInDocument(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfFTs = parseInt(line, lineNr);
    for (int i = 0; i < nrOfFTs; ++i) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      fightingTalentsInDocument.add(line);
    }
    return lineNr;
  }

  private int readDVValueChanges(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    if (version > 15) {
      for (DerivedValue dv : DerivedValue.values()) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        int value = parseInt(line, lineNr);
        derivedValueChanges.set(dv.ordinal(), value);
      }
    }
    else {
      for (DerivedValue dv : DerivedValue.values()) {
        derivedValueChanges.set(dv.ordinal(), 0);
      }
    }
    return lineNr;
  }

  private int readATParts(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfATParts = parseInt(line, lineNr);
    for (int i = 0; i < nrOfATParts; ++i) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      String atName = line;
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int value = parseInt(line, lineNr);
      atParts.put(atName, value);
    }
    return lineNr;
  }

  private int readVersion13Data(File f, BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    bgFile = Directories.getAbsolutePath(line, f);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    bgEditor = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    // useExternalBGEditor = (parseInt(line, lineNr) > 0); // obsolete
    notes = "";
    boolean inNotes = true;
    while (inNotes) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      if (line == null || line.equals("-- End Notes --"))
        inNotes = false;
      else {
        if (notes.length() > 0) notes += System.getProperty("line.separator");
        notes += line;
      }
    }
    return lineNr;
  }

  private int readThings(BufferedReader file, int lineNr) throws IOException {
    String line;
    things.clear();
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfThings = parseInt(line, lineNr);
    for (int i = 0; i < nrOfThings; ++i) {
      Thing thing = new Thing();
      lineNr = thing.readFromStream(file, lineNr);
      this.internalAddThing(thing.getName(), new ExtraThingData(
          ExtraThingData.Type.Thing));
    }
    return lineNr;
  }

  private int readWeapons(BufferedReader file, int lineNr) throws IOException {
    String line;
    weapons.clear();
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfWeapons = parseInt(line, lineNr);
    for (int i = 0; i < nrOfWeapons; ++i) {
      Weapon weapon = new Weapon();
      lineNr = weapon.readFromStream(file, lineNr);
      internalAddWeapon(weapon.getName());
    }
    return lineNr;
  }

  private int readVersion10Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    overallSpellOrTalentIncreaseTries = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    canMeditate = (parseInt(line, lineNr) > 0);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    beModification = parseInt(line, lineNr);
    lineNr++;
    lineNr = readArmours(file, lineNr);
    return lineNr;
  }

  private int readArmours(BufferedReader file, int lineNr) throws IOException {
    String line;
    line = file.readLine();
    testEmpty(line);
    int nrOfArmours = parseInt(line, lineNr);
    armours.clear();
    for (int i = 0; i < nrOfArmours; ++i) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      StringTokenizer tokenizer = new StringTokenizer(line, ";");
      String armourName = tokenizer.nextToken();
      if (dsa.model.data.Armours.getInstance().getArmour(armourName) == null) {
        if (tokenizer.hasMoreTokens()) {
          int be = 1;
          int rs = 1;
          int armourWeight = 100;
          int armourWorth = 0;
          String temp = tokenizer.nextToken();
          rs = parseInt(temp, lineNr);
          temp = tokenizer.nextToken();
          be = parseInt(temp, lineNr);
          temp = tokenizer.nextToken();
          armourWeight = parseInt(temp, lineNr);
          if (tokenizer.hasMoreTokens()) {
            temp = tokenizer.nextToken();
            armourWorth = parseInt(temp, lineNr);
          }
          Armours.getInstance().addArmour(
              new dsa.model.data.Armour(armourName, rs, be, armourWeight,
                  armourWorth));
        }
      }
      armours.add(armourName);
    }
    return lineNr;
  }

  private int readMoney(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int nrOfCurrencies = parseInt(line, lineNr);
    money.clear();
    currencies.clear();
    for (int i = 0; i < nrOfCurrencies; ++i) {
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int currency = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      int value = parseInt(line, lineNr);
      money.add(value);
      currencies.add(currency);
    }
    return lineNr;
  }

  private int readVersion7Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    goodPropertyChangeTries = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    badPropertyChangeTries = parseInt(line, lineNr);
    return lineNr;
  }

  private int readVersion5Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    overallSpellIncreaseTries = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    spellIncreasesPerStep = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    spellToTalentMoves = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mHasGreatMeditation = (parseInt(line, lineNr) > 0);
    return lineNr;
  }

  private int readVersion4Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    overallTalentIncreaseTries = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    talentIncreasesPerStep = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mLEIncreaseTries = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mHasExtraAEIncrease = (parseInt(line, lineNr) > 0);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    fixedLEIncrease = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    fixedAEIncrease = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mrBonus = parseInt(line, lineNr);
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    mAEIncreaseTries = parseInt(line, lineNr);
    return lineNr;
  }

  private int readVersion3Data(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    birthPlace = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    eyeColor = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    hairColor = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    skinColor = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    height = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    weight = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    sex = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    try {
      birthday = Date.parse(line);
    }
    catch (java.text.ParseException e) {
      throw new IOException(e);
    }
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    stand = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    god = line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    age = parseInt(line, lineNr);
    return lineNr;
  }

  private int readTalents(BufferedReader file, int lineNr, int version)
      throws IOException {
    String line;
    line = file.readLine();
    testEmpty(line);
    while (line != null && !line.equals("--")) {
      String talent = line;
      TalentData data = new TalentData();
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.currentValue = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.defaultValue = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.remainingIncreases = parseInt(line, lineNr);
      if (version > 4) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        data.remainingTries = parseInt(line, lineNr);
      }
      if (version > 5) {
        lineNr++;
        line = file.readLine();
        testEmpty(line);
        data.increasesPerStep = parseInt(line, lineNr);
      }
      if (Talents.getInstance().getTalent(talent) == null) {
        javax.swing.JOptionPane.showMessageDialog(null, "Das Talent " + talent
            + " ist unbekannt und wird ignoriert!", "Warnung",
            javax.swing.JOptionPane.WARNING_MESSAGE);
      }
      else
        talents.put(talent, data);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
    }
    return lineNr;
  }

  private int readEnergies(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    while (line != null && !line.equals("--")) {
      Energy energy = Energy.valueOf(line);
      EnergyData data = new EnergyData(false);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.currentValue = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.defaultValue = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.available = (parseInt(line, lineNr) == 1);
      energies.put(energy, data);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
    }
    lineNr++;
    return lineNr;
  }

  private int readProperties(BufferedReader file, int lineNr)
      throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    while (line != null && !line.equals("--")) {
      Property property = Property.valueOf(line);
      CurrentAndDefaultData data = new CurrentAndDefaultData();
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.currentValue = parseInt(line, lineNr);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
      data.defaultValue = parseInt(line, lineNr);
      properties.put(property, data);
      lineNr++;
      line = file.readLine();
      testEmpty(line);
    }
    return lineNr;
  }

  public boolean isChanged() {
    if (changed) return true;
    for (int i = 0; i < animals.size(); ++i) {
      if (animals.get(i).isChanged()) return true;
    }
    if (farRangedFightParams.isChanged()) return true;
    return false;
  }

  private String printingTemplateFile = "";

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetPrintingTemplateFile()
   */
  public String getPrintingTemplateFile() {
    return printingTemplateFile;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetPrintingTemplateFile()
   */
  public void setPrintingTemplateFile(String filePath) {
    if (filePath.equals(this.printingTemplateFile)) return;
    printingTemplateFile = filePath;
    changed = true;
  }

  String birthPlace = "";

  String eyeColor = "";

  String hairColor = "";

  String skinColor = "";

  String height = "";

  String weight = "";

  String sex = "";

  Date birthday = new Date(1, Date.Month.Praios, 1);

  String stand = "";

  String god = "";

  int age = 20;

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetBirthPlace()
   */
  public String getBirthPlace() {
    return birthPlace;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetEyeColor()
   */
  public String getEyeColor() {
    return eyeColor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetHairColor()
   */
  public String getHairColor() {
    return hairColor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetSkinColor()
   */
  public String getSkinColor() {
    return skinColor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetHeight()
   */
  public String getHeight() {
    return height;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetWeight()
   */
  public String getWeight() {
    return weight;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetSex()
   */
  public String getSex() {
    return sex;
  }

  public Date getBirthday() {
    return birthday;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetBirthPlace(java.lang.String)
   */
  public void setBirthPlace(String birthplace) {
    if (birthplace.equals(this.birthPlace)) return;
    this.birthPlace = birthplace;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetEyeColor(java.lang.String)
   */
  public void setEyeColor(String eyeColor) {
    if (eyeColor.equals(this.eyeColor)) return;
    this.eyeColor = eyeColor;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetHairColor(java.lang.String)
   */
  public void setHairColor(String hairColor) {
    if (hairColor.equals(this.hairColor)) return;
    this.hairColor = hairColor;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetSkinColor(java.lang.String)
   */
  public void setSkinColor(String skinColor) {
    if (skinColor.equals(this.skinColor)) return;
    this.skinColor = skinColor;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetHeight(java.lang.String)
   */
  public void setHeight(String height) {
    if (height.equals(this.height)) return;
    this.height = height;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetWeight(java.lang.String)
   */
  public void setWeight(String weight) {
    if (weight.equals(this.weight)) return;
    this.weight = weight;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetSex(java.lang.String)
   */
  public void setSex(String sex) {
    if (sex.equals(this.sex)) return;
    this.sex = sex;
    changed = true;
  }

  public void setBirthday(Date birthday) {
    if (birthday.equals(this.birthday)) return;
    this.birthday = birthday;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetStand()
   */
  public String getStand() {
    return stand;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetStand(java.lang.String)
   */
  public void setStand(String stand) {
    if (stand.equals(this.stand)) return;
    this.stand = stand;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetGod()
   */
  public String getGod() {
    return god;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetGod(java.lang.String)
   */
  public void setGod(String god) {
    if (god.equals(this.god)) return;
    this.god = god;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetAge()
   */
  public int getAge() {
    return age;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#SetAge(int)
   */
  public void setAge(int age) {
    if (age == this.age) return;
    this.age = age;
    changed = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Hero#GetRuf()
   */
  public String getRuf() {
    int step = this.getStep();
    if (step <= 4)
      return "unbekannt";
    else if (step <= 6)
      return "vielversprechend";
    else if (step <= 9)
      return "respektabel";
    else if (step <= 14)
      return "geachtet";
    else if (step <= 18)
      return "berühmt";
    else
      return "legendär";
  }

  int mLEIncreaseTries;

  int mAEIncreaseTries;

  boolean mHasExtraAEIncrease;

  int fixedLEIncrease;

  int fixedAEIncrease;

  public boolean hasLEIncreaseTry() {
    return mLEIncreaseTries > 0;
  }

  public boolean hasExtraAEIncrease() {
    return mHasExtraAEIncrease;
  }

  public int getFixedLEIncrease() {
    return fixedLEIncrease;
  }

  public int getFixedAEIncrease() {
    return fixedAEIncrease;
  }

  ArrayList<Integer> derivedValueChanges = new ArrayList<Integer>();

  public int getCurrentDerivedValueChange(DerivedValue dv) {
    return derivedValueChanges.get(dv.ordinal());
  }

  public void setCurrentDerivedValueChange(DerivedValue dv, int value) {
    derivedValueChanges.set(dv.ordinal(), value);
    for (CharacterObserver o : observers) {
      o.derivedValueChanged(dv);
    }
  }

  private boolean hasQvatPaBasis() {
    return dsa.model.characters.Group.getInstance().getOptions()
        .hasQvatPABasis();
  }

  public int getCurrentDerivedValue(DerivedValue dv) {
    int value = 0;
    if (dv == DerivedValue.MR) {
      value = (getCurrentProperty(Property.KL)
          + getCurrentProperty(Property.MU) + getStep())
          / 3 - 2 * getCurrentProperty(Property.AG) + mrBonus;
      if (hasTalent(SEELE)) {
        int talentValue = getCurrentTalentValue(SEELE);
        if (talentValue > 10) value += talentValue - 10;
      }
    }
    else if (dv == DerivedValue.AT) {
      value = Math
          .round((getCurrentProperty(Property.MU)
              + getCurrentProperty(Property.GE) + getCurrentProperty(Property.KK)) / 5.0f);
    }
    else if (dv == DerivedValue.AW) {
      value = (int) Math
          .floor((getCurrentProperty(Property.MU)
              + getCurrentProperty(Property.IN) + getCurrentProperty(Property.GE)) / 4.0);
    }
    else if (dv == DerivedValue.AB) {
      value = Math
          .round((getCurrentProperty(Property.IN) + getCurrentProperty(Property.GE)) / 4.0f);
    }
    else if (dv == DerivedValue.FK) {
      value = (int) Math
          .floor((getCurrentProperty(Property.IN)
              + getCurrentProperty(Property.FF) + getCurrentProperty(Property.KK)) / 4.0);
    }
    else if (dv == DerivedValue.PA) {
      if (hasQvatPaBasis()) {
        value = Math
            .round((getCurrentProperty(Property.MU)
                + getCurrentProperty(Property.IN) + getCurrentProperty(Property.GE)) / 5.0f);
      }
      else {
        value = Math
            .round((getCurrentProperty(Property.IN)
                + getCurrentProperty(Property.GE) + getCurrentProperty(Property.KK)) / 5.0f);
      }
    }
    else if (dv == DerivedValue.TK) {
      value = getCurrentProperty(Property.KK) * 50;
    }
    value += getCurrentDerivedValueChange(dv);
    return value;
  }

  public int getDefaultDerivedValue(DerivedValue dv) {
    if (dv == DerivedValue.MR) {
      int mr = (getDefaultProperty(Property.KL)
          + getDefaultProperty(Property.MU) + getStep())
          / 3 - 2 * getDefaultProperty(Property.AG) + mrBonus;
      if (hasTalent(SEELE)) {
        int talentValue = getDefaultTalentValue(SEELE);
        if (talentValue > 10) mr += talentValue - 10;
      }
      return mr;
    }
    else if (dv == DerivedValue.AT) {
      return Math
          .round((getDefaultProperty(Property.MU)
              + getDefaultProperty(Property.GE) + getDefaultProperty(Property.KK)) / 5.0f);
    }
    else if (dv == DerivedValue.AW) {
      return (int) Math
          .floor((getDefaultProperty(Property.MU)
              + getDefaultProperty(Property.IN) + getDefaultProperty(Property.GE)) / 4.0);
    }
    else if (dv == DerivedValue.AB) {
      return Math
          .round((getDefaultProperty(Property.IN) + getDefaultProperty(Property.GE)) / 4.0f);
    }
    else if (dv == DerivedValue.FK) {
      return (int) Math
          .floor((getDefaultProperty(Property.IN)
              + getDefaultProperty(Property.FF) + getDefaultProperty(Property.KK)) / 4.0);
    }
    else if (dv == DerivedValue.PA) {
      if (hasQvatPaBasis()) {
        return Math
            .round((getDefaultProperty(Property.MU)
                + getDefaultProperty(Property.IN) + getDefaultProperty(Property.GE)) / 5.0f);
      }
      else {
        return Math
            .round((getDefaultProperty(Property.IN)
                + getDefaultProperty(Property.GE) + getDefaultProperty(Property.KK)) / 5.0f);
      }
    }
    else if (dv == DerivedValue.TK) {
      return getDefaultProperty(Property.KK) * 50;
    }
    return 0;
  }

  public void increaseLEAndAE(int lePlus, int aePlus)
      throws LEAEIncreaseException {
    if (getInternalType().equals("Scharlatan")) {
      if (((lePlus == 0) || (aePlus == 0)) && (lePlus + aePlus > 1)) {
        throw new LEAEIncreaseException(
            "Ein Scharlatan muss auf AE und LE je mindestens 1 Punkt setzen.");
      }
    }
    if (getInternalType().startsWith("Sharizad")) {
      if ((aePlus > 0) && (this.getDefaultEnergy(Energy.AE) + aePlus > 30)) {
        throw new LEAEIncreaseException(
            "Eine Sharizad darf nicht mehr als 30 ASP haben.");
      }
    }
    mLEIncreaseTries--;
    mAEIncreaseTries--;
    changeDefaultEnergy(Energy.LE, lePlus);
    changeDefaultEnergy(Energy.AE, aePlus);
    checkForNextStepIncrease();
    changed = true;
    for (CharacterObserver o : observers) {
      o.increaseTriesChanged();
    }
  }

  public boolean hasAEIncreaseTry() {
    return mAEIncreaseTries > 0;
  }

  public void increaseLE(int lePlus) {
    mLEIncreaseTries--;
    changeDefaultEnergy(Energy.LE, lePlus);
    checkForNextStepIncrease();
    changed = true;
    for (CharacterObserver o : observers) {
      o.increaseTriesChanged();
    }
  }

  public void increaseAE(int aePlus) {
    mAEIncreaseTries--;
    changeDefaultEnergy(Energy.AE, aePlus);
    checkForNextStepIncrease();
    changed = true;
    for (CharacterObserver o : observers) {
      o.increaseTriesChanged();
    }
  }

  public int getOverallSpellIncreaseTries() {
    return overallSpellIncreaseTries;
  }

  public int getSpellOrTalentIncreaseTries() {
    return overallSpellOrTalentIncreaseTries;
  }

  boolean canMeditate;

  public boolean hasGreatMeditation() {
    return mHasGreatMeditation
        && canMeditate
        && (overallSpellIncreaseTries + overallSpellOrTalentIncreaseTries) >= 10;
  }

  public int doGreatMeditation() {
    if (overallSpellIncreaseTries + overallSpellOrTalentIncreaseTries < 10)
      return 0;
    if (overallSpellIncreaseTries >= 10) {
      overallSpellIncreaseTries -= 10;
    }
    else {
      overallSpellOrTalentIncreaseTries -= (10 - overallSpellIncreaseTries);
      overallSpellIncreaseTries = 0;
    }
    if (overallSpellIncreaseTries == 0
        && overallSpellOrTalentIncreaseTries == 0) {
      for (String n : talents.keySet()) {
        if (Talents.getInstance().getTalent(n).isSpell()) {
          talents.get(n).remainingIncreases = 0;
        }
      }
    }
    int change = dsa.control.Dice.roll(6) + 2;
    this.changeDefaultEnergy(Energy.AE, change);
    canMeditate = false;
    checkForNextStepIncrease();
    for (CharacterObserver o : observers) {
      o.defaultEnergyChanged(Energy.AE);
      o.increaseTriesChanged();
    }
    return change;
  }

  void setTalentIncreaseTries(String talent, int count) {
    if (talents.containsKey(talent)) {
      talents.get(talent).increasesPerStep = count;
    }
  }

  public int getMRBonus() {
    return mrBonus;
  }

  String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    if (title.equals(this.title)) return;
    this.title = title;
    changed = true;
  }

  public int getTalentIncreaseTriesPerStep(String talent) {
    if (!hasTalent(talent)) return 0;
    return talents.get(talent).increasesPerStep;
  }

  public void setTalentIncreaseTriesPerStep(String talent, int incrTries) {
    if (hasTalent(talent)) {
      int oldValue = talents.get(talent).increasesPerStep;
      if (oldValue == incrTries) return;
      talents.get(talent).increasesPerStep = incrTries;
      if (talents.get(talent).remainingIncreases == oldValue) {
        talents.get(talent).remainingIncreases = incrTries;
      }
      changed = true;
    }
  }

  private java.util.ArrayList<Integer> money = new java.util.ArrayList<Integer>();

  private java.util.ArrayList<Integer> currencies = new java.util.ArrayList<Integer>();

  private java.util.ArrayList<Integer> bankMoney = new java.util.ArrayList<Integer>();

  private java.util.ArrayList<Integer> bankCurrencies = new java.util.ArrayList<Integer>();

  public int getNrOfCurrencies(boolean bank) {
    return bank ? bankMoney.size() : money.size();
  }

  public int getMoney(int currencyIndex, boolean bank) {
    if (currencyIndex >= 0
        && currencyIndex < (bank ? bankMoney.size() : money.size())) {
      return bank ? bankMoney.get(currencyIndex) : money.get(currencyIndex);
    }
    else
      return 0;
  }

  public void setMoney(int currencyIndex, int value, boolean bank) {
    if (currencyIndex >= 0
        && currencyIndex < (bank ? bankMoney.size() : money.size())) {
      if (bank)
        bankMoney.set(currencyIndex, value);
      else
        money.set(currencyIndex, value);
      changed = true;
      if (!bank) for (CharacterObserver observer : observers)
        observer.weightChanged();
    }
  }

  public void addCurrency(boolean bank) {
    if (bank) {
      bankMoney.add(0);
      bankCurrencies.add(0);
    }
    else {
      money.add(0);
      currencies.add(0);
    }
    changed = true;
  }

  public void removeCurrency(boolean bank) {
    if (bank) {
      if (bankMoney.size() > 0) {
        bankMoney.remove(bankMoney.size() - 1);
        bankCurrencies.remove(bankCurrencies.size() - 1);
        changed = true;
      }
    }
    else {
      if (money.size() > 0) {
        money.remove(money.size() - 1);
        currencies.remove(currencies.size() - 1);
        changed = true;
        for (CharacterObserver observer : observers)
          observer.weightChanged();
      }
    }

  }

  public int getCurrency(int currencyIndex, boolean bank) {
    if (currencyIndex >= 0
        && currencyIndex < (bank ? bankMoney.size() : money.size())) {
      return bank ? bankCurrencies.get(currencyIndex) : currencies
          .get(currencyIndex);
    }
    else
      return 0;
  }

  public void setCurrency(int currencyIndex, int currency, boolean bank) {
    if (currencyIndex >= 0
        && currencyIndex < (bank ? bankMoney.size() : money.size())) {
      if (bank)
        bankCurrencies.set(currencyIndex, currency);
      else
        currencies.set(currencyIndex, currency);
      changed = true;
      if (!bank) for (CharacterObserver observer : observers)
        observer.weightChanged();
    }
  }

  public int getFreeLanguagePoints() {
    java.util.List<Talent> languages = Talents.getInstance()
        .getTalentsInCategory("Sprachen");
    java.util.Iterator<Talent> iterator = languages.iterator();
    int usedPoints = 0;
    boolean master = (getDefaultTalentValue("Sprachen Kennen") > 10);
    while (iterator.hasNext()) {
      Language language = (Language) iterator.next();
      if (this.hasTalent(language.getName())
          && !language.getName().equals(nativeTongue)) {
        if (!language.isOld()) {
          usedPoints += getDefaultTalentValue(language.getName());
          if (master && getDefaultTalentValue(language.getName()) > 1) {
            usedPoints--;
          }
        }
      }
    }
    return this.getDefaultTalentValue("Sprachen Kennen") - usedPoints;
  }

  public int getFreeOldLanguagePoints() {
    java.util.List<Talent> languages = Talents.getInstance()
        .getTalentsInCategory("Sprachen");
    java.util.Iterator<Talent> iterator = languages.iterator();
    int usedPoints = 0;
    boolean master = (getDefaultTalentValue("Alte Sprachen") > 10);
    while (iterator.hasNext()) {
      Language language = (Language) iterator.next();
      if (this.hasTalent(language.getName())
          && !language.getName().equals(nativeTongue)) {
        if (language.isOld()) {
          usedPoints += this.getDefaultTalentValue(language.getName());
          if (master && getDefaultTalentValue(language.getName()) > 1) {
            usedPoints--;
          }
        }
      }
    }
    return this.getDefaultTalentValue("Alte Sprachen") - usedPoints;
  }

  String nativeTongue;

  public String getNativeTongue() {
    return nativeTongue;
  }

  public void setNativeTongue(String tongue) {
    if (tongue.equals(nativeTongue)) return;
    removeTalent(nativeTongue);
    nativeTongue = tongue;
    Talent t = Talents.getInstance().getTalent(nativeTongue);
    Language l = (Language) t;
    if (!hasTalent(nativeTongue)) addTalent(nativeTongue);
    setDefaultTalentValue(nativeTongue, l.getMax(nativeTongue));
    setCurrentTalentValue(nativeTongue, l.getMax(nativeTongue));
    changed = true;
  }

  java.util.TreeSet<String> armours = new java.util.TreeSet<String>();

  public String[] getArmours() {
    String[] temp = new String[armours.size()];
    return armours.toArray(temp);
  }

  public void addArmour(String armourName) {
    armours.add(armourName);
    changed = true;
    for (CharacterObserver observer : observers)
      observer.weightChanged();
  }

  public void removeArmour(String armourName) {
    if (!armours.contains(armourName)) return;
    armours.remove(armourName);
    changed = true;
    for (CharacterObserver observer : observers) {
      observer.armourRemoved(armourName);
      observer.weightChanged();
    }
  }

  int beModification = 0;

  public int getBE() {
    int be = 0;
    for (String armourName : armours) {
      Armour armour = Armours.getInstance().getArmour(armourName);
      if (armour != null) be += armour.getBE();
    }
    return be - beModification > 0 ? be - beModification : 0;
  }

  public int getRS() {
    int rs = 0;
    for (String armourName : armours) {
      Armour armour = Armours.getInstance().getArmour(armourName);
      if (armour != null) rs += armour.getRS();
    }
    return rs;
  }

  public int getBEModification() {
    return beModification;
  }

  java.util.HashMap<String, Integer> weapons = new HashMap<String, Integer>();

  public String[] getWeapons() {
    String[] temp = new String[weapons.size()];
    return weapons.keySet().toArray(temp);
  }

  public String addWeapon(String weaponName) {
    String name2 = internalAddWeapon(weaponName);
    changed = true;
    for (CharacterObserver observer : observers)
      observer.weightChanged();
    return name2;
  }

  String internalAddWeapon(String weaponName) {
    int count = 1;
    String name2 = weaponName;
    while (weapons.containsKey(name2)) {
      ++count;
      name2 = weaponName + " " + count;
    }
    weapons.put(name2, 1);
    Weapon w = Weapons.getInstance().getWeapon(weaponName);
    bfs.put(name2, w != null ? w.getBF() : 0);
    nrOfProjectiles.put(name2, 0);
    return name2;
  }

  public void removeWeapon(String weaponName) {
    if (!weapons.containsKey(weaponName)) return;
    int count = weapons.get(weaponName);
    bfs.remove(weaponName);
    nrOfProjectiles.remove(weaponName);
    if (count == 1) {
      weapons.remove(weaponName);
      for (CharacterObserver observer : observers)
        observer.weaponRemoved(weaponName);
    }
    else {
      weapons.put(weaponName, count - 1);
    }
    changed = true;
    for (CharacterObserver observer : observers)
      observer.weightChanged();
  }

  public int getWeaponCount(String weaponName) {
    if (!weapons.containsKey(weaponName))
      return 0;
    else
      return weapons.get(weaponName);
  }

  java.util.HashMap<String, Integer> things = new java.util.HashMap<String, Integer>();

  public String[] getThings() {
    String[] allThings = new String[things.size()];
    return things.keySet().toArray(allThings);
  }

  public void addThing(String thingName) {
    addThing(thingName, new ExtraThingData(ExtraThingData.Type.Thing));
  }

  public void addThing(String thingName, ExtraThingData extraData) {
    internalAddThing(thingName, extraData);
    changed = true;
    for (CharacterObserver observer : observers)
      observer.weightChanged();
  }

  void internalAddThing(String thingName, ExtraThingData extraData) {
    if (!things.containsKey(thingName)) {
      things.put(thingName, 1);
    }
    else {
      things.put(thingName, things.get(thingName) + 1);
    }
    extraThingData.put(thingName + things.get(thingName), extraData);
  }

  public ExtraThingData getExtraThingData(String thing, boolean inWarehouse,
      int thingNumber) {
    if (!inWarehouse) {
      return extraThingData.get(thing + thingNumber);
    }
    else {
      return extraWarehouseData.get(thing + thingNumber);
    }
  }

  private HashMap<String, ExtraThingData> extraThingData = new HashMap<String, ExtraThingData>();

  public void removeThing(String thingName) {
    if (!things.containsKey(thingName)) return;
    int count = things.get(thingName);
    extraThingData.remove(thingName + count);
    if (count == 1) {
      things.remove(thingName);
      for (CharacterObserver o : observers)
        o.thingRemoved(thingName, false);
    }
    else {
      things.put(thingName, count - 1);
    }
    changed = true;
    for (CharacterObserver observer : observers)
      observer.weightChanged();
  }

  public int getThingCount(String thing) {
    if (!things.containsKey(thing))
      return 0;
    else
      return things.get(thing);
  }

  String bgFile;

  String bgEditor;

  String notes;

  public String getBGFile() {
    return bgFile;
  }

  public String getBGEditor() {
    return bgEditor;
  }

  public String getNotes() {
    return notes;
  }

  public void setBGFile(String fileName) {
    bgFile = fileName;
    changed = true;
  }

  public void setBGEditor(String fileName) {
    bgEditor = fileName;
    changed = true;
  }

  public void setNotes(String notes) {
    this.notes = notes;
    changed = true;
  }

  String picture;

  public void setPicture(String location) {
    picture = location;
    changed = true;
  }

  public String getPictureLocation() {
    return picture;
  }

  Map<String, Integer> atParts = new HashMap<String, Integer>();

  public int getATPart(String talent) {
    if (hasTalent(talent) && atParts.containsKey(talent)) {
      return atParts.get(talent);
    }
    else
      return 0;
  }

  public int getPAPart(String talent) {
    if (hasTalent(talent)) {
      return getCurrentTalentValue(talent) - getATPart(talent);
    }
    else
      return 0;
  }

  public void setATPart(String talent, int value) {
    if (hasTalent(talent)) {
      atParts.put(talent, value);
      for (CharacterObserver o : observers) {
        o.atPADistributionChanged(talent);
      }
      changed = true;
    }
  }

  public void setPAPart(String talent, int value) {
    if (hasTalent(talent)) {
      setATPart(talent, getCurrentTalentValue(talent) - value);
    }
  }

  ArrayList<String> fightingTalentsInDocument = new ArrayList<String>();

  public List<String> getFightingTalentsInDocument() {
    ArrayList<String> result = new ArrayList<String>();
    result.addAll(fightingTalentsInDocument);
    return result;
  }

  public void setFightingTalentsInDocument(List<String> someTalents) {
    fightingTalentsInDocument.clear();
    fightingTalentsInDocument.addAll(someTalents);
    changed = true;
  }

  String skin;

  public String getSkin() {
    return skin;
  }

  public void setSkin(String skin) {
    if (skin.equals(this.skin)) return;
    this.skin = skin;
    changed = true;
  }

  java.util.ArrayList<String> rituals = new java.util.ArrayList<String>();

  public List<String> getRituals() {
    ArrayList<String> r = new ArrayList<String>();
    r.addAll(rituals);
    return r;
  }

  public void addRitual(String ritual) {
    if (rituals.contains(ritual)) return;
    rituals.add(ritual);
    changed = true;
  }

  public void removeRitual(String ritual) {
    rituals.remove(ritual);
    changed = true;
  }

  String printFile;

  public String getPrintFile() {
    return printFile;
  }

  public void setPrintFile(String file) {
    if (!file.equals(printFile)) {
      printFile = file;
      changed = true;
    }
  }

  ArrayList<Animal> animals = new ArrayList<Animal>();

  public int getNrOfAnimals() {
    return animals.size();
  }

  public Animal getAnimal(int index) {
    if (index < 0 || index >= animals.size())
      return null;
    else
      return animals.get(index);
  }

  public void addAnimal(Animal animal) {
    animals.add(animal);
    changed = true;
  }

  public void removeAnimal(int index) {
    animals.remove(index);
    changed = true;
  }

  ArrayList<String> shields = new ArrayList<String>();

  public String[] getShields() {
    String[] dummy = new String[shields.size()];
    return shields.toArray(dummy);
  }

  public void addShield(String shieldName) {
    shields.add(shieldName);
    Shield shield = Shields.getInstance().getShield(shieldName);
    shieldBFs.put(shieldName, shield.getBF());
    changed = true;
    for (CharacterObserver o : observers)
      o.weightChanged();
  }

  public void removeShield(String shieldName) {
    if (!shields.contains(shieldName)) return;
    shields.remove(shieldName);
    shieldBFs.remove(shieldName);
    changed = true;
    for (CharacterObserver o : observers)
      o.shieldRemoved(shieldName);
    for (CharacterObserver o : observers)
      o.weightChanged();
  }

  String element = "";

  public String getElement() {
    return element;
  }

  public void setElement(String element) {
    this.element = element;
    changed = true;
  }

  String academy = "";

  public String getAcademy() {
    return academy;
  }

  public void setAcademy(String academy) {
    this.academy = academy;
    changed = true;
  }

  String magicSpecialization = "";

  public String getSpecialization() {
    return magicSpecialization;
  }

  public void setSpecialization(String specialization) {
    magicSpecialization = specialization;
    changed = true;
  }

  String soulAnimal = "";

  public String getSoulAnimal() {
    return soulAnimal;
  }

  public void setSoulAnimal(String animal) {
    soulAnimal = animal;
    changed = true;
  }

  HashMap<String, Integer> thingsInWarehouse = new HashMap<String, Integer>();

  public void addThingToWarehouse(String item) {
    addThingToWarehouse(item, new ExtraThingData(ExtraThingData.Type.Thing));
  }

  HashMap<String, ExtraThingData> extraWarehouseData = new HashMap<String, ExtraThingData>();

  public void addThingToWarehouse(String item, ExtraThingData extraData) {
    if (thingsInWarehouse.containsKey(item)) {
      int count = thingsInWarehouse.get(item);
      thingsInWarehouse.put(item, count + 1);
    }
    else {
      thingsInWarehouse.put(item, 1);
    }
    extraWarehouseData.put(item + thingsInWarehouse.get(item), extraData);
    changed = true;
  }

  public int getThingInWarehouseCount(String item) {
    if (thingsInWarehouse.containsKey(item)) {
      return thingsInWarehouse.get(item);
    }
    else
      return 0;
  }

  public void removeThingFromWarehouse(String thingName) {
    if (thingsInWarehouse.containsKey(thingName)) {
      int count = thingsInWarehouse.get(thingName);
      extraWarehouseData.remove(thingName + count);
      if (count > 1) {
        thingsInWarehouse.put(thingName, count - 1);
      }
      else {
        thingsInWarehouse.remove(thingName);
        for (CharacterObserver o : observers) {
          o.thingRemoved(thingName, true);
        }
      }
      changed = true;
    }
  }

  public String[] getThingsInWarehouse() {
    String[] thingArray = new String[thingsInWarehouse.size()];
    return thingsInWarehouse.keySet().toArray(thingArray);
  }

  ArrayList<String> clothes = new ArrayList<String>();

  public void addClothes(String item) {
    clothes.add(item);
    changed = true;
  }

  public void removeClothes(String item) {
    clothes.remove(item);
    changed = true;
  }

  public String[] getClothes() {
    String[] all = new String[clothes.size()];
    return clothes.toArray(all);
  }

  public int getTalentIncreasesPerStep() {
    return talentIncreasesPerStep;
  }

  public int getSpellIncreasesPerStep() {
    return spellIncreasesPerStep;
  }

  public int getSpellToTalentMoves() {
    return spellToTalentMoves;
  }

  public boolean canDoGreatMeditation() {
    return mHasGreatMeditation;
  }

  public void setCanDoGreatMeditation(boolean can) {
    if (mHasGreatMeditation == can) return;
    mHasGreatMeditation = can;
    changed = true;
    for (CharacterObserver o : observers)
      o.increaseTriesChanged();
  }

  public void setHasExtraAEIncrease(boolean extra) {
    if (mHasExtraAEIncrease == extra) return;
    mHasExtraAEIncrease = extra;
    changed = true;
  }

  public void setMRBonus(int bonus) {
    if (mrBonus == bonus) return;
    mrBonus = bonus;
    changed = true;
    for (CharacterObserver o : observers)
      o.derivedValueChanged(Hero.DerivedValue.MR);
  }

  public void setBEModification(int mod) {
    if (beModification == mod) return;
    beModification = mod;
    changed = true;
    for (CharacterObserver o : observers)
      o.beModificationChanged();
  }

  public void setTalentIncreasesPerStep(int increases) {
    if (talentIncreasesPerStep == increases) return;
    talentIncreasesPerStep = increases;
    changed = true;
  }

  public void setSpellIncreasesPerStep(int increases) {
    if (spellIncreasesPerStep == increases) return;
    spellIncreasesPerStep = increases;
    changed = true;
  }

  public void setSpellToTalentMoves(int moves) {
    if (spellToTalentMoves == moves) return;
    spellToTalentMoves = moves;
    changed = true;
  }

  public void setFixedLEIncrease(int increase) {
    if (fixedLEIncrease == increase) return;
    fixedLEIncrease = increase;
    changed = true;
  }

  public void setFixedAEIncrease(int increase) {
    if (fixedAEIncrease == increase) return;
    fixedAEIncrease = increase;
    changed = true;
  }

  public void setType(String type) {
    if (this.type.equals(type)) return;
    this.type = type;
    changed = true;
  }

  String fightMode;

  public String getFightMode() {
    return fightMode;
  }

  public void setFightMode(String mode) {
    if (fightMode != null && fightMode.equals(mode)) return;
    fightMode = mode;
    changed = true;
  }

  String firstHandWeapon = null;

  public String getFirstHandWeapon() {
    return firstHandWeapon;
  }

  public void setFirstHandWeapon(String weaponName) {
    if (firstHandWeapon != null && firstHandWeapon.equals(weaponName)) return;
    firstHandWeapon = weaponName;
    changed = true;
  }

  String secondHandItem = null;

  public String getSecondHandItem() {
    return secondHandItem;
  }

  public void setSecondHandItem(String itemName) {
    if (secondHandItem != null && secondHandItem.equals(itemName)) return;
    secondHandItem = itemName;
    changed = true;
  }

  int at1Bonus = 0;

  public int getAT1Bonus() {
    return at1Bonus;
  }

  public void setAT1Bonus(int bonus) {
    if (bonus != at1Bonus) {
      at1Bonus = bonus;
      changed = true;
    }
  }

  int at2Bonus = 0;

  public int getAT2Bonus() {
    return at2Bonus;
  }

  public void setAT2Bonus(int bonus) {
    if (at2Bonus != bonus) {
      at2Bonus = bonus;
      changed = true;
    }
  }

  boolean mIsGrounded = false;

  public boolean isGrounded() {
    return mIsGrounded;
  }

  public void setGrounded(boolean grounded) {
    if (mIsGrounded != grounded) {
      mIsGrounded = grounded;
      changed = true;
      for (CharacterObserver observer : observers)
        observer.fightingStateChanged();
    }
  }

  HashMap<String, Integer> bfs = new HashMap<String, Integer>();

  public void setBF(String weaponName, int weaponNr, int bf) {
    bfs.put(weaponName, bf);
    for (CharacterObserver o : observers)
      o.bfChanged(weaponName);
    changed = true;
  }

  public int getBF(String weaponName, int weaponNr) {
    if (bfs.containsKey(weaponName)) {
      return bfs.get(weaponName);
    }
    else
      return 0;
  }

  private HashMap<String, Integer> shieldBFs = new HashMap<String, Integer>();

  public int getBF(String shieldName) {
    return shieldBFs.get(shieldName);
  }

  public void setBF(String shieldName, int bf) {
    shieldBFs.put(shieldName, bf);
    for (CharacterObserver o : observers)
      o.bfChanged(shieldName);
    changed = true;
  }

  boolean dazed = false;

  public boolean isDazed() {
    return dazed;
  }

  public void setDazed(boolean dazed) {
    if (dazed == this.dazed) return;
    this.dazed = dazed;
    changed = true;
    for (CharacterObserver observer : observers) {
      observer.fightingStateChanged();
    }
  }

  int extraMarkers = 0;

  public int getMarkers() {
    int defaultLEThird = getDefaultEnergy(Energy.LE) / 3;
    int currentLE = getCurrentEnergy(Energy.LE);
    int markers = getExtraMarkers();
    if (currentLE < defaultLEThird)
      markers += 3;
    else if (currentLE < 2 * defaultLEThird) markers += 1;
    return markers;
  }

  public int getExtraMarkers() {
    return extraMarkers;
  }

  public void setExtraMarkers(int markers) {
    if (extraMarkers == markers) return;
    extraMarkers = markers;
    changed = true;
  }

  boolean useAUForFight = false;

  public boolean fightUsesAU() {
    return useAUForFight;
  }

  public void setFightUsesAU(boolean useAU) {
    if (useAUForFight != useAU) {
      useAUForFight = useAU;
      changed = true;
    }
  }

  int auDifference = 0;

  public void changeAU(int difference) {
    if (difference != 0) {
      auDifference += difference;
      if (energies.get(Energy.AU).currentValue + auDifference < 0) {
        auDifference = -energies.get(Energy.AU).currentValue;
      }
      for (CharacterObserver o : observers) {
        o.currentEnergyChanged(Energy.AU);
      }
      changed = true;
    }
  }

  public void fireWeightChanged() {
    for (CharacterObserver o : observers) {
      o.weightChanged();
    }
  }

  int remainingStepIncreases = 0;

  public int getRemainingStepIncreases() {
    return remainingStepIncreases;
  }

  public FileType getPrintingFileType() {
    return printingFileType;
  }

  public void setPrintingFileType(FileType fileType) {
    if (fileType != printingFileType) {
      printingFileType = fileType;
      changed = true;
    }
  }

  FileType printingFileType = FileType.XML;

  private int printingZFW = -6;

  public int getPrintingZFW() {
    return printingZFW;
  }

  public void setPrintingZFW(int zfw) {
    if (printingZFW != zfw) {
      printingZFW = zfw;
      changed = true;
    }
  }

  private HashMap<String, Integer> nrOfProjectiles = new HashMap<String, Integer>();

  public int getNrOfProjectiles(String weaponName) {
    if (nrOfProjectiles.containsKey(weaponName)) {
      return nrOfProjectiles.get(weaponName);
    }
    else
      return 0;
  }

  public void setNrOfProjectiles(String weaponName, int nrOfProjectiles) {
    if (getNrOfProjectiles(weaponName) != nrOfProjectiles) {
      this.nrOfProjectiles.put(weaponName, nrOfProjectiles);
      for (CharacterObserver o : observers) {
        o.weightChanged();
      }
      changed = true;
    }
  }
  
  private String internalType = "";

  public String getInternalType() {
    return internalType;
  }

  public void setInternalType(String typeName) {
    if (!internalType.equals(typeName)) {
      internalType = typeName;
      changed = true;
    }
  }

  public boolean hasPrintingCustomizations() {
    return true;
  }

  public Printer getPrinter() {
    return dsa.control.printing.CharacterPrinter.getInstance();
  }
  
  private boolean loadedNewerVersion = false;

  public boolean hasLoadedNewerVersion() {
    return loadedNewerVersion;
  }

  public void setHasLoadedNewerVersion(boolean newer) {
    loadedNewerVersion = newer;
  }

  public int getCurrentLE() {
    return getCurrentEnergy(Energy.LE);
  }

  public int getMR() {
    return getCurrentDerivedValue(DerivedValue.MR);
  }

  public int getMaxLE() {
    return getDefaultEnergy(Energy.LE);
  }

  public int getNrOfAttacks() {
    if (getFightMode().equals("Zwei Waffen")) {
      return 2;
    }
    else {
      return 1;
    }
  }

  public int getNrOfParades() {
    if (getFightMode().equals("Zwei Waffen") || getFightMode().equals("Waffe + Parade, separat")) {
      if (getCurrentTalentValue("Linkshändig") >= 9) {
        return 2;
      }
    }
    return 1;
  }

  public Optional<Integer> getAT(int nr) {
    if (nr == 0) {
      return Fighting.getFirstATValue(this);
    }
    else if (nr == 1) {
      return Fighting.getSecondATValue(this);
    }
    else return Optional.NULL_INT;
  }

  public Optional<Integer> getPA(int nr) {
    if (nr == 0) {
      return Fighting.getFirstPAValue(this);
    }
    else if (nr == 1) {
      return Fighting.getSecondPAValue(this);
    }
    else return Optional.NULL_INT;
  }

  public DiceSpecification getTP(int nr) {
    if (nr == 0) {
      return Fighting.getFirstTP(this);
    }
    else {
      return Fighting.getSecondTP(this);
    }
  }

  public void setCurrentLE(int le) {
    setCurrentEnergy(Energy.LE, le);
  }

  public List<String> getFightingWeapons() {
    ArrayList<String> weapons = new ArrayList<String>();
    weapons.add(getFirstHandWeapon());
    if (getFightMode().equals("Zwei Waffen")) {
      weapons.add(getSecondHandItem());
    }
    return weapons;
  }
  
  private ArrayList<String> targets = new ArrayList<String>();
  
  public void setTarget(int weaponIndex, String target) {
    if (weaponIndex < 0) return;
    if (weaponIndex > 1) return;
    while (weaponIndex >= targets.size()) targets.add("");
    if (targets.get(weaponIndex).equals(target)) return;
    targets.set(weaponIndex, target);
    changed = true;
  }
  
  public String getTarget(int weaponIndex) {
    if (weaponIndex < 0) return "";
    if (weaponIndex >= targets.size()) return "";
    return targets.get(weaponIndex);
  }
  
  public void fireActiveWeaponsChanged() {
    for (CharacterObserver o : observers) o.activeWeaponsChanged();
  }
  
  private boolean hasStumbled = false;

  public boolean hasStumbled() {
    return hasStumbled;
  }

  public void setHasStumbled(boolean hasStumbled) {
    if (this.hasStumbled != hasStumbled) {
      this.hasStumbled = hasStumbled;
      changed = true;
    }
  }

  public int getATBonus(int nr) {
    return (nr == 0) ? getAT1Bonus() : getAT2Bonus();
  }

  public void setATBonus(int nr, int bonus) {
    if (nr == 0) setAT1Bonus(bonus);
    else setAT2Bonus(bonus);
  }
  
  private FarRangedFightParams farRangedFightParams;

  public FarRangedFightParams getFarRangedFightParams() {
    return farRangedFightParams;
  }
}

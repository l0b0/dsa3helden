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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.LEAEIncreaseException;
import dsa.model.characters.Property;
import dsa.model.data.Talents;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;

public class HeroStepIncreaser {
  
  private Hero hero;
  private String log;
  
  public HeroStepIncreaser(Hero hero) {
    this.hero = hero;
    log = "";
  }
  
  public void increaseStepsAutomatically(int nrOfSteps) {
    for (int i = 0; i < nrOfSteps; ++i) {
      if (hero.getRemainingStepIncreases() > 0) {
        if (!log.equals("")) log += "\n\n";
        log +=   "Steigerung auf Stufe " + (hero.getStep() - nrOfSteps + i + 1);
        log += "\n=======================";
        increaseStep();
      }
    }
  }
  
  public String getLog() {
    return log;
  }
  
  public boolean tryToIncreaseGoodProperty(Property property) {
    int currentValue = hero.getDefaultProperty(property);
    for (int i = 0; i < 3; ++i) {
      int diceThrow = Dice.roll(20);
      if (diceThrow >= currentValue) {
        hero.changeDefaultProperty(property, 1);
        return true;
      }
    }    
    return false;
  }
  
  public boolean tryToDecreaseBadProperty(Property property) {
    int currentValue = hero.getDefaultProperty(property);
    for (int i = 0; i < 3; ++i) {
      int diceThrow = Dice.roll(20);
      if (diceThrow <= currentValue) {
        hero.changeDefaultProperty(property, -1);
        return true;
      }
    }    
    return false;
  }
  
  private void increaseStep() {
    int oldRemainingSteps = hero.getRemainingStepIncreases();
    increaseGoodProperties();
    decreaseBadProperties();
    increaseEnergies();
    increaseSpells();
    increaseTalents();
    if (oldRemainingSteps == hero.getRemainingStepIncreases()) {
      // in strange cases, there could be increase tries left 
      // e.g., not enough spells to use all tries?
      hero.removeRemainingIncreaseTries();
      log +="\nWarnung: nicht alle Steigerungsversuche verbraucht!";
      if (oldRemainingSteps == hero.getRemainingStepIncreases()) {
        log += "\nFehler: konnte nicht komplett steigern!";
      }
    }
  }
  
  private static final class PropertyAndValue {
    public PropertyAndValue(Property p, int v) {
      this.p = p;
      this.v = v;
    }
    public Property getProperty() { return p; }
    public int getValue() { return v; }
    private Property p;
    private int v;
  }
  
  private static final class PropertyComparer implements Comparator<PropertyAndValue> {
    
    private boolean higher;
    
    public PropertyComparer(boolean takeHigherValues) {
      higher = takeHigherValues;
    }

    public int compare(PropertyAndValue o1, PropertyAndValue o2) {
      if (o1.v < o2.v) return higher ? 1 : -1;
      if (o1.v > o2.v) return higher ? -1 : 1;
      return 0;
    }
  }

  private void increaseGoodProperties() {
    int nrOfPropertiesOver17 = 0;
    int nrOfPropertiesOver15 = 0;
    ArrayList<PropertyAndValue> properties = new ArrayList<PropertyAndValue>();
    for (int i = 0 ; i < 7 ; ++i) {
      Property p = Property.values()[i];
      int v = hero.getDefaultProperty(p);
      if (v > 15) ++nrOfPropertiesOver15;
      if (v > 17) ++nrOfPropertiesOver17;        
      if (hero.hasPropertyChangeTry(p)) {
        properties.add(new PropertyAndValue(p, v));
      }
    }
    if (properties.size() == 0) return;
    Collections.sort(properties, new PropertyComparer(true));
    
    // determine the index of the property which shall be increased
    int candidateIndex = 0;
    if (nrOfPropertiesOver15 >= 2) {
      if (nrOfPropertiesOver17 >= 5) {
        candidateIndex = properties.size() - 1;
      }
      else if (nrOfPropertiesOver17 >= 3) {
        if (nrOfPropertiesOver15 >= 4) {
          candidateIndex += 4;
        }
        else {
          candidateIndex += 3;
        }
      }
      else {
        candidateIndex += 2;
      }
    }
    else if (nrOfPropertiesOver15 == 1) {
      candidateIndex += 1; 
    }
    if (candidateIndex >= properties.size()) {
      candidateIndex = properties.size() - 1;
    }
    
    // determine the possible properties which could be increased
    int value = properties.get(candidateIndex).getValue();
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < properties.size(); ++i) {
      if (properties.get(i).getValue() == value) {
        indexes.add(i);
      }
    }
    
    // randomly select a property
    int indexesIndex = Dice.roll(indexes.size()) - 1;
    int propertyIndex = indexes.get(indexesIndex);
    Property property = properties.get(propertyIndex).getProperty();
    
    // and finally, try to increase it
    boolean success = tryToIncreaseGoodProperty(property);
    if (success) {
      log += "\n" + property + " auf " + hero.getDefaultProperty(property) + " erhöht";
    }
    else {
      log += "\nErhöhen von " + property + " nicht gelungen, bleibt auf " + hero.getDefaultProperty(property);
    }
    hero.removePropertyChangeTry(true);
  }
  
  private void decreaseBadProperties() {
    ArrayList<PropertyAndValue> properties = new ArrayList<PropertyAndValue>();
    boolean hasPropertyHigherThan7 = false;
    boolean hasPropertyHigherThan1 = false;
    for (int i = 0; i < 7; ++i) {
      Property p = Property.values()[i + 7];
      int v = hero.getDefaultProperty(p);
      if (v > 7) hasPropertyHigherThan7 = true;
      if (v > 1) hasPropertyHigherThan1 = true;
      if (hero.hasPropertyChangeTry(p)) {
        properties.add(new PropertyAndValue(p, v));
      }
    }
    if (properties.size() == 0) return;
    Collections.sort(properties, new PropertyComparer(true));
    
    int candidateIndex = 0; // by default, lower the highest one
    if (!hasPropertyHigherThan7 && hasPropertyHigherThan1) {
      candidateIndex = properties.size() - 1; // lower the lowest one
      // ... which is higher than 1
      while (properties.get(candidateIndex).getValue() <= 1 && candidateIndex > 0) {
        --candidateIndex;
      }
    }
    
    // determine the possible properties which could be increased
    int value = properties.get(candidateIndex).getValue();
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    for (int i = 0; i < properties.size(); ++i) {
      if (properties.get(i).getValue() == value) {
        indexes.add(i);
      }
    }
    
    // randomly select a property
    int indexesIndex = Dice.roll(indexes.size()) - 1;
    int propertyIndex = indexes.get(indexesIndex);
    Property property = properties.get(propertyIndex).getProperty();
    
    // and finally, try to increase it
    boolean success = tryToDecreaseBadProperty(property);
    if (success) {
      log += "\n" + property + " auf " + hero.getDefaultProperty(property) + " erniedrigt";
    }
    else {
      log += "\nErniedrigen von " + property + " nicht gelungen, bleibt auf " + hero.getDefaultProperty(property);
    }
    hero.removePropertyChangeTry(false);
  }
  
  private void increaseEnergies() {
    if (!hero.hasEnergy(Energy.AE)) {
      if (hero.hasLEIncreaseTry()) {
        int delta = Dice.roll(6) + hero.getFixedLEIncrease();
        hero.increaseLE(delta);
        log += "\nLE um " + delta + " auf " + hero.getDefaultEnergy(Energy.LE) + " erhöht";
      }
      return;
    }
    boolean doneGreatMeditation = false;
    if (hero.canDoGreatMeditation() && hero.hasGreatMeditation()) {
      int oldAE = hero.getDefaultEnergy(Energy.AE);
      hero.doGreatMeditation();
      doneGreatMeditation = true;
      int delta = hero.getDefaultEnergy(Energy.AE) - oldAE;
      log += "\nGroße Meditation: AE um " + delta + " auf " + hero.getDefaultEnergy(Energy.AE) + " erhöht";
    }
    if (hero.hasExtraAEIncrease()) {
      if (hero.hasLEIncreaseTry()) {
        int delta = Dice.roll(6) + hero.getFixedLEIncrease();
        hero.increaseLE(delta);
        log += "\nLE um " + delta + " auf " + hero.getDefaultEnergy(Energy.LE) + " erhöht";
      }
      if (hero.hasAEIncreaseTry()) {
        int delta = Dice.roll(6) + hero.getFixedAEIncrease();
        hero.increaseAE(delta);
        log += "\nAE um " + delta + " auf " + hero.getDefaultEnergy(Energy.AE) + " erhöht";
      }
    }
    else if (hero.hasLEIncreaseTry()) {
      int increase = Dice.roll(6) + hero.getFixedLEIncrease();
      int aeIncrease = increase / 2;
      if (!doneGreatMeditation && (increase % 2 != 0)) {
        ++aeIncrease;
      }
      if (hero.getInternalType().startsWith("Sharizad")) {
        int currentAE = hero.getDefaultEnergy(Energy.AE);
        int maxIncrease = 30 - currentAE;
        if (maxIncrease < 0) maxIncrease = 0;
        if (aeIncrease > maxIncrease) aeIncrease = maxIncrease;
      }
      if (hero.isMagicDilletant()) {
        aeIncrease = (increase > 4) ? 2 : ((increase > 1) ? 1 : 0);
      }
      try {
        hero.increaseLEAndAE(increase - aeIncrease, aeIncrease);
        log += "\nLE um " + (increase - aeIncrease) + " auf " + hero.getDefaultEnergy(Energy.LE) + " erhöht";
        log += "\nAE um " + aeIncrease + " auf " + hero.getDefaultEnergy(Energy.AE) + " erhöht";
      }
      catch (LEAEIncreaseException e) {
        // should not happen
        e.printStackTrace(); // will show error dialog
      }
    }
  }
  
  private static final class TalentData {
    private String name;
    private int value;
    public String getName() {
      return name;
    }
    public int getValue() {
      return value;
    }
    public void setValue(int v) {
      value = v;
    }
    public TalentData(String n, int v) {
      name = n;
      value = v;
    }
  }
  
  private static final class TalentDataComparer implements Comparator<TalentData> {
    
    private boolean higher;
    
    public TalentDataComparer(boolean takeHigherValues) {
      higher = takeHigherValues;
    }

    public int compare(TalentData o1, TalentData o2) {
      if (o1.getValue() > o2.getValue()) return higher ? -1 : 1;
      if (o1.getValue() < o2.getValue()) return higher ? 1 : -1;
      return 0;
    }
    
  }
  
  private void increaseSpells() {
    if (!hero.isMagicDilletant() && (hero.getOverallSpellIncreaseTries() + hero.getSpellOrTalentIncreaseTries() < 1)) return;
    ArrayList<TalentData> spellsUnder16 = new ArrayList<TalentData>();
    ArrayList<TalentData> allSpells = new ArrayList<TalentData>();
    for (String category : Talents.getInstance().getKnownCategories()) {
      for (Talent talent : Talents.getInstance().getTalentsInCategory(category)) {
        if (talent.isSpell() && hero.hasTalent(talent.getName())) {
          int value = hero.getDefaultTalentValue(talent.getName());
          allSpells.add(new TalentData(talent.getName(), value));
          if (value < 16) {
            spellsUnder16.add(new TalentData(talent.getName(), value));
          }
        }
      }
    }
    // first pass: only spells lower than 16
    increaseSpells(spellsUnder16);
    // second pass: all spells (also those lower than 16)
    if (!hero.isMagicDilletant()) increaseSpells(allSpells);
  }
  
  private void increaseSpells(ArrayList<TalentData> spells) {
    int increaseTries = hero.getOverallSpellIncreaseTries() + hero.getSpellOrTalentIncreaseTries(); 
    if (hero.isMagicDilletant()) increaseTries = spells.size() * 3;
    if (increaseTries < 1) return;
    // split the candidates into several categories (see below)
    ArrayList<TalentData> with3Increases = new ArrayList<TalentData>();
    ArrayList<TalentData> specialities = new ArrayList<TalentData>();
    ArrayList<TalentData> with1Increase = new ArrayList<TalentData>();
    ArrayList<TalentData> others = new ArrayList<TalentData>();
    for (TalentData t : spells) {
      int increaseTriesPerStep = hero.getTalentIncreaseTriesPerStep(t.getName()); 
      if (increaseTriesPerStep > 2) {
        with3Increases.add(t);
      }
      else if (increaseTriesPerStep == 1) {
        with1Increase.add(t);
      }
      else if (increaseTriesPerStep == 0) {
        // strange ... ignore
        continue;
      }
      else {
        Spell spell = (Spell) Talents.getInstance().getTalent(t.getName());
        if (spell.getCategory().equals(hero.getSpecialization())) {
          specialities.add(t);
        }
        else {
          others.add(t);
        }
      }
    }
    // sort the categories according to the talent value
    Collections.sort(with3Increases, new TalentDataComparer(false));
    TalentDataComparer comparer = new TalentDataComparer(true);
    Collections.sort(with1Increase, comparer);
    Collections.sort(specialities, comparer);
    Collections.sort(others, comparer);
    // first increase those with 3 increases
    increaseTries = tryTalentIncrease(with3Increases, increaseTries);
    if (increaseTries < 1) return;
    // then those of the speciality
    increaseTries = tryTalentIncrease(specialities, increaseTries);
    // then those with only one increase
    increaseTries = tryTalentIncrease(with1Increase, increaseTries);
    // finally, the rest (two increases, not speciality --> other mage spells)
    tryTalentIncrease(others, increaseTries);
  }
  
  private int tryTalentIncrease(ArrayList<TalentData> talents, int increaseTries) {
    for (int i = 0; i < talents.size(); ++i) {
      if (increaseTries < 1) break;
      // try to increase as much as possible
      increaseTries = tryMaximumIncrease(talents.get(i).getName(), increaseTries);
    }
    return increaseTries;
  }
  
  private int tryMaximumIncrease(String talent, int increaseTries) {
    int oldValue = hero.getDefaultTalentValue(talent);
    if (oldValue == 18) return increaseTries;
    while (hero.getTalentIncreaseTries(talent) > 0 && increaseTries > 0) {
      tryTalentIncrease(talent, hero.getDefaultTalentValue(talent));
      --increaseTries;
    }
    int newValue = hero.getDefaultTalentValue(talent);
    if (newValue > oldValue) {
      log += "\n" + talent + " um " + (newValue - oldValue) + " auf " + newValue + " erhöht";
    }
    else {
      log += "\nErhöhung von " + talent + " nicht gelungen, bleibt auf " + oldValue;
    }
    return increaseTries;
  }
  
  private int tryOneIncrease(String talent, int increaseTries) {
    if (hero.getTalentIncreaseTries(talent) < 1) return increaseTries;
    int oldValue = hero.getDefaultTalentValue(talent);
    if (oldValue == 18) return increaseTries;
    while (hero.getTalentIncreaseTries(talent) > 0 && increaseTries > 0) {
      tryTalentIncrease(talent, oldValue);
      --increaseTries;
      if (hero.getDefaultTalentValue(talent) > oldValue) break;
    }
    int newValue = hero.getDefaultTalentValue(talent);
    if (newValue > oldValue) {
      log += "\n" + talent + " um " + (newValue - oldValue) + " auf " + newValue + " erhöht";
    }
    else {
      log += "\nErhöhung von " + talent + " nicht gelungen, bleibt auf " + oldValue;
    }    
    return increaseTries;
  }
  
  private void tryTalentIncrease(String talent, int currentValue) {
    int diceThrow = Dice.roll(6) + Dice.roll(6);
    if (currentValue > 9) diceThrow += Dice.roll(6);
    hero.removeTalentIncreaseTry(talent, diceThrow > currentValue);    
  }
  
  private void increaseTalents() {
    int increaseTries = hero.getOverallTalentIncreaseTries();
    if (increaseTries < 1) return;
    // first, the fighting talents. Increase those of which
    // the character carries a weapon
    HashSet<String> weaponTalents = new HashSet<String>();
    for (String weaponName : hero.getWeapons()) {
      Weapon w = Weapons.getInstance().getWeapon(weaponName);
      int weaponType = w.getType();
      weaponTalents.add(Weapons.getCategoryName(weaponType));
    }
    ArrayList<TalentData> fightingTalents = new ArrayList<TalentData>();
    for (String name : weaponTalents) {
      int value = hero.getDefaultTalentValue(name);
      if (value < 18) {
        fightingTalents.add(new TalentData(name, value));
      }
    }
    Collections.sort(fightingTalents, new TalentDataComparer(true));
    increaseTries = tryTalentIncrease(fightingTalents, increaseTries);
    if (increaseTries < 1) return;
    
    // second, the two special intuitive talents
    final String DANGER_SENSE = "Gefahreninstinkt";
    final String SENSES = "Sinnesschärfe";
    if (hero.getDefaultTalentValue(DANGER_SENSE) < 10) {
      increaseTries = tryMaximumIncrease(DANGER_SENSE, increaseTries);
      if (increaseTries < 1) return;
    }
    if (hero.getDefaultTalentValue(SENSES) < 10) {
      increaseTries = tryMaximumIncrease(SENSES, increaseTries);
      if (increaseTries < 1) return;
    }
    
    // third, the highest 3 talents, as much as possible
    HashSet<String> talentNames = new HashSet<String>();
    ArrayList<TalentData> talents = new ArrayList<TalentData>();
    for (String category : Talents.getInstance().getKnownCategories()) {
      for (Talent t : Talents.getInstance().getTalentsInCategory(category)) {
        if (!t.isLanguage() && !t.isSpell() && !t.isFightingTalent() && hero.hasTalent(t.getName())) {
          if (!t.getName().equals(SENSES) && !t.getName().equals(DANGER_SENSE)) {
            if (!talentNames.contains(t.getName()) && hero.getDefaultTalentValue(t.getName()) < 18) {
              talents.add(new TalentData(t.getName(), hero.getDefaultTalentValue(t.getName())));
              talentNames.add(t.getName());
            }
          }
        }
      }
    }
    Collections.sort(talents, new TalentDataComparer(true));
    int endIndex = Math.min(3, talents.size());
    for (int i = 0; i < endIndex; ++i) {
      increaseTries = tryMaximumIncrease(talents.get(i).getName(), increaseTries);
      if (increaseTries < 1) return;
    }
    for (int i = 0; i < endIndex; ++i) {
      talents.remove(0);
    }
    
    // fourth, the highest 14 talents -- just 1 increase
    endIndex = Math.min(14, talents.size());
    for (int i = 0; i < endIndex; ++i) {
      increaseTries = tryOneIncrease(talents.get(i).getName(), increaseTries);
      if (increaseTries < 1) return;
    }
    // fifth, the highest 14 talents -- a second increase (if possible)
    for (int i = 0; i < endIndex; ++i) {
      increaseTries = tryOneIncrease(talents.get(i).getName(), increaseTries);
      if (increaseTries < 1) return;
    }
    
    // sixth, always the highest talent which is still possible
    PriorityQueue<TalentData> remainingTalents = 
      new PriorityQueue<TalentData>(talents.size(), new TalentDataComparer(true));
    for (int i = 0; i < talents.size(); ++i) {
      TalentData t = talents.get(i);
      if (hero.getTalentIncreaseTries(t.getName()) > 0) {
        t.setValue(hero.getDefaultTalentValue(t.getName()));
        if (t.getValue() < 18) {
          remainingTalents.offer(t);
        }
      }
    }
    while (remainingTalents.peek() != null && increaseTries >= 1) {
      TalentData t = remainingTalents.peek();
      increaseTries = tryOneIncrease(t.getName(), increaseTries);
      if (hero.getTalentIncreaseTries(t.getName()) <= 0) {
        t = remainingTalents.poll();
      }
    }
  }

}

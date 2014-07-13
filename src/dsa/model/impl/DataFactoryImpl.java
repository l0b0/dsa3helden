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
package dsa.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import dsa.model.DataFactory;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Adventure;
import dsa.model.data.Talents;
import dsa.model.talents.Talent;

/**
 * 
 */
public class DataFactoryImpl extends DataFactory {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateTalent(java.lang.String, , , )
   */
  public dsa.model.talents.NormalTalent createNormalTalent(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3) {
    return new NormalTalent(arg0, arg1, arg2, arg3, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHero()
   */
  public Hero createHero() {
    return new HeroImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHero(dsa.data.Hero)
   */
  public Hero createHero(Hero prototype) {
    if (prototype instanceof HeroImpl) {
      try {
        return (Hero) ((HeroImpl) prototype).clone();
      }
      catch (CloneNotSupportedException e) {
        return null;
      }
    }
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHeroFromWiege(java.io.File)
   */
  public Hero createHeroFromWiege(File file) throws java.io.IOException {
    return WiegeImporter.importFromWiege(file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateTalent(java.lang.String, , , , int)
   */
  public dsa.model.talents.NormalTalent createNormalTalent(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, int arg4) {
    return new NormalTalent(arg0, arg1, arg2, arg3, arg4);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHeroFromFile(java.io.File)
   */
  public Hero createHeroFromFile(File file) throws IOException {
    HeroImpl hero = new HeroImpl();
    hero.readFromFile(file, false);
    return hero;
  }
  
  public Hero createHeroFromString(String serializedForm) {
	  HeroImpl hero = new HeroImpl();
	  try {
		  hero.readFromString(serializedForm);
		  return hero;
	  }
	  catch (IOException e) {
		  return null;
	  }
  }
  
  public Hero createHeroFromPrototypeFile(File file) throws java.io.IOException {
	HeroImpl hero = new HeroImpl();
	hero.readFromFile(file, true);
	return hero;
  }
  
  private static String getStringDiff(String first, String second)
  {
    if (first.equals(second)) return first;
    return first + " / " + second;
  }
  
  private static void calcMoneyDiff(Hero first, Hero second, HeroImpl diff, boolean bank) {
    HashSet<Integer> currencies = new HashSet<Integer>();
    HashMap<Integer, Integer> firstCurrencies = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> secondCurrencies = new HashMap<Integer, Integer>();
    for (int i = 0; i < first.getNrOfCurrencies(bank); ++i) {
      currencies.add(first.getCurrency(i, bank));
      firstCurrencies.put(first.getCurrency(i, bank), i);
    }
    for (int i = 0; i < second.getNrOfCurrencies(bank); ++i) {
      currencies.add(second.getCurrency(i, bank));
      secondCurrencies.put(second.getCurrency(i, bank), i);
    }
    for (Integer i : currencies) {
      if (firstCurrencies.containsKey(i) && !secondCurrencies.containsKey(i)) {
        diff.addCurrency(bank);
        diff.setCurrency(diff.getNrOfCurrencies(bank) -1, i, bank);
        diff.setMoney(diff.getNrOfCurrencies(bank) - 1, -first.getMoney(firstCurrencies.get(i), bank), bank);
      }
      else if (!firstCurrencies.containsKey(i) && secondCurrencies.containsKey(i)) {
        // nothing to do
      }
      else if (firstCurrencies.containsKey(i) && secondCurrencies.containsKey(i)) {
        diff.setMoney(secondCurrencies.get(i), 
            second.getMoney(secondCurrencies.get(i), bank) - first.getMoney(firstCurrencies.get(i), bank), 
            bank);
      }
    }    
  }
  
  private static void addDiffThing(HeroImpl diff, String thing, int type) {
    if (type == 0) diff.addThing(thing, "");
    else if (type == 1) diff.addThing(thing, "Lager");
    else if (type == 2) diff.addClothes(thing);
    else if (type == 3) diff.addArmour(thing);
    else if (type == 4) diff.addShield(thing);
    else if (type == 5) diff.addWeapon(thing);
    else if (type == 6) diff.addRitual(thing);
    else throw new InternalError();
  }
  
  private static void createThingDiff(Hero first, Hero second, HeroImpl diff, int type) {
    HashSet<String> secondThings = new HashSet<String>();
    String[] st = new String[1];
    
    if (type == 0) st = second.getThings();
    else if (type == 1) st = second.getThingsInContainer("Lager");
    else if (type == 2) st = second.getClothes();
    else if (type == 3) st = second.getArmours();
    else if (type == 4) st = second.getShields();
    else if (type == 5) st = second.getWeapons();
    else if (type == 6) st = second.getRituals().toArray(st);
    else throw new InternalError();
    
    for (String thing : st) secondThings.add(thing);
    
    HashSet<String> firstThings = new HashSet<String>();
    String[] ft = new String[1];
    
    if (type == 0) ft = first.getThings();
    else if (type == 1) ft = first.getThingsInContainer("Lager");
    else if (type == 2) ft = first.getClothes();
    else if (type == 3) ft = first.getArmours();
    else if (type == 4) ft = first.getShields();
    else if (type == 5) ft = first.getWeapons();
    else if (type == 6) ft = first.getRituals().toArray(ft);
    else throw new InternalError();
    
    for (String thing : ft) firstThings.add(thing);
    for (String thing : secondThings) {
      if (!firstThings.contains(thing)) {
        
        if (type == 0) diff.removeThing(thing, false);
        else if (type == 1) diff.removeThing(thing, true);
        else if (type == 2) diff.removeClothes(thing);
        else if (type == 3) diff.removeArmour(thing);
        else if (type == 4) diff.removeShield(thing);
        else if (type == 5) diff.removeWeapon(thing);
        else if (type == 6) diff.removeRitual(thing);
        else throw new InternalError();
        
        addDiffThing(diff, dsa.util.Strings.addChangeTag(thing, dsa.util.Strings.ChangeTag.Added), type);
      }
    }
    for (String thing : firstThings) {
      if (!secondThings.contains(thing)) {
        addDiffThing(diff, dsa.util.Strings.addChangeTag(thing, dsa.util.Strings.ChangeTag.Removed), type);
      }
    }
    
    
  }
  
  public Hero createHeroDifference(Hero first, Hero second)
  {
    HeroImpl diff = null;
    
    try {
      diff = (HeroImpl)((HeroImpl)second).clone();
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
    
    for (Property property : Property.values()) {
      diff.setCurrentProperty(property, second.getDefaultProperty(property) - first.getDefaultProperty(property));
    }
    for (Energy energy : Energy.values()) {
      diff.setCurrentEnergy(energy, second.getDefaultEnergy(energy) - first.getDefaultEnergy(energy));
    }
    for (String category : Talents.getInstance().getKnownCategories()) {
      createTalentDiff(first, second, diff, category);
    }
    createTalentDiff(first, second, diff, "Sprachen");
    
    diff.setAP(second.getAP() - first.getAP());
    diff.setStepDifference(second.getStep() - first.getStep());
    diff.setType(getStringDiff(first.getType(), second.getType()));
    diff.setBirthPlace(getStringDiff(first.getBirthPlace(), second.getBirthPlace()));
    diff.setEyeColor(getStringDiff(first.getEyeColor(), second.getEyeColor()));
    diff.setHairColor(getStringDiff(first.getHairColor(), second.getHairColor()));
    diff.setSkinColor(getStringDiff(first.getSkinColor(), second.getSkinColor()));
    
    // height, weight, sex?
    // Birthday?
    
    diff.setStand(getStringDiff(first.getStand(), second.getStand()));
    diff.setGod(getStringDiff(first.getGod(), second.getGod()));
    diff.setAge(second.getAge() - first.getAge());
    diff.setTitle(getStringDiff(first.getTitle(), second.getTitle()));

    diff.setName(getStringDiff(first.getName(), second.getName()) + " (Vergleich)");
    
    // Ruf?
    
    for (Hero.DerivedValue dv : Hero.DerivedValue.values()) {
      diff.setCurrentDerivedValueChange(dv, second.getDefaultDerivedValue(dv) - first.getDefaultDerivedValue(dv));
    }
    calcMoneyDiff(first, second, diff, false);
    calcMoneyDiff(first, second, diff, true);
    
    createThingDiff(first, second, diff, 3); // armours
    createThingDiff(first, second, diff, 5); // weapons 
    createThingDiff(first, second, diff, 4); // shields
    
    createThingDiff(first, second, diff, 0); // things
    
    // bgfile, bgeditor -- no diff
    
    diff.setNotes(getStringDiff(first.getNotes(), second.getNotes()));
    
    // picture -- no diff
    
    // at, pa parts ??
    
    diff.setSkin(getStringDiff(first.getSkin(), second.getSkin()));
    
    createThingDiff(first, second, diff, 6); // rituals
    
    // animals?
    
    diff.setElement(getStringDiff(first.getElement(), second.getElement()));
    diff.setAcademy(getStringDiff(first.getAcademy(), second.getAcademy()));
    diff.setSpecialization(getStringDiff(first.getSpecialization(), second.getSpecialization()));
    diff.setSoulAnimal(getStringDiff(first.getSoulAnimal(), second.getSoulAnimal()));
    
    createThingDiff(first, second, diff, 1); // things in warehouse
    createThingDiff(first, second, diff, 2); // clothes
    
    diff.setMRBonus(second.getMRBonus() - first.getMRBonus());
    diff.setBEModification(second.getBEModification() - first.getBEModification());
    diff.setTalentIncreasesPerStep(second.getTalentIncreasesPerStep() - first.getTalentIncreasesPerStep());
    diff.setSpellIncreasesPerStep(second.getSpellIncreasesPerStep() - first.getSpellIncreasesPerStep());
    diff.setSpellToTalentMoves(second.getSpellToTalentMoves() - first.getSpellToTalentMoves());
    diff.setFixedLEIncrease(second.getFixedLEIncrease() - first.getFixedLEIncrease());
    diff.setFixedAEIncrease(second.getFixedAEIncrease() - first.getFixedAEIncrease());
    
    // fighting attributes -- no diff
    
    diff.setInternalType(getStringDiff(first.getInternalType(), second.getInternalType()));
    // magic dilletant? (it's a bool ...)
    
    // adventures
    while (diff.getAdventures().length > 0) {
      diff.removeAdventure(0);
    }
    Adventure[] firstAdventures = first.getAdventures();
    Adventure[] secondAdventures = second.getAdventures();
    int index = 0;
    for (Adventure a : secondAdventures) {
      boolean found = false;
      for (Adventure b : firstAdventures) {
        if (b.getName().equals(a.getName())) {
          found = true;
          break;
        }
      }
      if (!found) {
        Adventure c = new Adventure(index++, a.getName(), a.getAP());
        diff.addAdventure(c);
      }
    }
    
    diff.setSO(second.getSO() - first.getSO());
    
    diff.setIsDifference();
    
    return diff;
  }

  private static void createTalentDiff(Hero first, Hero second, HeroImpl diff, String category) {
    for (Talent talent : Talents.getInstance().getTalentsInCategory(category)) {
      if (first.hasTalent(talent.getName()) && !second.hasTalent(talent.getName())) {
        diff.setCurrentTalentValue(talent.getName(), 0);
      }
      else if (!first.hasTalent(talent.getName()) && second.hasTalent(talent.getName())) {
        diff.addTalent(talent.getName());
        diff.setDefaultTalentValue(talent.getName(), 0);
        diff.setCurrentTalentValue(talent.getName(), second.getDefaultTalentValue(talent.getName()));
      }
      else if (first.hasTalent(talent.getName())) {
        diff.setCurrentTalentValue(talent.getName(), second.getDefaultTalentValue(talent.getName()) - first.getDefaultTalentValue(talent.getName()));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateFightingTalent(java.lang.String, boolean)
   */
  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      int beMinus) {
    return new FightingTalent(name, false, beMinus, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateFightingTalent(java.lang.String, boolean,
   *      int)
   */
  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus) {
    return new FightingTalent(name, projectile, beMinus, 1);
  }

  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus, int increases) {
    return new FightingTalent(name, projectile, beMinus, increases);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateSpell(java.lang.String, , , )
   */
  public dsa.model.talents.Spell createSpell(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, StringTokenizer spellAttributes) throws IOException {
    return new SpellImpl(arg0, arg1, arg2, arg3, 1, spellAttributes, false);
  }

  public dsa.model.talents.Spell createUserDefinedSpell(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, String category, String origin) {
    return new SpellImpl(arg0, arg1, arg2, arg3, 1, category, origin);
  }

  @Override
  public Talent createOtherTalent(String name, int increases) {
    return new SpecialTalent(name, increases);
  }

  @Override
  public dsa.model.talents.Spell createSpellForLoading() {
    return new SpellImpl();
  }

}

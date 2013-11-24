/*
 Copyright (c) 2006 [Joerg Ruedenauer]
 
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
 along with Foobar; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Animal;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
import dsa.model.data.Currencies;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;
import dsa.util.FileTransformer;
import dsa.util.LookupTable;

/**
 * 
 */
public class CharacterPrinter {

  private static class NameComparator implements Comparator<Spell> {
    public NameComparator(int direction) {
      dir = direction;
    }

    private final int dir;

    public int compare(Spell spell1, Spell spell2) {
      int r = spell1.getName().compareTo(spell2.getName());
      return dir == 0 ? 0 : (dir == 1 ? r : -r);
    }
  }

  private static class OriginComparator implements Comparator<Spell> {
    public OriginComparator(int direction) {
      dir = direction;
    }

    private final int dir;

    public int compare(Spell spell1, Spell spell2) {
      int r = spell1.getOrigin().compareTo(spell2.getOrigin());
      return dir == 0 ? 0 : (dir == 1 ? r : -r);
    }
  }

  private static class CategoryComparator implements Comparator<Spell> {
    public CategoryComparator(int direction) {
      dir = direction;
    }

    private final int dir;

    public int compare(Spell spell1, Spell spell2) {
      int r = spell1.getCategory().compareTo(spell2.getCategory());
      return dir == 0 ? 0 : (dir == 1 ? r : -r);
    }
  }

  private static class ValueComparator implements Comparator<Spell> {
    public ValueComparator(int direction, Hero aHero) {
      hero = aHero;
      dir = direction;
    }

    public int compare(Spell spell1, Spell spell2) {
      int v1 = hero.getDefaultTalentValue(spell1.getName());
      int v2 = hero.getDefaultTalentValue(spell2.getName());
      int r = v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
      return dir == 0 ? 0 : (dir == 1 ? r : -r);
    }

    private final Hero hero;

    private final int dir;
  }

  private static class SpellSorter {

    private class SpellHolder implements Comparable<SpellHolder> {
      private final Spell spell;

      public int compareTo(SpellHolder s2) {
        for (Comparator<Spell> c : comparators) {
          int result = c.compare(spell, s2.spell);
          if (result != 0) return result;
        }
        return 0;
      }

      public SpellHolder(Spell s) {
        spell = s;
      }
    }

    private final ArrayList<Comparator<Spell>> comparators;
    
    private static final String SPELL_KEY = "Zauber";

    public SpellSorter(Hero hero) {
      comparators = new ArrayList<Comparator<Spell>>();
      java.util.prefs.Preferences prefs = java.util.prefs.Preferences
          .userNodeForPackage(dsa.gui.frames.SpellFrame.class);
      String name = SPELL_KEY;
      int size = prefs.getInt(name + "SorterCount", 0);
      for (int i = 0; i < size; ++i) {
        int column = prefs.getInt(name + "SorterColumn" + i, -1);
        int direction = prefs.getInt(name + "SorterDirection" + i, 0);
        switch (column) {
        case 0:
          comparators.add(new NameComparator(direction));
          break;
        case 1:
          comparators.add(new CategoryComparator(direction));
          break;
        case 2:
          comparators.add(new OriginComparator(direction));
          break;
        case 3:
          comparators.add(new ValueComparator(direction, hero));
          break;
        default:
          break;
        }
      }
    }

    public void restoreSorting(ArrayList<Spell> spells) {
      SpellHolder[] s = new SpellHolder[spells.size()];
      for (int i = 0; i < spells.size(); ++i)
        s[i] = new SpellHolder(spells.get(i));
      Arrays.sort(s);
      for (int i = 0; i < spells.size(); ++i)
        spells.set(i, s[i].spell);
    }

  }
  
  private static class PrinterHolder {
    private static final CharacterPrinter PRINTER = new CharacterPrinter();
  }

  public static CharacterPrinter getInstance() {
    return PrinterHolder.PRINTER;
  }

  private static int getCategoryNumber(String category) {
    if (category.startsWith("Kampf")) {
      return 1;
    }
    else if (category.startsWith("Körperliche")) {
      return 2;
    }
    else if (category.startsWith("Gesellschaft")) {
      return 3;
    }
    else if (category.startsWith("Natur")) {
      return 4;
    }
    else if (category.startsWith("Wissen")) {
      return 5;
    }
    else if (category.startsWith("Handwerk")) {
      return 6;
    }
    else if (category.startsWith("Intuitiv")) {
      return 7;
    }
    else {
      return 8;
    }
  }

  public void printCharacter(Hero character, File template, File output,
      java.awt.Component parent, String message) throws java.io.IOException {
    LookupTable table = new LookupTable('!');
    printProperties(character, table);
    printEnergies(character, table);
    printBasicAttributes(character, table);

    printDerivedValues(character, table);

    printTalents(character, table);
    printSpells(character, table);

    int overallWeight = 0;
    overallWeight = printArmours(character, table, overallWeight);

    int paradeNr = 1;
    int weight = 0;
    boolean goodLH = character.getDefaultTalentValue("Linkshändig") >= 9;
    int shieldATMod = 0;
    int shieldPAMod = 0;
    for (String parade : character.getShields()) {
      dsa.model.data.Shield shield = dsa.model.data.Shields.getInstance()
          .getShield(parade);
      if (shield == null) continue;
      table.addItem("SN" + paradeNr, shield.getName());
      table.addItem("SP" + paradeNr, ""
          + (goodLH ? shield.getPaMod2() : shield.getPaMod()));
      table.addItem("SA" + paradeNr, "" + shield.getAtMod());
      table.addItem("SBE" + paradeNr, "" + shield.getBeMod());
      table.addItem("SFK" + paradeNr, "" + shield.getFkMod());
      table.addItem("SBF" + paradeNr, "" + character.getBF(shield.getName()));
      table.addItem("Sg" + paradeNr, "" + shield.getWeight());
      weight += shield.getWeight();
      overallWeight += shield.getWeight();
      shieldATMod += shield.getAtMod();
      shieldPAMod += goodLH ? shield.getPaMod2() : shield.getPaMod();
      paradeNr++;
    }
    while (paradeNr <= 3) {
      table.addItem("SN" + paradeNr, "");
      table.addItem("SP" + paradeNr, "");
      table.addItem("SA" + paradeNr, "");
      table.addItem("SBE" + paradeNr, "");
      table.addItem("SFK" + paradeNr, "");
      table.addItem("SBF" + paradeNr, "");
      table.addItem("Sg" + paradeNr, "");
      paradeNr++;
    }
    table.addItem("Sgsu", "" + weight / 40.0f);

    overallWeight = printWeapons(character, table, overallWeight, shieldATMod, shieldPAMod);
    printLanguages(character, table);

    printMoney(character, table);
    overallWeight = printThings(character, table, overallWeight);

    printMoneyWeights(character, table);

    float overallWeightF = overallWeight / 40.0f;
    table.addItem("Tgsu", "" + overallWeightF);
    table.addItem("tk", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.TK));
    table.addItem("Tk", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.TK) / 40.0f);

    printFightingTalents(character, table);

    table.addItem("bem", character.getNotes());

    printRituals(character, table);

    printAnimals(character, table);

    printMagicAttributes(character, table);
    // ...

    FileTransformer transformer = new FileTransformer(template, output);
    transformer.setLookupTable(table);
    transformer.setXMLAwareness(template.getName().toLowerCase(java.util.Locale.GERMAN).trim().endsWith(".xml"));
    transformer.transform(parent, message);
  }

  private static void printMagicAttributes(Hero character, LookupTable table) {
    table.addItem("Ele", character.getElement());
    table.addItem("SeT", character.getSoulAnimal());
    table.addItem("Aka", character.getAcademy());
    table.addItem("SpG", character.getSpecialization());
  }

  private static void printAnimals(Hero character, LookupTable table) {
    int animalIndex = 0;
    for (; animalIndex < character.getNrOfAnimals(); ++animalIndex) {
      int i = animalIndex + 1;
      Animal animal = character.getAnimal(animalIndex);
      table.addItem("TN" + i, animal.getName());
      table.addItem("TT" + i, animal.getCategory());
      int j = 0;
      for (; j < animal.getNrOfAttributes(); ++j) {
        table.addItem("TAN" + i, animal.getAttributeTitle(j), true);
        table.addItem("TAW" + i, animal.getAttributeValue(j).toString(), true);
      }
      while (j < 1) {
        table.addItem("TAN" + i, "", true);
        table.addItem("TAW" + i, "", true);
        ++j;
      }
      String[] things = animal.getThings();
      int weightSum = 0;
      for (int k = 0; k < things.length; ++k) {
        table.addItem("TGN" + i, things[k], true);
        table.addItem("TGC" + i, "" + animal.getThingCount(things[k]), true);
        int weight = 0;
        Thing thing = Things.getInstance().getThing(things[k]);
        if (thing != null) {
          weight = thing.getWeight();
        }
        weight *= animal.getThingCount(things[k]);
        weightSum += weight;
        table.addItem("TGG" + i, "" + weight, true);
      }
      table.addItem("TGSU" + i, "" + (weightSum / 40.0f));
      for (int k = things.length + 1; k <= 30; ++k) {
        table.addItem("TGN" + i, "", true);
        table.addItem("TGC" + i, "", true);
        table.addItem("TGG" + i, "", true);
      }
    }
    while (animalIndex < 5) {
      int i = animalIndex + 1;
      table.addItem("TN" + i, "");
      table.addItem("TT" + i, "");
      table.addItem("TAN" + i, "", true);
      table.addItem("TAW" + i, "", true);
      for (int k = 1; k <= 30; ++k) {
        table.addItem("TGN" + i, "");
        table.addItem("TGC" + i, "");
        table.addItem("TGG" + i, "");
      }
      table.addItem("TGSU" + i, "");
      ++animalIndex;
    }
  }

  private static void printRituals(Hero character, LookupTable table) {
    String rtitel = dsa.model.data.Rituals.getInstance().getRitualsTitle(
        character.getType());
    if (rtitel == null || rtitel.equals("")) rtitel = "Rituale";
    table.addItem("rtitel", rtitel);
    for (char ch = 'a'; ch != 'k'; ++ch) {
      table.addItem("rn" + ch, "");
      table.addItem("rw" + ch, "");
    }
    for (String ritual : character.getRituals()) {
      table.addItem("son", ritual, true);
    }
    if (character.getRituals().size() == 0) {
      table.addItem("son", "");
    }
  }

  private static void printFightingTalents(Hero character, LookupTable table) {
    char ftAppendix = 'a';
    java.util.List<String> fts = character.getFightingTalentsInDocument();
    for (Talent t : Talents.getInstance().getTalentsInCategory("Kampftalente")) {
      if (fts.contains(t.getName())) {
        table.addItem("kampf" + ftAppendix, t.getName());
        int beMod = ((dsa.model.talents.FightingTalent) t).getBEMinus();
        table.addItem("bm" + ftAppendix, "(BE " + beMod + ")");
        table.addItem("Bm" + ftAppendix, "" + beMod);
        if (((dsa.model.talents.FightingTalent) t).isLefthandIndicator()) {
          int value = character.getCurrentTalentValue(t.getName());
          int at = -7 + Math.round(value / 2.0f);
          int pa = -7 + value - Math.round(value / 2.0f);
          if (at > 0) at = 0;
          if (pa > 0) pa = 0;
          table.addItem("a1" + ftAppendix, "" + at);
          table.addItem("P1" + ftAppendix, "" + pa);
          table.addItem("a2" + ftAppendix, "" + at);
          table.addItem("P2" + ftAppendix, "" + pa);
        }
        else {
          int atPart = character.getATPart(t.getName());
          if (atPart < 0) atPart = 0;
          int paPart = character.getPAPart(t.getName());
          if (paPart < 0) paPart = 0;
          int at = character.getDefaultDerivedValue(Hero.DerivedValue.AT)
              + atPart;
          int pa = character.getDefaultDerivedValue(Hero.DerivedValue.PA)
              + paPart;
          table.addItem("a1" + ftAppendix, "" + at);
          if (((dsa.model.talents.FightingTalent) t).isProjectileTalent()) {
            table.addItem("P1" + ftAppendix, "-");
          }
          else {
            table.addItem("P1" + ftAppendix, "" + pa);
          }
          int actualBE = character.getBE() + beMod;
          if (actualBE < 0) actualBE = 0;
          pa -= Math.round(actualBE / 2.0f);
          at -= (int) Math.floor(actualBE / 2.0f);
          table.addItem("a2" + ftAppendix, "" + at);
          if (((dsa.model.talents.FightingTalent) t).isProjectileTalent()) {
            table.addItem("P2" + ftAppendix, "-");
          }
          else {
            table.addItem("P2" + ftAppendix, "" + pa);
          }
        }
        ++ftAppendix;
      }
    }
    while (ftAppendix != 'n') {
      table.addItem("kampf" + ftAppendix, "");
      table.addItem("a1" + ftAppendix, "");
      table.addItem("P1" + ftAppendix, "");
      table.addItem("a2" + ftAppendix, "");
      table.addItem("P2" + ftAppendix, "");
      table.addItem("bm" + ftAppendix, "");
      table.addItem("Bm" + ftAppendix, "");
      ++ftAppendix;
    }
  }

  private static void printMoneyWeights(Hero character, LookupTable table) {
    long moneyWeightTenthOfSkrupel = 0;
    for (int i = 0; i < character.getNrOfCurrencies(false); ++i) {
      long moneyWeight = character.getMoney(i, false)
          * dsa.model.data.Currencies.getInstance().getWeight(
              character.getCurrency(i, false));
      moneyWeightTenthOfSkrupel += moneyWeight;
      float moneyWeightF = moneyWeight / 250.0f;
      table.addItem("Mg", "" + moneyWeightF, true);
    }
    float moneyWeightUnzes = moneyWeightTenthOfSkrupel / 250.0f;
    float moneyWeightStones = moneyWeightUnzes / 40.0f;

    table.addItem("Gg", "" + moneyWeightStones);
  }

  private static int printThings(Hero character, LookupTable table, int overallWeight) {
    int weight;
    weight = 0;
    for (String thingName : character.getThings()) {
      table.addItem("gna", thingName, true);
      int tweight = dsa.model.data.Things.getInstance().getThing(thingName)
          .getWeight();
      int count = character.getThingCount(thingName);
      tweight *= count;
      weight += tweight;
      table.addItem("guz", "" + tweight, true);
      table.addItem("Gc", "" + count, true);
      overallWeight += tweight;
    }
    table.addItem("GSU", "" + (weight / 40.0f));
    if (character.getThings().length == 0) {
      table.addItem("gna", "");
      table.addItem("guz", "");
      table.addItem("Gc", "");
    }

    for (String thingName : character.getThingsInWarehouse()) {
      table.addItem("Lna", thingName, true);
      int tweight = dsa.model.data.Things.getInstance().getThing(thingName)
          .getWeight();
      int count = character.getThingInWarehouseCount(thingName);
      tweight *= count;
      weight += tweight;
      table.addItem("Luz", "" + tweight, true);
      table.addItem("Lc", "" + count, true);
    }
    if (character.getThingsInWarehouse().length == 0) {
      table.addItem("Lna", "");
      table.addItem("Luz", "");
      table.addItem("Lc", "");
    }

    for (String clothesName : character.getClothes()) {
      table.addItem("Kna", clothesName, true);
      int tweight = dsa.model.data.Things.getInstance().getThing(clothesName)
          .getWeight();
      table.addItem("Kuz", "" + tweight, true);
    }
    if (character.getClothes().length == 0) {
      table.addItem("Kna", "");
      table.addItem("Kuz", "");
    }
    return overallWeight;
  }

  private static void printMoney(Hero character, LookupTable table) {
    for (int i = 0; i < character.getNrOfCurrencies(false); ++i) {
      table.addItem("Mn", Currencies.getInstance().getCurrency(
          character.getCurrency(i, false)), true);
      table.addItem("Mv", "" + character.getMoney(i, false), true);
    }
    for (int i = 0; i < character.getNrOfCurrencies(true); ++i) {
      table.addItem("MBn", Currencies.getInstance().getCurrency(
          character.getCurrency(i, true)), true);
      table.addItem("MBv", "" + character.getMoney(i, true), true);
    }
  }

  private static void printLanguages(Hero character, LookupTable table) {
    char langC = 'a';
    for (Talent language : Talents.getInstance().getTalentsInCategory(
        "Sprachen")) {
      table.addItem("Spn", language.getName(), true);
      int maxLang = ((dsa.model.talents.Language) language).getMax(character
          .getNativeTongue());
      String valueAppendix = "/" + maxLang;
      if (character.hasTalent(language.getName())
          && character.getDefaultTalentValue(language.getName()) > 0) {
        table.addItem("Spw", ""
            + character.getDefaultTalentValue(language.getName()), true);
        table.addItem("Spm", "" + maxLang, true);
        table.addItem("sp" + langC, language.getName() + " "
            + character.getDefaultTalentValue(language.getName())
            + valueAppendix);
        ++langC;
      }
      else {
        table.addItem("Spw", "-", true);
        table.addItem("Spm", "" + maxLang, true);
      }
    }
    while (langC != 'j') {
      table.addItem("sp" + langC, "");
      ++langC;
    }
    table.addItem("Spw", "", true);
    table.addItem("Spm", "", true);
    table.addItem("Spn", "", true);
  }

  private static int printWeapons(Hero character, LookupTable table, int overallWeight, int shieldATMod, int shieldPAMod) {
    int weight;
    float weightF;
    int weaponNr = 1;
    int fkWeaponNr = 1;
    weight = 0;
    int fkWeight = 0;
    for (String name : character.getWeapons()) {
      Weapon weapon = Weapons.getInstance().getWeapon(name);
      if (weapon == null) continue;
      int constDamage = weapon.getConstDamage();
      if (weapon.getKKBonus().hasValue()
          && !weapon.isProjectileWeapon()
          && character.getDefaultProperty(Property.KK) > weapon.getKKBonus()
              .getValue()) {
        constDamage += character.getDefaultProperty(Property.KK)
            - weapon.getKKBonus().getValue();
      }
      String tp = weapon.getW6damage() + "W+" + constDamage;
      if (!weapon.isProjectileWeapon()) { // Nahkampfwaffe
        table.addItem("wn" + weaponNr, weapon.getName());
        table.addItem("Wn", weapon.getName(), true);
        table.addItem("wk" + weaponNr, weapon.getKKBonus().toString());
        table.addItem("Wk", weapon.getKKBonus().toString(), true);
        table.addItem("wv" + weaponNr, ""); // we don't have WV
        table.addItem("wtp" + weaponNr, tp);
        table.addItem("Wtp", tp, true);
        table.addItem("wb" + weaponNr, "" + character.getBF(name, 0));
        table.addItem("Wb", "" + character.getBF(name, 0), true);
        String talent = Weapons.getCategoryName(weapon.getType());
        int atPart = character.getATPart(talent);
        if (atPart < 0) atPart = 0;
        int paPart = character.getPAPart(talent);
        if (paPart < 0) paPart = 0;
        int at = character.getDefaultDerivedValue(Hero.DerivedValue.AT)
            + atPart;
        int pa = character.getDefaultDerivedValue(Hero.DerivedValue.PA)
            + paPart;
        table.addItem("w1" + weaponNr, "" + at);
        table.addItem("W1", "" + at, true);
        table.addItem("w2" + weaponNr, "" + pa);
        table.addItem("W2", "" + pa, true);
        int actualBE = character.getBE()
            + ((dsa.model.talents.FightingTalent) Talents.getInstance()
                .getTalent(talent)).getBEMinus();
        if (actualBE < 0) actualBE = 0;
        pa -= Math.round(actualBE / 2.0f);
        at -= (int) Math.floor(actualBE / 2.0f);
        if (!weapon.isTwoHanded()
            && !"Zwei Waffen".equals(character.getFightMode())) {
          at += shieldATMod;
          pa += shieldPAMod;
        }
        table.addItem("w3" + weaponNr, "" + at);
        table.addItem("W3", "" + at, true);
        table.addItem("w4" + weaponNr, "" + pa);
        table.addItem("W4", "" + pa, true);
        table.addItem("wg" + weaponNr, "" + weapon.getWeight());
        table.addItem("Wg", "" + weapon.getWeight(), true);
        weight += weapon.getWeight();
        weaponNr++;
      }
      else {
        table.addItem("fn" + fkWeaponNr, weapon.getName());
        table.addItem("Fn", weapon.getName(), true);
        table.addItem("ft" + fkWeaponNr, tp);
        table.addItem("Ft", tp, true);
        StringBuffer fr = new StringBuffer();
        StringBuffer  fm = new StringBuffer();
        for (int i = 0; i < Weapon.Distance.values().length; ++i) {
          if (i > 0) {
            fr.append('/');
          }
          fr.append(weapon.getDistancePaces(Weapon.Distance.values()[i]));
          int m = weapon.getDistanceCategoryTPMod(Weapon.Distance.values()[i]);
          if (m > 0) {
            fm.append('+');
          }
          fm.append(m);
        }
        table.addItem("fr" + fkWeaponNr, fr.toString());
        table.addItem("fm" + fkWeaponNr, fm.toString());
        table.addItem("fg" + fkWeaponNr, "" + weapon.getWeight());
        table.addItem("Fg", "" + weapon.getWeight(), true);
        String talent = Weapons.getCategoryName(weapon.getType());
        int at = character.getDefaultDerivedValue(Hero.DerivedValue.FK)
            + character.getCurrentTalentValue(talent);
        table.addItem("f1" + fkWeaponNr, "" + at);
        table.addItem("F1", "" + at, true);
        fkWeight += weapon.getWeight();
        fkWeaponNr++;
      }
      overallWeight += weapon.getWeight();
    }
    table.addItem("Fn", "", true);
    table.addItem("Ft", "", true);
    table.addItem("Fg", "", true);
    table.addItem("F1", "", true);
    table.addItem("Wn", "", true);
    table.addItem("Wk", "", true);
    table.addItem("Wtp", "", true);
    table.addItem("W1", "", true);
    table.addItem("W2", "", true);
    table.addItem("W3", "", true);
    table.addItem("W4", "", true);
    table.addItem("Wg", "", true);
    table.addItem("Wb", "", true);
    for (; weaponNr <= 5; ++weaponNr) {
      table.addItem("wn" + weaponNr, "");
      table.addItem("wk" + weaponNr, "");
      table.addItem("wv" + weaponNr, "");
      table.addItem("w1" + weaponNr, "");
      table.addItem("w2" + weaponNr, "");
      table.addItem("w3" + weaponNr, "");
      table.addItem("w4" + weaponNr, "");
      table.addItem("wtp" + weaponNr, "");
      table.addItem("wb" + weaponNr, "");
      table.addItem("wg" + weaponNr, "");
    }
    for (; fkWeaponNr <= 3; ++fkWeaponNr) {
      table.addItem("fn" + fkWeaponNr, "");
      table.addItem("ft" + fkWeaponNr, "");
      table.addItem("fr" + fkWeaponNr, "");
      table.addItem("fm" + fkWeaponNr, "");
      table.addItem("fg" + fkWeaponNr, "");
      table.addItem("f1" + fkWeaponNr, "");
    }
    weightF = weight / 40.0f;
    table.addItem("wgsu", "" + weightF);
    weightF = fkWeight / 40.0f;
    table.addItem("fgsu", "" + weightF);
    return overallWeight;
  }

  private static int printArmours(Hero character, LookupTable table, int overallWeight) {
    int weight = 0;
    int armourNr = 1;
    for (String name : character.getArmours()) {
      Armour armour = Armours.getInstance().getArmour(name);
      if (armour == null) continue;
      table.addItem("rn" + armourNr, armour.getName());
      table.addItem("Rn", armour.getName(), true);
      table.addItem("rs" + armourNr, "" + armour.getRS());
      table.addItem("Rs", "" + armour.getRS(), true);
      table.addItem("rb" + armourNr, "" + armour.getBE());
      table.addItem("Rb", "" + armour.getBE(), true);
      table.addItem("rg" + armourNr, "" + armour.getWeight());
      table.addItem("Rg", "" + armour.getWeight(), true);
      weight += armour.getWeight();
      overallWeight += armour.getWeight();
      ++armourNr;
    }
    float weightF = weight / 40.0f;
    for (; armourNr <= 5; ++armourNr) {
      table.addItem("rn" + armourNr, "");
      table.addItem("rs" + armourNr, "");
      table.addItem("rb" + armourNr, "");
      table.addItem("rg" + armourNr, "");
    }
    table.addItem("be-", "" + character.getBEModification());
    table.addItem("rsu", "" + character.getRS());
    table.addItem("bsu", "" + character.getBE());
    table.addItem("rgsu", "" + weightF);
    return overallWeight;
  }

  private static void printTalents(Hero character, LookupTable table) {
    for (String category : Talents.getInstance().getKnownCategories()) {
      if (category.equals("Favoriten")) continue;
      if (category.startsWith("Berufe")) continue;
      if (category.equals("Zauber")) continue;
      char appendix = 'a';
      int catNr = getCategoryNumber(category);
      for (Talent talent : Talents.getInstance().getTalentsInCategory(category)) {
        table.addItem("" + catNr + appendix, ""
            + character.getDefaultTalentValue(talent.getName()));
        appendix++;
      }
    }

    printSpecialTalents(character, table);

  }

  private static void printSpells(Hero character, LookupTable table) {
    ArrayList<Spell> spells = new ArrayList<Spell>();

    for (Talent talent : Talents.getInstance().getTalentsInCategory("Zauber")) {
      if (character.hasTalent(talent.getName())) {
        dsa.model.talents.Spell spell = (dsa.model.talents.Spell) talent;
        spells.add(spell);
      }
    }
    // Vorsortierung
    Collections.sort(spells, new OriginComparator(1));
    Collections.sort(spells, new NameComparator(1));
    Collections.sort(spells, new CategoryComparator(1));

    dsa.gui.frames.SubFrame frame = (dsa.gui.frames.FrameManagement
        .getInstance().getFrame("Zauber"));
    if (frame != null && !(frame instanceof dsa.gui.frames.SpellFrame)) {
      assert(false);
      return;
    }
    if (frame != null && (frame instanceof dsa.gui.frames.SpellFrame)) {
      dsa.gui.frames.SpellFrame spellFrame = (dsa.gui.frames.SpellFrame) frame;
      spellFrame.saveCurrentSortingState();
    }

    SpellSorter sorter = new SpellSorter(character);
    sorter.restoreSorting(spells);

    for (Spell sp : spells) {
      table.addItem("zn", sp.getName() + " (" + sp.getFirstProperty() + "/"
          + sp.getSecondProperty() + "/" + sp.getThirdProperty() + ")", true);
      table.addItem("zw", "" + character.getDefaultTalentValue(sp.getName()),
          true);
      table.addItem("zg", sp.getCategory(), true);
      table.addItem("zu", sp.getOrigin(), true);
    }

    if (!character.hasEnergy(Energy.AE)) {
      // remove the codes
      table.addItem("zn", "");
      table.addItem("zw", "");
      table.addItem("zg", "");
      table.addItem("zu", "");
    }
  }

  private static void printSpecialTalents(Hero character, LookupTable table) {
    table.addItem("mf", "" + character.getDefaultTalentValue("Jagen (Falle)"));
    table.addItem("mp", "" + character.getDefaultTalentValue("Jagen (Pirsch)"));
    char appendix = 'a';
    for (Talent talent : Talents.getInstance().getTalentsInCategory(
        "Berufe / Sonstiges")) {
      if (talent.getName().startsWith("Jagen (")) continue;
      if (character.hasTalent(talent.getName())) {
        table.addItem("bn" + appendix, "" + talent.getName());
        table.addItem("8" + appendix, ""
            + character.getDefaultTalentValue(talent.getName()));
        ++appendix;
      }
    }
    while (appendix != 'l') {
      table.addItem("bn" + appendix, "");
      table.addItem("8" + appendix, "");
      ++appendix;
    }
  }

  private static void printDerivedValues(Hero character, LookupTable table) {
    table.addItem("mb", "" + character.getMRBonus());
    table.addItem("mr", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.MR));
    table.addItem("atb", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.AT));
    table.addItem("pab", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.PA));
    table.addItem("fkb", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.FK));
    table.addItem("auw", ""
        + character.getDefaultDerivedValue(Hero.DerivedValue.AW));
  }

  private static void printBasicAttributes(Hero character, LookupTable table) {
    table.addItem("name", character.getName());
    table.addItem("typ", character.getType());
    table.addItem("ort", character.getBirthPlace());
    table.addItem("gro", character.getHeight());
    table.addItem("gew", character.getWeight());
    table.addItem("haar", character.getHairColor());
    table.addItem("sex", character.getSex());
    table.addItem("alt", "" + character.getAge());
    table.addItem("geb", character.getBirthday());
    table.addItem("gott", character.getGod());
    table.addItem("augen", character.getEyeColor());
    table.addItem("skin", character.getSkinColor());
    table.addItem("stand", character.getStand());
    table.addItem("gs", "10");

    table.addItem("ap", "" + character.getAP());
    table.addItem("ruf", character.getRuf());
    int step = character.getStep();
    table.addItem("stufe", "" + step);
    table.addItem("nst", "" + (step * (step + 1) * 50));
    table.addItem("APminus", ""
        + ((step * (step + 1) * 50) - character.getAP()));
    table.addItem("titel", character.getTitle());
    table.addItem("haut", character.getSkin());
  }

  private static void printEnergies(Hero character, LookupTable table) {
    table.addItem("le", "" + character.getDefaultEnergy(Energy.LE));
    table.addItem("ae", "" + character.getDefaultEnergy(Energy.AE));
    table.addItem("ke", "" + character.getDefaultEnergy(Energy.KE));
    table.addItem("ko", "" + character.getDefaultEnergy(Energy.KO));
    table.addItem("aus", "" + character.getDefaultEnergy(Energy.AU));
  }

  private static void printProperties(Hero character, LookupTable table) {
    for (int i = 0; i < 7; ++i) {
      table.addItem("p" + (i + 1), ""
          + character.getDefaultProperty(Property.values()[i]));
    }
    table.addItem("n1", "" + character.getDefaultProperty(Property.AG));
    table.addItem("n2", "" + character.getDefaultProperty(Property.HA));
    table.addItem("n3", "" + character.getDefaultProperty(Property.RA));
    table.addItem("n4", "" + character.getDefaultProperty(Property.TA));
    for (int i = 4; i < 7; ++i) {
      table.addItem("n" + (i + 1), ""
          + character.getDefaultProperty(Property.values()[i + 7]));
    }
  }

}

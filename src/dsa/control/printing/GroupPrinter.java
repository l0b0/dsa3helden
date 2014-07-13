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
package dsa.control.printing;

import java.util.ArrayList;
import java.util.Collections;

import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Printable;
import dsa.model.characters.Property;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
import dsa.model.data.Currencies;
import dsa.model.data.Opponent;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.model.talents.Talent;
import dsa.util.LookupTable;
import dsa.util.Strings;

public class GroupPrinter extends AbstractPrinter {

  private static class PrinterHolder {
    private static final GroupPrinter PRINTER = new GroupPrinter();
  }

  public static GroupPrinter getInstance() {
    return PrinterHolder.PRINTER;
  }

  protected final void fillTable(Printable p, LookupTable table) {
    
    assert(p instanceof Group);
    
    if (p instanceof Group) {
      Group group = (Group) p;
      
      table.addItem("gname", group.getName());
      
      char heroAppendix = 'A';
      for (Hero hero : group.getAllCharacters()) {
        printHero(hero, table, "" + heroAppendix);
        ++heroAppendix;
      }
      
      while (heroAppendix <= 'Z') {
        printHero(null, table, "" + heroAppendix);
        ++heroAppendix;
      }
      
      printOpponents(group, table);
    }    
  }
  
  private static void printOpponents(Group group, LookupTable table) {
    char app = 'A';
    for (String name : group.getOpponentNames()) {
      Opponent o = group.getOpponent(name);
      for (int i = 0; i < o.getWeapons().size(); ++i) {
        table.addItem("Geg" + app, o.getName());
        table.addItem("Gat" + app, o.getWeaponAT(i).getValue());
        String opa = o.getPA(i).hasValue() ? "" + o.getWeaponPA(i).getValue() : "-";
        table.addItem("Gpa" + app, opa);
        table.addItem("Gtp" + app, o.getWeaponTP(i).toString());
        table.addItem("Grs" + app, o.getRS());
        table.addItem("Gmr" + app, o.getMR());
        table.addItem("Gle" + app, o.getLE());
        ++app;
        if (app == 'Z') break;
      }
      if (app == 'Z') break;
    }
    while (app < 'Z') {
      table.addItem("Geg" + app, "");
      table.addItem("Gat" + app, "");
      table.addItem("Gpa" + app, "");
      table.addItem("Gtp" + app, "");
      table.addItem("Grs" + app, "");
      table.addItem("Gmr" + app, "");
      table.addItem("Gle" + app, "");
      ++app;      
    }
  }
  
  private static void printHero(Hero hero, LookupTable table, String app) {
    printBasicAttributes(hero, table, app);
    printProperties(hero, table, app);
    printEnergies(hero, table, app);
    printDerivedValues(hero, table, app);
    printTalents(hero, table, app);
    printMagicAttributes(hero, table, app);
    printEspeciallyBadProperties(hero, table, app);
    printEspeciallyGoodTalents(hero, table, app);
    printEspeciallyGoodSpells(hero, table, app);
    printRituals(hero, table, app);
    printWeight(hero, table, app);
    printName(hero, table, app);
    printRSAndBE(hero, table, app);
  }

  private static void printRSAndBE(Hero hero, LookupTable table, String app) {
    if (hero != null) {
      table.addItem("rs" + app, hero.getRS());
      table.addItem("be" + app, hero.getBE());
    }
    else {
      table.addItem("rs" + app, "");
      table.addItem("be" + app, "");
    }
  }

  private static void printName(Hero hero, LookupTable table, String app) {
    if (hero != null) {
      table.addItem("sname" + app, Strings.firstWord(hero.getName()));
    }
    else {
      table.addItem("sname" + app, "");
    }
  }

  private static void printWeight(Hero hero, LookupTable table, String app) {
    if (hero == null) {
      table.addItem("Gew" + app, "");
      return;
    }
    float weight = 0.0f;
    try {
      weight += Integer.parseInt(hero.getWeight()) * 40.0f;
    }
    catch (NumberFormatException e) {
      // ignore
    }
    for (String thing :  hero.getThings()) {
      Thing t = Things.getInstance().getThing(thing);
      if (t == null) continue;
      weight += t.getWeight() * hero.getThingCount(thing);
    }
    for (String weapon : hero.getWeapons()) {
      Weapon w = Weapons.getInstance().getWeapon(weapon);
      if (w == null) continue;
      weight += w.getWeight() * hero.getWeaponCount(weapon);
      if (w.isProjectileWeapon() && w.getProjectileWeight().hasValue()) {
        weight += hero.getNrOfProjectiles(weapon) * w.getProjectileWeight().getValue();
      }
    }
    for (String armour : hero.getArmours()) {
      Armour a = Armours.getInstance().getArmour(armour);
      if (a == null) continue;
      weight += a.getWeight();
    }
    for (String shield : hero.getShields()) {
      Shield s = Shields.getInstance().getShield(shield);
      if (s == null) continue;
      weight += s.getWeight();
    }
    long moneyWeightTenthOfSkrupel = 0;
    for (int i = 0; i < hero.getNrOfCurrencies(false); ++i) {
      int currency = hero.getCurrency(i, false);
      moneyWeightTenthOfSkrupel += Currencies.getInstance().getWeight(currency) * hero.getMoney(i, false);
    }
    weight += moneyWeightTenthOfSkrupel / 250.0f;
    weight /= 40.0f; // in Stein
    table.addItem("Gew" + app, weight);
  }

  private static class PropertyValue implements Comparable<PropertyValue> {
    
    public PropertyValue(Property p, int v) {
      property = p;
      value = v;
    }
    
    private Property property;
    private int value;
    
    public Property getProperty() { return property; }
    public int getValue() { return value; }

    public int compareTo(PropertyValue o) {
      if (value < o.value) return 1;
      if (value > o.value) return -1;
      return 0;
    }
    
  }
  
  private static void printEspeciallyBadProperties(Hero hero, LookupTable table, String app) {
    if (hero != null) {
      ArrayList<PropertyValue> values = new ArrayList<PropertyValue>();
      for (Property p : Property.values()) {
        if (p.ordinal() < 7) continue;
        values.add(new PropertyValue(p, hero.getDefaultProperty(p)));
      }
      assert(values.size() >= 7);
      Collections.sort(values);
      for (int i = 0; i < 7; ++i) {
        table.addItem("neg" + (i + 1) + app, values.get(i).getProperty().toString() + ": " + values.get(i).getValue());
      }
    }
    else {
      for (int i = 0; i < 7; ++i) {
        table.addItem("neg" + (i + 1) + app, "");
      }
    }
  }

 private static class TalentValue implements Comparable<TalentValue> {
    
    public TalentValue(String t, int v) {
      talent = t;
      value = v;
    }
    
    private String talent;
    private int value;
    
    public String getTalent() { return talent; }
    public int getValue() { return value; }

    public int compareTo(TalentValue o) {
      if (value < o.value) return 1;
      if (value > o.value) return -1;
      return 0;
    }
    
  }
  
  private static void printEspeciallyGoodTalents(Hero hero, LookupTable table, String app) {
    if (hero != null) {
      ArrayList<TalentValue> values = new ArrayList<TalentValue>();
      for (String category : Talents.getInstance().getKnownCategories()) {
        if (category.startsWith("Zauber") || category.startsWith("Sprachen") || category.startsWith("Favoriten")) continue;
        for (Talent t : Talents.getInstance().getTalentsInCategory(category)) {
          // additional safety
          if (t.isLanguage() || t.isSpell()) continue;
          values.add(new TalentValue(t.getName(), hero.getDefaultTalentValue(t.getName())));
        }
      }
      Collections.sort(values);
      String all = "";
      for (int i = 0; i < 8; ++i) {
        all += values.get(i).getTalent() + " " + values.get(i).getValue() + ", ";
      }
      if (all.length() > 0) {
        all = all.substring(0, all.length() - 2);
      }
      table.addItem("tal" + app, all);
    }
    else {
      table.addItem("tal" + app, "");
    }
  }

  private static void printEspeciallyGoodSpells(Hero hero, LookupTable table, String app) {
    if (hero != null && hero.hasEnergy(Energy.AE)) {
      ArrayList<TalentValue> values = new ArrayList<TalentValue>();
      for (String category : Talents.getInstance().getKnownCategories()) {
        if (category.startsWith("Favoriten")) continue;
        for (Talent t : Talents.getInstance().getTalentsInCategory(category)) {
          if (!t.isSpell()) continue;
          if (!hero.hasTalent(t.getName())) continue;
          values.add(new TalentValue(t.getName(), hero.getDefaultTalentValue(t.getName())));
        }
      }
      Collections.sort(values);
      String all = "";
      for (int i = 0; i < 8; ++i) {
        if (i >= values.size()) break;
        all += values.get(i).getTalent() + " " + values.get(i).getValue() + ", ";
      }
      if (all.length() > 0) {
        all = all.substring(0, all.length() - 2);
      }
      table.addItem("zau" + app, all);
    }
    else if (hero != null) {
      table.addItem("zau" + app, "-");
    }
    else table.addItem("zau" + app, "");
  }

  private static void printRituals(Hero hero, LookupTable table, String app) {
    String all = "";
    if (hero != null) {
      for (String r : hero.getRituals()) {
        all += r + ", ";
      }
      if (hero.getRituals().size() == 0) {
        all = "-";
      }
      else {
        all = all.substring(0, all.length() - 2);
      }
    }
    table.addItem("son" + app, all);
  }

}

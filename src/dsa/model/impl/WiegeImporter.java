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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import dsa.model.DataFactory;
import dsa.model.Date;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
import dsa.model.data.Talents;
import dsa.model.data.Things;
import dsa.model.data.Weapons;
import dsa.util.Optional;
import dsa.util.Strings;

import static dsa.model.characters.Energy.*;
import static dsa.model.characters.Property.*;

/**
 * 
 */
public class WiegeImporter {
  
  static int lineNr;

  public static HeroImpl importFromWiege(File file) throws IOException {
    HeroImpl hero = new HeroImpl();
    BufferedReader in = new BufferedReader(new FileReader(file));
    int /* temprs = 0, tempbe = 0, */tempmu = hero.getDefaultProperty(MU), tempge = hero
        .getDefaultProperty(GE);
    int tempkl = hero.getDefaultProperty(KL), tempch = hero
        .getDefaultProperty(CH), tempff = hero.getDefaultProperty(FF);
    int tempin = hero.getDefaultProperty(IN), tempkk = hero
        .getDefaultProperty(KK), temple = hero.getDefaultEnergy(LE);
    int tempag = hero.getDefaultProperty(AG), tempha = hero
        .getDefaultProperty(HA), tempra = hero.getDefaultProperty(RA);
    int tempta = hero.getDefaultProperty(TA), tempng = hero
        .getDefaultProperty(NG), tempgg = hero.getDefaultProperty(GG);
    int tempjz = hero.getDefaultProperty(JZ);
    lineNr = 0;
    boolean temphasae = hero.hasEnergy(AE);
    ArrayList<String> tempWeapons = new ArrayList<String>(5);
    // tempWeapons.add(0, Weapon.FIST.clone());
    ArrayList<String> tempArmours = new ArrayList<String>();
    Map<String, Integer> temptalents = new java.util.HashMap<String, Integer>();
    String line = in.readLine(); // Typ
    lineNr++;
    String type = line;
    if (type.equals("Magier (keine Akademie)")) {
      type = "Magier";
    }
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": Eigenschaften fehlen!");
    StringTokenizer tokenizer = new StringTokenizer(line);
    if (tokenizer.countTokens() != 7) {
      throw new IOException("Zeile " + lineNr + ": Falsche Zahl Eigenschaften!");
    }
    try {
      tempmu = Integer.parseInt(tokenizer.nextToken());
      tempkl = Integer.parseInt(tokenizer.nextToken());
      tempin = Integer.parseInt(tokenizer.nextToken());
      tempch = Integer.parseInt(tokenizer.nextToken());
      tempff = Integer.parseInt(tokenizer.nextToken());
      tempge = Integer.parseInt(tokenizer.nextToken());
      tempkk = Integer.parseInt(tokenizer.nextToken());
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": Eigenschaft keine Zahl!");
    }
    if (tempmu < 7 || tempmu > 20 || tempin < 7 || tempin > 20 || tempge < 7
        || tempge > 20 || tempkk < 7 || tempkk > 20 || tempkl < 7
        || tempkl > 20 || tempch < 7 || tempch > 20 || tempff < 7
        || tempff > 20) {
      throw new IOException("Zeile " + lineNr
          + ": Eigenschaft zu klein oder zu gross!");
    }
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": Eigenschaften fehlen!");
    tokenizer = new StringTokenizer(line);
    if (tokenizer.countTokens() != 7) {
      throw new IOException("Zeile " + lineNr + ": Falsche Zahl Eigenschaften!");
    }
    try {
      tempag = Integer.parseInt(tokenizer.nextToken());
      tempha = Integer.parseInt(tokenizer.nextToken());
      tempra = Integer.parseInt(tokenizer.nextToken());
      tempta = Integer.parseInt(tokenizer.nextToken());
      tempng = Integer.parseInt(tokenizer.nextToken());
      tempgg = Integer.parseInt(tokenizer.nextToken());
      tempjz = Integer.parseInt(tokenizer.nextToken());
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": Eigenschaft keine Zahl!");
    }
    if (tempag < 0 || tempag > 20 || tempha < 0 || tempha > 20 || tempra < 0
        || tempra > 20 || tempta < 0 || tempta > 20 || tempng < 0
        || tempng > 20 || tempgg < 0 || tempgg > 20 || tempjz < 0
        || tempjz > 20) {
      throw new IOException("Zeile " + lineNr
          + ": Eigenschaft zu klein oder zu gross!");
    }
    readTalents(in, temptalents);
    readRituals(in);
    temple = readNumber(in, "LE");
    if (temple < 1)
      throw new IOException("Zeile " + lineNr + ": LE zu klein!");
    int tempae = readNumber(in, "AE");
    temphasae = (tempae > 0);
    int tempke = readNumber(in, "KE");
    int tempZFPlus = readNumber(in, "Zauber-Steigerungszahl");
    int tempTalentPlus = readNumber(in, "Talent-Steigerungszahl");
    int tempZFTWMove = readNumber(in, "ZF-TaW-Verschiebung");
    int tempbemod = readNumber(in, "BE-Minus");
    in.readLine(); // Geschwindigkeit
    lineNr++;
    int tempMRBonus = readNumber(in, "MR-Bonus");
    int tempLEPlus = readIncrease(in, "LE");
    int tempAEPlus = readIncrease(in, "AE");
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": AE-Separat-Flag fehlt!");
    boolean tempHasExtraAEIncrease = (line.equals("TRUE"));

    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": Meditations-Flag fehlt!");
    boolean tempHasGreatMeditation = (line.equals("TRUE"));

    Map<String, Integer> tempSpellIncreases = new java.util.HashMap<String, Integer>();
    readSpells(in, temptalents, tempSpellIncreases);
    int nrOfRituals = readNumber(in, "Anzahl Rituale");
    for (int i = 0; i < nrOfRituals; i++) {
      in.readLine();
      lineNr++;
    }
    line = in.readLine();
    lineNr++; // ? Immer FALSE?
    line = in.readLine();
    lineNr++; // ? Immer 0?
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": Name fehlt!");
    String tempname = line;
    Armours armours = Armours.getInstance();
    Weapons weapons = Weapons.getInstance();
    int[] attackValues = new int[20];
    int[] paradeValues = new int[20];
    line = in.readLine();
    lineNr++; // Geburtsregion
    String tempHairColor = in.readLine();
    lineNr++;
    String tempStand = in.readLine();
    lineNr++;
    String tempHeight = in.readLine();
    lineNr++;
    String tempWeight = in.readLine();
    lineNr++;
    String dateString = in.readLine();
    lineNr++;
    Date date = parseDate(dateString);
    line = in.readLine();
    lineNr++;
    int tempAge = 0;
    try {
      tempAge = Integer.parseInt(line);
      if (tempAge < 0) throw new NumberFormatException("");
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": Alter hat falsches Format!");
    }
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Anzahl Sonderfertigkeiten fehlt!");
    int skip = 0;
    try {
      skip = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr
          + ": Anz. Sonderfertigkeiten keine Zahl!");
    }
    ArrayList<String> tempRituals = new ArrayList<String>();
    for (int i = 0; i < skip; i++) {
      line = in.readLine();
      lineNr++;
      tempRituals.add(line);
    }
    line = in.readLine();
    lineNr++;
    String tempSex = ("TRUE".equals(line) ? "m" : "w");
    in.readLine();
    lineNr++; // ? -- immer 3 Nullen: evtl. restliche
    // Steigerungsversuche?
    String tempGod = in.readLine();
    lineNr++;
    String tempEyeColor = in.readLine();
    lineNr++;
    String tempTitle = in.readLine();
    lineNr++;
    String tempBirthplace = in.readLine();
    lineNr++;
    line = in.readLine();
    lineNr++;
    int tempap = 0;
    try {
      tempap = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": AP sind keine Zahl!");
    }
    for (int i = 0; i < 2; i++) {
      in.readLine();
      lineNr++; // Stufe
    }
    readATPA(in, attackValues, paradeValues);
    readWeaponsAndArmours(in, tempWeapons, tempArmours, armours, weapons);
    ArrayList<String> tempFTs = new ArrayList<String>();
    readFightTalents(in, tempFTs);
    line = in.readLine();
    lineNr++; // Dokumentvorlage
    String tempDocumentTemplate = ""; // line;
    line = in.readLine();
    lineNr++; // ? Scheint immer 5 5 zu sein
    String tempnotes = readNotes(in);
    String tempNativeTongue = readLanguages(in, temptalents);
    ArrayList<String> tempthings = new ArrayList<String>();
    readThings(in, tempthings);

    hero.setType(type);
    hero.setAP(tempap);
    hero.setName(tempname);
    hero.setCompleteEnergy(LE, temple);
    hero.setCompleteEnergy(AE, tempae);
    hero.setCompleteProperty(MU, tempmu);
    hero.setCompleteProperty(KL, tempkl);
    hero.setCompleteProperty(IN, tempin);
    hero.setCompleteProperty(CH, tempch);
    hero.setCompleteProperty(FF, tempff);
    hero.setCompleteProperty(GE, tempge);
    hero.setCompleteProperty(KK, tempkk);
    hero.setCompleteProperty(AG, tempag);
    hero.setCompleteProperty(HA, tempha);
    hero.setCompleteProperty(RA, tempra);
    hero.setCompleteProperty(TA, tempta);
    hero.setCompleteProperty(NG, tempng);
    hero.setCompleteProperty(GG, tempgg);
    hero.setCompleteProperty(JZ, tempjz);
    hero.setHasEnergy(AE, temphasae);
    hero.setCompleteEnergy(AU, temple + tempkk);
    hero.setHasEnergy(KE, tempke > 0);
    hero.setCompleteEnergy(KE, tempke);
    hero.calcKO();
    // this.weapons = tempWeapons; rs = temprs; be = tempbe;
    Iterator<Entry<String, Integer>> it = temptalents.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, Integer> entry = it.next();
      String talentName = entry.getKey();
      hero.addTalent(talentName);
      hero.setDefaultTalentValue(talentName, entry.getValue()
          .intValue());
      hero.setCurrentTalentValue(talentName, entry.getValue()
          .intValue());
      if (tempSpellIncreases.containsKey(talentName)) {
        hero.setTalentIncreaseTries(talentName, entry.getValue()
          .intValue());
      }
    }
    hero.setGod(tempGod);
    hero.setEyeColor(tempEyeColor);
    hero.setBirthday(date);
    hero.setBirthPlace(tempBirthplace);
    hero.setSex(tempSex);
    hero.setHairColor(tempHairColor);
    hero.setHeight(tempHeight);
    hero.setWeight(tempWeight);
    hero.setStand(tempStand);
    hero.setAge(tempAge);
    hero.setTalentIncreasesPerStep(tempTalentPlus);
    hero.spellIncreasesPerStep = tempZFPlus;
    hero.spellToTalentMoves = tempZFTWMove;
    hero.mrBonus = tempMRBonus;
    hero.fixedLEIncrease = tempLEPlus;
    hero.fixedAEIncrease = tempAEPlus;
    hero.mHasExtraAEIncrease = tempHasExtraAEIncrease;
    hero.mHasGreatMeditation = tempHasGreatMeditation;
    hero.setTitle(tempTitle);
    hero.nativeTongue = tempNativeTongue;
    hero.beModification = tempbemod;
    for (String armour : tempArmours)
      hero.addArmour(armour);
    for (String weapon : tempWeapons)
      hero.addWeapon(weapon);
    for (String thing : tempthings)
      hero.addThing(thing);
    hero.notes = tempnotes;
    hero.setPrintingTemplateFile(tempDocumentTemplate);
    int ftNumber = 0;
    int baseAT = hero.getCurrentDerivedValue(Hero.DerivedValue.AT);
    for (dsa.model.talents.Talent talent : Talents.getInstance()
        .getTalentsInCategory("Kampftalente")) {
      if (ftNumber < attackValues.length) {
        hero.atParts.put(talent.getName(), attackValues[ftNumber] - baseAT);
        ++ftNumber;
      }
    }
    hero.fightingTalentsInDocument = tempFTs;
    hero.title = "";
    hero.skin = "";
    hero.rituals = tempRituals;
    return hero;
  }

  private static int readIncrease(BufferedReader in, String name) throws IOException {
    String line;
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr + ": " + name + "-Steigerung fehlt!");
    if (!line.startsWith("1W6")) {
      throw new IOException("Zeile " + lineNr
          + ": " + name + "-Steigerung hat falsches Format!");
    }
    int tempLEPlus = 0;
    if (line.length() > 3) {
      try {
        int start = 3;
        if (line.charAt(start) == '+') start = 4;
        tempLEPlus = Integer.parseInt(line.substring(4));
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": " + name + "-Steigerung hat falsches Format!");
      }
    }
    return tempLEPlus;
  }

  private static int readNumber(BufferedReader in, String name) throws IOException {
    String line;
    line = in.readLine();
    lineNr++;
    if (line == null) throw new IOException("Zeile " + lineNr + ": " + name + " fehlt!");
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": " + name + " keine Zahl!");
    }
  }

  private static String readLanguages(BufferedReader in, Map<String, Integer> temptalents) throws IOException {
    String line;
    String tempNativeTongue = "";
    line = in.readLine();
    lineNr++; // Anz. Sprachen
    if (line == null)
      throw new IOException("Zeile " + lineNr
          + ": Ende der Datei, aber Anzahl Sprachen erwartet!");
    int nrOfLanguages = 0;
    try {
      nrOfLanguages = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": # Sprachen ist keine Zahl!");
    }
    for (int i = 0; i < nrOfLanguages; ++i) {
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Zeile " + lineNr
            + ": Ende der Datei, aber Sprache erwartet!");
      String language = line;
      if (language.charAt(0) == '*') language = language.substring(1);
      // mapping of differing names
      if (language.equals("Norbardisch (Alaanii)"))
        language = "Alaanii";
      else if (language.equals("Tulamidisch (Novadisch)"))
        language = "Tulamidya";
      else if (language.equals("Zelemja (Selemisch)"))
        language = "Zelemja";
      else if (language.equals("Altelfisch")) language = "Asdharia";
      if (Talents.getInstance().getTalent(language) == null) {
        throw new IOException("Zeile " + lineNr + ": Unbekannte Sprache!");
      }
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Ende der Datei, aber Sprachenwert erwartet!");
      int value = 0;
      try {
        value = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": Sprachen-Wert ist keine Zahl!");
      }
      if (value == 0) {
        value = ((Language) Talents.getInstance().getTalent(language)).getMax(); // Muttersprache
        tempNativeTongue = language;
      }
      temptalents.put(language, value);
    }
    return tempNativeTongue;
  }

  private static String readNotes(BufferedReader in) throws IOException {
    String line = in.readLine();
    lineNr++; // Anzahl Zusatzinfozeilen
    if (line == null)
      throw new IOException("Zeile " + lineNr
          + ": Ende der Datei, aber Anz. Zusatzinfozeilen erwartet.");
    String tempnotes = "";
    int nrOfNoteLines = 0;
    try {
      nrOfNoteLines = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr
          + ": # Zusatzinfozeilen ist keine Zahl!");
    }
    for (int i = 0; i < nrOfNoteLines; ++i) {
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Zeile " + lineNr
            + ": Zusatzinformation erwartet!");
      if (tempnotes.length() > 0)
        tempnotes += System.getProperty("line.separator");
      tempnotes += line;
    }
    return tempnotes;
  }

  private static void readFightTalents(BufferedReader in, ArrayList<String> tempFTs) throws IOException {
    String line;
    line = in.readLine();
    lineNr++; // Kampffertigkeiten in 1. Seite
    StringTokenizer ftTokenizer = new StringTokenizer(line);
    for (dsa.model.talents.Talent t : Talents.getInstance()
        .getTalentsInCategory("Kampftalente")) {
      if (!ftTokenizer.hasMoreTokens()) break;
      if (ftTokenizer.nextToken().equals("1")) {
        tempFTs.add(t.getName());
      }
    }
  }

  private static void readThings(BufferedReader in, ArrayList<String> tempthings) throws IOException {
    String line;
    line = in.readLine();
    lineNr++; // Anzahl Gegenstaende
    if (line == null) throw new IOException("Anzahl Gegenstände fehlt!");
    int nrOfThings = 0;
    try {
      nrOfThings = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr
          + ": Anzahl Gegenstände ist keine Zahl!");
    }
    Things things = Things.getInstance();
    for (int i = 0; i < nrOfThings; ++i) {
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Anzahl Sonderfertigkeiten fehlt!");
      String name = line;
      line = in.readLine();
      lineNr++;
      int weight = 0;
      if (line == null)
        throw new IOException("Gewicht des Gegenstands fehlt!");
      try {
        weight = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": Gewicht des Gegenstands ist keine Zahl!");
      }
      if (things.getThing(name) == null) {
        things.addThing(new dsa.model.data.Thing(name,
            dsa.util.Optional.NULL_INT, dsa.model.data.Thing.Currency.H, weight,
            "-", true));
      }
      tempthings.add(name);
    }
  }

  private static void readWeaponsAndArmours(BufferedReader in, ArrayList<String> tempWeapons, ArrayList<String> tempArmours, Armours armours, Weapons weapons) throws IOException {
    String line;
    line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Zeile " + lineNr
          + ": Ende der Datei, aber 2 Werte erwartet.");
    // Anzahl Waffen und Anzahl Ruestungen
    StringTokenizer watokens = new StringTokenizer(line);
    if (watokens.countTokens() != 2) {
      throw new IOException("Zeile " + lineNr + ": 2 Werte erwartet...");
    }
    int nrOfWeapons = 0;
    try {
      nrOfWeapons = Integer.parseInt(watokens.nextToken());
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": # Waffen keine Zahl!");
    }
    int nrOfArmours = 0;
    try {
      nrOfArmours = Integer.parseInt(watokens.nextToken());
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": # Rüstungen keine Zahl!");
    }
    readWeapons(in, tempWeapons, weapons, nrOfWeapons);
    readArmours(in, tempArmours, armours, nrOfArmours);
  }

  private static void readArmours(BufferedReader in, ArrayList<String> tempArmours, Armours armours, int nrOfArmours) throws IOException {
    String line;
    for (int i = 0; i < nrOfArmours; ++i) {
      line = in.readLine();
      lineNr++;
      if (line == null) throw new IOException("EOF statt Rüstung!");
      String name = line;
      line = in.readLine();
      lineNr++; // rs
      int rs = 1, be = 1;
      if (line == null) throw new IOException("EOF statt RS!");
      try {
        rs = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": RS ist keine Zahl!");
      }
      if ((rs < 0) || (rs > 20)) {
        throw new IOException("Zeile " + lineNr + ": RS falsch!");
      }
      line = in.readLine();
      lineNr++;
      if (line == null) throw new IOException("EOF statt BE!");
      try {
        be = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": BE ist keine Zahl!");
      }
      if ((be < 0) || (be > 20)) {
        throw new IOException("Zeile " + lineNr + ": BE falsch!");
      }
      line = in.readLine();
      lineNr++; // Gewicht
      if (line == null) throw new IOException("EOF statt Gewicht!");
      int weight = 0;
      try {
        weight = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Gewicht ist keine Zahl!");
      }
      if (armours.getArmour(name) == null) {
        Armour armour = new Armour(name, rs, be, weight, 0);
        armours.addArmour(armour);
      }
      tempArmours.add(name);
    }
  }

  private static void readWeapons(BufferedReader in, ArrayList<String> tempWeapons, Weapons weapons, int nrOfWeapons) throws IOException {
    String line;
    for (int i = 0; i < nrOfWeapons; ++i) {
      line = in.readLine();
      lineNr++;
      int category;
      try {
        category = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException(lineNr + " Waffen-Kategorie keine Zahl.");
      }
      if ((category < 0) || (category > 19)) {
        throw new IOException(lineNr + " Waffen-Kategorie falsch.");
      }
      line = in.readLine();
      lineNr++;
      if (line == null) throw new IOException("EOF statt Waffenname");
      String name = line;
      line = in.readLine();
      lineNr++;
      if (line == null) throw new IOException("EOF statt Waffenschaden");
      int w6d = 1, constd = 0;
      if (line.trim().equals("speziell")) {
        w6d = 0;
        constd = 0;
      }
      else {
        StringTokenizer wt = new StringTokenizer(line, "W+");
        if (wt.countTokens() > 2) {
          throw new IOException(lineNr + " Schaden d. Waffe falsch!");
        }
        try {
          w6d = Integer.parseInt(wt.nextToken());
          if (wt.hasMoreTokens()) {
            constd = Integer.parseInt(wt.nextToken());
          }
        }
        catch (NumberFormatException e) {
          throw new IOException(lineNr + " Schaden d. Waffe keine Zahl!");
        }
      }
      line = in.readLine();
      lineNr++;
      int bf = 0;
      if (line == null) throw new IOException("EOF statt Bruchfaktor!");
      if (line.trim().equals("-"))
        bf = 0;
      else
        try {
          bf = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException(lineNr + " Bruchfaktor falsch!");
        }
      line = in.readLine();
      lineNr++;
      int weight = 0;
      try {
        weight = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException(lineNr + " Gewicht falsch!");
      }
      line = in.readLine();
      lineNr++;
      if (line == null) throw new IOException("EOF statt KK-Zuschlag!");
      Optional<Integer> kkzuschlag = new Optional<Integer>();
      if (!((line.trim().equals("-")) || (line.charAt(0) == '('))) try {
        kkzuschlag = new Optional<Integer>(Integer.parseInt(line));
      }
      catch (NumberFormatException e) {
        throw new IOException(lineNr + " KK-Zuschlag falsch!");
      }
      line = in.readLine();
      lineNr++; // WV oder Reichweite
      if (weapons.getWeapon(name) == null) {
        weapons.addWeapon(new dsa.model.data.Weapon(w6d, constd, category,
            name, bf, kkzuschlag, weight, true, false, false, 
            dsa.util.Optional.NULL_INT, new dsa.model.data.Weapon.WV(4, 4)));
      }
      tempWeapons.add(name);
    }
  }

  private static void readATPA(BufferedReader in, int[] attackValues, int[] paradeValues) throws IOException {
    String line;
    line = in.readLine();
    lineNr++;
    StringTokenizer attackTokens = new StringTokenizer(line);
    if (attackTokens.countTokens() != 20) {
      throw new IOException("Zeile " + lineNr + ": Falsche Zahl Attackewerte!");
    }
    for (int i = 0; i < 20; i++) {
      String token = attackTokens.nextToken();
      try {
        attackValues[i] = Integer.parseInt(token);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Attackewert keine Zahl!");
      }
    }
    line = in.readLine();
    lineNr++;
    StringTokenizer paradeTokens = new StringTokenizer(line);
    if (paradeTokens.countTokens() != 20) {
      throw new IOException("Zeile " + lineNr + ": Falsche Zahl Paradewerte!");
    }
    for (int i = 0; i < 20; i++) {
      String token = paradeTokens.nextToken();
      try {
        paradeValues[i] = Integer.parseInt(token);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Paradewert keine Zahl!");
      }
    }
  }

  private static Date parseDate(String dateString) throws IOException {
    StringTokenizer tokenizer;
    if (dateString == null)
      throw new IOException("Zeile " + lineNr + ": Geburtstag fehlt!");
    tokenizer = new StringTokenizer(dateString);
    if (tokenizer.countTokens() != 3) {
      throw new IOException("Zeile " + lineNr
          + ": Geburtstag hat falsches Format!");
    }
    try {
      int day = Integer.parseInt(tokenizer.nextToken());
      int month = Integer.parseInt(tokenizer.nextToken());
      if (month < 1 || month > 12) throw new NumberFormatException("");
      int year = Integer.parseInt(tokenizer.nextToken());
      Date date = new Date(day, dsa.model.Date.Month
          .values()[month - 1], year);
      return date;
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr
          + ": Geburtstag hat falsches Format!");
    }
  }

  private static void readSpells(BufferedReader in, Map<String, Integer> temptalents, Map<String, Integer> tempSpellIncreases) throws IOException {
    String line;
    line = in.readLine();
    int nrOfSpells = 0;
    try {
      nrOfSpells = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": Falsche Anzahl Zauber!");
    }
    for (int i = 0; i < nrOfSpells; ++i) {
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Zeile " + lineNr + ": "
            + "Ende der Datei, weitere Zauber erwartet!");
      String talentName = Strings.cutTo(line, '(').trim();
      in.readLine();
      in.readLine();
      line = in.readLine();
      lineNr += 3;
      if (line == null)
        throw new IOException("Zeile " + lineNr + ": "
            + "Ende der Datei, Talentwert erwartet!");
      int talentValue = 0;
      try {
        talentValue = Integer.parseInt(Strings.cutTo(line, ' ').trim());
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Ungültiger Talentwert!");
      }
      int spellIncrease = 1;
      try {
        int firstSpace = line.indexOf(' ');
        int secondSpace = line.indexOf(' ', firstSpace + 1);
        spellIncrease = Integer.parseInt(line
            .substring(firstSpace, secondSpace).trim());
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": Ungültiger Zaubersteigerungsversuchswert!");
      }
      if (Talents.getInstance().getTalent(talentName) == null) {
        throw new IOException("Zeile " + lineNr + " Unbekannter Zauber: "
            + talentName);
      }
      temptalents.put(talentName, Integer.valueOf(talentValue));
      tempSpellIncreases.put(talentName, Integer.valueOf(spellIncrease));
    }
  }

  private static void readRituals(BufferedReader in) throws IOException {
    String line;
    in.readLine();
    lineNr++;
    line = in.readLine();
    lineNr++; // Anzahl Rituale...
    int skip = 0;
    try {
      skip = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr
          + ": Falsche Anzahl Schamanenrituale");
    }
    for (int i = 0; i < skip; i++) {
      in.readLine();
      in.readLine();
      lineNr += 2; // jeweils ein Ritual
    }
  }

  private static void readTalents(BufferedReader in, Map<String, Integer> temptalents) throws IOException {
    String line;
    String talentCategory = in.readLine();
    lineNr++;
    int nrOfTalents = 0;
    while (!"Schamanenrituale".equals(talentCategory)
        && !"magische Tänze".equals(talentCategory)) {
      in.readLine();
      lineNr++; // ? unbekannte Zahl, mglw. Anzahl
      // Talentkategorien?
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Zeile " + lineNr + ": "
            + "Ende der Datei, weitere Talente erwartet!");
      try {
        nrOfTalents = Integer.parseInt(line.trim());
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Falsche Anzahl Talente!");
      }
      for (int i = 0; i < nrOfTalents; i++) {
        line = in.readLine();
        lineNr++;
        if (line == null)
          throw new IOException("Zeile " + lineNr + ": "
              + "Ende der Datei, weitere Talente erwartet!");
        String talentName = Strings.cutTo(line, '(').trim();
        if (Talents.getInstance().getTalent(talentName) == null) {
          // besonderes Talent. Erstmal zu parsen versuchen
          String regexString = "(.+) \\(";
          String goodPropertiesString = "";
          for (Property property : Property.values()) {
            if (property == Property.HA) break;
            if (goodPropertiesString.length() > 0) goodPropertiesString += "|";
            goodPropertiesString += property.name();
          }
          regexString += "(" + goodPropertiesString + ")/("
              + goodPropertiesString + ")/(" + goodPropertiesString + ")\\)";
          java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
              regexString).matcher(line);
          if (matcher.matches()) {
            talentName = matcher.group(1);
            Property p1 = Property.valueOf(matcher.group(2));
            Property p2 = Property.valueOf(matcher.group(3));
            Property p3 = Property.valueOf(matcher.group(4));
            dsa.model.talents.Talent newTalent = DataFactory.getInstance()
                .createNormalTalent(talentName, p1, p2, p3, 2);
            Talents.getInstance().addUserTalent(newTalent);
          }
          else {
            if (line.endsWith("(kein TaW)")) {
              talentName = line.substring(0, line.lastIndexOf("(kein TaW)"));
            }
            else
              talentName = line;
            Talents.getInstance().addUserTalent(
                DataFactory.getInstance().createOtherTalent(talentName, 2));
          }
        }
        line = in.readLine();
        lineNr++;
        if (line == null)
          throw new IOException("Zeile " + lineNr + ": "
              + "Ende der Datei, Talentwert erwartet!");
        int talentValue = 0;
        try {
          talentValue = Integer.parseInt(Strings.cutTo(line, ' ').trim());
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr + ": Ungültiger Talentwert!");
        }
        temptalents.put(talentName, Integer.valueOf(talentValue));
      }
      line = in.readLine();
      lineNr++;
      if (line == null)
        throw new IOException("Zeile " + lineNr + ": "
            + "Ende der Datei, LE erwartet!");
      talentCategory = line;
    }
  }
}

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
package dsa.model.data;

import static dsa.model.characters.Property.*;

import java.util.*;
import java.io.*;

import dsa.model.DataFactory;
import dsa.model.characters.Property;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Talent;

// import java.text.NumberFormat;

/**
 * @author joerg
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Talents {

  private static Talents instance = null;

  private Map<String, Talent> talentsByName;

  private Map<String, LinkedList<Talent>> talentsByCategory;

  private LinkedList<String> categoryList;

  public static Talents getInstance() {
    if (instance == null) instance = new Talents();
    return instance;
  }

  public void addUserTalent(Talent talent) {
    talentsByCategory.get("User").add(talent);
    talentsByName.put(talent.getName(), talent);
  }

  public void removeUserTalent(String talent) {
    Talent t = getTalent(talent);
    talentsByCategory.get("User").remove(t);
    talentsByName.remove(t.getName());
  }

  public Talent getTalent(String name) {
    if (talentsByName.containsKey(name)) {
      return talentsByName.get(name);
    }
    else
      return null;
  }

  public List<Talent> getTalentsInCategory(String category) {
    if (category.equals("Berufe / Sonstiges")) {
      LinkedList<Talent> list = new LinkedList<Talent>();
      list.addAll(talentsByCategory.get(category));
      list.addAll(talentsByCategory.get("User"));
      return list;
    }
    else if (talentsByCategory.containsKey(category)) {
      return talentsByCategory.get(category);
    }
    else
      return new LinkedList<Talent>();
  }

  public List<String> getKnownCategories() {
    List<String> list = new LinkedList<String>();
    list.addAll(categoryList);
    list.remove("User");
    return list;
  }

  public void loadFightingTalents(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String line = in.readLine();
    int lineNr = 1;
    String category = "Kampftalente";
    StringTokenizer tokenizer = null;
    LinkedList<Talent> categoryTalents = new LinkedList<Talent>();
    while (line != null) {
      tokenizer = new StringTokenizer(line, ";");
      if (tokenizer.countTokens() != 3) {
        throw new IOException("Zeile " + lineNr
            + ": Syntaxfehler im Kampftalent");
      }
      String name = tokenizer.nextToken();
      boolean projectile = tokenizer.nextToken().equals("1");
      String beMinusS = tokenizer.nextToken();
      int beMinus = 0;
      try {
        beMinus = Integer.parseInt(beMinusS);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": Syntaxfehler bei BE-Abzug");
      }
      Talent talent = DataFactory.getInstance().createFightingTalent(name,
          projectile, beMinus);
      talentsByName.put(name, talent);
      categoryTalents.add(talent);
      line = in.readLine();
      lineNr++;
    }
    talentsByCategory.put(category, categoryTalents);
    categoryList.add(1, category);
    in.close();
  }

  public void loadLanguages(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String line = in.readLine();
    int lineNr = 1;
    StringTokenizer tokenizer = null;
    String category = "Sprachen";
    LinkedList<Talent> languages = new LinkedList<Talent>();
    while (line != null) {
      tokenizer = new StringTokenizer(line, ";");
      boolean isOld = false;
      if (tokenizer.countTokens() < 3) {
        throw new IOException("Zeile " + lineNr + ": Syntaxfehler in Sprache!");
      }
      String name = tokenizer.nextToken();
      int max = 0;
      try {
        max = Integer.parseInt(tokenizer.nextToken());
        if (max < 1)
          throw new IOException("Zeile " + lineNr + ": Sprachen-Max falsch");
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": Sprachen-Max ist keine Zahl!");
      }
      isOld = (tokenizer.nextToken().equals("1"));
      dsa.model.impl.Language language = new dsa.model.impl.Language(name, max,
          isOld);
      while (tokenizer.hasMoreTokens()) {
        String tongue = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens())
          throw new IOException("Zeile " + lineNr
              + ": Syntaxfehler in Sprache!");
        int tongueMax = 0;
        try {
          tongueMax = Integer.parseInt(tokenizer.nextToken());
          if (tongueMax < 1)
            throw new IOException("Zeile " + lineNr
                + ": Spezielles Sprachen-Max falsch");
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr
              + ": Spezielles Sprachen-Max ist keine Zahl!");
        }
        language.setMax(tongue, tongueMax);
      }
      languages.add(language);
      talentsByName.put(name, language);
      line = in.readLine();
      lineNr++;
    }
    talentsByCategory.put(category, languages);
    // categoryList.add(category);
    in.close();
  }

  public void loadNormalTalents(String fileName) throws IOException {
    loadFile(fileName, false);
  }

  public void loadSpells(String fileName) throws IOException {
    loadFile(fileName, true);
  }

  public boolean isUserTalent(String talent) {
    for (Talent t : getTalentsInCategory("User")) {
      if (t.getName().equals(talent)) return true;
    }
    return false;
  }

  public void saveUserTalents(String fileName) throws IOException {
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        fileName)));
    for (Talent talent : getTalentsInCategory("User")) {
      String line = talent.getName() + ";" + (talent.canBeTested() ? "1" : "0")
          + ";" + talent.getMaxIncreasePerStep();
      if (talent.canBeTested()) {
        NormalTalent nt = (NormalTalent) talent;
        line += ";" + nt.getFirstProperty().name() + ";"
            + nt.getSecondProperty().name() + ";"
            + nt.getThirdProperty().name();
      }
      out.println(line);
    }
    out.flush();
    out.close();
  }

  public void loadUserTalents(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) return;
    HashMap<String, Property> attributes = new HashMap<String, Property>();
    attributes.put("MU", MU);
    attributes.put("KL", KL);
    attributes.put("IN", IN);
    attributes.put("CH", CH);
    attributes.put("FF", FF);
    attributes.put("GE", GE);
    attributes.put("KK", KK);
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String line = in.readLine();
    int lineNr = 1;
    StringTokenizer tokenizer = null;
    while (line != null) {
      tokenizer = new StringTokenizer(line, ";");
      if (tokenizer.countTokens() != 3 && tokenizer.countTokens() != 6) {
        throw new IOException("Zeile " + lineNr + ": Syntaxfehler in Talent");
      }
      String name = tokenizer.nextToken();
      boolean testable = tokenizer.nextToken().equals("1");
      int increases = 0;
      try {
        increases = Integer.parseInt(tokenizer.nextToken());
        if (increases < 0 || increases > 3) {
          throw new IOException("Zeile " + lineNr
              + ": Falsche Zahl Steigerungsversuche");
        }
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr
            + ": Syntaxfehler bei Steigerungsversuchen");
      }
      Talent talent = null;
      if (testable) {
        String att1s = tokenizer.nextToken();
        String att2s = tokenizer.nextToken();
        String att3s = tokenizer.nextToken();
        if (!attributes.containsKey(att1s) || !attributes.containsKey(att2s)
            || !attributes.containsKey(att3s)) {
          throw new IOException("Zeile " + lineNr + ": unbekannte Eigenschaft");
        }
        talent = DataFactory.getInstance().createNormalTalent(name,
            attributes.get(att1s), attributes.get(att2s),
            attributes.get(att3s), increases);
      }
      else {
        talent = DataFactory.getInstance().createOtherTalent(name, increases);
      }
      addUserTalent(talent);
      line = in.readLine();
      lineNr++;
    }
    in.close();
  }

  private void loadFile(String fileName, boolean spells) throws IOException {
    HashMap<String, Property> attributes = new HashMap<String, Property>();
    attributes.put("MU", MU);
    attributes.put("KL", KL);
    attributes.put("IN", IN);
    attributes.put("CH", CH);
    attributes.put("FF", FF);
    attributes.put("GE", GE);
    attributes.put("KK", KK);
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String line = in.readLine();
    int lineNr = 1;
    boolean newCategory = true;
    LinkedList<Talent> allTalents = new LinkedList<Talent>();
    LinkedList<Talent> categoryTalents = null;
    String category = null;
    StringTokenizer tokenizer = null;
    while (line != null) {
      if (newCategory) {
        categoryTalents = new LinkedList<Talent>();
        category = line;
        newCategory = false;
      }
      else if (line.trim().equals("")) {
        newCategory = true;
        talentsByCategory.put(category, categoryTalents);
        categoryList.addLast(category);
      }
      else {
        tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() != (spells ? 6 : 5)) {
          throw new IOException("Zeile " + lineNr + ": Syntaxfehler in Talent");
        }
        String name = tokenizer.nextToken();
        String att1s = tokenizer.nextToken();
        String att2s = tokenizer.nextToken();
        String att3s = tokenizer.nextToken();
        if (!attributes.containsKey(att1s) || !attributes.containsKey(att2s)
            || !attributes.containsKey(att3s)) {
          throw new IOException("Zeile " + lineNr + ": unbekannte Eigenschaft");
        }
        int increases = 0;
        if (!spells)
          try {
            increases = Integer.parseInt(tokenizer.nextToken());
            if (increases < 0 || increases > 3) {
              throw new IOException("Zeile " + lineNr
                  + ": Falsche Zahl Steigerungsversuche");
            }
          }
          catch (NumberFormatException e) {
            throw new IOException("Zeile " + lineNr
                + ": Syntaxfehler bei Steigerungsversuchen");
          }
        else
          increases = 1;
        Talent talent = spells ? DataFactory.getInstance()
            .createSpell(name, attributes.get(att1s), attributes.get(att2s),
                attributes.get(att3s), tokenizer.nextToken(),
                tokenizer.nextToken()) : DataFactory.getInstance()
            .createNormalTalent(name, attributes.get(att1s),
                attributes.get(att2s), attributes.get(att3s), increases);
        if (!spells) allTalents.addLast(talent);
        talentsByName.put(name, talent);
        categoryTalents.addLast(talent);
      }
      line = in.readLine();
      lineNr++;
    }
    talentsByCategory.put(category, categoryTalents);
    categoryList.addLast(category);
    if (!spells) {
      // talentsByCategory.put("Alle", allTalents);
      // categoryList.addLast("Alle");
    }
    in.close();
  }

  private void addTalent(String category, String name,
      dsa.model.characters.Property p1, dsa.model.characters.Property p2,
      dsa.model.characters.Property p3) {
    Talent talent = DataFactory.getInstance().createNormalTalent(name, p1, p2,
        p3, 0);
    talentsByName.put(name, talent);
    talentsByCategory.get(category).add(talent);
  }

  // public void exportToPalmDB(String filename) throws IOException {
  // PrintWriter out = new PrintWriter(new BufferedWriter(new
  // FileWriter(filename)));
  // NumberFormat format = NumberFormat.getIntegerInstance();
  // format.setGroupingUsed(false);
  // format.setMaximumIntegerDigits(4);
  // format.setMinimumIntegerDigits(4);
  // out.println("file");
  // out.println(" filename=\"Talente-DSAG\";");
  // out.println(" attrib=backup;");
  // out.println(" creatorid='DSAG';");
  // out.println(" typeid='TALS';");
  // out.println("begin");
  // int count = 0;
  // for (Iterator it = categoryList.iterator(); it.hasNext(); ) {
  // List subList = (List) talentsByCategory.get(it.next());
  // for (Iterator it2 = subList.iterator(); it2.hasNext(); ) {
  // Talent talent = (Talent) it2.next();
  // out.println(" record");
  // out.println(" begin");
  // out.println(" dec padstring(25)");
  // out.println(" long");
  // out.println(" " + count);
  // out.println(" byte");
  // out.println(" " + talent.getFirstAttribute() + " " +
  // talent.getSecondAttribute() + " " + talent.getThirdAttribute() + " " +
  // (categoryList.indexOf(talent.getCategory()) + 1));
  // String name = talent.getName();
  // if (name.length() > 24) name = name.substring(0, 24);
  // out.println(" \"" + name + "\"");
  // out.println(" end;");
  // count++;
  // }
  // }
  // out.println("end;");
  // out.flush();
  // out.close();
  // }

  public void readFavorites(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String line = in.readLine();
    LinkedList<Talent> favorites = new LinkedList<Talent>();
    while (line != null) {
      Talent talent = getTalent(line.trim());
      if (talent != null) favorites.addLast(talent);
      line = in.readLine();
    }
    talentsByCategory.put("Favoriten", favorites);
    categoryList.addFirst("Favoriten");
  }

  private Talents() {
    talentsByName = new HashMap<String, Talent>();
    talentsByCategory = new HashMap<String, LinkedList<Talent>>();
    categoryList = new LinkedList<String>();
    talentsByCategory.put("User", new LinkedList<Talent>());
  }

}

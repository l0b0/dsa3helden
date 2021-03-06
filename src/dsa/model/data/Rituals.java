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
package dsa.model.data;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import dsa.model.DiceSpecification;
import dsa.model.characters.Property;

public class Rituals {

  private static class TypeRituals {
    String title;

    ArrayList<Ritual> rituals;
  }

  private final HashMap<String, TypeRituals> theRituals;

  private final HashMap<String, Ritual> allRituals;

  private Ritual readRitual(String line) throws IOException {
    StringTokenizer tok = new StringTokenizer(line, ";");
    if (tok.countTokens() < 3)
      throw new IOException("Zeile " + lineNr + ": Falsches Ritualformat!");
    String name = tok.nextToken();
    String req = tok.nextToken();
    if (req.equals("-")) req = "";
    boolean isKnown = req.equals("*");
    if (isKnown) req = "";
    String learningTestString = tok.nextToken();
    boolean hasLT = !learningTestString.equals("-");
    Ritual.LearningTestData lt = hasLT ? parseLearningTestData(learningTestString)
        : null;
    boolean hasT = tok.hasMoreTokens();
    Ritual.TestData t = hasT ? parseTestData(tok.nextToken()) : null;
    return new Ritual(name, isKnown, req, t, lt);
  }

  private String readLine(BufferedReader in) throws IOException {
    lineNr++;
    String line = in.readLine();
    testEmpty(line);
    return line;
  }

  private TypeRituals readRituals(BufferedReader in) throws IOException {
    TypeRituals readRituals = new TypeRituals();
    readRituals.title = readLine(in);
    readRituals.rituals = new ArrayList<Ritual>();
    String line = readLine(in);
    while (line != null && !"-- End --".equals(line)) {
      Ritual ritual = readRitual(line);
      readRituals.rituals.add(ritual);
      allRituals.put(ritual.getName(), ritual);
      line = readLine(in);
    }
    return readRituals;
  }

  private void testEmpty(String line) throws IOException {
    if (line == null || line.equals(""))
      throw new IOException("Rituale: Zeile " + lineNr
          + ": Zeile ist leer oder unerwartetes Dateiende!");
  }

  private int parseInt(String text) throws IOException {
    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException e) {
      throw new IOException("Rituale: Zeile " + lineNr
          + ": Integer-Zahl erwartet!");
    }
  }

  private void parseOnlyTestData(Ritual.TestData td, StringTokenizer tok)
      throws IOException {
    td.p1 = Property.valueOf(tok.nextToken());
    td.p2 = Property.valueOf(tok.nextToken());
    td.p3 = Property.valueOf(tok.nextToken());
    td.defaultModifier = parseInt(tok.nextToken());
    td.mHasHalfStepLess = tok.nextToken().equals("1");
  }

  private Ritual.TestData parseTestData(String text) throws IOException {
    StringTokenizer tok = new StringTokenizer(text, "/");
    if (tok.countTokens() != 5)
      throw new IOException("Rituale: Zeile " + lineNr
          + ": Falsches Format für Probendaten!");
    Ritual.TestData td = new Ritual.TestData();
    parseOnlyTestData(td, tok);
    return td;
  }

  private DiceSpecification parsePointsRemoval(String text) throws IOException {
    try {
      return DiceSpecification.parse(text);
    }
    catch (NumberFormatException e) {
      throw new IOException(text + " spezifiziert keine Würfel!");
    }
  }

  private Ritual.LearningTestData parseLearningTestData(String text)
      throws IOException {
    StringTokenizer tok = new StringTokenizer(text, "/");
    if (tok.countTokens() != 9)
      throw new IOException("Rituale: Zeile " + lineNr
          + ": Falsches Format für Lernprobendaten!");
    Ritual.LearningTestData td = new Ritual.LearningTestData();
    parseOnlyTestData(td, tok);
    td.ap = parsePointsRemoval(tok.nextToken());
    td.permanentAP = parsePointsRemoval(tok.nextToken());
    td.le = parsePointsRemoval(tok.nextToken());
    td.permanentLE = parsePointsRemoval(tok.nextToken());
    return td;
  }

  private void readAllRituals(BufferedReader in) throws IOException {
    theRituals.clear();
    allRituals.clear();
    lineNr = 0;
    lineNr++;
    String line = in.readLine();
    while (line != null) {
      String chType = line;
      TypeRituals tr = readRituals(in);
      theRituals.put(chType, tr);
      lineNr++;
      line = in.readLine();
    }
  }

  public void readFile(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1"));;
    try {
      readAllRituals(in);
    }
    finally {
      in.close();
    }
  }

  public ArrayList<String> getAllRituals(String characterType) {
    ArrayList<String> rs = new ArrayList<String>();
    for (String chType : theRituals.keySet()) {
      if (characterType.indexOf(chType) != -1) {
        for (Ritual r : theRituals.get(chType).rituals) {
          rs.add(r.getName());
        }
      }
    }
    return rs;
  }

  public String getRitualsTitle(String characterType) {
    for (String chType : theRituals.keySet()) {
      if (characterType.indexOf(chType) != -1) {
        return theRituals.get(chType).title;
      }
    }
    return "";
  }

  public ArrayList<String> getStartRituals(String characterType) {
    ArrayList<String> rs = new ArrayList<String>();
    if (characterType.toLowerCase(java.util.Locale.GERMAN).indexOf("halbelf") != -1) return rs;
    for (String chType : theRituals.keySet()) {
      if (characterType.toLowerCase(java.util.Locale.GERMAN).indexOf(
          chType.toLowerCase(java.util.Locale.GERMAN)) != -1) {
        for (Ritual r : theRituals.get(chType).rituals) {
          if (r.isKnownAtStart()) rs.add(r.getName());
        }
      }
    }
    return rs;
  }

  public boolean isRitualAvailable(String ritual, List<String> knownRituals) {
    Ritual r = allRituals.get(ritual);
    if (r == null) return false;
    if (r.getRequirement().equals("")) return true;
    return knownRituals.contains(r.getRequirement());
  }

  public Ritual.TestData getRitualTestData(String ritual) {
    Ritual r = allRituals.get(ritual);
    if (r == null) return null;
    return r.getTestData();
  }

  public Ritual.LearningTestData getRitualLearningTestData(String ritual) {
    Ritual r = allRituals.get(ritual);
    if (r == null) return null;
    return r.getLearningTestData();
  }

  public static Rituals getInstance() {
    if (!dataRead) {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
        + java.io.File.separator;
      try {
        instance.readFile(dirPath + "Rituale.dat");
        dataRead = true;
      }
      catch (java.io.IOException e) {
        javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
            "Fehler beim Laden der Rituale", javax.swing.JOptionPane.ERROR_MESSAGE);        
      }
    }
    return instance;
  }

  private static Rituals instance = new Rituals();
  
  private static boolean dataRead = false;

  private Rituals() {
    theRituals = new HashMap<String, TypeRituals>();
    allRituals = new HashMap<String, Ritual>();
  }

  private int lineNr;
}

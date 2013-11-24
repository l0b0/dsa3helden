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
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.model.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import dsa.model.DiceSpecification;

public class Animals {

  private final ArrayList<String> categories;

  private final HashMap<String, ArrayList<String>> races;

  private final HashMap<String, ArrayList<Animal>> prototypes;

  private String fileName;

  private void throwException(String message) throws IOException {
    throw new IOException("Datei " + fileName + ", Zeile " + lineNr + ": "
        + message);
  }

  public List<String> getCategories() {
    ArrayList<String> c = new ArrayList<String>();
    c.addAll(categories);
    return c;
  }

  public List<String> getRaces(String category) {
    ArrayList<String> r = new ArrayList<String>();
    if (!races.containsKey(category)) return r;
    r.addAll(races.get(category));
    return r;
  }

  public int getNrOfSteps(String category, String race) {
    String key = category + "##" + race;
    if (!prototypes.containsKey(key)) return 0;
    return prototypes.get(key).size();
  }

  private int getDicedValue(Object value, Animal.AttributeType attrType) {
    if (attrType == Animal.AttributeType.eDiced) {
      return ((DiceSpecification) value).calcValue();
    }
    else if (attrType == Animal.AttributeType.eInt) {
      return ((Integer) value).intValue();
    }
    else
      return 0;
  }

  public Animal createAnimal(String category, String race, int step) {
    String key = category + "##" + race;
    if (!prototypes.containsKey(key)) return null;
    ArrayList<Animal> p = prototypes.get(key);
    if (step < 0 || step >= p.size()) return null;
    Animal a = null;
    try {
      a = (Animal) p.get(step).clone();
    }
    catch (CloneNotSupportedException e) { 
      throw new InternalError();
    }
    for (int i = 0; i < a.attributeInfos.size(); ++i) {
      if (a.attributeInfos.get(i).type == Animal.AttributeType.eMultiplier) {
        a.attributeInfos.get(i).type = Animal.AttributeType.eInt;
        double multiplier = ((Double) a.attributes.get(i)).doubleValue();
        int factor = ((Integer) a.attributes.get(i - 1)).intValue();
        a.attributes.set(i, Integer.valueOf((int) (multiplier * factor)));
      }
      else if (a.attributeInfos.get(i).addedWithSteps) {
        int value = 0;
        for (int j = 0; j <= step; ++j) {
          value += getDicedValue(p.get(j).attributes.get(i), a.attributeInfos
              .get(i).type);
        }
        a.attributeInfos.get(i).type = Animal.AttributeType.eInt;
        a.attributes.set(i, value);
      }
      else if (a.attributeInfos.get(i).type == Animal.AttributeType.eDiced) {
        a.attributeInfos.get(i).type = Animal.AttributeType.eInt;
        a.attributes.set(i, getDicedValue(a.attributes.get(i),
            Animal.AttributeType.eDiced));
      }
    }
    a.setCategory(race);
    return a;
  }

  public void readFiles(String path) throws IOException {
    File dir = new File(path + "Tiere");
    if (dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (file.getName().endsWith(".dat")) {
          readFile(file);
        }
      }
    }
  }

  public static Animals getInstance() {
    return sInstance;
  }

  private Animals() {
    categories = new ArrayList<String>();
    races = new HashMap<String, ArrayList<String>>();
    prototypes = new HashMap<String, ArrayList<Animal>>();
  }

  private static Animals sInstance = new Animals();

  private int parseInt(String line) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throwException("Wert ist keine Zahl!");
      return 0;
    }
  }

  private void testEmpty(String line) throws IOException {
    if (line == null) throwException("Unerwartetes Dateiende!");
  }

  private void readFile(File file) throws IOException {
    lineNr = 0;
    fileName = file.getName();
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      String line = in.readLine();
      lineNr++;
      testEmpty(line);
      String category = line;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      int nrOfSteps = parseInt(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      int nrOfAttributes = parseInt(line);
      Animal.AttributeType[] attrTypes = new Animal.AttributeType[nrOfAttributes];
      boolean[] incr = new boolean[nrOfAttributes];
      ArrayList<Animal.AttributeMetaInfo> metaInfos = new ArrayList<Animal.AttributeMetaInfo>();
      for (int i = 0; i < nrOfAttributes; ++i) {
        Animal.AttributeMetaInfo info = new Animal.AttributeMetaInfo();
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        StringTokenizer t = new StringTokenizer(line, ";");
        if (t.countTokens() != 4)
          throwException("Formatfehler in Metaangabe!");
        info.title = t.nextToken();
        String s = t.nextToken();
        if (s.equals("W"))
          attrTypes[i] = Animal.AttributeType.eDicing;
        else if (s.equals("M"))
          attrTypes[i] = Animal.AttributeType.eMultiplier;
        else if (s.equals("I"))
          attrTypes[i] = Animal.AttributeType.eInt;
        else if (s.equals("D"))
          attrTypes[i] = Animal.AttributeType.eDiced;
        else if (s.equals("G"))
          attrTypes[i] = Animal.AttributeType.eSpeed;
        else if (s.equals("S"))
          attrTypes[i] = Animal.AttributeType.eString;
        else
          throwException("Falsches Format von Attributtyp!");
        incr[i] = t.nextToken().equals("1");
        info.testable = t.nextToken().equals("1");
        info.addedWithSteps = incr[i];
        info.type = attrTypes[i];
        metaInfos.add(info);
      }
      categories.add(category);
      ArrayList<String> r = new ArrayList<String>();
      line = in.readLine();
      lineNr++;
      while (line != null) {
        String art = line;
        int artSteps = nrOfSteps;
        StringTokenizer s = new StringTokenizer(art, ";");
        if (s.countTokens() > 1) {
          art = s.nextToken();
          artSteps = parseInt(s.nextToken());
        }
        ArrayList<Animal> p = new ArrayList<Animal>();
        for (int i = 0; i < artSteps; ++i) {
          Animal a = new Animal(category);
          a.step = i;
          a.attributeInfos = metaInfos;
          p.add(a);
        }
        readPrototypes(in, p, attrTypes, incr, artSteps);
        String key = category + "##" + art;
        prototypes.put(key, p);
        r.add(art);
        line = in.readLine();
        lineNr++;
      }
      races.put(category, r);
    }
    finally {
      in.close();
    }
  }

  private void readPrototypes(BufferedReader in, ArrayList<Animal> animals,
      Animal.AttributeType[] attrTypes, boolean[] increasing, int steps)
      throws IOException {
    for (int i = 0; i < attrTypes.length; ++i) {
      String line = in.readLine();
      lineNr++;
      testEmpty(line);
      Object[] values = new Object[steps];
      readValues(values, attrTypes[i], line);
      for (int j = 0; j < steps; ++j)
        animals.get(j).attributes.add(values[j]);
    }
  }

  private Object parseValue(String text, Animal.AttributeType attrType)
      throws IOException {
    if ((attrType == Animal.AttributeType.eDiced)
        || (attrType == Animal.AttributeType.eDicing)) {
      try {
        return DiceSpecification.parse(text);
      }
      catch (NumberFormatException e) {
        throwException(e.getMessage());
        return null;
      }
    }
    else if (attrType == Animal.AttributeType.eInt) {
      return parseInt(text);
    }
    else if (attrType == Animal.AttributeType.eMultiplier) {
      try {
        return Double.parseDouble(text);
      }
      catch (NumberFormatException e) {
        throwException(e.getMessage());
        return null;
      }
    }
    else if (attrType == Animal.AttributeType.eSpeed) {
      return Animal.SpeedData.parse(text, lineNr);
    }
    else
      return text;
  }

  private void readValues(Object[] values, Animal.AttributeType attrType,
      String line) throws IOException {
    if (attrType == Animal.AttributeType.eMultiplier) {
      Object m = parseValue(line, attrType);
      for (int i = 0; i < values.length; ++i)
        values[i] = m;
      return;
    }
    StringTokenizer t = new StringTokenizer(line);
    if (t.countTokens() != values.length) {
      throwException("Nicht genug Werte!");
    }
    for (int i = 0; i < values.length; ++i)
      values[i] = parseValue(t.nextToken(), attrType);
  }

  int lineNr;
}

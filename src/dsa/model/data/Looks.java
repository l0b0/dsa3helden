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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

public class Looks {

  private static Looks instance = new Looks();
  
  private static boolean dataRead = false;

  public static Looks getInstance() {
    if (!dataRead) {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
        + java.io.File.separator;
      try {
        instance.readEyeColors(dirPath + "Augen.dat");
        instance.readHairColors(dirPath + "Haare.dat");
        dataRead = true;
      }
      catch (java.io.IOException e) {
        javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
            "Fehler beim Laden der Haar- und Augenfarben", javax.swing.JOptionPane.ERROR_MESSAGE);        
      }
    }
    return instance;
  }

  private Looks() {
  }

  public static String getMatchingEyeColor(String hairColor) {
    String s = hairColor.toLowerCase(java.util.Locale.GERMAN);
    int roll = dsa.control.Dice.roll(20);
    if (s.contains("schwarz")) {
      if (roll <= 16)
        return "schwarz";
      else
        return "dunkelbraun";
    }
    else if (s.contains("hellbraun") || s.contains("dunkelblond")
        || s.contains("aschblond")) {
      if (roll <= 4)
        return "braun";
      else if (roll <= 8)
        return "grün";
      else if (roll <= 12)
        return "graugrün";
      else if (roll <= 16)
        return "graublau";
      else
        return "blau";
    }
    else if (s.contains("braun")) {
      if (roll <= 7)
        return "schwarz";
      else if (roll <= 13)
        return "dunkelbraun";
      else if (roll <= 17)
        return "braun";
      else if (roll <= 19)
        return "grün";
      else
        return "blau";
    }
    else if (s.contains("weiß") || s.contains("silber")) {
      if (roll <= 10)
        return "blau";
      else if (roll <= 13)
        return "graublau";
      else if (roll <= 16)
        return "grau";
      else if (roll <= 19)
        return "graugrün";
      else
        return "grün";
    }
    else if (s.contains("blond") || s.contains("grau")) {
      if (roll <= 3)
        return "grün";
      else if (roll <= 7)
        return "braun";
      else if (roll <= 14)
        return "grau";
      else if (roll <= 16)
        return "graublau";
      else
        return "blau";
    }
    else if (s.contains("rot")) {
      if (roll <= 10)
        return "grün";
      else if (roll <= 14)
        return "graugrün";
      else if (roll <= 17)
        return "grau";
      else if (roll <= 19)
        return "graublau";
      else
        return "blau";
    }
    else if (s.contains("albino")) {
      if (roll <= 18)
        return "rot";
      else if (roll == 19)
        return "violett";
      else
        return "weiß";
    }
    else {
      if (roll <= 2)
        return "schwarz";
      else if (roll <= 5)
        return "braun";
      else if (roll <= 10)
        return "grau";
      else if (roll <= 13)
        return "graugrün";
      else if (roll <= 15)
        return "grün";
      else if (roll <= 18)
        return "graublau";
      else
        return "blau";
    }
  }

  private final java.util.ArrayList<String> eyeColors = new java.util.ArrayList<String>();

  private final java.util.ArrayList<String> hairCategories = new java.util.ArrayList<String>();

  private final java.util.HashMap<String, java.util.ArrayList<String>> hairColors = new java.util.HashMap<String, java.util.ArrayList<String>>();

  public java.util.List<String> getEyeColors() {
    return Collections.unmodifiableList(eyeColors);
  }

  public java.util.List<String> getHairCategories() {
    return Collections.unmodifiableList(hairCategories);
  }

  public java.util.List<String> getHairColors(String category) {
    if (hairColors.containsKey(category)) {
      return Collections.unmodifiableList(hairColors.get(category));
    }
    else
      return new java.util.ArrayList<String>();
  }

  private String fileName;

  public void testEmpty(String line) throws IOException {
    if (line == null)
      throw new IOException(fileName + ": Unerwartetes Dateiende!");
  }

  public void readEyeColors(String name) throws IOException {
    fileName = name;
    eyeColors.clear();
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(name), "ISO-8859-1"));;
    try {
      String line = in.readLine();
      while (line != null) {
        eyeColors.add(line);
        line = in.readLine();
      }
    }
    finally {
      in.close();
    }
  }

  public void readHairColors(String name) throws IOException {
    fileName = name;
    hairColors.clear();
    hairCategories.clear();
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(name), "ISO-8859-1"));
    try {
      String line = in.readLine();
      while (line != null) {
        hairCategories.add(line);
        java.util.ArrayList<String> a = internalReadHairColors(in);
        hairColors.put(line, a);
        line = in.readLine();
      }
    }
    finally {
      in.close();
    }
  }

  private java.util.ArrayList<String> internalReadHairColors(BufferedReader in)
      throws IOException {
    java.util.ArrayList<String> colors = new java.util.ArrayList<String>();
    String line = in.readLine();
    testEmpty(line);
    while (!"-- end --".equals(line)) {
      colors.add(line);
      line = in.readLine();
      testEmpty(line);
    }
    return colors;
  }

}

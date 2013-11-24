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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import dsa.util.Optional;
import dsa.util.Strings;

public class Weapons {

  private static Weapons instance = new Weapons();

  private final ArrayList<LinkedList<Weapon>> weapons;

  public static Weapons getInstance() {
    return instance;
  }

  public Weapon getWeapon(String name) {
    String name2 = Strings.getStringWithoutChangeTag(name);
    for (Iterator<LinkedList<Weapon>> it = weapons.iterator(); it.hasNext();) {
      LinkedList<Weapon> typeList = it.next();
      for (Iterator<Weapon> it2 = typeList.iterator(); it2.hasNext();) {
        Weapon weapon = it2.next();
        if (weapon.getName().equals(name2)) {
          return weapon;
        }
        else if (name2.startsWith(weapon.getName())) {
          String test = name2.substring(weapon.getName().length() + 1);
          try {
            Integer.parseInt(test);
            return weapon;
          }
          catch (NumberFormatException e) {
            continue;
          }
        }
      }
    }
    return null;
  }

  public LinkedList<Weapon> getAllWeapons() {
    LinkedList<Weapon> allWeapons = new LinkedList<Weapon>();
    for (LinkedList<Weapon> wl : weapons) {
      allWeapons.addAll(wl);
    }
    return allWeapons;
  }
  
  public Set<String> getProjectileTypes() {
    java.util.HashSet<String> ptts = new java.util.HashSet<String>();
    LinkedList<Weapon> allWeapons = getAllWeapons();
    ptts.add("Keine");
    for (Weapon w : allWeapons) {
      if (w.isProjectileWeapon()) {
        ptts.add(w.getProjectileType());
      }
    }
    return ptts;
  }

  public void addWeapon(Weapon weapon) {
    weapons.get(weapon.getType()).add(weapon);
  }

  public void removeWeapon(String name) {
    Weapon weapon = getWeapon(name);
    if (weapon == null) return;
    weapons.get(weapon.getType()).remove(weapon);
  }

  private static String[] sCategories = { "Raufen", "Boxen", "Hruruzat",
      "Ringen", "Äxte und Beile", "Dolche", "Infanteriewaffen", "Linkshändig",
      "Kettenwaffen", "Peitschen", "Scharfe Hiebwaffen", "Schwerter",
      "Speere und Stäbe", "Stichwaffen", "Stumpfe Hiebwaffen", "Zweihänder",
      "Lanzenreiten", "Schußwaffen", "Wurfwaffen", "Schleuder" };

  public static String getCategoryName(int index) {
    return sCategories[index];
  }

  public static int getCategoryIndex(String categoryName) {
    for (int i = 0; i < sCategories.length; ++i) {
      if (sCategories[i].equals(categoryName)) {
        return i;
      }
    }
    return -1;
  }
  
  public static boolean isAUCategory(int index) {
    return isAUCategory(getCategoryName(index));
  }
  
  public static boolean isAUCategory(String name) {
    return name.equals("Raufen") || name.equals("Boxen") || name.equals("Ringen");
  }

  public static boolean isFarRangedCategory(String category) {
    int index = getCategoryIndex(category);
    return index > getCategoryIndex("Lanzenreiten");
  }

  public static boolean isFarRangedCategory(int index) {
    return index > getCategoryIndex("Lanzenreiten");
  }
  
  public static boolean isProjectileCategory(String category) {
    int index = getCategoryIndex(category);
    return isProjectileCategory(index);
  }
  
  public static boolean isProjectileCategory(int index) {
    return (index == getCategoryIndex("Schußwaffen")) || (index == getCategoryIndex("Schleuder"));
  }

  public static String[] getAvailableCategories() {
    String[] categories = new String[sCategories.length - 2];
    categories[0] = sCategories[0];
    System.arraycopy(sCategories, 3, categories, 1, sCategories.length - 3);
    return categories;
  }

  private static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  public void loadUserDefinedWeapons(String filename) throws IOException {
    File file = new File(filename);
    if (!file.exists()) return;
    BufferedReader in = new BufferedReader(new FileReader(file));
    int lineNr = 0;
    String line = in.readLine();
    lineNr++;
    int nrOfWeapons;
    try {
      nrOfWeapons = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Datei " + filename + ": Falsches Format.");
    }
    for (int i = 0; i < nrOfWeapons; ++i) {
      Weapon w = new Weapon();
      w.readFromStream(in, lineNr);
    }
  }

  public void storeUserDefinedWeapons(String filename) throws IOException {
    LinkedList<Weapon> udWeapons = new LinkedList<Weapon>();
    for (LinkedList<Weapon> ws : weapons) {
      for (Weapon w : ws) {
        if (w.isUserDefined()) udWeapons.addLast(w);
      }
    }
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        filename)));
    try {
      out.println(udWeapons.size());
      for (Weapon w : udWeapons) {
        w.writeToStream(out);
      }
      out.flush();
    }
    finally {
      out.close();
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

  public void loadFile(String filename) throws IOException {
    for (int i = 0; i < weapons.size(); ++i) {
      weapons.get(i).clear();
    }
    BufferedReader in = new BufferedReader(new FileReader(filename));
    try {
      int lineNr = 0;
      String line = in.readLine();
      lineNr++;
      while (line != null) {
        int category = parseInt(line, lineNr);
        if ((category < 0) || (category > 19)) {
          throw new IOException(lineNr + " Kategorie falsch.");
        }
        line = in.readLine();
        lineNr++;
        if (line == null) throw new IOException("EOF");
        String name = line;
        line = in.readLine();
        lineNr++;
        if (line == null) throw new IOException("Unerwartetes Dateieende in " + filename);
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
          w6d = parseInt(wt.nextToken(), lineNr);
          if (wt.hasMoreTokens()) {
            constd = parseInt(wt.nextToken(), lineNr);
          }
        }
        line = in.readLine();
        lineNr++;
        Optional<Integer> kkzuschlag = NULL_INT;
        if (line == null)
          throw new IOException("Unerwartetes Dateieende in " + filename);
        if (!((line.trim().equals("-")) || (line.charAt(0) == '('))) {
          kkzuschlag = new Optional<Integer>(parseInt(line, lineNr));
        }
        line = in.readLine();
        lineNr++;
        if (line == null) throw new IOException("Unerwartetes Dateieende in " + filename);
        int pos = line.indexOf('/');
        Weapon.WV wv = null;
        if (pos > 0) {
          int at = parseInt(line.substring(0, pos), lineNr);
          int pa = parseInt(line.substring(pos + 1), lineNr);
          wv = new Weapon.WV(at, pa);
        }
        else {
          // Reichweite
        }
        
        line = in.readLine();
        lineNr++;
        int bf = 0;
        if (line == null)
          throw new IOException("Unerwartetes Dateieende in " + filename);
        if (line.trim().equals("-"))
          bf = 0;
        else {
          bf = parseInt(line, lineNr);
        }
        line = in.readLine();
        lineNr++; // Gewicht
        if (line == null)
          throw new IOException("Unerwartetes Dateieende in " + filename);
        int weight = 0;
        weight = parseInt(line, lineNr);
        line = in.readLine();
        lineNr++;
        if (line == null)
          throw new IOException("Unerwartetes Dateieende in " + filename);
        boolean twoHanded = line.trim().equals("1");
        boolean projectile = Weapons.isFarRangedCategory(category);
        int nrOfDists = Weapon.Distance.values().length;
        int[] distances = new int[nrOfDists];
        int[] distMods = new int[nrOfDists];
        String ptt = "Keine";
        Optional<Integer> projectileWeight = new Optional<Integer>();
        Optional<Integer> projectileWorth = new Optional<Integer>();
        if (projectile) {
          line = in.readLine();
          lineNr++;
          if (line == null)
            throw new IOException("Unerwartetes Dateieende in " + filename);
          StringTokenizer st = new StringTokenizer(line, "/");
          if (st.countTokens() != nrOfDists) {
            throw new IOException("Zeile " + lineNr + ": Falsches Format!");
          }
          for (int i = 0; i < nrOfDists; ++i) {
            distances[i] = parseInt(st.nextToken(), lineNr);
          }
          line = in.readLine();
          lineNr++;
          if (line == null)
            throw new IOException("Unerwartetes Dateieende in " + filename);
          st = new StringTokenizer(line, "/");
          if (st.countTokens() != nrOfDists) {
            throw new IOException("Zeile " + lineNr + ": Falsches Format!");
          }
          for (int i = 0; i < nrOfDists; ++i) {
            distMods[i] = parseInt(st.nextToken(), lineNr);
          }
          if (isProjectileCategory(category)) {
            line = in.readLine();
            lineNr++;
            if (line == null)
              throw new IOException("Unerwartetes Dateiende in " + filename);
            ptt = line.trim();
            line = in.readLine();
            lineNr++;
            if (line == null)
              throw new IOException("Unerwartetes Dateiende in " + filename);
            projectileWeight.setValue(parseInt(line, lineNr));
            line = in.readLine();
            lineNr++;
            if (line == null)
              throw new IOException("Unerwartetes Dateiende in " + filename);
            projectileWorth.setValue(parseInt(line, lineNr));
          }
        }
        line = in.readLine();
        lineNr++;
        if (line == null) 
          throw new IOException("Unerwartetes Dateiende in " + filename);
        Optional<Integer> worth = Optional.NULL_INT;
        if (!("-".equals(line.trim()))) {
          int w = parseInt(line, lineNr);
          worth = new Optional<Integer>(w);
        }
        if (!projectile && wv == null) {
          wv = new Weapon.WV(4, 4);
        }
        Weapon weapon = new Weapon(w6d, constd, category, name, bf, kkzuschlag,
            weight, false, twoHanded, projectile, worth, wv, ptt, projectileWeight, 
            projectileWorth);
        if (projectile) {
          weapon.setDistanceMods(distMods);
          weapon.setDistances(distances);
        }
        weapons.get(category).addLast(weapon);
        line = in.readLine();
        lineNr++;
      }
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }

  private Weapons() {
    weapons = new ArrayList<LinkedList<Weapon>>(20);
    for (int i = 0; i < 20; ++i) {
      weapons.add(new LinkedList<Weapon>());
    }
  }

}

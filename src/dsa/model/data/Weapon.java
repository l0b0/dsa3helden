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
import java.io.IOException;
import java.io.PrintWriter;

import dsa.util.Optional;

/**
 * @author joerg
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Weapon implements Cloneable {

  private int w6damage;

  private int constDamage;

  private String name;

  private int type;

  private int bf;

  private Optional<Integer> kkBonus;

  private int weight;

  private boolean userDefined;

  private boolean farRanged;
  
  private Optional<Integer> worth;
  
  private Optional<Integer> projectileWeight;
  
  private Optional<Integer> projectileWorth;
  
  private String projectileType; 
  
  private boolean singular;
  
  public static class WV {
    private int at;
    private int pa;
    
    public int getAT() { return at; }
    public int getPA() { return pa; }
    
    public WV(int at, int pa) {
      this.at = at;
      this.pa = pa;
    }
    
    public String toString() {
      return at + "/" + pa;
    }
  }
  
  private WV wv;

  /**
   * 
   */
  public Weapon() {
    this(1, 0, 0, "unbenannt", 0, new Optional<Integer>(20), 0, false, false,
        false, Optional.NULL_INT, new WV(4, 4), false);
  }
  
  public boolean isFarRangedWeapon() {
    return farRanged;
  }

  public static enum Distance {
    VeryClose, Close, Far, VeryFar, ExtremelyFar
  }

  public int getDistanceCategoryTPMod(Distance d) {
    return distanceMods[d.ordinal()];
  }

  public int getDistanceCategory(int distance) {
    for (int i = 0; i < distances.length; ++i) {
      if (distance <= distances[i]) return i;
    }
    return Distance.values().length - 1;
  }

  public int getDistanceTPMod(int distance) {
    for (int i = 0; i < distances.length; ++i) {
      if (distance <= distances[i]) return distanceMods[i];
    }
    return 0;
  }

  public int getDistancePaces(Distance d) {
    return distances[d.ordinal()];
  }

  public boolean canHit(int distance) {
    return distance <= distances[distances.length - 1];
  }

  private int[] distanceMods = new int[Distance.values().length];

  private int[] distances = new int[Distance.values().length];

  public void setDistanceMods(int[] distanceMods) {
    this.distanceMods = new int[distanceMods.length];
    System.arraycopy(distanceMods, 0, this.distanceMods, 0, distanceMods.length);
  }

  public void setDistances(int[] distances) {
    this.distances = new int[distances.length];
    System.arraycopy(distances, 0, this.distances, 0, distances.length);
  }
  
  public Optional<Integer> getWorth() {
    return worth;
  }
  
  public WV getWV() {
    return wv;
  }
  
  public void setWV(int at, int pa) {
    if (!farRanged) wv = new WV(at, pa);
  }
  
  public boolean isSingular() {
    return singular;
  }
  
  public void setIsSingular(boolean singular) {
    this.singular = singular;
  }

  private static final int FILE_VERSION = 9;

  public void writeToStream(PrintWriter out) throws IOException {
    out.println(FILE_VERSION);
    out.println(name);
    out.println(w6damage);
    out.println(constDamage);
    out.println(type);
    out.println(bf);
    out.println(kkBonus);
    out.println(weight);
    // version 2
    out.println(userDefined ? 1 : 0);
    // version 3
    out.println(twoHanded ? 1 : 0);
    // version 4
    out.println(farRanged ? 1 : 0);
    if (farRanged) {
      for (int i = 0; i < distances.length; ++i) {
        out.println(distances[i]);
      }
      for (int i = 0; i < distanceMods.length; ++i) {
        out.println(distanceMods[i]);
      }
    }
    out.println(worth.hasValue() ? worth.getValue() : "-");
    out.println(projectileType);
    // version 7
    out.println(projectileWeight.hasValue() ? projectileWeight.getValue() : "-");
    out.println(projectileWorth.hasValue() ? projectileWorth.getValue() : "-");
    // version 8
    if (wv != null) {
      out.println(wv.getAT());
      out.println(wv.getPA());
    }
    else {
      out.println("-");
    }
    // version 9
    out.println(singular ? 1 : 0);
    out.println("-- End of Weapon --");
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

  private void testEmpty(String s) throws IOException {
    if (s == null) throw new IOException("Unerwartetes Dateiende!");
  }

  private static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  public int readFromStream(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    int version = parseInt(line, lineNr);
    // if (version > fileVersion) throw new IOException("Zeile " + lineNr + ":
    // Version ist zu neu!");
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    name = line;
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    w6damage = parseInt(line, lineNr);
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    constDamage = parseInt(line, lineNr);
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    type = parseInt(line, lineNr);
    if (type < 0 || type > 19)
      throw new IOException("Zeile " + lineNr + ": Waffenkategorie ist falsch!");
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    bf = parseInt(line, lineNr);
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    if (line != null && !line.trim().equals("-")) {
      kkBonus = new Optional<Integer>(parseInt(line, lineNr));
    }
    else
      kkBonus = NULL_INT;
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    weight = parseInt(line, lineNr);

    if (version >= 2) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      userDefined = "1".equals(line);
    }
    else
      userDefined = false;
    if (version >= 3) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      twoHanded = "1".equals(line);
    }
    else
      twoHanded = false;
    if (version >= 4) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      farRanged = "1".equals(line);
      if (farRanged) {
        for (int i = 0; i < distances.length; ++i) {
          line = in.readLine();
          lineNr++;
          testEmpty(line);
          distances[i] = parseInt(line, lineNr);
        }
        for (int i = 0; i < distanceMods.length; ++i) {
          line = in.readLine();
          lineNr++;
          testEmpty(line);
          distanceMods[i] = parseInt(line, lineNr);
        }
      }
    }
    else
      farRanged = false;
    if (version >= 5) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      if ("-".equals(line)) {
        worth = Optional.NULL_INT;
      }
      else {
        int w = parseInt(line, lineNr);
        worth = new Optional<Integer>(w);
      }
    }
    else
      worth = Optional.NULL_INT;
    
    if (version >= 6) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      projectileType = line;
    }
    else
      projectileType = "Keine";
    
    if (version >= 7) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      if ("-".equals(line)) {
        projectileWeight = Optional.NULL_INT;
      }
      else {
        int w = parseInt(line, lineNr);
        projectileWeight = new Optional<Integer>(w);
      }
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      if ("-".equals(line)) {
        projectileWorth = Optional.NULL_INT;
      }
      else {
        int w = parseInt(line, lineNr);
        projectileWorth = new Optional<Integer>(w);
      }
    }
    else {
      projectileWeight = Optional.NULL_INT;
      projectileWorth = Optional.NULL_INT;
    }
    
    if (version >= 8) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      if (!"-".equals(line)) {
        int at = parseInt(line, lineNr);
        line = in.readLine();
        lineNr++;
        int pa = parseInt(line, lineNr);
        wv = new WV(at, pa);
      }
      else wv = null;
    }
    else if (!farRanged) {
      wv = new WV(4, 4);
    }
    
    if (version >= 9) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      singular = parseInt(line, lineNr) == 1;
    }
    else
      singular = false;
    
    if (version < 3) {
      Weapon w = Weapons.getInstance().getWeapon(name);
      if (w != null) {
        userDefined = w.userDefined;
        twoHanded = w.twoHanded;
        farRanged = w.farRanged;
        if (farRanged) {
          for (int i = 0; i < distances.length; ++i) {
            distances[i] = w.distances[i];
            distanceMods[i] = w.distanceMods[i];
          }
        }
      }
    }

    do {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    } while (line != null && !line.equals("-- End of Weapon --"));
    if (Weapons.getInstance().getWeapon(name) == null) {
      userDefined = true;
      Weapons.getInstance().addWeapon(this);
    }
    return lineNr;
  }
  
  public Weapon(int w6d, int constD, int t, String n, int aBF,
      Optional<Integer> kk, int weight, boolean userDefined, boolean twoHanded,
      boolean farRanged, Optional<Integer> worth, WV wv, boolean singular)
  {
    this(w6d, constD, t, n, aBF, kk, weight, userDefined, twoHanded, farRanged, worth, wv, 
        "Keine", Optional.NULL_INT, Optional.NULL_INT, singular);
  }

  public Weapon(int w6d, int constD, int t, String n, int aBF,
      Optional<Integer> kk, int weight, boolean userDefined, boolean twoHanded,
      boolean farRanged, Optional<Integer> worth, WV wv, String ptt, 
      Optional<Integer> ptw, Optional<Integer> ptm, boolean singular) {
    w6damage = w6d;
    constDamage = constD;
    type = t;
    name = n;
    bf = aBF;
    kkBonus = kk;
    this.weight = weight;
    this.userDefined = userDefined;
    this.twoHanded = twoHanded;
    this.farRanged = farRanged;
    this.worth = worth;
    this.projectileType = ptt;
    this.projectileWeight = ptw;
    this.projectileWorth = ptm;
    this.wv = wv;    
    this.singular = singular;
  }

  public Object clone() throws CloneNotSupportedException {
    Weapon clone = (Weapon) super.clone();
    clone.wv = wv != null ? new WV(wv.at, wv.pa) : null;
    return clone;
  }

  /**
   * @return
   */
  public int getConstDamage() {
    return constDamage;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
  public int getW6damage() {
    return w6damage;
  }

  /**
   * @param i
   */
  public void setConstDamage(int i) {
    constDamage = i;
  }

  /**
   * @param string
   */
  public void setName(String string) {
    name = string;
  }

  /**
   * @param i
   */
  public void setW6damage(int i) {
    w6damage = i;
  }

  static final Weapon FIST = new Weapon(1, 0, 0, "Faust", 0,
      new Optional<Integer>(17), 0, false, false, false, Optional.NULL_INT,
      new WV(2, 0), false);

  /**
   * @return
   */
  public Optional<Integer> getKKBonus() {
    return kkBonus;
  }

  /**
   * @param i
   */
  public void setKKBonus(Optional<Integer> i) {
    kkBonus = i;
  }

  /**
   * @return
   */
  public int getBF() {
    return bf;
  }

  /**
   * @return
   */
  public int getType() {
    return type;
  }

  /**
   * @param i
   */
  public void setBF(int i) {
    bf = i;
  }

  /**
   * @param i
   */
  public void setType(int i) {
    type = i;
  }

  public void setFarRanged(boolean farRanged) {
    this.farRanged = farRanged;
    if (farRanged) wv = null;
    else if (wv == null) wv = new WV(4, 4);
  }

  public int getWeight() {
    return weight;
  }

  public boolean isUserDefined() {
    return userDefined;
  }

  public void setUserDefined(boolean ud) {
    userDefined = ud;
  }

  private boolean twoHanded;

  public boolean isTwoHanded() {
    return twoHanded;
  }
  
  public void setWorth(Optional<Integer> w) {
    worth = w;
  }

  public void setTwoHanded(boolean twoHanded) {
    this.twoHanded = twoHanded;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public String getProjectileType() {
    return projectileType;
  }

  public void setProjectileType(String projectileType) {
    this.projectileType = projectileType;
  }
  
  public Optional<Integer> getProjectileWeight() {
    return projectileWeight;
  }

  public void setProjectileWeight(Optional<Integer> projectileWeight) {
    this.projectileWeight = projectileWeight;
  }

  public boolean isProjectileWeapon() {
    return Weapons.isProjectileCategory(type);
  }

  public Optional<Integer> getProjectileWorth() {
    return projectileWorth;
  }

  public void setProjectileWorth(Optional<Integer> projectileWorth) {
    this.projectileWorth = projectileWorth;
  }

}

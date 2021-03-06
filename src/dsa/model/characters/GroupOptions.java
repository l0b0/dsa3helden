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
package dsa.model.characters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import dsa.control.Markers;
import dsa.gui.dialogs.OptionsDialog;

public class GroupOptions {

  GroupOptions() {
  }

  private boolean fastAERegeneration;

  private boolean fullFirstStep;

  private boolean earlyTwoHanded;

  private boolean qvatPABasis;

  private boolean qvatStunned;

  private boolean qvatMarkers;

  private boolean heavyClothes;
  private boolean lowerClothesBE;

  private boolean hardTwoHandedWeapons;
  
  private boolean qvatUseWV;
  
  private boolean useHitZones;

  private boolean changed = false;

  public boolean isChanged() {
    return changed;
  }

  void setChanged(boolean value) {
    changed = value;
  }

  public void getDefaults() {
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    setEarlyTwoHanded(prefs.getBoolean("EarlyLeftHanded", true));
    setFastAERegeneration(prefs.getBoolean("HighAERegeneration", true));
    setQvatStunned(prefs.getBoolean("QvatUseKO", false));
    setQvatMarkers(Markers.isUsingMarkers());
    heavyClothes = prefs.getBoolean("HeavyClothes", false);
    lowerClothesBE = prefs.getBoolean("LowerClothesBE", true);
    hardTwoHandedWeapons = prefs.getBoolean("Hard2HWeapons", true);
    setUseHitZones(prefs.getBoolean("UseHitZones", false));
    prefs = Preferences.userRoot().node("dsa/data/impl");
    setFullFirstStep(prefs.getBoolean("FullFirstStep", true));
    setQvatPABasis(prefs.getBoolean("QvatPaBasis", true));
    setQvatUseWV(prefs.getBoolean("QvatUseWV", false));
  }

  private static int fileVersion = 6;

  public void writeToFile(PrintWriter out) throws IOException {
    out.println(fileVersion);
    out.println(fastAERegeneration ? 1 : 0);
    out.println(fullFirstStep ? 1 : 0);
    out.println(earlyTwoHanded ? 1 : 0);
    out.println(qvatPABasis ? 1 : 0);
    out.println(qvatStunned ? 1 : 0);
    out.println(qvatMarkers ? 1 : 0);
    out.println(heavyClothes ? 1 : 0);
    out.println(hardTwoHandedWeapons ? 1 : 0);
    out.println(qvatUseWV ? 1 : 0);
    out.println(lowerClothesBE ? 1 : 0);
    out.println(useHitZones ? 1 : 0);
    out.println("-- End Options --");
    changed = false;
  }

  private static void testEmpty(String line) throws IOException {
    if (line == null) {
      throw new IOException("Unerwartetes Dateiende!");
    }
  }

  public int readFromFile(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    int version = 0;
    try {
      version = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": falsches Format!");
    }
    if (version > 0) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      fastAERegeneration = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      fullFirstStep = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      earlyTwoHanded = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      qvatPABasis = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      qvatStunned = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      qvatMarkers = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    if (version > 1) {
      heavyClothes = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    else {
      Preferences prefs = Preferences.userNodeForPackage(OptionsDialog.class);
      heavyClothes = prefs.getBoolean("HeavyClothes", false);
    }
    if (version > 2) {
      hardTwoHandedWeapons = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    else {
      Preferences prefs = Preferences.userNodeForPackage(OptionsDialog.class);
      hardTwoHandedWeapons = prefs.getBoolean("Hard2HWeapons", true);
    }
    if (version > 3) {
      qvatUseWV = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    else {
      Preferences prefs = Preferences.userNodeForPackage(OptionsDialog.class);
      qvatUseWV = prefs.getBoolean("QvatUseWV", false);
    }
    if (version > 4) {
      lowerClothesBE = "1".equals(line);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    else {
      lowerClothesBE = !heavyClothes;
    }
    if (version > 5) {
    	useHitZones = "1".equals(line);
    	line = in.readLine();
    	lineNr++;
    	testEmpty(line);
    }
    else {
    	Preferences prefs = Preferences.userNodeForPackage(OptionsDialog.class);
    	useHitZones = prefs.getBoolean("UseHitZones", false);
    }
    while (line != null && !line.equals("-- End Options --")) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    }
    Markers.setUseMarkers(qvatMarkers);
    changed = false;

    return lineNr;
  }

  public boolean isEarlyTwoHanded() {
    return earlyTwoHanded;
  }

  public void setEarlyTwoHanded(boolean earlyTwoHanded) {
    this.earlyTwoHanded = earlyTwoHanded;
    changed = true;
  }

  public boolean hasFastAERegeneration() {
    return fastAERegeneration;
  }

  public void setFastAERegeneration(boolean fastAERegeneration) {
    this.fastAERegeneration = fastAERegeneration;
    changed = true;
  }

  public boolean hasFullFirstStep() {
    return fullFirstStep;
  }

  public void setFullFirstStep(boolean fullFirstStep) {
    this.fullFirstStep = fullFirstStep;
    changed = true;
  }
  
  public enum ClothesBE {
	  Normal,
	  Lower,
	  Items
  }

  public ClothesBE getClothesBE() {
    return heavyClothes ? ClothesBE.Normal : (lowerClothesBE ? ClothesBE.Lower : ClothesBE.Items);
  }

  public void setClothesBE(ClothesBE clothesBE) {
	boolean heavyClothes = clothesBE == ClothesBE.Normal;
	boolean lowerBE = clothesBE == ClothesBE.Lower; 
    if (heavyClothes != this.heavyClothes || lowerBE != lowerClothesBE) {
      this.heavyClothes = heavyClothes;
      lowerClothesBE = lowerBE;
      changed = true;
    }
  }

  public boolean hasHard2HWeapons() {
    return hardTwoHandedWeapons;
  }

  public void setHard2HWeapons(boolean hard2HWeapons) {
    if (hard2HWeapons != hardTwoHandedWeapons) {
      hardTwoHandedWeapons = hard2HWeapons;
      loadCorrectFiles();
      changed = true;
    }
  }

  public boolean hasQvatMarkers() {
    return qvatMarkers;
  }

  public void setQvatMarkers(boolean qvatMarkers) {
    this.qvatMarkers = qvatMarkers;
    Markers.setUseMarkers(qvatMarkers);
    changed = true;
  }

  public boolean hasQvatPABasis() {
    return qvatPABasis;
  }

  public void setQvatPABasis(boolean qvatPABasis) {
    this.qvatPABasis = qvatPABasis;
    changed = true;
  }

  public boolean hasQvatStunned() {
    return qvatStunned;
  }

  public void setQvatStunned(boolean qvatStunned) {
    this.qvatStunned = qvatStunned;
    changed = true;
  }
  
  public boolean useWV() {
    return qvatUseWV;
  }
  
  public boolean qvatUseWV() {
    return qvatUseWV;
  }
  
  public void setQvatUseWV(boolean useWV) {
    this.qvatUseWV = useWV;
    changed = true;
  }
  
  public boolean useHitZones() {
	  return useHitZones;
  }
  
  public void setUseHitZones(boolean useHitZones) {
	  this.useHitZones = useHitZones;
	  changed = true;
  }

  public void loadCorrectFiles() {
    String weaponsFile = hardTwoHandedWeapons ? "Waffen.dat" : "Waffen2.dat";
    String ownWeaponsFile = "Eigene_Waffen.dat";
    String dirPath = dsa.util.Directories.getApplicationPath() + "daten" 
        + java.io.File.separator;
    String userDirPath = dsa.util.Directories.getUserDataPath();
    try {
      dsa.model.data.Weapons weapons = dsa.model.data.Weapons.getInstance();
      weapons.storeUserDefinedWeapons(userDirPath + ownWeaponsFile);
      weapons.loadFile(dirPath + weaponsFile);
      weapons.loadUserDefinedWeapons(userDirPath + ownWeaponsFile);
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(null,
          "Fehler beim Laden der Waffen!\nFehlermeldung: " + e.getMessage(),
          "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
    }
  }

}

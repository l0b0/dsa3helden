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

import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

// import net.sourceforge.napkinlaf.NapkinLookAndFeel;

import dsa.gui.frames.ControlFrame;
import dsa.gui.frames.SubFrame;
import dsa.gui.lf.LookAndFeels;
import dsa.model.DataFactory;
import dsa.model.data.Animals;
import dsa.model.data.CharacterTypes;
import dsa.model.data.Cities;
import dsa.model.data.Currencies;
import dsa.model.data.Looks;
import dsa.model.data.Names;
import dsa.model.data.Regions;
import dsa.model.data.Rituals;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Things;
import dsa.model.data.Weapons;

// import dsa.data.Armours;

/**
 * 
 */
public class Main {

  private static String[] startFiles = null;

  public static void main(String[] args) {
    System.setErr(new ErrorReportStream());

    if ((args.length > 0) && args[0].equals("-StoreFrameBounds")) {
      SubFrame.setSaveLocations(true);
    }
    else {
      SubFrame.setSaveLocations(false);
      if (args.length > 0) {
        startFiles = args;
      }
    }

    try {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
          + java.io.File.separator;
      Preferences prefs = Preferences
          .userNodeForPackage(dsa.gui.frames.SubFrame.class);
      boolean secondStart = prefs.getBoolean("initialized_"
          + Version.getCurrentVersionString(), false);
      if (!secondStart) {
        int test = prefs.getInt("Heldenverwaltungx", -1);
        if (test == -1) {
          SubFrame.loadAllBounds(false);
        }
        else {
          SubFrame.loadAllBounds(true);
        }
        prefs.putBoolean("initialized_" + Version.getCurrentVersionString(),
            true);
      }
      DataFactory.setFactory(new dsa.model.impl.DataFactoryImpl());
      Talents.getInstance().loadNormalTalents(dirPath + "Talente.dat");
      Talents.getInstance().loadFightingTalents(dirPath + "Kampftalente.dat");
      Talents.getInstance().loadSpells(dirPath + "Zauber.dat");
      Talents.getInstance().readFavorites(dirPath + "Favoriten.dat");
      Talents.getInstance().loadLanguages(dirPath + "Sprachen.dat");
      Talents.getInstance().loadUserTalents(dirPath + "Eigene_Talente.dat");
      Currencies.getInstance().readCurrencies(dirPath + "Waehrungen.dat");
      // Armours.getInstance().loadFile(dirPath + "Ruestungen.dat");
      Weapons.getInstance().loadFile(dirPath + "Waffen.dat");
      Weapons.getInstance().loadUserDefinedWeapons(
          dirPath + "Eigene_Waffen.dat");
      Things.getInstance().loadFile(dirPath + "Ausruestung.dat");
      Rituals.getInstance().readFile(dirPath + "Rituale.dat");
      Animals.getInstance().readFiles(dirPath);
      Things.getInstance().loadUserDefinedThings(
          dirPath + "Eigene_Ausruestung.dat");
      Looks.getInstance().readEyeColors(dirPath + "Augen.dat");
      Looks.getInstance().readHairColors(dirPath + "Haare.dat");
      Cities.getInstance().readCities(dirPath + "Staedte.dat");
      Names.getInstance().readFiles(dirPath + "Namen");
      Shields.getInstance().readFile(dirPath + "Schilde.dat");
      CharacterTypes.getInstance().parseFiles(dirPath + "Heldentypen");
      Regions.getInstance().readFiles(dirPath + "Regionen");
    }
    catch (java.io.IOException e) {
      javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
          "Fehler beim Laden der Daten", javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

  private static void createAndShowGUI() {
    try {
      LookAndFeels.setLastLookAndFeel();
      ControlFrame mainFrame = new ControlFrame(startFiles == null);
      mainFrame.setVisible(true);
      if (startFiles != null) {
        for (String file : startFiles) {
          if (file.endsWith(".dsagroup") || file.endsWith(".grp")) {
            java.io.File f = new java.io.File(file);
            if (f.exists()) {
              mainFrame.openGroup(f);
              return;
            }
          }
        }
        boolean first = false;
        for (String file : startFiles) {
          if (file.endsWith(".dsahero") || file.endsWith(".dsa")) {
            java.io.File f = new java.io.File(file);
            if (f.exists()) {
              if (first) {
                mainFrame.newGroup();
                first = false;
              }
              mainFrame.openHero(f);
            }
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}

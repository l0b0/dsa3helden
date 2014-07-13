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
package dsa.control;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import dsa.gui.dialogs.ErrorHandlingDialog;
import dsa.gui.frames.ControlFrame;
import dsa.gui.frames.FrameLayouts;
import dsa.gui.frames.SubFrame;
import dsa.gui.lf.LookAndFeels;
import dsa.model.DataFactory;
import dsa.model.data.Clothes;
import dsa.model.data.Currencies;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Things;
import dsa.model.data.Tradezones;
import dsa.model.data.Weapons;
import dsa.model.data.Armours;

/**
 * 
 */
public class Main {
  
  private Main() {}

  private static java.util.List<String> startFiles = null;
  
  public static void main(String[] args) {
    System.setErr(new ErrorReportStream());

    if ((args.length > 0) && args[0].equals("-StoreFrameBounds")) {
      SubFrame.setSaveLocations(true);
    }
    else {
      SubFrame.setSaveLocations(false);
      if (args.length > 0) {
        startFiles = java.util.Arrays.asList(args);
      }
    }

    try {
      Preferences prefs = Preferences
          .userNodeForPackage(dsa.gui.frames.SubFrame.class);
      boolean secondStart = prefs.getBoolean("initialized_"
          + Version.getCurrentVersionString(), false);
      if (!secondStart) {
        int test = prefs.getInt("Heldenverwaltungx", -1);
        if (test == -1) {
          Preferences prefs2 = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
          test = prefs2.getInt("Heldenverwaltungx", -1);
        }
        if (test == -1) {
          SubFrame.loadAllBounds(false);
        }
        else {
          SubFrame.loadAllBounds(true);
        }
        prefs.putBoolean("initialized_" + Version.getCurrentVersionString(),
            true);
      }

      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
        + java.io.File.separator;
      String userDataPath = dsa.util.Directories.getUserDataPath();
      DataFactory.setFactory(new dsa.model.impl.DataFactoryImpl());
      Tradezones.getInstance().readFile(dirPath + "Handelszonen.dat");
      Talents.getInstance().loadNormalTalents(dirPath + "Talente.dat");
      Talents.getInstance().loadFightingTalents(dirPath + "Kampftalente.dat");
      Talents.getInstance().loadSpells(dirPath + "Zauber.dat");
      Talents.getInstance().readFavorites(dirPath + "Favoriten.dat");
      Talents.getInstance().loadLanguages(dirPath + "Sprachen.dat");
      Talents.getInstance().loadUserTalents(userDataPath + "Eigene_Talente.dat");
      Talents.getInstance().loadUserSpells(userDataPath + "Eigene_Zauber.dat");
      Currencies.getInstance().readCurrencies(dirPath + "Waehrungen.dat");
      Armours.getInstance().loadFile(dirPath + "Ruestungen2.dat");
      Armours.getInstance().loadUserDefinedArmours(userDataPath + "Eigene_Ruestungen.dat");
      //Weapons.getInstance().loadFile(dirPath + "Waffen.dat");
      Weapons.getInstance().loadUserDefinedWeapons(
          userDataPath + "Eigene_Waffen.dat");
      Things.getInstance().loadFile(dirPath + "Ausruestung.dat");
      Things.getInstance().loadUserDefinedThings(
          userDataPath + "Eigene_Ausruestung.dat");
      Shields.getInstance().readFile(dirPath + "Schilde.dat");
      Shields.getInstance().readUserDefinedFile(
          userDataPath + "Eigene_Parade.dat");
      Clothes.getInstance().readFromFile(dirPath + "Kleidung.dat", false);
      Clothes.getInstance().readFromFile(userDataPath + "Eigene_Kleidung.dat", true);
      FrameLayouts layouts = FrameLayouts.getInstance();
      layouts.readFromFile(FrameLayouts.getDefaultLayoutsFilename(), true);
      if (layouts.getStoredLayouts().length == 0) {
        layouts.readFromFile(FrameLayouts.getOldLayoutsFilename(), false);
      }
      if (layouts.getStoredLayouts().length == 0) {
        layouts.readDefaultLayouts(dirPath + "StandardLayouts.dat");
      }
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
  
  public static void exit(int retCode)
  {
    System.exit(retCode);
  }

  private static void createAndShowGUI() {
    try {
      final UncaughtExceptionHandler oldHandler = java.lang.Thread.currentThread().getUncaughtExceptionHandler();
      java.lang.Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
		public void uncaughtException(Thread t, Throwable e) {
			if (e instanceof Exception) {
				ErrorHandlingDialog dialog = new ErrorHandlingDialog((Exception)e);
				dialog.setModal(true);
				dialog.setVisible(true);
			}
			else
				oldHandler.uncaughtException(t, e);
		}
      });

      // Synthetica licence
      String[] li = {"Licensee=Jörg Rüdenauer", "LicenseRegistrationNumber=NCJR110913", "Product=Synthetica", "LicenseType=Non Commercial", "ExpireDate=--.--.----", "MaxVersion=2.999.999"};
      UIManager.put("Synthetica.license.info", li);
      UIManager.put("Synthetica.license.key", "6B3713D8-767BE153-5CA73FFD-BB277655-EDA39A83");
        
      LookAndFeels.setLastLookAndFeel();
      ControlFrame mainFrame = new ControlFrame(startFiles == null);
      mainFrame.setVisible(true);
      if (startFiles != null) {
        for (String file : startFiles) {
          if (file.endsWith(".dsagroup") || file.endsWith(".grp")) {
            java.io.File f = new java.io.File(file);
            if (f.exists()) {
              GroupOperations.openGroup(f, mainFrame);
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
                GroupOperations.newGroup(mainFrame);
                first = false;
              }
              GroupOperations.openHero(f, mainFrame);
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

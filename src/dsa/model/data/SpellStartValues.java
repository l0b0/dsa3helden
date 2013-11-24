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

import java.util.HashMap;
import java.util.StringTokenizer;

import java.io.*;

import dsa.model.talents.Talent;

public class SpellStartValues {

  private static SpellStartValues instance = new SpellStartValues();
  
  private static boolean dataRead = false;
  
  private static class TypeSpellStarts {
    
    private final HashMap<String, Integer> startValues;
    private final HashMap<String, Integer> increases;
    
    private static int parseInt(String s, String n, int l) throws IOException {
      try {
        return Integer.parseInt(s);
      }
      catch (NumberFormatException e) {
        throw new IOException("Datei " + n + ", Zeile " + l + ": " + s + " ist keine Zahl!");
      }
    }
    
    public TypeSpellStarts(File file) throws IOException {
      startValues = new HashMap<String, Integer>();
      increases = new HashMap<String, Integer>();
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
      try {
        int lineNr = 0;
        // ignore first 3 lines
        for (int i = 0; i < 3; ++i) {
          lineNr++;
          in.readLine();
        }
        lineNr++;
        String line = in.readLine();
        while (line != null) {
          StringTokenizer st = new StringTokenizer(line, ";");
          int nrOfTokens = st.countTokens();
          if (nrOfTokens < 3) {
            throw new IOException("Datei " + file.getName() + ", Zeile " + lineNr + ": falsches Format!");
          }
          String spell = st.nextToken();
          Talent t = Talents.getInstance().getTalent(spell);
          if (t == null) {
            throw new IOException("Datei " + file.getName() + ", Zeile " + lineNr + ": unbekannter Zauber!");
          }
          if (!t.isSpell()) {
            throw new IOException("Datei " + file.getName() + ", Zeile " + lineNr + ": kein Zauber!");
          }
          if (nrOfTokens > 1) {
            int startValue = parseInt(st.nextToken(), file.getName(), lineNr);
            if (startValue < -25 || startValue > 18) {
              throw new IOException("Datei " + file.getName() + ", Zeile " + lineNr + ": Startwert falsch!");
            }
            startValues.put(spell, startValue);
          }
          if (nrOfTokens > 2) {
            int incr = parseInt(st.nextToken(), file.getName(), lineNr);
            if (incr < 0 || incr > 3) {
              throw new IOException("Datei " + file.getName() + ", Zeile " + lineNr + ": Anzahl Steigerungen falsch!");
            }
            increases.put(spell, incr);
          }
          
          lineNr++;
          line = in.readLine();
        }
      }
      finally {
        if (in != null) in.close();
      }
      for (Talent spell : Talents.getInstance().getTalentsInCategory("Zauber")) {
        if (!((dsa.model.talents.Spell)spell).isUserDefined() && !startValues.containsKey(spell.getName())) {
          javax.swing.JOptionPane.showMessageDialog(null, 
              "Warnung: die Datei " + file.getName() + " enthält nicht für alle Zauber Angaben!\n"
              + "Fehlender Zauber: " + spell.getName(), "Heldenverwaltung", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
      }
    }
    
    public int getStartValue(String spell) {
      return startValues.containsKey(spell) ? startValues.get(spell) : -6;
    }
    
    public int getIncreasesPerStep(String spell) {
      return increases.containsKey(spell) ? increases.get(spell) : 1;
    }
  }
  
  private final HashMap<String, TypeSpellStarts> values;
  
  private SpellStartValues() {
    values = new HashMap<String, TypeSpellStarts>();
  }
  
  private static class CSVFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name.endsWith(".csv");
    }    
  }
  
  public void parseFiles(String directory) throws IOException {
    values.clear();
    File file = new File(directory);
    if (!file.isDirectory()) {
      throw new IOException(directory + " ist kein Verzeichnis!");
    }
    File[] files = file.listFiles(new CSVFilter());
    for (File f : files) {
      TypeSpellStarts tss = new TypeSpellStarts(f);
      String typeName = f.getName().substring(0, f.getName().lastIndexOf('.'));
      values.put(typeName, tss);
    }
  }
  
  public static SpellStartValues getInstance() { 
    if (!dataRead) {
      String dirPath = dsa.util.Directories.getApplicationPath() + "daten"
        + java.io.File.separator;
      try {
        instance.parseFiles(dirPath + "Zauberstartwerte");
        dataRead = true;
      }
      catch (java.io.IOException e) {
        javax.swing.JOptionPane.showMessageDialog(null, e.getMessage(),
            "Fehler beim Laden der Zauberstartwerte", javax.swing.JOptionPane.ERROR_MESSAGE);        
      }
    }
    return instance;
  }
  
  public int getStartValue(String characterType, String spell) {
    if (values.containsKey(characterType)) {
      return values.get(characterType).getStartValue(spell);
    }
    else 
      return -6;
  }
  
  public int getIncreasesPerStep(String characterType, String spell) {
    if (values.containsKey(characterType)) {
      return values.get(characterType).getIncreasesPerStep(spell);
    }
    else
      return -6;
  }
}

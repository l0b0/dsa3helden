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
import java.util.LinkedHashSet;
import java.util.StringTokenizer;

import dsa.model.DataFactory;
import dsa.model.DiceSpecification;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;

public class CharacterType {

  private final String maleName;

  private final String femaleName;
  
  private final String typeName;

  private final boolean malePossible;

  private final boolean femalePossible;

  private final int[] requirements;

  private final String[] hairColors;

  private final String[] origins;

  private final DiceSpecification height;

  private final int weightLoss;

  private final boolean regionModifiable;

  private final String defaultNameRegion;

  private final File prototypeFile;

  CharacterType(File file) throws IOException {
    int lineNr = 0;
    String path = file.getAbsolutePath();
    path = path.substring(0, path.length() - 3) + "dsahero";
    prototypeFile = new File(path);
    if (!prototypeFile.exists()) {
      throw new IOException("Heldenvorlage " + path + " fehlt!");
    }
    String fileName = file.getName();
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      maleName = in.readLine();
      lineNr++;
      if (maleName == null)
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      malePossible = !maleName.equals("-");
      femaleName = in.readLine();
      lineNr++;
      if (femaleName == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      femalePossible = !femaleName.equals("-");
      requirements = new int[Property.values().length];
      String line = in.readLine();
      lineNr++;
      if (line == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      StringTokenizer t = new StringTokenizer(line);
      if (t.countTokens() != requirements.length / 2) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Falsche Zahl Werte!");
      }
      int i = 0;
      while (t.hasMoreTokens()) {
        try {
          requirements[i] = Integer.parseInt(t.nextToken());
          if (requirements[i] < -14 || requirements[i] > 14) {
            throw new IOException("Datei " + fileName + ", Zeile " + lineNr
                + ": Ungültige Voraussetzung!");
          }
        }
        catch (NumberFormatException e) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Voraussetzung falsch!");
        }
        ++i;
      }
      line = in.readLine();
      lineNr++;
      if (line == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      t = new StringTokenizer(line);
      if (t.countTokens() != requirements.length / 2) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Falsche Zahl Werte!");
      }
      while (t.hasMoreTokens()) {
        try {
          requirements[i] = Integer.parseInt(t.nextToken());
          if (requirements[i] < -14 || requirements[i] > 14) {
            throw new IOException("Datei " + fileName + ", Zeile " + lineNr
                + ": Ungültige Voraussetzung!");
          }
        }
        catch (NumberFormatException e) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Voraussetzung falsch!");
        }
        ++i;
      }
      // fix for different bad properties order
      int temp = requirements[7]; // AG
      requirements[7] = requirements[8]; // HA
      requirements[8] = requirements[10]; // TA
      requirements[10] = temp;
      hairColors = new String[20];
      for (int j = 0; j < 20; ++j) {
        hairColors[j] = in.readLine();
        lineNr++;
        if (hairColors[j] == null) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Unerwartetes Dateiende!");
        }
      }
      origins = new String[20];
      for (int j = 0; j < 20; ++j) {
        origins[j] = in.readLine();
        lineNr++;
        if (origins[j] == null) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Unerwartetes Dateiende!");
        }
      }
      line = in.readLine();
      lineNr++;
      if (line == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      try {
        height = DiceSpecification.parse(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Größe hat falsches Format!");
      }
      line = in.readLine();
      lineNr++;
      if (line == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      try {
        weightLoss = Integer.parseInt(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Gewichtsabzug hat falsches Format!");
      }
      line = in.readLine();
      lineNr++;
      if (line == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      regionModifiable = line.trim().equals("1");
      defaultNameRegion = in.readLine();
      lineNr++;
      if (defaultNameRegion == null) {
        throw new IOException("Datei " + fileName + ", Zeile " + lineNr
            + ": Unerwartetes Dateiende!");
      }
      line = in.readLine();
      lineNr++;
      if (line != null && !line.trim().equals("")) {
        try {
          talentReducement = Integer.parseInt(line);
        }
        catch (NumberFormatException e) {
          throw new IOException("Datei " + fileName + ", Zeile " + lineNr
              + ": Talentabzug hat falsches Format!");
        }
      }
      else
        talentReducement = 0;
      typeName = fileName.substring(0, fileName.lastIndexOf('.'));
    }
    finally {
      if (in != null) {
        in.close();
      }
    }
  }

  private int talentReducement;

  public int getTalentReducement() {
    return talentReducement;
  }

  public String getDefaultNameRegion() {
    return defaultNameRegion;
  }

  public String getFemaleName() {
    return femaleName;
  }

  public boolean isFemalePossible() {
    return femalePossible;
  }

  public DiceSpecification getHeight() {
    return height;
  }

  public String getMaleName() {
    return maleName;
  }

  public boolean isMalePossible() {
    return malePossible;
  }

  public boolean isRegionModifiable() {
    return regionModifiable;
  }

  public int[] getRequirements() {
    int[] result = new int[requirements.length];
    System.arraycopy(requirements, 0, result, 0, requirements.length);
    return result;
  }

  public int getWeightLoss() {
    return weightLoss;
  }

  public String getOrigin(int nr) {
    return origins[nr];
  }

  public java.util.LinkedHashSet<String> getPossibleOrigins() {
    java.util.LinkedHashSet<String> lhs = new LinkedHashSet<String>();
    for (int i = 0; i < origins.length; ++i) {
      lhs.add(origins[i]);
    }
    return lhs;
  }

  public String getHairColor(int nr) {
    return hairColors[nr];
  }
  
  public String getTypeName() {
    return typeName;
  }

  private Hero prototype = null;

  public Hero getPrototype() throws IOException {
    if (prototype == null) {
      prototype = DataFactory.getInstance().createHeroFromFile(prototypeFile);
    }
    return prototype;
  }

}

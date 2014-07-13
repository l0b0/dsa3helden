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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import dsa.util.AbstractObservable;
import dsa.util.Observer;
import dsa.util.Strings;

interface ThingsListenerBase extends Observer {
  void thingChanged(String thing);
}

public class Things extends AbstractObservable<ThingsListenerBase> {
  
  public static interface ThingsListener extends ThingsListenerBase {
  }
  
  private static Things instance = new Things();

  private final java.util.HashMap<String, Thing> theThings;

  public static Things getInstance() {
    return instance;
  }

  public Thing getThing(String name) {
    String name2 = Strings.getStringWithoutChangeTag(name);
    if (theThings.containsKey(name2))
      return theThings.get(name2);
    else {
      for (String tName : theThings.keySet()) {
        if (name2.startsWith(tName)) {
          String test = name2.substring(tName.length() + 1);
          try {
            Integer.parseInt(test);
            return theThings.get(tName);
          }
          catch (NumberFormatException e) {
            continue;
          }
        }
      }
    }
    return null;
  }

  public void addThing(Thing thing) {
    theThings.put(thing.getName(), thing);
  }

  public void removeThing(String thing) {
    String name2 = Strings.getStringWithoutChangeTag(thing);
    theThings.remove(name2);
  }

  public Thing[] getAllThings() {
    Thing[] allThings = new Thing[theThings.size()];
    return theThings.values().toArray(allThings);
  }

  public java.util.ArrayList<Thing> getAllWearableThings() {
    java.util.ArrayList<Thing> result = new java.util.ArrayList<Thing>();
    for (Thing thing : theThings.values()) {
      String category = thing.getCategory();
      if (!category.equals("Transport / Reise")
          && !category.equals("Gasthaus / Miete")
          && !category.equals("Dienstleistung")
          && !category.equals("Nutztiere")) {
        result.add(thing);
      }
    }
    return result;
  }

  public java.util.HashSet<String> getKnownCategories() {
    java.util.HashSet<String> categories = new java.util.HashSet<String>();
    for (Thing t : theThings.values()) {
      if (t.getCategory() != null) categories.add(t.getCategory());
    }
    return categories;
  }
  
  public void thingChanged(String thing) {
    for (ThingsListenerBase listener : observers) {
      listener.thingChanged(thing);
    }
  }

  public void loadFile(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1"));;
    try {
      int lineNr = 0;
      String line = in.readLine();
      lineNr++;
      while (line != null) {
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() != 7)
          throw new IOException("Zeile " + lineNr
              + ": Syntaxfehler in Ausrüstung!");
        String name = tokenizer.nextToken();
        String tradezones = tokenizer.nextToken();
        if ("-".equals(tradezones.trim())) {
          tradezones = "";
        }
        StringTokenizer tokenizer2 = new StringTokenizer(tradezones.trim());
        while (tokenizer2.hasMoreTokens()) {
          String zone = tokenizer2.nextToken();
          if (Tradezones.getInstance().getTradezone(zone) == null) {
            throw new IOException("Zeile " + lineNr + ": Unbekannte Handelszone!");
          }
        }
        String valueS = tokenizer.nextToken().trim();
        if (valueS.endsWith("+"))
          valueS = valueS.substring(0, valueS.length() - 1);
        if (valueS.indexOf(',') != -1)
          valueS = valueS.substring(0, valueS.indexOf(','));
        char currencyIndicator = valueS.charAt(valueS.length() - 1);
        Thing.Currency currency;
        switch (currencyIndicator) {
        case 'D':
          currency = Thing.Currency.D;
          break;
        case 'S':
          currency = Thing.Currency.S;
          break;
        case 'K':
          currency = Thing.Currency.K;
          break;
        case 'H':
          currency = Thing.Currency.H;
          break;
        default:
          throw new IOException("Zeile " + lineNr
              + ": Ausrüstung hat falsches Währungsformat!");
        }
        int value = 0;
        try {
          value = Integer.parseInt(valueS.substring(0, valueS.length() - 1));
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr
              + ": Ausrüstung hat falschen Wert!");
        }
        String weightS = tokenizer.nextToken();
        int weight = 0;
        try {
          weight = Integer.parseInt(weightS);
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr
              + ": Ausrüstung hat falsches Gewicht!");
        }
        String category = tokenizer.nextToken();
        boolean isContainer = tokenizer.nextToken().equals("1");
        // one token remains, either 1 or 0, don't know what it means
        Thing thing = new Thing(name, new dsa.util.Optional<Integer>(value),
            currency, weight, category, false, false, isContainer);
        thing.setTradezones(tradezones);
        theThings.put(name, thing);
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

  public void loadUserDefinedThings(String fileName) throws IOException {
    File file = new File(fileName);
    if (!file.exists()) return;
    BufferedReader in = new BufferedReader(new FileReader(file));
    int lineNr = 0;
    String line = in.readLine();
    lineNr++;
    if (line == null)
      throw new IOException("Unerwartetes Dateiende in " + fileName + "!");
    int nrOfThings = 0;
    try {
      nrOfThings = Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Falsches Format der Gegenstandsanzahl in "
          + fileName + "!");
    }
    for (int i = 0; i < nrOfThings; ++i) {
      Thing thing = new Thing();
      thing.readFromStream(in, lineNr);
    }
  }

  public void writeUserDefinedThings(String fileName) throws IOException {
    ArrayList<Thing> userDefinedThings = new ArrayList<Thing>();
    for (Thing t : theThings.values()) {
      if (t.isUserDefined()) userDefinedThings.add(t);
    }
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        fileName)));
    try {
      out.println(userDefinedThings.size());
      for (Thing t : userDefinedThings)
        t.writeToStream(out, t.getName());
      out.flush();
    }
    finally {
      out.close();
    }
  }

  private Things() {
    theThings = new java.util.HashMap<String, Thing>();
  }

}

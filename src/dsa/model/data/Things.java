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
package dsa.model.data;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Things {

  private static Things instance = null;

  private java.util.HashMap<String, Thing> things;

  public static Things getInstance() {
    if (instance == null) instance = new Things();
    return instance;
  }

  public Thing getThing(String name) {
    if (things.containsKey(name))
      return things.get(name);
    else
      return null;
  }

  public void addThing(Thing thing) {
    things.put(thing.getName(), thing);
  }

  public void removeThing(String thing) {
    things.remove(thing);
  }

  public Thing[] getAllThings() {
    Thing[] allThings = new Thing[things.size()];
    return things.values().toArray(allThings);
  }

  public java.util.ArrayList<Thing> getAllWearableThings() {
    java.util.ArrayList<Thing> result = new java.util.ArrayList<Thing>();
    for (Thing thing : things.values()) {
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
    for (Thing t : things.values()) {
      if (t.getCategory() != null) categories.add(t.getCategory());
    }
    return categories;
  }

  public void loadFile(String fileName) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    int lineNr = 0;
    String line = in.readLine();
    lineNr++;
    while (line != null) {
      StringTokenizer tokenizer = new StringTokenizer(line, ";");
      if (tokenizer.countTokens() != 6)
        throw new IOException("Zeile " + lineNr
            + ": Syntaxfehler in Ausrüstung!");
      String name = tokenizer.nextToken();
      tokenizer.nextToken(); // Regionen
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
      // one token remains, either 1 or 0, don't know what it means
      Thing thing = new Thing(name, new dsa.util.Optional<Integer>(value),
          currency, weight, category, false);
      things.put(name, thing);
      line = in.readLine();
      lineNr++;
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
    for (Thing t : things.values()) {
      if (t.isUserDefined()) userDefinedThings.add(t);
    }
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        fileName)));
    try {
      out.println(userDefinedThings.size());
      for (Thing t : userDefinedThings)
        t.writeToStream(out);
      out.flush();
    }
    finally {
      out.close();
    }
  }

  private Things() {
    things = new java.util.HashMap<String, Thing>();
  }

}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import dsa.util.Optional;

public class Thing {

  String name;

  public enum Currency {
    D, S, H, K
  };

  private Optional<Integer> value;

  private Currency currency;

  private int weight;

  private String category;

  private boolean isUserDefined;

  public Thing() {
    this("-", Optional.NullInt, Currency.K, 0, "", false);
  }

  public Thing(String name, Optional<Integer> value, Currency currency,
      int weight, String category, boolean userDefined) {
    this.name = name;
    this.value = value;
    this.currency = currency;
    this.weight = weight;
    this.category = category;
    this.isUserDefined = userDefined;
  }

  public Currency getCurrency() {
    return currency;
  }

  public String getName() {
    return name;
  }

  public Optional<Integer> getValue() {
    return value;
  }

  public int getWeight() {
    return weight;
  }

  public boolean isUserDefined() {
    return isUserDefined;
  }

  private static final int fileVersion = 2;

  public void writeToStream(PrintWriter out) throws IOException {
    out.println(fileVersion);
    out.println(name);
    out.println(value);
    out.println(currency.ordinal());
    out.println(weight);
    out.println(isUserDefined ? 1 : 0);
    out.println(category);
    out.println("-- End of Thing --");
  }

  private static int parseInt(String line, int lineNr) throws IOException {
    try {
      int value = Integer.parseInt(line);
      return value;
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": " + line
          + " ist keine Zahl!");
    }
  }

  private static void testEmpty(String s) throws IOException {
    if (s == null) throw new IOException("Unerwartetes Dateiende!");
  }

  public int readFromStream(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    int version = parseInt(line, lineNr);
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    name = line;
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    if (line.trim().equals("-")) {
      value = Optional.NullInt;
    }
    else {
      value = new Optional<Integer>(parseInt(line, lineNr));
    }
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    currency = Currency.values()[parseInt(line, lineNr)];
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    weight = parseInt(line, lineNr);
    if (version > 1) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      isUserDefined = parseInt(line, lineNr) == 1;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      category = line;
    }
    else {
      isUserDefined = false;
      category = "";
    }
    do {
      line = in.readLine();
      lineNr++;
    } while (!line.equals("-- End of Thing --"));
    if (Things.getInstance().getThing(name) == null) {
      isUserDefined = true;
      String basename = getName();
      String name = basename;
      int nameAppendix = 0;
      while (Things.getInstance().getThing(name) != null) {
        nameAppendix++;
        name = basename + nameAppendix;
      }
      this.name = name;
      Things.getInstance().addThing(this);
    }
    return lineNr;
  }

  public String getCategory() {
    return category;
  }
}

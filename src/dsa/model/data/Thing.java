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

import java.util.ArrayList;

import dsa.util.Optional;

public class Thing {

  String name;

  public enum Currency {
    D, S, H, K;
    
    public String getLongName() {
      if (this == D) return "Dukaten (Mittelreich / LF)";
      else if (this == S) return "Silbertaler (Mittelreich / LF)";
      else if (this == H) return "Heller (Mittelreich / LF)";
      else return "Kreuzer (Mittelreich / LF)";
    }
  };

  private Optional<Integer> value;

  private Currency currency;

  private int weight;

  private String category;

  private boolean mIsUserDefined;
  
  private boolean mIsSingular;
  
  private boolean mIsContainer;
  
  private ArrayList<String> mTradezones;

  public Thing() {
    this("-", Optional.NULL_INT, Currency.K, 0, "", false, false, false);
  }

  public Thing(String name, Optional<Integer> value, Currency currency,
      int weight, String category, boolean userDefined, boolean isSingular, boolean isContainer) {
    this.name = name;
    this.value = value;
    this.currency = currency;
    this.weight = weight;
    this.category = category;
    this.mIsUserDefined = userDefined;
    this.mIsSingular = isSingular;
    this.mIsContainer = isContainer;
    mTradezones = new ArrayList<String>();
    for (String zone : Tradezones.getInstance().getTradezoneIDs()) {
      mTradezones.add(zone);
    }
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
    return mIsUserDefined;
  }
  
  public boolean isSingular() {
    return mIsSingular;
  }
  
  public boolean isContainer() {
    return mIsContainer;
  }
  
  public String[] getTradezones() {
    String[] result = new String[mTradezones.size()];
    return mTradezones.toArray(result);
  }

  private static final int FILE_VERSION = 5;

  public void writeToStream(PrintWriter out, String otherName) throws IOException {
    out.println(FILE_VERSION);
    out.println(otherName);
    out.println(value);
    out.println(currency.ordinal());
    out.println(weight);
    out.println(mIsUserDefined ? 1 : 0);
    out.println(category);
    out.println(mIsSingular ? 1 : 0);
    out.println(mIsContainer ? 1 : 0);
    for (String tradezone : mTradezones) {
      out.print(tradezone);
      out.print(" ");
    }
    out.println();
    out.println("-- End of Thing --");
  }

  private static int parseInt(String line, int lineNr) throws IOException {
    try {
      return Integer.parseInt(line);
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
    if (line != null && line.trim().equals("-")) {
      value = Optional.NULL_INT;
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
      mIsUserDefined = parseInt(line, lineNr) == 1;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      category = line;
    }
    else {
      mIsUserDefined = false;
      category = "";
    }
    if (version > 2) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      mIsSingular = parseInt(line, lineNr) == 1;
    }
    else
      mIsSingular = false;
    if (version > 3) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      mIsContainer = parseInt(line, lineNr) == 1;
    }
    else
      mIsContainer = false;
    if (version > 4) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      setTradezones(line);
    }
    else
      setTradezones("");
    do {
      line = in.readLine();
      lineNr++;
    } while (line != null && !"-- End of Thing --".equals(line));
    if (Things.getInstance().getThing(name) == null) {
      mIsUserDefined = true;
      String basename = getName();
      String thingName = basename;
      int nameAppendix = 0;
      while (Things.getInstance().getThing(thingName) != null) {
        nameAppendix++;
        thingName = basename + nameAppendix;
      }
      this.name = thingName;
      Things.getInstance().addThing(this);
    }
    return lineNr;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public void setValue(Optional<Integer> value) {
    this.value = value;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
  
  public void setIsSingular(boolean singular) {
    this.mIsSingular = singular;
  }
  
  public void setIsContainer(boolean container) {
    this.mIsContainer = container;
  }
  
  public void setTradezones(String tradezones) {
    mTradezones.clear();
    java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(tradezones.trim());
    while (tokenizer.hasMoreTokens()) {
      mTradezones.add(tokenizer.nextToken());
    }    
    if (mTradezones.isEmpty()) {
      for (String zone : Tradezones.getInstance().getTradezoneIDs()) {
        mTradezones.add(zone);
      }
    }
  }
}

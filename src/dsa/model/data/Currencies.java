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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Currencies {

  private Currencies() {
  }

  public static Currencies getInstance() {
    return sInstance;
  }

  private static Currencies sInstance = new Currencies();

  public void readCurrencies(String filename) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(filename));
    try {
      StringTokenizer tokenizer = null;
      String line = in.readLine();
      int lineNr = 1;
      while (line != null) {
        tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() != 3) {
          throw new IOException("Zeile " + lineNr
              + ": Syntaxfehler in der Währung");
        }
        String name = tokenizer.nextToken();
        String valueS = tokenizer.nextToken();
        String weightS = tokenizer.nextToken();
        try {
          long value = Long.parseLong(valueS);
          if (value < 0)
            throw new IOException("Zeile " + lineNr
                + ": Falscher Wert für Währung!");
          long weight = Long.parseLong(weightS);
          if (weight < 0)
            throw new IOException("Zeile " + lineNr
                + ": Falsches Gewicht für Währung!");
          names.add(name);
          values.add(value);
          weights.add(weight);
        }
        catch (NumberFormatException e) {
          throw new IOException("Zeile " + lineNr
              + ": Falscher Wert für Währung!");
        }
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

  public int changeValueIndex(int value, int oldCurrency, int newCurrency) {
    long baseValue = values.get(oldCurrency) * value;
    return (int) (baseValue / values.get(newCurrency));
  }

  public int changeValue(int value, String oldCurrency, String newCurrency) {
    return changeValueIndex(value, getIndex(oldCurrency), getIndex(newCurrency));
  }

  public String getCurrency(int index) {
    return names.get(index);
  }

  public int getIndex(String currency) {
    int index = -1;
    for (int i = 0; i < names.size(); ++i) {
      if (names.get(i).equals(currency)) {
        index = i;
        break;
      }
    }
    return index;
  }

  public String[] getCurrencyNames() {
    String[] n = new String[names.size()];
    return names.toArray(n);
  }

  public long getWeight(int index) {
    return weights.get(index);
  }

  public long getBaseValue(int index) {
    return values.get(index);
  }

  private final ArrayList<Long> weights = new ArrayList<Long>();

  private final ArrayList<String> names = new ArrayList<String>();

  private final ArrayList<Long> values = new ArrayList<Long>();
}

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
package dsa.model.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import dsa.model.characters.Property;

/**
 * 
 */
public class NormalTalent implements dsa.model.talents.NormalTalent {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetFirstProperty()
   */
  public Property getFirstProperty() {
    return property1;
  }

  public boolean canBeTested() {
    return true;
  }

  public boolean isSpell() {
    return false;
  }

  public boolean isLanguage() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetSecondProperty()
   */
  public Property getSecondProperty() {
    return property2;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetThirdProperty()
   */
  public Property getThirdProperty() {
    return property3;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#GetMaxIncreasePerStep()
   */
  public int getMaxIncreasePerStep() {
    return maxIncreasePerStep;
  }

  public NormalTalent(String name, Property p1, Property p2, Property p3,
      int mIpS) {
    this.name = name;
    this.property1 = p1;
    this.property2 = p2;
    this.property3 = p3;
    this.maxIncreasePerStep = mIpS;
  }

  private Property property1;

  private Property property2;

  private Property property3;

  private String name;

  private int maxIncreasePerStep;

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.Talent#IsFightingTalent()
   */
  public boolean isFightingTalent() {
    return false;
  }

  public int store(PrintWriter out) throws IOException {
    out.println("1"); // version
    out.println(name);
    out.println(property1.ordinal());
    out.println(property2.ordinal());
    out.println(property3.ordinal());
    out.println(maxIncreasePerStep);
    return 6 + doStore(out);
  }

  protected static final void testEmpty(String line) throws IOException {
    if (line == null || line.equals("")) {
      throw new IOException("Unerwartetes Dateiende!");
    }
  }

  protected static final int parseInt(String line, int lineNr)
      throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": " + line
          + " ist keine Zahl!");
    }
  }

  public int load(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    int version = parseInt(line, lineNr);
    if (version > 0) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      name = line;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      property1 = Property.values()[parseInt(line, lineNr)];
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      property2 = Property.values()[parseInt(line, lineNr)];
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      property3 = Property.values()[parseInt(line, lineNr)];
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      maxIncreasePerStep = parseInt(line, lineNr);
    }
    return doLoad(in, lineNr);
  }

  protected int doStore(PrintWriter out) throws IOException {
    return 0;
  }

  protected int doLoad(BufferedReader in, int lineNr) throws IOException {
    return lineNr;
  }

}

/*
 Copyright (c) 2006-2007 [Joerg Ruedenauer]
 
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
package dsa.model.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import dsa.model.characters.Property;
import dsa.model.talents.Spell;

public class SpellImpl extends NormalTalent implements Spell {

  public SpellImpl(String name, Property p1, Property p2, Property p3,
      int mIpS, String aCategory, String anOrigin) {
    this(name, p1, p2, p3, mIpS, aCategory, anOrigin, true);
  }

  SpellImpl(String name, Property p1, Property p2, Property p3, int mIpS,
      String aCategory, String anOrigin, boolean userDefined) {
    super(name, p1, p2, p3, mIpS);
    category = aCategory;
    origin = anOrigin;
    this.userDefined = userDefined;
  }

  SpellImpl() {
    super("", Property.MU, Property.MU, Property.MU, 1);
    category = "";
    origin = "";
    userDefined = true;
  }

  public boolean isSpell() {
    return true;
  }

  public String getCategory() {
    return category;
  }

  public String getOrigin() {
    return origin;
  }

  public boolean isUserDefined() {
    return userDefined;
  }

  protected int doStore(PrintWriter out) throws IOException {
    out.println(category);
    out.println(origin);
    out.println(userDefined ? "1" : "0");
    return 3;
  }

  protected int doLoad(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    category = line;
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    origin = line;
    line = in.readLine();
    lineNr++;
    testEmpty(line);
    userDefined = (parseInt(line, lineNr) == 1);
    return lineNr;
  }

  private String category;

  private String origin;

  private boolean userDefined;
}

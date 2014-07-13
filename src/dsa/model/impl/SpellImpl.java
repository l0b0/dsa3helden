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
package dsa.model.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import dsa.model.DiceSpecification;
import dsa.model.characters.Property;
import dsa.model.talents.Spell;

public class SpellImpl extends NormalTalent implements Spell {

  public SpellImpl(String name, Property p1, Property p2, Property p3,
      int mIpS, String aCategory, String anOrigin) {
    super(name, p1, p2, p3, mIpS);
    category = aCategory;
    origin = anOrigin;
    this.userDefined = true;
    mCosts = new ArrayList<ArrayList<Cost>>();
    mCosts.add(new ArrayList<Cost>());
    mCosts.get(0).add(new Cost(CostType.Custom, DiceSpecification.create(0, 6, 1), 1, ""));
    mNrOfVariants = 1;
    mVariantNames = new ArrayList<String>();
  }
  
  private static int parseInt(String s) throws IOException {
    try {
      return Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
      throw new IOException(e.getMessage());
    }
  }
  
  private static void checkToken(StringTokenizer s) throws IOException {
    if (!s.hasMoreTokens()) throw new IOException("Vorzeitiges Zeilenende");
  }
  
  private static Cost parseCost(String costTypeToken, StringTokenizer tokens) throws IOException {
    int minimum = 0;
    if (costTypeToken.charAt(0) == '-') {
      checkToken(tokens);
      minimum = parseInt(tokens.nextToken());
      costTypeToken = costTypeToken.substring(1);
    }
    CostType t = CostType.Special;
    DiceSpecification ds = null;
    String text = "";
    if (costTypeToken.equals("F")) {
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Fixed;
    }
    else if (costTypeToken.equals("C")) {
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Custom;
    }
    else if (costTypeToken.equals("M")) {
      checkToken(tokens);
      text = tokens.nextToken();
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Multiplied;
    }
    else if (costTypeToken.equals("V")) {
      checkToken(tokens);
      text = tokens.nextToken();
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Variable;
    }
    else if (costTypeToken.equals("D")) {
        checkToken(tokens);
        ds = DiceSpecification.parse(tokens.nextToken());
        t = CostType.Dice;
    }
    else if (costTypeToken.equals("Q")) {
      checkToken(tokens);
      text = tokens.nextToken();
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Quadratic;
    }
    else if (costTypeToken.equals("S")) {
      t = CostType.Step;
    }
    else if (costTypeToken.equals("f")) {
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.Permanent;
    }
    else if (costTypeToken.equals("L")) {
      checkToken(tokens);
      ds = DiceSpecification.parse(tokens.nextToken());
      t = CostType.LP;
    }
    else if (costTypeToken.equals("X")) {
      t = CostType.Special;
    }
    else throw new IOException("Unbekannter Kostentyp " + costTypeToken);
    return new Cost(t, ds, minimum, text);
  }
  

  SpellImpl(String name, Property p1, Property p2, Property p3, int mIpS,
      StringTokenizer spellAttributes, boolean userDefined) throws IOException {
    super(name, p1, p2, p3, mIpS);
    category = spellAttributes.nextToken();
    origin = spellAttributes.nextToken();
    this.userDefined = userDefined;
    mCosts = new ArrayList<ArrayList<Cost>>();
    mNrOfVariants = parseInt(spellAttributes.nextToken());
    mVariantNames = new ArrayList<String>();
    if (mNrOfVariants > 1) {
      for (int i = 0; i < mNrOfVariants; ++i) {
        checkToken(spellAttributes);
        mVariantNames.add(spellAttributes.nextToken());
      }
    }
    for (int i = 0; i < mNrOfVariants; ++i) {
      ArrayList<Cost> variantCosts = new ArrayList<Cost>();
      checkToken(spellAttributes);
      String typeSpecifiers = spellAttributes.nextToken();
      StringTokenizer typeTokenizer = new StringTokenizer(typeSpecifiers, "+");
      int nrOfCosts = typeTokenizer.countTokens();
      for (int j = 0; j < nrOfCosts; ++j) {
        checkToken(typeTokenizer);
        variantCosts.add(parseCost(typeTokenizer.nextToken(), spellAttributes));
      }
      mCosts.add(variantCosts);
    }
  }

  SpellImpl() {
    super("", Property.MU, Property.MU, Property.MU, 1);
    category = "";
    origin = "";
    userDefined = true;
    mCosts = new ArrayList<ArrayList<Cost>>();
    mCosts.add(new ArrayList<Cost>());
    mNrOfVariants = 1;
    mVariantNames = new ArrayList<String>();
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
  
  public int getNrOfVariants() {
    return mNrOfVariants;
  }
  
  public String getVariantName(int variant) {
    if (variant < 0 || variant >= mVariantNames.size()) {
      return "";
    }
    else return mVariantNames.get(variant);
  }
  
  public List<Cost> getCosts(int variant) {
    if (variant < 0 || variant >= mCosts.size()) {
      return new ArrayList<Cost>();
    }
    return mCosts.get(variant);
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
    mCosts.get(0).add(new Cost(CostType.Custom, DiceSpecification.parse("1"), 1, ""));
    return lineNr;
  }

  private String category;

  private String origin;

  private boolean userDefined;
  
  private ArrayList<ArrayList<Cost>> mCosts;
  
  private int mNrOfVariants;
  
  private ArrayList<String> mVariantNames;
}

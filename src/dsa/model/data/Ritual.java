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

import dsa.model.DiceSpecification;
import dsa.model.characters.Property;

public class Ritual {

  private final String name;

  private final boolean mIsKnownAtStart;

  private final String requirement;

  public static class TestData {
    Property p1;

    Property p2;

    Property p3;

    int defaultModifier;

    boolean mHasHalfStepLess;

    public int getDefaultModifier() {
      return defaultModifier;
    }

    public boolean hasHalfStepLess() {
      return mHasHalfStepLess;
    }

    public Property getP1() {
      return p1;
    }

    public Property getP2() {
      return p2;
    }

    public Property getP3() {
      return p3;
    }
  }

  public static class LearningTestData extends TestData {
    DiceSpecification ap;

    DiceSpecification permanentAP;

    DiceSpecification le;

    DiceSpecification permanentLE;

    public DiceSpecification getAp() {
      return ap;
    }

    public DiceSpecification getLe() {
      return le;
    }

    public DiceSpecification getPermanentAP() {
      return permanentAP;
    }

    public DiceSpecification getPermanentLE() {
      return permanentLE;
    }
  }

  private final TestData testData;

  private final LearningTestData learningTestData;

  Ritual(String n, boolean kas, String r, TestData td, LearningTestData ltd) {
    name = n;
    mIsKnownAtStart = kas;
    requirement = r;
    testData = td;
    learningTestData = ltd;
  }

  public boolean isKnownAtStart() {
    return mIsKnownAtStart;
  }

  public String getName() {
    return name;
  }

  public String getRequirement() {
    return requirement;
  }

  public TestData getTestData() {
    return testData;
  }

  public LearningTestData getLearningTestData() {
    return learningTestData;
  }

}

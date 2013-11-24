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

import dsa.model.DiceSpecification;
import dsa.model.characters.Property;

public class Ritual {

  private String name;

  private boolean isKnownAtStart;

  private String requirement;

  public static class TestData {
    public Property p1;

    public Property p2;

    public Property p3;

    public int defaultModifier;

    public boolean hasHalfStepLess;
  }

  public static class LearningTestData extends TestData {
    public DiceSpecification ap;

    public DiceSpecification permanentAP;

    public DiceSpecification le;

    public DiceSpecification permanentLE;
  }

  private TestData testData;

  private LearningTestData learningTestData;

  Ritual(String n, boolean kas, String r, TestData td, LearningTestData ltd) {
    name = n;
    isKnownAtStart = kas;
    requirement = r;
    testData = td;
    learningTestData = ltd;
  }

  public boolean isKnownAtStart() {
    return isKnownAtStart;
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

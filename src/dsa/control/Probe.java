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
package dsa.control;

public class Probe {

  public static final int DAEMONENPECH = 1001;

  public static final int PATZER = 1002;

  public static final int FEHLSCHLAG = 1003;

  public static final int GELUNGEN = 1004;

  public static final int PERFEKT = 1005;

  public static final int GOETTERGLUECK = 1006;

  public Probe() {
    firstProperty = 10;
    secondProperty = 10;
    thirdProperty = 10;
    modifier = 0;
    skill = 0;
  }

  public void setFirstProperty(int value) {
    if (value < 8 || value > 21) throw new IllegalArgumentException();
    firstProperty = value;
  }

  public void setSecondProperty(int value) {
    if (value < 8 || value > 21) throw new IllegalArgumentException();
    secondProperty = value;
  }

  public void setThirdProperty(int value) {
    if (value < 8 || value > 21) throw new IllegalArgumentException();
    thirdProperty = value;
  }

  public void setModifier(int value) {
    modifier = value;
  }

  public void setSkill(int value) {
    if (skill < -20 || skill > 18) throw new IllegalArgumentException();
    skill = value;
  }

  public int getFirstProperty() {
    return firstProperty;
  }

  public int getSecondProperty() {
    return secondProperty;
  }

  public int getThirdProperty() {
    return thirdProperty;
  }

  public int getModifier() {
    return modifier;
  }

  public int getSkill() {
    return skill;
  }

  public boolean performTest(int firstThrow, int secondThrow, int thirdThrow) {
    int outcome = performDetailedTest(firstThrow, secondThrow, thirdThrow);
    return (outcome >= 0 && outcome != FEHLSCHLAG && outcome != PATZER && outcome != DAEMONENPECH);
  }

  /**
   * Falls gelungen, aber hoechstens eine 1, wird zurueckgegeben, wieviel vom
   * Modifier noch uebrig ist.
   * 
   * @param firstThrow
   * @param secondThrow
   * @param thirdThrow
   * @return
   */
  public int performDetailedTest(int firstThrow, int secondThrow, int thirdThrow) {
    if (firstThrow < 1 || firstThrow > 20)
      throw new IllegalArgumentException();
    if (secondThrow < 1 || secondThrow > 20)
      throw new IllegalArgumentException();
    if (thirdThrow < 1 || thirdThrow > 20)
      throw new IllegalArgumentException();
    if (firstThrow == 1 && secondThrow == 1 && thirdThrow == 1) {
      return GOETTERGLUECK;
    }
    if (firstThrow == 20 && secondThrow == 20 && thirdThrow == 20) {
      return DAEMONENPECH;
    }
    if ((firstThrow == 1 && secondThrow == 1)
        || (firstThrow == 1 && thirdThrow == 1)
        || (secondThrow == 1 && thirdThrow == 1)) {
      return PERFEKT;
    }
    if ((firstThrow == 20 && secondThrow == 20)
        || (firstThrow == 20 && thirdThrow == 20)
        || (secondThrow == 20 && thirdThrow == 20)) {
      return PATZER;
    }
    int mod = modifier - skill;
    if (firstThrow < firstProperty) {
      if (mod > 0) mod = Math.max(0, mod - (firstProperty - firstThrow));
    }
    else if (firstThrow > firstProperty) {
      if (mod > (firstProperty - firstThrow))
        return FEHLSCHLAG;
      else
        mod = Math.min(0, mod + (firstThrow - firstProperty));
    }
    if (secondThrow < secondProperty) {
      if (mod > 0) mod = Math.max(0, mod - (secondProperty - secondThrow));
    }
    else if (secondThrow > secondProperty) {
      if (mod > (secondProperty - secondThrow))
        return FEHLSCHLAG;
      else
        mod = Math.min(0, mod + (secondThrow - secondProperty));
    }
    if (thirdThrow < thirdProperty) {
      if (mod > 0) mod = Math.max(0, mod - (thirdProperty - thirdThrow));
    }
    else if (thirdThrow > thirdProperty) {
      if (mod > (thirdProperty - thirdThrow))
        return FEHLSCHLAG;
      else
        mod = Math.min(0, mod + (thirdThrow - thirdProperty));
    }
    if ((modifier - skill) <= 0)
      return -mod;
    else
      return (mod == 0) ? 0 : FEHLSCHLAG;
  }

  private int firstProperty;

  private int secondProperty;

  private int thirdProperty;

  private int modifier;

  private int skill;
}

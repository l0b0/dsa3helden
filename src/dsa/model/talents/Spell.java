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
package dsa.model.talents;

import java.util.List;

import dsa.model.DiceSpecification;

/**
 * 
 */
public interface Spell extends NormalTalent {

  String getCategory();

  String getOrigin();
  
  boolean isUserDefined();
  
  int getNrOfVariants();
  
  String getVariantName(int variant);
  
  enum CostType {
    Fixed,
    Variable,
    Custom,
    Multiplied,
    Quadratic,
    Step,
    Permanent,
    LP,
    Special
  }
  
  public static class Cost {
    public CostType getCostType() { return mCostType; }
    public DiceSpecification getCost() { return mDiceSpecification; }
    public int getMinimumCost() { return mMinimum; }
    public String getText() { return mText; }
    
    public Cost(CostType t, DiceSpecification ds, int minimum, String text) {
      mCostType = t;
      if (ds != null) {
        assert(t != CostType.Step && t != CostType.Special);
        mDiceSpecification = ds;
      }
      else {
        assert(t == CostType.Step || t == CostType.Special);
        mDiceSpecification = DiceSpecification.create(0, 6, 0);
      }
      mMinimum = minimum;
      mText = text;
      if (!mText.equals("")) {
        assert(t == CostType.Variable || t == CostType.Multiplied || t == CostType.Quadratic);
      }
      else {
        assert(t != CostType.Variable && t != CostType.Multiplied && t != CostType.Quadratic);
      }
    }
    
    private CostType mCostType;
    private DiceSpecification mDiceSpecification;
    private int mMinimum;
    private String mText;
  }
  
  List<Cost> getCosts(int variant);

}

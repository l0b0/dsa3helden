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
package dsa.model;

import java.io.File;

import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.FightingTalent;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;

/**
 * 
 */
public abstract class DataFactory {

  public static DataFactory getInstance() {
    return sFactory;
  }

  public abstract NormalTalent createNormalTalent(String name, Property p1,
      Property p2, Property p3);

  public abstract NormalTalent createNormalTalent(String name, Property p1,
      Property p2, Property p3, int increases);

  public abstract FightingTalent createFightingTalent(String name, int beMinus);

  public abstract FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus);

  public abstract FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus, int increases);

  public abstract Talent createOtherTalent(String name, int increases);

  public abstract Spell createSpell(String name, Property p1, Property p2,
      Property p3, String category, String origin);

  public abstract Spell createUserDefinedSpell(String name, Property p1,
      Property p2, Property p3, String category, String origin);

  public abstract Spell createSpellForLoading();

  public abstract Hero createHero();

  public abstract Hero createHero(Hero prototype);

  public abstract Hero createHeroFromWiege(java.io.File file)
      throws java.io.IOException;

  public static void setFactory(DataFactory factory) {
    sFactory = factory;
  }

  private static DataFactory sFactory = null;

  /**
   * 
   * @param file
   * @return
   */
  public abstract Hero createHeroFromFile(File file) throws java.io.IOException;
}

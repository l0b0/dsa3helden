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

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import dsa.model.DataFactory;
import dsa.model.characters.Hero;
import dsa.model.talents.Talent;

/**
 * 
 */
public class DataFactoryImpl extends DataFactory {

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateTalent(java.lang.String, , , )
   */
  public dsa.model.talents.NormalTalent createNormalTalent(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3) {
    return new NormalTalent(arg0, arg1, arg2, arg3, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHero()
   */
  public Hero createHero() {
    return new HeroImpl();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHero(dsa.data.Hero)
   */
  public Hero createHero(Hero prototype) {
    if (prototype instanceof HeroImpl) {
      try {
        return (Hero) ((HeroImpl) prototype).clone();
      }
      catch (CloneNotSupportedException e) {
        return null;
      }
    }
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHeroFromWiege(java.io.File)
   */
  public Hero createHeroFromWiege(File file) throws java.io.IOException {
    return WiegeImporter.importFromWiege(file);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateTalent(java.lang.String, , , , int)
   */
  public dsa.model.talents.NormalTalent createNormalTalent(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, int arg4) {
    return new NormalTalent(arg0, arg1, arg2, arg3, arg4);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateHeroFromFile(java.io.File)
   */
  public Hero createHeroFromFile(File file) throws IOException {
    HeroImpl hero = new HeroImpl();
    hero.readFromFile(file);
    return hero;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateFightingTalent(java.lang.String, boolean)
   */
  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      int beMinus) {
    return new FightingTalent(name, false, beMinus, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateFightingTalent(java.lang.String, boolean,
   *      int)
   */
  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus) {
    return new FightingTalent(name, projectile, beMinus, 1);
  }

  public dsa.model.talents.FightingTalent createFightingTalent(String name,
      boolean projectile, int beMinus, int increases) {
    return new FightingTalent(name, projectile, beMinus, increases);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.DataFactory#CreateSpell(java.lang.String, , , )
   */
  public dsa.model.talents.Spell createSpell(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, StringTokenizer spellAttributes) throws IOException {
    return new SpellImpl(arg0, arg1, arg2, arg3, 1, spellAttributes, false);
  }

  public dsa.model.talents.Spell createUserDefinedSpell(String arg0,
      dsa.model.characters.Property arg1, dsa.model.characters.Property arg2,
      dsa.model.characters.Property arg3, String category, String origin) {
    return new SpellImpl(arg0, arg1, arg2, arg3, 1, category, origin);
  }

  @Override
  public Talent createOtherTalent(String name, int increases) {
    return new SpecialTalent(name, increases);
  }

  @Override
  public dsa.model.talents.Spell createSpellForLoading() {
    return new SpellImpl();
  }

}

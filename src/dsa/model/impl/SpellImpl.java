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

import dsa.model.characters.Property;
import dsa.model.talents.Spell;

public class SpellImpl extends NormalTalent implements Spell {

  public SpellImpl(String name, Property p1, Property p2, Property p3,
      int mIpS, String aCategory, String anOrigin) {
    super(name, p1, p2, p3, mIpS, true);
    category = aCategory;
    origin = anOrigin;
  }

  public String getCategory() {
    return category;
  }

  public String getOrigin() {
    return origin;
  }

  private String category;

  private String origin;

}

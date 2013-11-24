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
package dsa.control;

import dsa.model.characters.Group;
import dsa.model.data.Armour;
import dsa.model.data.Cloth;
import dsa.model.data.Clothes;

public class ClothesBE {

	public static int getBE(Armour armour) {
		String armourName = armour.getName();
		if (armourName.contains("kleidung") || armourName.contains("Kleidung")) {
			if (!armourName.contains("Wattierte")) {
				switch (Group.getInstance().getOptions().getClothesBE()) {
				case Normal:
					return armour.getBE();
				case Lower:
					return armour.getBE() - 1;
				case Items:
					return getCurrentClothBE(armour);
			    default:
			    	return armour.getBE();
				}
			}
		}
		return armour.getBE();
	}
	
	private static int getCurrentClothBE(Armour armour) {
		if (Group.getInstance().getActiveHero() == null)
			return armour.getBE();
		int sum = 0;
		for (String cloth : Group.getInstance().getActiveHero().getClothes()) {
			Cloth c = Clothes.getInstance().getCloth(cloth);
			if (c != null) {
				sum += c.getBE();
			}
		}
		return sum;
	}
}

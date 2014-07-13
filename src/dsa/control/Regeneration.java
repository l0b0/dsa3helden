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

import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;

public class Regeneration {

	public enum RegenerationType { Normal, Halfed, Changed }
	
	public static class RegenerationOptions {
		private RegenerationType regType;
		private int regChange;
		private boolean wholeGroup;

		public RegenerationOptions(RegenerationType t, int c, boolean g) {
			regType = t;
			regChange = c;
			wholeGroup = g;
		}

		public RegenerationType getRegenerationType() {
			return regType;
		}

		public int getRegenerationChange() {
			return regChange;
		}

		public boolean regenerateWholeGroup() {
			return wholeGroup;
		}
	}
	
	private static String getIndent(RegenerationOptions options)
	{
		final String indent = "  ";
		return options.regenerateWholeGroup() ? indent : "";
	}
	
	private static int changeRegenValue(int value, RegenerationOptions options)
	{
		if (options.getRegenerationType() == RegenerationType.Halfed)
		{
			return (int)Math.round((double)value / 2.0);
		}
		else if (options.getRegenerationType() == RegenerationType.Changed)
		{
			return Math.max(0, value + options.getRegenerationChange());
		}
		else
		{
			return value;
		}
	}

	public static String regenerate(Hero hero, RegenerationOptions options) {
		int missingLE = hero.getDefaultEnergy(Energy.LE)
				- hero.getCurrentEnergy(Energy.LE);
		final String notNecessary = getIndent(options) + "Keine Regeneration notwendig.";
		if (hero.isMagicDilletant()) {
			int missingAE = hero.getDefaultEnergy(Energy.AE)
					- hero.getCurrentEnergy(Energy.AE);
			if (missingAE <= 0 && missingLE <= 0)
				return notNecessary;
			int aeChange = 0;
			if (missingAE > 0
					&& missingLE < hero.getDefaultEnergy(Energy.LE) / 2) {
				hero.changeCurrentEnergy(Energy.AE, 1);
				aeChange = 1;
			}
			int value = Dice.roll(6);
			value = changeRegenValue(value, options);
			value = Math.min(value, missingLE);
			hero.changeCurrentEnergy(Energy.LE, value);
			String result = getIndent(options) + "LE: " + value + " Punkte regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.LE);
			if (hero.getCurrentEnergy(Energy.LE) == hero.getDefaultEnergy(Energy.LE)) 
				result += " (voll)";
			result += "\n" + getIndent(options) + "AE: "
					+ ((aeChange > 0) ? "1 Punkt" : "0 Punkte")
					+ " regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.AE);
			if (hero.getCurrentEnergy(Energy.AE) == hero.getDefaultEnergy(Energy.AE))
				result += " (voll)";
			return result;
			
		} else if (hero.hasEnergy(Energy.AE)
				&& hero.getCurrentEnergy(Energy.AE) < hero
						.getDefaultEnergy(Energy.AE)) {
			int missingAE = hero.getDefaultEnergy(Energy.AE)
					- hero.getCurrentEnergy(Energy.AE);
			boolean hasBonus = Group.getInstance().getOptions()
					.hasFastAERegeneration();
			int bonusRegeneration = hasBonus ? (int) Math
					.round((double) missingAE / 10.0) : 0;
			if (missingLE > 0) {
				int first = Dice.roll(6);
				int second = Dice.roll(6);
				int aeRoll = second;
				if (first < second) {
					int temp = first;
					first = second;
					second = temp;
				}
				first = changeRegenValue(first, options);
				second += bonusRegeneration;
				second = changeRegenValue(second, options);
				first = Math.min(first, missingLE);
				second = Math.min(second, missingAE);
				hero.changeCurrentEnergy(Energy.LE, first);
				hero.changeCurrentEnergy(Energy.AE, second);
				String text = getIndent(options) + "LE: " + first + " Punkte regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.LE);
				if (hero.getCurrentEnergy(Energy.LE) == hero.getDefaultEnergy(Energy.LE)) 
					text += " (voll)";
				text += "\n";
				text += getIndent(options) + "AE: " + second + " Punkte regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.AE);
				if (hero.getCurrentEnergy(Energy.AE) == hero.getDefaultEnergy(Energy.AE))
					text += " (voll)";
				if (hasBonus) {
					text += "\n" + getIndent(options) + "  (Gewürfelt: " + aeRoll + ", Bonus: "
							+ bonusRegeneration + ")";
				}
				return text;
			} else {
				if (missingAE <= 0)
					return notNecessary;
				int aeRoll = Dice.roll(6);
				int value = aeRoll + bonusRegeneration;
				value = changeRegenValue(value, options);
				value = Math.min(value, missingAE);
				hero.changeCurrentEnergy(Energy.AE, value);
				String text = getIndent(options) + "AE: " + value + " Punkte regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.AE);
				if (hero.getCurrentEnergy(Energy.AE) == hero.getDefaultEnergy(Energy.AE))
					text += " (voll)";
				if (hasBonus) {
					text += "\n" + getIndent(options) + "  (Gewürfelt: " + aeRoll + ", Bonus: "
							+ bonusRegeneration + ")";
				}
				return text;
			}
		} else {
			if (missingLE <= 0)
				return notNecessary;
			int value = Dice.roll(6);
			value = changeRegenValue(value, options);
			value = Math.min(value, missingLE);
			hero.changeCurrentEnergy(Energy.LE, value);
			String result = getIndent(options) + "LE: " + value + " Punkte regeneriert; jetzt bei " + hero.getCurrentEnergy(Energy.LE);
			if (hero.getCurrentEnergy(Energy.LE) == hero.getDefaultEnergy(Energy.LE)) 
				result += " (voll)";
			return result;
		}

	}
}

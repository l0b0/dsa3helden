/*
 Copyright (c) 2006-2013 [Joerg Ruedenauer]
 
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
package dsa.remote;

import java.util.HashMap;
import java.util.LinkedList;

public class RemoteFight {
	
	public class Attack {
		private int quality;
		private int tp;
		private boolean isProjectile;
		
		public Attack(int quality, int tp, boolean isProjectile) {
			this.quality = quality;
			this.tp = tp;
			this.isProjectile = isProjectile;
		}
		
		public int getQuality() {
			return quality;
		}
		
		public int getTP() {
			return tp;
		}
		
		public boolean isProjectile() {
			return isProjectile;
		}
	}
	
	private HashMap<String, LinkedList<Attack>> receivedAttacksByHeroes = new HashMap<String, LinkedList<Attack>>();
	private HashMap<String, LinkedList<Attack>> receivedAttacksAgainstHeroes = new HashMap<String, LinkedList<Attack>>();
	
	void attackReceivedByHero(String name, int quality, int tp, boolean isProjectile) {
		if (!receivedAttacksByHeroes.containsKey(name)) {
			receivedAttacksByHeroes.put(name,  new LinkedList<Attack>());
		}
		receivedAttacksByHeroes.get(name).addLast(new Attack(quality, tp, isProjectile));
	}
	
	void attackReceivedAgainstHero(String name, int quality, int tp, boolean isProjectile) {
		if (!receivedAttacksAgainstHeroes.containsKey(name)) {
			receivedAttacksAgainstHeroes.put(name,  new LinkedList<Attack>());
		}
		receivedAttacksAgainstHeroes.get(name).addLast(new Attack(quality, tp, isProjectile));
	}
	
	Attack getLastReceivedAttackByHero(String name) {
		if (receivedAttacksByHeroes.containsKey(name)) {
			if (!receivedAttacksByHeroes.get(name).isEmpty()) {
				return receivedAttacksByHeroes.get(name).getFirst();
			}
		}
		return null;
	}

	Attack getLastReceivedAttackAgainstHero(String name) {
		if (receivedAttacksAgainstHeroes.containsKey(name)) {
			if (!receivedAttacksAgainstHeroes.get(name).isEmpty()) {
				return receivedAttacksAgainstHeroes.get(name).getFirst();
			}
		}
		return null;
	}
	
	void removeLastAttackByHero(String name) {
		if (receivedAttacksByHeroes.containsKey(name)) {
			if (!receivedAttacksByHeroes.get(name).isEmpty()) {
				receivedAttacksByHeroes.get(name).removeFirst();
			}
		}
	}
	
	void removeLastAttackAgainstHero(String name) {
		if (receivedAttacksAgainstHeroes.containsKey(name)) {
			if (!receivedAttacksAgainstHeroes.get(name).isEmpty()) {
				receivedAttacksAgainstHeroes.get(name).removeFirst();
			}
		}
	}
	
	void removeAllAttacksByHero(String name) {
		if (receivedAttacksByHeroes.containsKey(name)) {
			receivedAttacksByHeroes.get(name).clear();
		}
	}
	
	void removeAllAttacksAgainstHero(String name) {
		if (receivedAttacksAgainstHeroes.containsKey(name)) {
			receivedAttacksAgainstHeroes.get(name).clear();
		}
	}

	void clear() {
		receivedAttacksByHeroes.clear();
		receivedAttacksAgainstHeroes.clear();
	}
}

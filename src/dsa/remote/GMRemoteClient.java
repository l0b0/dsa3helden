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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

import dsa.model.DataFactory;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.GroupObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Hero.DerivedValue;
import dsa.model.characters.Property;
import dsa.model.data.Opponent;
import dsa.remote.IServer.FightProperty;
import dsa.remote.IServer.FightPropertyChange;
import dsa.remote.IServer.HeroAddition;
import dsa.remote.IServer.HeroEnergyUpdate;
import dsa.remote.IServer.HeroHit;
import dsa.remote.IServer.HeroMeleeAttack;
import dsa.remote.IServer.HeroNameChange;
import dsa.remote.IServer.HeroProbe;
import dsa.remote.IServer.HeroProjectileAttack;
import dsa.remote.IServer.HeroPropertyUpdate;
import dsa.remote.IServer.HeroRegeneration;
import dsa.remote.IServer.HeroRemoval;
import dsa.remote.IServer.HeroUpdate;
import dsa.remote.IServer.OpponentMeleeAttack;
import dsa.remote.IServer.OpponentProjectileAttack;
import dsa.remote.IServer.Parade;
import dsa.remote.IServer.RemoteUpdate;
import dsa.remote.IServer.WeaponChange;
import dsa.util.Strings;

class GMRemoteClient extends RemoteClient implements IServer.GMUpdateVisitor, CharacterObserver, GroupObserver {

	@Override
	protected boolean isMaster() {
		return true;
	}

	private HashSet<Hero> mHeroesOnline = new HashSet<Hero>();
	private HashSet<String> mHeroNamesOnline = new HashSet<String>();
	
	@Override
	protected void onConnect() {
		if (mActiveHero != null) {
			mActiveHero.addHeroObserver(this);
		}
		Group.getInstance().addObserver(this);
	}
	
	@Override
	protected void onConnectionLost() {
		mHeroesOnline.clear();
		mHeroNamesOnline.clear();
		if (mActiveHero != null) {
			mActiveHero.removeHeroObserver(this);
		}
		Group.getInstance().removeObserver(this);
	}
	
	@Override
	protected void onDisconnect() {
		mHeroesOnline.clear();
		mHeroNamesOnline.clear();		
		if (mActiveHero != null) {
			mActiveHero.removeHeroObserver(this);
		}
		Group.getInstance().removeObserver(this);
	}

	@Override
	protected void processUpdates(ArrayList<RemoteUpdate> updates) {
		mListenForChanges = false;
		for (RemoteUpdate update : updates) {
			update.visitByGM(this);
		}
		mListenForChanges = true;
	}

	public void informPlayerOfProbe(Hero hero, String probeResult, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		try {
			getServer().informPlayerOfProbe(hero.getName(), probeResult, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}		
	}
	
	public void informPlayerOfRegeneration(Hero hero, String text, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		try {
			getServer().informPlayerOfRegeneration(hero.getName(), text, hero.getCurrentEnergy(Energy.LE),
					hero.getCurrentEnergy(Energy.AE), hero.getCurrentEnergy(Energy.KE), informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}
	
	public void informPlayerOfHeroAT(Hero hero, String text, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"AT von " + Strings.firstWord(hero.getName()) + ": " + text);
		}
		try {
			getServer().informPlayerOfHeroAttack(hero.getName(), text, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}
	
	public void informPlayerOfHeroProjectileAT(Hero hero, String text, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Fernkampf-AT von " + Strings.firstWord(hero.getName()) + ": " + text);
		}
		try {
			getServer().informPlayerOfHeroProjectileAttack(hero.getName(), text, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informPlayerOfHeroPA(Hero hero, String text, boolean success, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"PA von " + Strings.firstWord(hero.getName()) + ": " + text);
		}
		try {
			getServer().informPlayerOfHeroParade(hero.getName(), text, success, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informPlayerOfHeroHit(Hero hero, String text, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hero.getName()) + " wird getroffen" + text);
		}
		try {
			getServer().informPlayerOfHeroHit(hero.getName(), text, hero.getCurrentEnergy(Energy.LE), 
					hero.getCurrentEnergy(Energy.AU), informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}								
	}
	
	public void informPlayerOfOpponentAT(Hero hero, Opponent opponent, String text, int quality, boolean hit, int tp, boolean isWeaponLess, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					opponent != null ? 
							"AT von " + opponent.getName() + " auf " + Strings.firstWord(hero.getName()) + ": " + text :
							"AT auf " + Strings.firstWord(hero.getName()) + ": " + text
							);
		}
		try {
			getServer().informPlayerOfOpponentAttack(hero.getName(), opponent != null ? opponent.getName() : "", text, quality, hit, tp, isWeaponLess, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}								
	}
	
	public void informPlayerOfOpponentProjectileAT(Hero hero, Opponent opponent, String text, boolean hit, int tp, boolean informOtherPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					opponent != null ? 
							"Fernkampf-AT von " + opponent.getName() + " auf " + Strings.firstWord(hero.getName()) + ": " + text :
							"Fernkampf-AT auf " + Strings.firstWord(hero.getName()) + ": " + text
							);
		}
		try {
			getServer().informPlayerOfOpponentProjectileAttack(hero.getName(), opponent != null ? opponent.getName() : "", text, hit, tp, informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}								
	}
	
	public void informPlayerOfOpponentPA(Hero hero, Opponent opponent, String text, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"PA von " + opponent.getName() + ": " + text);
		}
		try {
			getServer().informPlayerOfOpponentParade(hero.getName(), opponent != null ? opponent.getName() : "", text, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}										
	}
	
	public void informPlayerOfFightPropertyChange(Hero hero, FightProperty fp, boolean informAllPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		try {
			int newValue = 0;
			switch (fp) {
			case atBonus1:
				newValue = hero.getAT1Bonus();
				break;
			case atBonus2:
				newValue = hero.getAT2Bonus();
				break;
			case dazed:
				newValue = hero.isDazed() ? 1 : 0;
				break;
			case stumbled:
				newValue = hero.hasStumbled() ? 1 : 0;
				break;
			case grounded:
				newValue = hero.isGrounded() ? 1 : 0;
				break;
			case markers:
				newValue = hero.getExtraMarkers();
				break;
			default:
				return;
			}
			getServer().informPlayerOfFightPropertyChange(hero.getName(), fp, newValue, informAllPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}										
	}
	
	public void informPlayerOfWeaponChange(Hero hero) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		try {
			getServer().informPlayerOfWeaponChange(hero.getName(), hero.getFightMode(), 
					hero.getFirstHandWeapon(), hero.getSecondHandItem(), true);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}										
	}

	@Override
	public void visitHeroProbe(HeroProbe hp) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game, "Probe von " + Strings.firstWord(hp.getHeroName()) + ": " + hp.getResult());
		}
	}

	@Override
	public void visitHeroEnergyUpdate(HeroEnergyUpdate heu) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game, 
					Strings.firstWord(heu.getHeroName()) + ": " + heu.getEnergy() + " jetzt bei " + heu.getValue());
		}
		Hero hero = getHero(heu.getHeroName());
		if (hero != null) {
			mListenForChanges = false;
			hero.setCurrentEnergy(heu.getEnergy(), heu.getValue());
			mListenForChanges = true;
		}
	}

	@Override
	public void visitHeroPropertyUpdate(HeroPropertyUpdate hpu) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hpu.getHeroName()) + ": " + hpu.getProperty() + " jetzt bei " + hpu.getValue());
		}
		Hero hero = getHero(hpu.getHeroName());
		if (hero != null) {
			mListenForChanges = false;
			hero.setCurrentProperty(hpu.getProperty(), hpu.getValue());
			mListenForChanges = true;
		}
	}
	
	@Override
	public void visitHeroRegeneration(HeroRegeneration hr) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Regeneration für " + Strings.firstWord(hr.getHeroName()) + ": " + hr.getText());
		}
		Hero hero = getHero(hr.getHeroName());
		if (hero != null) {
			mListenForChanges = false;
			hero.setCurrentEnergy(Energy.LE, hr.getLe());
			hero.setCurrentEnergy(Energy.AE, hr.getAe());
			hero.setCurrentEnergy(Energy.KE, hr.getKe());
			mListenForChanges = true;
		}
	}
	
	@Override
	public void visitHeroMeleeAttack(HeroMeleeAttack hma) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"AT von " + Strings.firstWord(hma.getHeroName()) + ": " + hma.getText());
		}
		if (getRemoteFight() != null) {
			getRemoteFight().attackReceivedByHero(hma.getHeroName(), hma.getQuality(), hma.getTP(), false);
		}
	}

	@Override
	public void visitHeroProjectileAttack(HeroProjectileAttack hpa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Fernkampf-AT von " + Strings.firstWord(hpa.getHeroName()) + ": " + hpa.getText());
		}
		if (getRemoteFight() != null && hpa.wasHit()) {
			getRemoteFight().attackReceivedByHero(hpa.getHeroName(), 0, hpa.getTP(), true);
		}
	}

	@Override
	public void visitHeroParade(Parade pa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"PA von " + Strings.firstWord(pa.getHeroName()) + ": " + pa.getText());
		}
	}
	
	@Override
	public void visitHeroHit(HeroHit hh) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hh.getHeroName()) + " wird getroffen" + hh.getText());
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hh.getHeroName()) + ": LE jetzt bei " + hh.getLe());
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hh.getHeroName()) + ": AU jetzt bei " + hh.getAu());
		}
		Hero hero = getHero(hh.getHeroName());
		if (hero != null) {
			mListenForChanges = false;
			hero.setCurrentEnergy(Energy.LE, hh.getLe());
			hero.setCurrentEnergy(Energy.AU, hh.getAu());
			mListenForChanges = true;
		}
	}

	@Override
	public void visitFightPropertyChange(FightPropertyChange fpc) {
		mListenForChanges = false;
		Hero hero = getHero(fpc.getHeroName());
		switch (fpc.getFightProperty()) {
		case atBonus1:
			if (hero != null) hero.setAT1Bonus(fpc.getNewValue());
			if (getLog() != null && hero != null) getLog().addLog(IRemoteLog.LogCategory.Game, "Bonus für erste AT von " + Strings.firstWord(fpc.getHeroName()) 
					+ " empfangen: " + fpc.getNewValue());
			break;
		case atBonus2:
			if (hero != null) hero.setAT2Bonus(fpc.getNewValue());
			if (getLog() != null && hero != null) getLog().addLog(IRemoteLog.LogCategory.Game, "Bonus für zweite AT von " + Strings.firstWord(fpc.getHeroName()) 
					+ " empfangen: " + fpc.getNewValue());
			break;
		case dazed:
			if (hero != null) hero.setDazed(fpc.getNewValue() != 0);
			if (getLog() != null) getLog().addLog(IRemoteLog.LogCategory.Game, Strings.firstWord(fpc.getHeroName()) 
					+ " ist" + (fpc.getNewValue() != 0 ? "" : " nicht mehr") + " benommen.");
			break;
		case stumbled:
			if (hero != null) hero.setHasStumbled(fpc.getNewValue() != 0);
			if (getLog() != null) getLog().addLog(IRemoteLog.LogCategory.Game, Strings.firstWord(fpc.getHeroName()) 
					+ " ist" + (fpc.getNewValue() != 0 ? "" : " nicht mehr") + " gestolpert.");
			break;
		case grounded:
			if (hero != null) hero.setGrounded(fpc.getNewValue() != 0);
			if (getLog() != null) getLog().addLog(IRemoteLog.LogCategory.Game, Strings.firstWord(fpc.getHeroName()) 
					+ " ist" + (fpc.getNewValue() != 0 ? "" : " nicht mehr") + " am Boden.");
			break;
		case markers:
			if (hero != null) hero.setExtraMarkers(fpc.getNewValue());
			if (getLog() != null) getLog().addLog(IRemoteLog.LogCategory.Game, Strings.firstWord(fpc.getHeroName()) 
					+ " hat jetzt " + fpc.getNewValue() + " Wundmarker.");
			break;
		default:
			if (getLog() != null) getLog().addLog(IRemoteLog.LogCategory.Error, "Unbekannte Kampfeigenschaft empfangen.");
		}
		mListenForChanges = true;
	}

	@Override
	public void visitWeaponChange(WeaponChange wc) {
		mListenForChanges = false;
		if (getLog() != null) {
			String log = Strings.firstWord(wc.getHeroName()) + " kämpft jetzt mit " + wc.getFirstHand();
			if (wc.getFightMode() != null && (wc.getFightMode().equals("Zwei Waffen") || wc.getFightMode().startsWith("Waffe + "))) {
				if (!wc.getSecondHand().isEmpty()) {
					log += " und " + wc.getSecondHand();
				}
			}
			getLog().addLog(IRemoteLog.LogCategory.Game, log);
		}
		Hero hero = getHero(wc.getHeroName());
		if (hero != null) {
			if (!wc.getFightMode().isEmpty())
				hero.setFightMode(wc.getFightMode());
			if (!wc.getFirstHand().isEmpty())
				hero.setFirstHandWeapon(wc.getFirstHand());
			if (!wc.getSecondHand().isEmpty())
				hero.setSecondHandItem(wc.getSecondHand());
		}
		hero.fireActiveWeaponsChanged();
		mListenForChanges = true;
	}

	@Override
	public void visitOpponentMeleeAttack(OpponentMeleeAttack oma) {
	}

	@Override
	public void visitOpponentProjectileAttack(OpponentProjectileAttack opa) {
	}

	@Override
	public void visitOpponentParade(Parade pa) {
	}

	private Hero getHero(String name) {
		for (Hero hero : Group.getInstance().getAllCharacters()) {
			if (hero.getName().equals(name))
				return hero;
		}
		return null;
	}
	
	private Hero mActiveHero = Group.getInstance().getActiveHero();

	@Override
	public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
		if (mActiveHero != null) {
			mActiveHero.removeHeroObserver(this);
		}
		mActiveHero = newCharacter;
		if (mActiveHero != null) {
			mActiveHero.addHeroObserver(this);
		}
	}

	@Override
	public void characterRemoved(Hero character) {
		if (!mListenForChanges)
			return;
		if (mHeroesOnline.contains(character)) {
			mHeroesOnline.remove(character);
			if (getRemoteFight() != null) {
				getRemoteFight().removeAllAttacksAgainstHero(character.getName());
				getRemoteFight().removeAllAttacksByHero(character.getName());
			}
		}
	}

	@Override
	public void characterAdded(Hero character) {
	}

	@Override
	public void globalLockChanged() {
	}

	@Override
	public void groupLoaded() {
	}

	@Override
	public void orderChanged() {
	}

	@Override
	public void opponentsChanged() {
	}

	@Override
	public void talentAdded(String talent) {
	}

	@Override
	public void talentRemoved(String talent) {
	}

	@Override
	public void defaultTalentChanged(String talent) {
	}

	@Override
	public void currentTalentChanged(String talent) {
	}

	@Override
	public void defaultPropertyChanged(Property property) {
	}

	@Override
	public void currentPropertyChanged(Property property) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().informPlayerOfPropertyChange(mActiveHero.getName(), property, mActiveHero.getCurrentProperty(property));
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}

	@Override
	public void defaultEnergyChanged(Energy energy) {
	}

	@Override
	public void currentEnergyChanged(Energy energy) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().informPlayerOfEnergyChange(mActiveHero.getName(), energy, mActiveHero.getCurrentEnergy(energy));
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}

	@Override
	public void stepIncreased() {
	}

	@Override
	public void increaseTriesChanged() {
	}

	@Override
	public void weightChanged() {
	}

	@Override
	public void atPADistributionChanged(String talent) {
	}

	@Override
	public void derivedValueChanged(DerivedValue dv) {
	}

	@Override
	public void thingRemoved(String thing, boolean fromWarehouse) {
	}

	@Override
	public void weaponRemoved(String weapon) {
	}

	@Override
	public void armourRemoved(String armour) {
	}

	@Override
	public void shieldRemoved(String name) {
	}

	@Override
	public void nameChanged(String oldName, String newName) {
	}

	@Override
	public void bfChanged(String item) {
	}

	@Override
	public void beModificationChanged() {
	}

	@Override
	public void thingsChanged() {
	}

	@Override
	public void activeWeaponsChanged() {
	}

	@Override
	public void fightingStateChanged() {
	}

	@Override
	public void opponentWeaponChanged(int weaponNr) {
		// TODO Later?
	}

	@Override
	public void moneyChanged() {
	}

	@Override
	public void visitHeroAddition(HeroAddition ha) {
		mHeroNamesOnline.add(ha.getHeroName());
		Hero hero = getHero(ha.getHeroName());
		if (hero != null) {
			mHeroesOnline.add(hero);
		}
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Management, "Held \"" + ha.getHeroName() + "\" ist jetzt online.");
		}
	}

	@Override
	public void visitHeroRemoval(HeroRemoval hr) {
		if (mHeroNamesOnline.contains(hr.getHeroName())) {
			mHeroNamesOnline.remove(hr.getHeroName());
			Hero hero = getHero(hr.getHeroName());
			if (hero != null && mHeroesOnline.contains(hero)) {
				mHeroesOnline.remove(hero);
			}
			if (getLog() != null) {
				getLog().addLog(IRemoteLog.LogCategory.Management, "Held \"" + hr.getHeroName() + "\" ist nicht mehr online.");
			}
			if (getRemoteFight() != null) {
				getRemoteFight().removeAllAttacksAgainstHero(hr.getHeroName());
				getRemoteFight().removeAllAttacksByHero(hr.getHeroName());
			}
		}
			
	}

	@Override
	public void visitHeroUpdate(HeroUpdate hu) {
		if (mHeroNamesOnline.contains(hu.getHeroName())) {
			Hero oldHero = getHero(hu.getHeroName());
			Hero newHero = DataFactory.getInstance().createHeroFromString(hu.getSerializedHero());
			if (newHero != null) {
				if (oldHero != null) {
					Group.getInstance().replaceCharacter(oldHero, newHero);
					mHeroesOnline.remove(oldHero);
					mHeroesOnline.add(newHero);
					if (getLog() != null) {
						getLog().addLog(IRemoteLog.LogCategory.Game, "Held \"" + Strings.firstWord(newHero.getName()) + "\" wurde aktualisiert.");
					}
				}
				else {
					Group.getInstance().addHero(newHero);
					Group.getInstance().setActiveHero(newHero);
					mHeroesOnline.add(newHero);
					if (getLog() != null) {
						getLog().addLog(IRemoteLog.LogCategory.Management, "Neuer Held \"" + newHero.getName() + "\" wurde hinzugefügt.");
					}
				}
			}
			else {
				if (getLog() != null) {
					getLog().addLog(IRemoteLog.LogCategory.Error, "Update für Held \"" + hu.getHeroName() + "\" empfangen, konnte aber nicht gelesen werden."); 
				}
			}
		}
	}

	@Override
	public void visitHeroNameChange(HeroNameChange hnc) {
		if (mHeroNamesOnline.contains(hnc.getHeroName())) {
			Hero hero = getHero(hnc.getHeroName());
			if (hero != null) {
				hero.setName(hnc.getNewName());
			}
			mHeroNamesOnline.remove(hnc.getHeroName());
			mHeroNamesOnline.add(hnc.getNewName());
			if (getLog() != null) {
				getLog().addLog(IRemoteLog.LogCategory.Management, "Held \"" + hnc.getHeroName() + "\" heißt jetzt \"" + hnc.getNewName() + "\"");
			}
		}
	}

}

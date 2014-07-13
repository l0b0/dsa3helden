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

import dsa.model.characters.CharacterObserver;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Hero.DerivedValue;
import dsa.model.characters.Property;
import dsa.remote.IServer.FightProperty;
import dsa.remote.IServer.FightPropertyChange;
import dsa.remote.IServer.HeroEnergyUpdate;
import dsa.remote.IServer.HeroHit;
import dsa.remote.IServer.HeroMeleeAttack;
import dsa.remote.IServer.HeroProbe;
import dsa.remote.IServer.HeroProjectileAttack;
import dsa.remote.IServer.HeroPropertyUpdate;
import dsa.remote.IServer.HeroRegeneration;
import dsa.remote.IServer.OpponentMeleeAttack;
import dsa.remote.IServer.OpponentProjectileAttack;
import dsa.remote.IServer.Parade;
import dsa.remote.IServer.RemoteUpdate;
import dsa.remote.IServer.WeaponChange;
import dsa.util.Strings;

class PlayerRemoteClient extends RemoteClient implements IServer.PlayerUpdateVisitor, CharacterObserver, CharactersObserver {

	@Override
	protected boolean isMaster() {
		return false;
	}
	
	private HashSet<Hero> mHeroesOnline = new HashSet<Hero>();
	
	public void connectHeroOnline(Hero hero) {
		if (mHeroesOnline.contains(hero))
			return;
		if (!isConnected())
			return;
		try {
			getServer().addHero(getClientId(), hero.getName());
			String serializedHero = hero.storeToString();
			if (serializedHero != null && !serializedHero.isEmpty()) {
				getServer().updateHero(getClientId(), hero.getName(), serializedHero);
			}
			else {
				for (Energy energy : Energy.values()) {
					getServer().informGMOfEnergyChange(getClientId(), hero.getName(), energy, hero.getCurrentEnergy(energy));
				}
				for (Property property : Property.values()) {
					getServer().informGMOfPropertyChange(getClientId(), hero.getName(), property, hero.getCurrentProperty(property));
				}
			}
			if (getLog() != null) {
				getLog().addLog(IRemoteLog.LogCategory.Management, hero.getName() + " am Server angemeldet.");
			}
			mHeroesOnline.add(hero);
			hero.addObserver(this);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}
	}
	
	private void updateHeroOnline(Hero hero) {
		if (!mHeroesOnline.contains(hero))
			return;
		if (!isConnected())
			return;
		try {
			String serializedHero = hero.storeToString();
			if (serializedHero != null && !serializedHero.isEmpty()) {
				getServer().updateHero(getClientId(), hero.getName(), serializedHero);
			}
			if (getLog() != null) {
				getLog().addLog(IRemoteLog.LogCategory.Management, hero.getName() + " am Server aktualisiert.");
			}
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}		
	}
	
	public boolean isHeroConnectedOnline(Hero hero) {
		return mHeroesOnline.contains(hero);
	}
	
	public void disconnectHeroOnline(Hero hero) {
		if (!mHeroesOnline.contains(hero))
			return;
		if (!isConnected())
			return;
		try {
			getServer().removeHero(getClientId(), hero.getName());
			if (getLog() != null) {
				getLog().addLog(IRemoteLog.LogCategory.Management, hero.getName() + " vom Server abgemeldet.");
			}
			mHeroesOnline.remove(hero);
			hero.removeObserver(this);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}
	}
	
	@Override
	protected void onConnect() {
		mActiveHero = Group.getInstance().getActiveHero();
		Group.getInstance().addObserver(this);
	}
	
	@Override
	protected void onDisconnect() {
		for (Hero hero : mHeroesOnline) {
			hero.removeObserver(this);
		}
		Group.getInstance().removeObserver(this);
		mHeroesOnline.clear();
	}
	
	@Override
	protected void onConnectionLost() {
		for (Hero hero : mHeroesOnline) {
			hero.removeObserver(this);
		}
		Group.getInstance().removeObserver(this);
		mHeroesOnline.clear();		
	}

	public void informGMOfProbe(Hero hero, String probeResult) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game, "Probe von " + Strings.firstWord(hero.getName()) + ": " + probeResult);
		}
		try {
			getServer().informGMOfProbe(getClientId(), hero.getName(), probeResult);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}		
	}
	
	public void informPlayersOfProbe(Hero hero, String probeResult) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game, "Probe von " + Strings.firstWord(hero.getName()) + ": " + probeResult);
		}
		try {
			getServer().informPlayersOfProbe(getClientId(), hero.getName(), probeResult);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}		
	
	}
	
	public void informGMOfEnergyChange(Hero hero, Energy energy, int newValue) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		try {
			getServer().informGMOfEnergyChange(getClientId(), hero.getName(), energy, newValue);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}
	
	public void informGMOfRegeneration(Hero hero, String text) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Regeneration für " + Strings.firstWord(hero.getName()) + ": " + text);
		}		
		try {
			getServer().informGMOfRegeneration(getClientId(), hero.getName(), text, hero.getCurrentEnergy(Energy.LE), 
					hero.getCurrentEnergy(Energy.AE), hero.getCurrentEnergy(Energy.KE));
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informPlayersOfRegeneration(Hero hero, String text) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Regeneration für " + Strings.firstWord(hero.getName()) + ": " + text);
		}		
		try {
			getServer().informPlayersOfRegeneration(getClientId(), hero.getName(), text, hero.getCurrentEnergy(Energy.LE), 
					hero.getCurrentEnergy(Energy.AE), hero.getCurrentEnergy(Energy.KE));
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informOfAT(Hero hero, String text, int quality, boolean hit, int tp, boolean isWeaponLess, boolean informOtherPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"AT von " + Strings.firstWord(hero.getName()) + ": " + text);
		}		
		try {
			getServer().informGMOfAttack(getClientId(), hero.getName(), text, quality, hit, tp, isWeaponLess, informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informOfProjectileAT(Hero hero, String text, boolean hit, int tp, boolean informOtherPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Fernkampf-AT von " + Strings.firstWord(hero.getName()) + ": " + text);
		}		
		try {
			getServer().informGMOfProjectileAttack(getClientId(), hero.getName(), text, hit, tp, informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informOfPA(Hero hero, String text, boolean success, boolean informOtherPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"PA von " + Strings.firstWord(hero.getName()) + ": " + text);
		}		
		try {
			getServer().informGMOfParade(getClientId(), hero.getName(), text, success, informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}
	
	public void informOfHit(Hero hero, String text, boolean informOtherPlayers) {
		if (!isConnected())
			return;
		if (!mHeroesOnline.contains(hero))
			return;
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					Strings.firstWord(hero.getName()) + " wird getroffen" + text);
		}
		try {
			getServer().informGMOfHit(getClientId(), hero.getName(), text, hero.getCurrentEnergy(Energy.LE), 
					hero.getCurrentEnergy(Energy.AU), informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
		
	}
	
	public void informOfFightPropertyChange(Hero hero, FightProperty fp, boolean informOtherPlayers) {
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
			getServer().informGMOfFightPropertyChange(getClientId(), hero.getName(), fp, newValue, informOtherPlayers);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}

	@Override
	protected void processUpdates(ArrayList<RemoteUpdate> updates) {
		mListenForChanges = false;
		for (RemoteUpdate update : updates) {
			update.visitByPlayer(this);
		}
		mListenForChanges = true;
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
	}

	@Override
	public void visitHeroProjectileAttack(HeroProjectileAttack hpa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"Fernkampf-AT von " + Strings.firstWord(hpa.getHeroName()) + ": " + hpa.getText());
		}
	}

	@Override
	public void visitHeroParade(Parade pa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					"PA von " + Strings.firstWord(pa.getHeroName()) + ": " + pa.getText());
		}
		if (!pa.wasSuccessful()) {
			if (getLog() != null) {
			}
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
	public void visitOpponentMeleeAttack(OpponentMeleeAttack oma) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					oma.getOpponentName().length() > 0 ? 
							"AT von " + oma.getOpponentName() + " auf " + Strings.firstWord(oma.getHeroName()) + ": " + oma.getText() :
							"AT auf " + Strings.firstWord(oma.getHeroName()) + ": " + oma.getText()
							);
		}
		if (getRemoteFight() != null) {
			getRemoteFight().attackReceivedAgainstHero(oma.getHeroName(), oma.getQuality(), oma.getTP(), false);
		}
	}

	@Override
	public void visitOpponentProjectileAttack(OpponentProjectileAttack opa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
					opa.getOpponentName().length() > 0 ? 
							"Fernkampf-AT von " + opa.getOpponentName() + " auf " + Strings.firstWord(opa.getHeroName()) + ": " + opa.getText() :
							"Fernakmpf-AT auf " + Strings.firstWord(opa.getHeroName()) + ": " + opa.getText()
							);
		}
		if (getRemoteFight() != null && opa.wasHit()) {
			getRemoteFight().attackReceivedAgainstHero(opa.getHeroName(), 0, opa.getTP(), true);
		}
	}

	@Override
	public void visitOpponentParade(Parade pa) {
		if (getLog() != null) {
			getLog().addLog(IRemoteLog.LogCategory.Game,
				pa.getHeroName().length() > 0 ? 
						"PA von " +  pa.getHeroName() + ": " + pa.getText() :
						"PA: " + pa.getText()
						);
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
			hero.fireActiveWeaponsChanged();
		}
		mListenForChanges = true;
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
		mActiveHero = newCharacter;
	}

	@Override
	public void characterRemoved(Hero character) {
		if (!mListenForChanges)
			return;
		if (mHeroesOnline.contains(character)) {
			disconnectHeroOnline(character);
		}
	}

	@Override
	public void characterAdded(Hero character) {
	}

	@Override
	public void globalLockChanged() {
	}

	@Override
	public void talentAdded(String talent) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void talentRemoved(String talent) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void defaultTalentChanged(String talent) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void currentTalentChanged(String talent) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void defaultPropertyChanged(Property property) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void currentPropertyChanged(Property property) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().informGMOfPropertyChange(getClientId(), mActiveHero.getName(), property, mActiveHero.getCurrentProperty(property));
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
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void currentEnergyChanged(Energy energy) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().informGMOfEnergyChange(getClientId(), mActiveHero.getName(), energy, mActiveHero.getCurrentEnergy(energy));
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
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void atPADistributionChanged(String talent) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void derivedValueChanged(DerivedValue dv) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void thingRemoved(String thing, boolean fromWarehouse) {
		// information transferred through weight change
	}

	@Override
	public void weaponRemoved(String weapon) {
		// information transferred through weight change
	}

	@Override
	public void armourRemoved(String armour) {
		// information transferred through weight change
	}

	@Override
	public void shieldRemoved(String name) {
		// information transferred through weight change
	}

	@Override
	public void nameChanged(String oldName, String newName) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().changeHeroName(getClientId(), oldName, newName);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}				
	}

	@Override
	public void bfChanged(String item) {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void beModificationChanged() {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		updateHeroOnline(mActiveHero);
	}

	@Override
	public void thingsChanged() {
		// information transferred through weight change
	}

	@Override
	public void activeWeaponsChanged() {
		if (!mListenForChanges || !isConnected())
			return;
		if (!mHeroesOnline.contains(mActiveHero))
			return;
		try {
			getServer().informGMOfWeaponChange(getClientId(), mActiveHero.getName(), mActiveHero.getFightMode(), 
					mActiveHero.getFirstHandWeapon(), mActiveHero.getSecondHandItem(), true);
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}						
	}

	@Override
	public void fightingStateChanged() {
		// information about dazed / grounded is explicitly called
	}

	@Override
	public void opponentWeaponChanged(int weaponNr) {
	}

	@Override
	public void moneyChanged() {
		// information transferred through weight change
	}

}

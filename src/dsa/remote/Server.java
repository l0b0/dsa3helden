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
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;

import dsa.model.characters.Energy;
import dsa.model.characters.Property;

public class Server implements IServer {
	
	private IRemoteLog mLog;
	private static final int GM_ID = 0;
	private boolean hasGM = false;
    private HashMap<Integer, String> mClientPlayers = new HashMap<Integer, String>();
	private HashMap<Integer, ArrayList<String>> mClientHeroes = new HashMap<Integer, ArrayList<String>>();
	private HashMap<String, Integer> mRemoteHeroes = new HashMap<String, Integer>();
	private HashMap<Integer, ArrayList<RemoteUpdate>> mUpdatesPerClient = new HashMap<Integer, ArrayList<RemoteUpdate>>();
	private HashMap<Integer, TimerTask> mTimeoutTimers = new HashMap<Integer, TimerTask>();
	
	private Timer mTimeoutTimer = new Timer("ClientTimeouts");
	
	private int mNextClientId = 1;
	
	private boolean mShutdown = false;
	
	public Server(IRemoteLog log) {
		mLog = log;
		mUpdatesPerClient.put(GM_ID, new ArrayList<RemoteUpdate>());
	}
	
	public void shutdown() {
		mClientPlayers.clear();
		mClientHeroes.clear();
		mRemoteHeroes.clear();
		mUpdatesPerClient.clear();
		mTimeoutTimers.clear();
		mTimeoutTimer.cancel();
		mTimeoutTimer.purge();
		mShutdown = true;
	}
	
	private static final int CLIENT_TIMEOUT = 30000;
	
	@Override
	public synchronized int addClient(String playerName, boolean isGM, int version) throws RemoteException,
			ServerException {
		if (version != SERVER_VERSION) 
			throw new ServerException("Ungleiche Versionen von Server und Client.");
		if (mShutdown)
			throw new ServerShutdownException();
		if (playerName == null || playerName.isEmpty())
			throw new ServerException("Client must have a player name");
		if (hasGM && isGM)
			throw new ServerException("Es ist bereits ein Meister angemeldet.");
		final int id = isGM ? GM_ID : mNextClientId;
		mClientPlayers.put(id, playerName);
		
		TimerTask task = new TimerTask() {
			public void run() {
				clientTimeOut(id);
			}
		};
		mTimeoutTimer.schedule(task, CLIENT_TIMEOUT);
		mTimeoutTimers.put(id, task);
		if (!isGM) {
			mNextClientId++;
			mClientHeroes.put(id, new ArrayList<String>());
			mUpdatesPerClient.put(id, new ArrayList<RemoteUpdate>());
		}
		else {
			hasGM = true;
			for (String name : mRemoteHeroes.keySet()) {
				mUpdatesPerClient.get(GM_ID).add(new HeroAddition(name));
			}
		}
		if (isGM)
			mLog.addLog(IRemoteLog.LogCategory.Management, "Meister angemeldet: " + playerName);
		else
			mLog.addLog(IRemoteLog.LogCategory.Management, "Neuer Spieler angemeldet: " + playerName);
		return id;
	}

	@Override
	public synchronized void removeClient(int clientId) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (mClientPlayers.containsKey(clientId)) {
			if (clientId != GM_ID) {
				for (String hero : mClientHeroes.get(clientId)) {
					mLog.addLog(IRemoteLog.LogCategory.Management, "Held " + hero + " ist nicht mehr online verbunden.");
					mRemoteHeroes.remove(hero);
					mUpdatesPerClient.get(GM_ID).add(new HeroRemoval(hero));
				}
				mClientHeroes.remove(clientId);
				mUpdatesPerClient.remove(clientId);
			}
			else {
				hasGM = false;
			}
			mLog.addLog(IRemoteLog.LogCategory.Management, "Spieler " + mClientPlayers.get(clientId) + " hat sich abgemeldet.");
			mClientPlayers.remove(clientId);
			mTimeoutTimers.get(clientId).cancel();
			mTimeoutTimers.remove(clientId);
		}
		else {
			throw new UnknownClientException();
		}
	}
	
	private synchronized void clientTimeOut(int clientId) {
		if (mClientPlayers.containsKey(clientId)) {
			if (clientId != GM_ID) {
				for (String hero : mClientHeroes.get(clientId)) {
					mLog.addLog(IRemoteLog.LogCategory.Management, "Held " + hero + " ist nicht mehr online verbunden.");
					mRemoteHeroes.remove(hero);
					mUpdatesPerClient.get(GM_ID).add(new HeroRemoval(hero));
				}
				mClientHeroes.remove(clientId);
				mUpdatesPerClient.remove(clientId);
			}
			else {
				hasGM = false;
			}
			mLog.addLog(IRemoteLog.LogCategory.Management, "Timeout für Spieler " + mClientPlayers.get(clientId) + ": wurde abgemeldet.");
			mClientPlayers.remove(clientId);
			mTimeoutTimers.get(clientId).cancel();
			mTimeoutTimers.remove(clientId);			
		}
	}
	
	private void RescheduleTimeoutTimer(final int clientId) {
		if (mTimeoutTimers.containsKey(clientId)) {
			TimerTask task = mTimeoutTimers.get(clientId); 
			task.cancel();
			task = new TimerTask() {
				public void run() {
					clientTimeOut(clientId);
				}
			};
			mTimeoutTimers.put(clientId, task);
			mTimeoutTimer.schedule(task, CLIENT_TIMEOUT);
		}		
	}
	
	@Override
	public synchronized ArrayList<RemoteUpdate> getUpdates(int clientId) throws RemoteException,
	        ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		ArrayList<RemoteUpdate> currentList = mUpdatesPerClient.get(clientId);
		mUpdatesPerClient.put(clientId, new ArrayList<RemoteUpdate>());
		RescheduleTimeoutTimer(clientId);
		return currentList;
	}
	
	@Override
	public synchronized void addHero(int clientId, String name) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (name == null || name.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (mRemoteHeroes.containsKey(name))
			throw new ServerException("Ein Held dieses Namens ist bereits angemeldet.");
		if (clientId == GM_ID)
			throw new ServerException("Der Meister kann keine Helden anmelden.");
		
		mRemoteHeroes.put(name, clientId);
		mClientHeroes.get(clientId).add(name);
		if (hasGM) {
			mUpdatesPerClient.get(GM_ID).add(new HeroAddition(name));
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Management, "Held " + name + " (Spieler " + mClientPlayers.get(clientId) + ") ist jetzt online verbunden.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void removeHero(int clientId, String name) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (name == null || name.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(name))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(name) != clientId)
			throw new ServerException("Hero is online for another player");
		mLog.addLog(IRemoteLog.LogCategory.Management, "Held " + name + " ist nicht mehr online verbunden.");
		if (hasGM) {
			mUpdatesPerClient.get(GM_ID).add(new HeroRemoval(name));
		}
		mClientHeroes.get(clientId).remove(name);
		mRemoteHeroes.remove(name);

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void updateHero(int clientId, String name, String serializedHero)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (name == null || name.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(name))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(name) != clientId)
			throw new ServerException("Hero is online for another player");
		mUpdatesPerClient.get(GM_ID).add(new HeroUpdate(name, serializedHero));

		mLog.addLog(IRemoteLog.LogCategory.Game, "Update von " + name + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}
	
	@Override
	public synchronized void changeHeroName(int clientId, String oldName, String newName)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (oldName == null || oldName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (newName == null || newName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(oldName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(oldName) != clientId)
			throw new ServerException("Hero is online for another player");
		if (mRemoteHeroes.containsKey(newName))
			throw new ServerException("Ein Held dieses Namens ist bereits angemeldet.");
		
		mClientHeroes.get(clientId).remove(oldName);
		mClientHeroes.get(clientId).add(newName);
		mRemoteHeroes.remove(oldName);
		mRemoteHeroes.put(newName, clientId);
		mUpdatesPerClient.get(GM_ID).add(new HeroNameChange(oldName, newName));
		
		mLog.addLog(IRemoteLog.LogCategory.Management, "Held \"" + oldName + "\" heißt jetzt \"" + newName + "\"");
	
		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void informGMOfProbe(int clientId, String heroName, String probeResult)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new ServerException("Unknown client");
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		mUpdatesPerClient.get(GM_ID).add(new HeroProbe(heroName, probeResult));
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "Probe von " + heroName + " nur für Meister empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void informPlayersOfProbe(int clientId, String heroName, String probeResult)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		for (int id : mClientPlayers.keySet()) {
			if (id != clientId) {
				mUpdatesPerClient.get(id).add(new HeroProbe(heroName, probeResult));
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Probe von " + heroName + " für Meister und andere Spieler empfangen.");

		RescheduleTimeoutTimer(clientId);
	}
	
	@Override
	public synchronized void informGMOfRegeneration(int clientId, String heroName, String text, int le, int ae, int ke)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		mUpdatesPerClient.get(GM_ID).add(new HeroRegeneration(heroName, text, le, ae, ke));

		mLog.addLog(IRemoteLog.LogCategory.Game, "Regeneration von " + heroName + " nur für Meister empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void informPlayersOfRegeneration(int clientId, String heroName, String text, int le, int ae, int ke)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		for (int id : mClientPlayers.keySet()) {
			if (id != clientId) {
				mUpdatesPerClient.get(id).add(new HeroRegeneration(heroName, text, le, ae, ke));
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Regeneration von " + heroName + " für Meister und andere Spieler empfangen.");

		RescheduleTimeoutTimer(clientId);
	}
	
	@Override
	public synchronized void informGMOfEnergyChange(int clientId, String heroName,
			Energy energy, int newValue) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		mUpdatesPerClient.get(GM_ID).add(new HeroEnergyUpdate(heroName, energy, newValue));

		mLog.addLog(IRemoteLog.LogCategory.Game, "Statusupdate von " + heroName + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}
	
	@Override
	public synchronized void informGMOfPropertyChange(int clientId, String heroName,
			Property property, int newValue) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		mUpdatesPerClient.get(GM_ID).add(new HeroPropertyUpdate(heroName, property, newValue));

		mLog.addLog(IRemoteLog.LogCategory.Game, "Statusupdate von " + heroName + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public synchronized void informPlayerOfProbe(String heroName, String probeResult,
			boolean informAllPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (informAllPlayers) {
			for (int clientId : mClientPlayers.keySet()) {
				mUpdatesPerClient.get(clientId).add(new HeroProbe(heroName, probeResult));
			}
		}
		else {
			int client = mRemoteHeroes.get(heroName);
			mUpdatesPerClient.get(client).add(new HeroProbe(heroName, probeResult));
			if (client != GM_ID) {
				mUpdatesPerClient.get(GM_ID).add(new HeroProbe(heroName, probeResult));
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Probe für " + heroName + " empfangen" + 
				(informAllPlayers ? " (geht auch an Mitspieler)" : "") + ".");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public synchronized void informPlayerOfRegeneration(String heroName, String text, int le, int ae, int ke,
			boolean informAllPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (informAllPlayers) {
			for (int clientId : mClientPlayers.keySet()) {
				mUpdatesPerClient.get(clientId).add(
						new HeroRegeneration(heroName, text, le, ae, ke));
			}
		} else {
			int client = mRemoteHeroes.get(heroName);
			mUpdatesPerClient.get(client).add(
					new HeroRegeneration(heroName, text, le, ae, ke));
			if (client != GM_ID) {
				mUpdatesPerClient.get(GM_ID).add(
						new HeroRegeneration(heroName, text, le, ae, ke));
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Regeneration für " + heroName
				+ " empfangen"
				+ (informAllPlayers ? " (geht auch an Mitspieler)" : "") + ".");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public synchronized void informPlayerOfEnergyChange(String heroName, Energy energy,
			int newValue) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new HeroEnergyUpdate(heroName, energy, newValue));

		mLog.addLog(IRemoteLog.LogCategory.Game, "Statusupdate für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public synchronized void informPlayerOfPropertyChange(String heroName,
			Property property, int newValue) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new HeroPropertyUpdate(heroName, property, newValue));		

		mLog.addLog(IRemoteLog.LogCategory.Game, "Statusupdate für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informGMOfAttack(int clientId, String heroName, String text,
			int quality, boolean hit, int tp, boolean isWeaponLess,
			boolean informOtherPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new HeroMeleeAttack(heroName, text, quality, hit, tp, isWeaponLess));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new HeroMeleeAttack(heroName, text, quality, hit, tp, isWeaponLess));					
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "AT von " + heroName + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informGMOfProjectileAttack(int clientId, String heroName,
			String text, boolean hit, int quality, int tp, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new HeroProjectileAttack(heroName, text, hit, quality, tp));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new HeroProjectileAttack(heroName, text, hit, quality, tp));					
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Fernkampf-AT von " + heroName + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informGMOfParade(int clientId, String heroName, String text,
			boolean success, boolean informOtherPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new Parade(heroName, text, success, true));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new Parade(heroName, text, success, true));					
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "PA von " + heroName + " empfangen.");

		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informGMOfHit(int clientId, String heroName, String text,
			int newLe, int newAu, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new HeroHit(heroName, text, newLe, newAu));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new HeroHit(heroName, text, newLe, newAu));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Treffer gegen " + heroName + " empfangen.");
		
		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informGMOfFightPropertyChange(int clientId, String heroName,
			FightProperty fp, int newValue, boolean informOtherPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new FightPropertyChange(heroName, fp, newValue));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new FightPropertyChange(heroName, fp, newValue));
				}
			}
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "Kampfstatus-Update von " + heroName + " empfangen.");
		
		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informGMOfWeaponChange(int clientId, String heroName,
			String fightMode, String firstHand, String secondHand,
			boolean informOtherPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (clientId == GM_ID)
			throw new ServerException("GM can't inform GM");
		if (!mClientPlayers.containsKey(clientId))
			throw new UnknownClientException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		if (mRemoteHeroes.get(heroName) != clientId)
			throw new ServerException("Hero is online for another player");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(GM_ID).add(new WeaponChange(heroName, fightMode, firstHand, secondHand));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != clientId) {
					mUpdatesPerClient.get(id).add(new WeaponChange(heroName, fightMode, firstHand, secondHand));
				}
			}
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "Kampfstatus-Update von " + heroName + " empfangen.");
		
		RescheduleTimeoutTimer(clientId);
	}

	@Override
	public void informPlayerOfHeroAttack(String heroName, String text,
			boolean informAllPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informAllPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new HeroMeleeAttack(heroName, text, 0, false, 0, false));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new HeroMeleeAttack(heroName, text, 0, false, 0, false));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "AT für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfOpponentAttack(String heroName,
			String opponentName, String text, int quality, boolean hit, int tp,
			boolean isWeaponLess, boolean informAllPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (opponentName == null)
			throw new ServerException("Opponent name mustn't be null");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informAllPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new OpponentMeleeAttack(opponentName, heroName, text, quality, hit, tp, isWeaponLess));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new OpponentMeleeAttack(opponentName, heroName, text, quality, hit, tp, isWeaponLess));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "AT gegen " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfHeroProjectileAttack(String heroName,
			String text, boolean informOtherPlayers) throws RemoteException,
			ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new HeroProjectileAttack(heroName, text, false, 0, 0));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new HeroProjectileAttack(heroName, text, false, 0, 0));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Fernkampf-AT für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfOpponentProjectileAttack(String heroName,
			String opponentName, String text, boolean hit, int quality, int tp, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (opponentName == null)
			throw new ServerException("Opponent name mustn't be null");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new OpponentProjectileAttack(opponentName, heroName, text, hit, quality, tp));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new OpponentProjectileAttack(opponentName, heroName, text, hit, quality, tp));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Fernkampf-AT gegen " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfHeroParade(String heroName, String text, boolean success, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new Parade(heroName, text, success, true));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new Parade(heroName, text, success, true));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "AT für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfOpponentParade(String heroName, String opponentName, String text,
			boolean informOtherPlayers) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (opponentName == null)
			throw new ServerException("Opponent name mustn't be null");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new Parade(opponentName, text, false, false));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new Parade(opponentName, text, false, false));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "AT für " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfHeroHit(String heroName, String text, int newLe,
			int newAu, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");
		
		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new HeroHit(heroName, text, newLe, newAu));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new HeroHit(heroName, text, newLe, newAu));
				}
			}
		}

		mLog.addLog(IRemoteLog.LogCategory.Game, "Treffer gegen " + heroName + " empfangen.");

		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfFightPropertyChange(String heroName,
			FightProperty fp, int newValue, boolean informOtherPlayers) 
					throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new FightPropertyChange(heroName, fp, newValue));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new FightPropertyChange(heroName, fp, newValue));
				}
			}
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "Kampfstatus-Update für " + heroName + " empfangen.");
		
		RescheduleTimeoutTimer(GM_ID);
	}

	@Override
	public void informPlayerOfWeaponChange(String heroName, String fightMode,
			String firstHand, String secondHand, boolean informOtherPlayers)
			throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		if (heroName == null || heroName.isEmpty())
			throw new ServerException("Hero name mustn't be empty");
		if (!mRemoteHeroes.containsKey(heroName))
			throw new ServerException("Hero is not online");

		if (!informOtherPlayers) {
			mUpdatesPerClient.get(mRemoteHeroes.get(heroName)).add(new WeaponChange(heroName, fightMode, firstHand, secondHand));
		}
		else {
			for (int id : mClientPlayers.keySet()) {
				if (id != GM_ID) {
					mUpdatesPerClient.get(id).add(new WeaponChange(heroName, fightMode, firstHand, secondHand));
				}
			}
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "Waffen-Update für " + heroName + " empfangen.");
		
		RescheduleTimeoutTimer(GM_ID);
	}
	
	@Override
	public void informPlayersOfKRChange(int newKr) throws RemoteException, ServerException {
		if (mShutdown)
			throw new ServerShutdownException();
		for (int id : mClientPlayers.keySet()) {
			mUpdatesPerClient.get(id).add(new KRChange(newKr));
		}
		
		mLog.addLog(IRemoteLog.LogCategory.Game, "KR-Update empfangen.");
		
		RescheduleTimeoutTimer(GM_ID);
	}

}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import dsa.model.characters.Hero;
import dsa.model.data.Opponent;
import dsa.remote.IServer.FightProperty;
import dsa.util.Directories;

public class RemoteManager extends LogManager {
	
	private static RemoteManager sInstance;
	
	public static RemoteManager getInstance() {
		if (sInstance == null) {
			sInstance = new RemoteManager();
		}
		return sInstance;
	}
	
	private RemoteFight mFight;
	
	private RemoteManager() {
		loadGameLog();
		mFight = new RemoteFight();
	}
	
	private static final String GAME_LOG_FILE = "remotegamelog.txt";
	
	private void loadGameLog() {
		isInLogRead = true;
		BufferedReader reader = null;
		String filename = Directories.getUserDataPath() + GAME_LOG_FILE;
		try {
			File file = new File(filename);
			if (!file.exists())
				return;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				addLog(IRemoteLog.LogCategory.Game, line);
				line = reader.readLine();
			}
		}
		catch (IOException ex) {
			addLog(IRemoteLog.LogCategory.Error, "Konnte Log nicht lesen: " + ex.getLocalizedMessage());
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException ex) {
				}
			}
			isInLogRead = false;
		}
	}
	
	private boolean gameLogErrorWritten = false;
	private boolean isInLogRead = false;
	
	@Override
	protected void writeGameLogEntry(String entry) {
		if (isInLogRead)
			return;
		PrintWriter writer = null;
		String filename = Directories.getUserDataPath() + GAME_LOG_FILE;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "UTF-8"));
			writer.println(entry);
			writer.flush();
		}
		catch (IOException ex) {
			if (!gameLogErrorWritten) {
				addLog(IRemoteLog.LogCategory.Error, "Konnte Log nicht schreiben: " + ex.getLocalizedMessage());
				gameLogErrorWritten = true;
			}
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	@Override
	protected void deleteGameLogFile() {
		String filename = Directories.getUserDataPath() + GAME_LOG_FILE;
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
	}

	private GMRemoteClient mGMClient;
	private PlayerRemoteClient mPlayerClient;
	
	public boolean isConnectedAsGM() {
		return mGMClient != null && mGMClient.isConnected();
	}
	
	public boolean isConnectedAsPlayer() {
		return mPlayerClient != null && mPlayerClient.isConnected();
	}
	
	public boolean isConnected() { 
		return isConnectedAsGM() || isConnectedAsPlayer();
	}
	
	public void connectAsGM(String playerName, String host, int port) {
		if (mGMClient == null) {
			mGMClient = new GMRemoteClient();
			mGMClient.setRemoteLog(this);
			mGMClient.setRemoteFight(mFight);
		}
		if (mGMClient.isConnected())
			mGMClient.disconnect();
		mGMClient.connect(playerName, host, port);
		mFight.clear();
	}
	
	public void connectAsPlayer(String playerName, String host, int port) {
		if (mPlayerClient == null) {
			mPlayerClient = new PlayerRemoteClient();
			mPlayerClient.setRemoteLog(this);
			mPlayerClient.setRemoteFight(mFight);
		}
		if (mPlayerClient.isConnected())
			mPlayerClient.disconnect();
		mPlayerClient.connect(playerName, host, port);
		mFight.clear();
	}
	
	public void disconnect() {
		if (mPlayerClient != null && mPlayerClient.isConnected())
			mPlayerClient.disconnect();
		if (mGMClient != null && mGMClient.isConnected())
			mGMClient.disconnect();
	}
	
	public void registerHero(Hero hero) {
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.connectHeroOnline(hero);
		}
	}
	
	public void unregisterHero(Hero hero) {
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.disconnectHeroOnline(hero);
			mFight.removeAllAttacksAgainstHero(hero.getName());
			mFight.removeAllAttacksByHero(hero.getName());
		}
	}
	
	public boolean isHeroRegistered(Hero hero) {
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			return mPlayerClient.isHeroConnectedOnline(hero);
		}
		else {
			return false;
		}
	}
	
	public void setListenForHeroChanges(boolean listen) {
		if (mPlayerClient != null) {
			mPlayerClient.setListenForHeroChanges(listen);
		}
		if (mGMClient != null) {
			mGMClient.setListenForHeroChanges(listen);
		}
	}
	
	public void informOfRegeneration(Hero hero, String text, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfRegeneration(hero, text, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			if (informOtherPlayers) {
				mPlayerClient.informPlayersOfRegeneration(hero, text);
			}
			else {
				mPlayerClient.informGMOfRegeneration(hero, text);
			}
		}
	}
	
	public void informOfProbe(Hero hero, String text, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfProbe(hero, text, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			if (informOtherPlayers) {
				mPlayerClient.informPlayersOfProbe(hero, text);
			}
			else {
				mPlayerClient.informGMOfProbe(hero, text);
			}
		}
	}

	public void informOfHeroAt(Hero hero, String text, int quality, boolean hit, int tp, boolean weaponLess, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfHeroAT(hero, text, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.informOfAT(hero, text, quality, hit, tp, weaponLess, informOtherPlayers);
		}
	}
	
	public void informOfOpponentAt(Hero hero, Opponent opponent, String text, int quality, boolean hit, int tp, boolean weaponLess, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfOpponentAT(hero, opponent, text, quality, hit, tp, weaponLess, informOtherPlayers);
		}
	}
	
	public void informOfHeroProjectileAT(Hero hero, String text, boolean hit, int quality, int tp, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfHeroProjectileAT(hero, text, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.informOfProjectileAT(hero, text, hit, quality, tp, informOtherPlayers);
		}		
	}
	
	public void informOfOpponentProjectileAT(Hero hero, Opponent opponent, String text, boolean hit, int quality, int tp, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfOpponentProjectileAT(hero, opponent, text, hit, quality, tp, informOtherPlayers);
		}
	}
	
	public void informOfHeroPA(Hero hero, String text, boolean success, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfHeroPA(hero, text, success, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.informOfPA(hero, text, success, informOtherPlayers);
		}		
	}
	
	public void informOfOpponentPA(Hero hero, Opponent opponent, String text, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfOpponentPA(hero, opponent, text, informOtherPlayers);
		}
	}
	
	public void informOfHit(Hero hero, String text, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfHeroHit(hero, text, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.informOfHit(hero, text, informOtherPlayers);
		}
	}
	
	public void informOfFightPropertyChange(Hero hero, FightProperty fp, boolean informOtherPlayers) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfFightPropertyChange(hero, fp, informOtherPlayers);
		}
		if (mPlayerClient != null && mPlayerClient.isConnected()) {
			mPlayerClient.informOfFightPropertyChange(hero, fp, informOtherPlayers);
		}
	}
	
	public void informPlayerOfWeaponChange(Hero hero) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayerOfWeaponChange(hero);
		}
	}
	
	public void informPlayersOfKRChange(int kr) {
		if (mGMClient != null && mGMClient.isConnected()) {
			mGMClient.informPlayersOfKRChange(kr);
		}
	}
	
	public RemoteFight.Attack getLastAttackAgainstHero(Hero hero) {
		return mFight.getLastReceivedAttackAgainstHero(hero.getName());
	}
	
	public RemoteFight.Attack getLastAttackByHero(Hero hero) {
		return mFight.getLastReceivedAttackByHero(hero.getName());
	}
	
	public void removeLastAttackAgainstHero(Hero hero) {
		mFight.removeLastAttackAgainstHero(hero.getName());
	}
	
	public void removeLastAttackByHero(Hero hero) {
		mFight.removeLastAttackByHero(hero.getName());
	}

}

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.Timer;

import dsa.gui.PackageID;
import dsa.util.Directories;
import dsa.util.Sounds;

abstract class RemoteClient {

	private IServer mServer = null;
	
	private int mClientId = -1;
	
	private boolean mIsConnected = false;
	
	private IRemoteLog mLog = null;
	
	protected abstract boolean isMaster();
	
	boolean isConnected() { return mIsConnected; }
	
	private Timer mTimer = null;
	
	private static final int POLLING_DELAY = 1000;
	
	protected boolean mListenForChanges = true;
	
	private RemoteFight mRemoteFight = null;
	
	void setListenForHeroChanges(boolean listen) {
		mListenForChanges = listen; 
	}
	
	void connect(String playerName, String host, int port) {
		if (mIsConnected)
			disconnect();
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			IServer server = (IServer)registry.lookup(IServer.REGISTERED_NAME);
			int clientId = server.addClient(playerName, isMaster(), IServer.SERVER_VERSION);
			
			mTimer = new Timer(POLLING_DELAY, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					processUpdatesFromServer();
				}
			});
			mTimer.start();
			
			mClientId = clientId;
			mServer = server;
			mIsConnected = true;
			if (mLog != null) {
				if (isMaster()) {
					mLog.addLog(IRemoteLog.LogCategory.Management, "Mit Server als Meister verbunden.");
				}
				else {
					mLog.addLog(IRemoteLog.LogCategory.Management, "Mit Server verbunden als Spieler \"" + playerName + "\"");
				}
			}
			onConnect();
		}
		catch (RemoteException re) {
			if (mLog != null) 
				mLog.addLog(IRemoteLog.LogCategory.Error, "Konnte keine Verbindung zum Server herstellen: " + re.getLocalizedMessage());
		}
		catch (NotBoundException nbe) {
			if (mLog != null)
				mLog.addLog(IRemoteLog.LogCategory.Error, "Dienst auf Server nicht gefunden");
		}
		catch (ServerException ex) {
			handleServerException(ex);
		}
	}
	
	protected void onConnect() {}
	protected void onDisconnect() {}
	protected void onConnectionLost() {}
	
	void disconnect() {
		if (!mIsConnected)
			return;
		try {
			onDisconnect();
			mServer.removeClient(mClientId);
			mClientId = -1;
			mServer = null;
			mIsConnected = false;
			mTimer.stop();
			mTimer = null;
			if (mLog != null) {
				mLog.addLog(IRemoteLog.LogCategory.Management, "Vom Server abgemeldet.");
			}
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException ex) {
			handleServerException(ex);
		}
	}
	
	void processUpdatesFromServer() {
		if (!mIsConnected)
			return;
		try {
			ArrayList<IServer.RemoteUpdate> updates = mServer.getUpdates(mClientId);
			processUpdates(updates);
			if (updates.size() > 0) {
				Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
				boolean playSound = prefs.getBoolean("PlayOnlineMessageSound", true);
				if (playSound) {
					String filename = Directories.getApplicationPath() + "daten" + File.separator + "Sounds" + File.separator + "GlockeKlein.wav";
					Sounds.play(filename);
				}
			}
		}
		catch (RemoteException re) {
			handleRemoteException(re);
		}
		catch (ServerException se) {
			handleServerException(se);
		}
	}
	
	protected abstract void processUpdates(ArrayList<IServer.RemoteUpdate> updates);
	
	protected void handleRemoteException(RemoteException re) {
		mServer = null; 
		mClientId = -1;
		mIsConnected = false;
		onConnectionLost();
		if (mLog != null) {
			mLog.addLog(IRemoteLog.LogCategory.Error, "Kommunikationsfehler: " + re.getLocalizedMessage());
			mLog.addLog(IRemoteLog.LogCategory.Management, "Verbindung zum Server getrennt");
			mLog.connectionStatusChanged();
		}
	}
	
	protected void handleServerException(ServerException se) {
		if (se instanceof ServerShutdownException) {
			mServer = null;
			mClientId = -1;
			mIsConnected = false;
			onConnectionLost();
			if (mLog != null) {
				mLog.addLog(IRemoteLog.LogCategory.Management, "Server wurde gestoppt.");
				mLog.connectionStatusChanged();
			}
		}
		else if (se instanceof UnknownClientException) {
			mServer = null;
			mClientId = -1;
			mIsConnected = false;
			onConnectionLost();
			if (mLog != null) {
				mLog.addLog(IRemoteLog.LogCategory.Management, "Client nicht mehr am Server verbunden.");
				mLog.connectionStatusChanged();
			}			
		}
		else if (mLog != null) {
			mLog.addLog(IRemoteLog.LogCategory.Error, se.getLocalizedMessage());
		}
	}
	
	protected IRemoteLog getLog() { return mLog; }
	
	protected IServer getServer() { return mServer; }
	
	protected int getClientId() { return mClientId; }
	
	void setRemoteLog(IRemoteLog log) {
		mLog = log;
	}
	
	public void setRemoteFight(RemoteFight fight) {
		mRemoteFight = fight;
	}
	
	protected RemoteFight getRemoteFight() {
		return mRemoteFight;
	}
}

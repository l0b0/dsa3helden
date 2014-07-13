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
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import dsa.util.Directories;

public class ServerManager extends LogManager {

	private static ServerManager sInstance;
	
	public static ServerManager getInstance() {
		if (sInstance == null) {
			sInstance = new ServerManager();
		}
		return sInstance;
	}
	
	private ServerManager() {
		loadServerLog();
	}
	
	private boolean mServerStarted = false;
	private Registry mRegistry = null;
	private Server mServer = null;
	private HashMap<Integer, Registry> mRegistries = new HashMap<Integer, Registry>();
	
	private static final String SERVER_LOG_FILE = "serverlog.txt";
	
	private void loadServerLog() {
		isInLogRead = true;
		BufferedReader reader = null;
		String filename = Directories.getUserDataPath() + SERVER_LOG_FILE;
		try {
			File file = new File(filename);
			if (!file.exists())
				return;
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = reader.readLine();
			while (line != null) {
				int cat = Integer.parseInt(line);
				if (cat < 0 || cat >= IRemoteLog.LogCategory.values().length)
					cat = 0;
				line = reader.readLine();
				if (line != null) {
					addLog(IRemoteLog.LogCategory.values()[cat], line);
					line = reader.readLine();
				}
			}
		}
		catch (NumberFormatException ex) {
			addLog(IRemoteLog.LogCategory.Error, "Konnte Log nicht lesen: " + ex.getLocalizedMessage());
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
	
	private boolean isInLogRead = false;
	private boolean serverLogErrorWritten = false;
	
	@Override
	protected void writeLogEntry(LogCategory category, String entry) {
		if (isInLogRead)
			return;
		PrintWriter writer = null;
		String filename = Directories.getUserDataPath() + SERVER_LOG_FILE;
		try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "UTF-8"));
			writer.println(category.ordinal());
			writer.println(entry);
			writer.flush();
		}
		catch (IOException ex) {
			if (!serverLogErrorWritten) {
				addLog(IRemoteLog.LogCategory.Error, "Konnte Log nicht schreiben: " + ex.getLocalizedMessage());
				serverLogErrorWritten = true;
			}
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	@Override
	protected void deleteLogFile() {
		String filename = Directories.getUserDataPath() + SERVER_LOG_FILE;
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
	}

	public boolean isServerStarted() {
		return mServerStarted;
	}
	
	public void startServer(int port) {
		if (mServerStarted)
			stopServer();
		try {
			Registry registry = null;
			if (mRegistries.containsKey(port)) {
				registry = mRegistries.get(port);
			}
			else {
				registry = LocateRegistry.createRegistry(port);
				mRegistries.put(port, registry);
			}
			mServer = new Server(this);
			IServer stub = (IServer)UnicastRemoteObject.exportObject(mServer, port);
			registry.bind(IServer.REGISTERED_NAME, stub);
			mServerStarted = true;
			mRegistry = registry;
			addLog(IRemoteLog.LogCategory.Management, "Server auf Port " + port + " gestartet.");
		}
		catch (RemoteException ex) {
			addLog(IRemoteLog.LogCategory.Error, "Konnte Server nicht starten: " + ex.getLocalizedMessage());
		}
		catch (AlreadyBoundException ex) {
			addLog(IRemoteLog.LogCategory.Error, ex.getLocalizedMessage());
		}
	}
	
	public void stopServer() {
		if (!mServerStarted)
			return;
		try {
			mRegistry.unbind(IServer.REGISTERED_NAME);
		}
		catch (AccessException ex) {
			addLog(IRemoteLog.LogCategory.Error, ex.getLocalizedMessage());
		}
		catch (NotBoundException ex) {
			addLog(IRemoteLog.LogCategory.Error, ex.getLocalizedMessage());
		}
		catch (RemoteException ex) {
			addLog(IRemoteLog.LogCategory.Error, ex.getLocalizedMessage());
		}
		if (mServer != null) {
			mServer.shutdown();
		}
		mRegistry = null;
		mServer = null;
		mServerStarted = false;
	}
	
}

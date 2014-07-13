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

import java.util.ArrayList;

public abstract class LogManager implements IRemoteLog {

	public static final int LOG_ALL = 0;
	public static final int LOG_GAME = 1;
	public static final int LOG_MANAGEMENT = 2;
	public static final int LOG_ERROR = 3;
	
	private ArrayList<AllLogEntry> mAllLog = new ArrayList<AllLogEntry>();
	private ArrayList<ArrayList<String>> mLogs = new ArrayList<ArrayList<String>>();
	
	private IRemoteLog mRemoteLogClient = null;
	
	protected LogManager() {
		for (int i = 0; i < LOG_ERROR; ++i)
			mLogs.add(new ArrayList<String>());
	}
	
	public ArrayList<String> getLog(int logId) {
		if (logId == LOG_ALL) {
			ArrayList<String> res = new ArrayList<String>();
			for (AllLogEntry entry : mAllLog) {
				res.add(entry.message);
			}
			return res;
		}
		else if (logId > 0 && logId < mLogs.size())
			return mLogs.get(logId - 1);
		else
			return new ArrayList<String>();
	}
	
	public void clearLog(int logId) {
		if (logId > 0 && logId < mLogs.size()) {
			mLogs.get(logId - 1).clear();
			if (logId == LOG_GAME) {
				deleteGameLogFile();
			}
			ArrayList<AllLogEntry> newEntries = new ArrayList<AllLogEntry>();
			for (AllLogEntry entry : mAllLog) {
				if (entry.category != logId) {
					newEntries.add(entry);
				}
			}
			mAllLog = newEntries;
		}
		else if (logId == LOG_ALL) {
			for (int i = LOG_ALL; i < LOG_ERROR; ++i) {
				clearLog(i + 1);
			}
			mAllLog.clear();
			deleteLogFile();
		}
	}
	
	public void setRemoteLogClient(IRemoteLog client) {
		mRemoteLogClient = client;
	}
	
	private class AllLogEntry {
		public int category;
		public String message;
		public AllLogEntry(int c, String m) {
			category = c;
			message = m;
		}
	}
	
	@Override
	public void addLog(LogCategory category, String message) {
		switch (category) {
		case Game:
			mLogs.get(LOG_GAME - 1).add(message);
			mAllLog.add(new AllLogEntry(LOG_GAME, message));
			writeGameLogEntry(message);
			break;
		case Management:
			mLogs.get(LOG_MANAGEMENT - 1).add(message);
			mAllLog.add(new AllLogEntry(LOG_MANAGEMENT, message));
			break;
		case Error:
			mLogs.get(LOG_ERROR - 1).add(message);
			mAllLog.add(new AllLogEntry(LOG_ERROR, message));
			break;
		default:
			break;
		}
		writeLogEntry(category, message);
		if (mRemoteLogClient != null) {
			mRemoteLogClient.addLog(category, message);
		}
	}
	
	@Override
	public void connectionStatusChanged() {
		if (mRemoteLogClient != null) {
			mRemoteLogClient.connectionStatusChanged();
		}
	}
	
	protected void deleteGameLogFile() {}
	protected void deleteLogFile() {}
	protected void writeGameLogEntry(String message) {}
	protected void writeLogEntry(LogCategory category, String message) {}
}

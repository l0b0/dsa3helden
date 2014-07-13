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
package dsa.gui.frames;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import dsa.gui.PackageID;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.remote.IRemoteLog;
import dsa.remote.RemoteManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JTextArea;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.JMenuItem;

public class RemoteFrame extends SubFrame implements IRemoteLog, CharactersObserver {
	
	private JTextField hostField;
	private JButton connectAsPlayerButton;
	private JButton connectAsGMButton;
	private JButton disconnectButton;
	private JSpinner portSpinner;
	private JTextField playerNameField;
	private JTabbedPane logsPane;
	private JButton registerHeroButton;
	private JButton unregisterHeroButton;
	
	private ArrayList<JTextArea> logAreas = new ArrayList<JTextArea>();
	
	public RemoteFrame() {
		initialize();
	}

	/**
	 * Create the frame.
	 */
	public RemoteFrame(String title) {
		super(title);
		initialize();
	}
	
	private void initialize() {
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		//setBounds(100, 100, 505, 331);
		RemoteManager.getInstance().setRemoteLogClient(this);
		Group.getInstance().addObserver(this);
	    addWindowListener(new WindowAdapter() {
	        boolean done = false;

	        public void windowClosing(WindowEvent e) {
        	  RemoteManager.getInstance().setRemoteLogClient(null);
        	  Group.getInstance().removeObserver(RemoteFrame.this);
	          done = true;
	        }

	        public void windowClosed(WindowEvent e) {
	          if (!done) {
                RemoteManager.getInstance().setRemoteLogClient(null);
                Group.getInstance().removeObserver(RemoteFrame.this);
	            done = true;
	          }
	        }
	      });
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), Localization.getString("OnlineSpiel.Verbindung"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		getContentPane().add(connectionPanel, BorderLayout.NORTH);
		connectionPanel.setLayout(new BorderLayout(10, 10));
		
		JPanel panel = new JPanel();
		connectionPanel.add(panel, BorderLayout.EAST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblPort = new JLabel(Localization.getString("OnlineSpiel.Port")); //$NON-NLS-1$
		panel.add(lblPort);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		panel.add(horizontalStrut_1);
		
		portSpinner = new JSpinner();
		portSpinner.setModel(new SpinnerNumberModel(1099, 1000, 2000, 1));
		panel.add(portSpinner);
		
		JPanel panel_3 = new JPanel();
		connectionPanel.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel(Localization.getString("OnlineSpiel.Spielername")); //$NON-NLS-1$
		panel_3.add(lblNewLabel);
		
		Component horizontalStrut_4 = Box.createHorizontalStrut(10);
		panel_3.add(horizontalStrut_4);
		
		playerNameField = new JTextField();
		panel_3.add(playerNameField);
		playerNameField.setColumns(10);
		
		JPanel panel_1 = new JPanel();
		connectionPanel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JLabel lblHost = new JLabel(Localization.getString("OnlineSpiel.Host")); //$NON-NLS-1$
		panel_1.add(lblHost);
		
		Component horizontalStrut = Box.createHorizontalStrut(5);
		panel_1.add(horizontalStrut);
		
		hostField = new JTextField();
		panel_1.add(hostField);
		hostField.setColumns(10);
		
		JPanel panel_2 = new JPanel();
		connectionPanel.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new BorderLayout(5, 5));
		JPanel panel_4 = new JPanel();
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
		
		connectAsPlayerButton = new JButton(Localization.getString("OnlineSpiel.AlsSpielerVerbinden")); //$NON-NLS-1$
		connectAsPlayerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connectAsPlayer();
			}
		});
		panel_4.add(connectAsPlayerButton);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(10);
		panel_4.add(horizontalStrut_2);
		
		disconnectButton = new JButton(Localization.getString("OnlineSpiel.Trennen")); //$NON-NLS-1$
		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});
		
		connectAsGMButton = new JButton(Localization.getString("OnlineSpiel.AlsMeisterVerbinden")); //$NON-NLS-1$
		connectAsGMButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectAsGM();
			}
		});
		panel_4.add(connectAsGMButton);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(10);
		panel_4.add(horizontalStrut_3);
		panel_4.add(disconnectButton);
		
		panel_2.add(panel_4, BorderLayout.NORTH);
		
		JPanel panel_5 = new JPanel();
		panel_2.add(panel_5, BorderLayout.SOUTH);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		
		registerHeroButton = new JButton(Localization.getString("OnlineSpiel.HeldAnmelden")); //$NON-NLS-1$
		registerHeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				registerHero();
			}
		});
		panel_5.add(registerHeroButton);
		
		Component horizontalStrut_5 = Box.createHorizontalStrut(10);
		panel_5.add(horizontalStrut_5);
		
		unregisterHeroButton = new JButton(Localization.getString("OnlineSpiel.HeldAbmelden")); //$NON-NLS-1$
		unregisterHeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unregisterHero();
			}
		});
		panel_5.add(unregisterHeroButton);
		
		JPanel logPanel = new JPanel();
		logPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), Localization.getString("OnlineSpiel.Nachrichten"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		getContentPane().add(logPanel, BorderLayout.CENTER);
		logPanel.setLayout(new BorderLayout(0, 0));
		
		logsPane = new JTabbedPane(JTabbedPane.BOTTOM);
		logsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		logPanel.add(logsPane, BorderLayout.CENTER);
		
		JPopupMenu popupMenu = new JPopupMenu();
		
		JMenuItem mntmCopyLogItem = new JMenuItem(Localization.getString("OnlineSpiel.Kopieren")); //$NON-NLS-1$
		mntmCopyLogItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyLog();
			}
		});
		popupMenu.add(mntmCopyLogItem);
		JMenuItem mntmClearLogItem = new JMenuItem(Localization.getString("OnlineSpiel.Loeschen")); //$NON-NLS-1$
		mntmClearLogItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		popupMenu.add(mntmClearLogItem);
		
		createLogArea(Localization.getString("OnlineSpiel.Alle"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineSpiel.Spiel"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineSpiel.Verwaltung"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineSpiel.Fehler"), popupMenu); //$NON-NLS-1$
		
		logsPane.setPreferredSize(new Dimension(210, 160));
		
		loadParameters();
		updateData();
		if (!hasSavedFrameBounds()) {
			pack();
		}
	}
	
	private void createLogArea(String name, JPopupMenu popupMenu) {
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setColumns(0);
		area.setRows(0);
		area.setEditable(false);
		// area.setPreferredSize(new Dimension(200, 150));
		JScrollPane pane = new JScrollPane(area);
		logsPane.addTab(name, null, pane, null);
		logAreas.add(area);
		addPopup(area, popupMenu);
	}
	
	private void updateData() {
		updateState();
		updateLogs();
	}
	
	private void updateLogs() {
		for (int i = 0; i <= RemoteManager.LOG_ERROR; ++i) {
			ArrayList<String> logs = RemoteManager.getInstance().getLog(i);
			StringBuilder builder = new StringBuilder();
			for (String line : logs) {
				builder.append(line);
				builder.append("\n"); //$NON-NLS-1$
			}
			int oldLength = logAreas.get(i).getDocument().getLength();
			logAreas.get(i).replaceRange(builder.toString(), 0, oldLength);
			/*
			try {
				logAreas.get(i).getDocument().remove(0, oldLength);
				logAreas.get(i).getDocument().insertString(0, builder.toString(), null);
			} catch (BadLocationException e) {
			}
			*/
		}
	}
	
	private void updateState() {
		if (RemoteManager.getInstance().isConnectedAsGM()) {
			connectAsGMButton.setEnabled(false);
			connectAsPlayerButton.setEnabled(false);
			disconnectButton.setEnabled(true);
		}
		else if (RemoteManager.getInstance().isConnectedAsPlayer()) {
			connectAsGMButton.setEnabled(false);
			connectAsPlayerButton.setEnabled(false);
			disconnectButton.setEnabled(true);
		}
		else {
			connectAsGMButton.setEnabled(true);
			connectAsPlayerButton.setEnabled(true);
			disconnectButton.setEnabled(false);			
		}		
		Hero hero = Group.getInstance().getActiveHero();
		boolean isHeroOnline = RemoteManager.getInstance().isHeroRegistered(hero);
		registerHeroButton.setEnabled(RemoteManager.getInstance().isConnectedAsPlayer() && !isHeroOnline);
		unregisterHeroButton.setEnabled(isHeroOnline);
	}

	private void disconnect() {
		RemoteManager.getInstance().disconnect();
		updateState();
	}

	private void connectAsPlayer() {
		String host = hostField.getText();
		int port = ((SpinnerNumberModel)portSpinner.getModel()).getNumber().intValue();
		String playerName = playerNameField.getText();
		if (host == null || host.isEmpty()) {
			JOptionPane.showMessageDialog(this, Localization.getString("OnlineSpiel.HostEintragen"), Localization.getString("OnlineSpiel.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (playerName == null || playerName.isEmpty()) {
			JOptionPane.showMessageDialog(this, Localization.getString("OnlineSpiel.SpielernamenEintragen"), Localization.getString("OnlineSpiel.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		RemoteManager.getInstance().connectAsPlayer(playerName, host, port);
		Hero activeHero = Group.getInstance().getActiveHero();
		if (RemoteManager.getInstance().isConnectedAsPlayer()) {
			RemoteManager.getInstance().registerHero(activeHero);
			callOnlineConnectCommand();
		}
		saveParameters();
		updateState();
	}
	
	private void connectAsGM() {
		String host = hostField.getText();
		int port = ((SpinnerNumberModel)portSpinner.getModel()).getNumber().intValue();
		String playerName = playerNameField.getText();
		if (host == null || host.isEmpty()) {
			JOptionPane.showMessageDialog(this, Localization.getString("OnlineSpiel.HostEintragen"), Localization.getString("OnlineSpiel.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (playerName == null || playerName.isEmpty()) {
			JOptionPane.showMessageDialog(this, Localization.getString("OnlineSpiel.SpielernamenEintragen"), Localization.getString("OnlineSpiel.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		RemoteManager.getInstance().connectAsGM(playerName, host, port);
		if (RemoteManager.getInstance().isConnectedAsGM()) {
			callOnlineConnectCommand();
		}
		saveParameters();
		updateState();
	}
	
	private void callOnlineConnectCommand() {
	    Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
	    String connectCommand = prefs.get("OnlineConnectCommand", ""); //$NON-NLS-1$ //$NON-NLS-2$
	    if (!connectCommand.isEmpty()) {
	    	connectCommand = connectCommand.replace("%HOST%", hostField.getText()); //$NON-NLS-1$
	    	String[] cmdArray = connectCommand.split(" "); //$NON-NLS-1$
	    	try {
				Runtime.getRuntime().exec(cmdArray);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, Localization.getString("OnlineSpiel.ProgrammausfuehrenFehler") + e.getLocalizedMessage(),  //$NON-NLS-1$
						Localization.getString("OnlineSpiel.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
	    }
	}
	
	private void saveParameters() {
		Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
		prefs.put("RemotePlayerName", playerNameField.getText()); //$NON-NLS-1$
		prefs.put("RemoteServerName", hostField.getText()); //$NON-NLS-1$
		prefs.putInt("RemotePort", ((SpinnerNumberModel)portSpinner.getModel()).getNumber().intValue()); //$NON-NLS-1$
	}
	
	private void loadParameters() {
		Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
		String playerName = prefs.get("RemotePlayerName", ""); //$NON-NLS-1$ //$NON-NLS-2$
		playerNameField.setText(playerName);
		String hostName = prefs.get("RemoteServerName", ""); //$NON-NLS-1$ //$NON-NLS-2$
		hostField.setText(hostName);
		int port = prefs.getInt("RemotePort", java.rmi.registry.Registry.REGISTRY_PORT); //$NON-NLS-1$
		if (port < 1000 || port > 2000)
			port = java.rmi.registry.Registry.REGISTRY_PORT;
		portSpinner.getModel().setValue(port);
	}

	@Override
	public String getHelpPage() {
		return "Online-Spiel"; //$NON-NLS-1$
	}
	
	private void clearLog() {
		RemoteManager.getInstance().clearLog(logsPane.getSelectedIndex());
		updateLogs();
	}
	
	private void copyLog() {
		JTextArea area = logAreas.get(logsPane.getSelectedIndex());
		String text = area.getSelectedText();
		if (text == null || text.isEmpty()) {
			area.selectAll();
			area.copy();
			area.setCaretPosition(area.getText().length());
		}
		area.copy();
	}
	
	private void registerHero() {
		RemoteManager.getInstance().registerHero(Group.getInstance().getActiveHero());
		updateState();
	}
	
	private void unregisterHero() {
		RemoteManager.getInstance().unregisterHero(Group.getInstance().getActiveHero());
		updateState();
	}

	@Override
	public void addLog(LogCategory category, String message) {
		appendLine(logAreas.get(RemoteManager.LOG_ALL), message);
		switch (category) {
		case Game:
			appendLine(logAreas.get(RemoteManager.LOG_GAME), message);
			break;
		case Management:
			appendLine(logAreas.get(RemoteManager.LOG_MANAGEMENT), message);
			break;
		case Error:
			appendLine(logAreas.get(RemoteManager.LOG_ERROR), message);
			break;
		default:
			break;
		}
	}
	
	private void appendLine(JTextArea area, String line) {
		area.append(line + "\n"); //$NON-NLS-1$
		area.setCaretPosition(area.getText().length());
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
		updateState();
	}

	@Override
	public void characterRemoved(Hero character) {
		updateState();
	}

	@Override
	public void characterAdded(Hero character) {
		updateState();
	}

	@Override
	public void globalLockChanged() {
	}

	@Override
	public void connectionStatusChanged() {
		updateState();
	}
}

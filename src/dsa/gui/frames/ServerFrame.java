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
import javax.swing.JSpinner;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import dsa.gui.PackageID;
import dsa.remote.IRemoteLog;
import dsa.remote.ServerManager;

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
import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.FlowLayout;

public class ServerFrame extends SubFrame implements IRemoteLog {
	
	private JButton startButton;
	private JButton stopButton;
	private JTabbedPane logsPane;
	private JSpinner portSpinner;
	private JTextField ipAddressField;
	
	private ArrayList<JTextArea> logAreas = new ArrayList<JTextArea>();
	
	public ServerFrame() {
		initialize();
	}

	/**
	 * Create the frame.
	 */
	public ServerFrame(String title) {
		super(title);
		initialize();
	}
	
	private void initialize() {
		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		//setBounds(600, 100, 505, 331);
		ServerManager.getInstance().setRemoteLogClient(this);
	    addWindowListener(new WindowAdapter() {
	        boolean done = false;

	        public void windowClosing(WindowEvent e) {
        	  ServerManager.getInstance().setRemoteLogClient(null);
	          done = true;
	        }

	        public void windowClosed(WindowEvent e) {
	          if (!done) {
	        	  ServerManager.getInstance().setRemoteLogClient(null);
	            done = true;
	          }
	        }
	      });
		
		JPanel connectionPanel = new JPanel();
		connectionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), Localization.getString("OnlineServer.Verbindung"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51))); //$NON-NLS-1$
		getContentPane().add(connectionPanel, BorderLayout.NORTH);
		connectionPanel.setLayout(new BorderLayout(10, 10));
		
		JPanel panel = new JPanel();
		connectionPanel.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel(Localization.getString("OnlineServer.Host")); //$NON-NLS-1$
		panel.add(lblNewLabel);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		panel.add(horizontalStrut);
		
		ipAddressField = new JTextField();
		panel.add(ipAddressField);
		JPanel panel_2 = new JPanel();
		connectionPanel.add(panel_2, BorderLayout.EAST);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		JLabel label = new JLabel(Localization.getString("OnlineServer.Port")); //$NON-NLS-1$
		panel_2.add(label);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(5);
		panel_2.add(horizontalStrut_1);
		
		portSpinner = new JSpinner();
		portSpinner.setAlignmentX(Component.RIGHT_ALIGNMENT);
		portSpinner.setModel(new SpinnerNumberModel(1099, 1000, 2000, 1));
		panel_2.add(portSpinner);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		connectionPanel.add(panel_1, BorderLayout.SOUTH);
		
		startButton = new JButton(Localization.getString("OnlineServer.Starten")); //$NON-NLS-1$
		panel_1.add(startButton);
		
		stopButton = new JButton(Localization.getString("OnlineServer.Stoppen")); //$NON-NLS-1$
		panel_1.add(stopButton);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopServer();
			}
		});
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startServer();
			}
		});
		
		JPanel logPanel = new JPanel();
		logPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), Localization.getString("OnlineServer.Nachrichten"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		getContentPane().add(logPanel, BorderLayout.CENTER);
		logPanel.setLayout(new BorderLayout(0, 0));
		
		logsPane = new JTabbedPane(JTabbedPane.BOTTOM);
		logsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		logPanel.add(logsPane, BorderLayout.CENTER);
		
		JPopupMenu popupMenu = new JPopupMenu();
		
		JMenuItem mntmCopyLogItem = new JMenuItem(Localization.getString("OnlineServer.Kopieren")); //$NON-NLS-1$
		mntmCopyLogItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyLog();
			}
		});
		popupMenu.add(mntmCopyLogItem);
		JMenuItem mntmClearLogItem = new JMenuItem(Localization.getString("OnlineServer.Loeschen")); //$NON-NLS-1$
		mntmClearLogItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});
		popupMenu.add(mntmClearLogItem);
		
		createLogArea(Localization.getString("OnlineServer.Alle"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineServer.Spiel"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineServer.Verwaltung"), popupMenu); //$NON-NLS-1$
		createLogArea(Localization.getString("OnlineServer.Fehler"), popupMenu); //$NON-NLS-1$
		
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
		area.setEditable(false);
		JScrollPane pane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logsPane.addTab(name, null, pane, null);
		logAreas.add(area);
		addPopup(area, popupMenu);
	}
	
	private void updateData() {
		updateState();
		updateLogs();
	}
	
	private void updateLogs() {
		for (int i = 0; i <= ServerManager.LOG_ERROR; ++i) {
			ArrayList<String> logs = ServerManager.getInstance().getLog(i);
			StringBuilder builder = new StringBuilder();
			for (String line : logs) {
				builder.append(line);
				builder.append("\n"); //$NON-NLS-1$
			}
			logAreas.get(i).setText(builder.toString());
		}
	}
	
	private void updateState() {
		if (ServerManager.getInstance().isServerStarted()) {
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
		}
		else {
			startButton.setEnabled(true);
			stopButton.setEnabled(false);			
		}		
	}

	private void stopServer() {
		ServerManager.getInstance().stopServer();
		updateState();
	}

	private void startServer() {
		String host = ipAddressField.getText();
		if (host == null || host.isEmpty()) {
			JOptionPane.showMessageDialog(this, Localization.getString("OnlineServer.HostEintragen"), Localization.getString("OnlineServer.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		System.setProperty("java.rmi.server.hostname", ipAddressField.getText()); //$NON-NLS-1$
		int port = ((SpinnerNumberModel)portSpinner.getModel()).getNumber().intValue();
		ServerManager.getInstance().startServer(port);
		saveParameters();
		updateState();
	}
	
	private void saveParameters() {
		Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
		prefs.putInt("RemotePort", ((SpinnerNumberModel)portSpinner.getModel()).getNumber().intValue()); //$NON-NLS-1$
		prefs.put("IPAddress", ipAddressField.getText()); //$NON-NLS-1$
	}
	
	private void loadParameters() {
		Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
		int port = prefs.getInt("RemotePort", java.rmi.registry.Registry.REGISTRY_PORT); //$NON-NLS-1$
		if (port < 1000 || port > 2000)
			port = java.rmi.registry.Registry.REGISTRY_PORT;
		portSpinner.getModel().setValue(port);
		String ipAddress = prefs.get("IPAddress", ""); //$NON-NLS-1$ //$NON-NLS-2$
		ipAddressField.setText(ipAddress);
	}

	@Override
	public String getHelpPage() {
		return "Online-Server"; //$NON-NLS-1$
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

	private void clearLog() {
		ServerManager.getInstance().clearLog(logsPane.getSelectedIndex());
		updateLogs();
	}

	@Override
	public void addLog(LogCategory category, String message) {
		appendLine(logAreas.get(ServerManager.LOG_ALL), message);
		switch (category) {
		case Game:
			appendLine(logAreas.get(ServerManager.LOG_GAME), message);
			break;
		case Management:
			appendLine(logAreas.get(ServerManager.LOG_MANAGEMENT), message);
			break;
		case Error:
			appendLine(logAreas.get(ServerManager.LOG_ERROR), message);
			break;
		default:
			break;
		}
	}
	
	private void appendLine(JTextArea area, String line) {
		String oldText = area.getText();
		String newText = oldText + line + "\n"; //$NON-NLS-1$
		area.setText(newText);
		area.setCaretPosition(newText.length());
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
	public void connectionStatusChanged() {
		updateState();
	}
}

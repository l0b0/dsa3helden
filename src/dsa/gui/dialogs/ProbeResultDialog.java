package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import dsa.remote.RemoteManager;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;

public class ProbeResultDialog extends BGDialog {

	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel3 = null;
	private JButton closeButton = null;
	
	public static final int SEND_TO_SINGLE = 1;
	public static final int SEND_TO_ALL = 2;
	
	public static int showDialog(Container parent, String message, String title, boolean allowOnlineOperations)
	{
		if (parent instanceof Dialog)
			return showDialog((Dialog)parent, message, title, allowOnlineOperations);
		else
			return showDialog((Frame)parent, message, title, allowOnlineOperations);
	}
	
	public static int showDialog(Frame parent, String message, String title, boolean allowOnlineOperations)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setAllowOnlineOperations(allowOnlineOperations);
		dialog.setVisible(true);
		int result = 0;
		if (dialog.shallSendToMasterOrPlayer())
			result += SEND_TO_SINGLE;
		if (dialog.shallSendToOtherPlayers())
			result += SEND_TO_ALL;
		return result;
	}

	public static int showDialog(Frame parent, String message, String title, ImageIcon icon, boolean allowOnlineOperations)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent, icon);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setAllowOnlineOperations(allowOnlineOperations);
		dialog.setVisible(true);
		int result = 0;
		if (dialog.shallSendToMasterOrPlayer())
			result += SEND_TO_SINGLE;
		if (dialog.shallSendToOtherPlayers())
			result += SEND_TO_ALL;
		return result;
	}

	public static int showDialog(Dialog parent, String message, String title, boolean allowOnlineOperations)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setAllowOnlineOperations(allowOnlineOperations);
		dialog.setVisible(true);
		int result = 0;
		if (dialog.shallSendToMasterOrPlayer())
			result += SEND_TO_SINGLE;
		if (dialog.shallSendToOtherPlayers())
			result += SEND_TO_ALL;
		return result;
	}

	public static int showDialog(Dialog parent, String message, String title, ImageIcon icon)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent, icon);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setAllowOnlineOperations(false);
		dialog.setVisible(true);
		int result = 0;
		if (dialog.shallSendToMasterOrPlayer())
			result += SEND_TO_SINGLE;
		if (dialog.shallSendToOtherPlayers())
			result += SEND_TO_ALL;
		return result;
	}

	/**
	 * This method initializes 
	 * 
	 */
	private ProbeResultDialog() {
		super();
		initialize();
	}
	
	private ProbeResultDialog(Frame parent) {
		super(parent);
		initialize();
	}
	
	private ProbeResultDialog(Frame parent, ImageIcon icon) {
		super(parent);
		this.icon = icon;
		initialize();
	}
	
	private ProbeResultDialog(Dialog parent) {
		super(parent);
		initialize();
	}

	private ProbeResultDialog(Dialog parent, ImageIcon icon) {
		super(parent);
		this.icon = icon;
		initialize();
	}

	private String text = "";
	private JScrollPane jScrollPane = null;
	private JLabel textLabel = null;
	private ImageIcon icon = null;
	
	public void setText(String text)
	{
		String labelText = text.replace("\n", "<BR />");
		labelText = "<HTML><BODY>" + labelText + "</BODY></HTML>";
		textLabel.setText(labelText);
		this.text = text;
	}
	
	private void setAllowOnlineOperations(boolean allowOperations) {
		if (allowOperations && RemoteManager.getInstance().isConnectedAsGM()) {
			sendToMasterOrPlayerBox.setEnabled(true);
			sendToMasterOrPlayerBox.setSelected(true);
			sendToOtherPlayersBox.setEnabled(true);
			sendToOtherPlayersBox.setSelected(true);
			sendToMasterOrPlayerBox.setText("An Spieler senden");
			copyBox.setSelected(false);
		}
		else if (allowOperations && RemoteManager.getInstance().isConnectedAsPlayer()) {
			sendToMasterOrPlayerBox.setEnabled(false);
			sendToMasterOrPlayerBox.setSelected(true);
			sendToOtherPlayersBox.setEnabled(true);
			sendToOtherPlayersBox.setSelected(true);
			sendToMasterOrPlayerBox.setText("An Meister senden");
			copyBox.setSelected(false);				
		}
		else {
			sendToMasterOrPlayerBox.setEnabled(false);
			sendToMasterOrPlayerBox.setSelected(false);
			sendToOtherPlayersBox.setEnabled(false);
			sendToOtherPlayersBox.setSelected(false);
			sendToMasterOrPlayerBox.setText("An Meister senden");
			copyBox.setSelected(true);				
		}
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(400, 200));
        this.setContentPane(getJContentPane());
        this.setTitle("ProbeResult");
        this.setModal(true);
        this.setLocationRelativeTo(getParent());
        this.getRootPane().setDefaultButton(closeButton);
        setEscapeButton(closeButton);
	}

	@Override
	public String getHelpPage() {
		return "Probe";
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("JLabel");
			jLabel2.setPreferredSize(new Dimension(5, 5));
			jLabel = new JLabel();
			jLabel.setPreferredSize(new Dimension(5, 5));
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setSize(new Dimension(277, 178));
			jContentPane.add(jLabel, BorderLayout.NORTH);
			jContentPane.add(jLabel2, BorderLayout.EAST);
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJPanel1(), BorderLayout.CENTER);
			if (icon != null) {
				jContentPane.add(getIconPanel(), BorderLayout.WEST);
			}
			else {
				jLabel1 = new JLabel();
				jLabel1.setText("");
				jLabel1.setPreferredSize(new Dimension(5, 5));
				jContentPane.add(jLabel1, BorderLayout.WEST);				
			}
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
			jPanel.add(getPanel_1());
			jPanel.add(getPanel());
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("");
			jLabel3.setPreferredSize(new Dimension(5, 5));
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			jPanel1.add(jLabel3, BorderLayout.SOUTH);
			jPanel1.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jPanel1;
	}
	
	private JPanel iconPanel;
	private JLabel iconLabel;
	private JPanel panel;
	private JPanel panel_1;
	private JCheckBox copyBox;
	private JCheckBox sendToOtherPlayersBox;
	private JCheckBox sendToMasterOrPlayerBox;
	
	private JPanel getIconPanel() {
		if (iconPanel == null) {
			iconPanel = new JPanel();
			iconPanel.setLayout(new BorderLayout());
			iconLabel = new JLabel(icon);
			iconPanel.add(iconLabel, BorderLayout.NORTH);
		}
		return iconPanel;
	}

	/**
	 * This method initializes closeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText("Schließen");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doCloseActions();
					dispose();
				}
			});
		}
		return closeButton;
	}
	
	private boolean sendToMasterOrPlayer = false;
	private boolean sendToOtherPlayers = false;
	
	private void doCloseActions() {
		if (copyBox.isSelected()) {
			copyText();
		}
		sendToMasterOrPlayer = sendToMasterOrPlayerBox.isSelected();
		sendToOtherPlayers = sendToOtherPlayersBox.isSelected();
	}
	
	public boolean shallSendToMasterOrPlayer() {
		return sendToMasterOrPlayer;
	}
	
	public boolean shallSendToOtherPlayers() {
		return sendToOtherPlayers;
	}
	
	private void copyText()
	{
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(new StringSelection(text), new ClipboardOwner() {

			public void lostOwnership(Clipboard clipboard, Transferable contents) {
			}
		});
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			textLabel = new JLabel();
			textLabel.setText("");
			textLabel.setVerticalAlignment(SwingConstants.TOP);
			textLabel.setVerticalTextPosition(SwingConstants.TOP);
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(textLabel);
		    jScrollPane.setOpaque(false);
		    jScrollPane.getViewport().setOpaque(false);
		}
		return jScrollPane;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			panel.add(getCloseButton());
		}
		return panel;
	}
	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
			panel_1.add(getCopyBox());
			panel_1.add(getSendToMasterOrPlayerBox());
			panel_1.add(getSendToOtherPlayersBox());
		}
		return panel_1;
	}
	private JCheckBox getCopyBox() {
		if (copyBox == null) {
			copyBox = new JCheckBox("Kopieren");
			copyBox.setSelected(true);
		}
		return copyBox;
	}
	private JCheckBox getSendToOtherPlayersBox() {
		if (sendToOtherPlayersBox == null) {
			sendToOtherPlayersBox = new JCheckBox("An andere Spieler senden");
		}
		return sendToOtherPlayersBox;
	}
	private JCheckBox getSendToMasterOrPlayerBox() {
		if (sendToMasterOrPlayerBox == null) {
			sendToMasterOrPlayerBox = new JCheckBox("An Meister senden");
			sendToMasterOrPlayerBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (sendToMasterOrPlayerBox.isSelected()) {
						sendToOtherPlayersBox.setEnabled(true);
					}
					else {
						sendToOtherPlayersBox.setEnabled(false);
						sendToOtherPlayersBox.setSelected(false);
					}
				}
			});
			sendToMasterOrPlayerBox.setSelected(true);
			sendToMasterOrPlayerBox.setEnabled(false);
		}
		return sendToMasterOrPlayerBox;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

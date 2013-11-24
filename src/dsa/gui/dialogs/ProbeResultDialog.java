package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;

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

public class ProbeResultDialog extends BGDialog {

	private JPanel jContentPane = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel3 = null;
	private JButton closeButton = null;
	private JButton copyButton = null;
	
	public static void showDialog(Container parent, String message, String title)
	{
		if (parent instanceof Dialog)
			showDialog((Dialog)parent, message, title);
		else
			showDialog((Frame)parent, message, title);
	}
	
	public static void showDialog(Frame parent, String message, String title)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setVisible(true);
	}

	public static void showDialog(Frame parent, String message, String title, ImageIcon icon)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent, icon);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setVisible(true);
	}

	public static void showDialog(Dialog parent, String message, String title)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setVisible(true);
	}

	public static void showDialog(Dialog parent, String message, String title, ImageIcon icon)
	{
		ProbeResultDialog dialog = new ProbeResultDialog(parent, icon);
		dialog.setTitle(title);
		dialog.setText(message);
		dialog.setVisible(true);
	}

	/**
	 * This method initializes 
	 * 
	 */
	public ProbeResultDialog() {
		super();
		initialize();
	}
	
	public ProbeResultDialog(Frame parent) {
		super(parent);
		initialize();
	}
	
	public ProbeResultDialog(Frame parent, ImageIcon icon) {
		super(parent);
		this.icon = icon;
		initialize();
	}
	
	public ProbeResultDialog(Dialog parent) {
		super(parent);
		initialize();
	}

	public ProbeResultDialog(Dialog parent, ImageIcon icon) {
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

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(352, 203));
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
			jPanel.setLayout(new FlowLayout());
			jPanel.setPreferredSize(new Dimension(120, 40));
			jPanel.add(getCopyButton(), null);
			jPanel.add(getCloseButton(), null);
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
					dispose();
				}
			});
		}
		return closeButton;
	}

	/**
	 * This method initializes copyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCopyButton() {
		if (copyButton == null) {
			copyButton = new JButton();
			copyButton.setText("Kopieren");
			copyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					copyText();
				}
			});
		}
		return copyButton;
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

}  //  @jve:decl-index=0:visual-constraint="10,10"

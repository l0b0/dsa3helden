package dsa.gui.dialogs.fighting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dsa.gui.lf.BGDialog;
import dsa.remote.RemoteManager;

import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HitDialog extends BGDialog {

	private final JPanel contentPanel = new JPanel();
	
	public boolean sendToGM() {
		return sendToGMBox.isSelected();
	}
	
	public boolean sendToOthers() {
		return sendToOthersBox.isSelected();
	}
	
	public int getTP() {
		return ((SpinnerNumberModel)tpSpinner.getModel()).getNumber().intValue();
	}
	
	public boolean wasConfirmed() {
		return confirmed;
	}
	
	public void setDefaultTP(int tp) {
		((SpinnerNumberModel)tpSpinner.getModel()).setValue(tp);
	}
	
	private JCheckBox sendToGMBox;
	private JCheckBox sendToOthersBox;
	private JSpinner tpSpinner;
	private boolean confirmed = false;
	private JButton cancelButton;
	private JButton okButton;

	/**
	 * Create the dialog.
	 */
	public HitDialog(JFrame parent) {
		super(parent, true);
		setTitle("Treffer");
		setBounds(100, 100, 272, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JPanel panel = new JPanel();
			panel.setAlignmentY(Component.TOP_ALIGNMENT);
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			contentPanel.add(panel);
			{
				JLabel lblNewLabel = new JLabel("TrefferPunkte:");
				panel.add(lblNewLabel);
			}
			{
				tpSpinner = new JSpinner();
				tpSpinner.setModel(new SpinnerNumberModel(0, 0, 200, 1));
				panel.add(tpSpinner);
			}
		}
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			contentPanel.add(verticalStrut);
		}
		{
			sendToGMBox = new JCheckBox("An Meister senden");
			sendToGMBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					sendToOthersBox.setEnabled(sendToGMBox.isSelected());
					if (!sendToGMBox.isSelected()) {
						sendToOthersBox.setSelected(false);
					}
				}
			});
			contentPanel.add(sendToGMBox);
		}
		{
			Component verticalStrut = Box.createVerticalStrut(5);
			contentPanel.add(verticalStrut);
		}
		{
			sendToOthersBox = new JCheckBox("An andere Spieler senden");
			contentPanel.add(sendToOthersBox);
		}
		{
			Component verticalGlue = Box.createVerticalGlue();
			contentPanel.add(verticalGlue);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						confirmed = true;
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				okButton.setPreferredSize(new Dimension(101, 21));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Abbrechen");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						confirmed = false;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				cancelButton.setPreferredSize(new Dimension(101, 21));
				buttonPane.add(cancelButton);
			}
		}
		setEscapeButton(cancelButton);
		getRootPane().setDefaultButton(okButton);
		pack();
		setLocationRelativeTo(parent);
		
		boolean allowRemote = (RemoteManager.getInstance().isConnected());
		sendToGMBox.setEnabled(allowRemote && RemoteManager.getInstance().isConnectedAsGM());
		sendToGMBox.setSelected(allowRemote);
		sendToOthersBox.setEnabled(allowRemote);
		sendToOthersBox.setSelected(allowRemote);
	}

	@Override
	public String getHelpPage() {
		return "Kampf (Spieler)";
	}

}

/*

 Copyright (c) 2006-2008 [Joerg Ruedenauer]
 
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
package dsa.gui.dialogs;

import dsa.control.Regeneration;
import dsa.gui.lf.BGDialog;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

public class RegenerationDialog extends BGDialog {

	private JPanel jContentPane = null;
	private JRadioButton normalButton = null;
	private JRadioButton halfButton = null;
	private JRadioButton changedButton = null;
	private JLabel jLabel = null;
	private JSpinner changeSpinner = null;
	private JCheckBox jCheckBox = null;
	private JButton cancelButton = null;
	private JButton okButton = null;
	
	  private boolean closedByOK = false;
	  
	  public boolean wasClosedByOK() {
	    return closedByOK;
	  }

	/**
	 * This method initializes 
	 * 
	 */
	public RegenerationDialog() {
		super();
		initialize();
	}
	
	public RegenerationDialog(JFrame parent)
	{
		super(parent);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(311, 237));
        this.setContentPane(getJContentPane());
        this.setTitle("Regeneration");
        this.setModal(true);
        this.setEscapeButton(getCancelButton());
        this.getRootPane().setDefaultButton(getOkButton());
        this.setLocationRelativeTo(getParent());			
	}

	@Override
	public String getHelpPage() {
		return "Regeneration";
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(30, 100, 103, 21));
			jLabel.setText("Veränderung:");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getNormalButton(), null);
			jContentPane.add(getHalfButton(), null);
			jContentPane.add(getChangedButton(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(getChangeSpinner(), null);
			jContentPane.add(getJCheckBox(), null);
			jContentPane.add(getCancelButton(), null);
			jContentPane.add(getOkButton(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes normalButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getNormalButton() {
		if (normalButton == null) {
			normalButton = new JRadioButton();
			normalButton.setBounds(new Rectangle(10, 10, 241, 21));
			normalButton.setSelected(true);
			normalButton.setText("Normale Regeneration");
			normalButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (normalButton.isSelected())
					{
						getHalfButton().setSelected(false);
						getChangedButton().setSelected(false);
					}
				}
			});
		}
		return normalButton;
	}

	/**
	 * This method initializes halfButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getHalfButton() {
		if (halfButton == null) {
			halfButton = new JRadioButton();
			halfButton.setBounds(new Rectangle(10, 40, 251, 21));
			halfButton.setText("Halbierte Regeneration");
			halfButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (halfButton.isSelected())
					{
						getNormalButton().setSelected(false);
						getChangedButton().setSelected(false);
					}
				}
			});
		}
		return halfButton;
	}

	/**
	 * This method initializes changedButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getChangedButton() {
		if (changedButton == null) {
			changedButton = new JRadioButton();
			changedButton.setBounds(new Rectangle(10, 70, 281, 21));
			changedButton.setText("Regeneration mit Abzug / Zuschlag");
			changedButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					changeSpinner.setEnabled(changedButton.isSelected());
					if (changedButton.isSelected())
					{
						getNormalButton().setSelected(false);
						getHalfButton().setSelected(false);
					}
				}
			});
		}
		return changedButton;
	}

	/**
	 * This method initializes changeSpinner	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JSpinner getChangeSpinner() {
		if (changeSpinner == null) {
			changeSpinner = new JSpinner();
			changeSpinner.setBounds(new Rectangle(140, 100, 61, 21));
			changeSpinner.setEnabled(false);
			changeSpinner.setModel(new SpinnerNumberModel(0, -5, 5, 1));
		}
		return changeSpinner;
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setBounds(new Rectangle(10, 130, 271, 21));
			jCheckBox.setText("Regeneration für gesamte Gruppe");
		}
		return jCheckBox;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setBounds(new Rectangle(180, 170, 111, 21));
			cancelButton.setText("Abbrechen");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closedByOK = false;
					dispose();
				}
			});
		}
		return cancelButton;
	}
	
	private Regeneration.RegenerationOptions options = null;
	
	public Regeneration.RegenerationOptions getOptions()
	{
		return options;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setBounds(new Rectangle(60, 170, 111, 21));
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					closedByOK = true;
					Regeneration.RegenerationType rType = Regeneration.RegenerationType.Normal;
					if (getHalfButton().isSelected())
						rType = Regeneration.RegenerationType.Halfed;
					else if (getChangedButton().isSelected())
						rType = Regeneration.RegenerationType.Changed;
					options = new Regeneration.RegenerationOptions(rType, ((Number)changeSpinner.getValue()).intValue(), getJCheckBox().isSelected());
					dispose();
				}
			});
		}
		return okButton;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

package dsa.gui.dialogs;

import java.awt.Frame;

import dsa.control.Dice;
import dsa.gui.lf.BGDialog;
import dsa.model.characters.Group;
import dsa.remote.RemoteManager;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class DiceRollDialog extends BGDialog {

	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JSpinner numberSpinner1 = null;
	private JLabel jLabel = null;
	private JComboBox diceBox1 = null;
	private JSpinner numberSpinner2 = null;
	private JSpinner numberSpinner3 = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JComboBox diceBox2 = null;
	private JComboBox diceBox3 = null;
	private JButton cancelButton = null;
	private JButton rollButton = null;


	public DiceRollDialog(Frame owner) {
		super(owner, "Würfeln", true);
		initialize();
		getNumberSpinner1().setValue(1);
		getDiceBox1().setSelectedItem("20");
		getDiceBox2().setSelectedItem("6");
		getDiceBox3().setSelectedItem("100");
	}


	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(230, 208));
        this.setTitle("Würfeln");
        this.setContentPane(getJContentPane());
        this.setLocationRelativeTo(getParent());
        this.getRootPane().setDefaultButton(getRollButton());
        setEscapeButton(getCancelButton());			
	}


	@Override
	public String getHelpPage() {
		return "Wuerfeln";
	}


	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getCancelButton(), null);
			jContentPane.add(getRollButton(), null);
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
			jLabel2 = new JLabel();
			jLabel2.setBounds(new Rectangle(70, 90, 31, 21));
			jLabel2.setText("W");
			jLabel1 = new JLabel();
			jLabel1.setBounds(new Rectangle(70, 60, 31, 21));
			jLabel1.setText("W");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(70, 30, 31, 21));
			jLabel.setText("W");
			jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBounds(new Rectangle(13, 11, 198, 120));
			jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Würfel wählen", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(getNumberSpinner1(), null);
			jPanel.add(jLabel, null);
			jPanel.add(getDiceBox1(), null);
			jPanel.add(getNumberSpinner2(), null);
			jPanel.add(getNumberSpinner3(), null);
			jPanel.add(jLabel1, null);
			jPanel.add(jLabel2, null);
			jPanel.add(getDiceBox2(), null);
			jPanel.add(getDiceBox3(), null);
		}
		return jPanel;
	}


	/**
	 * This method initializes numberSpinner1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JSpinner getNumberSpinner1() {
		if (numberSpinner1 == null) {
			numberSpinner1 = new JSpinner();
			numberSpinner1.setBounds(new Rectangle(10, 30, 51, 21));
			numberSpinner1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 20, 1));
		}
		return numberSpinner1;
	}


	/**
	 * This method initializes diceBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiceBox1() {
		if (diceBox1 == null) {
			diceBox1 = new JComboBox();
			diceBox1.setBounds(new Rectangle(110, 30, 61, 21));
			diceBox1.addItem("2");
			diceBox1.addItem("3");
			diceBox1.addItem("4");
			diceBox1.addItem("6");
			diceBox1.addItem("8");
			diceBox1.addItem("10");
			diceBox1.addItem("12");
			diceBox1.addItem("20");
			diceBox1.addItem("100");
		}
		return diceBox1;
	}


	/**
	 * This method initializes numberSpinner2	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getNumberSpinner2() {
		if (numberSpinner2 == null) {
			numberSpinner2 = new JSpinner();
			numberSpinner2.setBounds(new Rectangle(11, 61, 50, 20));
			numberSpinner2.setModel(new SpinnerNumberModel(0, 0, 20, 1));
		}
		return numberSpinner2;
	}


	/**
	 * This method initializes numberSpinner3	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getNumberSpinner3() {
		if (numberSpinner3 == null) {
			numberSpinner3 = new JSpinner();
			numberSpinner3.setBounds(new Rectangle(10, 90, 51, 21));
			numberSpinner3.setModel(new SpinnerNumberModel(0, 0, 20, 1));
		}
		return numberSpinner3;
	}


	/**
	 * This method initializes diceBox2	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiceBox2() {
		if (diceBox2 == null) {
			diceBox2 = new JComboBox();
			diceBox2.setBounds(new Rectangle(110, 60, 61, 21));
			diceBox2.addItem("2");
			diceBox2.addItem("3");
			diceBox2.addItem("4");
			diceBox2.addItem("6");
			diceBox2.addItem("8");
			diceBox2.addItem("10");
			diceBox2.addItem("12");
			diceBox2.addItem("20");
			diceBox2.addItem("100");
		}
		return diceBox2;
	}


	/**
	 * This method initializes diceBox3	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiceBox3() {
		if (diceBox3 == null) {
			diceBox3 = new JComboBox();
			diceBox3.setBounds(new Rectangle(110, 90, 61, 21));
			diceBox3.addItem("2");
			diceBox3.addItem("3");
			diceBox3.addItem("4");
			diceBox3.addItem("6");
			diceBox3.addItem("8");
			diceBox3.addItem("10");
			diceBox3.addItem("12");
			diceBox3.addItem("20");
			diceBox3.addItem("100");
		}
		return diceBox3;
	}


	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setBounds(new Rectangle(120, 140, 91, 21));
		    cancelButton.setText("Abbruch");
		    cancelButton.setMnemonic(java.awt.event.KeyEvent.VK_A);
		      cancelButton.addActionListener(new java.awt.event.ActionListener() {
		          public void actionPerformed(java.awt.event.ActionEvent e) {
		            DiceRollDialog.this.dispose();
		          }
		        });
		}
		return cancelButton;
	}


	/**
	 * This method initializes rollButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRollButton() {
		if (rollButton == null) {
			rollButton = new JButton();
			rollButton.setBounds(new Rectangle(20, 140, 91, 21));
			rollButton.setText("Würfeln!");
		    rollButton.setMnemonic(java.awt.event.KeyEvent.VK_W);
		      rollButton.addActionListener(new java.awt.event.ActionListener() {
		          public void actionPerformed(java.awt.event.ActionEvent e) {
		            DiceRollDialog.this.rollDice();
		          }
		        });
		}
		return rollButton;
	}
	
	private void rollDice() {
		int nrOfDice = 0;
		int sum = 0;
		StringBuilder result = new StringBuilder();
		int nr = ((Number)getNumberSpinner1().getValue()).intValue();
		if (nr > 0)
		{
			sum += rollDice(nr, getDiceBox1().getSelectedItem().toString(), result);
			nrOfDice += nr;
		}
		nr = ((Number)getNumberSpinner2().getValue()).intValue();
		if (nr > 0)
		{
			sum += rollDice(nr, getDiceBox2().getSelectedItem().toString(), result);
			nrOfDice += nr;
		}
		nr = ((Number)getNumberSpinner3().getValue()).intValue();
		if (nr > 0)
		{
			sum += rollDice(nr, getDiceBox3().getSelectedItem().toString(), result);
			nrOfDice += nr;
		}
		if (nrOfDice == 0) {
			JOptionPane.showMessageDialog(getParent(), "Keine Würfel ausgewählt!", "Würfeln", JOptionPane.PLAIN_MESSAGE);
		}
		else {
			dispose();
			if (nrOfDice > 1) {
				result.append("\nSumme: " + sum);
			}
			int dialogResult = ProbeResultDialog.showDialog(getParent(), result.toString(), "Würfeln", true);
			boolean sendToServer = (dialogResult & ProbeResultDialog.SEND_TO_SINGLE) != 0;
			boolean informOtherPlayers = (dialogResult & ProbeResultDialog.SEND_TO_ALL) != 0;
			if (sendToServer) {
				RemoteManager.getInstance().informOfProbe(Group.getInstance().getActiveHero(), result.toString(), informOtherPlayers);
			}
		}
	}
	
	private int rollDice(int nr, String dice, StringBuilder result) {
		int diceNr = Integer.parseInt(dice);
		if (result.length() > 0)
			result.append("\n");
		result.append("" +  nr + " W" + diceNr + ":");
		int sum = 0;
		for (int i = 0; i < nr; ++i) {
			int res = Dice.roll(diceNr);
			sum += res;
			result.append(" " + res);
		}
		return sum;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

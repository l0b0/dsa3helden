package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;

public class NewStepDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JPanel jPanel = null;
  private JLabel jLabel = null;
  private JButton okButton = null;
  
  private boolean automatic = false;
  private boolean showLog = false;
  private String heroName;
  private int heroStep;
  private JPanel jPanel1 = null;
  private JLabel introLabel = null;
  private JLabel jLabel3 = null;
  private JRadioButton manualButton = null;
  private JRadioButton autoButton = null;
  private JCheckBox logBox = null;
  
  /**
   * This method initializes 
   * 
   */
  public NewStepDialog(JFrame parent, String name, int step) {
  	super(parent);
    heroName = name;
    heroStep = step;
  	initialize();
  }
  
  public boolean isAutomaticSelected() {
    return automatic;
  }
  
  public boolean shallShowLog() {
    return showLog;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(405, 203));
    this.setContentPane(getJContentPane());
    this.setTitle("Heldenverwaltung");
    this.setModal(true);
    this.setLocationRelativeTo(getParent());
    this.getRootPane().setDefaultButton(okButton);
  }

  public String getHelpPage() {
    return "Automatisch_Steigern";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new Dimension(15, 10));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), BorderLayout.SOUTH);
      jContentPane.add(jLabel, BorderLayout.WEST);
      jContentPane.add(getJPanel1(), BorderLayout.CENTER);
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
      jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
      jPanel.setPreferredSize(new Dimension(0, 40));
      jPanel.add(getOkButton(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setMnemonic(KeyEvent.VK_O);
      okButton.setText("OK");
      okButton.setDefaultCapable(true);
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showLog = logBox.isSelected();
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jLabel3 = new JLabel();
      jLabel3.setBounds(new Rectangle(10, 40, 292, 21));
      jLabel3.setText("Wie soll die Steigerung gemacht werden?");
      introLabel = new JLabel();
      introLabel.setBounds(new Rectangle(10, 10, 359, 21));
      introLabel.setText(heroName + " ist gerade auf Stufe " + heroStep + " gestiegen!");
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.add(introLabel, null);
      jPanel1.add(jLabel3, null);
      jPanel1.add(getManualButton(), null);
      jPanel1.add(getAutoButton(), null);
      jPanel1.add(getLogBox(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes manualButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getManualButton() {
    if (manualButton == null) {
      manualButton = new JRadioButton();
      manualButton.setBounds(new Rectangle(10, 70, 121, 21));
      manualButton.setSelected(true);
      manualButton.setText("Manuell");
      manualButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          autoButton.setSelected(false);
          automatic = false;
          logBox.setEnabled(false);
        }
      });
    }
    return manualButton;
  }

  /**
   * This method initializes autoButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getAutoButton() {
    if (autoButton == null) {
      autoButton = new JRadioButton();
      autoButton.setBounds(new Rectangle(10, 100, 111, 21));
      autoButton.setText("Automatisch");
      autoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          manualButton.setSelected(false);
          automatic = true;
          logBox.setEnabled(true);
        }
      });
    }
    return autoButton;
  }

  /**
   * This method initializes logBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getLogBox() {
    if (logBox == null) {
      logBox = new JCheckBox();
      logBox.setBounds(new Rectangle(139, 101, 212, 21));
      logBox.setEnabled(false);
      logBox.setSelected(true);
      logBox.setText("Nachher Log anzeigen");
    }
    return logBox;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

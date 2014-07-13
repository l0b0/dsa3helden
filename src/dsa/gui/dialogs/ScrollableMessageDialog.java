package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.SystemColor;

public class ScrollableMessageDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JPanel jPanel = null;
  private JButton okButton = null;
  private JLabel jLabel = null;
  private JLabel jLabel1 = null;
  private JLabel jLabel2 = null;
  private JScrollPane jScrollPane = null;
  private JTextArea messageArea = null;
  
  private String text;
  private String helpPage;

  /**
   * This method initializes 
   * 
   */
  public ScrollableMessageDialog(JFrame parent, String text, String helpPage) {
  	super();
    this.text = text;
    this.helpPage = helpPage;
  	initialize();
  }

  public ScrollableMessageDialog(JDialog parent, String text, String helpPage) {
    super();
    this.text = text;
    this.helpPage = helpPage;
    initialize();
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(424, 300));
    this.setTitle("Heldenverwaltung");
    this.setContentPane(getJContentPane());
    this.setModal(true);
    this.setLocationRelativeTo(getParent());
  }

  public String getHelpPage() {
    return helpPage;
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel2 = new JLabel();
      jLabel2.setText("");
      jLabel2.setPreferredSize(new Dimension(10, 10));
      jLabel1 = new JLabel();
      jLabel1.setText("");
      jLabel1.setPreferredSize(new Dimension(15, 15));
      jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new Dimension(15, 15));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), BorderLayout.SOUTH);
      jContentPane.add(jLabel, BorderLayout.WEST);
      jContentPane.add(jLabel1, BorderLayout.EAST);
      jContentPane.add(jLabel2, BorderLayout.NORTH);
      jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
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
      jPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
      jPanel.setPreferredSize(new Dimension(40, 45));
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
      okButton.setText("OK");
      okButton.setPreferredSize(new Dimension(80, 25));
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes jScrollPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getJScrollPane() {
    if (jScrollPane == null) {
      jScrollPane = new JScrollPane();
      jScrollPane.setViewportView(getMessageArea());
      jScrollPane.setOpaque(false);
    }
    return jScrollPane;
  }

  /**
   * This method initializes messageArea	
   * 	
   * @return javax.swing.JTextArea	
   */
  private JTextArea getMessageArea() {
    if (messageArea == null) {
      messageArea = new JTextArea();
      messageArea.setEditable(false);
      messageArea.setWrapStyleWord(true);
      messageArea.setBackground(SystemColor.window);
      messageArea.setText(text);
      messageArea.setOpaque(false);
    }
    return messageArea;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

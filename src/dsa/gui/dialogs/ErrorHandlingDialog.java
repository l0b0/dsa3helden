/*
    Copyright (c) 2006 [Joerg Ruedenauer]
  
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
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

public class ErrorHandlingDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JTextArea jTextArea = null;

  private JButton mailButton = null;

  private JButton continueButton = null;

  private JButton quitButton = null;

  private Exception error;

  /**
   * This method initializes
   * 
   */
  public ErrorHandlingDialog() {
    super();
    initialize();
  }

  public ErrorHandlingDialog(Exception error) {
    super();
    initialize();
    String text = "Ein unerwarteter Fehler ist aufgetreten. Dies ist"
        + " wahrscheinlich ein Bug im Programm.\nBitte informiere mich per "
        + " Mail darüber und schreibe dazu, was Du als letztes im Programm gemacht hast.\n"
        + " \nDu kannst versuchen, weiterzumachen, es kann dann allerdings zu Folgefehlern "
        + " kommen. Ich empfehle daher, das Programm entweder sofort zu beenden oder veränderte"
        + " Helden in neue Dateien abzuspeichern und dann das Programm neu zu starten.\n"
        + "\nFehlerinformation: " + error.toString();
    getJTextArea().setText(text);
    this.error = error;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(439, 299));
    this.setContentPane(getJContentPane());
    this.setTitle("Heldenverwaltung: Fehler im Programm");
    this.setLocationByPlatform(true);
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
      jContentPane.add(getJTextArea(), null);
      jContentPane.add(getMailButton(), null);
      jContentPane.add(getContinueButton(), null);
      jContentPane.add(getQuitButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jTextArea
   * 
   * @return javax.swing.JTextArea
   */
  private JTextArea getJTextArea() {
    if (jTextArea == null) {
      jTextArea = new JTextArea();
      jTextArea.setBounds(new java.awt.Rectangle(10, 9, 401, 172));
      jTextArea.setEditable(false);
      jTextArea.setWrapStyleWord(true);
      jTextArea.setLineWrap(true);
    }
    return jTextArea;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getMailButton() {
    if (mailButton == null) {
      mailButton = new JButton();
      mailButton.setBounds(new java.awt.Rectangle(121, 195, 151, 26));
      mailButton.setText("E-Mail senden");
      mailButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          sendMail();
        }
      });
    }
    return mailButton;
  }

  protected void sendMail() {
    try {
      Message message = new Message();
      message.setSubject("Fehler in Heldenverwaltung "
          + dsa.control.Version.getCurrentVersionString());
      java.util.ArrayList<String> tos = new java.util.ArrayList<String>();
      tos.add("joerg@ruedenauer.net");
      message.setToAddrs(tos);
      String body = "Ich habe in der Heldenverwaltung gerade: \n\n\nAls folgender Fehler aufgetreten ist:\n"
          + "Fehlermeldung: "
          + error.getLocalizedMessage()
          + "\nStack trace:\n";
      StackTraceElement[] stackTrace = error.getStackTrace();
      for (StackTraceElement ste : stackTrace) {
        body += ste.toString();
      }
      message.setBody(body);
      Desktop.mail(message);
    }
    catch (org.jdesktop.jdic.desktop.DesktopException ex) {
      JOptionPane.showMessageDialog(this,
          "Die E-Mail kann nicht erstellt werden. Fehler:\n" + ex.getMessage()
              + "\nBitte schreibe manuell an joerg@ruedenauer.net.", "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getContinueButton() {
    if (continueButton == null) {
      continueButton = new JButton();
      continueButton.setBounds(new java.awt.Rectangle(11, 235, 191, 26));
      continueButton.setText("Versuchen, fortzusetzen");
      continueButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          ErrorHandlingDialog.this.dispose();
        }
      });
    }
    return continueButton;
  }

  /**
   * This method initializes jButton2
   * 
   * @return javax.swing.JButton
   */
  private JButton getQuitButton() {
    if (quitButton == null) {
      quitButton = new JButton();
      quitButton.setBounds(new java.awt.Rectangle(220, 235, 191, 26));
      quitButton.setText("Programm beenden");
      quitButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          ErrorHandlingDialog.this.dispose();
          System.exit(1);
        }
      });
    }
    return quitButton;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

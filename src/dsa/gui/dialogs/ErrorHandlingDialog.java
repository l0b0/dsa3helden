/*
    Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import dsa.gui.lf.BGDialog;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

public final class ErrorHandlingDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JTextArea jTextArea = null;

  private JButton mailButton = null;

  private JButton continueButton = null;

  private JButton quitButton = null;

  private Exception error;

  private JButton copyButton = null;

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
  
  public String getHelpPage() {
    return "Fehler";
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
    this.getRootPane().setDefaultButton(getMailButton());
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
      jContentPane.add(getJButton(), null);
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
      mailButton.setBounds(new java.awt.Rectangle(50,195,150,25));
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
      StringBuilder mailURI = new StringBuilder();
      mailURI.append("joerg@ruedenauer.net?subject=");
      mailURI.append("Fehler in Heldenverwaltung ");
      mailURI.append(dsa.control.Version.getCurrentVersionString());
      mailURI.append("&body=Ich habe in der Heldenverwaltung gerade: %0D%0A%0D%0A%0D%0A");
      mailURI.append("Als folgender Fehler aufgetreten ist:%0D%0A");
      mailURI.append("Fehlermeldung: ");
      mailURI.append(error.getLocalizedMessage());
      mailURI.append("%0D%0AStack trace:%0D%0A");
      StackTraceElement[] stackTrace = error.getStackTrace();
      for (StackTraceElement ste : stackTrace) {
        mailURI.append(ste.toString());
      }
      URI uri = new URI("mailto", mailURI.toString(), null);
      if (Desktop.isDesktopSupported()) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.MAIL)) {
          desktop.mail(uri);
        }
      }
    }
    catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
          "Die E-Mail kann nicht erstellt werden. Fehler:\n" + ex.getMessage()
              + "\nBitte schreibe manuell an joerg@ruedenauer.net.", "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
    JOptionPane.showMessageDialog(this,
        "Die E-Mail kann nicht erstellt werden -- das Betriebssystem unterstützt keinen automatischen Mail-Versand."
            + "\nBitte schreibe manuell an joerg@ruedenauer.net.", "Fehler",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getContinueButton() {
    if (continueButton == null) {
      continueButton = new JButton();
      continueButton.setBounds(new java.awt.Rectangle(10, 235, 190, 25));
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
      quitButton.setBounds(new java.awt.Rectangle(220, 235, 190, 25));
      quitButton.setText("Programm beenden");
      quitButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          ErrorHandlingDialog.this.dispose();
          dsa.control.Main.exit(1);
        }
      });
    }
    return quitButton;
  }

  /**
   * This method initializes jButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getJButton() {
    if (copyButton == null) {
      copyButton = new JButton();
      copyButton.setBounds(new java.awt.Rectangle(220,195,175,25));
      copyButton.setText("In Zw.Ablage kopieren");
      copyButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          copyToClipboard();
        }
      });
    }
    return copyButton;
  }
  
  private static final class TextTransfer implements ClipboardOwner {

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
    
    public void setClipboardContents(String aString) {
      StringSelection stringSelection = new StringSelection(aString);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, this);
    }
    
  }

  private void copyToClipboard() {
    String text = "Fehler aufgetreten in Heldenverwaltung "
      + dsa.control.Version.getCurrentVersionString()
      + "\nFehlermeldung: "
      + error.getLocalizedMessage()
      + "\nStack trace:\n";
    StackTraceElement[] stackTrace = error.getStackTrace();
    for (StackTraceElement ste : stackTrace) {
      text += ste.toString() + "\n";
    }
    TextTransfer tt = new TextTransfer();
    tt.setClipboardContents(text);
  }

} // @jve:decl-index=0:visual-constraint="10,10"

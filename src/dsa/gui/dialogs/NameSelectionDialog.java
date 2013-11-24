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

import javax.swing.JPanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import dsa.gui.lf.BGDialog;

public class NameSelectionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private String name = null;

  private NameSelectionPanel panel = null;
  
  public String getHelpPage() {
    return "Namen";
  }

  public NameSelectionDialog() {
    super();
    initialize();
  }

  public NameSelectionDialog(Frame owner) {
    super(owner);
    initialize();
  }

  public NameSelectionDialog(Frame owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public NameSelectionDialog(Frame owner, String title) {
    super(owner, title);
    initialize();
  }

  public NameSelectionDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public NameSelectionDialog(Frame owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }

  public NameSelectionDialog(Dialog owner) {
    super(owner);
    initialize();
  }

  public NameSelectionDialog(Dialog owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public NameSelectionDialog(Dialog owner, String title) {
    super(owner, title);
    initialize();
  }

  public NameSelectionDialog(Dialog owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public NameSelectionDialog(Dialog owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(386, 316);
    this.setTitle("Namen generieren");
    this.setContentPane(getJContentPane());
  }

  public void setCharacterName(String aName) {
    getPanel().setCharacterName(aName);
  }

  public String getCharacterName() {
    return name;
  }

  public void setDefaultRegion(String region) {
    getPanel().setDefaultRegion(region);
  }

  private boolean female = false;

  public boolean isFemale() {
    return female;
  }

  public void setSex(boolean isFemale) {
    getPanel().setSex(isFemale);
  }

  public String getNativeTongue() {
    return getPanel().getNativeTongue();
  }

  private NameSelectionPanel getPanel() {
    if (panel == null) {
      panel = new NameSelectionPanel();
    }
    return panel;
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
      jContentPane.add(getPanel(), null);
      jContentPane.add(getOKButton(), null);
      jContentPane.add(getCancelButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(54, 253, 105, 19));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          name = getPanel().getCharacterName();
          if (name.length() == 0) {
            javax.swing.JOptionPane.showMessageDialog(NameSelectionDialog.this,
                "Der Charakter muss einen Namen haben!", "Fehler",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            name = null;
            return;
          }
          female = getPanel().isFemale();
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new java.awt.Rectangle(213, 253, 105, 19));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          name = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

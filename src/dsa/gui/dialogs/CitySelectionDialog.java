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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JScrollPane;
import javax.swing.JList;

import dsa.gui.lf.BGDialog;
import dsa.gui.lf.BGList;
import dsa.model.data.Cities;

public class CitySelectionDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JComboBox regionCombo = null;

  private JPanel jPanel1 = null;

  private JButton okButton = null;

  private JButton cancelButton = null;

  private JPanel jPanel2 = null;

  private JScrollPane jScrollPane = null;

  private JList cityList = null;

  public CitySelectionDialog() {
    super();
    initialize();
  }

  public CitySelectionDialog(Frame owner) {
    super(owner);
    initialize();
  }

  public CitySelectionDialog(Frame owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public CitySelectionDialog(Frame owner, String title) {
    super(owner, title);
    initialize();
  }

  public CitySelectionDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public CitySelectionDialog(Frame owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }

  public CitySelectionDialog(Dialog owner) {
    super(owner);
    initialize();
  }

  public CitySelectionDialog(Dialog owner, boolean modal) {
    super(owner, modal);
    initialize();
  }

  public CitySelectionDialog(Dialog owner, String title) {
    super(owner, title);
    initialize();
  }

  public CitySelectionDialog(Dialog owner, String title, boolean modal) {
    super(owner, title, modal);
    initialize();
  }

  public CitySelectionDialog(Dialog owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
    initialize();
  }
  
  public String getHelpPage() {
    return "Staedte";
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setSize(350, 267);
    this.setTitle("Stadtauswahl");
    this.setContentPane(getJContentPane());
    for (String r : Cities.getInstance().getRegions()) {
      getRegionCombo().addItem(r);
    }
    getRegionCombo().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        fillListBox();
      }
    });
    if (getRegionCombo().getItemCount() > 0) {
      getRegionCombo().setSelectedIndex(0);
      fillListBox();
    }
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
      jContentPane.add(getJPanel1(), java.awt.BorderLayout.SOUTH);
      jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(10, 10));
      jContentPane.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
      l2.setPreferredSize(new java.awt.Dimension(10, 10));
      jContentPane.add(l2, BorderLayout.EAST);
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
      FlowLayout flowLayout2 = new FlowLayout();
      flowLayout2.setVgap(10);
      jPanel = new JPanel();
      jPanel.setLayout(flowLayout2);
      jPanel.add(getRegionCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getRegionCombo() {
    if (regionCombo == null) {
      regionCombo = new JComboBox();
      regionCombo.setPreferredSize(new java.awt.Dimension(250, 25));
    }
    return regionCombo;
  }

  /**
   * This method initializes jPanel1
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      FlowLayout flowLayout = new FlowLayout();
      flowLayout.setHgap(15);
      flowLayout.setVgap(10);
      jPanel1 = new JPanel();
      jPanel1.setLayout(flowLayout);
      jPanel1.add(getOkButton(), null);
      jPanel1.add(getCancelButton(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setText("OK");
      okButton.setPreferredSize(new java.awt.Dimension(100, 25));
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (getCityList().getSelectedValue() != null) {
            selectedCity = getCityList().getSelectedValue().toString();
          }
          else
            selectedCity = null;
          dispose();
        }
      });
    }
    return okButton;
  }

  String selectedCity = null;

  public String getSelectedCity() {
    return selectedCity;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setText("Abbrechen");
      cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectedCity = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes jPanel2
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel2() {
    if (jPanel2 == null) {
      BorderLayout flowLayout1 = new BorderLayout();
      flowLayout1.setHgap(20);
      flowLayout1.setVgap(10);
      jPanel2 = new JPanel();
      jPanel2.setLayout(flowLayout1);
      jPanel2.add(getJScrollPane(), BorderLayout.CENTER);
    }
    return jPanel2;
  }

  /**
   * This method initializes jScrollPane
   * 
   * @return javax.swing.JScrollPane
   */
  private JScrollPane getJScrollPane() {
    if (jScrollPane == null) {
      jScrollPane = new JScrollPane();
      jScrollPane.setViewportView(getCityList());
      jScrollPane.setOpaque(false);
      jScrollPane.getViewport().setOpaque(false);
    }
    return jScrollPane;
  }

  /**
   * This method initializes jList
   * 
   * @return javax.swing.JList
   */
  private JList getCityList() {
    if (cityList == null) {
      cityList = new BGList();
      cityList.setModel(new DefaultListModel());
      cityList
          .setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }
    return cityList;
  }

  private void fillListBox() {
    JList list = getCityList();
    ((DefaultListModel) list.getModel()).clear();
    String region = getRegionCombo().getSelectedItem().toString();
    for (String c : Cities.getInstance().getCities(region)) {
      ((DefaultListModel) list.getModel()).addElement(c);
    }
    if (list.getModel().getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }

} // @jve:decl-index=0:visual-constraint="10,10"

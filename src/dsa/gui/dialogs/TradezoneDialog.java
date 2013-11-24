/*
 Copyright (c) 2006-2009 [Joerg Ruedenauer]
 
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

import dsa.gui.lf.BGDialog;
import dsa.model.characters.Group;
import dsa.model.data.Tradezone;
import dsa.model.data.Tradezones;

import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class TradezoneDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JPanel jPanel = null;
  private JButton okButton = null;
  private JButton cancelButton = null;
  private JLabel jLabel = null;
  private JLabel jLabel1 = null;
  private JLabel jLabel2 = null;
  private JLabel jLabel3 = null;
  private JScrollPane jScrollPane = null;
  private JList zonesList = null;
  private final boolean selectCurrentRegion; 
  /**
   * This method initializes 
   * 
   */
  public TradezoneDialog(JFrame parent) {
  	super(parent);
  	this.tradeZones = null;
  	selectCurrentRegion = true;
  	initialize();
  	setLocationRelativeTo(parent);
  	fillList();
  }
  
  public TradezoneDialog(JDialog parent, String[] zones) {
    super(parent);
    tradeZones = new Tradezone[zones.length];
    for (int i = 0; i < zones.length; ++i) {
      tradeZones[i] = Tradezones.getInstance().getTradezone(zones[i]);
    }
    selectCurrentRegion = false;
    initialize();
    setLocationRelativeTo(parent);
    fillList();
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(309, 244));
    this.setTitle("Handelsregion");
    this.setContentPane(getJContentPane());
    this.getRootPane().setDefaultButton(getOkButton());
    this.setEscapeButton(getCancelButton());
    this.setModal(true);
  }
  
  private Tradezone[] tradeZones;
  private JPanel jPanel1 = null;
  private JLabel jLabel4 = null;
  private JLabel jLabel5 = null;
  private JLabel jLabel6 = null;
  
  private void fillList() {
    if (tradeZones == null) {
      tradeZones = Tradezones.getInstance().getTradezones();
    }
    DefaultListModel model = new DefaultListModel();
    for (Tradezone zone : tradeZones) {
      model.addElement(zone.getName());
    }
    zonesList.setModel(model);
    String currentZone = Group.getInstance().getTradezone();
    for (int i = 0; i < tradeZones.length; ++i) {
      if (currentZone.equals(tradeZones[i].getID())) {
        zonesList.setSelectedIndex(i);
        break;
      }
    }
  }

  /* (non-Javadoc)
   * @see dsa.gui.util.HelpProvider#getHelpPage()
   */
  @Override
  public String getHelpPage() {
    return "Handelsregion";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel6 = new JLabel();
      jLabel6.setText("");
      jLabel6.setPreferredSize(new Dimension(10, 10));
      jLabel5 = new JLabel();
      jLabel5.setText("");
      jLabel5.setPreferredSize(new Dimension(10, 10));
      jLabel4 = new JLabel();
      jLabel4.setText("");
      jLabel4.setPreferredSize(new Dimension(10, 10));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), BorderLayout.CENTER);
      jContentPane.add(getJPanel1(), BorderLayout.SOUTH);
      jContentPane.add(jLabel4, BorderLayout.NORTH);
      jContentPane.add(jLabel5, BorderLayout.EAST);
      jContentPane.add(jLabel6, BorderLayout.WEST);
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
      jLabel3 = new JLabel();
      jLabel3.setText("");
      jLabel3.setPreferredSize(new Dimension(10, 10));
      jLabel2 = new JLabel();
      jLabel2.setText("");
      jLabel2.setPreferredSize(new Dimension(10, 10));
      jLabel1 = new JLabel();
      jLabel1.setText("");
      jLabel1.setPreferredSize(new Dimension(10, 10));
      jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new Dimension(10, 10));
      jPanel = new JPanel();
      jPanel.setLayout(new BorderLayout());
      jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
          selectCurrentRegion ? "Aktuelle Region wählen" : "Herstellungsregion wählen", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
      jPanel.add(jLabel, BorderLayout.NORTH);
      jPanel.add(jLabel1, BorderLayout.WEST);
      jPanel.add(jLabel2, BorderLayout.EAST);
      jPanel.add(jLabel3, BorderLayout.SOUTH);
      jPanel.add(getJScrollPane(), BorderLayout.CENTER);
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
      okButton.setBounds(new Rectangle(30, 10, 100, 20));
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectedZone = tradeZones[zonesList.getSelectedIndex()].getID();
          dispose();
        }
      });
    }
    return okButton;
  }
  
  private String selectedZone = null;
  
  public String getSelectedZone()
  {
    return selectedZone;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setText("Abbrechen");
      cancelButton.setBounds(new Rectangle(160, 10, 100, 20));
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectedZone = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes jScrollPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JScrollPane getJScrollPane() {
    if (jScrollPane == null) {
      jScrollPane = new JScrollPane();
      jScrollPane.setViewportView(getZonesList());
    }
    return jScrollPane;
  }

  /**
   * This method initializes zonesList	
   * 	
   * @return javax.swing.JList	
   */
  private JList getZonesList() {
    if (zonesList == null) {
      zonesList = new JList();
      zonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    return zonesList;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setPreferredSize(new Dimension(300, 40));
      jPanel1.add(getCancelButton(), null);
      jPanel1.add(getOkButton(), null);
    }
    return jPanel1;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

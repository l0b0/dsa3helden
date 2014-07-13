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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsa.gui.lf.BGDialog;
import dsa.model.characters.Hero;

public class ThingsImportDialog extends BGDialog {

  private boolean wasClosedByOK;

  public ThingsImportDialog(JFrame parent) {
    super(parent);
    initialize();
    this.setLocationRelativeTo(parent);
  }
  
  public final String getHelpPage() {
    return "Gegenstaende_Importieren";
  }

  public boolean closedByOK() {
    return wasClosedByOK;
  }

  public long getSelectedThingTypes() {
    return selection;
  }

  final String[] names = {
      "Gegenstände",
      "Kleidung",
      "Waffen",
      "Rüstung",
      "Parade",
      "Lager"
  };
  final long[] flags = {
      Hero.THINGS,
      Hero.CLOTHES,
      Hero.WEAPONS,
      Hero.ARMOURS,
      Hero.SHIELDS,
      Hero.WAREHOUSE
  };

  private void initialize() {
    JPanel contentPane = new JPanel(new BorderLayout(5, 15));

    JPanel subPanel = new JPanel();
    subPanel.setLayout(null);

    JButton okButton = new JButton("OK");
    okButton.setBounds(5, 5, 100, 25);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getSelectionStatus();
        wasClosedByOK = true;
        dispose();
      }
    });
    subPanel.add(okButton, null);

    JButton cancelButton = new JButton("Abbrechen");
    cancelButton.setBounds(120, 5, 100, 25);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        wasClosedByOK = false;
        dispose();
      }
    });
    subPanel.add(cancelButton, null);

    subPanel.setPreferredSize(new java.awt.Dimension(180, 40));
    contentPane.add(subPanel, BorderLayout.SOUTH);

    JPanel topPanel = new JPanel();
    topPanel.setLayout(null);
    JLabel introLabel = new JLabel("Gegenstände aus der Datei importieren:");
    introLabel.setBounds(5, 5, 220, 25);
    topPanel.add(introLabel, null);
    Dimension preferredSize = introLabel.getPreferredSize();
    topPanel.setPreferredSize(new java.awt.Dimension(preferredSize.width + 10,
        preferredSize.height + 10));
    contentPane.add(topPanel, BorderLayout.NORTH);

    // JScrollPane centerPane = new JScrollPane();
    JPanel scrollPanel = new JPanel(new BorderLayout());

    JPanel centerPanel = new JPanel(new GridLayout(0, 1, 10, 3));
    for (int i = 0; i < names.length; ++i) {
      JCheckBox checkBox = new JCheckBox(names[i]);
      checkBox.setSelected(true);
      centerPanel.add(checkBox);
      checkBoxes.add(checkBox);
    }

    // centerPane.add(centerPanel);

    JLabel leftFiller = new JLabel();
    leftFiller.setPreferredSize(new java.awt.Dimension(10, 10));
    scrollPanel.add(leftFiller, BorderLayout.WEST);
    JLabel rightFiller = new JLabel();
    rightFiller.setPreferredSize(new java.awt.Dimension(10, 10));
    scrollPanel.add(rightFiller, BorderLayout.EAST);
    scrollPanel.add(centerPanel, BorderLayout.CENTER);

    contentPane.add(scrollPanel, BorderLayout.CENTER);

    this.setContentPane(contentPane);
    this.getRootPane().setDefaultButton(okButton);
    setEscapeButton(cancelButton);
    this.pack();
    this.setModal(true);
  }

  private void getSelectionStatus() {
    selection = 0;
    for (int i = 0; i < checkBoxes.size(); ++i) {
      if (checkBoxes.get(i).isSelected()) {
        selection |= flags[i];
      }
    }
  }

  private final ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

  private long selection;
}

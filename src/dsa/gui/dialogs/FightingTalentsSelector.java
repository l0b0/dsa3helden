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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import dsa.gui.lf.BGDialog;
import dsa.model.data.Talents;
import dsa.model.talents.Talent;

class FightingTalentsSelector extends BGDialog {

  private boolean wasClosedByOK;

  public FightingTalentsSelector(JDialog parent, List<String> preSelection) {
    super(parent);
    initialize(preSelection);
    this.setLocationRelativeTo(parent);
  }
  
  public final String getHelpPage() {
    return "Kampftalente_waehlen";
  }

  public boolean closedByOK() {
    return wasClosedByOK;
  }

  public List<String> getSelectedTalents() {
    return selection;
  }

  private void initialize(List<String> preSelection) {
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
    JLabel introLabel = new JLabel("Kampftalente im Dokument:");
    introLabel.setBounds(5, 5, 220, 25);
    topPanel.add(introLabel, null);
    Dimension preferredSize = introLabel.getPreferredSize();
    topPanel.setPreferredSize(new java.awt.Dimension(preferredSize.width + 10,
        preferredSize.height + 10));
    contentPane.add(topPanel, BorderLayout.NORTH);

    // JScrollPane centerPane = new JScrollPane();
    JPanel scrollPanel = new JPanel(new BorderLayout());

    List<Talent> fts = Talents.getInstance().getTalentsInCategory(
        "Kampftalente");
    JPanel centerPanel = new JPanel(new GridLayout(0, 2, 10, 3));
    for (Talent t : fts) {
      JCheckBox checkBox = new JCheckBox(t.getName());
      checkBox.setSelected(preSelection.contains(t.getName()));
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
    selection.clear();
    for (int i = 0; i < checkBoxes.size(); ++i) {
      if (checkBoxes.get(i).isSelected()) {
        selection.add(checkBoxes.get(i).getText());
      }
    }
  }

  private final ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

  private final ArrayList<String> selection = new ArrayList<String>();
}

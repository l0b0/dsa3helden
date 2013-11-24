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
    along with Heldenverwaltung; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.lf.BGDialog;
import dsa.gui.lf.BGList;
import dsa.model.characters.Group;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.data.Rituals;

public final class RitualsSelectionDialog extends BGDialog {

  public interface RitualsSelectionCallback {
    void ritualAdded(String ritual);
  }

  private JPanel jContentPane = null;

  /**
   * This method initializes
   * 
   */
  public RitualsSelectionDialog(java.awt.Frame parent) {
    super(parent, "Sonderfertigkeit hinzufügen", true);
    initialize();
    setLocationRelativeTo(parent);
    setSize(470, 330);
  }
  
  public String getHelpPage() {
    return "SF_hinzufuegen";
  }

  public void setCallback(RitualsSelectionCallback callback) {
    this.callback = callback;
  }

  private RitualsSelectionCallback callback;

  private void initialize() {
    // this.setSize(new java.awt.Dimension(364,253));
    this.setContentPane(getJContentPane());
    this.setTitle("Sonderfertigkeiten");
    // this.pack();
    currentHero = Group.getInstance().getActiveHero();
    updateData();
  }

  private List<String> heroRituals;

  private void updateData() {
    inUpdate = true;
    model.clear();
    if (currentHero != null) {
      List<String> rituals = Rituals.getInstance().getAllRituals(
          currentHero.getInternalType());
      heroRituals = currentHero.getRituals();
      for (String r : rituals) {
        if (!heroRituals.contains(r)) {
          model.addElement(r);
        }
      }
    }
    ritualsList.setModel(model);
    if (model.size() > 0) {
      ritualsList.setSelectedIndex(0);
      String ritual = ritualsList.getSelectedValue().toString();
      boolean canGetRitual = Rituals.getInstance().isRitualAvailable(ritual,
          heroRituals);
      getAddTestButton().setEnabled(
          (Rituals.getInstance().getRitualLearningTestData(ritual) != null)
              && canGetRitual);
      getAddButton().setEnabled(canGetRitual);
    }
    else {
      getAddTestButton().setEnabled(false);
      getAddButton().setEnabled(false);
    }
    inUpdate = false;
  }

  private JButton addButton;

  private JButton addTestButton;

  private JButton closeButton;

  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton("Hinzufügen");
      addButton.setBounds(10, 20, 140, 25);
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (callback != null) {
            callback.ritualAdded(ritualsList.getSelectedValue().toString());
            updateData();
          }
        }
      });
    }
    return addButton;
  }

  private JButton getAddTestButton() {
    if (addTestButton == null) {
      addTestButton = new JButton("Mit Probe hinzu");
      addTestButton.setBounds(160, 20, 140, 25);
      addTestButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (callback == null) return;
          doTest();
        }
      });

    }
    return addTestButton;
  }

  private int getPoints(dsa.model.DiceSpecification ds) {
    return ds.calcValue();
  }

  protected void doTest() {
    String ritual = ritualsList.getSelectedValue().toString();
    dsa.model.data.Ritual.LearningTestData ltd = Rituals.getInstance()
        .getRitualLearningTestData(ritual);
    RitualProbeDialog dialog = new RitualProbeDialog(this, currentHero, ltd);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    if (dialog.getResult() != RitualProbeDialog.Result.Success) return;
    int defaultAPChange = getPoints(ltd.getPermanentAP());
    int currentAPChange = getPoints(ltd.getAp());
    int defaultLEChange = getPoints(ltd.getPermanentLE());
    int currentLEChange = getPoints(ltd.getLe());
    if (currentLEChange > currentHero.getCurrentEnergy(Energy.LE)) {
      JOptionPane.showMessageDialog(this,
          "Es steht nicht genug LE zur Verfügung!", "Ritual",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (defaultLEChange > currentHero.getDefaultEnergy(Energy.LE)) {
      JOptionPane.showMessageDialog(this,
          "Es steht nicht genug LE zur Verfügung!", "Ritual",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (currentAPChange > currentHero.getCurrentEnergy(Energy.AE)) {
      JOptionPane.showMessageDialog(this,
          "Es steht nicht genug AE zur Verfügung!", "Ritual",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (defaultAPChange > currentHero.getDefaultEnergy(Energy.AE)) {
      JOptionPane.showMessageDialog(this,
          "Es steht nicht genug AE zur Verfügung!", "Ritual",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (defaultAPChange != 0)
      currentHero.changeDefaultEnergy(Energy.AE, -defaultAPChange);
    if (currentAPChange != 0)
      currentHero.changeCurrentEnergy(Energy.AE, -currentAPChange
          + defaultAPChange);
    if (defaultLEChange != 0)
      currentHero.changeDefaultEnergy(Energy.LE, -defaultLEChange);
    if (currentLEChange != 0)
      currentHero.changeCurrentEnergy(Energy.LE, -currentLEChange
          + defaultLEChange);
    callback.ritualAdded(ritual);
    updateData();
  }

  private JButton getCloseButton() {
    if (closeButton == null) {
      closeButton = new JButton("Schließen");
      closeButton.setBounds(310, 20, 140, 25);
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dispose();
        }
      });
    }
    return closeButton;
  }

  private JPanel lowerPanel;

  private JPanel getLowerPanel() {
    if (lowerPanel == null) {
      lowerPanel = new JPanel();
      lowerPanel.setLayout(null);
      lowerPanel.setPreferredSize(new java.awt.Dimension(210, 60));
      lowerPanel.add(getAddButton(), null);
      lowerPanel.add(getAddTestButton(), null);
      lowerPanel.add(getCloseButton(), null);
    }
    return lowerPanel;
  }

  private Hero currentHero;

  private JPanel getJContentPane() {
    if (jContentPane == null) {
      JLabel jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new java.awt.Dimension(10, 10));
      JLabel jLabel2 = new JLabel();
      jLabel2.setText("");
      jLabel2.setPreferredSize(new java.awt.Dimension(10, 10));
      JLabel jLabel3 = new JLabel();
      jLabel3.setText("");
      jLabel3.setPreferredSize(new java.awt.Dimension(10, 10));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(jLabel, java.awt.BorderLayout.NORTH);
      jContentPane.add(jLabel2, java.awt.BorderLayout.WEST);
      jContentPane.add(jLabel3, java.awt.BorderLayout.EAST);
      JScrollPane scrollPane = new JScrollPane(getRitualsList());
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      jContentPane.add(scrollPane, java.awt.BorderLayout.CENTER);
      jContentPane.add(getLowerPanel(), java.awt.BorderLayout.SOUTH);
    }
    return jContentPane;
  }

  private JList ritualsList;

  private DefaultListModel model;

  private boolean inUpdate = false;

  private JList getRitualsList() {
    if (ritualsList == null) {
      model = new DefaultListModel();
      ritualsList = new BGList(model);
      ritualsList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (inUpdate) return;
          if (model.size() == 0) return;
          if (ritualsList.getSelectedIndex() == -1) return;
          String ritual = ritualsList.getSelectedValue().toString();
          boolean canGetRitual = Rituals.getInstance().isRitualAvailable(
              ritual, heroRituals);
          getAddTestButton().setEnabled(
              (Rituals.getInstance().getRitualLearningTestData(ritual) != null)
                  && canGetRitual);
          getAddButton().setEnabled(canGetRitual);
        }
      });
      ritualsList.setPreferredSize(new java.awt.Dimension(300, 300));
    }
    return ritualsList;
  }

}

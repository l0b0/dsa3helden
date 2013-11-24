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
package dsa.gui.frames;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.gui.dialogs.RitualProbeDialog;
import dsa.gui.dialogs.RitualsSelectionDialog;
import dsa.gui.lf.BGList;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Ritual;
import dsa.model.data.Rituals;

public class RitualsFrame extends SubFrame implements CharactersObserver,
    RitualsSelectionDialog.RitualsSelectionCallback {

  private JPanel jContentPane = null;

  /**
   * This method initializes
   * 
   */
  public RitualsFrame() {
    super("Sonderfertigkeiten");
    initialize();
  }

  public String getHelpPage() {
    return "Sonderfertigkeiten";
  }
  
  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    // this.setSize(new java.awt.Dimension(364,253));
    this.setContentPane(getJContentPane());
    this.setTitle("Sonderfertigkeiten");
    // this.pack();
    Group.getInstance().addObserver(this);
    currentHero = Group.getInstance().getActiveHero();
    updateData();
  }

  private boolean inUpdate = false;

  private void updateData() {
    inUpdate = true;
    model.clear();
    if (currentHero != null) {
      List<String> rituals = currentHero.getRituals();
      for (String r : rituals)
        model.addElement(r);
    }
    ritualsList.setModel(model);
    if (model.size() > 0) {
      ritualsList.setSelectedIndex(0);
      getRemoveButton().setEnabled(!currentHero.isDifference());
      getTestButton().setEnabled(!currentHero.isDifference() &&
          dsa.model.data.Rituals.getInstance().getRitualTestData(
              ritualsList.getSelectedValue().toString()) != null);
      checkRemoveButton();
    }
    else {
      getTestButton().setEnabled(false);
      getRemoveButton().setEnabled(false);
    }
    getAddButton().setEnabled(currentHero != null && !currentHero.isDifference());
    inUpdate = false;
  }

  private void checkRemoveButton() {
    if (currentHero.isMagicDilletant() && !currentHero.isDifference()) {
      getRemoveButton().setEnabled(true);
      return;
    }
    String ritual = ritualsList.getSelectedValue().toString();
    List<String> test = currentHero.getRituals();
    test.remove(ritual);
    dsa.model.data.Rituals rituals = Rituals.getInstance();
    for (String r : currentHero.getRituals()) {
      if (!rituals.isRitualAvailable(r, test)) {
        getRemoveButton().setEnabled(false);
        return;
      }
    }
    getRemoveButton().setEnabled(!currentHero.isDifference());
  }

  private Hero currentHero;

  private JList ritualsList;

  private DefaultListModel model;

  private JList getRitualsList() {
    if (ritualsList == null) {
      model = new DefaultListModel();
      ritualsList = new BGList(model);
      ritualsList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (inUpdate) return;
          if (model.size() == 0) return;
          if (ritualsList.getSelectedIndex() == -1) return;
          getTestButton().setEnabled(
              dsa.model.data.Rituals.getInstance().getRitualTestData(
                  ritualsList.getSelectedValue().toString()) != null);
          checkRemoveButton();
        }
      });
    }
    return ritualsList;
  }

  private JButton addButton;

  private JButton testButton;

  private JButton removeButton;

  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setBounds(10, 5, 60, 25);
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          RitualsSelectionDialog dialog = new RitualsSelectionDialog(
              RitualsFrame.this);
          dialog.setCallback(RitualsFrame.this);
          dialog.setVisible(true);
        }
      });
    }
    return addButton;
  }

  private JButton getTestButton() {
    if (testButton == null) {
      testButton = new JButton(ImageManager.getIcon("probe"));
      testButton.setDisabledIcon(ImageManager.getIcon("probe_disabled"));
      testButton.setBounds(10, 40, 60, 25);
      testButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Ritual.TestData testData = Rituals.getInstance().getRitualTestData(
              ritualsList.getSelectedValue().toString());
          RitualProbeDialog dialog = new RitualProbeDialog(RitualsFrame.this,
              currentHero, testData);
          dialog.setLocationRelativeTo(RitualsFrame.this);
          dialog.setVisible(true);
        }
      });
    }
    return testButton;

  }

  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(10, 75, 60, 25);
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          currentHero.removeRitual(ritualsList.getSelectedValue().toString());
          model.removeElement(ritualsList.getSelectedValue().toString());
          updateData();
          if (ritualsList.getModel().getSize() > 0) {
            ritualsList.setSelectedIndex(ritualsList.getModel().getSize() - 1);
          }
        }
      });
    }
    return removeButton;
  }

  private JPanel rightPanel;

  private JPanel getRightPanel() {
    if (rightPanel == null) {
      rightPanel = new JPanel();
      rightPanel.setLayout(null);
      rightPanel.setPreferredSize(new java.awt.Dimension(80, 80));
      rightPanel.add(getAddButton(), null);
      rightPanel.add(getRemoveButton(), null);
      rightPanel.add(getTestButton(), null);
    }
    return rightPanel;
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
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
      jContentPane.add(jLabel3, java.awt.BorderLayout.SOUTH);
      JScrollPane scrollPane = new JScrollPane(getRitualsList());
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      jContentPane.add(scrollPane, java.awt.BorderLayout.CENTER);
      jContentPane.add(getRightPanel(), java.awt.BorderLayout.EAST);
    }
    return jContentPane;
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }

  public void ritualAdded(String ritual) {
    currentHero.addRitual(ritual);
    updateData();
  }

} // @jve:decl-index=0:visual-constraint="10,10"

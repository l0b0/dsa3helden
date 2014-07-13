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

// import java.awt.Dimension;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dsa.gui.dialogs.SpecialTalentDialog;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Hero;
import dsa.model.talents.Talent;

/**
 * 
 */
public class SpecialTalentsFrame extends TalentFrame {

  public SpecialTalentsFrame(String title) {
    super(title, true);
    initialize();
  }

  public String getHelpPage() {
    return "Berufe"; //$NON-NLS-1$
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    recreateUI();
  }

  protected boolean shallDisplay(Talent talent) {
    return (talent != null) && (currentHero != null)
        && (currentHero.hasTalent(talent.getName()));
  }

  protected void addSubclassSpecificComponents(java.awt.Container container) {
    container.add(getSubPanel(), BorderLayout.SOUTH);
  }

  protected void updateStaticSubclassSpecificData() {
    getAddButton().setEnabled(currentHero != null && !currentHero.isDifference());
    getRemoveButton().setEnabled(currentHero != null && !currentHero.isDifference());
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 30);
  }

  private JButton addButton;

  private JButton removeButton;

  private JPanel subPanel;

  private JPanel getSubPanel() {
    if (subPanel == null) {
      subPanel = new JPanel();
      subPanel.setLayout(null);
      subPanel.add(getAddButton());
      subPanel.add(getRemoveButton());
      subPanel.setPreferredSize(new java.awt.Dimension(260, 30));
    }
    return subPanel;
  }

  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase")); //$NON-NLS-1$
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled")); //$NON-NLS-1$
      addButton.setBounds(5, 5, 34, 20);
      addButton.setToolTipText(Localization.getString("Berufe.TalentHinzufuegen")); //$NON-NLS-1$
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addTalent();
        }
      });
    }
    return addButton;
  }

  protected void addTalent() {
    SpecialTalentDialog dialog = new SpecialTalentDialog(this);
    dialog.setModal(true);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    if (dialog.wasClosedByOK() && !currentHero.hasTalent(dialog.getTalent())) {
      currentHero.addTalent(dialog.getTalent());
      currentHero.setDefaultTalentValue(dialog.getTalent(), 3);
      currentHero.setCurrentTalentValue(dialog.getTalent(), 3);
    }
    else if (dialog.wasClosedByOK()) recreateUI();
  }

  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled")); //$NON-NLS-1$
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease")); //$NON-NLS-1$
      removeButton.setBounds(45, 5, 34, 20);
      removeButton.setToolTipText(Localization.getString("Berufe.TalentEntfernen")); //$NON-NLS-1$
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeTalent();
        }
      });
    }
    return removeButton;
  }

  protected void removeTalent() {
    int row = mTable.getSelectedRow();
    if (row < 0) return;
    String talent = (String) mSorter.getValueAt(row, getNameDummyColumn());
    if (talent.equals(Localization.getString("Berufe.MirakelTalent")) || talent.equals(Localization.getString("Berufe.JagenFalleTalent")) || talent.equals(Localization.getString("Berufe.JagenPirschTalent"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      JOptionPane.showMessageDialog(this, Localization.getString("Berufe.TalentEntfernenFehler1") + talent //$NON-NLS-1$
          + Localization.getString("Berufe.TalentEntfernenFehler2"), Localization.getString("Berufe.Fehler"), //$NON-NLS-1$ //$NON-NLS-2$
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (JOptionPane.showConfirmDialog(this, Localization.getString("Berufe.TalentEntfernen1") + talent //$NON-NLS-1$
        + Localization.getString("Berufe.TalentEntfernen2"), Localization.getString("Berufe.TalentEntfernenTitel"), //$NON-NLS-1$ //$NON-NLS-2$
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      currentHero.removeTalent(talent);
    }
  }

}

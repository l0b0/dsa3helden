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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.talents.Talent;
import dsa.util.Optional;

public final class LanguageFrame extends TalentFrame {

  public LanguageFrame(String title) {
    super(title, false);
    loadSubclassState();
    initialize();
  }
  
  public String getHelpPage() {
    return "Sprachen";
  }

  private JCheckBox mCheckbox;

  private JLabel label1;

  private JLabel label2;

  private JLabel label3;

  private JLabel label4;

  protected boolean isColumnEditable(int column) {
    if (!super.isColumnEditable(column)) return false;
    if (column == getMaxColumn()) return false;
    return true;
  }

  private int getMaxColumn() {
    return 3;
  }

  protected int getDefaultValueColumn() {
    return 1;
  }

  protected int getCurrentValueColumn() {
    return 2;
  }

  protected int getLockColumn() {
    return 4;
  }

  protected int getIncrColumn() {
    return 5;
  }

  protected int getNameDummyColumn() {
    return 6;
  }

  protected int getNrOfColumns() {
    return 7;
  }

  private JLabel mCBLabel;

  private JComboBox mNativeCombo;

  protected void addSubclassSpecificComponents(java.awt.Container container) {
    JPanel panel = new JPanel();
    panel.setLayout(null);
    mCheckbox = new JCheckBox("Unbekannte Sprachen anzeigen");
    mCheckbox.setBounds(5, 5, 250, 18);
    mCheckbox.setSelected(displayUnknownLanguages);
    mCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        displayUnknownLanguages = mCheckbox.isSelected();
        recreateUI();
      }
    });
    panel.add(mCheckbox);
    mCBLabel = new JLabel("Muttersprache: ");
    mCBLabel.setBounds(5, 30, 100, 25);
    panel.add(mCBLabel);
    mNativeCombo = new JComboBox();
    mNativeCombo.setBounds(130, 30, 150, 25);
    for (Talent language : Talents.getInstance().getTalentsInCategory(
        "Sprachen")) {
      mNativeCombo.addItem(language.getName());
    }
    mNativeCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (inUpdate) return;
        if (currentHero == null) return;
        if (mNativeCombo == null) return;
        if (mNativeCombo.getSelectedItem() == null) return;
        currentHero.setNativeTongue(mNativeCombo.getSelectedItem().toString());
        LanguageFrame.this.reupdateData();
      }
    });
    if (currentHero != null && mNativeCombo != null)
      mNativeCombo.setSelectedItem(currentHero.getNativeTongue());
    panel.add(mNativeCombo);
    panel.setPreferredSize(new java.awt.Dimension(260, 65));
    container.add(panel, BorderLayout.NORTH);
    JPanel panel2 = new JPanel();
    panel2.setLayout(null);
    getLabel1().setBounds(5, 5, 200, 15);
    getLabel2().setBounds(5, 25, 200, 15);
    getLabel3().setBounds(205, 5, 30, 15);
    getLabel4().setBounds(205, 25, 30, 15);
    panel2.add(label1);
    panel2.add(label2);
    panel2.add(label3);
    panel2.add(label4);
    panel2.setPreferredSize(new Dimension(260, 45));
    container.add(panel2, BorderLayout.SOUTH);
  }

  protected boolean canIncreaseUnknownTalents() {
    return true;
  }

  protected void tryTalentIncrease(String talent) {
    if (currentHero == null) return;
    if (currentHero.hasTalent(talent)) {
      currentHero.changeTalentValue(talent, 1);
    }
    else {
      currentHero.addTalent(talent);
      currentHero.setDefaultTalentValue(talent, 1);
      currentHero.setCurrentTalentValue(talent, 1);
    }
  }

  protected Class<?> getColumnClass(int column, Class<?> defaultValue) {
    if (column == getMaxColumn())
      return NULL_INT.getClass();
    else
      return super.getColumnClass(column, defaultValue);
  }

  protected void updateSubclassSpecificData(DefaultTableModel model, int i,
      int displayIndex) {
    Talent talent = this.talents.get(i);
    if (!talent.isLanguage()) {
      model.setValueAt(NULL_INT, displayIndex, getMaxColumn());
    }
    else {
      dsa.model.talents.Language language = (dsa.model.talents.Language) talent;
      int max = currentHero != null ? language.getMax(currentHero
          .getNativeTongue()) : language.getMax();
      model
          .setValueAt(new Optional<Integer>(max), displayIndex, getMaxColumn());
    }
  }

  protected void initSubclassSpecificData(DefaultTableModel model, int i) {
    Talent talent = this.talents.get(i);
    int displayIndex = model.getRowCount() - 1;
    if (!talent.isLanguage()) {
      model.setValueAt(NULL_INT, displayIndex, getMaxColumn());
    }
    else {
      dsa.model.talents.Language language = (dsa.model.talents.Language) talent;
      int max = currentHero != null ? language.getMax(currentHero
          .getNativeTongue()) : language.getMax();
      model
          .setValueAt(new Optional<Integer>(max), displayIndex, getMaxColumn());
    }
  }

  private boolean inUpdate = false;

  protected void updateStaticSubclassSpecificData() {
    int nr1 = currentHero != null ? currentHero.getFreeLanguagePoints() : 0;
    int nr2 = currentHero != null ? currentHero.getFreeOldLanguagePoints() : 0;
    getLabel3().setText("" + nr1);
    getLabel4().setText("" + nr2);
    inUpdate = true;
    if (currentHero != null && mNativeCombo != null)
      mNativeCombo.setSelectedItem(currentHero.getNativeTongue());
    inUpdate = false;
  }

  private JLabel getLabel1() {
    if (label1 == null) label1 = new JLabel("Freie Punkte für Sprachen: ");
    return label1;
  }

  private JLabel getLabel2() {
    if (label2 == null)
      label2 = new JLabel("Freie Punkte für Alte Sprachen: ");
    return label2;
  }

  private JLabel getLabel3() {
    if (label3 == null) label3 = new JLabel("");
    label3.setForeground(java.awt.Color.RED);
    return label3;
  }

  private JLabel getLabel4() {
    if (label4 == null) label4 = new JLabel("");
    label4.setForeground(java.awt.Color.RED);
    return label4;
  }

  protected void addSubclassSpecificColumns(DefaultTableColumnModel tcm) {
    tcm.addColumn(new TableColumn(getMaxColumn(), 15));
    tcm.moveColumn(tcm.getColumnCount() - 1, getMaxColumn());
  }

  protected Vector<String> getColumnIdentifiers() {
    Vector<String> defaultNames = super.getColumnIdentifiers();
    defaultNames.add(getMaxColumn(), "Max");
    return defaultNames;
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 20);
  }

  protected boolean shallDisplay(Talent talent) {
    if (displayUnknownLanguages)
      return true;
    else if (currentHero == null)
      return false;
    else
      return currentHero.hasTalent(talent.getName());
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (displayUnknownLanguages)
      super.activeCharacterChanged(newCharacter, oldCharacter);
    else {
      currentHero = newCharacter;
      if (newCharacter != null)
        newCharacter.addHeroObserver(myCharacterObserver);
      if (oldCharacter != null)
        oldCharacter.removeHeroObserver(myCharacterObserver);
      recreateUI();
    }
  }

  protected void loadSubclassState() {
    boolean dUL = java.util.prefs.Preferences.userNodeForPackage(
        dsa.gui.PackageID.class).getBoolean(
        getTitle() + "DisplayUnknownSpells", false);
    if (dUL != displayUnknownLanguages) {
      displayUnknownLanguages = dUL;
      recreateUI();
    }
  }

  protected void saveSubclassState() {
    java.util.prefs.Preferences.userNodeForPackage(dsa.gui.PackageID.class)
        .putBoolean(getTitle() + "DisplayUnknownSpells",
            displayUnknownLanguages);
  }
  
  protected boolean isTalentRelevant(String talent) {
    return (talent.equals("Sprachen Kennen") || talent.equals("Alte Sprachen"));
  }

  private boolean displayUnknownLanguages;

  protected void changeTalentValue(boolean current, String talent, int value) {
    if (currentHero.hasTalent(talent))
      super.changeTalentValue(current, talent, value);
    else if (!current) {
      currentHero.addTalent(talent);
      currentHero.setDefaultTalentValue(talent, value);
      currentHero.setCurrentTalentValue(talent, value);
    }
  }
}

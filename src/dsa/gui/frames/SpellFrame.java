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
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;

import dsa.gui.dialogs.SpellSelectionDialog;
import dsa.gui.dialogs.SelectionDialogBase.SelectionDialogCallback;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.talents.Spell;
import dsa.model.talents.Talent;
import dsa.util.Optional;

public class SpellFrame extends TalentFrame {

  public SpellFrame(String title) {
    super(title, true);
  }

  protected boolean isColumnEditable(int column) {
    if (!super.isColumnEditable(column)) return false;
    if (column == getCategoryColumn()) return false;
    if (column == getOriginColumn()) return false;
    return true;
  }

  public void saveCurrentSortingState() {
    super.saveSortingState();
  }

  private int getCategoryColumn() {
    return 1;
  }

  private int getOriginColumn() {
    return 2;
  }

  protected int getDefaultValueColumn() {
    return 3;
  }

  protected int getCurrentValueColumn() {
    return 4;
  }

  protected int getLockColumn() {
    return 5;
  }

  protected int getTestColumn() {
    return 6;
  }

  protected int getIncrColumn() {
    return 8;
  }

  protected int getNameDummyColumn() {
    return 9;
  }

  private int getIncrCountColumn() {
    return 7;
  }

  protected int getNrOfColumns() {
    return 10;
  }

  protected Class<?> getColumnClass(int column, Class<?> defaultValue) {
    if (column == getIncrCountColumn())
      return NullInt.getClass();
    else
      return super.getColumnClass(column, defaultValue);
  }

  protected void initSubclassSpecificData(DefaultTableModel model, int i) {
    Talent talent = this.talents.elementAt(i);
    int displayIndex = model.getRowCount() - 1;
    if (!talent.isSpell()) {
      model.setValueAt("-", displayIndex, getCategoryColumn());
      model.setValueAt("-", displayIndex, getOriginColumn());
      model.setValueAt(NullInt, displayIndex, getIncrCountColumn());
    }
    else {
      Spell spell = (Spell) talent;
      model.setValueAt(spell.getCategory(), displayIndex, getCategoryColumn());
      model.setValueAt(spell.getOrigin(), displayIndex, getOriginColumn());
      int increases = (currentHero != null) ? currentHero
          .getTalentIncreaseTriesPerStep(talent.getName()) : 0;
      model.setValueAt(new Optional<Integer>(increases), displayIndex,
          getIncrCountColumn());
    }
  }

  protected void addSubclassSpecificColumns(DefaultTableColumnModel tcm) {
    tcm.addColumn(new TableColumn(getCategoryColumn(), 60));
    tcm.moveColumn(tcm.getColumnCount() - 1, getCategoryColumn());
    tcm.addColumn(new TableColumn(getOriginColumn(), 60));
    tcm.moveColumn(tcm.getColumnCount() - 1, getOriginColumn());
    FormattedTextFieldCellEditor numberEditor = new FormattedTextFieldCellEditor(
        new NumberFormatter(NumberFormat.getIntegerInstance()));
    numberEditor.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    numberEditor.addCellEditorListener(new SpellIncrChanger());
    GreyingCellRenderer greyingRenderer = new GreyingCellRenderer();
    tcm.addColumn(new TableColumn(getIncrCountColumn(), 15, greyingRenderer,
        numberEditor));
    tcm.moveColumn(tcm.getColumnCount() - 1, getIncrCountColumn());
  }

  private class SpellIncrChanger implements CellEditorListener {

    public void editingStopped(ChangeEvent e) {
      FormattedTextFieldCellEditor editor = (FormattedTextFieldCellEditor) e
          .getSource();
      if (currentHero != null) {
        String talent = editor.mTalent;
        Number number = (Number) editor.getValue();
        currentHero.setTalentIncreaseTriesPerStep(talent, number.intValue());
      }
    }

    public void editingCanceled(ChangeEvent e) {
    }

  }

  protected boolean isLockedColumn(int column) {
    if (column == getIncrCountColumn()) return true;
    return super.isLockedColumn(column);
  }

  protected Vector<String> getColumnIdentifiers() {
    Vector<String> defaultNames = super.getColumnIdentifiers();
    defaultNames.add(getCategoryColumn(), "Kategorie");
    defaultNames.add(getOriginColumn(), "Ursprung");
    defaultNames.add(getIncrCountColumn(), "+");
    return defaultNames;
  }

  private JCheckBox mCheckbox;

  protected void addSubclassSpecificComponents(java.awt.Container container) {
    JPanel panel = new JPanel();
    panel.setLayout(null);
    mCheckbox = new JCheckBox("Unbekannte Zauber anzeigen");
    mCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        displayUnknownSpells = mCheckbox.isSelected();
        createUI();
      }
    });
    mCheckbox.setBounds(5, 5, 250, 18);
    mCheckbox.setSelected(displayUnknownSpells);
    panel.add(mCheckbox);
    panel.setPreferredSize(new java.awt.Dimension(260, 33));
    container.add(panel, BorderLayout.NORTH);
    container.add(getSubPanel(), BorderLayout.SOUTH);
  }

  protected void updateStaticSubclassSpecificData() {
    getAddButton().setEnabled(
        currentHero != null && currentHero.hasEnergy(Energy.AE));
    getRemoveButton().setEnabled(
        currentHero != null && currentHero.hasEnergy(Energy.AE));
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
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 34, 20);
      addButton.setToolTipText("Zauber hinzufügen ...");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addSpell();
        }
      });
    }
    return addButton;
  }

  private class SpellAdder implements SelectionDialogCallback {
    public SpellAdder(SpellSelectionDialog dialog) {
      this.dialog = dialog;
    }

    public void ItemSelected(String item) {
      currentHero.addTalent(item);
      currentHero.setDefaultTalentValue(item, -6);
      currentHero.setCurrentTalentValue(item, -6);
      dialog.updateTable();
    }

    private SpellSelectionDialog dialog;
  }

  protected void addSpell() {
    SpellSelectionDialog dialog = new SpellSelectionDialog(this, currentHero);
    dialog.setCallback(new SpellAdder(dialog));
    dialog.setVisible(true);
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 50);
  }

  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(45, 5, 34, 20);
      removeButton.setToolTipText("Zauber entfernen ...");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeSpell();
        }
      });
    }
    return removeButton;
  }

  protected void removeSpell() {
    int row = mTable.getSelectedRow();
    if (row < 0) return;
    String talent = (String) mSorter.getValueAt(row, getNameDummyColumn());
    if (!currentHero.hasTalent(talent)) return;
    if (JOptionPane.showConfirmDialog(this, "Zauber \"" + talent
        + "\" wirklich entfernen?", "Zauber entfernen",
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      currentHero.removeTalent(talent);
    }
  }

  protected boolean shallDisplay(Talent talent) {
    if (displayUnknownSpells)
      return true;
    else if (currentHero == null)
      return false;
    else
      return (currentHero.hasTalent(talent.getName()));
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    if (displayUnknownSpells)
      super.activeCharacterChanged(newCharacter, oldCharacter);
    else {
      currentHero = newCharacter;
      if (newCharacter != null)
        newCharacter.addHeroObserver(myCharacterObserver);
      if (oldCharacter != null)
        oldCharacter.removeHeroObserver(myCharacterObserver);
      createUI();
    }
  }

  protected void loadSubclassState() {
    displayUnknownSpells = java.util.prefs.Preferences.userNodeForPackage(
        dsa.gui.PackageID.class).getBoolean(
        getTitle() + "DisplayUnknownSpells", false);
  }

  protected void saveSubclassState() {
    java.util.prefs.Preferences.userNodeForPackage(dsa.gui.PackageID.class)
        .putBoolean(getTitle() + "DisplayUnknownSpells", displayUnknownSpells);
  }

  private boolean displayUnknownSpells;

  protected void changeTalentValue(boolean current, String talent, int value) {
    if (currentHero.hasTalent(talent))
      super.changeTalentValue(current, talent, value);
    else if (!current) {
      currentHero.addTalent(talent);
      currentHero.setDefaultTalentValue(talent, value);
      currentHero.setCurrentTalentValue(talent, value);
      for (int i = 0; i < mTable.getRowCount(); ++i) {
        if (mSorter.getValueAt(i, getNameDummyColumn()).equals(talent)) {
          mTable.scrollRectToVisible(mTable.getCellRect(i, 0, true));
          break;
        }
      }
    }
  }

}

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import dsa.gui.util.ImageManager;
import dsa.gui.util.table.CellRenderers;
import dsa.gui.util.table.TableButtonInput;
import dsa.model.characters.Hero;
import dsa.model.talents.FightingTalent;
import dsa.model.talents.Talent;
import dsa.util.Optional;

public class FightingTalentsFrame extends TalentFrame {

  public FightingTalentsFrame(String title) {
    super(title, false);
    initialize();
  }
  
  public String getHelpPage() {
    return "Kampftalente"; //$NON-NLS-1$
  }

  protected boolean isColumnEditable(int column) {
    if (super.isColumnEditable(column)) return true;
    return false;
  }

  protected int getDefaultValueColumn() {
    return 1;
  }

  protected int getCurrentValueColumn() {
    return 2;
  }

  protected int getATColumn() {
    return 3;
  }

  protected int getATIncrColumn() {
    return 4;
  }

  protected int getPAIncrColumn() {
    return 5;
  }

  protected int getPAColumn() {
    return 6;
  }

  protected int getLockColumn() {
    return 7;
  }

  protected int getIncrColumn() {
    return 8;
  }

  protected int getNameDummyColumn() {
    return 9;
  }

  protected int getNrOfColumns() {
    return 10;
  }

  protected Class<?> getColumnClass(int column, Class<?> defaultValue) {
    if (column == getATColumn())
      return NULL_INT.getClass();
    else if (column == getPAColumn())
      return NULL_INT.getClass();
    else if (column == getATIncrColumn())
      return Boolean.class;
    else if (column == getPAIncrColumn())
      return Boolean.class;
    else
      return super.getColumnClass(column, defaultValue);
  }

  public boolean shallBeGray(int row, int column) {
    if (column == getATColumn())
      return true;
    else if (column == getPAColumn())
      return true;
    else
      return super.shallBeGray(row, column);
  }

  protected boolean isRelevant(Hero.DerivedValue dv) {
    if (dv == Hero.DerivedValue.AT)
      return true;
    else if (dv == Hero.DerivedValue.PA)
      return true;
    else
      return super.isRelevant(dv);
  }

  protected void initSubclassSpecificData(DefaultTableModel model, int i) {
    int displayIndex = model.getRowCount() - 1;
    updateSubclassSpecificData(model, i, displayIndex);
  }

  protected void updateSubclassSpecificData(DefaultTableModel model, int i,
      int displayIndex) {
    Talent talent = this.talents.get(i);
    if ((currentHero == null) || !talent.isFightingTalent()) {
      model.setValueAt(NULL_INT, displayIndex, getATColumn());
      model.setValueAt(NULL_INT, displayIndex, getPAColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getATIncrColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getPAIncrColumn());
    }
    else if (((FightingTalent) talent).isProjectileTalent()) {
      int at = currentHero.getCurrentDerivedValue(Hero.DerivedValue.FK)
          + currentHero.getCurrentTalentValue(talent.getName());
      model.setValueAt(new Optional<Integer>(at), displayIndex, getATColumn());
      model.setValueAt(NULL_INT, displayIndex, getPAColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getATIncrColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getPAIncrColumn());
    }
    else if (((FightingTalent) talent).isLefthandIndicator()) {
      int value = currentHero.getCurrentTalentValue(talent.getName());
      int at = -7 + Math.round(value / 2.0f);
      int pa = -7 + value - Math.round(value / 2.0f);
      model.setValueAt(new Optional<Integer>(at), displayIndex, getATColumn());
      model.setValueAt(new Optional<Integer>(pa), displayIndex, getPAColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getATIncrColumn());
      model.setValueAt(Boolean.FALSE, displayIndex, getPAIncrColumn());
    }
    else {
      int at = currentHero.getCurrentDerivedValue(Hero.DerivedValue.AT)
          + currentHero.getATPart(talent.getName());
      int pa = currentHero.getCurrentDerivedValue(Hero.DerivedValue.PA)
          + currentHero.getPAPart(talent.getName());
      model.setValueAt(new Optional<Integer>(at), displayIndex, getATColumn());
      model.setValueAt(new Optional<Integer>(pa), displayIndex, getPAColumn());
      model.setValueAt(Boolean.valueOf(!currentHero.isDifference()), displayIndex, getATIncrColumn());
      model.setValueAt(Boolean.valueOf(!currentHero.isDifference()), displayIndex, getPAIncrColumn());
    }
  }

  private class ATPAMover implements ActionListener {
    ATPAMover(boolean moveToAT) {
      mToAT = moveToAT;
    }

    public void actionPerformed(ActionEvent e) {
      String talent = (String) mSorter.getValueAt(mButtonClickedRow,
          getNameDummyColumn());
      if (!currentHero.hasTalent(talent)) return;
      currentHero.setATPart(talent, currentHero.getATPart(talent)
          + (mToAT ? 1 : -1));
      reupdateData();
    }

    private final boolean mToAT;
  }

  protected void addSubclassSpecificColumns(DefaultTableColumnModel tcm) {
    DefaultTableCellRenderer greyingRenderer = CellRenderers.createGreyingCellRenderer(this);
    greyingRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    // FormattedTextFieldCellEditor editor = new
    // FormattedTextFieldCellEditor(new
    // NumberFormatter(NumberFormat.getIntegerInstance()));
    // editor.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    TableCellEditor editor = TableButtonInput.createDummyCellEditor();
    tcm.addColumn(new TableColumn(getATColumn(), 20, greyingRenderer, editor));
    tcm.moveColumn(tcm.getColumnCount() - 1, getATColumn());
    JButton incrATButton = TableButtonInput.createButton(ImageManager.getIcon("move_left")); //$NON-NLS-1$
    incrATButton.addActionListener(new ATPAMover(true));
    TableCellRenderer atRenderer = TableButtonInput.createButtonCellRenderer(incrATButton,
        new javax.swing.table.DefaultTableCellRenderer());
    tcm.addColumn(new TableColumn(getATIncrColumn(), 20, atRenderer,
        editor));
    tcm.moveColumn(tcm.getColumnCount() - 1, getATIncrColumn());
    JButton incrPAButton = TableButtonInput.createButton(ImageManager.getIcon("move_right")); //$NON-NLS-1$
    incrPAButton.addActionListener(new ATPAMover(false));
    TableCellRenderer paRenderer = TableButtonInput.createButtonCellRenderer(incrPAButton,
        new javax.swing.table.DefaultTableCellRenderer());
    tcm.addColumn(new TableColumn(getPAIncrColumn(), 20, paRenderer,
        editor));
    tcm.moveColumn(tcm.getColumnCount() - 1, getPAIncrColumn());
    tcm.addColumn(new TableColumn(getPAColumn(), 20, greyingRenderer, editor));
    tcm.moveColumn(tcm.getColumnCount() - 1, getPAColumn());
  }

  protected Vector<String> getColumnIdentifiers() {
    Vector<String> defaultNames = super.getColumnIdentifiers();
    defaultNames.add(getATColumn(), Localization.getString("Kampftalente.AT")); //$NON-NLS-1$
    defaultNames.add(getATIncrColumn(), ""); //$NON-NLS-1$
    defaultNames.add(getPAIncrColumn(), ""); //$NON-NLS-1$
    defaultNames.add(getPAColumn(), Localization.getString("Kampftalente.PA")); //$NON-NLS-1$
    return defaultNames;
  }

}

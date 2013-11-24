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
package dsa.gui.tables;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.gui.util.TableSorter;
import dsa.model.data.SpellStartValues;
import dsa.model.talents.Spell;

public class SpellTable extends AbstractTable {

  @Override
  protected int getNameColumn() {
    return 0;
  }

  static int getOriginColumn() {
    return 2;
  }

  static int getCategoryColumn() {
    return 1;
  }
  
  static int getSkillColumn() {
    return 3;
  }

  static class MyTableModel extends DefaultTableModel {
    public boolean isCellEditable(int row, int column) {
      return false;
    }
    
    public Class<?> getColumnClass(int column) {
      if (column == getSkillColumn()) return Integer.class;
      return super.getColumnClass(column);
    }
  }

  MyTableModel mModel;
  
  private final String characterType;

  public SpellTable(String characterType) {
    super();
    this.characterType = characterType;
    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Kategorie");
    mModel.addColumn("Ursprung");
    mModel.addColumn("Startwert");
    tcm.addColumn(new TableColumn(0, 160));
    tcm.addColumn(new TableColumn(1, 160));
    tcm.addColumn(new TableColumn(2, 140));
    tcm.addColumn(new TableColumn(3, 80));
    mSorter = new TableSorter(mModel);
    mTable = new JTable(mSorter, tcm);
    mSorter.setTableHeader(mTable.getTableHeader());

    for (int i = 0; i < mTable.getColumnCount(); ++i)
      tcm.getColumn(i).setHeaderValue(mTable.getColumnName(i));

    mTable.setColumnSelectionAllowed(false);
    mTable.setIntercellSpacing(new java.awt.Dimension(6, 6));
    mTable.setRowSelectionAllowed(true);
    mTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(22);
    mTable.addMouseListener(createMouseListener());

    // mTable.setBackground(BACKGROUND_GRAY);
    JScrollPane scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);
    setCellRenderer();

    mSorter.setSortingListener(this);
  }

  public void addSpell(Spell spell) {
    Object[] rowData = new Object[4];
    rowData[getNameColumn()] = spell.getName();
    rowData[getCategoryColumn()] = spell.getCategory();
    rowData[getOriginColumn()] = spell.getOrigin();
    rowData[getSkillColumn()] = SpellStartValues.getInstance().getStartValue(
        characterType, spell.getName());
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public void removeSelectedSpell() {
    String selectedSpell = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedSpell)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }
}

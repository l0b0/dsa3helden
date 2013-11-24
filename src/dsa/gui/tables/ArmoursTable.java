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
package dsa.gui.tables;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.control.ClothesBE;
import dsa.gui.util.table.TableSorter;
import dsa.model.data.Armour;
import dsa.util.Optional;

public class ArmoursTable extends AbstractTable {

  protected int getNameColumn() {
    return 0;
  }

  int getRSColumn() {
    return 1;
  }

  int getBEColumn() {
    return 2;
  }

  int getWeightColumn() {
    return 3;
  }
  
  int getWorthColumn() {
    return 4;
  }

  static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn()) {
        return NULL_INT.getClass();
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;

  public ArmoursTable() {
    super();
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("RS");
    mModel.addColumn("BE");
    mModel.addColumn("Gewicht");
    mModel.addColumn("Wert (S)");

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(0, 140));
    tcm.addColumn(new TableColumn(1, 25));
    tcm.addColumn(new TableColumn(2, 25));
    tcm.addColumn(new TableColumn(3, 40));
    tcm.addColumn(new TableColumn(4, 40));

    mSorter = new TableSorter(mModel);
    mTable = new ViewportFillingTable(mSorter, tcm);
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
    setCellRenderer();

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);

    mSorter.setSortingListener(this);

  }

  public void removeSelectedArmour() {
    String selectedArmour = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedArmour)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

  public void removeArmour(String armour) {
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(armour)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

  public void addArmour(Armour armour) {
    addArmour(armour, armour.getName());
  }
  
  public void addArmour(Armour armour, String name) {
    Object[] rowData = new Object[5];
    rowData[getNameColumn()] = name;
    rowData[getRSColumn()] = armour.getRS();
    rowData[getBEColumn()] = ClothesBE.getBE(armour);
    rowData[getWeightColumn()] = armour.getWeight();
    rowData[getWorthColumn()] = new Optional<Integer>(armour.getWorth());
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public void addUnknownArmour(String name) {
    Object[] rowData = new Object[5];
    rowData[getNameColumn()] = name;
    rowData[getRSColumn()] = NULL_INT;
    rowData[getBEColumn()] = NULL_INT;
    rowData[getWeightColumn()] = NULL_INT;
    rowData[getWorthColumn()] = NULL_INT;
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

}

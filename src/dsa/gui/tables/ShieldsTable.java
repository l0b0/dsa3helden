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
package dsa.gui.tables;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.gui.util.TableSorter;
import dsa.model.data.Shield;
import dsa.util.Optional;

public class ShieldsTable extends BasicTable {

  protected int getNameColumn() {
    return 0;
  }

  int getPAColumn() {
    return 1;
  }

  int getATColumn() {
    return 2;
  }

  int getBEColumn() {
    return 3;
  }

  int getFKColumn() {
    return 4;
  }

  int getBFColumn() {
    return 5;
  }

  int getWorthColumn() {
    return 7;
  }

  int getWeightColumn() {
    return 6;
  }

  static Optional<Integer> NullInt = Optional.NullInt;

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn()) {
        return NullInt.getClass();
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;

  public ShieldsTable() {
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("PA");
    mModel.addColumn("AT");
    mModel.addColumn("BE");
    mModel.addColumn("FK");
    mModel.addColumn("BF");
    mModel.addColumn("Gewicht");
    mModel.addColumn("Wert");

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(0, 140));
    tcm.addColumn(new TableColumn(1, 25));
    tcm.addColumn(new TableColumn(2, 25));
    tcm.addColumn(new TableColumn(3, 25));
    tcm.addColumn(new TableColumn(4, 25));
    tcm.addColumn(new TableColumn(5, 25));
    tcm.addColumn(new TableColumn(6, 40));
    tcm.addColumn(new TableColumn(7, 40));

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
    mTable.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1 && dcListener != null) {
          dcListener.actionPerformed(new ActionEvent(this, 0, ""));
        }
      }
    });

    mTable.setBackground(BACKGROUND_GRAY);
    JScrollPane scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    setCellRenderer();

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);

    mSorter.setSortingListener(this);

  }

  public void removeSelectedShield() {
    String selectedShield = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedShield)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

  public void addShield(Shield shield) {
    addShield(shield, shield.getBf());
  }

  public void addShield(Shield shield, int bf) {
    Object[] rowData = new Object[8];
    rowData[getNameColumn()] = shield.getName();
    dsa.model.characters.Hero currentHero = dsa.model.characters.Group
        .getInstance().getActiveHero();
    boolean goodLH = (currentHero != null)
        && (currentHero.getDefaultTalentValue("Linkshändig") >= 9);
    rowData[getPAColumn()] = goodLH ? shield.getPaMod2() : shield.getPaMod();
    rowData[getATColumn()] = shield.getAtMod();
    rowData[getBEColumn()] = shield.getBeMod();
    rowData[getFKColumn()] = shield.getFkMod();
    rowData[getBFColumn()] = bf;
    rowData[getWeightColumn()] = shield.getWeight();
    rowData[getWorthColumn()] = shield.getWorth();
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public void addUnknownShield(String name) {
    Object[] rowData = new Object[4];
    rowData[getNameColumn()] = name;
    rowData[getPAColumn()] = NullInt;
    rowData[getATColumn()] = NullInt;
    rowData[getBEColumn()] = NullInt;
    rowData[getFKColumn()] = NullInt;
    rowData[getBFColumn()] = NullInt;
    rowData[getWorthColumn()] = NullInt;
    rowData[getWeightColumn()] = NullInt;
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

}

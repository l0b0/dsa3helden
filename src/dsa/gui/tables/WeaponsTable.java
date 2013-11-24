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
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.util.Optional;

public class WeaponsTable extends AbstractTable {

  protected int getNameColumn() {
    return 0;
  }

  int getTypeColumn() {
    return 1;
  }

  int getDamageColumn() {
    return 2;
  }

  int getBFColumn() {
    return 3;
  }

  int getKKColumn() {
    return 4;
  }

  int getWeightColumn() {
    return 5;
  }

  int getCountColumn() {
    return 6;
  }

  static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn() && columnIndex != getTypeColumn()
          && columnIndex != getDamageColumn()) {
        return NULL_INT.getClass();
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;

  boolean hasCount;

  public WeaponsTable(boolean withCount) {
    super();
    hasCount = withCount;
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Kategorie");
    mModel.addColumn("Schaden");
    mModel.addColumn("BF");
    mModel.addColumn("KK");
    mModel.addColumn("Gewicht");
    if (hasCount) {
      mModel.addColumn("Anzahl");
    }

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(0, 160));
    tcm.addColumn(new TableColumn(1, 160));
    tcm.addColumn(new TableColumn(2, 80));
    tcm.addColumn(new TableColumn(3, 35));
    tcm.addColumn(new TableColumn(4, 35));
    tcm.addColumn(new TableColumn(5, 80));
    if (hasCount) {
      tcm.addColumn(new TableColumn(6, 55));
    }

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
        if (e.getClickCount() > 1 && getDoubleClickListener() != null) {
          getDoubleClickListener().actionPerformed(new ActionEvent(this, 0, ""));
        }
      }
    });

    // mTable.setBackground(BACKGROUND_GRAY);
    JScrollPane scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);
    setCellRenderer();

    mSorter.setSortingListener(this);

  }

  public void removeSelectedWeapon() {
    String selectedArmour = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedArmour)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

  public void addWeapon(Weapon weapon, int count) {
    addWeapon(weapon, weapon.getName(), weapon.getBF(), count);
  }

  public void addWeapon(Weapon weapon, String name, int bf, int count) {
    Object[] rowData = new Object[hasCount ? 7 : 6];
    rowData[getNameColumn()] = name;
    rowData[getBFColumn()] = bf;
    rowData[getKKColumn()] = weapon.getKKBonus();
    rowData[getWeightColumn()] = new Optional<Integer>(count
        * weapon.getWeight());
    rowData[getTypeColumn()] = Weapons.getCategoryName(weapon.getType());
    if (weapon.getW6damage() == 0 && weapon.getConstDamage() == 0) {
      rowData[getDamageColumn()] = NULL_INT;
    }
    else {
      rowData[getDamageColumn()] = weapon.getW6damage() + "W+"
          + weapon.getConstDamage();
    }
    if (hasCount) {
      rowData[getCountColumn()] = count;
    }
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public void addUnknownWeapon(String name) {
    Object[] rowData = new Object[6];
    rowData[getNameColumn()] = name;
    rowData[getBFColumn()] = NULL_INT;
    rowData[getKKColumn()] = NULL_INT;
    rowData[getWeightColumn()] = NULL_INT;
    rowData[getTypeColumn()] = "-";
    rowData[getDamageColumn()] = "-";
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  @SuppressWarnings("unchecked")
  public void setWeaponCount(String item, int count) {
    if (!hasCount) return;
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(item)) {
        int oldCount = (Integer) mModel.getValueAt(i, getCountColumn());
        mModel.setValueAt(count, i, getCountColumn());
        Optional<Integer> oldWeight = (Optional<Integer>) mModel.getValueAt(i,
            getWeightColumn());
        if (oldWeight.hasValue()) {
          mModel.setValueAt(new Optional<Integer>(oldWeight.getValue() * count
              / oldCount), i, getWeightColumn());
        }
        break;
      }
    }
  }

  public void removeWeapon(String weapon) {
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(weapon)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

}

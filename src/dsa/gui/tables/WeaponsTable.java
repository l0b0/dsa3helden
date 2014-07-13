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
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.gui.lf.BGTableCellRenderer;
import dsa.gui.util.table.SpinnerCellEditor;
import dsa.gui.util.table.TableSorter;
import dsa.model.characters.Group;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;
import dsa.util.Optional;

public class WeaponsTable extends AbstractTable implements
    SpinnerCellEditor.EditorClient {

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
  
  int getWVColumn() {
    return Group.getInstance().getOptions().useWV() ? 5 : -1;
  }

  int getWeightColumn() {
    return Group.getInstance().getOptions().useWV() ? 6 : 5;
  }

  int getWorthColumn() {
    return Group.getInstance().getOptions().useWV() ? 7 : 6;
  }

  int getProjectilesColumn() {
    return Group.getInstance().getOptions().useWV() ? 8 : 7;
  }

  static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  public static interface ValueChanger {
    void bfChanged(String name, int bf);
    void projectilesChanged(String name, int projectiles);
  }

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn() && columnIndex != getTypeColumn()
          && columnIndex != getDamageColumn()) {
        return NULL_INT.getClass();
      }
      return super.getColumnClass(columnIndex);
    }

    @SuppressWarnings("unchecked")
    public boolean isCellEditable(int row, int column) {
      if (column == getBFColumn()) return true;
      if (column == getProjectilesColumn()) {
        Optional<Integer> value = (Optional<Integer>) getValueAt(row, column);
        return value.hasValue();
      }
      return false;
    }
  }

  MyTableModel mModel;

  boolean hasProjectiles;

  ValueChanger mValueChanger;

  public WeaponsTable(boolean withProjectiles) {
    this(withProjectiles, null);
  }

  public WeaponsTable(boolean withProjectiles, ValueChanger bfChanger) {
    super();
    boolean withWV = Group.getInstance().getOptions().useWV();
    hasProjectiles = withProjectiles;
    mValueChanger = bfChanger;
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Kategorie");
    mModel.addColumn("Schaden");
    mModel.addColumn("BF");
    mModel.addColumn("KK");
    if (withWV) {
      mModel.addColumn("WV");
    }
    mModel.addColumn("Gewicht");
    mModel.addColumn("Wert (S)");
    if (hasProjectiles) {
      mModel.addColumn("Geschosse");
    }

    SpinnerCellEditor numberEditor = new SpinnerCellEditor(this);
    numberEditor.setModel(new SpinnerNumberModel(0, -10, 12, 1));
    numberEditor.addCellEditorListener(new CellEditorListener() {

      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.bfChanged(editor.getCellInfo(), number.intValue());
        }
      }

      public void editingCanceled(ChangeEvent e) {
      }
    });
    
    SpinnerCellEditor projectilesEditor = new SpinnerCellEditor(this);
    projectilesEditor.setModel(new SpinnerNumberModel(0, 0, 100, 1));
    projectilesEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.projectilesChanged(editor.getCellInfo(), number.intValue());
        }        
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(0, 150));
    tcm.addColumn(new TableColumn(1, 150));
    tcm.addColumn(new TableColumn(2, 80));
    if (bfChanger != null) {
      tcm.addColumn(new TableColumn(3, 55, new BGTableCellRenderer(), numberEditor));
    }
    else {
      tcm.addColumn(new TableColumn(3, 35));
    }
    tcm.addColumn(new TableColumn(4, 35));
    if (withWV) {
      tcm.addColumn(new TableColumn(5, 50));
    }
    tcm.addColumn(new TableColumn(withWV ? 6 : 5, 80));
    tcm.addColumn(new TableColumn(withWV ? 7 : 6, 80));
    if (hasProjectiles) {
      tcm.addColumn(new TableColumn(withWV ? 8 : 7,80, new BGTableCellRenderer(), projectilesEditor));
    }

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

    mTable.setBackground(BACKGROUND_GRAY);
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
    addWeapon(weapon, weapon.getName(), weapon.getBF(), count, NULL_INT);
  }

  public void addWeapon(Weapon weapon, String name, int bf, int count, Optional<Integer> projectiles) {
    int nrOfColumns = 7;
    if (hasProjectiles) ++nrOfColumns;
    if (Group.getInstance().getOptions().useWV()) ++nrOfColumns;
    Object[] rowData = new Object[nrOfColumns];
    rowData[getNameColumn()] = name;
    rowData[getBFColumn()] = bf;
    rowData[getKKColumn()] = weapon.getKKBonus();
    if (Group.getInstance().getOptions().useWV()) {
      rowData[getWVColumn()] = weapon.getWV() != null ? weapon.getWV() : "-";
    }
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
    rowData[getWorthColumn()] = weapon.getWorth();
    if (hasProjectiles) {
      rowData[getProjectilesColumn()] = projectiles;
    }
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public void addUnknownWeapon(String name) {
    Object[] rowData = new Object[Group.getInstance().getOptions().useWV() ? 7 : 6];
    rowData[getNameColumn()] = name;
    rowData[getBFColumn()] = NULL_INT;
    rowData[getKKColumn()] = NULL_INT;
    if (Group.getInstance().getOptions().useWV()) {
      rowData[getWVColumn()] = "-";
    }
    rowData[getWeightColumn()] = NULL_INT;
    rowData[getTypeColumn()] = "-";
    rowData[getDamageColumn()] = "-";
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  // @SuppressWarnings("unchecked")
  public void setWeaponCount(String item, int count) {
    /*if (!hasCount) return;
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
    }*/
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

  public String getCellInfo(int row) {
    return mSorter.getValueAt(row, getNameColumn()).toString();
  }

  public int getCellMaximum(int row, int column) {
	  return -1;
  }

}

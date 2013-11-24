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

import dsa.gui.util.table.TableSorter;
import dsa.model.characters.Group;
import dsa.model.characters.GroupOptions;
import dsa.model.data.Cloth;
import dsa.model.data.Clothes;
import dsa.model.data.Thing;
import dsa.util.Optional;

public class ClothesTable extends AbstractTable {

  protected int getNameColumn() {
    return 0;
  }

  int getKSColumn() {
    return 3;
  }
  
  int getBEColumn() {
	return 4;
  }

  int getValueColumn() {
    return 1;
  }

  int getWeightColumn() {
    return 2;
  }

  static final Optional<Integer> NULL_INT = Optional.NULL_INT;

  static class MyValue implements Comparable<MyValue> {
    private final dsa.model.data.Thing.Currency currency;

    private final int value;

    public int compareTo(MyValue o) {
      if (currency != o.currency) {
        return currency.ordinal() < o.currency.ordinal() ? 1 : -1;
      }
      else if (value != o.value) {
        return value < o.value ? -1 : 1;
      }
      else
        return 0;
    }

    public MyValue(Thing.Currency currency, int value) {
      this.currency = currency;
      this.value = value;
    }

    public String toString() {
      return value + " " + currency.name();
    }
  }

  static final Optional<MyValue> NULL_VALUE = new Optional<MyValue>();

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == getWeightColumn()) {
        return NULL_INT.getClass();
      }
      else if (columnIndex == getValueColumn()) {
        return NULL_VALUE.getClass();
      }
      else if (columnIndex == getKSColumn()) {
        return NULL_INT.getClass();
      }
      else if (columnIndex == getBEColumn()) {
    	return NULL_INT.getClass();
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;
  
  boolean showBEColumn = false;
  
  public ClothesTable() {
    super();
    showBEColumn = Group.getInstance().getOptions().getClothesBE() == GroupOptions.ClothesBE.Items;
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Wert");
    mModel.addColumn("Gewicht");
    mModel.addColumn("KS");
    if (showBEColumn) {
    	mModel.addColumn("BE");
    }

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    int c = 0;
    tcm.addColumn(new TableColumn(c++, 140));
    tcm.addColumn(new TableColumn(c++, 60));
    tcm.addColumn(new TableColumn(c++, 60));
    tcm.addColumn(new TableColumn(c++, 60));
    if (showBEColumn) {
    	tcm.addColumn(new TableColumn(c++, 60));
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
    scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    setCellRenderer();

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);

    mSorter.setSortingListener(this);

  }
  
  JScrollPane scrollPane;

  public void removeSelectedThing() {
    String selectedThing = getSelectedItem();
    removeThing(selectedThing);
  }

  public void removeThing(String thing) {
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(thing)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }

  public void addThing(Thing thing) {
    addThing(thing, thing.getName(), false);
  }
  
  public void addThing(Thing thing, String name) {
    addThing(thing, name, false);
  }

  public void addThing(Thing thing, String name, boolean selectThisThing) {
    int colCount = showBEColumn ? 5 : 4;
    Object[] rowData = new Object[colCount];
    rowData[getNameColumn()] = name;
    rowData[getWeightColumn()] = new Optional<Integer>(thing.getWeight());
    if (thing.getValue().hasValue()) {
      rowData[getValueColumn()] = new Optional<MyValue>(new MyValue(thing
          .getCurrency(), thing.getValue().getValue().intValue()));
    }
    else {
      rowData[getValueColumn()] = NULL_VALUE;
    }
    Cloth cloth = Clothes.getInstance().getCloth(name);
    if (cloth != null) {
    	rowData[getKSColumn()] = new Optional<Integer>(cloth.getKS());
    	if (showBEColumn) {
    		rowData[getBEColumn()] = new Optional<Integer>(cloth.getBE());
    	}
    }
    else {
    	rowData[getKSColumn()] = NULL_INT;
    	if (showBEColumn) {
    		rowData[getBEColumn()] = NULL_INT;
    	}
    }
    mModel.addRow(rowData);
    if (selectThisThing) {
      for (int i = 0; i < mModel.getRowCount(); ++i) {
        if (mSorter.getValueAt(i, getNameColumn()).equals(thing.getName())) {
          setSelectedRow(i);
          break;
        }
      }
      mTable.scrollRectToVisible(mTable.getCellRect(mTable.getSelectedRow(), 0,
          true));
    }
  }

  public void addUnknownThing(String name) {
    int colCount = showBEColumn ? 5 : 4;
    Object[] rowData = new Object[colCount];
    rowData[getNameColumn()] = name;
    rowData[getValueColumn()] = NULL_VALUE;
    rowData[getWeightColumn()] = NULL_INT;
    rowData[getKSColumn()] = NULL_INT;
    if (showBEColumn) {
    	rowData[getBEColumn()] = NULL_INT;
    }
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }
  
  public void setSelectedItem(String item) {
	  for (int i = 0; i < mModel.getRowCount(); ++i) {
		  if (mModel.getValueAt(i, getNameColumn()).equals(item)) {
			  mTable.getSelectionModel().setSelectionInterval(i, i);
			  break;
		  }
	  }
  }

}

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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.gui.util.TableSorter;
import dsa.model.data.Thing;
import dsa.util.Optional;

public class ThingsTable extends AbstractTable {

  protected int getNameColumn() {
    return 0;
  }

  int getCategoryColumn() {
    return 1;
  }

  int getCountColumn() {
    return withCategoryColumn ? 2 : 1;
  }

  int getValueColumn() {
    int column = 1;
    if (withCountColumn) column++;
    if (withCategoryColumn) column++;
    return column;
  }

  int getWeightColumn() {
    int column = 2;
    if (withCountColumn) column++;
    if (withCategoryColumn) column++;
    return column;
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
      else if (columnIndex == getCountColumn() && withCountColumn) {
        return Integer.class;
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;
  
  public ThingsTable(boolean showCountColumn) {
    this(showCountColumn, true);
  }

  public ThingsTable(boolean showCountColumn, boolean showCategoryColumn) {
    super();
    withCountColumn = showCountColumn;
    withCategoryColumn = showCategoryColumn;
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    if (withCategoryColumn) mModel.addColumn("Kategorie");
    if (withCountColumn) mModel.addColumn("Anzahl");
    mModel.addColumn("Wert");
    mModel.addColumn("Gewicht");

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    int c = 0;
    tcm.addColumn(new TableColumn(c++, 140));
    if (withCategoryColumn) tcm.addColumn(new TableColumn(c++, 140));
    tcm.addColumn(new TableColumn(c++, 60));
    tcm.addColumn(new TableColumn(c++, 60));
    if (withCountColumn) tcm.addColumn(new TableColumn(c++, 60));

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
    addThing(thing, false);
  }

  public void addThing(Thing thing, boolean selectThisThing) {
    int colCount = 4;
    if (withCountColumn) ++colCount;
    if (withCategoryColumn) ++colCount;
    Object[] rowData = new Object[colCount];
    rowData[getNameColumn()] = thing.getName();
    if (withCategoryColumn) rowData[getCategoryColumn()] = thing.getCategory();
    rowData[getWeightColumn()] = new Optional<Integer>(thing.getWeight());
    if (thing.getValue().hasValue()) {
      rowData[getValueColumn()] = new Optional<MyValue>(new MyValue(thing
          .getCurrency(), thing.getValue().getValue().intValue()));
    }
    else {
      rowData[getValueColumn()] = NULL_VALUE;
    }
    if (withCountColumn) rowData[getCountColumn()] = Integer.valueOf(1);
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
    int colCount = 4;
    if (withCountColumn) ++colCount;
    if (withCategoryColumn) ++colCount;
    Object[] rowData = new Object[colCount];
    rowData[getNameColumn()] = name;
    if (withCategoryColumn) rowData[getCategoryColumn()] = "-";
    rowData[getValueColumn()] = NULL_VALUE;
    rowData[getWeightColumn()] = NULL_INT;
    if (withCountColumn) rowData[getCountColumn()] = Integer.valueOf(1);
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  private boolean withCountColumn;

  private boolean withCategoryColumn;

  @SuppressWarnings("unchecked")
  public void setCount(String thing, int count) {
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(thing)) {
        int oldCount = (Integer) mModel.getValueAt(i, getCountColumn());
        mModel.setValueAt(new Integer(count), i, getCountColumn());
        Optional<Integer> oldWeight = (Optional<Integer>) mModel.getValueAt(i,
            getWeightColumn());
        if (oldWeight.hasValue()) {
          mModel.setValueAt(new Optional<Integer>(oldWeight.getValue() * count
              / oldCount), i, getWeightColumn());
        }
        Optional<MyValue> oldValue = (Optional<MyValue>) mModel.getValueAt(i,
            getValueColumn());
        if (oldValue.hasValue()) {
          long newValue = (long) oldValue.getValue().value * count / oldCount;
          Thing.Currency currency = oldValue.getValue().currency;
          while (currency != Thing.Currency.K) {
            newValue *= 10;
            currency = Thing.Currency.values()[currency.ordinal() + 1];
          }
          while (currency != Thing.Currency.D && newValue > 100) {
            newValue /= 10;
            currency = Thing.Currency.values()[currency.ordinal() - 1];
          }
          mModel.setValueAt(new Optional<MyValue>(new MyValue(currency,
              (int) newValue)), i, getValueColumn());
        }
        break;
      }
    }
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mSorter.getValueAt(i, getNameColumn()).equals(thing)) {
        setSelectedRow(i);
        break;
      }
    }
  }

}

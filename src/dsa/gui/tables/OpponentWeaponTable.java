/*
   Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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
import java.util.HashMap;

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
import dsa.gui.util.table.TextFieldCellEditor;
import dsa.model.DiceSpecification;
import dsa.util.Optional;

public final class OpponentWeaponTable extends AbstractTable 
    implements SpinnerCellEditor.EditorClient, TextFieldCellEditor.EditorClient {

  public static interface ValueChanger {
    void nameChanged(int item, String newName);
    void atChanged(int item, int at);
    void paChanged(int item, int pa);
    void tpChanged(int item, DiceSpecification tp);
  }
  
  protected int getNameColumn() {
    return 0;
  }

  private int getTPColumn() {
    return 1;
  }
  
  private int getATColumn() {
    return 2;
  }
  
  private int getPAColumn() {
    return 3;
  }
  
  private static Optional<Integer> NULL_INT = new Optional<Integer>();
  
  private class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == getPAColumn()) {
        return NULL_INT.getClass();
      }
      else if (columnIndex != getNameColumn() && columnIndex != getTPColumn()) {
        return Integer.class;
      }
      else if (columnIndex == getTPColumn()) {
        return DiceSpecification.class;
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      if (getColumnClass(column) == NULL_INT.getClass()) {
        return ((Optional)getValueAt(row, column)).hasValue();
      }
      return true;
    }
  }
  
  private MyTableModel mModel;
  
  private ValueChanger mValueChanger;
  
  private HashMap<String, Integer> cellInfoMap = new HashMap<String, Integer>();
  
  public void setValueChanger(ValueChanger changer) {
    mValueChanger = changer;
  }

  public OpponentWeaponTable() {
    super();
    
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("TP");
    mModel.addColumn("AT");
    mModel.addColumn("PA");

    SpinnerCellEditor atEditor = new SpinnerCellEditor(this);
    atEditor.setModel(new SpinnerNumberModel(10, 1, 40, 1));
    atEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.atChanged(cellInfoMap.get(editor.getCellInfo()), number.intValue());
        }        
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    SpinnerCellEditor paEditor = new SpinnerCellEditor(this);
    paEditor.setModel(new SpinnerNumberModel(10, 1, 40, 1));
    paEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.paChanged(cellInfoMap.get(editor.getCellInfo()), number.intValue());
        }        
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    TextFieldCellEditor nameEditor = new TextFieldCellEditor(this, getNameColumn());
    nameEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          TextFieldCellEditor editor = (TextFieldCellEditor) e.getSource();
          mValueChanger.nameChanged(cellInfoMap.get(editor.getCellInfo()), editor.getText());
        }
      }
      public void editingCanceled(ChangeEvent e) {
      }
    });

    TextFieldCellEditor tpEditor = new TextFieldCellEditor(this, getTPColumn());
    tpEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          TextFieldCellEditor editor = (TextFieldCellEditor) e.getSource();
          String text = editor.getText();
          DiceSpecification ds = DiceSpecification.parse(text);
          mValueChanger.tpChanged(cellInfoMap.get(editor.getCellInfo()), ds);
        }
      }
      public void editingCanceled(ChangeEvent e) {
      }
    });

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(getNameColumn(), 140, new BGTableCellRenderer(), nameEditor));
    tcm.addColumn(new TableColumn(getTPColumn(), 80, new BGTableCellRenderer(), tpEditor));
    tcm.addColumn(new TableColumn(getATColumn(), 55, new BGTableCellRenderer(), atEditor));
    tcm.addColumn(new TableColumn(getPAColumn(), 55, new BGTableCellRenderer(), paEditor));
  
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
    setCellRenderer();

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);

    mSorter.setSortingListener(this);    
  }
  
  public void removeSelectedWeapon() {
    int selectedRow = mTable.getSelectedRow();
    if (selectedRow != -1) {
        if (mTable.isEditing()) {
          mTable.removeEditor();
        }
        mModel.removeRow(selectedRow);
        setSelectedRow((selectedRow > 0) ? selectedRow - 1 : 0);
        return;
    }
  }
  
  public void addWeapon(String name, DiceSpecification tp, int at, Optional<Integer> pa) {
    Object[] rowData = new Object[4];
    rowData[getNameColumn()] = name;
    rowData[getTPColumn()] = tp;
    rowData[getATColumn()] = at;
    rowData[getPAColumn()] = pa;
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }
  
  public String getCellInfo(int row) {
    String info = "" + row;
    cellInfoMap.put(info, row);
    return info;
  }
  
  public int getSelectedIndex() {
    return mTable.getSelectedRow();
  }

  public boolean canUseText(String cellInfo, String text, int id) {
    if (text == null || text.length() == 0) return false;
    if (id == getTPColumn()) {
      try {
        DiceSpecification.parse(text);
        return true;
      }
      catch (NumberFormatException e) {
        return false;
      }
    }
    else return true;
  }
}

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
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;

import dsa.gui.lf.BGTableCellRenderer;
import dsa.gui.util.table.FormattedTextFieldCellEditor;
import dsa.gui.util.table.TableSorter;
import dsa.gui.util.table.TextFieldCellEditor;
import dsa.model.data.Adventure;

public class AdventureTable extends AbstractTable implements 
  TextFieldCellEditor.EditorClient, FormattedTextFieldCellEditor.EditorClient {

  protected int getNameColumn() {
    return 0;
  }

  int getAPColumn() {
    return 1;
  }
  
  int getIndexColumn() { 
    return 2;
  }

  class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn()) {
        return Integer.class;
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return true;
    }
  }

  MyTableModel mModel;
  
  public interface AdventureChanger {
    void changeAP(int index, int ap);
    void changeName(int index, String newName);
  }
  
  private AdventureChanger mAdventureChanger;
  
  public AdventureTable() {
    this(null);
  }
  
  public AdventureTable(AdventureChanger adChanger) {
    super();
    mAdventureChanger = adChanger;
    
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("AP");
    mModel.addColumn("Index");

    FormattedTextFieldCellEditor numberEditor = new FormattedTextFieldCellEditor(
        new NumberFormatter(NumberFormat.getIntegerInstance()), this);
    numberEditor.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    numberEditor.addCellEditorListener(new CellEditorListener() {

      public void editingStopped(ChangeEvent e) {
        if (mAdventureChanger != null) {
          FormattedTextFieldCellEditor editor = (FormattedTextFieldCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          int index = Integer.parseInt(editor.getCellInfo());
          mAdventureChanger.changeAP(index, number.intValue());
        }
      }

      public void editingCanceled(ChangeEvent e) {
      }
    });
    
    TextFieldCellEditor textEditor = new TextFieldCellEditor(this, 1);
    textEditor.addCellEditorListener(new CellEditorListener() {
      
      public void editingStopped(ChangeEvent e) {
        if (mAdventureChanger != null) {
          TextFieldCellEditor editor = (TextFieldCellEditor) e.getSource();
          int index = Integer.parseInt(editor.getCellInfo());
          mAdventureChanger.changeName(index, editor.getText());
        }
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    if (mAdventureChanger != null) {
      tcm.addColumn(new TableColumn(0, 140, new BGTableCellRenderer(), textEditor));
      tcm.addColumn(new TableColumn(1, 25, new BGTableCellRenderer(), numberEditor));
    }
    else {
      tcm.addColumn(new TableColumn(0, 140));
      tcm.addColumn(new TableColumn(1, 25));
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
    setCellRenderer();

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);

    mSorter.setSortingListener(this);

  }

  public void removeSelectedAdventure() {
    String selectedAdventure = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedAdventure)) {
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }
  
  public int getSelectedItemIndex() {
    int row = 0;
    if (mTable.getSelectedRow() != -1) {
      row = mTable.getSelectedRow();
    }
    if (mTable.getRowCount() == 0) return -1;
    return (Integer) mSorter.getValueAt(row, getIndexColumn());
  }
  
  public void addAdventure(Adventure adventure) {
    Object[] rowData = new Object[3];
    rowData[getNameColumn()] = adventure.getName();
    rowData[getAPColumn()] = adventure.getAp();
    rowData[getIndexColumn()] = adventure.getIndex();
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }

  public String getCellInfo(int row) {
    return mSorter.getValueAt(row, getIndexColumn()).toString();
  }

  public boolean canUseText(String cellInfo, String text, int id) {
    return text != null && text.length() > 0;
  }

}

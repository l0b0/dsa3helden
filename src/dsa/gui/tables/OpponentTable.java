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
import dsa.gui.util.table.TextFieldCellEditor;
import dsa.model.data.Opponent;

public final class OpponentTable extends AbstractTable 
    implements SpinnerCellEditor.EditorClient, TextFieldCellEditor.EditorClient {

  public static interface ValueChanger {
    boolean canUseName(String newName);
    void nameChanged(String oldName, String newName);
    void categoryChanged(String item, String category);
    void leChanged(String item, int le);
    void rsChanged(String item, int rs);
    void mrChanged(String item, int mr);
  }
  
  protected int getNameColumn() {
    return 0;
  }

  private int getCategoryColumn() {
    return 1;
  }
  
  private int getLEColumn() {
    return 2;
  }
  
  private int getRSColumn() {
    return 3;
  }
  
  private int getMRColumn() {
    return 4;
  }
  
  private class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex != getNameColumn() && columnIndex != getCategoryColumn()) {
        return Integer.class;
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      return canEdit;
    }
  }
  
  private MyTableModel mModel;
  
  private final boolean canEdit;
  
  private ValueChanger mValueChanger;
  
  public void setValueChanger(ValueChanger changer) {
    mValueChanger = changer;
  }

  public OpponentTable(boolean canEdit) {
    super();
    this.canEdit = canEdit;
    
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Kategorie");
    mModel.addColumn("LE");
    mModel.addColumn("RS");
    mModel.addColumn("MR");

    SpinnerCellEditor leEditor = new SpinnerCellEditor(this);
    leEditor.setModel(new SpinnerNumberModel(1, 1, 10000, 1));
    leEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.leChanged(editor.getCellInfo(), number.intValue());
        }        
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    SpinnerCellEditor mrEditor = new SpinnerCellEditor(this);
    mrEditor.setModel(new SpinnerNumberModel(0, -50, 1000, 1));
    mrEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.mrChanged(editor.getCellInfo(), number.intValue());
        }        
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });

    SpinnerCellEditor rsEditor = new SpinnerCellEditor(this);
    rsEditor.setModel(new SpinnerNumberModel(0, 0, 50, 1));
    rsEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          SpinnerCellEditor editor = (SpinnerCellEditor) e
              .getSource();
          Number number = (Number) editor.getValue();
          mValueChanger.rsChanged(editor.getCellInfo(), number.intValue());
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
          mValueChanger.nameChanged(editor.getCellInfo(), editor.getText());
        }
      }
      public void editingCanceled(ChangeEvent e) {
      }
    });

    TextFieldCellEditor categoryEditor = new TextFieldCellEditor(this, getCategoryColumn());
    categoryEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        if (mValueChanger != null) {
          TextFieldCellEditor editor = (TextFieldCellEditor) e.getSource();
          mValueChanger.categoryChanged(editor.getCellInfo(), editor.getText());
        }
      }
      public void editingCanceled(ChangeEvent e) {
      }
    });

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(getNameColumn(), 140, new BGTableCellRenderer(), nameEditor));
    tcm.addColumn(new TableColumn(getCategoryColumn(), 140, new BGTableCellRenderer(), categoryEditor));
    tcm.addColumn(new TableColumn(getLEColumn(), 45, new BGTableCellRenderer(), leEditor));
    tcm.addColumn(new TableColumn(getRSColumn(), 45, new BGTableCellRenderer(), rsEditor));
    tcm.addColumn(new TableColumn(getMRColumn(), 45, new BGTableCellRenderer(), mrEditor));
  
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
  
  public void removeSelectedOpponent() {
    String selectedOpponent = getSelectedItem();
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(selectedOpponent)) {
        if (mTable.isEditing()) {
          mTable.removeEditor();
        }
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }
  }
  
  public void removeOpponent(String opponent) {
    for (int i = 0; i < mModel.getRowCount(); ++i) {
      if (mModel.getValueAt(i, getNameColumn()).equals(opponent)) {
        if (mTable.isEditing()) {
          mTable.removeEditor();
        }
        mModel.removeRow(i);
        setSelectedRow((i > 0) ? i - 1 : 0);
        return;
      }
    }    
  }
  
  public void addOpponent(Opponent opponent) {
    Object[] rowData = new Object[5];
    rowData[getNameColumn()] = opponent.getName();
    rowData[getCategoryColumn()] = opponent.getCategory();
    rowData[getLEColumn()] = opponent.getLE();
    rowData[getRSColumn()] = opponent.getRS();
    rowData[getMRColumn()] = opponent.getMR();
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }
  
  public String getCellInfo(int row) {
    return mSorter.getValueAt(row, getNameColumn()).toString();
  }

  public boolean canUseText(String cellInfo, String text, int id) {
    if (id == getNameColumn()) {
      return text != null && text.length() > 0 && 
        (text.equals(cellInfo) || (mValueChanger != null && mValueChanger.canUseName(text)));
    }
    else return true;
  }
}

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
package dsa.gui.util.table;

import java.awt.Component;
import java.util.EventObject;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;

public final class ComboBoxCellEditor extends DefaultCellEditor {

  public interface EditorClient {
    List<String> getItems(int row, int column);
    void itemSelected(int row, int column, int index);
  }

  public ComboBoxCellEditor(EditorClient client) {
    super(new JComboBox());
    mClient = client;
  }

  private Object oldObject;

  private int mRow;
  private int mColumn;

  private EditorClient mClient;
  
  private boolean listen = true;

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    
    listen = false;
    
    JComboBox box = (JComboBox)(this.editorComponent);
    box.removeAllItems();
    int index = -1, i = 0;
    for (String item : mClient.getItems(row, column)) {
      box.addItem(item);
      if (item.equals(value)) index = i;
      ++i;
    }
    if (index != -1) {
      box.setSelectedIndex(index);
      oldObject = value;
    }
    else oldObject = null;
    
    listen = true;
    mRow = row;
    mColumn = column;
    return this.editorComponent;
  }
  
  public int getSelectedIndex() {
    return ((JComboBox)this.editorComponent).getSelectedIndex();
  }
  
  public Object getCellEditorValue() {
    return ((JComboBox)this.editorComponent).getSelectedItem();
  }

  public boolean isCellEditable(EventObject anEvent) {
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    return false;
  }

  public boolean stopCellEditing() {
    fireEditingStopped();
    return true;
  }

  public void cancelCellEditing() {
    if (oldObject != null) ((JComboBox)this.editorComponent).setSelectedItem(oldObject);
    fireEditingCanceled();
  }

  protected void fireEditingCanceled() {
    Object[] listeners1 = listenerList.getListenerList();
    for (int i = listeners1.length - 2; i >= 0; i -= 2) {
      if (listeners1[i] == CellEditorListener.class) {
        if (event == null) event = new javax.swing.event.ChangeEvent(this);
        ((CellEditorListener) listeners1[i + 1]).editingCanceled(event);
      }
    }
  }

  protected void fireEditingStopped() {
    Object[] listeners2 = listeners.getListenerList();
    for (int i = listeners2.length - 2; i >= 0; i -= 2) {
      if (listeners2[i] == CellEditorListener.class) {
        if (event == null) event = new javax.swing.event.ChangeEvent(this);
        ((CellEditorListener) listeners2[i + 1]).editingStopped(event);
      }
    }
    if (listen) mClient.itemSelected(mRow, mColumn, getSelectedIndex());
  }

  public void addCellEditorListener(CellEditorListener l) {
    listeners.add(CellEditorListener.class, l);
  }

  public void removeCellEditorListener(CellEditorListener l) {
    listeners.remove(CellEditorListener.class, l);
  }

  private final EventListenerList listeners = new EventListenerList();

  private javax.swing.event.ChangeEvent event = null;

}
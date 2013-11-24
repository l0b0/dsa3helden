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
package dsa.gui.util;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.text.NumberFormatter;

public final class FormattedTextFieldCellEditor extends JFormattedTextField
    implements TableCellEditor {

  public interface EditorClient {
    String getCellInfo(int row);
  }

  public FormattedTextFieldCellEditor(NumberFormatter formatter,
      EditorClient client) {
    super(formatter);
    mClient = client;
  }

  private Object oldObject;

  private int mColumn;

  private EditorClient mClient;

  private String mCellInfo;

  public String getCellInfo() {
    return mCellInfo;
  }

  public int getColumn() {
    return mColumn;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    this.setText(value.toString());
    oldObject = value;
    mColumn = column;
    mCellInfo = mClient.getCellInfo(row);
    return this;
  }

  public Object getCellEditorValue() {
    return this.getValue();
  }

  public boolean isCellEditable(EventObject anEvent) {
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    return false;
  }

  public boolean stopCellEditing() {
    if (isEditValid()) {
      try {
        commitEdit();
      }
      catch (java.text.ParseException e) {
        return false;
      }
      fireEditingStopped();
    }
    return isEditValid();
  }

  public void cancelCellEditing() {
    setText(oldObject.toString());
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

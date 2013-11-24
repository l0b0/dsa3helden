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

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

import dsa.util.Optional;

public class SpinnerCellEditor extends JSpinner implements TableCellEditor {

  private Integer oldObject;

  private String mCellInfo;

  public interface EditorClient {
    String getCellInfo(int row);
  }

  private EditorClient mClient;

  public String getCellInfo() {
    return mCellInfo;
  }

  public SpinnerCellEditor(EditorClient client) {
    mClient = client;
  }

  private boolean isOptional;

  @SuppressWarnings("unchecked")
  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    isOptional = value instanceof Optional;
    if (isOptional) {
      Optional<Integer> casted = (Optional<Integer>) value;
      setValue(casted.getValue());
      oldObject = casted.getValue();
    }
    else {
      setValue(value);
      oldObject = (Integer) value;
    }
    mCellInfo = mClient.getCellInfo(row);
    return this;
  }

  private final EventListenerList listeners = new EventListenerList();

  private javax.swing.event.ChangeEvent event = null;

  public void addCellEditorListener(CellEditorListener l) {
    listeners.add(CellEditorListener.class, l);
  }

  public void cancelCellEditing() {
    setValue(oldObject);
    fireEditingCanceled();
  }

  public Object getCellEditorValue() {
    if (isOptional) {
      return new Optional<Integer>(((Number) this.getValue()).intValue());
    }
    else {
      return this.getValue();
    }
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

  public boolean isCellEditable(EventObject anEvent) {
    return true;
  }

  public void removeCellEditorListener(CellEditorListener l) {
    listeners.remove(CellEditorListener.class, l);
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    return false;
  }

  public boolean stopCellEditing() {
    fireEditingStopped();
    return true;
  }

}

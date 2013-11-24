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

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;

public final class TextFieldCellEditor extends DefaultCellEditor {

  public interface EditorClient {
    String getCellInfo(int row);
    boolean canUseText(String cellInfo, String text, int id);
  }

  public TextFieldCellEditor(EditorClient client, int id) {
    super(new JTextField());
    mClient = client;
    mID = id;
  }

  private Object oldObject;

  private int mColumn;

  private EditorClient mClient;

  private String mCellInfo;
  
  private int mID;

  public String getCellInfo() {
    return mCellInfo;
  }

  public int getColumn() {
    return mColumn;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    ((JTextField)this.editorComponent).setText(value.toString());
    oldObject = value;
    mColumn = column;
    mCellInfo = mClient.getCellInfo(row);
    return this.editorComponent;
  }
  
  public String getText() {
    return ((JTextField)this.editorComponent).getText();
  }

  public Object getCellEditorValue() {
    return ((JTextField)this.editorComponent).getText();
  }

  public boolean isCellEditable(EventObject anEvent) {
    return true;
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    return false;
  }

  public boolean stopCellEditing() {
    if (mClient.canUseText(mCellInfo, ((JTextField)this.editorComponent).getText(), mID)) {
      fireEditingStopped();
      return true;
    }
    else {
      return false;
    }
  }

  public void cancelCellEditing() {
    ((JTextField)this.editorComponent).setText(oldObject.toString());
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
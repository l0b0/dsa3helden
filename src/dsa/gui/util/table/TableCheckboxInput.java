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
package dsa.gui.util.table;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public final class TableCheckboxInput {
  
  private TableCheckboxInput() {}
  
  public interface Callbacks {
    void checkBoxValueChanged(int row, int column, boolean newValue);
  }
  
  private static class TableInput {
    
    private JTable mTable;
    private Callbacks mCallbacks;
    
    protected TableInput(JTable table, Callbacks callbacks) {
      mTable = table;
      mCallbacks = callbacks;
    }
    
    private void changeValue(Component component, int row, int column)
    {
      if (!(component instanceof JCheckBox)) return;
      JCheckBox checkBox = (JCheckBox) component;
      if (!checkBox.isEnabled()) return;
      boolean oldValue = checkBox.isSelected();
      checkBox.setSelected(!oldValue);
      mTable.setValueAt(Boolean.valueOf(!oldValue), row, column);
      mCallbacks.checkBoxValueChanged(row, column, !oldValue);
    }
    

    protected void forwardToButton(MouseEvent e) {
      TableColumnModel columnModel = mTable.getColumnModel();
      int column = columnModel.getColumnIndexAtX(e.getX());
      int row = e.getY() / mTable.getRowHeight();
      if (row < 0 || row >= mTable.getRowCount()) return;
      if (column < 3 || column >= mTable.getColumnCount()) return;
      Component component = mTable.getCellRenderer(row, column)
          .getTableCellRendererComponent(mTable,
              mTable.getValueAt(row, column), true, true, row, column);
      changeValue(component, row, column);
    }

    protected void forwardToButton(KeyEvent e) {
      if (e.getKeyCode() != KeyEvent.VK_SPACE) return;
      int column = mTable.getSelectedColumn();
      int row = mTable.getSelectedRow();
      if (row < 0 || row >= mTable.getRowCount()) return;
      if (column < 3 || column >= mTable.getColumnCount()) return;
      Component component = mTable.getCellRenderer(row, column)
          .getTableCellRendererComponent(mTable,
              mTable.getValueAt(row, column), true, true, row, column);
      changeValue(component, row, column);
    }
  }
  
  private static final class TableKeyListener extends TableInput implements KeyListener {
    public TableKeyListener(JTable table, Callbacks callbacks) {
      super(table, callbacks);
    }
    public void keyPressed(KeyEvent e) {
      forwardToButton(e);
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    
  }

  private static final class TableMouseListener extends TableInput implements MouseListener {

    public TableMouseListener(JTable table, Callbacks callbacks) {
      super(table, callbacks);
    }

    public void mouseClicked(MouseEvent e) {
      forwardToButton(e);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
  }

  public static void attachToTable(Callbacks callbacks, JTable table) {
    table.addMouseListener(new TableMouseListener(table, callbacks));
    table.addKeyListener(new TableKeyListener(table, callbacks));
  }
  
  private static class FocusCheckBox extends JCheckBox {
    public FocusCheckBox() {
      super();
      mPretendFocus = false;
    }
    
    public void pretendFocus(boolean pretend) {
      mPretendFocus = pretend;
    }

    public boolean isFocusOwner() {
      return mPretendFocus ? true : super.isFocusOwner();
    }
    
    private boolean mPretendFocus;
  }  

  private static final class CheckBoxCellRenderer implements TableCellRenderer {
    public CheckBoxCellRenderer(TableCellRenderer defaultRenderer, boolean enabled) {
      mRenderer = defaultRenderer;
      mCheckBox = new FocusCheckBox();
      mEnabled = enabled;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      if (row == -1 || value == null)
        return mRenderer.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);
      mCheckBox.setSelected(((Boolean)value).booleanValue());
      mCheckBox.setEnabled(mEnabled);
      mCheckBox.pretendFocus(hasFocus);
      return mCheckBox;
    }

    FocusCheckBox mCheckBox;

    TableCellRenderer mRenderer;
    
    boolean mEnabled;
  }
  
  public static TableCellRenderer createCheckBoxCellRenderer(TableCellRenderer defaultRenderer, boolean enabled) {
    return new CheckBoxCellRenderer(defaultRenderer, enabled);
  }
  
  private static class DummyCellEditor implements TableCellEditor {

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      return null;
    }

    public Object getCellEditorValue() {
      return null;
    }

    public boolean isCellEditable(EventObject anEvent) {
      return false;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
      return false;
    }

    public boolean stopCellEditing() {
      return false;
    }

    public void cancelCellEditing() {
    }

    public void addCellEditorListener(CellEditorListener l) {
    }

    public void removeCellEditorListener(CellEditorListener l) {
    }
  }
  
  public static TableCellEditor createDummyCellEditor() {
    return new DummyCellEditor();
  }
}

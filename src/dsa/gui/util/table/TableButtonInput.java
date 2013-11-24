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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import dsa.gui.util.ImageManager;

public final class TableButtonInput {
  
  private TableButtonInput() {}
  
  public interface Callbacks {
    void setClickedRow(int row);
  }
  
  private static final class ButtonChanger implements Runnable {
    
    public ButtonChanger(Component component, int row, int column, int type,
                          Callbacks callbacks, JTable table) {
      this.type = type;
      this.component = component;
      this.row = row;
      this.column = column;
      manualReset = false;
      this.callbacks = callbacks;
      mTable = table;
    }
    
    private int type;
    private Component component;
    private int row;
    private int column;
    private boolean manualReset;
    private Callbacks callbacks;
    private JTable mTable;
    
    public void run() {
      if (component instanceof AbstractButton) {
        if (type == 0) {
          callbacks.setClickedRow(row);
          ((AbstractButton) component).doClick();
        }
        else if (type == 1) {
          TableCellRenderer renderer = mTable.getCellRenderer(row, column);
          if (renderer instanceof ButtonCellRenderer) {
            ((ButtonCellRenderer)renderer).setPressedCell(row, column);
            mTable.repaint(mTable.getCellRect(row, column, true));
          }
          type = 2;
          manualReset = true;
        }
        else if (type == 2) {
          if (manualReset) {
            try {
              Thread.sleep(300);
            }
            catch (InterruptedException ex) {}            
          }
          TableCellRenderer renderer = mTable.getCellRenderer(row, column);
          if (renderer instanceof ButtonCellRenderer) {
            ((ButtonCellRenderer)renderer).setPressedCell(-1, -1);
            mTable.repaint(mTable.getCellRect(row, column, true));
            type = 0;
            // the 'clicked' event is simulated here manually to come after
            // the 'released' event
            javax.swing.SwingUtilities.invokeLater(this);
          }
        }
      }
    }
  }  

  private static class TableInput {
    
    private JTable mTable;
    private Callbacks mCallbacks;
    
    protected TableInput(JTable table, Callbacks callbacks) {
      mTable = table;
      mCallbacks = callbacks;
    }
    
    protected void forwardToButton(MouseEvent e, int type) {
      TableColumnModel columnModel = mTable.getColumnModel();
      int column = columnModel.getColumnIndexAtX(e.getX());
      int row = e.getY() / mTable.getRowHeight();
      if (row < 0 || row >= mTable.getRowCount()) return;
      if (column < 3 || column >= mTable.getColumnCount()) return;
      Component component = mTable.getCellRenderer(row, column)
          .getTableCellRendererComponent(mTable,
              mTable.getValueAt(row, column), true, true, row, column);
      if (!component.isEnabled()) return;
      ButtonChanger changer = new ButtonChanger(component, row, column, type, mCallbacks, mTable);
      changer.run();
    }

    protected void forwardToButton(KeyEvent e, int type) {
      if (e.getKeyCode() != KeyEvent.VK_SPACE) return;
      int column = mTable.getSelectedColumn();
      int row = mTable.getSelectedRow();
      if (row < 0 || row >= mTable.getRowCount()) return;
      if (column < 3 || column >= mTable.getColumnCount()) return;
      Component component = mTable.getCellRenderer(row, column)
          .getTableCellRendererComponent(mTable,
              mTable.getValueAt(row, column), true, true, row, column);
      if (!component.isEnabled()) return;
      ButtonChanger changer = new ButtonChanger(component, row, column, type, mCallbacks, mTable);
      changer.run();
      // automatically release after the time (doesn't receive keyboard release event)
      javax.swing.SwingUtilities.invokeLater(changer);
    }
  }
  
  private static final class TableKeyListener extends TableInput implements KeyListener {
    public TableKeyListener(JTable table, Callbacks callbacks) {
      super(table, callbacks);
    }
    public void keyPressed(KeyEvent e) {
      forwardToButton(e, 1);
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
      // forwardToButton(e, 0);
    }

    public void mousePressed(MouseEvent e) {
      forwardToButton(e, 1);
    }

    public void mouseReleased(MouseEvent e) {
      forwardToButton(e, 2);
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
  
  private static interface FocusPretender {
    void pretendFocus(boolean pretend);
  }
  
  private static class PretenderButton extends JButton implements FocusPretender {
    public PretenderButton() {
      super();
      mPretendFocus = false;
    }
    
    public PretenderButton(ImageIcon icon) {
      super(icon);
      mPretendFocus = false;
    }
    
    public PretenderButton(String text) {
      super(text);
      mPretendFocus = false;
    }
    
    public void pretendFocus(boolean pretend) {
      mPretendFocus = pretend;
    }

    public boolean isFocusOwner() {
      return mPretendFocus ? true : super.isFocusOwner();
    }
    
    public void doClick() {
      boolean wasEnabled = isEnabled();
      setEnabled(true);
      super.doClick();
      setEnabled(wasEnabled);
    }

    private boolean mPretendFocus;
  }  

  private static class PretenderToggleButton extends JToggleButton implements FocusPretender {
    public PretenderToggleButton() {
      super();
      mPretend = false;
    }
    public PretenderToggleButton(ImageIcon icon) {
      super(icon);
      mPretend = false;
    }
    public PretenderToggleButton(String text) {
      super(text);
      mPretend = false;
    }
    public void pretendFocus(boolean pretend) {
      mPretend = pretend;
    }
    public boolean isFocusOwner() {
      return mPretend ? true : super.isFocusOwner();
    }
    public void doClick() {
      boolean wasEnabled = isEnabled();
      setEnabled(true);
      super.doClick();
      setEnabled(wasEnabled);
    }
    
    private boolean mPretend;
  }
  
  public static JButton createButton(ImageIcon icon) {
    return new PretenderButton(icon);
  }
  
  public static JToggleButton createToggleButton(ImageIcon icon) { 
    return new PretenderToggleButton(icon);
  }

  private static final class ButtonCellRenderer implements TableCellRenderer {
    public ButtonCellRenderer(AbstractButton button,
        TableCellRenderer defaultRenderer) {
      mButton = button;
      mRenderer = defaultRenderer;
      pressedRow = -1;
      pressedColumn = -1;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      if (row == -1)
        return mRenderer.getTableCellRendererComponent(table, value,
            isSelected, hasFocus, row, column);
      if (mButton instanceof JToggleButton) {
        mButton.setSelected(((Boolean) value).booleanValue());
        mButton.setIcon(((Boolean) value).booleanValue() ? unlockedIcon
            : lockedIcon);
      }
      else
        mButton.setEnabled(((Boolean) value).booleanValue());
      if (mButton instanceof FocusPretender) {
        ((FocusPretender)mButton).pretendFocus(hasFocus);
      }
      if (mButton instanceof JButton) { // not for toggle buttons
        mButton.setSelected(row == pressedRow && column == pressedColumn);
      }
      return mButton;
    }
    
    public void setPressedCell(int row, int column) {
      pressedRow = row;
      pressedColumn = column;
    }

    ImageIcon lockedIcon = ImageManager.getIcon("locked");

    ImageIcon unlockedIcon = ImageManager.getIcon("unlocked");

    javax.swing.AbstractButton mButton;

    TableCellRenderer mRenderer;
    
    int pressedRow; 
    int pressedColumn;

  }
  
  public static TableCellRenderer createButtonCellRenderer(AbstractButton button,
        TableCellRenderer defaultRenderer) {
    return new ButtonCellRenderer(button, defaultRenderer);
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

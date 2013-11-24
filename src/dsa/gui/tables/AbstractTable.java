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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import dsa.gui.util.TableSorter;

public abstract class AbstractTable implements TableSorter.SortingListener {

  protected JPanel thePanel;

  protected JTable mTable;

  protected TableSorter mSorter;
  
  protected static class ViewportFillingTable extends JTable {
    public ViewportFillingTable(TableModel tm, TableColumnModel tcm) {
      super(tm, tcm);
    }
    
    public boolean getScrollableTracksViewportHeight() {
      return getPreferredSize().height < getParent().getHeight();
    }
    
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
      if (ks != pasteStroke && ks != cutStroke && ks != copyStroke) {
        return super.processKeyBinding(ks, e, condition, pressed);
      }
      else {
        return true; // stop search! Cut, Copy, Paste is done by this program manually!
      }
    }
    
    private static KeyStroke pasteStroke = KeyStroke.getKeyStroke("control V");
    private static KeyStroke cutStroke = KeyStroke.getKeyStroke("control X");
    private static KeyStroke copyStroke = KeyStroke.getKeyStroke("control C");
  }

  private static class MyCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);
      ((JComponent) c).setOpaque(isSelected);
      return c;
    }
  }

  protected void setCellRenderer() {
    if (dsa.gui.lf.Colors.hasCustomColors()) {
      MyCellRenderer renderer = new MyCellRenderer();
      mTable.setDefaultRenderer(Object.class, renderer);
      mTable.setDefaultRenderer(dsa.util.Optional.NULL_INT.getClass(), renderer);
      mTable.setSelectionBackground(dsa.gui.lf.Colors.getSelectedBackground());
      mTable.setSelectionForeground(dsa.gui.lf.Colors.getSelectedForeground());
      mTable.setOpaque(false);
    }
  }

  protected static final Color BACKGROUND_GRAY = new Color(238, 238, 238);

  public void setFirstSelectedRow() {
    setSelectedRow(0);
  }

  protected void setSelectedRow(int row) {
    if (row >= 0 && mTable.getModel().getRowCount() > row) {
      mTable.setRowSelectionInterval(row, row);
    }
  }

  public void restoreSortingState(String title) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.util.TableSorter.class);
    mSorter.restoreState(title, prefs);
  }

  public void saveSortingState(String title) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.util.TableSorter.class);
    mSorter.saveState(title, prefs);
  }

  public JPanel getPanelWithTable() {
    return thePanel;
  }

  public String getSelectedItem() {
    int row = 0;
    if (mTable.getSelectedRow() != -1) {
      row = mTable.getSelectedRow();
    }
    if (mTable.getRowCount() == 0) return null;
    return (String) mSorter.getValueAt(row, getNameColumn());
  }
  
  public boolean containsItem(String item) {
    for (int row = 0; row < mTable.getRowCount(); ++row) {
      if (mSorter.getValueAt(row, getNameColumn()).equals(item)) {
        return true;
      }
    }
    return false;
  }

  protected abstract int getNameColumn();

  private ActionListener dcListener = null;
  
  public void setDoubleClickListener(ActionListener listener) {
    dcListener = listener;
  }

  public void addSelectionListener(ListSelectionListener l) {
    mTable.getSelectionModel().addListSelectionListener(l);
  }

  public void invalidate() {
    mTable.revalidate();
    mTable.repaint();
  }

  public void clear() {
    ((DefaultTableModel) mSorter.getTableModel()).setRowCount(0);
  }

  public void sortingStarting() {
    // no action
  }

  public void sortingFinished() {
    setFirstSelectedRow();
  }

  public void addMouseMotionListener(MouseMotionListener l) {
    mTable.addMouseMotionListener(l);
  }
  
  private MouseListener popupListener = null;
  
  public void setPopupListener(MouseListener l) {
    popupListener = l;
  }
  
  protected final MouseListener createMouseListener() {
    return new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1 && dcListener != null) {
          dcListener.actionPerformed(new ActionEvent(this, 0, ""));
        }
      }
      
      public void mousePressed(MouseEvent e) {
        setSelectedRow(mTable.rowAtPoint(e.getPoint()));
        if (popupListener != null) {
          popupListener.mousePressed(e);
        }
      }
      
      public void mouseReleased(MouseEvent e) {
        if (popupListener != null) {
          popupListener.mouseReleased(e);
        }
      }
    };
  }
  
  public JComponent getDnDComponent() {
    return mTable;
  }
  
  public void setTransferHandler(TransferHandler handler) {
    mTable.setTransferHandler(handler);
    thePanel.setTransferHandler(handler);
  }
  
  public void setKeyListener(KeyListener kl) {
    mTable.addKeyListener(kl);
  }
  
}

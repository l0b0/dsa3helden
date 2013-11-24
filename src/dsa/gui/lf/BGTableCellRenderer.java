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
package dsa.gui.lf;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BGTableCellRenderer extends DefaultTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {

    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    setOpaque(isSelected);
    Object name = table.getValueAt(row, 0);
    if (!isSelected && name instanceof String) {
      dsa.util.Strings.ChangeTag tag = dsa.util.Strings.getChangeTag((String) name);
      if (tag == dsa.util.Strings.ChangeTag.Added) {
        c.setForeground(java.awt.Color.GREEN);
      }
      else if (tag == dsa.util.Strings.ChangeTag.Removed) {
        c.setForeground(java.awt.Color.RED);
      }
      else {
        c.setForeground(java.awt.Color.BLACK);
      }
    }
    return c;
  }
}

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
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.lf;

import java.awt.Component;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;

public class BGList extends JList {

  public BGList(ListModel dataModel) {
    super(dataModel);
    setRenderer();
  }

  public BGList(Object[] listData) {
    super(listData);
    setRenderer();
  }

  public BGList(Vector<?> listData) {
    super(listData);
    setRenderer();
  }

  public BGList() {
    super();
    setRenderer();
  }

  protected void setRenderer() {
    if (Colors.hasCustomColors()) {
      setSelectionForeground(Colors.getSelectedForeground());
      setSelectionBackground(Colors.getSelectedBackground());
      setCellRenderer(new MyRenderer());
      setOpaque(false);
    }
  }

  private static class MyRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      JComponent c = (JComponent) super.getListCellRendererComponent(list,
          value, index, isSelected, cellHasFocus);
      c.setOpaque(isSelected);
      return c;
    }
  }

}

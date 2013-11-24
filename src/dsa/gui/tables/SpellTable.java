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
package dsa.gui.tables;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import dsa.gui.util.TableSorter;
import dsa.model.talents.Spell;

public class SpellTable extends BasicTable {

  @Override
  protected int getNameColumn() {
    return 0;
  }

  int getOriginColumn() {
    return 2;
  }

  int getCategoryColumn() {
    return 1;
  }

  class MyTableModel extends DefaultTableModel {
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  MyTableModel mModel;

  public SpellTable() {
    super();
    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Kategorie");
    mModel.addColumn("Ursprung");
    tcm.addColumn(new TableColumn(0, 160));
    tcm.addColumn(new TableColumn(1, 160));
    tcm.addColumn(new TableColumn(2, 160));
    mSorter = new TableSorter(mModel);
    mTable = new JTable(mSorter, tcm);
    mSorter.setTableHeader(mTable.getTableHeader());

    for (int i = 0; i < mTable.getColumnCount(); ++i)
      tcm.getColumn(i).setHeaderValue(mTable.getColumnName(i));

    mTable.setColumnSelectionAllowed(false);
    mTable.setIntercellSpacing(new java.awt.Dimension(6, 6));
    mTable.setRowSelectionAllowed(true);
    mTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(22);
    mTable.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1 && dcListener != null) {
          dcListener.actionPerformed(new ActionEvent(this, 0, ""));
        }
      }
    });

    // mTable.setBackground(BACKGROUND_GRAY);
    JScrollPane scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);

    thePanel = new JPanel(new BorderLayout());
    thePanel.add(scrollPane, BorderLayout.CENTER);
    setCellRenderer();

    mSorter.setSortingListener(this);
  }

  public void addSpell(Spell spell) {
    Object[] rowData = new Object[3];
    rowData[getNameColumn()] = spell.getName();
    rowData[getCategoryColumn()] = spell.getCategory();
    rowData[getOriginColumn()] = spell.getOrigin();
    mModel.addRow(rowData);
    setSelectedRow(mModel.getRowCount() - 1);
  }
}

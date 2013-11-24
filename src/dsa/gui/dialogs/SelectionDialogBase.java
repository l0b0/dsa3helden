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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.gui.lf.BGDialog;
import dsa.gui.tables.BasicTable;

public abstract class SelectionDialogBase extends BGDialog {

  public interface SelectionDialogCallback {
    void ItemSelected(String item);
  }

  public SelectionDialogBase(JFrame owner, String title, BasicTable table,
      String tableSortingID) {
    super(owner, true);
    mTable = table;
    setTitle(title);
    JPanel panel = table.getPanelWithTable();
    this.setContentPane(panel);

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(200, 40));
    lowerPanel.add(getAddButton());
    lowerPanel.add(getCloseButton(tableSortingID));

    addSubclassSpecificButtons(lowerPanel);

    panel.add(lowerPanel, java.awt.BorderLayout.SOUTH);

    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(null);
    upperPanel.setPreferredSize(new java.awt.Dimension(310, 29));
    JLabel label1 = new JLabel("Filter:");
    label1.setBounds(5, 5, 50, 19);
    upperPanel.add(label1);
    upperPanel.add(getFilterField());
    panel.add(upperPanel, java.awt.BorderLayout.NORTH);

    table.restoreSortingState(tableSortingID);
    table.setDoubleClickListener(myListener);
    pack();
  }

  protected void addSubclassSpecificButtons(JPanel lowerPanel) {
  }

  public void setCallback(SelectionDialogCallback callback) {
    this.callback = callback;
  }

  JButton addButton;

  JButton closeButton;

  private SelectionDialogCallback callback = null;

  protected BasicTable mTable;

  ActionListener myListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (callback != null) callback.ItemSelected(mTable.getSelectedItem());
    }
  };

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton("Hinzufügen");
      addButton.setBounds(5, 5, 140, 25);
      addButton.addActionListener(myListener);
    }
    return addButton;
  }

  class Closer implements ActionListener {
    public Closer(String sortingID) {
      mID = sortingID;
    }

    public void actionPerformed(ActionEvent e) {
      mTable.saveSortingState(mID);
      SelectionDialogBase.this.dispose();
    }

    private String mID;
  }

  JButton getCloseButton(String sortingID) {
    if (closeButton == null) {
      closeButton = new JButton("Schließen");
      closeButton.setBounds(160, 5, 140, 25);
      closeButton.addActionListener(new Closer(sortingID));
    }
    return closeButton;
  }

  private JTextField filterField;

  JTextField getFilterField() {
    if (filterField == null) {
      filterField = new JTextField();
      filterField.setBounds(55, 5, 250, 19);
      filterField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          filter();
        }

        public void removeUpdate(DocumentEvent e) {
          filter();
        }

        public void changedUpdate(DocumentEvent e) {
          filter();
        }

      });
    }
    return filterField;
  }

  private Pattern filter = Pattern.compile(".*");

  protected final boolean isDisplayed(String item) {
    return filter.matcher(item.toLowerCase()).matches();
  }

  private void filter() {
    String s = getFilterField().getText();
    if (s == null) s = "";
    if (s.equals("")) s = "*";
    if (!s.endsWith("*")) s = s + "*";
    s = s.replaceAll("\\*", ".*");
    s = s.toLowerCase();
    try {
      filter = Pattern.compile(s);
    }
    catch (java.util.regex.PatternSyntaxException e) {
      filter = Pattern.compile(".*");
    }
    mTable.clear();
    fillTable();
  }

  protected abstract void fillTable();

}

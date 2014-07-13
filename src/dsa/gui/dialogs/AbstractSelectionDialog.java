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
package dsa.gui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.gui.dialogs.ItemProviders.ItemProvider;
import dsa.gui.lf.BGDialog;
import dsa.gui.tables.AbstractTable;

public abstract class AbstractSelectionDialog extends BGDialog {

  public interface SelectionDialogCallback {
    void itemSelected(String item);
    void itemChanged(String item);
  }

  public AbstractSelectionDialog(JFrame owner, String title, ItemProvider provider,
      String tableSortingID) {
    this(owner, title, provider, tableSortingID, false);
  }
  
  public AbstractSelectionDialog(JFrame owner, String title, ItemProvider provider,
      String tableSortingID, boolean singleSelection) {
    super(owner, true);
    mTable = provider.getTable();
    mProvider = provider;
    setTitle(title);
    mSortingID = tableSortingID;
    mSingleSelection = singleSelection;
  }
  
  public AbstractSelectionDialog(JDialog owner, String title, ItemProvider provider,
      String tableSortingID, boolean singleSelection) {
    super(owner, true);
    mTable = provider.getTable();
    mProvider = provider;
    setTitle(title);
    mSortingID = tableSortingID;
    mSingleSelection = singleSelection;
  }
  
  public String getHelpPage() {
    return "Gegenstandsauswahl";
  }
  
  private final String mSortingID;
  protected final boolean mSingleSelection;
  
  protected boolean showSingularBox() { return false; }
  
  protected final boolean showSingularItems() {
    return getSingularBox().isSelected();
  }
  
  protected final void initialize() {
    JPanel panel = mTable.getPanelWithTable();
    this.setContentPane(panel);

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(200, 40));
    lowerPanel.add(getAddButton());
    lowerPanel.add(getCloseButton(mSortingID));

    addSubclassSpecificButtons(lowerPanel);

    lowerPanel.setPreferredSize(new Dimension(510, 35));
    panel.add(lowerPanel, java.awt.BorderLayout.SOUTH);

    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(null);
    upperPanel.setPreferredSize(new java.awt.Dimension(310, 29));
    JLabel label1 = new JLabel("Filter:");
    label1.setBounds(5, 5, 50, 19);
    upperPanel.add(label1);
    upperPanel.add(getFilterField());
    if (showSingularBox()) {
      upperPanel.add(getSingularBox());
    }
    panel.add(upperPanel, java.awt.BorderLayout.NORTH);

    mTable.restoreSortingState(mSortingID);
    mTable.setDoubleClickListener(myListener);
    pack();
    this.getRootPane().setDefaultButton(getAddButton());
    setEscapeButton(closeButton);
  }

  protected void addSubclassSpecificButtons(JPanel lowerPanel) {
  }

  public void setCallback(SelectionDialogCallback callback) {
    this.callback = callback;
  }
  
  JButton addButton;
  
  JButton closeButton;

  private SelectionDialogCallback callback = null;
  
  protected final SelectionDialogCallback getCallback() {
    return callback;
  }

  protected AbstractTable mTable;
  protected ItemProvider mProvider;

  ActionListener myListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      if (callback != null) callback.itemSelected(mTable.getSelectedItem());
      if (mSingleSelection) {
        mTable.saveSortingState(mSortingID);
        AbstractSelectionDialog.this.dispose();
      }
    }
  };

  protected final JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(mSingleSelection ? "OK" : "Hinzufügen");
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
      AbstractSelectionDialog.this.dispose();
    }

    private final String mID;
  }

  protected final JButton getCloseButton(String sortingID) {
    if (closeButton == null) {
      closeButton = new JButton(mSingleSelection ? "Abbrechen" : "Schließen");
      closeButton.setBounds(160, 5, 140, 25);
      closeButton.addActionListener(new Closer(sortingID));
    }
    return closeButton;
  }
  
  private JTextField filterField;

  protected final JTextField getFilterField() {
    if (filterField == null) {
      filterField = new JTextField();
      filterField.setBounds(55, 5, 195, 20);
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
  
  protected final JCheckBox getSingularBox() {
    if (singularBox == null) {
      singularBox = new JCheckBox("Einzelstücke");
      singularBox.setBounds(260, 5, 120, 20);
      singularBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mTable.clear();
          fillTable();
        }
      });
    }
    return singularBox;
  }
  
  private JCheckBox singularBox;

  private void filter() {
    String s = getFilterField().getText();
    mProvider.setFilter(s);
    mTable.clear();
    fillTable();
  }
  
  protected boolean listen = true;

  protected final void fillTable() {
    listen = false;
    mProvider.fillTable(showSingularItems());
    listen = true;
    updateDeleteButton();    
  }
  
  protected abstract void updateDeleteButton();
}

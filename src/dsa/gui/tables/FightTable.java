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
package dsa.gui.tables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import dsa.control.Fighting;
import dsa.gui.lf.BGTableCellRenderer;
import dsa.gui.util.ImageManager;
import dsa.gui.util.table.CellRenderers;
import dsa.gui.util.table.SpinnerCellEditor;
import dsa.gui.util.table.ComboBoxCellEditor;
import dsa.gui.util.table.TableButtonInput;
import dsa.gui.util.table.TableCheckboxInput;
import dsa.gui.util.table.TableSorter;
import dsa.model.DiceSpecification;
import dsa.model.Fighter;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.remote.RemoteManager;
import dsa.util.Optional;

public final class FightTable 
    implements SpinnerCellEditor.EditorClient, 
                 ComboBoxCellEditor.EditorClient,
                 TableButtonInput.Callbacks,
                 TableCheckboxInput.Callbacks {

  private JTable mTable;
  private JPanel mPanel;
  
  private HashMap<String, Integer> cellInfoMap = new HashMap<String, Integer>();
  
  private ArrayList<Fighter> fighters = new ArrayList<Fighter>();
  private ArrayList<Fighter> opponents;
  
  private HashMap<String, Fighter> targets = new HashMap<String, Fighter>();
  
  private int mSortingColumn = -1;
  private int mSortingDirection = 0;

  private static final class ViewportFillingTable extends JTable {
    public ViewportFillingTable(TableModel tm, TableColumnModel tcm) {
      super(tm, tcm);
    }
    
    public boolean getScrollableTracksViewportHeight() {
      return getPreferredSize().height < getParent().getHeight();
    }
  }  
  
  public interface Client {
    void doAttack(Fighter fighter, int weaponIndex, Fighter opponent);
    Component getMessageBoxParent();
  }
  
  private Client mClient;
  
  private int mButtonClickedRow = 0;
  
  private int getNameColumn() { return 0; }
  private int getWeaponColumn() { return 1; }
  private int getLEColumn() { return 2; }
  private int getATColumn() { return 3; }
  private int getPAColumn() { return 4; }
  private int getTPColumn() { return 5; }
  private int getRSColumn() { return 6; }
  private int getMRColumn() { return 7; }
  private int getGroundedColumn() { return 8; }
  private int getDazedColumn() { return 9; }
  private int getOpponentColumn() { return 10; }
  private int getAttackColumn() { return 11; }
  private int getHealColumn() { return 12; }
  
  private int getNrOfColumns() { return 13; }
  
  private class MyTableModel extends DefaultTableModel {
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == getATColumn() ||
          columnIndex == getPAColumn()) {
        return Integer.class;
      }
      else if (columnIndex == getTPColumn()) {
        return DiceSpecification.class;
      }
      else if (columnIndex == getRSColumn() ||
                columnIndex == getMRColumn()) {
        return Optional.NULL_INT.getClass();
      }
      else if (columnIndex == getGroundedColumn() ||
                columnIndex == getDazedColumn()) {
        return Boolean.class;
      }
      return super.getColumnClass(columnIndex);
    }

    public boolean isCellEditable(int row, int column) {
      if (column == getLEColumn()) {
        return (row == 0) || (fighters.get(row - 1) != fighters.get(row));
      }
      else if (column == getOpponentColumn() || column == getWeaponColumn()) {
        return true;
      }
      else return false;
    }
  }
  
  private MyTableModel mModel;
  
  public JPanel getPanelWithTable() {
    return mPanel;
  }

  public void invalidate() {
    mTable.revalidate();
    mTable.repaint();
  }
  
  private static class MyColourSelector implements CellRenderers.ColourSelector {
    public boolean shallBeGray(int row, int column) {
      return false;
    }

    public boolean shallBeOpaque(int column) {
      return false;
    }
    
    public Color getForeground(int row, int column) {
      return Color.BLACK;
    }
    
    public Color getBackground(int row, int column) {
      return null;
    }
  }

  public FightTable(Client client) {
    mClient = client;
    
    mModel = new MyTableModel();
    mModel.addColumn("Name");
    mModel.addColumn("Waffe");
    mModel.addColumn("LE");
    mModel.addColumn("AT");
    mModel.addColumn("PA");
    mModel.addColumn("TP");
    mModel.addColumn("RS");
    mModel.addColumn("MR");
    mModel.addColumn("Boden");
    mModel.addColumn("Ben.");
    mModel.addColumn("Gegner");
    mModel.addColumn("");
    mModel.addColumn("");
    
    SpinnerCellEditor leEditor = new SpinnerCellEditor(this);
    leEditor.setModel(new SpinnerNumberModel(10, 1, 40, 1));
    leEditor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        SpinnerCellEditor editor = (SpinnerCellEditor) e
            .getSource();
        Number number = (Number) editor.getValue();
        fighters.get(cellInfoMap.get(editor.getCellInfo())).setCurrentLE(number.intValue());
      }
      
      public void editingCanceled(ChangeEvent e) {
      }
    });
    
    ComboBoxCellEditor targetEditor = new ComboBoxCellEditor(this);
    ComboBoxCellEditor weaponEditor = new ComboBoxCellEditor(this);

    DefaultTableCellRenderer greyingRenderer = CellRenderers.createGreyingCellRenderer(
        new MyColourSelector());
    greyingRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    TableCellEditor dummyEditor = TableButtonInput.createDummyCellEditor();
    TableCellRenderer bgRenderer = new BGTableCellRenderer();
    TableCellRenderer middleShorteningRenderer = CellRenderers.createMiddleShorteningCellRenderer();
    JButton attackButton = TableButtonInput.createButton(ImageManager.getIcon("attack"));
    attackButton.setToolTipText("Attacke");
    attackButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Fighter attacker = fighters.get(mButtonClickedRow);
        int weaponIndex = getWeaponIndex(mButtonClickedRow);
        String targetName = attacker.getTarget(weaponIndex);
        Fighter target = targets.get(targetName);
        if (target == null) {
          javax.swing.JOptionPane.showMessageDialog(null, "Gegner " + targetName 
              + " nicht gefunden!", "Fehler", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        else {
          mClient.doAttack(attacker, weaponIndex, target);
        }
      }
    });
    TableCellRenderer atRenderer = TableButtonInput.createButtonCellRenderer(attackButton,
        bgRenderer);
    JButton healButton = TableButtonInput.createButton(ImageManager.getIcon("heal"));
    healButton.setToolTipText("Heilen");
    healButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Fighter fighter = fighters.get(mButtonClickedRow);
        fighter.setCurrentLE(fighter.getMaxLE());
        leChanged(fighter);
      }
    });
    TableCellRenderer healRenderer = TableButtonInput.createButtonCellRenderer(healButton, 
        bgRenderer);
    
    TableCellRenderer groundedRenderer = TableCheckboxInput.createCheckBoxCellRenderer(bgRenderer, true);
    boolean withDazed = Group.getInstance().getOptions().hasQvatStunned();
    TableCellRenderer dazedRenderer = TableCheckboxInput.createCheckBoxCellRenderer(bgRenderer, withDazed);
    
    TableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new TableColumn(getNameColumn(), 100, middleShorteningRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getWeaponColumn(), 100, bgRenderer, weaponEditor));
    tcm.addColumn(new TableColumn(getLEColumn(), 45, bgRenderer, leEditor));
    tcm.addColumn(new TableColumn(getATColumn(), 40, bgRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getPAColumn(), 40, bgRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getTPColumn(), 60, bgRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getRSColumn(), 40, bgRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getMRColumn(), 40, bgRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getGroundedColumn(), 25, groundedRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getDazedColumn(), 25, dazedRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getOpponentColumn(), 100, middleShorteningRenderer, targetEditor));
    tcm.addColumn(new TableColumn(getAttackColumn(), 30, atRenderer, dummyEditor));
    tcm.addColumn(new TableColumn(getHealColumn(), 30, healRenderer, dummyEditor));
    
    mTable = new ViewportFillingTable(mModel, tcm);
    for (int i = 0; i < mTable.getColumnCount(); ++i)
      tcm.getColumn(i).setHeaderValue(mTable.getColumnName(i));

    mTable.setColumnSelectionAllowed(false);
    mTable.setIntercellSpacing(new java.awt.Dimension(6, 6));
    mTable.setRowSelectionAllowed(false);
    mTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(22);
    TableButtonInput.attachToTable(this, mTable);
    TableCheckboxInput.attachToTable(this, mTable);
    
    TableCellRenderer headerRenderer = CellRenderers.createImageCellRenderer();
    tcm.getColumn(getGroundedColumn()).setHeaderValue(new CellRenderers.ImageAndText(
        "Am Boden", ImageManager.getIcon("grounded")));
    tcm.getColumn(getDazedColumn()).setHeaderValue(new CellRenderers.ImageAndText(
        "Benommen", ImageManager.getIcon("dazed")));
    tcm.getColumn(getGroundedColumn()).setHeaderRenderer(
        headerRenderer);
    tcm.getColumn(getDazedColumn()).setHeaderRenderer(
        headerRenderer);
    
    mTable.getTableHeader().addMouseListener(new SortingMouseListener());
    mTable.getTableHeader().setDefaultRenderer(new SortableHeaderRenderer(mTable.getTableHeader().getDefaultRenderer()));
    
    JScrollPane scrollPane = new JScrollPane(mTable);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    setCellRenderer();
    mPanel = new JPanel(new BorderLayout());
    mPanel.add(scrollPane, BorderLayout.CENTER);
  }
  
  private class SortingMouseListener extends MouseAdapter {
      public void mouseClicked(MouseEvent e) {
          JTableHeader h = (JTableHeader) e.getSource();
          TableColumnModel columnModel = h.getColumnModel();
          int viewColumn = columnModel.getColumnIndexAtX(e.getX());
          int column = columnModel.getColumn(viewColumn).getModelIndex();
          if (column == getNameColumn() || column == getLEColumn() || column == getGroundedColumn() || column == getDazedColumn()) {
        	  if (column == mSortingColumn) {
        		  mSortingDirection++;
        		  if (mSortingDirection == 2)
        			  mSortingDirection = -1;
        		  if (mSortingDirection == 0) 
        			  mSortingColumn = -1;
        	  }
        	  else {
        		  mSortingColumn = column;
        		  mSortingDirection = 1;
        	  }
        	  resortFighters();
          }
      }	  
  }
  
  private Icon getHeaderRendererIcon(int column, int size) {
	  if (column != mSortingColumn) {
		  return null;
	  }
      if (mSortingDirection == 0) {
          return null;
      }
      return new TableSorter.Arrow(mSortingDirection == -1, size, 0);
  }

  private class SortableHeaderRenderer implements TableCellRenderer {
      private TableCellRenderer tableCellRenderer;

      public SortableHeaderRenderer(TableCellRenderer tableCellRenderer) {
          this.tableCellRenderer = tableCellRenderer;
      }

      public Component getTableCellRendererComponent(JTable table, 
                                                     Object value,
                                                     boolean isSelected, 
                                                     boolean hasFocus,
                                                     int row, 
                                                     int column) {
          Component c = tableCellRenderer.getTableCellRendererComponent(table, 
                  value, isSelected, hasFocus, row, column);
          if (c instanceof JLabel) {
              JLabel l = (JLabel) c;
              l.setHorizontalTextPosition(JLabel.LEFT);
              int modelColumn = table.convertColumnIndexToModel(column);
              l.setIcon(getHeaderRendererIcon(modelColumn, l.getFont().getSize()));
          }
          return c;
      }
  }
  
  private boolean isSorting() {
	  return mSortingColumn != -1;
  }
  
  public void restoreSortingState(String title) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.util.table.TableSorter.class);
    int oldColumn = mSortingColumn;
    int oldDirection = mSortingDirection;
    mSortingColumn = prefs.getInt(title + "_SortingColumn", -1);
    mSortingDirection = prefs.getInt(title + "_SortingDirection", -1);
    if (mSortingColumn != oldColumn || mSortingDirection != oldDirection) {
    	sortingStateChanged();
    }
  }

  public void saveSortingState(String title) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.util.table.TableSorter.class);
    prefs.putInt(title + "_SortingColumn", mSortingColumn);
    prefs.putInt(title + "_SortingDirection", mSortingDirection);
  }
  
  private void sortingStateChanged() {
	  resortFighters();
  }

  public void dazedOptionChanged() {
    mTable.getColumnModel().getColumn(getDazedColumn()).setCellRenderer(
      TableCheckboxInput.createCheckBoxCellRenderer(mTable.getDefaultRenderer(Optional.NULL_INT.getClass()), 
            Group.getInstance().getOptions().hasQvatStunned()));
  }
  
  private void setCellRenderer() {
    if (dsa.gui.lf.Colors.hasCustomColors()) {
      BGTableCellRenderer renderer = new BGTableCellRenderer();
      mTable.setDefaultRenderer(Object.class, renderer);
      mTable.setDefaultRenderer(Optional.NULL_INT.getClass(), renderer);
      mTable.setSelectionBackground(dsa.gui.lf.Colors.getSelectedBackground());
      mTable.setSelectionForeground(dsa.gui.lf.Colors.getSelectedForeground());
      mTable.setOpaque(false);
    }
  }

  public String getCellInfo(int row) {
    String info = "" + row;
    cellInfoMap.put(info, row);
    return info;
  }
  
  public int getCellMaximum(int row, int column) {
	  if (column == getLEColumn()) {
		  return fighters.get(row).getMaxLE();
	  }
	  else {
		  return -1;
	  }
  }
  
  public void setClickedRow(int row) {
    mButtonClickedRow = row;
  }
  
  public void checkBoxValueChanged(int row, int column, boolean newValue)
  {
    if (column == getGroundedColumn()) {
      Fighter fighter = fighters.get(row);	
      fighter.setGrounded(newValue);
      if (RemoteManager.getInstance().isConnected() && (fighter instanceof Hero))
      	RemoteManager.getInstance().informOfFightPropertyChange((Hero) fighter, dsa.remote.IServer.FightProperty.grounded, true);
    }
    else if (column == getDazedColumn()) {
      Fighter fighter = fighters.get(row);	
      fighter.setDazed(newValue);
      if (RemoteManager.getInstance().isConnected() && (fighter instanceof Hero))
      	RemoteManager.getInstance().informOfFightPropertyChange((Hero) fighter, dsa.remote.IServer.FightProperty.dazed, true);
    }
    if (mSortingColumn == column) {
    	resortFighters();
    }
  }
  
  public void addFighter(Fighter fighter) {
    addFighterBeforeSorting(fighter);
    if (isSorting()) {
    	resortFighters();
    }
    else {
    	addFighter(fighter, fighters.size(), true);
    }
  }
  
  private ArrayList<Fighter> mFightersBeforeSorting = new ArrayList<Fighter>();
  private ArrayList<Fighter> mFightersAfterSorting = new ArrayList<Fighter>();
  
  private void addFighterBeforeSorting(Fighter fighter) {
	  mFightersBeforeSorting.add(fighter);
	  mFightersAfterSorting.add(fighter);
  }
  
  private void resortFighters() {
	  if (isSorting()) {
		  Collections.sort(mFightersAfterSorting, new Comparator<Fighter>() {
			public int compare(Fighter f1, Fighter f2) {
				int value = 0;
				if (mSortingColumn == getNameColumn()) {
					value = f1.getName().compareTo(f2.getName());
				}
				else if (mSortingColumn == getLEColumn()) {
					value = f1.getCurrentLE() - f2.getCurrentLE();
				}
				else if (mSortingColumn == getGroundedColumn()) {
					value = f1.isGrounded() ? (f2.isGrounded() ? 0 : -1) : 1; 
				}
				else if (mSortingColumn == getDazedColumn()) {
					value = f1.isDazed() ? (f2.isDazed() ? 0 : -1) : 1;
				}
				return mSortingDirection == -1 ? -value : (mSortingDirection == 1 ? value : 0);
			}
		  });
	  }
	  else {
		  mFightersAfterSorting.clear();
		  mFightersAfterSorting.addAll(mFightersBeforeSorting);
	  }
	  mModel.setRowCount(0);
	  fighters.clear();
	  for (int i = 0; i < mFightersAfterSorting.size(); ++i) {
		  addFighter(mFightersAfterSorting.get(i), fighters.size(), false);
	  }
	  mModel.fireTableDataChanged();
  }
  
  private void retrieveTarget(Object[] rowData, Fighter fighter, int index) {
    if (index >= fighter.getFightingWeapons().size()) {
      rowData[getOpponentColumn()] = "";
      rowData[getAttackColumn()] = Boolean.FALSE;
      return;
    }
    String target = fighter.getTarget(index);
    rowData[getOpponentColumn()] = target;
    rowData[getAttackColumn()] = Boolean.valueOf(!target.equals("")) && 
      Fighting.canAttack(fighter);
  }
  
  private void addFighter(Fighter fighter, int position, boolean fireChangeEvent) {
    Object[] rowData = new Object[getNrOfColumns()];
    rowData[getNameColumn()] = (fighter instanceof Hero) ? dsa.util.Strings.cutTo(fighter.getName(), ' ') : fighter.getName();
    List<String> weapons = fighter.getFightingWeapons();
    rowData[getWeaponColumn()] = weapons.size() > 0 ? weapons.get(0) : "";
    rowData[getLEColumn()] = fighter.getCurrentLE();
    rowData[getATColumn()] = fighter.getAT(0);
    rowData[getPAColumn()] = fighter.getPA(0);
    rowData[getTPColumn()] = fighter.getTP(0);
    rowData[getRSColumn()] = new Optional<Integer>(fighter.getRS());
    rowData[getMRColumn()] = new Optional<Integer>(fighter.getMR());
    rowData[getGroundedColumn()] = Boolean.valueOf(fighter.isGrounded());
    rowData[getDazedColumn()] = Boolean.valueOf(fighter.isDazed());
    rowData[getOpponentColumn()] = "";
    retrieveTarget(rowData, fighter, 0);
    rowData[getHealColumn()] = Boolean.TRUE;
    mModel.insertRow(position, rowData);
    fighters.add(position, fighter);
    for (int i = 1; i < fighter.getNrOfAttacks(); ++i) {
      Object[] rowData2 = new Object[getNrOfColumns()];
      rowData2[getNameColumn()] = "";
      rowData2[getWeaponColumn()] = weapons.get(i); 
      rowData2[getLEColumn()] = "";
      rowData2[getATColumn()] = fighter.getAT(i);
      rowData2[getPAColumn()] = fighter.getPA(i);
      rowData2[getTPColumn()] = fighter.getTP(i);
      rowData2[getRSColumn()] = Optional.NULL_INT;
      rowData2[getMRColumn()] = Optional.NULL_INT;
      rowData2[getGroundedColumn()] = null;
      rowData2[getDazedColumn()] = null;
      rowData2[getOpponentColumn()] = "";
      retrieveTarget(rowData2, fighter, i);
      rowData2[getHealColumn()] = Boolean.FALSE;
      mModel.insertRow(position + i, rowData2);
      fighters.add(position + i, fighter);
    }
    if (fireChangeEvent)
    	mModel.fireTableDataChanged();
  }
  
  public void setOpponents(ArrayList<Fighter> opponents) {
    this.opponents = opponents;
    targets.clear();
    for (int i = 0; i < opponents.size(); ++i) {
      targets.put(opponents.get(i).getName(), opponents.get(i));
    }
    int weaponIndex = 0; Fighter oldFighter = null;
    int row = 0; 
    for (int f = 0; f < fighters.size(); ++f) {
      if (fighters.get(f) != oldFighter) {
        weaponIndex = 0;
        oldFighter = fighters.get(f);
      }
      else {
        weaponIndex++;
      }
      String target = fighters.get(f).getTarget(weaponIndex);
      if (!targets.containsKey(target)) {
        mModel.setValueAt("", row, getOpponentColumn());
        mModel.setValueAt(Boolean.FALSE, row, getAttackColumn());
      }
      ++row;
    }
  }
  
  public void updateFighter(Fighter fighter) {
    int index = 0;
    while (index < fighters.size()) {
      if (fighters.get(index) == fighter) break;
      ++index;
    }
    if (index == fighters.size()) return;
    if (!isSorting()) {
	    while (fighters.size() > index && fighters.get(index) == fighter) {
	      mModel.removeRow(index);
	      fighters.remove(index);
	    }
	    addFighter(fighter, index, true);
    }
    else {
    	resortFighters();
    }
  }
  
  public void leChanged(Fighter fighter) {
    if (mSortingColumn == getLEColumn()) {
    	resortFighters();
    }
    else {
	    int index = 0;
	    while (index < fighters.size()) {
	      if (fighters.get(index) == fighter) break;
	      ++index;
	    }
	    if (index == fighters.size()) return;
	    mModel.setValueAt(fighter.getCurrentLE(), index, getLEColumn());
    }
  }
  
  public void clear() {
    fighters.clear();
    mFightersBeforeSorting.clear();
    mFightersAfterSorting.clear();
    mModel.setRowCount(0);
  }
  
  public List<String> getItems(int row, int column) {
    if (column == getOpponentColumn()) {
      ArrayList<String> items = new ArrayList<String>();
      items.add("<keiner>");
      for (int i = 0; i < opponents.size(); ++i) {
        items.add(opponents.get(i).getName());
      }
      return items;
    }
    else if (column == getWeaponColumn()) {
      int weaponIndex = getWeaponIndex(row);
      return fighters.get(row).getPossibleWeapons(weaponIndex);
    }
    else {
      return new ArrayList<String>();
    }
  }
  
  private int getWeaponIndex(int row) {
    int index = 0;
    Fighter f = fighters.get(row);
    while (row > 0 && fighters.get(row - 1) == f) {
      --row;
      ++index;
    }
    return index;
  }

  public void itemSelected(int row, int column, int index) {
    int weaponIndex = getWeaponIndex(row);
    if (column == getOpponentColumn()) {
      if (index < 0 || index >= opponents.size()) return;
      if (index == 0) {
        fighters.get(row).setTarget(weaponIndex, "");
        mModel.setValueAt("", row, getOpponentColumn());
        mModel.setValueAt(Boolean.FALSE, row, getAttackColumn());
      }
      else {
        fighters.get(row).setTarget(weaponIndex, opponents.get(index - 1).getName());
        mModel.setValueAt(Boolean.valueOf(weaponIndex <= fighters.get(row).getFightingWeapons().size()), 
          row, getAttackColumn());
      }
    }
    else if (column == getWeaponColumn()) {
      List<String> items = fighters.get(row).getPossibleWeapons(weaponIndex);
      if (index < 0 || index >= items.size()) return;
      String item = items.get(index);
      String oldItem = fighters.get(row).getFightingWeapons().get(weaponIndex);
      fighters.get(row).setUsedWeapon(weaponIndex, item);
      mModel.setValueAt(fighters.get(row).getAT(weaponIndex), row, getATColumn());
      mModel.setValueAt(fighters.get(row).getPA(weaponIndex), row, getPAColumn());
      mModel.setValueAt(fighters.get(row).getTP(weaponIndex), row, getTPColumn());
      if (fighters.get(row) instanceof Hero && RemoteManager.getInstance().isConnectedAsGM()) {
    	  RemoteManager.getInstance().informPlayerOfWeaponChange((Hero)fighters.get(row));
      }
      boolean change = oldItem == null || !oldItem.equals(item);
      if (change && (Fighting.Flammenschwert1.equals(item) || Fighting.Flammenschwert2.equals(item))) {
      	if (!Fighting.Flammenschwert1.equals(oldItem) && !Fighting.Flammenschwert2.equals(oldItem) && fighters.get(row) instanceof Hero) {
      		Hero hero = (Hero)fighters.get(row);
  	    	if (JOptionPane.showConfirmDialog(mClient.getMessageBoxParent(), "ASP für Umwandlung abziehen?", "Heldenverwaltung", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
  	    		if (hero.getCurrentEnergy(Energy.AE) >= 5) {
  	    			hero.changeCurrentEnergy(Energy.AE, -5);
  	    		}
  	    		else {
  	    			JOptionPane.showMessageDialog(mClient.getMessageBoxParent(), "Nicht genügend ASP vorhanden!", "Heldenverwaltung", JOptionPane.INFORMATION_MESSAGE);
  	    		}
  	    	}
      	}
      }
    }
  }
  
}

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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.NumberFormatter;

import dsa.control.Dice;
import dsa.gui.dialogs.ProbeDialog;
import dsa.gui.dialogs.SpellProbeDialog;
import dsa.gui.lf.Colors;
import dsa.gui.util.ImageManager;
import dsa.gui.util.table.FormattedTextFieldCellEditor;
import dsa.gui.util.table.TableButtonInput;
import dsa.gui.util.table.TableSorter;
import dsa.gui.util.table.CellRenderers;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.CharacterObserver;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.SpellStartValues;
import dsa.model.data.Talents;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Talent;
import dsa.util.Optional;

/**
 * 
 */
class TalentFrame extends SubFrame implements CharactersObserver, 
  FormattedTextFieldCellEditor.EditorClient, CellRenderers.ColourSelector {

  private javax.swing.JPanel jContentPane = null;

  protected boolean enableTests;

  /**
   * This is the default constructor
   */
  protected TalentFrame(String title, boolean enableTests) {
    super(title);
    setTitle(title);
    this.enableTests = enableTests;
    talents = new ArrayList<Talent>();
    currentHero = Group.getInstance().getActiveHero();
    if (currentHero != null) currentHero.addHeroObserver(myCharacterObserver);
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        saveSortingState();
        saveSubclassState();
        Group.getInstance().removeObserver(TalentFrame.this);
        if (currentHero != null)
          currentHero.removeHeroObserver(myCharacterObserver);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          saveSortingState();
          saveSubclassState();
          Group.getInstance().removeObserver(TalentFrame.this);
          if (currentHero != null)
            currentHero.removeHeroObserver(myCharacterObserver);
          done = true;
        }
      }
    });
    // pack();
  }
  
  public String getHelpPage() {
    return "Talente"; //$NON-NLS-1$
  }

  protected void saveSubclassState() {
  }

  private boolean disableChange = false;
  
  public boolean shallBeGray(int row, int column) {
    if (isLockedColumn(column)
        && !(((Boolean) mSorter.getValueAt(row, getLockColumn()))
            .booleanValue()) && !Group.getInstance().getGlobalUnlock()) {
      return true;
    }
    else if (column == getCurrentValueColumn()) {
      String talentName = (String) mSorter
          .getValueAt(row, getNameDummyColumn());
      return (currentHero == null) || !currentHero.hasTalent(talentName);
    }
    else
      return false;
  }
  
  public Color getForeground(int row, int column) {
    if (currentHero == null || !currentHero.isDifference()) return Color.BLACK;
    if (column != getCurrentValueColumn()) return Color.BLACK;
    String talentName = (String) mSorter.getValueAt(row, getNameDummyColumn());
    if (!currentHero.hasTalent(talentName)) return Color.BLACK;
    int value = currentHero.getCurrentTalentValue(talentName);
    return (value == 0) ? Color.BLACK : (value > 0 ? Color.GREEN : Color.RED);
  }
  
  private HashMap<String, Color> backgrounds = new HashMap<String, Color>();
  
  public Color getBackground(int row, int column) {
	  String name = (String) mSorter.getValueAt(row, getNameDummyColumn());
	  return backgrounds.containsKey(name) ? backgrounds.get(name) : null; 
  }
  
  public boolean shallBeOpaque(int column) {
    return isLockedColumn(column) || (column == getCurrentValueColumn());
  }

  private static final Color BACKGROUND_GRAY = new Color(238, 238, 238);
  
  public String getCellInfo(int row) {
    return (String) mSorter.getValueAt(row, getNameDummyColumn());    
  }

  protected boolean isLockedColumn(int column) {
    return (column == getDefaultValueColumn());
  }

  protected static final Optional<Integer> NULL_INT = new Optional<Integer>();

  protected Class<?> getColumnClass(int column, Class<?> defaultValue) {
    if (column == getDefaultValueColumn())
      return NULL_INT.getClass();
    else if (column == getCurrentValueColumn())
      return NULL_INT.getClass();
    else
      return defaultValue;
  }

  private class TalentTableModel extends DefaultTableModel {
    public TalentTableModel(int rows, int columns) {
      super(rows, columns);
    }

    public boolean isCellEditable(int row, int column) {
      if (!isColumnEditable(column)) return false;
      if (currentHero != null && currentHero.isDifference()) return false;
      if (isLockedColumn(column)) {
        return Group.getInstance().getGlobalUnlock()
            || ((Boolean) this.getValueAt(row, getLockColumn())).booleanValue();
      }
      String talent = (String) getValueAt(row, getNameDummyColumn());
      return ((currentHero != null) && (currentHero.hasTalent(talent)));
    }

    public Class<?> getColumnClass(int columnIndex) {
      return TalentFrame.this.getColumnClass(columnIndex, super
          .getColumnClass(columnIndex));
    }

  }

  protected boolean isColumnEditable(int column) {
    if (column == 0) return false;
    if (column == getTestColumn()) return false;
    if (column == getIncrColumn()) return false;
    if (column == getLockColumn()) return false;
    return true;
  }

  protected int mButtonClickedRow;

  private TalentTableModel mModel;

  protected JTable mTable;

  protected int getDefaultValueColumn() {
    return 1;
  }

  protected int getCurrentValueColumn() {
    return 2;
  }

  protected int getLockColumn() {
    return 3;
  }

  protected int getTestColumn() {
    return 4;
  }

  protected int getIncrColumn() {
    return enableTests ? 5 : 4;
  }

  protected int getNameDummyColumn() {
    return enableTests ? 6 : 5;
  }

  protected int getNrOfColumns() {
    return enableTests ? 7 : 6;
  }

  protected void updateStaticSubclassSpecificData() {
  }

  protected boolean canIncreaseUnknownTalents() {
    return false;
  }

  protected final void reupdateData() {
    updateData();
  }
  
  protected final void recreateUI() {
    createUI();
  }

  /**
   * 
   * 
   */
  private void updateData() {
    disableChange = true;
    int displayIndex = 0;
    updateStaticSubclassSpecificData();
    for (int i = 0; i < talents.size(); ++i) {
      if (!shallDisplay(talents.get(i))) continue;
      String talentName = talents.get(i).getName();
      if (currentHero != null && currentHero.hasTalent(talentName)) {
        mModel.setValueAt(new Optional<Integer>(Integer.valueOf(currentHero
            .getDefaultTalentValue(talentName))), displayIndex,
            getDefaultValueColumn());
        mModel.setValueAt(new Optional<Integer>(Integer.valueOf(currentHero
            .getCurrentTalentValue(talentName))), displayIndex,
            getCurrentValueColumn());
        boolean locked = ((Boolean) mModel.getValueAt(displayIndex,
            getLockColumn())).booleanValue();
        if (enableTests) {
          mModel.setValueAt(talents.get(i).canBeTested() && !currentHero.isDifference() ? Boolean.TRUE
              : Boolean.FALSE, displayIndex, getTestColumn());
        }
        boolean canIncrease = !currentHero.isDifference() && (locked || Group.getInstance().getGlobalUnlock()
            || (currentHero.getTalentIncreaseTries(talentName) > 0));
        mModel.setValueAt(canIncrease, displayIndex,
            getIncrColumn());
      }
      else {
        mModel.setValueAt(NULL_INT, displayIndex, getDefaultValueColumn());
        mModel.setValueAt(NULL_INT, displayIndex, getCurrentValueColumn());
        mModel.setValueAt(Boolean.FALSE, displayIndex, getLockColumn());
        boolean canIncrease = (currentHero != null)
            && !currentHero.isDifference()
            && canIncreaseUnknownTalents()
            && (currentHero.getTalentIncreaseTries(talentName) > 0);
        mModel.setValueAt(canIncrease, displayIndex, getIncrColumn());
        if (enableTests)
          mModel.setValueAt(Boolean.FALSE, displayIndex, getTestColumn());
      }
      mModel.setValueAt(talentName, displayIndex, getNameDummyColumn());
      updateSubclassSpecificData(mModel, i, displayIndex);
      displayIndex++;
    }
    disableChange = false;
  }

  protected Vector<String> getColumnIdentifiers() {
    String[] columnHeaders = enableTests ? new String[] { Localization.getString("Talente.Talent"), Localization.getString("Talente.Std"), //$NON-NLS-1$ //$NON-NLS-2$
        Localization.getString("Talente.Akt"), "", "", "", "" } : new String[] { Localization.getString("Talente.Talent"), Localization.getString("Talente.Std"), Localization.getString("Talente.Akt"), "", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
        "", "" }; //$NON-NLS-1$ //$NON-NLS-2$
    Vector<String> result = new Vector<String>();
    for (String header : columnHeaders)
      result.add(header);
    return result;
  }

  protected void updateSubclassSpecificData(DefaultTableModel model, int i,
      int displayIndex) {
  }

  protected void initSubclassSpecificData(DefaultTableModel model, int i) {
  }

  void addTalent(Talent talent, boolean update) {
    talents.add(talent);
    if (update) createUI();
  }

  private TableCellRenderer mLockButtons;

  private TableCellRenderer mIncreaseButtons;

  private TableCellRenderer mTestButtons;

  protected TableSorter mSorter;

  /**
   * 
   * 
   */
  private void createUI() {
    if (mSorter != null && mSorter.isSorting()) {
      saveSortingState();
    }

    getJContentPane().removeAll();
    talents.clear();

    mModel = new TalentTableModel(0, getNrOfColumns());

    DefaultTableColumnModel tcm = new DefaultTableColumnModel();
    tcm.addColumn(new javax.swing.table.TableColumn(0, 160));
    FormattedTextFieldCellEditor numberEditor = new FormattedTextFieldCellEditor(
        new NumberFormatter(NumberFormat.getIntegerInstance()), this);
    numberEditor.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    numberEditor.addCellEditorListener(new TalentChanger());
    DefaultTableCellRenderer colouringRenderer = CellRenderers.createColouringCellRenderer(this);
    colouringRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    tcm.addColumn(new javax.swing.table.TableColumn(getDefaultValueColumn(),
        25, colouringRenderer, numberEditor));
    tcm.addColumn(new javax.swing.table.TableColumn(getCurrentValueColumn(),
        25, colouringRenderer, numberEditor));
    JToggleButton lockButton = TableButtonInput.createToggleButton(ImageManager.getIcon("locked")); //$NON-NLS-1$
    mLockButtons = TableButtonInput.createButtonCellRenderer(lockButton,
        new DefaultTableCellRenderer());
    lockButton.setPressedIcon(ImageManager.getIcon("unlocked")); //$NON-NLS-1$
    lockButton.setToolTipText(Localization.getString("Talente.SchuetzenFreigeben")); //$NON-NLS-1$
    tcm.addColumn(new javax.swing.table.TableColumn(getLockColumn(), 12,
        mLockButtons, TableButtonInput.createDummyCellEditor()));
    JButton testButton = null;
    if (enableTests) {
      testButton = TableButtonInput.createButton(ImageManager.getIcon("probe")); //$NON-NLS-1$
      mTestButtons = TableButtonInput.createButtonCellRenderer(testButton,
          new DefaultTableCellRenderer());
      testButton.setDisabledIcon(ImageManager.getIcon("probe_disabled")); //$NON-NLS-1$
      testButton.setToolTipText(Localization.getString("Talente.Probe")); //$NON-NLS-1$
      tcm.addColumn(new javax.swing.table.TableColumn(getTestColumn(), 12,
          mTestButtons, TableButtonInput.createDummyCellEditor()));
    }
    JButton increaseButton = TableButtonInput.createButton(ImageManager.getIcon("increase")); //$NON-NLS-1$
    mIncreaseButtons = TableButtonInput.createButtonCellRenderer(increaseButton,
        new DefaultTableCellRenderer());
    increaseButton.setDisabledIcon(ImageManager.getIcon("increase_disabled")); //$NON-NLS-1$
    increaseButton.setToolTipText(Localization.getString("Talente.Erhoehen")); //$NON-NLS-1$
    tcm.addColumn(new javax.swing.table.TableColumn(getIncrColumn(), 12,
        mIncreaseButtons, TableButtonInput.createDummyCellEditor()));

    addSubclassSpecificColumns(tcm);

    lockButton.addActionListener(new Locker());
    if (enableTests) testButton.addActionListener(new Tester());
    increaseButton.addActionListener(new Increaser());

    List<Talent> talentsInCategory = Talents.getInstance()
        .getTalentsInCategory(getTitle());
    for (Talent aTalent : talentsInCategory)
      addTalent(aTalent, false);

    for (int i = 0; i < talents.size(); ++i) {
      if (!shallDisplay(talents.get(i))) continue;
      StringBuffer descr = new StringBuffer(talents.get(i).getName());
      if (enableTests && talents.get(i).canBeTested()) {
        descr.append(" ("); //$NON-NLS-1$
        NormalTalent normalTalent = (NormalTalent) talents.get(i);
        descr.append(normalTalent.getFirstProperty());
        descr.append('/');
        descr.append(normalTalent.getSecondProperty());
        descr.append('/');
        descr.append(normalTalent.getThirdProperty());
        descr.append(')');
      }
      mModel.addRow(new Object[] { descr.toString() });
      mModel.setValueAt(Boolean.FALSE, mModel.getRowCount() - 1,
          getLockColumn());
      initSubclassSpecificData(mModel, i);
    }

    mModel.setColumnIdentifiers(getColumnIdentifiers());
    updateData();

    mSorter = new TableSorter(mModel);

    mTable = new JTable(mSorter, tcm);
    mTable.setOpaque(false);
    mSorter.setTableHeader(mTable.getTableHeader());

    for (int i = 0; i < mTable.getColumnCount(); ++i)
      tcm.getColumn(i).setHeaderValue(mTable.getColumnName(i));

    mTable.setColumnSelectionAllowed(false);
    mTable.setIntercellSpacing(new java.awt.Dimension(6, 6));
    mTable.setRowSelectionAllowed(false);
    mTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(22);
    TableButtonInput.Callbacks cb = new TableButtonInput.Callbacks() {
      public void setClickedRow(int row) { mButtonClickedRow = row; }
    };
    TableButtonInput.attachToTable(cb, mTable);
    mTable.setBackground(BACKGROUND_GRAY);
    if (dsa.gui.lf.Colors.hasCustomColors()) {
      mTable.setSelectionForeground(Colors.getSelectedForeground());
      mTable.setSelectionBackground(Colors.getSelectedBackground());
      mTable.setDefaultRenderer(Object.class, CellRenderers.createNormalCellRenderer());
      mTable.setDefaultRenderer(Optional.NULL_INT.getClass(),
          CellRenderers.createNormalCellRenderer());
    }
    mScrollPane = new JScrollPane(mTable);
    mScrollPane.setOpaque(false);
    mScrollPane.getViewport().setOpaque(false);
    ((BorderLayout) getJContentPane().getLayout()).setHgap(10);
    getJContentPane().add(mScrollPane, BorderLayout.CENTER);
    addSubclassSpecificComponents(getJContentPane());

    restoreSortingState();
    // this.setSize(getBounds().width, (nrOfTalents + 1) * 27 +
    // getSubclassSpecificSizeOffset().height);

    this.validate();
  }

  private JScrollPane mScrollPane;

  private void restoreSortingState() {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.PackageID.class);
    mSorter.restoreState(this.getTitle(), prefs);
  }

  protected final void saveSortingState() {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.PackageID.class);
    mSorter.saveState(this.getTitle(), prefs);
  }

  protected void addSubclassSpecificColumns(DefaultTableColumnModel tcm) {
  }

  protected void addSubclassSpecificComponents(java.awt.Container container) {
  }

  protected Dimension getSubclassSpecificSizeOffset() {
    return new Dimension(0, 0);
  }

  /**
   * 
   * @param talent
   * @return
   */
  protected boolean shallDisplay(Talent talent) {
    return true;
  }

  private class Locker implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      boolean wasEnabled = ((Boolean) mSorter.getValueAt(mButtonClickedRow,
          getLockColumn())).booleanValue();
      mSorter.setValueAt(!wasEnabled, mButtonClickedRow,
          getLockColumn());
      mSorter.setValueAt(!wasEnabled
          || Group.getInstance().getGlobalUnlock()
          || ((currentHero != null) && currentHero
              .getTalentIncreaseTries((String) mSorter.getValueAt(
                  mButtonClickedRow, getNameDummyColumn())) > 0),
          mButtonClickedRow, getIncrColumn());
      for (int column = 0; column < mTable.getColumnCount(); ++column)
        if (isLockedColumn(column))
          mTable.repaint(mTable.getCellRect(mButtonClickedRow, column, true));
    }
  };

  private class TalentChanger implements CellEditorListener {
    public void editingCanceled(ChangeEvent evt) {
    }

    public void editingStopped(ChangeEvent evt) {
      if (TalentFrame.this.disableChange) return;
      FormattedTextFieldCellEditor editor = (FormattedTextFieldCellEditor) evt
          .getSource();
      boolean current = (editor.getColumn() == getCurrentValueColumn());
      if (currentHero != null) {
        Number number = (Number) editor.getValue();
        changeTalentValue(current, editor.getCellInfo(), number.intValue());
      }
    }
  };

  protected void changeTalentValue(boolean current, String talent, int value) {
    if (current)
      currentHero.setCurrentTalentValue(talent, value);
    else
      currentHero.setDefaultTalentValue(talent, value);
  }

  private class Tester implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String talent = (String) mSorter.getValueAt(mButtonClickedRow,
          getNameDummyColumn());
      boolean spell = Talents.getInstance().getTalent(talent).isSpell();
      JDialog dialog = spell ? new SpellProbeDialog(TalentFrame.this, currentHero, talent) 
        : new ProbeDialog(TalentFrame.this, currentHero, talent);
      dialog.setLocationRelativeTo(TalentFrame.this);
      dialog.setVisible(true);
    };
  };

  protected void tryTalentIncrease(final String talent) {
    Hero hero = TalentFrame.this.currentHero;
    if (hero == null) return;
    if (!hero.hasTalent(talent)) {
      hero.addTalent(talent);
      int talentStartValue = SpellStartValues.getInstance().getStartValue(
          currentHero.getInternalType(), talent);
      currentHero.setDefaultTalentValue(talent, talentStartValue);
      currentHero.setCurrentTalentValue(talent, talentStartValue);
      currentHero.setTalentIncreaseTriesPerStep(talent, 
          SpellStartValues.getInstance().getIncreasesPerStep(currentHero.getInternalType(), talent));
      updateData();
      for (int i = 0; i < mTable.getRowCount(); ++i) {
        if (mSorter.getValueAt(i, getNameDummyColumn()).equals(talent)) {
          mTable.scrollRectToVisible(mTable.getCellRect(i, 0, true));
          break;
        }
      }
      // return;
    }
    int currentValue = hero.getDefaultTalentValue(talent);
    int diceThrow = Dice.roll(6) + Dice.roll(6);
    if (currentValue > 9) diceThrow += Dice.roll(6);
    boolean success = diceThrow > currentValue;
    if (((Boolean) mSorter.getValueAt(mButtonClickedRow, getLockColumn()))
        .booleanValue()
        || Group.getInstance().getGlobalUnlock()) {
      if (success) {
    	  hero.changeTalentValue(talent, 1);
      }
    }
    else {
      hero.removeTalentIncreaseTry(talent, success);
    }
    backgrounds.put(talent, success ? Color.GREEN : Color.RED);
    final int row = mButtonClickedRow;
    mTable.repaint(mTable.getCellRect(row, getDefaultValueColumn(), true));
    mTable.repaint(mTable.getCellRect(row, getCurrentValueColumn(), true));
    Timer timer = new Timer(250, new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		backgrounds.remove(talent);
    	    mTable.repaint(mTable.getCellRect(row, getDefaultValueColumn(), true));
    	    mTable.repaint(mTable.getCellRect(row, getCurrentValueColumn(), true));
    	}
    });
    timer.setRepeats(false);
    timer.start();
  }

  private class Increaser implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String talent = (String) mSorter.getValueAt(mButtonClickedRow,
          getNameDummyColumn());
      tryTalentIncrease(talent);
    }
  };

  /**
   * This method initializes this
   * 
   * @return void
   */
  protected void initialize() {
    this.setContentPane(getJContentPane());
    createUI();
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(new java.awt.BorderLayout());
    }
    return jContentPane;
  }
  
  protected boolean isTalentRelevant(String talent) {
    return false;
  }

  protected Hero currentHero = null;

  protected ArrayList<Talent> talents;

  protected CharacterObserver myCharacterObserver = new MyCharacterObserver();

  private class MyCharacterObserver extends CharacterAdapter {
    public void defaultTalentChanged(String talent) {
      if (isTalentRelevant(talent)) {
        TalentFrame.this.updateData();
        return;
      }
      for (Talent test : talents)
        if (test.getName().equals(talent)) {
          TalentFrame.this.updateData();
          break;
        }
    }

    public void currentTalentChanged(String talent) {
      if (isTalentRelevant(talent)) {
        TalentFrame.this.updateData();
        return;
      }
      for (Talent test : talents)
        if (test.getName().equals(talent)) {
          TalentFrame.this.updateData();
          break;
        }
    }

    public void talentAdded(String talent) {
      if (isTalentRelevant(talent)) {
        TalentFrame.this.createUI();
        return;
      }
      for (Talent test : Talents.getInstance().getTalentsInCategory(getTitle())) {
        if (test.getName().equals(talent)) {
          TalentFrame.this.createUI();
          break;
        }
      }
    }

    public void talentRemoved(String talent) {
      if (isTalentRelevant(talent)) {
        TalentFrame.this.createUI();
        return;
      }
      for (Talent test : talents)
        if (test.getName().equals(talent)) {
          TalentFrame.this.createUI();
          break;
        }
    }

    public void stepIncreased() {
      TalentFrame.this.updateData();
    }

    public void increaseTriesChanged() {
      TalentFrame.this.updateData();
    }

    public void derivedValueChanged(Hero.DerivedValue dv) {
      if (isRelevant(dv)) TalentFrame.this.updateData();
    }
  };

  protected boolean isRelevant(Hero.DerivedValue dv) {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#ActiveCharacterChanged(dsa.data.Hero,
   *      dsa.data.Hero)
   */
  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    if (newCharacter != null)
      newCharacter.addHeroObserver(myCharacterObserver);
    if (oldCharacter != null)
      oldCharacter.removeHeroObserver(myCharacterObserver);
    updateData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterRemoved(dsa.data.Hero)
   */
  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterAdded(dsa.data.Hero)
   */
  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
    updateData();
  }
}

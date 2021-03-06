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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import dsa.control.GroupOperations;
import dsa.control.OnlineOperations;
import dsa.remote.RemoteManager;
import dsa.remote.ServerManager;
import dsa.util.Directories;
import dsa.gui.dialogs.AnimalSelectionDialog;
import dsa.gui.dialogs.DiceRollDialog;
import dsa.gui.dialogs.OptionsDialog;
import dsa.gui.dialogs.PrintingDialog;
import dsa.gui.lf.BGList;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.gui.util.OptionsChange.OptionsListener;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.GroupObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Animal;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
import dsa.model.data.Clothes;
import dsa.model.data.Opponents;
import dsa.model.data.Shield;
import dsa.model.data.Shields;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Weapon;
import dsa.model.data.Weapons;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * 
 */
public final class ControlFrame extends SubFrame 
    implements DropTargetListener, FrameManagement.FrameStateChanger {

  private javax.swing.JPanel jContentPane = null;

  private JTabbedPane jTabbedPane = null;

  private JPanel heroPanel = null;

  /**
   * This is the default constructor
   */
  public ControlFrame(boolean loadLastGroup) {
    super(Localization.getString("Hauptfenster.Heldenverwaltung")); //$NON-NLS-1$
    inStart = true;
    listenForFrames = false;
    initialize(loadLastGroup);
    FrameManagement.getInstance().setFrameOpener(this);
    FrameLayouts.getInstance().restoreLastLayout();
    listenForFrames = true;
    rebuildLayoutsPanel();
    inStart = false;
    boolean checkForNewVersion = Preferences.userNodeForPackage(dsa.gui.PackageID.class).getBoolean("VersionCheckAtStart", true); //$NON-NLS-1$
    if (checkForNewVersion) {
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          OnlineOperations.checkForUpdate(ControlFrame.this, false);
        }
      });
    }
  }
  
  private boolean inStart = false;

  public String getHelpPage() {
    return "Hauptfenster";  //$NON-NLS-1$
  }

  private DropTarget dropTarget = null;

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize(boolean loadLastGroup) {
    this.setJMenuBar(getJJMenuBar());
    // this.setSize(582, 138);
    this.setContentPane(getJContentPane());
    this.addWindowListener(new MyWindowListener());
    this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    if (loadLastGroup) {
      String fileName = Preferences.userNodeForPackage(dsa.gui.PackageID.class)
          .get("lastUsedGroupFile", ""); //$NON-NLS-1$ //$NON-NLS-2$
      if (!fileName.equals("")) loadChars(fileName); //$NON-NLS-1$
      String lastHero = Preferences.userNodeForPackage(dsa.gui.PackageID.class)
          .get("lastActiveHero", ""); //$NON-NLS-1$ //$NON-NLS-2$
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        if (hero.getName().equals(lastHero)) {
          Group.getInstance().setActiveHero(hero);
          break;
        }
      }
      OptionsChange.fireOptionsChanged();
      getGroupSaveItem().setEnabled(!fileName.equals("")); //$NON-NLS-1$
    }
    if (dropTarget == null) {
      dropTarget = new DropTarget(this, this);
    }
  }
  
  /**
   * 
   * 
   */
  public void saveAndExit() {
    if (!GroupOperations.autoSaveAll(this)) return;
    try {
      FrameLayouts.getInstance().storeToFile(FrameLayouts.getDefaultLayoutsFilename());
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, Localization.getString("Hauptfenster.FensterlayoutsSpeicherFehler")  //$NON-NLS-1$
          + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    
    prefs.putInt("SelectedPaneIndex", getJTabbedPane().getSelectedIndex()); //$NON-NLS-1$
    prefs.put("lastUsedGroupFile", Group.getInstance().getCurrentFileName()); //$NON-NLS-1$
    if (Group.getInstance().getActiveHero() != null) {
      prefs
          .put("lastActiveHero", Group.getInstance().getActiveHero().getName()); //$NON-NLS-1$
    }
    String baseDir = Directories.getUserDataPath();
    try {
      Talents.getInstance().saveUserTalents(baseDir + "Eigene_Talente.dat"); //$NON-NLS-1$
    }
    catch (java.io.IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.TalenteSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      Talents.getInstance().saveUserSpells(baseDir + "Eigene_Zauber.dat"); //$NON-NLS-1$
    }
    catch (java.io.IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.ZauberSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      Things.getInstance().writeUserDefinedThings(
          baseDir + "Eigene_Ausruestung.dat"); //$NON-NLS-1$
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.AusruestungSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      Weapons.getInstance().storeUserDefinedWeapons(
          baseDir + "Eigene_Waffen.dat"); //$NON-NLS-1$
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.WaffenSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      Armours.getInstance().saveUserDefinedArmours(
          baseDir + "Eigene_Ruestungen.dat"); //$NON-NLS-1$
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.RuestungenSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      Shields.getInstance().writeUserDefinedFile(baseDir + "Eigene_Parade.dat"); //$NON-NLS-1$
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.ParadeSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
    	Clothes.getInstance().writeUserDefinedCloths(baseDir + "Eigene_Kleidung.dat"); //$NON-NLS-1$
    }
    catch (IOException e) {
        JOptionPane.showMessageDialog(this,
                Localization.getString("Hauptfenster.KleidungSpeicherFehler") //$NON-NLS-1$
                    + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE);    	 //$NON-NLS-1$
    }
    try {
      Opponents.getOpponentsDB().writeToFile(baseDir + "Eigene_Gegner.dat", true); //$NON-NLS-1$
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.GegnerSpeicherFehler") //$NON-NLS-1$
          + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    try {
      prefs.flush();
    }
    catch (BackingStoreException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.EinstellungenSpeicherFehler") //$NON-NLS-1$
              + e.getMessage(), Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
    RemoteManager.getInstance().disconnect();
    ServerManager.getInstance().stopServer();
    Group.getInstance().setActiveHero(null);
    dispose();
    if (FrameManagement.getInstance().getOpenFrames().size() == 0) {
      System.exit(0);
    }
    else {
      FrameManagement.getInstance().setExiting();
      FrameManagement.getInstance().closeAllFrames(ControlFrame.this);
    }
  }

  protected void loadChars(String filename) {
    try {
      Group.getInstance().loadFromFile(new File(filename));
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          Localization.getString("Hauptfenster.GruppenLadeFehler") + e.getMessage(), //$NON-NLS-1$
          Localization.getString("Hauptfenster.Heldenverwaltung"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
    }
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
      jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
      getJTabbedPane().setSelectedIndex(
          Preferences.userNodeForPackage(dsa.gui.PackageID.class).getInt(
              "SelectedPaneIndex", 0)); //$NON-NLS-1$
      jContentPane.add(getAlwaysPane(), java.awt.BorderLayout.NORTH);
    }
    return jContentPane;
  }

  private JPanel alwaysPane = null;

  private JToggleButton stepButton;

  private JToggleButton derivedValuesButton;
  
  private JToggleButton onlineButton, onlineServerButton;

  private final class MyWindowListener extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      saveAndExit();
    }

    public void windowClosed(WindowEvent e) {
      // saveAndExit();
    }

    public void windowIconified(WindowEvent e) {
      FrameManagement.getInstance().iconifyAllFrames(ControlFrame.this);
    }

    public void windowDeiconified(WindowEvent e) {
      FrameManagement.getInstance().deiconifyAllFrames(ControlFrame.this);
    }

    boolean update = false;

    public void windowActivated(WindowEvent e) {
      if (update) {
        update = false;
        return;
      }
      update = true;
      FrameManagement.getInstance().activateAllFrames(ControlFrame.this);
    }
  }

  class CharacterNameListener extends CharacterAdapter {
    public void nameChanged(String oldName, String newName) {
      listenForCharacterBox = false;
      heroBox.removeItem(oldName);
      heroBox.addItem(newName);
      if (Group.getInstance().getActiveHero().getName().equals(newName)) {
        heroBox.setSelectedItem(newName);
      }
      listenForCharacterBox = true;
    }
  }

  private CharacterNameListener nameListener;

  private boolean listenForCharacterBox = true;

  private JPanel getAlwaysPane() {
    if (alwaysPane == null) {
      alwaysPane = new JPanel();
      alwaysPane.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,
          12, 10));
      newHeroButton = new JButton(ImageManager.getIcon("tsa")); //$NON-NLS-1$
      newHeroButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.newHero(ControlFrame.this);
        }
      });
      final int BUTTON_SIZE = 22;
      newHeroButton.setToolTipText(Localization.getString("Hauptfenster.NeuenHeldenErzeugen")); //$NON-NLS-1$
      newHeroButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
      alwaysPane.add(newHeroButton);
      
      printHeroButton = new JButton(ImageManager.getIcon("print")); //$NON-NLS-1$
      printHeroButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          PrintingDialog dialog = new PrintingDialog(Group.getInstance()
              .getActiveHero(), ControlFrame.this);
          dialog.setVisible(true);
        }
      });
      printHeroButton.setToolTipText(Localization.getString("Hauptfenster.HeldendatenDrucken")); //$NON-NLS-1$
      printHeroButton.setEnabled(false);
      printHeroButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
      alwaysPane.add(printHeroButton);
      
      heroBox = new JComboBox();
      nameListener = new CharacterNameListener();
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        heroBox.addItem(hero.getName());
        hero.addHeroObserver(nameListener);
      }
      Group.getInstance().addObserver(new GroupObserver() {
        public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
          ControlFrame.this.activeCharacterChanged(newCharacter);
        }

        public void characterRemoved(Hero character) {
          character.removeHeroObserver(nameListener);
          heroBox.removeItem(character.getName());
        }

        public void characterAdded(Hero character) {
          heroBox.addItem(character.getName());
          character.addHeroObserver(nameListener);
          if (!inStart) {
            if (!physButton.isSelected()) {
              physButton.doClick();
            }
            else {
              FrameManagement.getInstance().getFrame(Localization.getString("Hauptfenster.Grunddaten")).toFront(); //$NON-NLS-1$
            }
          }
        }

        public void globalLockChanged() {
        }

        public void groupLoaded() {
        }

        public void orderChanged() {
          listenForCharacterBox = false;
          heroBox.removeAllItems();
          for (Hero h : Group.getInstance().getAllCharacters()) {
            heroBox.addItem(h.getName());
          }
          heroBox.setSelectedItem(Group.getInstance().getActiveHero().getName());
          listenForCharacterBox = true;
        }
        
        public void opponentsChanged() {
        }
        
        public void characterReplaced(Hero oldHero, Hero newHero) {
        	oldHero.removeHeroObserver(nameListener);
        	newHero.addHeroObserver(nameListener);
        }
      });
      heroBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (!listenForCharacterBox) return;
          String selectedHero = (String) heroBox.getSelectedItem();
          for (Hero hero : Group.getInstance().getAllCharacters()) {
            if (hero.getName().equals(selectedHero)) {
              Group.getInstance().setActiveHero(hero);
              break;
            }
          }
        }
      });
      heroBox.setPreferredSize(new Dimension(210,
          heroBox.getPreferredSize().height));
      alwaysPane.add(heroBox);
      
      lockButton = new JToggleButton(ImageManager.getIcon("locked")); //$NON-NLS-1$
      lockButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lockButton.setIcon(ImageManager
              .getIcon(lockButton.isSelected() ? "unlocked" : "locked")); //$NON-NLS-1$ //$NON-NLS-2$
          Group.getInstance().setGlobalUnlock(lockButton.isSelected());
        }
      });
      lockButton.setToolTipText(Localization.getString("Hauptfenster.AllesSchuetzenFreigeben")); //$NON-NLS-1$
      alwaysPane.add(lockButton);
      
      groupButton = new JToggleButton(ImageManager.getIcon("group")); //$NON-NLS-1$
      groupButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new GroupFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.groupButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private GroupFrame frame = null;
      });
      groupButton.setToolTipText(Localization.getString("Hauptfenster.Gruppe")); //$NON-NLS-1$
      alwaysPane.add(groupButton);
      frameButtons.put(Localization.getString("Hauptfenster.Gruppe"), groupButton); //$NON-NLS-1$
      
      saveAllButton = new JButton(ImageManager.getIcon("saveall")); //$NON-NLS-1$
      saveAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.saveAllHeroes(ControlFrame.this);
          GroupOperations.saveGroup(ControlFrame.this);
          rebuildLastGroupsMenu();
        }
      });
      saveAllButton.setToolTipText(Localization.getString("Hauptfenster.AllesSpeichern")); //$NON-NLS-1$
      saveAllButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
      alwaysPane.add(saveAllButton);
      
      diceButton = new JButton(ImageManager.getIcon("probe")); //$NON-NLS-1$
      diceButton.addActionListener(new ActionListener() {
    	  public void actionPerformed(ActionEvent e) {
    		  DiceRollDialog dialog = new DiceRollDialog(ControlFrame.this);
    		  dialog.setVisible(true);
    	  }
      });
      diceButton.setToolTipText(Localization.getString("Hauptfenster.FreiWuerfeln")); //$NON-NLS-1$
      diceButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
      alwaysPane.add(diceButton);
    }
    return alwaysPane;
  }

  private JToggleButton lockButton;
  
  private JToggleButton groupButton;

  private JButton newHeroButton;
  
  private JButton printHeroButton;
  
  private JButton saveAllButton;
  
  private JButton diceButton;

  /**
   * This method initializes jTabbedPane
   * 
   * @return javax.swing.JTabbedPane
   */
  private JTabbedPane getJTabbedPane() {
    if (jTabbedPane == null) {
      jTabbedPane = new JTabbedPane();
      jTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Layouts"), null, getLayoutsPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Allgemein"), null, getHeroPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Talente"), null, getTalentsPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Kampf"), null, getFightPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Gegenstaende"), null, getThingsPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Tiere"), null, getAnimalsPanel(), null); //$NON-NLS-1$
      jTabbedPane.addTab(Localization.getString("Hauptfenster.Sonstiges"), null, getOthersPanel(), null); //$NON-NLS-1$
    }
    return jTabbedPane;
  }

  private static final int VGAP = 12;

  private JPanel animalsPane;

  private JList animalsList;

  private DefaultListModel animalsModel;

  private JButton addAnimalButton;

  private JButton editAnimalButton;

  private JButton deleteAnimalButton;

  private class AnimalsListFiller implements CharactersObserver {
    public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
      clearList(oldCharacter);
      fillAnimalsList(newCharacter, true);
    }

    private void clearList(Hero hero) {
      Preferences prefs = Preferences
          .userNodeForPackage(dsa.gui.PackageID.class);
      if (hero != null) {
    	String name = hero.getName();
    	String keyExt = "_AnimalFrameCount"; //$NON-NLS-1$
    	if (name.length() + keyExt.length() > Preferences.MAX_KEY_LENGTH)
    	{
    		name = name.substring(0, Preferences.MAX_KEY_LENGTH - keyExt.length());
    	}
        prefs.putInt(name + keyExt, animalFrames.size());
        int nr = 0;
        for (String animal : animalFrames.keySet()) {
          int index = -1;
          for (int i = 0; i < hero.getNrOfAnimals(); ++i) {
            if (hero.getAnimal(i).getName().equals(animal)) {
              index = i;
              break;
            }
          }
          prefs.putInt(name + "_AnimalFrame_" + nr, index); //$NON-NLS-1$
          ++nr;
        }
      }
      HashMap<String, AnimalFrame> temp = new HashMap<String, AnimalFrame>();
      temp.putAll(animalFrames);
      for (AnimalFrame frame : temp.values()) {
        frame.dispose();
      }
      temp.clear();
      animalFrames.clear();
    }

    public void characterRemoved(Hero character) {
    }

    public void characterAdded(Hero character) {
    }

    public void globalLockChanged() {
    }
  }

  private void fillAnimalsList(Hero hero, boolean openFrames) {
    if (animalsList == null) {
      return;
    }
    animalsModel.clear();
    if (hero == null) {
      editAnimalButton.setEnabled(false);
      deleteAnimalButton.setEnabled(false);
      addAnimalButton.setEnabled(false);
      return;
    }
    addAnimalButton.setEnabled(!hero.isDifference());
    int nrOfAnimals = hero.getNrOfAnimals();
    for (int i = 0; i < nrOfAnimals; ++i) {
      dsa.model.data.Animal animal = hero.getAnimal(i);
      String entry = animal.getName() + " (" + animal.getCategory() + " )"; //$NON-NLS-1$ //$NON-NLS-2$
      animalsModel.addElement(entry);
    }
    if (nrOfAnimals > 0 && !hero.isDifference()) {
      animalsList.setSelectedIndex(0);
      editAnimalButton.setEnabled(true);
      deleteAnimalButton.setEnabled(true);
    }
    else {
      editAnimalButton.setEnabled(false);
      deleteAnimalButton.setEnabled(false);
    }
    if (openFrames) {
      Preferences prefs = Preferences
          .userNodeForPackage(dsa.gui.PackageID.class);
      String name = hero.getName();
	  String keyExt = "_AnimalFrameCount"; //$NON-NLS-1$
	  if (name.length() + keyExt.length() > Preferences.MAX_KEY_LENGTH)
	  {
		name = name.substring(0, Preferences.MAX_KEY_LENGTH - keyExt.length());
	  }
      int nrOfOpenFrames = prefs
          .getInt(name + "_AnimalFrameCount", 0); //$NON-NLS-1$
      for (int i = 0; i < nrOfOpenFrames; ++i) {
        int index = prefs.getInt(name + "_AnimalFrame_" + i, -1); //$NON-NLS-1$
        if (index != -1) {
          editAnimal(index, false);
        }
      }
    }
  }

  private void deleteAnimal() {
    int index = animalsList.getSelectedIndex();
    if (index < 0) return;
    Hero hero = Group.getInstance().getActiveHero();
    hero.removeAnimal(index);
    fillAnimalsList(hero, false);
  }

  private final HashMap<String, AnimalFrame> animalFrames = new HashMap<String, AnimalFrame>();

  private final HashMap<String, Animal.Listener> animalListeners = new HashMap<String, Animal.Listener>();

  private void editAnimal() {
    int index = animalsList.getSelectedIndex();
    if (index < 0) return;
    editAnimal(index, false);
  }

  private void editAnimal(int index, boolean setPosition) {
    Hero hero = Group.getInstance().getActiveHero();
    dsa.model.data.Animal animal = hero.getAnimal(index);
    if (animal == null) return;
    if (animalFrames.containsKey(animal.getName())) {
      animalFrames.get(animal.getName()).toFront();
    }
    else {
      AnimalFrame af = new AnimalFrame(animal, setPosition);
      if (setPosition) {
        af.setLocationRelativeTo(this);
      }
      af.addWindowListener(new WindowAdapter() {
        public void windowClosed(WindowEvent e) {
          String name = ((AnimalFrame) e.getSource()).getTitle();
          animalFrames.remove(name);
          Animal.Listener l = animalListeners.get(name);
          ((AnimalFrame) e.getSource()).getAnimal().removeListener(l);
          animalListeners.remove(name);
        }

        public void windowClosing(WindowEvent e) {
          String name = ((AnimalFrame) e.getSource()).getTitle();
          animalFrames.remove(name);
          Animal.Listener l = animalListeners.get(name);
          ((AnimalFrame) e.getSource()).getAnimal().removeListener(l);
          animalListeners.remove(name);
        }
      });
      Animal.Listener l = new dsa.model.data.Animal.Listener() {
        public void nameChanged(String oldName, String newName) {
          AnimalFrame f = animalFrames.get(oldName);
          if (f != null) {
            animalFrames.remove(oldName);
            animalFrames.put(newName, f);
          }
          fillAnimalsList(Group.getInstance().getActiveHero(), false);
        }

      };
      animal.addListener(l);
      animalListeners.put(animal.getName(), l);
      animalFrames.put(animal.getName(), af);
      af.setVisible(true);
    }
  }

  private void addAnimal() {
    AnimalSelectionDialog asd = new AnimalSelectionDialog(this);
    asd.setVisible(true);
    dsa.model.data.Animal a = asd.getAnimal();
    if (a != null) {
      Group.getInstance().getActiveHero().addAnimal(a);
      fillAnimalsList(Group.getInstance().getActiveHero(), false);
      animalsList.setSelectedIndex(animalsModel.size() - 1);
      editAnimal(animalsModel.size() - 1, true);
    }
  }

  private JPanel getAnimalsPanel() {
    if (animalsPane == null) {
      animalsPane = new JPanel(new BorderLayout());
      animalsModel = new DefaultListModel();
      animalsList = new BGList(animalsModel);
      animalsList.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() > 1) {
            editAnimal();
          }
        }
      });
      JScrollPane scrollPane = new JScrollPane(animalsList);
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      animalsPane.add(scrollPane, BorderLayout.CENTER);
      JPanel buttonsPane = new JPanel();
      buttonsPane.setLayout(null);
      addAnimalButton = new JButton(ImageManager.getIcon("increase")); //$NON-NLS-1$
      addAnimalButton
          .setDisabledIcon(ImageManager.getIcon("increase_disabled")); //$NON-NLS-1$
      addAnimalButton.setBounds(5, 5, 60, 25);
      addAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addAnimal();
        }
      });
      buttonsPane.add(addAnimalButton, null);
      editAnimalButton = new JButton("..."); //$NON-NLS-1$
      editAnimalButton.setBounds(5, 40, 60, 25);
      editAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          editAnimal();
        }
      });
      buttonsPane.add(editAnimalButton, null);
      deleteAnimalButton = new JButton(ImageManager.getIcon("decrease_enabled")); //$NON-NLS-1$
      deleteAnimalButton.setDisabledIcon(ImageManager.getIcon("decrease")); //$NON-NLS-1$
      deleteAnimalButton.setBounds(5, 75, 60, 25);
      deleteAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteAnimal();
        }
      });
      buttonsPane.add(deleteAnimalButton, null);
      buttonsPane.setPreferredSize(new java.awt.Dimension(70, 80));
      animalsPane.add(buttonsPane, BorderLayout.EAST);
      JLabel filler1 = new JLabel(""); //$NON-NLS-1$
      filler1.setPreferredSize(new java.awt.Dimension(5, 5));
      animalsPane.add(filler1, BorderLayout.NORTH);
      JLabel filler2 = new JLabel(""); //$NON-NLS-1$
      filler2.setPreferredSize(new java.awt.Dimension(5, 5));
      animalsPane.add(filler2, BorderLayout.WEST);
      JLabel filler3 = new JLabel(""); //$NON-NLS-1$
      filler3.setPreferredSize(new java.awt.Dimension(5, 5));
      animalsPane.add(filler3, BorderLayout.SOUTH);
      Hero hero = Group.getInstance().getActiveHero();
      fillAnimalsList(hero, false);
      Group.getInstance().addObserver(new AnimalsListFiller());
    }
    return animalsPane;
  }

  private static final int SIDESPACE = 15;

  private static final int HGAP = 15;

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getHeroPanel() {
    if (heroPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      physButton = new JToggleButton(Localization.getString("Hauptfenster.Grunddaten")); //$NON-NLS-1$
      physButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PhysFrame(Localization.getString("Hauptfenster.Grunddaten")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.physButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private PhysFrame frame = null;
      });
      temp.add(physButton);
      frameButtons.put(Localization.getString("Hauptfenster.Grunddaten"), physButton); //$NON-NLS-1$

      stepButton = new JToggleButton(Localization.getString("Hauptfenster.Erfahrung")); //$NON-NLS-1$
      stepButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new StepFrame(Localization.getString("Hauptfenster.Erfahrung")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.stepButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private StepFrame frame = null;
      });
      temp.add(stepButton);
      frameButtons.put(Localization.getString("Hauptfenster.Erfahrung"), stepButton); //$NON-NLS-1$

      goodPropertiesButton = new JToggleButton(Localization.getString("Hauptfenster.GuteEigenschaften")); //$NON-NLS-1$
      goodPropertiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PropertyFrame(Localization.getString("Hauptfenster.GuteEigenschaften"), true); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.goodPropertiesButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private PropertyFrame frame = null;
      });
      temp.add(goodPropertiesButton);
      frameButtons.put(Localization.getString("Hauptfenster.GuteEigenschaften"), goodPropertiesButton); //$NON-NLS-1$

      badPropertiesButton = new JToggleButton(Localization.getString("Hauptfenster.SchlechteEigenschaften")); //$NON-NLS-1$
      badPropertiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PropertyFrame(Localization.getString("Hauptfenster.SchlechteEigenschaften"), false); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.badPropertiesButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private PropertyFrame frame = null;
      });
      temp.add(badPropertiesButton);
      frameButtons.put(Localization.getString("Hauptfenster.SchlechteEigenschaften"), badPropertiesButton); //$NON-NLS-1$

      energiesButton = new JToggleButton(Localization.getString("Hauptfenster.Energien")); //$NON-NLS-1$
      energiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new EnergyFrame(Localization.getString("Hauptfenster.Energien")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.energiesButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private EnergyFrame frame = null;
      });
      temp.add(energiesButton);
      frameButtons.put(Localization.getString("Hauptfenster.Energien"), energiesButton); //$NON-NLS-1$

      derivedValuesButton = new JToggleButton(Localization.getString("Hauptfenster.BerechneteWerte")); //$NON-NLS-1$
      derivedValuesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new DerivedValuesFrame(Localization.getString("Hauptfenster.BerechneteWerte")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.derivedValuesButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private DerivedValuesFrame frame = null;
      });
      temp.add(derivedValuesButton);
      frameButtons.put(Localization.getString("Hauptfenster.BerechneteWerte"), derivedValuesButton); //$NON-NLS-1$
      
      temp.add(new JLabel("")); //$NON-NLS-1$
      temp.add(new JLabel("")); //$NON-NLS-1$
      
      onlineButton = new JToggleButton(Localization.getString("Hauptfenster.OnlineSpiel")); //$NON-NLS-1$
      onlineButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new RemoteFrame(Localization.getString("Hauptfenster.OnlineSpiel")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.onlineButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private RemoteFrame frame = null;
      });
      temp.add(onlineButton);
      frameButtons.put(Localization.getString("Hauptfenster.OnlineSpiel"), onlineButton); //$NON-NLS-1$
      
      onlineServerButton = new JToggleButton(Localization.getString("Hauptfenster.OnlineServer")); //$NON-NLS-1$
      onlineServerButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ServerFrame(Localization.getString("Hauptfenster.OnlineServer")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.onlineServerButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ServerFrame frame = null;
      });
      temp.add(onlineServerButton);
      frameButtons.put(Localization.getString("Hauptfenster.OnlineServer"), onlineServerButton); //$NON-NLS-1$

      heroPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel(""); //$NON-NLS-1$
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel(""); //$NON-NLS-1$
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      heroPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel(""); //$NON-NLS-1$
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      heroPanel.add(l2, BorderLayout.EAST);
      heroPanel.add(temp2, BorderLayout.CENTER);
    }
    return heroPanel;
  }

  private JToggleButton ritualsButton;

  private JToggleButton magicButton;

  private JToggleButton metaButton;

  private JPanel getOthersPanel() {
    if (othersPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      langButton = new JToggleButton(Localization.getString("Hauptfenster.Sprachen")); //$NON-NLS-1$
      langButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new LanguageFrame(Localization.getString("Hauptfenster.Sprachen")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.langButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private LanguageFrame frame = null;
      });
      temp.add(langButton);
      frameButtons.put(Localization.getString("Hauptfenster.Sprachen"), langButton); //$NON-NLS-1$

      bgButton = new JToggleButton(Localization.getString("Hauptfenster.Hintergrund")); //$NON-NLS-1$
      bgButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new BackgroundFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.bgButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private BackgroundFrame frame = null;
      });
      temp.add(bgButton);
      frameButtons.put(Localization.getString("Hauptfenster.Hintergrund"), bgButton); //$NON-NLS-1$

      ritualsButton = new JToggleButton(Localization.getString("Hauptfenster.Sonderfertigkeiten")); //$NON-NLS-1$
      ritualsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new RitualsFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.ritualsButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private RitualsFrame frame = null;
      });
      temp.add(ritualsButton);
      frameButtons.put(Localization.getString("Hauptfenster.Sonderfertigkeiten"), ritualsButton); //$NON-NLS-1$

      personsButton = new JToggleButton(Localization.getString("Hauptfenster.Personen")); //$NON-NLS-1$
      personsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PersonsFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.personsButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private PersonsFrame frame = null;
      });
      temp.add(personsButton);
      frameButtons.put(Localization.getString("Hauptfenster.Personen"), personsButton); //$NON-NLS-1$

      magicButton = new JToggleButton(Localization.getString("Hauptfenster.Magie")); //$NON-NLS-1$
      magicButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MagicAttributeFrame(Localization.getString("Hauptfenster.Magie")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.magicButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private MagicAttributeFrame frame = null;
      });
      temp.add(magicButton);
      frameButtons.put(Localization.getString("Hauptfenster.Magie"), magicButton); //$NON-NLS-1$

      imageButton = new JToggleButton(Localization.getString("Hauptfenster.Bild")); //$NON-NLS-1$
      imageButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ImageFrame(Localization.getString("Hauptfenster.Bild")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.imageButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ImageFrame frame = null;
      });
      temp.add(imageButton);
      frameButtons.put(Localization.getString("Hauptfenster.Bild"), imageButton); //$NON-NLS-1$

      metaButton = new JToggleButton(Localization.getString("Hauptfenster.Metadaten")); //$NON-NLS-1$
      metaButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new TypeMetaFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.metaButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private TypeMetaFrame frame = null;
      });
      temp.add(metaButton);
      frameButtons.put(Localization.getString("Hauptfenster.Metadaten"), metaButton); //$NON-NLS-1$

      othersPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel(""); //$NON-NLS-1$
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel(""); //$NON-NLS-1$
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      othersPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel(""); //$NON-NLS-1$
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      othersPanel.add(l2, BorderLayout.EAST);
      othersPanel.add(temp2, BorderLayout.CENTER);
    }
    return othersPanel;
  }

  private javax.swing.JLabel sumLabel, sumLabel2, sumLabel3;
  
  private static String getTopLevelContainer(Hero hero, String thing) {
    String container = thing;
    while (hero.getThingCount(container) > 0) {
      container = hero.getThingContainer(container);
    }
    return container;
  }

  private void calcSums() {
    long weight = 0;
    Hero currentHero = Group.getInstance().getActiveHero();
    if (currentHero == null) {
      sumLabel.setText(""); //$NON-NLS-1$
      sumLabel3.setText(""); //$NON-NLS-1$
      sumLabel2.setText(Localization.getString("Hauptfenster.Gesamtgewicht0Stein")); //$NON-NLS-1$
      return;
    }
    Things things = Things.getInstance();
    for (String name : currentHero.getThings()) {
      if (Localization.getString("Hauptfenster.Ausruestung").equals(getTopLevelContainer(currentHero, name))) { //$NON-NLS-1$
        Thing thing = things.getThing(name);
        if (thing != null) {
          weight += (long) thing.getWeight() * currentHero.getThingCount(name);
        }
      }
    }
    float weightStones = weight / 40.0f;

    int armourWeight = 0;
    Armours armours = Armours.getInstance();
    for (String name : currentHero.getArmours()) {
      Armour armour = armours.getArmour(name);
      if (armour != null) {
        armourWeight += armour.getWeight();
      }
    }
    float armourWeightStones = armourWeight / 40.0f;

    long weaponWeight = 0;
    Weapons weapons = Weapons.getInstance();
    for (String name : currentHero.getWeapons()) {
      Weapon weapon = weapons.getWeapon(name);
      if (weapon != null) {
        weaponWeight += (long) currentHero.getWeaponCount(name)
            * weapon.getWeight();
        if (weapon.isProjectileWeapon() && weapon.getProjectileWeight().hasValue()) {
          weaponWeight += (long) (currentHero.getNrOfProjectiles(name) * weapon.getProjectileWeight().getValue());
        }
      }
    }
    float weaponWeightStones = weaponWeight / 40.0f;

    int moneyWeightTenthOfSkrupel = 0;
    for (int i = 0; i < currentHero.getNrOfCurrencies(false); ++i) {
      moneyWeightTenthOfSkrupel += currentHero.getMoney(i, false)
          * dsa.model.data.Currencies.getInstance().getWeight(
              currentHero.getCurrency(i, false));
    }
    float moneyWeightUnzes = moneyWeightTenthOfSkrupel / 250.0f;
    float moneyWeightStones = moneyWeightUnzes / 40.0f;

    int shieldWeight = 0;
    dsa.model.data.Shields shields = Shields.getInstance();
    for (String name : currentHero.getShields()) {
      Shield shield = shields.getShield(name);
      if (shield != null) {
        shieldWeight += shield.getWeight();
      }
    }
    float shieldWeightStones = shieldWeight / 40.0f;

    float overallWeight = (weight + armourWeight + weaponWeight
        + moneyWeightUnzes + shieldWeight) / 40.0f;
    float kkWeight = currentHero.getCurrentDerivedValue(Hero.DerivedValue.TK) / 40.0f;

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(3);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);

    sumLabel.setText(Localization.getString("Hauptfenster.AusruestungDoppelpunkt") + format.format(weightStones) //$NON-NLS-1$
        + Localization.getString("Hauptfenster.SteinWaffen") + format.format(weaponWeightStones) //$NON-NLS-1$
        + Localization.getString("Hauptfenster.SteinParade") + format.format(shieldWeightStones) + Localization.getString("Hauptfenster.Stein")); //$NON-NLS-1$ //$NON-NLS-2$
    sumLabel3.setText(Localization.getString("Hauptfenster.RuestungDoppelpunkt") + format.format(armourWeightStones) //$NON-NLS-1$
        + Localization.getString("Hauptfenster.SteinGeld") + format.format(moneyWeightStones) + Localization.getString("Hauptfenster.Stein")); //$NON-NLS-1$ //$NON-NLS-2$
    sumLabel2.setText(Localization.getString("Hauptfenster.Gesamtgewicht") + format.format(overallWeight) //$NON-NLS-1$
        + Localization.getString("Hauptfenster.SteinTragkraft") + format.format(kkWeight) + Localization.getString("Hauptfenster.Stein")); //$NON-NLS-1$ //$NON-NLS-2$
    sumLabel2.setForeground(overallWeight > kkWeight ? Color.RED : DARKGREEN);
  }

  private static final Color DARKGREEN = new Color(0, 175, 0);

  private JPanel fightPanel;

  private JPanel getFightPanel() {
    if (fightPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      weaponsButton = new JToggleButton(Localization.getString("Hauptfenster.Waffen")); //$NON-NLS-1$
      weaponsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new WeaponsFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.weaponsButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private WeaponsFrame frame = null;
      });
      temp.add(weaponsButton);
      frameButtons.put(Localization.getString("Hauptfenster.Waffen"), weaponsButton); //$NON-NLS-1$
      
      armoursButton = new JToggleButton(Localization.getString("Hauptfenster.Ruestungen")); //$NON-NLS-1$
      armoursButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ArmoursFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.armoursButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ArmoursFrame frame = null;
      });
      temp.add(armoursButton);
      frameButtons.put(Localization.getString("Hauptfenster.Ruestungen"), armoursButton); //$NON-NLS-1$
      
      paradeButton = new JToggleButton(Localization.getString("Hauptfenster.Parade")); //$NON-NLS-1$
      paradeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ShieldsFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.paradeButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ShieldsFrame frame = null;
      });
      temp.add(paradeButton);
      frameButtons.put(Localization.getString("Hauptfenster.Parade"), paradeButton); //$NON-NLS-1$
      
      fightButton = new JToggleButton(Localization.getString("Hauptfenster.KampfSpieler")); //$NON-NLS-1$
      fightButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new FightFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.fightButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private FightFrame frame = null;
      });
      temp.add(fightButton);
      frameButtons.put(Localization.getString("Hauptfenster.KampfSpieler"), fightButton); //$NON-NLS-1$
      
      opponentsButton = new JToggleButton(Localization.getString("Hauptfenster.Gegner")); //$NON-NLS-1$
      opponentsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new OpponentsFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.opponentsButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private OpponentsFrame frame = null;
      });
      temp.add(opponentsButton);
      frameButtons.put(Localization.getString("Hauptfenster.Gegner"), opponentsButton); //$NON-NLS-1$
      
      groupFightButton = new JToggleButton(Localization.getString("Hauptfenster.KampfMeister")); //$NON-NLS-1$
      groupFightButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new GroupFightFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.groupFightButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private GroupFightFrame frame = null;
      });
      temp.add(groupFightButton);
      frameButtons.put(Localization.getString("Hauptfenster.KampfMeister"), groupFightButton); //$NON-NLS-1$

      fightPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel(""); //$NON-NLS-1$
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel(""); //$NON-NLS-1$
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      fightPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel(""); //$NON-NLS-1$
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      fightPanel.add(l2, BorderLayout.EAST);
      fightPanel.add(temp2, BorderLayout.CENTER);

    }
    return fightPanel;
  }

  private JToggleButton fightButton;
  
  private JToggleButton opponentsButton;
  
  private JToggleButton groupFightButton;

  private JPanel getThingsPanel() {
    if (thingsPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      thingsButton = new JToggleButton(Localization.getString("Hauptfenster.Ausruestung")); //$NON-NLS-1$
      thingsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ThingsFrame(Group.getInstance().getActiveHero(), Localization.getString("Hauptfenster.Ausruestung")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.thingsButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ThingsFrame frame = null;
      });
      temp.add(thingsButton);
      frameButtons.put(Localization.getString("Hauptfenster.Ausruestung"), thingsButton); //$NON-NLS-1$

      thingsInWarehouseButton = new JToggleButton(Localization.getString("Hauptfenster.Lager")); //$NON-NLS-1$
      thingsInWarehouseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ThingsFrame(Group.getInstance().getActiveHero(), Localization.getString("Hauptfenster.Lager")); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.thingsInWarehouseButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ThingsFrame frame = null;
      });
      temp.add(thingsInWarehouseButton);
      frameButtons.put(Localization.getString("Hauptfenster.Lager"), thingsInWarehouseButton); //$NON-NLS-1$

      moneyButton = new JToggleButton(Localization.getString("Hauptfenster.Geld")); //$NON-NLS-1$
      moneyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MoneyFrame(Localization.getString("Hauptfenster.Geld"), false); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.moneyButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private MoneyFrame frame = null;
      });
      temp.add(moneyButton);
      frameButtons.put(Localization.getString("Hauptfenster.Geld"), moneyButton); //$NON-NLS-1$

      bankMoneyButton = new JToggleButton(Localization.getString("Hauptfenster.Bankkonto")); //$NON-NLS-1$
      bankMoneyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MoneyFrame(Localization.getString("Hauptfenster.Bankkonto"), true); //$NON-NLS-1$
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.bankMoneyButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private MoneyFrame frame = null;
      });
      temp.add(bankMoneyButton);
      frameButtons.put(Localization.getString("Hauptfenster.Bankkonto"), bankMoneyButton); //$NON-NLS-1$

      clothesButton = new JToggleButton(Localization.getString("Hauptfenster.Kleidung")); //$NON-NLS-1$
      clothesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ClothesFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.clothesButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private ClothesFrame frame = null;
      });
      temp.add(clothesButton);
      frameButtons.put(Localization.getString("Hauptfenster.Kleidung"), clothesButton); //$NON-NLS-1$

      JPanel lower = new JPanel();
      lower.setLayout(null);
      sumLabel = new JLabel();
      sumLabel.setBounds(5, 5, 440, 25);
      sumLabel.setForeground(Color.BLUE);
      sumLabel2 = new JLabel();
      sumLabel2.setBounds(5, 50, 440, 25);
      sumLabel2.setForeground(DARKGREEN);
      sumLabel3 = new JLabel();
      sumLabel3.setBounds(5, 25, 440, 25);
      sumLabel3.setForeground(Color.BLUE);
      lower.add(sumLabel);
      lower.add(sumLabel2);
      lower.add(sumLabel3);
      lower.setPreferredSize(new java.awt.Dimension(460, 80));
      thingsPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel(""); //$NON-NLS-1$
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel(""); //$NON-NLS-1$
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      thingsPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel(""); //$NON-NLS-1$
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      thingsPanel.add(l2, BorderLayout.EAST);
      thingsPanel.add(temp2, BorderLayout.CENTER);
      thingsPanel.add(lower, BorderLayout.SOUTH);

      class WeightUpdater extends CharacterAdapter implements OptionsListener {
        public void weightChanged() {
          calcSums();
        }

        public void currentPropertyChanged(Property property) {
          if (property == Property.KK) calcSums();
        }

        public void derivedValueChanged(Hero.DerivedValue dv) {
          if (dv == Hero.DerivedValue.TK) calcSums();
        }
        
        public void thingsChanged() {
          calcSums();
        }

        public void optionsChanged() {
          calcSums();
        }
      }
      class WeightUpdaterRegisterer implements CharactersObserver {

        public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
          if (oldCharacter != null) oldCharacter.removeHeroObserver(updater);
          if (newCharacter != null) newCharacter.addHeroObserver(updater);
          calcSums();
        }

        public void characterRemoved(Hero character) {
        }

        public void characterAdded(Hero character) {
        }

        public void globalLockChanged() {
        }

        WeightUpdater updater = new WeightUpdater();

        public WeightUpdaterRegisterer() {
          Hero hero = Group.getInstance().getActiveHero();
          if (hero != null) hero.addHeroObserver(updater);
          OptionsChange.addListener(updater);
          calcSums();
        }
      }

      Group.getInstance().addObserver(new WeightUpdaterRegisterer());
    }
    return thingsPanel;
  }

  private JPanel othersPanel;

  private JPanel thingsPanel;

  private JToggleButton moneyButton;

  private JToggleButton bankMoneyButton;

  private JToggleButton clothesButton;

  private JToggleButton langButton;

  private JToggleButton bgButton;
  
  private JToggleButton personsButton;

  private JToggleButton imageButton;

  private JToggleButton armoursButton;

  private JToggleButton weaponsButton;

  private JToggleButton paradeButton;

  private JToggleButton thingsButton;

  private JToggleButton thingsInWarehouseButton;

  private JToggleButton energiesButton;

  private JToggleButton goodPropertiesButton;

  private JToggleButton badPropertiesButton;

  private JToggleButton physButton;

  private JComboBox heroBox;

  private JPanel talentsPanel = null;
  
  private JPanel layoutsPanel = null;

  private JMenuBar menuBar = null;

  private JMenu heroesMenu = null;

  private JMenu groupMenu = null;

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getTalentsPanel() {
    if (talentsPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      List<String> talentCategories = Talents.getInstance()
          .getKnownCategories();
      for (String category : talentCategories) {
        JToggleButton button = new JToggleButton(category);
        button.addActionListener(new TalentFrameAction(category, button));
        temp.add(button);
        frameButtons.put(category, button);
      }
      talentsPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel(""); //$NON-NLS-1$
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel(""); //$NON-NLS-1$
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      talentsPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel(""); //$NON-NLS-1$
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      talentsPanel.add(l2, BorderLayout.EAST);
      talentsPanel.add(temp2, BorderLayout.CENTER);
    }
    return talentsPanel;
  }
  
  private class LayoutAction implements ActionListener {
    public LayoutAction(int index) { layout = index; }
    
    public void actionPerformed(ActionEvent e) {
      listenForFrames = false;
      if (!((JToggleButton)e.getSource()).isSelected()) {
        FrameManagement.getInstance().closeAllFrames(ControlFrame.this);
      }
      else {
        FrameLayouts.getInstance().restoreLayout(layout);
      }
      listenForFrames = true;
      rebuildLayoutsPanel();
    }
    
    private int layout;
  }
  
  private void rebuildLayoutsPanel() {
    JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
    
    String[] layouts = FrameLayouts.getInstance().getStoredLayouts();
    String currentLayout = FrameLayouts.getInstance().getCurrentLayout();
    for (int i = 0; i < layouts.length; ++i) {
      JToggleButton button = new JToggleButton(layouts[i]);
      button.addActionListener(new LayoutAction(i));
      temp.add(button);
      button.setSelected(layouts[i].equals(currentLayout));
    }
    
    JPanel temp2 = new JPanel(new BorderLayout());
    JPanel temp3 = new JPanel(new BorderLayout());
    temp3.add(temp, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(temp3);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    JLabel l3 = new JLabel(""); //$NON-NLS-1$
    l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
    temp2.add(l3, BorderLayout.NORTH);
    temp2.add(scrollPane, BorderLayout.CENTER);
    JLabel l4 = new JLabel(Localization.getString("Hauptfenster.LayoutHint")); //$NON-NLS-1$
    l4.setPreferredSize(new java.awt.Dimension(10, 15));
    temp2.add(l4, BorderLayout.SOUTH);

    layoutsPanel.removeAll();
    JLabel l1 = new JLabel(""); //$NON-NLS-1$
    l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
    layoutsPanel.add(l1, BorderLayout.WEST);
    JLabel l2 = new JLabel(""); //$NON-NLS-1$
    l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
    JLabel l5 = new JLabel(""); //$NON-NLS-1$
    l5.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
    layoutsPanel.add(l2, BorderLayout.EAST);
    layoutsPanel.add(l5, BorderLayout.SOUTH);
    layoutsPanel.add(temp2, BorderLayout.CENTER);     
  }
  
  private JPanel getLayoutsPanel() {
    if (layoutsPanel == null) {
      layoutsPanel = new JPanel(new BorderLayout());
    }
    return layoutsPanel;
  }

  private class TalentFrameAction implements ActionListener {
    public TalentFrameAction(String category, JToggleButton button) {
      this.category = category;
      this.button = button;
    }

    public void actionPerformed(ActionEvent e) {
      if (frame != null && frame.isVisible()) {
        frame.dispose();
        frame = null;
      }
      else {
        if (category.startsWith(Localization.getString("Hauptfenster.Berufe"))) //$NON-NLS-1$
          frame = new SpecialTalentsFrame(category);
        else if (category.startsWith(Localization.getString("Hauptfenster.Zauber"))) //$NON-NLS-1$
          frame = new SpellFrame(category);
        else if (category.startsWith(Localization.getString("Hauptfenster.Kampf"))) //$NON-NLS-1$
          frame = new FightingTalentsFrame(category);
        else
          frame = new NormalTalentFrame(category);
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            button.setSelected(false);
          }
        });
        frame.setVisible(true);
      }
    }

    private final String category;

    private final JToggleButton button;

    private TalentFrame frame = null;
  };
  
  /**
   * This method initializes jJMenuBar
   * 
   * @return javax.swing.JMenuBar
   */
  private JMenuBar getJJMenuBar() {
    if (menuBar == null) {
      menuBar = new JMenuBar();
      menuBar.add(getHeroesMenu());
      menuBar.add(getGroupMenu());
      menuBar.add(getExtrasMenu());
      menuBar.add(getWindowMenu());
      menuBar.add(getHelpMenu());
    }
    return menuBar;
  }

  private JMenu getExtrasMenu() {
    if (extrasMenu == null) {
      extrasMenu = new JMenu();
      extrasMenu.setText(Localization.getString("Hauptfenster.MenuExtras")); //$NON-NLS-1$
      extrasMenu.setMnemonic(java.awt.event.KeyEvent.VK_X);
      extrasMenu.add(getThingsExportItem());
      extrasMenu.add(getThingsImportItem());
      extrasMenu.addSeparator();
      extrasMenu.add(getHeroComparisonItem());
      extrasMenu.addSeparator();
      extrasMenu.add(getUpdateCheckItem());
      extrasMenu.add(getOptionsItem());
    }
    return extrasMenu;
  }

  private JMenu extrasMenu;
  
  private JMenu windowMenu;
  
  private JMenu getWindowMenu() {
    if (windowMenu == null) {
      windowMenu = new JMenu();
      windowMenu.setText(Localization.getString("Hauptfenster.MenuFenster")); //$NON-NLS-1$
      windowMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);
      windowMenu.add(getSaveWindowsItem());
      windowMenu.add(getLoadWindowsItem());
      windowMenu.add(getDeleteWindowsItem());
      windowMenu.addSeparator();
      windowMenu.add(getCloseWindowsItem());
    }
    return windowMenu;
  }

  private JMenu getHelpMenu() {
    if (helpMenu == null) {
      helpMenu = new JMenu();
      helpMenu.setText(Localization.getString("Hauptfenster.MenuHilfe")); //$NON-NLS-1$
      helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_I);
      helpMenu.add(getHomepageItem());
      helpMenu.add(getManualItem());
      helpMenu.add(getMailItem());
      helpMenu.addSeparator();
      helpMenu.add(getAboutItem());
    }
    return helpMenu;
  }

  private JMenu helpMenu;

  private JMenuItem optionsItem;

  private JMenuItem homepageItem;

  private JMenuItem manualItem;

  private JMenuItem mailItem;

  private JMenuItem updateCheckItem;
  
  private JMenuItem thingsExportItem;
  
  private JMenuItem thingsImportItem;
  
  private JMenuItem heroComparisonItem;
  
  private JMenuItem getThingsExportItem() {
    if (thingsExportItem == null) {
      thingsExportItem = new JMenuItem();
      thingsExportItem.setText(Localization.getString("Hauptfenster.MenuGegenstandsexport")); //$NON-NLS-1$
      thingsExportItem.setMnemonic(java.awt.event.KeyEvent.VK_X);
      thingsExportItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.exportThings(ControlFrame.this);
        }
      });
      thingsExportItem.setEnabled(false);
    }
    return thingsExportItem;
  }
  
  private JMenuItem getThingsImportItem() {
    if (thingsImportItem == null) {
      thingsImportItem = new JMenuItem();
      thingsImportItem.setText(Localization.getString("Hauptfenster.MenuGegenstandsimport")); //$NON-NLS-1$
      thingsImportItem.setMnemonic(java.awt.event.KeyEvent.VK_I);
      thingsImportItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.importThings(ControlFrame.this);
        }
      });
      thingsImportItem.setEnabled(false);
    }    
    return thingsImportItem;
  }
  
  private JMenuItem getHeroComparisonItem() {
    if (heroComparisonItem == null) {
      heroComparisonItem = new JMenuItem();
      heroComparisonItem.setText(Localization.getString("Hauptfenster.MenuHeldenvergleich")); //$NON-NLS-1$
      heroComparisonItem.setMnemonic(java.awt.event.KeyEvent.VK_V);
      heroComparisonItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.compareHeroes(ControlFrame.this);
        }
      });
      heroComparisonItem.setEnabled(true);
    }
    return heroComparisonItem;
  }

  private JMenuItem getUpdateCheckItem() {
    if (updateCheckItem == null) {
      updateCheckItem = new JMenuItem();
      updateCheckItem.setText(Localization.getString("Hauptfenster.MenuUpdate")); //$NON-NLS-1$
      updateCheckItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
      updateCheckItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OnlineOperations.checkForUpdate(ControlFrame.this, true);
        }
      });
    }
    return updateCheckItem;
  }

  private JMenuItem getMailItem() {
    if (mailItem == null) {
      mailItem = new JMenuItem();
      mailItem.setText(Localization.getString("Hauptfenster.MenuMail")); //$NON-NLS-1$
      mailItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      mailItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OnlineOperations.mailToAuthor(ControlFrame.this);
        }
      });
    }
    return mailItem;
  }

  private JMenuItem getManualItem() {
    if (manualItem == null) {
      manualItem = new JMenuItem();
      manualItem.setText(Localization.getString("Hauptfenster.MenuAnleitung")); //$NON-NLS-1$
      manualItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      manualItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          Desktop desktop = null;
          if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
              desktop = null;
            }
          }
          if (desktop == null) {
            JOptionPane.showMessageDialog(ControlFrame.this, Localization.getString("Hauptfenster.BrowserStartFehler") //$NON-NLS-1$
                + Localization.getString("Hauptfenster.BrowserStartFehler2"), //$NON-NLS-1$
                Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            return;
          }
          try {
            File file = new File(Directories.getApplicationPath() + "hilfe/Start.html"); //$NON-NLS-1$
            URI uri = file.toURI();
            desktop.browse(uri);
          }
          catch (java.net.MalformedURLException ex) {
            ex.printStackTrace();
          }
          catch (IOException ex) {
            JOptionPane.showMessageDialog(ControlFrame.this,
                Localization.getString("Hauptfenster.HilfeStartFehler") //$NON-NLS-1$
                    + ex.getMessage() + Localization.getString("Hauptfenster.HilfeStartFehler2"), //$NON-NLS-1$
                Localization.getString("Hauptfenster.Fehler"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
          }
        }
      });
    }
    return manualItem;
  }

  private JMenuItem getHomepageItem() {
    if (homepageItem == null) {
      homepageItem = new JMenuItem();
      homepageItem.setText(Localization.getString("Hauptfenster.MenuHomepage")); //$NON-NLS-1$
      homepageItem.setMnemonic(java.awt.event.KeyEvent.VK_H);
      homepageItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OnlineOperations.showHomepage(ControlFrame.this);
        }
      });
    }
    return homepageItem;
  }

  private JMenuItem getOptionsItem() {
    if (optionsItem == null) {
      optionsItem = new JMenuItem();
      optionsItem.setText(Localization.getString("Hauptfenster.MenuOptionen")); //$NON-NLS-1$
      optionsItem.setMnemonic(java.awt.event.KeyEvent.VK_O);
      optionsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OptionsDialog dialog = new OptionsDialog(ControlFrame.this);
          dialog.setLocationRelativeTo(ControlFrame.this);
          dialog.setVisible(true);
        }
      });
    }
    return optionsItem;
  }

  private JMenuItem getAboutItem() {
    if (aboutItem == null) {
      aboutItem = new JMenuItem();
      aboutItem.setText(Localization.getString("Hauptfenster.MenuUeber")); //$NON-NLS-1$
      aboutItem.setMnemonic(java.awt.event.KeyEvent.VK_B);
      aboutItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          String text = Localization.getString("Hauptfenster.Version") //$NON-NLS-1$
              + dsa.control.Version.getCurrentVersionString()
              + "\n        von Jörg Rüdenauer\n" + Localization.getString("Hauptfenster.Danke") //$NON-NLS-1$ //$NON-NLS-2$
              + "\n        Frank Willberger" + "\n        Birgit Bucher" //$NON-NLS-1$ //$NON-NLS-2$
              + Localization.getString("Hauptfenster.Betatester") //$NON-NLS-1$
              + Localization.getString("Hauptfenster.SpeziellerDank") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
          JOptionPane.showMessageDialog(ControlFrame.this, text, Localization.getString("Hauptfenster.Ueber"), //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    return aboutItem;
  }

  private JMenuItem aboutItem;
  
  private JMenuItem saveWindowsItem;
  
  private JMenuItem loadWindowsItem;
  
  private JMenuItem deleteWindowsItem;
  
  private JMenuItem getSaveWindowsItem() {
    if (saveWindowsItem == null) {
      saveWindowsItem = new JMenuItem();
      saveWindowsItem.setText(Localization.getString("Hauptfenster.MenuLayoutSpeichern")); //$NON-NLS-1$
      saveWindowsItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      saveWindowsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveWindows();
        }
      });
    }
    return saveWindowsItem;
  }
  
  private void saveWindows() {
    String name = JOptionPane.showInputDialog(this, Localization.getString("Hauptfenster.LayoutName"), Localization.getString("Hauptfenster.Heldenverwaltung"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
    if (name != null) {
      FrameLayouts.getInstance().storeLayout(name);
      getLoadWindowsItem().setEnabled(true);
      getDeleteWindowsItem().setEnabled(true);
      rebuildLayoutsPanel();      
    }
  }
  
  private JMenuItem getLoadWindowsItem() {
    if (loadWindowsItem == null) {
      loadWindowsItem = new JMenuItem();
      loadWindowsItem.setText(Localization.getString("Hauptfenster.MenuLayoutLaden")); //$NON-NLS-1$
      loadWindowsItem.setMnemonic(java.awt.event.KeyEvent.VK_L);
      loadWindowsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          loadWindows();
        }
      });
      loadWindowsItem.setEnabled(FrameLayouts.getInstance().getStoredLayouts().length > 0);
    }
    return loadWindowsItem;
  }
    
  private void loadWindows() {
    String[] layouts = FrameLayouts.getInstance().getStoredLayouts();
    Object layout = JOptionPane.showInputDialog(this, Localization.getString("Hauptfenster.Layoutwaehlen"), Localization.getString("Hauptfenster.Heldenverwaltung"), JOptionPane.PLAIN_MESSAGE, //$NON-NLS-1$ //$NON-NLS-2$
        null, layouts, layouts[0]);
    if (layout != null) {
      for (int i = 0; i < layouts.length; ++i) {
        if (layouts[i] == layout) {
          listenForFrames = false;
          FrameLayouts.getInstance().restoreLayout(i);
          listenForFrames = true;
          rebuildLayoutsPanel();
          break;
        }
      }
    }
  }
  
  private JMenuItem getDeleteWindowsItem() {
    if (deleteWindowsItem == null) {
      deleteWindowsItem = new JMenuItem();
      deleteWindowsItem.setText(Localization.getString("Hauptfenster.MenuLayoutLoeschen")); //$NON-NLS-1$
      deleteWindowsItem.setMnemonic(java.awt.event.KeyEvent.VK_H);
      deleteWindowsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteWindows();
        }
      });
      deleteWindowsItem.setEnabled(FrameLayouts.getInstance().getStoredLayouts().length > 0);
    }
    return deleteWindowsItem;
  }
  
  private void deleteWindows() {
    String[] layouts = FrameLayouts.getInstance().getStoredLayouts();
    Object layout = JOptionPane.showInputDialog(this, Localization.getString("Hauptfenster.Layoutwaehlen2"), Localization.getString("Hauptfenster.Heldenverwaltung"), JOptionPane.PLAIN_MESSAGE, //$NON-NLS-1$ //$NON-NLS-2$
        null, layouts, layouts[0]);
    if (layout != null) {
      for (int i = 0; i < layouts.length; ++i) {
        if (layouts[i] == layout) {
          FrameLayouts.getInstance().deleteLayout(i);
          getDeleteWindowsItem().setEnabled(layouts.length > 1);
          getLoadWindowsItem().setEnabled(layouts.length > 1);
          rebuildLayoutsPanel();
          break;
        }
      }
    }    
  }
  
  private JMenuItem closeWindowsItem;
  
  private JMenuItem getCloseWindowsItem() {
    if (closeWindowsItem == null) {
      closeWindowsItem = new JMenuItem();
      closeWindowsItem.setText(Localization.getString("Hauptfenster.MenuAlleFensterSchliessen")); //$NON-NLS-1$
      closeWindowsItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      closeWindowsItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          FrameManagement.getInstance().closeAllFrames(ControlFrame.this);
        }
      });
    }
    return closeWindowsItem;
  }
  

  /**
   * This method initializes jMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getHeroesMenu() {
    if (heroesMenu == null) {
      heroesMenu = new JMenu();
      heroesMenu.setText(Localization.getString("Hauptfenster.MenuHelden")); //$NON-NLS-1$
      heroesMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
      heroesMenu.add(getNewItem());
      heroesMenu.add(getOpenHeroMenuItem());
      heroesMenu.add(getImportHeroMenuItem());
      heroesMenu.add(getCloneHeroMenuItem());
      heroesMenu.addSeparator();
      heroesMenu.add(getSaveHeroMenuItem());
      heroesMenu.add(getSaveHeroAsMenuItem());
      heroesMenu.add(getSaveAllItem());
      heroesMenu.add(getRemoveHeroMenuItem());
      heroesMenu.addSeparator();
      heroesMenu.add(getPrintMenuItem());
      heroesMenu.addSeparator();
      heroesMenu.add(getQuitMenuItem());
    }
    return heroesMenu;
  }

  private JMenuItem printMenuItem = null;

  /**
   * 
   * @return
   */
  private JMenuItem getPrintMenuItem() {
    if (printMenuItem == null) {
      printMenuItem = new JMenuItem();
      printMenuItem.setText(Localization.getString("Hauptfenster.MenuDrucken")); //$NON-NLS-1$
      printMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
      printMenuItem.setEnabled(false);
      printMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          PrintingDialog dialog = new PrintingDialog(Group.getInstance()
              .getActiveHero(), ControlFrame.this);
          dialog.setVisible(true);
        }
      });
    }
    return printMenuItem;
  }

  private JMenu getGroupMenu() {
    if (groupMenu == null) {
      groupMenu = new JMenu();
      groupMenu.setText(Localization.getString("Hauptfenster.MenuGruppe")); //$NON-NLS-1$
      groupMenu.setMnemonic(java.awt.event.KeyEvent.VK_G);
      buildGroupMenu();
    }
    return groupMenu;
  }

  private void buildGroupMenu() {
    groupMenu.add(getGroupNewItem());
    groupMenu.add(getGroupOpenItem());
    groupMenu.add(getGroupSaveItem());
    groupMenu.add(getGroupSaveAsItem());
    groupMenu.addSeparator();
    groupMenu.add(getGroupPrintItem());
    groupMenu.add(getGroupTimeItem());
    groupMenu.add(getGroupRegionItem());
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.PackageID.class);
    int nrOfLastGroups = prefs.getInt("LastUsedGroupsCount", 0); //$NON-NLS-1$
    if (nrOfLastGroups > 0) groupMenu.addSeparator();
    class LastGroupActionListener implements java.awt.event.ActionListener {
      public LastGroupActionListener(String fileName) {
        this.fileName = fileName;
      }

      public void actionPerformed(java.awt.event.ActionEvent e) {
        inStart = true;
        if (GroupOperations.openGroup(new File(fileName), ControlFrame.this)) {
          getGroupSaveItem().setEnabled(true);
          rebuildLastGroupsMenu();
        }
        inStart = false;
      }

      private final String fileName;
    }
    for (int i = 0; i < nrOfLastGroups; ++i) {
      JMenuItem lastGroupItem = new JMenuItem();
      lastGroupItem.setText((i + 1) + " " //$NON-NLS-1$
          + prefs.get("LastUsedGroupName" + i, "Gruppe " + (i + 1))); //$NON-NLS-1$ //$NON-NLS-2$
      lastGroupItem.setMnemonic(java.awt.event.KeyEvent.VK_1 + i);
      lastGroupItem.addActionListener(new LastGroupActionListener(prefs.get(
          "LastUsedGroupFile" + i, ""))); //$NON-NLS-1$ //$NON-NLS-2$
      groupMenu.add(lastGroupItem);
    }
  }

  private void rebuildLastGroupsMenu() {
    boolean groupSaveEnabled = getGroupSaveItem().isEnabled();
    groupMenu.removeAll();
    buildGroupMenu();
    getGroupSaveItem().setEnabled(groupSaveEnabled);
  }

  private JMenuItem getGroupOpenItem() {
    if (groupOpenItem == null) {
      groupOpenItem = new JMenuItem();
      groupOpenItem.setText(Localization.getString("Hauptfenster.MenuOeffnen")); //$NON-NLS-1$
      groupOpenItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
      groupOpenItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          inStart = true;
          if (GroupOperations.openGroup(ControlFrame.this)) {
            getGroupSaveItem().setEnabled(true);
            rebuildLastGroupsMenu();
          }
          inStart = false;
        }
      });
    }
    return groupOpenItem;
  }

  private JMenuItem getGroupSaveItem() {
    if (groupSaveItem == null) {
      groupSaveItem = new JMenuItem();
      groupSaveItem.setText(Localization.getString("Hauptfenster.MenuSpeichern")); //$NON-NLS-1$
      groupSaveItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      groupSaveItem.setEnabled(false);
      groupSaveItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.saveGroup(ControlFrame.this);
          rebuildLastGroupsMenu();
        }
      });
    }
    return groupSaveItem;
  }

  private JMenuItem getGroupSaveAsItem() {
    if (groupSaveAsItem == null) {
      groupSaveAsItem = new JMenuItem();
      groupSaveAsItem.setText(Localization.getString("Hauptfenster.MenuSpeichernUnter")); //$NON-NLS-1$
      groupSaveAsItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
      groupSaveAsItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (GroupOperations.saveAsGroup(ControlFrame.this)) {
            groupSaveItem.setEnabled(true);
            rebuildLastGroupsMenu();
          }
        }
      });
    }
    return groupSaveAsItem;
  }

  private JMenuItem getGroupNewItem() {
    if (groupNewItem == null) {
      groupNewItem = new JMenuItem();
      groupNewItem.setText(Localization.getString("Hauptfenster.MenuNeu")); //$NON-NLS-1$
      groupNewItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
      groupNewItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.newGroup(ControlFrame.this);
          getGroupSaveItem().setEnabled(false);
        }
      });
    }
    return groupNewItem;
  }

  private JMenuItem getGroupPrintItem() {
    if (groupPrintItem == null) {
      groupPrintItem = new JMenuItem();
      groupPrintItem.setText(Localization.getString("Hauptfenster.MenuDrucken")); //$NON-NLS-1$
      groupPrintItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
      groupPrintItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          PrintingDialog dialog = new PrintingDialog(Group.getInstance(), ControlFrame.this);
          dialog.setVisible(true);
        }
      });
    }
    return groupPrintItem;
  }
  
  private JMenuItem getGroupTimeItem() {
    if (groupTimeItem == null) {
      groupTimeItem = new JMenuItem();
      groupTimeItem.setText(Localization.getString("Hauptfenster.MenuSpielzeit")); //$NON-NLS-1$
      groupTimeItem.setMnemonic(java.awt.event.KeyEvent.VK_Z);
      groupTimeItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.selectGroupTime(ControlFrame.this);
        }
      });
    }
    return groupTimeItem;
  }
  
  private JMenuItem getGroupRegionItem() {
    if (groupZoneItem == null) {
      groupZoneItem = new JMenuItem();
      groupZoneItem.setText(Localization.getString("Hauptfenster.MenuHandelsregion")); //$NON-NLS-1$
      groupZoneItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
      groupZoneItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.selectGroupRegion(ControlFrame.this);
        }
      });
    }
    return groupZoneItem;
  }
  
  /**
   * This method initializes newItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getNewItem() {
    if (newItem == null) {
      newItem = new JMenuItem();
      newItem.setText(Localization.getString("Hauptfenster.MenuNeu2")); //$NON-NLS-1$
      newItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
      newItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.newHero(ControlFrame.this);
        }
      });
    }
    return newItem;
  }

  private JMenuItem newItem = null;

  private JMenuItem openItem = null;

  private JMenuItem saveItem = null;

  private JMenuItem saveAsItem = null;

  private JMenuItem saveAllItem = null;

  private JMenuItem importItem = null;

  private JMenuItem closeItem = null;

  private JMenuItem removeItem = null;

  private JMenuItem cloneItem = null;

  private JMenuItem groupNewItem = null;

  private JMenuItem groupSaveItem = null;

  private JMenuItem groupSaveAsItem = null;

  private JMenuItem groupOpenItem = null;
  
  private JMenuItem groupPrintItem = null;
  
  private JMenuItem groupTimeItem = null;
  
  private JMenuItem groupZoneItem = null;

  /**
   * This method initializes jMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getOpenHeroMenuItem() {
    if (openItem == null) {
      openItem = new JMenuItem();
      openItem.setText(Localization.getString("Hauptfenster.MenuOeffnen")); //$NON-NLS-1$
      openItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
      openItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.openHeroes(ControlFrame.this);
        }
      });
    }
    return openItem;
  }


  /**
   * This method initializes jMenuItem1
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveHeroMenuItem() {
    if (saveItem == null) {
      saveItem = new JMenuItem();
      saveItem.setText(Localization.getString("Hauptfenster.MenuSpeichern")); //$NON-NLS-1$
      saveItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      saveItem.setEnabled(false);
      saveItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.saveHero(Group.getInstance().getActiveHero(), ControlFrame.this);
        }
      });
    }
    return saveItem;
  }

  /**
   * This method initializes jMenuItem2
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveHeroAsMenuItem() {
    if (saveAsItem == null) {
      saveAsItem = new JMenuItem();
      saveAsItem.setText(Localization.getString("Hauptfenster.MenuSpeichernUnter")); //$NON-NLS-1$
      saveAsItem.setEnabled(false);
      saveAsItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
      saveAsItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          if (GroupOperations.saveHeroAs(Group.getInstance().getActiveHero(), ControlFrame.this)) {
            saveItem.setEnabled(true);
          }
        }
      });
    }
    return saveAsItem;
  }

  private JMenuItem getSaveAllItem() {
    if (saveAllItem == null) {
      saveAllItem = new JMenuItem();
      saveAllItem.setText(Localization.getString("Hauptfenster.MenuAlleSpeichern")); //$NON-NLS-1$
      saveAllItem.setMnemonic(java.awt.event.KeyEvent.VK_L);
      saveAllItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.saveAllHeroes(ControlFrame.this);
        }
      });
    }
    return saveAllItem;
  }

  /**
   * This method initializes jMenuItem3
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getImportHeroMenuItem() {
    if (importItem == null) {
      importItem = new JMenuItem();
      importItem.setText(Localization.getString("Hauptfenster.MenuImportieren")); //$NON-NLS-1$
      importItem.setMnemonic(java.awt.event.KeyEvent.VK_I);
      importItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.importHeroes(ControlFrame.this);
        }
      });
    }
    return importItem;
  }

  /**
   * This method initializes jMenuItem4
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getQuitMenuItem() {
    if (closeItem == null) {
      closeItem = new JMenuItem();
      closeItem.setText(Localization.getString("Hauptfenster.MenuBeenden")); //$NON-NLS-1$
      closeItem.setMnemonic(java.awt.event.KeyEvent.VK_B);
      closeItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          saveAndExit();
        }
      });
    }
    return closeItem;
  }

  /**
   * This method initializes jMenuItem5
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getRemoveHeroMenuItem() {
    if (removeItem == null) {
      removeItem = new JMenuItem();
      removeItem.setText(Localization.getString("Hauptfenster.MenuEntfernen")); //$NON-NLS-1$
      removeItem.setEnabled(false);
      removeItem.setMnemonic(java.awt.event.KeyEvent.VK_E);
      removeItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.removeHero(ControlFrame.this);
        }
      });
    }
    return removeItem;
  }

  /**
   * This method initializes jMenuItem6
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getCloneHeroMenuItem() {
    if (cloneItem == null) {
      cloneItem = new JMenuItem();
      cloneItem.setText(Localization.getString("Hauptfenster.MenuKlonen")); //$NON-NLS-1$
      cloneItem.setEnabled(false);
      cloneItem.setMnemonic(java.awt.event.KeyEvent.VK_K);
      cloneItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          GroupOperations.cloneHero(ControlFrame.this);
        }
      });
    }
    return cloneItem;
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    GroupOperations.dragEnter(dtde);
  }

  public void dragOver(DropTargetDragEvent dtde) {
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  public void dragExit(DropTargetEvent dte) {
  }

  public void drop(DropTargetDropEvent dtde) {
    if (GroupOperations.drop(dtde, this)) {
      getGroupSaveItem().setEnabled(true);
      rebuildLastGroupsMenu();
    }
  }

  private void activeCharacterChanged(Hero newCharacter) {
    listenForCharacterBox = false;
    if (newCharacter != null) heroBox.setSelectedItem(newCharacter.getName());
    // newItem.setEnabled(false);
    // openItem.setEnabled(true);
    String path = Group.getInstance().getFilePath(newCharacter);
    saveItem.setEnabled(newCharacter != null && path != null
        && !path.equals("") && !newCharacter.isDifference()); //$NON-NLS-1$
    saveAsItem.setEnabled(newCharacter != null && !newCharacter.isDifference());
    // importItem.setEnabled(true);
    // closeItem.setEnabled(true);
    removeItem.setEnabled(newCharacter != null);
    cloneItem.setEnabled(newCharacter != null && !newCharacter.isDifference());
    getPrintMenuItem().setEnabled(newCharacter != null && !newCharacter.isDifference());
    printHeroButton.setEnabled(newCharacter != null && !newCharacter.isDifference());
    getThingsExportItem().setEnabled(newCharacter != null);
    getThingsImportItem().setEnabled(newCharacter != null);
    listenForCharacterBox = true;
  }
  
  private HashMap<String, JToggleButton> frameButtons = new HashMap<String, JToggleButton>();

  public void openFrame(String name, Rectangle bounds) {
    if (name.equals(getTitle())) {
      setBounds(bounds);
      return;
    }
    JToggleButton button = frameButtons.get(name);
    if (button != null) {
      SubFrame.saveFrameBounds(name, bounds);
      button.doClick();
    }
  }
  
  public void closeFrame(SubFrame frame) {
    String title = frame.getTitle();
    frame.dispose();
    if (frameButtons.containsKey(title)) {
      frameButtons.get(title).setSelected(false);
    }
  }
  
  private boolean listenForFrames = true;
  
  public void frameStateChanged(SubFrame frame) {
    if (listenForFrames) {
      rebuildLayoutsPanel();
    }
  }

} // @jve:decl-index=0:visual-constraint="10,10"

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
package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
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
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import dsa.control.Version;
import dsa.util.Directories;
import dsa.gui.dialogs.AnimalSelectionDialog;
import dsa.gui.dialogs.HeroWizard;
import dsa.gui.dialogs.OptionsDialog;
import dsa.gui.dialogs.PrintingDialog;
import dsa.gui.lf.BGList;
import dsa.gui.util.ImageManager;
import dsa.gui.util.OptionsChange;
import dsa.gui.util.OptionsChange.OptionsListener;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Animal;
import dsa.model.data.Armour;
import dsa.model.data.Armours;
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
import javax.swing.filechooser.FileFilter;

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;

/**
 * 
 */
public final class ControlFrame extends SubFrame implements DropTargetListener {

  private javax.swing.JPanel jContentPane = null;

  private JTabbedPane jTabbedPane = null;

  private JPanel heroPanel = null;

  /**
   * This is the default constructor
   */
  public ControlFrame(boolean loadLastGroup) {
    super("Heldenverwaltung");
    initialize(loadLastGroup);
  }

  private String currentGroupFileName = "";

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
          .get("lastUsedGroupFile", "");
      if (!fileName.equals("")) loadChars(fileName);
      currentGroupFileName = fileName;
      String lastHero = Preferences.userNodeForPackage(dsa.gui.PackageID.class)
          .get("lastActiveHero", "");
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        if (hero.getName().equals(lastHero)) {
          Group.getInstance().setActiveHero(hero);
          break;
        }
      }
      OptionsChange.fireOptionsChanged();
      getGroupSaveItem().setEnabled(!fileName.equals(""));
    }
    if (dropTarget == null) {
      dropTarget = new DropTarget(this, this);
    }
  }

  /**
   * 
   * 
   */
  protected void saveAndExit() {
    if (!autoSaveAll()) return;
    List<SubFrame> openFrames = FrameManagement.getInstance().getOpenFrames();
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    prefs.putInt("OpenFramesCount", openFrames.size());
    Iterator<SubFrame> it = openFrames.iterator();
    for (int i = 0; i < openFrames.size(); ++i) {
      SubFrame frame = it.next();
      // if (frame == this) continue;
      prefs.put("OpenFrames" + i, frame.getTitle());
    }
    prefs.putInt("SelectedPaneIndex", getJTabbedPane().getSelectedIndex());
    prefs.put("lastUsedGroupFile", currentGroupFileName);
    if (Group.getInstance().getActiveHero() != null) {
      prefs
          .put("lastActiveHero", Group.getInstance().getActiveHero().getName());
    }
    String baseDir = Directories.getApplicationPath() + "daten"
        + File.separator;
    try {
      Talents.getInstance().saveUserTalents(baseDir + "Eigene_Talente.dat");
    }
    catch (java.io.IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Talente konnten nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      Talents.getInstance().saveUserSpells(baseDir + "Eigene_Zauber.dat");
    }
    catch (java.io.IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Zauber konnten nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      Things.getInstance().writeUserDefinedThings(
          baseDir + "Eigene_Ausruestung.dat");
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Ausrüstung konnte nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      Weapons.getInstance().storeUserDefinedWeapons(
          baseDir + "Eigene_Waffen.dat");
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Waffen konnten nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      Armours.getInstance().saveUserDefinedArmours(
          baseDir + "Eigene_Ruestungen.dat");
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Rüstungen konnten nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      Shields.getInstance().writeUserDefinedFile(baseDir + "Eigene_Parade.dat");
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this,
          "Benutzerdefinierte Paradehilfen konnten nicht gespeichert werden. Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    try {
      prefs.flush();
    }
    catch (BackingStoreException e) {
      JOptionPane.showMessageDialog(this,
          "Einstellungen konnten nicht gespeichert werden.Grund:\n"
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
    Group.getInstance().setActiveHero(null);
    dispose();
    if (openFrames.size() == 0) {
      System.exit(0);
    }
    else {
      FrameManagement.getInstance().setExiting();
      FrameManagement.getInstance().closeAllFrames(ControlFrame.this);
    }
  }

  private boolean autoSaveAll() {
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      if (hero.isChanged()) {
        int result = JOptionPane.showConfirmDialog(this, "Held '"
            + hero.getName() + "' ist nicht gespeichert.\nJetzt speichern?",
            "Beenden", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION) return false;
        if (result == JOptionPane.YES_OPTION) {
          saveHero(hero);
        }
      }
    }
    if (Group.getInstance().isChanged()) {
      int result = JOptionPane.showConfirmDialog(this,
          "Heldengruppe wurde geändert.\nGruppe speichern?", "Beenden",
          JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) return false;
      if (result == JOptionPane.YES_OPTION) {
        String file = currentGroupFileName;
        if (file.equals("")) {
          boolean saved = saveAsGroup();
          if (!saved) return false;
        }
        else
          saveGroup();
      }
    }
    return true;
  }

  protected void loadChars(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      Group.getInstance().loadFromFile(reader);
    }
    catch (IOException e) {
      return;
    }
  }

  protected boolean shouldFrameOpen(String title) {
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    int frameCount = prefs.getInt("OpenFramesCount", 0);
    for (int i = 0; i < frameCount; ++i) {
      String frameTitle = prefs.get("OpenFrames" + i, "");
      if (frameTitle.equals(title)) return true;
    }
    return false;
  }

  private void copyFile(File first, File second) throws IOException {
    BufferedReader in = new BufferedReader(new FileReader(first));
    try {
      PrintWriter out = new PrintWriter(
          new BufferedWriter(new FileWriter(second)));
      try {
        String s = in.readLine();
        while (s != null) {
          out.println(s);
          s = in.readLine();
        }
        out.flush();
      }
      finally {
        if (out != null) out.close();
      }
    }
    finally {
      if (in != null) in.close();      
    }
  }

  /**
   * 
   * @param hero
   */
  private boolean saveHero(Hero hero) {
    String filePath = Group.getInstance().getFilePath(hero);
    if (filePath == null || filePath.equals("")) return saveHeroAs(hero);
    File tempFile = null;
    try {
      tempFile = File.createTempFile("dsa_" + hero.getName(), null);
      File realFile = new File(filePath);
      hero.storeToFile(tempFile, realFile);
      if (realFile.exists()) realFile.delete();
      if (tempFile.renameTo(realFile) && realFile.exists()) {
        return true;
      }
      else {
        copyFile(tempFile, realFile);
        tempFile.deleteOnExit();
      }
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Fehler beim Speichern:\n"
          + e.getMessage(), "Speichern", JOptionPane.ERROR_MESSAGE);
      if (tempFile != null) tempFile.deleteOnExit();
    }
    return false;
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
              "SelectedPaneIndex", 0));
      jContentPane.add(getAlwaysPane(), java.awt.BorderLayout.NORTH);
    }
    return jContentPane;
  }

  private JPanel alwaysPane = null;

  private JToggleButton stepButton;

  private JToggleButton derivedValuesButton;

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

  private static final class GroupFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".dsagroup") || f.isDirectory();
    }

    public String getDescription() {
      return "DSA-Gruppen (*.dsagroup)";
    }
  }

  private static final class WiegeFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".h42") || f.isDirectory();
    }

    public String getDescription() {
      return "Wiege-Charaktere (*.h42)";
    }
  }

  private static final class HeroFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".dsahero") || f.isDirectory();
    }

    public String getDescription() {
      return "DSA-Charaktere (*.dsahero)";
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
          15, 10));
      newHeroButton = new JButton(ImageManager.getIcon("tsa"));
      newHeroButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          newHero();
        }
      });
      newHeroButton.setToolTipText("Neuen Helden erzeugen");
      alwaysPane.add(newHeroButton);
      heroBox = new JComboBox();
      nameListener = new CharacterNameListener();
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        heroBox.addItem(hero.getName());
        hero.addHeroObserver(nameListener);
      }
      Group.getInstance().addObserver(new CharactersObserver() {
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
        }

        public void globalLockChanged() {
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
      lockButton = new JToggleButton(ImageManager.getIcon("locked"));
      lockButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          lockButton.setIcon(ImageManager
              .getIcon(lockButton.isSelected() ? "unlocked" : "locked"));
          Group.getInstance().setGlobalUnlock(lockButton.isSelected());
        }
      });
      lockButton.setToolTipText("Alles schützen / freigeben");
      alwaysPane.add(lockButton);
    }
    return alwaysPane;
  }

  private JToggleButton lockButton;

  private JButton newHeroButton;

  /**
   * This method initializes jTabbedPane
   * 
   * @return javax.swing.JTabbedPane
   */
  private JTabbedPane getJTabbedPane() {
    if (jTabbedPane == null) {
      jTabbedPane = new JTabbedPane();
      jTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
      jTabbedPane.addTab("Allgemein", null, getHeroPanel(), null);
      jTabbedPane.addTab("Talente", null, getTalentsPanel(), null);
      jTabbedPane.addTab("Kampf", null, getFightPanel(), null);
      jTabbedPane.addTab("Gegenstände", null, getThingsPanel(), null);
      jTabbedPane.addTab("Tiere", null, getAnimalsPanel(), null);
      jTabbedPane.addTab("Sonstiges", null, getOthersPanel(), null);
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
      Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
      if (hero != null) {
        prefs.putInt(hero.getName() + "_AnimalFrameCount", animalFrames.size());
        int nr = 0;
        for (String animal : animalFrames.keySet()) {
          int index = -1;
          for (int i = 0; i < hero.getNrOfAnimals(); ++i) {
            if (hero.getAnimal(i).getName().equals(animal)) {
              index = i; 
              break;
            }
          }
          prefs.putInt(hero.getName() + "_AnimalFrame_" + nr, index);
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
    addAnimalButton.setEnabled(true);
    int nrOfAnimals = hero.getNrOfAnimals();
    for (int i = 0; i < nrOfAnimals; ++i) {
      dsa.model.data.Animal animal = hero.getAnimal(i);
      String entry = animal.getName() + " (" + animal.getCategory() + " )";
      animalsModel.addElement(entry);
    }
    if (nrOfAnimals > 0) {
      animalsList.setSelectedIndex(0);
      editAnimalButton.setEnabled(true);
      deleteAnimalButton.setEnabled(true);
    }
    else {
      editAnimalButton.setEnabled(false);
      deleteAnimalButton.setEnabled(false);
    }
    if (openFrames) {
      Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
      int nrOfOpenFrames = prefs.getInt(hero.getName() + "_AnimalFrameCount", 0);
      for (int i = 0; i < nrOfOpenFrames; ++i) {
        int index = prefs.getInt(hero.getName() + "_AnimalFrame_" + i, -1);
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
        public void thingRemoved(String thing) {
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
      addAnimalButton = new JButton(ImageManager.getIcon("increase"));
      addAnimalButton
          .setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addAnimalButton.setBounds(5, 5, 60, 25);
      addAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addAnimal();
        }
      });
      buttonsPane.add(addAnimalButton, null);
      editAnimalButton = new JButton("...");
      editAnimalButton.setBounds(5, 40, 60, 25);
      editAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          editAnimal();
        }
      });
      buttonsPane.add(editAnimalButton, null);
      deleteAnimalButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      deleteAnimalButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      deleteAnimalButton.setBounds(5, 75, 60, 25);
      deleteAnimalButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          deleteAnimal();
        }
      });
      buttonsPane.add(deleteAnimalButton, null);
      buttonsPane.setPreferredSize(new java.awt.Dimension(70, 80));
      animalsPane.add(buttonsPane, BorderLayout.EAST);
      JLabel filler1 = new JLabel("");
      filler1.setPreferredSize(new java.awt.Dimension(5, 5));
      animalsPane.add(filler1, BorderLayout.NORTH);
      JLabel filler2 = new JLabel("");
      filler2.setPreferredSize(new java.awt.Dimension(5, 5));
      animalsPane.add(filler2, BorderLayout.WEST);
      JLabel filler3 = new JLabel("");
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
      physButton = new JToggleButton("Grunddaten");
      physButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PhysFrame("Grunddaten");
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
      if (shouldFrameOpen("Grunddaten")) {
        physButton.doClick();
      }

      stepButton = new JToggleButton("Erfahrung");
      stepButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new StepFrame("Erfahrung");
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
      if (shouldFrameOpen("Erfahrung")) {
        stepButton.doClick();
      }

      goodPropertiesButton = new JToggleButton("Gute Eigenschaften");
      goodPropertiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PropertyFrame("Gute Eigenschaften", true);
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
      if (shouldFrameOpen("Gute Eigenschaften")) {
        goodPropertiesButton.doClick();
      }

      badPropertiesButton = new JToggleButton("Schlechte Eigenschaften");
      badPropertiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new PropertyFrame("Schlechte Eigenschaften", false);
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
      if (shouldFrameOpen("Schlechte Eigenschaften")) {
        badPropertiesButton.doClick();
      }

      energiesButton = new JToggleButton("Energien");
      energiesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new EnergyFrame("Energien");
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
      if (shouldFrameOpen("Energien")) {
        energiesButton.doClick();
      }

      derivedValuesButton = new JToggleButton("Berechnete Werte");
      derivedValuesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new DerivedValuesFrame("Berechnete Werte");
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
      if (shouldFrameOpen("Berechnete Werte")) {
        derivedValuesButton.doClick();
      }

      heroPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel("");
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      heroPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
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
      langButton = new JToggleButton("Sprachen");
      langButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new LanguageFrame("Sprachen");
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
      if (shouldFrameOpen("Sprachen")) {
        langButton.doClick();
      }

      bgButton = new JToggleButton("Hintergrund");
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
      if (shouldFrameOpen("Hintergrund")) {
        bgButton.doClick();
      }

      ritualsButton = new JToggleButton("Sonderfertigkeiten");
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
      if (shouldFrameOpen("Sonderfertigkeiten")) {
        ritualsButton.doClick();
      }

      magicButton = new JToggleButton("Magie");
      magicButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MagicAttributeFrame("Magie");
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
      if (shouldFrameOpen("Magie")) {
        magicButton.doClick();
      }

      imageButton = new JToggleButton("Bild");
      imageButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ImageFrame("Bild");
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
      if (shouldFrameOpen("Bild")) {
        imageButton.doClick();
      }

      metaButton = new JToggleButton("Metadaten");
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
      if (shouldFrameOpen("Metadaten")) {
        metaButton.doClick();
      }

      othersPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel("");
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      othersPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      othersPanel.add(l2, BorderLayout.EAST);
      othersPanel.add(temp2, BorderLayout.CENTER);
    }
    return othersPanel;
  }

  private javax.swing.JLabel sumLabel, sumLabel2, sumLabel3;

  private void calcSums() {
    long weight = 0;
    Hero currentHero = Group.getInstance().getActiveHero();
    if (currentHero == null) {
      sumLabel.setText("");
      sumLabel3.setText("");
      sumLabel2.setText("Gesamtgewicht: 0 Stein");
      return;
    }
    Things things = Things.getInstance();
    for (String name : currentHero.getThings()) {
      Thing thing = things.getThing(name);
      if (thing != null) {
        weight += (long) thing.getWeight() * currentHero.getThingCount(name);
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

    sumLabel.setText("Ausrüstung: " + format.format(weightStones)
        + " Stein,  Waffen: " + format.format(weaponWeightStones)
        + " Stein, Parade: " + format.format(shieldWeightStones) + " Stein");
    sumLabel3.setText("Rüstung: " + format.format(armourWeightStones)
        + " Stein, Geld: " + format.format(moneyWeightStones) + " Stein");
    sumLabel2.setText("Gesamtgewicht:  " + format.format(overallWeight)
        + " Stein;  Tragkraft: " + format.format(kkWeight) + " Stein");
    sumLabel2.setForeground(overallWeight > kkWeight ? Color.RED : DARKGREEN);
  }

  private static final Color DARKGREEN = new Color(0, 175, 0);

  private JPanel fightPanel;

  private JPanel getFightPanel() {
    if (fightPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      weaponsButton = new JToggleButton("Waffen");
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
      if (shouldFrameOpen("Waffen")) {
        weaponsButton.doClick();
      }
      armoursButton = new JToggleButton("Rüstungen");
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
      if (shouldFrameOpen("Rüstungen")) {
        armoursButton.doClick();
      }
      paradeButton = new JToggleButton("Parade");
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
      if (shouldFrameOpen("Parade")) {
        paradeButton.doClick();
      }
      fightButton = new JToggleButton("Kampf (Spieler)");
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
      if (shouldFrameOpen("Kampf (Spieler)")) {
        fightButton.doClick();
      }
      fightPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel("");
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      fightPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      fightPanel.add(l2, BorderLayout.EAST);
      fightPanel.add(temp2, BorderLayout.CENTER);

    }
    return fightPanel;
  }

  private JToggleButton fightButton;

  private JPanel getThingsPanel() {
    if (thingsPanel == null) {
      JPanel temp = new JPanel(new GridLayout(0, 2, HGAP, VGAP));
      thingsButton = new JToggleButton("Ausrüstung");
      thingsButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new ThingsFrame();
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
      if (shouldFrameOpen("Ausrüstung")) {
        thingsButton.doClick();
      }

      thingsInWarehouseButton = new JToggleButton("Lager");
      thingsInWarehouseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new WarehouseFrame();
            frame.addWindowListener(new WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                ControlFrame.this.thingsInWarehouseButton.setSelected(false);
              }
            });
            frame.setVisible(true);
          }
        }

        private WarehouseFrame frame = null;
      });
      temp.add(thingsInWarehouseButton);
      if (shouldFrameOpen("Lager")) {
        thingsInWarehouseButton.doClick();
      }

      moneyButton = new JToggleButton("Geld");
      moneyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MoneyFrame("Geld", false);
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
      if (shouldFrameOpen("Geld")) {
        moneyButton.doClick();
      }

      bankMoneyButton = new JToggleButton("Bankkonto");
      bankMoneyButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (frame != null && frame.isVisible()) {
            frame.dispose();
            frame = null;
          }
          else {
            frame = new MoneyFrame("Bankkonto", true);
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
      if (shouldFrameOpen("Bankkonto")) {
        bankMoneyButton.doClick();
      }

      clothesButton = new JToggleButton("Kleidung");
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
      if (shouldFrameOpen("Kleidung")) {
        clothesButton.doClick();
      }

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
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel("");
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      thingsPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
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

  private JMenuBar jJMenuBar = null;

  private JMenu jMenu = null;

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
        if (shouldFrameOpen(category)) {
          button.doClick();
        }
      }
      talentsPanel = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JPanel temp3 = new JPanel(new BorderLayout());
      temp3.add(temp, BorderLayout.NORTH);
      JLabel l1 = new JLabel("");
      l1.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      JLabel l3 = new JLabel("");
      l3.setPreferredSize(new java.awt.Dimension(SIDESPACE, VGAP));
      temp2.add(l3, BorderLayout.NORTH);
      temp2.add(temp3, BorderLayout.CENTER);
      talentsPanel.add(l1, BorderLayout.WEST);
      JLabel l2 = new JLabel("");
      l2.setPreferredSize(new java.awt.Dimension(SIDESPACE, SIDESPACE));
      talentsPanel.add(l2, BorderLayout.EAST);
      talentsPanel.add(temp2, BorderLayout.CENTER);
    }
    return talentsPanel;
  }

  private static class TalentFrameAction implements ActionListener {
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
        if (category.startsWith("Berufe"))
          frame = new SpecialTalentsFrame(category);
        else if (category.startsWith("Zauber"))
          frame = new SpellFrame(category);
        else if (category.startsWith("Kampf"))
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

  private void checkForUpdate() {
    String urlS = "http://www.ruedenauer.net/Software/dsa/helden_version.txt";
    try {
      URL url = new URL(urlS);
      BufferedReader in = new BufferedReader(new InputStreamReader(url
          .openStream()));
      try {
        String line = in.readLine();
        Version thisVersion = Version.getCurrentVersion();
        Version serverVersion = Version.parse(line);
        int result = thisVersion.compareTo(serverVersion);
        if (result == -1) {
          if (JOptionPane
              .showConfirmDialog(
                  this,
                  "Eine neuere Version ist verfügbar!\nSoll die Homepage geöffnet werden?",
                  "Heldenverwaltung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            showHomepage();
          }
        }
        else {
          JOptionPane.showMessageDialog(this,
              "Es ist keine neuere Version verfügbar.", "Heldenverwaltung",
              JOptionPane.INFORMATION_MESSAGE);
        }
      }
      finally {
        in.close();
      }
    }
    catch (java.net.MalformedURLException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,
          "Ein Fehler ist bei der Versionsabfrage aufgetreten:\n"
              + e.getMessage(), "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
    }
    catch (java.text.ParseException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this,
          "Ein Fehler ist bei der Versionsabfrage aufgetreten:\n"
              + e.getMessage(), "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
    }

  }

  /**
   * This method initializes jJMenuBar
   * 
   * @return javax.swing.JMenuBar
   */
  private JMenuBar getJJMenuBar() {
    if (jJMenuBar == null) {
      jJMenuBar = new JMenuBar();
      jJMenuBar.add(getJMenu());
      jJMenuBar.add(getGroupMenu());
      jJMenuBar.add(getExtrasMenu());
      jJMenuBar.add(getHelpMenu());
    }
    return jJMenuBar;
  }

  private JMenu getExtrasMenu() {
    if (extrasMenu == null) {
      extrasMenu = new JMenu();
      extrasMenu.setText("Extras");
      extrasMenu.setMnemonic(java.awt.event.KeyEvent.VK_X);
      extrasMenu.add(getOptionsItem());
    }
    return extrasMenu;
  }

  private JMenu extrasMenu;

  private JMenu getHelpMenu() {
    if (helpMenu == null) {
      helpMenu = new JMenu();
      helpMenu.setText("Hilfe");
      helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
      helpMenu.add(getHomepageItem());
      helpMenu.add(getManualItem());
      helpMenu.add(getMailItem());
      helpMenu.add(getUpdateCheckItem());
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

  private JMenuItem getUpdateCheckItem() {
    if (updateCheckItem == null) {
      updateCheckItem = new JMenuItem();
      updateCheckItem.setText("Auf Update überprüfen");
      updateCheckItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
      updateCheckItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          checkForUpdate();
        }
      });
    }
    return updateCheckItem;
  }

  private JMenuItem getMailItem() {
    if (mailItem == null) {
      mailItem = new JMenuItem();
      mailItem.setText("E-Mail an Autor");
      mailItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      mailItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
            Message message = new Message();
            message.setSubject("Heldenverwaltung "
                + dsa.control.Version.getCurrentVersionString());
            java.util.ArrayList<String> tos = new java.util.ArrayList<String>();
            tos.add("joerg@ruedenauer.net");
            message.setToAddrs(tos);
            Desktop.mail(message);
          }
          catch (org.jdesktop.jdic.desktop.DesktopException ex) {
            JOptionPane.showMessageDialog(ControlFrame.this,
                "Die E-Mail kann nicht erstellt werden. Fehler:\n"
                    + ex.getMessage()
                    + "\nBitte schreibe manuell an joerg@ruedenauer.net.",
                "Fehler", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    }
    return mailItem;
  }

  private JMenuItem getManualItem() {
    if (manualItem == null) {
      manualItem = new JMenuItem();
      manualItem.setText("Anleitung");
      manualItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
      manualItem.addActionListener(new ActionListener() {
        String url = "http://www.ruedenauer.net/Software/dsa/helden_anleitung.html";

        public void actionPerformed(ActionEvent e) {
          try {
            Desktop.browse(new java.net.URL(url));
          }
          catch (java.net.MalformedURLException ex) {
            ex.printStackTrace();
          }
          catch (org.jdesktop.jdic.desktop.DesktopException ex) {
            JOptionPane.showMessageDialog(ControlFrame.this,
                "Die Anleitung konnte nicht geöffnet werden. Fehler:\n"
                    + ex.getMessage() + "\nBitte öffne manuell " + url,
                "Fehler", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    }
    return manualItem;
  }

  private JMenuItem getHomepageItem() {
    if (homepageItem == null) {
      homepageItem = new JMenuItem();
      homepageItem.setText("Homepage");
      homepageItem.setMnemonic(java.awt.event.KeyEvent.VK_H);
      homepageItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          showHomepage();
        }
      });
    }
    return homepageItem;
  }

  private void showHomepage() {
    String url = "http://www.ruedenauer.net/Software/dsa/helden.html";
    try {
      Desktop.browse(new java.net.URL(url));
    }
    catch (java.net.MalformedURLException ex) {
      ex.printStackTrace();
    }
    catch (org.jdesktop.jdic.desktop.DesktopException ex) {
      JOptionPane.showMessageDialog(ControlFrame.this,
          "Die Homepage konnte nicht geöffnet werden. Fehler:\n"
              + ex.getMessage() + "\nBitte öffne manuell " + url, "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  private JMenuItem getOptionsItem() {
    if (optionsItem == null) {
      optionsItem = new JMenuItem();
      optionsItem.setText("Optionen...");
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
      aboutItem.setText("Über...");
      aboutItem.setMnemonic(java.awt.event.KeyEvent.VK_B);
      aboutItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          String text = "DSA3-Heldenverwaltung Version "
              + dsa.control.Version.getCurrentVersionString()
              + "\n        von Jörg Rüdenauer\n" + "\nIch danke:"
              + "\n        Frank Willberger" + "\n        Birgit Bucher"
              + "\n        allen weiteren Betatestern\n"
              + "\nSpezieller Dank an Dirk Oz" + "\n";
          JOptionPane.showMessageDialog(ControlFrame.this, text, "Über",
              JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    return aboutItem;
  }

  private JMenuItem aboutItem;

  /**
   * This method initializes jMenu
   * 
   * @return javax.swing.JMenu
   */
  private JMenu getJMenu() {
    if (jMenu == null) {
      jMenu = new JMenu();
      jMenu.setText("Helden");
      jMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
      jMenu.add(getNewItem());
      jMenu.add(getOpenHeroMenuItem());
      jMenu.add(getImportHeroMenuItem());
      jMenu.add(getCloneHeroMenuItem());
      jMenu.addSeparator();
      jMenu.add(getSaveHeroMenuItem());
      jMenu.add(getSaveHeroAsMenuItem());
      jMenu.add(getSaveAllItem());
      jMenu.add(getRemoveHeroMenuItem());
      jMenu.addSeparator();
      jMenu.add(getPrintMenuItem());
      jMenu.addSeparator();
      jMenu.add(getQuitMenuItem());
    }
    return jMenu;
  }

  private JMenuItem printMenuItem = null;

  /**
   * 
   * @return
   */
  private JMenuItem getPrintMenuItem() {
    if (printMenuItem == null) {
      printMenuItem = new JMenuItem();
      printMenuItem.setText("Drucken...");
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
      groupMenu.setText("Gruppe");
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
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.PackageID.class);
    int nrOfLastGroups = prefs.getInt("LastUsedGroupsCount", 0);
    if (nrOfLastGroups > 0) groupMenu.addSeparator();
    class LastGroupActionListener implements java.awt.event.ActionListener {
      public LastGroupActionListener(String fileName) {
        this.fileName = fileName;
      }

      public void actionPerformed(java.awt.event.ActionEvent e) {
        openGroup(new File(fileName));
      }

      private final String fileName;
    }
    for (int i = 0; i < nrOfLastGroups; ++i) {
      JMenuItem lastGroupItem = new JMenuItem();
      lastGroupItem.setText((i + 1) + " "
          + prefs.get("LastUsedGroupName" + i, "Gruppe " + (i + 1)));
      lastGroupItem.setMnemonic(java.awt.event.KeyEvent.VK_1 + i);
      lastGroupItem.addActionListener(new LastGroupActionListener(prefs.get(
          "LastUsedGroupFile" + i, "")));
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
      groupOpenItem.setText("Öffnen...");
      groupOpenItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
      groupOpenItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          openGroup();
        }
      });
    }
    return groupOpenItem;
  }

  /**
   * 
   * 
   */
  protected void openGroup() {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Groups");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new GroupFileFilter());
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      Directories.setLastUsedDirectory(this, "Groups", chooser
          .getSelectedFile());
      openGroup(chooser.getSelectedFile());
    }
  }

  public void openGroup(File file) {
    if (!autoSaveAll()) return;
    try {
      Group.getInstance().removeAllCharacters();
      Group.getInstance()
          .loadFromFile(new BufferedReader(new FileReader(file)));
      currentGroupFileName = file.getAbsolutePath();
      getGroupSaveItem().setEnabled(true);
      updateLastGroups(file.getCanonicalPath(), file.getName());
      OptionsChange.fireOptionsChanged();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Fehler beim Laden: "
          + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateLastGroups(String path, String name) {
    java.util.prefs.Preferences prefs = java.util.prefs.Preferences
        .userNodeForPackage(dsa.gui.PackageID.class);
    int nrOfLastGroups = prefs.getInt("LastUsedGroupsCount", 0);
    if (nrOfLastGroups < 4) nrOfLastGroups++;
    int previousPos = nrOfLastGroups - 1;
    for (int i = nrOfLastGroups - 2; i >= 0; --i) {
      String oldPath = prefs.get("LastUsedGroupFile" + i, "");
      if (oldPath.equals(path)) {
        previousPos = i;
        --nrOfLastGroups;
        break;
      }
    }
    for (int i = previousPos; i > 0; --i) {
      String oldPath = prefs.get("LastUsedGroupFile" + (i - 1), "");
      String oldName = prefs.get("LastUsedGroupName" + (i - 1), "");
      prefs.put("LastUsedGroupFile" + i, oldPath);
      prefs.put("LastUsedGroupName" + i, oldName);
    }
    prefs.put("LastUsedGroupFile" + 0, path);
    prefs.put("LastUsedGroupName" + 0, name);
    prefs.putInt("LastUsedGroupsCount", nrOfLastGroups);
    rebuildLastGroupsMenu();
  }

  private JMenuItem getGroupSaveItem() {
    if (groupSaveItem == null) {
      groupSaveItem = new JMenuItem();
      groupSaveItem.setText("Speichern");
      groupSaveItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      groupSaveItem.setEnabled(false);
      groupSaveItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          saveGroup();
        }
      });
    }
    return groupSaveItem;
  }

  /**
   * 
   * 
   */
  protected boolean saveGroup() {
    try {
      Group.getInstance().writeToFile(
          new PrintWriter(new FileWriter(currentGroupFileName)));
      updateLastGroups(currentGroupFileName, (new File(currentGroupFileName))
          .getName());
      return true;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Fehler beim Speichern: "
          + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private JMenuItem getGroupSaveAsItem() {
    if (groupSaveAsItem == null) {
      groupSaveAsItem = new JMenuItem();
      groupSaveAsItem.setText("Speichern unter...");
      groupSaveAsItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
      groupSaveAsItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          saveAsGroup();
        }
      });
    }
    return groupSaveAsItem;
  }

  /**
   * 
   * 
   */
  protected boolean saveAsGroup() {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Groups");
    if (f != null) chooser.setCurrentDirectory(f);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new GroupFileFilter());
    chooser.setMultiSelectionEnabled(false);
    do {
      int result = chooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        try {
          if (!file.getName().endsWith(".dsagroup"))
            file = new File(file.getCanonicalPath() + ".dsagroup");
          if (file.exists()) {
            result = JOptionPane.showConfirmDialog(this,
                "Datei existiert bereits. Überschreiben?", "Speichern",
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.NO_OPTION) continue;
            if (result == JOptionPane.CANCEL_OPTION) break;
          }
          currentGroupFileName = file.getPath();
          Directories.setLastUsedDirectory(this, "Groups", file);
          groupSaveItem.setEnabled(true);
          return saveGroup();
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Speichern",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      else if (result == JFileChooser.CANCEL_OPTION) break;
    } while (true);
    return false;
  }

  private JMenuItem getGroupNewItem() {
    if (groupNewItem == null) {
      groupNewItem = new JMenuItem();
      groupNewItem.setText("Neu");
      groupNewItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
      groupNewItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          newGroup();
        }
      });
    }
    return groupNewItem;
  }

  /**
   * 
   * 
   */
  public void newGroup() {
    if (!autoSaveAll()) return;
    Group chars = Group.getInstance();
    chars.removeAllCharacters();
    chars.prepareNewGroup();
    getGroupSaveItem().setEnabled(false);
    currentGroupFileName = "";
    OptionsChange.fireOptionsChanged();
  }

  /**
   * This method initializes newItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getNewItem() {
    if (newItem == null) {
      newItem = new JMenuItem();
      newItem.setText("Neu...");
      newItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
      newItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          newHero();
        }
      });
    }
    return newItem;
  }

  /**
   * 
   * 
   */
  protected void newHero() {
    HeroWizard wizard = new HeroWizard(this);
    wizard.setVisible(true);
    Hero hero = wizard.getCreatedHero();
    if (hero != null) {
      Group.getInstance().addHero(hero);
      Group.getInstance().setActiveHero(hero);
      if (!physButton.isSelected()) {
        physButton.doClick();
      }
      else {
        FrameManagement.getInstance().getFrame("Grunddaten").toFront();
      }
    }
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

  /**
   * This method initializes jMenuItem
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getOpenHeroMenuItem() {
    if (openItem == null) {
      openItem = new JMenuItem();
      openItem.setText("Öffnen...");
      openItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
      openItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          openHeroes();
        }
      });
    }
    return openItem;
  }

  /**
   * 
   * 
   */
  protected void openHeroes() {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Heros");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new HeroFileFilter());
    chooser.setMultiSelectionEnabled(true);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File[] files = chooser.getSelectedFiles();
      for (int i = 0; i < files.length; i++) {
        openHero(files[i]);
      }
      if (files.length > 0) {
        Directories.setLastUsedDirectory(this, "Heros", files[0]);
      }
    }
  }

  public void openHero(File file) {
    Hero newCharacter = null;
    try {
      String path = file.getCanonicalPath();
      String dir = path.substring(0, path.lastIndexOf(File.separator));
      java.util.prefs.Preferences.userNodeForPackage(dsa.gui.PackageID.class)
          .put("lastUsedImportPath", dir);
      newCharacter = dsa.model.DataFactory.getInstance().createHeroFromFile(
          file);
      Group.getInstance().addHero(newCharacter);
      Group.getInstance().setFilePath(newCharacter, path);
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Laden von " + file.getName()
          + " fehlgeschlagen!\nFehlermeldung: " + e.getMessage(), "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * This method initializes jMenuItem1
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveHeroMenuItem() {
    if (saveItem == null) {
      saveItem = new JMenuItem();
      saveItem.setText("Speichern");
      saveItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
      saveItem.setEnabled(false);
      saveItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          saveHero();
        }
      });
    }
    return saveItem;
  }

  /**
   * 
   * 
   */
  protected void saveHero() {
    saveHero(Group.getInstance().getActiveHero());
  }

  /**
   * This method initializes jMenuItem2
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getSaveHeroAsMenuItem() {
    if (saveAsItem == null) {
      saveAsItem = new JMenuItem();
      saveAsItem.setText("Speichern unter...");
      saveAsItem.setEnabled(false);
      saveAsItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
      saveAsItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          saveHeroAs();
        }
      });
    }
    return saveAsItem;
  }

  private JMenuItem getSaveAllItem() {
    if (saveAllItem == null) {
      saveAllItem = new JMenuItem();
      saveAllItem.setText("Alle speichern");
      saveAllItem.setMnemonic(java.awt.event.KeyEvent.VK_L);
      saveAllItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveAll();
        }
      });
    }
    return saveAllItem;
  }

  protected void saveAll() {
    for (Hero c : Group.getInstance().getAllCharacters()) {
      saveHero(c);
    }
  }

  /**
   * 
   * 
   */
  protected void saveHeroAs() {
    saveHeroAs(Group.getInstance().getActiveHero());
  }

  /**
   * 
   * @param hero
   */
  private boolean saveHeroAs(Hero hero) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Heros");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new HeroFileFilter());
    chooser.setMultiSelectionEnabled(false);
    chooser.setSelectedFile(new File(hero.getName() + ".dsahero"));
    do {
      int result = chooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        try {
          if (!file.getName().endsWith(".dsahero"))
            file = new File(file.getCanonicalPath() + ".dsahero");
          if (file.exists()) {
            result = JOptionPane.showConfirmDialog(this,
                "Datei existiert bereits. Überschreiben?", "Speichern",
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.NO_OPTION) continue;
            if (result == JOptionPane.CANCEL_OPTION) break;
          }
          Group.getInstance().setFilePath(hero, file.getCanonicalPath());
          Directories.setLastUsedDirectory(this, "Heros", file);
          saveItem.setEnabled(true);
          return saveHero(hero);
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Speichern",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      else if (result == JFileChooser.CANCEL_OPTION) break;
    } while (true);
    return false;
  }

  /**
   * This method initializes jMenuItem3
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getImportHeroMenuItem() {
    if (importItem == null) {
      importItem = new JMenuItem();
      importItem.setText("Importieren...");
      importItem.setMnemonic(java.awt.event.KeyEvent.VK_I);
      importItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          importHeroes();
        }
      });
    }
    return importItem;
  }

  /**
   * 
   * 
   */
  protected void importHeroes() {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Wiege-Files");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new WiegeFileFilter());
    chooser.setMultiSelectionEnabled(true);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File[] files = chooser.getSelectedFiles();
      for (int i = 0; i < files.length; i++) {
        Hero newCharacter = null;
        try {
          String path = files[i].getCanonicalPath();
          String dir = path.substring(0, path.lastIndexOf(File.separator));
          java.util.prefs.Preferences.userNodeForPackage(
              dsa.gui.PackageID.class).put("lastUsedImportPath", dir);
          newCharacter = dsa.model.DataFactory.getInstance()
              .createHeroFromWiege(files[i]);
          Group.getInstance().addHero(newCharacter);
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(this, "Import von "
              + files[i].getName() + " fehlgeschlagen!\nFehlermeldung: "
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
      }
      if (files.length > 0) {
        Directories.setLastUsedDirectory(this, "Wiege-Files", files[0]);
      }
    }
  }

  /**
   * This method initializes jMenuItem4
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getQuitMenuItem() {
    if (closeItem == null) {
      closeItem = new JMenuItem();
      closeItem.setText("Beenden");
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
      removeItem.setText("Entfernen");
      removeItem.setEnabled(false);
      removeItem.setMnemonic(java.awt.event.KeyEvent.VK_E);
      removeItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          removeHero();
        }
      });
    }
    return removeItem;
  }

  /**
   * 
   * 
   */
  protected void removeHero() {
    Hero hero = Group.getInstance().getActiveHero();
    if (hero.isChanged()) {
      int result = JOptionPane.showConfirmDialog(this, "Held '"
          + hero.getName() + "' ist nicht gespeichert.\nJetzt speichern?",
          "Entfernen", JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) return;
      if (result == JOptionPane.YES_OPTION) {
        if (!saveHero(hero)) return;
      }
    }
    Group.getInstance().removeCharacter(hero);

  }

  /**
   * This method initializes jMenuItem6
   * 
   * @return javax.swing.JMenuItem
   */
  private JMenuItem getCloneHeroMenuItem() {
    if (cloneItem == null) {
      cloneItem = new JMenuItem();
      cloneItem.setText("Klonen...");
      cloneItem.setEnabled(false);
      cloneItem.setMnemonic(java.awt.event.KeyEvent.VK_K);
      cloneItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          cloneHero();
        }
      });
    }
    return cloneItem;
  }

  /**
   * 
   * 
   */
  protected void cloneHero() {
    Hero c = Group.getInstance().getActiveHero();
    String newName = JOptionPane.showInputDialog(this,
        "Name des neuen Charakters? ", c.getName());
    if (newName == null) return;
    Hero newChar = dsa.model.DataFactory.getInstance().createHero(c);
    newChar.setName(newName);
    Group.getInstance().addHero(newChar);
  }

  private static java.util.List textURIListToFileList(String data) {
    java.util.List<File> list = new java.util.ArrayList<File>(1);
    for (java.util.StringTokenizer st = new java.util.StringTokenizer(data,
        "\r\n"); st.hasMoreTokens();) {
      String s = st.nextToken();
      if (s.charAt(0) == '#') {
        // the line is a comment (as per the RFC 2483)
        continue;
      }
      try {
        java.net.URI uri = new java.net.URI(s);
        java.io.File file = new java.io.File(uri);
        list.add(file);
      }
      catch (java.net.URISyntaxException e) {
        // malformed URI
        continue;
      }
      catch (IllegalArgumentException e) {
        // the URI is not a valid 'file:' URI
        continue;
      }
    }
    return list;
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    try {
      Transferable tr = dtde.getTransferable();
      List fileList = null;
      DataFlavor uriListFlavor = new DataFlavor(
          "text/uri-list;class=java.lang.String");
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        fileList = (java.util.List) tr
            .getTransferData(DataFlavor.javaFileListFlavor);
      }
      else if (tr.isDataFlavorSupported(uriListFlavor)) {
        String data = (String) tr.getTransferData(uriListFlavor);
        fileList = textURIListToFileList(data);
      }
      if (fileList != null) {
        Iterator iterator = fileList.iterator();
        while (iterator.hasNext()) {
          File f = (File) iterator.next();
          if (f.getName().endsWith(".dsagroup")
              || f.getName().endsWith(".dsahero")
              || f.getName().endsWith(".dsa") || f.getName().endsWith(".grp")) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
            return;
          }
        }
      }

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (UnsupportedFlavorException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    dtde.rejectDrag();
  }

  public void dragOver(DropTargetDragEvent dtde) {
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  public void dragExit(DropTargetEvent dte) {
  }

  public void drop(DropTargetDropEvent dtde) {
    try {
      Transferable tr = dtde.getTransferable();
      List fileList = null;
      DataFlavor uriListFlavor = new DataFlavor(
          "text/uri-list;class=java.lang.String");
      if (dtde.getDropAction() == DnDConstants.ACTION_MOVE) {
        if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) == 0) {
          dtde.rejectDrop();
          return;
        }
      }
      else if (dtde.getDropAction() != DnDConstants.ACTION_COPY) {
        dtde.rejectDrop();
        return;
      }
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        fileList = (java.util.List) tr
            .getTransferData(DataFlavor.javaFileListFlavor);
      }
      else if (tr.isDataFlavorSupported(uriListFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        String data = (String) tr.getTransferData(uriListFlavor);
        fileList = textURIListToFileList(data);
      }
      if (fileList != null) {
        Iterator iterator = fileList.iterator();
        File groupFile = null;
        boolean ok = false;
        while (iterator.hasNext()) {
          File file = (File) iterator.next();
          if (file.getName().endsWith(".dsagroup")
              || file.getName().endsWith(".grp")) {
            groupFile = file;
            ok = true;
            break;
          }
          else if (file.getName().endsWith(".dsahero")
              || file.getName().endsWith(".dsa")) {
            ok = true;
          }
        }
        if (!ok) {
          dtde.rejectDrop();
          return;
        }
        if (groupFile != null) {
          openGroup(groupFile);
        }
        else {
          iterator = fileList.iterator();
          while (iterator.hasNext()) {
            File file = (File) iterator.next();
            if (file.getName().endsWith(".dsahero")
                || file.getName().endsWith(".dsa")) {
              openHero(file);
            }
          }
        }
        dtde.getDropTargetContext().dropComplete(true);
      }
      else {
        System.err.println("Rejected");
        dtde.rejectDrop();
      }
    }
    catch (IOException io) {
      io.printStackTrace();
      dtde.rejectDrop();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
      dtde.rejectDrop();
    }
    catch (UnsupportedFlavorException ufe) {
      ufe.printStackTrace();
      dtde.rejectDrop();
    }
  }

  private void activeCharacterChanged(Hero newCharacter) {
    if (newCharacter != null)
      heroBox.setSelectedItem(newCharacter.getName());
    // newItem.setEnabled(false);
    // openItem.setEnabled(true);
    String path = Group.getInstance().getFilePath(newCharacter);
    saveItem.setEnabled(newCharacter != null && path != null
        && !path.equals(""));
    saveAsItem.setEnabled(newCharacter != null);
    // importItem.setEnabled(true);
    // closeItem.setEnabled(true);
    removeItem.setEnabled(newCharacter != null);
    cloneItem.setEnabled(newCharacter != null);
    getPrintMenuItem().setEnabled(newCharacter != null);
  }

} // @jve:decl-index=0:visual-constraint="10,10"

/*
    Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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
package dsa.model.characters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import dsa.control.Printer;
import dsa.model.DataFactory;
import dsa.model.data.Opponent;
import dsa.model.data.Opponents;
import dsa.util.AbstractObservable;
import dsa.util.Directories;
import dsa.util.FileType;

/**
 * @author joerg
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Group extends AbstractObservable<CharactersObserver> implements Printable {

  private final ArrayList<Hero> characters;

  private final ArrayList<String> filePaths;

  private Hero activeHero = null;

  private boolean changed;

  private final GroupOptions options;
  
  private Opponents opponents;

  private static Group instance = new Group();

  public static Group getInstance() {
    return instance;
  }

  public GroupOptions getOptions() {
    return options;
  }

  public int getNrOfCharacters() {
    return characters.size();
  }

  public Hero getActiveHero() {
    return activeHero;
  }

  public void setActiveHero(Hero hero) {
    Hero oldHero = activeHero;
    if (oldHero == hero) return;
    activeHero = hero;
    for (CharactersObserver observer : observers) {
      observer.activeCharacterChanged(hero, oldHero);
    }
  }

  public Hero getCharacter(int index) {
    return characters.get(index);
  }
  
  public void moveHero(int oldIndex, int newIndex) {
    if (oldIndex == newIndex) return;
    Hero h = characters.get(oldIndex);
    String fp = filePaths.get(oldIndex);
    characters.remove(oldIndex);
    filePaths.remove(oldIndex);
    characters.add(newIndex, h);
    filePaths.add(newIndex, fp);
    for (CharactersObserver observer : observers) {
      if (observer instanceof GroupObserver) {
        ((GroupObserver) observer).orderChanged();
      }
    }
    changed = true;
  }

  public List<Hero> getAllCharacters() {
    return java.util.Collections.unmodifiableList(characters);
  }

  public void removeCharacter(int index) {
    Hero character = characters.get(index);
    characters.remove(index);
    changed = true;
    filePaths.remove(index);
    for (CharactersObserver observer : observers) {
      observer.characterRemoved(character);
    }
    if (activeHero == character) {
      setActiveHero(characters.size() > 0 ? characters.get(0) : null);
    }
  }

  public void removeAllCharacters() {
    ArrayList<Hero> temp = new ArrayList<Hero>();
    for (Hero hero : characters)
      temp.add(hero);
    characters.clear();
    filePaths.clear();
    for (Hero hero : temp)
      for (CharactersObserver observer : observers) {
        observer.characterRemoved(hero);
      }
    setActiveHero(null);
    changed = false;
  }

  public void importHero(File file) throws IOException {
    Hero hero = DataFactory.getInstance().createHeroFromWiege(file);
    characters.add(hero);
    filePaths.add("");
    changed = true;
    for (CharactersObserver observer : observers) {
      observer.characterAdded(hero);
    }
    if (activeHero == null) {
      setActiveHero(hero);
    }
  }

  public void addHero(Hero hero) {
    characters.add(hero);
    filePaths.add("");
    changed = true;
    LinkedList<CharactersObserver> temp = new LinkedList<CharactersObserver>();
    temp.addAll(observers);
    for (CharactersObserver observer : temp) {
      observer.characterAdded(hero);
    }
    if (activeHero == null) {
      setActiveHero(hero);
    }
  }

  public Set<String> getOpponentNames() {
    return opponents.getOpponentNames();
  }
  
  public Opponent getOpponent(String name) {
    return opponents.getOpponent(name);
  }
  
  public Opponent addOpponent(Opponent opponent) {
    int nr = 2;
    Opponent clone = opponent.makeClone();
    while (opponents.getOpponent(clone.getName()) != null) {
      clone.setName(opponent.getName() + " " + nr);
      ++nr;
    }
    opponents.addOpponent(clone);
    return clone;
  }
  
  public void removeOpponent(String name) {
    opponents.removeOpponent(name);
  }
  
  public void replaceOpponent(String name, Opponent newOpponent) {
    opponents.removeOpponent(name);
    opponents.addOpponent(newOpponent);
  }
  
  private Group() {
    super();
    characters = new ArrayList<Hero>();
    filePaths = new ArrayList<String>();
    options = new GroupOptions();
    opponents = new Opponents();
    options.getDefaults();
    options.setChanged(false);
    options.loadCorrectFiles();
  }

  public String getFilePath(Hero hero) {
    for (int i = 0; i < characters.size(); ++i) {
      if (characters.get(i) == hero) return filePaths.get(i);
    }
    return "";
  }

  public void setFilePath(Hero hero, String path) {
    for (int i = 0; i < characters.size(); ++i) {
      if (characters.get(i) == hero) {
        filePaths.set(i, path);
        changed = true;
        break;
      }
    }
    hero.setHasLoadedNewerVersion(false);
  }

  /**
   * @return
   */
  public boolean isChanged() {
    return changed || options.isChanged() || opponents.wasChanged();
  }
  
  private static final int GROUP_VERSION = 5;

  public void writeToFile(java.io.File f) throws java.io.IOException {
    PrintWriter file = new PrintWriter(new FileWriter(f));
    try {
      file.println(GROUP_VERSION); // version
      for (String path : filePaths) {
        if (path != null && !path.equals("")) file.println(path);
      }
      file.println("--");
      // version 2
      file.println(activeHero != null ? activeHero.getName() : "");
      // version 3
      options.writeToFile(file);
      // version 4
      file.println(name);
      file.println(Directories.getRelativePath(printFile, f));
      file.println(Directories.getAbsolutePath(printingTemplate, f));
      file.println(printingFileType.toString());
      // version 5
      opponents.writeToFile(file);
      
      file.println("-End Characters-");
      file.flush();
      changed = false;
    }
    finally {
      if (file != null) file.close();
    }
  }
  
  public void loadFromFile(java.io.File f) throws IOException {
    BufferedReader file = new BufferedReader(new FileReader(f));
    String line = file.readLine();
    testEmpty(line);
    int version = 1;
    try {
      version = Integer.parseInt(line);
      loadedNewerVersion = (version > GROUP_VERSION);
    }
    catch (NumberFormatException e) {
      throw new IOException("Dateiformat falsch");
    }
    line = file.readLine();
    testEmpty(line);
    while (line != null && !line.equals("--")) {
      try {
        Hero hero = DataFactory.getInstance()
            .createHeroFromFile(new File(line));
        characters.add(hero);
        filePaths.add(line);
        LinkedList<CharactersObserver> copiedList = new LinkedList<CharactersObserver>();
        copiedList.addAll(observers);
        for (CharactersObserver observer : copiedList)
          observer.characterAdded(hero);
        line = file.readLine();
        testEmpty(line);
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(null, "Konnte Held in " + line
            + " nicht laden:\n" + e.getMessage(), "Fehler",
            JOptionPane.ERROR_MESSAGE);
        line = file.readLine();
        testEmpty(line);
      }
    }
    line = file.readLine();
    testEmpty(line);
    if (version >= 2) {
      for (Hero hero : characters)
        if (hero.getName().equals(line)) {
          setActiveHero(hero);
          break;
        }
    }
    if (version >= 3) {
      options.readFromFile(file, 0);
    }
    if (version >= 4) {
      line = file.readLine();
      testEmpty(line);
      name = line;
      line = file.readLine();
      testEmpty(line);
      printFile = Directories.getAbsolutePath(line, f);
      line = file.readLine();
      testEmpty(line);
      printingTemplate = Directories.getAbsolutePath(line, f);
      line = file.readLine();
      testEmpty(line);
      printingFileType = FileType.valueOf(line);
    }
    else {
      name = f.getName();
      int index = name.lastIndexOf('.');
      if (index != -1) {
        name = name.substring(0, index);
      }
      printingTemplate = "";
      printFile = "";
      printingFileType = FileType.WordML;
    }
    opponents = new Opponents();
    if (version >= 5) {
      opponents.readFromFile(file, f.getName(), true);
    }
    while (line != null && !line.equals("-End Characters-")) {
      line = file.readLine();
      testEmpty(line);
    }
    options.loadCorrectFiles();
    for (CharactersObserver o : observers) {
      if (o instanceof GroupObserver) {
        ((GroupObserver)o).groupLoaded();
      }
    }
    if (characters.size() > 0 && activeHero == null)
      setActiveHero(characters.get(0));
    currentFileName = f.getAbsolutePath();
  }

  private void testEmpty(String s) throws IOException {
    if (s == null) throw new IOException("Unerwartetes Dateiende!");
  }

  private boolean globalUnlock = false;

  public void setGlobalUnlock(boolean unlock) {
    globalUnlock = unlock;
    for (CharactersObserver o : observers) {
      o.globalLockChanged();
    }
  }

  public boolean getGlobalUnlock() {
    return globalUnlock;
  }

  /**
   * 
   * @param hero
   */
  public void removeCharacter(Hero hero) {
    if (!characters.contains(hero)) return;
    int index = characters.indexOf(hero);
    characters.remove(hero);
    filePaths.remove(index);
    changed = true;
    for (CharactersObserver observer : observers) {
      observer.characterRemoved(hero);
    }
  }

  public void prepareNewGroup() {
    removeAllCharacters();
    opponents = new Opponents();
    options.getDefaults();
    options.loadCorrectFiles();
    name = "";
    printFile = "";
    loadedNewerVersion = false;
    currentFileName = "";
    for (CharactersObserver o : observers) {
      if (o instanceof GroupObserver) {
        ((GroupObserver)o).groupLoaded();
      }
    }
  }
  

  public List<String> getFightingTalentsInDocument() {
    return null;
  }
  
  private String name = "";

  public String getName() {
    return name;
  }
  
  public void setName(String newName) {
    if (!name.equals(newName)) {
      name = newName;
      changed = true;
    }
  }
  
  private String printFile = "";

  public String getPrintFile() {
    return printFile;
  }
  
  public Printer getPrinter() {
    return dsa.control.GroupPrinter.getInstance();
  }
  
  private FileType printingFileType = FileType.ODT;

  public FileType getPrintingFileType() {
    return printingFileType;
  }
  
  private String printingTemplate = "";

  public String getPrintingTemplateFile() {
    return printingTemplate;
  }

  public int getPrintingZFW() {
    return 0;
  }

  public boolean hasPrintingCustomizations() {
    return false;
  }

  public void setFightingTalentsInDocument(List<String> talents) {
  }

  public void setPrintFile(String file) {
    if (!printFile.equals(file)) {
      printFile = file;
      changed = true;
    }
  }

  public void setPrintingFileType(FileType fileType) {
    if (printingFileType != fileType) {
      printingFileType = fileType;
      changed = true;
    }
  }

  public void setPrintingTemplateFile(String filePath) {
    if (!printingTemplate.equals(filePath)) {
      printingTemplate = filePath;
      changed = true;
    }
  }

  public void setPrintingZFW(int zfw) {
  }
  
  private boolean loadedNewerVersion = false;
  
  public boolean hasLoadedNewerVersion() {
    return loadedNewerVersion;
  }
  
  public void setHasLoadedNewerVersion(boolean newer) {
    loadedNewerVersion = newer;
  }
  
  private String currentFileName = "";
  
  public String getCurrentFileName() {
    return currentFileName;
  }
  
  public void setCurrentFileName(String fileName) {
    currentFileName = fileName;
  }

}

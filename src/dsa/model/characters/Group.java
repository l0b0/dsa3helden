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
package dsa.model.characters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import dsa.model.DataFactory;
import dsa.util.AbstractObservable;

/**
 * @author joerg
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Group extends AbstractObservable<CharactersObserver> {

  private final ArrayList<Hero> characters;

  private final ArrayList<String> filePaths;

  private Hero activeHero = null;

  private boolean changed;

  private final GroupOptions options;

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
    for (CharactersObserver observer : observers) {
      observer.characterAdded(hero);
    }
    if (activeHero == null) {
      setActiveHero(hero);
    }
  }

  private Group() {
    super();
    characters = new ArrayList<Hero>();
    filePaths = new ArrayList<String>();
    options = new GroupOptions();
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
  }

  /**
   * @return
   */
  public boolean isChanged() {
    return changed || options.isChanged();
  }

  public void writeToFile(java.io.PrintWriter file) throws java.io.IOException {
    file.println(3); // version
    for (String path : filePaths) {
      if (path != null && !path.equals("")) file.println(path);
    }
    file.println("--");
    file.println(activeHero != null ? activeHero.getName() : "");
    options.writeToFile(file);
    file.println("-End Characters-");
    file.flush();
  }

  public void loadFromFile(java.io.BufferedReader file) throws IOException {
    String line = file.readLine();
    testEmpty(line);
    int version = 1;
    try {
      version = Integer.parseInt(line);
      // if (version > 3) throw new IOException("Version zu hoch!");
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
        for (CharactersObserver observer : observers)
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
    while (line != null && !line.equals("-End Characters-")) {
      line = file.readLine();
      testEmpty(line);
    }
    options.loadCorrectFiles();
    if (characters.size() > 0 && activeHero == null)
      setActiveHero(characters.get(0));
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
    options.getDefaults();
    options.loadCorrectFiles();
  }

}

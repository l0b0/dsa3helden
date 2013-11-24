package dsa.control;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import dsa.gui.dialogs.HeroComparisonDialog;
import dsa.gui.dialogs.HeroWizard;
import dsa.gui.dialogs.ThingsImportDialog;
import dsa.gui.dialogs.TradezoneDialog;
import dsa.gui.dialogs.YearSelectionDialog;
import dsa.gui.frames.FrameManagement;
import dsa.gui.frames.PhysFrame;
import dsa.gui.frames.SubFrame;
import dsa.gui.util.OptionsChange;
import dsa.model.Date;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.Talents;
import dsa.model.talents.Talent;
import dsa.util.Directories;

public final class GroupOperations {
  
  private GroupOperations() {}

  public static final class HeroFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".dsahero") || f.isDirectory();
    }

    public String getDescription() {
      return "DSA-Charaktere (*.dsahero)";
    }
  }

  public static void openHeroes(JFrame parent) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Heros");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new HeroFileFilter());
    chooser.setMultiSelectionEnabled(true);
    int result = chooser.showOpenDialog(parent);
    if (result == JFileChooser.APPROVE_OPTION) {
      File[] files = chooser.getSelectedFiles();
      for (int i = 0; i < files.length; i++) {
        openHero(files[i], parent);
      }
      if (files.length > 0) {
        Directories.setLastUsedDirectory(parent, "Heros", files[0]);
      }
    }
  }

  public static boolean saveHeroAs(Hero hero, JFrame parent) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Heros");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new HeroFileFilter());
    chooser.setMultiSelectionEnabled(false);
    chooser.setSelectedFile(new File(hero.getName() + ".dsahero"));
    do {
      int result = chooser.showSaveDialog(parent);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        try {
          if (!file.getName().endsWith(".dsahero"))
            file = new File(file.getCanonicalPath() + ".dsahero");
          if (file.exists()) {
            result = JOptionPane.showConfirmDialog(parent,
                "Datei existiert bereits. Überschreiben?", "Speichern",
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.NO_OPTION) continue;
            if (result == JOptionPane.CANCEL_OPTION) break;
          }
          Group.getInstance().setFilePath(hero, file.getCanonicalPath());
          Directories.setLastUsedDirectory(parent, "Heros", file);
          return saveHero(hero, parent);
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(parent, e.getMessage(), "Speichern",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      else if (result == JFileChooser.CANCEL_OPTION) break;
    } while (true);
    return false;
  }

  public static void openHero(File file, JFrame parent) {
    Hero newCharacter = null;
    try {
      String path = file.getCanonicalPath();
      String dir = path.substring(0, path.lastIndexOf(File.separator));
      java.util.prefs.Preferences.userNodeForPackage(dsa.gui.PackageID.class)
          .put("lastUsedImportPath", dir);
      newCharacter = dsa.model.DataFactory.getInstance().createHeroFromFile(
          file);
      Group.getInstance().addHero(newCharacter, path);
      Group.getInstance().setActiveHero(newCharacter);
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(parent, "Laden von " + file.getName()
          + " fehlgeschlagen!\nFehlermeldung: " + e.getMessage(), "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public static void compareHeroes(JFrame parent) {
    HeroComparisonDialog dialog = new HeroComparisonDialog(parent);
    dialog.setVisible(true);
    if (dialog.wasClosedByOK()) {
      String file1 = dialog.getFirstHeroFile();
      String file2 = dialog.getSecondHeroFile();
      Hero firstHero = null;
      Hero secondHero = null;
      try {
        firstHero = dsa.model.DataFactory.getInstance().createHeroFromFile(new File(file1));
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(parent, "Konnte Held aus Datei " + file1 + " nicht laden!\nFehlermeldung:\n" 
            + e.getMessage(), "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
        return;
      }
      try {
        secondHero = dsa.model.DataFactory.getInstance().createHeroFromFile(new File(file2));
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(parent, "Konnte Held aus Datei " + file2 + " nicht laden!\nFehlermeldung:\n" 
            + e.getMessage(), "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Hero comparison = dsa.model.DataFactory.getInstance().createHeroDifference(firstHero, secondHero);
      boolean wasChanged = Group.getInstance().isChanged();
      Group.getInstance().addHero(comparison);
      Group.getInstance().setActiveHero(comparison);
      Group.getInstance().setChanged(wasChanged);
      
      String result = "Unterschiede zwischen den Helden / Stufen:\n\n";
      result += comparison.getAP() + " Abenteuerpunkte\n";
      result += comparison.getStep() + " Stufen\n\n";
      result += comparison.getCurrentEnergy(Energy.LE) + " Lebenspunkte\n";
      if (comparison.hasEnergy(Energy.AE)) {
        result += comparison.getCurrentEnergy(Energy.AE) + " Astralpunkte\n";
      }
      if (comparison.hasEnergy(Energy.KE)) {
        result += comparison.getCurrentEnergy(Energy.KE) + " Karmapunkte\n";
      }
      int goodSum = 0; int badSum = 0;
      for (Property property : Property.values()) {
        if (property.isGoodProperty()) goodSum += comparison.getCurrentProperty(property);
        else badSum += comparison.getCurrentProperty(property);
      }
      result += "\n" + goodSum + " gute Eigenschaftspunkte\n";
      result += badSum + " schlechte Eigenschaftspunkte\n\n";
      int talentSum = 0; int spellSum = 0;
      for (String category : Talents.getInstance().getKnownCategories()) {
        if (category.equals("Favoriten")) continue;
        for (Talent talent : Talents.getInstance().getTalentsInCategory(category)) {
          if (comparison.hasTalent(talent.getName())) {
            if (talent.isSpell()) {
              spellSum += comparison.getCurrentTalentValue(talent.getName());
            }
            else {
              talentSum += comparison.getCurrentTalentValue(talent.getName());
            }
          }
        }
      }
      result += talentSum + " Talentpunkte";
      if (comparison.hasEnergy(Energy.AE)) {
        result += "\n" + spellSum + " Zauberfertigkeitspunkte";
      }
      javax.swing.JOptionPane.showMessageDialog(parent, result, "Heldenverwaltung", 
          JOptionPane.PLAIN_MESSAGE);
    }
  }

  private static void copyFile(File first, File second) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(first), "ISO-8859-1"));
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          second), "ISO-8859-1")));
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

  public static boolean saveHero(Hero hero, JFrame parent) {
    String filePath = Group.getInstance().getFilePath(hero);
    if (filePath == null || filePath.equals("")) return saveHeroAs(hero, parent);
    if (hero.hasLoadedNewerVersion()) {
      String warningText = hero.getName() + " wurde von einer neueren Version der Heldenverwaltung gespeichert.\n"
        + "Beim Speichern mit dieser Version können Daten verloren gehen.\n"
        + "Trotzdem speichern?";
      if (JOptionPane.showConfirmDialog(parent, warningText, "Heldenverwaltung", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
        return false;
      }
    }
    File tempFile = null;
    try {
      tempFile = File.createTempFile("dsa_" + hero.getName(), null);
      File realFile = new File(filePath);
      hero.storeToFile(tempFile, realFile);
      if (realFile.exists()) {
        @SuppressWarnings("unused")
        boolean success = realFile.delete();
        // success is actually irrelevant here
      }
      if (tempFile.renameTo(realFile) && realFile.exists()) {
        return true;
      }
      else {
        copyFile(tempFile, realFile);
        tempFile.deleteOnExit();
        return true;
      }
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(parent, "Fehler beim Speichern:\n"
          + e.getMessage(), "Speichern", JOptionPane.ERROR_MESSAGE);
      if (tempFile != null) tempFile.deleteOnExit();
    }
    return false;
  }

  public static void saveAllHeroes(JFrame parent) {
    for (Hero c : Group.getInstance().getAllCharacters()) {
      if (!c.isDifference()) saveHero(c, parent);
    }
  }

  public static void removeHero(JFrame parent) {
    Hero hero = Group.getInstance().getActiveHero();
    if (hero.isChanged()) {
      int result = JOptionPane.showConfirmDialog(parent, "Held '"
          + hero.getName() + "' ist nicht gespeichert.\nJetzt speichern?",
          "Entfernen", JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) return;
      if (result == JOptionPane.YES_OPTION) {
        if (!saveHero(hero, parent)) return;
      }
    }
    boolean isChanged = Group.getInstance().isChanged();
    isChanged = isChanged || !hero.isDifference();
    Group.getInstance().removeCharacter(hero);
    Group.getInstance().setChanged(isChanged);
  }

  public static void newHero(JFrame parent) {
    HeroWizard wizard = new HeroWizard(parent);
    wizard.setVisible(true);
    Hero hero = wizard.getCreatedHero();
    if (hero != null) {
      Group.getInstance().addHero(hero);
      Group.getInstance().setActiveHero(hero);
    }
  }

  public static void cloneHero(JFrame parent) {
    Hero c = Group.getInstance().getActiveHero();
    String newName = JOptionPane.showInputDialog(parent,
        "Name des neuen Charakters? ", c.getName());
    if (newName == null) return;
    Hero newChar = dsa.model.DataFactory.getInstance().createHero(c);
    newChar.setName(newName);
    Group.getInstance().addHero(newChar);
  }

  private static final class GroupFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".dsagroup") || f.isDirectory();
    }

    public String getDescription() {
      return "DSA-Gruppen (*.dsagroup)";
    }
  }

  public static boolean autoSaveAll(JFrame parent) {
    for (Hero hero : Group.getInstance().getAllCharacters()) {
      if (hero.isChanged()) {
        int result = JOptionPane.showConfirmDialog(parent, "Held '"
            + hero.getName() + "' ist nicht gespeichert.\nJetzt speichern?",
            "Beenden", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.CANCEL_OPTION) return false;
        if (result == JOptionPane.YES_OPTION) {
          if (!saveHero(hero, parent)) return false;
        }
      }
    }
    if (Group.getInstance().isChanged()) {
      int result = JOptionPane.showConfirmDialog(parent,
          "Heldengruppe wurde geändert.\nGruppe speichern?", "Beenden",
          JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) return false;
      if (result == JOptionPane.YES_OPTION) {
        String file = Group.getInstance().getCurrentFileName();
        if (file.equals("")) {
          boolean saved = saveAsGroup(parent);
          if (!saved) return false;
        }
        else {
          if (!saveGroup(parent)) return false;
        }
      }
    }
    return true;
  }

  public static boolean saveGroup(JFrame parent) {
    if (Group.getInstance().hasLoadedNewerVersion()) {
      String warningText = Group.getInstance().getName() + " wurde von einer neueren Version der Heldenverwaltung gespeichert.\n"
        + "Beim Speichern mit dieser Version können Daten verloren gehen.\n"
        + "Trotzdem speichern?";
      if (JOptionPane.showConfirmDialog(parent, warningText, "Heldenverwaltung", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
        return false;
      }
    }
    try {
      String fileName = Group.getInstance().getCurrentFileName();
      Group.getInstance().writeToFile(new File(fileName));
      updateLastGroups(fileName, (new File(fileName))
          .getName());
      return true;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(parent, "Fehler beim Speichern der Gruppe: "
          + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  public static boolean saveAsGroup(JFrame parent) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Groups");
    if (f != null) chooser.setCurrentDirectory(f);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new GroupFileFilter());
    chooser.setMultiSelectionEnabled(false);
    do {
      int result = chooser.showSaveDialog(parent);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        try {
          if (!file.getName().endsWith(".dsagroup"))
            file = new File(file.getCanonicalPath() + ".dsagroup");
          if (file.exists()) {
            result = JOptionPane.showConfirmDialog(parent,
                "Datei existiert bereits. Überschreiben?", "Speichern",
                JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.NO_OPTION) continue;
            if (result == JOptionPane.CANCEL_OPTION) break;
          }
          Group.getInstance().setCurrentFileName(file.getPath());
          Group.getInstance().setHasLoadedNewerVersion(false);
          Directories.setLastUsedDirectory(parent, "Groups", file);
          return saveGroup(parent);
        }
        catch (IOException e) {
          JOptionPane.showMessageDialog(parent, e.getMessage(), "Speichern",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      else if (result == JFileChooser.CANCEL_OPTION) break;
    } while (true);
    return false;
  }

  public static boolean openGroup(JFrame parent) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Groups");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new GroupFileFilter());
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(parent);
    if (result == JFileChooser.APPROVE_OPTION) {
      Directories.setLastUsedDirectory(parent, "Groups", chooser
          .getSelectedFile());
      return openGroup(chooser.getSelectedFile(), parent);
    }
    else return false;
  }

  public static boolean openGroup(File file, JFrame parent) {
    if (!autoSaveAll(parent)) return false;
    try {
      Group.getInstance().removeAllCharacters();
      Group.getInstance().loadFromFile(file);
      updateLastGroups(file.getCanonicalPath(), file.getName());
      OptionsChange.fireOptionsChanged();
      return true;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(parent, "Fehler beim Laden: "
          + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private static void updateLastGroups(String path, String name) {
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
  }

  public static void newGroup(JFrame parent) {
    if (!autoSaveAll(parent)) return;
    Group chars = Group.getInstance();
    chars.prepareNewGroup();
    OptionsChange.fireOptionsChanged();
  }
  
  private static final class WiegeFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".h42") || f.isDirectory();
    }

    public String getDescription() {
      return "Wiege-Charaktere (*.h42)";
    }
  }

  public static void importHeroes(JFrame parent) {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Wiege-Files");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.addChoosableFileFilter(new WiegeFileFilter());
    chooser.setMultiSelectionEnabled(true);
    int result = chooser.showOpenDialog(parent);
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
          JOptionPane.showMessageDialog(parent, "Import von "
              + files[i].getName() + " fehlgeschlagen!\nFehlermeldung: "
              + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
      }
      if (files.length > 0) {
        Directories.setLastUsedDirectory(parent, "Wiege-Files", files[0]);
      }
    }
  }

  private static java.util.List<File> textURIListToFileList(String data) {
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

  @SuppressWarnings("unchecked")
  public static void dragEnter(DropTargetDragEvent dtde) {
    try {
      Transferable tr = dtde.getTransferable();
      List<File> fileList = null;
      DataFlavor uriListFlavor = new DataFlavor(
          "text/uri-list;class=java.lang.String");
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        fileList = (java.util.List<File>) tr
            .getTransferData(DataFlavor.javaFileListFlavor);
      }
      else if (tr.isDataFlavorSupported(uriListFlavor)) {
        String data = (String) tr.getTransferData(uriListFlavor);
        fileList = textURIListToFileList(data);
      }
      if (fileList != null) {
        Iterator<File> iterator = fileList.iterator();
        while (iterator.hasNext()) {
          File f = iterator.next();
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


  @SuppressWarnings("unchecked")
  public static boolean drop(DropTargetDropEvent dtde, JFrame parent) {
    boolean retCode = false;
    try {
      Transferable tr = dtde.getTransferable();
      List<File> fileList = null;
      DataFlavor uriListFlavor = new DataFlavor(
          "text/uri-list;class=java.lang.String");
      if (dtde.getDropAction() == DnDConstants.ACTION_MOVE) {
        if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) == 0) {
          dtde.rejectDrop();
          return false;
        }
      }
      else if (dtde.getDropAction() != DnDConstants.ACTION_COPY) {
        dtde.rejectDrop();
        return false;
      }
      if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        fileList = (java.util.List<File>) tr
            .getTransferData(DataFlavor.javaFileListFlavor);
      }
      else if (tr.isDataFlavorSupported(uriListFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        String data = (String) tr.getTransferData(uriListFlavor);
        fileList = textURIListToFileList(data);
      }
      if (fileList != null) {
        Iterator<File> iterator = fileList.iterator();
        File groupFile = null;
        boolean ok = false;
        while (iterator.hasNext()) {
          File file = iterator.next();
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
          return false;
        }
        if (groupFile != null) {
          retCode = openGroup(groupFile, parent);
        }
        else {
          iterator = fileList.iterator();
          while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.getName().endsWith(".dsahero")
                || file.getName().endsWith(".dsa")) {
              GroupOperations.openHero(file, parent);
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
    return retCode;
  }
  
  public static void exportThings(JFrame parent) {
    Hero hero = Group.getInstance().getActiveHero();
    if (hero == null) return;
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Things-Export");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setMultiSelectionEnabled(false);
    int result = JFileChooser.CANCEL_OPTION;
    do {
      result = chooser.showSaveDialog(parent);
      if (result == JFileChooser.CANCEL_OPTION) return;
      if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile().exists()) {
        int ret = JOptionPane.showConfirmDialog(parent, "Datei existiert bereits. Soll sie überschrieben werden?", "Heldenverwaltung", JOptionPane.YES_NO_CANCEL_OPTION);
        if (ret == JOptionPane.CANCEL_OPTION) return;
        if (ret == JOptionPane.NO_OPTION) result = JFileChooser.CANCEL_OPTION;
      }
    }
    while (result == JFileChooser.CANCEL_OPTION);
    if (result == JFileChooser.APPROVE_OPTION) {
      try {
        hero.storeThingsToFile(chooser.getSelectedFile());
        Directories.setLastUsedDirectory(parent, "Things-Export", chooser.getSelectedFile());
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(parent, 
            "Fehler beim Speichern der Daten:\n" + e.getMessage(), 
            "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
      }
    } 
  }
  
  public static void importThings(JFrame parent) {
    Hero hero = Group.getInstance().getActiveHero();
    if (hero == null) return;
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(parent, "Things-Export");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(parent);
    if (result != JFileChooser.APPROVE_OPTION) return;
    ThingsImportDialog dialog = new ThingsImportDialog(parent);
    dialog.setTitle("Gegenstände importieren");
    dialog.setModal(true);
    dialog.setVisible(true);
    if (!dialog.closedByOK()) return;
    long thingTypes = dialog.getSelectedThingTypes();
    try {
      hero.readThingsFromFile(thingTypes, chooser.getSelectedFile());
      Directories.setLastUsedDirectory(parent, "Things-Export", chooser.getSelectedFile());
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(parent, 
          "Fehler beim Laden der Daten:\n" + e.getMessage(), 
          "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
    }
    
  }

  public static void selectGroupTime(JFrame parent) {
    Date currentDate = Group.getInstance().getDate();
    YearSelectionDialog dialog = new YearSelectionDialog(parent, currentDate);
    dialog.setVisible(true);
    YearSelectionDialog.Result result = dialog.getDialogResult();
    if (result == null) return;
    Date newDate = result.getDate();
    if (result.getAction() == YearSelectionDialog.Action.AdaptAges) {
      int yearDifference = newDate.yearDifference(currentDate);
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        hero.setAge(hero.getAge() + yearDifference);
      }
    }
    else if (result.getAction() == YearSelectionDialog.Action.AdaptBirthdays) {
      for (Hero hero : Group.getInstance().getAllCharacters()) {
        int newBirthYear = newDate.getYear() - hero.getAge();
        Date.Era newEra = newDate.getEra();
        if (newBirthYear < 0 && newDate.getEra() == Date.Era.nach) {
          newBirthYear = -newBirthYear;
          newEra = Date.Era.vor;
        }
        Date oldBirthDate = hero.getBirthday();
        Date newBirthDate = new Date(oldBirthDate.getDay(), oldBirthDate.getMonth(),
            newBirthYear, newEra, newDate.getEvent());
        hero.setBirthday(newBirthDate);
      }
    }
    Group.getInstance().setDate(newDate);
    SubFrame baseDataFrame = FrameManagement.getInstance().getFrame("Grunddaten");
    if (baseDataFrame != null) {
      ((PhysFrame)baseDataFrame).groupDateChanged();
    }
  }
  
  public static void selectGroupRegion(JFrame parent) {
    TradezoneDialog dialog = new TradezoneDialog(parent);
    dialog.setVisible(true);
    String newZone = dialog.getSelectedZone();
    if (newZone != null) {
      Group.getInstance().setTradezone(newZone);
    }
  }

}

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
package dsa.gui.frames;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import dsa.gui.util.HelpProvider;
import dsa.util.Directories;

/**
 * 
 */
public abstract class SubFrame extends JFrame implements HelpProvider {

  private static boolean shallSave;

  public static void setSaveLocations(boolean save) {
    shallSave = save;
  }

  private void storeBounds(Rectangle r) {
    PrintWriter out = null;
    try {
      File file = new File(Directories.getApplicationPath() + "daten"
          + File.separator + "allframebounds_"
          + dsa.control.Version.getCurrentVersionString() + ".dat");
      if (file.exists()) {
        out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
      }
      else {
        out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
      }
      String line = getTitle() + ";" + r.x + ";" + r.y + ";" + r.width + ";"
          + r.height;
      out.println(line);
      out.flush();
    }
    catch (IOException e) {
      javax.swing.JOptionPane.showMessageDialog(this,
          "Writing of bounds failed!\n" + e.getMessage());
    }
    finally {
      if (out != null) out.close();
    }
  }

  public static void loadAllBounds(boolean update) throws IOException {
    File file = new File(update ? Directories.getApplicationPath() + "daten"
        + File.separator + "allframebounds_"
        + dsa.control.Version.getCurrentVersionString() + ".dat" : Directories
        .getApplicationPath()
        + "daten" + File.separator + "allframebounds.dat");
    if (!file.exists()) return;
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    BufferedReader in = new BufferedReader(new InputStreamReader(
        new FileInputStream(file), "ISO-8859-1"));
    try {
      String line = in.readLine();
      while (line != null) {
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() != 5) {
          throw new IOException("Falsches Format in bounds-Datei!");
        }
        String title = tokenizer.nextToken();
        int x = Integer.parseInt(tokenizer.nextToken());
        int y = Integer.parseInt(tokenizer.nextToken());
        int w = Integer.parseInt(tokenizer.nextToken());
        int h = Integer.parseInt(tokenizer.nextToken());
        prefs.putInt(title + "x", x);
        prefs.putInt(title + "y", y);
        prefs.putInt(title + "w", w);
        prefs.putInt(title + "h", h);
        line = in.readLine();
      }
    }
    catch (NumberFormatException e) {
      throw new IOException(e.getMessage());
    }
    finally {
      in.close();
    }
    if (!update) loadAllBounds(true);
  }

  public static void saveFrameBounds(String title, Rectangle r) {
    Preferences prefs = Preferences
      .userNodeForPackage(dsa.gui.PackageID.class);
    prefs.putInt(title + "x", r.x);
    prefs.putInt(title + "y", r.y);
    prefs.putInt(title + "w", r.width);
    prefs.putInt(title + "h", r.height);
  }

  public SubFrame() {
    super();
  }

  public SubFrame(String title) {
    super(title);
    setTitle(title);
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    int x = prefs.getInt(title + "x", 50);
    int y = prefs.getInt(title + "y", 50);
    int w = prefs.getInt(title + "w", 420);
    int h = prefs.getInt(title + "h", 100);
    java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit()
        .getScreenSize();
    if (w > screen.width) w = screen.width;
    if (h > screen.height) h = screen.height;
    if (x < 0) x = 0;
    if (y < 0) y = 0;
    if (x + w > screen.width) x = screen.width - w;
    if (y + h > screen.height) y = screen.height - h;
    this.setBounds(x, y, w, h);
    addWindowListener(new WindowAdapter() {
      private void saveBounds() {
        Rectangle r = getBounds();
        String title = getTitle();
        saveFrameBounds(title, r);
        if (shallSave) storeBounds(r);
      }

      public void windowClosing(WindowEvent e) {
        saveBounds();
      }

      public void windowClosed(WindowEvent e) {
        saveBounds();
      }
    });
    this.setIconImage(getIcon().getImage());
    FrameManagement.getInstance().registerFrame(this);
  }
  
  //public String getHelpPage() { return null; }
  
  public final java.awt.Component getHelpParent() { return this; }

  private static ImageIcon getIcon() {
    return dsa.gui.util.ImageManager.getIcon("icon");
  }

  protected JRootPane createRootPane() {
    String rootPaneClass = UIManager.getString("dsa.gui.rootPaneClass");
    if (rootPaneClass == null || rootPaneClass.equals("")) {
      return super.createRootPane();
    }
    else
      try {
        return (JRootPane) Class.forName(rootPaneClass).newInstance();
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
      catch (InstantiationException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
  }

}

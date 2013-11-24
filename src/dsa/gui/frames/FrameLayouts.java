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

import java.awt.Component;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.prefs.Preferences;

public class FrameLayouts {
  
  private static FrameLayouts sInstance = new FrameLayouts();
  
  public static FrameLayouts getInstance() {
    return sInstance;
  }
  
  private static int parseInt(String line) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException();
    }
  }
  
  private static void testEmpty(String line) throws IOException {
    if (line == null || line.length() == 0) throw new IOException();
  }
  
  private static class FrameLayout {
    
    private ArrayList<String> frames = new ArrayList<String>();
    
    private ArrayList<Rectangle> bounds = new ArrayList<Rectangle>();
    
    private String title;
    
    private FrameLayout() {
    }
    
    public boolean isEqualTo(FrameLayout other) {
      // bounds are not relevant here!
      
      if (frames.size() != other.frames.size()) return false;

      java.util.HashSet<String> s1 = new java.util.HashSet<String>(frames);
      java.util.HashSet<String> s2 = new java.util.HashSet<String>(other.frames);
      
      for (String entry : s1) {
        if (!s2.contains(entry)) return false;
      }

      return true;
    }
    
    public void updateBounds() {
      frames.clear();
      bounds.clear();
      Map<Component, Rectangle> positions = FrameManagement.getInstance().getPositions();
      for (Component c : positions.keySet()) {
        if (!(c instanceof SubFrame)) continue;
        SubFrame frame = (SubFrame) c;
        frames.add(frame.getTitle());
        bounds.add(frame.getBounds());
      }
    }
    
    public void discardBounds() {
      bounds.clear();
      for (String frame : frames) {
        bounds.add(SubFrame.getSavedFrameBounds(frame));
      }
    }
    
    public static FrameLayout createFromCurrentLayout(String title) {
      FrameLayout layout = new FrameLayout();
      layout.title = title;
      layout.updateBounds();
      return layout;
    }
    
    public static FrameLayout createFromFrameTitles(String title, ArrayList<String> frames) {
      FrameLayout layout = new FrameLayout();
      layout.title = title;
      for (String frame : frames) {
        layout.frames.add(frame);
        layout.bounds.add(SubFrame.getSavedFrameBounds(frame));
      }
      return layout;
    }
    
    public static FrameLayout createFromPersistency(BufferedReader in, int version) throws IOException {
      FrameLayout layout = new FrameLayout();
      String line = in.readLine();
      testEmpty(line);
      layout.title = line;
      line = in.readLine();
      testEmpty(line);
      int nrOfFrames = parseInt(line);
      for (int i = 0; i < nrOfFrames; ++i) {
        line = in.readLine();
        testEmpty(line);
        layout.frames.add(line);
        line = in.readLine();
        testEmpty(line);
        int x = parseInt(line);
        line = in.readLine();
        testEmpty(line);
        int y = parseInt(line);
        line = in.readLine();
        testEmpty(line);
        int w = parseInt(line);
        line = in.readLine();
        testEmpty(line);
        int h = parseInt(line);
        layout.bounds.add(new Rectangle(x, y, w, h));
      }
      return layout;
    }
    
    public void storeToPersistency(PrintWriter out) {
      out.println(title);
      out.println(frames.size());
      for (int i = 0; i < frames.size(); ++i) {
        out.println(frames.get(i));
        Rectangle r = bounds.get(i);
        out.println(r.x);
        out.println(r.y);
        out.println(r.width);
        out.println(r.height);
      }
    }
    
    public String getTitle() {
      return title;
    }
    
    public void apply() {
      ArrayList<String> copy = new ArrayList<String>(frames);
      java.util.Collections.sort(copy);
      for (SubFrame frame : FrameManagement.getInstance().getOpenFrames()) {
        String title = frame.getTitle();
        if (java.util.Collections.binarySearch(copy, title) < 0) {
          FrameManagement.getInstance().closeFrame(frame);
        }
      }
      for (int i = 0; i < frames.size(); ++i) {
        String title = frames.get(i);
        FrameManagement.getInstance().openFrame(title, bounds.get(i));
      }
    }
  }
  
  private ArrayList<FrameLayout> layouts = new ArrayList<FrameLayout>();
  
  private FrameLayout lastLayout = null;
  
  private String currentLayout = "";
  
  public String getCurrentLayout() {
    return currentLayout;
  }

  public void storeLayout(String name) {
    FrameLayout layout = FrameLayout.createFromCurrentLayout(name);
    currentLayout = name;
    for (int i = 0; i < layouts.size(); ++i) {
      if (layouts.get(i).getTitle().equals(name)) {
        layouts.set(i, layout);
        return;
      }
    }
    layouts.add(layout);
  }
  
  public void saveCurrentLayout() {
    for (int i = 0; i < layouts.size(); ++i) {
      if (layouts.get(i).getTitle().equals(currentLayout)) {
        layouts.get(i).updateBounds();
      }
    }
  }
  
  public String[] getStoredLayouts() {
    ArrayList<String> layoutNames = new ArrayList<String>();
    for (FrameLayout layout : layouts) {
      layoutNames.add(layout.getTitle());
    }
    String[] result = new String[layoutNames.size()];
    layoutNames.toArray(result);
    return result;
  }
  
  public void restoreLayout(int index) {
    if (index >= 0 && index < layouts.size()) {
      saveCurrentLayout();
      layouts.get(index).apply();
      currentLayout = layouts.get(index).getTitle();
    }
  }
  
  public void deleteLayout(int index) {
    if (index >= 0 && index < layouts.size()) {
      if (currentLayout.equals(layouts.get(index).getTitle())) {
        currentLayout = "";
      }
      layouts.remove(index);
    }
  }
  
  private void openFrameForBackwardsCompatibility(String title) {
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    int x = prefs.getInt(title + "x", 50);
    int y = prefs.getInt(title + "y", 50);
    int w = prefs.getInt(title + "w", 420);
    int h = prefs.getInt(title + "h", 100);
    Rectangle bounds = new Rectangle(x, y, w, h);
    FrameManagement.getInstance().openFrame(title, bounds);
  }
  
  public void findCurrentLayout() {
    FrameLayout current = FrameLayout.createFromCurrentLayout("Temp");
    for (int i = 0; i < layouts.size(); ++i) {
      if (layouts.get(i).isEqualTo(current)) {
        currentLayout = layouts.get(i).getTitle();
        return;
      }
    }
    currentLayout = "";
  }
  
  public void restoreLastLayout() {
    if (lastLayout != null) {
      lastLayout.apply();
    }
    else {
      // compatibility
      Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
      int frameCount = prefs.getInt("OpenFramesCount", 0);
      for (int i = 0; i < frameCount; ++i) {
        String title = prefs.get("OpenFrames" + i, "");
        openFrameForBackwardsCompatibility(title);
      }      
      openFrameForBackwardsCompatibility("Heldenverwaltung");
    }
    findCurrentLayout();
  }
  
  private static String getLocalHostName()
  {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e) {
      return "Unknown";
    }
  }
  
  public static String getDefaultLayoutsFilename()
  {
    return dsa.util.Directories.getUserDataPath() + "Fensterlayout_" 
      + getLocalHostName() + ".dat";
  }
  
  public static String getOldLayoutsFilename()
  {
    return dsa.util.Directories.getUserHomePath() + "Fensterlayout.dat";
  }

  public void storeToFile(String filename) throws IOException {
    saveCurrentLayout();
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
    try {
      out.println(1); // version
      out.println(layouts.size());
      for (int i = 0; i < layouts.size(); ++i) {
        layouts.get(i).storeToPersistency(out);
      }
      lastLayout = FrameLayout.createFromCurrentLayout("lastLayout");
      lastLayout.storeToPersistency(out);
      out.flush();
    }
    finally {
      if (out != null) out.close();
    }
  }
  
  public void readDefaultLayouts(String fileName) throws IOException {
    layouts.clear();
    
    File file = new File(fileName);
    if (!file.exists()) return;
    
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      String title = in.readLine();
      while (title != null && !"".equals(title)) {
        ArrayList<String> frames = new ArrayList<String>();
        String line = in.readLine();
        while (line != null && !"".equals(line)) {
          frames.add(line);
          line = in.readLine();
        }
        layouts.add(FrameLayout.createFromFrameTitles(title, frames));
        title = in.readLine();
      }
    }
    finally {
      if (in != null) in.close();
    }
    
  }
  
  public void readFromFile(String filename/*, boolean secondStart*/) throws IOException {
    layouts.clear();
    File file = new File(filename);
    if (!file.exists()) return;
    BufferedReader in = new BufferedReader(new FileReader(file));
    try {
      String line = in.readLine();
      testEmpty(line);
      int version = parseInt(line);
      line = in.readLine();
      int nrOfLayouts = parseInt(line);
      for (int i = 0; i < nrOfLayouts; ++i) {
        FrameLayout layout = FrameLayout.createFromPersistency(in, version);
        layouts.add(layout);
      }
      lastLayout = FrameLayout.createFromPersistency(in, version);
      // if (!secondStart) lastLayout.discardBounds();
    }
    finally {
      if (in != null) in.close();
    }
  }
}

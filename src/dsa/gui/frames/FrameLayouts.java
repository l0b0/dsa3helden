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
import java.util.ArrayList;
import java.util.Map;
import java.util.prefs.Preferences;

public class FrameLayouts {
  
  private static FrameLayouts sInstance = new FrameLayouts();
  
  public static FrameLayouts getInstance() {
    return sInstance;
  }
  
  public static int parseInt(String line) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException();
    }
  }
  
  public static void testEmpty(String line) throws IOException {
    if (line == null || line.length() == 0) throw new IOException();
  }
  
  private static class FrameLayout {
    
    private ArrayList<String> frames = new ArrayList<String>();
    
    private ArrayList<Rectangle> bounds = new ArrayList<Rectangle>();
    
    private String title;
    
    private FrameLayout() {
    }
    
    
    public static FrameLayout createFromCurrentLayout(String title) {
      FrameLayout layout = new FrameLayout();
      layout.title = title;
      Map<Component, Rectangle> positions = FrameManagement.getInstance().getPositions();
      for (Component c : positions.keySet()) {
        if (!(c instanceof SubFrame)) continue;
        SubFrame frame = (SubFrame) c;
        layout.frames.add(frame.getTitle());
        layout.bounds.add(frame.getBounds());
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

  public void storeLayout(String name) {
    FrameLayout layout = FrameLayout.createFromCurrentLayout(name);
    layouts.add(layout);
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
      layouts.get(index).apply();
    }
  }
  
  public void deleteLayout(int index) {
    if (index >= 0 && index < layouts.size()) {
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
  }
  
  public void storeToFile(String filename) throws IOException {
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
  
  public void readFromFile(String filename) throws IOException {
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
    }
    finally {
      if (in != null) in.close();
    }
  }
}

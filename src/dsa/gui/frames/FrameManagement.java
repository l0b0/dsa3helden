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

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * 
 */
public class FrameManagement {

  private final class WindowSnapper extends ComponentAdapter {

    boolean ud, lr;

    boolean doSnap = true;

    private void snap(Component frame, Component other) {
      if (frame == other) return;
      int threshold = 10;
      Rectangle coords = frame.getBounds();
      Rectangle otherCoords = other.getBounds();
      // check whether right/left might be snapped
      if ((coords.y >= otherCoords.y && coords.y <= otherCoords.y
          + otherCoords.height)
          || (coords.y + coords.height >= otherCoords.y && coords.y <= otherCoords.y)) {
        // snap right border
        if (Math.abs(coords.x + coords.width - otherCoords.x) < threshold) {
          if (!lr) coords.x = otherCoords.x - coords.width;
          lr = true;
        }
        // snap left border
        else if (Math.abs(coords.x - (otherCoords.x + otherCoords.width)) < threshold) {
          if (!lr) coords.x = otherCoords.x + otherCoords.width;
          lr = true;
        }
      }
      // check whether top/bottom might be snapped
      if ((coords.x >= otherCoords.x && coords.x <= otherCoords.x
          + otherCoords.width)
          || (coords.x <= otherCoords.x && coords.x + coords.width >= otherCoords.x)) {
        // snap upper border
        if (Math.abs(coords.y - (otherCoords.y + otherCoords.height)) < threshold) {
          if (!ud) coords.y = otherCoords.y + otherCoords.height;
          ud = true;
        }
        // snap lower border
        else if (Math.abs(coords.y + coords.height - otherCoords.y) < threshold) {
          if (!ud) coords.y = otherCoords.y - coords.height;
          ud = true;
        }
      }
      frame.setBounds(coords);
    }

    private void snapComponent(Component c) {
      lr = false;
      ud = false;
      doSnap = false;
      for (SubFrame other : frames) {
        snap(c, other);
      }
      snap(c, mainFrame);
      doSnap = true;
    }

    public void componentMoved(ComponentEvent e) {
      if (doSnap) {
        Rectangle oldPos = positions.get(e.getComponent());
        Rectangle newPos = e.getComponent().getBounds();
        if (newPos.equals(oldPos)) return;
        snapComponent(e.getComponent());
        positions.put(e.getComponent(), e.getComponent().getBounds());
      }
    }

    public void componentResized(ComponentEvent e) {
      // if (doSnap) snapComponent(e.getComponent());
      positions.put(e.getComponent(), e.getComponent().getBounds());
    }
  }

  private static final class ApplicationCloser implements Runnable {
    public void run() {
      System.exit(0);
    }
  }

  private static FrameManagement instance = new FrameManagement();

  public static FrameManagement getInstance() {
    return instance;
  }

  private SubFrame mainFrame;

  private FrameManagement() {
    frames = new java.util.LinkedList<SubFrame>();
    positions = new HashMap<Component, Rectangle>();
    windowSnapper = new WindowSnapper();
  }

  private final java.util.LinkedList<SubFrame> frames;
  private HashMap<Component, Rectangle> positions;

  private final ComponentListener windowSnapper;

  private boolean isExiting = false;

  public void setExiting() {
    isExiting = true;
  }

  public java.util.List<SubFrame> getOpenFrames() {
    return frames;
  }

  public void registerFrame(SubFrame frame) {
    frame.addComponentListener(windowSnapper);
    positions.put(frame, frame.getBounds());
    if (frame instanceof ControlFrame) {
      mainFrame = frame;
      return;
    }
    frames.add(frame);
    frame.addWindowListener(new WindowAdapter() {
      private void frameClosed(WindowEvent e) {
        frames.remove(e.getSource());
        positions.remove(e.getComponent());
        if (frames.size() == 0 && isExiting) {
          javax.swing.SwingUtilities.invokeLater(new ApplicationCloser());
        }
      }

      public void windowClosing(WindowEvent e) {
        frameClosed(e);
      }

      public void windowClosed(WindowEvent e) {
        frameClosed(e);
      }
    });
  }

  public void closeAllFrames(SubFrame caller) {
    for (SubFrame frame : frames)
      if (frame != null && frame.isVisible() && frame != caller)
        frame.dispose();
  }

  public void iconifyAllFrames(SubFrame caller) {
    for (SubFrame frame : frames)
      if (frame != null && frame.isVisible() && frame != caller) {
        frame.setState(java.awt.Frame.ICONIFIED);
      }
  }

  public SubFrame getFrame(String title) {
    for (SubFrame frame : frames)
      if (frame.getTitle().equals(title)) return frame;
    return null;
  }

  public void deiconifyAllFrames(SubFrame caller) {
    for (SubFrame frame : frames)
      if (frame != null && frame != caller)
        frame.setState(java.awt.Frame.NORMAL);
  }

  public void activateAllFrames(SubFrame caller) {
    Preferences prefs = Preferences.userNodeForPackage(dsa.gui.PackageID.class);
    if (prefs.getBoolean("BringWindowsToTop", true)) {
      for (SubFrame frame : frames)
        if (frame != null && frame.isVisible() && frame != caller) {
          frame.toFront();
        }
      if (caller != null) caller.toFront();
    }
  }
}
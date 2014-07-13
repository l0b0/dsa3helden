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
package dsa.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollablePicture extends JLabel implements Scrollable,
    MouseMotionListener {

  private int maxUnitIncrement = 1;

  private final boolean missingPicture;

  public ScrollablePicture(ImageIcon i, int m) {
    super(i);
    if (i == null) {
      missingPicture = true;
      setText("Kein Bild ausgewählt");
      setHorizontalAlignment(CENTER);
      setOpaque(true);
      setBackground(Color.white);
    }
    else missingPicture = false;
    maxUnitIncrement = m;

    // Let the user scroll by dragging to outside the window.
    setAutoscrolls(true); // enable synthetic drag events
    addMouseMotionListener(this); // handle mouse drags
  }

  // Methods required by the MouseMotionListener interface:
  public void mouseMoved(MouseEvent e) {
    // no action necessary
  }

  public void mouseDragged(MouseEvent e) {
    // The user is dragging us, so scroll!
    Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
    scrollRectToVisible(r);
  }

  public Dimension getPreferredSize() {
    if (missingPicture) {
      return new Dimension(300, 380);
    }
    else {
      return super.getPreferredSize();
    }
  }

  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
      int direction) {
    // Get the current position.
    int currentPosition = 0;
    if (orientation == SwingConstants.HORIZONTAL) {
      currentPosition = visibleRect.x;
    }
    else {
      currentPosition = visibleRect.y;
    }

    // Return the number of pixels between currentPosition
    // and the nearest tick mark in the indicated direction.
    if (direction < 0) {
      int newPosition = currentPosition - (currentPosition / maxUnitIncrement)
          * maxUnitIncrement;
      return (newPosition == 0) ? maxUnitIncrement : newPosition;
    }
    else {
      return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement
          - currentPosition;
    }
  }

  public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect,
      int orientation, int direction) {
    if (orientation == javax.swing.SwingConstants.HORIZONTAL) {
      return visibleRect.width - maxUnitIncrement;
    }
    else {
      return visibleRect.height - maxUnitIncrement;
    }
  }

  public boolean getScrollableTracksViewportWidth() {
    return false;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public void setMaxUnitIncrement(int pixels) {
    maxUnitIncrement = pixels;
  }

}

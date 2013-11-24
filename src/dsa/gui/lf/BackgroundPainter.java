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
package dsa.gui.lf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

import javax.swing.ImageIcon;
//import javax.swing.JComboBox;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;

import de.javasoft.plaf.synthetica.painter.SyntheticaPainter;

public class BackgroundPainter extends SyntheticaPainter {
  
  private static class BGImageHolder {
    private static final Image IMAGE = loadImage("bg");
  }

  private static Image getBGImage() {
    return BGImageHolder.IMAGE;
  }

  // public void paintButtonBackground(SynthContext context, Graphics g, int x,
  // int y, int w, int h) {
  // paintBackground(context, g, x, y, w, h, GetBGImage());
  // }

  private static class InactiveImageHolder {
    private static final Image IMAGE = loadImage("hout");
  }
  
  private static Image getInactiveImage() {
    return InactiveImageHolder.IMAGE;
  }

  private static class HoverImageHolder {
    private static final Image IMAGE = loadImage("hout-hover");
  }
  
  private static Image getHoverImage() {
    return HoverImageHolder.IMAGE;
  }

  private static class PressedImageHolder {
    private static final Image IMAGE = loadImage("hout-active");
  }
  
  private static Image getPressedImage() {
    return PressedImageHolder.IMAGE;
  }

  private static class InactiveVertImageHolder {
    private static final Image IMAGE = loadImage("hout-vert");
  }
  

  private static Image getInactiveVertImage() {
    return InactiveVertImageHolder.IMAGE;
  }

  private static class HoverVertImageHolder {
    private static final Image IMAGE = loadImage("hout-vert-hover");
  }
  
  private static Image getHoverVertImage() {
    return HoverVertImageHolder.IMAGE;
  }

  private static class PressedVertImageHolder {
    private static final Image IMAGE = loadImage("hout-vert-active");
  }
  

  private static Image getPressedVertImage() {
    return PressedVertImageHolder.IMAGE;
  }

  public void paintScrollBarThumbBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h, int orientation) {
    JScrollBar bar = (JScrollBar) context.getComponent();
    Image image = null;
    if (orientation == JScrollBar.VERTICAL) {
      if (bar.getValueIsAdjusting()) {
        image = getPressedVertImage();
      }
      else if ((context.getComponentState() & SynthConstants.MOUSE_OVER) != 0) {
        image = getHoverVertImage();
      }
      else {
        image = getInactiveVertImage();
      }
    }
    else {
      if (bar.getValueIsAdjusting()) {
        image = getPressedImage();
      }
      else if ((context.getComponentState() & SynthConstants.MOUSE_OVER) != 0) {
        image = getHoverImage();
      }
      else {
        image = getInactiveImage();
      }
    }
    paintBackground(context, g, x, y, w, h, image);
  }

  public void paintArrowButtonBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h) {
    java.awt.Container c = context.getComponent().getParent();
    if ((c instanceof JSpinner)
    /* (c instanceof JComboBox) */) {
      super.paintArrowButtonBackground(context, g, x, y, w, h);
    }
    else {
      paintBtnBackground(context, g, x, y, w, h);
    }
  }

  private static class ArrowButtonLeftImageHolder {
    private static final Image IMAGE = loadImage("arrowButtonLeft");
  }
  
  private static class ArrowButtonRightImageHolder {
    private static final Image IMAGE = loadImage("arrowButtonRight");
  }

  private static class ArrowButtonUpImageHolder {
    private static final Image IMAGE = loadImage("arrowButtonUp");
  }

  private static class ArrowButtonDownImageHolder {
    private static final Image IMAGE = loadImage("arrowButtonDown");
  }

  private static Image loadImage(String id) {
    String img = UIManager.getString("dsa.gui.lf." + id);
    ImageIcon icon = new ImageIcon(BackgroundPainter.class.getResource(img));
    return icon.getImage();
  }

  private static Image getArrowBtnImage(boolean horizontal, boolean leftOrUp) {
    if (horizontal) {
      if (leftOrUp) {
        return ArrowButtonLeftImageHolder.IMAGE;
      }
      else {
        return ArrowButtonRightImageHolder.IMAGE;
      }
    }
    else {
      if (leftOrUp) {
        return ArrowButtonUpImageHolder.IMAGE;
      }
      else {
        return ArrowButtonDownImageHolder.IMAGE;
      }
    }
  }

  public void paintArrowButtonForeground(SynthContext context, Graphics g,
      int x, int y, int w, int h, int direction) {
    java.awt.Container c = context.getComponent().getParent();
    if ((c instanceof JSpinner)
    /* (c instanceof JComboBox) */) {
      super.paintArrowButtonForeground(context, g, x, y, w, h, direction);
    }
    else {
      Image image = null;
      switch (direction) {
      case SwingConstants.NORTH:
        image = getArrowBtnImage(false, true);
        break;
      case SwingConstants.SOUTH:
        image = getArrowBtnImage(false, false);
        break;
      case SwingConstants.EAST:
        image = getArrowBtnImage(true, false);
        break;
      default:
        image = getArrowBtnImage(true, true);
        break;
      }
      int w2 = image.getWidth(context.getComponent());
      int h2 = image.getHeight(context.getComponent());
      int x2 = x + (w - w2) / 2;
      int y2 = y + (h - h2) / 2;
      if (w2 > w || h2 > h) {
        w2 = w2 > w ? w : w2;
        h2 = h2 > h ? h : h2;
        x2 = x;
        y2 = y;
        image = image.getScaledInstance(w2, h2, Image.SCALE_DEFAULT);
      }
      g.drawImage(image, x2, y2, context.getComponent());
    }
  }

  public void paintCheckBoxBorder(SynthContext context, Graphics g, int x,
      int y, int w, int h) {

  }

  public void paintCheckBoxBackground(SynthContext context, Graphics g, int x,
      int y, int w, int h) {

  }

  public void paintOptionPaneBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h) {
    paintBackground(context, g, x, y, w, h, getBGImage());
  }

  public void paintFileChooserBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h) {
    paintBackground(context, g, x, y, w, h, getBGImage());
  }

  public void paintButtonBorder(SynthContext context, Graphics g, int x, int y,
      int w, int h) {
    paintBtnBorder(context, g, x, y, w, h);
  }

  public void paintToggleButtonBorder(SynthContext context, Graphics g, int x,
      int y, int w, int h) {
    paintBtnBorder(context, g, x, y, w, h);
  }

  public void paintToggleButtonBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h) {
    paintBtnBackground(context, g, x, y, w, h);
  }

  public void paintButtonBackground(SynthContext context, Graphics g, int x,
      int y, int w, int h) {
    paintBtnBackground(context, g, x, y, w, h);
  }

  private void paintBtnBorder(SynthContext context, Graphics g, int x, int y,
      int w, int h) {
    if (((context.getComponentState() & SynthConstants.PRESSED) != 0)
        || (context.getComponent() instanceof JToggleButton && ((JToggleButton) context
            .getComponent()).isSelected())) {
      draw3DRect(g, x, y, w - 1, h - 1, false);
    }
    else {
      draw3DRect(g, x, y, w - 1, h - 1, true);
    }
  }

  private void draw3DRect(Graphics g, int x, int y, int width, int height,
      boolean raised) {
    Color c = g.getColor();
    Color brighter = Color.WHITE;
    Color darker = raised ? Color.GRAY : Color.DARK_GRAY;

    g.setColor(raised ? brighter : darker);
    g.drawLine(x, y, x, y + height);
    g.drawLine(x, y, x + width, y);
    g.setColor(raised ? darker : brighter);
    g.drawLine(x, y + height, x + width, y + height);
    g.drawLine(x + width, y, x + width, y + height);
    g.setColor(c);
  }

  private void paintBtnBackground(SynthContext context, Graphics g, int x,
      int y, int w, int h) {
    paintBtnBorder(context, g, x, y, w, h);
    Image img = null;
    if ((context.getComponentState() & SynthConstants.PRESSED) != 0) {
      img = getPressedImage();
    }
    else if ((context.getComponentState() & SynthConstants.DISABLED) != 0) {
      return;
    }
    else if (context.getComponent() instanceof JToggleButton
        && ((JToggleButton) context.getComponent()).isSelected()) {
      img = getHoverImage();
    }
    else if ((context.getComponentState() & SynthConstants.MOUSE_OVER) != 0) {
      img = getHoverImage();
    }
    else {
      img = getInactiveImage();
    }
    paintBackground(context, g, x + 1, y + 1, w - 3, h - 3, img);
  }

  public void paintTabbedPaneTabBackground(SynthContext context, Graphics g,
      int x, int y, int w, int h, int tabIndex) {
    Image img = null;
    boolean active = false;
    if ((context.getComponentState() & SynthConstants.SELECTED) != 0) {
      img = getBGImage();
      active = true;
    }
    else if ((context.getComponentState() & SynthConstants.MOUSE_OVER) != 0) {
      img = getHoverImage();
    }
    else {
      img = getInactiveImage();
    }
    GeneralPath path = new GeneralPath();
    Rectangle r = new Rectangle(x, y, w, h);
    if (((JTabbedPane) context.getComponent()).getTabPlacement() == JTabbedPane.TOP) {
      path.moveTo(r.x + 5, r.y);
      path.lineTo(r.x, r.y + 5);
      path.lineTo(r.x, r.y + r.height);
      path.lineTo(r.x + r.width, r.y + r.height);
      path.lineTo(r.x + r.width, r.y);
    }
    else {
      path.moveTo(r.x, r.y);
      path.lineTo(r.x, r.y + r.height - 5);
      path.lineTo(r.x + 5, r.y + r.height);
      path.lineTo(r.x + r.width, r.y + r.height);
      path.lineTo(r.x + r.width, r.y);
    }
    path.closePath();
    Shape clip = g.getClip();
    g.setClip(path);
    g.drawImage(img, r.x, r.y, context.getComponent());
    g.setClip(clip);
    if (((JTabbedPane) context.getComponent()).getTabPlacement() == JTabbedPane.TOP) {
      g.drawLine(r.x, r.y + r.height, r.x, r.y + 5);
      g.drawLine(r.x, r.y + 5, r.x + 5, r.y);
      g.drawLine(r.x + 5, r.y, r.x + r.width, r.y);
      g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
      if (!active) {
        g.drawLine(r.x + r.width, r.y + r.height, r.x, r.y + r.height);
      }
    }
    else {
      g.drawLine(r.x, r.y, r.x, r.y + r.height - 5);
      g.drawLine(r.x, r.y + r.height - 5, r.x + 5, r.y + r.height);
      g.drawLine(r.x + 5, r.y + r.height, r.x + r.width, r.y + r.height);
      g.drawLine(r.x + r.width, r.y + r.height, r.x + r.width, r.y);
      if (!active) {
        g.drawLine(r.x + r.width, r.y, r.x, r.y);
      }
    }
  }

  private void paintBackground(SynthContext context, Graphics g, int x, int y,
      int w, int h, Image img) {
    int i, j;
    int width, height;
    width = img.getHeight(context.getComponent());
    height = img.getWidth(context.getComponent());
    Shape clip = g.getClip();
    Rectangle r = new Rectangle(x, y, w, h);
    Area newClip = new Area(clip);
    newClip.intersect(new Area(r));
    g.setClip(newClip);
    if (width > 0 && height > 0) {
      for (i = x; i < (x + w); i += width) {
        for (j = y; j < (y + h); j += height) {
          g.drawImage(img, i, j, context.getComponent());
        }
      }
    }
    g.setClip(clip);
  }
}

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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

public class PropertyLabel extends JLabel {

  private static int necessaryBadIncreases = 0;

  private static float necessaryGoodDecreases = 0;

  public static int getNecessaryBadIncreases() {
    return necessaryBadIncreases;
  }

  public static float getNecessaryGoodDecreases() {
    return necessaryGoodDecreases;
  }

  public static void resetNecessaryBadIncreases() {
    necessaryBadIncreases = 0;
    necessaryGoodDecreases = 0;
    fireNecessaryIncreasesChanged();
  }

  private static ActionListener listener = null;

  public static void setNecessaryIncreaseListener(ActionListener l) {
    listener = l;
  }

  private static void fireNecessaryIncreasesChanged() {
    listener.actionPerformed(null);
  }

  private class MouseHandler extends MouseAdapter implements
      MouseMotionListener {
    public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        if (e.getClickCount() > 1) {
          if (isGood) {
            if (mValue == 14) {
              JOptionPane.showMessageDialog(PropertyLabel.this,
                  "Am Anfang kann keine gute\nEigenschaft über 14 liegen.",
                  "Heldenerstellung", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              setValue(mValue + 1);
              necessaryBadIncreases += 2;
              fireNecessaryIncreasesChanged();
            }
          }
          else if (necessaryBadIncreases > 0) {
            if (mValue == 8) {
              JOptionPane.showMessageDialog(PropertyLabel.this,
                  "Es kann keine schlechte\nEigenschaft über 8 liegen.",
                  "Heldenerstellung", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              setValue(mValue + 1);
              necessaryBadIncreases -= 1;
              fireNecessaryIncreasesChanged();
            }
          }
        }
      }
      else if (SwingUtilities.isRightMouseButton(e)) {
        if (e.getClickCount() > 1) {
          if (isGood && necessaryGoodDecreases > 0) {
            if (mValue == 8) {
              JOptionPane.showMessageDialog(PropertyLabel.this,
                  "Es kann keine gute\nEigenschaft unter 8 liegen.",
                  "Heldenerstellung", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              setValue(mValue - 1);
              necessaryGoodDecreases -= 1;
              fireNecessaryIncreasesChanged();
            }
          }
          else if (!isGood) {
            if (mValue == 2) {
              JOptionPane
                  .showMessageDialog(
                      PropertyLabel.this,
                      "Am Anfang kann keine schlechte\nEigenschaft unter 2 liegen.",
                      "Heldenerstellung", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
              setValue(mValue - 1);
              necessaryGoodDecreases += 2;
              fireNecessaryIncreasesChanged();
            }
          }
        }
      }
    }

    public void mouseDragged(MouseEvent e) {
      JComponent c = (JComponent) e.getSource();
      TransferHandler handler = c.getTransferHandler();
      handler.exportAsDrag(c, e, TransferHandler.MOVE);
    }

    public void mouseMoved(MouseEvent e) {
      // no special action
    }
  };

  public PropertyLabel(int value, boolean good) {
    super("" + value);
    isGood = good;
    mValue = value;
    adjustColor();
    MouseHandler handler = new MouseHandler();
    this.addMouseListener(handler);
    this.addMouseMotionListener(handler);
    this.setTransferHandler(new PropertyTransferHandler(isGood));
  }

  int mValue = 10;

  public void setValue(int value) {
    mValue = value;
    setText("" + value);
    adjustColor();
  }

  public int getValue() {
    return mValue;
  }

  public void setConstraint(int constraint) {
    mConstraint = constraint;
  }

  private void adjustColor() {
    if (mConstraint == 0) {
      setForeground(Color.BLACK);
    }
    else if (mConstraint < 0) {
      setForeground(mValue <= (-mConstraint) ? Color.GREEN : Color.RED);
    }
    else {
      setForeground(mValue >= mConstraint ? Color.GREEN : Color.RED);
    }
    invalidate();
  }

  private static class PropertyFlavor extends DataFlavor {
    public PropertyFlavor(String mimeType, boolean good)
        throws ClassNotFoundException {
      super(mimeType);
      isGood = good;
    }

    private final boolean isGood;
  }

  private static class PropertyTransfer implements Transferable {
    private static final String PROPERTY_TYPE = DataFlavor.javaJVMLocalObjectMimeType
        + ";class=java.lang.Integer";

    private static DataFlavor goodPropertyFlavor = null;

    private static DataFlavor badPropertyFlavor = null;

    public PropertyTransfer(boolean good, int value) {
      isGood = good;
      this.value = value;
    }

    private final boolean isGood;

    private final int value;

    public static DataFlavor getFlavor(boolean good) {
      if (good) {
        if (goodPropertyFlavor == null) {
          try {
            goodPropertyFlavor = new PropertyFlavor(PROPERTY_TYPE, true);
          }
          catch (ClassNotFoundException e) {
            throw new InternalError();
          }
        }
        return goodPropertyFlavor;
      }
      else {
        if (badPropertyFlavor == null) {
          try {
            badPropertyFlavor = new PropertyFlavor(PROPERTY_TYPE, false);
          }
          catch (ClassNotFoundException e) {
            throw new InternalError();
          }
        }
        return badPropertyFlavor;
      }
    }

    public DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] flavors = new DataFlavor[1];
      flavors[0] = getFlavor(isGood);
      return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return (flavor instanceof PropertyFlavor)
          && (((PropertyFlavor) flavor).isGood == isGood);
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
      if (!isDataFlavorSupported(flavor))
        throw new UnsupportedFlavorException(flavor);
      return value;
    }

  }

  private static class PropertyTransferHandler extends TransferHandler {
    private static Integer targetValue = null;

    public PropertyTransferHandler(boolean good) {
      super();
      isGood = good;
    }

    private final boolean isGood;

    protected Transferable createTransferable(JComponent c) {
      if (!(c instanceof PropertyLabel)) throw new InternalError();
      return new PropertyTransfer(isGood, ((PropertyLabel) c).getValue());
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
      if (action != MOVE) return;
      if (targetValue == null) throw new InternalError();
      if (!(source instanceof PropertyLabel)) throw new InternalError();
      ((PropertyLabel) source).setValue(targetValue);
      targetValue = null;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      for (DataFlavor d : transferFlavors) {
        if ((d instanceof PropertyFlavor)
            && (((PropertyFlavor) d).isGood == isGood)) return true;
      }
      return false;
    }

    public boolean importData(JComponent comp, Transferable t) {
      if (!(comp instanceof PropertyLabel)) throw new InternalError();
      try {
        Object o = t.getTransferData(PropertyTransfer.getFlavor(isGood));
        if (o == null) return false;
        targetValue = Integer.valueOf(((PropertyLabel) comp).getValue());
        ((PropertyLabel) comp).setValue((Integer) o);
        return true;
      }
      catch (UnsupportedFlavorException e) {
        return false;
      }
      catch (IOException e) {
        return false;
      }
    }

    public int getSourceActions(JComponent c) {
      return MOVE;
    }
  }

  int mConstraint;

  boolean isGood;

}

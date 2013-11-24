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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import dsa.gui.tables.AbstractTable;
import dsa.gui.tables.ThingTransfer;
import dsa.model.data.ExtraThingData;

abstract class AbstractDnDFrame extends SubFrame {

  public AbstractDnDFrame(ThingTransfer.Flavors flavor, String title) {
    super(title);
    mFlavor = flavor;
  }

  protected abstract boolean addItem(String item, ExtraThingData extraData);

  protected abstract void removeItem(String item);
  
  protected abstract ExtraThingData getExtraDnDData(String item);

  protected final void registerForDnD(AbstractTable table) {
    table.setTransferHandler(new ThingTransferHandler(table));
    table.addMouseMotionListener(new DragStarter());
  }

  private static final class DragStarter extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      JComponent c = (JComponent) e.getComponent();
      TransferHandler handler = c.getTransferHandler();
      handler.exportAsDrag(c, e, TransferHandler.MOVE);
    }
  }

  private final class ThingTransferHandler extends TransferHandler {

    private String targetValue;

    private AbstractTable mTable;
    
    private boolean dragStartedHere;

    public ThingTransferHandler(AbstractTable table) {
      mTable = table;
      dragStartedHere = false;
    }

    protected Transferable createTransferable(JComponent c) {
      if (mTable.getSelectedItem() == null) return null;
      targetValue = mTable.getSelectedItem();
      ExtraThingData data = getExtraDnDData(targetValue);
      dragStartedHere = true;
      return new ThingTransfer(mFlavor, mTable.getSelectedItem(), data);
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
      dragStartedHere = false;
      if (action != MOVE) return;
      if (targetValue == null) throw new InternalError();
      removeItem(targetValue);
      targetValue = null;
    }
    
    private boolean canInsertFlavor(ThingTransfer.Flavors flavor) {
      if (flavor == ThingTransfer.Flavors.Thing) {
        return true;
      }
      if (mFlavor == ThingTransfer.Flavors.Thing) {
        return true;
      }
      return flavor == mFlavor;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      for (DataFlavor d : transferFlavors) {
        if (d instanceof ThingTransfer.ThingFlavor) {
          if (canInsertFlavor(((ThingTransfer.ThingFlavor)d).getFlavor())) {
            return true;
          }
        }
      }
      return false;
    }

    public boolean importData(JComponent comp, Transferable t) {
      if (dragStartedHere) {
        return false;
      }
      try {
        Object o = t.getTransferData(ThingTransfer.THING_FLAVOR);
        if (o == null) return false;
        String data = o.toString();
        StringReader r = new StringReader(data);
        BufferedReader in = new BufferedReader(r);
        targetValue = in.readLine();
        ExtraThingData extraData = new ExtraThingData();
        extraData.read(in, 0);
        return addItem(targetValue, extraData);
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

  private ThingTransfer.Flavors mFlavor;

}

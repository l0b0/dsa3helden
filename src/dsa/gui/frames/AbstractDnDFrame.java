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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import dsa.gui.tables.AbstractTable;
import dsa.gui.tables.ThingTransfer;
import dsa.model.data.ExtraThingData;

abstract class AbstractDnDFrame extends SubFrame {

  public AbstractDnDFrame(ThingTransfer.Flavors flavor, String title) {
    super(title);
    mFlavor = flavor;
  }
  
  protected static boolean isChangeAllowed()
  {
    dsa.model.characters.Hero hero = dsa.model.characters.Group.getInstance().getActiveHero();
    return hero != null && !hero.isDifference();
  }

  protected abstract boolean addItem(String item, ExtraThingData extraData);

  protected abstract void removeItem(String item);
  
  protected abstract void selectItem();
  
  protected abstract ExtraThingData getExtraDnDData(String item);

  protected final void registerForDnD(AbstractTable table) {
    table.setTransferHandler(new ThingTransferHandler(table));
    table.addMouseMotionListener(new DragStarter());
    ContextMenuManager cmm = new ContextMenuManager(table.getDnDComponent(), 
        getAddAction(), getRemoveAction(table));
    table.setPopupListener(cmm);
    table.setKeyListener(cmm);
  }
  
  private Action getAddAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (isChangeAllowed()) selectItem();
      }
    };
  }
  
  private Action getRemoveAction(AbstractTable table) {
    class RemoveAction extends AbstractAction {
      public RemoveAction(AbstractTable table) {
        mTable = table;
      }
      public void actionPerformed(ActionEvent e) {
        if (!isChangeAllowed()) return;
        String item = mTable.getSelectedItem();
        if (item != null) removeItem(item);
      }
      private AbstractTable mTable;
    }
    return new RemoveAction(table);
  }
  
  private static class EmptyTransferable implements Transferable {
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[0];
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return false;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      throw new UnsupportedFlavorException(flavor);
    }
    
    private static EmptyTransferable instance = new EmptyTransferable();

    public static EmptyTransferable getInstance() {
      return instance;
    }
  }
  
  private static final class PasteAction extends AbstractAction implements ClipboardOwner {
    public void actionPerformed(ActionEvent e) {
      if (!isChangeAllowed()) return;
      Object src = e.getSource();
      if (src instanceof JComponent) {
        JComponent c = (JComponent) src;
        TransferHandler th = c.getTransferHandler();
        Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        boolean pasted = false;
        try {
          Transferable trans = clipboard.getContents(null);
          if (trans != null) pasted = th.importData(c, trans);
          if (pasted) {
            clipboard.setContents(EmptyTransferable.getInstance(), this);
          }
        }
        catch (IllegalStateException ex) {
          // clipboard was unavailable
          UIManager.getLookAndFeel().provideErrorFeedback(c);
          return;
        }
      }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // nothing to do
    }
  }

  private static final class ContextMenuManager extends MouseAdapter implements ActionListener, KeyListener {
    
    private ActionMap actionMap;
    
    private InputMap inputMap;
    
    private JComponent component;
    
    public ContextMenuManager(JComponent c, Action addAction, Action removeAction) {
      actionMap = new ActionMap();
      actionMap.put("ADD", addAction);
      actionMap.put("REMOVE", removeAction);
      actionMap.put((String)TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
      actionMap.put("MY_PASTE", pasteAction);
      inputMap = new InputMap();
      inputMap.put(KeyStroke.getKeyStroke("control X"), (String)TransferHandler.getCutAction().getValue(Action.NAME));
      inputMap.put(KeyStroke.getKeyStroke("control V"), "MY_PASTE");
      component = c;
    }
    
    private static Action pasteAction = new PasteAction();
    
    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger() && isChangeAllowed()) showPopupMenu(e);
    }
    
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger() && isChangeAllowed()) showPopupMenu(e);
    }
    
    private void showPopupMenu(MouseEvent e) {
      JPopupMenu popup = new JPopupMenu();
      JMenuItem addItem = new JMenuItem("Hinzufügen ...");
      addItem.setMnemonic(KeyEvent.VK_H);
      addItem.setActionCommand("ADD");
      addItem.addActionListener(this);
      popup.add(addItem);
      JMenuItem removeItem = new JMenuItem("Entfernen");
      removeItem.setMnemonic(KeyEvent.VK_E);
      removeItem.setActionCommand("REMOVE");
      removeItem.addActionListener(this);
      popup.add(removeItem);
      JMenuItem cutItem = new JMenuItem("Ausschneiden");
      cutItem.setMnemonic(KeyEvent.VK_A);
      cutItem.setActionCommand((String)TransferHandler.getCutAction().getValue(Action.NAME));
      cutItem.addActionListener(this);
      popup.add(cutItem);
      JMenuItem pasteItem = new JMenuItem("Einfügen");
      pasteItem.setMnemonic(KeyEvent.VK_F);
      pasteItem.setActionCommand("MY_PASTE");
      pasteItem.addActionListener(this);
      popup.add(pasteItem);
      popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public void actionPerformed(ActionEvent e) {
      String action = e.getActionCommand();
      Action a = actionMap.get(action);
      if (a != null) {
        a.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, null));
      }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
      KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
      Object actionName = inputMap.get(keyStroke);
      if (actionName == null) return;
      Action action = actionMap.get(actionName);
      if (action != null) {
        action.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, null));
      }
    }

    public void keyReleased(KeyEvent e) {
    }
  }

  private static final class DragStarter extends MouseMotionAdapter {
    public void mouseDragged(MouseEvent e) {
      if (!isChangeAllowed()) return;
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
      if (!isChangeAllowed()) return false;
      if (flavor == ThingTransfer.Flavors.Thing) {
        return true;
      }
      if (mFlavor == ThingTransfer.Flavors.Thing) {
        return true;
      }
      return flavor == mFlavor;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      if (!isChangeAllowed()) return false;
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
      if (dragStartedHere || !isChangeAllowed()) {
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

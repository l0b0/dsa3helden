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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import dsa.gui.util.HelpProvider;

public abstract class BGDialog extends JDialog implements HelpProvider{

  public BGDialog() {
    super();
  }

  public BGDialog(Frame owner) {
    super(owner);
  }

  public BGDialog(Frame owner, boolean modal)  {
    super(owner, modal);
  }

  public BGDialog(Frame owner, String title)  {
    super(owner, title);
  }

  public BGDialog(Frame owner, String title, boolean modal)
       {
    super(owner, title, modal);
  }

  public BGDialog(Frame owner, String title, boolean modal,
      GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
  }

  public BGDialog(Dialog owner)  {
    super(owner);
  }

  public BGDialog(Dialog owner, boolean modal)  {
    super(owner, modal);
  }

  public BGDialog(Dialog owner, String title)  {
    super(owner, title);
  }

  public BGDialog(Dialog owner, String title, boolean modal)
       {
    super(owner, title, modal);
  }

  public BGDialog(Dialog owner, String title, boolean modal,
      GraphicsConfiguration gc)  {
    super(owner, title, modal, gc);
  }
  
  //public String getHelpPage() {
    //return null;
  //}
  
  public final java.awt.Component getHelpParent() {
    return this;
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

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
 along with Heldenverwaltung; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.util;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JOptionPane;

import org.jdesktop.jdic.desktop.Desktop;

import dsa.util.Directories;

public class Help {

  
  private Help() {}
  
  public static void showIndex(Component parent) {
    openHelpBrowser(parent, "hilfe/index.html");
  }
  
  public static void showContents(Component parent) {
    openHelpBrowser(parent, "hilfe/inhalt.html");
  }
  
  public static void showPage(Component parent, String page) {
    openHelpBrowser(parent, "hilfe/" + page + ".html");
  }
  
  private static void openHelpBrowser(Component parent, String fileName) {
    File file = new File(Directories.getApplicationPath() + fileName);
    if (!file.exists()) {
      JOptionPane.showMessageDialog(parent, "Hilfedatei " + file.getName() + " nicht gefunden!", "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      URL url = file.toURL();
      Desktop.browse(url);
    }
    catch (java.net.MalformedURLException ex) {
      ex.printStackTrace();
    }
    catch (org.jdesktop.jdic.desktop.DesktopException ex) {
      JOptionPane.showMessageDialog(parent,
          "Die Hilfe konnte nicht geöffnet werden. Fehler:\n"
              + ex.getMessage() + "\nBitte öffne manuell " + file.getAbsolutePath(), "Heldenverwaltung",
          JOptionPane.ERROR_MESSAGE);
    }
  }
  
}

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

import java.util.HashMap;

import javax.swing.ImageIcon;

public class ImageManager {

  public static ImageIcon getIcon(String name) {
    return getInstance().getIconImpl(name);
  }

  private static ImageManager getInstance() {
    return sInstance;
  }

  private static ImageManager sInstance = new ImageManager();

  private ImageManager() {
    images = new HashMap<String, ImageIcon>();
  }

  private HashMap<String, ImageIcon> images;

  private ImageIcon getIconImpl(String name) {
    ImageIcon icon = images.get(name);
    if (icon == null) {
      icon = new ImageIcon(getClass().getResource(name + ".png"));
      images.put(name, icon);
    }
    return icon;
  }

}

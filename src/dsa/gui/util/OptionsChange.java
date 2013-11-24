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
package dsa.gui.util;

public class OptionsChange {

  public interface OptionsListener {
    void optionsChanged();
  }

  private static java.util.ArrayList<OptionsListener> listeners = new java.util.ArrayList<OptionsListener>();

  public static void addListener(OptionsListener listener) {
    listeners.add(listener);
  }

  public static void removeListener(OptionsListener listener) {
    listeners.remove(listener);
  }

  public static void fireOptionsChanged() {
    OptionsListener[] ls = new OptionsListener[listeners.size()];
    ls = listeners.toArray(ls);
    for (OptionsListener l : ls)
      l.optionsChanged();
  }

}
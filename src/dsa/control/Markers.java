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
package dsa.control;

import java.util.prefs.Preferences;

import dsa.model.characters.Hero;

public class Markers {

  private Markers() {
  }

  private static boolean useMarkers = Preferences.userNodeForPackage(
      Markers.class).getBoolean("QvatUseMarkers", false);

  public static boolean useMarkers() {
    return useMarkers;
  }

  public static void setUseMarkers(boolean use) {
    useMarkers = use;
  }

  public static int getMarkers(Hero hero) {
    if (useMarkers()) {
      return hero.getMarkers();
    }
    else {
      return 0;
    }
  }

}

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
package dsa.util;

import java.io.File;
import java.util.prefs.Preferences;

public class Directories {
  
  private Directories() {}

  public static File getLastUsedDirectory(Object o, String key) {
    Preferences prefs = Preferences.userNodeForPackage(o.getClass());
    String dir = prefs.get("Directory" + key, "");
    if (dir.length() > 0) {
      File f = new File(dir);
      if (!f.exists()) return null;
      if (!f.isDirectory())
        return f.getParentFile();
      else
        return f;
    }
    else
      return null;
  }

  public static void setLastUsedDirectory(Object o, String key, File f) {
    Preferences prefs = Preferences.userNodeForPackage(o.getClass());
    if (!f.isDirectory()) f = f.getParentFile();
    prefs.put("Directory" + key, f.getAbsolutePath());
  }

  public static String getApplicationPath() {
    String cp = System.getProperty("java.class.path");
    java.util.StringTokenizer st = new java.util.StringTokenizer(cp,
        File.pathSeparator);
    while (st.hasMoreTokens()) {
      String s = st.nextToken();
      String p = s.toLowerCase(java.util.Locale.GERMAN);
      if (p.endsWith("images") || p.endsWith("images" + File.separator)) {
        File file = new File(s);
        file = file.getParentFile();
        return file.getAbsolutePath() + File.separator;
      }
      else if (p.endsWith("heldenverwaltung.jar")) {
        if ("heldenverwaltung.jar".equals(p)) {
          s = "." + File.separator + "heldenverwaltung.jar";
        }
        File file = new File(s);
        file = file.getParentFile();
        return file.getAbsolutePath() + File.separator;
      }
    }
    return "";
  }
  
  public static String getUserDataPath() {
    Preferences prefs = Preferences
      .userNodeForPackage(Directories.class);
    boolean inUserDir = prefs.getBoolean("storedDataInUserDir", false);
    if (inUserDir) {
      String path = System.getProperty("user.home") + File.separator + 
        "Heldenverwaltung";
      File test = new File(path);
      if (!test.exists()) {
        test.mkdir();
      }
      path += File.separator + "Eigene_Daten";
      test = new File(path);
      if (!test.exists()) {
        test.mkdir();
      }
      path += File.separator;
      return path;
    }
    else {
      // in future, use user directory
      prefs.putBoolean("storedDataInUserDir", true);
      return getApplicationPath() + File.separator + "daten" + File.separator;
    }
  }

}

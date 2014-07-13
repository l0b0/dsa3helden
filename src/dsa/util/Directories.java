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
package dsa.util;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
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

  public static String getAbsolutePath(String relativePath, File f)  {
    if (relativePath == null || relativePath.equals("")) {
      return "";
    }
    try {
	    File test = new File(relativePath);
	    if (test.isAbsolute()) return test.getCanonicalPath();
	    if (f == null || f.getParentFile() == null) return relativePath;
    }
    catch (IOException e) {
    }
    if (f == null)
    	return relativePath;
    try {
	    String p1 = f.getParentFile().getCanonicalPath();
	    p1 += File.separator + relativePath;
	    return new File(p1).getCanonicalPath();
    }
    catch (IOException e) {
    	return relativePath;
    }
  }

  public static String getRelativePath(String absolutePath, File f) throws IOException {
	if (f == null)
		return absolutePath;
    File abs = new File(absolutePath);
    if (abs.getParentFile() == null) return absolutePath;
    if (f.getParentFile() == null) return absolutePath;
    String p1 = abs.getParentFile().getCanonicalPath();
    String p2 = f.getParentFile().getCanonicalPath();
    if (p1.length() > 2 && p2.length() > 2 && p1.charAt(1) == ':'
        && p2.charAt(1) == ':' && p1.charAt(0) != p2.charAt(0)) {
      // under windows, different drives
      return absolutePath;
    }
    StringTokenizer t1 = new StringTokenizer(p1, File.separator);
    String[] p1Parts = new String[t1.countTokens()];
    int i = 0;
    while (t1.hasMoreTokens())
      p1Parts[i++] = t1.nextToken();
    StringTokenizer t2 = new StringTokenizer(p2, File.separator);
    String[] p2Parts = new String[t2.countTokens()];
    i = 0;
    while (t2.hasMoreTokens())
      p2Parts[i++] = t2.nextToken();
    if (p1Parts.length == 0 || p2Parts.length == 0) return p1;
    i = 0;
    while (i < p1Parts.length && i < p2Parts.length
        && p1Parts[i].equals(p2Parts[i]))
      ++i;
    String path = "";
    for (int j = i; j < p2Parts.length; ++j)
      path += ".." + File.separator;
    for (int j = i; j < p1Parts.length; ++j)
      path += p1Parts[j] + File.separator;
    path += abs.getName();
    return path;
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
  
  public static String getUserHomePath() {
    String path = System.getProperty("user.home") + File.separator + 
      "Heldenverwaltung";
    File test = new File(path);
    if (!test.exists()) {
      test.mkdir();
    }
    return path + File.separator;
  }
  
  public static String getUserDataPath() {
    int option = Preferences.userNodeForPackage(dsa.gui.PackageID.class).getInt("CustomDataDirOption", -1);
    if (option == -1) {
      boolean inUserDir = Preferences.userNodeForPackage(Directories.class).getBoolean("storedDataInUserDir", false);
      option = inUserDir ? 0 : 1;
      // in future, use user directory
      Preferences.userNodeForPackage(dsa.gui.PackageID.class).putInt("CustomDataDirOption", 0);
    }
    String path = "";
    if (option == 2) {
      path = Preferences.userNodeForPackage(dsa.gui.PackageID.class).get("CustomDataDir", "");
      File test = new File(path);
      if (!test.exists() || !test.isDirectory()) {
        option = 0;
      }
      if (!path.endsWith(File.separator)) {
        path += File.separator;
      }
    }
    if (option == 0) {
      path = getUserHomePath();
      path += "Eigene_Daten";
      File test = new File(path);
      if (!test.exists()) {
        test.mkdir();
      }
      path += File.separator;
    }
    else if (option == 1) {
      path = getApplicationPath();
      path += "Eigene_Daten";
      File test = new File(path);
      if (!test.exists()) {
        test.mkdir();
      }
      path += File.separator;
    }      
    return path;
  }

}

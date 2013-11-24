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
package dsa.control;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dsa.gui.dialogs.ChangeLogDialog;

public class OnlineOperations {
  
  // private static final String URL_BASE = "http://www.ruedenauer.net/";
  // private static final String URL_BASE = "file:///home/joerg/projekte/homepage2/trunk/";
  private static final String URL_BASE = "http://dsa3helden.sourceforge.net/";
  
  public static void showHomepage(JFrame parent) {
    final String url = getUrlBase() + "index.html";
    showWebpage(parent, url);
  }

  private static void showWebpage(JFrame parent, String url) {
    Desktop desktop = null;
    if (Desktop.isDesktopSupported()) {
      desktop = Desktop.getDesktop();
      if (!desktop.isSupported(Desktop.Action.OPEN)) {
        desktop = null;
      }
    }
    if (desktop == null) {
      JOptionPane.showMessageDialog(parent, "Das Betriebssystem erlaubt keinen direkten Browser-Aufruf."
          + "\nBitte öffne manuell " + url,
          "Fehler", JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      desktop.browse(new URI(url));
    }
    catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Die Homepage konnte nicht geöffnet werden. Fehler:\n"
              + ex.getMessage() + "\nBitte öffne manuell " + url, "Fehler",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void mailToAuthor(JFrame parent) {
    Desktop desktop = null;
    if (Desktop.isDesktopSupported()) {
      desktop = Desktop.getDesktop();
      if (!desktop.isSupported(Desktop.Action.OPEN)) {
        desktop = null;
      }
    }
    if (desktop == null) {
      JOptionPane.showMessageDialog(parent, "Das Betriebssystem erlaubt keine direkte Mail-Erstellung.\n"
          + "Bitte schreibe manuell an joerg@ruedenauer.net.", 
          "Fehler", JOptionPane.ERROR_MESSAGE);
      return;
    }
    try {
      String messageURI = "joerg@ruedenauer.net?subject=Heldenverwaltung "
          + dsa.control.Version.getCurrentVersionString();
      desktop.mail(new URI("mailto", messageURI, null));
    }
    catch (URISyntaxException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      JOptionPane.showMessageDialog(parent,
          "Die E-Mail kann nicht erstellt werden. Fehler:\n"
              + ex.getMessage()
              + "\nBitte schreibe manuell an joerg@ruedenauer.net.",
          "Fehler", JOptionPane.ERROR_MESSAGE);
    }    
  }
  
  public static void downloadSetup(JFrame parent, Version version) {
    final String urlBase = "http://prdownloads.sourceforge.net/dsa3helden/Heldenverwaltung-";
    final String urlAppendix = "?download";
    final String windowsPart = "-Setup.exe";
    final String linuxPart = "-Linux-x86-Install";
    boolean isWindows = System.getProperty("os.name").contains("Windows");
    String url = urlBase + version.toString() + (isWindows ? windowsPart : linuxPart) + urlAppendix;
    showWebpage(parent, url);
    if (JOptionPane.showConfirmDialog(parent, "Zum Update muss die Heldenverwaltung geschlossen werden.\nJetzt beenden?", 
        "Heldenverwaltung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      if (!(parent instanceof dsa.gui.frames.ControlFrame)) return;
      ((dsa.gui.frames.ControlFrame)parent).saveAndExit();
    }
  }
  
  private static abstract class ContentCallback implements Runnable {
    private String content;
    private Exception exception;
    private boolean canceled;

    public void setContent(String content) {
      this.content = content;
    }
    public void setException(Exception ex) {
      this.exception = ex;
    }
    public void setCanceled(boolean canceled) {
      this.canceled = canceled;
    }
    
    protected String getContent() { return content; }
    protected Exception getException() { return exception; }
    protected boolean wasCanceled() { return canceled; }
    
    protected ContentCallback() {
      content = "";
      exception = null;
      canceled = false;
    }
  }
  
  private static class ChangeLogCallback extends ContentCallback {
    private final JFrame parent;
    private final Version version;
    
    public ChangeLogCallback(JFrame parent, Version serverVersion) {
      this.parent = parent;
      this.version = serverVersion;
    }
    
    public void run() {
      if (wasCanceled()) return;
      String text = getContent();
      if (getException() != null) {
        text = "Fehler! Änderungen konnten nicht geholt werden. Grund:\n"
          + getException().getMessage();
      }
      ChangeLogDialog dialog = new ChangeLogDialog(parent, text, version);
      dialog.setVisible(true);
    }
    
  }
  
  private static class FileDownloader implements Runnable {
    
    public FileDownloader(JFrame parent, ContentCallback callback, String url, String text) {
      this.parent = parent;
      this.callback = callback;
      this.urlS = url;
      this.text = text;
      this.verbose = true;
    }
    
    private final JFrame parent;
    private final ContentCallback callback;
    private final String urlS;
    private final String text;
    private boolean verbose;
    
    public void setVerbose(boolean verbose) {
      this.verbose = verbose;
    }
    
    public void download() {
      (new Thread(this)).start();
    }

    public void run() {
        dsa.gui.util.ProgressMonitor monitor = null;
        if (verbose) monitor = new dsa.gui.util.ProgressMonitor(parent, text);
        try {
          URL url = new URL(urlS);
          BufferedReader in = new BufferedReader(new InputStreamReader(url
              .openStream()));
          try {
            StringBuffer sb = new StringBuffer();
            String line = in.readLine();
            while (line != null) {
              sb.append(line);
              sb.append("\n");
              line = in.readLine();
            }
            callback.setContent(sb.toString());
            if (monitor != null && monitor.isCanceled()) callback.setCanceled(true);
          }
          finally {
            in.close();
          }
        }
        catch (java.net.MalformedURLException e) {
          e.printStackTrace();
        }
        catch (java.io.IOException e) {
          callback.setException(e);
        }
        finally {
          if (monitor != null && !monitor.isCanceled()) monitor.close();
        }
        javax.swing.SwingUtilities.invokeLater(callback);
      }
  }
  
  public final static String getUrlBase() { return URL_BASE; }
  
  public static class VersionCallback extends ContentCallback {
    private JFrame parent;
    private boolean verbose;
    public VersionCallback(JFrame parent, boolean verbose) {
      this.parent = parent;
      this.verbose = verbose;
    }
    public void run() {
      Version serverVersion = null;
      if (getException() == null) {
        try {
          serverVersion = Version.parse(getContent().trim());
        }
        catch (java.text.ParseException e) {
          setException(e);
        }
      }
      if (getException() != null) {
        JOptionPane.showMessageDialog(parent,
            "Ein Fehler ist bei der Versionsabfrage aufgetreten:\n"
              + getException().getMessage(), "Heldenverwaltung", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Version thisVersion = Version.getCurrentVersion();
      int res = thisVersion.compareTo(serverVersion);
      if (res == -1 && (!verbose || !wasCanceled())) {
        Object[] options = { "Download", "Änderungen zeigen" };
        Object result = JOptionPane.showInputDialog(parent, 
            "Eine neuere Version ist verfügbar!", "Heldenverwaltung", 
            JOptionPane.INFORMATION_MESSAGE, null, options, "Download");
        if ("Download".equals(result)) {
          OnlineOperations.downloadSetup(parent, serverVersion);
        }
        else if (result != null) {
          ChangeLogCallback callback = new ChangeLogCallback(parent, serverVersion);
          final String urlS = OnlineOperations.getUrlBase() + "ChangeLog.txt";
          final String text = "Hole Versionshistorie ...";
          FileDownloader downloader = new FileDownloader(parent, callback, urlS, text);
          downloader.download();
        }
      }
      else if (verbose && !wasCanceled()) {
        JOptionPane.showMessageDialog(parent,
            "Es ist keine neuere Version verfügbar.", "Heldenverwaltung",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  public static void checkForUpdate(JFrame parent, boolean verbose) {
    final String urlS = OnlineOperations.getUrlBase() + "helden_version.txt";
    final String text = "Suche nach neuer Version ...";
    VersionCallback callback = new VersionCallback(parent, verbose);
    FileDownloader downloader = new FileDownloader(parent, callback, urlS, text);
    downloader.setVerbose(verbose);
    downloader.download();
  }
  
}

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
package dsa.util;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitorInputStream;

/**
 * 
 */
public class FileTransformer {

  public FileTransformer(File input, File output) {
    this.input = input;
    this.output = output;
    lookupTable = new LookupTable();
    xml = false;
  }

  public void setLookupTable(LookupTable table) {
    lookupTable = table;
  }

  public void setXMLAwareness(boolean useXML) {
    xml = useXML;
  }
  
  public void transform(Component component, String message) throws IOException {
    if (input.getName().toLowerCase(java.util.Locale.GERMAN).trim().endsWith(
        ".odt")) {
      setXMLAwareness(true);
      try {
        ZipFile zipFile = new ZipFile(input);
        try {
          ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(
              output));
          try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            byte[] dataBuffer = new byte[5 * 1024 * 1024];
            while (entries.hasMoreElements()) {
              ZipEntry entry = entries.nextElement();
              InputStream entryInput = new BufferedInputStream(zipFile
                  .getInputStream(entry));
              try {
                if (entry.getName().equalsIgnoreCase("content.xml")) {
                  ZipEntry newEntry = new ZipEntry(entry.getName());
                  newEntry.setComment(entry.getComment());
                  newEntry.setExtra(entry.getExtra());
                  newEntry.setMethod(entry.getMethod());
                  outStream.putNextEntry(newEntry);
                  InputStream progressStream = new ProgressMonitorInputStream(
                      component, message, entryInput);
                  transform(progressStream, outStream);
                }
                else {
                  ZipEntry newEntry = (ZipEntry) entry.clone();
                  outStream.putNextEntry(newEntry);
                  while (entryInput.available() > 0) {
                    int read = entryInput.read(dataBuffer);
                    if (read > 0) {
                      outStream.write(dataBuffer, 0, read);
                    }
                  }
                }
                outStream.closeEntry();
              }
              finally {
                entryInput.close();
              }
            }
          }
          finally {
            outStream.close();
          }
        }
        finally {
          zipFile.close();
        }
      }
      catch (IOException e) {
        throw e;
      }
    }
    else {
      InputStream inStream = new BufferedInputStream(
          new ProgressMonitorInputStream(component, message,
              new FileInputStream(input)));
      try {
        OutputStream outStream = new FileOutputStream(output);
        try {
          transform(inStream, outStream);
          outStream.flush();
        }
        finally {
          outStream.close();
        }
      }
      finally {
        inStream.close();
      }
    }
  }

  private void transform(InputStream inStream, OutputStream outStream)
      throws IOException {
    PushbackInputStream in = new PushbackInputStream(inStream);
    BufferedOutputStream out = new BufferedOutputStream(outStream);
    
    final String encoding = xml ? "UTF-8" : "ISO-8859-15";

    Character ch;
    int retCode;
    ArrayList<Character> charBuffer = new ArrayList<Character>();
    charBuffer.ensureCapacity(20);
    LookupTable.LookupPerformer lookuper = lookupTable.getLookupPerformer();
    while ((retCode = in.read()) != -1) {
      ch = new Character((char) retCode);
      if (xml && ((char) retCode == '<')) {
        out.write(retCode);
        while ((retCode = in.read()) != -1) {
          out.write(retCode);
          if ((char) retCode == '>') break;
        }
      }
      else {
        LookupTable.LookupPerformer.NextCharResult result = lookuper
            .nextChar(ch);
        if (result != LookupTable.LookupPerformer.NextCharResult.Continue) {
          if (result == LookupTable.LookupPerformer.NextCharResult.Hit) {
            String text = lookuper.getItem();
            if (text != null) {
              out.write(encode(text, encoding));
            }
          }
          else {
            for (int i = 0; i < charBuffer.size(); ++i) {
              out.write(charBuffer.get(i));
            }
            if (charBuffer.size() > 0)
              in.unread(retCode); // may be the start of a new token
            else
              out.write(ch);
          }
          charBuffer.clear();
          lookuper.restart();
        }
        else
          charBuffer.add(ch);
      }
    }
    out.flush();
  }
  
  private byte[] encode(String text, String encoding) throws IOException {
    if (xml) {
      text = text.replace("&", "&amp;");
      text = text.replace("'", "&pos;");
      text = text.replace("\"", "&quot;");
      text = text.replace("<", "&lt;");
      text = text.replace(">", "&gt;");
    }
    return text.getBytes(encoding);
  }

  private LookupTable lookupTable;

  private final File input;

  private final File output;

  private boolean xml;

}

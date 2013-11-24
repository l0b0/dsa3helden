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
package dsa.control.filetransforms;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitorInputStream;


final class OpenDocumentTransformer extends AbstractFileTransformer {
  
  protected void doTransform(Component component, String message) throws IOException {
    TextStreamTransformer streamTransformer = new TextStreamTransformer(lookupTable, Bufferers.createBufferer(FileType.ODT));
    // ODT files: these are de facto zip files, with the main
    // content file but one of the files which are zipped together in the odt.
    ZipFile zipFile = new ZipFile(input);
    try {
      ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(
          output));
      try {
        // search for the main document
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        byte[] dataBuffer = new byte[5 * 1024 * 1024];
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();
          if (entry.getName().equalsIgnoreCase("content.xml")) {
            // this is the main document. It must be transformed.
            // all other zip attributes remain the same
            ZipEntry newEntry = new ZipEntry(entry.getName());
            newEntry.setComment(entry.getComment());
            newEntry.setExtra(entry.getExtra());
            newEntry.setMethod(entry.getMethod());
            outStream.putNextEntry(newEntry);
            InputStream entryInput = new BufferedInputStream(zipFile
                .getInputStream(entry));
            InputStream progressStream = new ProgressMonitorInputStream(
                component, message, entryInput);
            // real work delegated into another class
            try {
              streamTransformer.transform(progressStream, outStream);
            }
            finally {
              progressStream.close();
            }
          }
          else {
            // other files in the zip file are simply cloned
            ZipEntry newEntry = (ZipEntry) entry.clone();
            outStream.putNextEntry(newEntry);
            InputStream entryInput = new BufferedInputStream(zipFile
                .getInputStream(entry));
            // entry.clone() doesn't clone the content of the
            // file, though. Do that manually.
            try {
              while (entryInput.available() > 0) {
                int read = entryInput.read(dataBuffer);
                if (read > 0) {
                  outStream.write(dataBuffer, 0, read);
                }
              }
            }
            finally {
              entryInput.close();
            }
          }
          outStream.closeEntry();
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
}

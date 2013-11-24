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
package dsa.control.filetransforms;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.ProgressMonitorInputStream;

final class SimpleFileTransformer extends AbstractFileTransformer {
  
  private Bufferer bufferer;
  
  public SimpleFileTransformer(Bufferer bufferer) {
    this.bufferer = bufferer;
  }

  protected void doTransform(Component component, String message) throws IOException {
    TextStreamTransformer streamTransformer = new TextStreamTransformer(lookupTable, bufferer);
    // simple case: just one file, which is transformed directly.
    InputStream inStream = new BufferedInputStream(
        new ProgressMonitorInputStream(component, message,
            new FileInputStream(input)));
    try {
      OutputStream outStream = new FileOutputStream(output);
      try {
        streamTransformer.transform(inStream, outStream);
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

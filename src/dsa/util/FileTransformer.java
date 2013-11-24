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
import java.io.*;
import java.util.Vector;

import javax.swing.ProgressMonitorInputStream;

/**
 * 
 */
public class FileTransformer {

  public FileTransformer(File input, File output) {
    this.input = input;
    this.output = output;
    lookupTable = new LookupTable();
  }

  public void setLookupTable(LookupTable table) {
    lookupTable = table;
  }

  public void transform(Component component, String message) throws IOException {
    PushbackInputStream in = new PushbackInputStream(new BufferedInputStream(
        new ProgressMonitorInputStream(component, message, new FileInputStream(
            input))));
    PrintWriter out = new PrintWriter(
        new BufferedWriter(new FileWriter(output)));

    try {
      Character ch;
      int retCode;
      Vector<Character> charBuffer = new Vector<Character>();
      charBuffer.ensureCapacity(20);
      LookupTable.LookupPerformer lookuper = lookupTable.GetLookupPerformer();
      while ((retCode = in.read()) != -1) {
        ch = new Character((char) retCode);
        LookupTable.LookupPerformer.NextCharResult result = lookuper
            .nextChar(ch);
        if (result != LookupTable.LookupPerformer.NextCharResult.Continue) {
          if (result == LookupTable.LookupPerformer.NextCharResult.Hit) {
            String text = lookuper.getItem();
            out.print(text != null ? text : "");
          }
          else {
            for (int i = 0; i < charBuffer.size(); ++i) {
              out.print(charBuffer.get(i));
            }
            if (charBuffer.size() > 0)
              in.unread(retCode); // may be the start of a new token
            else
              out.print(ch);
          }
          charBuffer.clear();
          lookuper.restart();
        }
        else
          charBuffer.add(ch);
      }
      out.flush();
    }
    finally {
      in.close();
      out.close();
    }
  }

  private LookupTable lookupTable;

  private File input, output;

}

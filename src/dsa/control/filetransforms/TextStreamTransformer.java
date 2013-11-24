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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

import dsa.util.LookupTable;

class TextStreamTransformer {
  
  private final LookupTable lookupTable;
  
  private final Bufferer bufferer;
  
  public TextStreamTransformer(LookupTable table, Bufferer bufferer) {
    this.lookupTable = table;
    this.bufferer = bufferer;
  }
  
  /**
   * Does the transformation, but is merely 'glue code' between the
   * Bufferer and the LookupTable
   */
  public void transform(InputStream inStream, OutputStream outStream)
      throws IOException {
    PushbackInputStream in = new PushbackInputStream(inStream);
    BufferedOutputStream out = new BufferedOutputStream(outStream);
    
    Character ch;
    int retCode;
    
    LookupTable.LookupPerformer lookuper = lookupTable.getLookupPerformer();
    
    while ((retCode = in.read()) != -1) {
      
      // perhaps skip a part of the file
      if (bufferer.skip(in, out, (char) retCode)) continue;
      
      // look for next tag
      ch = new Character((char) retCode);
      LookupTable.LookupPerformer.NextCharResult result = lookuper.nextChar(ch);
      
      if (result == LookupTable.LookupPerformer.NextCharResult.Continue) {
        // may yet become a tag
        bufferer.addCharacter(outStream, ch);
      }
      else if (result == LookupTable.LookupPerformer.NextCharResult.Hit) {
        // tag found
        bufferer.printHit(in, out, lookuper.getItem(), lookuper.isNumberItem());
        lookuper.restart();
      }
      else {
        // no tag
        bufferer.printMiss(in, out, retCode);
        lookuper.restart();
      }
    }
    
    bufferer.flushBuffer(out);
    out.flush();
  }
  

}

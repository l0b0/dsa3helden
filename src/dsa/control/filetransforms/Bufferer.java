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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/**
 * For the transformation, the characters that are read in must be buffered so 
 * they can later be replaced. The concrete handling of the buffering and 
 * replacement is different for the different file types. Therefore, different
 * classes are used (algorithm pattern). This is the interface for the buffers.
 */
public interface Bufferer {

  /**
   * In some cases, whole sections of the input file may be directly written to
   * the output file. This is determined by the last read character.
   * Returns whether something was skipped.
   */
  boolean skip(InputStream in, OutputStream out, int lastRead) throws IOException;
  
  /**
   * Adds a character to a buffer. Theoretically, if currently no buffering takes
   * place, the character may also be written directly to the output.
   */
  
  void addCharacter(OutputStream out, Character ch) throws IOException;
  
  /**
   * Prints the replacement for the last tag to the output stream.
   */
  void printHit(PushbackInputStream in, OutputStream out, String item, boolean isNumber) throws IOException;

  /**
   * Notification that no tag was found. The buffered data may be written 
   * to the output stream. The last read sign -- which determined that there
   * was a miss -- is given so it may be put back into the stream.
   */
  void printMiss(PushbackInputStream in, OutputStream out, int ch) throws IOException;

  /**
   * Print all buffered data to the output.
   */
  void flushBuffer(OutputStream out) throws IOException;    
  
}

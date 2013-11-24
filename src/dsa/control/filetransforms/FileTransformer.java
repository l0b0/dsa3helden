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
import java.io.File;
import java.io.IOException;

import dsa.util.LookupTable;

/**
 * Prints a character by replacing tokens in an input file with the values
 * in a lookup table. 
 */
public interface FileTransformer {
  
  void setInputFile(File inputFile);
  
  void setOutputFile(File outputFile);
  
  void setLookupTable(LookupTable table);

  /**
   * Performs the transformation. Shows a progress dialog if necessary (if
   * the transformation takes long).
   * 
   * @param component Parent component for the progress dialog
   * @param message Message displayed in the progress dialog
   * @see java.io.ProgressMonitorInputStream
   */
  void transform(Component component, String message) throws IOException;
}

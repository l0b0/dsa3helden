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
package dsa.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import dsa.gui.dialogs.ErrorHandlingDialog;

class ErrorReportStream extends PrintStream {
  
  public void print(Object object) {
    super.print(object);
    if (object instanceof Exception) {
      final ErrorHandlingDialog dialog = new ErrorHandlingDialog((Exception) object);
      dialog.setModal(true);
      dialog.setVisible(true);
    }
  }
  
  public ErrorReportStream() {
    super(System.out);
  }

  public ErrorReportStream(OutputStream out) {
    super(out);
  }

  public ErrorReportStream(OutputStream out, boolean autoFlush) {
    super(out, autoFlush);
  }

  public ErrorReportStream(OutputStream out, boolean autoFlush, String encoding)
      throws UnsupportedEncodingException {
    super(out, autoFlush, encoding);
  }

  public ErrorReportStream(String fileName) throws FileNotFoundException {
    super(fileName);
  }

  public ErrorReportStream(String fileName, String csn)
      throws FileNotFoundException, UnsupportedEncodingException {
    super(fileName, csn);
  }

  public ErrorReportStream(File file) throws FileNotFoundException {
    super(file);
  }

  public ErrorReportStream(File file, String csn) throws FileNotFoundException,
      UnsupportedEncodingException {
    super(file, csn);
  }

}

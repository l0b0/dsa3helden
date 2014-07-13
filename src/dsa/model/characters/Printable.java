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
package dsa.model.characters;

import java.util.List;

import dsa.control.filetransforms.FileType;
import dsa.control.printing.Printer;

public interface Printable {

  String getName();

  String getPrintingTemplateFile();

  void setPrintingTemplateFile(String filePath);
  
  FileType getPrintingFileType();
  
  void setPrintingFileType(FileType fileType);

  String getPrintFile();

  void setPrintFile(String file);
  
  Printer getPrinter();
  
  boolean hasPrintingCustomizations();

  List<String> getFightingTalentsInDocument();

  void setFightingTalentsInDocument(List<String> talents);
  
  int getPrintingZFW();
  
  void setPrintingZFW(int zfw);

}

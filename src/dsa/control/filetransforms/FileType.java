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

public enum FileType {
  WordML, ODT, ExcelML, PDF, RTF, XML, HTML, Unknown;
  
  public String getExtension() {
    if (this == PDF) {
      return "pdf";
    }
    else if (this == WordML) {
      return "xml";
    }
    else if (this == ODT) {
      return "odt";
    }
    else if (this == ExcelML) {
      return "xml";
    }
    else if (this == RTF) {
      return "rtf";
    }
    else if (this == XML) {
      return "xml";
    }
    else if (this == HTML) {
      return "html";
    }
    else {
      return ""; 
    }
  }
  
  public String getDescription() {
    if (this == PDF) {
      return "PDF";
    }
    else if (this == WordML) {
      return "MS Word XML";
    }
    else if (this == ODT) {
      return "Open Document Format";
    }
    else if (this == ExcelML) {
      return "MS Excel XML";
    }
    else if (this == RTF) {
      return "Rich Text Format";
    }
    else if (this == XML) {
      return "Einfaches XML";
    }
    else if (this == HTML) {
      return "HTML";
    }
    else {
      return "Anderes Textformat";
    }
  }
}
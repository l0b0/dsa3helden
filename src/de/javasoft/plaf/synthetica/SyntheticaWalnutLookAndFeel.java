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
package de.javasoft.plaf.synthetica;

public class SyntheticaWalnutLookAndFeel extends SyntheticaLookAndFeel {

  public SyntheticaWalnutLookAndFeel() throws java.text.ParseException {
    super("walnut/synth.xml");
  }

  public String getID() {
    return "SyntheticaWalnutLookAndFeel";
  }

  public String getName() {
    return "Synthetica Walnut Look and Feel";
  }
}

/*
 Copyright (c) 2006-2009 [Joerg Ruedenauer]
 
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
package dsa.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class IExtraThingData {

  public static enum Type { Thing, Weapon, Armour, Shield, Container };
  
  public abstract Type getType();
  
  public abstract void store(PrintWriter out) throws IOException;
  
  public static IExtraThingData create(BufferedReader in, int lineNr) throws IOException {
    String line = in.readLine(); testEmpty(line);
    int version = parseInt(line, lineNr); 
    lineNr++;
    line = in.readLine(); testEmpty(line);
    int typeOrdinal = parseInt(line, lineNr);
    if (typeOrdinal < 0 || typeOrdinal >= Type.values().length) {
      throw new IOException("Unknown type");
    }
    if (typeOrdinal == Type.Container.ordinal()) {
      return new ContainerThingData(in, lineNr, version);
    }
    else {
      return new ExtraThingData(Type.values()[typeOrdinal], in, lineNr, version);
    }
  }
  
  protected static void testEmpty(String line) throws IOException {
    if (line == null) {
      throw new IOException("Premature end of data");
    }
  }
  
  protected static int parseInt(String line, int lineNr) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Not an integer: " + line + " (at line " + lineNr + ")");
    }
  }
  
}

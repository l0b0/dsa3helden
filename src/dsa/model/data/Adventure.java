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
package dsa.model.data;

public class Adventure {
  
  private String name;
  private int ap;
  private int index;
  private boolean changed;
  
  public int getIndex() {
    return index;
  }

  public Adventure(int index, String name, int ap) {
    this.name = name;
    this.ap = ap;
    this.index = index;
    changed = false;
  }

  public int getAp() {
    return ap;
  }

  public void setAp(int ap) {
    if (this.ap == ap) return;
    this.ap = ap;
    changed = true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (this.name.equals(name)) return;
    this.name = name;
    changed = true;
  }

  public boolean isChanged() {
    return changed;
  }

  public void setChanged(boolean changed) {
    this.changed = changed;
  }
  
 
}

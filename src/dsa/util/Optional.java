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
package dsa.util;

public class Optional<T extends Comparable<T>> implements
    Comparable<Optional<T>> {
  public boolean hasValue() {
    return value != null;
  }

  public String toString() {
    if (value != null)
      return value.toString();
    else
      return "-";
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public void clearValue() {
    value = null;
  }

  public Optional(T value) {
    this.value = value;
  }

  public Optional() {
    this.value = null;
  }

  T value;

  public int compareTo(Optional<T> other) {
    if (other.hasValue() && !hasValue()) return -1;
    if (!other.hasValue() && hasValue()) return 1;
    if (!other.hasValue() && !hasValue()) return 0;
    return ((Comparable<T>) value).compareTo(other.value);
  }

  public static final Optional<Integer> NULL_INT = new Optional<Integer>();
}

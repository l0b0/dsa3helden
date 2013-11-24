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
package dsa.model;

public class Date {

  public enum Month {
    Praios, Rondra, Efferd, Travia, Boron, Hesinde, Firun, Tsa, Phex, Peraine, Ingerimm, Rhaja, Namenloser
  }

  public enum Era {
    vor, nach
  }

  private int day;

  private int year;

  private Era era;

  private Month month;

  public Date(int day, Month month, int year) {
    this.day = day;
    this.month = month;
    this.year = Math.abs(year);
    this.era = year > 0 ? Era.nach : Era.vor;
  }

  public Date(int day, Month month, int year, Era era) {
    this.day = day;
    this.month = month;
    this.year = year;
    this.era = era;
  }

  public String format() {
    return "" + day + ". " + month + " " + year + " " + era + " Hal";
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public Era getEra() {
    return era;
  }

  public void setEra(Era era) {
    this.era = era;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }
}

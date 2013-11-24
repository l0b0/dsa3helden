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
package dsa.model;

import java.text.ParseException;
import java.util.StringTokenizer;

public class Date {

  public enum Month {
    Praios, Rondra, Efferd, Travia, Boron, Hesinde, Firun, Tsa, Phex, Peraine, Ingerimm, Rhaja, Namenloser
  }

  public enum Era {
    vor, nach
  }
  
  public enum Event {
    Hal, BF, Horas, EG, Rastullah
  }

  private int day;

  private int year;

  private Era era;

  private Month month;
  
  private Event event;
  
  private static int[] yearBases = { 2485, 1492, 0, 2177, 2251 };
  
  public static int rebaseYear(int year, Event oldEvent, Era oldEra, Event newEvent, Era newEra) {
    int base = 0;
    if (oldEra == Era.nach) {
      base = year + yearBases[oldEvent.ordinal()];
    }
    else {
      base = -year + yearBases[oldEvent.ordinal()];
    }
    if (newEra == Era.nach) {
      return base - yearBases[newEvent.ordinal()];
    }
    else {
      return yearBases[newEvent.ordinal()] - base;
    }
  }
  
  public static Date parse(String s) throws ParseException {
    StringTokenizer t = new StringTokenizer(s, " .");
    String dayS = t.nextToken();
    int day = -1;
    try {
      day = Integer.parseInt(dayS);
    }
    catch (NumberFormatException e) {
      throw new ParseException(s, 0);
    }
    if (day < 1 || day > 30) throw new ParseException(s, 0);
    String monthS = t.nextToken();
    Month month;
    try {
      month = Month.valueOf(monthS);
    }
    catch (IllegalArgumentException e) {
      throw new ParseException(s, 1);
    }
    if (month == Month.Namenloser && day > 5) {
      throw new ParseException(s, 1);
    }
    String yearS = t.nextToken();
    int year = 0;
    try {
      year = Integer.parseInt(yearS);
    }
    catch (NumberFormatException e) {
      throw new ParseException(s, 2);
    }
    String eraS = t.nextToken();
    Era era;
    try {
      era = Era.valueOf(eraS);
    }
    catch (IllegalArgumentException e) {
      throw new ParseException(s, 3);
    }
    Event event = Event.Hal;
    if (t.hasMoreTokens()) {
      String eventS = t.nextToken();
      try {
        event = Event.valueOf(eventS);
      }
      catch (IllegalArgumentException e) {
        throw new ParseException(s, 4);
      }
    }
    return new Date(day, month, year, era, event);
  }
  
  public int yearDifference(Date other) {
    int baseYear = rebaseYear(year, event, era, Event.Horas, Era.nach);
    int otherBaseYear = rebaseYear(other.year, other.event, other.era, Event.Horas, Era.nach);
    return baseYear - otherBaseYear;
  }

  public Date(int day, Month month, int year) {
    this.day = day;
    this.month = month;
    this.year = Math.abs(year);
    this.era = year > 0 ? Era.nach : Era.vor;
    this.event = Event.Hal;
  }

  public Date(int day, Month month, int year, Era era, Event event) {
    this.day = day;
    this.month = month;
    this.year = year;
    this.era = era;
    this.event = event;
  }

  public String format() {
    return "" + day + ". " + month + " " + year + " " + era + " " + event;
  }
  
  public String toString() {
    return format();
  }
  
  public boolean equals(Object other) {
    if (other instanceof Date) {
      return equals((Date)other);
    }
    else return false;
  }
  
  public boolean isSameDate(Date other) {
    int baseYear = rebaseYear(year, event, era, Event.Horas, Era.nach);
    int otherBaseYear = rebaseYear(other.year, other.event, other.era, Event.Horas, Era.nach);
    return day == other.day && month == other.month && baseYear == otherBaseYear;
  }
  
  public boolean equals(Date other) {
    return day == other.day && month == other.month && year == other.year
      && era == other.era && event == other.event;
  }
  
  public int hashCode() {
    int baseYear = rebaseYear(year, event, era, Event.Horas, Era.nach);
    return 30 * day + 12 * month.ordinal() + baseYear + 2 * era.ordinal() + 5 * event.ordinal();
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

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }

  public void setEraAndEventAndRebaseYear(Era newEra, Event newEvent) {
    year = rebaseYear(year, event, era, newEvent, newEra);
    era = newEra;
    event = newEvent;
  }
  
}

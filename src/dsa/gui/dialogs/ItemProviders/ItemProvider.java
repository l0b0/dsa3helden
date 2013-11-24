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
package dsa.gui.dialogs.ItemProviders;

import java.util.regex.Pattern;

import dsa.gui.tables.AbstractTable;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Tradezones;

public abstract class ItemProvider {

  public ItemProvider(AbstractTable table) {
    mTable = table;
  }
  
  public int getDefaultPrice(String item) { 
    return 0;
  }
  
  public Thing.Currency getCurrency(String item) {
    return Thing.Currency.S;
  }
  
  public String[] getTradezones(String item) {
    Thing thing = Things.getInstance().getThing(item);
    if (thing != null) {
      return thing.getTradezones();
    }
    else
      return Tradezones.getInstance().getTradezoneIDs();
  }
  
  public final AbstractTable getTable() {
    return mTable;
  }
  
  private Pattern mFilter = Pattern.compile(".*");
  
  private AbstractTable mTable;

  protected final boolean isDisplayed(String item) {
    return mFilter.matcher(item.toLowerCase(java.util.Locale.GERMAN)).matches();
  }

  public void setFilter(String s) {
    if (s == null) s = "";
    if (s.equals("")) s = "*";
    if (!s.endsWith("*")) s = s + "*";
    s = s.replaceAll("\\*", ".*");
    s = s.toLowerCase(java.util.Locale.GERMAN);
    try {
      mFilter = Pattern.compile(s);
    }
    catch (java.util.regex.PatternSyntaxException e) {
      mFilter = Pattern.compile(".*");
    }    
  }
  
  public abstract void fillTable(boolean showSingularItems);
}

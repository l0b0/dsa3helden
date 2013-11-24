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
package dsa.gui.tables;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import dsa.model.data.ExtraThingData;

public class ThingTransfer implements Transferable {

  public enum Flavors {
    Thing, Weapon, Armour, Shield
  }

  public ThingTransfer(Flavors flavor, String value, ExtraThingData extraData) {
    this.flavor = flavor;
    this.value = value;
    this.extraData = extraData;
  }

  private final Flavors flavor;

  private final String value;
  
  private final ExtraThingData extraData;

  public static class ThingFlavor extends DataFlavor {
    public ThingFlavor(String mimeType, Flavors flavor)
        throws ClassNotFoundException {
      super(mimeType);
      this.flavor = flavor;
    }
    
    public Flavors getFlavor() {
      return flavor;
    }

    private final Flavors flavor;
  }

  private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType
      + ";class=java.lang.String";

  private static DataFlavor createFlavor(Flavors flavor) {
    try {
      return new ThingFlavor(MIME_TYPE, flavor);
    }
    catch (ClassNotFoundException e) {
      assert (false);
      return null;
    }
  }

  public static final DataFlavor THING_FLAVOR = createFlavor(Flavors.Thing);

  public static final DataFlavor WEAPON_FLAVOR = createFlavor(Flavors.Weapon);

  public static final DataFlavor ARMOUR_FLAVOR = createFlavor(Flavors.Armour);

  public static final DataFlavor SHIELD_FLAVOR = createFlavor(Flavors.Shield);

  private static final DataFlavor[] FLAVORS = { THING_FLAVOR, WEAPON_FLAVOR,
      ARMOUR_FLAVOR, SHIELD_FLAVOR };

  public DataFlavor[] getTransferDataFlavors() {
    if (flavor == Flavors.Thing) {
      DataFlavor[] flavors = new DataFlavor[4];
      System.arraycopy(FLAVORS, 0, flavors, 0, 4);
      return flavors;
    }
    else {
      DataFlavor[] flavors = new DataFlavor[2];
      flavors[0] = FLAVORS[flavor.ordinal()];
      flavors[1] = THING_FLAVOR;
      return flavors;
    }
  }

  public boolean isDataFlavorSupported(DataFlavor aFlavor) {
    if (!(aFlavor instanceof ThingFlavor)) return false;
    ThingFlavor other = (ThingFlavor) aFlavor;
    if (this.flavor == Flavors.Thing) {
      return true;
    }
    else if (this.flavor == Flavors.Weapon) {
      return other.flavor == Flavors.Thing || other.flavor == Flavors.Weapon;
    }
    else if (this.flavor == Flavors.Armour) {
      return other.flavor == Flavors.Thing || other.flavor == Flavors.Armour;
    }
    else if (this.flavor == Flavors.Shield) {
      return other.flavor == Flavors.Thing || other.flavor == Flavors.Shield;
    }
    else
      return false;
  }

  public Object getTransferData(DataFlavor aFlavor)
      throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(aFlavor)) return null;
    StringWriter s = new StringWriter();
    PrintWriter out = new PrintWriter(new BufferedWriter(s));
    out.println(value);
    extraData.store(out);
    out.flush();
    return s.toString();
  }

}

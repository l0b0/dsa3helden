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
package dsa.control.printing;

import java.io.File;

import dsa.control.filetransforms.FileTransformer;
import dsa.control.filetransforms.FileType;
import dsa.control.filetransforms.TransformerFactory;
import dsa.model.characters.Energy;
import dsa.model.characters.Hero;
import dsa.model.characters.Printable;
import dsa.model.characters.Property;
import dsa.model.data.Adventure;
import dsa.model.data.Talents;
import dsa.model.talents.Talent;
import dsa.util.LookupTable;

abstract class AbstractPrinter implements Printer {
  
  public final void print(Printable printable, File template, File output,
      FileType fileType, java.awt.Component parent, String message)
      throws java.io.IOException {
    LookupTable table = new LookupTable('!');
    
    fillTable(printable, table);
    
    FileTransformer transformer = TransformerFactory.createTransformer(fileType);
    transformer.setInputFile(template);
    transformer.setOutputFile(output);
    transformer.setLookupTable(table);
    transformer.transform(parent, message);    
  }
  
  protected abstract void fillTable(Printable printable, LookupTable table);

  protected static int getCategoryNumber(String category) {
    if (category.startsWith("Kampf")) {
      return 1;
    }
    else if (category.startsWith("Körperliche")) {
      return 2;
    }
    else if (category.startsWith("Gesellschaft")) {
      return 3;
    }
    else if (category.startsWith("Natur")) {
      return 4;
    }
    else if (category.startsWith("Wissen")) {
      return 5;
    }
    else if (category.startsWith("Handwerk")) {
      return 6;
    }
    else if (category.startsWith("Intuitiv")) {
      return 7;
    }
    else {
      return 8;
    }
  }

  protected static void printBasicAttributes(Hero character, LookupTable table, String app) {
    if (character != null) {
      table.addItem("name" + app, character.getName());
      table.addItem("typ" + app, character.getType());
      table.addItem("ort" + app, character.getBirthPlace());
      table.addItem("gro" + app, character.getHeight());
      table.addItem("gew" + app, character.getWeight());
      table.addItem("haar" + app, character.getHairColor());
      table.addItem("sex" + app, character.getSex());
      table.addItem("alt" + app, character.getAge());
      table.addItem("geb" + app, character.getBirthday().toString());
      table.addItem("gott" + app, character.getGod());
      table.addItem("augen" + app, character.getEyeColor());
      table.addItem("skin" + app, character.getSkinColor());
      table.addItem("stand" + app, character.getStand());
      table.addItem("gs" + app, 10);
  
      table.addItem("ap" + app, character.getAP());
      table.addItem("ruf" + app, character.getRuf());
      int step = character.getStep();
      table.addItem("stufe" + app, step);
      table.addItem("nst" + app, (step * (step + 1) * 50));
      table.addItem("APminus" + app, ((step * (step + 1) * 50) - character.getAP()));
      table.addItem("titel" + app, character.getTitle());
      table.addItem("haut" + app, character.getSkin());
      table.addItem("SO" + app, character.getSO());
    }
    else {
      table.addItem("name" + app, "");
      table.addItem("typ" + app, "");
      table.addItem("ort" + app, "");
      table.addItem("gro" + app, "");
      table.addItem("gew" + app, "");
      table.addItem("haar" + app, "");
      table.addItem("sex" + app, "");
      table.addItem("alt" + app, "");
      table.addItem("geb" + app, "");
      table.addItem("gott" + app, "");
      table.addItem("augen" + app, "");
      table.addItem("skin" + app, "");
      table.addItem("stand" + app, "");
      table.addItem("gs" + app, "");
  
      table.addItem("ap" + app, "");
      table.addItem("ruf" + app, "");
      table.addItem("stufe" + app, "");
      table.addItem("nst" + app, "");
      table.addItem("APminus" + app, "");
      table.addItem("titel" + app, "");
      table.addItem("haut" + app, "");    
      table.addItem("SO" + app, "");
    }
  }

  protected static void printProperties(Hero character, LookupTable table, String app) {
    if (character != null) {
      for (int i = 0; i < 7; ++i) {
        table.addItem("p" + (i + 1) + app, character.getDefaultProperty(Property
            .values()[i]));
      }
      table.addItem("n1" + app, character.getDefaultProperty(Property.AG));
      table.addItem("n2" + app, character.getDefaultProperty(Property.HA));
      table.addItem("n3" + app, character.getDefaultProperty(Property.RA));
      table.addItem("n4" + app, character.getDefaultProperty(Property.TA));
      for (int i = 4; i < 7; ++i) {
        table.addItem("n" + (i + 1) + app, character.getDefaultProperty(Property
            .values()[i + 7]));
      }
    }
    else {
      for (int i = 0; i < 7; ++i) {
        table.addItem("p" + (i + 1) + app, "");
      }
      table.addItem("n1" + app, "");
      table.addItem("n2" + app, "");
      table.addItem("n3" + app, "");
      table.addItem("n4" + app, "");
      for (int i = 4; i < 7; ++i) {
        table.addItem("n" + (i + 1) + app, "");
      }      
    }
  }

  protected static void printEnergies(Hero character, LookupTable table, String app) {
    if (character != null) {
      table.addItem("le" + app, character.getDefaultEnergy(Energy.LE));
      table.addItem("ae" + app, character.getDefaultEnergy(Energy.AE));
      table.addItem("ke" + app, character.getDefaultEnergy(Energy.KE));
      table.addItem("ko" + app, character.getDefaultEnergy(Energy.KO));
      table.addItem("aus" + app, character.getDefaultEnergy(Energy.AU));
      String ake = "";
      if (character.hasEnergy(Energy.KE)) ake = "" + character.getDefaultEnergy(Energy.KE);
      else if (character.hasEnergy(Energy.AE)) ake = "" + character.getDefaultEnergy(Energy.AE);
      table.addItem("ake" + app, ake);
    }
    else {
      table.addItem("le" + app, "");
      table.addItem("ae" + app, "");
      table.addItem("ke" + app, "");
      table.addItem("ko" + app, "");
      table.addItem("aus" + app, ""); 
      table.addItem("ake" + app, "");
    }
  }

  protected static void printDerivedValues(Hero character, LookupTable table, String app) {
    if (character != null) {
      table.addItem("mb" + app, character.getMRBonus());
      table.addItem("mr" + app, character.getDefaultDerivedValue(Hero.DerivedValue.MR));
      table.addItem("atb" + app, character.getDefaultDerivedValue(Hero.DerivedValue.AT));
      table.addItem("pab" + app, character.getDefaultDerivedValue(Hero.DerivedValue.PA));
      table.addItem("fkb" + app, character.getDefaultDerivedValue(Hero.DerivedValue.FK));
      table.addItem("auw" + app, character.getDefaultDerivedValue(Hero.DerivedValue.AW));
    }
    else {
      table.addItem("mb" + app, "");
      table.addItem("mr" + app, "");
      table.addItem("atb" + app, "");
      table.addItem("pab" + app, "");
      table.addItem("fkb" + app, "");
      table.addItem("auw" + app, "");      
    }
  }

  protected static void printSpecialTalents(Hero character, LookupTable table, String app) {
    if (character != null) {
      table.addItem("mf" + app, character.getDefaultTalentValue("Jagen (Falle)"));
      table.addItem("mp" + app, character.getDefaultTalentValue("Jagen (Pirsch)"));
      char appendix = 'a';
      for (Talent talent : Talents.getInstance().getTalentsInCategory(
          "Berufe / Sonstiges")) {
        if (talent.getName().startsWith("Jagen (")) continue;
        if (character.hasTalent(talent.getName())) {
          table.addItem("bn" + appendix + app, talent.getName());
          table.addItem("8" + appendix + app, character.getDefaultTalentValue(talent
              .getName()));
          ++appendix;
        }
      }
      while (appendix != 'l') {
        table.addItem("bn" + appendix + app, "");
        table.addItem("8" + appendix + app, "");
        ++appendix;
      }
    }
    else {
      table.addItem("mf" + app, "");
      table.addItem("mp" + app, "");
      char appendix = 'a';
      while (appendix != 'l') {
        table.addItem("bn" + appendix + app, "");
        table.addItem("8" + appendix + app, "");
        ++appendix;
      }
    }
  }

  protected static void printTalents(Hero character, LookupTable table, String app) {
    for (String category : Talents.getInstance().getKnownCategories()) {
      if (category.equals("Favoriten")) continue;
      if (category.startsWith("Berufe")) continue;
      if (category.equals("Zauber")) continue;
      char appendix = 'a';
      int catNr = getCategoryNumber(category);
      for (Talent talent : Talents.getInstance().getTalentsInCategory(category)) {
        if (character != null) {
          table.addItem("" + catNr + appendix + app, 
              character.getDefaultTalentValue(talent.getName()));
        }
        else {
          table.addItem("" + catNr + appendix + app, "");
        }
        appendix++;
      }
    }

    printSpecialTalents(character, table, app);
  }

  protected static void printMagicAttributes(Hero character, LookupTable table, String app) {
    if (character != null) {
      table.addItem("Ele" + app, character.getElement());
      table.addItem("SeT" + app, character.getSoulAnimal());
      table.addItem("Aka" + app, character.getAcademy());
      table.addItem("SpG" + app, character.getSpecialization());
    }
    else {
      table.addItem("Ele" + app, "");
      table.addItem("SeT" + app, "");
      table.addItem("Aka" + app, "");
      table.addItem("SpG" + app, "");      
    }
  }

  protected static void printAdventures(Hero character, LookupTable table) {
    if (character != null) {
      Adventure[] adventures = character.getAdventures();
      for (Adventure adventure : adventures) {
        table.addItem("Abenteuer", adventure.getName(), true);
        table.addItem("AbtAP", adventure.getAP(), true);
      }
      if (adventures.length == 0) {
        table.addItem("Abenteuer", "");
        table.addItem("AbtAP", "");        
      }
    }
    else {
      table.addItem("Abenteuer", "");
      table.addItem("AbtAP", "");
    }
  }

}

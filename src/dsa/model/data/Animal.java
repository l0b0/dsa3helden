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
package dsa.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import dsa.model.AbstractThingCarrier;
import dsa.model.DiceSpecification;
import dsa.model.characters.Group;
import dsa.model.data.Thing.Currency;

public class Animal extends AbstractThingCarrier implements Cloneable {

  public interface Listener {
    void nameChanged(String oldName, String newName);
  }

  public enum AttributeType {
    eString, eInt, eDicing, eDiced, eSpeed, eMultiplier
  }

  private final ArrayList<Listener> listeners = new ArrayList<Listener>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public static class SpeedData implements Cloneable {
    private final int value1;

    private final int value2;

    public String toString() {
      return "" + value1 + "/" + value2;
    }

    public SpeedData(int v1, int v2) {
      value1 = v1;
      value2 = v2;
    }

    public static SpeedData parse(String text, int lineNr) throws IOException {
      StringTokenizer t = new StringTokenizer(text, "/");
      if (t.countTokens() != 2)
        throw new IOException("Zeile " + lineNr
            + ": keine Geschwindigkeitswerte!");
      int v1 = parseInt(t.nextToken(), lineNr);
      int v2 = parseInt(t.nextToken(), lineNr);
      return new SpeedData(v1, v2);
    }

    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  }

  static class AttributeMetaInfo implements Cloneable {
    String title;

    AttributeType type = null;

    boolean addedWithSteps;

    boolean testable;

    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }
  }

  private String category;

  int step;

  protected boolean changed;

  ArrayList<Object> attributes;

  ArrayList<AttributeMetaInfo> attributeInfos;

  public String getCategory() {
    return category;
  }

  public int getStep() {
    return step;
  }

  public Object getAttributeValue(int index) {
    return attributes.get(index);
  }

  public String getAttributeTitle(int index) {
    return attributeInfos.get(index).title;
  }

  public AttributeType getAttributeType(int index) {
    return attributeInfos.get(index).type;
  }

  public boolean isAttributeTestable(int index) {
    return attributeInfos.get(index).testable;
  }

  public int getNrOfAttributes() {
    return attributes.size();
  }

  private String name;

  public String getName() {
    return name;
  }

  public boolean isChanged() {
    return changed;
  }

  public Animal(String category) {
    this.category = category;
    step = 1;
    attributes = new ArrayList<Object>();
    attributeInfos = new ArrayList<AttributeMetaInfo>();
    name = "";
    changed = false;
  }

  private static final int VERSION = 4;

  public void writeToFile(PrintWriter out) throws IOException {
    printSubclassSpecificData(out);
    out.println(VERSION);
    // version 1
    out.println(name);
    out.println(category);
    out.println(step);
    out.println(attributes.size());
    for (int i = 0; i < attributes.size(); ++i) {
      out.println(attributeInfos.get(i).type.ordinal());
      out.println(attributes.get(i).toString());
      out.println(attributeInfos.get(i).addedWithSteps ? 1 : 0);
      out.println(attributeInfos.get(i).testable ? 1 : 0);
      out.println(attributeInfos.get(i).title);
    }
    // version 2
    String[] things = getThings();
    out.println(things.length);
    for (String thing : things) {
      out.println(thing);
      out.println(getThingCount(thing));
    }
    // version 3
    printExtraThingData(out);
    // version 4
    printContainers(out);
    out.println("-- End of Animal --");
    changed = false;
  }

  protected void printSubclassSpecificData(PrintWriter out) throws IOException {
    // nothing to do in this class
  }

  public int readFromFile(BufferedReader in, int lineNr) throws IOException {
    lineNr = readSubclassSpecificData(in, lineNr);
    String line = in.readLine();
    lineNr++;
    testEmpty(line);
    int version = parseInt(line, lineNr);
    attributes.clear();
    attributeInfos.clear();
    if (version > 0) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      name = line;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      category = line;
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      step = parseInt(line, lineNr);
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      int attrCount = parseInt(line, lineNr);
      for (int i = 0; i < attrCount; ++i) {
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        AttributeType type = AttributeType.values()[parseInt(line, lineNr)];
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        Object value = parseValue(type, line, lineNr);
        if (value == null)
          throw new IOException("Zeile " + (lineNr - 1)
              + ": wrong attribute type!");
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        boolean addedAtStep = ("1".equals(line));
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        boolean testable = ("1".equals(line));
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        AttributeMetaInfo info = new AttributeMetaInfo();
        info.addedWithSteps = addedAtStep;
        info.type = type;
        info.title = line;
        info.testable = testable;
        attributeInfos.add(info);
        attributes.add(value);
      }
    }
    clearThings();
    if (version > 1) {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
      int thingCount = parseInt(line, lineNr);
      for (int i = 0; i < thingCount; ++i) {
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        String thing = line;
        line = in.readLine();
        lineNr++;
        testEmpty(line);
        int thingNr = parseInt(line, lineNr);
        addThingsFromFile(thing, name, thingNr);
      }
    }
    clearExtraThingData();
    if (version > 2) {
      lineNr = readExtraThingData(in, lineNr, 1);
    }
    clearThingContainers();
    if (version > 3) {
      lineNr = readContainers(in, lineNr);
    }
    initForOldVersions(name);
    do {
      line = in.readLine();
      lineNr++;
      testEmpty(line);
    } while (!"-- End of Animal --".equals(line));
    changed = false;
    return lineNr;
  }

  private static Object parseValue(AttributeType type, String line, int lineNr)
      throws IOException {
    Object value = null;
    if (type == AttributeType.eString) {
      value = line;
    }
    else if (type == AttributeType.eInt) {
      value = parseInt(line, lineNr);
    }
    else if (type == AttributeType.eDicing) {
      try {
        value = DiceSpecification.parse(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": " + e.getMessage());
      }
    }
    else if (type == AttributeType.eSpeed) {
      value = SpeedData.parse(line, lineNr);
    }
    else if (type == AttributeType.eDiced) {
      try {
        value = DiceSpecification.parse(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": " + e.getMessage());
      }
    }
    else if (type == AttributeType.eMultiplier) {
      try {
        value = Double.parseDouble(line);
      }
      catch (NumberFormatException e) {
        throw new IOException("Zeile " + lineNr + ": " + e.getMessage());
      }
    }
    return value;
  }

  protected int readSubclassSpecificData(BufferedReader in, int lineNr)
      throws IOException {
    return lineNr;
  }

  public void setCategory(String category) {
    this.category = category;
    changed = true;
  }

  public void setName(String name) {
    String oldName = this.name;
    this.name = name;
    changed = true;
    for (Listener l : listeners)
      l.nameChanged(oldName, name);
  }

  public void setStep(int step) {
    this.step = step;
    changed = true;
  }

  public void setAttributeValue(int index, Object value) {
    if (index < 0 || index >= attributes.size()) return;
    if (value == null) return;
    String test = value.toString();
    try {
      parseValue(attributeInfos.get(index).type, test, 0);
    }
    catch (IOException e) {
      throw new NumberFormatException(test);
    }
    attributes.set(index, value);
    changed = true;
  }

  public Object clone() throws CloneNotSupportedException {
    Animal clone = (Animal) super.clone();
    clone.attributes = new ArrayList<Object>();
    for (int i = 0; i < attributes.size(); ++i) {
      Object o = attributes.get(i);
      if (o instanceof String) {
        clone.attributes.add(o.toString());
      }
      else if (o instanceof DiceSpecification) {
        clone.attributes.add(((DiceSpecification) o).clone());
      }
      else if (o instanceof SpeedData) {
        clone.attributes.add(((SpeedData) o).clone());
      }
      else if (o instanceof Integer) {
        clone.attributes.add(((Integer) o).intValue());
      }
      else
        clone.attributes.add(o);
    }
    clone.attributeInfos = new ArrayList<AttributeMetaInfo>();
    for (int i = 0; i < attributeInfos.size(); ++i) {
      clone.attributeInfos.add((AttributeMetaInfo) attributeInfos.get(i)
          .clone());
    }
    clone.changed = false;
    return clone;
  }
  
  protected void setChanged() {
    changed = true;
  }

  /* (non-Javadoc)
   * @see dsa.model.ThingCarrier#isDifference()
   */
  @Override
  public boolean isDifference() {
    return Group.getInstance().getActiveHero().isDifference();
  }

  /* (non-Javadoc)
   * @see dsa.model.ThingCarrier#pay(int, dsa.model.data.Thing.Currency)
   */
  @Override
  public void pay(int price, Currency currency) {
    Group.getInstance().getActiveHero().pay(price, currency);
  }
  
}

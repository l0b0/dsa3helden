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
package dsa.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import dsa.model.characters.CharacterObserver;
import dsa.model.data.ContainerThingData;
import dsa.model.data.ExtraThingData;
import dsa.model.data.IExtraThingData;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.util.AbstractObservable;

public abstract class AbstractThingCarrier extends AbstractObservable<CharacterObserver> 
implements ThingCarrier, Cloneable {

  public Object clone() throws CloneNotSupportedException {
    AbstractThingCarrier carrier = (AbstractThingCarrier)super.clone();
    carrier.things = new HashMap<String, Integer>();
    carrier.things.putAll(things);
    carrier.extraThingData = new HashMap<String, ExtraThingData>();
    carrier.extraThingData.putAll(extraThingData);
    carrier.thingContainers = new HashMap<String, String>(thingContainers);
    return carrier;
  }

  public final void removeThing(String thingName, boolean removeContents) {
    if (!things.containsKey(thingName)) return;
    int count = things.get(thingName);
    extraThingData.remove(thingName + count);
    String[] contents = getThingsInContainer(thingName);
    for (String thing : contents) {
      if (thingContainers.get(thing).equals(thingName)) {
        if (removeContents) {
          removeThing(thing, true);
        }
        else {
          thingContainers.put(thing, "Ausrüstung");
        }
      }
    }
    if (count == 1) {
      things.remove(thingName);
      thingContainers.remove(thingName);
      for (CharacterObserver o : observers)
        o.thingRemoved(thingName, false);
    }
    else {
      things.put(thingName, count - 1);
    }
    setChanged();
    fireWeightChanged();
  }
  
  public final IExtraThingData getExtraThingData(String thing, int thingNumber) {
    ExtraThingData data = extraThingData.get(thing + thingNumber);
    Thing t = Things.getInstance().getThing(thing);
    if (t != null && t.isContainer()) {
      String[] thingsInContainer = getThingsInContainer(thing);
      int[] thingCounts = new int[thingsInContainer.length];
      int count = 0;
      for (int i = 0; i < thingsInContainer.length; ++i) {
        thingCounts[i] = getThingCount(thingsInContainer[i]);
        count += thingCounts[i];
      }
      IExtraThingData[] subData = new IExtraThingData[count];
      count = 0;
      for (int i = 0; i < thingsInContainer.length; ++i) {
        for (int j = 0; j < getThingCount(thingsInContainer[i]); ++j) {
          subData[count++] = getExtraThingData(thingsInContainer[i], j + 1);
        }
      }
      return new ContainerThingData(data, thingsInContainer, thingCounts, subData);
    }
    else {
      return data;
    }
  }
  
  public final int getThingCount(String thing) {
    if (!things.containsKey(thing))
      return 0;
    else
      return things.get(thing);
  }

  public final String[] getThings() {
    String[] allThings = new String[things.size()];
    return things.keySet().toArray(allThings);
  }
  
  public final String[] getThingsInContainer(String container) {
    ArrayList<String> result = new ArrayList<String>();
    for (String thing : thingContainers.keySet()) {
      if (thingContainers.get(thing).equals(container)) {
        result.add(thing);
      }
    }
    String[] array = new String[result.size()];
    return result.toArray(array);
  }
  
  public final String addThing(String thingName, String container) {
    ExtraThingData data = new ExtraThingData(ExtraThingData.Type.Thing);
    Thing thing = Things.getInstance().getThing(thingName);
    if (thing != null) {
      data.setProperty("Worth", thing.getValue().hasValue() ? thing.getValue().getValue() : 0);
      data.setProperty("Weight", thing.getWeight());
      data.setProperty("Category", thing.getCategory());
      data.setProperty("Singular", thing.isSingular() ? 1 : 0);      
    }
    return addThing(thingName, data, container);
  }

  public final String addThing(String thingName, IExtraThingData extraData, String container) {
    String result = internalAddThing(thingName, extraData, container);
    setChanged();
    fireWeightChanged();
    return result;
  }
  
  public final boolean canMoveTo(String thingName, String newContainer) {
    if (!things.containsKey(thingName) || !thingContainers.containsKey(thingName)) {
      return false;
    }
    Thing thing = Things.getInstance().getThing(thingName);
    if (thing != null && !thing.isContainer()) {
      // non-containers can be move everywhere
      return true;
    }
    // containers can not be moved into themselves, even transitive
    String container = newContainer;
    while (true) {
      if (thingName.equals(container)) {
        return false;
      }
      if (!thingContainers.containsKey(container)) {
        return true;
      }
      container = thingContainers.get(container);
    }
  }
  
  public final String moveThing(String thingName, String newContainer) {
    if (thingContainers.containsKey(thingName) && thingContainers.get(thingName).equals(newContainer)) {
      return thingName;
    }
    String result = thingName;
    Thing thing = Things.getInstance().getThing(thingName);
    if (thing != null && !thing.isContainer()) {
      // not a container. Check if such a thing is already in the new container
      String[] thingsInContainer = getThingsInContainer(newContainer);
      for (String existing : thingsInContainer) {
        if (Things.getInstance().getThing(existing) == thing) {
          // found some!
          result = existing;
          // move _one_ from old to new container
          things.put(result, things.get(result) + 1);
          int count = things.get(thingName) - 1;
          if (count == 0) {
            // none remain in old container (with different name)
            things.remove(thingName);
            thingContainers.remove(thingName);
          }
          else {
            things.put(thingName, count);
          }
          String oldKey = thingName + (count + 1);
          extraThingData.put(result + things.get(result), extraThingData.get(oldKey));
          extraThingData.remove(oldKey);
          setChanged();
          fireWeightChanged();
          return result;
        }
      }
      // not in the container, not itself a container
      if (things.get(thingName) == 1) {
        // it's just one
        thingContainers.put(thingName, newContainer);
        setChanged();
        fireWeightChanged();
        return result;
      }
      else {
        // move _one_ from old to new container
        // first, get a new different name
        int i = 2;
        String realThingName = Things.getInstance().getThing(thingName).getName();
        if (things.containsKey(realThingName)) {
          result = realThingName + " " + i;
          while (things.containsKey(result)) {
            result = realThingName + " " + (++i);
          }
        }
        else {
          result = realThingName;
        }
        things.put(result, 1);
        thingContainers.put(result, newContainer);
        String oldKey = thingName + things.get(thingName);
        things.put(thingName, things.get(thingName) - 1);
        extraThingData.put(result + things.get(result), extraThingData.get(oldKey));
        extraThingData.remove(oldKey);
        setChanged();
        fireWeightChanged();
        return result;
      }
    }
    else {
      // it is a container --> containers are always unique
      thingContainers.put(thingName, newContainer);
      setChanged();
      fireWeightChanged();
      return result;
    }
  }
  
  public final String getThingContainer(String thing) {
    if (thingContainers.containsKey(thing)) {
      return thingContainers.get(thing);
    }
    else {
      return "";
    }
  }
  
  public final void fireWeightChanged() {
    for (CharacterObserver o : observers) {
      o.weightChanged();
    }
  }

  protected abstract void setChanged();
  
  protected final void clearThings() {
    things.clear();
  }
  
  protected final void clearThingContainers() {
    thingContainers.clear();
  }
  
  protected final void clearExtraThingData() {
    extraThingData.clear();
  }

  protected final void printExtraThingData(PrintWriter file) throws IOException {
    file.println(extraThingData.size());
    for (Map.Entry<String, ExtraThingData> entry : extraThingData.entrySet()) {
      file.println(entry.getKey());
      entry.getValue().store(file);
    }
  }

  protected final int readExtraThingData(BufferedReader file, int lineNr, int count) throws IOException {
    for (int i = 0; i < count; ++i) {
      String line = file.readLine();
      lineNr++;
      testEmpty(line);
      int count2 = parseInt(line, lineNr);
      for (int j = 0; j < count2; ++j) {
        String key = file.readLine();
        lineNr++;
        testEmpty(line);
        ExtraThingData extraData = (ExtraThingData)IExtraThingData.create(file, lineNr);
        extraThingData.put(key, extraData);
      }
    }
    return lineNr;
  }

  protected final String internalAddThing(String thingName, IExtraThingData extraData, String container) {
    if (extraData.getType() == IExtraThingData.Type.Container) {
      ContainerThingData containerData = (ContainerThingData)extraData;
      IExtraThingData dataOfContainer = containerData.getDataOfContainer();
      String containerName = internalAddThing(thingName, dataOfContainer, container);
      int count = 0;
      for (int i = 0; i < containerData.getThingsInContainer().length; ++i) {
        for (int j = 0; j < containerData.getThingCounts()[i]; ++j) {
          internalAddThing(containerData.getThingsInContainer()[i], containerData.getSubData()[count++], containerName);
        }
      }
      return containerName;
    }
    else {
      String result = thingName;
      if (!things.containsKey(thingName)) {
        things.put(thingName, 1);
      }
      else {
        Thing thing = Things.getInstance().getThing(thingName);
        if (thing != null && thing.isContainer()) {
          int count = 1;
          while (things.containsKey(result)) {
            ++count;
            result = thingName +  " " + count;
          }
          things.put(result, 1);
        }
        else {
          int count = 1;
          while (things.containsKey(result)) {
            if (thingContainers.get(result).equals(container)) {
              break;
            }
            ++count;
            result = thingName + " " + count;
          }       
          if (things.containsKey(result)) {
            things.put(result, things.get(result) + 1);
          }
          else {
            things.put(result, 1);
          }
        }
      }
      extraThingData.put(result + things.get(result), (ExtraThingData)extraData);
      thingContainers.put(result, container);
      return result;
    }
  }
  
  protected final void addThingFromFile(String name, String container, ExtraThingData extraData) {
    if (things.containsKey(name)) {
      things.put(name, things.get(name) + 1);
    }
    else {
      things.put(name, 1);
      thingContainers.put(name, container);
    }
    extraThingData.put(name + things.get(name), extraData); 
  }
  
  protected final void addThingsFromFile(String name, String container, int count) {
    things.put(name, count);
    thingContainers.put(name, container);
    Thing thing = Things.getInstance().getThing(name);
    if (thing != null) {
      for (int i = 1; i <= count; ++i) {
        ExtraThingData data = new ExtraThingData(ExtraThingData.Type.Thing);
        data.setProperty("Worth", thing.getValue().hasValue() ? thing.getValue().getValue() : 0);
        data.setProperty("Weight", thing.getWeight());
        data.setProperty("Category", thing.getCategory());
        data.setProperty("Singular", thing.isSingular() ? 1 : 0);      
        extraThingData.put(name + i, data);
      }
    }
  }

  protected final void printContainers(PrintWriter file) {
    file.println(thingContainers.size());
    for (String thing : thingContainers.keySet()) {
      file.println(thing + ";" + thingContainers.get(thing));
    }
  }

  protected final int readContainers(BufferedReader file, int lineNr) throws IOException {
    String line;
    lineNr++;
    line = file.readLine();
    testEmpty(line);
    int count = parseInt(line, lineNr);
    for (int i = 0; i < count; ++i) {
      lineNr++;
      line = file.readLine();
      StringTokenizer tokenizer = new StringTokenizer(line, ";");
      if (tokenizer.countTokens() != 2) {
        throw new IOException("Formatfehler in Zeile " + lineNr);
      }
      String thing = tokenizer.nextToken();
      String container = tokenizer.nextToken();
      if (things.containsKey(thing)) {
        thingContainers.put(thing, container);
      }
    }
    return lineNr;
  }
  
  protected final void initForOldVersions(String defaultContainer) {
    for (String thing : things.keySet()) {
      for (int i = 1; i <= things.get(thing); ++i) {
        if (!extraThingData.containsKey(thing + i)) {
          extraThingData.put(thing + i, new ExtraThingData(ExtraThingData.Type.Thing));
        }
      }
      if (!thingContainers.containsKey(thing)) {
        thingContainers.put(thing, defaultContainer);
      }
    }    
  }

  protected static int parseInt(String line, int lineNr) throws IOException {
    try {
      return Integer.parseInt(line);
    }
    catch (NumberFormatException e) {
      throw new IOException("Zeile " + lineNr + ": " + line
          + " ist keine Zahl!");
    }
  }
  
  protected final boolean isInWarehouse(String thing) {
    String runner = thing;
    while (thingContainers.containsKey(runner)) {
      runner = thingContainers.get(runner);
      if (runner.equals("Lager")) {
        return true;
      }
    }
    return false;
  }

  protected static void testEmpty(String s) throws IOException {
    if (s == null) throw new IOException("Unerwartetes Dateiende!");
  }

  private java.util.HashMap<String, Integer> things = new java.util.HashMap<String, Integer>();

  private java.util.HashMap<String, String> thingContainers = new java.util.HashMap<String, String>();

  private HashMap<String, ExtraThingData> extraThingData = new HashMap<String, ExtraThingData>();

}

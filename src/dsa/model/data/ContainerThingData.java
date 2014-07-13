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

public class ContainerThingData extends IExtraThingData {
  
  private String[] thingsInContainer;
  private int[] thingCounts;
  private IExtraThingData[] subData;
  private IExtraThingData dataOfContainer;
  
  public ContainerThingData(BufferedReader in, int lineNr, int version) throws IOException {
    dataOfContainer = IExtraThingData.create(in, lineNr);
    String line = in.readLine(); testEmpty(line);
    int count = parseInt(line, lineNr); ++lineNr;
    thingsInContainer = new String[count];
    thingCounts = new int[count];
    for (int i = 0; i < count; ++i) {
      thingsInContainer[i] = in.readLine(); ++lineNr;
      line = in.readLine(); testEmpty(line);
      thingCounts[i] = parseInt(line, lineNr); ++lineNr;
    }
    line = in.readLine(); testEmpty(line);
    int length = parseInt(line, lineNr); ++lineNr;
    subData = new IExtraThingData[length];
    for (int i = 0; i < length; ++i) {
      subData[i] = IExtraThingData.create(in, lineNr);
    }
    line = in.readLine();
    while (!line.equals("End_Container_Data")) {
      line = in.readLine();
    }
  }
  
  public String[] getThingsInContainer() { 
    return thingsInContainer;
  }
  
  public int[] getThingCounts() {
    return thingCounts;
  }
  
  public IExtraThingData[] getSubData() {
    return subData;
  }
  
  public IExtraThingData getDataOfContainer() {
    return dataOfContainer;
  }
  
  public ContainerThingData(IExtraThingData dataOfContainer, String[] things, int[] counts, IExtraThingData[] subData) {
    this.subData = subData;
    this.dataOfContainer = dataOfContainer;
    this.thingsInContainer = things;
    this.thingCounts = counts;
  }

  /* (non-Javadoc)
   * @see dsa.model.data.IExtraThingData#getType()
   */
  @Override
  public Type getType() {
    return IExtraThingData.Type.Container;
  }
  
  private static final int STREAM_VERSION = 1;

  /* (non-Javadoc)
   * @see dsa.model.data.IExtraThingData#store(java.io.PrintWriter)
   */
  @Override
  public void store(PrintWriter out) throws IOException {
     out.println(STREAM_VERSION);
     out.println(getType().ordinal());
     dataOfContainer.store(out);
     out.println(thingsInContainer.length);
     for (int i = 0; i < thingsInContainer.length; ++i) {
       out.println(thingsInContainer[i]);
       out.println(thingCounts[i]);
     }
     out.println(subData.length);
     for (int i = 0; i < subData.length; ++i) {
       subData[i].store(out);
     }
     out.println("End_Container_Data");
  }

}

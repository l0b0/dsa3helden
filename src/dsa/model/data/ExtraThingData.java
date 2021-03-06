package dsa.model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public final class ExtraThingData extends IExtraThingData {
  
  public static class PropertyException extends Exception {
    public PropertyException(String message) {
      super(message);
    }
    
    public PropertyException(String message, Throwable inner) {
      super(message, inner);
    }
  }
  
  public ExtraThingData(Type type) {
    this.type = type;
    this.properties = new HashMap<String, String>();
  }
  
  public ExtraThingData(Type type, BufferedReader in, int lineNr, int version) throws IOException {
    this.type = type;
    this.properties = new HashMap<String, String>();
    read(in, lineNr, version);
  }
  
  public Type getType() {
    return type;
  }
  
  public String getProperty(String key) throws PropertyException {
    if (!properties.containsKey(key)) throw new PropertyException("No property " + key);
    return properties.get(key);
  }
  
  public void setProperty(String key, String value) {
    properties.put(key, value);
  }
  
  public int getPropertyInt(String key) throws PropertyException {
    if (!properties.containsKey(key)) throw new PropertyException("No property " + key);
    try {
      return Integer.parseInt(properties.get(key));
    }
    catch (NumberFormatException e) {
      throw new PropertyException("Wrong type of property " + key);
    }
  }
  
  public void setProperty(String key, int value) {
    properties.put(key, "" + value);
  }
  
  public void store(PrintWriter out) throws IOException {
    out.println(STREAM_VERSION);
    out.println(type.ordinal());
    out.println(properties.size());
    for (String key : properties.keySet()) {
      out.println(key);
      out.println(properties.get(key));
    }
    out.println("--End of extra thing data--");
  }
  
  public ExtraThingData() {
    properties = new HashMap<String, String>();
  }
  
  private void read(BufferedReader in, int lineNr, int version) throws IOException {
    String line = null;
    if (version > 0) {
      line = in.readLine(); testEmpty(line); ++lineNr;
      int size = parseInt(line, lineNr);
      for (int i = 0; i < size; ++i) {
        String key = in.readLine(); testEmpty(key); ++lineNr;
        String value = in.readLine(); testEmpty(value); ++lineNr;
        setProperty(key, value);
      }
    }    
    do {
      line = in.readLine(); testEmpty(line); ++lineNr;
    }
    while (line != null && !line.trim().equals("--End of extra thing data--"));
  }

  private Type type;
  
  private HashMap<String, String> properties;
  
  private static final int STREAM_VERSION = 1;
  
}

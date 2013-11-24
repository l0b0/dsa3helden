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
package dsa.control.filetransforms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;


class Bufferers {
  
  public static Bufferer createBufferer(FileType fileType) {
    switch (fileType) {
    case WordML:
    case ODT:
    case XML:
    case HTML:
      return new XMLBufferer();
    case RTF:
    case Unknown:
      return new PlainBufferer();
    case ExcelML:
      return new ExcelBufferer();
    default:
      throw new IllegalArgumentException("Wrong fileType");
    }
  }

  /**
   * Abstract base class for the bufferers. Provides an encoding facility.
   */
  static abstract class AbstractBufferer implements Bufferer {
    
    protected AbstractBufferer(boolean xml) {
      this.encoding =  xml ? "UTF-8" : "ISO-8859-15";
      this.xml = xml;
    }

    /**
     * Encodes a text. For XML files, the encoding will be UTF-8, for other
     * files, it will be ISO-8859-15. For XML files, this method also
     * masks special characters like the ampersand.
     */
    protected final byte[] encode(String text) throws IOException {
      if (xml) {
        text = text.replace("&", "&amp;");
        text = text.replace("'", "&apos;");
        text = text.replace("\"", "&quot;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
      }
      return text.getBytes(encoding);
    }

    private final String encoding;
    private final boolean xml;
  }
  
  /**
   * Base class for normal buffers. The concrete subclasses differ only in
   * the handling of XML. This class does the buffering.
   */
  static abstract class SimpleBuffererBase extends AbstractBufferer {
    
    private ArrayList<Character> charBuffer;
    
    protected SimpleBuffererBase(boolean xml) {
      super(xml);
      charBuffer = new ArrayList<Character>();
      charBuffer.ensureCapacity(20);
    }

    public void addCharacter(OutputStream out, Character ch) throws IOException {
      charBuffer.add(ch);
    }

    public void flushBuffer(OutputStream out)  throws IOException {
      for (int i = 0; i < charBuffer.size(); ++i) {
        out.write(charBuffer.get(i));
      }      
    }

    public void printHit(PushbackInputStream in, OutputStream out, String item, boolean isNumber) throws IOException {
      if (item != null) out.write(encode(item));
      charBuffer.clear();
    }

    public void printMiss(PushbackInputStream in, OutputStream out, int ch) throws IOException {
      // the already read data can be written out now
      // the tokens are made in such a way that this data can't be 
      // the start of a new token: no token contains a '!' sign.
      flushBuffer(out);
      // the transformer will read the next sign from the input
      // in its next loop. The currently read sign can't be ignored
      // because it may be the start of a new token (a '!'). So put
      // it back in the stream and it can be read again and processed
      // normally.
      // We don't check for '!' because of genericity (the transformer 
      // doesn't know about the tokens). Instead, put back the token
      // if there has been something in the buffer. If not, it can't 
      // be a '!' since that would have been recognized and printMiss
      // wouldn't have been called.
      if (charBuffer.size() > 0)
        in.unread(ch);
      else
        out.write(ch);
      charBuffer.clear();
    }
  }
  
  /**
   * Concrete Bufferer for RTF files, plain text files, ...
   */
  
  static final class PlainBufferer extends SimpleBuffererBase {
    public PlainBufferer() {
      super(false);
    }
    
    public boolean skip(InputStream in, OutputStream out, int lastRead) throws IOException {
      return false;
    } 
  }
  
  /**
   * Concrete Bufferer for XML files. Skips XML tokens.
   */
  
  static final class XMLBufferer extends SimpleBuffererBase {
    public XMLBufferer() {
      super(true);
    }
    
    // when having read a start token ('!'), don't skip over XML tags
    // otherwise, the text ...!<Tag>... would be processed in a wrong
    // way: The <Tag> would be written out before the '!' is written.
    boolean inReplacementTag = false;

    public boolean skip(InputStream in, OutputStream out, int lastRead) throws IOException {
      // don't replace inside XML tokens, only inside values
      if (!inReplacementTag && (((char) lastRead) == '<')) {
        out.write(lastRead);
        while ((lastRead = in.read()) != -1) {
          out.write(lastRead);
          if ((char) lastRead == '>')
            break;
        }
        return true;
      }      
      else return false;
    }

    public void addCharacter(OutputStream out, Character ch) throws IOException {
      super.addCharacter(out, ch);
      inReplacementTag = true;
    }
    
    public void printHit(PushbackInputStream in, OutputStream out, String item, boolean isNumber) throws IOException {
      super.printHit(in, out, item, isNumber);
      inReplacementTag = false;
    }
    
    public void printMiss(PushbackInputStream in, OutputStream out, int ch) throws IOException {
      super.printMiss(in, out, ch);
      inReplacementTag = false;
    }
    
  }

  /**
   * Concrete Bufferer for MS Excel XML. In Excel files, if a value
   * is a number, the designation of the cell type should be changed.
   * In the template, it will be "String", since the tag is a string.
   * In the output file, it should be "Number".
   * 
   * The Bufferer maintains a second buffer for that purpose. This 
   * bufferer contains all character from the last "String" on. If 
   * there is a hit, it replaces this "String" with "Number".
   */
  static final class ExcelBufferer extends AbstractBufferer {
    
    private ArrayList<Character> tokenBuffer;
    
    private ArrayList<Character> outputBuffer;
    
    // see the XML bufferer
    private boolean inReplacementTag = false;
    
    // stores how much characters in the outputBuffer
    // already match "String"
    private int matchingLength = 0;
    
    // note that the "" chars are /not/ masked in the XML in this case
    private static final String STRING_TAG = "\"String\"";
    
    private static final int STRING_LENGTH = 8;
    
    private static final String NUMBER_TAG = "\"Number\"";

    public ExcelBufferer() {
      super(true);
      tokenBuffer = new ArrayList<Character>();
      outputBuffer = new ArrayList<Character>();
      
      tokenBuffer.ensureCapacity(20);
      outputBuffer.ensureCapacity(200);
    }

    public void addCharacter(OutputStream out, Character ch) throws IOException {
      tokenBuffer.add(ch);
      inReplacementTag = true;      
    }

    public void flushBuffer(OutputStream out) throws IOException {
      // first the output buffer, then the token buffer, since the
      // output buffer contains the earlier characters
      for (int i = 0; i < outputBuffer.size(); ++i) {
        out.write(outputBuffer.get(i));
      }      
      outputBuffer.clear();
      for (int i = 0; i < tokenBuffer.size(); ++i) {
        out.write(tokenBuffer.get(i));
      }
      tokenBuffer.clear();
    }

    public void printHit(PushbackInputStream in, OutputStream out, String item, boolean isNumber) throws IOException {
      int outputBufferStart = 0;
      if (matchingLength == STRING_LENGTH && isNumber) {
        // only replace if the to-be-replaced is the only content of the cell!
        // otherwise, something like: "Ausdauer: !au" would be set to be a number.
        int retCode = in.read();
        if (retCode != -1 && (char) retCode == '<') {
          // now the "String", which is at the start, can be replaced
          // Write the new "Number"
          for (int i = 0; i < NUMBER_TAG.length(); ++i) {
            out.write(NUMBER_TAG.charAt(i));
          }
          outputBufferStart = STRING_LENGTH; // skip the original "String"
        }
        if (retCode != -1) in.unread(retCode);
      }
      // write the characters between the original "String" and the tag
      for (int i = outputBufferStart; i < outputBuffer.size(); ++i) {
        out.write(outputBuffer.get(i));
      }
      // finally, write the tag
      if (item != null) out.write(encode(item));
      inReplacementTag = false;
      matchingLength = 0;
      outputBuffer.clear();
      tokenBuffer.clear();
    }

    public void printMiss(PushbackInputStream in, OutputStream out, int ch) throws IOException {
      // don't write directly to the output, write to the output buffer!
      for (int i = 0; i < tokenBuffer.size(); ++i) {
        writeToOutputBuffer(out, tokenBuffer.get(i));
      }
      // see the SimpleBuffererBase for an explanation of unread
      if (tokenBuffer.size() > 0)
        in.unread(ch);
      else
        writeToOutputBuffer(out, (char)ch);
      tokenBuffer.clear();
      inReplacementTag = false;
    }

    public boolean skip(InputStream in, OutputStream out, int lastRead) throws IOException {
      // see the XMLBufferer
      if (!inReplacementTag && (((char) lastRead) == '<')) {
        writeToOutputBuffer(out, (char) lastRead);
        while ((lastRead = in.read()) != -1) {
          // write to the output buffer, not to the output
          writeToOutputBuffer(out, (char) lastRead);
          if ((char) lastRead == '>') 
            break;
        }
        return true;
      }      
      return false;
    }
    
    private void writeToOutputBuffer(OutputStream out, Character ch) throws IOException {
      if (matchingLength < STRING_LENGTH) { 
        if (ch.charValue() == STRING_TAG.charAt(matchingLength)) {
          ++matchingLength;
          outputBuffer.add(ch);
        }
        else {
          // Does not match. Write out already buffered characters
          for (int i = 0; i < outputBuffer.size(); ++i) {
            out.write(outputBuffer.get(i));
          }
          outputBuffer.clear();
          // The ch may be the start of "String"
          if (ch.charValue() == '"') {
            matchingLength = 1;
            outputBuffer.add(ch);
          }
          else {
            matchingLength = 0;
            out.write(ch);
          }
        }
      }
      else {
        // outputBuffer already starts with "String"
        outputBuffer.add(ch);
        // only the last "String" needs to be kept
        // this way we also ensure that, if we have a "String", the
        // last "String" is always at the start of the output buffer
        boolean hasNewString = true;
        // compare the last 8 values in the output buffer
        for (int i = 0; i < STRING_LENGTH; ++i) {
          if (outputBuffer.get(outputBuffer.size() - STRING_LENGTH + i) != STRING_TAG.charAt(i)) {
            hasNewString = false;
            break;
          }
        }
        if (hasNewString) {
          // write out everything before the new "String"
          for (int i = 0; i < outputBuffer.size() - STRING_LENGTH; ++i) {
            out.write(outputBuffer.get(i));
          }
          // put only "String" in the output buffer
          outputBuffer.clear();
          for (int i = 0; i < STRING_LENGTH; ++i) {
            outputBuffer.add(STRING_TAG.charAt(i));
          }
        }
      }
    }
    
  }
}

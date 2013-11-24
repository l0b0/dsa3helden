/*
 Copyright (c) 2006 [Joerg Ruedenauer]
 
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
 along with Foobar; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.util;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.ProgressMonitorInputStream;

/**
 * Prints a character by replacing tokens in an input file with the values
 * in a lookup table. 
 */
public class FileTransformer {
  
  public FileTransformer(File input, File output, FileType fileType) {
    this.input = input;
    this.output = output;
    lookupTable = new LookupTable();
    this.fileType = fileType;
    xml = (fileType != FileType.RTF && fileType != FileType.Unknown);
  }

  public void setLookupTable(LookupTable table) {
    lookupTable = table;
  }

  /**
   * Performs the transformation. Shows a progress dialog if necessary (if
   * the transformation takes long).
   * 
   * @param component Parent component for the progress dialog
   * @param message Message displayed in the progress dialog
   * @see java.io.ProgressMonitorInputStream
   */
  public void transform(Component component, String message) throws IOException {
    if (fileType == FileType.ODT) {
      // special case for ODT files: these are de facto zip files, with the main
      // content file but one of the files which are zipped together in the odt.
      try {
        ZipFile zipFile = new ZipFile(input);
        try {
          ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(
              output));
          try {
            // search for the main document
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            byte[] dataBuffer = new byte[5 * 1024 * 1024];
            while (entries.hasMoreElements()) {
              ZipEntry entry = entries.nextElement();
              InputStream entryInput = new BufferedInputStream(zipFile
                  .getInputStream(entry));
              try {
                if (entry.getName().equalsIgnoreCase("content.xml")) {
                  // this is the main document. It must be transformed.
                  // all other zip attributes remain the same
                  ZipEntry newEntry = new ZipEntry(entry.getName());
                  newEntry.setComment(entry.getComment());
                  newEntry.setExtra(entry.getExtra());
                  newEntry.setMethod(entry.getMethod());
                  outStream.putNextEntry(newEntry);
                  InputStream progressStream = new ProgressMonitorInputStream(
                      component, message, entryInput);
                  // real work delegated into another method
                  transform(progressStream, outStream);
                }
                else {
                  // other files in the zip file are simply cloned
                  ZipEntry newEntry = (ZipEntry) entry.clone();
                  outStream.putNextEntry(newEntry);
                  // entry.clone() doesn't clone the content of the
                  // file, though. Do that manually.
                  while (entryInput.available() > 0) {
                    int read = entryInput.read(dataBuffer);
                    if (read > 0) {
                      outStream.write(dataBuffer, 0, read);
                    }
                  }
                }
                outStream.closeEntry();
              }
              finally {
                entryInput.close();
              }
            }
          }
          finally {
            outStream.close();
          }
        }
        finally {
          zipFile.close();
        }
      }
      catch (IOException e) {
        throw e;
      }
    }
    else {
      // simple case: just one file, which is transformed directly.
      InputStream inStream = new BufferedInputStream(
          new ProgressMonitorInputStream(component, message,
              new FileInputStream(input)));
      try {
        OutputStream outStream = new FileOutputStream(output);
        try {
          transform(inStream, outStream);
          outStream.flush();
        }
        finally {
          outStream.close();
        }
      }
      finally {
        inStream.close();
      }
    }
  }
  
  /**
   * For the transformation, the characters that are read in must be buffered so 
   * they can later be replaced. The concrete handling of the buffering and 
   * replacement is different for the different file types. Therefore, different
   * classes are used (algorithm pattern). This is the interface for the buffers.
   */
  static interface Bufferer {
    /**
     * In some cases, whole sections of the input file may be directly written to
     * the output file. This is determined by the last read character.
     * Returns whether something was skipped.
     */
    boolean skip(InputStream in, OutputStream out, int lastRead) throws IOException;
    
    /**
     * Adds a character to a buffer. Theoretically, if currently no buffering takes
     * place, the character may also be written directly to the output.
     */
    
    void addCharacter(OutputStream out, Character ch) throws IOException;
    
    /**
     * Prints the replacement for the last tag to the output stream.
     */
    void printHit(PushbackInputStream in, OutputStream out, String item, boolean isNumber) throws IOException;

    /**
     * Notification that no tag was found. The buffered data may be written 
     * to the output stream. The last read sign -- which determined that there
     * was a miss -- is given so it may be put back into the stream.
     */
    void printMiss(PushbackInputStream in, OutputStream out, int ch) throws IOException;

    /**
     * Print all buffered data to the output.
     */
    void flushBuffer(OutputStream out) throws IOException;    
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
  
  /**
   * Does the transformation, but is merely 'glue code' between the
   * Bufferer and the LookupTable
   */
  private void transform(InputStream inStream, OutputStream outStream)
      throws IOException {
    PushbackInputStream in = new PushbackInputStream(inStream);
    BufferedOutputStream out = new BufferedOutputStream(outStream);
    
    Character ch;
    int retCode;
    
    LookupTable.LookupPerformer lookuper = lookupTable.getLookupPerformer();
    Bufferer bufferer = fileType == FileType.ExcelML ? 
        new ExcelBufferer() : 
          (xml ? new XMLBufferer() : 
            new PlainBufferer());
    
    while ((retCode = in.read()) != -1) {
      
      // perhaps skip a part of the file
      if (bufferer.skip(in, out, (char) retCode)) continue;
      
      // look for next tag
      ch = new Character((char) retCode);
      LookupTable.LookupPerformer.NextCharResult result = lookuper.nextChar(ch);
      
      if (result == LookupTable.LookupPerformer.NextCharResult.Continue) {
        // may yet become a tag
        bufferer.addCharacter(outStream, ch);
      }
      else if (result == LookupTable.LookupPerformer.NextCharResult.Hit) {
        // tag found
        bufferer.printHit(in, out, lookuper.getItem(), lookuper.isNumberItem());
        lookuper.restart();
      }
      else {
        // no tag
        bufferer.printMiss(in, out, retCode);
        lookuper.restart();
      }
    }
    
    bufferer.flushBuffer(out);
    out.flush();
  }
  
  private LookupTable lookupTable;

  private final File input;

  private final File output;
  
  private final FileType fileType;

  private final boolean xml;

}

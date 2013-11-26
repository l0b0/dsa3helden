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

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ProgressMonitorInputStream;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import dsa.util.LookupTable.LookupPerformer;
import dsa.util.LookupTable.LookupPerformer.NextCharResult;

class PDFTransformer extends AbstractFileTransformer {

  @SuppressWarnings("rawtypes")
  @Override
  protected void doTransform(Component component, String message)
      throws IOException {
    LookupPerformer performer = lookupTable.getLookupPerformer();
    InputStream inStream = new BufferedInputStream(
        new ProgressMonitorInputStream(component, message,
            new FileInputStream(input)));
    try {
      PdfReader reader = new PdfReader(inStream);
      FileOutputStream outStream = new FileOutputStream(output);
      PdfStamper stamper = new PdfStamper(reader, outStream);
      try {
        AcroFields form = stamper.getAcroFields();
        HashMap fields = (HashMap)form.getFields();
        ArrayList<String> fieldNames = new ArrayList<String>();
        for (Object o : fields.keySet()) {
          String originalFieldName = (String) o;
          fieldNames.add(originalFieldName);
        }
        java.util.Collections.sort(fieldNames);
        for (String originalFieldName : fieldNames) {
          String fieldName = originalFieldName;
          if (fieldName.startsWith("#21")) {
            // #21 is '!' in pdf
            fieldName = "!" + fieldName.substring(3);
          }
          else if (!fieldName.startsWith("!")) {
            continue; // can't be a tag
          }
          performer.restart();
          boolean missed = false;
          for (int i = 0; !missed && i < fieldName.length(); ++i) {
            NextCharResult result = performer.nextChar(Character.valueOf(fieldName.charAt(i)));
            switch (result) {
              case Miss:
                missed = true;
                break;
              case Continue:
                break;
              case Hit:
                //if (i == fieldName.length() - 1) {
                  String replacement = performer.getItem();
                  form.setField(originalFieldName, replacement);
                //}
                break;
              default:
                throw new InternalError();
            }
          }
        }
        stamper.setFormFlattening(true);
      }
      finally {
        stamper.close();
      }
    }
    catch (DocumentException e) {
      throw new IOException("DocumentException: " + e.getMessage());
    }
    finally {
      inStream.close();
    }
  }

}

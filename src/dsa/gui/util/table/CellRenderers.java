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
package dsa.gui.util.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public final class CellRenderers {
  
  private CellRenderers() {}
  
  public interface ColourSelector {
    boolean shallBeOpaque(int column);
    boolean shallBeGray(int row, int column);
    Color getForeground(int row, int column);
    Color getBackground(int row, int column);
  }
  
  private static class GreyingCellRenderer extends DefaultTableCellRenderer {
    
    protected ColourSelector mCallbacks;
    
    private static final Color BACKGROUND_GRAY = new Color(238, 238, 238);
    
    public GreyingCellRenderer(ColourSelector selector) {
      mCallbacks = selector;
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component comp = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);
      comp.setBackground(mCallbacks.shallBeGray(row, column) ? BACKGROUND_GRAY
          : Color.WHITE);
      comp.setForeground(mCallbacks.getForeground(row, column));
      ((JComponent) comp).setOpaque(isSelected || mCallbacks.shallBeOpaque(column));
      return comp;
    }
  }
  
  private static final class ColouringCellRenderer extends GreyingCellRenderer {
	  public ColouringCellRenderer(ColourSelector selector) {
		  super(selector);
	  }
	  
	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
	          Component comp = super.getTableCellRendererComponent(table, value,
	              isSelected, hasFocus, row, column);
	          Color background = mCallbacks.getBackground(row, column);
	          if (background != null)
	        	  comp.setBackground(background);
	          return comp;
	    }	  
  }

  private static final class NormalCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component comp = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);
      ((JComponent) comp).setOpaque(isSelected);
      return comp;
    }
  }
  
  private static final class ImageCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component comp = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);
      ((JComponent) comp).setOpaque(isSelected);
      if (comp instanceof JLabel) {
        if (value instanceof ImageIcon) {
          ((JLabel)comp).setText("");
          ((JLabel)comp).setIcon((ImageIcon)value);
        }
        else if (value instanceof ImageAndText) {
          ((JLabel)comp).setText("");
          ((JLabel)comp).setIcon(((ImageAndText)value).getImage());
          ((JLabel)comp).setToolTipText(((ImageAndText)value).getText());
        }
      }
      return comp;
    }
  }
  
  private static final class MiddleShorteningLabelUI extends BasicLabelUI {
	  protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, 
			  Rectangle viewR, Rectangle iconR, Rectangle textR) {
		  String newText = text;
		  Rectangle oldViewR = new Rectangle(viewR);
		  Rectangle oldIconR = new Rectangle(iconR);
		  Rectangle oldTextR = new Rectangle(textR);
		  String test = super.layoutCL(label, fontMetrics, newText, icon, viewR, iconR, textR);
		  if (!test.equals(newText)) {
			  // text was cut. Cut differently
			  boolean shorten = true;
			  int length = text.length();
			  int diff = length / 2;
			  while (true) {
				  int newLength = shorten ? length - diff : length + diff;
				  int after = newLength > 5 ? 2 : 1;
				  int before = newLength > 7 ? newLength - 5 : 1;
				  newText = text.substring(0, before) + "..." + text.substring(text.length() - after);
				  viewR = new Rectangle(oldViewR);
				  iconR = new Rectangle(oldIconR);
				  textR = new Rectangle(oldTextR);
				  test = super.layoutCL(label, fontMetrics, newText, icon, viewR, iconR, textR);
				  if (!test.equals(newText)) {
					  // too long
					  if (newLength <= 5) {
						  // stop anwyay
						  newText = text.substring(0, before + 3);
						  break;
					  }
					  else if (diff == 1) {
						  newLength = newLength - 1;
						  after = newLength > 5 ? 2 : 1;
						  before = newLength > 7 ? newLength - 5 : 3;
						  newText = text.substring(0, before) + "..." + text.substring(text.length() - after);
						  break;
					  }
					  shorten = true;
				  }
				  else {
					  // short enough
					  if (diff == 1)
						  break;
					  shorten = false;
				  }
				  diff = diff / 2;
				  length = newLength;
			  }
			  viewR = oldViewR;
			  iconR = oldIconR;
			  textR = oldTextR;
			  return super.layoutCL(label, fontMetrics, newText, icon, viewR, iconR, textR);
		  }
		  else {
			  return test;
		  }
	  }
  }
  
  private static final class MiddleShorteningCellRenderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent(JTable table, Object value, 
	            boolean isSelected, boolean hasFocus, int row, int column) {
	          Component comp = super.getTableCellRendererComponent(table, value,
	              isSelected, hasFocus, row, column);
	          setOpaque(isSelected);
	          if (comp instanceof JLabel) {
	        	  JLabel label = (JLabel)comp;
	        	  String text = value.toString();
	        	  label.setText(text);
	        	  label.setToolTipText(text);
	        	  label.setUI(new MiddleShorteningLabelUI());
	          }
	          return comp;
	    }
    
  }
  
  public static class ImageAndText {
    private String text;
    private ImageIcon image;
    public ImageAndText(String text, ImageIcon image) {
      this.text = text;
      this.image = image;
    }
    public ImageIcon getImage() { return image; }
    public String getText() { return text; }
  }

  public static DefaultTableCellRenderer createGreyingCellRenderer(ColourSelector selector) {
    return new GreyingCellRenderer(selector);
  }
  
  public static DefaultTableCellRenderer createColouringCellRenderer(ColourSelector selector) {
	return new ColouringCellRenderer(selector);
  }
	  
  public static TableCellRenderer createNormalCellRenderer() {
    return new NormalCellRenderer();
  }
  
  public static TableCellRenderer createImageCellRenderer() {
    return new ImageCellRenderer();
  }

  public static TableCellRenderer createMiddleShorteningCellRenderer() {
	  return new MiddleShorteningCellRenderer();
  }
}

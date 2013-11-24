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
package dsa.gui.frames;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import dsa.gui.dialogs.AnimalProbeDialog;
import dsa.gui.util.ImageManager;
import dsa.model.DiceSpecification;
import dsa.model.data.Animal;
import dsa.model.data.Thing;
import dsa.model.data.Things;

public class AnimalFrame extends AbstractThingsFrame {

  private static final int GAP = 10;

  JTextField nameField;

  JTextField categoryField;
  
  JPanel thingsPanel;

  public Animal getAnimal() {
    return animal;
  }
  
  JPanel center, grid;
  
  protected void updateData()
  {
    super.updateData();
    if (grid != null) center.remove(grid);
    grid = getGrid();
    center.add(grid, BorderLayout.NORTH);
    invalidate();
  }

  public AnimalFrame(Animal a, boolean setSize) {
    super(a, a.getName());
    this.animal = a;
    JPanel contentPane = new JPanel(new BorderLayout());
    this.setContentPane(contentPane);
    JLabel l1 = new JLabel("");
    l1.setPreferredSize(new java.awt.Dimension(GAP, GAP));
    contentPane.add(l1, BorderLayout.NORTH);
    JLabel l2 = new JLabel("");
    l2.setPreferredSize(new java.awt.Dimension(GAP, GAP));
    contentPane.add(l2, BorderLayout.SOUTH);
    JLabel l3 = new JLabel("");
    l3.setPreferredSize(new java.awt.Dimension(GAP, GAP));
    contentPane.add(l3, BorderLayout.WEST);
    JLabel l4 = new JLabel("");
    l4.setPreferredSize(new java.awt.Dimension(GAP, GAP));
    contentPane.add(l4, BorderLayout.EAST);
    
    center = new JPanel(new BorderLayout());

    grid = getGrid();
    center.add(grid, BorderLayout.NORTH);

    thingsPanel = getThingsPanel();

    thingsPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
        "Gegenstände"));
    
    center.add(thingsPanel, BorderLayout.CENTER);
    
    contentPane.add(center, BorderLayout.CENTER);
    if (setSize) {
      pack();
    }
    
    listener = new Animal.Listener() {
      public void nameChanged(String oldName, String newName) {
        AnimalFrame.this.setTitle(newName);
      }
    };
    animal.addListener(listener);
    this.addWindowListener(new WindowAdapter() {
      boolean done = false;
      public void windowClosing(WindowEvent e) {
        animal.removeListener(listener);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          animal.removeListener(listener);
        }
      }
    });
    
    updateData();
  }
  
  public String getHelpPage() {
    return "Tier";
  }
  
  protected Rectangle getSumLabelPos() {
    return new Rectangle(5, 5, 440, 25);
  }

  protected Rectangle getAddButtonPos() {
    return new Rectangle(5, 5, 60, 25);
  }

  protected Rectangle getBuyButtonPos() {
     return new Rectangle(5, 35, 60, 25);
  }
  
  protected Rectangle getRemoveButtonPos() {
     return new Rectangle(5, 65, 60, 25);
  }
  
  protected Rectangle getEditButtonPos() {
    return new Rectangle(5, 95, 60, 25);
  }
  
  protected String calcSumLabelText() {
    String text = super.calcSumLabelText();

    long weight = 0;
    Things things = Things.getInstance();
    for (String name : animal.getThings()) {
      Thing thing = things.getThing(name);
      if (thing != null) {
        long count = animal.getThingCount(name);
        weight += count * (long) thing.getWeight();
      }
    }
    float weightStones = weight / 40.0f;

    int tk = -1;
    for (int i = 0; i < animal.getNrOfAttributes(); ++i) {
      String attrName = animal.getAttributeTitle(i);
      if ("TK".equals(attrName)) {
        Object tkValue = animal.getAttributeValue(i);
        if (tkValue instanceof Number) {
          tk = ((Number)tkValue).intValue();
          break;
        }
      }
    }

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(3);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);

    text += "(Gesamt: ";
    text += format.format(weightStones) + " Stein); TK: ";
    text += (tk == -1) ? "??" : tk;
    text += " Stein";
    if (weightStones <= tk) {
      sumLabel.setForeground(DARK_GREEN);
    }
    else {
      sumLabel.setForeground(java.awt.Color.RED);
    }
    
    return text;
  }
  
  private static final Color DARK_GREEN = new Color(0, 175, 0);
  
  private JPanel getGrid() {
    JPanel grid = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(3, 3, 3, 3);
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    JLabel nameLabel = new JLabel("Name: ");
    grid.add(nameLabel, c);
    nameField = new JTextField();
    nameField.setText(animal.getName());
    nameField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        animal.setName(nameField.getText());
      }

      public void removeUpdate(DocumentEvent e) {
        animal.setName(nameField.getText());
      }

      public void changedUpdate(DocumentEvent e) {
        animal.setName(nameField.getText());
      }
    });
    c.gridx = 1;
    grid.add(nameField, c);
    c.gridx++;
    JLabel ln = new JLabel("");
    grid.add(ln, c);
    c.gridx++;
    JLabel lf = new JLabel("");
    lf.setPreferredSize(new Dimension(GAP, GAP));
    grid.add(lf, c);
    c.gridx--;
    c.gridx--;
    c.gridx = 4;
    JLabel categoryLabel = new JLabel("Typ");
    grid.add(categoryLabel, c);
    categoryField = new JTextField();
    categoryField.setText(animal.getCategory());
    categoryField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        animal.setCategory(categoryField.getText());
      }

      public void removeUpdate(DocumentEvent e) {
        animal.setCategory(categoryField.getText());
      }

      public void changedUpdate(DocumentEvent e) {
        animal.setCategory(categoryField.getText());
      }
    });
    c.gridx = 5;
    grid.add(categoryField, c);
    c.gridx++;
    JLabel lc = new JLabel("");
    grid.add(lc, c);
    c.gridx--;
    int column = 0;
    int row = 0;
    for (int i = 0; i < animal.getNrOfAttributes(); ++i) {
      if ((i > animal.getNrOfAttributes() / 2) && (column < 3)) {
        column = 4;
        row = 0;
      }
      ++row;
      JLabel label = new JLabel(animal.getAttributeTitle(i) + ": ");
      c.gridx = column;
      c.gridy = row;
      grid.add(label, c);
      c.gridx++;
      Animal.AttributeType type = animal.getAttributeType(i);
      if (animal.getAttributeTitle(i).equals("AP")) {
        JTextField tf = new JTextField();
        tf.setText(animal.getAttributeValue(i).toString());
        textFields.put(i, tf);
        tf.setEditable(false);
        grid.add(tf, c);
        c.gridx++;
        JButton button = new JButton(ImageManager.getIcon("increase"));
        button.setPreferredSize(new java.awt.Dimension(40, 18));
        button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            increaseAP();
          }
        });
        grid.add(button, c);
        c.gridx--;
      }
      else if (type == Animal.AttributeType.eString) {
        JTextField tf = new JTextField();
        tf.setText(animal.getAttributeValue(i).toString());
        tf.getDocument().addDocumentListener(new MyDocumentListener(i));
        textFields.put(i, tf);
        grid.add(tf, c);
        c.gridx++;
        JLabel l = new JLabel("");
        grid.add(l, c);
        c.gridx--;
      }
      else if (type == Animal.AttributeType.eDicing) {
        JTextField tf = new JTextField();
        tf.setText(animal.getAttributeValue(i).toString());
        tf.getDocument().addDocumentListener(new MyDicingDocumentListener(i));
        textFields.put(i, tf);
        grid.add(tf, c);
        c.gridx++;
        JLabel l = new JLabel("");
        grid.add(l, c);
        c.gridx--;
      }
      else if (type == Animal.AttributeType.eSpeed) {
        JTextField tf = new JTextField();
        tf.setText(animal.getAttributeValue(i).toString());
        tf.getDocument().addDocumentListener(new MySpeedDocumentListener(i));
        textFields.put(i, tf);
        grid.add(tf, c);
        c.gridx++;
        JLabel l = new JLabel("");
        grid.add(l, c);
        c.gridx--;
      }
      else {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
        sp.setValue(animal.getAttributeValue(i));
        sp.addChangeListener(new MyChangeListener(i));
        spinners.put(i, sp);
        grid.add(sp, c);
        if (animal.isAttributeTestable(i)) {
          c.gridx++;
          JButton button = new JButton(ImageManager.getIcon("probe"));
          button.setPreferredSize(new java.awt.Dimension(40, 18));
          button.addActionListener(new ProbePerformer(i));
          grid.add(button, c);
          c.gridx--;
        }
        else {
          c.gridx++;
          JLabel l = new JLabel("");
          grid.add(l, c);
          c.gridx--;
        }
      }
      c.gridx--;
      if (animal.getAttributeTitle(i).equals("AP")) {
        if ((i > animal.getNrOfAttributes() / 2) && (column < 3)) {
          column = 4;
          row = 0;
        }
        ++row;
        JLabel label2 = new JLabel("Stufe: ");
        c.gridx = column;
        c.gridy = row;
        grid.add(label2, c);
        c.gridx++;
        int ap = ((Integer)animal.getAttributeValue(i)).intValue();
        int step = calcStep(ap);
        JLabel label3 = new JLabel("" + step);
        grid.add(label3, c);
        c.gridx++;
        JLabel label4 = new JLabel("");
        grid.add(label4, c);
        c.gridx -= 2;
      }
    }
    ++row;
    c.gridy = row;
    grid.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
        "Eigenschaften"));
    return grid;
  }
  
  private int calcStep(int ap) {
    return (int) Math.floor(0.5 + 0.01 * Math.sqrt(2500 + 200 * ap));
  }

  private void increaseAP() {
    String apS = javax.swing.JOptionPane.showInputDialog(this,
        "Im letzten Abenteuer verdiente AP:", "AP erhöhen",
        JOptionPane.PLAIN_MESSAGE);
    if (apS == null) return;
    int ap = 0;
    try {
      ap = Integer.parseInt(apS);
      if (ap < 0) throw new NumberFormatException("");
    }
    catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,
          "Bitte eine positive ganze Zahl eingeben.", "Fehler",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    int index = findAttributeIndex("AP");
    int oldAP = ((Integer)animal.getAttributeValue(index)).intValue();
    int oldStep = calcStep(oldAP);
    int newAP = oldAP + ap;
    animal.setAttributeValue(index, Integer.valueOf(newAP));
    int newStep = calcStep(newAP);
    if (newStep > oldStep) {
      String message = animal.getName() + " ist um " + (newStep - oldStep) + ((newStep - oldStep > 1) ? " Stufen" : " Stufe")
        + " gestiegen!";
      JOptionPane.showMessageDialog(this, message, "Stufenanstieg", JOptionPane.INFORMATION_MESSAGE);
      while (newStep > oldStep) {
        Object[] options = new Object[] {"AT", "PA" };
        int result = JOptionPane.showOptionDialog(this, "Soll die AT oder die PA erhöht werden?", "Stufenanstieg", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "AT");
        increaseAttribute(result == 0 ? "AT" : "PA");
        ArrayList<String> attrs = new ArrayList<String>();
        attrs.add("MU");
        attrs.add("KL");
        attrs.add("IN");
        attrs.add("CH");
        attrs.add("GE");
        attrs.add("FF");
        attrs.add("KK");
        attrs.add("GS");
        attrs.add("MR");
        result = JOptionPane.showOptionDialog(this, "Welche Eigenschaft soll erhöht werden?", "Stufenanstieg", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, attrs.toArray(), attrs.get(0));
        increaseAttribute(attrs.get(result));
        attrs.remove(attrs.get(result));
        result = JOptionPane.showOptionDialog(this, "Welche Eigenschaft soll außerdem erhöht werden?", "Stufenanstieg", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, attrs.toArray(), attrs.get(0));
        increaseAttribute(attrs.get(result));
        increaseAttribute("LE");
        ++oldStep;
      }
    }
    updateData();
  }
  
  private void increaseAttribute(String name) {
    int index = findAttributeIndex(name);
    if (animal.getAttributeType(index) == Animal.AttributeType.eInt) {
      increaseIntegerAttribute(index);
    }
    else {
      increaseStringAttribute(index);
    }
  }
  
  private void increaseIntegerAttribute(int index) {
    int oldValue = ((Integer)animal.getAttributeValue(index)).intValue();
    animal.setAttributeValue(index, Integer.valueOf(oldValue + 1));
  }
  
  private void increaseStringAttribute(int index) {
    String oldValue = animal.getAttributeValue(index).toString();
    StringTokenizer toks = new StringTokenizer(oldValue, "/");
    String newValue = "";
    while (toks.hasMoreTokens()) {
      if (newValue.length() > 0) newValue = newValue + "/";
      String tok = toks.nextToken();
      try {
        int old = Integer.parseInt(tok);
        newValue = newValue + (old + 1);
      }
      catch (NumberFormatException e) {
        newValue = newValue + tok;
      }
    }
    animal.setAttributeValue(index, newValue);
  }
  
  private int findAttributeIndex(String name) {
    int index = 0;
    for (int i = 0; i < animal.getNrOfAttributes(); ++i) {
      if (animal.getAttributeTitle(i).equals(name)) {
        index = i;
        break;
      }
    }    
    return index;
  }

  private final Animal.Listener listener;

  private final Animal animal;

  private final HashMap<Integer, JTextField> textFields = new HashMap<Integer, JTextField>();

  private final HashMap<Integer, JSpinner> spinners = new HashMap<Integer, JSpinner>();

  class ProbePerformer implements ActionListener {
    public ProbePerformer(int i) {
      nr = i;
    }

    private final int nr;

    public void actionPerformed(ActionEvent e) {
      AnimalProbeDialog dialog = new AnimalProbeDialog(AnimalFrame.this,
          animal, nr);
      dialog.setLocationRelativeTo(AnimalFrame.this);
      dialog.setModal(true);
      dialog.setVisible(true);
    }
  }

  class MyChangeListener implements ChangeListener {
    public MyChangeListener(int i) {
      nr = i;
    }

    private int nr;

    public void stateChanged(ChangeEvent e) {
      try {
        animal.setAttributeValue(nr, spinners.get(nr).getValue());
      }
      catch (NumberFormatException ex) {
        // ignore
      }
    }
  }

  class MyDocumentListener implements DocumentListener {
    public MyDocumentListener(int i) {
      nr = i;
    }

    private int nr;

    public void insertUpdate(DocumentEvent e) {
      setValue();
    }

    public void removeUpdate(DocumentEvent e) {
      setValue();
    }

    public void changedUpdate(DocumentEvent e) {
      setValue();
    }
    
    private void setValue() {
      try {
        animal.setAttributeValue(nr, textFields.get(nr).getText());
      }
      catch (NumberFormatException e) {
        // ignore
      }
    }
  }

  class MySpeedDocumentListener implements DocumentListener {
    public MySpeedDocumentListener(int i) {
      nr = i;
    }

    private int nr;

    public void insertUpdate(DocumentEvent e) {
      updateValue();
    }

    public void removeUpdate(DocumentEvent e) {
      updateValue();
    }

    public void changedUpdate(DocumentEvent e) {
      updateValue();
    }

    private void updateValue() {
      String s = textFields.get(nr).getText();
      try {
        Animal.SpeedData sd = Animal.SpeedData.parse(s, 0);
        animal.setAttributeValue(nr, sd);
      }
      catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(AnimalFrame.this,
            "Format muss a/b sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
      }
      catch (java.io.IOException e) {
        JOptionPane.showMessageDialog(AnimalFrame.this,
            "Format muss a/b sein!", "Fehler", JOptionPane.ERROR_MESSAGE);        
      }
    }
  }

  class MyDicingDocumentListener implements DocumentListener {
    public MyDicingDocumentListener(int i) {
      nr = i;
    }

    private int nr;

    public void insertUpdate(DocumentEvent e) {
      updateValue();
    }

    public void removeUpdate(DocumentEvent e) {
      updateValue();
    }

    public void changedUpdate(DocumentEvent e) {
      updateValue();
    }

    private void updateValue() {
      String s = textFields.get(nr).getText();
      try {
        DiceSpecification ds = DiceSpecification.parse(s);
        animal.setAttributeValue(nr, ds);
      }
      catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(AnimalFrame.this, "Falsches Format für den Wert!",
            "Fehler", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

}

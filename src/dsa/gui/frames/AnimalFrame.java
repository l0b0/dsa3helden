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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.HashMap;

import dsa.gui.dialogs.AnimalProbeDialog;
import dsa.gui.dialogs.ThingSelectionDialog;
import dsa.gui.dialogs.AbstractSelectionDialog.SelectionDialogCallback;
import dsa.gui.tables.ThingTransfer;
import dsa.gui.tables.ThingsTable;
import dsa.gui.util.ImageManager;
import dsa.model.DiceSpecification;
import dsa.model.data.Animal;
import dsa.model.data.ExtraThingData;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.util.Optional;

public class AnimalFrame extends AbstractDnDFrame implements Things.ThingsListener {

  private static final int GAP = 10;

  JTextField nameField;

  JTextField categoryField;
  
  JPanel thingsPanel;

  public Animal getAnimal() {
    return animal;
  }

  public AnimalFrame(Animal a, boolean setSize) {
    super(ThingTransfer.Flavors.Thing, a.getName());
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
    
    JPanel center = new JPanel(new BorderLayout());
    
    JPanel grid = getGrid();
    center.add(grid, BorderLayout.NORTH);

    thingsPanel = getThingsPanel();
    center.add(thingsPanel, BorderLayout.CENTER);
    
    contentPane.add(center, BorderLayout.CENTER);
    if (setSize) {
      pack();
    }
    
    calcSums();

    listener = new Animal.Listener() {
      public void nameChanged(String oldName, String newName) {
        AnimalFrame.this.setTitle(newName);
      }
      public void thingRemoved(String thing) {
        int count = animal.getThingCount(thing);
        if (count == 0) {
          mTable.removeThing(thing);
        }
        else {
          mTable.setCount(thing, count);
        }
        calcSums();
      }
    };
    animal.addListener(listener);
    Things.getInstance().addObserver(this);
    this.addWindowListener(new WindowAdapter() {
      boolean done = false;
      public void windowClosing(WindowEvent e) {
        animal.removeListener(listener);
        Things.getInstance().removeObserver(AnimalFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          animal.removeListener(listener);
          Things.getInstance().removeObserver(AnimalFrame.this);
        }
      }
    });
  }
  
  public String getHelpPage() {
    return "Tier";
  }
  
  private ThingsTable mTable;
  JLabel sumLabel;

  JLabel getSumLabel() {
    if (sumLabel == null) {
      sumLabel = new JLabel("");
      sumLabel.setForeground(java.awt.Color.BLUE);
      sumLabel.setBounds(5, 5, 440, 25);
    }
    return sumLabel;
  }

  JButton addButton;

  JButton removeButton;

  JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setDisabledIcon(ImageManager.getIcon("increase_disabled"));
      addButton.setBounds(5, 5, 60, 25);
      addButton.setToolTipText("Gegenstand hinzufügen");
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectItem();
        }
      });
    }
    return addButton;
  }

  protected void selectItem() {
    ThingSelectionDialog dialog = new ThingSelectionDialog(this);
    dialog.setCallback(new SelectionDialogCallback() {
      public void itemSelected(String item) {
        addItem(item, new ExtraThingData(ExtraThingData.Type.Thing));
      }
      public void itemChanged(String item) {
        Things.getInstance().thingChanged(item);
      }
    });
    dialog.setVisible(true);
  }

  JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(5, 35, 60, 25);
      removeButton.setToolTipText("Gegenstand entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String name = mTable.getSelectedItem();
          removeItem(name);
        }
      });
    }
    return removeButton;
  }

  private void calcSums() {
    long weight = 0;
    long value = 0;
    Things things = Things.getInstance();
    for (String name : animal.getThings()) {
      Thing thing = things.getThing(name);
      if (thing != null) {
        long count = animal.getThingCount(name);
        weight += count * (long) thing.getWeight();
        if (thing.getValue().hasValue()) {
          if (thing.getCurrency() == Thing.Currency.D)
            value += count * (long) thing.getValue().getValue() * 1000L;
          else if (thing.getCurrency() == Thing.Currency.S)
            value += count * (long) thing.getValue().getValue() * 100L;
          else if (thing.getCurrency() == Thing.Currency.K)
            value += count * (long) thing.getValue().getValue() * 10L;
          else if (thing.getCurrency() == Thing.Currency.H)
            value += count * (long) thing.getValue().getValue();
        }
      }
    }
    float weightStones = weight / 40.0f;
    float valueD = value / 1000.0f;

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(0);
    format.setMinimumIntegerDigits(1);
    
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

    String text = "Gesamt: Wert ca. " + format.format(valueD)
        + " Dukaten, Gewicht ca. ";
    format.setMaximumFractionDigits(3);
    text += format.format(weightStones) + " Stein; TK: ";
    text += (tk == -1) ? "??" : tk;
    text += " Stein";
    if (weightStones <= tk) {
      sumLabel.setForeground(DARK_GREEN);
    }
    else {
      sumLabel.setForeground(java.awt.Color.RED);
    }
    sumLabel.setText(text);
  }
  
  private static final Color DARK_GREEN = new Color(0, 175, 0);
  
  private JPanel getThingsPanel() {
    mTable = new ThingsTable(true);
    registerForDnD(mTable);
    JPanel thingsTablePanel = mTable.getPanelWithTable();

    JPanel lowerPanel = new JPanel();
    lowerPanel.setLayout(null);
    lowerPanel.setPreferredSize(new java.awt.Dimension(150, 40));
    lowerPanel.add(getSumLabel(), null);
    thingsTablePanel.add(lowerPanel, BorderLayout.SOUTH);

    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(null);
    rightPanel.setPreferredSize(new java.awt.Dimension(70, 50));
    rightPanel.add(getAddButton(), null);
    rightPanel.add(getRemoveButton(), null);
    thingsTablePanel.add(rightPanel, BorderLayout.EAST);

    thingsTablePanel.setPreferredSize(new Dimension(200, 200));
        
    if (animal != null) {
      Things things = Things.getInstance();
      for (String name : animal.getThings()) {
        Thing thing = things.getThing(name);
        if (thing != null) {
          mTable.addThing(thing);
        }
        else
          mTable.addUnknownThing(name);
        mTable.setCount(name, animal.getThingCount(name));
      }
      calcSums();
      removeButton.setEnabled(animal.getThings().length > 0);
      addButton.setEnabled(true);
    }
    else {
      sumLabel
          .setText("Gesamt: Wert 0 Dukaten, Gewicht 0 Stein");
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
    }
    mTable.setFirstSelectedRow();
    
    thingsTablePanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
        "Gegenstände"));

    return thingsTablePanel;
  }

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
      if (type == Animal.AttributeType.eString) {
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
    }
    ++row;
    c.gridy = row;
    grid.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED),
        "Eigenschaften"));
    return grid;
  }

  protected boolean addItem(String item, ExtraThingData extraData) {
    if (extraData.getType() == ExtraThingData.Type.Weapon) {
      dsa.model.data.Weapon w = dsa.model.data.Weapons.getInstance().getWeapon(item);
      item = w.getName();
    }
    if (animal.getThingCount(item) == 0) {
      Thing thing = Things.getInstance().getThing(item);
      if (thing != null) {
        mTable.addThing(thing);
      }
      else if (extraData.getType() != ExtraThingData.Type.Thing) {
        try {
          String category = extraData.getProperty("Category");
          int value = extraData.getPropertyInt("Worth");
          int weight = extraData.getPropertyInt("Weight");
          thing = new Thing(item, new Optional<Integer>(value), Thing.Currency.S, weight, category, true);
          Things.getInstance().addThing(thing);
        }
        catch (ExtraThingData.PropertyException e) {
          e.printStackTrace();
          return false;
        }
      }
      else {
        JOptionPane.showMessageDialog(this, "Unbekannter Gegenstand.", 
            "Gegenstand hinzufügen", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    else
      mTable.setCount(item, animal.getThingCount(item) + 1);
    animal.addThing(item, extraData);
    removeButton.setEnabled(true);
    calcSums();
    return true;
  }
  
  protected ExtraThingData getExtraDnDData(String item) {
    return animal.getExtraThingData(item, animal.getThingCount(item));
  }

  protected void removeItem(String name) {
    int oldCount = animal.getThingCount(name);
    animal.removeThing(name);
    if (oldCount != 1) {
      mTable.setCount(name, oldCount - 1);
    }
    // else
      // mTable.RemoveSelectedThing();
    removeButton.setEnabled(animal.getThings().length > 0);
    calcSums();
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

  public void thingChanged(String thing) {
    if (mTable.containsItem(thing)) {
      mTable.removeThing(thing);
      mTable.addThing(Things.getInstance().getThing(thing));
      mTable.setCount(thing, animal.getThingCount(thing));
      calcSums();
    }
  }
}

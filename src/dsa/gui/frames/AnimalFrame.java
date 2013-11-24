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
package dsa.gui.frames;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import dsa.gui.dialogs.AnimalProbeDialog;
import dsa.gui.util.ImageManager;
import dsa.model.DiceSpecification;
import dsa.model.data.Animal;

public class AnimalFrame extends JFrame {

  private static final int GAP = 10;

  JTextField nameField;

  JTextField categoryField;

  public Animal getAnimal() {
    return animal;
  }

  public AnimalFrame(Animal a) {
    super(a.getName());
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

    contentPane.add(grid, BorderLayout.CENTER);
    pack();

    listener = new Animal.NameListener() {
      public void nameChanged(String oldName, String newName) {
        AnimalFrame.this.setTitle(newName);
      }
    };
    animal.addListener(listener);
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animal.removeListener(listener);
      }

      public void windowClosed(WindowEvent e) {
        animal.removeListener(listener);
      }
    });
  }

  protected JRootPane createRootPane() {
    String rootPaneClass = UIManager.getString("dsa.gui.rootPaneClass");
    if (rootPaneClass == null || rootPaneClass.equals("")) {
      return super.createRootPane();
    }
    else
      try {
        return (JRootPane) Class.forName(rootPaneClass).newInstance();
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
      catch (InstantiationException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
        return super.createRootPane();
      }
  }

  private Animal.NameListener listener;

  private Animal animal;

  private HashMap<Integer, JTextField> textFields = new HashMap<Integer, JTextField>();

  private HashMap<Integer, JSpinner> spinners = new HashMap<Integer, JSpinner>();

  class ProbePerformer implements ActionListener {
    public ProbePerformer(int i) {
      nr = i;
    }

    private int nr;

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
      animal.setAttributeValue(nr, spinners.get(nr).getValue());
    }
  }

  class MyDocumentListener implements DocumentListener {
    public MyDocumentListener(int i) {
      nr = i;
    }

    private int nr;

    public void insertUpdate(DocumentEvent e) {
      animal.setAttributeValue(nr, textFields.get(nr).getText());
    }

    public void removeUpdate(DocumentEvent e) {
      animal.setAttributeValue(nr, textFields.get(nr).getText());
    }

    public void changedUpdate(DocumentEvent e) {
      animal.setAttributeValue(nr, textFields.get(nr).getText());
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
        JOptionPane.showMessageDialog(AnimalFrame.this, e.getMessage(),
            "Fehler", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}

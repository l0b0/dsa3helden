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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.text.NumberFormatter;

import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.Currencies;

public class MoneyFrame extends SubFrame implements CharactersObserver {

  private JScrollPane jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JButton addButton = null;

  private JButton removeButton = null;

  /**
   * This method initializes
   * 
   */
  public MoneyFrame() {
    super();
    initialize();
  }

  dsa.model.characters.Hero currentHero = null;

  private boolean bank;

  public MoneyFrame(String title, boolean bank) {
    super(title);
    this.bank = bank;
    Group.getInstance().addObserver(this);
    currentHero = Group.getInstance().getActiveHero();
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(MoneyFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(MoneyFrame.this);
          done = true;
        }
      }
    });
    initialize();
    updateData();
  }

  private class MoneyChangeListener implements PropertyChangeListener {

    public MoneyChangeListener(int index) {
      mIndex = index;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      if (!evt.getPropertyName().equals("value")) return;
      int value = ((Number) ((JFormattedTextField) evt.getSource()).getValue())
          .intValue();
      if (currentHero != null) {
        currentHero.setMoney(mIndex, value, bank);
      }
      updateData();
    }

    private final int mIndex;
  }

  private class CurrencyChangeListener implements ActionListener {
    public CurrencyChangeListener(int index, JFormattedTextField field) {
      mIndex = index;
      mField = field;
    }

    public void actionPerformed(ActionEvent e) {
      int newCurrency = ((JComboBox) e.getSource()).getSelectedIndex();
      if (currentHero == null) return;
      int oldCurrency = currentHero.getCurrency(mIndex, bank);
      int oldValue = currentHero.getMoney(mIndex, bank);
      int newValue = Currencies.getInstance().changeValueIndex(oldValue,
          oldCurrency, newCurrency);
      mField.setText("" + newValue);
      currentHero.setCurrency(mIndex, newCurrency, bank);
      currentHero.setMoney(mIndex, newValue, bank);
      updateData();
    }

    private final int mIndex;

    private final JFormattedTextField mField;
  }

  private final ArrayList<JFormattedTextField> valueFields = new ArrayList<JFormattedTextField>();

  private final ArrayList<JComboBox> currencyBoxes = new ArrayList<JComboBox>();

  private void addCurrency(JPanel panel, int i, int currency, int value) {
    JFormattedTextField field = new JFormattedTextField(new NumberFormatter(
        NumberFormat.getIntegerInstance()));
    GridBagConstraints c = new GridBagConstraints();
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = i;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.WEST;
    field.setPreferredSize(new java.awt.Dimension(35, 25));
    field.setValue(Integer.valueOf(value));
    field.addPropertyChangeListener(new MoneyChangeListener(i));
    panel.add(field, c);
    valueFields.add(field);
    JComboBox comboBox = new JComboBox(Currencies.getInstance()
        .getCurrencyNames());
    comboBox.setSelectedIndex(currency);
    comboBox.addActionListener(new CurrencyChangeListener(i, field));
    c.gridx = 1;
    c.weightx = 0.5;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(comboBox, c);
    currencyBoxes.add(comboBox);
    updateData();
    this.validate();
    this.repaint();
  }

  private void removeCurrency(JPanel panel, int index) {
    panel.remove(valueFields.get(index));
    panel.remove(currencyBoxes.get(index));
    valueFields.remove(index);
    currencyBoxes.remove(index);
    updateData();
    this.validate();
    this.repaint();
  }

  private boolean reactToChanges = true;

  private void updateData() {
    if (!reactToChanges) return;
    reactToChanges = false;
    currentHero = Group.getInstance().getActiveHero();
    JPanel panel = getJPanel();
    panel.removeAll();
    valueFields.clear();
    currencyBoxes.clear();
    int nrOfCurrencies = (currentHero != null) ? currentHero
        .getNrOfCurrencies(bank) : 0;
    for (int i = 0; i < nrOfCurrencies; ++i) {
      addCurrency(panel, i, currentHero.getCurrency(i, bank), currentHero
          .getMoney(i, bank));
    }
    removeButton.setEnabled(nrOfCurrencies > 0);
    updateWeightLabel();
    reactToChanges = true;
  }

  private void updateWeightLabel() {
    int i = 0;
    long weightInTenthOfScrupels = 0;
    long worth = 0;
    for (JFormattedTextField value : valueFields) {
      int amount = ((Number) value.getValue()).intValue();
      int currency = currencyBoxes.get(i).getSelectedIndex();
      weightInTenthOfScrupels += amount
          * Currencies.getInstance().getWeight(currency);
      worth += amount * Currencies.getInstance().getBaseValue(currency);
      ++i;
    }
    long weightInUnzes = weightInTenthOfScrupels / 250;
    double worthInDuks = (double) worth
        / (double) Currencies.getInstance().getBaseValue(0);
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(true);
    format.setMaximumFractionDigits(2);
    format.setMinimumIntegerDigits(1);
    format.setMinimumFractionDigits(0);
    String text = "Gesamt: Wert ca. " + format.format(worthInDuks) + " Dukaten";
    if (!bank) text += ", Gewicht ca. " + weightInUnzes + " Unzen";
    getWeightLabel().setText(text);
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    // this.setSize(new java.awt.Dimension(348,213));
    this.setContentPane(getJContentPane());
    // this.setTitle("Geld");
  }

  private JPanel getSidePanel() {
    if (sidePanel == null) {
      sidePanel = new JPanel();
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(7, 0, 80, 29));
      jLabel.setText("Währungen");
      sidePanel.setLayout(null);
      sidePanel.setPreferredSize(new java.awt.Dimension(100, 90));
      sidePanel.add(jLabel, null);
      sidePanel.add(getAddButton(), null);
      sidePanel.add(getRemoveButton(), null);
    }
    return sidePanel;
  }

  private JPanel sidePanel;

  private JPanel upperPanel;

  private JLabel weightLabel;

  private JLabel getWeightLabel() {
    if (weightLabel == null) {
      weightLabel = new JLabel();
      weightLabel.setForeground(java.awt.Color.BLUE);
      weightLabel.setPreferredSize(new java.awt.Dimension(150, 20));
    }
    return weightLabel;
  }

  private JPanel getUpperPanel() {
    if (upperPanel == null) {
      upperPanel = new JPanel(new BorderLayout(5, 5));
      upperPanel.add(getWeightLabel(), BorderLayout.CENTER);
      JLabel upper = new JLabel("");
      upper.setPreferredSize(new java.awt.Dimension(10, 10));
      JLabel left = new JLabel("");
      left.setPreferredSize(new java.awt.Dimension(10, 10));
      upperPanel.add(upper, BorderLayout.NORTH);
      upperPanel.add(left, BorderLayout.WEST);
    }
    return upperPanel;
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JScrollPane getJContentPane() {
    if (jContentPane == null) {
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BorderLayout());
      JPanel temp = new JPanel(new BorderLayout());
      JPanel temp2 = new JPanel(new BorderLayout());
      JLabel filler = new JLabel("");
      filler.setPreferredSize(new java.awt.Dimension(10, 20));
      temp2.add(filler, BorderLayout.NORTH);
      temp2.add(getJPanel(), BorderLayout.CENTER);
      temp.add(temp2, BorderLayout.NORTH);
      temp.add(new JLabel(""), BorderLayout.CENTER);
      innerPanel.add(temp, BorderLayout.CENTER);
      innerPanel.add(getSidePanel(), BorderLayout.EAST);
      // JLabel upper = new JLabel("");
      // upper.setPreferredSize(new java.awt.Dimension(10, 10));
      JLabel left = new JLabel("");
      left.setPreferredSize(new java.awt.Dimension(10, 10));
      JLabel lower = new JLabel("");
      lower.setPreferredSize(new java.awt.Dimension(10, 10));
      innerPanel.add(getUpperPanel(), BorderLayout.NORTH);
      innerPanel.add(lower, BorderLayout.SOUTH);
      innerPanel.add(left, BorderLayout.WEST);
      jContentPane = new JScrollPane(innerPanel);
      jContentPane.setOpaque(false);
      jContentPane.getViewport().setOpaque(false);
    }
    return jContentPane;
  }

  /**
   * This method initializes jPanel
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jPanel = new JPanel();
      jPanel.setBounds(new java.awt.Rectangle(13, 19, 221, 3));
      GridBagLayout layout = new java.awt.GridBagLayout();
      jPanel.setLayout(layout);
    }
    return jPanel;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton(ImageManager.getIcon("increase"));
      addButton.setBounds(new java.awt.Rectangle(5, 26, 78, 23));
      addButton.setToolTipText("Währung hinzufügen");
      addButton.addActionListener((new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (currentHero == null) return;
          currentHero.addCurrency(bank);
          addCurrency(getJPanel(), valueFields.size(), 0, 0);
          removeButton.setEnabled(true);
        }
      }));
    }
    return addButton;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setBounds(new java.awt.Rectangle(5, 60, 78, 23));
      removeButton.setToolTipText("Unterste Währung entfernen");
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (currentHero == null) return;
          currentHero.removeCurrency(bank);
          removeCurrency(getJPanel(), currentHero.getNrOfCurrencies(bank));
          if (currentHero.getNrOfCurrencies(bank) == 0) {
            removeButton.setEnabled(false);
          }
        }
      });
    }
    return removeButton;
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    updateData();
  }

  public void characterRemoved(Hero character) {
    updateData();
  }

  public void characterAdded(Hero character) {
    updateData();
  }

  public void globalLockChanged() {
  }
} // @jve:decl-index=0:visual-constraint="10,10"

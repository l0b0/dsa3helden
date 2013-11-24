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
package dsa.gui.dialogs;

import dsa.control.Dice;
import dsa.control.Probe;
import dsa.gui.lf.BGDialog;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Talent;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

public class ShopDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JLabel jLabel = null;
  private JLabel jLabel1 = null;
  private JLabel jLabel2 = null;
  private JLabel jLabel3 = null;
  private JLabel jLabel4 = null;
  private JButton barterButton = null;
  private JLabel jLabel5 = null;
  private JPanel jPanel = null;
  private JButton okButton = null;
  private JButton cancelButton = null;
  private JTextField defaultPriceFiield = null;
  private JComboBox categoryBox = null;
  private JSpinner zonesSpinner = null;
  private JSpinner countSpinner = null;
  private JLabel jLabel6 = null;
  private JSpinner barterSpinner = null;
  private JTextField finalPriceField = null;
  private JLabel categoryLabel = null;
  private JLabel zonesLabel = null;
  private JLabel barterLabel = null;
  private JLabel barterResultLabel = null;
  private JLabel unitLabel1 = null;
  private JLabel unitLabel3 = null;

  /**
   * This method initializes 
   * 
   */
  public ShopDialog() {
  	super();
  	initialize();
  }
  
  private int defaultPrice;
  private int finalPrice;
  private boolean closedByOK;
  private int count;
  private Thing.Currency currency;
  
  private boolean barterSuccessful;
  
  private final static String[] PRICE_CATEGORY_NAMES = { "Schnäppchen", "Billig", "Normal", "Teuer", "Wucher"};
  private final static double[] PRICE_CATEGORIES = { 0.7, 0.85, 1, 1.20, 1.5 };
  private JLabel jLabel7 = null;
  private JLabel jLabel8 = null;
  private JSpinner zonesSpinner1 = null;
  private JLabel zonesLabel1 = null;
  private JLabel tooExpensiveLabel = null;
  
  public ShopDialog(JDialog owner, int price, Thing.Currency currency) {
    super(owner, "Einkaufen", true);
    initialize();
    String c = currency.toString();
    unitLabel1.setText(c);
    unitLabel3.setText(c);
    getRootPane().setDefaultButton(getOkButton());
    setEscapeButton(getCancelButton());
    this.setLocationRelativeTo(owner);
    defaultPriceFiield.setText("" + price);
    defaultPrice = finalPrice = price;
    closedByOK = false;
    count = 1;
    barterSuccessful = false;
    this.currency = currency;
    int random = Dice.roll(20);
    int priceIndex = 0;
    if (random < 3) priceIndex = 0;
    else if (random < 5) priceIndex = 1;
    else if (random < 12) priceIndex = 2;
    else if (random < 18) priceIndex = 3;
    else priceIndex = 4;
    getCategoryBox().setSelectedIndex(priceIndex);
  }
  
  public boolean wasClosedByOK() {
    return closedByOK;
  }
  
  public int getFinalPrice() {
    return finalPrice;
  }
  
  public int getCount() {
    return count;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(338, 340));
    this.setContentPane(getJContentPane());
    this.setTitle("Einkaufen");
  }

  @Override
  public String getHelpPage() {
    return "Einkaufen";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel5 = new JLabel();
      jLabel5.setText("Endpreis:");
      jLabel5.setBounds(new Rectangle(10, 220, 101, 21));
      jLabel4 = new JLabel();
      jLabel4.setText("Feilschen:");
      jLabel4.setBounds(new Rectangle(10, 160, 101, 21));
      jLabel3 = new JLabel();
      jLabel3.setText("Anzahl:");
      jLabel3.setBounds(new Rectangle(10, 130, 101, 21));
      jLabel2 = new JLabel();
      jLabel2.setText("Zonen:");
      jLabel2.setBounds(new Rectangle(10, 70, 51, 21));
      jLabel1 = new JLabel();
      jLabel1.setText("Preislage:");
      jLabel1.setBounds(new Rectangle(10, 40, 101, 21));
      jLabel = new JLabel();
      jLabel.setText("Normalpreis:");
      jLabel.setBounds(new Rectangle(10, 10, 101, 21));
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getOkButton(), null);
      jContentPane.add(getCancelButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes barterButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getBarterButton() {
    if (barterButton == null) {
      barterButton = new JButton();
      barterButton.setBounds(new Rectangle(120, 190, 51, 21));
      barterButton.setIcon(ImageManager.getIcon("probe"));
      barterButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          makeBarterTest();
        }
      });
    }
    return barterButton;
  }

  private void makeBarterTest() {
    Hero hero = dsa.model.characters.Group.getInstance().getActiveHero();
    if (hero == null) return;
    Probe probe = new Probe();
    final String talentName = "Feilschen";
    if (!hero.hasTalent(talentName)) {
      JOptionPane.showMessageDialog(this, "Der Held kann nicht feilschen.", "Heldenverwaltung", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    probe.setSkill(hero.getCurrentTalentValue(talentName));
    Talent talent = Talents.getInstance().getTalent(talentName);
    if (talent == null) throw new InternalError("Talent 'Feilschen' nicht gefunden!");
    if (!talent.canBeTested()) throw new InternalError("Talent 'Feilschen' erlaubt keine Probe!");
    NormalTalent barterTalent = (NormalTalent)talent;
    probe.setFirstProperty(hero.getCurrentProperty(barterTalent.getFirstProperty()));
    probe.setSecondProperty(hero.getCurrentProperty(barterTalent.getSecondProperty()));
    probe.setThirdProperty(hero.getCurrentProperty(barterTalent.getThirdProperty()));
    probe.setModifier(((Number)barterSpinner.getValue()).intValue());
    barterSuccessful = probe.performTest(Dice.roll(20), Dice.roll(20), Dice.roll(20));
    if (barterSuccessful) {
      barterResultLabel.setText("Probe gelungen!");
    }
    else {
      barterResultLabel.setText("Probe misslungen.");
    }
    calcFinalPrice();
  }

  private void calcFinalPrice() {
    int price = defaultPrice;
    price *= PRICE_CATEGORIES[categoryBox.getSelectedIndex()];
    price *= Math.pow(1.5, ((Number)zonesSpinner.getValue()).intValue());
    price *= Math.pow(1.2, ((Number)zonesSpinner1.getValue()).intValue());
    count = ((Number)countSpinner.getValue()).intValue();
    price *= count;
    if (barterSuccessful) {
      price *= (1 - ((Number)barterSpinner.getValue()).intValue() * 0.05);
    }
    if (price == 0)
      price = 1;
    finalPriceField.setText("" + price);
    finalPrice = price;
    if (!dsa.model.characters.Group.getInstance().getActiveHero().canPay(finalPrice, currency)) {
      tooExpensiveLabel.setText("Zu teuer!");
      okButton.setEnabled(false);
    }
    else {
      tooExpensiveLabel.setText("");
      okButton.setEnabled(true);
    }
  }
  
  

  /**
   * This method initializes jPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      tooExpensiveLabel = new JLabel();
      tooExpensiveLabel.setBounds(new Rectangle(210, 221, 81, 18));
      tooExpensiveLabel.setText("");
      tooExpensiveLabel.setForeground(java.awt.Color.RED);
      zonesLabel1 = new JLabel();
      zonesLabel1.setBounds(new Rectangle(240, 100, 51, 21));
      zonesLabel1.setText("*1.0");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new Rectangle(80, 100, 31, 21));
      jLabel8.setText("See:");
      jLabel7 = new JLabel();
      jLabel7.setBounds(new Rectangle(70, 70, 41, 21));
      jLabel7.setText("Land:");
      unitLabel3 = new JLabel();
      unitLabel3.setBounds(new Rectangle(180, 220, 21, 21));
      unitLabel3.setText("D");
      unitLabel1 = new JLabel();
      unitLabel1.setBounds(new Rectangle(180, 10, 21, 21));
      unitLabel1.setText("D");
      barterResultLabel = new JLabel();
      barterResultLabel.setBounds(new Rectangle(180, 190, 111, 21));
      barterResultLabel.setText("Nicht probiert.");
      barterLabel = new JLabel();
      barterLabel.setBounds(new Rectangle(240, 160, 51, 21));
      barterLabel.setText("-0%");
      zonesLabel = new JLabel();
      zonesLabel.setBounds(new Rectangle(240, 70, 51, 21));
      zonesLabel.setText("*1.0");
      categoryLabel = new JLabel();
      categoryLabel.setBounds(new Rectangle(240, 40, 51, 21));
      categoryLabel.setText("+20%");
      jLabel6 = new JLabel();
      jLabel6.setBounds(new Rectangle(110, 160, 11, 21));
      jLabel6.setText("+");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new Rectangle(10, 10, 301, 251));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel1, null);
      jPanel.add(jLabel2, null);
      jPanel.add(jLabel3, null);
      jPanel.add(jLabel4, null);
      jPanel.add(getBarterButton(), null);
      jPanel.add(jLabel5, null);
      jPanel.add(getDefaultPriceFiield(), null);
      jPanel.add(getCategoryBox(), null);
      jPanel.add(getZonesSpinner(), null);
      jPanel.add(getCountSpinner(), null);
      jPanel.add(jLabel6, null);
      jPanel.add(getBarterSpinner(), null);
      jPanel.add(getFinalPriceField(), null);
      jPanel.add(categoryLabel, null);
      jPanel.add(zonesLabel, null);
      jPanel.add(barterLabel, null);
      jPanel.add(barterResultLabel, null);
      jPanel.add(unitLabel1, null);
      jPanel.add(unitLabel3, null);
      jPanel.add(jLabel7, null);
      jPanel.add(jLabel8, null);
      jPanel.add(getZonesSpinner1(), null);
      jPanel.add(zonesLabel1, null);
      jPanel.add(tooExpensiveLabel, null);
    }
    return jPanel;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(40, 280, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closedByOK = true;
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(180, 280, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes defaultPriceFiield	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getDefaultPriceFiield() {
    if (defaultPriceFiield == null) {
      defaultPriceFiield = new JTextField();
      defaultPriceFiield.setBounds(new Rectangle(120, 10, 51, 21));
      defaultPriceFiield.setEditable(false);
      defaultPriceFiield.setHorizontalAlignment(JTextField.RIGHT);
    }
    return defaultPriceFiield;
  }

  /**
   * This method initializes categoryBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getCategoryBox() {
    if (categoryBox == null) {
      categoryBox = new JComboBox();
      categoryBox.setBounds(new Rectangle(120, 40, 111, 21));
      for (int i = 0; i < PRICE_CATEGORY_NAMES.length; ++i) {
        categoryBox.addItem(PRICE_CATEGORY_NAMES[i]);
      }
      categoryBox.setSelectedIndex(2);
      categoryBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          double val = PRICE_CATEGORIES[categoryBox.getSelectedIndex()];
          int percent = (int) (val * 100.0);
          if (percent >= 100) {
            percent -= 100;
            categoryLabel.setText("+" + percent + "%");
          }
          else {
            percent = 100 - percent;
            categoryLabel.setText("-" + percent + "%");
          }
          calcFinalPrice();
        }
      });
    }
    return categoryBox;
  }

  /**
   * This method initializes zonesSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getZonesSpinner() {
    if (zonesSpinner == null) {
      zonesSpinner = new JSpinner();
      zonesSpinner.setBounds(new Rectangle(120, 70, 51, 21));
      zonesSpinner.setModel(new SpinnerNumberModel(0, 0, 10, 1));
      zonesSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent arg0) {
          int val = ((Number)zonesSpinner.getValue()).intValue();
          double factor = Math.pow(1.5, val);
          String sFactor = java.text.NumberFormat.getNumberInstance().format(factor);
          zonesLabel.setText("*" + sFactor);
          calcFinalPrice();
        }
      });
    }
    return zonesSpinner;
  }

  /**
   * This method initializes countSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getCountSpinner() {
    if (countSpinner == null) {
      countSpinner = new JSpinner();
      countSpinner.setBounds(new Rectangle(120, 130, 51, 21));
      countSpinner.setModel(new SpinnerNumberModel(1, 1, 99, 1));
      countSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent arg0) {
          calcFinalPrice();
        }        
      });
    }
    return countSpinner;
  }

  /**
   * This method initializes barterSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getBarterSpinner() {
    if (barterSpinner == null) {
      barterSpinner = new JSpinner();
      barterSpinner.setBounds(new Rectangle(120, 160, 51, 21));
      barterSpinner.setModel(new SpinnerNumberModel(0, 0, 19, 1));
      barterSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent arg0) {
          barterResultLabel.setText("Nicht probiert.");
          barterSuccessful = false;
          int value = ((Number)barterSpinner.getValue()).intValue();
          barterLabel.setText("-" + (5 * value) + "%");
          calcFinalPrice();
        }        
      });
    }
    return barterSpinner;
  }

  /**
   * This method initializes finalPriceField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getFinalPriceField() {
    if (finalPriceField == null) {
      finalPriceField = new JFormattedTextField(new NumberFormatter(
          NumberFormat.getIntegerInstance()));
      finalPriceField.setBounds(new Rectangle(120, 220, 51, 21));
      finalPriceField.setHorizontalAlignment(JTextField.RIGHT);
      finalPriceField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          changed();
        }

        public void removeUpdate(DocumentEvent e) {
          changed();
        }

        public void changedUpdate(DocumentEvent e) {
          changed();
        }
        
        private void changed() {
          Number value = ((Number)((JFormattedTextField)finalPriceField).getValue());
          finalPrice = value != null ? value.intValue() : 0;
          if (!dsa.model.characters.Group.getInstance().getActiveHero().canPay(finalPrice, currency)) {
            tooExpensiveLabel.setText("Zu teuer!");
            okButton.setEnabled(false);
          }
          else {
            tooExpensiveLabel.setText("");
            okButton.setEnabled(true);
          }          
        }
      });
    }
    return finalPriceField;
  }

  /**
   * This method initializes zonesSpinner1	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getZonesSpinner1() {
    if (zonesSpinner1 == null) {
      zonesSpinner1 = new JSpinner();
      zonesSpinner1.setBounds(new Rectangle(120, 100, 51, 21));
      zonesSpinner1.setModel(new SpinnerNumberModel(0, 0, 10, 1));
      zonesSpinner1.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent arg0) {
          int val = ((Number)zonesSpinner1.getValue()).intValue();
          double factor = Math.pow(1.2, val);
          java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
          zonesLabel1.setText("*" + format.format(factor));
          calcFinalPrice();
        }        
      });
    }
    return zonesSpinner1;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

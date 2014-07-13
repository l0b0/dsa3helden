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
import dsa.gui.dialogs.ItemProviders.ItemProvider;
import dsa.gui.lf.BGDialog;
import dsa.gui.tables.AbstractTable;
import dsa.gui.tables.ThingsTable;
import dsa.gui.util.ImageManager;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.data.Talents;
import dsa.model.data.Thing;
import dsa.model.data.Things;
import dsa.model.data.Tradezones;
import dsa.model.talents.NormalTalent;
import dsa.model.talents.Talent;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;

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
  private JTextField defaultPriceField = null;
  private JComboBox categoryBox = null;
  private JLabel jLabel6 = null;
  private JSpinner barterSpinner = null;
  private JTextField finalPriceField = null;
  private JLabel categoryLabel = null;
  private JLabel barterLabel = null;
  private JLabel barterResultLabel = null;
  private JLabel unitLabel1 = null;
  private JLabel unitLabel3 = null;
  
  static int sLastTraderIndex = -1;
  static int sLastPriceIndex = -1;

  private ItemProvider mProvider;
  
  private int finalPrice;
  private boolean closedByOK;
  private Thing.Currency currency;
  
  private boolean barterSuccessful;
  
  private final static String[] PRICE_CATEGORY_NAMES = { "Schnäppchen", "Billig", "Normal", "Teuer", "Wucher"};
  private final static double[] PRICE_CATEGORIES = { 0.7, 0.85, 1, 1.20, 1.5 };
  private JLabel zonesLabel1 = null;
  private JLabel tooExpensiveLabel = null;
  private JLabel zonesLabel = null;
  private JLabel importLabel = null;
  private JButton zonesButton = null;
  
  private String[] mSourceZones;
  
  private HashMap<String, Integer> mCart = new HashMap<String, Integer>();
  private HashMap<String, Integer> mPrices = new HashMap<String, Integer>();
  private String mCurrentItemName;
  private int mLastPriceCategory;
  
  Thing.Currency currentItemCurrency;
  
  public ShopDialog(JFrame owner, ItemProvider provider) {
    super(owner, "Einkaufen", true);
    mProvider = provider;
    initialize();
    getRootPane().setDefaultButton(getOkButton());
    setEscapeButton(getCancelButton());
    this.setLocationRelativeTo(owner);
    closedByOK = false;
    int traderIndex = 0;
    getTraderBox().addItem("Trödler");
    getTraderBox().addItem("Krämer");
    getTraderBox().addItem("Händler");
    getTraderBox().addItem("Großhändler");
    getTraderBox().addItem("Störrebrandt");
    if (sLastTraderIndex == -1) {
      int random = Dice.roll(20);
      if (random < 3) traderIndex = 4;
      else if (random < 7) traderIndex = 3;
      else if (random < 15) traderIndex = 2;
      else if (random < 19) traderIndex = 1;
      else traderIndex = 0;
    }
    else traderIndex = sLastTraderIndex;
    getTraderBox().setSelectedIndex(traderIndex);
    traderBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calcPriceIndex();
        calcBorderInfo();
        calcFinalPrice();
      }
    });
    if (sLastPriceIndex == -1) {
      calcPriceIndex();
    }
    else {
      getCategoryBox().setSelectedIndex(sLastPriceIndex);
    }
    mLastPriceCategory = getCategoryBox().getSelectedIndex();
    unitLabel1.setText("D");
    unitLabel2.setText("D");
    unitLabel3.setText("D");
    unitLabel4.setText("D");
    barterSuccessful = false;
    addButton.setEnabled(false);
    removeButton.setEnabled(false);
    zonesButton.setEnabled(false);
    this.currency = Thing.Currency.D;
    mCurrentItemName = "";
    defaultPriceField.setText("");
    calcFinalPrice();
  }
  
  private void itemSelected(String name, Thing.Currency currency, int price, String[] sourceZones)
  {
    if (name.equals(mCurrentItemName)) return;
    manualZone = false;
    String c = currency.toString();
    unitLabel1.setText(c);
    unitLabel2.setText(c);
    defaultPriceField.setText("" + price);
    mSourceZones = sourceZones;
    calcBorderInfo();
    addButton.setEnabled(true);
    removeButton.setEnabled(mCart.containsKey(name));
    zonesButton.setEnabled(true);
    mCurrentItemName = name;
    currentItemCurrency = currency;
    calcItemPrice();
  }
  
  private void calcItemPrice() {
    String text = defaultPriceField.getText();
    if (text == null || text.equals("")) return;
    int price = Integer.parseInt(text);
    price *= PRICE_CATEGORIES[categoryBox.getSelectedIndex()];
    if (mBorderInfo != null) {
      price *= Math.pow(1.5, mBorderInfo.getNrOfLandBorders());
      price *= Math.pow(1.2, mBorderInfo.getNrOfSeaBorders());
    }
    if (price == 0)
      price = 1;
    itemPriceField.setText("" + price);
  }
  
  private void calcPriceIndex() {
    int random = Dice.roll(20);
    
    int traderIndex = getTraderBox().getSelectedIndex();
    if (traderIndex == 4) random += 4;
    else if (traderIndex == 3) random += 2;
    else if (traderIndex == 1) random -= 1;
    else if (traderIndex == 0) random -= 3;
    // else if (traderIndex == 2) random += 0;
    
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
  
  public Thing.Currency getCurrency() {
    return currency;
  }
  
  public Map<String, Integer> getBoughtItems() {
    return mCart;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(840, 465));
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
      jLabel5.setBounds(new Rectangle(10, 110, 81, 21));
      jLabel4 = new JLabel();
      jLabel4.setText("Feilschen:");
      jLabel4.setBounds(new Rectangle(10, 50, 81, 21));
      jLabel3 = new JLabel();
      jLabel3.setText("Preis hier:");
      jLabel3.setBounds(new Rectangle(10, 110, 81, 21));
      jLabel2 = new JLabel();
      jLabel2.setText("Zonen:");
      jLabel2.setBounds(new Rectangle(10, 50, 51, 21));
      jLabel1 = new JLabel();
      jLabel1.setText("Preislage:");
      jLabel1.setBounds(new Rectangle(10, 50, 81, 21));
      jLabel = new JLabel();
      jLabel.setText("Normalpreis:");
      jLabel.setBounds(new Rectangle(10, 20, 81, 21));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel3(), BorderLayout.EAST);
      jContentPane.add(getJSplitPane(), BorderLayout.CENTER);
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
      barterButton.setIcon(ImageManager.getIcon("probe"));
      barterButton.setBounds(new Rectangle(100, 80, 51, 21));
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
  
  private Tradezones.BorderInfo mBorderInfo;
  private JLabel jLabel7 = null;
  private JComboBox traderBox = null;
  
  private static final Color DARKGREEN = new Color(0, 175, 0);
  private JPanel jPanel1 = null;
  private JLabel jLabel8 = null;
  private JTextField priceSumField = null;
  private JLabel unitLabel4 = null;
  private JPanel jPanel2 = null;
  private JButton addButton = null;
  private JButton removeButton = null;
  private JPanel jPanel3 = null;
  private JSplitPane jSplitPane = null;
  private JPanel cartPanel = null;
  private JPanel jPanel4 = null;
  private JPanel jPanel5 = null;
  private JLabel jLabel9 = null;
  private JTextField filterField = null;
  private JPanel waresPanel = null;
  private JTextField itemPriceField = null;
  private JLabel unitLabel2 = null;
  
  private boolean manualZone = false;
  
  private void calcBorderInfo() {
    if (manualZone) return;
    mBorderInfo = null;
    if (mSourceZones == null) return;
    String destination = Group.getInstance().getTradezone();
    for (String source : mSourceZones) {
      Tradezones.BorderInfo borders = Tradezones.getInstance().getTradeRoute(source, destination);
      if (borders != null) {
        borders = new Tradezones.BorderInfo(source, borders.getNrOfLandBorders(), borders.getNrOfSeaBorders());
        int chance = 18;
        chance -= borders.getNrOfLandBorders() * 3;
        chance -= borders.getNrOfSeaBorders();
        if (Dice.roll(20) <= chance) {
          if (mBorderInfo == null || borders.compareTo(mBorderInfo) < 0) {
            mBorderInfo = borders;
          }
        }
      }
    }
    calcZoneInfo();
  }
  
  private void calcZoneInfo() {
    if (mBorderInfo == null) {
      importLabel.setText("Nicht verfügbar");
      importLabel.setForeground(Color.RED);
      zonesLabel.setText("Keine Zonen");
      zonesLabel1.setText("*1.0");
    }
    else {
      importLabel.setText("Importiert aus: " + mBorderInfo.getSource());
      importLabel.setForeground(DARKGREEN);
      zonesLabel.setText("" + mBorderInfo.getNrOfLandBorders() + " Land, " 
          + mBorderInfo.getNrOfSeaBorders() + " See");
      double factor = 1.0;
      factor *= Math.pow(1.5, mBorderInfo.getNrOfLandBorders());
      factor *= Math.pow(1.2, mBorderInfo.getNrOfSeaBorders());
      java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance();
      format.setMinimumFractionDigits(2);
      zonesLabel1.setText("*" + format.format(factor));
    }
    calcItemPrice();
  }
  
  private void selectZone() {
    if (mSourceZones == null) return;
    TradezoneDialog dialog = new TradezoneDialog(this, mSourceZones);
    dialog.setVisible(true);
    String sourceZone = dialog.getSelectedZone();
    if (sourceZone != null) {
      mBorderInfo = null;
      manualZone = true;
      Tradezones.BorderInfo borders = Tradezones.getInstance().getTradeRoute(sourceZone, 
          Group.getInstance().getTradezone());
      if (borders != null) {
        borders = new Tradezones.BorderInfo(sourceZone, borders.getNrOfLandBorders(), borders.getNrOfSeaBorders());
        mBorderInfo = borders;
      }
      calcZoneInfo();
    }
  }

  private void calcFinalPrice() {
    int price = 0;
    for (String thing : mCart.keySet()) {
      price += mPrices.get(thing) * mCart.get(thing);
    }
    priceSumField.setText("" + price);
    if (barterSuccessful) {
      price *= (1 - ((Number)barterSpinner.getValue()).intValue() * 0.05);
    }
    finalPriceField.setText("" + price);
    finalPrice = price;
    if (finalPrice > 0 && !dsa.model.characters.Group.getInstance().getActiveHero().canPay(finalPrice, currency)) {
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
      unitLabel2 = new JLabel();
      unitLabel2.setBounds(new Rectangle(160, 110, 21, 21));
      unitLabel2.setText("D");
      jLabel7 = new JLabel();
      jLabel7.setText("Größe:");
      jLabel7.setBounds(new Rectangle(10, 20, 81, 21));
      importLabel = new JLabel();
      importLabel.setBounds(new Rectangle(100, 50, 131, 21));
      importLabel.setName("importLabel");
      importLabel.setText("Importiert aus NAG");
      zonesLabel = new JLabel();
      zonesLabel.setBounds(new Rectangle(100, 80, 131, 21));
      zonesLabel.setName("zonesLabel");
      zonesLabel.setText("3 Land, 2 See");
      tooExpensiveLabel = new JLabel();
      tooExpensiveLabel.setText("");
      tooExpensiveLabel.setBounds(new Rectangle(190, 110, 101, 21));
      tooExpensiveLabel.setForeground(java.awt.Color.RED);
      zonesLabel1 = new JLabel();
      zonesLabel1.setBounds(new Rectangle(240, 80, 51, 21));
      zonesLabel1.setText("*1.0");
      unitLabel3 = new JLabel();
      unitLabel3.setText("D");
      unitLabel3.setBounds(new Rectangle(160, 110, 21, 21));
      unitLabel1 = new JLabel();
      unitLabel1.setBounds(new Rectangle(160, 20, 21, 21));
      unitLabel1.setText("D");
      barterResultLabel = new JLabel();
      barterResultLabel.setText("Nicht probiert.");
      barterResultLabel.setBounds(new Rectangle(160, 80, 131, 21));
      barterLabel = new JLabel();
      barterLabel.setText("-0%");
      barterLabel.setBounds(new Rectangle(160, 50, 51, 21));
      categoryLabel = new JLabel();
      categoryLabel.setText("+20%");
      categoryLabel.setBounds(new Rectangle(240, 50, 51, 21));
      jLabel6 = new JLabel();
      jLabel6.setText("+");
      jLabel6.setBounds(new Rectangle(90, 50, 11, 21));
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Ware", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
      jPanel.setBounds(new Rectangle(10, 100, 301, 141));
      jPanel.add(jLabel, null);
      jPanel.add(jLabel2, null);
      jPanel.add(jLabel3, null);
      jPanel.add(getDefaultPriceField(), null);
      jPanel.add(unitLabel1, null);
      jPanel.add(zonesLabel1, null);
      jPanel.add(zonesLabel, null);
      jPanel.add(importLabel, null);
      jPanel.add(getZonesButton(), null);
      jPanel.add(getAddButton(), null);
      jPanel.add(getRemoveButton(), null);
      jPanel.add(getItemPriceField(), null);
      jPanel.add(unitLabel2, null);
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
      okButton.setText("OK");
      okButton.setBounds(new Rectangle(100, 400, 101, 21));
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          closedByOK = true;
          sLastTraderIndex = getTraderBox().getSelectedIndex();
          sLastPriceIndex = getCategoryBox().getSelectedIndex();
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
      cancelButton.setText("Abbrechen");
      cancelButton.setBounds(new Rectangle(210, 400, 101, 21));
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
  private JTextField getDefaultPriceField() {
    if (defaultPriceField == null) {
      defaultPriceField = new JTextField();
      defaultPriceField.setBounds(new Rectangle(100, 20, 51, 21));
      defaultPriceField.setEditable(false);
      defaultPriceField.setHorizontalAlignment(JTextField.RIGHT);
    }
    return defaultPriceField;
  }

  /**
   * This method initializes categoryBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getCategoryBox() {
    if (categoryBox == null) {
      categoryBox = new JComboBox();
      for (int i = 0; i < PRICE_CATEGORY_NAMES.length; ++i) {
        categoryBox.addItem(PRICE_CATEGORY_NAMES[i]);
      }
      categoryBox.setSelectedIndex(2);
      categoryBox.setBounds(new Rectangle(100, 50, 131, 21));
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
          if (categoryBox.getSelectedIndex() != mLastPriceCategory)
          {
            double lastVal = PRICE_CATEGORIES[mLastPriceCategory];
            for (String thing : mCart.keySet()) {
              int price = mPrices.get(thing);
              price /= lastVal;
              price *= val;
              mPrices.put(thing, price);
            }
            mLastPriceCategory = categoryBox.getSelectedIndex();
            calcItemPrice();
            calcFinalPrice();
          }
        }
      });
    }
    return categoryBox;
  }

  /**
   * This method initializes barterSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getBarterSpinner() {
    if (barterSpinner == null) {
      barterSpinner = new JSpinner();
      barterSpinner.setModel(new SpinnerNumberModel(0, 0, 19, 1));
      barterSpinner.setBounds(new Rectangle(100, 50, 51, 21));
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
      finalPriceField.setHorizontalAlignment(JTextField.RIGHT);
      finalPriceField.setBounds(new Rectangle(100, 110, 51, 21));
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
          if (finalPrice > 0 && !dsa.model.characters.Group.getInstance().getActiveHero().canPay(finalPrice, currency)) {
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
   * This method initializes zonesButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getZonesButton() {
    if (zonesButton == null) {
      zonesButton = new JButton();
      zonesButton.setBounds(new Rectangle(240, 50, 51, 21));
      zonesButton.setText("...");
      zonesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectZone(); 
        }
      });
    }
    return zonesButton;
  }

  /**
   * This method initializes traderBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getTraderBox() {
    if (traderBox == null) {
      traderBox = new JComboBox();
      traderBox.setBounds(new Rectangle(100, 20, 131, 21));
    }
    return traderBox;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      unitLabel4 = new JLabel();
      unitLabel4.setBounds(new Rectangle(160, 20, 23, 22));
      unitLabel4.setText("D");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new Rectangle(10, 20, 81, 21));
      jLabel8.setText("Summe:");
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gesamtpreis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
      jPanel1.setBounds(new Rectangle(10, 250, 301, 141));
      jPanel1.add(jLabel8, null);
      jPanel1.add(getPriceSumField(), null);
      jPanel1.add(jLabel4, null);
      jPanel1.add(jLabel6, null);
      jPanel1.add(getBarterSpinner(), null);
      jPanel1.add(barterLabel, null);
      jPanel1.add(jLabel5, null);
      jPanel1.add(getFinalPriceField(), null);
      jPanel1.add(unitLabel3, null);
      jPanel1.add(getBarterButton(), null);
      jPanel1.add(barterResultLabel, null);
      jPanel1.add(tooExpensiveLabel, null);
      jPanel1.add(unitLabel4, null);
    }
    return jPanel1;
  }

  /**
   * This method initializes priceSumField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getPriceSumField() {
    if (priceSumField == null) {
      priceSumField = new JTextField();
      priceSumField.setBounds(new Rectangle(100, 20, 51, 19));
      priceSumField.setEditable(false);
      priceSumField.setHorizontalAlignment(JTextField.RIGHT);
    }
    return priceSumField;
  }

  /**
   * This method initializes jPanel2	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel2() {
    if (jPanel2 == null) {
      jPanel2 = new JPanel();
      jPanel2.setLayout(null);
      jPanel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Händler", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
      jPanel2.setBounds(new Rectangle(10, 10, 301, 81));
      jPanel2.add(jLabel7, null);
      jPanel2.add(getTraderBox(), null);
      jPanel2.add(jLabel1, null);
      jPanel2.add(getCategoryBox(), null);
      jPanel2.add(categoryLabel, null);
    }
    return jPanel2;
  }

  /**
   * This method initializes addButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton();
      addButton.setBounds(new Rectangle(180, 110, 51, 21));
      addButton.setText("");
      addButton.setIcon(ImageManager.getIcon("increase"));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          addThing();
        }
      });
    }
    return addButton;
  }
  
  private void addThing() {
    if (mCart.containsKey(mCurrentItemName)) {
      mCart.put(mCurrentItemName, mCart.get(mCurrentItemName) + 1);
      cartTable.setCount(mCurrentItemName, mCart.get(mCurrentItemName));
    }
    else {
      Thing thing = Things.getInstance().getThing(mCurrentItemName);
      if (thing != null) {
        mCart.put(mCurrentItemName, 1);
        cartTable.addThing(thing);
      }
      else {
        mCart.put(mCurrentItemName, 1);
        cartTable.addUnknownThing(mCurrentItemName);
      }
    }
    int price = Integer.parseInt(itemPriceField.getText());
    if (currentItemCurrency != currency) {
      Thing.Currency maxCurrency = currentItemCurrency.ordinal() > currency.ordinal() ? currentItemCurrency : currency;
      if (maxCurrency != currentItemCurrency) {
        int factor = (maxCurrency.ordinal() - currentItemCurrency.ordinal()) * 10;
        price *= factor;
      }
      if (maxCurrency != currency) {
        int factor = (maxCurrency.ordinal() - currency.ordinal()) * 10;
        for (String t : mPrices.keySet()) {
          mPrices.put(t, mPrices.get(t) * factor);
        }
        currency = maxCurrency;
        unitLabel3.setText("" + currency);
        unitLabel4.setText("" + currency);
      }
    }
    mPrices.put(mCurrentItemName, price);
    calcFinalPrice();
    removeButton.setEnabled(true);
  }

  /**
   * This method initializes removeButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton();
      removeButton.setBounds(new Rectangle(238, 110, 53, 21));
      removeButton.setIcon(ImageManager.getIcon("decrease_enabled"));
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          removeThing();
        }
      });
    }
    return removeButton;
  }
  
  private void removeThing() {
    if (!mCart.containsKey(mCurrentItemName)) return;
    int count = mCart.get(mCurrentItemName) - 1;
    if (count > 0) {
      mCart.put(mCurrentItemName, count);
      cartTable.setCount(mCurrentItemName, count);
    }
    else {
      mCart.remove(mCurrentItemName);
      mPrices.remove(mCurrentItemName);
      cartTable.removeThing(mCurrentItemName);
    }
    calcFinalPrice();
  }

  /**
   * This method initializes jPanel3	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel3() {
    if (jPanel3 == null) {
      jPanel3 = new JPanel();
      jPanel3.setLayout(null);
      jPanel3.setPreferredSize(new Dimension(320, 400));
      jPanel3.add(getJPanel(), null);
      jPanel3.add(getJPanel2(), null);
      jPanel3.add(getJPanel1(), null);
      jPanel3.add(getOkButton(), null);
      jPanel3.add(getCancelButton(), null);
    }
    return jPanel3;
  }

  /**
   * This method initializes jSplitPane	
   * 	
   * @return javax.swing.JSplitPane	
   */
  private JSplitPane getJSplitPane() {
    if (jSplitPane == null) {
      jSplitPane = new JSplitPane();
      jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      jSplitPane.setTopComponent(getJPanel4());
      jSplitPane.setBottomComponent(getCartPane());
    }
    return jSplitPane;
  }
  
  private ThingsTable cartTable;
  private AbstractTable selectionTable;

  /**
   * This method initializes cartPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JPanel getCartPane() {
    if (cartPanel == null) {
      cartPanel = new JPanel();
      cartPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Warenkorb", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
      cartPanel.setPreferredSize(new Dimension(200, 100));
      cartTable = new ThingsTable(true, true);
      cartTable.addSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent arg0) {
          thingSelected(cartTable.getSelectedItem());
        }
      });
      cartPanel.setLayout(new BorderLayout());
      cartPanel.add(cartTable.getPanelWithTable(), BorderLayout.CENTER);
    }
    return cartPanel;
  }
  
  private void thingSelected(String name) {
    if (name != null) {
      Thing.Currency currency = mProvider.getCurrency(name);
      int price = mProvider.getDefaultPrice(name);
      String[] zones = mProvider.getTradezones(name);
      itemSelected(name, currency, price, zones);
    }
    else {
      addButton.setEnabled(false);
      removeButton.setEnabled(false);
      zonesButton.setEnabled(false);
    }
  }

  /**
   * This method initializes jPanel4	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel4() {
    if (jPanel4 == null) {
      jPanel4 = new JPanel();
      jPanel4.setLayout(new BorderLayout());
      jPanel4.setPreferredSize(new Dimension(350, 270));
      jPanel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Auswahl", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
      jPanel4.add(getJPanel5(), BorderLayout.NORTH);
      jPanel4.add(getWaresPane(), BorderLayout.CENTER);
    }
    return jPanel4;
  }

  /**
   * This method initializes jPanel5	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel5() {
    if (jPanel5 == null) {
      jLabel9 = new JLabel();
      jLabel9.setBounds(new Rectangle(5, 0, 45, 21));
      jLabel9.setText("Filter:");
      jPanel5 = new JPanel();
      jPanel5.setLayout(null);
      jPanel5.setPreferredSize(new Dimension(200, 30));
      jPanel5.add(jLabel9, null);
      jPanel5.add(getFilterField(), null);
      jPanel5.add(getSingularBox(), null);
    }
    return jPanel5;
  }

  /**
   * This method initializes filterField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getFilterField() {
    if (filterField == null) {
      filterField = new JTextField();
      filterField.setBounds(new Rectangle(55, 1, 139, 19));
      filterField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          filter();
        }

        public void removeUpdate(DocumentEvent e) {
          filter();
        }

        public void changedUpdate(DocumentEvent e) {
          filter();
        }

      });
    }
    return filterField;
  }
  
  private void filter() {
    mProvider.setFilter(filterField.getText());
    selectionTable.clear();
    mProvider.fillTable(showSingularItems());
  }

  /**
   * This method initializes waresPane	
   * 	
   * @return javax.swing.JScrollPane	
   */
  private JPanel getWaresPane() {
    if (waresPanel == null) {
      waresPanel = new JPanel();
      selectionTable = mProvider.getTable();
      mProvider.fillTable(showSingularItems());
      selectionTable.addSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          thingSelected(selectionTable.getSelectedItem());
        }
      });
      selectionTable.setDoubleClickListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          thingSelected(selectionTable.getSelectedItem());
          addThing();
        }
      });
      waresPanel.setLayout(new BorderLayout());
      waresPanel.add(selectionTable.getPanelWithTable(), BorderLayout.CENTER);
    }
    return waresPanel;
  }

  /**
   * This method initializes itemPriceField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getItemPriceField() {
    if (itemPriceField == null) {
      itemPriceField = new JTextField();
      itemPriceField.setBounds(new Rectangle(100, 110, 51, 21));
      itemPriceField.setEditable(false);
      itemPriceField.setHorizontalAlignment(JTextField.RIGHT);
    }
    return itemPriceField;
  }

  protected final boolean showSingularItems() {
    return getSingularBox().isSelected();
  }
  
  protected final JCheckBox getSingularBox() {
    if (singularBox == null) {
      singularBox = new JCheckBox("Einzelstücke");
      singularBox.setBounds(200, 0, 120, 20);
      singularBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectionTable.clear();
          mProvider.fillTable(showSingularItems());
        }
      });
    }
    return singularBox;
  }
  
  private JCheckBox singularBox;

}  //  @jve:decl-index=0:visual-constraint="10,10"

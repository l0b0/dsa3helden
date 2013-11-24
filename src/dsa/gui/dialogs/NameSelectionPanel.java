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
package dsa.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dsa.model.data.Names;
import dsa.model.data.RegionNames;

public class NameSelectionPanel extends JPanel {

  private JLabel jLabel = null;

  private JComboBox regionBox = null;

  private JLabel jLabel1 = null;

  private JComboBox sexBox = null;

  private JPanel jPanel = null;

  private JPanel jPanel1 = null;

  private JLabel firstNameLabel = null;

  private JComboBox firstNameCombo = null;

  private JLabel lastNameLabel = null;

  private JComboBox lastNameCombo = null;

  private JCheckBox nobleBox = null;

  private JLabel nobleLabel = null;

  private JLabel secondNameLabel = null;

  private JComboBox secondNameCombo = null;

  private JButton generateButton = null;

  private JLabel jLabel2 = null;

  private JTextField resultField = null;

  public NameSelectionPanel() {
    super();
    initialize();
    fillStaticData();
  }

  private void initialize() {
    setLayout(null);
    add(getJPanel(), null);
    add(getJPanel1(), null);
    setPreferredSize(new java.awt.Dimension(365, 240));
    setBounds(0, 0, 365, 240);
  }

  public void setCharacterName(String name) {
    resultField.setText(name);
  }

  public String getCharacterName() {
    return resultField.getText();
  }

  public void setDefaultRegion(String region) {
    for (int i = 0; i < regionBox.getItemCount(); ++i) {
      if (regionBox.getItemAt(i).equals(region)) {
        regionBox.setSelectedIndex(i);
      }
    }
  }

  public boolean isFemale() {
    return sexBox.getSelectedIndex() == 0;
  }

  public void setSex(boolean female) {
    sexBox.setSelectedIndex(female ? 0 : 1);
  }

  public String getNativeTongue() {
    return getNameGenerator().getNativeTongue();
  }

  private void fillStaticData() {
    sexBox.addItem("weiblich");
    sexBox.addItem("männlich");

    regionBox.addItem("Gareth");
    regionBox.addItem("Liebliches Feld");
    regionBox.addItem("Tulamiden");
    regionBox.addItem("Al'Anfa");
    regionBox.addItem("Albernia");
    regionBox.addItem("Almada");
    regionBox.addItem("Aranien");
    regionBox.addItem("Darpatien");
    regionBox.addItem("Weiden");
    regionBox.addItem("Bornland");
    regionBox.addItem("Maraskan");
    regionBox.addItem("Novadis");
    regionBox.addItem("Thorwaler");
    regionBox.addItem("Mohas");
    regionBox.addItem("Nivesen");
    regionBox.addItem("Elfen");
    regionBox.addItem("Zwerge");

    regionBox.setSelectedIndex(0);
    nameGenerator = getNameGenerator();
    listen = false;
    nameGenerator.initializeComponents();
    listen = true;
  }

  private AbstractNameGenerator nameGenerator = null;

  private final Random random = new Random(System.currentTimeMillis());

  abstract class AbstractNameGenerator {
    abstract void initializeComponents();

    abstract void sexChanged();

    abstract void generateName();

    abstract String getAggregateName();

    abstract String getNativeTongue();

    void firstComboChanged() {
    }

    void secondComboChanged() {
    }

    void thirdComboChanged() {
    }

    void nobleChanged() {
    }

    void fillComboBox(java.util.List<String> names, JComboBox box) {
      box.removeAllItems();
      for (String name : names)
        box.addItem(name);
      if (box.getItemCount() > 0) box.setSelectedIndex(0);
    }

    void selectRandom(JComboBox box) {
      box.setSelectedIndex(random.nextInt(box.getItemCount()));
    }
  }

  class DefaultNameGenerator extends AbstractNameGenerator {
    public DefaultNameGenerator(String region) {
      super();
      this.names = Names.getInstance().getRegionNames(region);
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(true);
      fillComboBox(names.getLastNames(), lastNameCombo);
      lastNameLabel.setText("Nachname:");
      firstNameLabel.setText("Vorname:");
      firstNameCombo.setEnabled(true);
      sexChanged();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
    }

    public String getAggregateName() {
      return firstNameCombo.getSelectedItem() + " "
          + lastNameCombo.getSelectedItem();
    }

    private RegionNames names;
  }

  class MohaNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(false);
      lastNameCombo.removeAllItems();
      lastNameLabel.setEnabled(false);
      lastNameLabel.setText("Nachname:");
      firstNameLabel.setText("Rufname:");
      firstNameCombo.setEnabled(true);
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
    }

    public String getAggregateName() {
      return firstNameCombo.getSelectedItem().toString();
    }

    private RegionNames names = Names.getInstance().getRegionNames("Mohas");
  }

  class ParentNameGenerator extends AbstractNameGenerator {
    public ParentNameGenerator(String region, boolean maleParent,
        String maleAttr, String femaleAttr) {
      super();
      this.names = Names.getInstance().getRegionNames(region);
      this.parentMale = maleParent;
      this.maleAttr = maleAttr;
      this.femaleAttr = femaleAttr;
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(true);
      if (parentMale) {
        lastNameLabel.setText("Name des Vaters:");
        fillComboBox(names.getMFirstNames(), lastNameCombo);
      }
      else {
        lastNameLabel.setText("Name der Mutter:");
        fillComboBox(names.getWFirstNames(), lastNameCombo);
      }
      firstNameLabel.setText("Vorname:");
      firstNameCombo.setEnabled(true);
      sexChanged();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
    }

    public String getAggregateName() {
      boolean female = sexBox.getSelectedIndex() == 0;
      return firstNameCombo.getSelectedItem()
          + (female ? femaleAttr : maleAttr) + lastNameCombo.getSelectedItem();
    }

    private RegionNames names;

    private String maleAttr;

    private String femaleAttr;

    private boolean parentMale;
  }

  class NivesenNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      lastNameCombo.setEnabled(true);
      lastNameCombo.removeAllItems();
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Stamm:");
      firstNameLabel.setText("Rufname:");
      firstNameCombo.setEnabled(true);
      for (String name : names.getMFirstNames()) {
        lastNameCombo.addItem(name);
      }
      for (String name : names.getWFirstNames()) {
        lastNameCombo.addItem(name);
      }
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
    }

    public String getAggregateName() {
      return firstNameCombo.getSelectedItem() + " aus "
          + lastNameCombo.getSelectedItem() + "s Stamm";
    }

    private RegionNames names = Names.getInstance().getRegionNames("Nivesen");
  }

  class ThorwalerNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(true);
      lastNameCombo.removeAllItems();
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Vater / Mutter:");
      firstNameLabel.setText("Rufname:");
      firstNameCombo.setEnabled(true);
      for (String name : names.getWFirstNames()) {
        lastNameCombo.addItem(name);
      }
      for (String name : names.getMFirstNames()) {
        lastNameCombo.addItem(name);
      }
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
    }

    public String getAggregateName() {
      boolean female = sexBox.getSelectedIndex() == 0;
      return firstNameCombo.getSelectedItem() + " "
          + lastNameCombo.getSelectedItem()
          + (female ? (random.nextBoolean() ? "dottir" : "dotter") : "son");
    }

    private RegionNames names = Names.getInstance().getRegionNames("Thorwal");
  }

  class NorthNameGenerator extends AbstractNameGenerator {
    public NorthNameGenerator(String region) {
      super();
      names = Names.getInstance().getRegionNames(region);
    }

    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.setEnabled(true);
      secondNameLabel.setEnabled(true);
      lastNameCombo.setEnabled(true);
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Nachname (1):");
      firstNameLabel.setText("Vorname:");
      secondNameLabel.setText("Nachname (2):");
      fillComboBox(names.getLastNames(), lastNameCombo);
      fillComboBox(names.getNobleLastNames(), secondNameCombo);
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
      selectRandom(secondNameCombo);
    }

    public String getAggregateName() {
      String result = firstNameCombo.getSelectedItem() + " ";
      String s = lastNameCombo.getSelectedItem().toString();
      result += s.substring(0, s.length() - 1);
      s = secondNameCombo.getSelectedItem().toString();
      result += s.substring(1);
      return result;
    }

    private RegionNames names;
  }

  class LFNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(true);
      nobleLabel.setEnabled(true);
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      lastNameCombo.setEnabled(true);
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Nachname:");
      firstNameLabel.setText("Vorname:");
      secondNameLabel.setText("Adelszusatz:");
      firstNameCombo.setEnabled(true);
      fillComboBox(names.getNobleLastNames(), secondNameCombo);
      fillComboBox(names.getLastNames(), lastNameCombo);
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
    }

    public void nobleChanged() {
      boolean noble = nobleBox.isSelected();
      secondNameCombo.setEnabled(noble);
      secondNameLabel.setEnabled(noble);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
      if (nobleBox.isSelected()) selectRandom(secondNameCombo);
    }

    public String getAggregateName() {
      String result = firstNameCombo.getSelectedItem() + " ";
      if (nobleBox.isSelected()) {
        result += secondNameCombo.getSelectedItem() + " ";
      }
      result += lastNameCombo.getSelectedItem();
      return result;
    }

    private RegionNames names = Names.getInstance().getRegionNames(
        "Liebliches Feld");
  }

  class AlmadaNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(true);
      nobleLabel.setEnabled(true);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(true);
      secondNameLabel.setEnabled(true);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(true);
      lastNameCombo.removeAllItems();
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Nachname:");
      firstNameLabel.setText("Vorname:");
      firstNameCombo.setEnabled(true);
      nobleChanged();
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          firstNameCombo);
      fillComboBox(female ? names.getWSecondNames() : names.getMSecondNames(),
          secondNameCombo);
    }

    public void nobleChanged() {
      boolean noble = nobleBox.isSelected();
      fillComboBox(noble ? names.getNobleLastNames() : names.getLastNames(),
          lastNameCombo);
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
      selectRandom(secondNameCombo);
    }

    public String getAggregateName() {
      return firstNameCombo.getSelectedItem() + " "
          + secondNameCombo.getSelectedItem() + " "
          + lastNameCombo.getSelectedItem();
    }

    private RegionNames names = Names.getInstance().getRegionNames("Almada");
  }

  class MaraskanNameGenerator extends AbstractNameGenerator {
    public void initializeComponents() {
      nobleBox.setSelected(false);
      nobleBox.setEnabled(false);
      nobleLabel.setEnabled(false);
      secondNameCombo.removeAllItems();
      secondNameCombo.setEnabled(false);
      secondNameLabel.setEnabled(false);
      secondNameLabel.setText("Beinamen:");
      lastNameCombo.setEnabled(true);
      lastNameCombo.removeAllItems();
      lastNameLabel.setEnabled(true);
      lastNameLabel.setText("Anhängsel:");
      firstNameLabel.setText("Originalname:");
      firstNameCombo.setEnabled(true);
      sexChanged();
    }

    public String getNativeTongue() {
      return names.getLanguage();
    }

    public void sexChanged() {
      boolean female = sexBox.getSelectedIndex() == 0;
      fillComboBox(female ? names.getWFirstNames() : names.getMFirstNames(),
          lastNameCombo);
      firstNameCombo.removeAllItems();
      if (female) {
        for (String name : names2.getWFirstNames()) {
          firstNameCombo.addItem(name);
        }
        for (String name : names3.getWFirstNames()) {
          firstNameCombo.addItem(name);
        }
      }
      else {
        for (String name : names2.getMFirstNames()) {
          firstNameCombo.addItem(name);
        }
        for (String name : names3.getMFirstNames()) {
          firstNameCombo.addItem(name);
        }
      }
    }

    public void generateName() {
      selectRandom(firstNameCombo);
      selectRandom(lastNameCombo);
    }

    public String getAggregateName() {
      return firstNameCombo.getSelectedItem().toString()
          + lastNameCombo.getSelectedItem();
    }

    private RegionNames names = Names.getInstance().getRegionNames("Maraskan");

    private RegionNames names2 = Names.getInstance().getRegionNames("Gareth");

    private RegionNames names3 = Names.getInstance().getRegionNames("Novadis");
  }

  boolean listen = false;

  private AbstractNameGenerator getNameGenerator() {
    String region = regionBox.getSelectedItem().toString();
    if (region.equals("Gareth")) {
      return new DefaultNameGenerator("Gareth");
    }
    else if (region.equals("Al'Anfa")) {
      return new DefaultNameGenerator("AlAnfa");
    }
    else if (region.equals("Bornland")) {
      return new DefaultNameGenerator("Bornland");
    }
    else if (region.equals("Elfen")) {
      return new DefaultNameGenerator("Elfen");
    }
    else if (region.equals("Albernia")) {
      return new DefaultNameGenerator("Albernia");
    }
    else if (region.equals("Darpatien")) {
      return new NorthNameGenerator("Darpatien");
    }
    else if (region.equals("Weiden")) {
      return new NorthNameGenerator("Weiden");
    }
    else if (region.equals("Liebliches Feld")) {
      return new LFNameGenerator();
    }
    else if (region.equals("Mohas")) {
      return new MohaNameGenerator();
    }
    else if (region.equals("Zwerge")) {
      return new ParentNameGenerator("Zwerge", true, ", Sohn des ",
          ", Tochter des ");
    }
    else if (region.equals("Novadis")) {
      return new ParentNameGenerator("Novadis", true, " ben ", " saba ");
    }
    else if (region.equals("Tulamiden")) {
      return new ParentNameGenerator("Novadis", true, " ben ", " saba ");
    }
    else if (region.equals("Aranien")) {
      return new ParentNameGenerator("Novadis", false, " ben ", " saba ");
    }
    else if (region.equals("Nivesen")) {
      return new NivesenNameGenerator();
    }
    else if (region.equals("Thorwaler")) {
      return new ThorwalerNameGenerator();
    }
    else if (region.equals("Almada")) {
      return new AlmadaNameGenerator();
    }
    else if (region.equals("Maraskan")) {
      return new MaraskanNameGenerator();
    }

    else
      return null;
  }

  private JPanel getJPanel() {
    if (jPanel == null) {
      jPanel = new JPanel();
      nobleLabel = new JLabel();
      nobleLabel.setBounds(new java.awt.Rectangle(303, 44, 44, 15));
      nobleLabel.setText("adelig");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 42, 107, 15));
      jLabel1.setText("Geschlecht:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 14, 110, 15));
      jLabel.setText("Namensregion:");
      jPanel.setLayout(null);
      jPanel.setBounds(new java.awt.Rectangle(8, 10, 357, 71));
      jPanel.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel.add(jLabel, null);
      jPanel.add(getRegionBox(), null);
      jPanel.add(jLabel1, null);
      jPanel.add(getSexBox(), null);
      jPanel.add(getNobleBox(), null);
      jPanel.add(nobleLabel, null);
    }
    return jPanel;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getRegionBox() {
    if (regionBox == null) {
      regionBox = new JComboBox();
      regionBox.setBounds(new java.awt.Rectangle(133, 12, 212, 19));
      regionBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          nameGenerator = getNameGenerator();
          listen = false;
          nameGenerator.initializeComponents();
          listen = true;
        }
      });
    }
    return regionBox;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getSexBox() {
    if (sexBox == null) {
      sexBox = new JComboBox();
      sexBox.setBounds(new java.awt.Rectangle(133, 40, 106, 19));
      sexBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (!listen) return;
          listen = false;
          nameGenerator.sexChanged();
          listen = true;
        }
      });
    }
    return sexBox;
  }

  /**
   * This method initializes jPanel1
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(9, 121, 114, 15));
      jLabel2.setText("Ergebnis:");
      secondNameLabel = new JLabel();
      secondNameLabel.setBounds(new java.awt.Rectangle(10, 66, 114, 15));
      secondNameLabel.setText("Beinamen:");
      lastNameLabel = new JLabel();
      lastNameLabel.setBounds(new java.awt.Rectangle(10, 37, 114, 15));
      lastNameLabel.setText("Nachnamen:");
      firstNameLabel = new JLabel();
      firstNameLabel.setBounds(new java.awt.Rectangle(10, 9, 114, 15));
      firstNameLabel.setText("Vornamen:");
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBounds(new java.awt.Rectangle(8, 88, 357, 152));
      jPanel1.setBorder(javax.swing.BorderFactory
          .createEtchedBorder(javax.swing.border.EtchedBorder.LOWERED));
      jPanel1.add(firstNameLabel, null);
      jPanel1.add(getFirstNameCombo(), null);
      jPanel1.add(lastNameLabel, null);
      jPanel1.add(getLastNameCombo(), null);
      jPanel1.add(secondNameLabel, null);
      jPanel1.add(getSecondNameCombo(), null);
      jPanel1.add(getGenerateButton(), null);
      jPanel1.add(jLabel2, null);
      jPanel1.add(getResultField(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getFirstNameCombo() {
    if (firstNameCombo == null) {
      firstNameCombo = new JComboBox();
      firstNameCombo.setBounds(new java.awt.Rectangle(133, 7, 212, 19));
      firstNameCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (!listen) return;
          nameGenerator.firstComboChanged();
          resultField.setText(nameGenerator.getAggregateName());
        }
      });
    }
    return firstNameCombo;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getLastNameCombo() {
    if (lastNameCombo == null) {
      lastNameCombo = new JComboBox();
      lastNameCombo.setBounds(new java.awt.Rectangle(133, 35, 212, 19));
      lastNameCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (!listen) return;
          nameGenerator.secondComboChanged();
          resultField.setText(nameGenerator.getAggregateName());
        }
      });
    }
    return lastNameCombo;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getNobleBox() {
    if (nobleBox == null) {
      nobleBox = new JCheckBox();
      nobleBox.setBounds(new java.awt.Rectangle(277, 40, 21, 21));
      nobleBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!listen) return;
          listen = false;
          nameGenerator.nobleChanged();
          listen = true;
        }
      });
    }
    return nobleBox;
  }

  /**
   * This method initializes jComboBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getSecondNameCombo() {
    if (secondNameCombo == null) {
      secondNameCombo = new JComboBox();
      secondNameCombo.setBounds(new java.awt.Rectangle(133, 64, 212, 19));
      secondNameCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (!listen) return;
          nameGenerator.thirdComboChanged();
          resultField.setText(nameGenerator.getAggregateName());
        }
      });
    }
    return secondNameCombo;
  }

  /**
   * This method initializes jButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getGenerateButton() {
    if (generateButton == null) {
      generateButton = new JButton();
      generateButton.setBounds(new java.awt.Rectangle(10, 91, 216, 19));
      generateButton.setText("Namen zufällig generieren");
      generateButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          nameGenerator.generateName();
          resultField.setText(nameGenerator.getAggregateName());
        }
      });
    }
    return generateButton;
  }

  /**
   * This method initializes jTextField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getResultField() {
    if (resultField == null) {
      resultField = new JTextField();
      resultField.setBounds(new java.awt.Rectangle(132, 119, 212, 19));
    }
    return resultField;
  }

}

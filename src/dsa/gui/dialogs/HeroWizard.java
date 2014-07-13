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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;

import dsa.control.Dice;
import dsa.control.HeroStepIncreaser;
import dsa.gui.lf.BGDialog;
import dsa.gui.util.PropertyLabel;
import dsa.model.DataFactory;
import dsa.model.Date;
import dsa.model.DiceSpecification;
import dsa.model.characters.Energy;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;
import dsa.model.characters.Property;
import dsa.model.data.CharacterType;
import dsa.model.data.CharacterTypes;
import dsa.model.data.Looks;
import dsa.model.data.Region;
import dsa.model.data.Regions;

public final class HeroWizard extends BGDialog {

  public HeroWizard(Frame owner) {
    super(owner, "Helden erstellen", true);
    initialize();
    this.setLocationRelativeTo(owner);
  }
  
  public String getHelpPage() {
    return "Heldenerstellung";
  }

  private CardLayout cards;

  private JButton nextButton;

  private JButton previousButton;

  private JButton cancelButton;

  private Hero result = null;

  private int pageNr = 0;

  private boolean female;

  private CharacterType characterType;

  private String regMod = "Keine";

  private int[] properties = new int[Property.values().length];

  private String name;

  private class FirstPage extends JPanel {
    private final JTree typesTree;

    private final JComboBox sexCombo;

    private final JScrollPane scrollPane;

    private final JComboBox regCombo;

    private boolean listen = true;

    private FirstPage() {
      super(new BorderLayout());
      typesTree = new JTree(CharacterTypes.getInstance().getAllTypes(true));
      // typesTree.setPreferredSize(new java.awt.Dimension(400, 400));
      typesTree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          treeSelectionChanged();
        }
      });
      scrollPane = new JScrollPane(typesTree);
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      this.add(scrollPane, BorderLayout.CENTER);
      scrollPane.setPreferredSize(new java.awt.Dimension(400, 360));
      JLabel lower = new JLabel("");
      lower.setBounds(0, 0, 5, 5);
      JPanel lowerPanel = new JPanel(new BorderLayout());
      lowerPanel.add(lower, BorderLayout.SOUTH);
      JPanel innerLower = new JPanel(null);
      JLabel regLabel = new JLabel("Regionalmodifikation:");
      regLabel.setBounds(13, 5, 200, 15);
      regCombo = new JComboBox();
      regCombo.setBounds(160, 5, 160, 20);
      regCombo.addItem("Keine");
      for (String region : Regions.getInstance().getRegions()) {
        regCombo.addItem(region);
      }
      regCombo.setSelectedIndex(0);
      regCombo.setEnabled(false);
      regCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          regMod = regCombo.getSelectedItem().toString();
        }
      });
      innerLower.add(regLabel, null);
      innerLower.add(regCombo, null);
      innerLower.setPreferredSize(new java.awt.Dimension(380, 30));
      lowerPanel.add(innerLower, BorderLayout.CENTER);
      this.add(lowerPanel, BorderLayout.SOUTH);
      JLabel left = new JLabel("");
      left.setBounds(0, 0, 5, 5);
      this.add(left, BorderLayout.WEST);
      JLabel right = new JLabel("");
      right.setBounds(0, 0, 5, 5);
      this.add(right, BorderLayout.EAST);
      JPanel upper = new JPanel(new BorderLayout());
      JPanel inner = new JPanel(new FlowLayout());
      JLabel descr = new JLabel("Geschlecht: ");
      inner.add(descr);
      sexCombo = new JComboBox();
      sexCombo.addItem("weiblich");
      sexCombo.addItem("männlich");
      sexCombo.setSelectedIndex(0);
      sexCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          sexChanged();
        }
      });
      inner.add(sexCombo);
      upper.add(inner, BorderLayout.NORTH);
      this.add(upper, BorderLayout.NORTH);
      female = true;
      characterType = null;
    }

    private void sexChanged() {
      listen = false;
      female = sexCombo.getSelectedIndex() == 0;
      typesTree.setModel(CharacterTypes.getInstance().getAllTypes(female));
      characterType = null;
      nextButton.setEnabled(false);
      listen = true;
      regCombo.setSelectedIndex(0);
      regMod = "Keine";
      regCombo.setEnabled(false);
      // typesTree.invalidate();
    }

    private void treeSelectionChanged() {
      if (!listen) return;
      regCombo.setSelectedIndex(0);
      regMod = "Keine";
      if (typesTree.isSelectionEmpty()
          || (typesTree.getSelectionPath() == null)) {
        nextButton.setEnabled(false);
        characterType = null;
        return;
      }
      TreeNode node = ((TreeNode) typesTree.getSelectionPath()
          .getLastPathComponent());
      if (node.isLeaf()) {
        characterType = CharacterTypes.getInstance().getType(node);
        nextButton.setEnabled(true);
        regCombo.setEnabled(characterType.isRegionModifiable());
      }
      else {
        nextButton.setEnabled(false);
        characterType = null;
        regCombo.setEnabled(false);
      }
    }
  }

  private FirstPage page0 = null;

  private FirstPage getFirstPage() {
    if (page0 == null) {
      page0 = new FirstPage();
    }
    return page0;
  }

  private class SecondPage extends JPanel {
    private JLabel reqLabel = null;

    private JLabel jLabel = null;

    private JLabel jLabel1 = null;

    private JLabel jLabel2 = null;

    private JLabel jLabel3 = null;

    private JLabel jLabel4 = null;

    private JLabel jLabel5 = null;

    private JLabel jLabel6 = null;

    private PropertyLabel muLabel = null;

    private PropertyLabel klLabel = null;

    private PropertyLabel inLabel = null;

    private PropertyLabel chLabel = null;

    private PropertyLabel ffLabel = null;

    private PropertyLabel geLabel = null;

    private PropertyLabel kkLabel = null;

    private JLabel jLabel7 = null;

    private JLabel jLabel8 = null;

    private JLabel jLabel9 = null;

    private JLabel jLabel10 = null;

    private JLabel jLabel11 = null;

    private JLabel jLabel12 = null;

    private JLabel jLabel13 = null;

    private PropertyLabel agLabel = null;

    private PropertyLabel haLabel = null;

    private PropertyLabel raLabel = null;

    private PropertyLabel taLabel = null;

    private PropertyLabel ngLabel = null;

    private PropertyLabel ggLabel = null;

    private PropertyLabel jzLabel = null;

    private JLabel reqTitleLabel = null;

    private JLabel reqLabel2 = null;

    private JLabel jLabel15 = null;

    private JLabel badNecessaryLabel = null;

    private JLabel goodNecessaryLabel = null;

    private JButton diceButton = null;

    private JButton removeIncrButton = null;

    private SecondPage() {
      super();
      initialize();
    }

    private void initialize() {
      this.setPreferredSize(new java.awt.Dimension(422, 320));
      badNecessaryLabel = new JLabel();
      badNecessaryLabel.setBounds(new java.awt.Rectangle(13, 375, 379, 15));
      badNecessaryLabel
          .setText("Es müssen noch 13 schlechte Eigenschaften erhöht werden!");
      badNecessaryLabel.setForeground(Color.RED);
      goodNecessaryLabel = new JLabel();
      goodNecessaryLabel.setBounds(new Rectangle(13, 395, 379, 15));
      goodNecessaryLabel.setForeground(Color.RED);
      jLabel15 = new JLabel();
      jLabel15.setBounds(new java.awt.Rectangle(13, 298, 371, 15));
      jLabel15
          .setText("Doppelklick links: erhöhen; Doppelklick rechts: erniedrigen");
      JLabel label16 = new JLabel("Drag & Drop: vertauschen");
      label16.setBounds(new java.awt.Rectangle(13, 318, 371, 15));
      reqTitleLabel = new JLabel();
      reqTitleLabel.setBounds(new java.awt.Rectangle(13, 226, 205, 15));
      reqTitleLabel.setText("Voraussetzungen:");
      JLabel title = new JLabel("Eigenschaften auswürfeln / einstellen:");
      title.setBounds(13, 10, 300, 15);
      jzLabel = new PropertyLabel(6, false);
      jzLabel.setBounds(new java.awt.Rectangle(310, 195, 38, 15));
      ggLabel = new PropertyLabel(6, false);
      ggLabel.setBounds(new java.awt.Rectangle(310, 170, 38, 15));
      ngLabel = new PropertyLabel(6, false);
      ngLabel.setBounds(new java.awt.Rectangle(310, 145, 38, 15));
      taLabel = new PropertyLabel(6, false);
      taLabel.setBounds(new java.awt.Rectangle(310, 120, 38, 15));
      raLabel = new PropertyLabel(6, false);
      raLabel.setBounds(new java.awt.Rectangle(310, 95, 38, 15));
      haLabel = new PropertyLabel(6, false);
      haLabel.setBounds(new java.awt.Rectangle(310, 70, 38, 15));
      agLabel = new PropertyLabel(6, false);
      agLabel.setBounds(new java.awt.Rectangle(310, 45, 38, 15));
      jLabel13 = new JLabel();
      jLabel13.setBounds(new java.awt.Rectangle(217, 195, 86, 15));
      jLabel13.setText("JähZorn:");
      jLabel12 = new JLabel();
      jLabel12.setBounds(new java.awt.Rectangle(217, 170, 86, 15));
      jLabel12.setText("GoldGier:");
      jLabel11 = new JLabel();
      jLabel11.setBounds(new java.awt.Rectangle(217, 145, 86, 15));
      jLabel11.setText("NeuGier:");
      jLabel10 = new JLabel();
      jLabel10.setBounds(new java.awt.Rectangle(217, 120, 86, 15));
      jLabel10.setText("TotenAngst:");
      jLabel9 = new JLabel();
      jLabel9.setBounds(new java.awt.Rectangle(217, 95, 86, 15));
      jLabel9.setText("RaumAngst:");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new java.awt.Rectangle(217, 70, 86, 15));
      jLabel8.setText("HöhenAngst:");
      jLabel7 = new JLabel();
      jLabel7.setBounds(new java.awt.Rectangle(217, 45, 86, 15));
      jLabel7.setText("AberGlaube:");
      kkLabel = new PropertyLabel(8, true);
      kkLabel.setBounds(new java.awt.Rectangle(125, 195, 38, 15));
      geLabel = new PropertyLabel(8, true);
      geLabel.setBounds(new java.awt.Rectangle(125, 170, 38, 15));
      ffLabel = new PropertyLabel(8, true);
      ffLabel.setBounds(new java.awt.Rectangle(125, 145, 38, 15));
      chLabel = new PropertyLabel(8, true);
      chLabel.setBounds(new java.awt.Rectangle(125, 120, 38, 15));
      inLabel = new PropertyLabel(8, true);
      inLabel.setBounds(new java.awt.Rectangle(125, 95, 38, 15));
      klLabel = new PropertyLabel(8, true);
      klLabel.setBounds(new java.awt.Rectangle(125, 70, 38, 15));
      muLabel = new PropertyLabel(8, true);
      muLabel.setBounds(new java.awt.Rectangle(125, 45, 38, 15));
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(13, 195, 105, 15));
      jLabel6.setText("KörperKraft:");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(13, 170, 105, 15));
      jLabel5.setText("GEwandtheit:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(13, 145, 105, 15));
      jLabel4.setText("FingerFertigkeit:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(13, 120, 105, 15));
      jLabel3.setText("CHarisma:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(13, 95, 105, 15));
      jLabel2.setText("INtuition:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(13, 70, 105, 15));
      jLabel1.setText("KLugheit:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(13, 45, 105, 15));
      jLabel.setText("MUt:");
      reqLabel = new JLabel();
      reqLabel.setBounds(new java.awt.Rectangle(13, 246, 376, 15));
      reqLabel.setText("KL>=13, CH>=13, IN>=12, JZ<=4, GG<=3, NG>=7");
      reqLabel2 = new JLabel();
      reqLabel2.setBounds(13, 266, 376, 15);
      this.setLayout(null);
      this.add(reqLabel, null);
      this.add(jLabel, null);
      this.add(jLabel1, null);
      this.add(jLabel2, null);
      this.add(jLabel3, null);
      this.add(jLabel4, null);
      this.add(jLabel5, null);
      this.add(jLabel6, null);
      this.add(muLabel, null);
      this.add(klLabel, null);
      this.add(inLabel, null);
      this.add(chLabel, null);
      this.add(ffLabel, null);
      this.add(geLabel, null);
      this.add(kkLabel, null);
      this.add(jLabel7, null);
      this.add(jLabel8, null);
      this.add(jLabel9, null);
      this.add(jLabel10, null);
      this.add(jLabel11, null);
      this.add(jLabel12, null);
      this.add(jLabel13, null);
      this.add(agLabel, null);
      this.add(haLabel, null);
      this.add(raLabel, null);
      this.add(taLabel, null);
      this.add(ngLabel, null);
      this.add(ggLabel, null);
      this.add(jzLabel, null);
      this.add(reqTitleLabel, null);
      this.add(jLabel15, null);
      this.add(badNecessaryLabel, null);
      this.add(getDiceButton(), null);
      this.add(getRemoveIncrButton(), null);
      add(goodNecessaryLabel, null);
      add(label16, null);
      add(title);
      add(reqLabel2);
      PropertyLabel.setNecessaryIncreaseListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int ni = PropertyLabel.getNecessaryBadIncreases();
          badNecessaryLabel.setText(ni > 0 ? "Es müssen noch " + ni
              + " schlechte Eigenschaften erhöht werden!" : "");
          float ng = PropertyLabel.getNecessaryGoodDecreases();
          ni = (int) Math.ceil(ng);
          goodNecessaryLabel.setText(ni > 0 ? "Es müssen noch " + ni
              + " gute Eigenschaften gesenkt werden!" : "");
        }
      });
    }

    private JButton getDiceButton() {
      if (diceButton == null) {
        diceButton = new JButton();
        diceButton.setBounds(new java.awt.Rectangle(13, 348, 140, 20));
        diceButton.setText("Neu würfeln");
        diceButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            randomizeProperties();
          }
        });
      }
      return diceButton;
    }

    private JButton getRemoveIncrButton() {
      if (removeIncrButton == null) {
        removeIncrButton = new JButton();
        removeIncrButton.setBounds(new java.awt.Rectangle(167, 348, 223, 20));
        removeIncrButton.setText("Steigerungen zurücknehmen");
        removeIncrButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            removeIncreases();
          }
        });
      }
      return removeIncrButton;
    }

    private int[] origProperties = new int[Property.values().length];

    private void randomizeProperties() {
      ArrayList<Integer> dices = new ArrayList<Integer>();
      for (int i = 0; i < 8; ++i) {
        dices.add(Dice.roll(6) + 7);
      }
      java.util.Collections.sort(dices);
      ArrayList<Integer> temp = new ArrayList<Integer>();
      for (int i = 0; i < 7; ++i) {
        temp.add(dices.get(i + 1));
      }
      java.util.Collections.shuffle(temp);
      for (int i = 0; i < 7; ++i) {
        properties[i] = temp.get(i);
        origProperties[i] = temp.get(i);
      }
      dices.clear();
      for (int i = 0; i < 7; ++i) {
        dices.add(Dice.roll(6) + 1);
      }
      java.util.Collections.shuffle(dices);
      for (int i = 0; i < 7; ++i) {
        properties[i + 7] = dices.get(i);
        origProperties[i + 7] = dices.get(i);
      }
      PropertyLabel.resetNecessaryBadIncreases();
      setLabels();
    }

    public void reInitialize() {
      String text = "";
      String temp = "";
      int count = 0;
      for (int i = 0; i < properties.length; ++i) {
        int req = characterType.getRequirements()[i];
        if (req != 0) {
          if (count != 0) temp += ", ";
          temp += Property.values()[i].toString();
          temp += (req > 0) ? ">=" : "<=";
          temp += Math.abs(req);
          ++count;
          if (count == 4 && text.equals("")) {
            text = temp;
            temp = "";
            count = 0;
          }
        }
      }
      if (text.equals("")) text = temp;
      reqLabel.setText(text);
      reqLabel2.setText(text.equals(temp) ? "" : temp);
      reqTitleLabel.setText(text.equals("") ? "Voraussetzungen: keine"
          : "Voraussetzungen:");
      muLabel.setConstraint(characterType.getRequirements()[0]);
      klLabel.setConstraint(characterType.getRequirements()[1]);
      inLabel.setConstraint(characterType.getRequirements()[2]);
      chLabel.setConstraint(characterType.getRequirements()[3]);
      ffLabel.setConstraint(characterType.getRequirements()[4]);
      geLabel.setConstraint(characterType.getRequirements()[5]);
      kkLabel.setConstraint(characterType.getRequirements()[6]);
      haLabel.setConstraint(characterType.getRequirements()[7]);
      taLabel.setConstraint(characterType.getRequirements()[8]);
      raLabel.setConstraint(characterType.getRequirements()[9]);
      agLabel.setConstraint(characterType.getRequirements()[10]);
      ngLabel.setConstraint(characterType.getRequirements()[11]);
      ggLabel.setConstraint(characterType.getRequirements()[12]);
      jzLabel.setConstraint(characterType.getRequirements()[13]);
      randomizeProperties();
    }

    private void removeIncreases() {
      System.arraycopy(origProperties, 0, properties, 0, origProperties.length);
      PropertyLabel.resetNecessaryBadIncreases();
      setLabels();
    }

    private void setLabels() {
      muLabel.setValue(properties[0]);
      klLabel.setValue(properties[1]);
      inLabel.setValue(properties[2]);
      chLabel.setValue(properties[3]);
      ffLabel.setValue(properties[4]);
      geLabel.setValue(properties[5]);
      kkLabel.setValue(properties[6]);
      haLabel.setValue(properties[7]);
      taLabel.setValue(properties[8]);
      raLabel.setValue(properties[9]);
      agLabel.setValue(properties[10]);
      ngLabel.setValue(properties[11]);
      ggLabel.setValue(properties[12]);
      jzLabel.setValue(properties[13]);
    }

    public void getData() {
      properties[0] = muLabel.getValue();
      properties[1] = klLabel.getValue();
      properties[2] = inLabel.getValue();
      properties[3] = chLabel.getValue();
      properties[4] = ffLabel.getValue();
      properties[5] = geLabel.getValue();
      properties[6] = kkLabel.getValue();
      properties[7] = haLabel.getValue();
      properties[8] = taLabel.getValue();
      properties[9] = raLabel.getValue();
      properties[10] = agLabel.getValue();
      properties[11] = ngLabel.getValue();
      properties[12] = ggLabel.getValue();
      properties[13] = jzLabel.getValue();

    }

  }

  private SecondPage secondPage = null;

  private SecondPage getSecondPage() {
    if (secondPage == null) {
      secondPage = new SecondPage();
    }
    return secondPage;
  }

  public Hero getCreatedHero() {
    return result;
  }

  public void initialize() {
    JPanel contentPane = new JPanel(new BorderLayout(10, 10));
    JPanel bottomPane = new JPanel();
    bottomPane.setLayout(null);
    cancelButton = new JButton("Abbrechen");
    cancelButton.setBounds(13, 5, 120, 20);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    previousButton = new JButton("<< Zurück");
    previousButton.setBounds(143, 5, 120, 20);
    previousButton.setEnabled(false);
    previousButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        previousPage();
      }
    });
    nextButton = new JButton("Weiter >>");
    nextButton.setBounds(273, 5, 120, 20);
    nextButton.setEnabled(false);
    nextButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextPage();
      }
    });
    bottomPane.add(cancelButton, null);
    bottomPane.add(previousButton, null);
    bottomPane.add(nextButton, null);
    bottomPane.setPreferredSize(new java.awt.Dimension(390, 35));

    cards = new CardLayout();
    centerPane = new JPanel(cards);
    centerPane.add(getFirstPage(), "0");
    centerPane.add(getSecondPage(), "1");
    centerPane.add(getThirdPage(), "2");
    contentPane.add(centerPane, BorderLayout.CENTER);
    contentPane.add(bottomPane, BorderLayout.SOUTH);

    this.setContentPane(contentPane);
    this.getRootPane().setDefaultButton(nextButton);
    setEscapeButton(cancelButton);
    this.pack();
  }

  private JPanel centerPane;

  private boolean checkConstraints() {
    if (PropertyLabel.getNecessaryBadIncreases() > 0
        || PropertyLabel.getNecessaryGoodDecreases() > 0) {
      JOptionPane.showMessageDialog(this,
          "Die Werte müssen noch angepasst werden!", "Heldenerstellung",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }
    for (int i = 0; i < Property.values().length; ++i) {
      int c = characterType.getRequirements()[i];
      if ((c > 0 && properties[i] < c) || (c < 0 && properties[i] > -c)) {
        JOptionPane.showMessageDialog(this,
            "Die Werte erfüllen noch nicht die Voraussetzungen!",
            "Heldenerstellung", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true;
  }

  private String language;

  private class ThirdPage extends JPanel {
    private final dsa.gui.dialogs.NameSelectionPanel panel;
    
    private JCheckBox autoStepIncreaseBox;
    private JCheckBox autoStepShowLog;
    private JSpinner  autoStepSpinner;
    private JCheckBox dilletantBox;

    public ThirdPage() {
      super();
      setLayout(null);
      panel = new dsa.gui.dialogs.NameSelectionPanel();
      panel.setLocation(5, 5);
      add(panel, null);
      JPanel autoStepPanel = new JPanel();
      autoStepPanel.setLayout(null);
      autoStepPanel.setBorder(BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Stufe festlegen"));
      autoStepIncreaseBox = new JCheckBox("Automatisch auf höhere Stufe steigern", false);
      autoStepIncreaseBox.setBounds(8, 20, 300, 20);
      autoStepPanel.add(autoStepIncreaseBox, null);
      JLabel autoStepLabel = new JLabel("Stufe: ");
      autoStepLabel.setBounds(28, 50, 80, 20);
      autoStepPanel.add(autoStepLabel, null);
      autoStepSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
      autoStepSpinner.setEnabled(false);
      autoStepSpinner.setBounds(68, 50, 50, 20);
      autoStepPanel.add(autoStepSpinner, null);
      autoStepPanel.setPreferredSize(new java.awt.Dimension(300, 100));
      autoStepPanel.setBounds(12, 250, 360, 90);
      autoStepIncreaseBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          autoStepSpinner.setEnabled(autoStepIncreaseBox.isSelected());
          autoStepShowLog.setEnabled(autoStepIncreaseBox.isSelected());
        }
      });
      autoStepShowLog = new JCheckBox("Nachher Log anzeigen");
      autoStepShowLog.setBounds(130, 50, 220, 20);
      autoStepShowLog.setSelected(false);
      autoStepShowLog.setEnabled(false);
      autoStepPanel.add(autoStepShowLog, null);
      add(autoStepPanel, null);
      JPanel dilletantPanel = new JPanel();
      dilletantPanel.setLayout(null);
      dilletantPanel.setBorder(BorderFactory.createTitledBorder(
          BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Magiebegabung"));
      dilletantBox = new JCheckBox("Magiedilletant", false);
      dilletantBox.setBounds(8, 20, 300, 20);
      dilletantBox.setToolTipText("Voraussetzungen: KL 10, IN 13, AG 6, spez. Heldentyp");
      dilletantPanel.add(dilletantBox, null);
      dilletantPanel.setPreferredSize(new java.awt.Dimension(300, 55));
      dilletantPanel.setBounds(12, 345, 360, 55);
      add(dilletantPanel, null);
    }

    public void reInitialize() {
      panel.setName(name != null ? name : "");
      panel.setSex(female);
      panel.setDefaultRegion(characterType.getDefaultNameRegion());
      boolean dt = characterType.canBeMagicDilletant();
      if (properties[Property.KL.ordinal()] < 10) dt = false;
      if (properties[Property.IN.ordinal()] < 13) dt = false;
      if (properties[Property.AG.ordinal()] < 6) dt = false;
      dilletantBox.setEnabled(dt);
      if (!dt) dilletantBox.setSelected(false);
    }

    public String getCharacterName() {
      return panel.getCharacterName();
    }

    public boolean isFemale() {
      return panel.isFemale();
    }

    public String getLanguage() {
      return panel.getNativeTongue();
    }
    
    public int increaseToStep() {
      if (autoStepIncreaseBox.isSelected()) {
        return ((Number)autoStepSpinner.getValue()).intValue();
      }
      else return 0;
    }
    
    public boolean showLog() {
      return autoStepShowLog.isSelected();
    }
    
    public boolean isMagicDilletant() {
      return dilletantBox.isSelected();
    }
  }

  private ThirdPage thirdPage;

  private ThirdPage getThirdPage() {
    if (thirdPage == null) {
      thirdPage = new ThirdPage();
    }
    return thirdPage;
  }

  private boolean retrieveNameProperties() {
    String temp = getThirdPage().getCharacterName();
    if (temp != null && !temp.equals("")) {
      name = temp;
      language = getThirdPage().getLanguage();
      female = getThirdPage().isFemale();
      return true;
    }
    else {
      JOptionPane.showMessageDialog(this, "Der Held muss einen Namen haben!",
          "Heldenerstellung", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void setInitialMoney(Hero hero) {
    String stand = hero.getStand().toLowerCase(java.util.Locale.GERMAN);
    DiceSpecification money = null;
    int currency = 0;
    if (stand.contains("unfrei")) {
      money = DiceSpecification.parse("1W6");
      currency = 1;
    }
    else if (stand.contains("arm")) {
      money = DiceSpecification.parse("1W6");
    }
    else if (stand.contains("mittelst")) {
      money = DiceSpecification.parse("1W20");
    }
    else if (stand.contains("reich")) {
      money = DiceSpecification.parse("2W20+20");
    }
    else if (stand.contains("adel")) {
      money = DiceSpecification.parse("3W20");
    }
    if (money != null) {
      hero.setMoney(currency, money.calcValue(), false);
    }
  }

  private void createHero() {
    Hero prototype = null;
    try {
      prototype = characterType.getPrototype();
    }
    catch (java.io.IOException e) {
      JOptionPane.showMessageDialog(this,
          "Fehler beim Laden der Heldenvorlage:\n" + e.getMessage(),
          "Heldenerstellung", JOptionPane.ERROR_MESSAGE);
      return;
    }
    Hero hero = DataFactory.getInstance().createHero(prototype);
    hero.setSex(female ? "w" : "m");
    hero.setName(name);
    for (int i = 0; i < properties.length; ++i) {
      hero.setProperty(i, properties[i]);
    }
    if (!regMod.equals("Keine")) {
      Region region = Regions.getInstance().getRegion(regMod);
      if (region != null) {
        ArrayList<String> talents = region.getModifiedTalents();
        ArrayList<Integer> mods = region.getModifications();
        for (int i = 0; i < talents.size(); ++i) {
          hero.changeTalentValue(talents.get(i), mods.get(i));
        }
      }
    }
    DiceSpecification ds = characterType.getHeight();
    int height = ds.calcValue();
    hero.setHeight("" + height);
    hero.setWeight("" + (height - characterType.getWeightLoss()));
    hero.setHairColor(characterType.getHairColor(Dice.roll(20) - 1));
    String stand = "arm";
    for (int i = 0; i < 20; ++i) {
      String test = characterType.getOrigin(Dice.roll(20) - 1);
      if (!test.contains("adlig") && !test.contains("wohlhabend") && !test.contains("adel")) {
        stand = test;
        break;
      }
    }
    hero.setStand(stand);
    hero.setEyeColor(Looks.getMatchingEyeColor(hero.getHairColor()));
    hero.setNativeTongue(language);
    int day = Dice.roll(30);
    int month = Dice.roll(20);
    switch (month) {
    case 1:
    case 2:
      month = 0;
      break;
    case 3:
    case 4:
      month = 1;
      break;
    case 5:
    case 6:
      month = 2;
      break;
    case 7:
    case 8:
      month = 3;
      break;
    case 9:
    case 10:
      month = 4;
      break;
    case 11:
      month = 5;
      break;
    case 12:
      month = 6;
      break;
    case 13:
      month = 7;
      break;
    case 14:
    case 15:
    case 16:
      month = 8;
      break;
    case 17:
      month = 9;
      break;
    case 18:
      month = 10;
      break;
    default:
      month = 11;
      break;
    }
    Date currentDate = Group.getInstance().getDate();
    int newYear = currentDate.getYear() - hero.getAge();
    Date.Era currentEra = currentDate.getEra();
    if (newYear < 0 && currentEra == Date.Era.nach) {
      newYear = -newYear;
      currentEra = Date.Era.vor;
    }
    else if (newYear < 0 && currentEra == Date.Era.vor) {
      newYear = -newYear;
      currentEra = Date.Era.nach;
    }
    hero.setBirthday(new Date(day, Date.Month.values()[month], newYear, currentEra, currentDate.getEvent()));
    String msg = name + " hat im " + Date.Month.values()[month] + " Geburtstag.\n";
    switch (month) {
    case 0:
      msg += "Etwas des göttlichen Mutes färbt auch auf "
          + (female ? "sie" : "ihn") + " ab!";
      hero.changeDefaultProperty(Property.MU, 1);
      break;
    case 1:
      msg += (female ? "Sie" : "Er") + " kann besser mit Schwertern kämpfen!";
      hero.changeTalentValue("Schwerter", 1);
      break;
    case 2:
      msg += (female ? "Sie" : "Er")
          + " kann besser schwimmen und Boote steuern!";
      hero.changeTalentValue("Schwimmen", 1);
      hero.changeTalentValue("Boote Fahren", 1);
      break;
    case 3:
      msg += (female ? "Sie" : "Er")
          + " versteht etwas von Seelenheilung und Kochen!";
      hero.changeTalentValue("Heilkunde, Seele", 1);
      hero.changeTalentValue("Kochen", 1);
      break;
    case 4:
      msg += (female ? "Sie" : "Er") + " durchschaut die Menschen leichter!";
      hero.changeTalentValue("Menschenkenntnis", 1);
      break;
    case 5:
      msg += (female ? "Sie" : "Er") + " kommt gut mit Alchimie zurecht!";
      hero.changeTalentValue("Alchimie", 1);
      break;
    case 6:
      msg += (female ? "Sie" : "Er")
          + " findet die Fährten des Wildes und trifft auch besser!";
      hero.changeTalentValue("Fährtensuchen", 1);
      hero.changeTalentValue("Schußwaffen", 1);
      break;
    case 7:
      msg += (female ? "Sie" : "Er") + " ist ein" + (female ? "e" : "")
          + " besonders schöne" + (female ? " Heldin!" : "r Held!");
      hero.changeDefaultProperty(Property.CH, 1);
      break;
    case 8:
      msg += (female ? "Sie" : "Er") + " ist besonders lautlos und ein"
          + (female ? "e bessere Diebin!" : " besserer Dieb!");
      hero.changeTalentValue("Schleichen", 1);
      hero.changeTalentValue("Taschendiebstahl", 1);
      break;
    case 9:
      msg += "Mit Krankheiten und Wunden kennt " + (female ? "sie" : "er")
          + " sich besser aus!";
      hero.changeTalentValue("Heilkunde, Wunden", 1);
      hero.changeTalentValue("Heilkunde, Krankh.", 1);
      break;
    case 10:
      msg += (female ? "Sie" : "Er") + " kann besser mit Äxten kämpfen!";
      hero.changeTalentValue("Äxte und Beile", 1);
      break;
    case 11:
      msg += "Betörung, Musik und Tanz liegen " + (female ? "ihr" : "ihm")
          + " im Blut!";
      hero.changeTalentValue("Betören", 1);
      hero.changeTalentValue("Musizieren", 1);
      hero.changeTalentValue("Tanzen", 1);
      break;
    default:
      assert(false);
    }
    JOptionPane.showMessageDialog(this, msg, "Göttergeschenk",
        JOptionPane.INFORMATION_MESSAGE);
    hero.setBGEditor("");
    hero.setBGFile("");
    hero.setPrintFile("");
    hero.setPrintingTemplateFile("");
    hero.setPicture("");
    setInitialMoney(hero);
    int talentReducement = characterType.getTalentReducement();
    if (getThirdPage().isMagicDilletant()) {
      talentReducement = 30;
      hero.setIsMagicDilettant(true);
      hero.setHasEnergy(Energy.AE, true);
      hero.setDefaultEnergy(Energy.AE, Dice.roll(6) + 3);
      hero.addRitual("Magisches Meisterhandwerk");
      hero.addRitual("Schutzgeist");
    }
    hero.toStepOne(talentReducement);
    int stepToIncrease = getThirdPage().increaseToStep();
    if (stepToIncrease > 1) {
      int apToGive = stepToIncrease * (stepToIncrease - 1) * 50;
      hero.changeAP(apToGive);
    }
    if (stepToIncrease > 0) {
      HeroStepIncreaser increaser = new HeroStepIncreaser(hero);
      increaser.increaseStepsAutomatically(stepToIncrease);
      if (getThirdPage().showLog()) {
        ScrollableMessageDialog dialog = new ScrollableMessageDialog(this, 
            increaser.getLog(), "Automatisch_Steigern");
        dialog.setVisible(true);
      }
    }
    this.result = hero;
  }

  protected void nextPage() {
    if (pageNr == 0) {
      cards.next(centerPane);
      getSecondPage().reInitialize();
      previousButton.setEnabled(true);
      pageNr = 1;
    }
    else if (pageNr == 1) {
      getSecondPage().getData();
      if (!checkConstraints()) return;
      nextButton.setText("Fertig!");
      cards.next(centerPane);
      getThirdPage().reInitialize();
      pageNr = 2;
    }
    else if (pageNr == 2) {
      if (!retrieveNameProperties()) return;
      createHero();
      dispose();
    }
  }

  protected void previousPage() {
    if (pageNr == 1) {
      cards.previous(centerPane);
      previousButton.setEnabled(false);
      pageNr = 0;
    }
    else if (pageNr == 2) {
      cards.previous(centerPane);
      nextButton.setText("Weiter >>");
      pageNr = 1;
    }
  }

}

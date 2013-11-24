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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.gui.dialogs.CitySelectionDialog;
import dsa.gui.dialogs.DateSelectionDialog;
import dsa.gui.dialogs.NameSelectionDialog;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.CharacterType;
import dsa.model.data.CharacterTypes;
import dsa.model.data.Looks;

/**
 * 
 */
public class PhysFrame extends SubFrame implements CharactersObserver {

  private javax.swing.JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JLabel jLabel5 = null;

  private JTextField nameField = null;

  private JLabel jLabel6 = null;

  private JLabel jLabel7 = null;

  private JComboBox sexCombo = null;

  private JTextField typeField = null;

  private JComboBox godBox = null;

  private JComboBox standCombo = null;

  private JTextField birthPlaceField = null;

  private JButton nameButton = null;

  private JButton birthPlaceButton = null;

  private JLabel jLabel8 = null;

  private JLabel jLabel9 = null;

  private JLabel jLabel10 = null;

  private JLabel jLabel11 = null;

  private JTextField birthDayField = null;

  private JSpinner ageSpinner = null;

  private SpinnerNumberModel spinnerNumberModel = null; // @jve:decl-index=0:

  private JSpinner heightSpinner = null;

  private SpinnerNumberModel spinnerNumberModel1 = null; // @jve:decl-index=0:

  private JSpinner weightSpinner = null;

  private SpinnerNumberModel spinnerNumberModel2 = null; // @jve:decl-index=0:

  private JTextField hairField = null;

  private JComboBox eyeCombo = null;

  private Hero currentHero;

  private JButton birthdayButton = null;

  /**
   * This is the default constructor -- only for the visual editor!
   */
  public PhysFrame() {
    super();
    initialize();
    currentHero = null;
    updateData();
  }

  public PhysFrame(String title) {
    super(title);
    initialize();
    currentHero = Group.getInstance().getActiveHero();
    updateData();
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(PhysFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(PhysFrame.this);
          done = true;
        }
      }
    });
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Grunddaten");
    // this.setSize(new java.awt.Dimension(739,313));
    this.setContentPane(getJContentPane());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel13 = new JLabel();
      jLabel13.setBounds(new java.awt.Rectangle(16, 191, 103, 15));
      jLabel13.setText("Titel:");
      jLabel12 = new JLabel();
      jLabel12.setBounds(new java.awt.Rectangle(320, 103, 112, 15));
      jLabel12.setText("Hautfarbe:");
      jLabel11 = new JLabel();
      jLabel10 = new JLabel();
      jLabel9 = new JLabel();
      jLabel8 = new JLabel();
      jLabel7 = new JLabel();
      jLabel6 = new JLabel();
      jLabel5 = new JLabel();
      jLabel4 = new JLabel();
      jLabel3 = new JLabel();
      jLabel2 = new JLabel();
      jLabel1 = new JLabel();
      jLabel = new JLabel();
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(null);
      jLabel.setBounds(16, 15, 96, 16);
      jLabel.setText("Name:");
      jLabel1.setBounds(16, 37, 96, 16);
      jLabel1.setText("Typ:");
      jLabel2.setBounds(16, 59, 96, 16);
      jLabel2.setText("Geschlecht:");
      jLabel3.setBounds(16, 81, 96, 16);
      jLabel3.setText("Gottheit:");
      jLabel4.setBounds(16, 103, 109, 16);
      jLabel4.setText("Stand der Eltern:");
      jLabel5.setBounds(16, 169, 96, 16);
      jLabel5.setText("Geburtsort:");
      jLabel6.setText("Geburtstag:");
      jLabel6.setLocation(16, 147);
      jLabel6.setSize(110, 16);
      jLabel7.setText("Alter (Jahre):");
      jLabel7.setLocation(320, 147);
      jLabel7.setSize(110, 16);
      jLabel8.setText("Größe (Halbfinger):");
      jLabel8.setLocation(320, 59);
      jLabel8.setSize(125, 16);
      jLabel9.setText("Gewicht (Stein):");
      jLabel9.setSize(110, 16);
      jLabel9.setLocation(320, 81);
      jLabel10.setText("Haarfarbe:");
      jLabel10.setLocation(16, 125);
      jLabel10.setSize(110, 16);
      jLabel11.setText("Augenfarbe:");
      jLabel11.setLocation(320, 125);
      jLabel11.setSize(110, 16);
      jContentPane.add(jLabel, null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(jLabel5, null);
      jContentPane.add(getNameField(), null);
      jContentPane.add(jLabel6, null);
      jContentPane.add(jLabel7, null);
      jContentPane.add(getSexCombo(), null);
      jContentPane.add(getTypeField(), null);
      jContentPane.add(getGodBox(), null);
      jContentPane.add(getStandCombo(), null);
      jContentPane.add(getBirthplaceField(), null);
      jContentPane.add(getNameButton(), null);
      jContentPane.add(getBirthPlaceButton(), null);
      jContentPane.add(jLabel8, null);
      jContentPane.add(jLabel9, null);
      jContentPane.add(jLabel10, null);
      jContentPane.add(jLabel11, null);
      jContentPane.add(getBirthdayField(), null);
      jContentPane.add(getAgeSpinner(), null);
      jContentPane.add(getHeightSpinner(), null);
      jContentPane.add(getWeightSpinner(), null);
      jContentPane.add(getHairField(), null);
      jContentPane.add(getEyeCombo(), null);
      jContentPane.add(getBirthdayButton(), null);
      jContentPane.add(jLabel12, null);
      jContentPane.add(getSkinField(), null);
      jContentPane.add(jLabel13, null);
      jContentPane.add(getTitleField(), null);
      jContentPane.add(getHairButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(135, 15, 310, 16);
      nameField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setName(getNameField().getText());
        }

        public void removeUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setName(getNameField().getText());
        }

        public void changedUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setName(getNameField().getText());
        }
      });
    }
    return nameField;
  }

  /**
   * This method initializes jComboBox1
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getSexCombo() {
    if (sexCombo == null) {
      sexCombo = new JComboBox();
      sexCombo.setBounds(135, 59, 128, 16);
      sexCombo.addItem("weiblich");
      sexCombo.addItem("männlich");
      sexCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSex(getSexCombo().getSelectedItem()
              .equals("männlich") ? "m" : "w");
        }
      });
    }
    return sexCombo;
  }

  /**
   * This method initializes jTextField1
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getTypeField() {
    if (typeField == null) {
      typeField = new JTextField();
      typeField.setBounds(135, 37, 310, 16);
      typeField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          updateType();
        }

        public void removeUpdate(DocumentEvent e) {
          updateType();
        }

        public void changedUpdate(DocumentEvent e) {
          updateType();
        }

        private void updateType() {
          if (disableChanges) return;
          currentHero.setType(typeField.getText());
        }

      });
    }
    return typeField;
  }

  private enum God {
    Praios, Rondra, Efferd, Travia, Boron, Hesinde, Firun, Tsa, Phex, Peraine, Ingerimm, Rhaja, Swafnir, Kor, Rastullah, Sumu, Satuaria, Angrosch, Keine
  };

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getGodBox() {
    if (godBox == null) {
      godBox = new JComboBox();
      godBox.setBounds(135, 81, 128, 16);
      godBox.setEditable(true);
      for (God god : God.values())
        godBox.addItem(god.toString());
      godBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setGod(getGodBox().getSelectedItem()
              .toString());
        }
      });
    }
    return godBox;
  }

  /**
   * This method initializes jComboBox2
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getStandCombo() {
    if (standCombo == null) {
      standCombo = new JComboBox();
      standCombo.setBounds(135, 103, 169, 16);
      standCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (disableChanges) return;
          if (currentHero != null && standCombo.getSelectedItem() != null) {
            currentHero.setStand(standCombo.getSelectedItem().toString());
          }
        }
      });
    }
    return standCombo;
  }

  /**
   * This method initializes jTextField2
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getBirthplaceField() {
    if (birthPlaceField == null) {
      birthPlaceField = new JTextField();
      birthPlaceField.setBounds(135, 169, 310, 16);
      birthPlaceField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setBirthPlace(getBirthplaceField()
              .getText());
        }

        public void removeUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setBirthPlace(getBirthplaceField()
              .getText());
        }

        public void changedUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setBirthPlace(getBirthplaceField()
              .getText());
        }
      });
    }
    return birthPlaceField;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getNameButton() {
    if (nameButton == null) {
      nameButton = new JButton();
      nameButton.setBounds(455, 15, 36, 16);
      nameButton.setText("...");
      nameButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectName();
        }
      });
    }
    return nameButton;
  }

  private void selectName() {
    NameSelectionDialog dialog = new NameSelectionDialog(this, true);
    dialog.setLocationRelativeTo(this);
    dialog.setCharacterName(getNameField().getText());
    dialog.setSex(currentHero.getSex().toLowerCase(Locale.GERMAN).startsWith("w"));
    dialog.setVisible(true);
    String name = dialog.getCharacterName();
    if (name != null) {
      getNameField().setText(name);
      getSexCombo().setSelectedIndex(dialog.isFemale() ? 0 : 1);
    }
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getBirthPlaceButton() {
    if (birthPlaceButton == null) {
      birthPlaceButton = new JButton();
      birthPlaceButton.setBounds(455, 169, 36, 16);
      birthPlaceButton.setText("...");
      birthPlaceButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectBirthPlace();
        }
      });
    }
    return birthPlaceButton;
  }

  private void selectBirthPlace() {
    CitySelectionDialog dialog = new CitySelectionDialog(this, true);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    String city = dialog.getSelectedCity();
    if (city != null) getBirthplaceField().setText(city);
  }

  /**
   * This method initializes jTextField3
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getBirthdayField() {
    if (birthDayField == null) {
      birthDayField = new JTextField();
      birthDayField.setSize(128, 16);
      birthDayField.setLocation(135, 147);
    }
    return birthDayField;
  }

  /**
   * This method initializes jSpinner
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getAgeSpinner() {
    if (ageSpinner == null) {
      ageSpinner = new JSpinner();
      ageSpinner.setSize(57, 16);
      ageSpinner.setLocation(455, 147);
      ageSpinner.setModel(getSpinnerNumberModel());
      ageSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setAge(((Integer) getAgeSpinner()
              .getModel().getValue()).intValue());
        }
      });
    }
    return ageSpinner;
  }

  /**
   * This method initializes spinnerNumberModel
   * 
   * @return javax.swing.SpinnerNumberModel
   */
  private SpinnerNumberModel getSpinnerNumberModel() {
    if (spinnerNumberModel == null) {
      spinnerNumberModel = new SpinnerNumberModel();
      spinnerNumberModel.setMinimum(Integer.valueOf(0));
      spinnerNumberModel.setStepSize(Integer.valueOf(1));
    }
    return spinnerNumberModel;
  }

  /**
   * This method initializes jSpinner1
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getHeightSpinner() {
    if (heightSpinner == null) {
      heightSpinner = new JSpinner();
      heightSpinner.setSize(57, 16);
      heightSpinner.setLocation(455, 59);
      heightSpinner.setModel(getSpinnerNumberModel1());
      heightSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setHeight(((Integer) getHeightSpinner()
              .getModel().getValue()).toString());
        }
      });
    }
    return heightSpinner;
  }

  /**
   * This method initializes spinnerNumberModel1
   * 
   * @return javax.swing.SpinnerNumberModel
   */
  private SpinnerNumberModel getSpinnerNumberModel1() {
    if (spinnerNumberModel1 == null) {
      spinnerNumberModel1 = new SpinnerNumberModel();
      spinnerNumberModel1.setMinimum(Integer.valueOf(0));
    }
    return spinnerNumberModel1;
  }

  /**
   * This method initializes jSpinner2
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getWeightSpinner() {
    if (weightSpinner == null) {
      weightSpinner = new JSpinner();
      weightSpinner.setLocation(455, 81);
      weightSpinner.setSize(57, 16);
      weightSpinner.setModel(getSpinnerNumberModel2());
      weightSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setWeight(((Integer) getWeightSpinner()
              .getModel().getValue()).toString());
        }
      });
    }
    return weightSpinner;
  }

  /**
   * This method initializes spinnerNumberModel2
   * 
   * @return javax.swing.SpinnerNumberModel
   */
  private SpinnerNumberModel getSpinnerNumberModel2() {
    if (spinnerNumberModel2 == null) {
      spinnerNumberModel2 = new SpinnerNumberModel();
      spinnerNumberModel2.setMinimum(Integer.valueOf(0));
    }
    return spinnerNumberModel2;
  }

  /**
   * This method initializes jComboBox3
   * 
   * @return javax.swing.JComboBox
   */
  private JTextField getHairField() {
    if (hairField == null) {
      hairField = new JTextField();
      hairField.setSize(128, 16);
      hairField.setLocation(135, 125);
      hairField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setHairColor(getHairField().getText());
        }

        public void removeUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setHairColor(getHairField().getText());
        }

        public void changedUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setHairColor(getHairField().getText());
        }
      });
    }
    return hairField;
  }

  private JButton hairButton = null;

  private JButton getHairButton() {
    if (hairButton == null) {
      hairButton = new JButton("...");
      hairButton.setBounds(268, 125, 36, 16);
      hairButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectHair();
        }
      });
    }
    return hairButton;
  }

  private class HairSelector implements ActionListener {
    private final String hair;

    public HairSelector(String hair) {
      this.hair = hair;
    }

    public void actionPerformed(ActionEvent e) {
      getHairField().setText(hair);
    }
  }

  private void selectHair() {
    JMenu menu = new JMenu();
    for (String hairCategory : Looks.getInstance().getHairCategories()) {
      JMenu subMenu = new JMenu(hairCategory);
      for (String hairColor : Looks.getInstance().getHairColors(hairCategory)) {
        JMenuItem item = new JMenuItem(hairColor);
        item.addActionListener(new HairSelector(hairColor));
        subMenu.add(item);
      }
      menu.add(subMenu);
    }
    menu.getPopupMenu().show(jContentPane, 268, 125 + 16);
  }

  /**
   * This method initializes jComboBox4
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getEyeCombo() {
    if (eyeCombo == null) {
      eyeCombo = new JComboBox();
      eyeCombo.setSize(128, 16);
      eyeCombo.setLocation(455, 125);
      eyeCombo.addItem("<passend>");
      for (String color : Looks.getInstance().getEyeColors()) {
        eyeCombo.addItem(color);
      }
      eyeCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          String color = getEyeCombo().getSelectedItem().toString();
          if (color.equals("<passend>")) {
            color = Looks.getMatchingEyeColor(currentHero.getHairColor());
            disableChanges = true;
            getEyeCombo().setSelectedItem(color);
            disableChanges = false;
          }
          currentHero.setEyeColor(color);
        }
      });
    }
    return eyeCombo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#ActiveCharacterChanged(dsa.data.Hero,
   *      dsa.data.Hero)
   */
  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    updateData();
  }

  public void globalLockChanged() {
  }

  /**
   * 
   * 
   */
  private void updateData() {
    disableChanges = true;
    boolean hasHero = currentHero != null;
    getBirthdayField().setEnabled(true);
    getBirthdayField().setEditable(true);

    if (hasHero) {
      getNameField().setText(currentHero.getName());
      getTypeField().setText(currentHero.getType());
      getSexCombo().setSelectedIndex(currentHero.getSex().equals("m") ? 1 : 0);
      getGodBox().setSelectedItem(currentHero.getGod());
      getStandCombo().removeAllItems();
      CharacterType heroType = CharacterTypes.getInstance().getType(
          currentHero.getType());
      if (heroType != null) {
        getStandCombo().setEnabled(true);
        for (String origin : heroType.getPossibleOrigins()) {
          getStandCombo().addItem(origin);
        }
      }
      else
        getStandCombo().setEnabled(false);
      getStandCombo().setSelectedItem(currentHero.getStand());
      getBirthplaceField().setText(currentHero.getBirthPlace());
      getBirthdayField().setText(currentHero.getBirthday());
      getAgeSpinner().getModel().setValue(Integer.valueOf(currentHero.getAge()));
      int height = 170;
      try {
        height = Integer.parseInt(currentHero.getHeight());
      }
      catch (NumberFormatException e) {
        height = 170;
      }
      getHeightSpinner().getModel().setValue(Integer.valueOf(height));
      int weight = 70;
      try {
        weight = Integer.parseInt(currentHero.getWeight());
      }
      catch (NumberFormatException e) {
        weight = 70;
      }
      getWeightSpinner().getModel().setValue(Integer.valueOf(weight));
      getHairField().setText(currentHero.getHairColor());
      getEyeCombo().setSelectedItem(currentHero.getEyeColor());
      getTitleField().setText(currentHero.getTitle());
      getSkinField().setText(currentHero.getSkin());
    }
    else {
      getNameField().setText("");
      getTypeField().setText("");
      getSexCombo().setSelectedIndex(0);
      getGodBox().setSelectedIndex(0);
      getStandCombo().setSelectedItem("");
      getBirthplaceField().setText("");
      getBirthdayField().setText("");
      getAgeSpinner().getModel().setValue(Integer.valueOf(20));
      getHeightSpinner().getModel().setValue(Integer.valueOf(170));
      getWeightSpinner().getModel().setValue(Integer.valueOf(70));
      getHairField().setText("");
      getEyeCombo().setSelectedItem("");
      getTitleField().setText("");
      getSkinField().setText("");
      getStandCombo().setEnabled(false);
    }
    getNameField().setEnabled(hasHero);
    getSexCombo().setEnabled(hasHero);
    getGodBox().setEnabled(hasHero);
    getBirthplaceField().setEnabled(hasHero);
    getAgeSpinner().setEnabled(hasHero);
    getHeightSpinner().setEnabled(hasHero);
    getWeightSpinner().setEnabled(hasHero);
    getTitleField().setEnabled(hasHero);
    getSkinField().setEnabled(hasHero);
    getBirthPlaceButton().setEnabled(hasHero);
    getHairButton().setEnabled(hasHero);
    getEyeCombo().setEnabled(hasHero);
    getEyeCombo().setEditable(hasHero);
    getHairField().setEnabled(hasHero);
    getHairField().setEditable(hasHero);
    getNameButton().setEnabled(hasHero);
    getTypeField().setEnabled(hasHero);

    getBirthdayField().setEnabled(hasHero);
    getBirthdayField().setEditable(false);
    getBirthdayButton().setEnabled(hasHero);
    getStandCombo().setEditable(false);
    disableChanges = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterRemoved(dsa.data.Hero)
   */
  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dsa.data.CharactersObserver#CharacterAdded(dsa.data.Hero)
   */
  public void characterAdded(Hero character) {
  }

  private boolean disableChanges = false;

  private JLabel jLabel12 = null;

  private JTextField skinField = null;

  private JLabel jLabel13 = null;

  private JTextField titleField = null;

  /**
   * This method initializes jButton2
   * 
   * @return javax.swing.JButton
   */
  private JButton getBirthdayButton() {
    if (birthdayButton == null) {
      birthdayButton = new JButton();
      birthdayButton.setText("...");
      birthdayButton.setSize(36, 16);
      birthdayButton.setLocation(268, 147);
      birthdayButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectBirthday();
        }
      });
    }
    return birthdayButton;
  }

  private void selectBirthday() {
    DateSelectionDialog dialog = new DateSelectionDialog(this, true);
    dialog.setLocationRelativeTo(this);
    dialog.setDate(getBirthdayField().getText());
    dialog.setVisible(true);
    String date = dialog.getDate();
    if (date != null) {
      getBirthdayField().setText(date);
      currentHero.setBirthday(date);
    }
  }

  /**
   * This method initializes jTextField4
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getSkinField() {
    if (skinField == null) {
      skinField = new JTextField();
      skinField.setBounds(new java.awt.Rectangle(455, 103, 128, 16));
      skinField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSkin(getSkinField().getText());
        }

        public void removeUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSkin(getSkinField().getText());
        }

        public void changedUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSkin(getSkinField().getText());
        }
      });
    }
    return skinField;
  }

  /**
   * This method initializes jTextField4
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getTitleField() {
    if (titleField == null) {
      titleField = new JTextField();
      titleField.setBounds(new java.awt.Rectangle(135, 191, 310, 16));
      titleField.getDocument().addDocumentListener(new DocumentListener() {
        public void insertUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setTitle(getTitleField().getText());
        }

        public void removeUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setTitle(getTitleField().getText());
        }

        public void changedUpdate(DocumentEvent e) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setTitle(getTitleField().getText());
        }
      });
    }
    return titleField;
  }
} // @jve:decl-index=0:visual-constraint="18,8"

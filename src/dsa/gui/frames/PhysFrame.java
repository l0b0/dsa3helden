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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import dsa.gui.dialogs.CitySelectionDialog;
import dsa.gui.dialogs.DateSelectionDialog;
import dsa.gui.dialogs.NameSelectionDialog;
import dsa.model.Date;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.model.data.CharacterType;
import dsa.model.data.CharacterTypes;
import dsa.model.data.Looks;
import javax.swing.JPanel;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.lang.Integer;

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
  
  public String getHelpPage() {
    return "Grunddaten"; //$NON-NLS-1$
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
  
  public void groupDateChanged() {
    updateData();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle(Localization.getString("Grunddaten.Grunddaten")); //$NON-NLS-1$
    // this.setSize(new Dimension(569, 398));
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
      jLabel13.setText(Localization.getString("Grunddaten.Titel")); //$NON-NLS-1$
      jLabel13.setBounds(new Rectangle(10, 40, 103, 15));
      jLabel12 = new JLabel();
      jLabel12.setText(Localization.getString("Grunddaten.Hautfarbe")); //$NON-NLS-1$
      jLabel12.setBounds(new Rectangle(10, 60, 112, 15));
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
      jLabel.setText(Localization.getString("Grunddaten.Name")); //$NON-NLS-1$
      jLabel.setBounds(new Rectangle(10, 20, 96, 16));
      jLabel1.setText(Localization.getString("Grunddaten.Typ")); //$NON-NLS-1$
      jLabel1.setBounds(new Rectangle(10, 40, 96, 16));
      jLabel2.setText(Localization.getString("Grunddaten.Geschlecht")); //$NON-NLS-1$
      jLabel2.setBounds(new Rectangle(320, 20, 111, 16));
      jLabel3.setText(Localization.getString("Grunddaten.Gottheit")); //$NON-NLS-1$
      jLabel3.setBounds(new Rectangle(10, 20, 96, 16));
      jLabel4.setText(Localization.getString("Grunddaten.Stand")); //$NON-NLS-1$
      jLabel4.setBounds(new Rectangle(10, 60, 109, 16));
      jLabel5.setText(Localization.getString("Grunddaten.Geburtsort")); //$NON-NLS-1$
      jLabel5.setBounds(new Rectangle(10, 40, 96, 16));
      jLabel6.setText(Localization.getString("Grunddaten.Geburtstag")); //$NON-NLS-1$
      jLabel6.setBounds(new Rectangle(10, 20, 110, 16));
      jLabel7.setText(Localization.getString("Grunddaten.AlterJahre")); //$NON-NLS-1$
      jLabel7.setBounds(new Rectangle(320, 20, 110, 16));
      jLabel8.setText(Localization.getString("Grunddaten.GroesseHalbfinger")); //$NON-NLS-1$
      jLabel8.setBounds(new Rectangle(320, 40, 121, 16));
      jLabel9.setText(Localization.getString("Grunddaten.GewichtStein")); //$NON-NLS-1$
      jLabel9.setBounds(new Rectangle(320, 60, 110, 16));
      jLabel10.setText(Localization.getString("Grunddaten.Haarfarbe")); //$NON-NLS-1$
      jLabel10.setBounds(new Rectangle(10, 20, 111, 16));
      jLabel11.setText(Localization.getString("Grunddaten.Augenfarbe")); //$NON-NLS-1$
      jLabel11.setBounds(new Rectangle(10, 40, 110, 16));
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getJPanel1(), null);
      jContentPane.add(getJPanel2(), null);
      jContentPane.add(getJPanel3(), null);
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
      nameField.setBounds(new Rectangle(125, 20, 310, 16));
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
      sexCombo.setBounds(new Rectangle(445, 20, 86, 16));
      sexCombo.addItem(Localization.getString("Grunddaten.weiblich")); //$NON-NLS-1$
      sexCombo.addItem(Localization.getString("Grunddaten.maennlich")); //$NON-NLS-1$
      sexCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSex(getSexCombo().getSelectedItem()
              .equals(Localization.getString("Grunddaten.maennlich")) ? "m" : "w"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
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
      typeField.setBounds(new Rectangle(125, 40, 310, 16));
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
      godBox.setEditable(true);
      godBox.setBounds(new Rectangle(125, 20, 131, 16));
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
  
  private boolean standComboFilled = false;

  /**
   * This method initializes jComboBox2
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getStandCombo() {
    if (standCombo == null) {
      standCombo = new JComboBox();
      standCombo.setBounds(new Rectangle(125, 60, 176, 16));
      standCombo.addPopupMenuListener(new PopupMenuListener() {
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          if (!standComboFilled) {
            CharacterType heroType = CharacterTypes.getInstance().getType(
                currentHero.getInternalType());
            if (heroType != null) {
              for (String origin : heroType.getPossibleOrigins()) {
                getStandCombo().addItem(origin);
              }
            }
            standComboFilled = true;
          }
        }
      });
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
      birthPlaceField.setBounds(new Rectangle(125, 40, 310, 16));
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
      nameButton.setText("...");  //$NON-NLS-1$
      nameButton.setBounds(new Rectangle(445, 20, 36, 16));
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
    dialog.setSex(currentHero.getSex().toLowerCase(Locale.GERMAN).startsWith("w"));  //$NON-NLS-1$
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
      birthPlaceButton.setText("...");  //$NON-NLS-1$
      birthPlaceButton.setBounds(new Rectangle(445, 40, 36, 16));
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
      birthDayField.setBounds(new Rectangle(125, 20, 128, 16));
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
      ageSpinner.setBounds(new Rectangle(445, 20, 57, 16));
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
      heightSpinner.setBounds(new Rectangle(445, 40, 57, 16));
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
      weightSpinner.setBounds(new Rectangle(445, 60, 57, 16));
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
      hairField.setBounds(new Rectangle(125, 20, 131, 16));
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
      hairButton = new JButton("...");  //$NON-NLS-1$
      hairButton.setBounds(new Rectangle(270, 20, 36, 16));
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
    menu.getPopupMenu().show(jPanel, 270, 20 + 16);
  }

  /**
   * This method initializes jComboBox4
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getEyeCombo() {
    if (eyeCombo == null) {
      eyeCombo = new JComboBox();
      eyeCombo.setBounds(new Rectangle(125, 40, 131, 16));
      eyeCombo.addPopupMenuListener(new PopupMenuListener() {
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }
        
        private boolean filled = false;

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          if (!filled) {
            fillBox();
            filled = true;
          }
        }
        
        private void fillBox() {
          eyeCombo.addItem(Localization.getString("Grunddaten.passend")); //$NON-NLS-1$
          for (String color : Looks.getInstance().getEyeColors()) {
            eyeCombo.addItem(color);
          }          
        }
      });
      eyeCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          String color = getEyeCombo().getSelectedItem().toString();
          if (color.equals(Localization.getString("Grunddaten.passend"))) { //$NON-NLS-1$
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
    getEyeCombo().setEnabled(true);
    getEyeCombo().setEditable(true);
    getStandCombo().setEnabled(true);
    getStandCombo().setEditable(true);

    if (hasHero) {
      getNameField().setText(currentHero.getName());
      getTypeField().setText(currentHero.getType());
      getSexCombo().setSelectedIndex(currentHero.getSex().equals("m") ? 1 : 0);  //$NON-NLS-1$
      getGodBox().setSelectedItem(currentHero.getGod());
      getStandCombo().removeAllItems();
      standComboFilled = false;
      getStandCombo().setSelectedItem(currentHero.getStand());
      getBirthplaceField().setText(currentHero.getBirthPlace());
      getBirthdayField().setText(currentHero.getBirthday().toString());
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
      getSoSpinner().getModel().setValue(currentHero.getSO());
    }
    else {
      getNameField().setText("");  //$NON-NLS-1$
      getTypeField().setText("");  //$NON-NLS-1$
      getSexCombo().setSelectedIndex(0);
      getGodBox().setSelectedIndex(0);
      getStandCombo().setSelectedItem("");  //$NON-NLS-1$
      getBirthplaceField().setText("");  //$NON-NLS-1$
      getBirthdayField().setText("");   //$NON-NLS-1$
      getAgeSpinner().getModel().setValue(Integer.valueOf(20));
      getHeightSpinner().getModel().setValue(Integer.valueOf(170));
      getWeightSpinner().getModel().setValue(Integer.valueOf(70));
      getHairField().setText("");   //$NON-NLS-1$
      getEyeCombo().setSelectedItem("");   //$NON-NLS-1$
      getTitleField().setText("");   //$NON-NLS-1$
      getSkinField().setText("");   //$NON-NLS-1$
      getSoSpinner().getModel().setValue(8);
    }
    boolean changeable = hasHero && !currentHero.isDifference();
    getStandCombo().setEnabled(changeable);
    getNameField().setEnabled(changeable);
    getSexCombo().setEnabled(changeable);
    getGodBox().setEnabled(changeable);
    getBirthplaceField().setEnabled(changeable);
    getAgeSpinner().setEnabled(changeable);
    getHeightSpinner().setEnabled(changeable);
    getWeightSpinner().setEnabled(changeable);
    getTitleField().setEnabled(changeable);
    getSkinField().setEnabled(changeable);
    getBirthPlaceButton().setEnabled(changeable);
    getHairButton().setEnabled(changeable);
    getEyeCombo().setEnabled(changeable);
    getEyeCombo().setEditable(changeable);
    getHairField().setEnabled(changeable);
    getHairField().setEditable(changeable);
    getNameButton().setEnabled(changeable);
    getTypeField().setEnabled(changeable);
    getSoSpinner().setEnabled(changeable);

    getBirthdayField().setEnabled(changeable);
    getBirthdayField().setEditable(false);
    getBirthdayButton().setEnabled(changeable);
    getStandCombo().setEditable(changeable);
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

  private JPanel jPanel = null;

  private JPanel jPanel1 = null;

  private JLabel jLabel14 = null;

  private JPanel jPanel2 = null;

  private JSpinner soSpinner = null;

  private JPanel jPanel3 = null;

  /**
   * This method initializes jButton2
   * 
   * @return javax.swing.JButton
   */
  private JButton getBirthdayButton() {
    if (birthdayButton == null) {
      birthdayButton = new JButton();
      birthdayButton.setText("...");   //$NON-NLS-1$
      birthdayButton.setBounds(new Rectangle(265, 20, 36, 16));
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
    dialog.setDate(currentHero.getBirthday());
    dialog.setVisible(true);
    Date date = dialog.getDate();
    if (date != null) {
      getBirthdayField().setText(date.toString());
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
      skinField.setBounds(new Rectangle(125, 60, 131, 16));
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
      titleField.setBounds(new Rectangle(125, 40, 310, 16));
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

  /**
   * This method initializes jPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel() {
    if (jPanel == null) {
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setBounds(new Rectangle(10, 90, 541, 91));
      jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), Localization.getString("Grunddaten.Aussehen"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null)); //$NON-NLS-1$
      jPanel.add(jLabel2, null);
      jPanel.add(getSexCombo(), null);
      jPanel.add(jLabel8, null);
      jPanel.add(getHeightSpinner(), null);
      jPanel.add(jLabel10, null);
      jPanel.add(getHairField(), null);
      jPanel.add(getHairButton(), null);
      jPanel.add(jLabel9, null);
      jPanel.add(getWeightSpinner(), null);
      jPanel.add(jLabel12, null);
      jPanel.add(getSkinField(), null);
      jPanel.add(jLabel11, null);
      jPanel.add(getEyeCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jLabel14 = new JLabel();
      jLabel14.setBounds(new Rectangle(320, 20, 116, 16));
      jLabel14.setText(Localization.getString("Grunddaten.Sozialstatus")); //$NON-NLS-1$
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBounds(new Rectangle(10, 190, 541, 71));
      jPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), Localization.getString("Grunddaten.Gesellschaft"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null)); //$NON-NLS-1$
      jPanel1.add(jLabel13, null);
      jPanel1.add(getTitleField(), null);
      jPanel1.add(jLabel3, null);
      jPanel1.add(getGodBox(), null);
      jPanel1.add(jLabel14, null);
      jPanel1.add(getSoSpinner(), null);
    }
    return jPanel1;
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
      jPanel2.setBounds(new Rectangle(10, 270, 541, 91));
      jPanel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), Localization.getString("Grunddaten.Geburt"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null)); //$NON-NLS-1$
      jPanel2.add(jLabel6, null);
      jPanel2.add(getBirthdayField(), null);
      jPanel2.add(getBirthdayButton(), null);
      jPanel2.add(jLabel7, null);
      jPanel2.add(getAgeSpinner(), null);
      jPanel2.add(jLabel5, null);
      jPanel2.add(getBirthplaceField(), null);
      jPanel2.add(jLabel4, null);
      jPanel2.add(getStandCombo(), null);
      jPanel2.add(getBirthPlaceButton(), null);
    }
    return jPanel2;
  }

  /**
   * This method initializes soSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getSoSpinner() {
    if (soSpinner == null) {
      SpinnerNumberModel spinnerNumberModel3 = new SpinnerNumberModel();
      spinnerNumberModel3.setMinimum(Integer.valueOf(0));
      spinnerNumberModel3.setMaximum(Integer.valueOf(21));
      spinnerNumberModel3.setStepSize(Integer.valueOf(1));
      soSpinner = new JSpinner();
      soSpinner.setBounds(new Rectangle(443, 20, 58, 16));
      soSpinner.setModel(spinnerNumberModel3);
      soSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
          if (PhysFrame.this.disableChanges) return;
          if (PhysFrame.this.currentHero == null) return;
          PhysFrame.this.currentHero.setSO(
              ((Integer)getSoSpinner().getModel().getValue()).intValue());
        }
      });
    }
    return soSpinner;
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
      jPanel3.setBounds(new Rectangle(10, 10, 541, 71));
      jPanel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), Localization.getString("Grunddaten.Allgemein"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null)); //$NON-NLS-1$
      jPanel3.add(jLabel, null);
      jPanel3.add(getNameField(), null);
      jPanel3.add(jLabel1, null);
      jPanel3.add(getTypeField(), null);
      jPanel3.add(getNameButton(), null);
    }
    return jPanel3;
  }
} // @jve:decl-index=0:visual-constraint="18,8"

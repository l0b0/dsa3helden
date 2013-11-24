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
 along with Heldenverwaltung; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import dsa.model.data.Weapon;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class ProjectileAttackDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JLabel jLabel5 = null;

  private JLabel jLabel6 = null;

  private JLabel sumLabel = null;

  private JComboBox movementBox = null;

  private JComboBox sightBox = null;

  private JComboBox windBox = null;

  private JSpinner fightSpinner = null;

  private JLabel jLabel8 = null;

  private JComboBox shootBox = null;

  private JComboBox sizeBox = null;

  private JSpinner distanceSpinner = null;

  private JLabel jLabel9 = null;

  private JButton okButton = null;

  private JButton jButton = null;

  /**
   * This method initializes
   * 
   */
  public ProjectileAttackDialog(JFrame owner, int at, Weapon weapon) {
    super(owner, true);
    initialize();
    setLocationRelativeTo(owner);
    baseAT = at;
    this.weapon = weapon;
    calcSums();
  }
  
  public String getHelpPage() {
    return "Fernkampf";
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(360, 337));
    this.setTitle("Fernkampf");
    this.setContentPane(getJContentPane());

  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel9 = new JLabel();
      jLabel9.setBounds(new java.awt.Rectangle(190, 10, 81, 21));
      jLabel9.setText("Schritt");
      jLabel8 = new JLabel();
      jLabel8.setBounds(new java.awt.Rectangle(190, 160, 91, 21));
      jLabel8.setText("Teilnehmer");
      sumLabel = new JLabel();
      sumLabel.setBounds(new java.awt.Rectangle(100, 230, 201, 21));
      sumLabel.setText("AT: 13  Zuschlag: 17");
      jLabel6 = new JLabel();
      jLabel6.setBounds(new java.awt.Rectangle(10, 190, 101, 21));
      jLabel6.setText("Sorgfalt:");
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(10, 160, 101, 21));
      jLabel5.setText("Nahkampf:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(10, 130, 101, 21));
      jLabel4.setText("Wind:");
      jLabel3 = new JLabel();
      jLabel3.setBounds(new java.awt.Rectangle(10, 100, 101, 21));
      jLabel3.setText("Sicht:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(10, 70, 101, 21));
      jLabel2.setText("Bewegung:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(10, 40, 101, 21));
      jLabel1.setText("Größe des Ziels:");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(10, 10, 91, 21));
      jLabel.setText("Entfernung:");
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(jLabel, null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(jLabel3, null);
      jContentPane.add(jLabel4, null);
      jContentPane.add(jLabel5, null);
      jContentPane.add(jLabel6, null);
      jContentPane.add(sumLabel, null);
      jContentPane.add(getMovementCombo(), null);
      jContentPane.add(getSightCombo(), null);
      jContentPane.add(getWindCombo(), null);
      jContentPane.add(getFightSpinner(), null);
      jContentPane.add(jLabel8, null);
      jContentPane.add(getShootCombo(), null);
      jContentPane.add(getSizeCombo(), null);
      jContentPane.add(getDistanceSpinner(), null);
      jContentPane.add(jLabel9, null);
      jContentPane.add(getOKButton(), null);
      jContentPane.add(getCancelButton(), null);
    }
    return jContentPane;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getMovementCombo() {
    if (movementBox == null) {
      movementBox = new JComboBox();
      movementBox.setBounds(new java.awt.Rectangle(130, 70, 211, 21));
      movementBox.addItem("Keine (+0)");
      movementBox.addItem("Längs (+2)");
      movementBox.addItem("Quer  (+4)");
      movementBox.addItem("Haken (+6)");
      movementBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          calcSums();
        }
      });
    }
    return movementBox;
  }

  private boolean cancel = true;

  private int atValue = 0;

  private int modifier = 0;

  private int baseAT = 0;

  private int distance = 0;

  private Weapon weapon;

  public boolean wasCanceled() {
    return cancel;
  }

  public int getATValue() {
    return atValue;
  }

  public int getModifier() {
    return modifier;
  }

  public int getDistance() {
    return distance;
  }

  protected void calcSums() {
    modifier = 0;
    distance = ((Number) distanceSpinner.getValue()).intValue();
    switch (weapon.getDistanceCategory(distance)) {
    case 0:
      modifier -= 4;
      break;
    case 1:
      break;
    case 2:
      modifier += 4;
      break;
    case 3:
      modifier += 8;
      break;
    case 4:
      modifier += 12;
      break;
    default:
      break;
    }
    switch (sizeBox.getSelectedIndex()) {
    case 0:
      modifier += 9;
      break;
    case 1:
      modifier += 6;
      break;
    case 2:
      modifier += 3;
      break;
    case 3:
      break;
    case 4:
      modifier -= 3;
      break;
    case 5:
      modifier -= 6;
      break;
    default:
      break;
    }
    switch (movementBox.getSelectedIndex()) {
    case 0:
      break;
    case 1:
      modifier += 2;
      break;
    case 2:
      modifier += 4;
      break;
    case 3:
      modifier += 6;
      break;
    default:
      break;
    }
    modifier += sightBox.getSelectedIndex();
    modifier += windBox.getSelectedIndex();
    modifier += 3 * ((Number) fightSpinner.getValue()).intValue();
    switch (shootBox.getSelectedIndex()) {
    case 0:
      atValue = baseAT / 2;
      break;
    case 1:
      atValue = baseAT;
      break;
    case 2:
      atValue = baseAT + baseAT / 2;
      break;
    default:
      atValue = baseAT;
    }
    sumLabel.setText("AT-Wert: " + atValue + "  Zuschlag: " + modifier);
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getSightCombo() {
    if (sightBox == null) {
      sightBox = new JComboBox();
      sightBox.setBounds(new java.awt.Rectangle(130, 100, 211, 21));
      sightBox.addItem("+0 (Gut)");
      sightBox.addItem("+1 (Leichter Regen)");
      for (int i = 2; i < 15; ++i)
        sightBox.addItem("+" + i);
      sightBox.addItem("+15 (Finsternis)");
      sightBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          calcSums();
        }
      });
    }
    return sightBox;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getWindCombo() {
    if (windBox == null) {
      windBox = new JComboBox();
      windBox.setBounds(new java.awt.Rectangle(130, 130, 211, 21));
      windBox.addItem("+0 (Windstill)");
      windBox.addItem("+1 (Leichte Brise)");
      for (int i = 2; i < 15; ++i)
        windBox.addItem("+" + i);
      windBox.addItem("+15 (Orkan)");
      windBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          calcSums();
        }
      });
    }
    return windBox;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getFightSpinner() {
    if (fightSpinner == null) {
      fightSpinner = new JSpinner();
      fightSpinner.setBounds(new java.awt.Rectangle(130, 160, 51, 21));
      fightSpinner.setModel(new SpinnerNumberModel(0, 0, 5, 1));
      fightSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          calcSums();
        }
      });
    }
    return fightSpinner;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getShootCombo() {
    if (shootBox == null) {
      shootBox = new JComboBox();
      shootBox.setBounds(new java.awt.Rectangle(130, 190, 211, 21));
      shootBox.addItem("Schnellschuss");
      shootBox.addItem("Normaler Schuss");
      shootBox.addItem("Gezielter Schuss");
      shootBox.setSelectedIndex(1);
      shootBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          calcSums();
        }
      });
    }
    return shootBox;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getSizeCombo() {
    if (sizeBox == null) {
      sizeBox = new JComboBox();
      sizeBox.setBounds(new java.awt.Rectangle(130, 40, 211, 21));
      sizeBox.addItem("Winzig (Münze, Maus) -- +9");
      sizeBox.addItem("Sehr klein (Kaninchen) -- +6");
      sizeBox.addItem("Klein (Zwerg, Reh, Wolf) -- +3");
      sizeBox.addItem("Mittel (Mensch, Ork) -- +0");
      sizeBox.addItem("Groß (Oger, Pferd) -- -3");
      sizeBox.addItem("Sehr groß (Drache, Mammut) -- -6");
      sizeBox.setSelectedIndex(3);
      sizeBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          calcSums();
        }
      });
    }
    return sizeBox;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JSpinner
   */
  private JSpinner getDistanceSpinner() {
    if (distanceSpinner == null) {
      distanceSpinner = new JSpinner();
      distanceSpinner.setBounds(new java.awt.Rectangle(130, 10, 51, 21));
      distanceSpinner.setModel(new SpinnerNumberModel(20, 0, 250, 1));
      distanceSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          calcSums();
        }
      });
    }
    return distanceSpinner;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new java.awt.Rectangle(50, 270, 101, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          cancel = false;
          dispose();
        }
      });
    }
    return okButton;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (jButton == null) {
      jButton = new JButton();
      jButton.setBounds(new java.awt.Rectangle(200, 270, 101, 21));
      jButton.setText("Abbrechen");
      jButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          dispose();
        }
      });
    }
    return jButton;
  }

} // @jve:decl-index=0:visual-constraint="10,10"

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

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;

public class MagicAttributeFrame extends SubFrame implements CharactersObserver {

  private JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JLabel jLabel4 = null;

  private JPanel innerPanel = null;

  private JPanel innerPanel2 = null;

  private JPanel leftPanel = null;

  private JPanel rightPanel = null;

  private JLabel elementLabel = null;

  private JComboBox elementCombo = null;

  private JLabel academyLabel = null;

  private JTextField academyField = null;

  private JLabel soulAnimalLabel = null;

  private JTextField soulAnimalField = null;

  private JLabel specialLabel = null;

  private JComboBox specialCombo = null;

  public MagicAttributeFrame(String title) {
    super(title);
    this.setContentPane(getJContentPane());
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(MagicAttributeFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(MagicAttributeFrame.this);
          done = true;
        }
      }
    });
    updateData();
  }
  
  public String getHelpPage() {
    return "Magie"; //$NON-NLS-1$
  }

  private boolean disableChange = false;

  private void updateData() {
    disableChange = true;
    if (currentHero != null) {
      soulAnimalField.setText(currentHero.getSoulAnimal());
      specialCombo.setSelectedItem(currentHero.getSpecialization());
      elementCombo.setSelectedItem(currentHero.getElement());
      academyField.setText(currentHero.getAcademy());
    }
    else {
      soulAnimalField.setText(""); //$NON-NLS-1$
      specialCombo.setSelectedIndex(0);
      elementCombo.setSelectedIndex(0);
      academyField.setText(""); //$NON-NLS-1$
    }
    boolean editable = currentHero != null && !currentHero.isDifference();
    soulAnimalField.setEditable(editable);
    specialCombo.setEditable(editable);
    specialCombo.setEnabled(editable);
    elementCombo.setEnabled(editable);
    academyField.setEditable(editable);
    disableChange = false;
  }

  private Hero currentHero = null;

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel = new JLabel();
      jLabel.setText(""); //$NON-NLS-1$
      jLabel.setPreferredSize(new Dimension(5, 5));
      jLabel2 = new JLabel();
      jLabel2.setText(""); //$NON-NLS-1$
      jLabel2.setPreferredSize(new Dimension(5, 5));
      jLabel3 = new JLabel();
      jLabel3.setText(""); //$NON-NLS-1$
      jLabel3.setPreferredSize(new Dimension(5, 5));
      jLabel4 = new JLabel();
      jLabel4.setText(""); //$NON-NLS-1$
      jLabel4.setPreferredSize(new Dimension(5, 5));
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(jLabel, java.awt.BorderLayout.NORTH);
      jContentPane.add(jLabel2, java.awt.BorderLayout.SOUTH);
      jContentPane.add(jLabel3, java.awt.BorderLayout.EAST);
      jContentPane.add(jLabel4, java.awt.BorderLayout.WEST);
      innerPanel = new JPanel(new BorderLayout(10, 5));
      innerPanel2 = new JPanel(new BorderLayout());
      leftPanel = new JPanel(new GridLayout(4, 1, 5, 8));
      rightPanel = new JPanel(new GridLayout(4, 1, 5, 8));
      elementLabel = new JLabel(Localization.getString("Magie.Element")); //$NON-NLS-1$
      academyLabel = new JLabel(Localization.getString("Magie.Akademie")); //$NON-NLS-1$
      specialLabel = new JLabel(Localization.getString("Magie.Spezialgebiet")); //$NON-NLS-1$
      soulAnimalLabel = new JLabel(Localization.getString("Magie.Seelentier")); //$NON-NLS-1$
      leftPanel.add(specialLabel);
      leftPanel.add(academyLabel);
      leftPanel.add(elementLabel);
      leftPanel.add(soulAnimalLabel);
      innerPanel.add(leftPanel, BorderLayout.WEST);
      elementCombo = new JComboBox();
      elementCombo.addItem(Localization.getString("Magie.Keines")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Feuer")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Wasser")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Luft")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Erz")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Humus")); //$NON-NLS-1$
      elementCombo.addItem(Localization.getString("Magie.Eis")); //$NON-NLS-1$
      elementCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!disableChange) {
            currentHero.setElement(elementCombo.getSelectedItem().toString());
          }
        }
      });
      academyField = new JTextField();
      academyField.getDocument().addDocumentListener(new DocumentListener() {
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
          if (disableChange) return;
          currentHero.setAcademy(academyField.getText());
        }
      });
      specialCombo = new JComboBox();
      specialCombo.setEditable(true);
      specialCombo.addItem(Localization.getString("Magie.Keines")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Clarobservantia")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Combattiva")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Communicativa")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Conjuratio")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Contraria")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Controllaria")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Curativa")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Invocatio")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Moventia")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Mutanda")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Phantasmagorica")); //$NON-NLS-1$
      specialCombo.addItem(Localization.getString("Magie.Transformatorica")); //$NON-NLS-1$
      specialCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (disableChange) return;
          currentHero.setSpecialization(specialCombo.getSelectedItem()
              .toString());
        }
      });
      specialCombo.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (disableChange) return;
          currentHero.setSpecialization(specialCombo.getSelectedItem()
              .toString());
        }
      });
      soulAnimalField = new JTextField();
      soulAnimalField.getDocument().addDocumentListener(new DocumentListener() {
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
          if (disableChange) return;
          currentHero.setSoulAnimal(soulAnimalField.getText());
        }

      });
      rightPanel.add(specialCombo);
      rightPanel.add(academyField);
      rightPanel.add(elementCombo);
      rightPanel.add(soulAnimalField);
      innerPanel.add(rightPanel, BorderLayout.CENTER);
      innerPanel2.add(innerPanel, BorderLayout.NORTH);
      jContentPane.add(innerPanel2, BorderLayout.CENTER);
    }
    return jContentPane;
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

  public void globalLockChanged() {
  }
} // @jve:decl-index=0:visual-constraint="10,10"

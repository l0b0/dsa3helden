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
package dsa.gui.dialogs;

import dsa.control.Markers;
import dsa.gui.PackageID;
import dsa.gui.lf.BGDialog;
import dsa.gui.lf.LookAndFeels;
import dsa.model.characters.Group;
import dsa.model.characters.GroupOptions;
import dsa.util.Directories;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public class OptionsDialog extends BGDialog {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JButton jButton = null;

  private JButton jButton1 = null;

  private JTabbedPane jTabbedPane = null;

  private JPanel jPanel1 = null;

  private JCheckBox fgBox = null;

  private JLabel jLabel = null;

  private JComboBox lfBox = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JTextField skinField = null;

  private JButton skinButton = null;

  private JPanel rulesPanel = null;

  private JCheckBox aeBox = null;

  private JCheckBox fullStepBox = null;

  private JCheckBox leftHandBox = null;

  private JPanel qvatPanel = null;

  private JCheckBox paBasisBox = null;

  private JCheckBox koBox = null;

  private JCheckBox markersBox = null;

  private JCheckBox heavyClothesBox = null;

  private JCheckBox twohwBox = null;

  /**
   * This method initializes
   * 
   */
  public OptionsDialog(JFrame owner) {
    super(owner);
    initialize();
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new java.awt.Dimension(328, 257));
    this.setContentPane(getJContentPane());
    this.setTitle("Optionen");
    updateData();
    onLFSelected();
  }

  private void onLFSelected() {
    String lf = lfBox.getSelectedItem().toString();
    if (lf.startsWith("Skin")) {
      skinField.setEnabled(true);
      skinButton.setEnabled(true);
    }
    else {
      skinField.setEnabled(false);
      skinButton.setEnabled(false);
    }
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
      jContentPane.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
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
      jPanel.setPreferredSize(new java.awt.Dimension(300, 40));
      jPanel.setLayout(null);
      jPanel.add(getOKButton(), null);
      jPanel.add(getCancelButton(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getOKButton() {
    if (jButton == null) {
      jButton = new JButton();
      jButton.setText("OK");
      jButton.setBounds(new java.awt.Rectangle(49, 10, 100, 20));
      jButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (savePreferences()) {
            dispose();
          }
        }
      });
    }
    return jButton;
  }

  protected boolean savePreferences() {
    try {
      String lf = lfBox.getSelectedItem().toString();
      if (lf.startsWith("Skin")) {
        String name = skinField.getText();
        if (name == null || name.equals("")) {
          javax.swing.JOptionPane.showMessageDialog(this,
              "Bitte ein Skin auswählen.", "Fehler",
              javax.swing.JOptionPane.ERROR_MESSAGE);
          return false;
        }
        LookAndFeels.setLookAndFeel(lf, name);
      }
      else {
        LookAndFeels.setLookAndFeel(lf);
      }
      Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
      prefs.putBoolean("BringWindowsToTop", fgBox.isSelected());

      GroupOptions options = Group.getInstance().getOptions();
      boolean changed = false;
      changed = changed
          || leftHandBox.isSelected() != options.isEarlyTwoHanded();
      changed = changed
          || aeBox.isSelected() != options.hasFastAERegeneration();
      changed = changed
          || fullStepBox.isSelected() != options.hasFullFirstStep();
      changed = changed || markersBox.isSelected() != options.hasQvatMarkers();
      changed = changed || paBasisBox.isSelected() != options.hasQvatPABasis();
      changed = changed || koBox.isSelected() != options.hasQvatStunned();
      changed = changed
          || heavyClothesBox.isSelected() == options.hasHeavyClothes();
      changed = changed || twohwBox.isSelected() != options.hasHard2HWeapons();
      if (!changed) return true;
      String[] values = { "Diese Gruppe", "Neu erstellte Gruppen",
          "Diese Gruppe und neu erstellte Gruppen" };
      Object result = JOptionPane.showInputDialog(this,
          "Die Regeln speichern für ...", "Optionen",
          JOptionPane.QUESTION_MESSAGE, null, values, values[0]);
      if (result == values[0] || result == values[2]) {
        options.setEarlyTwoHanded(leftHandBox.isSelected());
        options.setFastAERegeneration(aeBox.isSelected());
        options.setFullFirstStep(fullStepBox.isSelected());
        options.setQvatMarkers(markersBox.isSelected());
        options.setQvatPABasis(paBasisBox.isSelected());
        options.setQvatStunned(koBox.isSelected());
        options.setHeavyClothes(!heavyClothesBox.isSelected());
        options.setHard2HWeapons(twohwBox.isSelected());
        dsa.gui.util.OptionsChange.fireOptionsChanged();
      }
      if (result == values[1] || result == values[2]) {
        prefs.putBoolean("EarlyLeftHanded", leftHandBox.isSelected());
        prefs.putBoolean("HighAERegeneration", aeBox.isSelected());
        prefs.putBoolean("QvatUseKO", koBox.isSelected());
        prefs.putBoolean("HeavyClothes", !heavyClothesBox.isSelected());
        prefs.putBoolean("Hard2HWeapons", twohwBox.isSelected());
        Preferences.userNodeForPackage(Markers.class).putBoolean(
            "QvatUseMarkers", markersBox.isSelected());
        prefs = Preferences.userRoot().node("dsa/data/impl");
        prefs.putBoolean("FullFirstStep", fullStepBox.isSelected());
        prefs.putBoolean("QvatPaBasis", paBasisBox.isSelected());
      }
      return result != null;
    }
    catch (Exception e) {
      e.printStackTrace();
      javax.swing.JOptionPane.showMessageDialog(this,
          "Das Look & Feel konnte nicht gewählt werden:\n" + e.getMessage(),
          "Fehler", javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void updateData() {
    GroupOptions options = Group.getInstance().getOptions();
    Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
    fgBox.setSelected(prefs.getBoolean("BringWindowsToTop", true));
    leftHandBox.setSelected(options.isEarlyTwoHanded());
    aeBox.setSelected(options.hasFastAERegeneration());
    koBox.setSelected(options.hasQvatStunned());
    markersBox.setSelected(options.hasQvatMarkers());
    fullStepBox.setSelected(options.hasFullFirstStep());
    paBasisBox.setSelected(options.hasQvatPABasis());
    heavyClothesBox.setSelected(!options.hasHeavyClothes());
    twohwBox.setSelected(options.hasHard2HWeapons());
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getCancelButton() {
    if (jButton1 == null) {
      jButton1 = new JButton();
      jButton1.setBounds(new java.awt.Rectangle(175, 10, 100, 20));
      jButton1.setText("Abbrechen");
      jButton1.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dispose();
        }
      });
    }
    return jButton1;
  }

  /**
   * This method initializes jTabbedPane
   * 
   * @return javax.swing.JTabbedPane
   */
  private JTabbedPane getJTabbedPane() {
    if (jTabbedPane == null) {
      jTabbedPane = new JTabbedPane();
      jTabbedPane.addTab("Hausregeln", null, getRulesPanel(), null);
      jTabbedPane.addTab("QVAT", null, getQvatPanel(), null);
      jTabbedPane.addTab("Aussehen", null, getJPanel1(), null);
    }
    return jTabbedPane;
  }

  /**
   * This method initializes jPanel1
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jLabel2 = new JLabel();
      jLabel2.setBounds(new java.awt.Rectangle(15, 37, 90, 15));
      jLabel2.setText("Skin:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new java.awt.Rectangle(15, 59, 203, 15));
      jLabel1.setText("(erfordert Programmneustart)");
      jLabel = new JLabel();
      jLabel.setBounds(new java.awt.Rectangle(14, 11, 91, 15));
      jLabel.setText("Look & Feel:");
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.add(getFGBox(), null);
      jPanel1.add(jLabel, null);
      jPanel1.add(getLFCombo(), null);
      jPanel1.add(jLabel1, null);
      jPanel1.add(jLabel2, null);
      jPanel1.add(getSkinField(), null);
      jPanel1.add(getSkinButton(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getFGBox() {
    if (fgBox == null) {
      fgBox = new JCheckBox();
      fgBox.setBounds(new java.awt.Rectangle(10, 80, 268, 21));
      fgBox.setText("Fenster in den Vordergrund bringen");
    }
    return fgBox;
  }

  /**
   * This method initializes jComboBox
   * 
   * @return javax.swing.JComboBox
   */
  private JComboBox getLFCombo() {
    if (lfBox == null) {
      lfBox = new JComboBox();
      lfBox.setBounds(new java.awt.Rectangle(116, 8, 183, 20));
      for (String name : LookAndFeels.getLookAndFeels()) {
        lfBox.addItem(name);
      }
      lfBox.setSelectedItem(LookAndFeels.getCurrentLookAndFeel());
      lfBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          onLFSelected();
        }
      });
    }
    return lfBox;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getSkinField() {
    if (skinField == null) {
      skinField = new JTextField();
      skinField.setBounds(new java.awt.Rectangle(116, 34, 141, 20));
      String themePack = LookAndFeels.getLastThemePack();
      File f = new File(themePack);
      if (f.exists()) {
        skinField.setText(themePack);
      }
    }
    return skinField;
  }

  /**
   * This method initializes jButton2
   * 
   * @return javax.swing.JButton
   */
  private JButton getSkinButton() {
    if (skinButton == null) {
      skinButton = new JButton();
      skinButton.setBounds(new java.awt.Rectangle(268, 34, 30, 20));
      skinButton.setText("...");
      skinButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectSkin();
        }
      });
    }
    return skinButton;
  }

  protected void selectSkin() {
    JFileChooser chooser = new JFileChooser();
    File f = Directories.getLastUsedDirectory(this, "Skins");
    if (f != null) {
      chooser.setCurrentDirectory(f);
    }
    else {
      File f2 = new File("skins");
      if (f2.exists() && f2.isDirectory()) {
        chooser.setCurrentDirectory(f2);
      }
    }
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.getName().endsWith(".zip") || f.isDirectory();
      }

      public String getDescription() {
        return "Skins (*.zip)";
      }
    });
    chooser.setMultiSelectionEnabled(false);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Directories
          .setLastUsedDirectory(this, "Skins", chooser.getSelectedFile());
      skinField.setText(chooser.getSelectedFile().getAbsolutePath());
    }

  }

  /**
   * This method initializes jPanel2
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getRulesPanel() {
    if (rulesPanel == null) {
      rulesPanel = new JPanel();
      rulesPanel.setLayout(null);
      rulesPanel.add(getAEBox(), null);
      rulesPanel.add(getFullStepBox(), null);
      rulesPanel.add(getLeftHandBox(), null);
      rulesPanel.add(getHeavyClothesBox(), null);
      rulesPanel.add(getTwoHWBox(), null);
    }
    return rulesPanel;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getAEBox() {
    if (aeBox == null) {
      aeBox = new JCheckBox();
      aeBox.setBounds(new java.awt.Rectangle(10, 10, 201, 21));
      aeBox.setText("schnellere AE-Regeneration");
    }
    return aeBox;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getFullStepBox() {
    if (fullStepBox == null) {
      fullStepBox = new JCheckBox();
      fullStepBox.setBounds(new java.awt.Rectangle(10, 40, 251, 21));
      fullStepBox.setText("volle Steigerung von Stufe 0 auf 1");
    }
    return fullStepBox;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getLeftHandBox() {
    if (leftHandBox == null) {
      leftHandBox = new JCheckBox();
      leftHandBox.setBounds(new java.awt.Rectangle(10, 100, 251, 21));
      leftHandBox.setText("früher beidhändiger Kampf");
    }
    return leftHandBox;
  }

  /**
   * This method initializes jPanel2
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getQvatPanel() {
    if (qvatPanel == null) {
      qvatPanel = new JPanel();
      qvatPanel.setLayout(null);
      qvatPanel.add(getPABasisBox(), null);
      qvatPanel.add(getKOBox(), null);
      qvatPanel.add(getMarkersBox(), null);
    }
    return qvatPanel;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getPABasisBox() {
    if (paBasisBox == null) {
      paBasisBox = new JCheckBox();
      paBasisBox.setBounds(new java.awt.Rectangle(10, 10, 221, 21));
      paBasisBox.setText("PA-Basis als MU/IN/GE");
    }
    return paBasisBox;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getKOBox() {
    if (koBox == null) {
      koBox = new JCheckBox();
      koBox.setBounds(new java.awt.Rectangle(10, 40, 261, 21));
      koBox.setText("Mit KO-Proben / Benommenheit");
    }
    return koBox;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getMarkersBox() {
    if (markersBox == null) {
      markersBox = new JCheckBox();
      markersBox.setBounds(new java.awt.Rectangle(10, 70, 231, 21));
      markersBox.setText("Mit Markern");
    }
    return markersBox;
  }

  /**
   * This method initializes jCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getHeavyClothesBox() {
    if (heavyClothesBox == null) {
      heavyClothesBox = new JCheckBox();
      heavyClothesBox.setBounds(new java.awt.Rectangle(10, 70, 251, 21));
      heavyClothesBox.setText("Kleidung ohne Gewicht und BE");
    }
    return heavyClothesBox;
  }

  /**
   * This method initializes jCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getTwoHWBox() {
    if (twohwBox == null) {
      twohwBox = new JCheckBox();
      twohwBox.setBounds(new java.awt.Rectangle(10, 130, 291, 21));
      twohwBox.setText("1W mehr Schaden bei Zweihandwaffen");
    }
    return twohwBox;
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

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
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JRadioButton;

public final class OptionsDialog extends BGDialog {

  private static final class ZipFileFilter extends FileFilter {
    public boolean accept(File f) {
      return f.getName().endsWith(".zip") || f.isDirectory();
    }

    public String getDescription() {
      return "Skins (*.zip)";
    }
  }

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JButton jButton = null;

  private JButton jButton1 = null;

  private JTabbedPane jTabbedPane = null;

  private JPanel uiPanel = null;

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
  
  private JCheckBox wvBox = null;

  private JComboBox clothesBEBox = null;

  private JCheckBox twohwBox = null;

  private JPanel programPanel = null;

  private JCheckBox versionCheckBox = null;

  private JLabel jLabel3 = null;

  private JRadioButton dataHomeDirButton = null;

  private JRadioButton dataProgramDirButton = null;

  private JRadioButton dataCustomDirButton = null;

  private JTextField dataCustomDirBox = null;

  private JButton selDataDirButton = null;
  
  private JCheckBox useHitZonesBox = null;

  /**
   * This method initializes
   * 
   */
  public OptionsDialog(JFrame owner) {
    super(owner);
    initialize();
  }
  
  public final String getHelpPage() {
    return "Optionen";
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(375, 257));
    this.setContentPane(getJContentPane());
    this.setTitle("Optionen");
    updateData();
    onLFSelected();
    this.getRootPane().setDefaultButton(getOKButton());
    setEscapeButton(getCancelButton());
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
    
    int dataOption = 0;
    if (dataProgramDirButton.isSelected()) {
      dataOption = 1;
    }
    else if (dataCustomDirButton.isSelected()) {
      String dataCustomDir = dataCustomDirBox.getText();
      File test = new File(dataCustomDir);
      if (!test.exists() || !test.isDirectory()) {
        javax.swing.JOptionPane.showMessageDialog(this,
            "Bitte ein gültiges Verzeichnis (oder eine andere Option) wählen", "Fehler",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
      }
      prefs.put("CustomDataDir", dataCustomDir);
      dataOption = 2;
    }
    prefs.putInt("CustomDataDirOption", dataOption);
    prefs.putBoolean("BringWindowsToTop", fgBox.isSelected());
    prefs.putBoolean("VersionCheckAtStart", versionCheckBox.isSelected());
    prefs.putBoolean("PlayOnlineMessageSound", onlineSoundBox.isSelected());
    prefs.put("OnlineConnectCommand", getOnlineConnectCommandField().getText());

    GroupOptions options = Group.getInstance().getOptions();
    boolean changed = false;
    changed = changed
        || leftHandBox.isSelected() != options.isEarlyTwoHanded();
    changed = changed
        || aeBox.isSelected() != options.hasFastAERegeneration();
    changed = changed
        || fullStepBox.isSelected() != options.hasFullFirstStep();
    changed = changed || markersBox.isSelected() != options.hasQvatMarkers();
    changed = changed || wvBox.isSelected() != options.qvatUseWV();
    changed = changed || paBasisBox.isSelected() != options.hasQvatPABasis();
    changed = changed || koBox.isSelected() != options.hasQvatStunned();
    changed = changed || useHitZonesBox.isSelected() != options.useHitZones();
    changed = changed
        || clothesBEBox.getSelectedIndex() != options.getClothesBE().ordinal();
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
      options.setQvatUseWV(wvBox.isSelected());
      options.setUseHitZones(useHitZonesBox.isSelected());
      options.setQvatPABasis(paBasisBox.isSelected());
      options.setQvatStunned(koBox.isSelected());
      options.setClothesBE(GroupOptions.ClothesBE.values()[clothesBEBox.getSelectedIndex()]);
      options.setHard2HWeapons(twohwBox.isSelected());
      dsa.gui.util.OptionsChange.fireOptionsChanged();
    }
    if (result == values[1] || result == values[2]) {
      prefs.putBoolean("EarlyLeftHanded", leftHandBox.isSelected());
      prefs.putBoolean("HighAERegeneration", aeBox.isSelected());
      prefs.putBoolean("QvatUseKO", koBox.isSelected());
      prefs.putBoolean("QvatUseWV", wvBox.isSelected());
      prefs.putBoolean("UseHitZones", useHitZonesBox.isSelected());
      prefs.putBoolean("HeavyClothes", clothesBEBox.getSelectedIndex() == GroupOptions.ClothesBE.Normal.ordinal());
      prefs.putBoolean("LowerClothesBE", clothesBEBox.getSelectedIndex() == GroupOptions.ClothesBE.Lower.ordinal());
      prefs.putBoolean("Hard2HWeapons", twohwBox.isSelected());
      Preferences.userNodeForPackage(Markers.class).putBoolean(
          "QvatUseMarkers", markersBox.isSelected());
      prefs = Preferences.userRoot().node("dsa/data/impl");
      prefs.putBoolean("FullFirstStep", fullStepBox.isSelected());
      prefs.putBoolean("QvatPaBasis", paBasisBox.isSelected());
    }
    return result != null;
  }

  private void updateData() {
    GroupOptions options = Group.getInstance().getOptions();
    Preferences prefs = Preferences.userNodeForPackage(PackageID.class);
    fgBox.setSelected(prefs.getBoolean("BringWindowsToTop", true));
    onlineSoundBox.setSelected(prefs.getBoolean("PlayOnlineMessageSound", true));
    getOnlineConnectCommandField().setText(prefs.get("OnlineConnectCommand", ""));
    leftHandBox.setSelected(options.isEarlyTwoHanded());
    aeBox.setSelected(options.hasFastAERegeneration());
    koBox.setSelected(options.hasQvatStunned());
    markersBox.setSelected(options.hasQvatMarkers());
    wvBox.setSelected(options.qvatUseWV());
    useHitZonesBox.setSelected(options.useHitZones());
    fullStepBox.setSelected(options.hasFullFirstStep());
    paBasisBox.setSelected(options.hasQvatPABasis());
    clothesBEBox.setSelectedIndex(options.getClothesBE().ordinal());
    twohwBox.setSelected(options.hasHard2HWeapons());
    versionCheckBox.setSelected(prefs.getBoolean("VersionCheckAtStart", true));

    int dataDirOpt = prefs.getInt("CustomDataDirOption", -1);
    if (dataDirOpt == -1) dataDirOpt = 0; // wasn't set yet
    if (dataDirOpt == 0) {
      dataHomeDirButton.setSelected(true);
      dataProgramDirButton.setSelected(false);
      dataCustomDirButton.setSelected(false);
      dataCustomDirBox.setEnabled(false);
    }
    else if (dataDirOpt == 1) {
      dataHomeDirButton.setSelected(false);
      dataProgramDirButton.setSelected(true);
      dataCustomDirButton.setSelected(false);
      dataCustomDirBox.setEnabled(false);      
    }
    else {
      dataHomeDirButton.setSelected(false);
      dataProgramDirButton.setSelected(false);
      dataCustomDirButton.setSelected(true);
      dataCustomDirBox.setEnabled(true);  
    }
    dataCustomDirBox.setText(prefs.get("CustomDataDir", ""));
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
      jTabbedPane.addTab("Aussehen", null, getUIPanel(), null);
      jTabbedPane.addTab("Online", null, getOnlinePanel(), null);
      jTabbedPane.addTab("Programm", null, getProgramPanel(), null);
    }
    return jTabbedPane;
  }

  /**
   * This method initializes jPanel1
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getUIPanel() {
    if (uiPanel == null) {
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(10, 40, 96, 21));
      jLabel2.setText("Skin:");
      jLabel1 = new JLabel();
      jLabel1.setBounds(new Rectangle(10, 70, 203, 15));
      jLabel1.setText("(erfordert Programmneustart)");
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(10, 10, 97, 21));
      jLabel.setText("Look & Feel:");
      uiPanel = new JPanel();
      uiPanel.setLayout(null);
      uiPanel.add(getFGBox(), null);
      uiPanel.add(jLabel, null);
      uiPanel.add(getLFCombo(), null);
      uiPanel.add(jLabel1, null);
      uiPanel.add(jLabel2, null);
      uiPanel.add(getSkinField(), null);
      uiPanel.add(getSkinButton(), null);
    }
    return uiPanel;
  }
  
  private JPanel getOnlinePanel() {
	  JPanel onlinePanel = new JPanel();
	  onlinePanel.setLayout(null);
	  onlinePanel.add(getOnlineSoundBox(), null);
	  onlinePanel.add(getOnlineConnectCommandField(), null);
	  JLabel label1 = new JLabel("Beim Verbinden ausführen:");
	  label1.setBounds(10, 40, 268, 21);
	  onlinePanel.add(label1);
	  JLabel label2 = new JLabel("(Verwende %HOST% als Platzhalter)");
	  label2.setBounds(10, 80, 268, 21);
	  onlinePanel.add(label2);
	  return onlinePanel;
  }
  
  private JCheckBox onlineSoundBox;
  
  private JCheckBox getOnlineSoundBox() {
	  if (onlineSoundBox == null) {
		  onlineSoundBox = new JCheckBox();
		  onlineSoundBox.setBounds(new Rectangle(10, 10, 268, 21));
		  onlineSoundBox.setText("Sound bei Nachrichten");
	  }
	  return onlineSoundBox;
  }
  
  private JTextField onlineConnectCommandField;
  
  private JTextField getOnlineConnectCommandField() {
	  if (onlineConnectCommandField == null) {
		  onlineConnectCommandField = new JTextField();
		  onlineConnectCommandField.setBounds(new Rectangle(10, 60, 268, 21));
	  }
	  return onlineConnectCommandField;
  }

  /**
   * This method initializes jCheckBox
   * 
   * @return javax.swing.JCheckBox
   */
  private JCheckBox getFGBox() {
    if (fgBox == null) {
      fgBox = new JCheckBox();
      fgBox.setBounds(new Rectangle(10, 90, 268, 21));
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
      lfBox.setBounds(new Rectangle(110, 10, 181, 20));
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
      skinField.setBounds(new Rectangle(110, 40, 181, 20));
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
      skinButton.setBounds(new Rectangle(300, 40, 30, 20));
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
    chooser.addChoosableFileFilter(new ZipFileFilter());
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
      JLabel clothesLabel = new JLabel("Behinderung durch Kleidung:");
      clothesLabel.setBounds(10, 70, 170, 21);
      rulesPanel.add(clothesLabel, null);
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
      qvatPanel.add(getWVBox(), null);
      qvatPanel.add(getUseHitZonesBox(), null);
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
  
  private JCheckBox getWVBox() {
    if (wvBox == null) {
      wvBox = new JCheckBox();
      wvBox.setBounds(new java.awt.Rectangle(10, 100, 261, 21));
      wvBox.setText("Benutze WaffenVergleich");
    }
    return wvBox;
  }
  
  private JCheckBox getUseHitZonesBox() {
	  if (useHitZonesBox == null) {
		 useHitZonesBox = new JCheckBox();
		 useHitZonesBox.setBounds(new java.awt.Rectangle(10, 130, 261, 21));
		 useHitZonesBox.setText("Trefferzonen auswürfeln");
	  }
	  return useHitZonesBox;
  }

  /**
   * This method initializes jCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JComboBox getHeavyClothesBox() {
    if (clothesBEBox == null) {
    	clothesBEBox = new JComboBox();
    	clothesBEBox.setBounds(new java.awt.Rectangle(180, 70, 151, 21));
    	clothesBEBox.addItem("Normal");
    	clothesBEBox.addItem("BE 1 niedriger");
    	clothesBEBox.addItem("BE von Einzelkleidung");
    }
    return clothesBEBox;
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

  /**
   * This method initializes programPanel	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getProgramPanel() {
    if (programPanel == null) {
      jLabel3 = new JLabel();
      jLabel3.setBounds(new Rectangle(10, 40, 271, 21));
      jLabel3.setText("Selbstdefinierte Daten speichern in:");
      programPanel = new JPanel();
      programPanel.setLayout(null);
      programPanel.add(getVersionCheckBox(), null);
      programPanel.add(jLabel3, null);
      programPanel.add(getDataHomeDirButton(), null);
      programPanel.add(getDataProgramDirButton(), null);
      programPanel.add(getDataCustomDirButton(), null);
      programPanel.add(getDataCustomDirBox(), null);
      programPanel.add(getSelDataDirButton(), null);
    }
    return programPanel;
  }

  /**
   * This method initializes versionCheckBox	
   * 	
   * @return javax.swing.JCheckBox	
   */
  private JCheckBox getVersionCheckBox() {
    if (versionCheckBox == null) {
      versionCheckBox = new JCheckBox();
      versionCheckBox.setBounds(new Rectangle(10, 10, 251, 21));
      versionCheckBox.setText("Beim Start auf neue Version prüfen");
    }
    return versionCheckBox;
  }
  
  private void updateDataButtonState(int sel) {
    if (sel != 0) dataHomeDirButton.setSelected(false);
    if (sel != 1) dataProgramDirButton.setSelected(false);
    if (sel != 2) dataCustomDirButton.setSelected(false);
    dataCustomDirBox.setEnabled(sel == 2);
  }

  /**
   * This method initializes dataHomeDirButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getDataHomeDirButton() {
    if (dataHomeDirButton == null) {
      dataHomeDirButton = new JRadioButton();
      dataHomeDirButton.setBounds(new Rectangle(10, 70, 261, 21));
      dataHomeDirButton.setText("Benutzer-Stammverzeichnis");
      dataHomeDirButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateDataButtonState(0);
        }
      });
    }
    return dataHomeDirButton;
  }

  /**
   * This method initializes dataProgramDirButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getDataProgramDirButton() {
    if (dataProgramDirButton == null) {
      dataProgramDirButton = new JRadioButton();
      dataProgramDirButton.setBounds(new Rectangle(10, 100, 181, 21));
      dataProgramDirButton.setText("Programmverzeichnis");
      dataProgramDirButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateDataButtonState(1);
        }
      });
    }
    return dataProgramDirButton;
  }

  /**
   * This method initializes dataCustomDirButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getDataCustomDirButton() {
    if (dataCustomDirButton == null) {
      dataCustomDirButton = new JRadioButton();
      dataCustomDirButton.setBounds(new Rectangle(10, 131, 21, 20));
      dataCustomDirButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateDataButtonState(2);
        }
      });
    }
    return dataCustomDirButton;
  }

  /**
   * This method initializes dataCustomDirBox	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getDataCustomDirBox() {
    if (dataCustomDirBox == null) {
      dataCustomDirBox = new JTextField();
      dataCustomDirBox.setBounds(new Rectangle(30, 130, 251, 21));
    }
    return dataCustomDirBox;
  }

  /**
   * This method initializes selDataDirButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getSelDataDirButton() {
    if (selDataDirButton == null) {
      selDataDirButton = new JButton();
      selDataDirButton.setBounds(new Rectangle(290, 130, 31, 21));
      selDataDirButton.setText("...");
      selDataDirButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectDataCustomDir();
        }
      });
    }
    return selDataDirButton;
  }
  
  private void selectDataCustomDir() {
    JFileChooser chooser = new JFileChooser();
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      dataCustomDirBox.setText(chooser.getSelectedFile().getAbsolutePath());
      dataCustomDirButton.setSelected(true);
      updateDataButtonState(2);
    }
  }

} //  @jve:decl-index=0:visual-constraint="10,10"

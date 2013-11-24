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

import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.jdic.desktop.Desktop;

import dsa.gui.lf.BGDialog;
import dsa.gui.util.ExampleFileFilter;
import dsa.model.characters.Hero;
import dsa.util.Directories;
import dsa.util.FileType;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.Point;

/**
 * 
 */
public final class PrintingDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JTextField templateField = null;

  private JButton templateButton = null;

  private JLabel jLabel1 = null;

  private JTextField outputField = null;

  private JButton outputButton = null;

  private JButton createButton = null;

  private JButton displayButton = null;

  private JButton closeButton = null;

  private Hero hero;

  private JButton fightTalentButton = null;

  private JLabel jLabel2 = null;

  private JComboBox fileTypeBox = null;

  private JLabel jLabel = null;

  private JPanel jPanel = null;

  private JPanel jPanel1 = null;

  private JLabel jLabel3 = null;

  private JSpinner zfwSpinner = null;

  /**
   * This is the default constructor -- do not use!
   */
  public PrintingDialog() {
    super();
    initialize();
  }

  public PrintingDialog(Hero character, java.awt.Frame parent) {
    super(parent);
    hero = character;
    initialize();
    getTemplateField().setText(hero.getPrintingTemplateFile());
    getOutputField().setText(hero.getPrintFile());
    for (FileType ft : FileType.values()) {
      getFileTypeBox().addItem(ft.getDescription());
    }
    getFileTypeBox().setSelectedIndex(hero.getPrintingFileType().ordinal());
    getZfwSpinner().setValue(hero.getPrintingZFW());
    this.setLocationRelativeTo(parent);
    this.setTitle("Drucken: " + hero.getName());
    updateButtons();
  }
  
  public final String getHelpPage() {
    return "Drucken";
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Drucken");
    this.setModal(true);
    this.setSize(435, 364);
    this.setContentPane(getJContentPane());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel = new JLabel();
      jLabel.setBounds(new Rectangle(22, 65, 38, 15));
      jLabel.setText("Datei:");
      jLabel2 = new JLabel();
      jLabel2.setBounds(new Rectangle(22, 33, 38, 15));
      jLabel2.setText("Typ:");
      jLabel1 = new JLabel();
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(null);
      jLabel1.setBounds(22, 154, 83, 17);
      jLabel1.setText("Datei:");
      jContentPane.add(getTemplateField(), null);
      jContentPane.add(getTemplateButton(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getOutputField(), null);
      jContentPane.add(getOutputButton(), null);
      jContentPane.add(getCreateButton(), null);
      jContentPane.add(getDisplayButton(), null);
      jContentPane.add(getCloseButton(), null);
      jContentPane.add(getFightTalentButton(), null);
      jContentPane.add(jLabel2, null);
      jContentPane.add(getFileTypeBox(), null);
      jContentPane.add(jLabel, null);
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getJPanel1(), null);
    }
    return jContentPane;
  }

  class MyTextFieldListener implements DocumentListener {

    public void insertUpdate(DocumentEvent e) {
      updateButtons();
    }

    public void removeUpdate(DocumentEvent e) {
      updateButtons();
    }

    public void changedUpdate(DocumentEvent e) {
      updateButtons();
    }
  }

  private void updateButtons() {
    boolean hasTemplate = getTemplateField().getDocument().getLength() > 0;
    boolean hasOutput = getOutputField().getDocument().getLength() > 0;
    getCreateButton().setEnabled(hasTemplate && hasOutput);
    getDisplayButton().setEnabled(hasOutput);
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getTemplateField() {
    if (templateField == null) {
      templateField = new JTextField();
      templateField.setBounds(22, 88, 342, 20);
      templateField.setName("templateFiled");
      templateField.getDocument().addDocumentListener(new MyTextFieldListener());
    }
    return templateField;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getTemplateButton() {
    if (templateButton == null) {
      templateButton = new JButton();
      templateButton.setBounds(374, 88, 31, 22);
      templateButton.setText("...");
      templateButton.setName("templateButton");
      templateButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectTemplate();
        }
      });
    }
    return templateButton;
  }

  /**
   * 
   * 
   */
  protected void selectTemplate() {
    JFileChooser chooser = null;
    if (getTemplateField().getDocument().getLength() > 0) {
      chooser = new JFileChooser(new File(getTemplateField().getText()));
    }
    else {
      File f = Directories.getLastUsedDirectory(this, "PrintingTemplates");
      if (f != null) {
        chooser = new JFileChooser(f);
      }
      else
        chooser = new JFileChooser();
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setMultiSelectionEnabled(false);
    FileType ft = FileType.values()[getFileTypeBox().getSelectedIndex()];
    ExampleFileFilter fileFilter = new ExampleFileFilter();
    fileFilter.addExtension(ft.getExtension());
    fileFilter.setDescription(ft.getDescription());
    chooser.addChoosableFileFilter(fileFilter);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      getTemplateField().setText(chooser.getSelectedFile().getAbsolutePath());
      Directories.setLastUsedDirectory(this, "PrintingTemplates", chooser
          .getSelectedFile());
    }
  }

  /**
   * This method initializes jTextField1
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getOutputField() {
    if (outputField == null) {
      outputField = new JTextField();
      outputField.setBounds(22, 176, 342, 20);
      outputField.setName("outputField");
      outputField.getDocument().addDocumentListener(new MyTextFieldListener());
    }
    return outputField;
  }

  /**
   * This method initializes jButton1
   * 
   * @return javax.swing.JButton
   */
  private JButton getOutputButton() {
    if (outputButton == null) {
      outputButton = new JButton();
      outputButton.setBounds(374, 176, 31, 22);
      outputButton.setText("...");
      outputButton.setName("outputFileButton");
      outputButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectTarget();
        }
      });
    }
    return outputButton;
  }

  /**
   * 
   * 
   */
  protected void selectTarget() {
    JFileChooser chooser = null;
    if (getOutputField().getDocument().getLength() > 0) {
      chooser = new JFileChooser(new File(getOutputField().getText()));
    }
    else {
      File f = Directories.getLastUsedDirectory(this, "HeroDocuments");
      if (f != null)
        chooser = new JFileChooser(f);
      else
        chooser = new JFileChooser();
    }
    chooser.setAcceptAllFileFilterUsed(true);
    dsa.gui.util.ExampleFileFilter filter = new ExampleFileFilter();
    FileType ft = FileType.values()[getFileTypeBox().getSelectedIndex()];
    filter.addExtension(ft.getExtension());
    filter.setDescription(ft.getDescription());
    chooser.addChoosableFileFilter(filter);
    chooser.setMultiSelectionEnabled(false);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      getOutputField().setText(chooser.getSelectedFile().getAbsolutePath());
      Directories.setLastUsedDirectory(this, "HeroDocuments", chooser
          .getSelectedFile());
    }
  }

  /**
   * This method initializes jButton3
   * 
   * @return javax.swing.JButton
   */
  private JButton getCreateButton() {
    if (createButton == null) {
      createButton = new JButton();
      createButton.setBounds(10, 300, 100, 22);
      createButton.setText("Erstellen");
      createButton.setName("createButton");
      createButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          transform();
        }
      });
    }
    return createButton;
  }

  class TransformerHelper implements Runnable {

    private final File input;

    private final File output;
    
    private final FileType fileType;

    private int task = 0;

    private String errorMsg;
    
    public TransformerHelper(File input, File output, FileType fileType) {
      this.input = input;
      this.output = output;
      this.fileType = fileType;
    }

    public void run() {
      if (task == 0) {
        try {
          dsa.control.CharacterPrinter printer = dsa.control.CharacterPrinter
              .getInstance();
          printer.printCharacter(hero, input, output, fileType, PrintingDialog.this,
              "Datei wird erstellt");
          hero.setPrintingTemplateFile(input.getCanonicalPath());
          task = 1;
        }
        catch (java.io.IOException e) {
          task = 2;
          errorMsg = e.getMessage();
        }
        SwingUtilities.invokeLater(this);
      }
      else if (task == 1) {
        hero.setPrintFile(getOutputField().getText());
        hero.setPrintingFileType(fileType);
        JOptionPane.showMessageDialog(PrintingDialog.this,
            "Ausgabedatei erfolgreich erstellt!", "Drucken",
            JOptionPane.INFORMATION_MESSAGE);
        getDisplayButton().setEnabled(true);
      }
      else {
        JOptionPane.showMessageDialog(PrintingDialog.this,
            "Fehler beim Erstellen der Ausgabedatei:\n" + errorMsg, "Fehler",
            JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  /**
   * 
   * 
   */
  protected void transform() {
    File input = new File(getTemplateField().getText());
    FileType ft = FileType.values()[getFileTypeBox().getSelectedIndex()];
    String outputFile = getOutputField().getText();
    if (!outputFile.endsWith("." + ft.getExtension())) {
      outputFile += "." + ft.getExtension();
    }
    getOutputField().setText(outputFile);
    File output = new File(outputFile);
    if (output.exists()) {
      if (JOptionPane.showConfirmDialog(PrintingDialog.this,
          "Datei existiert und wird überschrieben.\nFortfahren?", "Drucken",
          JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }
    hero.setPrintingZFW(((Number)getZfwSpinner().getValue()).intValue());
    TransformerHelper helper = new TransformerHelper(input, output, ft);
    getDisplayButton().setEnabled(false);
    (new Thread(helper)).start();
  }

  /**
   * This method initializes jButton4
   * 
   * @return javax.swing.JButton
   */
  private JButton getDisplayButton() {
    if (displayButton == null) {
      displayButton = new JButton();
      displayButton.setBounds(131, 300, 100, 22);
      displayButton.setText("Anzeigen");
      displayButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          PrintingDialog.this.displayTransformed();
        }
      });
    }
    return displayButton;
  }

  /**
   * 
   * 
   */
  protected void displayTransformed() {
    try {
      Desktop.open(new File(getOutputField().getText()));
    }
    catch (org.jdesktop.jdic.desktop.DesktopException e) {
      JOptionPane.showMessageDialog(this, "Auruf des Viewers fehlgeschlagen:\n"
          + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * This method initializes jButton5
   * 
   * @return javax.swing.JButton
   */
  private JButton getCloseButton() {
    if (closeButton == null) {
      closeButton = new JButton();
      closeButton.setBounds(252, 300, 100, 22);
      closeButton.setText("Schließen");
      closeButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          PrintingDialog.this.dispose();
        }
      });
    }
    return closeButton;
  }

  /**
   * This method initializes jButton6
   * 
   * @return javax.swing.JButton
   */
  private JButton getFightTalentButton() {
    if (fightTalentButton == null) {
      fightTalentButton = new JButton();
      fightTalentButton.setText("Kampftalente auswählen ...");
      fightTalentButton.setSize(new Dimension(208, 22));
      fightTalentButton.setLocation(new Point(22, 249));
      fightTalentButton.setEnabled(hero != null);
      fightTalentButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectFightingTalents();
        }
      });
    }
    return fightTalentButton;
  }

  protected void selectFightingTalents() {
    java.util.List<String> talents = hero.getFightingTalentsInDocument();
    FightingTalentsSelector dialog = new FightingTalentsSelector(this, talents);
    dialog.setVisible(true);
    if (dialog.closedByOK()) {
      hero.setFightingTalentsInDocument(dialog.getSelectedTalents());
    }
  }

  /**
   * This method initializes fileTypeBox	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getFileTypeBox() {
    if (fileTypeBox == null) {
      fileTypeBox = new JComboBox();
      fileTypeBox.setBounds(new Rectangle(66, 33, 177, 21));
    }
    return fileTypeBox;
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
      jPanel.setBounds(new Rectangle(11, 10, 408, 112));
      jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Vorlage", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
      jLabel3 = new JLabel();
      jLabel3.setText("Zauber drucken ab einem ZFW von:");
      jLabel3.setLocation(new Point(12, 80));
      jLabel3.setSize(new Dimension(229, 21));
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBounds(new Rectangle(10, 130, 408, 159));
      jPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Ausgabe", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
      jPanel1.add(jLabel3, null);
      jPanel1.add(getZfwSpinner(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes zfwSpinner	
   * 	
   * @return javax.swing.JTextField	
   */
  private JSpinner getZfwSpinner() {
    if (zfwSpinner == null) {
      zfwSpinner = new JSpinner();
      zfwSpinner.setModel(new SpinnerNumberModel(-6, -20, 20, 1));
      zfwSpinner.setBounds(new Rectangle(250, 80, 41, 21));
    }
    return zfwSpinner;
  }

}  //  @jve:decl-index=0:visual-constraint="148,7"

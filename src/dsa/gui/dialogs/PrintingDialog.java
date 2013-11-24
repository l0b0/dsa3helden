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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.jdic.desktop.Desktop;

import dsa.gui.lf.BGDialog;
import dsa.gui.util.ExampleFileFilter;
import dsa.model.characters.Hero;
import dsa.util.Directories;

/**
 * 
 */
public final class PrintingDialog extends BGDialog {

  private javax.swing.JPanel jContentPane = null;

  private JLabel jLabel = null;

  private JTextField jTextField = null;

  private JButton jButton = null;

  private JLabel jLabel1 = null;

  private JTextField outputField = null;

  private JButton outputButton = null;

  private JButton createButton = null;

  private JButton displayButton = null;

  private JButton closeButton = null;

  private Hero hero;

  private JButton fightTalentButton = null;

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
    getJTextField().setText(hero.getPrintingTemplateFile());
    getOutputField().setText(hero.getPrintFile());
    this.setLocationRelativeTo(parent);
    this.setTitle("Drucken: " + hero.getName());
    updateButtons();
  }

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    this.setTitle("Drucken");
    this.setModal(true);
    this.setSize(406, 236);
    this.setContentPane(getJContentPane());
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel1 = new JLabel();
      jLabel = new JLabel();
      jContentPane = new javax.swing.JPanel();
      jContentPane.setLayout(null);
      jLabel.setBounds(14, 19, 81, 17);
      jLabel.setText("Vorlage:");
      jLabel1.setBounds(14, 76, 83, 17);
      jLabel1.setText("Zieldatei:");
      jContentPane.add(jLabel, null);
      jContentPane.add(getJTextField(), null);
      jContentPane.add(getJButton(), null);
      jContentPane.add(jLabel1, null);
      jContentPane.add(getOutputField(), null);
      jContentPane.add(getOutputButton(), null);
      jContentPane.add(getCreateButton(), null);
      jContentPane.add(getDisplayButton(), null);
      jContentPane.add(getCloseButton(), null);
      jContentPane.add(getFightTalentButton(), null);
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
    boolean hasTemplate = getJTextField().getDocument().getLength() > 0;
    boolean hasOutput = getOutputField().getDocument().getLength() > 0;
    getCreateButton().setEnabled(hasTemplate && hasOutput);
    getDisplayButton().setEnabled(hasOutput);
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getJTextField() {
    if (jTextField == null) {
      jTextField = new JTextField();
      jTextField.setBounds(14, 48, 331, 20);
      jTextField.setName("templateFiled");
      jTextField.getDocument().addDocumentListener(new MyTextFieldListener());
    }
    return jTextField;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getJButton() {
    if (jButton == null) {
      jButton = new JButton();
      jButton.setBounds(357, 47, 31, 22);
      jButton.setText("...");
      jButton.setName("templateButton");
      jButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          selectTemplate();
        }
      });
    }
    return jButton;
  }

  /**
   * 
   * 
   */
  protected void selectTemplate() {
    JFileChooser chooser = null;
    if (getJTextField().getDocument().getLength() > 0) {
      chooser = new JFileChooser(new File(getJTextField().getText()));
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
    dsa.gui.util.ExampleFileFilter filter = new dsa.gui.util.ExampleFileFilter();
    filter.addExtension("rtf");
    filter.setDescription("RTF-Dateien");
    chooser.setFileFilter(filter);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      getJTextField().setText(chooser.getSelectedFile().getAbsolutePath());
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
      outputField.setBounds(14, 100, 331, 20);
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
      outputButton.setBounds(356, 99, 31, 22);
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
    filter.addExtension("rtf");
    filter.setDescription("RTF-Dateien");
    chooser.setFileFilter(filter);
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
      createButton.setBounds(15, 168, 102, 22);
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

    private int task = 0;

    private String errorMsg;
    
    public TransformerHelper(File input, File output) {
      this.input = input;
      this.output = output;
    }

    public void run() {
      if (task == 0) {
        try {
          dsa.control.CharacterPrinter printer = dsa.control.CharacterPrinter
              .getInstance();
          printer.printCharacter(hero, input, output, PrintingDialog.this,
              "Datei wird erstellt");
          hero.setPrintingTemplateFile(input.getCanonicalPath());
          hero.setPrintFile(getOutputField().getText());
          task = 1;
        }
        catch (java.io.IOException e) {
          task = 2;
          errorMsg = e.getMessage();
        }
        SwingUtilities.invokeLater(this);
      }
      else if (task == 1) {
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
    File input = new File(getJTextField().getText());
    File output = new File(getOutputField().getText());
    if (output.exists()) {
      if (JOptionPane.showConfirmDialog(PrintingDialog.this,
          "Datei existiert und wird überschrieben.\nFortfahren?", "Drucken",
          JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }
    TransformerHelper helper = new TransformerHelper(input, output);
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
      displayButton.setBounds(130, 168, 102, 22);
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
      closeButton.setBounds(243, 168, 102, 22);
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
      fightTalentButton.setBounds(new java.awt.Rectangle(15, 130, 208, 22));
      fightTalentButton.setText("Kampftalente auswählen ...");
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

} //  @jve:decl-index=0:visual-constraint="168,21"

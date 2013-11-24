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

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.gui.util.ExampleFileFilter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.util.Directories;

public final class BackgroundFrame extends SubFrame implements CharactersObserver {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JPanel jPanel1 = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;

  private JLabel jLabel3 = null;

  private JTextPane notesPane = null;

  private JLabel jLabel4 = null;

  private JTextField backgroundFileField = null;

  private JButton fileSelectorButton = null;

  private JButton editButton = null;

  private JLabel jLabel5 = null;

  private Hero currentHero;

  public BackgroundFrame() {
    super("Hintergrund");
    currentHero = Group.getInstance().getActiveHero();
    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(BackgroundFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(BackgroundFrame.this);
          done = true;
        }
      }
    });
    initialize();
    updateData();
  }
  
  public String getHelpPage() {
    return "Hintergrund";
  }

  private boolean listenForChanges = true;

  private void updateData() {
    listenForChanges = false;
    if (currentHero != null) {
      backgroundFileField.setText(currentHero.getBGFile());
      notesPane.setText(currentHero.getNotes());
      editButton.setEnabled(backgroundFileField.getText().length() > 0 && !currentHero.isDifference());
      fileSelectorButton.setEnabled(!currentHero.isDifference());
      backgroundFileField.setEditable(!currentHero.isDifference());
      notesPane.setEditable(!currentHero.isDifference());
    }
    else {
      backgroundFileField.setText("");
      notesPane.setText("");
      editButton.setEnabled(false);
      fileSelectorButton.setEnabled(false);
    }
    listenForChanges = true;
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    // this.setSize(new java.awt.Dimension(397,359));
    this.setContentPane(getJContentPane());
    this.setTitle("Hintergrund");

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
      jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
      jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
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
      jLabel5 = new JLabel();
      jLabel5.setBounds(new java.awt.Rectangle(6, 93, 162, 20));
      jLabel5.setText("Anmerkungen:");
      jLabel4 = new JLabel();
      jLabel4.setBounds(new java.awt.Rectangle(6, 8, 163, 20));
      jLabel4.setText("Hintergrundgeschichte:");
      jPanel = new JPanel();
      jPanel.setLayout(null);
      jPanel.setPreferredSize(new Dimension(400, 122));
      jPanel.add(jLabel4, null);
      jPanel.add(getBackgroundFileField(), null);
      jPanel.add(getFileSelectorButton(), null);
      jPanel.add(getEditButton(), null);
      jPanel.add(jLabel5, null);
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
      jLabel3.setText("");
      jLabel3.setPreferredSize(new Dimension(6, 10));
      jLabel2 = new JLabel();
      jLabel2.setText("");
      jLabel2.setPreferredSize(new Dimension(10, 10));
      jLabel1 = new JLabel();
      jLabel1.setText("");
      jLabel1.setPreferredSize(new Dimension(10, 10));
      jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new Dimension(10, 1));
      jPanel1 = new JPanel();
      jPanel1.setLayout(new BorderLayout());
      jPanel1.add(jLabel3, BorderLayout.WEST);
      jPanel1.add(jLabel, java.awt.BorderLayout.NORTH);
      JScrollPane scrollPane = new JScrollPane(getNotesPane());
      jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);
      jPanel1.add(jLabel2, BorderLayout.EAST);
      jPanel1.add(jLabel1, BorderLayout.SOUTH);
    }
    return jPanel1;
  }

  /**
   * This method initializes jTextPane
   * 
   * @return javax.swing.JTextPane
   */
  private JTextPane getNotesPane() {
    if (notesPane == null) {
      notesPane = new JTextPane();
      notesPane.getDocument().addDocumentListener(new DocumentListener() {

        public void insertUpdate(DocumentEvent e) {
          saveText();
        }

        public void removeUpdate(DocumentEvent e) {
          saveText();
        }

        public void changedUpdate(DocumentEvent e) {
          saveText();
        }

        private void saveText() {
          if (!listenForChanges) return;
          if (currentHero != null) {
            currentHero.setNotes(notesPane.getText());
          }
        }

      });
      // notesPane.setBorder(new LineBorder(java.awt.Color.GRAY));
    }
    return notesPane;
  }

  /**
   * This method initializes jTextField
   * 
   * @return javax.swing.JTextField
   */
  private JTextField getBackgroundFileField() {
    if (backgroundFileField == null) {
      backgroundFileField = new JTextField();
      backgroundFileField.setBounds(new java.awt.Rectangle(6, 31, 280, 20));
      backgroundFileField.getDocument().addDocumentListener(
          new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
              saveText();
            }

            public void removeUpdate(DocumentEvent e) {
              saveText();
            }

            public void changedUpdate(DocumentEvent e) {
              saveText();
            }

            private void saveText() {
              if (!listenForChanges) return;
              if (currentHero != null) {
                currentHero.setBGFile(backgroundFileField.getText());
              }
            }

          });
    }
    return backgroundFileField;
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getFileSelectorButton() {
    if (fileSelectorButton == null) {
      fileSelectorButton = new JButton();
      fileSelectorButton.setBounds(new java.awt.Rectangle(300, 31, 40, 20));
      fileSelectorButton.setText("...");
      fileSelectorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectFile(backgroundFileField, false, false);
        }
      });
    }
    return fileSelectorButton;
  }

  protected void selectFile(JTextField textField, boolean mustExist,
      boolean editor) {
    JFileChooser chooser = null;
    if (textField.getDocument().getLength() > 0) {
      chooser = new JFileChooser(new File(textField.getText()));
    }
    else {
      File f = Directories.getLastUsedDirectory(this,
          editor ? "BackgroundEditors" : "BackgroundFiles");
      if (f != null)
        chooser = new JFileChooser(f);
      else
        chooser = new JFileChooser();
    }
    if (editor) {
      dsa.gui.util.ExampleFileFilter filter = new ExampleFileFilter();
      filter.addExtension("exe");
      filter.addExtension("com");
      filter.setDescription("Ausführbare Dateien (Windows)");
      chooser.setFileFilter(filter);
    }
    else {
      dsa.gui.util.ExampleFileFilter filter = new ExampleFileFilter();
      filter.addExtension("doc");
      filter.addExtension("odt");
      filter.addExtension("rtf");
      filter.addExtension("txt");
      filter.setDescription("Textdokumente");
      chooser.setFileFilter(filter);
    }
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setMultiSelectionEnabled(false);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      textField.setText(chooser.getSelectedFile().getAbsolutePath());
      Directories.setLastUsedDirectory(this, editor ? "BackgroundEditors"
          : "BackgroundFiles", chooser.getSelectedFile());
    }
  }

  /**
   * This method initializes jButton
   * 
   * @return javax.swing.JButton
   */
  private JButton getEditButton() {
    if (editButton == null) {
      editButton = new JButton();
      editButton.setBounds(new java.awt.Rectangle(6, 59, 106, 20));
      editButton.setText("Editieren ...");
      editButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Desktop desktop = null;
          if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
              desktop = null;
            }
          }
          if (desktop == null) {
            JOptionPane.showMessageDialog(BackgroundFrame.this, "Das Betriebssystem erlaubt keinen direkten Viewer-Aufruf.", 
                "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
          }
          try {
            desktop.open(new File(backgroundFileField.getText()));
          }
          catch (IOException ex) {
            JOptionPane.showMessageDialog(BackgroundFrame.this,
                "Aufruf des Editors fehlgeschlagen:\n" + ex.getMessage(),
                "Fehler", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    }
    return editButton;
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    currentHero = newCharacter;
    updateData();
  }

  public void characterRemoved(Hero character) {
    if (character == currentHero) {
      currentHero = null;
      updateData();
    }
  }

  public void characterAdded(Hero character) {
  }

  public void globalLockChanged() {
  }
} // @jve:decl-index=0:visual-constraint="10,10"

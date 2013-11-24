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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

import dsa.gui.util.ExampleFileFilter;
import dsa.model.characters.Group;
import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Hero;
import dsa.util.Directories;

public class ImageFrame extends SubFrame implements CharactersObserver {

  public ImageFrame(String title) {
    super(title);

    Group.getInstance().addObserver(this);
    addWindowListener(new WindowAdapter() {
      boolean done = false;

      public void windowClosing(WindowEvent e) {
        Group.getInstance().removeObserver(ImageFrame.this);
        done = true;
      }

      public void windowClosed(WindowEvent e) {
        if (!done) {
          Group.getInstance().removeObserver(ImageFrame.this);
          done = true;
        }
      }
    });
    initialize();
    updateData();
  }

  private void initialize() {
    this.setContentPane(getJContentPane());
    jContentPane.add(getUpperPane(null), BorderLayout.CENTER);
    jContentPane.add(getLowerPane(), BorderLayout.SOUTH);
    this.pack();
  }

  private void updateData() {
    Hero hero = Group.getInstance().getActiveHero();
    String location = "";
    if (hero != null) {
      location = hero.getPictureLocation();
      getNameSelectButton().setEnabled(true);
    }
    else {
      getNameSelectButton().setEnabled(false);
    }
    getNameField().setText(location);
    updatePicture();
  }

  private JPanel jContentPane;

  private JScrollPane upperPane;

  private JPanel lowerPane;

  private dsa.gui.util.ScrollablePicture picture;

  private JTextField nameField;

  private JButton nameSelectButton;

  private JPanel getLowerPane() {
    if (lowerPane == null) {
      lowerPane = new JPanel();
      lowerPane.setLayout(null);
      lowerPane.add(getNameField(), null);
      lowerPane.add(getNameSelectButton(), null);
      JLabel label = new JLabel("Datei:");
      label.setBounds(5, 5, 50, 20);
      lowerPane.add(label, null);
      lowerPane.setPreferredSize(new Dimension(350, 40));
    }
    return lowerPane;
  }

  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setEditable(false);
      nameField.setBounds(55, 5, 220, 20);
    }
    return nameField;
  }

  private JScrollPane getUpperPane(String img) {
    if (upperPane == null) {
      ImageIcon icon = (img != null) ? new ImageIcon(img) : null;
      picture = new dsa.gui.util.ScrollablePicture(icon, 50);
      upperPane = new JScrollPane(picture);
      upperPane.setOpaque(false);
      upperPane.getViewport().setOpaque(false);
      upperPane.setViewportBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    return upperPane;
  }

  private JButton getNameSelectButton() {
    if (nameSelectButton == null) {
      nameSelectButton = new JButton("...");
      nameSelectButton.setBounds(285, 5, 40, 20);
      nameSelectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectPicture();
        }
      });
    }
    return nameSelectButton;
  }

  private void selectPicture() {
    JFileChooser chooser = null;
    JTextField textField = getNameField();
    if (textField.getDocument().getLength() > 0) {
      chooser = new JFileChooser(new File(textField.getText()));
    }
    else {
      File f = Directories.getLastUsedDirectory(this, "HeroImages");
      if (f != null)
        chooser = new JFileChooser(f);
      else
        chooser = new JFileChooser();
    }
    dsa.gui.util.ExampleFileFilter filter = new ExampleFileFilter();
    filter.addExtension("png");
    filter.addExtension("jpeg");
    filter.addExtension("jpg");
    filter.addExtension("gif");
    filter.setDescription("Bilder");
    chooser.setFileFilter(filter);
    chooser.setAcceptAllFileFilterUsed(true);
    chooser.setMultiSelectionEnabled(false);
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      textField.setText(chooser.getSelectedFile().getAbsolutePath());
      Group.getInstance().getActiveHero().setPicture(textField.getText());
      Directories.setLastUsedDirectory(this, "HeroImages", chooser
          .getSelectedFile());
      updatePicture();
    }
  }

  private void updatePicture() {
    getJContentPane().remove(getUpperPane(null));
    upperPane = null;
    getJContentPane().add(getUpperPane(getNameField().getText()));
    getJContentPane().revalidate();
  }

  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(new BorderLayout(20, 20));
    }
    return jContentPane;
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    updateData();
  }

  public void characterRemoved(Hero character) {
    updateData();
  }

  public void characterAdded(Hero character) {
    updateData();
  }

  public void globalLockChanged() {
  }
}

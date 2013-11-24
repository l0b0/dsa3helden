/*
    Copyright (c) 2006-2007 [Joerg Ruedenauer]
  
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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import java.awt.Rectangle;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import dsa.control.GroupOperations;
import dsa.gui.dialogs.PrintingDialog;
import dsa.gui.util.ImageManager;
import dsa.model.characters.CharacterAdapter;
import dsa.model.characters.Group;
import dsa.model.characters.GroupObserver;
import dsa.model.characters.Hero;

public final class GroupFrame extends SubFrame 
    implements GroupObserver, DropTargetListener {

  private JPanel jContentPane = null;

  private JPanel jPanel = null;

  private JLabel jLabel = null;

  private JLabel jLabel1 = null;

  private JLabel jLabel2 = null;
  
  private JButton printButton = null;

  private JButton createButton = null;

  private JButton addButton = null;

  private JButton removeButton = null;
  
  private JButton upButton = null;
  
  private JButton downButton = null;

  private JPanel jPanel1 = null;

  private JPanel jPanel2 = null;

  private JLabel jLabel3 = null;

  private JTextField nameField = null;

  private JList heroList = null;

  /**
   * This is the default constructor
   */
  public GroupFrame() {
    super("Gruppe");
    initialize();
    Group.getInstance().addObserver(this);
    this.addWindowListener(new WindowAdapter() {
      
      boolean done = false;

      public void windowClosed(WindowEvent e) {
        if (!done) cleanup();
      }

      public void windowClosing(WindowEvent e) {
        cleanup();
      }
      
      private void cleanup() {
        Group.getInstance().removeObserver(GroupFrame.this);
        done = true;
      }
    });
  }

  private DropTarget dropTarget = null;

  /**
   * This method initializes this
   * 
   * @return void
   */
  private void initialize() {
    // this.setSize(405, 275);
    this.setContentPane(getJContentPane());
    this.setTitle("Gruppe");
    updateData();
    if (dropTarget == null) {
      dropTarget = new DropTarget(this, this);
      getHeroList().setDropTarget(new DropTarget(getHeroList(), this));
    }
  }
  
  private void updateData() {
    listenForChanges = false;
    ((DefaultListModel)heroList.getModel()).removeAllElements();
    Group group = Group.getInstance();
    nameField.setText(group.getName());
    for (Hero h : group.getAllCharacters()) {
      ((DefaultListModel)heroList.getModel()).addElement(h.getName());
    }
    setSelectedHero();
    getRemoveButton().setEnabled(group.getAllCharacters().size() > 0);
    getUpButton().setEnabled(group.getAllCharacters().size() > 0);
    getDownButton().setEnabled(group.getAllCharacters().size() > 0);
    listenForChanges = true;
  }

  private void setSelectedHero() {
    int i = 0;
    for (Hero h : Group.getInstance().getAllCharacters()) {
      if (h == Group.getInstance().getActiveHero()) {
        heroList.setSelectedIndex(i);
        break;
      }
      else ++i;
    }
  }

  /**
   * This method initializes jContentPane
   * 
   * @return javax.swing.JPanel
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jLabel2 = new JLabel();
      jLabel2.setText("");
      jLabel2.setPreferredSize(new Dimension(38, 2));
      jLabel1 = new JLabel();
      jLabel1.setText("");
      jLabel1.setPreferredSize(new Dimension(10, 15));
      jLabel = new JLabel();
      jLabel.setText("");
      jLabel.setPreferredSize(new Dimension(38, 10));
      BorderLayout borderLayout = new BorderLayout();
      borderLayout.setHgap(10);
      borderLayout.setVgap(10);
      jContentPane = new JPanel();
      jContentPane.setLayout(borderLayout);
      jContentPane.add(getJPanel(), BorderLayout.EAST);
      jContentPane.add(jLabel, BorderLayout.SOUTH);
      jContentPane.add(jLabel1, BorderLayout.WEST);
      jContentPane.add(jLabel2, BorderLayout.NORTH);
      jContentPane.add(getJPanel1(), BorderLayout.CENTER);
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
      jPanel.setLayout(null);
      jPanel.setPreferredSize(new Dimension(70, 50));
      jPanel.add(getPrintButton(), null);
      jPanel.add(getCreateButton(), null);
      jPanel.add(getAddButton(), null);
      jPanel.add(getRemoveButton(), null);
      jPanel.add(getUpButton(), null);
      jPanel.add(getDownButton(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes createButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCreateButton() {
    if (createButton == null) {
      createButton = new JButton();
      createButton.setBounds(new Rectangle(10, 45, 51, 21));
      createButton.setToolTipText("Neuen Helden erstellen ...");
      createButton.setIcon(ImageManager.getIcon("tsa"));
      createButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.newHero(GroupFrame.this);
        }
      });
    }
    return createButton;
  }

  /**
   * This method initializes addButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getAddButton() {
    if (addButton == null) {
      addButton = new JButton();
      addButton.setBounds(new Rectangle(10, 75, 51, 21));
      addButton.setToolTipText("Helden hinzufügen ...");
      addButton.setIcon(ImageManager.getIcon("increase"));
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.openHeroes(GroupFrame.this);
        }
      });
    }
    return addButton;
  }

  /**
   * This method initializes removeButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getRemoveButton() {
    if (removeButton == null) {
      removeButton = new JButton();
      removeButton.setBounds(new Rectangle(10, 105, 51, 21));
      removeButton.setToolTipText("Helden entfernen");
      removeButton.setIcon(ImageManager.getIcon("decrease_enabled"));
      removeButton.setDisabledIcon(ImageManager.getIcon("decrease"));
      removeButton.setEnabled(false);
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GroupOperations.removeHero(GroupFrame.this);
        }
      });
    }
    return removeButton;
  }
  
  private JButton getUpButton() {
    if (upButton == null) {
      upButton = new JButton();
      upButton.setBounds(new Rectangle(10, 135, 51, 21));
      upButton.setToolTipText("Nach oben verschieben");
      upButton.setIcon(ImageManager.getIcon("up"));
      upButton.setEnabled(false);
      upButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          moveHero(-1);
        }
      });
    }
    return upButton;
  }
  
  private JButton getDownButton() {
    if (downButton == null) {
      downButton = new JButton();
      downButton.setBounds(new Rectangle(10, 165, 51, 21));
      downButton.setToolTipText("Nach unten verschieben");
      downButton.setIcon(ImageManager.getIcon("down"));
      downButton.setEnabled(false);
      downButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          moveHero(1);
        }
      });
    }
    return downButton;
  }
  
  private void moveHero(int offset) {
    int index = getHeroList().getSelectedIndex();
    int newIndex = index + offset;
    if (newIndex >= 0 && newIndex < Group.getInstance().getNrOfCharacters()) {
      Group.getInstance().moveHero(index, newIndex);
      updateData();
    }
  }
  
  private JButton getPrintButton() {
    if (printButton == null) {
      printButton = new JButton();
      printButton.setBounds(new Rectangle(10, 10, 51, 21));
      printButton.setToolTipText("Gruppenübersicht drucken ...");
      printButton.setIcon(ImageManager.getIcon("print"));
      printButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          PrintingDialog dialog = new PrintingDialog(Group.getInstance(), GroupFrame.this);
          dialog.setVisible(true);          
        }
      });
    }
    return printButton;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jPanel1 = new JPanel();
      jPanel1.setLayout(new BorderLayout());
      jPanel1.add(getJPanel2(), BorderLayout.NORTH);
      JScrollPane scrollPane = new JScrollPane(getHeroList());
      jPanel1.add(scrollPane, BorderLayout.CENTER);
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
      jLabel3 = new JLabel();
      jLabel3.setBounds(new Rectangle(0, 10, 51, 21));
      jLabel3.setText("Name:");
      jPanel2 = new JPanel();
      jPanel2.setLayout(null);
      jPanel2.setPreferredSize(new Dimension(100, 40));
      jPanel2.add(jLabel3, null);
      jPanel2.add(getNameField(), null);
    }
    return jPanel2;
  }

  /**
   * This method initializes nameField	
   * 	
   * @return javax.swing.JTextField	
   */
  private JTextField getNameField() {
    if (nameField == null) {
      nameField = new JTextField();
      nameField.setBounds(new Rectangle(60, 10, 221, 21));
      nameField.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
          nameChanged();
        }

        public void insertUpdate(DocumentEvent e) {
          nameChanged();
        }

        public void removeUpdate(DocumentEvent e) {
          nameChanged();
        }
        private void nameChanged() {
          if (listenForChanges)
            Group.getInstance().setName(nameField.getText());
        }
      });
    }
    return nameField;
  }
  
  private boolean listenForChanges = false;

  /**
   * This method initializes heroList	
   * 	
   * @return javax.swing.JList	
   */
  private JList getHeroList() {
    if (heroList == null) {
      heroList = new JList();
      heroList.setModel(new DefaultListModel());
      heroList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      heroList.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (listenForChanges) {
            listenForChanges = false;
            Group.getInstance().setActiveHero(Group.getInstance().getCharacter(heroList.getSelectedIndex()));
            listenForChanges = true;
          }
        }
        
      });
    }
    return heroList;
  }

  public String getHelpPage() {
    return "Gruppe";
  }

  public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
    listenForChanges = false;
    setSelectedHero();
    listenForChanges = true;
  }
  
  private final class NameChanger extends CharacterAdapter {
    public void nameChanged(String oldName, String newName) {
      updateData();
    }
  }
  
  private NameChanger nameChanger = new NameChanger();

  public void characterAdded(Hero character) {
    character.addHeroObserver(nameChanger);
    updateData();
  }

  public void characterRemoved(Hero character) {
    character.removeHeroObserver(nameChanger);
    updateData();
  }

  public void globalLockChanged() {
    // do nothing
  }

  public void wholeGroupChanged() {
    updateData();
  }

  public void groupLoaded() {
    listenForChanges = false;
    nameField.setText(Group.getInstance().getName());
    listenForChanges = true;
  }

  public void dragEnter(DropTargetDragEvent dtde) {
    GroupOperations.dragEnter(dtde);
  }

  public void dragExit(DropTargetEvent dte) {
  }

  public void dragOver(DropTargetDragEvent dtde) {
  }

  public void drop(DropTargetDropEvent dtde) {
    GroupOperations.drop(dtde, this);
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
  }

  public void orderChanged() {
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

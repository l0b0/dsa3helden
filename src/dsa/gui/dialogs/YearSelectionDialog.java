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
package dsa.gui.dialogs;

import dsa.gui.lf.BGDialog;
import dsa.model.Date;

import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;

public class YearSelectionDialog extends BGDialog {

  private JPanel jContentPane = null;
  private JPanel jPanel = null;
  private JSpinner yearSpinner = null;
  private JComboBox eraCombo = null;
  private JComboBox eventCombo = null;
  private JPanel jPanel1 = null;
  private JRadioButton moveBirthButton = null;
  private JRadioButton adaptAgeButton = null;
  private JRadioButton timeJumpButton = null;
  private JButton cancelButton = null;
  private JButton okButton = null;
  
  private ButtonGroup buttonGroup = null;
  
  public static enum Action {
    AdaptBirthdays,
    AdaptAges,
    DoNothing
  }
  
  public static class Result {
    public Result(Date date, Action action) {
      this.date = date;
      this.action = action;
    }
    public Date getDate() { return date; }
    public Action getAction() { return action; }
    private Date date;
    private Action action;
  }
  
  private Result result = null;
  
  private Date currentDate;

  /**
   * This method initializes 
   * 
   */
  public YearSelectionDialog(JFrame parent, Date date) {
  	super(parent, true);
    currentDate = date;
  	initialize();
    setLocationRelativeTo(parent);
  }
  
  public Result getDialogResult() {
    return result;
  }
  
  private Result calcResult() {
    int year = ((Number)yearSpinner.getValue()).intValue();
    Date.Era era = (Date.Era) eraCombo.getSelectedItem();
    Date.Event event = (Date.Event) eventCombo.getSelectedItem();
    if (year < 0) {
      if (era == Date.Era.vor) {
        era = Date.Era.nach;
      }
      else {
        era = Date.Era.vor;
      }
      year = -year;
    }
    Date date = new Date(1, Date.Month.Praios, year, era, event);
    Action action = Action.DoNothing;
    if (adaptAgeButton.isSelected()) action = Action.AdaptAges;
    else if (moveBirthButton.isSelected()) action = Action.AdaptBirthdays;
    return new Result(date, action);
  }
  
  private void recalcYear() {
    Date.Era newEra = (Date.Era) eraCombo.getSelectedItem();
    Date.Event newEvent = (Date.Event) eventCombo.getSelectedItem();
    int currentYear = ((Number)yearSpinner.getValue()).intValue();
    int newYear = Date.rebaseYear(currentYear, currentDate.getEvent(), 
        currentDate.getEra(), newEvent, newEra);
    yearSpinner.setValue(newYear);
    currentDate = new Date(1, Date.Month.Praios, newYear, newEra, newEvent);
  }

  /**
   * This method initializes this
   * 
   */
  private void initialize() {
    this.setSize(new Dimension(291, 272));
    this.setContentPane(getJContentPane());
    this.setTitle("Spielzeit einstellen");
  	buttonGroup = new ButtonGroup();
    buttonGroup.add(moveBirthButton);
    buttonGroup.add(adaptAgeButton);
    buttonGroup.add(timeJumpButton);
    getRootPane().setDefaultButton(okButton);
    setEscapeButton(cancelButton);
  }

  public String getHelpPage() {
    return "Spieljahr";
  }

  /**
   * This method initializes jContentPane	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setLayout(null);
      jContentPane.add(getJPanel(), null);
      jContentPane.add(getJPanel1(), null);
      jContentPane.add(getCancelButton(), null);
      jContentPane.add(getOkButton(), null);
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
      jPanel.setBounds(new Rectangle(10, 10, 261, 61));
      jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Neues Jahr", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, null, null));
      jPanel.add(getYearSpinner(), null);
      jPanel.add(getEraCombo(), null);
      jPanel.add(getEventCombo(), null);
    }
    return jPanel;
  }

  /**
   * This method initializes yearSpinner	
   * 	
   * @return javax.swing.JSpinner	
   */
  private JSpinner getYearSpinner() {
    if (yearSpinner == null) {
      yearSpinner = new JSpinner();
      yearSpinner.setModel(new SpinnerNumberModel(10, -10000, 10000, 1));
      yearSpinner.setBounds(new Rectangle(10, 30, 51, 21));
      yearSpinner.setValue(currentDate.getYear());
    }
    return yearSpinner;
  }

  /**
   * This method initializes eraCombo	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getEraCombo() {
    if (eraCombo == null) {
      eraCombo = new JComboBox();
      eraCombo.setBounds(new Rectangle(69, 30, 62, 21));
      for (Date.Era era : Date.Era.values()) {
        eraCombo.addItem(era);
      }
      eraCombo.setSelectedItem(currentDate.getEra());
      eraCombo.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent e) {
          recalcYear();
        }
      });
    }
    return eraCombo;
  }

  /**
   * This method initializes eventCombo	
   * 	
   * @return javax.swing.JComboBox	
   */
  private JComboBox getEventCombo() {
    if (eventCombo == null) {
      eventCombo = new JComboBox();
      eventCombo.setBounds(new Rectangle(140, 30, 111, 21));
      for (Date.Event event : Date.Event.values()) {
        eventCombo.addItem(event);
      }
      eventCombo.setSelectedItem(currentDate.getEvent());
      eventCombo.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent e) {
          recalcYear();
        }
      });
    }
    return eventCombo;
  }

  /**
   * This method initializes jPanel1	
   * 	
   * @return javax.swing.JPanel	
   */
  private JPanel getJPanel1() {
    if (jPanel1 == null) {
      jPanel1 = new JPanel();
      jPanel1.setLayout(null);
      jPanel1.setBounds(new Rectangle(10, 80, 261, 121));
      jPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Aktion", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, null, null));
      jPanel1.add(getMoveBirthButton(), null);
      jPanel1.add(getAdaptAgeButton(), null);
      jPanel1.add(getTimeJumpButton(), null);
    }
    return jPanel1;
  }

  /**
   * This method initializes moveBirthButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getMoveBirthButton() {
    if (moveBirthButton == null) {
      moveBirthButton = new JRadioButton();
      moveBirthButton.setBounds(new Rectangle(10, 30, 241, 21));
      moveBirthButton.setSelected(true);
      moveBirthButton.setText("Geburtsjahre anpassen");
    }
    return moveBirthButton;
  }

  /**
   * This method initializes adaptAgeButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getAdaptAgeButton() {
    if (adaptAgeButton == null) {
      adaptAgeButton = new JRadioButton();
      adaptAgeButton.setBounds(new Rectangle(10, 60, 241, 21));
      adaptAgeButton.setText("Alter anpassen");
    }
    return adaptAgeButton;
  }

  /**
   * This method initializes timeJumpButton	
   * 	
   * @return javax.swing.JRadioButton	
   */
  private JRadioButton getTimeJumpButton() {
    if (timeJumpButton == null) {
      timeJumpButton = new JRadioButton();
      timeJumpButton.setBounds(new Rectangle(10, 91, 241, 21));
      timeJumpButton.setText("Keine (Zeitreise)");
    }
    return timeJumpButton;
  }

  /**
   * This method initializes cancelButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getCancelButton() {
    if (cancelButton == null) {
      cancelButton = new JButton();
      cancelButton.setBounds(new Rectangle(140, 210, 101, 21));
      cancelButton.setText("Abbrechen");
      cancelButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          result = null;
          dispose();
        }
      });
    }
    return cancelButton;
  }

  /**
   * This method initializes okButton	
   * 	
   * @return javax.swing.JButton	
   */
  private JButton getOkButton() {
    if (okButton == null) {
      okButton = new JButton();
      okButton.setBounds(new Rectangle(30, 210, 91, 21));
      okButton.setText("OK");
      okButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
          result = calcResult();
          dispose();
        }
      });
    }
    return okButton;
  }

}  //  @jve:decl-index=0:visual-constraint="10,10"

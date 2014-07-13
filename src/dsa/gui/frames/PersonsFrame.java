package dsa.gui.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dsa.model.characters.CharactersObserver;
import dsa.model.characters.Group;
import dsa.model.characters.Hero;

class PersonsFrame extends SubFrame implements CharactersObserver {

	private JPanel jContentPane = null;

	private JTextPane npcPane = null;
	
	private JTextPane pcPane = null;
	
	private Hero currentHero = null;
	
	public PersonsFrame() {
		super(Localization.getString("Personen.Personen")); //$NON-NLS-1$
		currentHero = Group.getInstance().getActiveHero();
		Group.getInstance().addObserver(this);
	    addWindowListener(new WindowAdapter() {
	        boolean done = false;

	        public void windowClosing(WindowEvent e) {
	          Group.getInstance().removeObserver(PersonsFrame.this);
	          done = true;
	        }

	        public void windowClosed(WindowEvent e) {
	          if (!done) {
	            Group.getInstance().removeObserver(PersonsFrame.this);
	            done = true;
	          }
	        }
	      });
	    initialize();
	    updateData();
	}
	
	  public String getHelpPage() {
		    return "Personen"; //$NON-NLS-1$
	  }

	  private boolean listenForChanges = true;

	  private void updateData() {
	    listenForChanges = false;
	    if (currentHero != null) {
		    npcPane.setText(currentHero.getKnownNPCs());
		    pcPane.setText(currentHero.getKnownPCs());
	    }
	    else {
	    	npcPane.setText(""); //$NON-NLS-1$
	    	pcPane.setText(""); //$NON-NLS-1$
	    }
	    listenForChanges = true;
	  }
	  
	  private void initialize() {
		    this.setContentPane(getJContentPane());
		    this.setTitle(Localization.getString("Personen.Personen")); //$NON-NLS-1$
		    if (!hasSavedFrameBounds()) {
		    	pack();
		    }
	  }	  
	  
	  private JPanel getJContentPane() {
		    if (jContentPane == null) {
		      jContentPane = new JPanel();
		      jContentPane.setLayout(new GridLayout(2, 1, 5, 5));
		      jContentPane.add(getPersonsPanel(false));
		      jContentPane.add(getPersonsPanel(true));
		    }
		    return jContentPane;
	  }
	  
	  private JPanel getPersonsPanel(boolean npcs) {
		  JPanel personsPanel = new JPanel(new BorderLayout(5, 5));
		  JLabel caption = new JLabel(npcs ? Localization.getString("Personen.BekannteNSC") : Localization.getString("Personen.BekannteSC")); //$NON-NLS-1$ //$NON-NLS-2$
		  JPanel inner = new JPanel(new BorderLayout(5, 5));
		  inner.add(caption, BorderLayout.NORTH);
		  JTextPane textPane = npcs ? getNPCPane() : getPCPane();
		  JScrollPane scrollPane = new JScrollPane(textPane);
		  inner.add(scrollPane, BorderLayout.CENTER);
		  personsPanel.add(inner, BorderLayout.CENTER);
		  JLabel l1 = new JLabel();
		  l1.setPreferredSize(new Dimension(5, 5));
		  personsPanel.add(l1, BorderLayout.EAST);
		  JLabel l2 = new JLabel();
		  l2.setPreferredSize(new Dimension(5, 5));
		  personsPanel.add(l2, BorderLayout.WEST);
		  JLabel l3 = new JLabel();
		  l3.setPreferredSize(new Dimension(5, 5));
		  personsPanel.add(l3, BorderLayout.SOUTH);
		  personsPanel.setPreferredSize(new Dimension(300, 150));
		  return personsPanel;
	  }
	  
	  private JTextPane getNPCPane() {
		    if (npcPane == null) {
		    	npcPane = new JTextPane();
		    	npcPane.getDocument().addDocumentListener(new DocumentListener() {

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
		            currentHero.setKnownNPCs(npcPane.getText());
		          }
		        }

		      });
		      // notesPane.setBorder(new LineBorder(java.awt.Color.GRAY));
		    }
		    return npcPane;
	  }

	  private JTextPane getPCPane() {
		    if (pcPane == null) {
		    	pcPane = new JTextPane();
		    	pcPane.getDocument().addDocumentListener(new DocumentListener() {

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
		            currentHero.setKnownPCs(pcPane.getText());
		          }
		        }

		      });
		      // notesPane.setBorder(new LineBorder(java.awt.Color.GRAY));
		    }
		    return pcPane;
	  }

	  @Override
	public void activeCharacterChanged(Hero newCharacter, Hero oldCharacter) {
		    currentHero = newCharacter;
		    updateData();
	}

	@Override
	public void characterAdded(Hero character) {
	}

	@Override
	public void characterRemoved(Hero character) {
	}

	@Override
	public void globalLockChanged() {
	}

}

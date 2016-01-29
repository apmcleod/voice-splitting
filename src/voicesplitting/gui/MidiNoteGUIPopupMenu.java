package voicesplitting.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * A <code>MidiNoteGUIPopupMenu</code> represents the right-click menu for any
 * {@link MidiNoteGUI} object. This also pops up as the right click on a {@link NoteDisplayer}.
 * 
 * @author Andrew McLeod - 28 July, 2015
 * @version 1.0
 * @since 1.0
 */
public class MidiNoteGUIPopupMenu extends JPopupMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8722374460720965624L;
	
	/**
	 * The current NoteDisplayer.
	 */
	private final NoteDisplayer displayer;
	
	/**
	 * What to do when change voice is selected.
	 */
	private final ActionListener voiceListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			displayer.changeHighlightedChannels(Integer.parseInt(((JMenuItem) e.getSource()).getText()));
		}
	};
	
	/**
	 * What to do when delete is selected.
	 */
	private final ActionListener deleteListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			displayer.deleteHighlightedNotes();
		}
	};
	
	/**
	 * What to do when split notes is selected.
	 */
	private final ActionListener splitNotesListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			displayer.splitHighlightedNotes();
		}
	};

	/**
	 * Create a new popup menu in the given NoteDisplayer.
	 * 
	 * @param displayer {@link #displayer}
	 */
	public MidiNoteGUIPopupMenu(NoteDisplayer displayer) {
		this.displayer = displayer;
		
		JMenu changeVoice = new JMenu("Change Voice");
		changeVoice.setEnabled(displayer.hasHighlightedNotes());
		
		for (int i = 0; i < 16; i++) {
			JMenuItem voice = new JMenuItem("" + i);
			voice.setBackground(MidiNoteGUI.getColor(i));
			voice.addActionListener(voiceListener);
			changeVoice.add(voice);
		}
		
		JMenuItem deleteNote = new JMenuItem("Delete");
		deleteNote.addActionListener(deleteListener);
		deleteNote.setEnabled(displayer.hasHighlightedNotes());
		
		JMenuItem splitNotes = new JMenuItem("Split Notes");
		splitNotes.addActionListener(splitNotesListener);
		splitNotes.setEnabled(displayer.canSplit());
		
		add(changeVoice);
		add(deleteNote);
		add(splitNotes);
	}
}

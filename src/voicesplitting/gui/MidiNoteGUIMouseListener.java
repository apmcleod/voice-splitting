package voicesplitting.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A <code>MidiNoteGUIMouseListener</code> controls the actions to be performed when a
 * {@link MidiNoteGUI} object is clicked with the mouse. It contains a static instance of
 * itself, {@link #listener}, which should be used because each {@link MidiNoteGUI} instance
 * does not need a unique listener.
 * 
 * @author Andrew McLeod - 28 July, 2015
 */
public class MidiNoteGUIMouseListener extends MouseAdapter {

	/**
	 * A static instance of a MidiNoteGUIMouseListener object.
	 */
	public static MidiNoteGUIMouseListener listener = new MidiNoteGUIMouseListener();
	
	/**
	 * Default constructor private so you have to use {@link #listener}.
	 */
	private MidiNoteGUIMouseListener() {}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		MidiNoteGUI note = (MidiNoteGUI) e.getComponent();
		
		note.setToolTipText(note.generateToolTipText());
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		MidiNoteGUI note = (MidiNoteGUI) e.getComponent();
		
		note.setToolTipText(null);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				// Left click
				doLeftClick(e);
				break;
				
			case MouseEvent.BUTTON2:
				// Middle click
				break;
				
			case MouseEvent.BUTTON3:
				// Right click
				MidiNoteGUI note = (MidiNoteGUI) e.getComponent();
				
				if (!note.isHighlighted()) {
					doLeftClick(e);
				}
				
				NoteDisplayer displayer = (NoteDisplayer) note.getParent();
				MidiNoteGUIPopupMenu menu = new MidiNoteGUIPopupMenu(displayer);
				menu.show(note, e.getX(), e.getY());
				break;
				
			default:
				// No button
				break;
		}
	}

	/**
	 * Perform the left click action on the given MouseEvent. This is used because when
	 * right-clicking on an unhighlighted note, a left click must be performed first.
	 * 
	 * @param e The MouseEvent.
	 */
	private void doLeftClick(MouseEvent e) {
		MidiNoteGUI note = (MidiNoteGUI) e.getComponent();
		NoteDisplayer displayer = (NoteDisplayer) note.getParent();
		
		MidiNoteGUI last = displayer.getLastClicked();
		displayer.setLastClicked(note);
		
		if (!e.isControlDown()) {
			// If not control, we need to clear all other highlights
			displayer.clearAllHighlights();
		}
		
		// Highlight this note
		displayer.toggleHighlight(note);
		
		if (e.isShiftDown() && last != null) {
			// If shift was down, we need every note between the last clicked one and this one
			// to be highlighted.
			displayer.highlightAllBetween(last, note);
		}
	}
}

package voicesplitting.gui;

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import voicesplitting.utils.MidiNote;

/**
 * A <code>NoteDisplayerWorker</code> does the work of updating the note displayer GUI in the background.
 * 
 * @author Andrew McLeod - 20 June, 2015
 */
public class NoteDisplayerWorker extends SwingWorker<NoteDisplayer, Void> {

	/**
	 * The GUI object we want to update.
	 */
	private VoiceSplittingGUI gui;
	
	/**
	 * The scroll pane containing the current {@link NoteDisplayer}.
	 */
	private JScrollPane scrollPane;
	
	/**
	 * The notes we want to load.
	 */
	private List<MidiNote> notes;
	
	/**
	 * Create a new NoteDisplayerWorker with the given params.
	 * 
	 * @param scrollPane {@link #scrollPane}
	 * @param gui {@link #gui}
	 */
	public NoteDisplayerWorker(JScrollPane scrollPane, VoiceSplittingGUI gui, List<MidiNote> notes) {
		this.scrollPane = scrollPane;
		this.gui = gui;
		this.notes = notes;
	}
	
	@Override
	protected NoteDisplayer doInBackground() {
		NoteDisplayer old = gui.getNoteDisplayer();
		int horizontal = old.getHorizontalScale();
		int vertical = old.getVerticalScale();
		
		NoteDisplayer nd = new NoteDisplayer(horizontal, vertical);
		long lastOffset = 0;
			
		for (MidiNote note : notes) {
			nd.add(new MidiNoteGUI(note, horizontal, vertical));
			lastOffset = Math.max(lastOffset, note.getOffsetTime());
				
			if (isCancelled()) {
				return old;
			}
		}
		
		nd.setLastOffset(lastOffset);
		nd.setPreferredSize(new Dimension(nd.getDesiredWidth(), nd.getDesiredHeight()));
		nd.updateColumnHeaderView();
		
		return nd;
	}

	@Override
	protected void done() {
		if (isCancelled()) {
			gui.updateActionButtons();
			return;
		}
		
		NoteDisplayer nd;
		try {
			nd = get();
			
		} catch (InterruptedException e) {
			gui.displayErrorDialog(e);
			return;
			
		} catch (ExecutionException e) {
			gui.displayErrorDialog(e);
			return;
		}
		
		NoteDisplayer old = gui.getNoteDisplayer();
		int vertical = scrollPane.getVerticalScrollBar().getValue();
		int horizontal = scrollPane.getHorizontalScrollBar().getValue();
		
		gui.setNoteDisplayer(nd);
		nd.setScale(old.getHorizontalScale(), old.getVerticalScale());
		
		scrollPane.setViewportView(nd);
		scrollPane.setColumnHeaderView(nd.getColumnHeaderView());
		scrollPane.setRowHeaderView(nd.getRowHeaderView());
		scrollPane.getHorizontalScrollBar().setValue(horizontal);
		scrollPane.getVerticalScrollBar().setValue(vertical);
		scrollPane.revalidate();
		scrollPane.repaint();
		
		gui.updateActionButtons();
	}
}

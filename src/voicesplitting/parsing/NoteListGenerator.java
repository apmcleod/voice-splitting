package voicesplitting.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;

import voicesplitting.time.TimeTracker;
import voicesplitting.utils.MidiNote;

/**
 * A <code>NoteListGenerator</code> parses Note On and Note Off events and generates a List of
 * the {@link voicesplitting.utils.MidiNote}s present in any given song. 
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class NoteListGenerator implements NoteEventParser {
	/**
	 * A list of the MidiNotes which have not yet been closed.
	 */
	private LinkedList<MidiNote> activeNotes;
	
	/**
	 * A list of MidiNotes which have already been closed.
	 */
	private List<MidiNote> completedNotes;
	
	/**
	 * The TimeTracker for this NoteListGenerator.
	 */
	protected TimeTracker timeTracker;
	
	/**
	 * Creates a new NoteListGenerator with the given TimeTracker.
	 * 
	 * @param timeTracker
	 */
	public NoteListGenerator(TimeTracker timeTracker) {
		activeNotes = new LinkedList<MidiNote>();
		completedNotes = new ArrayList<MidiNote>();
		
		this.timeTracker = timeTracker;
	}
	
	@Override
	public MidiNote noteOn(int key, int velocity, long tick, int channel) {
		long time = timeTracker.getTimeAtTick(tick);
		
		MidiNote note = new MidiNote(key, velocity, time, tick, channel, -1);
		
		activeNotes.add(note);
		
		return note;
	}

	@Override
	public void noteOff(int key, long tick, int channel) throws InvalidMidiDataException {
		long time = timeTracker.getTimeAtTick(tick);
		Iterator<MidiNote> iterator = activeNotes.iterator();
		
		while (iterator.hasNext()) {
			MidiNote note = iterator.next();
			
			if (note.getPitch() == key && note.getCorrectVoice() == channel) {
				iterator.remove();
				note.close(time, tick);
				completedNotes.add(note);
				return;
			}
		}
		
		// This is commented out because of a difference in handling MIDI onsets with velocity = 0.
		// Some files treat this as a note off event (this is the standard).
		// Some, however, add a single note with onset velocity 0 and a separate offset at the
		// end of each piece. If we allow this error to be thrown, these files will except
		// and crash.
		
		// Note off event didn't match any active notes.
		//throw new InvalidMidiDataException("Note off event doesn't match any note on: " +
				//"key=" + key + ", tick=" + tick + " track=" + track);
	}

	/**
	 * Returns a list of the notes present in this song, in time order.
	 * 
	 * @return A list of the notes present as described above.
	 */
	public List<MidiNote> getNoteList() {
		Collections.sort(completedNotes);
		
		return completedNotes;
	}
	
	/**
	 * Returns a List of the incoming note lists. This is exactly the sequence of note lists
	 * which should be fed into the {@link voicesplitting.generic.MidiModel#handleIncoming(List)}
	 * method.
	 * 
	 * @return A List of incoming note lists, where each individual list contains all of the notes
	 * which onset at a particular time, and the lists themselves are sorted by onset time.
	 */
	public List<List<MidiNote>> getIncomingLists() {
		List<List<MidiNote>> incomingLists = new ArrayList<List<MidiNote>>();
		
		List<MidiNote> noteList = getNoteList();
		
		int i = 0;
		while (i < noteList.size()) {
			List<MidiNote> incoming = new ArrayList<MidiNote>();
			long onsetTime = noteList.get(i).getOnsetTime();
			
			// Add the note and increment while we're still on the proper onset time.
			do {
				incoming.add(noteList.get(i++));
			} while (i < noteList.size() && noteList.get(i).getOnsetTime() == onsetTime);
			
			incomingLists.add(incoming);
		}
		
		return incomingLists;
	}
}

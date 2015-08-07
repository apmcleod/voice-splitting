package voicesplitting.voice;

import java.util.List;


/**
 * A <code>VoiceSplitter</code> will split a List of MidiNotes into voices. That is, it will take as input
 * a single ordered List of MidiNotes, and return a List of {@link Voice}s, each List consisting
 * of {@link voicesplitting.utils.MidiNote}s which occur contiguously in the song in a single
 * voice. For piano, this could be congruent to splitting the notes into right and left hand parts,
 * or it may be separating multiple instruments out into their respective parts.
 * 
 * @author Andrew McLeod - 1 April, 2015
 */
public interface VoiceSplitter {
	/**
	 * Get the {@link Voice}s we've split the song into.
	 * 
	 * @return A List of the Voices this song has been split into.
	 * @throws InterruptedException If we're running on a GUI and this gets cancelled.
	 */
	public List<? extends Voice> getVoices() throws InterruptedException;
}

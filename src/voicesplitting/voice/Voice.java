package voicesplitting.voice;

import voicesplitting.utils.MidiNote;

/**
 * A <code>Voice</code> represents a contiguous stream of {@link voicesplitting.utils.MidiNote}s
 * within a song.
 * <p>
 * A {@link SingleNoteVoice} is constrained such that two notes may not co-occur at
 * any time, and thus works with {@link voicesplitting.utils.MidiNote}s.
 * 
 * @author Andrew McLeod - 6 April, 2015
 */
public abstract class Voice {
	/**
	 * Decide if a new note of the given length can be added at the given time.
	 * 
	 * @param time The time we wish to check.
	 * @param length The length of the note we want to add.
	 * @return True if we can add a note of the given length at the given time.
	 * False otherwise.
	 */
	public abstract boolean canAddNoteAtTime(long time, long length);
	
	/**
	 * Get the last offset time of any note in this Voice.
	 * 
	 * @return The last offset time from the last note in the last chord in this Voice.
	 */
	public abstract long getLastOffsetTime();
	
	/**
	 * Get the MidiNote in this Voice with the most recent onset time.
	 * 
	 * @return The MidiNote from this Voice with the last onset time.
	 */
	public abstract MidiNote getLastNote();
	
	/**
	 * Get the weighted last pitch of this Voice. That is, the most recent pitch of this
	 * Voice, looking back {@link voicesplitting.voice.hmm.VoiceSplittingParameters#PITCH_HISTORY_LENGTH}
	 * notes, and weighting each one less the further back we look.
	*
	 * @return A weighted average of the average pitches of the last
	 * {@link voicesplitting.voice.hmm.VoiceSplittingParameters#PITCH_HISTORY_LENGTH}
	 * notes or chords, weighing more recent notes or chords higher, or 0 if we have none yet.
	 */
	public abstract double getWeightedLastPitch();
	
	/**
	 * Get the number of notes total in this Voice.
	 * 
	 * @return The number of notes in this Voice.
	 */
	public abstract int getNumNotes();
	
	/**
	 * Get the number of notes in this Voice which have been assigned to the correct Voice.
	 * That is the number which belong to the midi track from which the majority of the notes
	 * in this Voice come.
	 * 
	 * @return The number of notes which have been classified to this Voice correctly.
	 */
	public abstract int getNumNotesCorrect();
}

package voicesplitting.utils;

/**
 * A <code>MidiNote</code> represents a single instance of a played midi note. It contains
 * information on the note's pitch, onset, offset, and velocity, as well as which MidiChord
 * it is assigned to.
 * <p>
 * MidiNotes are Comparable, and their natural ordering is determined strictly by each note's
 * {@link #onsetTime}. That means that the {@link #compareTo(MidiNote)} method will return 0
 * if two notes are compared which have the same onset time. For this reason, MidiNotes should
 * not be used in any SortedSets, since they test for equality based soleley on the compareTo
 * method, and will therefore not be able to hold multiple notes with the same onset time.
 * 
 * @author Andrew McLeod - 23 October, 2014
 */
public class MidiNote implements Comparable<MidiNote> {
	
	/**
	 * The midi track which this note came from.
	 */
	private int track;
	
	/**
	 * The onset time of this note, measured in microseconds.
	 */
	private long onsetTime;
	
	/**
	 * The onset tick of this note.
	 */
	private long onsetTick;
	
	/**
	 * The offset time of this note, measured in microseconds, or 0 if it is still active.
	 */
	private long offsetTime;
	
	/**
	 * The offset tick of this note, or 0 if it is still active.
	 */
	private long offsetTick;
	
	/**
	 * The velocity of this note.
	 */
	private int velocity;
	
	/**
	 * The key number of this note. For piano, it should be on the range 21 to 108 inclusive.
	 */
	private int pitch;
	
	/**
	 * The Beat of the onset of this note.
	 */
	private Beat onsetBeat;
	
	/**
	 * The Beat of the offset of this note.
	 */
	private Beat offsetBeat;

	/**
	 * The index of the guessed voice of this note.
	 */
	private int guessedVoice;
	
	/**
	 * Constructor for a new note.
	 * 
	 * @param key {@link #pitch}
	 * @param velocity {@link #velocity}
	 * @param onsetTime {@link #onsetTime}
	 * @param onsetTick {@link #onsetTick}
	 * @param track {@link #track}
	 * @param guessedVoice {@link #guessedVoice}
	 */
	public MidiNote(int key, int velocity, long onsetTime, long onsetTick, Beat beat, int track, int guessedVoice) {
		this.pitch = key;
		this.velocity = velocity;
		this.onsetTime = onsetTime;
		this.onsetTick = onsetTick;
		this.onsetBeat = beat;
		this.track = track;
		offsetTime = 0;
		offsetTick = 0;
		this.guessedVoice = guessedVoice;
	}
	
	/**
	 * Move this note's onset to the given location.
	 * 
	 * @param onsetTime {@link #onsetTime}
	 * @param onsetTick {@link #onsetTick}
	 * @param beat {@link #onsetBeat}
	 */
	public void setOnset(long onsetTime, long onsetTick, Beat beat) {
		this.onsetTime = onsetTime;
		this.onsetTick = onsetTick;
		this.onsetBeat = beat;
	}
	
	/**
	 * Move this note's offset to the given location.
	 * 
	 * @param offsetTime {@link #offsetTime}
	 * @param offsetTick {@link #offsetTick}
	 * @param beat {@link #offsetBeat}
	 */
	public void setOffset(long offsetTime, long offsetTick, Beat beat) {
		this.offsetTime = offsetTime;
		this.offsetTick = offsetTick;
		this.offsetBeat = beat;
	}
	
	/**
	 * Returns whether this note is active (still on) or not. A note will be active
	 * 
	 * @return True if this note is active. False otherwise.
	 */
	public boolean isActive() {
		return offsetTime == 0;
	}
	
	/**
	 * Turns off this note at the given time and tick.
	 * 
	 * @param offsetTime {@link #offsetTime}
	 * @param offsetTick {@link #offsetTick}
	 */
	public void close(long offsetTime, long offsetTick, Beat beat) {
		this.offsetTime = offsetTime;
		this.offsetTick = offsetTick;
		this.offsetBeat = beat;
	}

	/**
	 * Get the length of this note in beats.
	 * 
	 * @return The length of this note in beats.
	 */
	public int getNoteDurationBeats() {
		if (isActive()) {
			return 0;
		}
		
		return onsetBeat.getNumBeatsUntil(offsetBeat);
	}

	/**
	 * Return whether this note overlaps another MidiNote in time.
	 * 
	 * @param other The note we want to check for overlap. This can be null, in which case
	 * we will return false.
	 * @return True if the notes overlap. False otherwise.
	 */
	public boolean overlaps(MidiNote other) {
		if (other == null) {
			return false;
		}
		
		if (onsetTick < other.getOffsetTick() && offsetTick > other.getOnsetTick()) {
			// We start before the other finishes, and finish after it starts.
			return true;
		}

		return false;
	}
	
	/**
	 * Gets the onset time of this note.
	 * 
	 * @return {@link #onsetTime}
	 */
	public long getOnsetTime() {
		return onsetTime;
	}
	
	/**
	 * Gets the onset tick of this note.
	 * 
	 * @return {@link #onsetTick}
	 */
	public long getOnsetTick() {
		return onsetTick;
	}

	/**
	 * Gets the offset time of this note.
	 * 
	 * @return {@link #offsetTime}
	 */
	public long getOffsetTime() {
		return offsetTime;
	}
	
	/**
	 * Gets the offset tick of this note.
	 * 
	 * @return {@link #offsetTick}
	 */
	public long getOffsetTick() {
		return offsetTick;
	}
	
	/**
	 * Get the duration of this MidiNote in microseconds.
	 * 
	 * @return The duration in microseconds.
	 */
	public long getDurationTime() {
		return offsetTime - onsetTime;
	}
	
	/**
	 * Gets the key number of this note.
	 * 
	 * @return {@link #pitch}
	 */
	public int getPitch() {
		return pitch;
	}
	
	/**
	 * Get the onset Beat for this note.
	 * 
	 * @return {@link #onsetBeat}
	 */
	public Beat getOnsetBeat() {
		return onsetBeat;
	}
	
	/**
	 * Get the offset Beat for this note.
	 * 
	 * @return {@link #offsetBeat}
	 */
	public Beat getOffsetBeat() {
		return offsetBeat;
	}
	
	/**
	 * Get the velocity of this note.
	 * 
	 * @return {@link #velocity}
	 */
	public int getVelocity() {
		return velocity;
	}

	/**
	 * Get the track number of this note.
	 * 
	 * @return {@link #track}
	 */
	public int getTrackNumber() {
		return track;
	}
	
	/**
	 * Set the track number of this note to a new value.
	 * 
	 * @param track {@link #track}
	 */
	public void setTrack(int track) {
		this.track = track;
	}
	
	/**
	 * Set the guessed voice of this note to the given value.
	 * 
	 * @param voice {@link #guessedVoice}
	 */
	public void setGuessedVoice(int voice) {
		guessedVoice = voice;
	}
	
	/**
	 * Get the guessed voice of this note.
	 * 
	 * @return {@link #guessedVoice}
	 */
	public int getGuessedVoice() {
		return guessedVoice;
	}
	
	/**
	 * Get a deep copy of this MidiNote.
	 * 
	 * @return a deep copy of this note.
	 */
	public MidiNote deepCopy() {
		Beat newOnsetBeat = onsetBeat.shallowCopy();
		Beat newOffsetBeat = offsetBeat.shallowCopy();
		
		MidiNote copy = new MidiNote(pitch, velocity, onsetTime, onsetTick, newOnsetBeat, track, guessedVoice);
		copy.setOffset(offsetTime, offsetTick, newOffsetBeat);
		
		newOnsetBeat.setNote(copy);
		newOffsetBeat.setNote(copy);
		
		return copy;
	}
	
	@Override
	public String toString() {
		return String.format("(K:%d  V:%d  [%d-%d] %s %d)", pitch, velocity, onsetTick, offsetTick, onsetBeat.toString(), track);
	}

	@Override
	public int compareTo(MidiNote o) {
		if (o == null) {
			return 1;
		}
		return ((Long) getOnsetTick()).compareTo(o.getOnsetTick());
	}
}

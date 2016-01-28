package voicesplitting.utils;

/**
 * A <code>MidiNote</code> represents a single instance of a played MIDI note. It contains
 * information on the note's pitch, onset, offset, and velocity, as well as its gold standard
 * voice and its guessed voice.
 * <p>
 * MidiNotes are Comparable, and their natural ordering is determined first by each note's
 * {@link #onsetTime}.
 * 
 * @author Andrew McLeod - 23 October, 2014
 * @version 1.0
 * @since 1.0
 */
public class MidiNote implements Comparable<MidiNote> {
	
	/**
	 * The gold standard voice which this note came from.
	 */
	private int correctVoice;
	
	/**
	 * The onset time of this note, measured in microseconds.
	 */
	private final long onsetTime;
	
	/**
	 * The onset tick of this note.
	 */
	private final long onsetTick;
	
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
	private final int velocity;
	
	/**
	 * The key number of this note. For piano, it should be on the range 21 to 108 inclusive.
	 */
	private final int pitch;

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
	 * @param correctVoice {@link #correctVoice}
	 * @param guessedVoice {@link #guessedVoice}
	 */
	public MidiNote(int key, int velocity, long onsetTime, long onsetTick, int correctVoice, int guessedVoice) {
		this.pitch = key;
		this.velocity = velocity;
		this.onsetTime = onsetTime;
		this.onsetTick = onsetTick;
		this.correctVoice = correctVoice;
		offsetTime = 0;
		offsetTick = 0;
		this.guessedVoice = guessedVoice;
	}
	
	/**
	 * Move this note's offset to the given location.
	 * 
	 * @param offsetTime {@link #offsetTime}
	 * @param offsetTick {@link #offsetTick}
	 */
	public void setOffset(long offsetTime, long offsetTick) {
		this.offsetTime = offsetTime;
		this.offsetTick = offsetTick;
	}
	
	/**
	 * Returns whether this note is active (still on) or not.
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
	public void close(long offsetTime, long offsetTick) {
		setOffset(offsetTime, offsetTick);
	}

	/**
	 * Return whether this note overlaps another MidiNote in time and pitch.
	 * 
	 * @param other The note we want to check for overlap. This can be null, in which case
	 * we will return false.
	 * @return True if the notes overlap. False otherwise.
	 */
	public boolean overlaps(MidiNote other) {
		if (other == null) {
			return false;
		}
		
		if (pitch == other.pitch) {
			if (onsetTick < other.offsetTick && offsetTick > other.onsetTick) {
				// We start before the other finishes, and finish after it starts.
				return true;
				
			} else if (other.onsetTick < onsetTick && other.offsetTick > offsetTick) {
				//Vice versa
				return true;
			}
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
	 * Get the velocity of this note.
	 * 
	 * @return {@link #velocity}
	 */
	public int getVelocity() {
		return velocity;
	}

	/**
	 * Get the gold standard voice of this note.
	 * 
	 * @return {@link #correctVoice}
	 */
	public int getCorrectVoice() {
		return correctVoice;
	}
	
	/**
	 * Set the gold standard voice of this note to a new value.
	 * 
	 * @param correctVoice {@link #correctVoice}
	 */
	public void setCorrectVoice(int correctVoice) {
		this.correctVoice = correctVoice;
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
	 * Get the String representation of this object, which is in the following format:
	 * <p>
	 * <code>(K:{@link #pitch}  V:{@link #velocity}  [{@link #onsetTick}-{@link #offsetTick}] {@link #correctVoice})</code>
	 * 
	 * @return The String representation of this KeySignature object.
	 */
	@Override
	public String toString() {
		return String.format("(K:%d  V:%d  [%d-%d] %d)", pitch, velocity, onsetTick, offsetTick, correctVoice);
	}

	/**
	 * Return whether the given Object is equal to this one, which is only the case
	 * when the given Object is a MidiNote, and all of its fields are equal to this one's.
	 * 
	 * @param o The object we are checking for equality.
	 * @return True if the given Object is equal to this one. False otherwise.
	 */
	@Override
	public int compareTo(MidiNote o) {
		if (o == null) {
			return 1;
		}
		
		int result = Long.compare(onsetTick, o.onsetTick);
		if (result != 0) {
			return result;
		}
		
		result = Long.compare(offsetTick, o.offsetTick);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(pitch, o.pitch);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(velocity, o.velocity);
		if (result != 0) {
			return result;
		}
		
		result = Integer.compare(correctVoice,  o.correctVoice);
		if (result != 0) {
			return result;
		}
		
		return Integer.compare(guessedVoice, o.guessedVoice);
	}
}

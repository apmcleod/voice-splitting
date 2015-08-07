package voicesplitting.utils;


/**
 * A <code>Beat</code> represents a single MIDI beat. It stores information about the onset
 * time and tick number of the beat as well as what note occurs on it, and the
 * beat number (in 32nd notes). Beats are Comparable and their natural ordering uses
 * only {@link #beat}, not any absolute timing information.
 * 
 * @author Andrew McLeod - 3 March, 2014
 */
public class Beat implements Comparable<Beat> {
	/**
	 * The beat number on which this Beat lies (measured in 32nd notes)
	 */
	private int beat;
	
	/**
	 * The time in microseconds at which this Beat lies.
	 */
	private long time;
	
	/**
	 * The tick at which this Beat lies.
	 */
	private long tick;
	
	/**
	 * The note which begins on this beat.
	 */
	private MidiNote note;
	
	/**
	 * Create a new default Beat, at time, tick, and beat 0.
	 */
	public Beat() {
		this(0, 0, 0);
	}
	
	/**
	 * Standard constructor for all fields.
	 * 
	 * @param beat {@link #beat}
	 * @param time {@link #time}
	 * @param tick {@link #tick}
	 */
	public Beat(int beat, long time, long tick) {
		this.beat = beat;
		this.time = time;
		this.tick = tick;
	}
	
	/**
	 * Get the number of beats from this one until the given Beat.
	 * 
	 * @param offsetBeat The Beat we want the distance to. offsetBeat must occur after
	 * the current Beat.
	 * @return The number of beats between this Beat and the given offsetBeat.
	 */
	public int getNumBeatsUntil(Beat offsetBeat) {
		return offsetBeat.getBeat() - getBeat();
	}
	
	/**
	 * Return a shallow copy of this Beat.
	 * 
	 * @return A shallow copy of this Beat. 
	 */
	public Beat shallowCopy() {
		Beat ret = new Beat(beat, time, tick);
		ret.setNote(note);
		return ret;
	}
	
	/**
	 * Move this Beat forward a number of beats, keeping all other fields the same.
	 * 
	 * @param step The number of beats forward we wish to move.
	 * @return This Beat. 
	 */
	public Beat increment(int step) {
		beat += step;
		
		return this;
	}
	
	/**
	 * Move this Beat backward a number of beats, keeping all other fields the same.
	 * 
	 * @param step The number of beats backward we wish to move.
	 * @return This Beat.
	 */
	public Beat decrement(int step) {
		return increment(-step);
	}
	
	/**
	 * Set {@link #note} to the given value.
	 * 
	 * @param note The MidiNote which begins on this Beat.
	 */
	public void setNote(MidiNote note) {
		this.note = note;
	}
	
	/**
	 * Get this Beat's note.
	 * 
	 * @return {@link #note}
	 */
	public MidiNote getNote() {
		return note;
	}
	
	/**
	 * Get this Beat's beat number.
	 * 
	 * @return {@link #beat}
	 */
	public int getBeat() {
		return beat;
	}
	
	/**
	 * Set this Beat's beat number.
	 * 
	 * @param newBeat The new value for {@link #beat}.
	 */
	public void setBeat(int newBeat) {
		beat = newBeat;
	}
	
	/**
	 * Get this Beat's time.
	 * 
	 * @return {@link #time}
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * Get this Beat's tick.
	 * 
	 * @return {@link #tick}
	 */
	public long getTick() {
		return tick;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(beat);
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Beat)) {
			return false;
		}
		
		Beat beat = (Beat) other;
		
		return beat.getBeat() == getBeat();
	}

	@Override
	public int compareTo(Beat o) {
		if (o == null) {
			return -1;
		}
		
		return ((Integer) getBeat()).compareTo(o.getBeat());
	}
}

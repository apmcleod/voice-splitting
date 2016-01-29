package voicesplitting.time;

/**
 * A <code>TimeTrackerNode</code> represents the state of a musical score at a given time, and is
 * able to convert between MIDI ticks and seconds. That is, it represents a
 * <{@link voicesplitting.time.TimeSignature}, {@link voicesplitting.time.Tempo},
 * {@link voicesplitting.time.KeySignature}> triple, and contains information about the times at
 * which that triple is valid.
 * <p>
 * All of the TimeTrackerNodes are kept track of by a single master {@link TimeTracker} object
 * per song, which organizes them into a list and queries appropriately based on each one's
 * valid times.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 * @version 1.0
 * @since 1.0
 */
public class TimeTrackerNode {
	/**
	 * The start tick for this TimeTrackerNode. That is, the tick at which this ones triple becomes
	 * valid.
	 * <p>
	 * <code>this.getTimeAtTick({@link #startTick}) == {@link #startTime}</code>
	 * <p>
	 * <code>this.getTickAtTime({@link #startTime}) == {@link #startTick}</code>
	 */
	private long startTick = 0L;
	
	/**
	 * The start time for this TimeTrackerNode, measured in microseconds. That is, the time at which
	 * this one's triple becomes valid.
	 * <p>
	 * <code>this.getTimeAtTick({@link #startTick}) == {@link #startTime}</code>
	 * <p>
	 * <code>this.getTickAtTime({@link #startTime}) == {@link #startTick}</code>
	 */
	private long startTime = 0L;
	
	/**
	 * The TimeSignature associated with this TimeTrackerNode.
	 */
	private TimeSignature timeSignature = null;
	
	/**
	 * The Tempo associated with this TimeTrackerNode.
	 */
	private Tempo tempo = null;
	
	/**
	 * The KeySignature associated with this TimeTrackerNode.
	 */
	private KeySignature keySignature = null;
	
	/**
	 * Create a new TimeTrackerNode with the given previous TimeTrackerNode at the given tick.
	 * 
	 * @param prev The previous TimeTrackerNode
	 * @param ppq The pulses per quarter note of the song.
	 * @param tick The tick at which this new one becomes valid.
	 */
	public TimeTrackerNode(TimeTrackerNode prev, long tick, double ppq) {
		startTick = tick;
		
		if (prev != null) {
			startTime = prev.getTimeAtTick(tick, ppq);
			timeSignature = prev.getTimeSignature();
			tempo = prev.getTempo();
			keySignature = prev.getKeySignature();
			
		} else {
			timeSignature = new TimeSignature();
			tempo = new Tempo();
			keySignature = new KeySignature();
		}
	}
	
	/**
	 * Get the tick number at the given time.
	 * 
	 * @param time The time at which we want the tick, measured in microseconds.
	 * @param ppq The pulses per quarter note of the song.
	 * @return The tick at the given time.
	 */
	public long getTickAtTime(long time, double ppq) {
		long timeOffset = time - getStartTime();
		return (long) (timeOffset / getTimePerTick(ppq)) + getStartTick();
	}
	
	/**
	 * Get the time at the given tick.
	 * 
	 * @param tick The tick at which we want the time.
	 * @param ppq The pulses per quarter note of the song.
	 * @return The time at the given tick, measured in microseconds.
	 */
	public long getTimeAtTick(long tick, double ppq) {
		long tickOffset = tick - getStartTick();
		return (long) (tickOffset * getTimePerTick(ppq)) + getStartTime();
	}
	
	/**
	 * Gets the amount of time, in microseconds, that passes between each tick.
	 * 
	 * @param ppq The pulses per quarter note of the song.
	 * @return The length of a tick in microseconds.
	 */
	private double getTimePerTick(double ppq) {
		return tempo.getMicroSecondsPerQuarter() / ppq;
	}
	
	/**
	 * Get the start tick of this node.
	 * 
	 * @return {@link #startTick}
	 */
	public long getStartTick() {
		return startTick;
	}
	
	/**
	 * Get the start time of this node.
	 * 
	 * @return {@link #startTime}
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Set the Tempo of this node.
	 * 
	 * @param tempo {@link #tempo}
	 */
	public void setTempo(Tempo tempo) {
		this.tempo = tempo;
	}
	
	/**
	 * Set the TimeSignature of this node.
	 * 
	 * @param timeSignature {@link #timeSignature}
	 */
	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}
	
	/**
	 * Set the KeySignature of this node.
	 * 
	 * @param keySignature {@link #keySignature}
	 */
	public void setKeySignature(KeySignature keySignature) {
		this.keySignature = keySignature;
	}
	
	/**
	 * Get the TimeSignature of this node.
	 * 
	 * @return {@link #timeSignature}
	 */
	public TimeSignature getTimeSignature() {
		return timeSignature;
	}
	
	/**
	 * Get the Tempo of this node.
	 * 
	 * @return {@link #tempo}
	 */
	public Tempo getTempo() {
		return tempo;
	}
	
	/**
	 * Get the KeySignature of this node.
	 * 
	 * @return {@link #keySignature}
	 */
	public KeySignature getKeySignature() {
		return keySignature;
	}
	
	/**
	 * Get the String representation of this TimeTrackerNode, which is its {@link #startTick},
	 * {@link #startTime}, {@link #keySignature}, {@link #timeSignature}, and {@link #tempo},
	 * all within braces.
	 * 
	 * @return The String representation of this TimeTrackerNode.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{Tick=");
		sb.append(startTick);
		sb.append(" Time=").append(startTime);
		sb.append(" ").append(getKeySignature());
		sb.append(" ").append(getTimeSignature());
		sb.append(" ").append(getTempo()).append('}');
		return sb.toString();
	}
}

package voicesplitting.time;

import voicesplitting.utils.Beat;

/**
 * A <code>TimeTrackerNode</code> represents the state of a musical score at a given time. That is,
 * it represents a <{@link voicesplitting.time.TimeSignature},
 * {@link voicesplitting.time.Tempo}, {@link voicesplitting.time.KeySignature}>
 * triple, and contains information about the times at which that triple is contiguously valid.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class TimeTrackerNode {
	/**
	 * The start tick for this TimeTrackerNode. That is, the tick at which this ones triple becomes
	 * valid.
	 */
	private long startTick = 0L;
	
	/**
	 * The start time for this TimeTrackerNode, measured in microseconds. That is, the time at which
	 * this one's triple becomes valid.
	 */
	private long startTime = 0L;
	
	/**
	 * The Beatat which this TimeTrackerNode becomes valid. This value is floored, and is
	 * measured in 32nd notes since the beginning of the current measure.
	 */
	private Beat startBeat = null;
	
	/**
	 * The number of ticks that have passed between the beginning of the startBeat until the beginning
	 * of this TimeTrackerNode's validity.
	 */
	private int startTicksAfterBeat = 0;
	
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
	 * @param tick The tick at which this new one becomes valid.
	 */
	public TimeTrackerNode(TimeTrackerNode prev, long tick) {
		startTick = tick;
		
		if (prev != null) {
			startTime = prev.getTimeAtTick(tick);
			startBeat = prev.getBeatAtTick(tick);
			startTicksAfterBeat = prev.getRemainderTicks(tick);
			timeSignature = prev.getTimeSignature();
			tempo = prev.getTempo();
			keySignature = prev.getKeySignature();
			
			if (startTicksAfterBeat >= getTimeSignature().getTicksPerNote32() / 2) {
				startBeat.decrement(1);
			}
			
		} else {
			startBeat = new Beat();
			timeSignature = new TimeSignature();
			tempo = new Tempo();
			keySignature = new KeySignature();
		}
	}
	
	/**
	 * Gets the number of ticks between the given tick number and the beat immediately preceeding it.
	 * 
	 * @param tick The tick which we want to measure
	 * @return The number of ticks between the given tick and the preceeding beat.
	 */
	public int getRemainderTicks(long tick) {
		long elapsedTicks = tick - startTick + startTicksAfterBeat;
		return (int) (elapsedTicks % timeSignature.getTicksPerNote32());
	}
	
	/**
	 * Get the Beat at the given tick, rounded to the nearest whole number.
	 * 
	 * @param tick The tick at which we want the beat number.
	 * @return The Beat closest to the given tick.
	 */
	public Beat getBeatAtTick(long tick) {
		long elapsedTicks = tick - startTick + startTicksAfterBeat;
		int elapsedBeats = (int) ((elapsedTicks + (timeSignature.getTicksPerNote32() / 2)) / timeSignature.getTicksPerNote32());
		return startBeat.shallowCopy().increment(elapsedBeats);
	}

	/**
	 * Get the Beat at the given time, rounded to the nearest whole number.
	 * 
	 * @param time The time at which we want the beat number, measured in microseconds.
	 * @return The Beat closest to the given time.
	 */
	public Beat getBeatAtTime(long time) {
		return getBeatAtTick(getTickAtTime(time));
	}
	
	/**
	 * Get the tick number at the given time.
	 * 
	 * @param time The time at which we want the tick, measured in microseconds.
	 * @return The tick at the given time.
	 */
	public long getTickAtTime(long time) {
		long timeOffset = time - getStartTime();
		return (long) (timeOffset / getTimePerTick()) + getStartTick();
	}
	
	/**
	 * Get the time at the given tick.
	 * 
	 * @param tick The tick at which we want the time.
	 * @return The time at the given tick, measured in microseconds.
	 */
	public long getTimeAtTick(long tick) {
		long tickOffset = tick - getStartTick();
		return (long) (tickOffset * getTimePerTick()) + getStartTime();
	}
	
	/**
	 * Gets the amount of time, in microseconds, that passes between each tick.
	 * 
	 * @return The length of a tick in microseconds.
	 */
	private double getTimePerTick() {
		return tempo.getMicroSecondsPerQuarter() / TimeTracker.PPQ;
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
	 * @param tempo
	 */
	public void setTempo(Tempo tempo) {
		this.tempo = tempo;
	}
	
	/**
	 * Set the TimeSignature of this node.
	 * 
	 * @param timeSignature
	 */
	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}
	
	/**
	 * Set the KeySignature of this node.
	 * 
	 * @param keySignature
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

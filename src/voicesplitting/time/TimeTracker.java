package voicesplitting.time;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

/**
 * A <code>TimeTracker</code> is able to interpret MIDI tempo, key, and time signature change events and keep track
 * of the song timing in seconds, instead of just using ticks as MIDI events do. It does this by using
 * a LinkedList of {@link TimeTrackerNode} objects.
 * 
 * @author Andrew McLeod - 23 October, 2014
 * @version 1.0
 * @since 1.0
 */
public class TimeTracker {
	/**
	 * Pulses (ticks) per Quarter note, as in the current Midi song's header.
	 */
	private double PPQ;
	
	/**
	 * The LInkedList of TimeTrackerNodes of this TimeTracker, ordered by start time.
	 */
	private final LinkedList<TimeTrackerNode> nodes;
	
	/**
	 * The last tick for any event in this song, initially 0.
	 */
	private long lastTick = 0;
    
	/**
	 * Create a new TimeTracker initially with a single {@link TimeTrackerNode} with default
	 * settings.
	 */
    public TimeTracker() {
    	nodes = new LinkedList<TimeTrackerNode>();
    	nodes.add(new TimeTrackerNode(null, 0L, PPQ));
    }
    
    /**
     * A {@link TimeSignature} event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addTimeSignatureChange(MidiEvent event, MetaMessage mm) {
    	TimeSignature ts = new TimeSignature(mm.getData());
    	
    	if (!ts.equals(nodes.getLast().getTimeSignature())) {
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setTimeSignature(ts);
    	}
    }
    
    /**
     * A {@link Tempo} event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addTempoChange(MidiEvent event, MetaMessage mm) {
    	Tempo t = new Tempo(mm.getData());
    	
    	if (!t.equals(nodes.getLast().getTempo())) {
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setTempo(t);
    	}
    }
    
    /**
     * A {@link KeySignature} event was detected. Deal with it.
     * 
     * @param event The event.
     * @param mm The message from the event.
     */
    public void addKeySignatureChange(MidiEvent event, MetaMessage mm) {
    	KeySignature ks = new KeySignature(mm.getData());
    	
    	if (!ks.equals(nodes.getLast().getKeySignature())) {
    		nodes.add(new TimeTrackerNode(nodes.getLast(), event.getTick(), PPQ));
    		nodes.getLast().setKeySignature(ks);
    	}
	}
    
    /**
     * Returns the time in microseconds of a given tick number.
     * 
     * @param tick The tick number to calculate the time of
     * @return The time of the given tick number, measured in microseconds since the most recent epoch.
     */
    public long getTimeAtTick(long tick) {
    	return getNodeAtTick(tick).getTimeAtTick(tick, PPQ);
    }
    
    /**
     * Get the {@link TimeTrackerNode} which is valid at the given tick.
     * 
     * @param tick The tick.
     * @return The valid TimeTrackerNode.
     */
    private TimeTrackerNode getNodeAtTick(long tick) {
    	ListIterator<TimeTrackerNode> iterator = nodes.listIterator();
    	
    	TimeTrackerNode node = iterator.next();
    	while (iterator.hasNext()) {
    		node = iterator.next();
    		
    		if (node.getStartTick() > tick) {
    			iterator.previous();
    			return iterator.previous();
    		}
    	}

    	return node;
    }
    
    /**
     * Gets the tick number at the given time, measured in microseconds.
     * 
     * @param time The time in microseconds whose tick number we want.
     * @return The tick number which corresponds to the given time.
     */
    public long getTickAtTime(long time) {
    	return getNodeAtTime(time).getTickAtTime(time, PPQ);
    }
    
    /**
     * Get the TimeTrackerNode which is valid at the given time.
     * 
     * @param time The time.
     * @return The valid TimeTrackerNode.
     */
    private TimeTrackerNode getNodeAtTime(long time) {
    	ListIterator<TimeTrackerNode> iterator = nodes.listIterator();
    	
    	TimeTrackerNode node = iterator.next();
    	while (iterator.hasNext()) {
    		node = iterator.next();
    		
    		if (node.getStartTime() > time) {
    			iterator.previous();
    			return iterator.previous();
    		}
    	}

    	return node;
    }
    
    /**
     * Get a list of the {@link TimeTrackerNode}s tracked by this object.
     * 
     * @return {@link #nodes}
     */
    public LinkedList<TimeTrackerNode> getNodes() {
    	return nodes;
    }
    
    /**
     * Set the last tick for this song to the given value.
     * 
     * @param lastTick {@link #lastTick}
     */
    public void setLastTick(long lastTick) {
		this.lastTick = lastTick;
	}
    
    /**
     * Get the last tick for this song.
     * 
     * @return {@link #lastTick}
     */
    public long getLastTick() {
    	return lastTick;
    }
    
    /**
     * Set the PPQ for this TimeTracker.
     * 
     * @param ppq {@link #PPQ}
     */
    public void setPPQ(double ppq) {
    	PPQ = ppq;
    }
    
    /**
     * Get the PPQ of this TimeTracker.
     * 
     * @return {@link #PPQ}
     */
    public double getPPQ() {
    	return PPQ;
    }
	
    /**
     * Get the String representation of this object, which is exactly {@link #nodes}.
     * 
     * @return The String representation of this object.
     */
	@Override
	public String toString() {
		return nodes.toString();
	}
}

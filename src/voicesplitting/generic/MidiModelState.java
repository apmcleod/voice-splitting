package voicesplitting.generic;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiModelState</code> represents the state of any {@link MidiModel}.
 * <p>
 * Each state has a score, accessible by the {@link #getScore()} method, and their
 * natural ordering should be based on this score.
 * <p>
 * Anyone wishing to create a new type of MIDI Model (besides a voice splitter) should
 * extend this class to another abstract class defining that type of model's state, as
 * was done in {@link voicesplitting.voice.VoiceSplittingModelState}.
 * <p>
 * <b>IMPORTANT</b>: The correct functionality of a MidiModelState is dependent on any non-abstract
 * subclass implementing the <code>Comparable</code> interface, ordering more likely
 * MidiModelStates more highly. This is due to use of java's <code>TreeSet</code> object,
 * which sorts based on the <code>compareTo</code> method of each object. Most likely,
 * you'll want to use {@link #getScore()} as the main metric for comparison, using
 * other fields to break ties as needed.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 * @version 1.0
 * @since 1.0
 */
public abstract class MidiModelState {

	/**
	 * Get the score of this MidiModelState. In general, a higher score reflects
	 * that a State is more likely given our prior and observed knowledge.
	 * <p>
	 * In almost every case, the natural ordering of MidiModelStates should be based on
	 * this score value.
	 * 
	 * @return The score of this MidiModelState.
	 */
	public abstract double getScore();
	
	/**
	 * Return a TreeSet of all of the possible MidiModelStates which we could tansition into
	 * given the List of MidiNotes.
	 * <p>
	 * NOTE: It is assumed that the notes Lists passed into this method will be passed
	 * chronologically. Specifically, each time this method is invoked, it should be passed
	 * the List of MidiNotes which occur next in the MIDI song currently being read.
	 * <p>
	 * Usually, this method is simply called by some {@link MidiModel}'s
	 * {@link MidiModel#handleIncoming(List)} method.
	 * 
	 * @param notes A List of the MidiNotes on which we need to transition.
	 * @return A TreeSet of MidiModelStates which we've transitioned into. The new MidiModelStates
	 * should not hold any common mutable objects, since they may modify them in the future.
	 */
	public abstract TreeSet<? extends MidiModelState> handleIncoming(List<MidiNote> notes);
}

package voicesplitting.generic;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiModelState</code> contains the state of any of our {@link MidiModel}s.
 * Each state has a score, accessible by the {@link #getScore()} method, and their
 * natural ordering is based on this score.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 */
public abstract class MidiModelState {

	/**
	 * Get the score of this MidiModelState. In general, a higher score reflects
	 * that a State is more likely given our prior knowledge. The natural ordering
	 * of MidiModelStates is based on this score value.
	 * 
	 * @return The score of this MidiModelState.
	 */
	public abstract double getScore();
	
	/**
	 * Return a TreeSet of all of the possible MidiModelStates which we could tansition into
	 * given the List of MidiNotes.
	 * 
	 * @param notes A List of the MidiNotes on which we need to transition.
	 * @return A TreeSet of MidiModelStates which we've transitioned into.
	 */
	public abstract TreeSet<? extends MidiModelState> handleIncoming(List<MidiNote> notes);
}

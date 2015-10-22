package voicesplitting.generic;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiModel</code> is a generic interface which defines any model which takes
 * a series of MidiNotes as input through the {@link #handleIncoming(List)} method,
 * and generates some sort of hypotheses, which can be obtained through the
 * {@link #getHypotheses()} method.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 */
public abstract class MidiModel {
	
	/**
	 * This method takes as input some List of MidiNotes, and then does some work on
	 * the notes, updating its list of hypotheses in the process.
	 *  
	 * @param notes A List of the MidiNotes which we want to handle next.
	 */
	public abstract void handleIncoming(List<MidiNote> notes);
	
	/**
	 * This method returns a TreeSet of the current hypothesis states of this MidiModel.
	 * 
	 * @return A TreeSet of the current hypothesis states of this MidiModel, sorted by
	 * score.
	 */
	public abstract TreeSet<? extends MidiModelState> getHypotheses();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		
		for (MidiModelState state : getHypotheses()) {
			sb.append(state).append(',');
		}
		
		sb.setCharAt(sb.length() - 1, '}');
		return sb.toString();
	}
}

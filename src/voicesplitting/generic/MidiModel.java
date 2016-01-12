package voicesplitting.generic;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiModel</code> is a generic interface which defines any model which takes
 * a series of MidiNotes incrimentally as input through the {@link #handleIncoming(List)}
 * method, and generates some sort of hypotheses, which can be obtained through the
 * {@link #getHypotheses()} method.
 * <p>
 * Anyone wishing to create a new type of Midi Model (besides a voice splitter) should
 * extend this class to another abstract class defining that type of model, as was done
 * in {@link voicesplitting.voice.VoiceSplittingModel}.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 */
public abstract class MidiModel {
	
	/**
	 * This method takes as input some List of MidiNotes, and then does some work on
	 * the notes, updating its list of hypotheses in the process.
	 * <p>
	 * NOTE: It is assumed that the notes Lists passed into this method will be passed
	 * chronologically. Specifically, each time this method is invoked, it should be passed
	 * the List of MidiNotes which occur next in the MIDI song currently being read.
	 * <p>
	 * Usually, this method should do little more than simply passing along the notes list
	 * to its {@link MidiModelState}'s {@link MidiModelState#handleIncoming(List)} method
	 * and updating current hypotheses with the result.
	 *  
	 * @param notes A List of the MidiNotes which we want to handle next.
	 */
	public abstract void handleIncoming(List<MidiNote> notes);
	
	/**
	 * This method returns a TreeSet of the current hypothesis states of this MidiModel.
	 * <p>
	 * The most highly scoring state can be retrieved by using <code>getHypotheses().first()</code>.
	 * <p>
	 * NOTE: This is dependent on proper implimentation of the {@link MidiModeState}'s
	 * <code>compareTo</code> method.
	 * 
	 * @return A TreeSet of the current hypothesis states of this MidiModel, in their natural order.
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

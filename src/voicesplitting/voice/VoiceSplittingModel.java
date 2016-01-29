package voicesplitting.voice;

import java.util.TreeSet;

import voicesplitting.generic.MidiModel;

/**
 * A <code>VoiceSplittingModel</code> is an abstract class representing any {@link MidiModel}
 * which is used to split MIDI data into voices. Any new voice splitting model should
 * implement this class.
 * <p>
 * Note that the {@link #getHypotheses()} method here now returns <code>TreeSet<? extends VoiceSplittingModelState></code>
 * rather than <code>TreeSet<? extends MidiModelState></code> as in {@link MidiModel#getHypotheses()}.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 * @version 1.0
 * @since 1.0
 */
public abstract class VoiceSplittingModel extends MidiModel {
	/**
	 * This method returns a TreeSet of the current hypothesis {@link VoiceSplittingModelState}s
	 * of this VoiceSplittingModel.
	 * <p>
	 * The most highly scoring state can be retrieved by using <code>getHypotheses().first()</code>.
	 * <p>
	 * NOTE: This is dependent on proper implimentation of the {@link VoiceSplittingModelState}'s
	 * <code>compareTo</code> method.
	 * 
	 * @return A TreeSet of the current hypothesis states of this VoiceSplittingModel, in their natural order.
	 */
	@Override
	public abstract TreeSet<? extends VoiceSplittingModelState> getHypotheses();
}

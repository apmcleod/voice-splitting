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
	@Override
	public abstract TreeSet<? extends VoiceSplittingModelState> getHypotheses();
}

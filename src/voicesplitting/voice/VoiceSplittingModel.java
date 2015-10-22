package voicesplitting.voice;

import java.util.TreeSet;

import voicesplitting.generic.MidiModel;

/**
 * A <code>VoiceSplittingModel</code> is any {@link MidiModel} which is used
 * to split MIDI data into seperate voices.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 */
public abstract class VoiceSplittingModel extends MidiModel {
	@Override
	public abstract TreeSet<? extends VoiceSplittingModelState> getHypotheses();
}

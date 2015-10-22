package voicesplitting.voice;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.generic.MidiModelState;
import voicesplitting.utils.MidiNote;

/**
 * A <code>VoiceSplittingModelState</code> is a {@link MidiModelState} which contains
 * a List of {@link Voice}s into which the incoming MIDI data has been split. In
 * order to get these voices, the {@link #getVoices()} method should be called.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 */
public abstract class VoiceSplittingModelState extends MidiModelState {
	/**
	 * Gets the Voices which are contained by this state currently.
	 * 
	 * @return A List of the Voices contained by this State.
	 */
	public abstract List<Voice> getVoices();
	
	@Override
	public abstract TreeSet<? extends VoiceSplittingModelState> handleIncoming(List<MidiNote> notes);
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		
		for (Voice voice : getVoices()) {
			sb.append(voice).append(',');
		}
		
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}
}

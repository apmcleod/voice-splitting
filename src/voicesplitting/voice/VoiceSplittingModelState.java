package voicesplitting.voice;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.generic.MidiModelState;
import voicesplitting.utils.MidiNote;

/**
 * A <code>VoiceSplittingModelState</code> is an abstract {@link MidiModelState} which
 * splits incoming {@link MidiNote}s into {@link Voice}s. In order to get these voices,
 * the {@link #getVoices()} method should be called.
 * <p>
 * Note that the {@link #handleIncoming(List)} method here now returns
 * <code>TreeSet<? extends VoiceSplittingModelState></code> rather than
 * <code>TreeSet<? extends MidiModelState></code> as in {@link MidiModelState#handleIncoming(List)}.
 * 
 * @author Andrew McLeod - 4 Sept, 2015
 * @version 1.0
 * @since 1.0
 */
public abstract class VoiceSplittingModelState extends MidiModelState {
	/**
	 * Gets the Voices which are currently contained by this state.
	 * 
	 * @return A List of the Voices contained by this State.
	 */
	public abstract List<Voice> getVoices();
	
	/**
	 * Return a TreeSet of all of the possible VoiceSplittingModelStates which we could tansition into
	 * given the List of MidiNotes.
	 * <p>
	 * NOTE: It is assumed that the notes Lists passed into this method will be passed
	 * chronologically. Specifically, each time this method is invoked, it should be passed
	 * the List of MidiNotes which occur next in the MIDI song currently being read.
	 * <p>
	 * Usually, this method is simply called by some {@link VoiceSplittingModel}'s
	 * {@link VoiceSplittingModel#handleIncoming(List)} method.
	 * 
	 * @param notes A List of the MidiNotes on which we need to transition.
	 * @return A TreeSet of VoiceSplittingModelStates which we've transitioned into. The new VoiceSplittingModelStates
	 * should not hold any common mutable objects, since they may modify them in the future.
	 */
	@Override
	public abstract TreeSet<? extends VoiceSplittingModelState> handleIncoming(List<MidiNote> notes);
	
	/**
	 * Get the String representation of this object, which is simply the List of Voices returned by
	 * {@link #getVoices()}.
	 * 
	 * @return The String representation of this object.
	 */
	@Override
	public String toString() {
		return getVoices().toString();
	}
}

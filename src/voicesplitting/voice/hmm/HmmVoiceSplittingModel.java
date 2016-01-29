package voicesplitting.voice.hmm;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;
import voicesplitting.voice.Voice;
import voicesplitting.voice.VoiceSplittingModel;

/**
 * An <code>HmmVoiceSplittingModel</code> is the model that performs voice separation as described
 * in the paper.
 * <p>
 * It contains a modified HMM, where the states are stored as {@link HmmVoiceSplittingModelState}s.
 * 
 * @author Andrew McLeod - 7 April, 2015
 * @version 1.0
 * @since 1.0
 */
public class HmmVoiceSplittingModel extends VoiceSplittingModel {
	
	/**
	 * A TreeSet of the {@link HmmVoiceSplittingModelState}s containing the most likely Voices
	 * for the given song. 
	 */
	private TreeSet<HmmVoiceSplittingModelState> hypothesisStates;
	
	/**
	 * The parameters we are going to use for this run.
	 */
	private HmmVoiceSplittingModelParameters params;
	
	/**
	 * Create a new HmmVoiceSplittingModel with the given parameters.
	 * 
	 * @param params {@link #params}
	 */
	public HmmVoiceSplittingModel(HmmVoiceSplittingModelParameters params) {
		this.params = params;
		
		hypothesisStates = new TreeSet<HmmVoiceSplittingModelState>();
		hypothesisStates.add(new HmmVoiceSplittingModelState(params));
	}

	/**
	 * This method returns a TreeSet of the current hypothesis {@link HmmVoiceSplittingModelState}s
	 * of this HmmVoiceSplittingModel.
	 * <p>
	 * The most highly scoring state can be retrieved by using <code>getHypotheses().first()</code>.
	 * <p>
	 * NOTE: This is dependent on proper implimentation of the {@link HmmVoiceSplittingModelState}'s
	 * <code>compareTo</code> method.
	 * 
	 * @return A TreeSet of the current hypothesis states of this HmmVoiceSplittingModel, in their natural order.
	 */
	@Override
	public TreeSet<HmmVoiceSplittingModelState> getHypotheses() {
		return hypothesisStates;
	}

	/**
	 * This method takes as input some List of {@link MidiNote}s, and then does some work on
	 * the notes, updating its list of hypothesis {@link HmmVoiceSplittingModelState}s in the process.
	 * Here, we also ensure that the {@link #hypothesisStates} list doesn't grow larger than
	 * {@link HmmVoiceSplittingModelParameters#BEAM_SIZE}.
	 * <p>
	 * NOTE: It is assumed that the note Lists passed into this method will be passed
	 * chronologically. Specifically, each time this method is invoked, it should be passed
	 * the List of MidiNotes which occur next in the MIDI song currently being read. This is
	 * done automatically by the {@link voicesplitting.parsing.NoteListGenerator#getIncomingLists()}
	 * method.
	 * <p>
	 * Usually, this method should do little more than simply passing along the notes list
	 * to its {@link HmmVoiceSplittingModelState}'s {@link HmmVoiceSplittingModelState#handleIncoming(List)}
	 * method and updating current hypotheses with the result.
	 *  
	 * @param notes A List of the MidiNotes which we want to handle next.
	 */
	@Override
	public void handleIncoming(List<MidiNote> notes) {
		TreeSet<HmmVoiceSplittingModelState> newStates = new TreeSet<HmmVoiceSplittingModelState>();
		
		for (HmmVoiceSplittingModelState state : hypothesisStates) {
			newStates.addAll(state.handleIncoming(notes));
			
			while (newStates.size() > params.BEAM_SIZE) {
				newStates.pollLast();
			}
		}
		
		hypothesisStates = newStates;
	}
	
	/**
	 * Get the F1-measure of the most likely {@link HmmVoiceSplittingModelState}'s {@link Voice}s
	 * from the {@link #hypothesisStates} list.
	 * 
	 * @param goldStandard The gold standard voices for the current song.
	 * @return The F1-measure of the most likely {@link HmmVoiceSplittingModelState}'s {@link Voice}s
	 * from the {@link #hypothesisStates} list, or 0 if that list is empty.
	 */
	public double getF1(List<List<MidiNote>> goldStandard) {
		if (hypothesisStates == null || hypothesisStates.isEmpty()) {
			return 0.0;
		}
		
		List<Voice> voices = hypothesisStates.first().getVoices();
		
		int totalPositives = 0;
		int truePositives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;

		for (List<MidiNote> goldVoice : goldStandard) {
			if (goldVoice.size() != 0) {
				totalPositives += goldVoice.size() - 1;
			}
		}
		
		for (Voice voice : voices) {
			int voiceTruePositives = voice.getNumLinksCorrect(goldStandard);
			int voiceFalsePositives = voice.getNumNotes() - 1 - voiceTruePositives;
			
			truePositives += voiceTruePositives;
			falsePositives += voiceFalsePositives;
		}
		
		falseNegatives = totalPositives - truePositives;
		
		double precision = ((double) truePositives) / (truePositives + falsePositives);
		double recall = ((double) truePositives) / (truePositives + falseNegatives);
		
		double f1 = 2 * precision * recall / (precision + recall);
		
		return f1;
	}
}

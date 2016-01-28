package voicesplitting.voice.hmm;

import java.util.List;
import java.util.TreeSet;

import voicesplitting.utils.MidiNote;
import voicesplitting.voice.Voice;
import voicesplitting.voice.VoiceSplittingModel;

/**
 * This is the Voice Splitting algorithm submitted to ISMIR 2015. It involves a modified
 * hmm algorithm.
 * 
 * @author Andrew McLeod - 7 April, 2015
 * @version 1.0
 * @since 1.0
 */
public class HmmVoiceSplittingModel extends VoiceSplittingModel {
	
	/**
	 * A TreeSet of the States containing the most likely voice splits for the given song. 
	 */
	private TreeSet<HmmVoiceSplittingModelState> hypothesisStates;
	
	/**
	 * The parameters we are going to use for this run.
	 */
	private HmmVoiceSplittingModelParameters params;
	
	/**
	 * Create a new HmmVoiceSplitter.
	 * 
	 * @param params The parameters we want to use for this Voice Split.
	 */
	public HmmVoiceSplittingModel(HmmVoiceSplittingModelParameters params) {
		this.params = params;
		
		hypothesisStates = new TreeSet<HmmVoiceSplittingModelState>();
		hypothesisStates.add(new HmmVoiceSplittingModelState(params));
	}

	@Override
	public TreeSet<HmmVoiceSplittingModelState> getHypotheses() {
		return hypothesisStates;
	}

	@Override
	public void handleIncoming(List<MidiNote> incoming) {
		TreeSet<HmmVoiceSplittingModelState> newStates = new TreeSet<HmmVoiceSplittingModelState>();
		
		for (HmmVoiceSplittingModelState state : hypothesisStates) {
			newStates.addAll(state.handleIncoming(incoming));
			
			while (newStates.size() > params.BEAM_SIZE) {
				newStates.pollLast();
			}
		}
		
		hypothesisStates = newStates;
	}
	
	/**
	 * Get the F1 of this split.
	 * 
	 * @param goldStandard The gold standard voices of this song.
	 * @return The F1 of this split.
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

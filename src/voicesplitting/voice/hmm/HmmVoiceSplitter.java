package voicesplitting.voice.hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import voicesplitting.gui.VoiceSplittingGUI;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.SingleNoteVoice;
import voicesplitting.voice.VoiceSplitter;

/**
 * This is the Voice Splitting algorithm submitted to ISMIR 2015. It involves a modified
 * hmm algorithm.
 * 
 * @author Andrew McLeod - 7 April, 2015
 */
public class HmmVoiceSplitter implements VoiceSplitter {

	/**
	 * A List of the MidiNotes that comprise a song. These should be in order of ascending
	 * onset time, which is the MidiNote's natural ordering.
	 */
	private List<MidiNote> song;
	
	/**
	 * A List of the Voices which we've split the {@link #song} into. This is created by a call
	 * to {@link #getVoices()}. Note that {@link #getVoices()} will call {@link #generateVoices()}
	 * upon its first run if it has not yet been called. 
	 */
	private List<SingleNoteVoice> voices;
	
	/**
	 * The parameters we are going to use for this run.
	 */
	private VoiceSplittingParameters params;
	
	/**
	 * Create a new HmmVoiceSplitter on the given song.
	 * 
	 * @param song {@link #song}
	 * @param params The parameters we want to use for this Voice Split.
	 */
	public HmmVoiceSplitter(List<MidiNote> song, VoiceSplittingParameters params) {
		this.song = song;
		this.params = params;
	}
	
	/**
	 * Get the F1 of this split.
	 * 
	 * @param goldStandard The gold standard voices of this song.
	 * @return The F1 of this split.
	 */
	public double getF1(List<List<MidiNote>> goldStandard) {
		if (voices == null) {
			return 0.0;
		}
		
		int totalPositives = 0;
		int truePositives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;

		for (List<MidiNote> goldVoice : goldStandard) {
			if (goldVoice.size() != 0) {
				totalPositives += goldVoice.size() - 1;
			}
		}
		
		for (SingleNoteVoice voice : voices) {
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
	
	/**
	 * Get the voices we've split the given {@link #song} into. This will automatically call
	 * {@link #generateVoices()} if it hasn't been yet. Otherwise, it simply returns {@link #voices}.
	 * 
	 * @return {@link #voices}
	 * @throws InterruptedException If we're running on a GUI and this gets cancelled.
	 */
	@Override
	public List<SingleNoteVoice> getVoices() throws InterruptedException {
		if (voices == null) {
			generateVoices();
			
			for (int i = 0; i < voices.size(); i++) {
				for (MidiNote note : voices.get(i).getNotes()) {
					note.setGuessedVoice(i);
				}
			}
		}
		
		return voices;
	}

	/**
	 * Generate the {@link #voices} list. The first call to {@link #getVoices()}
	 * will call this method automatically.
	 * 
	 * @throws InterruptedException If we're running on a GUI and this gets cancelled. 
	 */
	private void generateVoices() throws InterruptedException {
		TreeSet<State> states = new TreeSet<State>();
		states.add(new State(params));
		
		int i = 0;
		while (i < song.size()) {
			if (VoiceSplittingGUI.usingGui) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
			}
			// The set of notes which occur at the next quantized onset time.
			List<MidiNote> incoming = new ArrayList<MidiNote>();
			
			// Save the onset time we're currently on
			long onsetTime = song.get(i).getOnsetTime();
			
			// Add the note and increment while we're still on the proper onset time.
			do {
				incoming.add(song.get(i++));
			} while (i < song.size() && song.get(i).getOnsetTime() == onsetTime);
			
			// Here, we have a set which is every note that occurs at the next quantized time.
			states = handleIncoming(incoming, states);
		}
		
		voices = states.first().getVoices();
	}

	/**
	 * Divide the notes in the given Set into voices.
	 * 
	 * @param incoming A List of MidiNotes, all of which have the same onset time. Additionally,
	 * no note should have this onset time without being in this Set. This List will not be empty.
	 * @param states A SortedSet of possible States at this step in the algorithm.
	 * @return A Set of the new states which we have transitioned into.
	 */
	private TreeSet<State> handleIncoming(List<MidiNote> incoming, TreeSet<State> states) {
		TreeSet<State> newStates = new TreeSet<State>();
		
		for (State state : states) {
			newStates.addAll(state.getAllCandidateNewStates(incoming));
			
			while (newStates.size() > params.BEAM_SIZE) {
				newStates.pollLast();
			}
		}
		
		return newStates;
	}
}

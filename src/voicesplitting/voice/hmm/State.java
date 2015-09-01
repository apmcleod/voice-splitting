package voicesplitting.voice.hmm;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import voicesplitting.utils.MathUtils;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.SingleNoteVoice;

/**
 * A <code>State</code> is used by the {@link HmmVoiceSplitter} and contains a List of the
 * {@link SingleNoteVoice}s which are currently present in the HMM as well as its
 * cumulative probability so far.
 * 
 * @author Andrew McLeod - 7 April, 2015
 */
public class State implements Comparable<State> {

	/**
	 * A List of the Voices present in this State.
	 */
	private List<SingleNoteVoice> voices;
	
	/**
	 * The log probability of appearing in this State.
	 */
	private double logProb;
	
	/**
	 * The parameters we are using.
	 */
	private VoiceSplittingParameters params;
	
	/**
	 * Create a new default State with logProb = 0 (ie. prob = 1)
	 * 
	 * @param params {@link #params}
	 */
	public State(VoiceSplittingParameters params) {
		this(0, params);
	}
	
	/**
	 * Create a new empty State with the given log probability.
	 * 
	 * @param logProb {@link #logProb}
	 * @param params {@link #params} 
	 */
	public State(double logProb, VoiceSplittingParameters params) {
		voices = new ArrayList<SingleNoteVoice>();
		this.logProb = logProb;
		this.params = params;
	}
	
	/**
	 * Create a new State with the given log probability and voices.
	 * 
	 * @param logProb {@link #logProb}
	 * @param voices {@link #voices}
	 * @param params {@link #params}
	 */
	private State(double logProb, List<SingleNoteVoice> voices, VoiceSplittingParameters params) {
		this.voices = voices;
		this.logProb = logProb;
		this.params = params;
	}
	
	/**
	 * Get all of the candidate states based on transitions from this State given the incoming notes.
	 * 
	 * @param incoming A List of incoming notes.
	 * @return A List of possible States which we could transition into.
	 */
	public SortedSet<State> getAllCandidateNewStates(List<MidiNote> incoming) {
		// We need a deep copy of voices in case there are more transitions to perform
		// on this State
		return getAllCandidateNewStatesRecursive(getOpenVoiceIndices(incoming, voices), incoming, voices, logProb, 0);
	}

	/**
	 * This method does the work for {@link #getAllCandidateNewStates(List)} recursively.
	 * 
	 * @param openVoiceIndices The open voice indices for each note, gotten from {@link #getOpenVoiceIndices(List, List)}
	 * initially.
	 * @param incoming A List of incoming notes.
	 * @param newVoices A List of the Voices in this State as it is now. For each recursive call,
	 * this should be a deep copy since the Voices that lie within will be changed.
	 * @param logProbSum The sum of the current log probability of this State, including any transitions
	 * already made recursively.
	 * @param noteIndex The index of the note which we are tasked with transitioning on.
	 * 
	 * @return A List of all States which could be transitioned into given the parameters.
	 */
	private SortedSet<State> getAllCandidateNewStatesRecursive(List<List<Integer>> openVoiceIndices, List<MidiNote> incoming,
			List<SingleNoteVoice> newVoices, double logProbSum, int noteIndex) {
		if (noteIndex == incoming.size()) {
			// Base case - no notes left to transition. Return a State based on the given Voices and log prob.
			SortedSet<State> newStates = new TreeSet<State>();
			newStates.add(new State(logProbSum, new ArrayList<SingleNoteVoice>(newVoices), params));
			return newStates;
		}
		
		// Setup
		TreeSet<State> newStates = new TreeSet<State>();
		
		
		// Calculate transition probabilities for starting new voices
		double[] newVoiceProbs = new double[newVoices.size() + 1];
		for (int i = 0; i < newVoiceProbs.length; i++) {
			newVoiceProbs[i] = getTransitionProb(incoming.get(noteIndex), -i - 1, newVoices);
		}
		
		int maxIndex = MathUtils.getMaxIndex(newVoiceProbs);
		
		if (maxIndex != -1) {
			// There is a good place to add a new voice
			addNewVoicesRecursive(openVoiceIndices, incoming, newVoices, logProbSum, noteIndex, newVoiceProbs, newVoiceProbs[maxIndex], newStates);
		}
		
		
		// Add to existing voices
		double[] existingVoiceProbs = new double[openVoiceIndices.get(noteIndex).size()];
		for (int i = 0; i < existingVoiceProbs.length; i++) {
			existingVoiceProbs[i] = getTransitionProb(incoming.get(noteIndex), openVoiceIndices.get(noteIndex).get(i), newVoices);
		}
		
		addToExistingVoicesRecursive(openVoiceIndices, incoming, newVoices, logProbSum, noteIndex, existingVoiceProbs, newStates);
		
		return newStates;
	}

	/**
	 * This method does the work for {@link #getAllCandidateNewStatesRecursive(List, List, List, double, int)}
	 * of adding the notes into newly created voices.
	 * 
	 * @param openVoiceIndices The open voice indices for each note, gotten from {@link #getOpenVoiceIndices(List, List)}
	 * initially.
	 * @param incoming A List of incoming notes.
	 * @param newVoices A List of the Voices in this State as it is now. For each recursive call,
	 * this should be a deep copy since the Voices that lie within will be changed.
	 * @param logProbSum The sum of the current log probability of this State, including any transitions
	 * already made recursively.
	 * @param noteIndex The index of the note which we are tasked with transitioning on.
	 * @param newVoiceProbs The probability of adding the current note into a new voice at any given index.
	 * @param maxValue The maximum value of any number in newVoiceProbs.
	 * @param newStates The States List where we will add the newly created States.
	 */
	private void addNewVoicesRecursive(List<List<Integer>> openVoiceIndices, List<MidiNote> incoming, List<SingleNoteVoice> newVoices,
			double logProbSum, int noteIndex, double[] newVoiceProbs, double maxValue, TreeSet<State> newStates) {
		
		for (int newVoiceIndex = 0; newVoiceIndex < newVoiceProbs.length; newVoiceIndex++) {
			if (newVoiceProbs[newVoiceIndex] == maxValue) {
				// Add at any location with max probability
				doTransition(incoming.get(noteIndex), -newVoiceIndex - 1, newVoices);
				
				// Fix openVoiceIndices
				for (int note = noteIndex + 1; note < openVoiceIndices.size(); note++) {
					for (int voice = 0; voice < openVoiceIndices.get(note).size(); voice++) {
						if (openVoiceIndices.get(note).get(voice) >= newVoiceIndex) {
							openVoiceIndices.get(note).set(voice, openVoiceIndices.get(note).get(voice) + 1);
						}
					}
				}
				
				// (Pseudo-)recursive call
				newStates.addAll(getAllCandidateNewStatesRecursive(openVoiceIndices, incoming, newVoices, logProbSum + newVoiceProbs[newVoiceIndex], noteIndex + 1));
				
				// Fix for memory overflow - trim newStates as soon as we can
				while (newStates.size() > params.BEAM_SIZE) {
					newStates.pollLast();
				}
				
				// The objects are mutable, so reverse changes. This helps with memory usage as well.
				reverseTransition(-newVoiceIndex - 1, newVoices);
				
				// Reverse openVoiceIndices
				for (int note = noteIndex + 1; note < openVoiceIndices.size(); note++) {
					for (int voice = 0; voice < openVoiceIndices.get(note).size(); voice++) {
						if (openVoiceIndices.get(note).get(voice) > newVoiceIndex) {
							openVoiceIndices.get(note).set(voice, openVoiceIndices.get(note).get(voice) - 1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method does the work for {@link #getAllCandidateNewStatesRecursive(List, List, List, double, int)}
	 * of adding the notes into existing voices.
	 * 
	 * @param openVoiceIndices The open voice indices for each note, gotten from {@link #getOpenVoiceIndices(List, List)}
	 * initially.
	 * @param incoming A List of incoming notes.
	 * @param newVoices A List of the Voices in this State as it is now. For each recursive call,
	 * this should be a deep copy since the Voices that lie within will be changed.
	 * @param logProbSum The sum of the current log probability of this State, including any transitions
	 * already made recursively.
	 * @param noteIndex The index of the note which we are tasked with transitioning on.
	 * @param existingVoiceProbs The probability of adding this note into an existing voice at each index.
	 * @param newStates The States List where we will add the newly created States.
	 */
	private void addToExistingVoicesRecursive(List<List<Integer>> openVoiceIndices, List<MidiNote> incoming,
			List<SingleNoteVoice> newVoices, double logProbSum, int noteIndex, double[] existingVoiceProbs, TreeSet<State> newStates) {
		
		for (int openVoiceIndex = 0; openVoiceIndex < existingVoiceProbs.length; openVoiceIndex++) {
			// Try the transition
			int voiceIndex = openVoiceIndices.get(noteIndex).get(openVoiceIndex);
			doTransition(incoming.get(noteIndex), voiceIndex, newVoices);
			
			// Fix openVoiceIndices
			boolean[] removed = new boolean[openVoiceIndices.size()];
			for (int note = noteIndex + 1; note < openVoiceIndices.size(); note++) {
				removed[note] = openVoiceIndices.get(note).remove(new Integer(voiceIndex));
			}
			
			// (Pseudo-)recursive call
			newStates.addAll(getAllCandidateNewStatesRecursive(openVoiceIndices, incoming, newVoices, logProbSum + existingVoiceProbs[openVoiceIndex], noteIndex + 1));
			
			// Remove extras from newStates to save memory
			while (newStates.size() > params.BEAM_SIZE) {
				newStates.pollLast();
			}
			
			// Reverse transition
			reverseTransition(voiceIndex, newVoices);
			
			// Reverse openVoiceIndices
			for (int j = noteIndex + 1; j < removed.length; j++) {
				if (removed[j]) {
					int note;
					for (note = 0; note < openVoiceIndices.get(j).size() && openVoiceIndices.get(j).get(note) < voiceIndex; note++);
					openVoiceIndices.get(j).add(note, voiceIndex);
				}
			}
		}
	}

	/**
	 * Get a List of the indices at which open voices are in the given List of Voices.
	 * 
	 * @param incoming A List of the MidiNotes which occur at the given onset time.
	 * @param voices A List of the Voices we want to check.
	 * 
	 * @return A List of the open voices in newVoices for each incoming note. <code>return.get(i).get(j)</code>
	 * will return the index of the (j+1)th (since it is 0-indexed) open Voice in newVoices for the ith note
	 * from incoming.
	 */
	private List<List<Integer>> getOpenVoiceIndices(List<MidiNote> incoming, List<SingleNoteVoice> voices) {
		long onsetTime = incoming.get(0).getOnsetTime();
		List<List<Integer>> openIndices = new ArrayList<List<Integer>>(incoming.size());
		
		for (MidiNote note : incoming) {
			List<Integer> noteOpen = new ArrayList<Integer>();
			for (int i = 0; i < voices.size(); i++) {
				if (voices.get(i).canAddNoteAtTime(onsetTime, note.getDurationTime(), params)) {
					noteOpen.add(i);
				}
			}
			openIndices.add(noteOpen);
		}
		
		return openIndices;
	}
	
	/**
	 * Reverse the given transition.
	 * 
	 * @param transition The transition we want to reverse.
	 * @param newVoices The Voices List where we want to reverse the transition.
	 */
	private void reverseTransition(int transition, List<SingleNoteVoice> newVoices) {
		// For new Voices, we need to add the Voice, and then update the transition value to
		// point to that new Voice so the lower code works.
		if (transition < 0) {
			newVoices.remove(-transition - 1);
			
		} else {
			newVoices.set(transition, newVoices.get(transition).getPrevious());
		}
	}

	/**
	 * Perform the given transition WITHOUT calculating probability.
	 * 
	 * @param note The note we want to add to a Voice.
	 * @param transition The value of the transition we want to perform on the given note.
	 * A negative value tells us to add the note to a new Voice placed at index (-transition - 1).
	 * Any non-negative value means to add the note into the Voice at that index in newVoices.
	 * @param newVoices A List of the Voices available to have notes added to them.
	 */
	private void doTransition(MidiNote note, int transition, List<SingleNoteVoice> newVoices) {
		// For new Voices, we need to add the Voice, and then update the transition value to
		// point to that new Voice so the lower code works.
		if (transition < 0) {
			transition = -transition - 1;
			newVoices.add(transition, new SingleNoteVoice(note));
			
		} else {
			newVoices.set(transition, new SingleNoteVoice(note, newVoices.get(transition)));
		}
	}

	/**
	 * Get the probability of the given transition occurring.
	 * 
	 * @param note The note we want to add to a Voice.
	 * @param transition The value of the transition we want to perform on the given note.
	 * A negative value tells us to add the note to a new Voice placed at index (-transition - 1).
	 * Any non-negative value means to add the note into the Voice at that index in newVoices.
	 * @param newVoices A List of the Voices available to have notes added to them.
	 * @return The probability of the given transition.
	 */
	private double getTransitionProb(MidiNote note, int transition, List<SingleNoteVoice> newVoices) {
		double logProb;
		SingleNoteVoice prev, next;
		
		// For new Voices, we need to add the Voice, and then update the transition value to
		// point to that new Voice so the lower code works.
		if (transition < 0) {
			transition = -transition - 1;
			logProb = Math.log(params.NEW_VOICE_PROBABILITY);
			prev = transition == 0 ? null : newVoices.get(transition - 1);
			next = transition == newVoices.size() ? null : newVoices.get(transition);
			
		} else {
			logProb = Math.log(newVoices.get(transition).getProbability(note, params));
			prev = transition == 0 ? null : newVoices.get(transition - 1);
			next = transition == newVoices.size() - 1 ? null : newVoices.get(transition + 1);
		}
		
		// Check if we are in the wrong order with the prev or next Voices (or both)
		if (prev != null && note.getPitch() < prev.getMostRecentNote().getPitch()) {
			logProb -= Math.log(2);
		}
			
		if (next != null && note.getPitch() > next.getMostRecentNote().getPitch()) {
			logProb -= Math.log(2);
		}
		
		if (logProb == Double.NEGATIVE_INFINITY) {
			logProb = -Double.MAX_VALUE;
		}

		return logProb;
	}
	
	/**
	 * Get a list of the Voices we've found in this State.
	 * 
	 * @return {@link #voices}
	 */
	public List<SingleNoteVoice> getVoices() {
		return voices;
	}
	
	/**
	 * Get the log probability of appearing in this State.
	 * 
	 * @return {@link #logProb}
	 */
	public double getLogProb() {
		return logProb;
	}

	/**
	 * The default State Comparator. States are naturally ordered by decreasing {@link #logProb}.
	 * 
	 * @param o The other State we are comparing against. If null, we return -1.
	 * @return The int representing whether the ordering of this State with the given one.
	 */
	@Override
	public int compareTo(State o) {
		if (o == null) {
			return -1;
		}
		
		return ((Double) o.getLogProb()).compareTo(logProb);
	}
	
	@Override
	public String toString() {
		return voices.toString() + " " + logProb;
	}
}
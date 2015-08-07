package voicesplitting.voice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import voicesplitting.utils.MathUtils;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.hmm.VoiceSplittingParameters;

/**
 * A <code>SingleNoteVoice</code> is a {@link Voice} which can only contain a
 * single {@link voicesplitting.utils.MidiNote} at a time.
 * <p>
 * This is the Voice class used in the paper submitted to ISMIR 2015.
 * 
 * @author Andrew McLeod - 6 April, 2015
 */
public class SingleNoteVoice extends Voice {
	
	/**
	 * A node with the last note in this Voice. These are backwards-linked so we
	 * can get the entire List of notes from this node.
	 */
	private SingleNoteVoiceNode lastNoteNode;
	
	/**
	 * The parameters to use when splitting into voices.
	 */
	private VoiceSplittingParameters params;
	
	/**
	 * Create a new empty Voice.
	 * 
	 * @param params {@link #params}
	 */
	public SingleNoteVoice(VoiceSplittingParameters params) {
		lastNoteNode = null;
		this.params = params;
	}
	
	/**
	 * Create a new Voice with the given Voice's note list.
	 * 
	 * @param voice The Voice we want to copy.
	 */
	public SingleNoteVoice(SingleNoteVoice voice) {
		lastNoteNode = voice.getLastNoteNode();
		params = voice.getParams();
	}

	/**
	 * Add the given note to this Voice.
	 * 
	 * @param note The note to add to this Voice.
	 */
	public void addNote(MidiNote note) {
		if (!canAddNoteAtTime(note.getOnsetTime(), note.getDurationTime())) {
			System.out.println("INVALID ADD");
		}
		
		lastNoteNode = new SingleNoteVoiceNode(lastNoteNode, note);
	}
	
	/**
	 * Remove the last note added to this Voice.
	 */
	public void removeLastNote() {
		lastNoteNode = lastNoteNode.getPrev();	
	}
	
	/**
	 * Get the probability score of adding the given note to this Voice.
	 * 
	 * @param note The note we want to add.
	 * @return The probability score for the given note.
	 */
	public double getProbability(MidiNote note) {
		if (lastNoteNode == null) {
			return params.NEW_VOICE_PROBABILITY;
		}
		
		double pitch = pitchScore(getWeightedLastPitch(), note.getPitch());
		double gap = gapScore(note.getOnsetTime(), getLastOffsetTime());
		return pitch * gap;
	}

	/**
	 * Get the pitch closeness of the two given pitches. This value should be higher
	 * the closer together the two pitch values are. The first input parameter is a double
	 * because it is drawn from {@link SingleNoteVoice#getWeightedLastPitch()}.
	 * 
	 * @param weightedPitch A weighted pitch, drawn from {@link SingleNoteVoice#getWeightedLastPitch()}.
	 * @param pitch2 An exact pitch.
	 * @return The pitch score of the given two pitches, a value between 0 and 1.
	 */
	private double pitchScore(double weightedPitch, int pitch2) {
		return MathUtils.gaussianWindow(weightedPitch, pitch2, params.PITCH_STD);
	}

	/**
	 * Get the pitch closeness of the two given pitches. This value should be higher
	 * the closer together the two pitch values are.
	 * 
	 * @param time1 A time.
	 * @param time2 Another time.
	 * @return The gap score of the two given time values, a value between 0 and 1.
	 */
	private double gapScore(long time1, long time2) {
		double timeDiff = Math.abs(time2 - time1);
		double inside = Math.max(0, -timeDiff / params.GAP_STD_MICROS + 1);
		double log = Math.log(inside) + 1;
		return Math.max(log, params.MIN_GAP_SCORE);
	}
	
	@Override
	public boolean canAddNoteAtTime(long time, long length) {
		MidiNote last = getLastNote();
		
		if (last == null) {
			return true;
		}
		
		long overlap = last.getOffsetTime() - time;
		
		if ((overlap <= last.getDurationTime() / 2 && overlap < length) ||
				(overlap < last.getDurationTime() && overlap <= length / 2)) {
			return true;
		}
		
		return false;
	}

	@Override
	public long getLastOffsetTime() {
		MidiNote last = getLastNote();
		
		return last == null ? 0 : last.getOffsetTime();
	}

	@Override
	public MidiNote getLastNote() {
		if (lastNoteNode == null) {
			return null;
		}
		
		return lastNoteNode.getNote();
	}

	@Override
	public double getWeightedLastPitch() {
		if (lastNoteNode == null) {
			return 0;
		}
		
		double weight = 1;
		double totalWeight = 0;
		double sum = 0;
		
		// Most recent PITCH_HISTORY_LENGTH chords
		SingleNoteVoiceNode noteNode = lastNoteNode;
		for (int i = 0; i < params.PITCH_HISTORY_LENGTH && noteNode != null; i++, noteNode = noteNode.getPrev()) {
			sum += noteNode.getNote().getPitch() * weight;
			
			totalWeight += weight;
			weight *= 0.5;
		}
		
		return sum / totalWeight;
	}

	@Override
	public int getNumNotes() {
		if (lastNoteNode == null) {
			return 0;
		}
		
		return lastNoteNode.getNumNotes();
	}

	@Override
	public int getNumNotesCorrect() {
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		
		for (SingleNoteVoiceNode noteNode = lastNoteNode; noteNode != null; noteNode = noteNode.getPrev()) {
			int track = noteNode.getNote().getTrackNumber();
			if (!counts.containsKey(track)) {
				counts.put(track, 0);
			}
				
			counts.put(track, counts.get(track) + 1);
		}
				
		int maxCount = -1;
		for (int count : counts.values()) {
			maxCount = Math.max(maxCount, count);
		}
		
		return maxCount;
	}
	
	/**
	 * Get the number of links in this Voice which are correct. That is, the number of times
	 * that two consecutive notes belong to the same midi track.
	 * 
	 * @param goldStandard The gold standard voices for this song.
	 * @return The number of times that two consecutive notes belong to the same midi track.
	 */
	public int getNumLinksCorrect(List<List<MidiNote>> goldStandard) {
		int count = 0;
		int index = -1;
		
		for (SingleNoteVoiceNode node = lastNoteNode; node.getPrev() != null; node = node.getPrev()) {
			MidiNote guessedPrev = node.getPrev().getNote();
			MidiNote note = node.getNote();
			
			if (note.getTrackNumber() == guessedPrev.getTrackNumber()) {
				int track = note.getTrackNumber();
				if (index == -1) {
					// No valid index - refind
					index = goldStandard.get(track).indexOf(note);
					if (index == -1) {
						System.out.println("OUCH");
					}
				}
				
				if (index != 0 && goldStandard.get(track).get(--index).equals(guessedPrev)) {
					// Match!
					count++;
					
				} else {
					// No match - invalidate index
					index = -1;
				}
			} else {
				// Different track - invalidate index
				index = -1;
			}
		}
		
		return count;
	}
	
	/**
	 * Get a List of the MidiNotes contained by this Voice, in order.
	 * 
	 * @return An ordered List of the MidiNotes in this Voice.
	 */
	public List<MidiNote> getNotes() {
		return lastNoteNode.getList();
	}
	
	/**
	 * Get the node of the most recent note in this Voice.
	 * 
	 * @return {@link #lastNoteNode}
	 */
	public SingleNoteVoiceNode getLastNoteNode() {
		return lastNoteNode;
	}
	
	/**
	 * Get the parameters we are using.
	 * 
	 * @return {@link #params}
	 */
	private VoiceSplittingParameters getParams() {
		return params;
	}
	
	@Override
	public String toString() {
		return lastNoteNode.getList().toString();
	}
}

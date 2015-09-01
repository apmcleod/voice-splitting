package voicesplitting.voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import voicesplitting.utils.MathUtils;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.hmm.VoiceSplittingParameters;

/**
 * A <code>SingleNoteVoice</code> is a node in the LinkedList representing a
 * voice. Each node has only a previous pointer and a {@link MidiNote}.
 * Only a previous pointer is needed because we allow for Voices to split and clone themselves,
 * keeping the beginning of their note sequences identical. This allows us to have multiple
 * LinkedLists of notes without needing multiple full List objects. Rather, they all point
 * back to their common prefix LinkedLists.
 * <p>
 * This is the Voice class used in the paper submitted to ISMIR 2015.
 * 
 * @author Andrew McLeod - 6 April, 2015
 */
public class SingleNoteVoice {
	/**
	 * The Voice ending at second to last note in this voice.
	 */
	private final SingleNoteVoice previous;
	
	/**
	 * The most recent note of this voice.
	 */
	private final MidiNote mostRecentNote;
	
	/**
	 * Create a new Voice with the given previous voice.
	 * 
	 * @param note {@link #mostRecentNote}
	 * @param prev {@link #previous}
	 */
	public SingleNoteVoice(MidiNote note, SingleNoteVoice prev) {
		previous = prev;
		mostRecentNote = note;
	}
	
	/**
	 * Create a new Voice.
	 * 
	 * @param note {@link #mostRecentNote}
	 */
	public SingleNoteVoice(MidiNote note) {
		this(note, null);
	}
	
	/**
	 * Get the probability score of adding the given note to this Voice.
	 * 
	 * @param note The note we want to add.
	 * @return The probability score for the given note.
	 */
	public double getProbability(MidiNote note, VoiceSplittingParameters params) {
		double pitch = pitchScore(getWeightedLastPitch(params), note.getPitch(), params);
		double gap = gapScore(note.getOnsetTime(), mostRecentNote.getOffsetTime(), params);
		return pitch * gap;
	}

	/**
	 * Get the pitch closeness of the two given pitches. This value should be higher
	 * the closer together the two pitch values are. The first input parameter is a double
	 * because it is drawn from {@link #getWeightedLastPitch(VoiceSplittingParameters)}.
	 * 
	 * @param weightedPitch A weighted pitch, drawn from {@link #getWeightedLastPitch(VoiceSplittingParameters)}.
	 * @param pitch An exact pitch.
	 * @return The pitch score of the given two pitches, a value between 0 and 1.
	 */
	private double pitchScore(double weightedPitch, int pitch, VoiceSplittingParameters params) {
		return MathUtils.gaussianWindow(weightedPitch, pitch, params.PITCH_STD);
	}

	/**
	 * Get the pitch closeness of the two given pitches. This value should be higher
	 * the closer together the two pitch values are.
	 * 
	 * @param time1 A time.
	 * @param time2 Another time.
	 * @return The gap score of the two given time values, a value between 0 and 1.
	 */
	private double gapScore(long time1, long time2, VoiceSplittingParameters params) {
		double timeDiff = Math.abs(time2 - time1);
		double inside = Math.max(0, -timeDiff / params.GAP_STD_MICROS + 1);
		double log = Math.log(inside) + 1;
		return Math.max(log, params.MIN_GAP_SCORE);
	}
	
	/**
	 * Decide if we can add a note with the given length at the given time based on the given parameters.
	 * 
	 * @param time The onset time of the note we want to add.
	 * @param length The length of the note we want to add.
	 * @param params The parameters we're using.
	 * @return True if we can add a note of the given duration at the given time. False otherwise.
	 */
	public boolean canAddNoteAtTime(long time, long length, VoiceSplittingParameters params) {
		long overlap = mostRecentNote.getOffsetTime() - time;
		
		return overlap <= mostRecentNote.getDurationTime() / 2 && overlap < length;
	}

	/**
	 * Get the weighted pitch of this voice.
	 * 
	 * @param params The paramters we're using.
	 * @return The weighted pitch of this voice.
	 */
	public double getWeightedLastPitch(VoiceSplittingParameters params) {
		double weight = 1;
		double totalWeight = 0;
		double sum = 0;
		
		// Most recent PITCH_HISTORY_LENGTH notes
		SingleNoteVoice noteNode = this;
		for (int i = 0; i < params.PITCH_HISTORY_LENGTH && noteNode != null; i++, noteNode = noteNode.previous) {
			sum += noteNode.mostRecentNote.getPitch() * weight;
			
			totalWeight += weight;
			weight *= 0.5;
		}
		
		return sum / totalWeight;
	}

	/**
	 * Get the number of notes we've correctly grouped into this voice, based on the most common voice in the voice.
	 * 
	 * @return The number of notes we've assigned into this voice correctly.
	 */
	public int getNumNotesCorrect() {
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		
		for (SingleNoteVoice noteNode = this; noteNode != null; noteNode = noteNode.previous) {
			int channel = noteNode.mostRecentNote.getChannel();
			if (!counts.containsKey(channel)) {
				counts.put(channel, 0);
			}
				
			counts.put(channel, counts.get(channel) + 1);
		}
				
		int maxCount = -1;
		for (int count : counts.values()) {
			maxCount = Math.max(maxCount, count);
		}
		
		return maxCount;
	}
	
	/**
	 * Get the number of links in this Voice which are correct. That is, the number of times
	 * that two consecutive notes belong to the same midi channel.
	 * 
	 * @param goldStandard The gold standard voices for this song.
	 * @return The number of times that two consecutive notes belong to the same midi track.
	 */
	public int getNumLinksCorrect(List<List<MidiNote>> goldStandard) {
		int count = 0;
		int index = -1;
		
		for (SingleNoteVoice node = this; node.previous != null; node = node.previous) {
			MidiNote guessedPrev = node.previous.mostRecentNote;
			MidiNote note = node.mostRecentNote;
			
			if (note.getChannel() == guessedPrev.getChannel()) {
				int channel = note.getChannel();
				if (index == -1) {
					// No valid index - refind
					index = goldStandard.get(channel).indexOf(note);
				}
				
				if (index != 0 && goldStandard.get(channel).get(--index).equals(guessedPrev)) {
					// Match!
					count++;
					
				} else {
					// No match - invalidate index
					index = -1;
				}
			} else {
				// Different channel - invalidate index
				index = -1;
			}
		}
		
		return count;
	}
	
	/**
	 * Get the number of notes in the linked list with this node as its tail.
	 * 
	 * @return The number of notes.
	 */
	public int getNumNotes() {
		if (previous == null) {
			return 1;
		}
		
		return 1 + previous.getNumNotes();
	}

	/**
	 * Get the List of notes which this node is the tail of, in chronological order.
	 * 
	 * @return A List of notes in chronological order, ending with this one.
	 */
	public List<MidiNote> getNotes() {
		List<MidiNote> list = previous == null ? new ArrayList<MidiNote>() : previous.getNotes();
		
		list.add(mostRecentNote);
		
		return list;
	}
	
	/**
	 * Get the most recent note in this voice.
	 * 
	 * @return {@link #mostRecentNote}
	 */
	public MidiNote getMostRecentNote() {
		return mostRecentNote;
	}
	
	/**
	 * Get the voice ending at the previous note in this voice.
	 * 
	 * @return {@link #previous}
	 */
	public SingleNoteVoice getPrevious() {
		return previous;
	}
	
	@Override
	public String toString() {
		return getNotes().toString();
	}
}

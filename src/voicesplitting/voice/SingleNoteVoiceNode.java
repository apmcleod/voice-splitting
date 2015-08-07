package voicesplitting.voice;

import java.util.ArrayList;
import java.util.List;

import voicesplitting.utils.MidiNote;

/**
 * A <code>SingleNoteVoiceNode</code> is a node in the LinkedList representing a
 * {@link SingleNoteVoice}. Each node has only a previous pointer and a {@link MidiNote}.
 * Only a previous pointer is needed because we allow for Voices to split and clone themselves,
 * keeping the beginning of their note sequences identical. This allows us to have multiple
 * LinkedLists of notes without needing multiple full List objects. Rather, they all point
 * back to their common prefix LinkedLists.
 * 
 * @author Andrew McLeod - 6 April, 2015
 */
public class SingleNoteVoiceNode {
	/**
	 * The Node previous to this one.
	 */
	private final SingleNoteVoiceNode prev;
	
	/**
	 * The note held by this node.
	 */
	private final MidiNote note;
	
	/**
	 * Create a new SingleNoteVoiceNode with the given previous Node and note.
	 * 
	 * @param prev {@link #prev}
	 * @param note {@link #note}
	 */
	public SingleNoteVoiceNode(SingleNoteVoiceNode prev, MidiNote note) {
		this.prev = prev;
		this.note = note;
	}
	
	/**
	 * Get the number of notes in the linked list with this node as its tail.
	 * 
	 * @return The number of notes.
	 */
	public int getNumNotes() {
		if (prev == null) {
			return 1;
		}
		
		return 1 + prev.getNumNotes();
	}
	
	/**
	 * Get the note held by this node.
	 * 
	 * @return {@link #note}
	 */
	public MidiNote getNote() {
		return note;
	}
	
	/**
	 * Get the previous node.
	 * 
	 * @return {@link #prev}
	 */
	public SingleNoteVoiceNode getPrev() {
		return prev;
	}

	/**
	 * Get the List of notes which this node is the tail of, in chronological order.
	 * 
	 * @return A List of notes in chronological order, ending with this one.
	 */
	public List<MidiNote> getList() {
		List<MidiNote> list = getPrev() == null ? new ArrayList<MidiNote>() : getPrev().getList();
		
		list.add(getNote());
		
		return list;
	}
	
	@Override
	public String toString() {
		return note.toString();
	}
}

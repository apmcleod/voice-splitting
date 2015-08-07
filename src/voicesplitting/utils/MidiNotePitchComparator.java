package voicesplitting.utils;

import java.util.Comparator;

/**
 * A MidiNotePitchComparator can be used to sort {@link MidiNote}s in increasing pitch
 * order.
 * 
 * @author Andrew McLeod - 3 April, 2015
 */
public class MidiNotePitchComparator implements Comparator<MidiNote> {

	@Override
	public int compare(MidiNote arg0, MidiNote arg1) {
		return arg0.getPitch() - arg1.getPitch();
	}
}

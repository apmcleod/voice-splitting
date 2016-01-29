package voicesplitting.parsing;

import javax.sound.midi.InvalidMidiDataException;

import voicesplitting.utils.MidiNote;

/**
 * A <code>NoteEventParser</code> is any class which can handle MIDI Note On and Note Off events.
 * These are normally passed to it by an {@link voicesplitting.parsing.EventParser}.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 * @version 1.0
 * @since 1.0
 */
public interface NoteEventParser {
	/**
     * Process a Note On event.
     * 
     * @param key The key pressed. This will be a number between 1 and 88 for piano.
     * @param velocity The velocity of the press. This is a value between 1 and 127 inclusive.
     * @param tick The midi tick location of this event.
     * @param channel The midi channel this note came from.
	 * @return The MidiNote we just created.
     */
    public MidiNote noteOn(int key, int velocity, long tick, int channel);
    
    /**
     * Process a Note Off event.
     * 
     * @param key The midi key which has been turned off. This value is between 39 and 127, inclusive, for piano.
     * @param tick The midi tick location of this event.
     * @param channel The midi channel this note came from.
     * @throws InvalidMidiDataException If a note off event doesn't match any previously seen note on events.
     */
    public void noteOff(int key, long tick, int channel) throws InvalidMidiDataException;
}

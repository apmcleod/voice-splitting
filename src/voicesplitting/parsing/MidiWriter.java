package voicesplitting.parsing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import voicesplitting.time.KeySignature;
import voicesplitting.time.Tempo;
import voicesplitting.time.TimeSignature;
import voicesplitting.time.TimeTracker;
import voicesplitting.time.TimeTrackerNode;
import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiWriter</code> is able to take in {@link MidiNote}s and a {@link TimeTracker},
 * and write them out to a valid Midi File.
 * 
 * @author Andrew McLeod - 28 July, 2015
 */
public class MidiWriter {
	/**
	 * The File we want to write to.
	 */
	private File outFile;
	
	/**
	 * The TimeTracker for this Midi data.
	 */
	private TimeTracker timeTracker;
	
	/**
	 * The Sequence containing the Midi data we are going to write out.
	 */
	private Sequence sequence;
	
	/**
	 * Create a new MidiWriter to write out to the given File.
	 * 
	 * @param outFile {@link #outFile}
	 * @param tt {@link #timeTracker}
	 * 
	 * @throws InvalidMidiDataException If somehow the TimeTracker has an invalid PPQ value. 
	 */
	public MidiWriter(File outFile, TimeTracker tt) throws InvalidMidiDataException {
		this.outFile = outFile;
		timeTracker = tt;
		
		sequence = new Sequence(Sequence.PPQ, (int) TimeTracker.PPQ);
		sequence.createTrack();
		
		writeTimeTracker();
	}
	
	/**
	 * Write the proper TimeTracker events out to our {@link #sequence}.
	 * 
	 * @throws InvalidMidiDataException If the TimeTracker contained invalid Midi data. 
	 */
	private void writeTimeTracker() throws InvalidMidiDataException {
		LinkedList<TimeTrackerNode> nodes = timeTracker.getNodes();
		ListIterator<TimeTrackerNode> iterator = nodes.listIterator();
    	
    	TimeTrackerNode node = iterator.next();
    	long tick = node.getStartTick();
    	
    	writeKeySignature(node.getKeySignature(), tick);
    	writeTimeSignature(node.getTimeSignature(), tick);
    	writeTempo(node.getTempo(), tick);
    	
    	while (iterator.hasNext()) {
    		TimeTrackerNode prev = node;
    		node = iterator.next();
    		tick = node.getStartTick();
    		
    		if (!node.getKeySignature().equals(prev.getKeySignature())) {
    			writeKeySignature(node.getKeySignature(), tick);
    		}
    		
    		if (!node.getTimeSignature().equals(prev.getTimeSignature())) {
    			writeTimeSignature(node.getTimeSignature(), tick);
    		}
    		
    		if (!node.getTempo().equals(prev.getTempo())) {
    			writeTempo(node.getTempo(), tick);
    		}
    	}
	}

	/**
	 * Write the given key signature out to {@link #sequence} at the given tick.
	 * 
	 * @param keySignature The key signature to write.
	 * @param tick The tick at which to write it.
	 * @throws InvalidMidiDataException If the key signature produces invalid Midi data.
	 */
	private void writeKeySignature(KeySignature keySignature, long tick) throws InvalidMidiDataException {
		MetaMessage mm = new MetaMessage();
		
		byte[] data = {
				(byte) keySignature.getNumSharps(),
				(byte) (keySignature.isMajor() ? 0 : 1)};
		
		mm.setMessage(EventParser.KEY_SIGNATURE, data, data.length);
		
		sequence.getTracks()[0].add(new MidiEvent(mm, tick));
	}

	/**
	 * Write the given time signature out to {@link #sequence} at the given tick.
	 * 
	 * @param timeSignature The time signature to write.
	 * @param tick The tick at which to write it.
	 * @throws InvalidMidiDataException If the time signature contained invalid Midi data.
	 */
	private void writeTimeSignature(TimeSignature timeSignature, long tick) throws InvalidMidiDataException {
		MetaMessage mm = new MetaMessage();
		
		int denominator = timeSignature.getDenominator();
		
		// Base 2 log calculator for whole numbers
		int i = 0;
		while (denominator != 1) {
			denominator /= 2;
			i++;
		}
		
		byte[] data = {
				(byte) timeSignature.getNumerator(),
				(byte) i,
				(byte) timeSignature.getMetronomeTicksPerBeat(),
				(byte) timeSignature.getNotes32PerQuarter()};
		
		mm.setMessage(EventParser.TIME_SIGNATURE, data, data.length);
		
		sequence.getTracks()[0].add(new MidiEvent(mm, tick));
	}
	
	/**
	 * Write the given tempo out to {@link #sequence} at the given tick.
	 * 
	 * @param tempo The tempo to write.
	 * @param tick The tick at which to write it.
	 * 
	 * @throws InvalidMidiDataException If the tempo contained invalid Midi data.
	 */
	private void writeTempo(Tempo tempo, long tick) throws InvalidMidiDataException {
		MetaMessage mm = new MetaMessage();
		
		int mspq = tempo.getMicroSecondsPerQuarter();
		
		byte[] data = {
				(byte) ((mspq & 0xff000000) >> 24),
				(byte) ((mspq & 0x00ff0000) >> 16),
				(byte) ((mspq & 0x0000ff00) >> 8),
				(byte) (mspq & 0x000000ff)};
		
		// Clear leading 0's
		int i;
		for (i = 0; i < data.length - 1 && data[i] == 0; i++);
		if (i != 0) {
			data = Arrays.copyOfRange(data, i, data.length);
		}
		
		mm.setMessage(EventParser.TEMPO, data, data.length);
		
		sequence.getTracks()[0].add(new MidiEvent(mm, tick));
	}

	/**
	 * Add the given MidiNote into the {@link #sequence}.
	 *  
	 * @param note The note to add.
	 * 
	 * @throws InvalidMidiDataException If the MidiNote contains invalid Midi data. 
	 */
	public void addMidiNote(MidiNote note) throws InvalidMidiDataException {
		int channel = note.getChannel();
		
		// Pad with enough tracks
		while (sequence.getTracks().length <= channel) {
			sequence.createTrack();
		}
		
		// Get the correct track
		Track track = sequence.getTracks()[channel];
		
		ShortMessage noteOn = new ShortMessage();
		noteOn.setMessage(ShortMessage.NOTE_ON | channel, note.getPitch(), note.getVelocity());
		MidiEvent noteOnEvent = new MidiEvent(noteOn, note.getOnsetTick());
		
		ShortMessage noteOff = new ShortMessage();
		noteOff.setMessage(ShortMessage.NOTE_OFF | channel, note.getPitch(), 0);
		MidiEvent noteOffEvent = new MidiEvent(noteOff, note.getOffsetTick());
		
		track.add(noteOnEvent);
		track.add(noteOffEvent);
	}
	
	/**
	 * Actually write the data out to file.
	 * 
	 * @throws IOException If the file cannot be written to.
	 */
	public void write() throws IOException {
		MidiSystem.write(sequence, 1, outFile);
	}
}

package voicesplitting.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;

import voicesplitting.parsing.EventParser;
import voicesplitting.time.TimeTracker;
import voicesplitting.parsing.NoteListGenerator;
import voicesplitting.utils.MidiNote;

/**
 * A <code>BeatTrackingRunner</code> is the class which interfaces between a
 * {@link VoiceSplittingGUI} and the program logic contained in other packages.
 * 
 * @author Andrew McLeod - 17 June, 2015
 * @version 1.0
 * @since 1.0
 */
public class VoiceSplittingRunner {

	/**
	 * The currently loaded MIDI file.
	 */
	private File midiFile;
	
	/**
	 * The TimeTracker for the currently loaded file.
	 */
	private TimeTracker tt;
	
	/**
	 * A List of the notes of the currently loaded file.
	 */
	private NoteListGenerator nlg;
	
	/**
	 * A List of the gold standard voices for this song.
	 */
	private List<List<MidiNote>> goldStandardVoices;
	
	/**
	 * Create a new BeatTrackingRunner on the given File.
	 * 
	 * @param midiFile {@link #midiFile}
	 * @param useChannel True if we want to use the input data's channel as gold standard voices.
	 * False to use track instead.
	 * @throws IOException If some I/O error occurred when reading the file.
	 * @throws InvalidMidiDataException If the file contained some invlaid MIDI data.
	 * @throws InterruptedException If this is running on a GUI and gets cancelled.
	 */
	public VoiceSplittingRunner(File midiFile, boolean useChannel) throws InvalidMidiDataException, IOException, InterruptedException {
		this.midiFile = midiFile;
		tt = new TimeTracker();
		nlg = new NoteListGenerator(tt);
		EventParser ep = new EventParser(midiFile, nlg, tt, useChannel);
		
		ep.run();
		goldStandardVoices = ep.getGoldStandardVoices();
	}
	
	/**
	 * Get a list of the gold standard voices for this song.
	 * 
	 * @return {@link #goldStandardVoices}
	 */
	public List<List<MidiNote>> getGoldStandardVoices() {
		return goldStandardVoices;
	}
	
	/**
	 * Get the TimeTracker used in this runner.
	 * 
	 * @return {@link #tt}
	 */
	public TimeTracker getTimeTracker() {
		return tt;
	}
	
	/**
	 * Get the NoteListGeneratr from the current loaded MIDI file.
	 * 
	 * @return {@link #nlg}, or null if no MIDI file is loaded yet.
	 */
	public NoteListGenerator getNlg() {
		return nlg;
	}
	
	/**
	 * Get the MIDI file associated with this runner.
	 * 
	 * @return {@link #midiFile}
	 */
	public File getFile() {
		return midiFile;
	}
}
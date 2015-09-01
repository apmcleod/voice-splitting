package voicesplitting.parsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import voicesplitting.gui.VoiceSplittingGUI;
import voicesplitting.time.TimeTracker;
import voicesplitting.utils.MidiNote;

/**
 * An <code>EventParser</code> handles the interfacing between this program and MIDI files.
 * It can read in MIDI events from a file with {@link #run()}, play the audio
 * of the currently loaded data with {@link #playAudio()}, and write the
 * MIDI data out to a file with {@link #write(File)}.
 * <p>
 * One EventParser is required per song you wish to parse.
 * 
 * @author Andrew McLeod - 23 October, 2014
 */
public class EventParser {
	/**
	 * The mask for reading the channel number from a MidiMessage.
	 */
	public static final int CHANNEL_MASK = 0x0f;
	
	/**
	 * The mask for reading the message type from a MidiMessage.
	 */
	public static final int MESSAGE_MASK = 0xf0;
	
	/**
	 * The constant which midi uses for tempo change events.
	 */
	public static final int TEMPO = 0x51;
	
	/**
	 * The constant which midi uses for time signature change events.
	 */
	public static final int TIME_SIGNATURE = 0x58;
	
	/**
	 * The constant which midi uses for key signature change events.
	 */
	public static final int KEY_SIGNATURE = 0x59;
	
	/**
	 * The TimeTracker which will handle timing information for this song.
	 */
	private TimeTracker timeTracker;
	
	/**
	 * The NoteTracker which will keep track of the notes for this song.
	 */
	private final NoteEventParser noteEventParser;
	
	/**
	 * The song we are parsing.
	 */
	private final Sequence song;
	
	/**
	 * The gold standard voices from this song.
	 */
	private List<List<MidiNote>> goldStandard;
    
	/**
	 * Creates a new Midi EventParser
	 * 
	 * @param midiFile The MIDI file we will parse.
	 * @param noteEventParser The NoteTracker to pass events to when we run this parser.
	 * @throws IOException If an I/O error occurred when reading the given file. 
	 * @throws InvalidMidiDataException If the given file was is not in a valid MIDI format.
	 */
    public EventParser(File midiFile, NoteEventParser noteEventParser, TimeTracker timeTracker)
    		throws InvalidMidiDataException, IOException{
    	song = MidiSystem.getSequence(midiFile);
    	
    	this.noteEventParser = noteEventParser;
    	this.timeTracker = timeTracker;
    	
    	TimeTracker.PPQ = song.getResolution();
    	
    	goldStandard = new ArrayList<List<MidiNote>>(song.getTracks().length);
    }
	
    /**
     * Parses the events from the loaded MIDI file through to the NoteTracker.
     * @throws InvalidMidiDataException If a note off event doesn't match any previously seen note on.
     * @throws InterruptedException If this is running on a GUI and gets cancelled.
     */
    public void run() throws InvalidMidiDataException, InterruptedException {
        for (Track track : song.getTracks()) {
        	// multi-track support
        	
            for (int i = 0; i < track.size(); i++) {
            	if (VoiceSplittingGUI.usingGui) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}
            	}
            	int key, velocity;
            	
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                ShortMessage sm;
                int status = message.getStatus();
                
                if (status == MetaMessage.META) {
                	MetaMessage mm = (MetaMessage) message;
                	
                	switch (mm.getType()) {
                		case TEMPO:
                			// Tempo change
                			timeTracker.addTempoChange(event, mm);
                			break;
                		
                		case TIME_SIGNATURE:
                			// Time signature change
                			timeTracker.addTimeSignatureChange(event, mm);
                			break;
                			
                		case KEY_SIGNATURE:
                			// Key signature
                			timeTracker.addKeySignatureChange(event, mm);
                			break;
                			
                		default:
                			break;
                	}
                	
                } else {
                	int channel = status & CHANNEL_MASK;
	                switch (status & MESSAGE_MASK) {
		                	
	                	case ShortMessage.NOTE_ON:
	                		sm = (ShortMessage) message;
	                		
	                		key = sm.getData1();
	                        velocity = sm.getData2();
	                        
	                        if (velocity != 0) {
	                        	MidiNote note = noteEventParser.noteOn(key, velocity, event.getTick(), channel);
	                        	while (goldStandard.size() <= channel) {
	                        		goldStandard.add(new ArrayList<MidiNote>());
	                        	}
	                        	goldStandard.get(channel).add(note);
	                        	break;
	                        }
	                        
	                        // Fallthrough on velocity == 0 --> this is a NOTE_OFF
	                	case ShortMessage.NOTE_OFF:
	                		sm = (ShortMessage) message;
	                		
	                		key = sm.getData1();
	                		
	                        noteEventParser.noteOff(key, event.getTick(), channel);
	                        break;
	                        
	                    default:
	                    	break;
	                }
	            }
            }
        }
        
        for (List<MidiNote> gS : goldStandard) {
        	Collections.sort(gS);
        }
    }
    
    /**
     * Plays the audio of the current song.
     * 
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public void playAudio() throws MidiUnavailableException, InvalidMidiDataException {
    	Sequencer player = MidiSystem.getSequencer();
        
    	player.open();
    	player.setSequence(song);
    	player.start();
    }
    
    /**
     * Write the currently loaded MIDI data out to a file. 
     * 
     * @param outFile The File to write out to.
     * @throws IOException
     */
    public void write(File outFile) throws IOException {
    	MidiSystem.write(song, 1, outFile);
    }
    
    /**
     * Get a List of the gold standard voices from this song.
     * 
     * @return A List of the gold standard voices from this song.
     */
    public List<List<MidiNote>> getGoldStandardVoices() {
    	return goldStandard;
    }
}

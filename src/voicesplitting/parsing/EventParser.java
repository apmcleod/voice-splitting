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
 * of the currently loaded data with {@link #playAudio()}, and grab the gold standard
 * voices of a song with {@link #getGoldStandardVoices()}.
 * <p>
 * One EventParser is required per song you wish to parse.
 * 
 * @author Andrew McLeod - 23 October, 2014
 * @version 1.0
 * @since 1.0
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
	 * True if we want to use the input data's channel as gold standard voices. False to use track instead.
	 */
	private boolean useChannel;
    
	/**
	 * Creates a new Midi EventParser
	 * 
	 * @param midiFile The MIDI file we will parse.
	 * @param noteEventParser The NoteEventParser to pass events to when we run this parser.
	 * @param useChannel True if we want to use the input data's channel as gold standard voices.
	 * False to use track instead.
	 * @throws IOException If an I/O error occurred when reading the given file. 
	 * @throws InvalidMidiDataException If the given file was is not in a valid MIDI format.
	 */
    public EventParser(File midiFile, NoteEventParser noteEventParser, TimeTracker timeTracker, boolean useChannel)
    		throws InvalidMidiDataException, IOException{
    	song = MidiSystem.getSequence(midiFile);
    	
    	this.noteEventParser = noteEventParser;
    	this.timeTracker = timeTracker;
    	
    	timeTracker.setPPQ(song.getResolution());
    	
    	this.useChannel = useChannel;
    	goldStandard = new ArrayList<List<MidiNote>>(song.getTracks().length);
    }
	
    /**
     * Parses the events from the loaded MIDI file through to the NoteTracker.
     * @throws InvalidMidiDataException If a note off event doesn't match any previously seen note on.
     * @throws InterruptedException If this is running on a GUI and gets cancelled.
     */
    public void run() throws InvalidMidiDataException, InterruptedException {
    	long lastTick = 0;
        for (int trackNum = 0; trackNum < song.getTracks().length; trackNum++) {
        	Track track = song.getTracks()[trackNum];
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
                
                lastTick = Math.max(lastTick, event.getTick());
                
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
                	
                	int correctVoice = useChannel ? channel : trackNum;
                	
	                switch (status & MESSAGE_MASK) {
		                	
	                	case ShortMessage.NOTE_ON:
	                		sm = (ShortMessage) message;
	                		
	                		key = sm.getData1();
	                        velocity = sm.getData2();
	                        
	                        if (velocity != 0) {
	                        	MidiNote note = noteEventParser.noteOn(key, velocity, event.getTick(), correctVoice);
	                        	while (goldStandard.size() <= correctVoice) {
	                        		goldStandard.add(new ArrayList<MidiNote>());
	                        	}
	                        	goldStandard.get(correctVoice).add(note);
	                        	break;
	                        }
	                        
	                        // Fallthrough on velocity == 0 --> this is a NOTE_OFF
	                	case ShortMessage.NOTE_OFF:
	                		sm = (ShortMessage) message;
	                		
	                		key = sm.getData1();
	                		
	                        noteEventParser.noteOff(key, event.getTick(), correctVoice);
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
        
        timeTracker.setLastTick(lastTick);
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
     * Get a List of the gold standard voices from this song.
     * 
     * @return A List of the gold standard voices from this song.
     */
    public List<List<MidiNote>> getGoldStandardVoices() {
    	return goldStandard;
    }
}

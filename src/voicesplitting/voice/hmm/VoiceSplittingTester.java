package voicesplitting.voice.hmm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import voicesplitting.parsing.EventParser;
import voicesplitting.time.TimeTracker;
import voicesplitting.trackers.NoteListGenerator;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.SingleNoteVoice;
import voicesplitting.voice.VoiceSplitter;

/**
 * A <code>VoiceSplittingTester</code> tests the {@link VoiceSplitter} class {@link HmmVoiceSplitter}.
 * <p>
 * This was the class used to train for the ISMIR 2015 paper.
 * 
 * @author Andrew McLeod - 10 April, 2015
 */
public class VoiceSplittingTester implements Callable<VoiceSplittingTesterReturn> {
	
	/**
	 * The number of processes to use for tuning.
	 */
	private static final int NUM_PROCS = Runtime.getRuntime().availableProcessors();

	/**
	 * Epsilon needed for double comparison.
	 */
	private static final double EPSILON = 0.000000001;
	
	/**
	 * Print song-by song voice details when running.
	 */
	private static boolean VERBOSE = false;
	
	/**
	 * The songs we are tuning and testing.
	 */
	public static List<List<MidiNote>> songs;
	
	/**
	 * The gold standard Voices.
	 */
	public static List<List<List<MidiNote>>> goldStandard;
	
	/**
	 * A List of the Files we're working on.
	 */
	public static List<File> files;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidMidiDataException 
	 * @throws MidiUnavailableException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException, InterruptedException, ExecutionException {
		boolean tune = false;
		boolean run = false;
		
		// default values
		int BS = 2;
		double NVP = 1.0E-8;
		int PHL = 6;
		double GSM = 100000.0;
		double PS = 4.0;
		double MGS = 5.1E-5;
		
		int steps = 10;
		
		files = new ArrayList<File>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].charAt(0) != '-') {
				// File or directory selected
				File file = new File(args[i]);
				if (!file.exists()) {
					System.err.println("Error: File not found: " + args[i]);
					return;
				}
				
				files.addAll(getAllFilesRecursive(file));
				
			} else {
				// Option selected
				if (args[i].length() == 1) {
					argumentError(args[i]);
					return;
				}
				
				switch (args[i].charAt(1)) {
					case 'v':
						// Verbose
						VERBOSE = true;
						break;
						
					case 't':
						// Tune
						tune = true;
						try {
							steps = Integer.parseInt(args[++i]);
						} catch (Exception e) {
							// The steps param is optional
							i--;
						}
						break;
						
					case 'r':
						// Run
						run = true;
						break;
						
					case 'b':
						// Beam size
						try {
							BS = Integer.parseInt(args[++i]);
						} catch (Exception e) {
							argumentError("-b");
							return;
						}
						break;
						
					case 'n':
						// New voice probability
						try {
							NVP = Double.parseDouble(args[++i]);
						} catch (Exception e) {
							argumentError("-n");
							return;
						}
						break;
						
					case 'h':
						// Pitch history length
						try {
							PHL = Integer.parseInt(args[++i]);
						} catch (Exception e) {
							argumentError("-h");
							return;
						}
						break;
						
					case 'g':
						// Gap std micros
						try {
							GSM = Double.parseDouble(args[++i]);
						} catch (Exception e) {
							argumentError("-g");
							return;
						}
						break;
						
					case 'p':
						// Pitch std
						try {
							PS = Double.parseDouble(args[++i]);
						} catch (Exception e) {
							argumentError("-p");
							return;
						}
						break;
						
					case 'm':
						// Min gap score
						try {
							MGS = Double.parseDouble(args[++i]);
						} catch (Exception e) {
							argumentError("-m");
							return;
						}
						break;
						
					default:
						argumentError(args[i]);
						return;
				}
			}
		}
		
		songs = getSongs(files);
		
		VoiceSplittingParameters params = new VoiceSplittingParameters(BS, NVP, PHL, GSM, PS, MGS);
		
		if (tune) {
			VoiceSplittingParameters best = tune(steps);
			if (best != null) {
				params = best;
			}
		}

		if (run) {
			VoiceSplittingTesterReturn result = runTest(params);
			System.out.println(result);
		}
	}

	/**
	 * Recursively find all of the files under the given one
	 * 
	 * @param file The File under which to search.
	 * @return A list of all of the files under the given one, recursively grabbed from subdirectories.
	 */
	private static List<File> getAllFilesRecursive(File file) {
		List<File> files = new ArrayList<File>();
		
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				files.addAll(getAllFilesRecursive(subFile));
			}
			
		} else {
			files.add(file);
		}
		
		return files;
	}

	/**
	 * Tune the parameters.
	 * 
	 * @param steps The number of steps to make in our grid.
	 * @return The best params we found.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 * @throws MidiUnavailableException
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	private static VoiceSplittingParameters tune(int steps) throws InvalidMidiDataException, IOException, MidiUnavailableException, InterruptedException, ExecutionException {
		// min/max values
		double bsMin = 10, bsMax = 11;
		double nvpMin = 1E-9, nvpMax = 1.0E-7;
		double phlMin = 5, phlMax = 10;
		double gsmMin = 30000, gsmMax = 1000000;
		double psMin = 4, psMax = 9;
		double mgsMin = 1.0E-6, mgsMax = 1.0E-4;
		
		// step sizes
		steps = Math.max(1, steps);
		double bsStep = Math.max((bsMax - bsMin) / steps, 1);
		double nvpStep = (nvpMax - nvpMin) / steps;
		double phlStep = Math.max((phlMax - phlMin) / steps, 1);
		double gsmStep = (gsmMax - gsmMin) / steps;
		double psStep = Math.max((psMax - psMin) / steps, 0.5);
		double mgsStep = (mgsMax - mgsMin) / steps;
		
		// Enumerate testing parameters
		List<VoiceSplittingParameters> testList = new ArrayList<VoiceSplittingParameters>();
		
		for (double NVP = nvpMin; nvpMax - NVP > EPSILON; NVP += nvpStep) {
			for (double PHL = phlMin; phlMax - PHL > EPSILON; PHL += phlStep) {
				for (double GSM = gsmMin; gsmMax - GSM > EPSILON; GSM += gsmStep) {
					for (double PS = psMin; psMax - PS > EPSILON; PS += psStep) {
						for (double MGS = mgsMin; mgsMax - MGS > EPSILON; MGS += mgsStep) {
							for (double BS = bsMin; bsMax - BS > EPSILON; BS += bsStep) {
								testList.add(new VoiceSplittingParameters((int) Math.round(BS), NVP, (int)Math.round(PHL), GSM, PS, MGS));
							}
						}
					}
				}
			}
		}
		
		double paramsPerProc = ((double) testList.size()) / ((double) NUM_PROCS);
		
		// Create callables
		List<Callable<VoiceSplittingTesterReturn>> callables = new ArrayList<Callable<VoiceSplittingTesterReturn>>(NUM_PROCS);
	    for (int i = 0; i < NUM_PROCS; i++) {
	    	callables.add(new VoiceSplittingTester(
	    			testList.subList((int) Math.round(i * paramsPerProc), (int) Math.round((i + 1) * paramsPerProc))
	    	));
	    }
	    
	    VoiceSplittingTesterReturn best = new VoiceSplittingTesterReturn();

	    // Execute the callables
	    ExecutorService executor = Executors.newFixedThreadPool(NUM_PROCS);
	    List<Future<VoiceSplittingTesterReturn>> results = executor.invokeAll(callables);
	    
	    // Grab the results and save the best
	    for (Future<VoiceSplittingTesterReturn> result : results) {
	    	VoiceSplittingTesterReturn testerReturn = result.get();
	    	if (testerReturn.getF1() > best.getF1()) {
	    		best = testerReturn;
	    	}
	    }
	    executor.shutdown();
	    
	    System.out.println("BEST = " + best);
	    
	    return best.getParameters();
	}
	
	/**
	 * Run the VoiceSplitter on the given songs.
	 * 
	 * @param params The parameters we want to use for this run.
	 * @return The accuracy we get.
	 * 
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 * @throws MidiUnavailableException
	 */
	private static VoiceSplittingTesterReturn runTest(VoiceSplittingParameters params) throws InvalidMidiDataException, IOException, MidiUnavailableException {
		int noteCount = 0;
		int correct = 0;
		double voiceAccSum = 0;
		double voiceAccSongSum = 0;
		
		double recall = 0;
		double precision = 0;
		int songIndex = -1;

		for (List<MidiNote> song : songs) {
			List<List<MidiNote>> gS = goldStandard.get(++songIndex);
			if (VERBOSE) {
				System.out.println(files.get(songIndex).getAbsolutePath());
			}
			// The size of this Set will be the true number of voices in this song.
			Set<Integer> voiceCount = new HashSet<Integer>();
			for (MidiNote note : song) {
				voiceCount.add(note.getChannel());
			}
			
			voiceAccSongSum = 0;
			VoiceSplitter vs = new HmmVoiceSplitter(song, params);

			List<SingleNoteVoice> voices;
			try {
				voices = vs.getVoices();
			} catch (InterruptedException e) {
				System.err.println(e.getLocalizedMessage());
				return null;
			}
			
			int songTruePositives = 0;
			int songFalsePositives = 0;
			int songNoteCount = 0;

			for (SingleNoteVoice voice : voices) {
				int voiceNumNotes = voice.getNumNotes();
				int voiceCorrect = voice.getNumNotesCorrect();
				
				int voiceTruePositives = ((SingleNoteVoice) voice).getNumLinksCorrect(gS);
				int voiceFalsePositives = voiceNumNotes - voiceTruePositives - 1;
				
				songNoteCount += voiceNumNotes;
				correct += voiceCorrect;
				voiceAccSongSum += ((double) voiceCorrect) / voiceNumNotes;
				
				songTruePositives += voiceTruePositives;
				songFalsePositives += voiceFalsePositives;
				
				if (VERBOSE) {
					System.out.println(voiceCorrect + " / " + voiceNumNotes + " = " + (((double) voiceCorrect) / voiceNumNotes));
				}
			}
			
			int songFalseNegatives = songNoteCount - voiceCount.size() - songTruePositives;
			
			noteCount += songNoteCount;
			voiceAccSum += voiceAccSongSum / voices.size();
			
			precision += ((double) songTruePositives) / (songTruePositives + songFalsePositives);
			recall += ((double) songTruePositives) / (songTruePositives + songFalseNegatives);
			
			if (VERBOSE) {
				System.out.println("P=" + (((double) songTruePositives) / (songTruePositives + songFalsePositives)));
				System.out.println("R=" + (((double) songTruePositives) / (songTruePositives + songFalseNegatives)));
				System.out.println("F1=" + (2 * ((double) songTruePositives) / (2 * songTruePositives + songFalseNegatives + songFalsePositives)));
			}
		}

		double noteC = ((double) correct) / noteCount;
		double voiceC = voiceAccSum / songs.size();
		
		recall /= songs.size();
		precision /= songs.size();
		
		return new VoiceSplittingTesterReturn(params, noteC, voiceC, precision, recall);
	}
	
	/**
	 * Get the songs in the given midi Files.
	 * 
	 * @param files A List of Files (should be midi files).
	 * @return A List of the same songs.
	 * @throws InvalidMidiDataException
	 * @throws IOException
	 * @throws MidiUnavailableException
	 */
	private static List<List<MidiNote>> getSongs(List<File> files) throws InvalidMidiDataException, IOException, MidiUnavailableException {
		List<List<MidiNote>> songs = new ArrayList<List<MidiNote>>(files.size());
		goldStandard = new ArrayList<List<List<MidiNote>>>(files.size());
		
		for (File midi : files) {
			TimeTracker tt = new TimeTracker();
			NoteListGenerator nlg = new NoteListGenerator(tt);
		
			EventParser ep = new EventParser(midi, nlg, tt);
			try {
				ep.run();
			} catch (InterruptedException e) {
				System.err.println(e.getLocalizedMessage());
			}

			songs.add(nlg.getNoteList());
			goldStandard.add(ep.getGoldStandardVoices());
		}
		
		return songs;
	}
	
	/**
	 * Print an argument error to stderr.
	 * 
	 * @param arg The argument which caused the error.
	 */
	private static void argumentError(String arg) {
		StringBuilder sb = new StringBuilder("VoiceSplittingTester: Argument error: ");
		sb.append(arg).append('\n');
		
		sb.append("Usage: java VoiceSplittingTester args files\n");
		
		sb.append("PARAMETERS:\n");
		sb.append("-b INT = Set the Beam Size parameter to the value INT (defualt = 2)\n");
		sb.append("-n DOUBLE = Set the New Voice Probability parameter to the value DOUBLE(defualt = 1.0E-8)\n");
		sb.append("-h INT = Set the Pitch History Length parameter to the value INT(defualt = 6)\n");
		sb.append("-g DOUBLE = Set the Gap Std Micros parameter to the value DOUBLE(defualt = 100000.0)\n");
		sb.append("-p DOUBLE = Set the Pitch Std parameter to the value DOUBLE(defualt = 4.0)\n");
		sb.append("-m DOUBLE = Set the Min Gap Score parameter to the value DOUBLE(defualt = 5.1E-5)\n\n");
		
		sb.append("RUNNING:\n");
		sb.append("-t [STEPS] = Tune, and optionally set the number of steps to make within each parameter range");
				sb.append(" to an Integer value (default = 5)\n");
		sb.append("-r = Run test (if used with -t, we will use the tuned parameters instead of any given)\n");
		sb.append("-v = Verbose (print out each song and each individual voice when running)");
		
		System.err.println(sb);
	}
	
	/**
	 * A List of the parameters which we need to test with this Tester.
	 */
	private List<VoiceSplittingParameters> parametersList;
	
	/**
	 * Create a new Tester which should test on the given parameters.
	 * 
	 * @param params {@link #parametersList}
	 */
	public VoiceSplittingTester(List<VoiceSplittingParameters> params) {
		parametersList = params;
	}

	/**
	 * Run the test on our List of parameters, and return the best result.
	 * 
	 * @return A VoiceSplittingTester representing the best result found from our
	 * List of parameters.
	 */
	@Override
	public VoiceSplittingTesterReturn call() throws Exception {
		VoiceSplittingTesterReturn best = new VoiceSplittingTesterReturn();
		
		for (VoiceSplittingParameters params : parametersList) {
			VoiceSplittingTesterReturn result = runTest(params);
			
			System.out.println(result);
			
			if (result.getF1() > best.getF1()) {
				best = result;
			}
		}
		
		return best;
	}
}

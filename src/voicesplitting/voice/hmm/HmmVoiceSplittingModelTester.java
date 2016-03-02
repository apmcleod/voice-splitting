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

import voicesplitting.generic.MidiModel;
import voicesplitting.parsing.EventParser;
import voicesplitting.parsing.NoteListGenerator;
import voicesplitting.time.TimeTracker;
import voicesplitting.utils.MidiNote;
import voicesplitting.voice.Voice;
import voicesplitting.voice.VoiceSplittingModel;

/**
 * An <code>HmmVoiceSplittingModelTester</code> contains the {@link #main(String[])} method used
 * to train and test the {@link HmmVoiceSplittingModel} class from the command line.
 * <p>
 * It can run multiple threads (total number according to {@link #NUM_PROCS}) simultaneously, managing
 * return values and reporting the best setting for {@link HmmVoiceSplittingModelParameters}.
 * <p>
 * This was the class used to perform training for the paper.
 * 
 * @author Andrew McLeod - 10 April, 2015
 * @version 1.0
 * @since 1.0
 */
public class HmmVoiceSplittingModelTester implements Callable<HmmVoiceSplittingModelTesterReturn> {
	
	/**
	 * The number of processes to use for training, generated from the number of available
	 * processors.
	 */
	private static final int NUM_PROCS = Runtime.getRuntime().availableProcessors();

	/**
	 * Epsilon needed for double comparison.
	 */
	private static final double EPSILON = 0.000000001;
	
	/**
	 * Print song-by song voice details when running.
	 * <p>
	 * False by default, but can be set to true by using the <code>-v</code> flag.
	 */
	private static boolean VERBOSE = false;
	
	/**
	 * True if we want to use the input data's channel as gold standard voices. False to use track instead.
	 * <p>
	 * True by default, but can be set to false by using the <code>-T</code> flag.
	 */
	private static boolean USE_CHANNEL = true;
	
	/**
	 * A {@link NoteListGenerator} for each of the songs we are evaluating.
	 */
	private static List<NoteListGenerator> songs;
	
	/**
	 * The gold standard voices for each of the songs we are evaluating.
	 * <p>
	 * Specifically <code>goldStandard.get(i - 1)</code> is a List of the gold standard voices for the ith
	 * song, where a voice is an ordered List of the {@link MidiNote}s contained within.
	 */
	private static List<List<List<MidiNote>>> goldStandard;
	
	/**
	 * A List of the Files we're reading MIDI data in from.
	 */
	private static List<File> files;

	/**
	 * Train or test the voice splitter. The arguments are as follows:
	 * <p>
	 * <code>java HmmVoiceSplittingModelTester ARGS Files</code>
	 * <p>
	 * Here, <code>Files</code> should be a list of 1 or more MIDI files or directories containing only MIDI
	 * files. Any directories listed will be searched recursively for all files, and if any of the files
	 * found are not in MIDI format, an InvalidMidiDataException will be thrown.
	 * <p>
	 * ARGS:
	 * <ul>
	 * <li>
	 * Running arguments:
	 * <ul>
	 * <li><code>-r</code> = Run a test (if used with <code>-t</code>, the tuned parameters instead of any given).</li>
	 * <li><code>-t [STEPS]</code> = Tune to maximize F1-Measure, and optionally set the number of steps to make
     * within each parameter range to an Integer value (default = 5). It is HIGHLY recommended to use this training
     * method rather than your own script because it runs the tests in parallel as much as possible to speed up
     * training.</li>
     * <li><code>-v</code> = Verbose (print out each song and each individual voice when running).</li>
     * <li><code>-T</code> = Use tracks as correct voice (instead of channels).</li>
     * </ul>
     * </li>
     * <li>
     * If running with <code>-r</code>, the following arguments can be used to change the parameter settings from their default
     * values (those with which we tested the computer generated WTC fugues in the paper):
     * <ul>
     * <li><code>-b INT</code> = Set the Beam Size parameter to the value INT (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#BEAM_SIZE_DEFAULT}).</li>
     * <li><code>-n DOUBLE</code> = Set the New Voice Probability parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#NEW_VOICE_PROBABILITY_DEFAULT}).</li>
     * <li><code>-h INT</code> = Set the Pitch History Length parameter to the value INT (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#PITCH_HISTORY_LENGTH_DEFAULT}).</li>
     * <li><code>-g DOUBLE</code> = Set the Gap Std Micros parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#GAP_STD_MICROS_DEFAULT}).</li>
     * <li><code>-p DOUBLE</code> = Set the Pitch Std parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#PITCH_STD_DEFAULT}).</li>
     * <li><code>-m DOUBLE</code> = Set the Min Gap Score parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#MIN_GAP_SCORE_DEFAULT}).</li>
     * </ul>
     * </li>
     * </ul>
	 * 
	 * @param args The command line arguments.
	 * 
	 * @throws IOException If there was some I/O error in reading one of the Files.
	 * @throws InvalidMidiDataException If one of the Files is not in proper MIDI format.
	 * @throws ExecutionException If there is some generic execution exception.
	 * @throws InterruptedException If there is some interrupt received.
	 */
	public static void main(String[] args) throws InvalidMidiDataException, IOException, InterruptedException, ExecutionException {
		boolean tune = false;
		boolean run = false;
		
		// default values
		int BS = HmmVoiceSplittingModelParameters.BEAM_SIZE_DEFAULT;
		double NVP = HmmVoiceSplittingModelParameters.NEW_VOICE_PROBABILITY_DEFAULT;
		int PHL = HmmVoiceSplittingModelParameters.PITCH_HISTORY_LENGTH_DEFAULT;
		double GSM = HmmVoiceSplittingModelParameters.GAP_STD_MICROS_DEFAULT;
		double PS = HmmVoiceSplittingModelParameters.PITCH_STD_DEFAULT;
		double MGS = HmmVoiceSplittingModelParameters.MIN_GAP_SCORE_DEFAULT;
		
		int steps = 5;
		
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
					case 'T':
						// Use track
						USE_CHANNEL = false;
						break;
						
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
		
		HmmVoiceSplittingModelParameters params = new HmmVoiceSplittingModelParameters(BS, NVP, PHL, GSM, PS, MGS);
		
		if (tune) {
			HmmVoiceSplittingModelParameters best = tune(steps);
			if (best != null) {
				params = best;
			}
		}

		if (run) {
			HmmVoiceSplittingModelTesterReturn result = runTest(params);
			System.out.println(result);
		}
		
		if (!tune && !run) {
			argumentError("Neither -t nor -r selected");
		}
	}

	/**
	 * Tune the {@link HmmVoiceSplittingModelParameters}.
	 * 
	 * @param steps The number of steps to make in our grid search.
	 * @return The best {@link HmmVoiceSplittingModelParameters} we found.
	 * 
	 * @throws ExecutionException If there is some generic execution exception.
	 * @throws InterruptedException If there is some interrupt received.
	 */
	private static HmmVoiceSplittingModelParameters tune(int steps) throws InterruptedException, ExecutionException {
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
		List<HmmVoiceSplittingModelParameters> testList = new ArrayList<HmmVoiceSplittingModelParameters>();
		
		for (double NVP = nvpMin; nvpMax - NVP > EPSILON; NVP += nvpStep) {
			for (double PHL = phlMin; phlMax - PHL > EPSILON; PHL += phlStep) {
				for (double GSM = gsmMin; gsmMax - GSM > EPSILON; GSM += gsmStep) {
					for (double PS = psMin; psMax - PS > EPSILON; PS += psStep) {
						for (double MGS = mgsMin; mgsMax - MGS > EPSILON; MGS += mgsStep) {
							for (double BS = bsMin; bsMax - BS > EPSILON; BS += bsStep) {
								testList.add(new HmmVoiceSplittingModelParameters((int) Math.round(BS), NVP, (int)Math.round(PHL), GSM, PS, MGS));
							}
						}
					}
				}
			}
		}
		
		double paramsPerProc = ((double) testList.size()) / ((double) NUM_PROCS);
		
		// Create callables
		List<Callable<HmmVoiceSplittingModelTesterReturn>> callables = new ArrayList<Callable<HmmVoiceSplittingModelTesterReturn>>(NUM_PROCS);
	    for (int i = 0; i < NUM_PROCS; i++) {
	    	callables.add(new HmmVoiceSplittingModelTester(
	    			testList.subList((int) Math.round(i * paramsPerProc), (int) Math.round((i + 1) * paramsPerProc))
	    	));
	    }
	    
	    HmmVoiceSplittingModelTesterReturn best = new HmmVoiceSplittingModelTesterReturn();

	    // Execute the callables
	    ExecutorService executor = Executors.newFixedThreadPool(NUM_PROCS);
	    List<Future<HmmVoiceSplittingModelTesterReturn>> results = executor.invokeAll(callables);
	    
	    // Grab the results and save the best
	    for (Future<HmmVoiceSplittingModelTesterReturn> result : results) {
	    	HmmVoiceSplittingModelTesterReturn testerReturn = result.get();
	    	if (testerReturn.getF1() > best.getF1()) {
	    		best = testerReturn;
	    	}
	    }
	    executor.shutdown();
	    
	    System.out.println("BEST = " + best);
	    
	    return best.getParameters();
	}
	
	/**
	 * Run the {@link HmmVoiceSplittingModel} on the given songs.
	 * 
	 * @param params The {@link HmmVoiceSplittingModelParameters} we want to use for this run.
	 * @return The {@link HmmVoiceSplittingModelTesterReturn} object containing the parameters and the
	 * achieved accuracy.
	 */
	private static HmmVoiceSplittingModelTesterReturn runTest(HmmVoiceSplittingModelParameters params) {
		double voiceAccSum = 0;
		double voiceAccSongSum = 0;
		
		double recall = 0;
		double precision = 0;
		int songIndex = -1;

		for (NoteListGenerator nlg : songs) {
			List<List<MidiNote>> gS = goldStandard.get(++songIndex);
			if (VERBOSE) {
				System.out.println(files.get(songIndex).getAbsolutePath());
			}
			// The size of this Set will be the true number of voices in this song.
			Set<Integer> voiceCount = new HashSet<Integer>();
			for (MidiNote note : nlg.getNoteList()) {
				voiceCount.add(note.getCorrectVoice());
			}
			
			voiceAccSongSum = 0;
			VoiceSplittingModel vs = new HmmVoiceSplittingModel(params);
			
			performInference(vs, nlg);

			List<Voice> voices = vs.getHypotheses().first().getVoices();
			
			int songTruePositives = 0;
			int songFalsePositives = 0;
			int songNoteCount = 0;

			for (Voice voice : voices) {
				int voiceNumNotes = voice.getNumNotes();
				int voiceCorrect = voice.getNumNotesCorrect();
				
				int voiceTruePositives = voice.getNumLinksCorrect(gS);
				int voiceFalsePositives = voiceNumNotes - voiceTruePositives - 1;
				
				songNoteCount += voiceNumNotes;
				voiceAccSongSum += ((double) voiceCorrect) / voiceNumNotes;
				
				songTruePositives += voiceTruePositives;
				songFalsePositives += voiceFalsePositives;
				
				if (VERBOSE) {
					System.out.println(voiceCorrect + " / " + voiceNumNotes + " = " + (((double) voiceCorrect) / voiceNumNotes));
				}
			}
			
			int songFalseNegatives = songNoteCount - voiceCount.size() - songTruePositives;
			
			voiceAccSum += voiceAccSongSum / voices.size();
			
			precision += ((double) songTruePositives) / (songTruePositives + songFalsePositives);
			recall += ((double) songTruePositives) / (songTruePositives + songFalseNegatives);
			
			if (VERBOSE) {
				System.out.println("P=" + (((double) songTruePositives) / (songTruePositives + songFalsePositives)));
				System.out.println("R=" + (((double) songTruePositives) / (songTruePositives + songFalseNegatives)));
				System.out.println("F1=" + (2 * ((double) songTruePositives) / (2 * songTruePositives + songFalseNegatives + songFalsePositives)));
			}
		}
		
		double voiceC = voiceAccSum / songs.size();
		
		recall /= songs.size();
		precision /= songs.size();
		
		return new HmmVoiceSplittingModelTesterReturn(params, voiceC, precision, recall);
	}
	
	/**
	 * Generate a {@link NoteListGenerator}s for each given MIDI File and return them in a List.
	 * 
	 * @param files A List of Files to read (should be MIDI files).
	 * @return A List of the {@link NoteListGenerator}s for each song.
	 * @throws IOException If there was some I/O error in reading one of the Files.
	 * @throws InvalidMidiDataException If one of the Files is not in proper MIDI format.
	 */
	private static List<NoteListGenerator> getSongs(List<File> files) throws InvalidMidiDataException, IOException {
		List<NoteListGenerator> songs = new ArrayList<NoteListGenerator>(files.size());
		goldStandard = new ArrayList<List<List<MidiNote>>>(files.size());
		
		for (File midi : files) {
			TimeTracker tt = new TimeTracker();
			NoteListGenerator nlg = new NoteListGenerator(tt);
		
			EventParser ep = new EventParser(midi, nlg, tt, USE_CHANNEL);
			try {
				ep.run();
			} catch (InterruptedException e) {
				System.err.println(e.getLocalizedMessage());
			}

			songs.add(nlg);
			goldStandard.add(ep.getGoldStandardVoices());
		}
		
		return songs;
	}
	
	/**
	 * Perform inference on the given model.
	 * 
	 * @param model The model on which we want to perform inference.
	 * @param nlg The NoteListGenerator which will give us the incoming note lists.
	 */
	public static void performInference(MidiModel model, NoteListGenerator nlg) {
		for (List<MidiNote> incoming : nlg.getIncomingLists()) {
			model.handleIncoming(incoming);
		}
	}
	
	/**
	 * Get and return a List of every File beneath the given one recursively.
	 * 
	 * @param file The head File.
	 * @return A List of every File under the given head.
	 */
	public static List<File> getAllFilesRecursive(File file) {
		List<File> files = new ArrayList<File>();
		
		if (file.isFile()) {
			files.add(file);
			
		} else if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			if (fileList != null) {
				for (File subFile : fileList) {
					files.addAll(getAllFilesRecursive(subFile));
				}
			}
		}
		
		return files;
	}
	
	/**
	 * Print an argument error to stderr. The correct arguments are as follows:
	 * <p>
	 * <code>java HmmVoiceSplittingModelTester ARGS Files</code>
	 * <p>
	 * Here, <code>Files</code> should be a list of 1 or more MIDI files or directories containing only MIDI
	 * files. Any directories listed will be searched recursively for all files, and if any of the files
	 * found are not in MIDI format, an InvalidMidiDataException will be thrown.
	 * <p>
	 * ARGS:
	 * <ul>
	 * <li>
	 * Running arguments:
	 * <ul>
	 * <li><code>-r</code> = Run a test (if used with <code>-t</code>, the tuned parameters instead of any given).</li>
	 * <li><code>-t [STEPS]</code> = Tune to maximize F1-Measure, and optionally set the number of steps to make
     * within each parameter range to an Integer value (default = 5). It is HIGHLY recommended to use this training
     * method rather than your own script because it runs the tests in parallel as much as possible to speed up
     * training.</li>
     * <li><code>-v</code> = Verbose (print out each song and each individual voice when running).</li>
     * <li><code>-T</code> = Use tracks as correct voice (instead of channels).</li>
     * </ul>
     * </li>
     * <li>
     * If running with <code>-r</code>, the following arguments can be used to change the parameter settings from their default
     * values (those with which we tested the computer generated WTC fugues in the paper):
     * <ul>
     * <li><code>-b INT</code> = Set the Beam Size parameter to the value INT (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#BEAM_SIZE_DEFAULT}).</li>
     * <li><code>-n DOUBLE</code> = Set the New Voice Probability parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#NEW_VOICE_PROBABILITY_DEFAULT}).</li>
     * <li><code>-h INT</code> = Set the Pitch History Length parameter to the value INT (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#PITCH_HISTORY_LENGTH_DEFAULT}).</li>
     * <li><code>-g DOUBLE</code> = Set the Gap Std Micros parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#GAP_STD_MICROS_DEFAULT}).</li>
     * <li><code>-p DOUBLE</code> = Set the Pitch Std parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#PITCH_STD_DEFAULT}).</li>
     * <li><code>-m DOUBLE</code> = Set the Min Gap Score parameter to the value DOUBLE (defualt = {@value voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters#MIN_GAP_SCORE_DEFAULT}).</li>
     * </ul>
     * </li>
     * </ul>
	 * 
	 * @param arg The argument which caused the error.
	 */
	private static void argumentError(String arg) {
		StringBuilder sb = new StringBuilder("VoiceSplittingTester: Argument error: ");
		sb.append(arg).append('\n');
		
		sb.append("Usage: java VoiceSplittingTester ARGS Files\n");
		
		sb.append("RUNNING:\n");
		sb.append("-t [STEPS] = Tune, and optionally set the number of steps to make within each parameter range");
				sb.append(" to an Integer value (default = 5)\n");
		sb.append("-r = Run test (if used with -t, we will use the tuned parameters instead of any given)\n");
		sb.append("-v = Verbose (print out each song and each individual voice when running)\n");
		sb.append("-T = Use tracks as correct voice (instead of channels)\n\n");
		sb.append("Note that either -t or -r is required for the program to run.\n\n");
		
		sb.append("PARAMETERS (with -r):\n");
		sb.append("-b INT = Set the Beam Size parameter to the value INT (defualt = " + HmmVoiceSplittingModelParameters.BEAM_SIZE_DEFAULT + ")\n");
		sb.append("-n DOUBLE = Set the New Voice Probability parameter to the value DOUBLE(defualt = " + HmmVoiceSplittingModelParameters.NEW_VOICE_PROBABILITY_DEFAULT + ")\n");
		sb.append("-h INT = Set the Pitch History Length parameter to the value INT(defualt = " + HmmVoiceSplittingModelParameters.PITCH_HISTORY_LENGTH_DEFAULT + ")\n");
		sb.append("-g DOUBLE = Set the Gap Std Micros parameter to the value DOUBLE(defualt = " + HmmVoiceSplittingModelParameters.GAP_STD_MICROS_DEFAULT + ")\n");
		sb.append("-p DOUBLE = Set the Pitch Std parameter to the value DOUBLE(defualt = " + HmmVoiceSplittingModelParameters.PITCH_STD_DEFAULT + ")\n");
		sb.append("-m DOUBLE = Set the Min Gap Score parameter to the value DOUBLE(defualt = " + HmmVoiceSplittingModelParameters.MIN_GAP_SCORE_DEFAULT + ")\n\n");
		
		System.err.println(sb);
	}
	
	/**
	 * A List of the parameters which we need to test with this Tester thread.
	 */
	private List<HmmVoiceSplittingModelParameters> parametersList;
	
	/**
	 * Create a new Tester thread which should test on the given {@link HmmVoiceSplittingModelParameters}.
	 * 
	 * @param params {@link #parametersList}
	 */
	public HmmVoiceSplittingModelTester(List<HmmVoiceSplittingModelParameters> params) {
		parametersList = params;
	}

	/**
	 * Run the test on our List of {@link HmmVoiceSplittingModelParameters}, and return the best result.
	 * 
	 * @return A {@link HmmVoiceSplittingModelTesterReturn} representing the best result found from our
	 * List of parameters.
	 * @throws Exception If any exception occurs, most often an interrupt.
	 */
	@Override
	public HmmVoiceSplittingModelTesterReturn call() throws Exception {
		HmmVoiceSplittingModelTesterReturn best = new HmmVoiceSplittingModelTesterReturn();
		
		for (HmmVoiceSplittingModelParameters params : parametersList) {
			HmmVoiceSplittingModelTesterReturn result = runTest(params);
			
			System.out.println(result);
			
			if (result.getF1() > best.getF1()) {
				best = result;
			}
		}
		
		return best;
	}
}

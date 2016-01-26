package voicesplitting.voice.hmm;

/**
 * The parameters to use for an {@link HmmVoiceSplittingModel}.
 * 
 * @author Andrew McLeod - 13 April, 2015
 */
public class HmmVoiceSplittingModelParameters implements Comparable<HmmVoiceSplittingModelParameters> {
	/**
	 * The minimum possible gap score we will give out.
	 */
	public final double MIN_GAP_SCORE;
	
	/**
	 * The default value for {@link #MIN_GAP_SCORE}.
	 */
	public static final double MIN_GAP_SCORE_DEFAULT = 7E-5;
	
	/**
	 * The standard deviation to use for the Gaussian distribution used to calculate the
	 * pitch score. A pitch gap of exactly this many
	 * semitones will result in a Gaussian score of 1/sqrt(e).
	 */
	public final double PITCH_STD;
	
	/**
	 * The default value for {@link #PITCH_STD}.
	 */
	public static final double PITCH_STD_DEFAULT = 4;

	/**
	 * The standard deviation to use for the Gaussian distribution used to calculate the
	 * gap score. A gap of exactly this many microseconds will result in a Gaussian score of 1/sqrt(e).
	 */
	public final double GAP_STD_MICROS;
	
	/**
	 * The default value for {@link #GAP_STD_MICROS}.
	 */
	public static final double GAP_STD_MICROS_DEFAULT = 224000;
	
	/**
	 * The number of chords back we want to look in order to determine the weighted pitch.
	 */
	public final int PITCH_HISTORY_LENGTH;
	
	/**
	 * The default value for {@link #PITCH_HISTORY_LENGTH}
	 */
	public static final int PITCH_HISTORY_LENGTH_DEFAULT = 11;
	
	/**
	 * The probability score to return for adding a note to an empty Voice.
	 */
	public final double NEW_VOICE_PROBABILITY;
	
	/**
	 * The default value for {@link #NEW_VOICE_PROBABILITY}.
	 */
	public static final double NEW_VOICE_PROBABILITY_DEFAULT = 5E-10;
	
	/**
	 * The beam size for our search. It seems that 1 is optimal anyways, so we can remove this probably.
	 */
	public final int BEAM_SIZE;
	
	/**
	 * The default value for {@link #BEAM_SIZE}.
	 */
	public static final int BEAM_SIZE_DEFAULT = 25;
	
	/**
	 * Create a new params object with the given values.
	 * 
	 * @param BS {@link #BEAM_SIZE}
	 * @param NVP {@link #NEW_VOICE_PROBABILITY}
	 * @param PHL {@link #PITCH_HISTORY_LENGTH}
	 * @param GSM {@link #GAP_STD_MICROS}
	 * @param PS {@link #PITCH_STD}
	 * @param MGS {@link #MIN_GAP_SCORE}
	 */
	public HmmVoiceSplittingModelParameters(int BS, double NVP, int PHL, double GSM, double PS, double MGS) {
		BEAM_SIZE = BS;
		NEW_VOICE_PROBABILITY = NVP;
		PITCH_HISTORY_LENGTH = PHL;
		GAP_STD_MICROS = GSM;
		PITCH_STD = PS;
		MIN_GAP_SCORE = MGS;
	}
	
	/**
	 * Create new params with default values. The default values referred to here are those found to be optimized
	 * when tuning to test on the computer-generated WTC Fugues, as noted in the paper. Specifically:
	 * <br>
	 * {@link #BEAM_SIZE}<code> = 25</code>
	 * {@link #NEW_VOICE_PROBABILITY}<code> = 5E-10</code>
	 * {@link #PITCH_HISTORY_LENGTH}<code> = 11</code>
	 * {@link #GAP_STD_MICROS}<code> = 224000</code>
	 * {@link #PITCH_STD}<code> = 4</code>
	 * {@link #MIN_GAP_SCORE}<code> = 7E-5</code>
	 */
	public HmmVoiceSplittingModelParameters() {
		this(BEAM_SIZE_DEFAULT, NEW_VOICE_PROBABILITY_DEFAULT, PITCH_HISTORY_LENGTH_DEFAULT, GAP_STD_MICROS_DEFAULT, PITCH_STD_DEFAULT, MIN_GAP_SCORE_DEFAULT);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof HmmVoiceSplittingModelParameters)) {
			return false;
		}
		
		HmmVoiceSplittingModelParameters p = (HmmVoiceSplittingModelParameters) other;
		
		return BEAM_SIZE == p.BEAM_SIZE
				&& NEW_VOICE_PROBABILITY == p.NEW_VOICE_PROBABILITY
				&& PITCH_HISTORY_LENGTH == p.PITCH_HISTORY_LENGTH
				&& GAP_STD_MICROS == p.GAP_STD_MICROS
				&& PITCH_STD == p.PITCH_STD
				&& MIN_GAP_SCORE == p.MIN_GAP_SCORE;		
	}
	
	@Override
	public int hashCode() {
		return BEAM_SIZE +
				Double.valueOf(NEW_VOICE_PROBABILITY).hashCode() +
				PITCH_HISTORY_LENGTH +
				Double.valueOf(GAP_STD_MICROS).hashCode() +
				Double.valueOf(PITCH_STD).hashCode() + 
				Double.valueOf(MIN_GAP_SCORE).hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		
		sb.append(BEAM_SIZE).append(',');
		sb.append(NEW_VOICE_PROBABILITY).append(',');
		sb.append(PITCH_HISTORY_LENGTH).append(',');
		sb.append(GAP_STD_MICROS).append(',');
		sb.append(PITCH_STD).append(',');
		sb.append(MIN_GAP_SCORE).append(')');
		
		return sb.toString();
	}

	@Override
	public int compareTo(HmmVoiceSplittingModelParameters o) {
		if (o == null) {
			return -1;
		}
		
		int result = Integer.compare(BEAM_SIZE, o.BEAM_SIZE);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(MIN_GAP_SCORE, o.MIN_GAP_SCORE);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(PITCH_STD, o.PITCH_STD);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(GAP_STD_MICROS, o.GAP_STD_MICROS);
		if (result != 0) {
			return result;
		}
		
		result = Double.compare(NEW_VOICE_PROBABILITY, o.NEW_VOICE_PROBABILITY);
		if (result != 0) {
			return result;
		}
		
		return Integer.compare(PITCH_HISTORY_LENGTH, o.PITCH_HISTORY_LENGTH);
	}
}
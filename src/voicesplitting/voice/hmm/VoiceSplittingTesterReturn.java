package voicesplitting.voice.hmm;

/**
 * The return object for a {@link VoiceSplittingTester} thread. This is used because we need
 * both the parameters and their accuracy, yet a Callble can only return 1 value.
 * 
 * @author Andrew McLeod - 14 April, 2015
 */
public class VoiceSplittingTesterReturn {
	/**
	 * The parameters we used.
	 */
	private VoiceSplittingParameters parameters;
	
	/**
	 * The note consistency we achieved.
	 */
	private double noteConsistency;
	
	/**
	 * The voice consistency we achieved.
	 */
	private double voiceConsistency;
	
	/**
	 * The precision we achieved.
	 */
	private double precision;
	
	/**
	 * The recall we achieved.
	 */
	private double recall;
	
	/**
	 * Create a new empty Return result.
	 */
	public VoiceSplittingTesterReturn() {
		this(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	/**
	 * Create a new return result.
	 * 
	 * @param params {@link #parameters}
	 * @param noteC {@link #noteConsistency}
	 * @param voiceC {@link #voiceConsistency}
	 * @param prec {@link #precision}
	 * @param rec {@link #recall}
	 */
	public VoiceSplittingTesterReturn(VoiceSplittingParameters params, double noteC, double voiceC, double prec, double rec) {
		parameters = params;
		noteConsistency = noteC;
		voiceConsistency = voiceC;
		precision = prec;
		recall = rec;
	}
	
	/**
	 * Get the parameters from this run.
	 * 
	 * @return {@link #parameters}
	 */
	public VoiceSplittingParameters getParameters() {
		return parameters;
	}
	
	/**
	 * Get the note consistency from this run.
	 * 
	 * @return {@link #noteConsistency}
	 */
	public double getNoteConsistency() {
		return noteConsistency;
	}
	
	/**
	 * Get the voice consistency from this run.
	 * 
	 * @return {@link #voiceConsistency}
	 */
	public double getVoiceConsistency() {
		return voiceConsistency;
	}
	
	/**
	 * Get the precision from this run.
	 * 
	 * @return {@link #precision}
	 */
	public double getPrecision() {
		return precision;
	}
	
	/**
	 * Get the recall from this run.
	 * 
	 * @return {@link #recall}
	 */
	public double getRecall() {
		return recall;
	}
	
	/**
	 * Get the F1 score from this run. That is, the harmonic mean of {@link #precision} and {@link #recall}.
	 * 
	 * @return The F1 score from this run.
	 */
	public double getF1() {
		return precision == Double.NEGATIVE_INFINITY ? Double.NEGATIVE_INFINITY : 2 * precision * recall / (precision + recall);
	}
	
	/**
	 * Set the params from this run. Use this instead of creating a new object every time to
	 * save garbage collect time.
	 * 
	 * @param params {@link #parameters}
	 */
	public void setParams(VoiceSplittingParameters params) {
		parameters = params;
	}

	/**
	 * Set the note consistency of this run. Use this instead of creating a new object every time to
	 * save garbage collect time.
	 * 
	 * @param noteC {@link #noteConsistency}
	 */
	public void setNoteConsistency(double noteC) {
		noteConsistency = noteC;
	}
	
	/**
	 * Set the voice consistency of this run. Use this instead of creating a new object every time to
	 * save garbage collect time.
	 * 
	 * @param voiceC {@link #voiceConsistency}
	 */
	public void setVoiceConsistency(double voiceC) {
		voiceConsistency = voiceC;
	}
	
	/**
	 * Set the precision of this run. Use this instead of creating a new object every time to
	 * save garbage collect time.
	 * 
	 * @param prec {@link #precision}
	 */
	public void setPrecision(double prec) {
		precision = prec;
	}
	
	/**
	 * Set the recall of this run. Use this instead of creating a new object every time to
	 * save garbage collect time.
	 * 
	 * @param rec {@link #recall}
	 */
	public void setRecall(double rec) {
		recall = rec;
	}
	
	@Override
	public String toString() {
		return parameters + " = V=" + voiceConsistency + " N=" + noteConsistency + " P=" + precision + " R=" + recall + " F1=" + getF1();
	}
}

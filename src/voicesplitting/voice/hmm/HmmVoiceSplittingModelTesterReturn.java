package voicesplitting.voice.hmm;

/**
 * An <code>HmmVoiceSplittingModelTesterReturn</code> is the return object for a
 * {@link HmmVoiceSplittingModelTester} thread.
 * <p>
 * This is used because we need both the parameters and their accuracy, yet a Callable can
 * only return 1 object.
 * 
 * @author Andrew McLeod - 14 April, 2015
 * @version 1.0
 * @since 1.0
 */
public class HmmVoiceSplittingModelTesterReturn {
	/**
	 * The parameters we used.
	 */
	private HmmVoiceSplittingModelParameters parameters;
	
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
	 * Create a new empty Return result with null {@link #parameters}, and where every value is set to
	 * {@link Double#NEGATIVE_INFINITY}.
	 */
	public HmmVoiceSplittingModelTesterReturn() {
		this(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	/**
	 * Create a new return result.
	 * 
	 * @param params {@link #parameters}
	 * @param voiceC {@link #voiceConsistency}
	 * @param prec {@link #precision}
	 * @param rec {@link #recall}
	 */
	public HmmVoiceSplittingModelTesterReturn(HmmVoiceSplittingModelParameters params, double voiceC, double prec, double rec) {
		parameters = params;
		voiceConsistency = voiceC;
		precision = prec;
		recall = rec;
	}
	
	/**
	 * Get the parameters from this run.
	 * 
	 * @return {@link #parameters}
	 */
	public HmmVoiceSplittingModelParameters getParameters() {
		return parameters;
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
	public void setParams(HmmVoiceSplittingModelParameters params) {
		parameters = params;
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
	
	/**
	 * Get the String representation of this object, which is in the following format:
	 * <p>
	 * <code>{@link #parameters} = V={@link #voiceConsistency} P={@link #precision} R={@link #recall} F1={@link #getF1()}</code>
	 * 
	 * @return The String representation of this HmmVoiceSplittingModelTesterReturn object.
	 */
	@Override
	public String toString() {
		return parameters + " = V=" + voiceConsistency + " P=" + precision + " R=" + recall + " F1=" + getF1();
	}
}

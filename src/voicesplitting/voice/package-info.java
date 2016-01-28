/**
 * Provides functionality for performing voice separation.
 * <p>
 * Those wanting to create their own voice splitters should extend the abstract
 * {@link voicesplitting.voice.VoiceSplittingModel} and {@link voicesplitting.voice.VoiceSplittingModelState}
 * classes here in a new package, as was done by
 * {@link voicesplitting.voice.hmm.HmmVoiceSplittingModel} and
 * {@link voicesplitting.voice.hmm.HmmVoiceSplittingModelState}.
 *
 * @author Andrew McLeod - 28 Jan, 2016
 * @version 1.0
 * @since 1.0
 */
package voicesplitting.voice;
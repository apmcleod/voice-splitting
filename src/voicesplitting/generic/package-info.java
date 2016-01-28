/**
 * Provides abstract classes to guide the creation of MIDI models and their states.
 * <p>
 * MIDI models which do not perform voice separation should extend these classes further
 * as was done in {@link voicesplitting.voice.VoiceSplittingModel} and
 * {@link voicesplitting.voice.VoiceSplittingModelState}, while
 * MIDI models which do perform voice separation should implement those classes directly.
 *
 * @author Andrew McLeod - 28 Jan, 2016
 * @since 1.0
 * @version 1.0
 */
package voicesplitting.generic;
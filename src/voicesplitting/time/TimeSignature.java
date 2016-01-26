package voicesplitting.time;


/**
 * A <code>TimeSignature</code> represents some MIDI data's beat structure (time signature).
 * Equality is based only on the numerator and denominator.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 */
public class TimeSignature {

	/**
	 * The numerator of the time signature.
	 */
	private final int numerator;
	
	/**
	 * The denominator of the time signature.
	 */
	private final int denominator;
	
	/**
	 * The number of metronome ticks per beat at this time signature.
	 */
	private final int metronomeTicksPerBeat;
	
	/**
	 * The number of 32nd notes per quarter note at this time signature.
	 */
	private final int notes32PerQuarter;
	
	/**
	 * Create a new default TimeSignature (4/4 time)
	 */
	public TimeSignature() {
		this(new byte[] {4, 2, 24, 8});
	}
	
	/**
	 * Create a new TimeSignature from the given data array.
	 * 
	 * @param data Data array, parsed directly from midi.
	 */
	public TimeSignature(byte[] data) {
		numerator = data[0];
		denominator = (int) Math.pow(2, data[1]);
		metronomeTicksPerBeat = data[2];
		notes32PerQuarter = data[3];
	}
	
	/**
	 * Get the number of metronome ticks per beat.
	 * 
	 * @return {@link #metronomeTicksPerBeat}
	 */
	public int getMetronomeTicksPerBeat() {
		return metronomeTicksPerBeat;
	}
	
	/**
	 * Get the number of 32nd notes per quarter note.
	 * 
	 * @return {@link #notes32PerQuarter}
	 */
	public int getNotes32PerQuarter() {
		return notes32PerQuarter;
	}
	
	/**
	 * Get the number of MIDI ticks per 32nd note at this time signature.
	 * 
	 * @param ppq The pulses per quarter note of the song.
	 * @return The number of MIDI ticks per 32nd note.
	 */
	public int getTicksPerNote32(double ppq) {
		return (int) (ppq / notes32PerQuarter);
	}
	
	/**
	 * Get the number of 32nd notes per measure at this time signature.
	 * 
	 * @return The number of 32nd notes per measure.
	 */
	public int getNotes32PerMeasure() {
		return (notes32PerQuarter * numerator * 4 / denominator);
	}
	
	/**
	 * Get the numerator of this time signature.
	 * 
	 * @return {@link #numerator}
	 */
	public int getNumerator() {
		return numerator;
	}
	
	/**
	 * Get the denominator of this time signature.
	 * 
	 * @return {@link #denominator}
	 */
	public int getDenominator() {
		return denominator;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(4);
		sb.append(numerator);
		sb.append('/');
		sb.append(denominator);
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return getNumerator() + getDenominator();
	}
	
	@Override
	public boolean equals(Object other) {
		TimeSignature ts = (TimeSignature) other;
		
		return getDenominator() == ts.getDenominator() && getNumerator() == ts.getNumerator();
	}
}

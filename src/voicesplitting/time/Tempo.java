package voicesplitting.time;

/**
 * A <code>Tempo</code> object tracks the speed of MIDI data. That is, the rate at which quarter notes occur.
 * 
 * @author Andrew McLeod - 11 Feb, 2015
 * @version 1.0
 * @since 1.0
 */
public class Tempo {

	/**
	 * The number of microseconds which pass per quarter note in the song.
	 */
	private final int microSecondsPerQuarter;
	
	/**
	 * Create a default tempo - 120 BPM
	 */
	public Tempo() {
		microSecondsPerQuarter = 500000;
	}
	
	/**
	 * Create a new Tempo from the given data array.
	 * 
	 * @param data The tempo data array, grabbed directly from a MIDI file.
	 * @see #calculateMicroSecondsPerQuarter(byte[])
	 */
	public Tempo(byte[] data) {
		microSecondsPerQuarter = calculateMicroSecondsPerQuarter(data);
	}
	
	/**
	 * Gets the number of microseconds which pass per quarter note.
	 * 
	 * @return {@link #microSecondsPerQuarter}
	 */
	public int getMicroSecondsPerQuarter() {
		return microSecondsPerQuarter;
	}
	
	/**
     * Calculate the number of microseconds per quarter note based on the given data array. It is really just an int,
     * where the highest byte is data[0], and the lowest byte is data[3].
     * 
     * @param data The Midi byte array of the tempo data for microseconds/quarter. It is actually just represented as an int,
     * but it is grabbed from the file as a byte array, so we need this conversion.
     * @return The number of microseconds per quarter note of the given data
     */
    public static int calculateMicroSecondsPerQuarter(byte[] data) {
    	int tpq = 0;
		
		for (int j = 0; j < data.length; j++) {
			int byteNumber = data.length - 1 - j;
			
			// Grab the lowest byte of the casted int, and shift it into the proper int byte location
			tpq += (0x000000ff & data[j]) << (8 * byteNumber);
		}
		
		return tpq;
	}

    /**
     * Get the String representation of this Tempo object, which is the number of quarter notes
     * per minute.
     * 
     * @return The String representation of this Tempo object.
     */
    @Override
    public String toString() {
    	int qpm = (int) (60000000. / getMicroSecondsPerQuarter());
    	StringBuilder sb = new StringBuilder(6);
    	sb.append(qpm);
    	sb.append("QPM");
    	return sb.toString();
    }
    
    /**
     * Get the hash code of this Tempo object.
     * 
     * @return The hash code of this object.
     */
    @Override
	public int hashCode() {
		return getMicroSecondsPerQuarter();
	}
	
    /**
	 * Return whether the given Object is equal to this one, which is only the case
	 * when the given Object is a Tempo, and its {@link #microSecondsPerQuarter}
	 * field is equal to this one's.
	 * 
	 * @param other The object we are checking for equality.
	 * @return True if the given Object is equal to this one. False otherwise.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tempo)) {
			return false;
		}
		
		Tempo t = (Tempo) other;
		
		return getMicroSecondsPerQuarter() == t.getMicroSecondsPerQuarter();
	}
}

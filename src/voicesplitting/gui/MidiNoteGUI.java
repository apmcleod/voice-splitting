package voicesplitting.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

import voicesplitting.gui.GUIConstants;
import voicesplitting.utils.MidiNote;

/**
 * A <code>MidiNoteGUI</code> object controls the GUI layout of a {@link MidiNote}.
 * 
 * @author Andrew McLeod - 17 June, 2015
 */
public class MidiNoteGUI extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3443961025880089435L;
	
	/**
	 * The note which this GUI object represents.
	 */
	private MidiNote note;

	/**
	 * Create a new default MidiNoteGUI based on the given note at the given scale.
	 * 
	 * @param note {@link #note}
	 * @param horizontalScale The horizontal scale of this GUI object. Note times will be divided by this
	 * amount to get layout location.
	 * @param verticalScale The vertical scale of this GUI object. Note pitches will be multiplied by this
	 * amount to get layout location.
	 */
	public MidiNoteGUI(MidiNote note, int horizontalScale, int verticalScale) {
		this.note = note;
		
		setScale(horizontalScale, verticalScale);
		setOpaque(true);
		setBackground(getColor(note.getCorrectVoice()));
		setDefaultBorder(false);
		
		addMouseListener(MidiNoteGUIMouseListener.listener);
	}
	
	/**
	 * Update the border of this note to reflect any possible changes in the underlying NoteDisplayer.
	 */
	public void updateBorder() {
		if (isHighlighted()) {
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.WHITE));
			
		} else {
			setDefaultBorder(isVoices());
		}
	}
	
	/**
	 * Return if this note is highlighted or not.
	 * 
	 * @return True if this note is currently highlighted. False otherwise.
	 */
	public boolean isHighlighted() {
		return ((NoteDisplayer) getParent()).isHighlighted(this);
	}

	/**
	 * Set whether this note is highlighted or not.
	 * 
	 * @param highlight True to highlight this note. False otherwise.
	 */
	protected void setHighlighted(boolean highlight) {
		if (highlight) {
			setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 2), getBorder()));
			
		} else {
			setDefaultBorder(isVoices());
		}
	}
	
	/**
	 * Set the default border back on this note.
	 * 
	 * @param voices Whether we are displaying guessed voices or not.
	 */
	private void setDefaultBorder(boolean voices) {
		if (!voices || note.getGuessedVoice() == -1) {
			setBorder(BorderFactory.createRaisedBevelBorder());
			
		} else {
			Color color = getColor(note.getGuessedVoice());
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, color.brighter().brighter(), color.darker().darker()));
		}
	}

	/**
	 * Rescale this MidiNoteGUI based on the given scales.
	 * 
	 * @param horizontalScale The horizontal scale of this GUI object. Note times will be divided by this
	 * amount to get layout location.
	 * @param verticalScale The vertical scale of this GUI object. Note pitches will be multiplied by this
	 * amount to get layout location.
	 */
	public void setScale(int horizontalScale, int verticalScale) {
		int width = (int) (note.getOffsetTime() - note.getOnsetTime()) / horizontalScale;
		setBounds((int) note.getOnsetTime() / horizontalScale, (127 - note.getPitch()) * verticalScale + 1, Math.max(width, 1), verticalScale - 1);
	}
	
	/**
	 * Get the note of this note gui object.
	 * 
	 * @return {@link #note}
	 */
	public MidiNote getNote() {
		return note;
	}
	
	/**
	 * Get the NoteDisplayer containing this note.
	 * 
	 * @return The NoteDisplayer containing this note.
	 */
	public NoteDisplayer getDisplayer() {
		return (NoteDisplayer) getParent();
	}
	
	/**
	 * Get whether to show the guessed voice or not.
	 * 
	 * @return True if we want to display guessed voices. False otherwise.
	 */
	public boolean isVoices() {
		return getDisplayer().isVoices();
	}
	
	/**
	 * Get the color for the given track track.
	 * 
	 * @param track The track number whose color we want.
	 * @return The correct color.
	 */
	public static Color getColor(int track) {
		return track < GUIConstants.COLORS.length ? GUIConstants.COLORS[track] : Color.BLACK;
	}
	
	/**
	 * Get the String to be used as the ToolTip text for this note.
	 * 
	 * @return The tooltip text.
	 */
	public String generateToolTipText() {
		StringBuffer sb = new StringBuffer(200).append("<html>");
		
		sb.append("<b>MIDI Note</b><br>");
		sb.append("Pitch: ").append(note.getPitch()).append("<br>");
		sb.append("Velocity: ").append(note.getVelocity()).append("<br>");
		sb.append("Onset Time: ").append(note.getOnsetTime()).append("<br>");
		sb.append("Offset Time: ").append(note.getOffsetTime()).append("<br>");
		sb.append("Onset Tick: ").append(note.getOnsetTick()).append("<br>");
		sb.append("Offset Tick: ").append(note.getOffsetTick()).append("<br>");
		sb.append("Channel: ").append(note.getCorrectVoice()).append("<br>");
		
		if (isVoices() && note.getGuessedVoice() != -1) {
			sb.append("Guessed Voice: ").append(note.getGuessedVoice()).append("<br>");
		}
		
		return sb.append("</html>").toString();
	}
}

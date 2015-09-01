package voicesplitting.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import voicesplitting.utils.Beat;
import voicesplitting.utils.MidiNote;

/**
 * A <code>NoteDisplayer</code> is the JPanel that is able to display {@link MidiNote}s
 * graphically.
 * 
 * @author Andrew McLeod - 17 June, 2015
 */
public class NoteDisplayer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8178173777771945115L;
	
	/**
	 * The column header to use for the JScrollPane, saved as a field so we can just repaint it from
	 * this class.
	 */
	private JPanel columnHeader;
	
	/**
	 * The row header to use for the JScrollPane, saved as a field so we can just repaint it from
	 * this class.
	 */
	private JPanel rowHeader;
	
	/**
	 * The horizontal scale of this GUI object. Note times will be divided by this amount to get layout
	 * location.
	 */
	private int horizontalScale;
	
	/**
	 * The vertical scale of this GUI object. Note pitches will be multiplied by this amount to get layout
	 * location.
	 */
	private int verticalScale;
	
	/**
	 * The last note offset in the currently loaded MIDI.
	 */
	private long lastOffset = 0;
	
	/**
	 * Whether this is displaying voices or not.
	 */
	private boolean voices = false;
	
	/**
	 * A Set of the currently highlighted notes.
	 */
	private Set<MidiNoteGUI> highlightedNotes;
	
	/**
	 * The most recently clicked note.
	 */
	private MidiNoteGUI lastClicked;
	
	/**
	 * A Set of the currently soloed channels.
	 */
	private Set<Integer> soloedChannels;
	
	/**
	 * Create a new default NoteDisplayer.
	 */
	public NoteDisplayer() {
		this(10000, 10);
	}
	
	/**
	 * Create a new NoteDisplayer with the given scale values.
	 * 
	 * @param horizontalScale {@link #horizontalScale}
	 * @param verticalScale {@link #verticalScale}
	 */
	public NoteDisplayer(int horizontalScale, int verticalScale) {
		this.horizontalScale = Math.max(horizontalScale, GUIConstants.HORIZONTAL_SCALE_MIN);
		this.verticalScale = Math.max(verticalScale, GUIConstants.VERTICAL_SCALE_MIN);
		
		setLayout(null);
		setBackground(Color.DARK_GRAY);
		setPreferredSize(new Dimension(getDesiredWidth(), getDesiredHeight()));
		
		columnHeader = new JPanel(null);
		rowHeader = new JPanel(null);
		
		updateColumnHeaderView();
		updateRowHeaderView();
		
		ToolTipManager.sharedInstance().setInitialDelay(200);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		
		highlightedNotes = new HashSet<MidiNoteGUI>();
		soloedChannels = new HashSet<Integer>();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					if (!e.isControlDown() && !e.isShiftDown()) {
						clearAllHighlights();
						setLastClicked(null);
					}
					
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					MidiNoteGUIPopupMenu menu = new MidiNoteGUIPopupMenu(NoteDisplayer.this);
					menu.show(NoteDisplayer.this, e.getX(), e.getY());
				}
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(Color.GRAY);
        
		for (int i = 1; i < 128; i++) {
			int y = i * verticalScale;
			g2.drawLine(0, y, getDesiredWidth(), y);
		}
		
		double tickTime = getTickTime();
		
		for (double i = tickTime; (int) ((i * 1000000) / horizontalScale) + 10 < getDesiredWidth(); i += tickTime) {
			int x = (int) ((i * 1000000) / horizontalScale);
			g2.drawLine(x, 0, x, getDesiredHeight());
		}
	}

	/**
	 * Update and repaint the {@link #columnHeader}.
	 */
	public void updateColumnHeaderView() {
		columnHeader.removeAll();
		columnHeader.setBackground(Color.BLACK);
		columnHeader.setPreferredSize(new Dimension(getDesiredWidth(), GUIConstants.SCALE_SIZE));
		
		double tickTime = getTickTime();
		
		for (double i = tickTime; (int) ((i * 1000000) / horizontalScale) + 10 < getDesiredWidth(); i += tickTime) {
			BigDecimal bd = new BigDecimal(i);
		    bd = bd.setScale(2, RoundingMode.HALF_UP);
		    
		    JLabel label = new JLabel(bd.toString());
		    label.setVerticalAlignment(SwingConstants.BOTTOM);
			label.setForeground(Color.WHITE);
			int width = label.getPreferredSize().width;
			label.setBounds((int) ((i * 1000000) / horizontalScale) - width / 2, 0, width, GUIConstants.SCALE_SIZE - 2);
			columnHeader.add(label);
		}
	}

	/**
	 * Get the time difference we should have between column ticks, in seconds. This will
	 * ensure that we have proper spacing in between ticks.
	 * 
	 * @return The time difference in seconds.
	 */
	private double getTickTime() {
		double time = 1.;
		
		while ((time * 1000000.) / horizontalScale > 100) {
			time /= 2.;
		}
		
		while ((time * 1000000.) / horizontalScale < 50) {
			time *= 2.;
		}

		return time;
	}

	/**
	 * Update and repaint the {@link #rowHeader}.
	 */
	public void updateRowHeaderView() {
		rowHeader.removeAll();
		rowHeader.setBackground(Color.BLACK);
		rowHeader.setPreferredSize(new Dimension(GUIConstants.SCALE_SIZE, getDesiredHeight()));
		
		for (int i = 5; i < 128; i+= 5) {
			JLabel label = new JLabel(((Integer) i).toString(), SwingConstants.RIGHT);
			label.setForeground(Color.WHITE);
			int height = label.getPreferredSize().height;
			label.setBounds(0, (127 - i) * verticalScale + verticalScale / 2 - height / 2 + 1, GUIConstants.SCALE_SIZE - 2, height);
			rowHeader.add(label);
		}
	}
	
	/**
	 * Change the vertical scaling if we can (ie it doesn't get too far in or out).
	 * 
	 * @param direction negative number indicates zoom out. Positive means zoom in.
	 */
	public void zoomVertical(int direction) {
		int priorLocation = ((JScrollPane) getParent().getParent()).getVerticalScrollBar().getValue();
		int priorHeight = getDesiredHeight();
		double priorPercentage = (double) priorLocation / (double) priorHeight;
		
		if (direction > 0) {
			verticalScale++;
			
		} else if (direction < 0 && verticalScale > GUIConstants.VERTICAL_SCALE_MIN) {
			verticalScale--;
			
		} else {
			// No scaling happened. We can skip the repainting.
			return;
		}
		
		updateRowHeaderView();
		refresh();
		
		int newHeight = getDesiredHeight();
		((JScrollPane) getParent().getParent()).getVerticalScrollBar().setValue((int) (priorPercentage * newHeight));
	}
	
	/**
	 * Change the horizontal scaling if we can (ie it doesn't get too far in or out).
	 * 
	 * @param direction negative number indicates zoom out. Positive means zoom in.
	 */
	public void zoomHorizontal(int direction) {
		int priorLocation = ((JScrollPane) getParent().getParent()).getHorizontalScrollBar().getValue();
		int priorWidth = getDesiredWidth();
		double priorPercentage = (double) priorLocation / (double) priorWidth;
		
		if (direction > 0 && horizontalScale > 1000) {
			horizontalScale -= 1000;
			
		} else if (direction > 0 && horizontalScale > GUIConstants.HORIZONTAL_SCALE_MIN) {
			horizontalScale /= 2;
			
		} else if (direction < 0 && horizontalScale < 1000) {
			horizontalScale *= 2;
			
		} else if (direction < 0) {
			horizontalScale += 1000;
			
		} else {
			// No scaling happened. We can skip the repainting.
			return;
		}
		
		updateColumnHeaderView();
		refresh();
		
		int newWidth = getDesiredWidth();
		((JScrollPane) getParent().getParent()).getHorizontalScrollBar().setValue((int) (priorPercentage * newWidth));
	}
	
	/**
	 * Get the column header to use for the JScrollPane.
	 * 
	 * @return {@link #columnHeader}
	 */
	public Component getColumnHeaderView() {
		return columnHeader;
	}
	
	/**
	 * Get the row header to use for the JScrollPane.
	 * 
	 * @return {@link #rowHeader}
	 */
	public Component getRowHeaderView() {
		return rowHeader;
	}
	
	/**
	 * Get the desired width of this JPanel based on the last offset and the horizontal scale.
	 * 
	 * @return The desired width of this JPanel
	 */
	public int getDesiredWidth() {
		return Math.max(1000, (int) (lastOffset / horizontalScale + 10));
	}
	
	/**
	 * Get the desired height of this JPanel based on the vertical scale.
	 * 
	 * @return The desired height of this JPanel
	 */
	public int getDesiredHeight() {
		return 128 * verticalScale;
	}
	
	/**
	 * Revalidate and redraw this JPanel, including children. This also updates the preferred size.
	 */
	private void refresh() {
		setPreferredSize(new Dimension(getDesiredWidth(), getDesiredHeight()));
		
		for (Component component : this.getComponents()) {
			if (component instanceof MidiNoteGUI) {
				((MidiNoteGUI) component).setScale(horizontalScale, verticalScale);
			}
		}
		
		columnHeader.repaint();
		rowHeader.repaint();
		revalidate();
		repaint();
	}
	
	/**
	 * Set a new vertical and horizontal scale, and refresh if thevalues changed.
	 * 
	 * @param horizontal {@link #horizontalScale}
	 * @param vertical {@link #verticalScale}
	 */
	public void setScale(int horizontal, int vertical) {
		boolean change = false;
		
		if (horizontalScale != horizontal) {
			horizontalScale = horizontal;
			change = true;
		}
		
		if (verticalScale != vertical) {
			verticalScale = vertical;
			change = true;
		}
		
		if (change) {
			refresh();
		}
	}

	/**
	 * Get the current horizontal scale.
	 * 
	 * @return {@link #horizontalScale}
	 */
	public int getHorizontalScale() {
		return horizontalScale;
	}
	
	/**
	 * Get the current vertical scale.
	 * 
	 * @return {@link #verticalScale}
	 */
	public int getVerticalScale() {
		return verticalScale;
	}

	/**
	 * Set the last offset to the given value.
	 * 
	 * @param lastOffset {@link #lastOffset}
	 */
	public void setLastOffset(long lastOffset) {
		this.lastOffset = lastOffset;
	}

	/**
	 * Get whether this is displaying voices or not.
	 * 
	 * @return {@link #voices}
	 */
	public boolean isVoices() {
		return voices;
	}

	/**
	 * Set whether this is displaying voices or not.
	 * 
	 * @param voices {@link #voices}
	 */
	public void setVoices(boolean voices) {
		this.voices = voices;
		
		for (Component component : this.getComponents()) {
			if (component instanceof MidiNoteGUI) {
				MidiNoteGUI note = (MidiNoteGUI) component;
				
				note.updateBorder();
			}
		}
	}

	/**
	 * Unhighlight all notes currently highlighted.
	 */
	public void clearAllHighlights() {
		for (MidiNoteGUI note : highlightedNotes) {
			note.setHighlighted(false);
		}
		
		highlightedNotes.clear();
	}
	
	/**
	 * Get the last clicked note in this gui.
	 * 
	 * @return {@link #lastClicked}
	 */
	public MidiNoteGUI getLastClicked() {
		return lastClicked;
	}
	
	/**
	 * Set the last clicked note to a new value.
	 * 
	 * @param note {@link #lastClicked}
	 */
	public void setLastClicked(MidiNoteGUI note) {
		lastClicked = note;
	}
	
	/**
	 * Return whether we have highlighted notes or not.
	 * 
	 * @return True if we have highlighted notes. False otherwise.
	 */
	public boolean hasHighlightedNotes() {
		return !highlightedNotes.isEmpty();
	}

	/**
	 * Toggle the highlighting of the given note.
	 * 
	 * @param note The note whose highlighting we want to toggle.
	 */
	public void toggleHighlight(MidiNoteGUI note) {
		if (highlightedNotes.remove(note)) {
			note.setHighlighted(false);
			
		} else {
			highlightedNotes.add(note);
			note.setHighlighted(true);
		}
	}

	/**
	 * Change the channel of the highlighted notes to the given value.
	 * 
	 * @param channel The channel we want to change the notes to.
	 */
	public void changeHighlightedChannels(int channel) {
		VoiceSplittingGUI gui = (VoiceSplittingGUI) SwingUtilities.getWindowAncestor(this);
		List<List<MidiNote>> gS = gui.getRunner().getGoldStandardVoices();
		while (gS.size() <= channel) {
			gS.add(new ArrayList<MidiNote>());
		}
		
		boolean visible = soloedChannels.isEmpty() || soloedChannels.contains(channel); 
		for (MidiNoteGUI noteGui : highlightedNotes) {
			MidiNote note = noteGui.getNote();
			
			gS.get(note.getChannel()).remove(note);
			
			List<MidiNote> goldVoice = gS.get(channel);
			int index = 0;
			while (index < goldVoice.size() && note.compareTo(goldVoice.get(index)) > 0) {
				index++;
			}
			goldVoice.add(index, note);
			
			note.setChannel(channel);
			
			noteGui.setBackground(MidiNoteGUI.getColor(channel));
			noteGui.updateBorder();
			noteGui.setVisible(visible);
		}
		
		for (int i = gS.size() - 1; i >= 0 && gS.get(i).isEmpty(); i--) {
			gS.remove(i);
		}
	}

	/**
	 * Highlight all notes between the given 2 notes, inclusive. That is, all notes whose onsets and
	 * pitches are between or equal to those of either of the 2 notes.
	 * 
	 * @param note1 The first boundary
	 * @param note2 The second boundary
	 */
	public void highlightAllBetween(MidiNoteGUI note1, MidiNoteGUI note2) {
		long minOnset = Math.min(note1.getNote().getOnsetTick(), note2.getNote().getOnsetTick());
		long maxOnset = Math.max(note1.getNote().getOnsetTick(), note2.getNote().getOnsetTick());
		long minPitch = Math.min(note1.getNote().getPitch(), note2.getNote().getPitch());
		long maxPitch = Math.max(note1.getNote().getPitch(), note2.getNote().getPitch());
		
		for (Component component : this.getComponents()) {
			if (component instanceof MidiNoteGUI) {
				MidiNoteGUI note = (MidiNoteGUI) component;
				
				if (!note.isHighlighted() && note.isVisible() &&
						note.getNote().getOnsetTick() >= minOnset && note.getNote().getOnsetTick() <= maxOnset &&
						note.getNote().getPitch() >= minPitch && note.getNote().getPitch() <= maxPitch) {
					
					toggleHighlight(note);
				}
			}
		}
	}
	
	/**
	 * Delete all of the currently highlighted notes.
	 */
	public void deleteHighlightedNotes() {
		VoiceSplittingGUI gui = (VoiceSplittingGUI) SwingUtilities.getWindowAncestor(this);
		
		for (MidiNoteGUI noteGui : highlightedNotes) {
			remove(noteGui);
			
			MidiNote note = noteGui.getNote();
			gui.getRunner().getNotes().remove(note);
			gui.getRunner().getGoldStandardVoices().get(note.getChannel()).remove(note);
		}
		
		highlightedNotes.clear();
		lastClicked = null;
		
		refresh();
	}
	
	/**
	 * Return if the given note is highlighted or not.
	 * 
	 * @param note The note we are checking if it is highlighted.
	 * @return True if the given note is highlighted. False otherwise.
	 */
	public boolean isHighlighted(MidiNoteGUI note) {
		return highlightedNotes.contains(note);
	}

	/**
	 * Toggle the soloing of the given channel.
	 * 
	 * @param channel The index of the channel to solo.
	 */
	public void toggleSolo(int channel) {
		if (soloedChannels.add(channel)) {
			// This channel was not yet soloed
			
			if (soloedChannels.size() == 1) {
				// This is the first soloed channel - remove all other notes
				for (Component comp : getComponents()) {
					if (comp instanceof MidiNoteGUI && ((MidiNoteGUI) comp).getNote().getChannel() != channel) {
						comp.setVisible(false);
					}
				}
				
			} else {
				// There are other channels soloed already - make this track's notes visible
				for (Component comp : getComponents()) {
					if (comp instanceof MidiNoteGUI && ((MidiNoteGUI) comp).getNote().getChannel() == channel) {
						comp.setVisible(true);
					}
				}
			}
			
		} else {
			// This channel was already soloed
			
			if (soloedChannels.size() == 1) {
				// This channel was the only one soloed - clear all
				clearAllSolos();
				
			} else {
				// There are others still soloed - hide this channel's notes
				soloedChannels.remove(channel);
				for (Component comp : getComponents()) {
					if (comp instanceof MidiNoteGUI && ((MidiNoteGUI) comp).getNote().getChannel() == channel) {
						comp.setVisible(false);
					}
				}
			}
		}
	}

	/**
	 * Clear all soloed channels. That is, make all channels visible.
	 */
	public void clearAllSolos() {
		if (!soloedChannels.isEmpty()) {
			// There were soloed tracks
			for (Component comp : getComponents()) {
				if (comp instanceof MidiNoteGUI) {
					comp.setVisible(true);
				}
			}
			
			soloedChannels.clear();
		}
	}
	
	/**
	 * Return if the given channel is soloed or not.
	 * 
	 * @param channel The channel we want to check.
	 * @return True if the channel is currently soloed. False otherwise.
	 */
	public boolean isSoloed(int channel) {
		return soloedChannels.contains(channel);
	}
	
	/**
	 * Split the selected notes. This must be run when the only curently highlighted notes are exactly 2 that overlap.
	 * The split method simply switches the onset times of the 2 notes.
	 */
	public void splitHighlightedNotes() {
		if (highlightedNotes.size() != 2) {
			return;
		}
		
		// Get the 2 notes
		MidiNote note1 = null;
		MidiNoteGUI gui1 = null;
		MidiNote note2 = null;
		MidiNoteGUI gui2 = null;
		for (MidiNoteGUI note : highlightedNotes) {
			if (note1 == null) {
				note1 = note.getNote();
				gui1 = note;
			} else {
				note2 = note.getNote();
				gui2 = note;
			}
		}
		
		if (!note1.overlaps(note2)) {
			return;
		}
		
		long offsetTime = note1.getOffsetTime();
		long offsetTick = note1.getOffsetTick();
		Beat offsetBeat = note1.getOffsetBeat();
		
		note1.setOffset(note2.getOffsetTime(), note2.getOffsetTick(), note2.getOffsetBeat());
		note2.setOffset(offsetTime, offsetTick, offsetBeat);
		
		gui1.setScale(getHorizontalScale(), getVerticalScale());
		gui2.setScale(getHorizontalScale(), getVerticalScale());
	}
	
	/**
	 * Decide if we can split the currently highlighted notes or not. We can split if and only if
	 * there are exactly 2 notes highlighted and they overlap.
	 * 
	 * @return True if we can split. False otherwise.
	 */
	public boolean canSplit() {
		if (highlightedNotes.size() != 2) {
			return false;
		}
		
		MidiNote note1 = null;
		MidiNote note2 = null;
		for (MidiNoteGUI note : highlightedNotes) {
			if (note1 == null) {
				note1 = note.getNote();
				
			} else {
				note2 = note.getNote();
			}
		}
		
		return note1.overlaps(note2);
	}
}

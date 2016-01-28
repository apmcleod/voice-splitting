package voicesplitting.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import voicesplitting.parsing.MidiWriter;

/**
 * A <code>VoiceSplittingGUIMenuBar</code> is the MenuBar for the main {@link VoiceSplittingGUI}.
 * 
 * @author Andrew McLeod - 28 July, 2015
 * @version 1.0
 * @since 1.0
 */
public class VoiceSplittingGUIMenuBar extends JMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2954414392036036144L;
	
	/**
	 * The ActionListener to export MIDI.
	 */
	private ActionListener exportListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			VoiceSplittingGUI gui = (VoiceSplittingGUI) SwingUtilities.getWindowAncestor(VoiceSplittingGUIMenuBar.this);
			final VoiceSplittingRunner runner = gui.getRunner();
			final NoteDisplayer displayer = gui.getNoteDisplayer();
			
			final JFileChooser chooser = new JFileChooser();
		    
			if (chooser.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
	    		
				gui.executeSwingWorker(new SwingWorker<Void, Void>() {
	    			
	    			@Override
	    			protected Void doInBackground() throws IOException, InvalidMidiDataException {
	    				File midiFile = chooser.getSelectedFile();
	    				MidiWriter midiWriter = new MidiWriter(midiFile, runner.getTimeTracker());
	    				
	    				for (Component component : displayer.getComponents()) {
	    					if (isCancelled()) {
	    						return null;
	    					}
	    					
	    					if (component instanceof MidiNoteGUI) {
	    						midiWriter.addMidiNote(((MidiNoteGUI) component).getNote());
	    					}
	    				}
	    				
	    				midiWriter.write();
	    				return null;
	    			}
					
					@Override
					protected void done() {}
	    		});
	    	}
		}
	};

	/**
	 * Create a new MenuBar.
	 */
	public VoiceSplittingGUIMenuBar() {
		JMenu file = new JMenu("File");
		
		JMenuItem load = new JMenuItem("Load...");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VoiceSplittingGUI gui = (VoiceSplittingGUI) SwingUtilities.getWindowAncestor(VoiceSplittingGUIMenuBar.this);
				gui.loadNewFile();
			}
		});
		file.add(load);
		
		JMenuItem export = new JMenuItem("Export MIDI...");
		export.addActionListener(exportListener);
		file.add(export);
		
		JMenuItem pref = new JMenuItem("Preferences...");
		pref.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VoiceSplittingGUI gui = (VoiceSplittingGUI) SwingUtilities.getWindowAncestor(VoiceSplittingGUIMenuBar.this);
				
				HmmVoiceSplittingModelParametersDialog dialog = new HmmVoiceSplittingModelParametersDialog(gui);
				dialog.setVisible(true);
			}
		});
		file.add(pref);
		
		add(file);
	}
}

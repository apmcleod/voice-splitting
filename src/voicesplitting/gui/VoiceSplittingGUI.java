package voicesplitting.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import voicesplitting.voice.hmm.HmmVoiceSplitter;
import voicesplitting.voice.hmm.VoiceSplittingParameters;

/**
 * The <code>BeatTrackingGUI</code> is the class which creates the gui for the beat tracking project.
 * This {@link #main(String[])} method should be run if you want to use the gui.
 * 
 * @author Andrew McLeod - 17 June, 2015
 */
public class VoiceSplittingGUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1856954392026604751L;
	
	/**
	 * Field to tell whether we are using a gui or not.
	 * This will be used to call Thread.sleep(1) to check if a Worker has been cancelled.
	 */
	public static boolean usingGui = false;

	/**
	 * @param args The command line arguments
	 */
	public static void main(String[] args) {
		usingGui = true;
		
		EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VoiceSplittingGUI().setVisible(true);
            }
        });
	}
	
	/**
	 * The label showing the currently loaded MIDI file.
	 */
	private JLabel fileNameLabel;
	
	/**
	 * The checkbox marking whether to use channels to get the correct voice (checked, default),
	 * or tracks (unchecked).
	 */
	private JCheckBox useChannelCheckBox;
	
	/**
	 * The button used to toggle voice separation.
	 */
	private JButton separateButton;
	
	/**
	 * The JPanel used to display loaded notes.
	 */
	private NoteDisplayer noteDisplayer;
	
	/**
	 * The ScrollPane which contains the {@link #noteDisplayer}.
	 */
	private JScrollPane noteScroll;
	
	/**
	 * The object used to run the non-GUI code.
	 */
	private VoiceSplittingRunner runner;
	
	/**
	 * The params to use for Voice Splitting.
	 */
	private VoiceSplittingParameters params = new VoiceSplittingParameters();
	
	/**
	 * The currently running SwingWorker.
	 */
	@SuppressWarnings("rawtypes")
	private SwingWorker currentWorker;
	
	/**
	 * Create a new default GUI.
	 */
	public VoiceSplittingGUI() {
		initComponents();
	}
	
	/**
	 * Initialize the components of the GUI.
	 */
	public void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Voice Splitting");
        
        this.setJMenuBar(new VoiceSplittingGUIMenuBar());
        
        fileNameLabel = new JLabel("No File Loaded");
        
        JButton fileLoaderButton = new JButton("Load File");
        fileLoaderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
				loadNewFile();
            }
        });
        
        useChannelCheckBox = new JCheckBox("Use Channel");
        useChannelCheckBox.setSelected(true);
        useChannelCheckBox.setToolTipText("Toggle whether to use channels (checked) or tracks (unchecked) as gold standard voices.\n" +
        									"Note that this requires manually reloading the file to update.");
        useChannelCheckBox.setBackground(Color.BLACK);
        useChannelCheckBox.setForeground(Color.WHITE);
        
        separateButton = new JButton(GUIConstants.SEPARATE);
        separateButton.setEnabled(false);
        separateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleVoiceSeparation();
			}
        });
        
        // File Chooser JPanel
        Container fileContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileContainer.setBackground(Color.BLACK);
        
        fileContainer.add(fileNameLabel);
        fileNameLabel.setForeground(Color.GRAY);
        fileContainer.add(fileLoaderButton);
        fileContainer.add(useChannelCheckBox);
        
        // Midi Note displayer
        Component noteChart = initNoteChart();
        
        // Action buttons JPanel (quantize, beat track, etc.)
        Container actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionButtons.setBackground(Color.BLACK);
        
        actionButtons.add(separateButton);
        
        // Main JPanel
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        pane.setBackground(Color.BLACK);
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(fileContainer, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(noteChart, c);
        
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 2;
        pane.add(actionButtons, c);
        
        pack();
	}
	
	/**
	 * Initialize and return the main note chart panel.
	 * 
	 * @return The main note chart Component.
	 */
	private Component initNoteChart() {
		JPanel noteChart = new JPanel(new GridBagLayout());
        noteChart.setBackground(Color.BLACK);
        
        // Horizontal zoom buttons
        JPanel horizontalZoom = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        horizontalZoom.setPreferredSize(new Dimension(GUIConstants.SCALE_SIZE + GUIConstants.ZOOM_BUTTON_SIZE * 2, GUIConstants.ZOOM_BUTTON_SIZE));
        horizontalZoom.setBackground(Color.BLACK);
        
        JButton inHorizontal = new JButton("+");
		inHorizontal.setMargin(new Insets(0, 0, 0, 0));
		inHorizontal.setBorder(null);
		inHorizontal.setToolTipText("Zoom in horizontally");
		inHorizontal.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
		inHorizontal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				noteDisplayer.zoomHorizontal(1);
			}
		});
		
		JButton outHorizontal = new JButton("-");
		outHorizontal.setMargin(new Insets(0, 0, 0, 0));
		outHorizontal.setBorder(null);
		outHorizontal.setToolTipText("Zoom out horizontally");
		outHorizontal.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
		outHorizontal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				noteDisplayer.zoomHorizontal(-1);
			}
		});
		
		horizontalZoom.add(Box.createHorizontalStrut(GUIConstants.SCALE_SIZE));
		horizontalZoom.add(outHorizontal);
		horizontalZoom.add(inHorizontal);
		horizontalZoom.add(Box.createHorizontalStrut(30));
		
		// Track solo buttons
		for (int i = 0; i < 16; i++) {
			final int track = i;
			JButton soloButton = new JButton();
			soloButton.setMargin(new Insets(0, 0, 0, 0));
			soloButton.setBorder(BorderFactory.createRaisedBevelBorder());
			soloButton.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
			soloButton.setBackground(MidiNoteGUI.getColor(i));
			soloButton.setToolTipText("Toggle solo track " + i);
			soloButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					noteDisplayer.toggleSolo(track);
					if (noteDisplayer.isSoloed(track)) {
						((JButton) e.getSource()).setBorder(BorderFactory.createLoweredBevelBorder());
						
					} else {
						((JButton) e.getSource()).setBorder(BorderFactory.createRaisedBevelBorder());
					}
				}
			});
			horizontalZoom.add(soloButton);
		}
		
		// Clear solo button
		JButton clearSolos = new JButton("X");
		clearSolos.setMargin(new Insets(0, 0, 0, 0));
		clearSolos.setBorder(BorderFactory.createRaisedBevelBorder());
		clearSolos.setForeground(Color.RED);
		clearSolos.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
		clearSolos.setToolTipText("Unsolo all tracks");
		clearSolos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Component comp : ((Component) e.getSource()).getParent().getComponents()) {
					if (comp instanceof JButton) {
						JButton button = (JButton) comp;
						if (button.getText().equals("")) {
							button.setBorder(BorderFactory.createRaisedBevelBorder());
						}
					}
				}
				noteDisplayer.clearAllSolos();
			}
		});
		horizontalZoom.add(clearSolos);
		
		// Vertical zoom buttons
		JPanel verticalZoom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		verticalZoom.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.SCALE_SIZE + GUIConstants.ZOOM_BUTTON_SIZE * 2));
        verticalZoom.setBackground(Color.BLACK);
        
        JPanel verticalZoomPadding = new JPanel();
        verticalZoomPadding.setBackground(Color.BLACK);
        verticalZoomPadding.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.SCALE_SIZE));
        
        JButton inVertical = new JButton("+");
		inVertical.setBorder(null);
		inVertical.setToolTipText("Zoom in vertically");
		inVertical.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
		inVertical.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				noteDisplayer.zoomVertical(1);
			}
		});
		
		JButton outVertical = new JButton("-");
		outVertical.setBorder(null);
		outVertical.setToolTipText("Zoom out vertically");
		outVertical.setPreferredSize(new Dimension(GUIConstants.ZOOM_BUTTON_SIZE, GUIConstants.ZOOM_BUTTON_SIZE));
		outVertical.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				noteDisplayer.zoomVertical(-1);
			}
		});
		
		verticalZoom.add(verticalZoomPadding);
		verticalZoom.add(outVertical);
		verticalZoom.add(inVertical);
        
        // Chart scrolling area
        noteScroll = new JScrollPane();
        noteDisplayer = new NoteDisplayer();
        noteScroll.setViewportView(noteDisplayer);
        noteScroll.setColumnHeaderView(noteDisplayer.getColumnHeaderView());
        noteScroll.setRowHeaderView(noteDisplayer.getRowHeaderView());
        noteScroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, getCornerView());
        noteScroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, getCornerView());
        noteScroll.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, getCornerView());
        noteScroll.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, getCornerView());
        noteScroll.getVerticalScrollBar().setUnitIncrement(16);
        noteScroll.getHorizontalScrollBar().setUnitIncrement(16);
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 1;
        c.gridy = 0;
        noteChart.add(horizontalZoom, c);
        
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 1;
        noteChart.add(verticalZoom, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        noteChart.add(noteScroll, c);
        
        return noteChart;
	}
	
	/**
	 * Get the Component that is to make up the corners of the noteScroll.
	 * 
	 * @return Th Component for the noteScroll corners.
	 */
	public Component getCornerView() {
		JLabel label = new JLabel();
		label.setBackground(Color.BLACK);
		label.setOpaque(true);
		return label;
	}
	
	/**
	 * Choose and load a new MIDI File.
	 * 
	 * @throws IOException If some I/O error occurred when reading the file.
	 * @throws InvalidMidiDataException If the file contained some invlaid MIDI data.
	 */
	public void loadNewFile() {
		JFileChooser chooser = new JFileChooser();
		
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("MIDI Files", "mid", "midi");
	    chooser.setFileFilter(filter);
	    
	    if (runner != null) {
	    	chooser.setCurrentDirectory(runner.getFile());
	    }
	    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	final File midiFile = chooser.getSelectedFile();
	    	
	    	executeSwingWorker(new SwingWorker<VoiceSplittingRunner, Void>() {
	    			
    			@Override
    			protected VoiceSplittingRunner doInBackground() throws IOException, InvalidMidiDataException {
    				try {
						return new VoiceSplittingRunner(midiFile, shouldUseChannel());
					} catch (InterruptedException e) {
						return null;
					}
    			}
				
				@Override
				protected void done() {
					if (isCancelled()) {
						updateActionButtons();
						return;
					}
					
					try {
						runner = get();
						
					} catch (InterruptedException e) {
						displayErrorDialog(e);
						return;
						
					} catch (ExecutionException e) {
						displayErrorDialog(e);
						return;
					}
					
					fileNameLabel.setText(runner.getFile().getName());
					fileNameLabel.setForeground(Color.WHITE);
					separateButton.setEnabled(false);
					
					NoteDisplayerWorker ndw = new NoteDisplayerWorker(noteScroll, VoiceSplittingGUI.this, runner.getNotes());
					executeSwingWorker(ndw);
				}
    		});
	    }
	}
	
	/**
	 * Toggle showing voice separated notes in the currently loaded file.
	 */
	private void toggleVoiceSeparation() {
		separateButton.setEnabled(false);
		
		if (separateButton.getText().equals(GUIConstants.SEPARATE)) {
			separateButton.setText("Separating...");
			
			executeSwingWorker(new SwingWorker<Double, Void>() {

				@Override
				protected Double doInBackground() {
					HmmVoiceSplitter vs = new HmmVoiceSplitter(runner.getNotes(), params);
					
					try {
						vs.getVoices();
						return vs.getF1(runner.getGoldStandardVoices());
					} catch (InterruptedException e) {
						return null;
					}
				}
				
				@Override
				protected void done() {
					if (isCancelled()) {
						updateActionButtons();
						return;
					}
					
					double f1 = 0;
					
					try {
						f1 = get();
						
					} catch (InterruptedException e) {
						displayErrorDialog(e);
						return;
						
					} catch (ExecutionException e) {
						displayErrorDialog(e);
						return;
					}
					
					noteDisplayer.setVoices(true);
					updateActionButtons();
					JOptionPane.showMessageDialog(VoiceSplittingGUI.this, "F1 = " + f1, "Voice Splitting Results", JOptionPane.INFORMATION_MESSAGE);
				}
				
			});
			
		} else {
			
			// Unseparate
			separateButton.setText("Unseparating...");
			noteDisplayer.setVoices(false);
			updateActionButtons();
		}
	}
	
	/**
	 * Display an error popup box based on the given exception.
	 * 
	 * @param e The Exception that occurred.
	 */
	public void displayErrorDialog(Exception e) {
		JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error - " + e.getClass(), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Set the note displayer for this gui object.
	 * 
	 * @param nd {@link #noteDisplayer}
	 */
	public void setNoteDisplayer(NoteDisplayer nd) {
		noteDisplayer = nd;
	}

	/**
	 * Get the current NoteDisplayer
	 * 
	 * @return {@link #noteDisplayer}
	 */
	public NoteDisplayer getNoteDisplayer() {
		return noteDisplayer;
	}
	
	/**
	 * Get the current BeatTrackingRunner.
	 * 
	 * @return {@link #runner}
	 */
	public VoiceSplittingRunner getRunner() {
		return runner;
	}
	
	/**
	 * Get the currently set VoiceSplittingParameters.
	 * 
	 * @return {@link #params}
	 */
	public VoiceSplittingParameters getParams() {
		return params;
	}
	
	/**
	 * Set the VoiceSplittingParameters to this new value.
	 * 
	 * @param params {@link #params}
	 */
	public void setParams(VoiceSplittingParameters params) {
		this.params = params;
	}
	
	/**
	 * Get whether we are supposed to be reading in data with the correct voice as channels or tracks.
	 * 
	 * @return True if we should use channels. False to use tracks.
	 */
	protected boolean shouldUseChannel() {
		return useChannelCheckBox.isSelected();
	}
	
	/**
	 * Execute the given SwingWorker. This includes cancelling the {@link #currentWorker},
	 * if it is still running.
	 * 
	 * @param swingWorker The SwingWorker we want to execute.
	 */
	@SuppressWarnings("rawtypes")
	public void executeSwingWorker(SwingWorker swingWorker) {
		if (currentWorker != null && !currentWorker.isDone()) {
			currentWorker.cancel(true);
		}
		
		currentWorker = swingWorker;
		currentWorker.execute();
	}

	/**
	 * Update the enabled and text fields of the action buttons.
	 */
	public void updateActionButtons() {
		if (runner == null) {
			// No file loaded yet
			separateButton.setText(GUIConstants.SEPARATE);
			separateButton.setEnabled(false);
			
		} else {
			separateButton.setEnabled(true);
			separateButton.setText(noteDisplayer.isVoices() ? GUIConstants.UNSEPARATE : GUIConstants.SEPARATE);
		}
	}
}

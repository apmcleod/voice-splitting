package voicesplitting.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import voicesplitting.voice.hmm.HmmVoiceSplittingModelParameters;

/**
 * A <code>VoiceSplittingParametersDialog</code> object creates and displays the gui dialog
 * which can be used to change the currently loaded {@link HmmVoiceSplittingModelParameters}.
 * 
 * @author Andrew McLeod - 4 August, 2015
 */
public class HmmVoiceSplittingModelParametersDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8034043349191756712L;
	
	private static final String BEAM_SIZE = "Beam size";
	private static final String NEW_VOICE_PROBABILITY = "New voice prob";
	private static final String PITCH_HISTORY_LENGTH = "Pitch history length";
	private static final String GAP_STD_MICROS = "Gap std micros";
	private static final String PITCH_STD = "Pitch std";
	private static final String MIN_GAP_SCORE = "Min gap score";
	
	/**
	 * The map to keep track of the settings JTextFields.
	 */
	private Map<String, JTextField> textFieldMap;

	/**
	 * The parameters that were set initially.
	 */
	private HmmVoiceSplittingModelParameters params;
	
	/**
	 * The gui frame of this instance.
	 */
	private VoiceSplittingGUI gui;
	
	/**
	 * Create a new dialog based on the given gui frame.
	 * 
	 * @param gui {@link #gui}
	 */
	public HmmVoiceSplittingModelParametersDialog(VoiceSplittingGUI gui) {
		super(gui, "Voice Splitting Parameters");
		params = gui.getParams();
		this.gui = gui;
		
		textFieldMap = new HashMap<String, JTextField>(6);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		mainPanel.add(Box.createVerticalStrut(10));
		
		mainPanel.add(getSetting(BEAM_SIZE, params.BEAM_SIZE));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(getSetting(NEW_VOICE_PROBABILITY, params.NEW_VOICE_PROBABILITY));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(getSetting(PITCH_HISTORY_LENGTH, params.PITCH_HISTORY_LENGTH));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(getSetting(GAP_STD_MICROS, params.GAP_STD_MICROS));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(getSetting(PITCH_STD, params.PITCH_STD));
		mainPanel.add(Box.createVerticalGlue());
		mainPanel.add(getSetting(MIN_GAP_SCORE, params.MIN_GAP_SCORE));
		
		mainPanel.add(Box.createVerticalStrut(10));
		
		mainPanel.add(getButtons());
		
		add(mainPanel);
		
		pack();
		setLocationRelativeTo(gui);
	}

	/**
	 * Create the buttons for using this dialog.
	 * 
	 * @return The Component containing the Buttons for this dialog.
	 */
	private Component getButtons() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		
		JButton defaults = new JButton("Defaults");
		defaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HmmVoiceSplittingModelParameters def = new HmmVoiceSplittingModelParameters();
				textFieldMap.get(BEAM_SIZE).setText("" + def.BEAM_SIZE);
				textFieldMap.get(NEW_VOICE_PROBABILITY).setText("" + def.NEW_VOICE_PROBABILITY);
				textFieldMap.get(PITCH_HISTORY_LENGTH).setText("" + def.PITCH_HISTORY_LENGTH);
				textFieldMap.get(GAP_STD_MICROS).setText("" + def.GAP_STD_MICROS);
				textFieldMap.get(PITCH_STD).setText("" + def.PITCH_STD);
				textFieldMap.get(MIN_GAP_SCORE).setText("" + def.MIN_GAP_SCORE);
			}
		});
		panel.add(defaults);
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HmmVoiceSplittingModelParametersDialog.this.dispose();
			}
		});
		panel.add(cancel);
		
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HmmVoiceSplittingModelParameters newParams = HmmVoiceSplittingModelParametersDialog.this.loadDisplayedParameters();
				if (newParams != null) {
					if (!params.equals(newParams)) {
						gui.setParams(newParams);
					}
					HmmVoiceSplittingModelParametersDialog.this.dispose();
				}
			}
		});
		panel.add(ok);
		
		return panel;
	}

	/**
	 * Get the parameters currently displayed in the dialog.
	 * 
	 * @return The parameters, loaded from the text areas.
	 */
	protected HmmVoiceSplittingModelParameters loadDisplayedParameters() {
		try {
			return new HmmVoiceSplittingModelParameters(
					Integer.parseInt(textFieldMap.get(BEAM_SIZE).getText()),
					Double.parseDouble(textFieldMap.get(NEW_VOICE_PROBABILITY).getText()),
					Integer.parseInt(textFieldMap.get(PITCH_HISTORY_LENGTH).getText()),
					Double.parseDouble(textFieldMap.get(GAP_STD_MICROS).getText()),
					Double.parseDouble(textFieldMap.get(PITCH_STD).getText()),
					Double.parseDouble(textFieldMap.get(MIN_GAP_SCORE).getText()));
			
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Number format exception", "Parameter Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Get the Setting component for an integer setting with the given key and values.
	 * 
	 * @param key The key for this setting, to be displayed as the label for the textbox.
	 * @param value The current value for this setting.
	 * @return The Component which will be used for the given setting.
	 */
	private Component getSetting(String key, int value) {
		return getSetting(key, "" + value, true);
	}

	/**
	 * Get the Setting component for a double setting with the given key and values.
	 * 
	 * @param key The key for this setting, to be displayed as the label for the textbox.
	 * @param value The current value for this setting.
	 * @return The Component which will be used for the given setting.
	 */
	private Component getSetting(String key, double value) {
		return getSetting(key, "" + value, false);
	}
	
	/**
	 * Get the Setting component for a setting with the given key and values.
	 * 
	 * @param key The key for this setting, to be displayed as the label for the textbox.
	 * @param value The current value for this setting.
	 * @param isInt True if the value is to be an integer. False otherwise.
	 * @return The Component which will be used for the given setting.
	 */
	private Component getSetting(String key, String value, final boolean isInt) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
		
		JLabel label = new JLabel(key);
		label.setPreferredSize(new Dimension(150, 20));
		panel.add(label);

		JTextField textField = new JTextField(value, 25);
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {}
			
			@Override
			public void focusLost(FocusEvent e) {
				JTextField textField = (JTextField) e.getSource();
				
				try {
					if (isInt) {
						Integer.parseInt(textField.getText());
						textField.setBackground(Color.WHITE);
						
					} else {
						Double.parseDouble(textField.getText());
						textField.setBackground(Color.WHITE);
					}
					
				} catch (NumberFormatException ex) {
					textField.setBackground(Color.RED);
				}
			}
		});
		panel.add(textField);
		
		textFieldMap.put(key, textField);
		
		return panel;
	}
}

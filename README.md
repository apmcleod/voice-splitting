# Voice Splitting
The goal of this project is to have a Java program which is able to split MIDI performance
data into monophonic voices.

## Usage
You are free to use this software as is allowed by the [MIT License](https://www.github.com/apmcleod/VoiceSplitting/License).
I only ask that you please cite it as my work where appropriate.

## Documentation
This document contains some basic code examples and a general overview of how to use
the classes in this project. All specific documentation for the code found in this
project can be found in the [Javadocs](https://apmcleod.github.io/voice-splitting/doc). 

## Installing
The java files can all be compiled into class files in a bin directory using the Makefile
included with the project with the following command: `make`.

## Running
Once the class files are installed in the bin directory, the project can be run. To run the
GUI version of the project, use the command `java -cp bin voicesplitting.gui.VoiceSplittingGUI`.
To run the project from the command line, use the command `$ java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester`.
You should get an argument error. The proper arguments for running the VoiceSplittingTester are as follows:

`java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester ARGS Files`

ARGS:
 * Running arguments:
   * `-t [STEPS]` = Tune, and optionally set the number of steps to make within each parameter
     range to an Integer value (default = 5).
   * `-r` = Run a test (if used with -t, we will use the tuned parameters instead of any given).
   * `-v` = Verbose (print out each song and each individual voice when running).
   * `-T` = Use tracks as correct voice (instead of channels). See [Troubleshooting](#troubleshooting)
     for more information.
 * Parameter arguments (used only if `-r` is also used):
   * `-b INT` = Set the Beam Size parameter to the value INT (defualt = 2).
   * `-n DOUBLE` = Set the New Voice Probability parameter to the value DOUBLE(defualt = 1.0E-8).
   * `-h INT` = Set the Pitch History Length parameter to the value INT(defualt = 6).
   * `-g DOUBLE` = Set the Gap Std Micros parameter to the value DOUBLE(defualt = 100000.0).
   * `-p DOUBLE` = Set the Pitch Std parameter to the value DOUBLE(defualt = 4.0).
   * `-m DOUBLE` = Set the Min Gap Score parameter to the value DOUBLE(defualt = 5.1E-5).
 
Files should be a list of 1 or more MIDI files or directories containing only MIDI
files. Any directory entered will be searched recursively for files.

### Examples
To tune on the files in the directory "midi" with a step of 7 and print verbose results:
 * `$ java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester -t 7 -v midi`

To test on the files in the directory "midi" using all default values except a beam size of 25:
 * `$ java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester -r -b 25 midi`

To train and test on the files in the directory "midi" using a step of 10 in training:
 * `$ java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester -t 10 -r midi`


### Troubleshooting
Most Exceptions that occur while running this program should print a useful error message out to
stderr. In general, there are 2 main types of errors:
 * I/O Errors: These indicate that some I/O error occured while trying to read a MIDI file. These
   could indicate that the program does not have read permission on the given file, or that the
   file does not exist. Check for typos in the file names.
 * MIDI errors: These indicate that the MIDI files given are not in a format that can be read
   by Java. There is currently no way to solve this issue besides looking for new MIDI files.

If the results you are getting seem very low, try using the `-T` flag if running from the command line,
or unchecking the `Use Channel` box if using the GUI. Some MIDI files divide notes into voices
by track while others do it by channel. MIDI files created by this software does both.

## Contact
Please let me know if you are interested in using my work. If you run into any problems installing it,
using it, extending it, or you'd like to see me add any additional features, please let me know either by
email or by submitting an issue on github. Any and all questions are always welcome.

Thanks, and enjoy!
Andrew McLeod
A.McLeod-5@sms.ed.ac.uk
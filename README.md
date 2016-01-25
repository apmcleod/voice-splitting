# Voice Splitting
The goal of this project is to have a Java program which is able to split MIDI performance
data into monophonic voices.

## License
You are free to use this software as is allowed by the [MIT License](https://github.com/apmcleod/voice-splitting/blob/master/License).
I only ask that you please cite it as my work where appropriate, including
the paper on my [website](http://homepages.inf.ed.ac.uk/s1331854/software.html#VoiceSeparation)
which has been accepted to be published in the Journal of New Music Research.

## Documentation
This document contains some basic code examples and a general overview of how to use
the classes in this project. All specific documentation for the code found in this
project can be found in the [Javadocs](https://apmcleod.github.io/voice-splitting/doc). 

## Installing
The java files can all be compiled into class files in a bin directory using the Makefile
included with the project with the following command: `make`.

## Running
Once the class files are installed in the bin directory, the project can be run, either using the
[GUI](#gui) or from the [command line](#command-line).

### GUI
To run the GUI version of the project, use the command `java -cp bin voicesplitting.gui.VoiceSplittingGUI`.

### Command Line
#### Basics
To run the project from the command line, use the command

`java -cp bin voicesplitting.voice.hmm.HmmVoiceSplittingModelTester ARGS Files`

Here, `Files` should be a list of 1 or more MIDI files or directories containing only MIDI
files. Any directories listed will be searched recursively for all files, and if any of the files
found are not in MIDI format, an exception will be thrown.

ARGS:
 * Running arguments:
   * `-t [STEPS]` = Tune, and optionally set the number of steps to make within each parameter
     range to an Integer value (default = 5).
   * `-r` = Run a test (if used with -t, we will use the tuned parameters instead of any given).
   * `-v` = Verbose (print out each song and each individual voice when running).
   * `-T` = Use tracks as correct voice (instead of channels). See [Troubleshooting](#troubleshooting)
     for more information.
     
If running with `-r`, the following arguments can be used to change the parameter settings from their default
values (those with which we tested the computer generated WTC fugues in the paper):
   * `-b INT` = Set the Beam Size parameter to the value INT (defualt = 25).
   * `-n DOUBLE` = Set the New Voice Probability parameter to the value DOUBLE (defualt = 5E-10).
   * `-h INT` = Set the Pitch History Length parameter to the value INT (defualt = 11).
   * `-g DOUBLE` = Set the Gap Std Micros parameter to the value DOUBLE (defualt = 224000).
   * `-p DOUBLE` = Set the Pitch Std parameter to the value DOUBLE (defualt = 4).
   * `-m DOUBLE` = Set the Min Gap Score parameter to the value DOUBLE (defualt = 7E-5).

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

There is a paper which has been accepted to be published in the Journal of New Music Research available on
my [website](http://homepages.inf.ed.ac.uk/s1331854/software.html#VoiceSeparation) with further documentation.
Please cite this if you use my code or the paper.

Thanks, and enjoy!  
Andrew McLeod  
A.McLeod-5@sms.ed.ac.uk
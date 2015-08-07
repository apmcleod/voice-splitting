# Voice Splitting

## Project Overview
The goal of this project is to have a Java program which is able to split MIDI performance
data into monophonic voices.

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
To run the project from the command line, use the command `$ java -cp bin voicesplitting.voice.hmm.VoiceSplittingTester`.
You should get an argument error. The proper arguments for running the VoiceSplittingTester are as follows:

`java -cp bin voicesplitting.voice.hmm.VoiceSplittingTester ARGS Files`

ARGS:
 * Running arguments:
   * `-t [STEPS]` = Tune, and optionally set the number of steps to make within each parameter
     range to an Integer value (default = 5).
   * `-r` = Run a test (if used with -t, we will use the tuned parameters instead of any given).
   * `-v` = Verbose (print out each song and each individual voice when running).
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
 * `$ java beattracking/voice/hmm/VoiceSplittingTester -t 7 -v midi`

To test on the files in the directory "midi" using all default values except a beam size of 25:
 * `$ java beattracking/voice/hmm/VoiceSplittingTester -r -b 25 midi`

To train and test on the files in the directory "midi" using a step of 10 in training:
 * `$ java beattracking/voice/hmm/VoiceSplittingTester -t 10 -r midi`


### Troubleshooting
Most Exceptions that occur while running this program should print a useful error message out to
stderr. In general, there are 2 main types of errors:
 * I/O Errors: These indicate that some I/O error occured while trying to read either a config
   file or a MIDI file. These could indicate that the program does not have read permission
   on the given file, or that the file does not exist. Check for typos in the file names.
 * MIDI errors: These indicate that the MIDI files given are not in a format that can be read
   by Java. There is currently no way to solve this issue besides looking for new MIDI files.

## Module Overviews
The following sections contain detailed information about each of the modules of this project
separately. 

### MIDI Parsing
MIDI files are made of events, which each occurs on a tick. These events are all parsed by an
`EventParser` from the package `voicesplitting.parsing`. The events that we use can be organized
into two different types: [Time Events](#time-events) and [Note Events](#note-events). To use
an `EventParser` to (for example) get a list of the notes in a given MIDI file midi, do the
following:

```
TimeTracker tt = new TimeTracker();
NoteListGenerator nlg = new NoteListGenerator(tt);
EventParser ep = new EventParser(midi, nlg, tt);
ep.run();
List<MidiNote> noteList = nlg.getNoteList());
```

An `EventParser` can also be used to play a MIDI file, and write out to a new file.

#### Time Events
Time events are all handled by classes in the `beattracking.time` class using the interface
class `TimeTracker`. There are three types of events:

1. Key singature changes: These are handled by the `KeySignature` class, and are added to a
   `TimeTracker tt` with the code `tt.addKeySignatureChange(event, mm)`.
2. Tempo changes: These are handled by the `Tempo` class, and are added to a `TimeTracker tt`
   with the code `tt.addTempoChange(event, mm)`.
3. Time signature changes: These are handled by the `TimeSignature` class, and are added to a
   `TimeTracker tt` with the code `tt.addTimeSignatureChange(event, mm)`.

Once all of these events have been parsed by a `TimeTracker`, it is used mainly to convert
between ticks and times (measured in microseconds) by using the methods `tt.getTimeAtTick(tick)`
and `tt.getTickAtTime(time)`.

#### Note Events
Note events are all handled by classes implementing the `NoteEventParser` interface from the
package `voicesplitting.parsing`. There are two types of note events:

1. Note on events: These are handled by the `noteOn` method, and contain information about the
   key, velocity, tick, and track.
2. Note off events: These are handled by the `noteOff` method, and contain information about the
   key, tick, and track.

### Voice Separation
All voice separation code is found in the `voicesplitting.voice` package, and it can be performed
by any class implementing the `VoiceSplitter` interface there, which only necessitates that it
contains a method `getVoices()` which returns a `List` of some objects implementing the `Voice`
abstract class. The standard voice separation is done as follows after creating a `List` of
`MidiNotes` as shown above in [MIDI Parsing](#midi-parsing):

```
VoiceSplitter vs = new HmmVoiceSplitter(noteList, new VoiceSplittingParameters());
List<Voice> = vs.getVoices();
```

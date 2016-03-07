# run-tandem

Simple utility to run X! Tandem.

## Usage

Simple utility for running X! Tandem on the command line.  Takes a
text file containing paths to spectra files to be used in the searches
(one per line; -i), a FASTA database (-f) and a text file containing
X! Tandem parameters one per line (-p; for example, "spectrum, parent
monoisotopic mass error units=ppm"). Default parameters assume that
the TPP X! Tandem is being used, i.e. k-score scoring, but this can be
changed using the parameter file. Default parameters can be printed
using the '-d' flag. Expects to find a 'tandem' executable in the PATH
variable. Output file is the name of the spectra file with
'.tandem.xml' replacing the extension.

Run using the jar:

    $ java -jar run-tandem-0.1.0-standalone.jar [args]

Or the BASH script in the bin directory:

    $ run-tandem.sh [args]

## Options

```
  -l, --files PATH   Path to file containing paths of mzML or mgf files to be searched.
  -f, --fasta PATH   Path to fasta file to be used in searches.
  -p, --params PATH  OPTIONAL: Path to file containing X! Tandem parameters.
  -d, --defaults     Print default parameters used in searches.
  -h, --help         Print help message.
```

## License

Copyright © 2016 Jason Mulvenna

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

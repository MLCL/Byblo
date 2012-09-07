# Byblo

Byblo is a software package for the construction of large-scale *distributional thesauri*. It provides an efficient yet flexible framework for calculating all pair-wise similarities between terms in a corpus.

## Distribution Thesauri Overview 

### Informally

Naively, a distributional thesaurus can be thought of much like a tradition thesaurus. It allows one to look up a word, returning a list of synonyms. Unlike traditional thesauri, however, the synonyms are not manually curated by humans, they are calculated using a statistical model, estimated from a text corpus. Notionally the similarity between two terms might be calculated from the intersection of the features of those terms, extracted from a large corpus of text. For example if the phrases "christmas holiday" and "xmas holiday" occur very frequently in the corpus, the model might indicate that *christmas* and *xmas* are similar. Here we have decided that co-occurrent words are features, both *christmas* and *xmas* share the feature *holiday*, so they are similar. 

### Less Informally

Unfortunately, a distribution thesaurus is actually not at all like a manually curated one. Fundamentally we are defining a notion of similarity that is wholly different from synonymy. Worse still is that it changes depending on the corpus, feature selection and similarity measure. The previous paragraph is there to give a ludicrously high level overview of the projects purpose, to those who are only mildly curious. For those who are intent on using the software, let me start again:

A distributional thesaurus is a resource that contains the estimated *substitutability* of *entries*. Typically these entries are terms or phrases. The thesaurus can be queried with an entry (the *base entry*) and returns a list of other entries (the base entry's *neighbours*). These neighbours can be used as a part of an external text-processing pipeline, such as to expand queries during information retrieval. The entries are extracted from a text corpus, along with features of each entry. The similarity of entries is calculated as a function of their features. Underpinning this process is Harris' *Distribution Hypothesis*: "a word is characterised by the company it keeps". As mentioned above, it is hard to pin down the precise notion of similarity used because there are so many variables. 

### Example Build Process

To provide an intuition, here is an example:

Take as our input corpus a balanced collection of English language text (such as Wikipedia). Let our entries be all unique terms in the corpus. We shall select the features of a base-entry as the frequency of all terms that co-occur in the corpus within a window of A +/- 1 terms. Finally, the similarity function will be Cosine, which represents the feature sets as high dimensional vectors. Cosine calculates the similarity as being inversely proportional to the degree of orthogonality of vectors. Instructions for the thesaurus build process would proceeds as follows:

 1. Tokenise the corpus, extracting a list of all unique terms. For each entry record occurrences of all the other entries within a window of +/- 1. For example, if we encounter the string "the big red bus", the entry-features produced are the:big, big:the, big:red, red:big, red:bus, and bus:red. Note that this set of the process is not (yet) covered by the provided software.

 2. Count the occurrences of each unique feature with each unique entry. So we may find that the string "red bus" occurs 100 times in the corpus, and that "bus red" occurs 2 times. In this case we produce the features bus:red:102 and red:bus:102 (remember the feature windows is 1 term before and after). In words, we are saying that "red" occurs as a feature of "bus" 102 times, and "bus" occurs as a feature of "red" 102 times. For each entry, construct a multi-set of all the features it occurs with. So "bus" may occur with "green" 23 times, and with "big" 376 times, etc...

 3. Convert the feature multi-set for each entry to a vector, where each features is a dimension, and the magnitude along a dimension is the frequency.

 4. For all pairs of entries calculate their similarity as the cosine of the angle between their feature vectors.

 5. For each base entry, select as its neighbours the top k highest similarity entries.

The resultant thesaurus will have a highly semantic notion of similarity. The neighbours of a word are likely to be strongly related in terms of meaning and topic, but not at all in terms of syntax. So for example, the top neighbours of *happy* could be *pleased*, *impressed*, *satisfied*, *surprised*, *disappointed*, *thrilled*, *upset*, and *glad*. Notice that while some words - such as the adjectival sense of *pleased* - are good synonyms, there are some antonymic words such as *disappointed*.

## Distribution 

The software is primarily distributed in source-code form. Binaries may be available sporadically, and on request. 
The source code can be acquired from the [github repository](https://github.com/MLCL/Byblo), click the *Downloads* button, and select a version to download an archive of the source code.

## Dependencies

The project requires [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on the system. It also requires a unix command-line such as Linux or Mac OS X. Windows users may get it to work through [Cygwin](http://www.cygwin.com). The software is compiled using an [Apache Maven](http://maven.apache.org) script. In addition the following Java libraries are required:

 * [Google Guava 12.0](http://code.google.com/p/guava-libraries/) -- A library containing numerous useful features and tools; mostly the kind of things that should have been included with Java in the first place.

 * [JCommander 1.25](http://github.com/cbeust/jcommander) -- A framework for parsing command line arguments in a very elegant way.

 * [Fastutil 6.4.4](http://fastutil.dsi.unimi.it/) -- A library for collections that handle primitive data types efficiently. It also includes improved implementations of most of the standard collection framework.

 * [Commons Logging 1.1.1](http://commons.apache.org/logging/) -- A very light weight wrapper API that enables logging frameworks to be configured and "plugged in" at runtime. 

 * [MLCL Lib 0.2.1](https://github.com/MLCL/MLCLLib) -- A collection of generic Java utilities and classes developed by the authors of Byblo, for use in this and other projects.

 * [JDBM3 3.0-alpha3](https://github.com/jankotek/JDBM3) -- Embedded Key Value Java Database. Note that at time of writing there is no stable release,
but the dependancy will be updated as soon as there is. The jar can be downloaded from [http://kungf.eu:8081/nexus/service/local/repositories/releases/content/uk/ac/susx/mlcl/jdbm/3.0-alpha3/jdbm-3.0-alpha3.jar](http://kungf.eu:8081/nexus/service/local/repositories/releases/content/uk/ac/susx/mlcl/jdbm/3.0-alpha3/jdbm-3.0-alpha3.jar)

The following additional dependancies are optional:

 * [JUnit 4.10](http://www.junit.org/) -- Required for unit testing the project.

 * [Log4J 1.2.16](http://logging.apache.org/log4j/1.2/) -- Can be used as a replacement for JDK 1.4 Logging, at the users discretion. 


## Building

Compiling the software from a source distribution

### From the command line

First source distribution. To compile the software from the command line:

```sh
$ cd path/to/byblo/source/distribution
$ mvn -P release package -DskipTests -Dgpg.skip
```

This will download all dependencies, compile the source code, and create a new directory `/target/` containing the project `jar` archive, and the various assemblies. (See Build Output bellow) The command will also install the generated artefacts in your local maven repository.

### Building with Netbeans IDE.

This section details how build the project from with Netbeans 7. First acquire the source code as described above. 

1. Start Netbeans and select "File -> Open Project" from the menu bar.

2. Browse the directory to which you downloaded mlcl-lib. Select the directory and click "Open Project".

3. From the Run menu select "Clean and Build Main Project".

This will compile the source code, and create a new directory `/target/` containing the project `jar` file, and various assemblies. (See Build Output bellow)


### Build Output:

All build output is stored in the newly created directory `/target/`. It will
contain a number of sub-directories along with containing the compiled `jar` archive, and various assemblies:

```
...
Byblo-<version>.jar
Byblo-<version>-src.zip
Byblo-<version>-bin.zip
Byblo-<version>-bin-with-deps.zip
...
```

The ```jar``` file is just the compiled source code. The ```-bin.zip``` and ```-src.zip``` are binary and source distributions respectively. The binary distribution contains the ```jar``` archive, along with a copy of the README. The source distribution should be an exact replica of the distribution you just downloaded. The ```-bin-with-deps.zip``` is probably the file you want; it contains the jar, with all dependancies, and associated scripts. 

Extract ```Byblo-<version>-bin-with-deps.zip``` somewhere are run from there.

## Usage 

This `byblo.sh` script is designed to be the primary point of usage for the thesaurus building software. It runs a complete build process from frequency counting to K-Nearest-Neighbours in a single pass, providing all the
most commonly used functionality of the underlying software.

```sh
$ ./byblo.sh [<options>] [@<config>] -i <file>
```

Where the arguments are:

 * -i `<file>` Input instances file containing entry/feature pairs.

 * `@<config>` Options and input files can be read from a <config> file specified directly after an '$\mathtt{@}$' character. Options in this file should be specified exactly as they would be at the command line, and may contain additional `@` references to other config files. 

 * `<options>` Any number of the option switches

There are a large number of options. To view a complete list enter ```./byblo.sh --help``` or view the wiki page on [Running the Sotware](https://github.com/MLCL/Byblo/wiki/Running-the-Software)

## Attribution 

This project is supported a TSB (Technology Strategy Board) grant reference GCL-100934, and by the [EPSRC Doctoral Training Account Scheme](http://www.epsrc.ac.uk/funding/students/dta).

Special thanks to all members of the Machine Learning and Computational Linguistics Lab, Department of Informatics, University of Sussex, for all the helpful input.

## Contributing

In the event that you discover a bug whiles using Byblo, please submit a detailed report on our issue tracker: [https://github.com/MLCL/Byblo/issues](https://github.com/MLCL/Byblo/issues)

To contribute to the project you should fork the git repository. First click the "Fork" button on github. Then open a console and type the following:

```sh
$ git clone git@github.com:[your-user-name]/Byblo.git
$ cd Byblo
$ git remote add upsteam git@github.com:MLCL/Byblo.git
$ git fetch upstream
```

If you have changes to contribute back to the main project, send me a pull request by clicking the "Pull Request" button in your fork of the repository. For a detailed description click [here](http://help.github.com/send-pull-requests/).

## License

This software is distributed under the 3-clause [BSD License](https://github.com/MLCL/Byblo/wiki/License).

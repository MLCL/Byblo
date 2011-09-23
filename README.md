# Byblo

Byblo is a software package for the construction of large-scale *distributional thesauri*. It provides an efficient yet flexible framework for calculating all pair-wise similarities between terms in a corpus.

## Distribution Thesauri Overview 

### Informally

Naively, a distribution thesaurus can be thought of much like a tradition thesauri. It allows one to lookup a word to return a list of synonyms. Unlike a traditional thesauri, however, the synonyms aren't manually curated by a human, they are calculated using a statistical model estimated from a text corpus. The similarity between two terms might be calculated from the intersection of the features of the terms, extracted from to corpus. For example if the phrases "christmas holiday" and "xmas holiday" occur very frequently in the corpus, the model might indicate that *christmas* and *xmas* are similar. Here we have decided that co-occurrent words are features, both *christmas* and *xmas* share the feature *holiday*, so they are similar. 

### Less Informally

Unfortunately, a distribution thesauri is actually not at all like a manually curated one. Fundamentally we are defining a notions of similarity that is wholly different from synonymy. Worse still is that it changes depending on the corpus, features selection and similarity measure. The previous paragraph is there to give a ludicrously high level overview of the project purpose, to those who are only mildly curious. For those who are intent on using the software, let me start again:

A distributional thesaurus is a resource that contains the estimated *substitutability* of *entries*. Typically these entries are terms or phrases. The thesaurus can be queried with an entry (the *base entry*) and returns a list of other entries (the base entry's *neighbours*). These neighbours can be used as part of an external text-processing pipeline, such as to expand queries during information retrieval. The entries are extracted from a text corpus, along with features of each entry. The similarity of entries is calculated as a function of their features. Underpinning this process is Harris' *Distribution Hypothesis*: "a word is characterised by the company it keeps". As mentioned above, it's hard to pin down the precise notion of similarity used because there are so many variable. 

### Example Build Process

To provide an intuition, here is an example:

Take as our input corpus a balanced collection of English language text (such as Wikipedia). Let our entries be all unique terms in the corpus. We shall select the features of a base-entry as the frequency of all terms that co-occur in the corpus within a window of ±1 terms. Finally, the similarity function will be Cosine, which represents the feature sets as high dimensional vectors. Cosine calculates the similarity as being inversely proportional to the orthogonality of vectors. The thesaurus build process proceeds as follows:

 1. Tokenise the corpus, extracting a list of all unique terms. For each entry record occurrences of all the others entries within a window of ±1. E.g if we encounter the string "the big red bus", the entry-features produced are the:big, big:the, big:red, red:big, red:bus, and bus:red. Note that this process is not yet covered by the provided software.

 2. Count the occurrences of each unique feature with each unique entry. So we may find that the string "red bus" occurs 100 times in the corpus, and that "bus red" occurs 2 times. In this case we produce the features bus:red:102 and red:bus:102, because remember the feature windows is 1 term before and after. For each entry construct a multi-set of all features is occurs with.

 3. Convert the feature multi-set for each entry to a vector, where each features is a dimension, and the magnitude along a dimension is the frequency.

 4. For all pair-wise combinations of entries calculate their similarity as the cosine of the angle between their feature vectors.

 5. For each base entry, select as it's neighbours the top k highest similarity entries.

The resultant thesaurus will have a highly semantic notion of similarity. The neighbours of a word are likely to be strongly related in terms of meaning and topic, but not at all in terms of syntax. So for example the top neighbours of *happy* could be *pleased*, *impressed*, *satisfied*, *surprised*, *disappointed*, *thrilled*, *upset*, and *glad*. Notice that while some words - such as the adjectival sense of *pleased* - are good synonyms, there are some antonymic words such as *disappointed*.

## Distribution 

The software is primarily distributed in source-code form. Binaries may be available sporadically, and on request. 
The source code can be acquired from the [github repository](https://github.com/hamishmorgan/Byblo), click the *Downloads* button, and select a version to download an archive of the source code.

## Dependencies

The project requires [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on the system. It also requires a unix command-line such as Linux or Mac OS X. Windows users may get it to work through [Cygwin](http://www.cygwin.com). The software is compiled using an [Apache Ant](http://ant.apache.org) script. In addition the following Java libraries are required:

 * [Google Guava r09](http://code.google.com/p/guava-libraries/) -- A library containing numerous useful features and tools; mostly the kind of thing that should have been included with Java in the first place.

 * [JCommander 1.1.7](http://github.com/cbeust/jcommander) --- A framework for parsing command line arguments in a very elegant way.

 * [Fastutil 6.2.2](http://fastutil.dsi.unimi.it/) --- A library for collections that handle primitive data types efficiently. It also includes improved implementations of most of the standard collection framework.

 * [Commons Logging 1.1.1](http://commons.apache.org/logging/) --- A very light weight wrapper API that enables logging frameworks to be configured and "plugged in" at runtime. 

All except JCommander are available in pre-compiled binary form. Simply place the `.jar` files in the `/libs/` directory. In the case JCommander you must compile it first using [maven](http://maven.apache.org/). To streamline this process there is a script `/libs/download_libraries.sh`, which will attempt to automatically download (and compile where necessary) all the dependancies.

## Building

Compiling the software from a source distribution

### From the command line

To compile the software from the command line:

```sh
$ cd libs
$ ./download_libraries.sh
$ cd ..
$ ant jar
```

This will compile the source code, and create a new directory `/dist/` containing the project `jar` file, and a copy of the libraries of various dependencies.

### Creating a Netbeans project.

This section how build the project from with Netbeans 7. First acquire the source code as described above. 

1. From the command-line, enter the `libs` directory and run the download script:

    ```sh
$ cd libs
$ ./download_libraries.sh
```

2. Start Netbeans and select "File -> New Project" from the menu bar. Select *Java / Java Project with Existing Sources* and click *Next*.

4. Enter the *Project Name* as "Byblo", and select the *Project Folder* a the location of the project source code. Click *Next*.

5. To *Source Package Folders* click *Add Folder* and select `src`. To *Test Package Folders* click *Add Folder* and select `test`. Click *Next* then *Finish*.

6. Right click on *Libraries* in the *Projects* view, and select *Add JAR/Folder*. Select all `.jar` files in the `libs` directory and click *Choose*.

From here you can run the project by clicking *Run -> Run Main Project* from the menu bar, and selecting `uk.ac.susx.mlcl.byblo.Byblo` as the main class.

## Usage 

This `byblo.sh` script is designed to be the primary point of usage for the thesaurus building software. It runs a complete build process from frequency counting to K-Nearest-Neighbours in a single pass, providing all the
most commonly used functionality of the underlying software.


```sh
$ ./byblo.sh [<options>] [@<config>] <file>
```

Where the arguments are:

 * `<file>` Input instances file containing head/contexts pairs.

 * `@<config>` Options and input files can be read from a <config> file specified directly after an '$\mathtt{@}$' character. Options in this file should be specified exactly as they would be at the command line, and may contain additional `@` references to other config files. 

 * `<options>` Any number of the option switches

There are a large number of options. To view a complete list enter ```./byblo.sh --help``` or view the wiki page on [Running the Sotware](https://github.com/hamishmorgan/Byblo/wiki/Running-the-Software)

## Attribution 

This project is supported a TSB (Technology Strategy Board) grant, project  reference GCL-100934, and by [EPSRC Studentships](http://www.epsrc.ac.uk/funding/students/Pages/default.aspx), and by Graduate Teaching Assistantships from the School of Informatics, University of Sussex.

Special thanks to all members of the Machine Learning and Computational Linguistics Lab, School of Informatics, University of Sussex, for all the helpful input.

## Contributing

To contributed to the project you should fork the git repository. First click the "Fork" button on github. Then open a console and type the following:

```sh
$ git clone git@github.com:[your-user-name]/Byblo.git
$ cd Byblo
$ git remote add upsteam git@github.com:hamishmorgan/Byblo.git
$ git fetch upstream
```

If you have changes to contribute back to the main project, send me a pull request by clicking the "Pull Request" button my fork of the repository.

## Licence

This software is distributed under the 3-clause [BSD Licence](https://github.com/hamishmorgan/Byblo/wiki/Licence).

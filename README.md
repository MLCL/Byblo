# MLCLLib

MLCLLib is a discombobulated collection of Java code, thought be of sufficient quality and usefulness that it can be re-used across a number of the MLCL Labs projects.

## Distribution 

The software is primarily distributed in source-code form. Binaries may be available sporadically, and on request. 
The source code can be acquired from the [github repository](https://github.com/MLCL/MLCLLib), click the *Downloads* button, and select a version to download an archive of the source code.

## Dependencies

The project requires [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on the system. The software is compiled using an [Apache Ant](http://ant.apache.org) script. It should have no dependancies on third party libraries, with the optional exception of [JUnit 4](http://www.junit.org/), which is required for unit testing the project.

## Building

Compiling the software from a source distribution

### From the command line

To compile the software from the command line:

```sh
$ ant dist
```

This will compile the source code, and create a new directory `/dist/` containing the project `jar` file, and a copy of the various required libraries.

### Creating a Netbeans project.

This section details how build the project from with Netbeans 7. First acquire the source code as described above. 

1. Start Netbeans and select "File -> New Project" from the menu bar. Select *Java / Java Project with Existing Sources* and click *Next*.

4. Enter the *Project Name* as "MLCLLib", and select the *Project Folder* as the location of the project source code (the parent directory that contains the `build.xml` file.) Click *Next*.

5. To *Source Package Folders* click *Add Folder* and select `src`. To *Test Package Folders* click *Add Folder* and select `test`. Click *Next* then *Finish*.

From here you can run the project by clicking *Run -> Run Main Project* from the menu bar, and selecting `uk.ac.susx.mlcl.byblo.Byblo` as the main class.

## Attribution 

This project is partially supported a TSB (Technology Strategy Board) grant reference GCL-100934, and by the [EPSRC Doctoral Training Account Scheme](http://www.epsrc.ac.uk/funding/students/dta).

Special thanks to all members of the Machine Learning and Computational Linguistics Lab, School of Informatics, University of Sussex, for all the helpful input.

## Contributing

To contributed to the project you should fork the git repository. First click the "Fork" button on github. Then open a console and type the following:

```sh
$ git clone git@github.com:[your-user-name]/MLCLLib.git
$ cd Byblo
$ git remote add upsteam git@github.com:MLCL/MLCLLib.git
$ git fetch upstream
```

If you have changes to contribute back to the main project, send us a pull request by clicking the "Pull Request" button my fork of the repository.

## Licence

This software is distributed under the 3-clause [BSD Licence](https://raw.github.com/MLCL/MLCLLib/master/LICENCE).

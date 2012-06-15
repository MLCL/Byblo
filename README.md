# MLCLLib

MLCLLib is a discombobulated collection of Java code, thought be of sufficient quality and usefulness that it can be re-used across a number of the MLCL Labs projects.

## Distribution 

The software is primarily distributed in source-code form. Binaries may be available sporadically, and on request. 

The source code can be acquired from the [github repository](https://github.com/MLCL/MLCLLib), click the *Downloads* button, and select a version to download an archive of the source code.

API javadocs can be browsed here: [http://mlcl.github.com/MLCLLib/apidocs/](http://mlcl.github.com/MLCLLib/apidocs/)

### Acquire the library as a Maven Dependancy

To use this library as part of a Maven managed project, simply add the following snipped to the dependencies section of your ```pom.xml```.

```xml
<dependency>
   	<groupId>uk.ac.susx.mlcl</groupId>
	<artifactId>mlcl-lib</artifactId>
	<version>0.2</version>
	<type>jar</type>
</dependency>
```

Well almost simple... At time of writing the library is not available on the Maven central repository, so you will also need to add our repositories to your ```pom.xml```:

```xml
<repositories>
    <repository>
        <id>mlcl-repository</id>
        <name>MLCL Group Public Repository</name>
        <url>http://kungf.eu:8081/nexus/content/groups/public/</url>
        <snapshots><enabled>true</enabled></snapshots>
        <releases><enabled>true</enabled></releases>
    </repository>
</repositories>
```


## Dependencies

The project requires [Java 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed on the system. The software is compiled using an [Apache Maven](http://maven.apache.org/) script. It should have no compile or runtime dependancies on third party libraries, however unit testing requires [JUnit 4](http://www.junit.org/).


## Building

Compiling the software from a source distribution

### Building from the command line

To compile the software from the command line, run:

```sh
$ mvn -P release install -Dgpg.skip=true
```

This will compile the source code, and create a new directory `/target/` containing the project `jar` file, and various assemblies. (See Build Output bellow). It will also install the artefacts to your local Maven repository.

### Building as a Netbeans project.

This section details how to build the project from with Netbeans 7. First acquire the source code as described above. 

1. Start Netbeans and select "File -> Open Project" from the menu bar.

2. Browse the directory to which you downloaded mlcl-lib. Select the directory and click "Open Project".

3. From the Run menu select "Clean and Build Main Project".

This will compile the source code, and create a new directory `/target/` containing the project `jar` file, and various assemblies. (See Build Output bellow)

### Build Output:

All build output is stored in the newly created directory `/target/`. It will
contain a number of sub-directories along with containing the compiled `jar` archive, and various assemblies:

```
...
mlcl-lib-<version>.jar
mlcl-lib-<version>-sources.jar
mlcl-lib-<version>-javadoc.jar
mlcl-lib-<version>-bin.zip
mlcl-lib-<version>-src.zip
...
```

The `mlcl-lib-<version>.jar` file is all you really need to use the library. The `-sources.jar` and `-javadoc.jar` can be used by most IDEs to assist developers, but don't worry about them if you don't know how. The `-bin.zip` and `-src.zip` are binary and source distributions respectively. The binary distribution contains the ```jar``` archives, along with a copy of the README. The source distribution should be an exact replica of the distribution you just downloaded. In addition to the `.zip` distribution artefacts, there will be `.tar.gz` and `.tar.bz2` files, which contain the same data but use different compression algorithms.

## Attribution 

This project is partially supported by a TSB (Technology Strategy Board) grant reference GCL-100934, and by the [EPSRC Doctoral Training Account Scheme](http://www.epsrc.ac.uk/funding/students/dta).

Special thanks to all members of the Machine Learning and Computational Linguistics Lab, School of Informatics, University of Sussex, for all the helpful input.


## Contributing

To contributed to the project you should fork the git repository. First click the "Fork" button on GitHub. Then open a console to run the following:

```sh
$ git clone git@github.com:[your-user-name]/MLCLLib.git
$ cd MLCLLib
$ git remote add upsteam git@github.com:MLCL/MLCLLib.git
$ git fetch upstream
```

If you have changes to contribute back to the main project, push changes to your fork of the repository, then send us a pull request by clicking the "Pull Request" button on MLCL fork of the repository.


## Licence

This software is distributed under the 3-clause [BSD Licence](https://raw.github.com/MLCL/MLCLLib/master/LICENCE).

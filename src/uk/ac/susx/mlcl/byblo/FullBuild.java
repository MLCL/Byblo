/*
 * Copyright (c) 2010-2012, University of Sussex
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 *  * Neither the name of the University of Sussex nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import static java.text.MessageFormat.format;
import uk.ac.susx.mlcl.byblo.commands.ExternalCountCommand;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.byblo.commands.IndexTPCommand;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.DoubleConverter;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.TempFileFactoryConverter;

/**
 *
 * @author hiam20
 */
public class FullBuild extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(FullBuild.class);

    private Charset charset = Charset.defaultCharset();

    private File instancesFile;

    private File outputDir;

    @Parameter(names = {"-T", "--temp-dir"},
    description = "Temorary directory which will be used during filtering.",
    converter = TempFileFactoryConverter.class)
    private File tempBaseDir;

    private boolean skipIndex1 = false;

    private boolean skipIndex2 = false;

    private boolean compactFileFormat = true;

    private int numThreads = Runtime.getRuntime().availableProcessors() + 1;

    /*
     * === FILTER PARAMATERISATION ===
     */
    @Parameter(names = {"-fef", "--filter-entry-freq"},
    description = "Minimum entry pair frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEntryMinFreq;

    @Parameter(names = {"-few", "--filter-entry-whitelist"},
    description = "Whitelist file containing entries of interest. (All others will be ignored)",
    validateWith = InputFileValidator.class)
    private File filterEntryWhitelist;

    @Parameter(names = {"-fep", "--filter-entry-pattern"},
    description = "Regular expresion that accepted entries must match.")
    private String filterEntryPattern;

    @Parameter(names = {"-fvf", "--filter-event-freq"},
    description = "Minimum event frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEventMinFreq;

    @Parameter(names = {"-fff", "--filter-feature-freq"},
    description = "Minimum feature frequency threshold.",
    converter = DoubleConverter.class)
    private double filterFeatureMinFreq;

    @Parameter(names = {"-ffw", "--filter-feature-whitelist"},
    description = "Whitelist file containing features of interest. (All others will be ignored)",
    validateWith = InputFileValidator.class)
    private File filterFeatureWhitelist;

    @Parameter(names = {"-ffp", "--filter-feature-pattern"},
    description = "Regular expresion that accepted features must match.")
    private String filterFeaturePattern;

    public FullBuild() {
    }

    public void setInstancesFile(File instancesFile) {
        this.instancesFile = instancesFile;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public File getTempBaseDir() {
        return tempBaseDir;
    }

    public void setTempBaseDir(File tempBaseDir) {
        this.tempBaseDir = tempBaseDir;
    }

    @Override
    public void runCommand() throws Exception {

        checkValidInputFile("Instances file", instancesFile);

        // If the output dir isn't
        if (outputDir == null)
            outputDir = instancesFile.getParentFile();
        checkValidOutputDir("Output dir", outputDir);

        if (tempBaseDir == null)
            tempBaseDir = createTempSubdirDir(outputDir);

        File entryEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".entry-index");
        File featureEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".feature-index");
        File instancesEnumeratedFile =
                new File(outputDir, instancesFile.getName() + ".enumerated");


        {
            checkValidOutputFile("Enumerated instances file", instancesEnumeratedFile);
            checkValidOutputFile("Feature index file", featureEnumeratorFile);
            checkValidOutputFile("Entry index file", entryEnumeratorFile);

            IndexTPCommand indexCmd = new IndexTPCommand();
            indexCmd.setSourceFile(instancesFile);
            indexCmd.setDestinationFile(instancesEnumeratedFile);
            indexCmd.setCharset(charset);
            indexCmd.setEntryEnumeratorFile(entryEnumeratorFile);
            indexCmd.setFeatureEnumeratorFile(featureEnumeratorFile);
            indexCmd.setEnumeratorSkipIndexed1(skipIndex1);
            indexCmd.setEnumeratorSkipIndexed2(skipIndex2);
            indexCmd.setCompactFormatDisabled(!compactFileFormat);

            indexCmd.runCommand();

            checkValidInputFile("Enumerated instances file", instancesEnumeratedFile);
        }


        File entriesFile = new File(outputDir, instancesFile.getName() + ".entries");
        File featuresFile = new File(outputDir, instancesFile.getName() + ".features");
        File eventsFile = new File(outputDir, instancesFile.getName() + ".events");

        {
            int countMaxChunkSize = 500000;

            checkValidInputFile("Enumerated instances file", instancesEnumeratedFile);
            checkValidOutputFile("Entries file", entriesFile);
            checkValidOutputFile("Features file", featuresFile);
            checkValidOutputFile("Events file", eventsFile);

            File countTempDir = createTempSubdirDir(tempBaseDir);
            FileFactory countTmpFact = new TempFileFactory(countTempDir);

            ExternalCountCommand countCmd = new ExternalCountCommand();
            countCmd.setCharset(charset);
            countCmd.setInstancesFile(instancesEnumeratedFile);
            countCmd.setEntriesFile(entriesFile);
            countCmd.setFeaturesFile(featuresFile);
            countCmd.setEventsFile(eventsFile);
            countCmd.setTempFileFactory(countTmpFact);

            countCmd.setCompactFormatDisabled(!compactFileFormat);

            // Configure the enumeration
            countCmd.setEnumeratorSkipIndexed1(skipIndex1);
            countCmd.setEnumeratorSkipIndexed2(skipIndex2);
            countCmd.setEnumeratedEntries(true);
            countCmd.setEnumeratedFeatures(true);

            countCmd.setNumThreads(numThreads);
            countCmd.setMaxChunkSize(countMaxChunkSize);


            countCmd.runCommand();

            checkValidInputFile("Entries file", entriesFile);
            checkValidInputFile("Features file", featuresFile);
            checkValidInputFile("Events file", eventsFile);

            if (countTempDir.list().length > 0)
                throw new IllegalStateException(format("Count temporary directory is not empty: {0}", countTempDir));
            if (!countTempDir.delete())
                throw new IOException(format("Unable to delete count temporary directory is not empty: {0}", countTempDir));
        }


        File entriesFilteredFile = suffixed(entriesFile, ".filtered");
        File featuresFilteredFile = suffixed(featuresFile, ".filtered");
        File eventsFilteredFile = suffixed(eventsFile, ".filtered");


        {
            checkValidInputFile("Entries file", entriesFile);
            checkValidInputFile("Features file", featuresFile);
            checkValidInputFile("Events file", eventsFile);
            checkValidOutputFile("Filtered entries file", entriesFilteredFile);
            checkValidOutputFile("Filtered features file", featuresFilteredFile);
            checkValidOutputFile("Filtered events file", eventsFilteredFile);

            File filterTempDir = createTempSubdirDir(tempBaseDir);
            FileFactory filterTmpFact = new TempFileFactory(filterTempDir);

            FilterCommand filterCmd = new FilterCommand();
            filterCmd.setCharset(charset);
            filterCmd.setInputEntriesFile(entriesFile);
            filterCmd.setInputFeaturesFile(featuresFile);
            filterCmd.setInputEventsFile(eventsFile);
            filterCmd.setOutputEntriesFile(entriesFilteredFile);
            filterCmd.setOutputFeaturesFile(featuresFilteredFile);
            filterCmd.setOutputEventsFile(eventsFilteredFile);
            filterCmd.setTempFiles(filterTmpFact);

            filterCmd.setFilterEventMinFreq(filterEventMinFreq);
            filterCmd.setFilterEntryMinFreq(filterEntryMinFreq);
            filterCmd.setFilterEntryPattern(filterEntryPattern);
            filterCmd.setFilterEntryWhitelist(filterEntryWhitelist);
            filterCmd.setFilterFeatureMinFreq(filterFeatureMinFreq);
            filterCmd.setFilterFeaturePattern(filterFeaturePattern);
            filterCmd.setFilterFeatureWhitelist(filterFeatureWhitelist);

            filterCmd.setEnumeratedEntries(true);
            filterCmd.setEnumeratedFeatures(true);
            filterCmd.setEnumeratorSkipIndexed1(skipIndex1);
            filterCmd.setEnumeratorSkipIndexed2(skipIndex2);
            filterCmd.setEntryEnumeratorFile(entryEnumeratorFile);
            filterCmd.setFeatureEnumeratorFile(featureEnumeratorFile);

            filterCmd.setCompactFormatDisabled(!compactFileFormat);

            filterCmd.runCommand();

            checkValidInputFile("Filtered entries file", entriesFilteredFile);
            checkValidInputFile("Filtered features file", featuresFilteredFile);
            checkValidInputFile("Filtered events file", eventsFilteredFile);

            if (filterTempDir.list().length > 0)
                throw new IllegalStateException(format("Filter temporary directory is not empty: {0}", filterTempDir));
            if (!filterTempDir.delete())
                throw new IOException(format("Unable to delete filter temporary directory is not empty: {0}", filterTempDir));
        }

    }

    private static File createTempSubdirDir(File base) throws IOException {
        checkValidOutputDir("Temporary base directory", base);
        FileFactory tmp = new TempFileFactory(base);
        File tempDir = tmp.createFile("tempdir", "");
        LOG.debug(format("Creating temporary directory {0}", tempDir));
        if (!tempDir.delete() || !tempDir.mkdir())
            throw new IOException(format(
                    "Unable to create temporary directory {0}", tempDir));
        checkValidOutputDir("Temporary directory", tempDir);
        return tempDir;
    }

    private static File suffixed(File file, String suffix) {
        return new File(file.getParentFile(), file.getName() + suffix);
    }

    public static void checkValidInputFile(File file) {
        checkValidInputFile("Input file", file);
    }

    public static void checkValidInputFile(String name, File file) {
        Checks.checkNotNull(name, file);
        if (!file.exists())
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        if (!file.canRead())
            throw new IllegalArgumentException(format(
                    "{0} is not readable: {0}", name, file));
        if (!file.isFile())
            throw new IllegalArgumentException(format(
                    "{0} is not a regular file: ", name, file));

    }

    public static void checkValidOutputFile(File file) {
        checkValidOutputFile("Output file", file);
    }

    public static void checkValidOutputFile(String name, File file) {
        Checks.checkNotNull(name, file);
        if (file.exists()) {
            if (!file.isFile())
                throw new IllegalArgumentException(format(
                        "{0} already exists, but not regular: {1}", name, file));
            if (!file.canWrite())
                throw new IllegalArgumentException(format(
                        "{0} already exists, but is not writeable: {1}", name, file));
        } else {
            if (!file.getParentFile().canWrite())
                throw new IllegalArgumentException(
                        format("{0} can not be created, because the parent "
                        + "directory is not writeable: {1}", name, file));
        }
    }

    public static void checkValidOutputDir(File dir) {
        checkValidOutputDir("Output directory", dir);
    }

    public static void checkValidOutputDir(String name, File file) {
        Checks.checkNotNull(name, file);
        if (!file.exists())
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        if (!file.canWrite())
            throw new IllegalArgumentException(format(
                    "{0} is not writeable: {0}", name, file));
        if (!file.isDirectory())
            throw new IllegalArgumentException(format(
                    "{0} is not a directory: ", name, file));

    }

}

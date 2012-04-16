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
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.DoubleConverter;
import uk.ac.susx.mlcl.lib.commands.FileDeligate;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.TempFileFactoryConverter;

/**
 *
 * @author hiam20
 */
public class FullBuild extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(FullBuild.class);

    @ParametersDelegate
    private FileDeligate fileDeligate = new FileDeligate();

    @Parameter(names = {"-i", "--input"},
    description = "Input instances file",
    validateWith = InputFileValidator.class, required = true)
    private File instancesFile;

    @Parameter(names = {"-o", "--output"},
    description = "Output directory")
    private File outputDir;

    @Parameter(names = {"-T", "--temp-dir"},
    description = "Temorary directory which will be used during filtering.",
    converter = TempFileFactoryConverter.class, hidden=true)
    private File tempBaseDir;

    private boolean skipIndex1 = false;

    private boolean skipIndex2 = false;

    private EnumeratorType enumeratorType = EnumeratorType.JDBC;

    @Parameter(names = {"-t", "--threads"},
    description = "Number of concurrent processing threads.")
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

    @Parameter(names = {"-Smn", "--similarity-min"},
    description = "Minimum similarity threshold.",
    converter = DoubleConverter.class)
    private double minSimilarity = Double.NEGATIVE_INFINITY;

    @Parameter(names = {"-m", "--measure"},
    description = "Similarity measure to use.")
    private String measureName = "Lin";

    public FullBuild() {
    }

    public void setInstancesFile(File instancesFile) {
        this.instancesFile = instancesFile;
    }

    public void setCompactFormatDisabled(boolean compactFormatDisabled) {
        fileDeligate.setCompactFormatDisabled(compactFormatDisabled);
    }

    public final void setCharset(Charset charset) {
        fileDeligate.setCharset(charset);
    }

    public boolean isCompactFormatDisabled() {
        return fileDeligate.isCompactFormatDisabled();
    }

    public final Charset getCharset() {
        return fileDeligate.getCharset();
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
            indexCmd.setCharset(getCharset());

            indexCmd.setEntryEnumeratorFile(entryEnumeratorFile);
            indexCmd.setFeatureEnumeratorFile(featureEnumeratorFile);
            indexCmd.setEnumeratorSkipIndexed1(skipIndex1);
            indexCmd.setEnumeratorSkipIndexed2(skipIndex2);
            indexCmd.setEnumeratorType(enumeratorType);
            indexCmd.setCompactFormatDisabled(isCompactFormatDisabled());

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
            countCmd.setCharset(getCharset());
            countCmd.setInstancesFile(instancesEnumeratedFile);
            countCmd.setEntriesFile(entriesFile);
            countCmd.setFeaturesFile(featuresFile);
            countCmd.setEventsFile(eventsFile);
            countCmd.setTempFileFactory(countTmpFact);

            countCmd.setCompactFormatDisabled(isCompactFormatDisabled());

            // Configure the enumeration
            countCmd.setEnumeratorSkipIndexed1(skipIndex1);
            countCmd.setEnumeratorSkipIndexed2(skipIndex2);
            countCmd.setEnumeratedEntries(true);
            countCmd.setEnumeratedFeatures(true);
            countCmd.setEnumeratorType(enumeratorType);

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
            filterCmd.setCharset(getCharset());
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
            filterCmd.setEnumeratorType(enumeratorType);

            filterCmd.setCompactFormatDisabled(isCompactFormatDisabled());

            filterCmd.runCommand();

            checkValidInputFile("Filtered entries file", entriesFilteredFile);
            checkValidInputFile("Filtered features file", featuresFilteredFile);
            checkValidInputFile("Filtered events file", eventsFilteredFile);

            if (filterTempDir.list().length > 0)
                throw new IllegalStateException(format(
                        "Filter temporary directory is not empty: {0} --- countains {1}",
                        filterTempDir, Arrays.toString(filterTempDir.list())));
            
            if (!filterTempDir.delete())
                throw new IOException(format("Unable to delete filter temporary directory is not empty: {0}", filterTempDir));
        }





        File simsFile = new File(outputDir, instancesFile.getName() + ".sims");

        {
            int allPairsChunksSize = 2500;

            checkValidInputFile("Filtered entries file", entriesFilteredFile);
            checkValidInputFile("Filtered features file", featuresFilteredFile);
            checkValidInputFile("Filtered events file", eventsFilteredFile);
            checkValidOutputFile("Sims file", simsFile);


            AllPairsCommand allpairsCmd = new AllPairsCommand();
            allpairsCmd.setCharset(getCharset());
            allpairsCmd.setCompactFormatDisabled(isCompactFormatDisabled());

            allpairsCmd.setEntriesFile(entriesFilteredFile);
            allpairsCmd.setFeaturesFile(featuresFilteredFile);
            allpairsCmd.setEventsFile(eventsFilteredFile);
            allpairsCmd.setOutputFile(simsFile);

            allpairsCmd.setNumThreads(numThreads);
            allpairsCmd.setChunkSize(allPairsChunksSize);

            allpairsCmd.setMeasureName(measureName);
            allpairsCmd.setMinSimilarity(minSimilarity);

            allpairsCmd.setEnumeratedEntries(true);
            allpairsCmd.setEnumeratedFeatures(true);
            allpairsCmd.setEnumeratorSkipIndexed1(skipIndex1);
            allpairsCmd.setEnumeratorSkipIndexed2(skipIndex2);
            allpairsCmd.setEnumeratorType(enumeratorType);

            allpairsCmd.runCommand();
            checkValidInputFile("Sims file", simsFile);
        }





        File neighboursFile = suffixed(simsFile, ".neighbours");

        {
            checkValidInputFile("Sims file", simsFile);
            checkValidOutputFile("Neighbours file", neighboursFile);

            File knnTempDir = createTempSubdirDir(tempBaseDir);
            FileFactory knnTmpFact = new TempFileFactory(knnTempDir);

            ExternalKnnSimsCommand knnCmd = new ExternalKnnSimsCommand();
            knnCmd.setCharset(getCharset());
            knnCmd.setSourceFile(simsFile);
            knnCmd.setDestinationFile(neighboursFile);

            knnCmd.setEnumeratedEntries(true);
            knnCmd.setEnumeratedFeatures(true);
            knnCmd.setEnumeratorSkipIndexed1(skipIndex1);
            knnCmd.setEnumeratorSkipIndexed2(skipIndex2);
            knnCmd.setEnumeratorType(enumeratorType);

            knnCmd.setTempFileFactory(knnTmpFact);
            knnCmd.setCompactFormatDisabled(isCompactFormatDisabled());
            knnCmd.setNumThreads(numThreads);

            knnCmd.runCommand();

            checkValidInputFile("Neighbours file", neighboursFile);

            if (knnTempDir.list().length > 0)
                throw new IllegalStateException(format("Filter temporary directory is not empty: {0}", knnTempDir));
            if (!knnTempDir.delete())
                throw new IOException(format("Unable to delete filter temporary directory is not empty: {0}", knnTempDir));

        }




        File neighboursStringsFile = suffixed(neighboursFile, ".strings");

        {
            checkValidInputFile("Neighbours file", neighboursFile);
            checkValidOutputFile("Neighbours strings file", neighboursStringsFile);


            UnindexSimsCommand unindexCmd = new UnindexSimsCommand();
            unindexCmd.setSourceFile(neighboursFile);
            unindexCmd.setDestinationFile(neighboursStringsFile);
            unindexCmd.setCharset(getCharset());
            unindexCmd.setCompactFormatDisabled(isCompactFormatDisabled());

            unindexCmd.getIndexDeligate().setEnumerationEnabled(true);
            unindexCmd.getIndexDeligate().setEnumeratorSkipIndexed1(skipIndex1);
            unindexCmd.getIndexDeligate().setEnumeratorSkipIndexed2(skipIndex2);
            unindexCmd.getIndexDeligate().setEnumeratorFile(entryEnumeratorFile);
            unindexCmd.getIndexDeligate().setEnumeratorType(enumeratorType);

            unindexCmd.runCommand();

            checkValidInputFile("Neighbours strings file", neighboursStringsFile);

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

    public EnumeratorType getEnumeratorType() {
        return enumeratorType;
    }

    public void setEnumeratorType(EnumeratorType enumeratorType) {
        this.enumeratorType = enumeratorType;
    }

    public double getFilterEntryMinFreq() {
        return filterEntryMinFreq;
    }

    public void setFilterEntryMinFreq(double filterEntryMinFreq) {
        this.filterEntryMinFreq = filterEntryMinFreq;
    }

    public String getFilterEntryPattern() {
        return filterEntryPattern;
    }

    public void setFilterEntryPattern(String filterEntryPattern) {
        this.filterEntryPattern = filterEntryPattern;
    }

    public File getFilterEntryWhitelist() {
        return filterEntryWhitelist;
    }

    public void setFilterEntryWhitelist(File filterEntryWhitelist) {
        this.filterEntryWhitelist = filterEntryWhitelist;
    }

    public double getFilterEventMinFreq() {
        return filterEventMinFreq;
    }

    public void setFilterEventMinFreq(double filterEventMinFreq) {
        this.filterEventMinFreq = filterEventMinFreq;
    }

    public double getFilterFeatureMinFreq() {
        return filterFeatureMinFreq;
    }

    public void setFilterFeatureMinFreq(double filterFeatureMinFreq) {
        this.filterFeatureMinFreq = filterFeatureMinFreq;
    }

    public String getFilterFeaturePattern() {
        return filterFeaturePattern;
    }

    public void setFilterFeaturePattern(String filterFeaturePattern) {
        this.filterFeaturePattern = filterFeaturePattern;
    }

    public File getFilterFeatureWhitelist() {
        return filterFeatureWhitelist;
    }

    public void setFilterFeatureWhitelist(File filterFeatureWhitelist) {
        this.filterFeatureWhitelist = filterFeatureWhitelist;
    }

    public String getMeasureName() {
        return measureName;
    }

    public void setMeasureName(String measureName) {
        this.measureName = measureName;
    }

    public double getMinSimilarity() {
        return minSimilarity;
    }

    public void setMinSimilarity(double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public boolean isSkipIndex1() {
        return skipIndex1;
    }

    public void setSkipIndex1(boolean skipIndex1) {
        this.skipIndex1 = skipIndex1;
    }

    public boolean isSkipIndex2() {
        return skipIndex2;
    }

    public void setSkipIndex2(boolean skipIndex2) {
        this.skipIndex2 = skipIndex2;
    }

    
    public static void main(String[] args) throws Exception {
        new FullBuild().runCommand(args);
    }
}

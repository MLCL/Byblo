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
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.commands.AbstractExternalSortCommand;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.commands.ExternalCountCommand;
import uk.ac.susx.mlcl.byblo.commands.ExternalKnnSimsCommand;
import uk.ac.susx.mlcl.byblo.commands.FilterCommand;
import uk.ac.susx.mlcl.byblo.commands.IndexingCommands;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.measures.CrMi;
import uk.ac.susx.mlcl.byblo.measures.Lee;
import uk.ac.susx.mlcl.byblo.measures.Lp;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.DoubleConverter;
import uk.ac.susx.mlcl.lib.commands.FileDeligate;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.TempFileFactoryConverter;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

/**
 * Run complete build of Byblo, performing all stages in order.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Run the full thesaurus building pipeline.")
public final class FullBuild extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(FullBuild.class);

    /**
     * Whether or not some of the rarely used parameters should be hidden from
     * the help usage page.
     */
    public static final boolean HIDE_UNCOMMON_PARAMTERS = true;

    @ParametersDelegate
    private FileDeligate fileDeligate = new FileDeligate();

    @Parameter(names = {"-i", "--input"},
    description = "Input instances file",
    validateWith = InputFileValidator.class, required = true)
    private File instancesFile;

    @Parameter(names = {"-o", "--output"},
    description = "Output directory. Default: current working directory.")
    private File outputDir = null;

    @Parameter(names = {"-T", "--temp-dir"},
    description = "Temorary directory, used during processing. Default: A subdirectory will be created inside the output directory.",
    converter = TempFileFactoryConverter.class,
    hidden = HIDE_UNCOMMON_PARAMTERS)
    private File tempBaseDir;

    private boolean skipIndex1 = false;

    private boolean skipIndex2 = false;

    private EnumeratorType enumeratorType = EnumeratorType.JDBC;

    @Parameter(names = {"-t", "--threads"},
    description = "Number of concurrent processing threads.")
    private int numThreads = Runtime.getRuntime().availableProcessors() + 1;

    /*
     * === COUNTING PARAMATERISATION ===
     */
    @Parameter(names = {"--count-chunk-size"},
    description = "Number of lines per work unit. Larger values increase performance and memory usage.",
    hidden = true)
    private int countMaxChunkSize = ExternalCountCommand.DEFAULT_MAX_CHUNK_SIE;

    /*
     * === FILTER PARAMATERISATION ===
     */
    @Parameter(names = {"-fef", "--filter-entry-freq"},
    description = "Minimum entry frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEntryMinFreq;

    @Parameter(names = {"-few", "--filter-entry-whitelist"},
    description = "Whitelist file containing entries of interest. "
    + "(All others will be ignored)",
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
    description = "Whitelist file containing features of interest. "
    + "(All others will be ignored)",
    validateWith = InputFileValidator.class)
    private File filterFeatureWhitelist;

    @Parameter(names = {"-ffp", "--filter-feature-pattern"},
    description = "Regular expresion that accepted features must match.")
    private String filterFeaturePattern;
    /*
     * === ALL-PAIRS PARAMATERISATION ===
     */

    @Parameter(names = {"-m", "--measure"},
    description = "Similarity measure to use.")
    private String measureName = AllPairsCommand.DEFAULT_MEASURE;

    @Parameter(names = {"-Smn", "--similarity-min"},
    description = "Minimum similarity threshold.",
    converter = DoubleConverter.class)
    private double minSimilarity = AllPairsCommand.DEFAULT_MIN_SIMILARITY;

    @Parameter(names = {"-Smx", "--similarity-max"},
    description = "Maximyum similarity threshold.",
    hidden = HIDE_UNCOMMON_PARAMTERS,
    converter = DoubleConverter.class)
    private double maxSimilarity = AllPairsCommand.DEFAULT_MAX_SIMILARITY;

    @Parameter(names = {"--crmi-beta"},
    description = "Beta paramter to CRMI measure.",
    hidden = HIDE_UNCOMMON_PARAMTERS,
    converter = DoubleConverter.class)
    private double crmiBeta = CrMi.DEFAULT_BETA;

    @Parameter(names = {"--crmi-gamma"},
    description = "Gamma paramter to CRMI measure.",
    hidden = HIDE_UNCOMMON_PARAMTERS,
    converter = DoubleConverter.class)
    private double crmiGamma = CrMi.DEFAULT_GAMMA;

    @Parameter(names = {"--mink-p"},
    description = "P parameter to Minkowski distance measure.",
    hidden = HIDE_UNCOMMON_PARAMTERS,
    converter = DoubleConverter.class)
    private double minkP = Lp.DEFAULT_P;

    @Parameter(names = {"--measure-reversed"},
    description = "Swap similarity measure inputs.",
    hidden = HIDE_UNCOMMON_PARAMTERS)
    private boolean measureReversed = false;

    @Parameter(names = {"--lee-alpha"},
    description = "Alpha parameter to Lee alpha-skew divergence measure.",
    hidden = HIDE_UNCOMMON_PARAMTERS,
    converter = DoubleConverter.class)
    private double leeAlpha = Lee.DEFAULT_ALPHA;

    @Parameter(names = {"-ip", "--identity-pairs"},
    description = "Produce similarity between pair of identical entries.",
    hidden = HIDE_UNCOMMON_PARAMTERS)
    private boolean outputIdentityPairs = false;

    @Parameter(names = {"--allpairs-chunk-size"},
    description = "Number of entries to compare per work unit. Larger value increase performance and memory usage.",
    hidden = HIDE_UNCOMMON_PARAMTERS)
    private int chunkSize = AllPairsCommand.DEFAULT_MAX_CHUNK_SIZE;
    /*
     * === K-NEAREST-NEIGHBOURS PARAMATERISATION ===
     */

    @Parameter(names = {"-k"},
    description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnSimsCommand.DEFAULT_K;

    @Parameter(names = {"--knn-chunk-size"},
    description = "Number of lines per KNN work unit. Larger values increase memory usage and performace.",
    hidden = HIDE_UNCOMMON_PARAMTERS)
    private int knnMaxChunkSize = AbstractExternalSortCommand.DEFAULT_MAX_CHUNK_SIZE;

    /**
     * Should only be instantiated through the main method.
     */
    protected FullBuild() {
    }

    public static void main(String[] args) throws Exception {
        new FullBuild().runCommand(args);
    }

    @Override
    public void runCommand() throws Exception {

        checkValidInputFile("Instances file", instancesFile);

        if (outputDir == null)
            outputDir = new File(System.getProperty("user.dir"));
        checkValidOutputDir("Output dir", outputDir);

        if (tempBaseDir == null)
            tempBaseDir = createTempSubdirDir(outputDir);

        File entryEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".entry-index");
        File featureEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".feature-index");
        File instancesEnumeratedFile =
                new File(outputDir, instancesFile.getName() + ".enumerated");

        runIndex(instancesEnumeratedFile, featureEnumeratorFile, entryEnumeratorFile);

        File entriesFile = new File(outputDir,
                                    instancesFile.getName() + ".entries");
        File featuresFile = new File(outputDir,
                                     instancesFile.getName() + ".features");
        File eventsFile = new File(outputDir,
                                   instancesFile.getName() + ".events");

        runCount(instancesEnumeratedFile, entriesFile, featuresFile, eventsFile);


        File entriesFilteredFile = suffixed(entriesFile, ".filtered");
        File featuresFilteredFile = suffixed(featuresFile, ".filtered");
        File eventsFilteredFile = suffixed(eventsFile, ".filtered");

        runFilter(entriesFile, featuresFile, eventsFile, entriesFilteredFile,
                  featuresFilteredFile, eventsFilteredFile, entryEnumeratorFile,
                  featureEnumeratorFile);


        File simsFile = new File(outputDir, instancesFile.getName() + ".sims");
        runAllpairs(entriesFilteredFile, featuresFilteredFile, eventsFilteredFile, simsFile);

        File neighboursFile = suffixed(simsFile, ".neighbours");

        runKNN(simsFile, neighboursFile);

        File neighboursStringsFile = suffixed(neighboursFile, ".strings");
        runUnindexSim(neighboursFile, neighboursStringsFile, entryEnumeratorFile);


        if (tempBaseDir.list().length == 0)
            tempBaseDir.delete();
    }

    private void runIndex(File instancesEnumeratedFile,
                          File featureEnumeratorFile, File entryEnumeratorFile)
            throws Exception {
        checkValidOutputFile("Enumerated instances file",
                             instancesEnumeratedFile);
        checkValidOutputFile("Feature index file", featureEnumeratorFile);
        checkValidOutputFile("Entry index file", entryEnumeratorFile);

        IndexingCommands.IndexInstances indexCmd = new IndexingCommands.IndexInstances();
        indexCmd.setSourceFile(instancesFile);
        indexCmd.setDestinationFile(instancesEnumeratedFile);
        indexCmd.setCharset(getCharset());

        indexCmd.getIndexDeligate().setEntryEnumeratorFile(entryEnumeratorFile);
        indexCmd.getIndexDeligate().setFeatureEnumeratorFile(featureEnumeratorFile);
        indexCmd.getIndexDeligate().setEnumeratorType(enumeratorType);

        indexCmd.runCommand();

        checkValidInputFile("Enumerated instances file",
                            instancesEnumeratedFile);
    }

    private void runCount(File instancesEnumeratedFile, File entriesFile,
                          File featuresFile, File eventsFile) throws Exception {

        checkValidInputFile("Enumerated instances file",
                            instancesEnumeratedFile);
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

        // Configure the enumeration
        countCmd.setEnumeratedEntries(true);
        countCmd.setEnumeratedFeatures(true);
        countCmd.setEnumeratorType(enumeratorType);

        countCmd.setNumThreads(numThreads);
        countCmd.setMaxChunkSize(countMaxChunkSize);


        countCmd.runCommand();

        checkValidInputFile("Entries file", entriesFile);
        checkValidInputFile("Features file", featuresFile);
        checkValidInputFile("Events file", eventsFile);

        if (countTempDir.list().length > 0) {
            throw new IllegalStateException(format(
                    "Count temporary directory is not empty: {0}",
                    countTempDir));
        }
        if (!countTempDir.delete()) {
            throw new IOException(format(
                    "Unable to delete count temporary "
                    + "directory is not empty: {0}",
                    countTempDir));
        }
    }

    private void runFilter(File entriesFile, File featuresFile, File eventsFile,
                           File entriesFilteredFile, File featuresFilteredFile,
                           File eventsFilteredFile,
                           File entryEnumeratorFile, File featureEnumeratorFile)
            throws Exception {
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
        filterCmd.setEntryEnumeratorFile(entryEnumeratorFile);
        filterCmd.setFeatureEnumeratorFile(featureEnumeratorFile);
        filterCmd.setEnumeratorType(enumeratorType);

        filterCmd.runCommand();

        checkValidInputFile("Filtered entries file", entriesFilteredFile);
        checkValidInputFile("Filtered features file", featuresFilteredFile);
        checkValidInputFile("Filtered events file", eventsFilteredFile);

        if (filterTempDir.list().length > 0) {
            throw new IllegalStateException(format(
                    "Filter temporary directory is not "
                    + "empty: {0} --- countains {1}",
                    filterTempDir, Arrays.toString(filterTempDir.list())));
        }
        if (!filterTempDir.delete()) {
            throw new IOException(format(
                    "Unable to delete filter temporary "
                    + "directory is not empty: {0}",
                    filterTempDir));
        }
    }

    private void runAllpairs(File entriesFilteredFile, File featuresFilteredFile,
                             File eventsFilteredFile, File simsFile)
            throws Exception {
        checkValidInputFile("Filtered entries file", entriesFilteredFile);
        checkValidInputFile("Filtered features file", featuresFilteredFile);
        checkValidInputFile("Filtered events file", eventsFilteredFile);
        checkValidOutputFile("Sims file", simsFile);


        AllPairsCommand allpairsCmd = new AllPairsCommand();
        allpairsCmd.setCharset(getCharset());

        allpairsCmd.setEntriesFile(entriesFilteredFile);
        allpairsCmd.setFeaturesFile(featuresFilteredFile);
        allpairsCmd.setEventsFile(eventsFilteredFile);
        allpairsCmd.setOutputFile(simsFile);

        allpairsCmd.setNumThreads(numThreads);
        allpairsCmd.setChunkSize(chunkSize);

        allpairsCmd.setMinSimilarity(minSimilarity);
        allpairsCmd.setMaxSimilarity(maxSimilarity);
        allpairsCmd.setOutputIdentityPairs(outputIdentityPairs);

        allpairsCmd.setMeasureName(measureName);
        allpairsCmd.setCrmiBeta(crmiBeta);
        allpairsCmd.setCrmiGamma(crmiGamma);
        allpairsCmd.setLeeAlpha(leeAlpha);
        allpairsCmd.setMinkP(minkP);
        allpairsCmd.setMeasureReversed(measureReversed);

        allpairsCmd.setEnumeratedEntries(true);
        allpairsCmd.setEnumeratedFeatures(true);
        allpairsCmd.setEnumeratorType(enumeratorType);


        allpairsCmd.runCommand();
        checkValidInputFile("Sims file", simsFile);
    }

    private void runKNN(File simsFile, File neighboursFile) throws Exception {
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
        knnCmd.setEnumeratorType(enumeratorType);

        knnCmd.setTempFileFactory(knnTmpFact);
        knnCmd.setNumThreads(numThreads);
        knnCmd.setMaxChunkSize(knnMaxChunkSize);
        knnCmd.setK(k);

        knnCmd.runCommand();

        checkValidInputFile("Neighbours file", neighboursFile);

        if (knnTempDir.list().length > 0) {
            throw new IllegalStateException(format(
                    "Filter temporary directory is not empty: {0}",
                    knnTempDir));
        }
        if (!knnTempDir.delete()) {
            throw new IOException(format(
                    "Unable to delete filter temporary"
                    + " directory is not empty: {0}",
                    knnTempDir));
        }
    }

    private void runUnindexSim(File neighboursFile, File neighboursStringsFile,
                               File entryEnumeratorFile)
            throws Exception {

        checkValidInputFile("Neighbours file", neighboursFile);
        checkValidOutputFile("Neighbours strings file",
                             neighboursStringsFile);


        IndexingCommands.UnindexNeighbours unindexCmd = new IndexingCommands.UnindexNeighbours();
        unindexCmd.setSourceFile(neighboursFile);
        unindexCmd.setDestinationFile(neighboursStringsFile);
        unindexCmd.setCharset(getCharset());

        unindexCmd.getIndexDeligate().setEnumeratedEntries(true);
        unindexCmd.getIndexDeligate().setEntryEnumeratorFile(entryEnumeratorFile);
        unindexCmd.getIndexDeligate().setEnumeratorType(enumeratorType);

        unindexCmd.runCommand();

        checkValidInputFile("Neighbours strings file",
                            neighboursStringsFile);

    }

    private static File createTempSubdirDir(File base) throws IOException {
        checkValidOutputDir("Temporary base directory", base);
        FileFactory tmp = new TempFileFactory(base);
        File tempDir = tmp.createFile("tempdir", "");
        LOG.debug(format("Creating temporary directory {0}", tempDir));
        if (!tempDir.delete() || !tempDir.mkdir()) {
            throw new IOException(format(
                    "Unable to create temporary directory {0}", tempDir));
        }
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
        if (!file.exists()) {
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(format(
                    "{0} is not readable: {0}", name, file));
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(format(
                    "{0} is not a regular file: ", name, file));
        }

    }

    public static void checkValidOutputFile(File file) {
        checkValidOutputFile("Output file", file);
    }

    public static void checkValidOutputFile(String name, File file) {
        Checks.checkNotNull(name, file);
        if (file.exists()) {
            if (!file.isFile()) {
                throw new IllegalArgumentException(format(
                        "{0} already exists, but not regular: {1}",
                        name, file));
            }
            if (!file.canWrite()) {
                throw new IllegalArgumentException(format(
                        "{0} already exists, but is not writeable: {1}",
                        name, file));
            }
        } else {
            if (!file.getParentFile().canWrite()) {
                throw new IllegalArgumentException(
                        format("{0} can not be created, because the parent "
                        + "directory is not writeable: {1}", name, file));
            }
        }
    }

    public static void checkValidOutputDir(File dir) {
        checkValidOutputDir("Output directory", dir);
    }

    public static void checkValidOutputDir(String name, File file) {
        Checks.checkNotNull(name, file);
        if (!file.exists()) {
            throw new IllegalArgumentException(format(
                    "{0} does not exist: {1}", name, file));
        }
        if (!file.canWrite()) {
            throw new IllegalArgumentException(format(
                    "{0} is not writeable: {0}", name, file));
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException(format(
                    "{0} is not a directory: ", name, file));
        }

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

    public void setInstancesFile(File instancesFile) {
        this.instancesFile = instancesFile;
    }

    public void setCharset(Charset charset) {
        fileDeligate.setCharset(charset);
    }

    public Charset getCharset() {
        return fileDeligate.getCharset();
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDirectory) {
        this.outputDir = outputDirectory;
    }

    public File getTempBaseDir() {
        return tempBaseDir;
    }

    public void setTempBaseDir(File tempBaseDirectory) {
        this.tempBaseDir = tempBaseDirectory;
    }

}

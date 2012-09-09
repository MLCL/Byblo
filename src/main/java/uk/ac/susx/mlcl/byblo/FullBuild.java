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
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.measures.impl.LambdaDivergence;
import uk.ac.susx.mlcl.byblo.measures.impl.LeeSkewDivergence;
import uk.ac.susx.mlcl.byblo.measures.impl.LpSpaceDistance;
import uk.ac.susx.mlcl.byblo.measures.impl.Weeds;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.commands.*;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import static java.text.MessageFormat.format;

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
    public static final boolean HIDE_UNCOMMON_PARAMETERS = false;

    @ParametersDelegate
    private FileDelegate fileDelegate = new FileDelegate();

    @Parameter(names = {"-i", "--input"},
            description = "Input instances file",
            validateWith = InputFileValidator.class,
            required = true)
    private File instancesFile;

    @Parameter(names = {"-o", "--output"},
            description = "Output directory. Default: current working directory.")
    private File outputDir = null;

    @Parameter(names = {"-T", "--temp-dir"},
            description = "Temorary directory, used during processing. Default: A subdirectory will be created inside the output directory.",
            converter = TempFileFactoryConverter.class,
            hidden = HIDE_UNCOMMON_PARAMETERS)
    private File tempBaseDir;

    private boolean skipIndex1 = false;

    private boolean skipIndex2 = false;

    private EnumeratorType enumeratorType = EnumeratorType.JDBM;

    @Parameter(names = {"-t", "--threads"},
            description = "Number of concurrent processing threads.")
    private int numThreads = Runtime.getRuntime().availableProcessors();

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
            description = "Regular expression that accepted entries must match.")
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
            description = "Regular expression that accepted features must match.")
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
            hidden = HIDE_UNCOMMON_PARAMETERS,
            converter = DoubleConverter.class)
    private double maxSimilarity = AllPairsCommand.DEFAULT_MAX_SIMILARITY;

    @Parameter(names = {"--crmi-beta"},
            description = "Beta paramter to CRMI measure.",
            hidden = HIDE_UNCOMMON_PARAMETERS,
            converter = DoubleConverter.class)
    private double crmiBeta = Weeds.DEFAULT_BETA;

    @Parameter(names = {"--crmi-gamma"},
            description = "Gamma paramter to CRMI measure.",
            hidden = HIDE_UNCOMMON_PARAMETERS,
            converter = DoubleConverter.class)
    private double crmiGamma = Weeds.DEFAULT_GAMMA;

    @Parameter(names = {"--mink-p"},
            description = "P parameter to Minkowski distance measure.",
            hidden = HIDE_UNCOMMON_PARAMETERS,
            converter = DoubleConverter.class)
    private double minkP = LpSpaceDistance.DEFAULT_POWER;

    @Parameter(names = {"--measure-reversed"},
            description = "Swap similarity measure inputs.",
            hidden = HIDE_UNCOMMON_PARAMETERS)
    private boolean measureReversed = false;

    @Parameter(names = {"--lee-alpha"},
            description = "Alpha parameter to Lee alpha-skew divergence measure.",
            hidden = HIDE_UNCOMMON_PARAMETERS,
            converter = DoubleConverter.class)
    private double leeAlpha = LeeSkewDivergence.DEFAULT_ALPHA;

    @Parameter(names = {"--lambda-lambda"},
            description = "lambda parameter to Lambda-Divergence measure.",
            converter = DoubleConverter.class)
    private double lambdaLambda = LambdaDivergence.DEFAULT_LAMBDA;

    @Parameter(names = {"-ip", "--identity-pairs"},
            description = "Produce similarity between pair of identical entries.",
            hidden = HIDE_UNCOMMON_PARAMETERS)
    private boolean outputIdentityPairs = false;

    /*
     * === K-NEAREST-NEIGHBOURS PARAMATERISATION ===
     */

    @Parameter(names = {"-k"},
            description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnSimsCommand.DEFAULT_K;

    /**
     * Should only be instantiated through the main method.
     */
    protected FullBuild() {
    }


    public static void main(final String[] args) throws Exception {
        try {
            new FullBuild().runCommand(args);
        } catch (ParameterException ex) {
            System.exit(-1);
        }
    }

    @Override
    public void runCommand() throws Exception {
        LOG.info("\n=== Running full thesaurus build === \n");

        checkValidInputFile("Instances file", instancesFile);

        if (outputDir == null)
            outputDir = new File(System.getProperty("user.dir"));
        checkValidOutputDir("Output dir", outputDir);

        if (tempBaseDir == null)
            tempBaseDir = createTempSubdirDir(outputDir);


        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(" * Input instances file: {0}\n",
                    instancesFile));
            sb.append(MessageFormat.format(
                    " * Output directory: {0} ({1} free)\n", outputDir,
                    MiscUtil.humanReadableBytes(outputDir.getFreeSpace())));
            sb.append(MessageFormat.format(
                    " * Temporary directory: {0} ({1} free)\n",
                    tempBaseDir,
                    MiscUtil.humanReadableBytes(tempBaseDir.getFreeSpace())));
            sb.append(MessageFormat.format(" * Character encoding: {0}\n",
                    getCharset()));
            sb.append(MessageFormat.format(" * Num. Threads: {0}\n", numThreads));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append(MessageFormat.format(" * Java Spec: {0} {1}, {2}\n",
                    System.getProperty(
                            "java.specification.name"),
                    System.getProperty(
                            "java.specification.version"),
                    System.getProperty(
                            "java.specification.vendor")));
            sb.append(MessageFormat.format(" * Java VM: {0} {1}, {2}\n",
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.vm.version"),
                    System.getProperty("java.vm.vendor")));
            sb.append(MessageFormat.format(" * Java Runtime: {0} {1}\n",
                    System.getProperty(
                            "java.runtime.name"),
                    System.getProperty(
                            "java.runtime.version")));
            sb.append(MessageFormat.format(" * OS: {0} {1} {2}\n",
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch")));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append("\n");
            LOG.info(sb.toString());
        }

        File entryEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".entry-index");
        File featureEnumeratorFile =
                new File(outputDir, instancesFile.getName() + ".feature-index");
        File instancesEnumeratedFile =
                new File(outputDir, instancesFile.getName() + ".enumerated");


        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 1 of 6: Enumerating Strings ===\n");

        System.gc();
        runIndex(instancesEnumeratedFile, featureEnumeratorFile,
                entryEnumeratorFile);
        System.gc();

        File entriesFile = new File(outputDir,
                instancesFile.getName() + ".entries");
        File featuresFile = new File(outputDir,
                instancesFile.getName() + ".features");
        File eventsFile = new File(outputDir,
                instancesFile.getName() + ".events");

        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 2 of 6: Counting ===\n");
        System.gc();
        runCount(instancesEnumeratedFile, entriesFile, featuresFile, eventsFile);
        System.gc();

        File entriesFilteredFile = suffixed(entriesFile, ".filtered");
        File featuresFilteredFile = suffixed(featuresFile, ".filtered");
        File eventsFilteredFile = suffixed(eventsFile, ".filtered");
        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 3 of 6: Filtering ===\n");

        System.gc();
        runFilter(entriesFile, featuresFile, eventsFile, entriesFilteredFile,
                featuresFilteredFile, eventsFilteredFile, entryEnumeratorFile,
                featureEnumeratorFile);
        System.gc();

        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 4 of 6: All-Pairs ===\n");
        File simsFile = new File(outputDir, instancesFile.getName() + ".sims");

        System.gc();
        runAllPairs(entriesFilteredFile, featuresFilteredFile, eventsFilteredFile, simsFile);
        System.gc();

        File neighboursFile = suffixed(simsFile, ".neighbours");
        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 5 of 6: K-Nearest-Neighbours ===\n");

        System.gc();
        runKNN(simsFile, neighboursFile);
        System.gc();

        File neighboursStringsFile = suffixed(neighboursFile, ".strings");

        if (LOG.isInfoEnabled())
            LOG.info("\n=== Stage 6 of 6: Un-Enumerating ===\n");

        System.gc();
        runUnindexSim(neighboursFile, neighboursStringsFile, entryEnumeratorFile);
        System.gc();


        deleteTempDir(tempBaseDir, "FullBuild");

        final long endTime = System.currentTimeMillis();
        LOG.info("\n=== Completed full thesaurus build ===\n");

        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(" * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(MessageFormat.format(" * Elapsed time: {0}\n", formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n", MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }
    }

    static String formatElapsedTime(long timeMillis) {
        long remaining = timeMillis;
        double seconds = (remaining % 60000) / 1000.0;
        remaining /= 60000;
        int minutes = (int) remaining % 60;
        remaining /= 60;
        long hours = remaining;
        return String.format("%02d:%02d:%02.4f", hours, minutes, seconds);
    }

    private void runIndex(File instancesEnumeratedFile,
                          File featureEnumeratorFile, File entryEnumeratorFile)
            throws Exception {
        checkValidOutputFile("Enumerated instances file",
                instancesEnumeratedFile);
        checkValidOutputFile("Feature index file", featureEnumeratorFile);
        checkValidOutputFile("Entry index file", entryEnumeratorFile);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(" * Input instances file: {0}\n",
                    instancesFile));
            sb.append(MessageFormat.format(
                    " * Output enumerated instances file: {0}\n",
                    instancesEnumeratedFile));
            sb.append(MessageFormat.format(" * Output entry index: {0}\n",
                    entryEnumeratorFile));
            sb.append(MessageFormat.format(" * Output feature index: {0}\n",
                    featureEnumeratorFile));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

        IndexingCommands.IndexInstances indexCmd = new IndexingCommands.IndexInstances();
        indexCmd.setSourceFile(instancesFile);
        indexCmd.setDestinationFile(instancesEnumeratedFile);
        indexCmd.setCharset(getCharset());

        indexCmd.getIndexDelegate().setEntryEnumeratorFile(entryEnumeratorFile);
        indexCmd.getIndexDelegate().setFeatureEnumeratorFile(featureEnumeratorFile);
        indexCmd.getIndexDelegate().setEnumeratorType(enumeratorType);

        indexCmd.runCommand();

        checkValidInputFile("Enumerated instances file",
                instancesEnumeratedFile);

        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }
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


        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(" * Input instances file: {0}\n",
                    instancesEnumeratedFile));
            sb.append(MessageFormat.format(" * Output entries file: {0}\n",
                    entriesFile));
            sb.append(MessageFormat.format(" * Output features file: {0}\n",
                    featuresFile));
            sb.append(MessageFormat.format(" * Output events file: {0}\n",
                    eventsFile));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

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

        countCmd.runCommand();

        checkValidInputFile("Entries file", entriesFile);
        checkValidInputFile("Features file", featuresFile);
        checkValidInputFile("Events file", eventsFile);

        deleteTempDir(countTempDir, "Count");

        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));

            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }
    }

    static void deleteTempDir(File tempDir, String taskName) {
        if (!tempDir.delete()) {
            LOG.warn(format(
                    "Unable to delete temporary directory for task {1}: {0}",
                    tempDir, taskName));
            if (tempDir.list().length > 0)
                LOG.warn(format(
                        "Directory is not empty: {0}; contains {1}",
                        tempDir, Arrays.toString(tempDir.list())));
            if (tempDir.getParentFile() != null && !tempDir.getParentFile().
                    canWrite())
                LOG.warn(format("Insufficient permissions to delete {0}",
                        tempDir));
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

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(" * Input entries file: {0}\n",
                    entriesFile));
            sb.append(MessageFormat.format(" * Input features file: {0}\n",
                    featuresFile));
            sb.append(MessageFormat.format(" * Input events file: {0}\n",
                    eventsFile));
            sb.append(MessageFormat.format(" * Output entries file: {0}\n",
                    entriesFilteredFile));
            sb.append(MessageFormat.format(" * Output features file: {0}\n",
                    featuresFilteredFile));
            sb.append(MessageFormat.format(" * Output events file: {0}\n",
                    eventsFilteredFile));
            sb.append(MessageFormat.format(" * Min. Entry Freq: {0}\n",
                    filterEntryMinFreq));
            sb.append(MessageFormat.format(" * Min. Feature Freq: {0}\n",
                    filterFeatureMinFreq));
            sb.append(MessageFormat.format(" * Min. Event Freq: {0}\n",
                    filterEventMinFreq));
            sb.append(MessageFormat.format(" * Entry Pattern: {0}\n",
                    filterEntryPattern));
            sb.append(MessageFormat.format(" * Feature Pattern: {0}\n",
                    filterFeaturePattern));
            sb.append(MessageFormat.format(" * Entry Whitelist: {0}\n",
                    filterEntryWhitelist));
            sb.append(MessageFormat.format(" * Feature Whitelist: {0}\n",
                    filterFeatureWhitelist));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

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

        deleteTempDir(filterTempDir, "Filter");

        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append("\n");
            LOG.info(sb.toString());
        }
    }

    private void runAllPairs(File entriesFilteredFile, File featuresFilteredFile,
                             File eventsFilteredFile, File simsFile)
            throws Exception {
        checkValidInputFile("Filtered entries file", entriesFilteredFile);
        checkValidInputFile("Filtered features file", featuresFilteredFile);
        checkValidInputFile("Filtered events file", eventsFilteredFile);
        checkValidOutputFile("Sims file", simsFile);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(" * Input entries file: {0}\n",
                    entriesFilteredFile));
            sb.append(MessageFormat.format(" * Input features file: {0}\n",
                    featuresFilteredFile));
            sb.append(MessageFormat.format(" * Input events file: {0}\n",
                    eventsFilteredFile));
            sb.append(
                    MessageFormat.format(" * Ouput sims file: {0}\n", simsFile));
            sb.append(MessageFormat.format(" * Measure: {0}{1}\n", measureName,
                    measureReversed ? "(reversed)" : ""));
            sb.append(MessageFormat.format(" * Accept sims range: {0} to {1}\n",
                    minSimilarity, maxSimilarity));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }


        AllPairsCommand allPairsCmd = new AllPairsCommand();
        allPairsCmd.setCharset(getCharset());

        allPairsCmd.setEntriesFile(entriesFilteredFile);
        allPairsCmd.setFeaturesFile(featuresFilteredFile);
        allPairsCmd.setEventsFile(eventsFilteredFile);
        allPairsCmd.setOutputFile(simsFile);

        allPairsCmd.setNumThreads(numThreads);

        allPairsCmd.setMinSimilarity(minSimilarity);
        allPairsCmd.setMaxSimilarity(maxSimilarity);
        allPairsCmd.setOutputIdentityPairs(outputIdentityPairs);

        allPairsCmd.setMeasureName(measureName);
        allPairsCmd.setCrmiBeta(crmiBeta);
        allPairsCmd.setCrmiGamma(crmiGamma);
        allPairsCmd.setLeeAlpha(leeAlpha);
        allPairsCmd.setMinkP(minkP);
        allPairsCmd.setMeasureReversed(measureReversed);

        allPairsCmd.setEnumeratedEntries(true);
        allPairsCmd.setEnumeratedFeatures(true);
        allPairsCmd.setEnumeratorType(enumeratorType);


        allPairsCmd.runCommand();
        checkValidInputFile("Sims file", simsFile);

        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }
    }

    private void runKNN(File simsFile, File neighboursFile) throws Exception {
        checkValidInputFile("Sims file", simsFile);
        checkValidOutputFile("Neighbours file", neighboursFile);

        File knnTempDir = createTempSubdirDir(tempBaseDir);
        FileFactory knnTmpFact = new TempFileFactory(knnTempDir);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(
                    MessageFormat.format(" * Input sims file: {0}\n", simsFile));
            sb.append(MessageFormat.format(" * Ouput neighbours file: {0}\n",
                    neighboursFile));
            sb.append(MessageFormat.format(" * K: {0}\n", k));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

        ExternalKnnSimsCommand knnCmd = new ExternalKnnSimsCommand();
        knnCmd.setCharset(getCharset());
        knnCmd.setSourceFile(simsFile);
        knnCmd.setDestinationFile(neighboursFile);

        knnCmd.setEnumeratedEntries(true);
        knnCmd.setEnumeratedFeatures(true);
        knnCmd.setEnumeratorType(enumeratorType);

        knnCmd.setTempFileFactory(knnTmpFact);
        knnCmd.setNumThreads(numThreads);
        knnCmd.setK(k);

        knnCmd.runCommand();

        checkValidInputFile("Neighbours file", neighboursFile);

        deleteTempDir(knnTempDir, "K-Nearest-Neighbours");

        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }
    }

    private void runUnindexSim(File neighboursFile, File neighboursStringsFile,
                               File entryEnumeratorFile)
            throws Exception {

        checkValidInputFile("Neighbours file", neighboursFile);
        checkValidOutputFile("Neighbours strings file",
                neighboursStringsFile);


        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nConfiguration:\n");
            sb.append(MessageFormat.format(
                    " * Input enuemrated neighbours neighboursFile: {0}\n",
                    neighboursFile));
            sb.append(MessageFormat.format(" * Ouput neighbours file: {0}\n",
                    neighboursStringsFile));
            sb.append(MessageFormat.format(
                    " * Start time: {0,time,full} {0,date,full}\n", startTime));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

        IndexingCommands.UnindexNeighbours unindexCmd = new IndexingCommands.UnindexNeighbours();
        unindexCmd.setSourceFile(neighboursFile);
        unindexCmd.setDestinationFile(neighboursStringsFile);
        unindexCmd.setCharset(getCharset());

        unindexCmd.getIndexDelegate().setEnumeratedEntries(true);
        unindexCmd.getIndexDelegate().setEntryEnumeratorFile(entryEnumeratorFile);
        unindexCmd.getIndexDelegate().setEnumeratorType(enumeratorType);

        unindexCmd.runCommand();

        checkValidInputFile("Neighbours strings file",
                neighboursStringsFile);


        final long endTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nStats:\n");
            sb.append(MessageFormat.format(
                    " * End time: {0,time,full} {0,date,full}\n", endTime));
            sb.append(
                    MessageFormat.format(" * Ellapsed time: {0}\n",
                            formatElapsedTime(endTime - startTime)));
            sb.append(MessageFormat.format(" * {0}\n",
                    MiscUtil.memoryInfoString()));
            sb.append("\n");
            LOG.info(sb.toString());
        }

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
                        "{0} already exists, but is not writable: {1}",
                        name, file));
            }
        } else {
            if (!file.getParentFile().canWrite()) {
                throw new IllegalArgumentException(
                        format("{0} can not be created, because the parent "
                                + "directory is not writable: {1}", name, file));
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
                    "{0} is not writable: {0}", name, file));
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
        fileDelegate.setCharset(charset);
    }

    public Charset getCharset() {
        return fileDelegate.getCharset();
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

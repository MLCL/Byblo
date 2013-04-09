/*
 * Copyright (c) 2010-2013, University of Sussex
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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import static java.text.MessageFormat.format;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.commands.*;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.measures.impl.LambdaDivergence;
import uk.ac.susx.mlcl.byblo.measures.impl.LeeSkewDivergence;
import uk.ac.susx.mlcl.byblo.measures.impl.LpSpaceDistance;
import uk.ac.susx.mlcl.byblo.measures.impl.Weeds;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.commands.*;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

/**
 * Run complete build of Byblo, performing all stages in order.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Run the full thesaurus building pipeline.")
@Nonnull
public final class FullBuild extends AbstractCommand {

    /**
     *
     */
    private static final String NL = System.getProperty("line.separator");
    /**
     *
     */
    private static final Log LOG = LogFactory.getLog(FullBuild.class);
    /**
     * Whether or not some of the rarely used parameters should be hidden from the help usage page.
     */
    private static final boolean HIDE_UNCOMMON_PARAMETERS = false;
    /**
     *
     */
    @ParametersDelegate
    private final FileDelegate fileDelegate = new FileDelegate();
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-i", "--input"},
    description = "Input instances file",
    validateWith = InputFileValidator.class,
    required = true)
    private File instancesFile = null;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-o", "--output"},
    description = "Output directory. Default: current working directory.")
    private File outputDir = null;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-T", "--temp-dir"},
    description = "Temorary directory, used during processing. Default: A subdirectory will "
    + "be created inside the output directory.",
    converter = TempFileFactoryConverter.class,
    hidden = HIDE_UNCOMMON_PARAMETERS)
    private File tempBaseDir;
    /**
     *
     */
    private boolean skipIndex1 = false;
    /**
     *
     */
    private boolean skipIndex2 = false;
    /**
     *
     */
    private EnumeratorType enumeratorType = EnumeratorType.JDBM;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-t", "--threads"},
    description = "Number of concurrent processing threads.")
    private int numThreads = Runtime.getRuntime().availableProcessors();

    /*
     * === FILTER PARAMETERISATION ===
     */
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-fef", "--filter-entry-freq"},
    description = "Minimum entry frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEntryMinFreq;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-few", "--filter-entry-whitelist"},
    description = "Whitelist file containing entries of interest. "
    + "(All others will be ignored)",
    validateWith = InputFileValidator.class)
    private File filterEntryWhitelist;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-fep", "--filter-entry-pattern"},
    description = "Regular expression that accepted entries must match.")
    private String filterEntryPattern;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-fvf", "--filter-event-freq"},
    description = "Minimum event frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEventMinFreq;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-fff", "--filter-feature-freq"},
    description = "Minimum feature frequency threshold.",
    converter = DoubleConverter.class)
    private double filterFeatureMinFreq;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-ffw", "--filter-feature-whitelist"},
    description = "Whitelist file containing features of interest. "
    + "(All others will be ignored)",
    validateWith = InputFileValidator.class)
    private File filterFeatureWhitelist;
    /**
     *
     */
    @Nullable
    @Parameter(names = {"-ffp", "--filter-feature-pattern"},
    description = "Regular expression that accepted features must match.")
    private String filterFeaturePattern;
    /*
     * === ALL-PAIRS PARAMETERISATION ===
     */
    /**
     *
     */
    @Parameter(names = {"-m", "--measure"},
    description = "Similarity measure to use.")
    private String measureName = AllPairsCommand.DEFAULT_MEASURE;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-Smn", "--similarity-min"},
    description = "Minimum similarity threshold.",
    converter = DoubleConverter.class)
    private double minSimilarity = AllPairsCommand.DEFAULT_MIN_SIMILARITY;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"-Smx", "--similarity-max"},
    description = "Maximum similarity threshold.",
    hidden = HIDE_UNCOMMON_PARAMETERS,
    converter = DoubleConverter.class)
    private double maxSimilarity = AllPairsCommand.DEFAULT_MAX_SIMILARITY;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"--crmi-beta"},
    description = "Beta parameter to CRMI measure.",
    hidden = HIDE_UNCOMMON_PARAMETERS,
    converter = DoubleConverter.class)
    private double crmiBeta = Weeds.DEFAULT_BETA;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"--crmi-gamma"},
    description = "Gamma parameter to CRMI measure.",
    hidden = HIDE_UNCOMMON_PARAMETERS,
    converter = DoubleConverter.class)
    private double crmiGamma = Weeds.DEFAULT_GAMMA;
    /**
     *
     */
    @Parameter(names = {"--mink-p"},
    description = "P parameter to Minkowski distance measure.",
    hidden = HIDE_UNCOMMON_PARAMETERS,
    converter = DoubleConverter.class)
    private double minkP = LpSpaceDistance.DEFAULT_POWER;
    /**
     *
     */
    @Parameter(names = {"--measure-reversed"},
    description = "Swap similarity measure inputs.",
    hidden = HIDE_UNCOMMON_PARAMETERS)
    private boolean measureReversed = false;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"--lee-alpha"},
    description = "Alpha parameter to Lee alpha-skew divergence measure.",
    hidden = HIDE_UNCOMMON_PARAMETERS,
    converter = DoubleConverter.class)
    private double leeAlpha = LeeSkewDivergence.DEFAULT_ALPHA;
    /**
     *
     */
    @Nonnegative
    @Parameter(names = {"--lambda-lambda"},
    description = "lambda parameter to Lambda-Divergence measure.",
    converter = DoubleConverter.class)
    private double lambdaLambda = LambdaDivergence.DEFAULT_LAMBDA;
    /**
     *
     */
    @Parameter(names = {"-ip", "--identity-pairs"},
    description = "Produce similarity between pair of identical entries.",
    hidden = HIDE_UNCOMMON_PARAMETERS)
    private boolean outputIdentityPairs = false;

    /*
     * === K-NEAREST-NEIGHBOURS PARAMETERISATION ===
     */
    /**
     *
     */
    @Parameter(names = {"-k"},
    description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnSimsCommand.DEFAULT_K;

    private enum Stage {

        enumerate("Enumerating strings"),
        count("Counting events"),
        filter("Filtering"),
        allpairs("All-Pairs Similarity Search"),
        knn("K-Nearest Neighbours"),
        unenumerate("Un-enumerating strings");
        private final String description;

        private Stage(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Visible For JCommander access only
    public static class StageConverter extends EnumSetConverter<Stage> {

        public StageConverter(String optionName) {
            super(Stage.class, optionName);
        }
    }
    /**
     *
     */
    @Parameter(names = {"-s", "--stages"},
    converter = StageConverter.class,
    description = "Comma-separated list of stages to run. "
    + "The standard behaviour is to run all required stages (as listed in the default value.)")
    private Set<Stage> stagesToRun = EnumSet.allOf(Stage.class);

    /**
     * Default constructor for testing. Normally this class you only be instantiated through the
     * main method.
     */
    @VisibleForTesting
    protected FullBuild() {
    }

    public static void main(final String[] args) throws Exception {

        try {
            if (!new FullBuild().runCommand(args)) {
                System.exit(-2);
            }
        } catch (ParameterException ex) {
            System.exit(-1);
        }
    }

    @Override
    @CheckReturnValue
    public boolean runCommand() {
        try {
            LOG.info("\n=== Running full thesaurus build === \n");

            if (outputDir == null) {
                outputDir = new File(System.getProperty("user.dir"));
            }
            checkValidOutputDir("Output dir", outputDir);

            if (tempBaseDir == null) {
                tempBaseDir = createTempSubDir(outputDir);
            }

            final long startTime = System.currentTimeMillis();
            logGeneralConfiguration(startTime);

            final File entryEnumeratorFile = new File(outputDir, instancesFile.getName() + ".entry-index");
            final File featureEnumeratorFile = new File(outputDir, instancesFile.getName() + ".feature-index");
            final File instancesEnumeratedFile = new File(outputDir, instancesFile.getName() + ".enumerated");

            LOG.info("\n=== Stage 1 of 6: Enumerating Strings ===\n");

            if (stagesToRun.contains(Stage.enumerate)) {
                checkValidInputFile("Instances file", instancesFile);
                runIndex(instancesEnumeratedFile, featureEnumeratorFile, entryEnumeratorFile);
            } else {
                LOG.info("Skipped stage.");
            }

            final File entriesFile = new File(outputDir, instancesFile.getName() + ".entries");
            final File featuresFile = new File(outputDir, instancesFile.getName() + ".features");
            final File eventsFile = new File(outputDir, instancesFile.getName() + ".events");

            LOG.info("\n=== Stage 2 of 6: Counting ===\n");

            if (stagesToRun.contains(Stage.count)) {
                runCount(instancesEnumeratedFile, entriesFile, featuresFile, eventsFile);
            } else {
                LOG.info("Skipped stage.");
            }

            final File entriesFilteredFile = suffixed(entriesFile, ".filtered");
            final File featuresFilteredFile = suffixed(featuresFile, ".filtered");
            final File eventsFilteredFile = suffixed(eventsFile, ".filtered");


            LOG.info("\n=== Stage 3 of 6: Filtering ===\n");

            if (stagesToRun.contains(Stage.filter)) {
                runFilter(entriesFile, featuresFile, eventsFile, entriesFilteredFile,
                          featuresFilteredFile, eventsFilteredFile, entryEnumeratorFile,
                          featureEnumeratorFile);
            } else {
                LOG.info("Skipped stage.");
            }


            LOG.info("\n=== Stage 4 of 6: All-Pairs ===\n");

            final File simsFile = new File(outputDir, instancesFile.getName() + ".sims");

            if (stagesToRun.contains(Stage.allpairs)) {
                runAllPairs(entriesFilteredFile, featuresFilteredFile, eventsFilteredFile, simsFile);
            } else {
                LOG.info("Skipped stage.");
            }

            final File neighboursFile = suffixed(simsFile, ".neighbours");

            LOG.info("\n=== Stage 5 of 6: K-Nearest-Neighbours ===\n");

            if (stagesToRun.contains(Stage.knn)) {
                runKNN(simsFile, neighboursFile);
            } else {
                LOG.info("Skipped stage.");
            }

            final File neighboursStringsFile = suffixed(neighboursFile, ".strings");

            LOG.info("\n=== Stage 6 of 6: Un-Enumerating ===\n");

            if (stagesToRun.contains(Stage.unenumerate)) {
                runUnindexSim(neighboursFile, neighboursStringsFile, entryEnumeratorFile);
            } else {
                LOG.info("Skipped stage.");
            }

            deleteTempDir(tempBaseDir, "FullBuild");

            LOG.info("\n=== Completed full thesaurus build ===\n");
            final long endTime = System.currentTimeMillis();
            logStageEnd(startTime, endTime);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    /**
     *
     * @param instancesEnumeratedFile
     * @param featureEnumeratorFile
     * @param entryEnumeratorFile
     */
    private void runIndex(File instancesEnumeratedFile, File featureEnumeratorFile,
                          File entryEnumeratorFile) {
        checkValidOutputFile("Enumerated instances file", instancesEnumeratedFile);
        checkValidOutputFile("Feature index file", featureEnumeratorFile);
        checkValidOutputFile("Entry index file", entryEnumeratorFile);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input instances file: {0}\n", instancesFile));
            sb.append(format(" * Output enumerated instances file: {0}\n", instancesEnumeratedFile));
            sb.append(format(" * Output entry index: {0}\n", entryEnumeratorFile));
            sb.append(format(" * Output feature index: {0}\n", featureEnumeratorFile));
            sb.append(NL);
            LOG.info(sb.toString());
        }

        IndexingCommands.IndexInstances indexCmd = new IndexingCommands.IndexInstances();
        indexCmd.setSourceFile(instancesFile);
        indexCmd.setDestinationFile(instancesEnumeratedFile);
        indexCmd.setCharset(getCharset());

        indexCmd.getIndexDelegate().setEntryEnumeratorFile(entryEnumeratorFile);
        indexCmd.getIndexDelegate().setFeatureEnumeratorFile(featureEnumeratorFile);
        indexCmd.getIndexDelegate().setEnumeratorType(enumeratorType);

        if (!indexCmd.runCommand()) {
            throw new RuntimeException("Indexing command failed.");
        }

        checkValidInputFile("Enumerated instances file",
                            instancesEnumeratedFile);

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);
    }

    /**
     *
     * @param instancesEnumeratedFile
     * @param entriesFile
     * @param featuresFile
     * @param eventsFile
     * @throws IOException
     */
    private void runCount(File instancesEnumeratedFile, File entriesFile,
                          File featuresFile, File eventsFile) throws IOException {

        checkValidInputFile("Enumerated instances file",
                            instancesEnumeratedFile);
        checkValidOutputFile("Entries file", entriesFile);
        checkValidOutputFile("Features file", featuresFile);
        checkValidOutputFile("Events file", eventsFile);

        File countTempDir = createTempSubDir(tempBaseDir);
        FileFactory countTmpFact = new TempFileFactory(countTempDir);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input instances file: {0}\n", instancesEnumeratedFile));
            sb.append(format(" * Output entries file: {0}\n", entriesFile));
            sb.append(format(" * Output features file: {0}\n", featuresFile));
            sb.append(format(" * Output events file: {0}\n", eventsFile));
            sb.append(NL);
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

        if (!countCmd.runCommand()) {
            throw new RuntimeException("Count command failed.");
        }

        checkValidInputFile("Entries file", entriesFile);
        checkValidInputFile("Features file", featuresFile);
        checkValidInputFile("Events file", eventsFile);

        deleteTempDir(countTempDir, "Count");

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);
    }

    private void runFilter(File entriesFile, File featuresFile, File eventsFile,
                           File entriesFilteredFile, File featuresFilteredFile,
                           File eventsFilteredFile,
                           File entryEnumeratorFile, File featureEnumeratorFile)
            throws IOException {
        checkValidInputFile("Entries file", entriesFile);
        checkValidInputFile("Features file", featuresFile);
        checkValidInputFile("Events file", eventsFile);
        checkValidOutputFile("Filtered entries file", entriesFilteredFile);
        checkValidOutputFile("Filtered features file", featuresFilteredFile);
        checkValidOutputFile("Filtered events file", eventsFilteredFile);

        File filterTempDir = createTempSubDir(tempBaseDir);
        FileFactory filterTmpFact = new TempFileFactory(filterTempDir);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(" * Input entries file: ").append(entriesFile).append(NL);
            sb.append(" * Input features file: ").append(featuresFile).append(NL);
            sb.append(" * Input events file: ").append(eventsFile).append(NL);
            sb.append(" * Output entries file: ").append(entriesFilteredFile).append(NL);
            sb.append(" * Output features file: ").append(featuresFilteredFile).append(NL);
            sb.append(" * Output events file: ").append(eventsFilteredFile).append(NL);
            sb.append(" * Min. Entry Freq: ").append(filterEntryMinFreq).append(NL);
            sb.append(" * Min. Feature Freq: ").append(filterFeatureMinFreq).append(NL);
            sb.append(" * Min. Event Freq: ").append(filterEventMinFreq).append(NL);
            sb.append(" * Entry Pattern: ").append(filterEntryPattern).append(NL);
            sb.append(" * Feature Pattern: ").append(filterFeaturePattern).append(NL);
            sb.append(" * Entry Whitelist: ").append(filterEntryWhitelist).append(NL);
            sb.append(" * Feature Whitelist: ").append(filterFeatureWhitelist).append(NL);
            sb.append(NL);
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

        if (!filterCmd.runCommand()) {
            throw new RuntimeException("Filter command failed.");
        }

        checkValidInputFile("Filtered entries file", entriesFilteredFile);
        checkValidInputFile("Filtered features file", featuresFilteredFile);
        checkValidInputFile("Filtered events file", eventsFilteredFile);

        deleteTempDir(filterTempDir, "Filter");

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);
    }

    private void runAllPairs(File entriesFilteredFile, File featuresFilteredFile,
                             File eventsFilteredFile, File simsFile) {
        checkValidInputFile("Filtered entries file", entriesFilteredFile);
        checkValidInputFile("Filtered features file", featuresFilteredFile);
        checkValidInputFile("Filtered events file", eventsFilteredFile);
        checkValidOutputFile("Sims file", simsFile);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input entries file: {0}\n", entriesFilteredFile));
            sb.append(format(" * Input features file: {0}\n", featuresFilteredFile));
            sb.append(format(" * Input events file: {0}\n", eventsFilteredFile));
            sb.append(
                    format(" * Output sims file: {0}\n", simsFile));
            sb.append(format(" * Measure: {0}{1}\n", measureName,
                             measureReversed ? "(reversed)" : ""));
            sb.append(format(" * Accept sims range: {0} to {1}\n",
                             minSimilarity, maxSimilarity));
            sb.append(NL);
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
        allPairsCmd.setLambdaLambda(lambdaLambda);

        allPairsCmd.setEnumeratedEntries(true);
        allPairsCmd.setEnumeratedFeatures(true);
        allPairsCmd.setEnumeratorType(enumeratorType);

        if (!allPairsCmd.runCommand()) {
            throw new RuntimeException("All-Pairs command failed.");
        }

        checkValidInputFile("Sims file", simsFile);

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);
    }

    private void runKNN(File simsFile, File neighboursFile) throws IOException {
        checkValidInputFile("Sims file", simsFile);
        checkValidOutputFile("Neighbours file", neighboursFile);

        File knnTempDir = createTempSubDir(tempBaseDir);
        FileFactory knnTmpFact = new TempFileFactory(knnTempDir);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input sims file: {0}\n", simsFile));
            sb.append(format(" * Output neighbours file: {0}\n", neighboursFile));
            sb.append(format(" * K: {0}\n", k));
            sb.append(NL);
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

        if (!knnCmd.runCommand()) {
            throw new RuntimeException("KNN command failed.");
        }

        checkValidInputFile("Neighbours file", neighboursFile);

        deleteTempDir(knnTempDir, "K-Nearest-Neighbours");

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);
    }

    private void runUnindexSim(File neighboursFile, File neighboursStringsFile,
                               File entryEnumeratorFile) {

        checkValidInputFile("Neighbours file", neighboursFile);
        checkValidOutputFile("Neighbours strings file", neighboursStringsFile);

        final long startTime = System.currentTimeMillis();
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input enumerated neighbours neighboursFile: {0}\n", neighboursFile));
            sb.append(format(" * Output neighbours file: {0}\n", neighboursStringsFile));
            sb.append(NL);
            LOG.info(sb.toString());
        }

        IndexingCommands.UnindexNeighbours unindexCmd = new IndexingCommands.UnindexNeighbours();
        unindexCmd.setSourceFile(neighboursFile);
        unindexCmd.setDestinationFile(neighboursStringsFile);
        unindexCmd.setCharset(getCharset());

        unindexCmd.getIndexDelegate().setEnumeratedEntries(true);
        unindexCmd.getIndexDelegate().setEntryEnumeratorFile(entryEnumeratorFile);
        unindexCmd.getIndexDelegate().setEnumeratorType(enumeratorType);

        if (!unindexCmd.runCommand()) {
            throw new RuntimeException("Unindexing command failed.");
        }

        checkValidInputFile("Neighbours strings file", neighboursStringsFile);

        final long endTime = System.currentTimeMillis();
        logStageEnd(startTime, endTime);

    }

    private static File createTempSubDir(File base) throws IOException {
        checkValidOutputDir("Temporary base directory", base);
        FileFactory tmp = new TempFileFactory(base);
        File tempDir = tmp.createFile("tempDir", "");
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

    private static void checkValidInputFile(String name, File file) {
        Preconditions.checkNotNull(name, "name");
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

    private static void checkValidOutputFile(String name, File file) {
        Preconditions.checkNotNull(name, "name");
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

    private static void checkValidOutputDir(String name, File file) {
        Preconditions.checkNotNull(name, "name");
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

    Charset getCharset() {
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

    /*
     * 
     * 
     * 
     */
    /*
     * 
     * 
     * 
     */

    /*
     * Log (at info level) the general configuration settings for the current run; including
     * global settings, and relevant Java properties.
     */
    private void logGeneralConfiguration(final long startTime) {
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(format(" * Input instances file: {0}\n",
                             instancesFile));
            sb.append(format(" * Output directory: {0} ({1} free)\n", outputDir,
                             MiscUtil.humanReadableBytes(outputDir.getFreeSpace())));
            sb.append(format(" * Temporary directory: {0} ({1} free)\n",
                             tempBaseDir,
                             MiscUtil.humanReadableBytes(tempBaseDir.getFreeSpace())));
            sb.append(format(" * Character encoding: {0}\n",
                             getCharset()));
            sb.append(format(" * Num. Threads: {0}\n", numThreads));
            sb.append(format(" * {0}\n",
                             MiscUtil.memoryInfoString()));
            sb.append(format(" * Java Spec: {0} {1}, {2}\n",
                             System.getProperty("java.specification.name"),
                             System.getProperty("java.specification.version"),
                             System.getProperty("java.specification.vendor")));
            sb.append(format(" * Java VM: {0} {1}, {2}\n",
                             System.getProperty("java.vm.name"),
                             System.getProperty("java.vm.version"),
                             System.getProperty("java.vm.vendor")));
            sb.append(format(" * Java Runtime: {0} {1}\n",
                             System.getProperty("java.runtime.name"),
                             System.getProperty("java.runtime.version")));
            sb.append(format(" * OS: {0} {1} {2}\n",
                             System.getProperty("os.name"),
                             System.getProperty("os.version"),
                             System.getProperty("os.arch")));
            sb.append(" * Running stages: ").append(stagesToRun).append(NL);

            sb.append(NL);
            LOG.info(sb.toString());
        }
    }

    private void logStageEnd(long startTime, long endTime) {
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageEnd(startTime, endTime, sb);
            sb.append(NL);
            LOG.info(sb.toString());
        }
    }

    private void logStagStart(long startTime) {
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            appendStageStart(startTime, sb);
            sb.append(NL);
            LOG.info(sb.toString());
        }
    }

    private void appendStageEnd(long startTime, long endTime, StringBuilder sb) {
        sb.append("\nStats:\n");
        appendTime(" * End time: ", endTime, NL, sb);
        sb.append(format(" * Elapsed time: {0}\n",
                         formatElapsedTime(endTime - startTime)));
        sb.append(format(" * {0}\n",
                         MiscUtil.memoryInfoString()));
    }

    private void appendStageStart(long startTime, StringBuilder sb) {
        sb.append("\nConfiguration:\n");
        appendTime(" * Start time: ", startTime, NL, sb);

        sb.append(format(" * {0}\n",
                         MiscUtil.memoryInfoString()));
    }
    private static final MessageFormat TIMESTAMP_FORMAT =
            new MessageFormat("{0,time,full} {0,date,full}");

    private void appendTime(String prefix, long time, String suffix, StringBuilder out) {
        // Due to the MessageFormat interface being insane we are required to work with either
        // a synchronized StringBuffer or a totally unnecessary AttributedCharacterIterator. Nice.
        StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);
        TIMESTAMP_FORMAT.format(new Object[]{(Long) time}, buffer, null);
        buffer.append(suffix);
        out.append(buffer);
    }

    private static String formatElapsedTime(final long timeMillis) {
        long remaining = timeMillis;
        final double seconds = (remaining % 60000) / 1000.0;
        remaining /= 60000;
        final int minutes = (int) remaining % 60;
        remaining /= 60;
        final long hours = remaining;
        return String.format("%02d:%02d:%02.4f", hours, minutes, seconds);
    }

    private static void deleteTempDir(File tempDir, String taskName) {
        if (!tempDir.delete()) {
            LOG.warn(format("Unable to delete temporary directory for task {1}: {0}",
                            tempDir, taskName));
            if (tempDir.list().length > 0) {
                LOG.warn(format("Directory is not empty: {0}; contains {1}",
                                tempDir, Arrays.toString(tempDir.list())));
            }
            if (tempDir.getParentFile() != null && !tempDir.getParentFile().
                    canWrite()) {
                LOG.warn(format("Insufficient permissions to delete {0}",
                                tempDir));
            }
        }
    }
}

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

package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Function;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.Predicates2;
import uk.ac.susx.mlcl.lib.collect.IntBitSet;
import uk.ac.susx.mlcl.lib.commands.*;
import uk.ac.susx.mlcl.lib.events.ProgressDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressEvent;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.text.MessageFormat.format;
import static uk.ac.susx.mlcl.lib.Predicates2.*;

/**
 * TODO: Efficiency improvements could be found be combining predicates more
 * intelligently. If, for e.g, one predicate was found to be implied by another
 * then only the stronger need be taken.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Filter a set of frequency files")
public class FilterCommand extends AbstractCommand implements
        ProgressReporting {

    private static final Log LOG = LogFactory.getLog(FilterCommand.class);

    /**
     * Number of records to read or write between progress updates.
     */
    private static final int PROGRESS_INTERVAL = 1000000;

    public static final String FILTERED_STRING = "___FILTERED___";

    public static final int FILTERED_ID = 0;

    @ParametersDelegate
    private DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

    private final ProgressDelegate progress = new ProgressDelegate(this, true);
    /*
      * === INPUT FILES ===
      */

    @Parameter(names = {"-iv", "--input-events"}, required = true,
            description = "Input event frequencies file.", validateWith = InputFileValidator.class)
    private File inputEventsFile;

    @Parameter(names = {"-ie", "--input-entries"}, required = true,
            description = "Input entry frequencies file.", validateWith = InputFileValidator.class)
    private File inputEntriesFile;

    @Parameter(names = {"-if", "--input-features"}, required = true,
            description = "Input features frequencies file.", validateWith = InputFileValidator.class)
    private File inputFeaturesFile;
    /*
      * === OUTPUT FILES ===
      */

    @Parameter(names = {"-ov", "--output-events"}, required = true,
            description = "Output event frequencies file.", validateWith = OutputFileValidator.class)
    private File outputEventsFile;

    @Parameter(names = {"-oe", "--output-entries"}, required = true,
            description = "Output entry frequencies file", validateWith = OutputFileValidator.class)
    private File outputEntriesFile;

    @Parameter(names = {"-of", "--output-features"}, required = true,
            description = "Output features frequencies file.", validateWith = OutputFileValidator.class)
    private File outputFeaturesFile;

    @ParametersDelegate
    private final FileDelegate fileDelegate = new FileDelegate();

    /*
      * === FILTER PARAMATERISATION ===
      */
    @Parameter(names = {"-fef", "--filter-entry-freq"},
            description = "Minimum entry pair frequency threshold.", converter = DoubleConverter.class)
    private double filterEntryMinFreq;

    @Parameter(names = {"-few", "--filter-entry-whitelist"},
            description = "Whitelist file containing entries of interest. (All others will be ignored)",
            validateWith = InputFileValidator.class)
    private File filterEntryWhitelist;

    @Parameter(names = {"-fep", "--filter-entry-pattern"},
            description = "Regular expression that accepted entries must match.")
    private String filterEntryPattern;

    @Parameter(names = {"-fvf", "--filter-event-freq"},
            description = "Minimum event frequency threshold.", converter = DoubleConverter.class)
    private double filterEventMinFreq;

    @Parameter(names = {"-fff", "--filter-feature-freq"},
            description = "Minimum feature pair frequency threshold.", converter = DoubleConverter.class)
    private double filterFeatureMinFreq;

    @Parameter(names = {"-ffw", "--filter-feature-whitelist"},
            description = "Whitelist file containing features of interest. (All others will be ignored)",
            validateWith = InputFileValidator.class)
    private File filterFeatureWhitelist;

    @Parameter(names = {"-ffp", "--filter-feature-pattern"},
            description = "Regular expression that accepted features must match.")
    private String filterFeaturePattern;

    @Parameter(names = {"-T", "--temp-dir"},
            description = "Temporary directory which will be used during filtering.",
            converter = TempFileFactoryConverter.class)
    private FileFactory tempFiles = new TempFileFactory();

    /*
      * === INTERNAL ===
      */
    private Predicate<Weighted<Token>> acceptEntries = alwaysTrue();

    private Predicate<Weighted<TokenPair>> acceptEvents = alwaysTrue();

    private Predicate<Weighted<Token>> acceptFeatures = alwaysTrue();

    private boolean entryFilterRequired = false;

    private boolean eventFilterRequired = false;

    private boolean featureFilterRequired = false;

    private File activeEventsFile;

    private File activeEntriesFile;

    private File activeFeaturesFile;

    // Store the entry and feature blacklists globally because we need to add
    // to it throughout the process.
    private final IntSortedSet entryBlacklist;
    private final IntSortedSet featureBlacklist;

    public FilterCommand() {
        featureBlacklist = newIntSet(1 << 16);
        entryBlacklist = newIntSet(1 << 16);
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(), compose(not(in(featureBlacklist)), id())));
        setAcceptEntries(Predicates2.<Weighted<Token>>and(getAcceptEntries(),
                compose(not(in(entryBlacklist)), id())));
        setAcceptEvent(Predicates2.<Weighted<TokenPair>>and(acceptEvents,
                compose(not(in(entryBlacklist)), eventEntryId()),
                compose(not(in(featureBlacklist)), eventFeatureId())));
    }

    public FilterCommand(File inputEventsFile, File inputEntriesFile,
                         File inputFeaturesFile, File outputEventsFile,
                         File outputEntriesFile, File outputFeaturesFile, Charset charset) {
        this();
        setCharset(charset);
        setInputFeaturesFile(inputFeaturesFile);
        setInputEventsFile(inputEventsFile);
        setInputEntriesFile(inputEntriesFile);
        setOutputFeaturesFile(outputFeaturesFile);
        setOutputEventsFile(outputEventsFile);
        setOutputEntriesFile(outputEntriesFile);
    }

    public final DoubleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    public final void setIndexDelegate(DoubleEnumerating indexDelegate) {
        this.indexDelegate = indexDelegate;
    }

    @Override
    public void runCommand() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running filtering.");
        if (LOG.isDebugEnabled())
            LOG.debug(this);

        if (filterFeatureMinFreq > 0) {
            addFeaturesMinimumFrequency(filterFeatureMinFreq);
        }
        if (filterFeaturePattern != null) {
            addFeaturesPattern(filterFeaturePattern);
        }
        if (filterFeatureWhitelist != null) {
            addFeaturesWhitelist(com.google.common.io.Files.readLines(
                    filterFeatureWhitelist, getCharset()));
        }

        if (filterEntryMinFreq > 0) {
            addEntryMinimumFrequency(filterEntryMinFreq);
        }
        if (filterEntryPattern != null) {
            addEntryPattern(filterEntryPattern);
        }
        if (filterEntryWhitelist != null) {
            addEntryWhitelist(com.google.common.io.Files.readLines(
                    filterEntryWhitelist, getCharset()));
        }

        if (filterEventMinFreq > 0) {
            addEventMinimumFrequency(filterEventMinFreq);
        }

        checkState();
        activeEventsFile = inputEventsFile;
        activeEntriesFile = inputEntriesFile;
        activeFeaturesFile = inputFeaturesFile;

        progress.addProgressListener(new ProgressListener() {

            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                LOG.info(progressEvent.getSource().getProgressReport());
            }

        });

        progress.setState(State.RUNNING);
        progress.setProgressPercent(0);

        // Run the filters forwards then backwards. Each filtering step may
        // introduce additionally filters for the other files, so continue
        // looping until there is no work remaining. Depending on filters this
        // very unlikely to take more than 3 passes

        int passCount = 0;
        int opCount = 0;

        while (entryFilterRequired || eventFilterRequired
                || featureFilterRequired) {

            progress.setMessage("Running filtering pass (#" + (++passCount)
                    + ").");

            if (entryFilterRequired) {
                filterEntries();
                ++opCount;
                progress.setProgressPercent(100
                        * opCount
                        / (opCount + 3 + (entryFilterRequired ? 1 : 0)
                        + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                        : 0)));
            }

            if (eventFilterRequired) {
                filterEvents();
                ++opCount;
                progress.setProgressPercent(100
                        * opCount
                        / (opCount + 3 + (entryFilterRequired ? 1 : 0)
                        + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                        : 0)));
            }

            if (featureFilterRequired) {
                filterFeatures();
                ++opCount;
                progress.setProgressPercent(100
                        * opCount
                        / (opCount + 3 + (entryFilterRequired ? 1 : 0)
                        + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                        : 0)));
            }

            if (eventFilterRequired) {
                filterEvents();
                ++opCount;
                progress.setProgressPercent(100
                        * opCount
                        / (opCount + 3 + (entryFilterRequired ? 1 : 0)
                        + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                        : 0)));
            }

            if (entryFilterRequired) {
                filterEntries();
                ++opCount;
                progress.setProgressPercent(100
                        * opCount
                        / (opCount + 3 + (entryFilterRequired ? 1 : 0)
                        + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                        : 0)));
            }
        }

        // Finished filtering so copy the results files to the outputs.

        progress.setMessage("Copying final entries file.");

        finaliseFile(inputEntriesFile, activeEntriesFile, outputEntriesFile);
        ++opCount;

        progress.startAdjusting();
        progress.setProgressPercent(100
                * opCount
                / (opCount + 2 + (entryFilterRequired ? 1 : 0)
                + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                : 0)));
        progress.setMessage("Copying finally events file.");
        progress.endAdjusting();

        finaliseFile(inputEventsFile, activeEventsFile, outputEventsFile);
        ++opCount;

        progress.startAdjusting();
        progress.setProgressPercent(100
                * opCount
                / (opCount + 1 + (entryFilterRequired ? 1 : 0)
                + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                : 0)));
        progress.setMessage("Copying final features file.");
        progress.endAdjusting();

        finaliseFile(inputFeaturesFile, activeFeaturesFile, outputFeaturesFile);

        ++opCount;
        progress.setProgressPercent(100
                * opCount
                / (opCount + 0 + (entryFilterRequired ? 1 : 0)
                + (eventFilterRequired ? 1 : 0) + (featureFilterRequired ? 1
                : 0)));

        if (indexDelegate.isEnumeratorOpen()) {
            indexDelegate.saveEnumerator();
            indexDelegate.closeEnumerator();
        }

        progress.setState(State.COMPLETED);
    }

    private static void finaliseFile(File inputFile, File activeFile,
                                     File outputFile) throws IOException {
        assert inputFile != null : "inputFile is null";
        assert activeFile != null : "activeFile is null";
        assert outputFile != null : "outputFile is null";
        assert inputFile.exists() : "inputFile does not exist";
        assert activeFile.exists() : "activeFile does not exist";

        // Output file can't just be written over because we are going to try
        // and rename to it, so delete it first if it exists.
        if (outputFile.exists() && !outputFile.delete())
            throw new IOException("Failed to delete output entries file: "
                    + outputFile);

        assert !outputFile.exists() : "outputFile already exists";

        // Move the active to the output file. First try renaming which is much
        // faster but only works if both are on the same file system. Otherwise
        // fall back to a copy then delete.
        if (activeFile.equals(inputFile)) {
            com.google.common.io.Files.copy(inputFile, outputFile);
        } else if (!activeFile.renameTo(outputFile)) {
            com.google.common.io.Files.copy(activeFile, outputFile);
            if (!activeFile.equals(inputFile)) {
                if (activeFile.delete())
                    throw new IOException("Failed to delete active file: "
                            + activeFile);
            }
        }

        assert inputFile.exists() : "inputFile does not exist";
        assert outputFile.exists() : "outputFile does not exist";
    }

    // Read the entries file, passing it thought the filter. accepted entries
    // are written out to the output file while rejected entries are stored
    // for filtering the AllPairsTask.

    private void filterEntries() throws FileNotFoundException, IOException {

//        final IntSet rejected = newIntSet();


        WeightedTokenSource entriesSource = BybloIO.openEntriesSource(
                activeEntriesFile, getCharset(), getIndexDelegate());

        File outputFile = tempFiles.createFile();

        WeightedTokenSink entriesSink = BybloIO.openEntriesSink(outputFile,
                getCharset(), getIndexDelegate());

        progress.setMessage("Filtering entries.");

        final int filteredEntry = getIndexDelegate().getEntryEnumerator()
                .indexOf(FILTERED_STRING);
        double filteredWeight = 0;

        long inCount = 0;
        long outCount = 0;
        while (entriesSource.hasNext()) {
            ++inCount;
            Weighted<Token> record = entriesSource.read();

            if (record.record().id() == filteredEntry) {
                filteredWeight += record.weight();
            } else if (acceptEntries.apply(record)) {
                entriesSink.write(record);
                ++outCount;
            } else {
                eventFilterRequired = entryBlacklist.add(record.record().id()) || eventFilterRequired;
                filteredWeight += record.weight();
            }

            if ((inCount % PROGRESS_INTERVAL == 0 || !entriesSource.hasNext())
                    && LOG.isInfoEnabled()) {
                progress.setMessage(format("Accepted {0} of {1} entries.",
                        outCount, inCount));
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        if (filteredWeight != 0) {
            entriesSink.write(new Weighted<Token>(new Token(filteredEntry),
                    filteredWeight));
        }

        entriesSource.close();
        entriesSink.flush();
        entriesSink.close();

        if (!activeEntriesFile.equals(inputEntriesFile)) {
            activeEntriesFile.delete();
        }

        entryFilterRequired = false;
        activeEntriesFile = outputFile;
//
//        // Update the feature acceptance predicate
//        if (rejected.size() > 0) {
//            eventFilterRequired = true;
////            entryBlacklist.addAll(rejected);
//        }

    }

    // Filter the AllPairsTask file, rejecting all events that contain entries
    // dropped in the entries file filter pass. Store a list of features that
    // only appear in filtered entries to filter the features file.
    private void filterEvents() throws FileNotFoundException, IOException {

        // We wish to know which entries where *always* rejected, but this
        // information is not available locally because we are streaming the
        // events. To solve this we keep track of entries that where ever
        // accepted. At then end, those entries  that where *only* ever
        // rejected are *always* reject, so should be
        // removed from the entries file.

        final IntSet everAcceptedFeatures = newIntSet(1 << 8);
        int minFeatureId = Integer.MAX_VALUE;
        int maxFeatureId = Integer.MIN_VALUE;

        final IntSet everAcceptedEntries = newIntSet(1 << 8);
        int minEntryId = Integer.MAX_VALUE;
        int maxEntryId = Integer.MIN_VALUE;


        final WeightedTokenPairSource efSrc = BybloIO.openEventsSource(activeEventsFile, getCharset(), indexDelegate);

        File outputFile = tempFiles.createFile();

        WeightedTokenPairSink efSink = BybloIO.openEventsSink(outputFile, getCharset(), indexDelegate);

        progress.setMessage("Filtering events from.");

        // Store the id of the special filtered feature and entry
        // TODO This can probably be removed now but need to check
        final int filteredEntry = getIndexDelegate().getEntryEnumerator().indexOf(FILTERED_STRING);
        final int filteredFeature = getIndexDelegate().getFeatureEnumerator().indexOf(FILTERED_STRING);

        int currentEntryId = -1;
        int currentEventCount = 0;
        double currentEntryFilteredFeatureWeight = 0;

        double filteredEntryWeight = 0;

        int readCount = 0;
        int writeCount = 0;

        while (efSrc.hasNext()) {
            final Weighted<TokenPair> record = efSrc.read();
            final int entryId = record.record().id1();
            final int featureId = record.record().id2();
            ++readCount;

            if (featureId < minFeatureId)
                minFeatureId = featureId;
            if (featureId > maxFeatureId)
                maxFeatureId = featureId;

            if (entryId == filteredEntry) {
                filteredEntryWeight += record.weight();
                continue;
            }

            if (entryId != currentEntryId) {
                if (entryId < minEntryId)
                    minEntryId = entryId;
                if (entryId > maxEntryId)
                    maxEntryId = entryId;

                if (currentEntryId != -1 && currentEntryFilteredFeatureWeight != 0) {
                    if (currentEventCount == 0) {
                        filteredEntryWeight += currentEntryFilteredFeatureWeight;
                    } else {
                        efSink.write(new Weighted<TokenPair>(new TokenPair(
                                currentEntryId, filteredFeature),
                                currentEntryFilteredFeatureWeight));
                        ++writeCount;
                    }
                }

                currentEntryId = entryId;
                currentEntryFilteredFeatureWeight = 0;
                currentEventCount = 0;
            }

            if (featureId == filteredFeature) {

                currentEntryFilteredFeatureWeight += record.weight();

            } else if (acceptEvents.apply(record)) {

                efSink.write(record);
                ++writeCount;

                everAcceptedEntries.add(entryId);
                everAcceptedFeatures.add(featureId);
                ++currentEventCount;


            } else {

                currentEntryFilteredFeatureWeight += record.weight();
            }

            if ((readCount % PROGRESS_INTERVAL == 0 || !efSrc.hasNext()) && LOG.isInfoEnabled()) {
                progress.setMessage("Accepted " + writeCount + " of " + readCount + " events.");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        if (currentEntryId != -1 && currentEntryFilteredFeatureWeight != 0) {
            if (currentEventCount == 0) {
                filteredEntryWeight += currentEntryFilteredFeatureWeight;
            } else {
                efSink.write(new Weighted<TokenPair>(new TokenPair(
                        currentEntryId, filteredFeature),
                        currentEntryFilteredFeatureWeight));
            }
        }

        // If there have been entire entries filtered then write their summed
        // weights to a special filtered entry/feature pair
        if (filteredEntryWeight != 0) {
            efSink.write(new Weighted<TokenPair>(new TokenPair(filteredEntry, filteredFeature), filteredEntryWeight));
        }

        efSrc.close();
        efSink.flush();
        efSink.close();

        if (!activeEventsFile.equals(inputEventsFile)) {
            activeEventsFile.delete();
        }

        eventFilterRequired = false;
        activeEventsFile = outputFile;

        {
            IntBitSet alwaysRejectedEntries = IntBitSet.allOfRange(minEntryId, maxEntryId);
            alwaysRejectedEntries.removeAll(everAcceptedEntries);
            entryFilterRequired = entryBlacklist.addAll(alwaysRejectedEntries) | entryFilterRequired;
        }

        {
            IntBitSet alwaysRejectedFeature = IntBitSet.allOfRange(minFeatureId, maxFeatureId);
            alwaysRejectedFeature.removeAll(everAcceptedFeatures);
            featureFilterRequired = featureBlacklist.addAll(alwaysRejectedFeature) | featureFilterRequired;
        }

    }

    // Filter the AllPairsTask file, rejecting all entries that where found to
    // be only used by filtered entries.
    private void filterFeatures() throws FileNotFoundException, IOException {

        WeightedTokenSource featureSource = BybloIO.openFeaturesSource(
                activeFeaturesFile, getCharset(), indexDelegate);

        File outputFile = tempFiles.createFile();

        WeightedTokenSink featureSink = BybloIO.openFeaturesSink(outputFile,
                getCharset(), indexDelegate);

        progress.setMessage("Filtering features.");

        // Store an filtered weight here and record it so as to maintain
        // accurate priors for those features that remain
        double filteredWeight = 0;
        int filteredId = getIndexDelegate().getFeatureEnumerator().indexOf(
                FILTERED_STRING);

        long inCount = 0;
        long outCount = 0;
        while (featureSource.hasNext()) {
            Weighted<Token> feature = featureSource.read();
            ++inCount;

            if (feature.record().id() == filteredId) {
                filteredWeight += feature.weight();
            } else if (acceptFeatures.apply(feature)) {
                featureSink.write(feature);
                ++outCount;
            } else {
                eventFilterRequired = featureBlacklist.add(feature.record().id()) || eventFilterRequired;
                filteredWeight += feature.weight();
            }

            if ((inCount % PROGRESS_INTERVAL == 0 || !featureSource.hasNext())
                    && LOG.isInfoEnabled()) {
                progress.setMessage(format("Accepted {0} of {1} features.",
                        outCount, inCount));
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        if (filteredWeight != 0) {
            featureSink.write(new Weighted<Token>(new Token(filteredId),
                    filteredWeight));
        }
        featureSource.close();
        featureSink.flush();
        featureSink.close();

        if (!activeFeaturesFile.equals(inputFeaturesFile)) {
            activeFeaturesFile.delete();
        }

        featureFilterRequired = false;
        activeFeaturesFile = outputFile;
    }

    public final File getInputFeaturesFile() {
        return inputFeaturesFile;
    }

    public final void setInputFeaturesFile(File inputFeaturesFile) {
        this.inputFeaturesFile = checkNotNull(inputFeaturesFile);
    }

    public final File getInputEventsFile() {
        return inputEventsFile;
    }

    public final void setInputEventsFile(File inputEventsFile) {
        this.inputEventsFile = checkNotNull(inputEventsFile);
    }

    public final File getInputEntriesFile() {
        return inputEntriesFile;
    }

    public final void setInputEntriesFile(File inputEntriesFile) {
        this.inputEntriesFile = checkNotNull(inputEntriesFile);
    }

    public final File getOutputFeaturesFile() {
        return outputFeaturesFile;
    }

    public final void setOutputFeaturesFile(File outputFeaturesFile) {
        this.outputFeaturesFile = checkNotNull(outputFeaturesFile);
    }

    public final File getOutputEventsFile() {
        return outputEventsFile;
    }

    public final void setOutputEventsFile(File outputEventsFile) {
        this.outputEventsFile = checkNotNull(outputEventsFile);
    }

    public final File getOutputEntriesFile() {
        return outputEntriesFile;
    }

    public final void setOutputEntriesFile(File outputEntriesFile) {
        this.outputEntriesFile = checkNotNull(outputEntriesFile);
    }

    public Predicate<Weighted<Token>> getAcceptFeatures() {
        return acceptFeatures;
    }

    public void setAcceptFeatures(Predicate<Weighted<Token>> acceptFeature) {
        if (!acceptFeature.equals(this.acceptFeatures)) {
            this.acceptFeatures = acceptFeature;
            featureFilterRequired = true;
        }
    }

    public void addFeaturesMinimumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                Predicates2.compose(Predicates2.gte(threshold),
                        this.<Token>weight())));
    }

    public void addFeaturesMaximumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                Predicates2.compose(Predicates2.lte(threshold),
                        this.<Token>weight())));
    }

    public void addFeaturesFrequencyRange(double min, double max) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                Predicates2.compose(Predicates2.inRange(min, max),
                        this.<Token>weight())));
    }

    public void addFeaturesPattern(String pattern) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(), Predicates2.compose(
                Predicates2.containsPattern(pattern), featureString())));
    }

    public void addFeaturesWhitelist(List<String> strings) throws IOException {
        IntSet featureIdSet = toEnumeratedIntSet(strings, getIndexDelegate()
                .getFeatureEnumerator());
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                Predicates2.compose(Predicates2.in(featureIdSet), id())));
    }

    public void addFeaturesBlacklist(List<String> strings) throws IOException {
        IntSet featureIdSet = toEnumeratedIntSet(strings, getIndexDelegate()
                .getFeatureEnumerator());
        featureBlacklist.addAll(featureIdSet);
    }

    public Predicate<Weighted<TokenPair>> getAcceptEvent() {
        return acceptEvents;
    }

    public void setAcceptEvent(Predicate<Weighted<TokenPair>> acceptFeature) {
        if (!acceptFeature.equals(this.acceptEvents)) {
            this.acceptEvents = acceptFeature;
            eventFilterRequired = true;
        }
    }

    public void addEventMinimumFrequency(double threshold) {
        setAcceptEvent(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEvent(),
                Predicates2.compose(Predicates2.gte(threshold),
                        this.<TokenPair>weight())));
    }

    public void addEventMaximumFrequency(double threshold) {
        setAcceptEvent(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEvent(),
                Predicates2.compose(Predicates2.lte(threshold),
                        this.<TokenPair>weight())));
    }

    public void addEventFrequencyRange(double min, double max) {
        setAcceptEvent(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEvent(),
                Predicates2.compose(Predicates2.inRange(min, max),
                        this.<TokenPair>weight())));
    }

    public Predicate<Weighted<Token>> getAcceptEntries() {
        return acceptEntries;
    }

    public void setAcceptEntries(Predicate<Weighted<Token>> acceptEntry) {
        if (!acceptEntry.equals(this.acceptEntries)) {
            this.acceptEntries = acceptEntry;
            entryFilterRequired = true;
        }
    }

    public void addEntryMinimumFrequency(double threshold) {
        setAcceptEntries(Predicates2.<Weighted<Token>>and(
                getAcceptEntries(),
                Predicates2.compose(Predicates2.gte(threshold),
                        this.<Token>weight())));
    }

    public void addEntryMaximumFrequency(double threshold) {
        setAcceptEntries(Predicates2.<Weighted<Token>>and(
                getAcceptEntries(),
                Predicates2.compose(Predicates2.lte(threshold),
                        this.<Token>weight())));
    }

    public void addEntryFrequencyRange(double min, double max) {
        setAcceptEntries(Predicates2.<Weighted<Token>>and(
                getAcceptEntries(),
                Predicates2.compose(Predicates2.inRange(min, max),
                        this.<Token>weight())));
    }

    public void addEntryPattern(String pattern) {
        setAcceptEntries(Predicates2.<Weighted<Token>>and(getAcceptEntries(),
                Predicates2.compose(Predicates2.containsPattern(pattern),
                        entryString())));
    }

    public void addEntryWhitelist(List<String> strings) throws IOException {
        IntSet entryIdSet = toEnumeratedIntSet(strings, getIndexDelegate()
                .getEntryEnumerator());
        setAcceptEntries(Predicates2.<Weighted<Token>>and(getAcceptEntries(),
                Predicates2.compose(Predicates2.in(entryIdSet), id())));

    }

    public void addEntryBlacklist(List<String> strings) throws IOException {
        IntSet entryIdSet = toEnumeratedIntSet(strings, getIndexDelegate()
                .getEntryEnumerator());
        entryBlacklist.addAll(entryIdSet);
    }

    /**
     * Convert a list of collection of strings to an set of integer enumerations
     * in the list.
     *
     * @param strings
     * @param idx
     * @return
     */
    static IntSet toEnumeratedIntSet(final Collection<String> strings,
                                     final Enumerator<String> idx) {
        final IntSet intSet = newIntSet(1 << 16);
        for (String string : strings) {
            intSet.add(idx.indexOf(string));
        }
        return intSet;
    }

    /**
     * Instantiate a new IntSet. Note that this is abstract to a factory method
     * so it's easier to change the implementation.
     *
     * @return
     */
    static IntSortedSet newIntSet(final int largestElementHint) {
        return new IntBitSet(largestElementHint);
    }

    /**
     * Method that performance a number of sanity checks on the parameterisation
     * of this class. It is necessary to do this because the the class can be
     * instantiated via a null constructor when run from the command line.
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private void checkState() throws NullPointerException,
            IllegalStateException, FileNotFoundException {

        final Map<String, File> inputFiles = new HashMap<String, File>();
        inputFiles.put("inputEntries", inputEntriesFile);
        inputFiles.put("inputFeatures", inputFeaturesFile);
        inputFiles.put("inputEvents", inputEventsFile);

        final Map<String, File> outputFiles = new HashMap<String, File>();
        outputFiles.put("outputEntries", outputEntriesFile);
        outputFiles.put("outputFeatures", outputFeaturesFile);
        outputFiles.put("outputEvents", outputEventsFile);

        final Map<String, File> allFiles = new HashMap<String, File>();
        allFiles.putAll(inputFiles);
        allFiles.putAll(outputFiles);

        // Check non of the parameters are null
        for (Map.Entry<String, File> entry : allFiles.entrySet()) {
            if (entry.getValue() == null) {
                throw new NullPointerException(entry.getKey() + " is null");
            }
        }
        if (getCharset() == null) {
            throw new NullPointerException("charset is null");
        }

        // Check that no two files are the same
        for (Map.Entry<String, File> a : allFiles.entrySet()) {
            for (Map.Entry<String, File> b : allFiles.entrySet()) {
                if (!a.getKey().equals(b.getKey())
                        && a.getValue().equals(b.getValue())) {
                    throw new IllegalStateException(a.getKey() + " equal to "
                            + b.getKey());
                }
            }
        }

        // Check that the input files exists and is readable
        for (Map.Entry<String, File> entry : inputFiles.entrySet()) {
            if (!entry.getValue().exists()) {
                throw new FileNotFoundException(entry.getKey()
                        + " does not exist: " + entry.getValue());
            }
            if (!entry.getValue().isFile()) {
                throw new IllegalStateException(entry.getKey()
                        + " is not a normal data file: " + entry.getValue());
            }
            if (!entry.getValue().canRead()) {
                throw new IllegalStateException(entry.getKey()
                        + " is not readable: " + entry.getValue());
            }
        }

        // For each output file, check that either it exists and it writable,
        // or that it does not exist but is creatable
        for (Map.Entry<String, File> e : outputFiles.entrySet()) {
            if (e.getValue().exists()
                    && (!e.getValue().isFile() || !e.getValue().canWrite())) {
                throw new IllegalStateException(e.getKey()
                        + " exists but is not writable: " + e.getValue());
            }
            if (!e.getValue().exists()
                    && !e.getValue().getAbsoluteFile().getParentFile()
                    .canWrite()) {
                throw new IllegalStateException(e.getKey()
                        + " does not exists and can not be created: "
                        + e.getValue());
            }

        }
    }

    //
    // ==== FIELD EXTRACTION FUNCTIONS ====
    //
    private <T> Function<Weighted<T>, Double> weight() {
        return new Function<Weighted<T>, Double>() {

            @Override
            public Double apply(Weighted<T> input) {
                return input.weight();
            }

            @Override
            public String toString() {
                return "Weight";
            }

        };
    }

    private <T> Function<Weighted<Token>, Integer> id() {
        return new Function<Weighted<Token>, Integer>() {

            @Override
            public Integer apply(Weighted<Token> input) {
                return input.record().id();
            }

            @Override
            public String toString() {
                return "ID";
            }

        };
    }

    private Function<Weighted<Token>, String> entryString() {
        return new Function<Weighted<Token>, String>() {

            @Override
            public String apply(Weighted<Token> input) {
                try {
                    return getIndexDelegate().getEntryEnumerator().valueOf(
                            input.record().id());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String toString() {
                return "EntriesString";
            }

        };
    }

    private Function<Weighted<Token>, String> featureString() {
        return new Function<Weighted<Token>, String>() {

            @Override
            public String apply(Weighted<Token> input) {
                try {
                    return getIndexDelegate().getFeatureEnumerator().valueOf(
                            input.record().id());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String toString() {
                return "FeatureString";
            }

        };
    }

    private Function<Weighted<TokenPair>, Integer> eventEntryId() {
        return new Function<Weighted<TokenPair>, Integer>() {

            @Override
            public Integer apply(Weighted<TokenPair> input) {
                return input.record().id1();
            }

            @Override
            public String toString() {
                return "FeatureEntryID";
            }

        };
    }

    private Function<Weighted<TokenPair>, Integer> eventFeatureId() {
        return new Function<Weighted<TokenPair>, Integer>() {

            @Override
            public Integer apply(Weighted<TokenPair> input) {
                return input.record().id2();
            }

            @Override
            public String toString() {
                return "EventID";
            }

        };
    }

    private Function<Weighted<TokenPair>, String> eventFeatureString() {
        return new Function<Weighted<TokenPair>, String>() {

            @Override
            public String apply(Weighted<TokenPair> input) {
                try {
                    return getIndexDelegate().getFeatureEnumerator().valueOf(
                            input.record().id2());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String toString() {
                return "EventFeatureString";
            }

        };
    }

    private Function<Weighted<TokenPair>, String> eventEntryString() {
        return new Function<Weighted<TokenPair>, String>() {

            @Override
            public String apply(Weighted<TokenPair> input) {
                try {
                    return getIndexDelegate().getEntryEnumerator().valueOf(
                            input.record().id1());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public String toString() {
                return "EventEntryString";
            }

        };
    }

    public double getFilterEventMinFreq() {
        return filterEventMinFreq;
    }

    public void setFilterEventMinFreq(double filterEventMinFreq) {
        this.filterEventMinFreq = filterEventMinFreq;
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

    public void setFeatureEnumeratorFile(File featureEnumeratorFile) {
        indexDelegate.setFeatureEnumeratorFile(featureEnumeratorFile);
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDelegate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDelegate.setEnumeratedEntries(enumeratedEntries);
    }

    public void setEntryEnumeratorFile(File entryEnumeratorFile) {
        indexDelegate.setEntryEnumeratorFile(entryEnumeratorFile);
    }

    public boolean isEnumeratedFeatures() {
        return indexDelegate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDelegate.isEnumeratedEntries();
    }

    public File getFeatureEnumeratorFile() {
        return indexDelegate.getFeatureEnumeratorFile();
    }

    public File getEntryEnumeratorFile() {
        return indexDelegate.getEntryEnumeratorFile();
    }

    public final void setCharset(Charset charset) {
        fileDelegate.setCharset(charset);
    }

    public final Charset getCharset() {
        return fileDelegate.getCharset();
    }

    public FileFactory getTempFiles() {
        return tempFiles;
    }

    public void setTempFiles(FileFactory tempFiles) {
        this.tempFiles = tempFiles;
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDelegate.setEnumeratorType(type);
    }

    public EnumeratorType getEnumeratorType() {
        return indexDelegate.getEnumeratorType();
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    @Override
    public State getState() {
        return progress.getState();
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("eventsIn", inputEventsFile)
                .add("entriesIn", inputEntriesFile)
                .add("featuresIn", inputFeaturesFile)
                .add("eventsOut", outputEventsFile)
                .add("entriesOut", outputEntriesFile)
                .add("featuresOut", outputFeaturesFile)
                .add("charset", getCharset())
                .add("entryMinFreq", filterEntryMinFreq)
                .add("entryWhitelist", filterEntryWhitelist)
                .add("entryPattern", filterEntryPattern)
                .add("eventMinFreq", filterEventMinFreq)
                .add("featureMinFreq", filterFeatureMinFreq)
                .add("featureWhitelist", filterFeatureWhitelist)
                .add("featurePattern", filterFeaturePattern)
                .add("tmp", tempFiles).add("acceptEntry", acceptEntries)
                .add("acceptFeature", acceptFeatures)
                .add("acceptEvent", acceptEvents);
    }

}

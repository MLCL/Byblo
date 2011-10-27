/*
 * Copyright (c) 2010-2011, University of Sussex
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

import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.tasks.TempFileFactoryConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import com.google.common.base.Predicate;
import static uk.ac.susx.mlcl.lib.Predicates2.*;
import com.google.common.io.Files;
import uk.ac.susx.mlcl.lib.DoubleConverter;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.Predicates2;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractCommandTask;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;

/**
 *
 * TODO: Efficiency improvements could be found be combining predicates more
 *  intelligently. If, for e.g, one predicate was found to be implied by another
 *  then only the stronger need be taken.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Filter a set of frequency files")
public class FilterTask extends AbstractCommandTask implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(FilterTask.class);
    /**
     * Number of records to read or write between progress updates.
     */
    private static final int PROGRESS_INTERVAL = 10000000;
    public static final String FILTERED_STRING = "___FILTERED___";
    /*
     * === INPUT FILES ===
     */
    @Parameter(names = {"-ief", "--input-entry-features"}, required = true,
    description = "Input entry/feature pair frequencies file.")
    private File inputEntryFeaturesFile;
    @Parameter(names = {"-ie", "--input-entries"}, required = true,
    description = "Input entry frequencies file.")
    private File inputEntriesFile;
    @Parameter(names = {"-if", "--input-features"}, required = true,
    description = "Input features frequencies file.")
    private File inputFeaturesFile;
    /*
     * === OUTPUT FILES ===
     */
    @Parameter(names = {"-oef", "--output-entry-features"}, required = true,
    description = "Output entry/feature pair frequencies file.")
    private File outputEntryFeaturesFile;
    @Parameter(names = {"-oe", "--output-entries"}, required = true,
    description = "Output entry frequencies file")
    private File outputEntriesFile;
    @Parameter(names = {"-of", "--output-features"}, required = true,
    description = "Output features frequencies file.")
    private File outputFeaturesFile;

    /*
     * === CHARACTER ENCODING ===
     */
    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding to use for both input and output.")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    /*
     * === FILTER PARAMATERISATION ===
     */
    @Parameter(names = {"-fef", "--filter-entry-freq"},
    description = "Minimum entry pair frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEntryMinFreq;
    @Parameter(names = {"-few", "--filter-entry-whitelist"},
    description = "Whitelist file containing entries of interest. (All others will be ignored)")
    private File filterEntryWhitelist;
    @Parameter(names = {"-fep", "--filter-entry-pattern"},
    description = "Regular expresion that accepted entries must match.")
    private String filterEntryPattern;
    @Parameter(names = {"-feff", "--filter-entry-feature-freq"},
    description = "Minimum entry/feature pair frequency threshold.",
    converter = DoubleConverter.class)
    private double filterEntryFeatureMinFreq;
    @Parameter(names = {"-fff", "--filter-feature-freq"},
    description = "Minimum feature pair frequency threshold.",
    converter = DoubleConverter.class)
    private double filterFeatureMinFreq;
    @Parameter(names = {"-ffw", "--filter-feature-whitelist"},
    description = "Whitelist file containing features of interest. (All others will be ignored)")
    private File filterFeatureWhitelist;
    @Parameter(names = {"-ffp", "--filter-feature-pattern"},
    description = "Regular expresion that accepted features must match.")
    private String filterFeaturePattern;
    @Parameter(names = {"-T", "--temp-dir"},
    description = "Temorary directory which will be used during filtering.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFiles = new TempFileFactory();

    /*
     * === INTERNAL ===
     */
    private Predicate<Weighted<Token>> acceptEntry = alwaysTrue();
    private Predicate<Weighted<TokenPair>> acceptEntryFeature = alwaysTrue();
    private Predicate<Weighted<Token>> acceptFeature = alwaysTrue();
    private boolean entryFilterRequired = false;
    private boolean entryFeatureFilterRequired = false;
    private boolean featureFilterRequired = false;
    final ObjectIndex<String> entryIndex = new ObjectIndex<String>();
    final ObjectIndex<String> featureIndex = new ObjectIndex<String>();
    private File activeEntryFeaturesFile;
    private File activeEntriesFile;
    private File activeFeaturesFile;

    public FilterTask() {
    }

    public FilterTask(
            File inputEntryFeaturesFile, File inputEntriesFile,
            File inputFeaturesFile, File outputEntryFeaturesFile,
            File outputEntriesFile, File outputFeaturesFile,
            Charset charset) {
        setCharset(charset);
        setInputFeaturesFile(inputFeaturesFile);
        setInputEntryFeaturesFile(inputEntryFeaturesFile);
        setInputEntriesFile(inputEntriesFile);
        setOutputFeaturesFile(outputFeaturesFile);
        setOutputEntryFeaturesFile(outputEntryFeaturesFile);
        setOutputEntriesFile(outputEntriesFile);
    }

    @Override
    protected void initialiseTask() throws Exception {

        if (filterFeatureMinFreq > 0) {
            addFeaturesMinimumFrequency(filterFeatureMinFreq);
        }
        if (filterFeaturePattern != null) {
            addFeaturesPattern(filterFeaturePattern);
        }
        if (filterFeatureWhitelist != null) {
            addFeaturesWhitelist(Files.readLines(
                    filterFeatureWhitelist, charset));
        }

        if (filterEntryMinFreq > 0) {
            addEntryMinimumFrequency(filterEntryMinFreq);
        }
        if (filterEntryPattern != null) {
            addEntryPattern(filterEntryPattern);
        }
        if (filterEntryWhitelist != null) {
            addEntryWhitelist(Files.readLines(
                    filterEntryWhitelist, charset));
        }

        if (filterEntryFeatureMinFreq > 0) {
            addEntryFeatureMinimumFrequency(filterEntryFeatureMinFreq);
        }

        checkState();
        activeEntryFeaturesFile = inputEntryFeaturesFile;
        activeEntriesFile = inputEntriesFile;
        activeFeaturesFile = inputFeaturesFile;
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running filtering.");
        }


        // Run the filters forwards then backwards. Each filtering step may
        // introduce additionaly filters for the other files, so continue
        // looping until there is no work remaining. Depending on filters this
        // very unlikely to take more than 3 passes

        int passCount = 0;

        while (entryFilterRequired
                || entryFeatureFilterRequired
                || featureFilterRequired) {

            if (entryFilterRequired || entryFeatureFilterRequired) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                            "Running forwards filtering pass (#" + (++passCount) + ").");
                }

                if (entryFilterRequired) {
                    filterEntries();
                }

                if (entryFeatureFilterRequired) {
                    filterEntryFeatures();
                }

                if (featureFilterRequired) {
                    filterFeatures();
                }
            }

            if (featureFilterRequired || entryFeatureFilterRequired) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                            "Running backwards filtering pass (#" + (++passCount) + ").");
                }

                if (featureFilterRequired) {
                    filterFeatures();
                }

                if (entryFeatureFilterRequired) {
                    filterEntryFeatures();
                }

                if (entryFilterRequired) {
                    filterEntries();
                }
            }
        }

        // Finished filtering so copy the results files to the outputs.

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying entries from " + activeEntriesFile
                    + " to " + outputEntriesFile + ".");
        }
        Files.copy(activeEntriesFile, outputEntriesFile);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying features from " + activeEntryFeaturesFile
                    + " to " + outputEntryFeaturesFile + ".");
        }
        Files.copy(activeEntryFeaturesFile, outputEntryFeaturesFile);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Copying features from " + activeFeaturesFile
                    + " to " + outputFeaturesFile + ".");
        }
        Files.copy(activeFeaturesFile, outputFeaturesFile);

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed filtering.");
        }
    }
    // Read the entries file, passing it thought the filter. accepted entries
    // are written out to the output file while rejected entries are stored
    // for filtering the AllPairsTask.

    private void filterEntries()
            throws FileNotFoundException, IOException {

        final IntSet rejected = new IntOpenHashSet();

        WeightedTokenSource entriesSource = new WeightedTokenSource(
                activeEntriesFile, charset, entryIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        WeightedTokenSink entriesSink = new WeightedTokenSink(outputFile,
                charset, entryIndex);

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Filtering entries from " + activeEntriesFile + " to " + outputFile + ".");
        }

        final int filteredEntry = entryIndex.get(FILTERED_STRING);
        double filteredWeight = 0;


        while (entriesSource.hasNext()) {
            Weighted<Token> record = entriesSource.read();

            if (record.get().id() == filteredEntry) {
                filteredWeight += record.getWeight();
            } else if (acceptEntry.apply(record)) {
                entriesSink.write(record);
            } else {
                rejected.add(record.get().id());
                filteredWeight += record.getWeight();
            }

            if ((entriesSource.getCount() % PROGRESS_INTERVAL == 0 || !entriesSource.hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Accepted " + entriesSink.getCount()
                        + " of " + entriesSource.getCount()
                        + " entries. (" + (int) entriesSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        if (filteredWeight != 0) {
            entriesSink.write(new Weighted<Token>(new Token(filteredEntry),
                    filteredWeight));
        }

        entriesSink.flush();
        entriesSink.close();
        entryFilterRequired = false;
        activeEntriesFile = outputFile;

        // Update the feature acceptance predicate
        if (rejected.size() > 0) {
            entryFeatureFilterRequired = true;
            acceptEntryFeature = and(acceptEntryFeature,
                    compose(not(in(rejected)),
                    entryFeatureEntryId()));
        }
    }

    // Filter the AllPairsTask file, rejecting all entires that contain entries
    // dropped in the entries file filter pass. Store a list of featuress that
    // only appear in filtered entries to filter the featuress file.
    private void filterEntryFeatures()
            throws FileNotFoundException, IOException {

        IntSet acceptedEntries = new IntOpenHashSet();
        IntSet rejectedEntries = new IntOpenHashSet();

        IntSet rejectedFeatures = new IntOpenHashSet();
        IntSet acceptedFeatures = new IntOpenHashSet();

        WeightedTokenPairSource efSrc = new WeightedTokenPairSource(
                activeEntryFeaturesFile, charset, entryIndex, featureIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        WeightedTokenPairSink efSink = new WeightedTokenPairSink(
                outputFile, charset,
                entryIndex, featureIndex);

        if (LOG.isInfoEnabled()) {
            LOG.info("Filtering entry/features pairs from "
                    + activeEntryFeaturesFile + " to " + outputFile + ".");
        }

        // Store the id of the special filtered feature and entry
        final int filteredEntry = entryIndex.get(FILTERED_STRING);
        final int filteredFeature = featureIndex.get(FILTERED_STRING);

        int currentEntryId = -1;
        int currentEntryFeatureCount = 0;
        double currentEntryFilteredFeatureWeight = 0;

        double filteredEntryWeight = 0;

        while (efSrc.hasNext()) {
            Weighted<TokenPair> record = efSrc.read();

            if (record.get().id1() == filteredEntry) {
                filteredEntryWeight += record.getWeight();
                continue;
            }

            if (record.get().id1() != currentEntryId) {

                if (currentEntryId != -1 && currentEntryFilteredFeatureWeight != 0) {
                    if (currentEntryFeatureCount == 0) {
                        filteredEntryWeight += currentEntryFilteredFeatureWeight;
                    } else {
                        efSink.write(new Weighted<TokenPair>(
                                new TokenPair(currentEntryId, filteredFeature),
                                currentEntryFilteredFeatureWeight));
                    }
                }

                currentEntryId = record.get().id1();
                currentEntryFilteredFeatureWeight = 0;
                currentEntryFeatureCount = 0;
            }

            if (record.get().id2() == filteredFeature) {

                currentEntryFilteredFeatureWeight += record.getWeight();

            } else if (acceptEntryFeature.apply(record)) {

                efSink.write(record);
                acceptedEntries.add(record.get().id1());
                acceptedFeatures.add(record.get().id2());
                ++currentEntryFeatureCount;

            } else {
                rejectedEntries.add(record.get().id1());
                rejectedFeatures.add(record.get().id2());

                currentEntryFilteredFeatureWeight += record.getWeight();
            }


            if ((efSrc.getCount() % PROGRESS_INTERVAL == 0
                    || !efSrc.hasNext()) && LOG.isInfoEnabled()) {
                LOG.info(
                        "Accepted " + efSink.getCount() + " of " + efSrc.getCount() + " feature entries. (" + (int) efSrc.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }


        if (currentEntryId != -1 && currentEntryFilteredFeatureWeight != 0) {
            if (currentEntryFeatureCount == 0) {
                filteredEntryWeight += currentEntryFilteredFeatureWeight;
            } else {
                efSink.write(new Weighted<TokenPair>(
                        new TokenPair(currentEntryId, filteredFeature),
                        currentEntryFilteredFeatureWeight));
            }
        }

        // If there have been entire entries filtered then write their summed
        // weights to a special filtered entry/feature pair
        if (filteredEntryWeight != 0) {
            efSink.write(new Weighted<TokenPair>(
                    new TokenPair(filteredEntry, filteredFeature),
                    filteredEntryWeight));
        }

        efSink.flush();
        efSink.close();
        entryFeatureFilterRequired = false;
        activeEntryFeaturesFile = outputFile;

        rejectedFeatures.removeAll(acceptedFeatures);
        rejectedEntries.removeAll(acceptedEntries);

        if (rejectedEntries.size() > 0) {
            acceptEntry = and(acceptEntry,
                    compose(not(in(rejectedEntries)), id()));
            entryFilterRequired = true;
        }

        if (rejectedFeatures.size() > 0) {
            acceptFeature = and(acceptFeature,
                    not(compose(in(rejectedFeatures), id())));
            featureFilterRequired = true;

        }
    }

    // Filter the AllPairsTask file, rejecting all entries that where found to
    // be only used by filtered entries.
    private void filterFeatures()
            throws FileNotFoundException, IOException {
        IntSet rejectedFeatures = new IntOpenHashSet();

        WeightedTokenSource featureSource = new WeightedTokenSource(
                activeFeaturesFile, charset, featureIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        WeightedTokenSink featureSink = new WeightedTokenSink(
                outputFile, charset, featureIndex);

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Filtering features from " + activeFeaturesFile + " to " + outputFile + ".");
        }

        // Store an filtered wieght here and record it so as to maintain
        // accurate priors for those features that remain
        double filteredWeight = 0;
        int filteredId = featureSource.getStringIndex().get(FILTERED_STRING);

        while (featureSource.hasNext()) {
            Weighted<Token> feature = featureSource.read();

            if (feature.get().id() == filteredId) {
                filteredWeight += feature.getWeight();
            } else if (acceptFeature.apply(feature)) {
                featureSink.write(feature);
            } else {
                rejectedFeatures.add(feature.get().id());
                filteredWeight += feature.getWeight();
            }

            if ((featureSource.getCount() % PROGRESS_INTERVAL == 0
                    || !featureSource.hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Accepted " + featureSink.getCount() + " of " + featureSource.getCount()
                        + " features. (" + (int) featureSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }

        if (filteredWeight != 0) {
            featureSink.write(new Weighted<Token>(new Token(filteredId),
                    filteredWeight));
        }
        featureSink.flush();
        featureSink.close();
        featureFilterRequired = false;
        activeFeaturesFile = outputFile;


        // Update the feature acceptance predicate
        if (rejectedFeatures.size() > 0) {

            entryFeatureFilterRequired = true;
            acceptEntryFeature = and(
                    acceptEntryFeature,
                    compose(not(in(rejectedFeatures)), entryFeatureFeatureId()));

        }
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        this.charset = checkNotNull(charset);
    }

    public final File getInputFeaturesFile() {
        return inputFeaturesFile;
    }

    public final void setInputFeaturesFile(File inputFeaturesFile) {
        this.inputFeaturesFile = checkNotNull(inputFeaturesFile);
    }

    public final File getInputEntryFeaturesFile() {
        return inputEntryFeaturesFile;
    }

    public final void setInputEntryFeaturesFile(File inputEntryFeaturesFile) {
        this.inputEntryFeaturesFile = checkNotNull(inputEntryFeaturesFile);
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

    public final File getOutputEntryFeaturesFile() {
        return outputEntryFeaturesFile;
    }

    public final void setOutputEntryFeaturesFile(File outputEntryFeaturesFile) {
        this.outputEntryFeaturesFile = checkNotNull(outputEntryFeaturesFile);
    }

    public final File getOutputEntriesFile() {
        return outputEntriesFile;
    }

    public final void setOutputEntriesFile(File outputEntriesFile) {
        this.outputEntriesFile = checkNotNull(outputEntriesFile);
    }

    public Predicate<Weighted<Token>> getAcceptFeatures() {
        return acceptFeature;
    }

    public void setAcceptFeatures(Predicate<Weighted<Token>> acceptFeature) {
        if (!acceptFeature.equals(this.acceptFeature)) {
            this.acceptFeature = acceptFeature;
            featureFilterRequired = true;
        }
    }

    public void addFeaturesMinimumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(gte(threshold), this.<Token>weight())));
    }

    public void addFeaturesMaximumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(lte(threshold), this.<Token>weight())));
    }

    public void addFeaturesFrequencyRange(double min, double max) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(inRange(min, max), this.<Token>weight())));
    }

    public void addFeaturesPattern(String pattern) {
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(containsPattern(pattern), featureString())));
    }

    public void addFeaturesWhitelist(List<String> strings) {
        IntSet featureIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = featureIndex.get(string);
            featureIdSet.add(id);
        }
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(in(featureIdSet), id())));
    }

    public void addFeaturesBlacklist(List<String> strings) {
        IntSet featureIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = featureIndex.get(string);
            featureIdSet.add(id);
        }
        setAcceptFeatures(Predicates2.<Weighted<Token>>and(
                getAcceptFeatures(),
                compose(not(in(featureIdSet)), id())));

    }

    public Predicate<Weighted<TokenPair>> getAcceptEntryFeature() {
        return acceptEntryFeature;
    }

    public void setAcceptEntryFeature(
            Predicate<Weighted<TokenPair>> acceptFeature) {
        if (!acceptFeature.equals(this.acceptEntryFeature)) {
            this.acceptEntryFeature = acceptFeature;
            entryFeatureFilterRequired = true;
        }
    }

    public void addEntryFeatureMinimumFrequency(double threshold) {
        setAcceptEntryFeature(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEntryFeature(),
                compose(gte(threshold), this.<TokenPair>weight())));
    }

    public void addEntryFeatureMaximumFrequency(double threshold) {
        setAcceptEntryFeature(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEntryFeature(),
                compose(lte(threshold), this.<TokenPair>weight())));
    }

    public void addEntryFeatureFrequencyRange(double min, double max) {
        setAcceptEntryFeature(Predicates2.<Weighted<TokenPair>>and(
                getAcceptEntryFeature(),
                compose(inRange(min, max), this.<TokenPair>weight())));
    }

    public Predicate<Weighted<Token>> getAcceptEntry() {
        return acceptEntry;
    }

    public void setAcceptEntry(Predicate<Weighted<Token>> acceptEntry) {
        if (!acceptEntry.equals(this.acceptEntry)) {
            this.acceptEntry = acceptEntry;
            entryFilterRequired = true;
        }
    }

    public void addEntryMinimumFrequency(double threshold) {
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(gte(threshold), this.<Token>weight())));
    }

    public void addEntryMaximumFrequency(double threshold) {
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(lte(threshold), this.<Token>weight())));
    }

    public void addEntryFrequencyRange(double min, double max) {
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(inRange(min, max), this.<Token>weight())));
    }

    public void addEntryPattern(String pattern) {
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(containsPattern(pattern), entryString())));
    }

    public void addEntryWhitelist(List<String> strings) {
        IntSet entryIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = entryIndex.get(string);
            entryIdSet.add(id);
        }
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(in(entryIdSet), id())));

    }

    public void addEntryBlacklist(List<String> strings) {
        IntSet entryIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = entryIndex.get(string);
            entryIdSet.add(id);
        }
        setAcceptEntry(Predicates2.<Weighted<Token>>and(
                getAcceptEntry(),
                compose(not(in(entryIdSet)), id())));

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
    private void checkState()
            throws NullPointerException, IllegalStateException, FileNotFoundException {

        final Map<String, File> inputFiles = new HashMap<String, File>();
        inputFiles.put("inputEntries", inputEntriesFile);
        inputFiles.put("inputFeatures", inputFeaturesFile);
        inputFiles.put("inputEntryFeatures", inputEntryFeaturesFile);

        final Map<String, File> outputFiles = new HashMap<String, File>();
        outputFiles.put("outputEntries", outputEntriesFile);
        outputFiles.put("outputFeatures", outputFeaturesFile);
        outputFiles.put("outputEntryFeatures", outputEntryFeaturesFile);

        final Map<String, File> allFiles = new HashMap<String, File>();
        allFiles.putAll(inputFiles);
        allFiles.putAll(outputFiles);

        // Check non of the parameters are null
        for (Map.Entry<String, File> entry : allFiles.entrySet()) {
            if (entry.getValue() == null) {
                throw new NullPointerException(entry.getKey() + " is null");
            }
        }
        if (charset == null) {
            throw new NullPointerException("charset is null");
        }

        // Check that no two files are the same
        for (Map.Entry<String, File> a : allFiles.entrySet()) {
            for (Map.Entry<String, File> b : allFiles.entrySet()) {
                if (!a.getKey().equals(b.getKey()) && a.getValue().equals(b.getValue())) {
                    throw new IllegalStateException(a.getKey() + " equal to " + b.getKey());
                }
            }
        }

        // Check that the input files exists and is readable
        for (Map.Entry<String, File> entry : inputFiles.entrySet()) {
            if (!entry.getValue().exists()) {
                throw new FileNotFoundException(
                        entry.getKey() + " does not exist: " + entry.getValue());
            }
            if (!entry.getValue().isFile()) {
                throw new IllegalStateException(entry.getKey()
                        + " is not a normal data file: " + entry.getValue());
            }
            if (!entry.getValue().canRead()) {
                throw new IllegalStateException(
                        entry.getKey() + " is not readable: " + entry.getValue());
            }
        }

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        for (Map.Entry<String, File> e : outputFiles.entrySet()) {
            if (e.getValue().exists()
                    && (!e.getValue().isFile() || !e.getValue().canWrite())) {
                throw new IllegalStateException(e.getKey()
                        + " exists but is not writable: " + e.getValue());
            }
            if (!e.getValue().exists()
                    && !e.getValue().getAbsoluteFile().getParentFile().canWrite()) {
                throw new IllegalStateException(e.getKey()
                        + " does not exists and can not be reated: "
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
                return input.getWeight();
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
                return input.get().id();
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
                return entryIndex.get(input.get().id());
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
                return featureIndex.get(input.get().id());
            }

            @Override
            public String toString() {
                return "FeatureString";
            }
        };
    }

    private Function<Weighted<TokenPair>, Integer> entryFeatureEntryId() {
        return new Function<Weighted<TokenPair>, Integer>() {

            @Override
            public Integer apply(Weighted<TokenPair> input) {
                return input.get().id1();
            }

            @Override
            public String toString() {
                return "FeatureEntryID";
            }
        };
    }

    private Function<Weighted<TokenPair>, Integer> entryFeatureFeatureId() {
        return new Function<Weighted<TokenPair>, Integer>() {

            @Override
            public Integer apply(Weighted<TokenPair> input) {
                return input.get().id2();
            }

            @Override
            public String toString() {
                return "EntryFeatureID";
            }
        };
    }

    private Function<Weighted<TokenPair>, String> entryFeatureFeatureString() {
        return new Function<Weighted<TokenPair>, String>() {

            @Override
            public String apply(Weighted<TokenPair> input) {
                return featureIndex.get(input.get().id2());
            }

            @Override
            public String toString() {
                return "EntryFeatureFeatureString";
            }
        };
    }

    private Function<Weighted<TokenPair>, String> entryFeatureEntryString() {
        return new Function<Weighted<TokenPair>, String>() {

            @Override
            public String apply(Weighted<TokenPair> input) {
                return entryIndex.get(input.get().id1());
            }

            @Override
            public String toString() {
                return "EntryFeatureEntryString";
            }
        };
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("eventsIn", inputEntryFeaturesFile).
                add("entriesIn", inputEntriesFile).
                add("featuresIn", inputFeaturesFile).
                add("eventsOut", outputEntryFeaturesFile).
                add("entriesOut", outputEntriesFile).
                add("featuresOut", outputFeaturesFile).
                add("charset", charset).
                add("entryMinFreq", filterEntryMinFreq).
                add("entryWhitelist", filterEntryWhitelist).
                add("entryPattern", filterEntryPattern).
                add("eventMinFreq", filterEntryFeatureMinFreq).
                add("featureMinFreq", filterFeatureMinFreq).
                add("featureWhitelist", filterFeatureWhitelist).
                add("featurePattern", filterFeaturePattern).
                add("tmp", tempFiles).
                add("acceptEntry", acceptEntry).
                add("acceptFeature", acceptFeature).
                add("acceptEvent", acceptEntryFeature);
    }
}

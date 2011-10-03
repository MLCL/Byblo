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

import uk.ac.susx.mlcl.lib.io.TempFileFactoryConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import static com.google.common.base.Preconditions.*;
import com.google.common.base.Predicate;
import static uk.ac.susx.mlcl.lib.Predicates2.*;
import com.google.common.io.Files;
import uk.ac.susx.mlcl.lib.DoubleConverter;
import uk.ac.susx.mlcl.byblo.io.FeatureRecord;
import uk.ac.susx.mlcl.byblo.io.FeatureSink;
import uk.ac.susx.mlcl.byblo.io.FeatureSource;
import uk.ac.susx.mlcl.byblo.io.EntryRecord;
import uk.ac.susx.mlcl.byblo.io.EntrySink;
import uk.ac.susx.mlcl.byblo.io.EntrySource;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.Predicates2;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
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
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureRecord;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureSink;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureSource;

/**
 *
 * TODO: Efficiency improvements could be found be combining predicates more
 *  intelligently. If, for e.g, one predicate was found to be implied by another
 *  then only the stronger need be taken.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Filter a set of frequency files")
public class FilterTask extends AbstractTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(CountTask.class);

    /**
     * Number of records to read or write between progress updates.
     */
    private static final int PROGRESS_INTERVAL = 1000000;

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
    private FileFactory tempFiles = new TempFileFactory("temp", ".txt");

    /*
     * === INTERNAL ===
     */
    private Predicate<EntryRecord> acceptEntry = alwaysTrue();

    private Predicate<WeightedEntryFeatureRecord> acceptEntryFeature = alwaysTrue();

    private Predicate<FeatureRecord> acceptFeature = alwaysTrue();

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

        if (filterFeatureMinFreq > 0)
            addFeaturesMinimumFrequency(filterFeatureMinFreq);
        if (filterFeaturePattern != null)
            addFeaturesPattern(filterFeaturePattern);
        if (filterFeatureWhitelist != null)
            addFeaturesWhitelist(Files.readLines(
                    filterFeatureWhitelist, charset));

        if (filterEntryMinFreq > 0)
            addEntryMinimumFrequency(filterEntryMinFreq);
        if (filterEntryPattern != null)
            addEntryPattern(filterEntryPattern);
        if (filterEntryWhitelist != null)
            addEntryWhitelist(Files.readLines(
                    filterEntryWhitelist, charset));

        if (filterEntryFeatureMinFreq > 0)
            addEntryFeatureMinimumFrequency(filterEntryFeatureMinFreq);

        checkState();
        activeEntryFeaturesFile = inputEntryFeaturesFile;
        activeEntriesFile = inputEntriesFile;
        activeFeaturesFile = inputFeaturesFile;
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running " + this + ". (thread:" + Thread.currentThread().
                    getName() + ")");


        // Run the filters forwards then backwards. Each filtering step may
        // introduce additionaly filters for the other files, so continue
        // looping until there is no work remaining. Depending on filters this
        // very unlikely to take more than 3 passes

        int passCount = 0;

        while (entryFilterRequired
                || entryFeatureFilterRequired
                || featureFilterRequired) {

            if (entryFilterRequired || entryFeatureFilterRequired) {
                if (LOG.isInfoEnabled())
                    LOG.info(
                            "Running forwards filtering pass (#" + (++passCount) + ").");

                if (entryFilterRequired)
                    filterEntries();

                if (entryFeatureFilterRequired)
                    filterEntryFeatures();

                if (featureFilterRequired)
                    filterFeatures();
            }

            if (featureFilterRequired || entryFeatureFilterRequired) {
                if (LOG.isInfoEnabled())
                    LOG.info(
                            "Running backwards filtering pass (#" + (++passCount) + ").");

                if (featureFilterRequired)
                    filterFeatures();

                if (entryFeatureFilterRequired)
                    filterEntryFeatures();

                if (entryFilterRequired)
                    filterEntries();
            }
        }

        // Finished filtering so copy the results files to the outputs.

        if (LOG.isInfoEnabled())
            LOG.info("Copying entries from " + activeEntriesFile
                    + " to " + outputEntriesFile + ".");
        Files.copy(activeEntriesFile, outputEntriesFile);

        if (LOG.isInfoEnabled())
            LOG.info("Copying features from " + activeEntryFeaturesFile
                    + " to " + outputEntryFeaturesFile + ".");
        Files.copy(activeEntryFeaturesFile, outputEntryFeaturesFile);

        if (LOG.isInfoEnabled())
            LOG.info("Copying features from " + activeFeaturesFile
                    + " to " + outputFeaturesFile + ".");
        Files.copy(activeFeaturesFile, outputFeaturesFile);

        if (LOG.isInfoEnabled())
            LOG.info("Completed " + this + ". (thread:" + Thread.currentThread().
                    getName() + ")");
    }
    // Read the entries file, passing it thought the filter. accepted entries
    // are written out to the output file while rejected entries are stored
    // for filtering the AllPairsTask.

    private void filterEntries()
            throws FileNotFoundException, IOException {

        final IntSet rejected = new IntOpenHashSet();

        EntrySource entriesSource = new EntrySource(
                activeEntriesFile, charset, entryIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        EntrySink entriesSink = new EntrySink(outputFile, charset, entryIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering entries from " + activeEntriesFile + " to " + outputFile + ".");

        while (entriesSource.hasNext()) {
            EntryRecord entry = entriesSource.read();
            if (acceptEntry.apply(entry)) {
                entriesSink.write(entry);
            } else {
                rejected.add(entry.getEntryId());
            }
            if ((entriesSource.getCount() % PROGRESS_INTERVAL == 0 || !entriesSource.
                    hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + entriesSource.getCount()
                        + " entry entries."
                        + "(" + (int) entriesSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }
        entriesSink.flush();
        entriesSink.close();
        entryFilterRequired = false;
        activeEntriesFile = outputFile;

        // Update the feature acceptance predicate
        if (rejected.size() > 0) {
            entryFeatureFilterRequired = true;
            acceptEntryFeature = and(acceptEntryFeature,
                    compose(not(in(rejected)), entryFeatureEntryId()));
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

        WeightedEntryFeatureSource featuresSource = new WeightedEntryFeatureSource(
                activeEntryFeaturesFile, charset, entryIndex, featureIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        WeightedEntryFeatureSink entryFeaturesSink = new WeightedEntryFeatureSink(
                outputFile, charset,
                entryIndex, featureIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering entry/features pairs from " + activeEntryFeaturesFile + " to " + outputFile + ".");

        while (featuresSource.hasNext()) {
            WeightedEntryFeatureRecord entry = featuresSource.read();
            if (acceptEntryFeature.apply(entry)) {
                entryFeaturesSink.write(entry);
                acceptedFeatures.add(entry.getFeatureId());
                acceptedEntries.add(entry.getEntryId());
            } else {
                rejectedFeatures.add(entry.getFeatureId());
                rejectedEntries.add(entry.getEntryId());
            }


            if ((featuresSource.getCount() % PROGRESS_INTERVAL == 0
                    || !featuresSource.hasNext()) && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + featuresSource.getCount() + " feature entries."
                        + "(" + (int) featuresSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }
        entryFeaturesSink.flush();
        entryFeaturesSink.close();
        entryFeatureFilterRequired = false;
        activeEntryFeaturesFile = outputFile;

        rejectedFeatures.removeAll(acceptedFeatures);
        rejectedEntries.removeAll(acceptedEntries);

        if (rejectedEntries.size() > 0) {
            acceptEntry = and(acceptEntry,
                    compose(not(in(rejectedEntries)), entryId()));
            entryFilterRequired = true;
        }

        if (rejectedFeatures.size() > 0) {
            acceptFeature = and(acceptFeature,
                    not(compose(in(rejectedFeatures), featureId())));
            featureFilterRequired = true;

        }
    }

    // Filter the AllPairsTask file, rejecting all entries that where found to
    // be only used by filtered entries.
    private void filterFeatures()
            throws FileNotFoundException, IOException {
        IntSet rejectedFeatures = new IntOpenHashSet();

        FeatureSource featureSource = new FeatureSource(
                activeFeaturesFile, charset, featureIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        FeatureSink featureSink = new FeatureSink(
                outputFile, charset, featureIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering features from " + activeFeaturesFile + " to " + outputFile + ".");

        while (featureSource.hasNext()) {
            FeatureRecord feature = featureSource.read();

            if (acceptFeature.apply(feature)) {
                featureSink.write(feature);
            } else {
                rejectedFeatures.add(feature.getFeatureId());
            }

            if ((featureSource.getCount() % PROGRESS_INTERVAL == 0
                    || !featureSource.hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + featureSource.getCount()
                        + " features."
                        + "(" + (int) featureSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
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
    public String toString() {
        return "FilterTask{"
                + "inputEntryFeaturesFile=" + inputEntryFeaturesFile
                + ", inputEntriesFile=" + inputEntriesFile
                + ", inputFeaturesFile=" + inputFeaturesFile
                + ", outputEntryFeaturesFile=" + outputEntryFeaturesFile
                + ", outputEntriesFile=" + outputEntriesFile
                + ", outputFeaturesFile=" + outputFeaturesFile
                + ", charset=" + charset
                + ", filters={"
                + "EntryMinFreq=" + filterEntryMinFreq
                + ", EntryWhitelist=" + filterEntryWhitelist
                + ", EntryPattern=" + filterEntryPattern
                + ", EntryFeatureMinFreq=" + filterEntryFeatureMinFreq
                + ", FeatureMinFreq=" + filterFeatureMinFreq
                + ", FeatureWhitelist=" + filterFeatureWhitelist
                + ", FeaturePattern=" + filterFeaturePattern
                + "}"
                + ", acceptEntry=" + acceptEntry
                + ", acceptEntryFeature=" + acceptEntryFeature
                + ", acceptFeature=" + acceptFeature
                + '}';
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

    public Predicate<FeatureRecord> getAcceptFeatures() {
        return acceptFeature;
    }

    public void setAcceptFeatures(Predicate<FeatureRecord> acceptFeature) {
        if (!acceptFeature.equals(this.acceptFeature)) {
            this.acceptFeature = acceptFeature;
            featureFilterRequired = true;
        }
    }

    public void addFeaturesMinimumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(gte(threshold), featureFreq())));
    }

    public void addFeaturesMaximumFrequency(double threshold) {
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(lte(threshold), featureFreq())));
    }

    public void addFeaturesFrequencyRange(double min, double max) {
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(inRange(min, max), featureFreq())));
    }

    public void addFeaturesPattern(String pattern) {
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(containsPattern(pattern), featureString())));
    }

    public void addFeaturesWhitelist(List<String> strings) {
        IntSet featureIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = featureIndex.get(string);
            featureIdSet.add(id);
        }
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(in(featureIdSet), featureId())));
    }

    public void addFeaturesBlacklist(List<String> strings) {
        IntSet featureIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = featureIndex.get(string);
            featureIdSet.add(id);
        }
        setAcceptFeatures(Predicates2.<FeatureRecord>and(
                getAcceptFeatures(),
                compose(not(in(featureIdSet)), featureId())));

    }

    public Predicate<WeightedEntryFeatureRecord> getAcceptEntryFeature() {
        return acceptEntryFeature;
    }

    public void setAcceptEntryFeature(
            Predicate<WeightedEntryFeatureRecord> acceptFeature) {
        if (!acceptFeature.equals(this.acceptEntryFeature)) {
            this.acceptEntryFeature = acceptFeature;
            entryFeatureFilterRequired = true;
        }
    }

    public void addEntryFeatureMinimumFrequency(double threshold) {
        setAcceptEntryFeature(Predicates2.<WeightedEntryFeatureRecord>and(
                getAcceptEntryFeature(),
                compose(gte(threshold), entryFeatureFreq())));
    }

    public void addEntryFeatureMaximumFrequency(double threshold) {
        setAcceptEntryFeature(Predicates2.<WeightedEntryFeatureRecord>and(
                getAcceptEntryFeature(),
                compose(lte(threshold), entryFeatureFreq())));
    }

    public void addEntryFeatureFrequencyRange(double min, double max) {
        setAcceptEntryFeature(Predicates2.<WeightedEntryFeatureRecord>and(
                getAcceptEntryFeature(),
                compose(inRange(min, max), entryFeatureFreq())));
    }

    public Predicate<EntryRecord> getAcceptEntry() {
        return acceptEntry;
    }

    public void setAcceptEntry(Predicate<EntryRecord> acceptEntry) {
        if (!acceptEntry.equals(this.acceptEntry)) {
            this.acceptEntry = acceptEntry;
            entryFilterRequired = true;
        }
    }

    public void addEntryMinimumFrequency(double threshold) {
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(gte(threshold), entryFreq())));
    }

    public void addEntryMaximumFrequency(double threshold) {
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(lte(threshold), entryFreq())));
    }

    public void addEntryFrequencyRange(double min, double max) {
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(inRange(min, max), entryFreq())));
    }

    public void addEntryPattern(String pattern) {
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(containsPattern(pattern), entryString())));
    }

    public void addEntryWhitelist(List<String> strings) {
        IntSet entryIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = entryIndex.get(string);
            entryIdSet.add(id);
        }
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(in(entryIdSet), entryId())));

    }

    public void addEntryBlacklist(List<String> strings) {
        IntSet entryIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = entryIndex.get(string);
            entryIdSet.add(id);
        }
        setAcceptEntry(Predicates2.<EntryRecord>and(
                getAcceptEntry(),
                compose(not(in(entryIdSet)), entryId())));

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
        if (charset == null)
            throw new NullPointerException("charset is null");

        // Check that no two files are the same
        for (Map.Entry<String, File> a : allFiles.entrySet()) {
            for (Map.Entry<String, File> b : allFiles.entrySet()) {
                if (!a.getKey().equals(b.getKey()) && a.getValue().equals(b.
                        getValue())) {
                    throw new IllegalStateException(a.getKey() + " equal to " + b.
                            getKey());
                }
            }
        }

        // Check that the input files exists and is readable
        for (Map.Entry<String, File> entry : inputFiles.entrySet()) {
            if (!entry.getValue().exists())
                throw new FileNotFoundException(
                        entry.getKey() + " does not exist: " + entry.getValue());
            if (!entry.getValue().isFile())
                throw new IllegalStateException(entry.getKey()
                        + " is not a normal data file: " + entry.getValue());
            if (!entry.getValue().canRead())
                throw new IllegalStateException(
                        entry.getKey() + " is not readable: " + entry.getValue());
        }

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        for (Map.Entry<String, File> e : outputFiles.entrySet()) {
            if (e.getValue().exists()
                    && (!e.getValue().isFile() || !e.getValue().canWrite()))
                throw new IllegalStateException(e.getKey()
                        + " exists but is not writable: " + e.getValue());
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
    private Function<EntryRecord, Double> entryFreq() {
        return new Function<EntryRecord, Double>() {

            @Override
            public Double apply(EntryRecord input) {
                return input.getWeight();
            }

            @Override
            public String toString() {
                return "EntryFrequency";
            }
        };
    }

    private Function<EntryRecord, Integer> entryId() {
        return new Function<EntryRecord, Integer>() {

            @Override
            public Integer apply(EntryRecord input) {
                return input.getEntryId();
            }

            @Override
            public String toString() {
                return "EntryID";
            }
        };
    }

    private Function<EntryRecord, String> entryString() {
        return new Function<EntryRecord, String>() {

            @Override
            public String apply(EntryRecord input) {
                return entryIndex.get(input.getEntryId());
            }

            @Override
            public String toString() {
                return "EntriesString";
            }
        };
    }

    private Function<FeatureRecord, Double> featureFreq() {
        return new Function<FeatureRecord, Double>() {

            @Override
            public Double apply(FeatureRecord input) {
                return input.getWeight();
            }

            @Override
            public String toString() {
                return "FeatureFrequency";
            }
        };
    }

    private Function<FeatureRecord, Integer> featureId() {
        return new Function<FeatureRecord, Integer>() {

            @Override
            public Integer apply(FeatureRecord input) {
                return input.getFeatureId();
            }

            @Override
            public String toString() {
                return "FeatureID";
            }
        };
    }

    private Function<FeatureRecord, String> featureString() {
        return new Function<FeatureRecord, String>() {

            @Override
            public String apply(FeatureRecord input) {
                return featureIndex.get(input.getFeatureId());
            }

            @Override
            public String toString() {
                return "FeatureString";
            }
        };
    }

    private Function<WeightedEntryFeatureRecord, Double> entryFeatureFreq() {
        return new Function<WeightedEntryFeatureRecord, Double>() {

            @Override
            public Double apply(WeightedEntryFeatureRecord input) {
                return input.getWeight();
            }

            @Override
            public String toString() {
                return "FeatureEntryFrequency";
            }
        };
    }

    private Function<WeightedEntryFeatureRecord, Integer> entryFeatureEntryId() {
        return new Function<WeightedEntryFeatureRecord, Integer>() {

            @Override
            public Integer apply(WeightedEntryFeatureRecord input) {
                return input.getEntryId();
            }

            @Override
            public String toString() {
                return "FeatureEntryID";
            }
        };
    }

    private Function<WeightedEntryFeatureRecord, Integer> entryFeatureFeatureId() {
        return new Function<WeightedEntryFeatureRecord, Integer>() {

            @Override
            public Integer apply(WeightedEntryFeatureRecord input) {
                return input.getFeatureId();
            }

            @Override
            public String toString() {
                return "EntryFeatureID";
            }
        };
    }

    private Function<WeightedEntryFeatureRecord, String> entryFeatureFeatureString() {
        return new Function<WeightedEntryFeatureRecord, String>() {

            @Override
            public String apply(WeightedEntryFeatureRecord input) {
                return featureIndex.get(input.getFeatureId());
            }

            @Override
            public String toString() {
                return "EntryFeatureFeatureString";
            }
        };
    }

    private Function<WeightedEntryFeatureRecord, String> entryFeatureEntryString() {
        return new Function<WeightedEntryFeatureRecord, String>() {

            @Override
            public String apply(WeightedEntryFeatureRecord input) {
                return entryIndex.get(input.getEntryId());
            }

            @Override
            public String toString() {
                return "EntryFeatureEntryString";
            }
        };
    }
}

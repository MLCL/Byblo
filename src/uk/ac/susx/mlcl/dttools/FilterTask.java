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
package uk.ac.susx.mlcl.dttools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import static com.google.common.base.Preconditions.*;
import com.google.common.base.Predicate;
import static uk.ac.susx.mlcl.lib.Predicates2.*;
import com.google.common.io.Files;
import uk.ac.susx.mlcl.dttools.AllPairsCommand.DoubleConverter;
import uk.ac.susx.mlcl.dttools.io.ContextEntry;
import uk.ac.susx.mlcl.dttools.io.ContextSink;
import uk.ac.susx.mlcl.dttools.io.ContextSource;
import uk.ac.susx.mlcl.dttools.io.FeatureEntry;
import uk.ac.susx.mlcl.dttools.io.FeatureSink;
import uk.ac.susx.mlcl.dttools.io.FeatureSource;
import uk.ac.susx.mlcl.dttools.io.HeadEntry;
import uk.ac.susx.mlcl.dttools.io.HeadSink;
import uk.ac.susx.mlcl.dttools.io.HeadSource;
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

/**
 *
 * TODO: Efficiency improvements could be found be combining predicates more
 *  intelligently. If, for e.g, one predicate was found to be implied by another
 *  then only the stronger need be taken.
 *
 * @author hamish
 */
@Parameters(commandDescription = "",
            resourceBundle = "uk.ac.susx.mlcl.dttools.strings")
public class FilterTask extends AbstractTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(MemCountTask.class);

    /**
     * Number of records to read or write between progress updates.
     */
    private static final int PROGRESS_INTERVAL = 1000000;

    @Parameter(names = {"-if", "--input-features"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.INPUT_FEATURES_DESCRIPTION")
    private File inputFeatures;

    @Parameter(names = {"-ih", "--input-heads"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.INPUT_HEADS_DESCRIPTION")
    private File inputHeads;

    @Parameter(names = {"-ic", "--input-contexts"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.INPUT_CONTEXTS_DESCRIPTION")
    private File inputContexts;

    @Parameter(names = {"-of", "--output-features"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.OUTPUT_FEATURES_DESCRIPTION")
    private File outputFeatures;

    @Parameter(names = {"-oh", "--output-heads"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.OUTPUT_HEADS_DESCRIPTION")
    private File outputHeads;

    @Parameter(names = {"-oc", "--output-contexts"},
               required = true,
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.OUTPUT_CONTEXTS_DESCRIPTION")
    private File outputContexts;

    @Parameter(names = {"-c", "--charset"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.CHARSET_DESCRIPTION")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    @Parameter(names = {"-fhf", "--filter-head-freq"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_HEAD_FREQUENCY_DESCRIPTION",
               converter = DoubleConverter.class)
    private double filterHeadMinFreq;

    @Parameter(names = {"-fhw", "--filter-head-wordlist"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_HEAD_WORDLIST_DESCRIPTION")
    private File filterHeadWhitelist;

    @Parameter(names = {"-fhp", "--filter-head-pattern"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_HEAD_PATTERN_DESCRIPTION")
    private String filterHeadPattern;

    @Parameter(names = {"-fff", "--filter-feature-freq"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_FEATURE_FREQUENCY_DESCRIPTION",
               converter = DoubleConverter.class)
    private double filterFeatureMinFreq;

    @Parameter(names = {"-fcf", "--filter-context-freq"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_CONTEXT_FREQUENCY_DESCRIPTION",
               converter = DoubleConverter.class)
    private double filterContextMinFreq;

    @Parameter(names = {"-fcw", "--filter-context-wordlist"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_CONTEXT_WORDLIST_DESCRIPTION")
    private File filterContextWhitelist;

    @Parameter(names = {"-fcp", "--filter-context-pattern"},
               descriptionKey = "uk.ac.susx.mlcl.dttools.FilterHeadsTask.FILTER_CONTEXT_PATTERN_DESCRIPTION")
    private String filterContextPattern;

    @Parameter(names = {"-T", "--temp-dir"},
               descriptionKey = "USAGE_TEMP_DIR",
               converter = TempFileFactoryConverter.class)
    private FileFactory tempFiles = new TempFileFactory("temp", ".txt");
    //
    //===============================
    //
    //

    private Predicate<HeadEntry> acceptHead = alwaysTrue();

    private Predicate<FeatureEntry> acceptFeature = alwaysTrue();

    private Predicate<ContextEntry> acceptContext = alwaysTrue();

    private boolean headFilterRequired = false;

    private boolean featureFilterRequired = false;

    private boolean contextFilterRequired = false;

    final ObjectIndex<String> headIndex = new ObjectIndex<String>();

    final ObjectIndex<String> contextIndex = new ObjectIndex<String>();

    private File activeFeaturesFile;

    private File activeHeadsFile;

    private File activeContextsFile;

    public FilterTask() {
//        setCharset(IOUtil.DEFAULT_CHARSET);
    }

    public FilterTask(File inputFeatures, File inputHeads,
            File inputContexts, File outputFeatures,
            File outputHeads, File outputContexts,
            Charset charset) {
        setCharset(charset);
        setInputContexts(inputContexts);
        setInputFeatures(inputFeatures);
        setInputHeads(inputHeads);
        setOutputContexts(outputContexts);
        setOutputFeatures(outputFeatures);
        setOutputHeads(outputHeads);
    }

    @Override
    protected void initialiseTask() throws Exception {

        if (filterContextMinFreq > 0)
            addContextMinimumFrequency(filterContextMinFreq);
        if (filterContextPattern != null)
            addContextPattern(filterContextPattern);
        if (filterContextWhitelist != null)
            addContextWhitelist(Files.readLines(
                    filterContextWhitelist, charset));

        if (filterHeadMinFreq > 0)
            addHeadMinimumFrequency(filterHeadMinFreq);
        if (filterHeadPattern != null)
            addHeadPattern(filterHeadPattern);
        if (filterHeadWhitelist != null)
            addHeadWhitelist(Files.readLines(
                    filterHeadWhitelist, charset));

        if (filterFeatureMinFreq > 0)
            addFeatureMinimumFrequency(filterFeatureMinFreq);

        checkState();
        activeFeaturesFile = inputFeatures;
        activeHeadsFile = inputHeads;
        activeContextsFile = inputContexts;
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

        while (headFilterRequired
                || featureFilterRequired
                || contextFilterRequired) {

            if (headFilterRequired || featureFilterRequired) {
                // forwards pass
                LOG.info(
                        "Running forwards filtering pass (#" + (++passCount) + ").");

                if (headFilterRequired)
                    filterHeads();

                if (featureFilterRequired)
                    filterFeatures();

                if (contextFilterRequired)
                    filterContexts();
            }

            if (contextFilterRequired || featureFilterRequired) {
                LOG.info(
                        "Running backwards filtering pass (#" + (++passCount) + ").");

                if (contextFilterRequired)
                    filterContexts();

                if (featureFilterRequired)
                    filterFeatures();

                if (headFilterRequired)
                    filterHeads();
            }
        }

        // Finished filtering so copy the results files to the outputs.

        if (LOG.isInfoEnabled())
            LOG.info("Copying heads from " + activeHeadsFile
                    + " to " + outputHeads + ".");
        Files.copy(activeHeadsFile, outputHeads);

        if (LOG.isInfoEnabled())
            LOG.info("Copying features from " + activeFeaturesFile
                    + " to " + outputFeatures + ".");
        Files.copy(activeFeaturesFile, outputFeatures);

        if (LOG.isInfoEnabled())
            LOG.info("Copying contexts from " + activeContextsFile
                    + " to " + outputContexts + ".");
        Files.copy(activeContextsFile, outputContexts);

        if (LOG.isInfoEnabled())
            LOG.info("Completed " + this + ". (thread:" + Thread.currentThread().
                    getName() + ")");
    }
    // Read the heads file, passing it thought the filter. accepted entries
    // are written out to the output file while rejected entries are stored
    // for filtering the features.

    private void filterHeads()
            throws FileNotFoundException, IOException {

        final IntSet rejected = new IntOpenHashSet();

        HeadSource headSource = new HeadSource(
                activeHeadsFile, charset, headIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        HeadSink headSink = new HeadSink(outputFile, charset, headIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering heads from " + activeHeadsFile + " to " + outputFile + ".");

        while (headSource.hasNext()) {
            HeadEntry entry = headSource.read();
            if (acceptHead.apply(entry)) {
                headSink.write(entry);
            } else {
                rejected.add(entry.getHeadId());
            }
            if ((headSource.getCount() % PROGRESS_INTERVAL == 0 || !headSource.
                    hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + headSource.getCount()
                        + " head entries."
                        + "(" + (int) headSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }
        headSink.flush();
        headSink.close();
        headFilterRequired = false;
        activeHeadsFile = outputFile;

        // Update the feature acceptance predicate
        if (rejected.size() > 0) {
            featureFilterRequired = true;
            acceptFeature = and(acceptFeature,
                    compose(not(in(rejected)), featureHeadId()));
        }
    }

    // Filter the features file, rejecting all entires that contain heads
    // dropped in the heads file filter pass. Store a list of contexts that
    // only appear in filtered heads to filter the contexts file.
    private void filterFeatures()
            throws FileNotFoundException, IOException {
        IntSet acceptedHeads = new IntOpenHashSet();
        IntSet rejectedHeads = new IntOpenHashSet();

        IntSet rejectedContexts = new IntOpenHashSet();
        IntSet acceptedContexts = new IntOpenHashSet();

        FeatureSource featuresSource = new FeatureSource(
                activeFeaturesFile, charset, headIndex, contextIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        FeatureSink featuresSink = new FeatureSink(
                outputFile, charset,
                headIndex, contextIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering features from " + activeFeaturesFile + " to " + outputFile + ".");

        while (featuresSource.hasNext()) {
            FeatureEntry entry = featuresSource.read();
            if (acceptFeature.apply(entry)) {
                featuresSink.write(entry);
                acceptedContexts.add(entry.getContextId());
                acceptedHeads.add(entry.getHeadId());
            } else {
                rejectedContexts.add(entry.getContextId());
                rejectedHeads.add(entry.getHeadId());
            }


            if ((featuresSource.getCount() % PROGRESS_INTERVAL == 0
                    || !featuresSource.hasNext()) && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + featuresSource.getCount() + " feature entries."
                        + "(" + (int) featuresSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }
        featuresSink.flush();
        featuresSink.close();
        featureFilterRequired = false;
        activeFeaturesFile = outputFile;

        rejectedContexts.removeAll(acceptedContexts);
        rejectedHeads.removeAll(acceptedHeads);

        if (rejectedHeads.size() > 0) {
            acceptHead = and(acceptHead,
                    compose(not(in(rejectedHeads)), headId()));
            headFilterRequired = true;
        }

        if (rejectedContexts.size() > 0) {
            acceptContext = and(acceptContext,
                    not(compose(in(rejectedContexts), contextId())));
            contextFilterRequired = true;

        }
    }

    // Filter the contexts file, rejecting all entries that where found to
    // be only used by filtered heads.
    private void filterContexts()
            throws FileNotFoundException, IOException {
        IntSet rejectedContexts = new IntOpenHashSet();

        ContextSource contextSource = new ContextSource(
                activeContextsFile, charset, contextIndex);

        File outputFile = tempFiles.createFile();
        outputFile.deleteOnExit();

        ContextSink contextSink = new ContextSink(
                outputFile, charset, contextIndex);

        if (LOG.isInfoEnabled())
            LOG.info(
                    "Filtering contexts from " + activeContextsFile + " to " + outputFile + ".");

        while (contextSource.hasNext()) {
            ContextEntry entry = contextSource.read();

            if (acceptContext.apply(entry)) {
                contextSink.write(entry);
            } else {
                rejectedContexts.add(entry.getId());
            }

            if ((contextSource.getCount() % PROGRESS_INTERVAL == 0
                    || !contextSource.hasNext())
                    && LOG.isInfoEnabled()) {
                LOG.info(
                        "Read " + contextSource.getCount()
                        + " context entries."
                        + "(" + (int) contextSource.percentRead() + "% complete)");
                LOG.debug(MiscUtil.memoryInfoString());
            }
        }
        contextSink.flush();
        contextSink.close();
        contextFilterRequired = false;
        activeContextsFile = outputFile;

        // Update the feature acceptance predicate
        if (rejectedContexts.size() > 0) {

            featureFilterRequired = true;
            acceptFeature = and(
                    acceptFeature,
                    compose(not(in(rejectedContexts)), featureContextId()));

        }
    }

    @Override
    public String toString() {
        return "FilterTask{"
                + "inputFeatures=" + inputFeatures
                + ", inputHeads=" + inputHeads
                + ", inputContexts=" + inputContexts
                + ", outputFeatures=" + outputFeatures
                + ", outputHeads=" + outputHeads
                + ", outputContexts=" + outputContexts
                + ", charset=" + charset
                + ", filters={"
                + "HeadMinFreq=" + filterHeadMinFreq
                + ", HeadWhitelist=" + filterHeadWhitelist
                + ", HeadPattern=" + filterHeadPattern
                + ", FeatureMinFreq=" + filterFeatureMinFreq
                + ", ContextMinFreq=" + filterContextMinFreq
                + ", ContextWhitelist=" + filterContextWhitelist
                + ", ContextPattern=" + filterContextPattern
                + "}"
                + ", acceptHead=" + acceptHead
                + ", acceptFeature=" + acceptFeature
                + ", acceptContext=" + acceptContext
                + '}';
    }

    @Override
    protected void finaliseTask() throws Exception {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        this.charset = checkNotNull(charset);
    }

    public final File getInputContexts() {
        return inputContexts;
    }

    public final void setInputContexts(File inputContexts) {
        this.inputContexts = checkNotNull(inputContexts);
    }

    public final File getInputFeatures() {
        return inputFeatures;
    }

    public final void setInputFeatures(File inputFeatures) {
        this.inputFeatures = checkNotNull(inputFeatures);
    }

    public final File getInputHeads() {
        return inputHeads;
    }

    public final void setInputHeads(File inputHeads) {
        this.inputHeads = checkNotNull(inputHeads);
    }

    public final File getOutputContexts() {
        return outputContexts;
    }

    public final void setOutputContexts(File outputContexts) {
        this.outputContexts = checkNotNull(outputContexts);
    }

    public final File getOutputFeatures() {
        return outputFeatures;
    }

    public final void setOutputFeatures(File outputFeatures) {
        this.outputFeatures = checkNotNull(outputFeatures);
    }

    public final File getOutputHeads() {
        return outputHeads;
    }

    public final void setOutputHeads(File outputHeads) {
        this.outputHeads = checkNotNull(outputHeads);
    }

    public Predicate<ContextEntry> getAcceptContext() {
        return acceptContext;
    }

    public void setAcceptContext(Predicate<ContextEntry> acceptContext) {
        if (!acceptContext.equals(this.acceptContext)) {
            this.acceptContext = acceptContext;
            contextFilterRequired = true;
        }
    }

    public void addContextMinimumFrequency(double threshold) {
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(gte(threshold), contextFreq())));
    }

    public void addContextMaximumFrequency(double threshold) {
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(lte(threshold), contextFreq())));
    }

    public void addContextFrequencyRange(double min, double max) {
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(inRange(min, max), contextFreq())));
    }

    public void addContextPattern(String pattern) {
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(containsPattern(pattern), contextString())));
    }

    public void addContextWhitelist(List<String> strings) {
        IntSet contextIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = contextIndex.get(string);
            contextIdSet.add(id);
        }
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(in(contextIdSet), contextId())));

    }

    public void addContextBlacklist(List<String> strings) {
        IntSet contextIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = contextIndex.get(string);
            contextIdSet.add(id);
        }
        setAcceptContext(Predicates2.<ContextEntry>and(
                getAcceptContext(),
                compose(not(in(contextIdSet)), contextId())));

    }

    public Predicate<FeatureEntry> getAcceptFeature() {
        return acceptFeature;
    }

    public void setAcceptFeature(Predicate<FeatureEntry> acceptFeature) {
        if (!acceptFeature.equals(this.acceptFeature)) {
            this.acceptFeature = acceptFeature;
            featureFilterRequired = true;
        }
    }

    public void addFeatureMinimumFrequency(double threshold) {
        setAcceptFeature(Predicates2.<FeatureEntry>and(
                getAcceptFeature(),
                compose(gte(threshold), featureFreq())));
    }

    public void addFeatureMaximumFrequency(double threshold) {
        setAcceptFeature(Predicates2.<FeatureEntry>and(
                getAcceptFeature(),
                compose(lte(threshold), featureFreq())));
    }

    public void addFeatureFrequencyRange(double min, double max) {
        setAcceptFeature(Predicates2.<FeatureEntry>and(
                getAcceptFeature(),
                compose(inRange(min, max), featureFreq())));
    }

    public Predicate<HeadEntry> getAcceptHead() {
        return acceptHead;
    }

    public void setAcceptHead(Predicate<HeadEntry> acceptHead) {
        if (!acceptHead.equals(this.acceptHead)) {
            this.acceptHead = acceptHead;
            headFilterRequired = true;
        }
    }

    public void addHeadMinimumFrequency(double threshold) {
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(gte(threshold), headFreq())));
    }

    public void addHeadMaximumFrequency(double threshold) {
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(lte(threshold), headFreq())));
    }

    public void addHeadFrequencyRange(double min, double max) {
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(inRange(min, max), headFreq())));
    }

    public void addHeadPattern(String pattern) {
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(containsPattern(pattern), headString())));
    }

    public void addHeadWhitelist(List<String> strings) {
        IntSet headIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = headIndex.get(string);
            headIdSet.add(id);
        }
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(in(headIdSet), headId())));

    }

    public void addHeadBlacklist(List<String> strings) {
        IntSet headIdSet = new IntOpenHashSet();
        for (String string : strings) {
            final int id = headIndex.get(string);
            headIdSet.add(id);
        }
        setAcceptHead(Predicates2.<HeadEntry>and(
                getAcceptHead(),
                compose(not(in(headIdSet)), headId())));

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
        inputFiles.put("inputHeads", inputHeads);
        inputFiles.put("inputContexts", inputContexts);
        inputFiles.put("inputFeatures", inputFeatures);
        final Map<String, File> outputFiles = new HashMap<String, File>();
        outputFiles.put("outputHeads", outputHeads);
        outputFiles.put("outputContexts", outputContexts);
        outputFiles.put("outputFeatures", outputFeatures);
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
    //
    //
    // ===================================================================
    //
    //
    private Function<HeadEntry, Double> headFreq() {
        return new Function<HeadEntry, Double>() {

            @Override
            public Double apply(HeadEntry input) {
                return input.getTotal();
            }

            @Override
            public String toString() {
                return "HeadFrequency";
            }
        };
    }

    private Function<FeatureEntry, Double> featureFreq() {
        return new Function<FeatureEntry, Double>() {

            @Override
            public Double apply(FeatureEntry input) {
                return input.getWeight();
            }

            @Override
            public String toString() {
                return "FeatureFrequency";
            }
        };
    }

    private Function<ContextEntry, Double> contextFreq() {
        return new Function<ContextEntry, Double>() {

            @Override
            public Double apply(ContextEntry input) {
                return input.getWeight();
            }

            @Override
            public String toString() {
                return "ContextFrequency";
            }
        };
    }

    private Function<ContextEntry, Integer> contextId() {
        return new Function<ContextEntry, Integer>() {

            @Override
            public Integer apply(ContextEntry input) {
                return input.getId();
            }

            @Override
            public String toString() {
                return "ContextID";
            }
        };
    }

    private Function<HeadEntry, Integer> headId() {
        return new Function<HeadEntry, Integer>() {

            @Override
            public Integer apply(HeadEntry input) {
                return input.getHeadId();
            }

            @Override
            public String toString() {
                return "HeadID";
            }
        };
    }

    private Function<FeatureEntry, Integer> featureHeadId() {
        return new Function<FeatureEntry, Integer>() {

            @Override
            public Integer apply(FeatureEntry input) {
                return input.getHeadId();
            }

            @Override
            public String toString() {
                return "HeadID";
            }
        };
    }

    private Function<FeatureEntry, Integer> featureContextId() {
        return new Function<FeatureEntry, Integer>() {

            @Override
            public Integer apply(FeatureEntry input) {
                return input.getContextId();
            }

            @Override
            public String toString() {
                return "ContextID";
            }
        };
    }

    private Function<HeadEntry, String> headString() {
        return new Function<HeadEntry, String>() {

            @Override
            public String apply(HeadEntry input) {
                return headIndex.get(input.getHeadId());
            }

            @Override
            public String toString() {
                return "HeadString";
            }
        };
    }

    private Function<ContextEntry, String> contextString() {
        return new Function<ContextEntry, String>() {

            @Override
            public String apply(ContextEntry input) {
                return contextIndex.get(input.getId());
            }

            @Override
            public String toString() {
                return "ContextString";
            }
        };
    }

    private Function<FeatureEntry, String> featureHeadString() {
        return new Function<FeatureEntry, String>() {

            @Override
            public String apply(FeatureEntry input) {
                return headIndex.get(input.getHeadId());
            }

            @Override
            public String toString() {
                return "HeadString";
            }
        };
    }

    private Function<FeatureEntry, String> featureContextString() {
        return new Function<FeatureEntry, String>() {

            @Override
            public String apply(FeatureEntry input) {
                return contextIndex.get(input.getContextId());
            }

            @Override
            public String toString() {
                return "ContextString";
            }
        };
    }
}

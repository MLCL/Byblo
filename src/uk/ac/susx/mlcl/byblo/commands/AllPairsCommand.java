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

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.measure.*;
import uk.ac.susx.mlcl.byblo.tasks.InvertedApssTask;
import uk.ac.susx.mlcl.byblo.tasks.NaiveApssTask;
import uk.ac.susx.mlcl.byblo.tasks.ThreadedApssTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.DoubleConverter;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.FileDeligate;
import uk.ac.susx.mlcl.lib.commands.InputFileValidator;
import uk.ac.susx.mlcl.lib.commands.OutputFileValidator;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform all-pair similarity search on the given input frequency files.")
public class AllPairsCommand extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(AllPairsCommand.class);

    @ParametersDelegate
    private DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

    @ParametersDelegate
    private FileDeligate fileDeligate = new FileDeligate();

    @Parameter(names = {"-i", "--input"},
    description = "Entry-feature frequency vectors files.",
    required = true,
    validateWith = InputFileValidator.class)
    private File entryFeaturesFile;

    @Parameter(names = {"-if", "--input-features"},
    description = "Feature frequencies file",
    validateWith = InputFileValidator.class)
    private File featuresFile;

    @Parameter(names = {"-ie", "--input-entries"},
    description = "Entry frequencies file",
    validateWith = InputFileValidator.class)
    private File entriesFile;

    @Parameter(names = {"-o", "--output"},
    description = "Output similarity matrix file.",
    required = true,
    validateWith = OutputFileValidator.class)
    private File outputFile;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of entries to compare per work unit. Larger value increase performance and memory usage.")
    private int chunkSize = 2500;

    @Parameter(names = {"-t", "--threads"},
    description = "Number of conccurent processing threads.")
    private int nThreads = Runtime.getRuntime().availableProcessors() + 1;

    @Parameter(names = {"-Smn", "--similarity-min"},
    description = "Minimum similarity threshold.",
    converter = DoubleConverter.class)
    private double minSimilarity = Double.NEGATIVE_INFINITY;

    @Parameter(names = {"-Smx", "--similarity-max"},
    description = "Maximyum similarity threshold.",
    converter = DoubleConverter.class)
    private double maxSimilarity = Double.POSITIVE_INFINITY;

    @Parameter(names = {"-ip", "--identity-pairs"},
    description = "Produce similarity between pair of identical entries.")
    private boolean outputIdentityPairs = false;

    @Parameter(names = {"-m", "--measure"},
    description = "Similarity measure to use.")
    private String measureName = "Jaccard";

    @Parameter(names = {"--measure-reversed"},
    description = "Swap similarity measure inputs.")
    private boolean measureReversed = false;

    @Parameter(names = {"--lee-alpha"},
    description = "Alpha parameter to Lee's alpha-skew divergence measure.",
    converter = DoubleConverter.class)
    private double leeAlpha = Lee.DEFAULT_ALPHA;

    @Parameter(names = {"--crmi-beta"},
    description = "Beta paramter to Weed's CRMI measure.",
    converter = DoubleConverter.class)
    private double crmiBeta = CrMi.DEFAULT_BETA;

    @Parameter(names = {"--crmi-gamma"},
    description = "Gamma paramter to Weed's CRMI measure.",
    converter = DoubleConverter.class)
    private double crmiGamma = CrMi.DEFAULT_GAMMA;

    @Parameter(names = {"--mink-p"},
    description = "P parameter to Minkowski/Lp space measure.",
    converter = DoubleConverter.class)
    private double minkP = 2;

    public enum Algorithm {

        Naive(NaiveApssTask.class),
        Inverted(InvertedApssTask.class);

        private Class<? extends NaiveApssTask> implementation;

        private Algorithm(Class<? extends NaiveApssTask> imp) {
            this.implementation = imp;
            Object o = InvertedApssTask.class;
        }

        public Class<? extends NaiveApssTask> getImplementation() {
            return implementation;
        }

        public NaiveApssTask newInstance() throws InstantiationException, IllegalAccessException {
            return getImplementation().newInstance();
        }

    }

    @Parameter(names = {"--algorithm"},
    hidden = true,
    description = "APPS algorithm to use.")
    private Algorithm algorithm = Algorithm.Inverted;

    public AllPairsCommand(File entriesFile, File featuresFile,
                           File entryFeaturesFile, File outputFile,
                           Charset charset, DoubleEnumerating indexDeligate) {
        setEntryFeaturesFile(entryFeaturesFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
        setOutputFile(outputFile);
        setCharset(charset);
        this.indexDeligate = indexDeligate;
    }

    public AllPairsCommand() {
    }

    @Override
    public void runCommand() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Running all-pairs similarity search from \"" + getEntryFeaturesFile() + "\" to \"" + getOutputFile() + "\"");
        }

        // Instantiate the denote proxmity measure
        Proximity prox = getMeasureClass().newInstance();

        // Parameterise those measures that require them
        if (prox instanceof Lp) {
            ((Lp) prox).setP(getMinkP());
        } else if (prox instanceof Lee) {
            ((Lee) prox).setAlpha(getLeeAlpha());
        } else if (prox instanceof CrMi) {
            ((CrMi) prox).setBeta(getCrmiBeta());
            ((CrMi) prox).setGamma(getCrmiGamma());
        }

        // Mutual Information based proximity measures require the frequencies
        // of each feature, and other associate values
        if (prox instanceof AbstractMIProximity) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Loading features file " + getFeaturesFile());
                }

                WeightedTokenSource features = openFeaturesSource();

                AbstractMIProximity bmip = ((AbstractMIProximity) prox);
                bmip.setFeatureFrequencies(features.readAllAsArray());
                bmip.setFeatureFrequencySum(features.getWeightSum());
                bmip.setOccuringFeatureCount(features.getCardinality());

            } catch (IOException e) {
                throw e;
            }
        } else if (prox instanceof KendallTau) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Loading entries file for "
                            + "KendalTau.numFeatures: " + getFeaturesFile());
                }

                WeightedTokenSource features = openFeaturesSource();
                features.readAll();

                ((KendallTau) prox).setNumFeatures(features.getCardinality());

            } catch (IOException e) {
                throw e;
            }
        }
        //XXX This needs to be sorted out --- filter id must be read from the
        // stored enumeration, for optimal robustness
        prox.setFilteredFeatureId(FilterCommand.FILTERED_ID);

        // Swap the proximity measure inputs if required
        if (isMeasureReversed()) {
            prox = new ReversedProximity(prox);
        }


        // Instantiate two vector source objects than can scan and read the
        // main db. We need two because the algorithm takes all pairwise
        // combinations of vectors, so will be looking at two differnt points
        // in the file. Also this allows for the possibility of having differnt
        // files, e.g compare fruit words with cake words
        final WeightedTokenPairVectorSource sourceA = openEventsSource();
        final WeightedTokenPairVectorSource sourceB = openEventsSource();


        // Create a sink object that will act as a recipient for all pairs that
        // are produced by the algorithm.

        final Sink<Weighted<TokenPair>> sink = openSimsSink();

        final NaiveApssTask apss = newAlgorithmInstance();


        // Parameterise the all-pairs algorithm
        apss.setSourceA(sourceA);
        apss.setSourceB(sourceB);
        apss.setSink(sink);
        apss.setMeasure(prox);
        apss.setProducatePair(getProductionFilter());

        apss.run();

        if (sink instanceof Flushable)
            ((Flushable) sink).flush();
        if (sink instanceof Closeable)
            ((Closeable) sink).close();

        if (sourceA instanceof Closeable)
            ((Closeable) sourceA).close();

        if (sourceB instanceof Closeable)
            ((Closeable) sourceB).close();

        if (apss.isExceptionThrown())
            apss.throwException();

        indexDeligate.saveEnumerator();
        indexDeligate.closeEnumerator();

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed all-pairs similarity search.");
        }
    }

    private NaiveApssTask newAlgorithmInstance()
            throws InstantiationException, IllegalAccessException {

        if (getnThreads() == 1) {
            return getAlgorithm().newInstance();
        } else {
            ThreadedApssTask<Tell> tapss = new ThreadedApssTask<Tell>();
            tapss.setInnerAlgorithm(getAlgorithm().getImplementation());
            tapss.setNumThreads(getnThreads());
            tapss.setMaxChunkSize(getChunkSize());
            return tapss;
        }

    }

    private WeightedTokenSource openFeaturesSource() throws IOException {
        return WeightedTokenSource.open(
                getFeaturesFile(), getCharset(),
                EnumeratingDeligates.toSingleFeatures(getIndexDeligate()));

    }

    private WeightedTokenPairVectorSource openEventsSource() throws IOException {
        return WeightedTokenPairSource.open(
                getEntryFeaturesFile(), getCharset(),
                getIndexDeligate()).getVectorSource();
    }

    private WeightedTokenPairSink openSimsSink() throws IOException {
        return WeightedTokenPairSink.open(
                getOutputFile(), getCharset(),
                EnumeratingDeligates.toPair(EnumeratingDeligates.toSingleEntries(getIndexDeligate())),
                fileDeligate.isCompactFormatDisabled());

    }

    private Predicate<Weighted<TokenPair>> getProductionFilter() {
        List<Predicate<Weighted<TokenPair>>> pairFilters =
                new ArrayList<Predicate<Weighted<TokenPair>>>();

        if (getMinSimilarity() != Double.NEGATIVE_INFINITY) {
            pairFilters.add(Weighted.<TokenPair>greaterThanOrEqualTo(
                    getMinSimilarity()));
        }

        if (getMaxSimilarity() != Double.POSITIVE_INFINITY) {
            pairFilters.add(Weighted.<TokenPair>lessThanOrEqualTo(
                    getMaxSimilarity()));
        }

        if (!isOutputIdentityPairs()) {
            pairFilters.add(Predicates.not(Predicates.compose(
                    TokenPair.identity(), Weighted.<TokenPair>recordFunction())));
        }

        if (pairFilters.size() == 1) {
            return pairFilters.get(0);
        } else if (pairFilters.size() > 1) {
            return Predicates.<Weighted<TokenPair>>and(pairFilters);
        } else {
            return Predicates.alwaysTrue();
        }
    }

    public final Class<? extends Proximity> getMeasureClass()
            throws ClassNotFoundException {
        final Map<String, Class<? extends Proximity>> classLookup =
                buildMeasureClassLookupTable();
        final String mname = getMeasureName().toLowerCase().trim();
        if (classLookup.containsKey(mname)) {
            return classLookup.get(mname);
        } else {
            return (Class<? extends Proximity>) Class.forName(
                    getMeasureName());
        }
    }

    private Map<String, Class<? extends Proximity>> buildMeasureClassLookupTable() throws ClassNotFoundException {

        // Map that will store measure aliases to class
        final Map<String, Class<? extends Proximity>> classLookup =
                new HashMap<String, Class<? extends Proximity>>();

        final ResourceBundle res = ResourceBundle.getBundle(
                "uk.ac.susx.mlcl.byblo.measure.measures");
        final String[] measures = res.getString("measures").split(",");

        for (int i = 0; i < measures.length; i++) {
            final String measure = measures[i].trim();
            final String className = res.getString(
                    "measure." + measure + ".class");
            @SuppressWarnings("unchecked")
            final Class<? extends Proximity> clazz =
                    (Class<? extends Proximity>) Class.forName(className);
            classLookup.put(measure.toLowerCase(), clazz);
            if (res.containsKey("measure." + measure + ".aliases")) {
                final String[] aliases = res.getString(
                        "measure." + measure + ".aliases").split(",");
                for (String alias : aliases) {
                    classLookup.put(alias.toLowerCase().trim(), clazz);
                }
            }
        }
        return classLookup;
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("eventsIn", getEntryFeaturesFile()).
                add("entriesIn", getEntriesFile()).
                add("featuresIn", getFeaturesFile()).
                add("simsOut", getOutputFile()).
                add("charset", getCharset()).
                add("chunkSize", getChunkSize()).
                add("threads", getnThreads()).
                add("minSimilarity", getMinSimilarity()).
                add("maxSimilarity", getMaxSimilarity()).
                add("outputIdentityPairs", isOutputIdentityPairs()).
                add("measure", getMeasureName()).
                add("measureReversed", isMeasureReversed()).
                add("leeAlpha", getLeeAlpha()).
                add("crmiBeta", getCrmiBeta()).
                add("crmiGamma", getCrmiGamma()).
                add("minkP", getMinkP());
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        Checks.checkNotNull("algorithm", algorithm);
        this.algorithm = algorithm;
    }

    public final File getEntryFeaturesFile() {
        return entryFeaturesFile;
    }

    public final void setEntryFeaturesFile(File entryFeaturesFile) {
        Checks.checkNotNull("entryFeaturesFile", entryFeaturesFile);
        this.entryFeaturesFile = entryFeaturesFile;
    }

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(File featuresFile) {
        Checks.checkNotNull("featuresFile", featuresFile);
        this.featuresFile = featuresFile;
    }

    public File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(File entriesFile) {
        Checks.checkNotNull("entriesFile", entriesFile);
        this.entriesFile = entriesFile;
    }

    public final File getOutputFile() {
        return outputFile;
    }

    public final void setOutputFile(File outputFile) {
        Checks.checkNotNull("outputFile", outputFile);
        this.outputFile = outputFile;
    }

    public final Charset getCharset() {
        return fileDeligate.getCharset();
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull("charset", charset);
        this.fileDeligate.setCharset(charset);
    }

    public final int getChunkSize() {
        return chunkSize;
    }

    public void setCompactFormatDisabled(boolean compactFormatDisabled) {
        fileDeligate.setCompactFormatDisabled(compactFormatDisabled);
    }

    public boolean isCompactFormatDisabled() {
        return fileDeligate.isCompactFormatDisabled();
    }

    public final void setChunkSize(int chunkSize) {
        Checks.checkRangeIncl("chunkSize", chunkSize, 1, Integer.MAX_VALUE);
        this.chunkSize = chunkSize;
    }

    public final int getnThreads() {
        return nThreads;
    }

    public final void setnThreads(int nThreads) {
        Checks.checkRangeIncl("nThreads", nThreads, 1, Integer.MAX_VALUE);
        this.nThreads = nThreads;
    }

    public final double getMinSimilarity() {
        return minSimilarity;
    }

    public final void setMinSimilarity(double minSimilarity) {
        Checks.checkRangeIncl("minSimilarity", minSimilarity, 0,
                              Double.POSITIVE_INFINITY);
        this.minSimilarity = minSimilarity;
    }

    public final double getMaxSimilarity() {
        return maxSimilarity;
    }

    public final void setMaxSimilarity(double maxSimilarity) {
        Checks.checkRangeIncl("maxSimilarity", maxSimilarity, 0,
                              Double.POSITIVE_INFINITY);
        this.maxSimilarity = maxSimilarity;
    }

    public final boolean isOutputIdentityPairs() {
        return outputIdentityPairs;
    }

    public final void setOutputIdentityPairs(boolean outputIdentityPairs) {
        this.outputIdentityPairs = outputIdentityPairs;
    }

    public final String getMeasureName() {
        return measureName;
    }

    public final void setMeasureName(String measureName) {
        Checks.checkNotNull("measureName", measureName);
        this.measureName = measureName;
    }

    public final boolean isMeasureReversed() {
        return measureReversed;
    }

    public final void setMeasureReversed(boolean measureReversed) {
        this.measureReversed = measureReversed;
    }

    public final double getLeeAlpha() {
        return leeAlpha;
    }

    public final void setLeeAlpha(double leeAlpha) {
        Checks.checkRangeIncl("leeAlpha", leeAlpha, 0, 1);
        this.leeAlpha = leeAlpha;
    }

    public final double getCrmiBeta() {
        return crmiBeta;
    }

    public final void setCrmiBeta(double crmiBeta) {
        this.crmiBeta = crmiBeta;
    }

    public final double getCrmiGamma() {
        return crmiGamma;
    }

    public final void setCrmiGamma(double crmiGamma) {
        this.crmiGamma = crmiGamma;
    }

    public final double getMinkP() {
        return minkP;
    }

    public final void setMinkP(double minkP) {
        this.minkP = minkP;
    }

    public final DoubleEnumerating getIndexDeligate() {
        return indexDeligate;
    }

    public final void setIndexDeligate(DoubleEnumerating indexDeligate) {
        Checks.checkNotNull("indexDeligate", indexDeligate);
        this.indexDeligate = indexDeligate;
    }

}

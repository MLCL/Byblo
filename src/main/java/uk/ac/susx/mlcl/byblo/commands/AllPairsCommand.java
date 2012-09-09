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
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.BybloSettings;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratorType;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource.WTStatsSource;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.measures.Measures;
import uk.ac.susx.mlcl.byblo.measures.impl.*;
import uk.ac.susx.mlcl.byblo.tasks.InvertedApssTask;
import uk.ac.susx.mlcl.byblo.tasks.NaiveApssTask;
import uk.ac.susx.mlcl.byblo.tasks.ThreadedApssTask;
import uk.ac.susx.mlcl.byblo.weighings.FeatureMarginalsCarrier;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.commands.*;
import uk.ac.susx.mlcl.lib.events.ReportLoggingProgressListener;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.io.Tell;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Perform all-pair similarity search on the given input frequency files.")
public class AllPairsCommand extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(AllPairsCommand.class);

    @ParametersDelegate
    private DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

    @ParametersDelegate
    private FileDelegate fileDelegate = new FileDelegate();

    public static final int DEFAULT_MINK_P = 2;

    @Parameter(names = {"-i", "--input"},
            description = "Event frequency vectors files.",
            required = true,
            validateWith = InputFileValidator.class)
    private File eventsFile;

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

    @Parameter(names = {"-t", "--threads"},
            description = "Number of concurrent processing threads.")
    private int numThreads = Runtime.getRuntime().availableProcessors() + 1;

    public static final double DEFAULT_MIN_SIMILARITY = Double.NEGATIVE_INFINITY;

    public static final double DEFAULT_MAX_SIMILARITY = Double.POSITIVE_INFINITY;

    @Parameter(names = {"-Smn", "--similarity-min"},
            description = "Minimum similarity threshold.",
            converter = DoubleConverter.class)
    private double minSimilarity = DEFAULT_MIN_SIMILARITY;

    @Parameter(names = {"-Smx", "--similarity-max"},
            description = "Maximum similarity threshold.",
            converter = DoubleConverter.class)
    private double maxSimilarity = DEFAULT_MAX_SIMILARITY;

    @Parameter(names = {"-ip", "--identity-pairs"},
            description = "Produce similarity between pair of identical entries.")
    private boolean outputIdentityPairs = false;

    public static final String DEFAULT_MEASURE = "Lin";

    @Parameter(names = {"-m", "--measure"},
            description = "Similarity measure to use.")
    private String measureName = DEFAULT_MEASURE;

    @Parameter(names = {"--measure-reversed"},
            description = "Swap similarity measure inputs.")
    private boolean measureReversed = false;

    @Parameter(names = {"--lee-alpha"},
            description = "Alpha parameter to Lee's alpha-skew divergence measure.",
            converter = DoubleConverter.class)
    private double leeAlpha = LeeSkewDivergence.DEFAULT_ALPHA;

    @Parameter(names = {"--crmi-beta"},
            description = "Beta paramter to Weed's CRMI measure.",
            converter = DoubleConverter.class)
    private double crmiBeta = Weeds.DEFAULT_BETA;

    @Parameter(names = {"--crmi-gamma"},
            description = "Gamma paramter to Weed's CRMI measure.",
            converter = DoubleConverter.class)
    private double crmiGamma = Weeds.DEFAULT_GAMMA;

    @Parameter(names = {"--mink-p"},
            description = "P parameter to Minkowski/Lp space measure.",
            converter = DoubleConverter.class)
    private double minkP = LpSpaceDistance.DEFAULT_POWER;

    @Parameter(names = {"--lambda-lambda"},
            description = "lambda parameter to Lambda-Divergence measure.",
            converter = DoubleConverter.class)
    private double lambdaLambda = LambdaDivergence.DEFAULT_LAMBDA;

    private Weighting weighting = new NullWeighting();

    public enum Algorithm {

        Naive(NaiveApssTask.class),
        Inverted(InvertedApssTask.class);

        private Class<? extends NaiveApssTask> implementation;

        private Algorithm(Class<? extends NaiveApssTask> imp) {
            this.implementation = imp;
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
                           File eventsFile, File outputFile,
                           Charset charset, DoubleEnumerating indexDelegate) {
        setEventsFile(eventsFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
        setOutputFile(outputFile);
        setCharset(charset);
        this.indexDelegate = indexDelegate;
    }

    public AllPairsCommand() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void runCommand() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info("Running all-pairs similarity.");
        }

        // Instantiate the denote proxmity measure
        Measure measure = getMeasureClass().newInstance();

        // Parameterise those measures that require them
        if (measure instanceof LpSpaceDistance) {
            ((LpSpaceDistance) measure).setPower(getMinkP());
        }
        if (measure instanceof LeeSkewDivergence) {
            ((LeeSkewDivergence) measure).setAlpha(getLeeAlpha());
        }
        if (measure instanceof Weeds) {
            ((Weeds) measure).setBeta(getCrmiBeta());
            ((Weeds) measure).setGamma(getCrmiGamma());
        }
        if (measure instanceof LambdaDivergence) {
            ((LambdaDivergence) measure).setLambda(getLambdaLambda());
        }


        if (weighting.getClass().equals(NullWeighting.class)) {
            weighting = measure.getExpectedWeighting().newInstance();
        } else {
            weighting = Weightings.compose(
                    weighting,
                    measure.getExpectedWeighting().newInstance());
        }


        /*
         * Some weightings schemes require additional context information,
         * specifically the feature marginal distribution. Also there is one
         * measure (Confusion) that requires this information. The KendallsTau
         * and KullbackLeiblerDivergence measure just needs the total number of
         * features types.
         */
        if (measure instanceof FeatureMarginalsCarrier
                || weighting instanceof FeatureMarginalsCarrier) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Loading features file " + getFeaturesFile());
                }

                WTStatsSource features = new WTStatsSource(openFeaturesSource());

                final double[] featureMarginals = readAllAsArray(features);
                final double grandTotal = features.getWeightSum();
                final int featureCardinality = features.getMaxId() + 1;

                if (measure instanceof FeatureMarginalsCarrier) {
                    FeatureMarginalsCarrier fmc = ((FeatureMarginalsCarrier) measure);
                    fmc.setFeatureMarginals(featureMarginals);
                    fmc.setGrandTotal(grandTotal);
                    fmc.setFeatureCardinality(featureCardinality);
                }

                if (weighting instanceof FeatureMarginalsCarrier) {
                    FeatureMarginalsCarrier fmc = ((FeatureMarginalsCarrier) weighting);
                    fmc.setFeatureMarginals(featureMarginals);
                    fmc.setGrandTotal(grandTotal);
                    fmc.setFeatureCardinality(featureCardinality);
                }


            } catch (IOException e) {
                throw e;
            }
        } else if (measure instanceof KendallsTau
                || measure instanceof KullbackLeiblerDivergence) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.
                            info("Loading entries file for "
                                    + "KendallsTau.minCardinality: " + getFeaturesFile());
                }

                WTStatsSource features = new WTStatsSource(openFeaturesSource());
                ObjectIO.copy(features, ObjectIO.<Weighted<Token>>nullSink());


                int cardinality = features.getMaxId() + 1;

                if (measure instanceof KendallsTau)
                    ((KendallsTau) measure).setMinCardinality(cardinality);
                if (measure instanceof KullbackLeiblerDivergence)
                    ((KullbackLeiblerDivergence) measure).setMinCardinality(
                            cardinality);

            } catch (IOException e) {
                throw e;
            }
        }
//        //XXX This needs to be sorted out --- filter id must be read from the
//        // stored enumeration, for optimal robustness
//        measure.setFilteredFeatureId(FilterCommand.FILTERED_ID);

        // Swap the proximity measure inputs if required
        if (isMeasureReversed()) {
            measure = Measures.reverse(measure);
        }

        // XXX: Apply the weighing at similarity calculation time, 
        // inefficient but simple for now.
        measure = Measures.autoWeighted(measure, weighting);


        // Instantiate two vector source objects than can scan and read the
        // main db. We need two because the algorithm takes all pairwise
        // combinations of vectors, so will be looking at two different points
        // in the file. Also this allows for the possibility of having different
        // files, e.g compare fruit words with cake words
        final FastWeightedTokenPairVectorSource sourceA = openEventsSource();
        final FastWeightedTokenPairVectorSource sourceB = openEventsSource();


        // Create a sink object that will act as a recipient for all pairs that
        // are produced by the algorithm.

        WeightedTokenPairSink sink = openSimsSink();


        final NaiveApssTask apss = newAlgorithmInstance();


        // Parameterise the all-pairs algorithm
        apss.setSourceA(sourceA);
        apss.setSourceB(sourceB);
        apss.setSink(sink);
        apss.setMeasure(measure);
        apss.setProducatePair(getProductionFilter());


        apss.addProgressListener(new ReportLoggingProgressListener(LOG));

        apss.run();

        sink.flush();
        sink.close();

        if (sourceA instanceof Closeable)
            ((Closeable) sourceA).close();

        if (sourceB instanceof Closeable)
            ((Closeable) sourceB).close();

        if (apss.isExceptionTrapped())
            apss.throwTrappedException();

        if (indexDelegate.isEnumeratorOpen()) {
            indexDelegate.saveEnumerator();
            indexDelegate.closeEnumerator();
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed all-pairs similarity search.");
        }
    }

    public static double[] readAllAsArray(ObjectSource<Weighted<Token>> src) throws IOException {

        Int2DoubleMap entityFrequenciesMap = new Int2DoubleOpenHashMap();
        while (src.hasNext()) {
            Weighted<Token> entry = src.read();
            if (entityFrequenciesMap.containsKey(entry.record().id())) {
                // If we encounter the same Token more than once, it means
                // the perl has decided two strings are not-equal, which java
                // thinks are equal ... so we need to merge the records:

                // TODO: Not true any more.. remove this code?

                final int id = entry.record().id();
                final double oldFreq = entityFrequenciesMap.get(id);
                final double newFreq = oldFreq + entry.weight();

                if (LOG.isWarnEnabled())
                    LOG.warn(MessageFormat.format(
                            "Found duplicate entry (id={0}); merging records. Frequency before: {1}, after: {2}.",
                            entry.record().id(), oldFreq, newFreq));

                entityFrequenciesMap.put(id, newFreq);
            } else {
                entityFrequenciesMap.put(entry.record().id(), entry.weight());
            }
        }

        int maxId = 0;
        for (int k : entityFrequenciesMap.keySet()) {
            if (k > maxId)
                maxId = k;
        }
        double[] entryFreqs = new double[maxId + 1];
        ObjectIterator<Int2DoubleMap.Entry> it = entityFrequenciesMap.
                int2DoubleEntrySet().
                iterator();
        while (it.hasNext()) {
            Int2DoubleMap.Entry entry = it.next();
            entryFreqs[entry.getIntKey()] = entry.getDoubleValue();
        }
        return entryFreqs;
    }

    private NaiveApssTask newAlgorithmInstance()
            throws InstantiationException, IllegalAccessException {

        if (getNumThreads() == 1) {
            return getAlgorithm().newInstance();
        } else {
            ThreadedApssTask<Tell> tapss = new ThreadedApssTask<Tell>();
            tapss.setInnerAlgorithm(getAlgorithm().getImplementation());
            tapss.setNumThreads(getNumThreads());
            return tapss;
        }

    }

    private WeightedTokenSource openFeaturesSource() throws IOException {
        return BybloIO.openFeaturesSource(
                getFeaturesFile(), getCharset(),
                EnumeratingDelegates.toSingleFeatures(getIndexDelegate()));
    }

    private FastWeightedTokenPairVectorSource openEventsSource() throws IOException {
        return BybloIO.openEventsVectorSource(
                getEventsFile(), getCharset(),
                getIndexDelegate());
    }

    private WeightedTokenPairSink openSimsSink() throws IOException {
        return BybloIO.openSimsSink(
                getOutputFile(), getCharset(),
                EnumeratingDelegates.toSingleEntries(getIndexDelegate()));

    }

    private Predicate<Weighted<TokenPair>> getProductionFilter() {
        List<Predicate<Weighted<TokenPair>>> pairFilters =
                new ArrayList<Predicate<Weighted<TokenPair>>>();

        if (getMinSimilarity() != Double.NEGATIVE_INFINITY) {
            pairFilters.add(Weighted.<TokenPair>greaterThanOrEqualTo(getMinSimilarity()));
        }

        if (getMaxSimilarity() != Double.POSITIVE_INFINITY) {
            pairFilters.add(Weighted.<TokenPair>lessThanOrEqualTo(getMaxSimilarity()));
        }

        if (!isOutputIdentityPairs()) {
            pairFilters.add(Predicates.not(Predicates.compose(TokenPair.identity(), Weighted.<TokenPair>recordFunction())));
        }

        if (pairFilters.size() == 1) {
            return pairFilters.get(0);
        } else if (pairFilters.size() > 1) {
            return Predicates.<Weighted<TokenPair>>and(pairFilters);
        } else {
            return Predicates.alwaysTrue();
        }
    }

    @SuppressWarnings("unchecked")
    public final Class<? extends Measure> getMeasureClass()
            throws ClassNotFoundException {
        final Map<String, Class<? extends Measure>> classLookup =
                Measures.loadMeasureAliasTable();

        final String mname = getMeasureName().toLowerCase(BybloSettings.getLocale()).trim();
        if (classLookup.containsKey(mname)) {
            return classLookup.get(mname);
        } else {
            return (Class<? extends Measure>) Class.forName(getMeasureName());
        }
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("eventsIn", getEventsFile()).
                add("entriesIn", getEntriesFile()).
                add("featuresIn", getFeaturesFile()).
                add("simsOut", getOutputFile()).
                add("charset", getCharset()).
                add("threads", getNumThreads()).
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

    public final File getEventsFile() {
        return eventsFile;
    }

    public final void setEventsFile(File eventsFile) {
        Checks.checkNotNull("eventsFile", eventsFile);
        this.eventsFile = eventsFile;
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
        return fileDelegate.getCharset();
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull("charset", charset);
        this.fileDelegate.setCharset(charset);
    }

    public final int getNumThreads() {
        return numThreads;
    }

    public final void setNumThreads(int nThreads) {
        Checks.checkRangeIncl("nThreads", nThreads, 1, Integer.MAX_VALUE);
        this.numThreads = nThreads;
    }

    public final double getMinSimilarity() {
        return minSimilarity;
    }

    public final void setMinSimilarity(double minSimilarity) {
        this.minSimilarity = minSimilarity;
    }

    public final double getMaxSimilarity() {
        return maxSimilarity;
    }

    public final void setMaxSimilarity(double maxSimilarity) {
        Checks.checkRangeIncl("maxSimilarity", maxSimilarity, 0, Double.POSITIVE_INFINITY);
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

    public double getLambdaLambda() {
        return lambdaLambda;
    }

    public void setLambdaLambda(double lambdaLambda) {
        this.lambdaLambda = lambdaLambda;
    }

    public final DoubleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    public final void setIndexDelegate(DoubleEnumerating indexDelegate) {
        Checks.checkNotNull("indexDelegate", indexDelegate);
        this.indexDelegate = indexDelegate;
    }

    public void setEnumeratedFeatures(boolean enumeratedFeatures) {
        indexDelegate.setEnumeratedFeatures(enumeratedFeatures);
    }

    public void setEnumeratedEntries(boolean enumeratedEntries) {
        indexDelegate.setEnumeratedEntries(enumeratedEntries);
    }

    public boolean isEnumeratedFeatures() {
        return indexDelegate.isEnumeratedFeatures();
    }

    public boolean isEnumeratedEntries() {
        return indexDelegate.isEnumeratedEntries();
    }

    public void setEnumeratorType(EnumeratorType type) {
        indexDelegate.setEnumeratorType(type);
    }

    public EnumeratorType getEnumeratorType() {
        return indexDelegate.getEnumeratorType();
    }
}

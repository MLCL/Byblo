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

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import uk.ac.susx.mlcl.byblo.allpairs.InvertedApssTask;
import uk.ac.susx.mlcl.byblo.allpairs.ThreadedApssTask;
import uk.ac.susx.mlcl.byblo.io.ContextSource;
import uk.ac.susx.mlcl.byblo.io.FeatureSource;
import uk.ac.susx.mlcl.byblo.io.FeatureVectorSource;
import uk.ac.susx.mlcl.byblo.io.HeadSource;
import uk.ac.susx.mlcl.byblo.io.WeightedPairSink;
import uk.ac.susx.mlcl.byblo.measure.AbstractMIProximity;
import uk.ac.susx.mlcl.byblo.measure.CrMi;
import uk.ac.susx.mlcl.byblo.measure.KendallTau;
import uk.ac.susx.mlcl.byblo.measure.Lee;
import uk.ac.susx.mlcl.byblo.measure.Lp;
import uk.ac.susx.mlcl.byblo.measure.Proximity;
import uk.ac.susx.mlcl.byblo.measure.ReversedProximity;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.collect.WeightedPair;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.tasks.AbstractTask;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "USAGE_ALL_PAIRS",
            resourceBundle = "uk.ac.susx.mlcl.byblo.strings")
public class AllPairsCommand extends AbstractTask {

    private static final Logger LOG =
            Logger.getLogger(AllPairsCommand.class.getName());

    @Parameter(names = {"-i", "--input"},
               descriptionKey = "USAGE_FEATURE_FILE",
               required = true,
               validateWith = InputFileValidator.class)
    private File featuresFile;

    @Parameter(names = {"--input-contexts"},
               descriptionKey = "USAGE_CONTEXTS_FILE",
               validateWith = InputFileValidator.class)
    private File contextsFile;

    @Parameter(names = {"--input-heads"},
               descriptionKey = "USAGE_HEADS_FILE",
               validateWith = InputFileValidator.class)
    private File headsFile;

    @Parameter(names = {"-o", "--output"},
               descriptionKey = "USAGE_OUTPUT_FILE",
               required = true,
               validateWith = OutputFileValidator.class)
    private File outputFile;

    @Parameter(names = {"--charset"},
               descriptionKey = "USAGE_CHARSET")
    private Charset charset = IOUtil.DEFAULT_CHARSET;

    @Parameter(names = {"-C", "--chunk-size"},
               descriptionKey = "USAGE_CHUNK_SIZE")
    private int chunkSize = 5000;

    @Parameter(names = {"--threads"},
               descriptionKey = "USAGE_NUM_THREADS")
    private int nThreads = Runtime.getRuntime().availableProcessors() + 1;

    @Parameter(names = {"-Smn", "--similarity-min"},
               descriptionKey = "USAGE_MIN_SIMILARITY",
               converter = DoubleConverter.class)
    private double minSimilarity = Double.NEGATIVE_INFINITY;

    @Parameter(names = {"-Smx", "--similarity-max"},
               descriptionKey = "USAGE_MAX_SIMILARITY",
               converter = DoubleConverter.class)
    private double maxSimilarity = Double.POSITIVE_INFINITY;

    @Parameter(names = {"-ip", "--identity-pairs"},
               descriptionKey = "USAGE_IDENTITY_PAIRS")
    private boolean outputIdentityPairs = false;

    @Parameter(names = {"-m", "--measure"},
               descriptionKey = "USAGE_MEASURE")
    private String measureName = "Jaccard";

    @Parameter(names = {"--measure-reversed"},
               descriptionKey = "USAGE_MEASURE_REVERSED")
    private boolean measureReversed = false;

    @Parameter(names = {"--lee-alpha"},
               descriptionKey = "USAGE_LEE_ALPHA",
               converter = DoubleConverter.class)
    private double leeAlpha = Lee.DEFAULT_ALPHA;

    @Parameter(names = {"--crmi-beta"},
               descriptionKey = "USAGE_CRMI_BETA",
               converter = DoubleConverter.class)
    private double crmiBeta = CrMi.DEFAULT_BETA;

    @Parameter(names = {"--crmi-gamma"},
               descriptionKey = "USAGE_CRMI_GAMMA",
               converter = DoubleConverter.class)
    private double crmiGamma = CrMi.DEFAULT_GAMMA;

    @Parameter(names = {"--mink-p"},
               descriptionKey = "USAGE_MINK_P",
               converter = DoubleConverter.class)
    private double minkP = 2d;

    @Override
    protected void initialiseTask() throws Exception {
    }

    private Map<String, Class<? extends Proximity>> buildMeasureClassLookupTable() throws ClassNotFoundException {

        // Map that will store measure aliases to class
        final Map<String, Class<? extends Proximity>> classLookup =
                new HashMap<String, Class<? extends Proximity>>();

        final ResourceBundle res = ResourceBundle.getBundle(
                "uk.ac.susx.mlcl.byblo.measure.measures");
        final String[] measures = res.getString("measures").split(",");

        for (String measure : measures) {
            measure = measure.trim();
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
    @SuppressWarnings("unchecked")
    protected void runTask() throws Exception {

        LOG.info("Running All-Pairs Similarity Search Command.");
        LOG.log(Level.INFO, "Command Parameterisation {0}", this.toString());


        final Map<String, Class<? extends Proximity>> classLookup =
                buildMeasureClassLookupTable();

        Class<? extends Proximity> measureClass = null;
        if (classLookup.containsKey(measureName.toLowerCase().trim())) {
            measureClass = classLookup.get(measureName.toLowerCase().trim());
        } else {
            @SuppressWarnings("unchecked")
            Class<? extends Proximity> clazz =
                    (Class<? extends Proximity>) Class.forName(measureName);
            measureClass = clazz;
        }

        // Instantiate the denote proxmity measure
        Proximity prox = measureClass.newInstance();

        // Parameterise those measures that require them
        if (prox instanceof Lp) {
            ((Lp) prox).setP(minkP);
        } else if (prox instanceof Lee) {
            ((Lee) prox).setAlpha(leeAlpha);
        } else if (prox instanceof CrMi) {
            ((CrMi) prox).setBeta(crmiBeta);
            ((CrMi) prox).setGamma(crmiGamma);
        }

        ObjectIndex<String> strIndex = new ObjectIndex<String>();

        LOG.info("Loading head entries file " + headsFile);
        HeadSource headsDb = new HeadSource(
                headsFile, charset, strIndex);
        //TODO: Remove because it's never used?
        double[] heads = headsDb.readAllAsArray();


        // Headword index is not really required for the core algorithm
        // implementation but is used to filter headwords

        // Mutual Information based proximity measures require the frequencies
        // of each context feature, and other associate values, so load them
        // if required.
        if (prox instanceof AbstractMIProximity) {
            try {
                LOG.info("Loading context entries file " + contextsFile);

                ContextSource contexts = new ContextSource(
                        contextsFile, charset, strIndex);
                AbstractMIProximity bmip = ((AbstractMIProximity) prox);
//                contexts.position(0);
                bmip.setContextFreqs(contexts.readAllAsArray());
                bmip.setContextSum(contexts.getWeightSum());
                bmip.setContextCardinality(contexts.getCardinality());
                bmip.setUniqueContextCount(contexts.getCount());

            } catch (IOException e) {
                System.err.println("Error reading context totals file "
                        + contextsFile + " : " + e.toString());
                System.exit(1);
            }
            System.err.println("Loaded contexts for MI");
        } else if (prox instanceof KendallTau) {
            try {
                LOG.info("Loading context entries file for "
                        + "KendalTau.numFeatures: " + contextsFile);

                ContextSource contexts = new ContextSource(
                        contextsFile, charset, strIndex);
                contexts.readAll();

                ((KendallTau) prox).setNumFeatures(contexts.getCardinality());

            } catch (IOException e) {
                LOG.severe("Error reading contexts  file "
                        + contextsFile + " : " + e.toString());
                System.exit(1);
            }
        }




        // Swap the proximity measure inputs if required
        if (measureReversed) {
            prox = new ReversedProximity(prox);
        }

        // Instantiate two vector source objects than can scan and read the
        // main db. We need two because the algorithm takes all pairwise
        // combinations of vectors, so will be looking at two differnt points
        // in the file. Also this allows for the possibility of having differnt
        // files, e.g compare fruit words with cake words
        final FeatureVectorSource sourceA = new FeatureSource(
                featuresFile, charset, strIndex).getVectorSource();
        final FeatureVectorSource sourceB = new FeatureSource(
                featuresFile, charset, strIndex).getVectorSource();

        // Create a sink object that will act as a recipient for all pairs that
        // are produced by the algorithm.

        final Sink<WeightedPair> sink =
                new WeightedPairSink(outputFile, charset, strIndex, strIndex);

        // Instantiate the all-pairs algorithm as given on the command line.
        ThreadedApssTask apss = new ThreadedApssTask(sourceA, sourceB, sink);
        apss.setInnerAlgorithm(InvertedApssTask.class);

        // Parameterise the all-pairs algorithm
        apss.setNumThreads(nThreads);
        apss.setSink(sink);
        apss.setMeasure(prox);
        apss.setMaxChunkSize(chunkSize);

        List<Predicate<WeightedPair>> pairFilters =
                new ArrayList<Predicate<WeightedPair>>();

        if (minSimilarity != Double.NEGATIVE_INFINITY)
            pairFilters.add(WeightedPair.similarityGTE(minSimilarity));

        if (maxSimilarity != Double.POSITIVE_INFINITY)
            pairFilters.add(WeightedPair.similarityLTE(maxSimilarity));

        if (!outputIdentityPairs)
            pairFilters.add(Predicates.not(WeightedPair.identity()));

        if (pairFilters.size() == 1)
            apss.setProducatePair(pairFilters.get(0));
        else if (pairFilters.size() > 1)
            apss.setProducatePair(Predicates.and(pairFilters));

        LOG.info("Running APSS algorithm.");

        apss.run();
        LOG.info("Completed All-Pairs Similarity Search.");
    }

    @Override
    protected void finaliseTask() throws Exception {
    }

    public static class InputFileValidator implements IParameterValidator {

        public InputFileValidator() {
        }

        public void validate(String name, String value) throws ParameterException {
            File file = new File(value);
            if (!file.exists())
                throw new ParameterException(
                        "Input file \"" + value + "\" does not exist.");
            if (file.isDirectory())
                throw new ParameterException(
                        "Input file \"" + value + "\" exists but is a directory.");
            if (!file.isFile())
                throw new ParameterException(
                        "Input file \"" + value + "\" is not an ordinary file.");
            if (!file.canRead())
                throw new ParameterException(
                        "Input file \"" + value + "\" is not readble.");
        }
    }

    public static class OutputFileValidator implements IParameterValidator {

        public OutputFileValidator() {
        }

        public void validate(String name, String value) throws ParameterException {
            File file;
            try {
                file = new File(value).getCanonicalFile();
            } catch (IOException ex) {
                throw new ParameterException(ex);
            }
            if (file.exists()) {
                if (file.isDirectory())
                    throw new ParameterException(
                            "Output file \"" + value + "\" exists but is a directory.");
                if (!file.isFile())
                    throw new ParameterException(
                            "Output file \"" + value + "\" exists but is not an ordinary file.");
                if (!file.canWrite())
                    throw new ParameterException(
                            "Input file \"" + value + "\" is not writeable.");
            } else {

                if (file.getParentFile() == null || !file.getParentFile().
                        canWrite())
                    throw new ParameterException(
                            "Output file \"" + value + "\" does not exist and the parent directory is not writable.");
            }
        }
    }

    public static class DoubleConverter implements com.beust.jcommander.IStringConverter<Double> {

        @Override
        public Double convert(String value) {
            return Double.valueOf(value);
        }
    }

    @Override
    public String toString() {
        return "AllPairsCommand{"
                + "usageRequested=" + isUsageRequested()
                + ", exceptionThrown=" + isExceptionThrown()
                + ", featuresFile=" + featuresFile
                + ", contextsFile=" + contextsFile
                + ", headsFile=" + headsFile
                + ", outputFile=" + outputFile
                + ", charset=" + charset
                + ", chunkSize=" + chunkSize
                + ", nThreads=" + nThreads
                + ", minSimilarity=" + minSimilarity
                + ", maxSimilarity=" + maxSimilarity
                + ", outputIdentityPairs=" + outputIdentityPairs
                + ", measureName=" + measureName
                + ", measureReversed=" + measureReversed
                + ", leeAlpha=" + leeAlpha
                + ", crmiBeta=" + crmiBeta
                + ", crmiGamma=" + crmiGamma
                + ", minkP=" + minkP
                + '}';
    }
}

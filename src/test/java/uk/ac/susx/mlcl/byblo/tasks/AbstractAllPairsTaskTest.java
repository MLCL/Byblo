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

package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.ints.IntIterator;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.lib.PoissonDistribution;
import uk.ac.susx.mlcl.lib.events.ReportingProgressListener;
import uk.ac.susx.mlcl.testing.AbstractObjectTest;
import uk.ac.susx.mlcl.testing.SlowTestCategory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static uk.ac.susx.mlcl.TestConstants.DEFAULT_CHARSET;
import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_EVENTS;

/**
 * Abstract super-class to various algorithms that extends the {@link NaiveApssTask} implementation.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractAllPairsTaskTest<T extends NaiveApssTask> extends AbstractObjectTest<T> {


    private static final Measure MEASURE = new uk.ac.susx.mlcl.byblo.measures.impl.Jaccard();

    private static final Predicate<Weighted<TokenPair>> PAIR_FILTER =
            Weighted.greaterThanOrEqualTo(1E-10);


    @Test
    public void testRun() throws Exception {

        T instance = newInstance();

        DoubleEnumeratingDelegate del = new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairSource tokenSourceA = WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false);
        WeightedTokenPairVectorSource vectorSourceA = new WeightedTokenPairVectorSource(tokenSourceA);

        WeightedTokenPairSource tokenSourceB = WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false);

        WeightedTokenPairVectorSource vectorSourceB = new WeightedTokenPairVectorSource(tokenSourceB);


        final String outFileName = String.format("%s#%s-%s-sims",
                getClass().getName(), testName.getMethodName(), TestConstants.FRUIT_NAME);


        final File outSims = new File(TestConstants.TEST_OUTPUT_DIR, outFileName);

        WeightedTokenPairSink result = BybloIO.openSimsSink(outSims, DEFAULT_CHARSET, del.getEntriesEnumeratorCarrier());

        instance.setSourceA(vectorSourceA);
        instance.setSourceB(vectorSourceB);
        instance.setSink(result);
        instance.setMeasure(MEASURE);
        instance.setProducePair(PAIR_FILTER);

        instance.run();


        if (vectorSourceA instanceof Closeable)
            ((Closeable) vectorSourceA).close();
        if (vectorSourceB instanceof Closeable)
            ((Closeable) vectorSourceB).close();

        result.flush();
        result.close();

        if (instance.isExceptionTrapped())
            instance.throwTrappedException();

        Assert.assertTrue("Output sims file does not exist: " + outSims, outSims.exists());
        Assert.assertTrue("Output sims file is empty: " + outSims, outSims.length() > 0);

    }

    @Test
    @Category(SlowTestCategory.class)
    @Ignore
    public void testWorstCaseFeatures() throws Exception {

        final int nEntries = 2;
        final int nFeatures = 1000000;

        runOnGeneratedData(nEntries, nFeatures);
    }

    @Test
    @Category(SlowTestCategory.class)
    @Ignore
    public void testWorstCaseEntries() throws Exception {

        final int nEntries = 2000;
        final int nFeatures = 10;

        runOnGeneratedData(nEntries, nFeatures);
    }

    @Test
    @Category(SlowTestCategory.class)
    @Ignore
    public void testWorstCase() throws Exception {

        final int nEntries = 200;
        final int nFeatures = 20000;

        runOnGeneratedData(nEntries, nFeatures);
    }

    void runOnGeneratedData(int nEntries, int nFeatures) throws Exception {

        final String inFileName = String.format("%s-%dx%d-events",
                AbstractAllPairsTaskTest.class.getName(), nEntries, nFeatures);
        final String outFileName = String.format("%s#%s-%dx%d-sims",
                getClass().getName(), testName.getMethodName(), nEntries, nFeatures);

        final File inEvents = new File(TestConstants.TEST_OUTPUT_DIR, inFileName);

        final File outSims = new File(TestConstants.TEST_OUTPUT_DIR, outFileName);

        // Create the test data if necessary
        if (!inEvents.exists()) {
            generateEventsData(inEvents, nEntries, nFeatures);
        }

        if (outSims.exists() && !outSims.delete())
            throw new AssertionError("Failed to delete outSims file: " + outSims);


        T instance = newInstance();

        DoubleEnumeratingDelegate del = new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, true, true, null, null);

        FastWeightedTokenPairVectorSource vsa = BybloIO.openEventsVectorSource(inEvents, DEFAULT_CHARSET, del);
        FastWeightedTokenPairVectorSource vsb = BybloIO.openEventsVectorSource(inEvents, DEFAULT_CHARSET, del);
        WeightedTokenPairSink result = BybloIO.openSimsSink(outSims, DEFAULT_CHARSET, del.getEntriesEnumeratorCarrier());

        instance.setSourceA(vsa);
        instance.setSourceB(vsb);
        instance.setSink(result);
        instance.setMeasure(MEASURE);
        instance.setProducePair(PAIR_FILTER);

        instance.addProgressListener(new ReportingProgressListener());
        instance.addProgressListener(new TestConstants.InfoProgressListener());


        instance.run();

        vsa.close();
        vsb.close();

        result.flush();
        result.close();

        if (instance.isExceptionTrapped())
            instance.throwTrappedException();

        Assert.assertTrue("Output sims file does not exist: " + outSims, outSims.exists());
        Assert.assertTrue("Output sims file is empty: " + outSims, outSims.length() > 0);
    }


    /**
     * Routine that creates a large amount of data, that should be the absolute
     * worst case for all-pairs stage of the pipeline.
     *
     * @throws java.io.IOException
     */
    private static void generateEventsData(final File eventsFile,
                                           final int nEntries,
                                           final int nFeatures)
            throws IOException {
        final int nEvents = nEntries * nFeatures;

        System.out.printf("Generating worst-case data for AllPairs tasks "
                + "(nEntries=%d, nFeatures=%d, nEvents=%d)...%n",
                nEntries, nFeatures, nEvents);

        PoissonDistribution dist = new PoissonDistribution(1);
        dist.setRandom(newRandom());
        IntIterator freqGenerator = dist.generator();

        WeightedTokenPairSink eventsSink = null;
        try {
            final DoubleEnumeratingDelegate ded = new DoubleEnumeratingDelegate(
                    Enumerating.DEFAULT_TYPE, true, true, null, null);

            eventsSink = BybloIO.openEventsSink(eventsFile, DEFAULT_CHARSET, ded);

            for (int i = 0; i < nEntries; i++) {

                for (int j = 0; j < nFeatures; j++) {
                    int frequency = freqGenerator.nextInt() - 1;
                    if (frequency <= 0)
                        continue;

                    eventsSink.write(new Weighted<TokenPair>(new TokenPair(i, j), frequency));

                    if (((i + 1) * nFeatures + (j + 1)) % 5000000 == 0 || (i == nEntries - 1 && j == nFeatures - 1)) {
                        System.out.printf("> generated %d of %d events (%.2f%% complete)%n",
                                (i * nFeatures + j), nEvents, (100.0d * (i * nFeatures + j)) / nEvents);
                    }
                }
            }
        } finally {
            if (eventsSink != null) {
                eventsSink.flush();
                eventsSink.close();
            }
        }

        System.out.println("Generation completed.");
    }
}

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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.measures.Jaccard;
import uk.ac.susx.mlcl.byblo.measures.Proximity;
import uk.ac.susx.mlcl.lib.io.ObjectIO;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.Tell;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.disableExitTrapping;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.enableExistTrapping;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ThreadedApssTaskTest {

    private static final String subject = ThreadedApssTask.class.getName();

    private static final Proximity MEASURE = new Jaccard();

    private static final Predicate<Weighted<TokenPair>> PAIR_FILTER =
            Weighted.greaterThanOrEqualTo(0.1);

    @Test
    public void testCLI() throws Exception {
        String output = new File(TEST_OUTPUT_DIR,
                                 TEST_FRUIT_INPUT.getName() + ".sims").toString();
        String[] args = {
            "allpairs",
            "--input", TEST_FRUIT_EVENTS.toString(),
            "--input-entries", TEST_FRUIT_ENTRIES.toString(),
            "--input-features", TEST_FRUIT_FEATURES.toString(),
            "--output", output,
            "--measure", "Jaccard",};

        try {
            enableExistTrapping();
            Tools.main(args);
        } finally {
            disableExitTrapping();
        }



    }

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test
    public void testNaive() throws Exception {
        System.out.println("Testing " + subject + " Naive");

        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);


        WeightedTokenPairVectorSource vsa =
                new WeightedTokenPairVectorSource(
                WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

        WeightedTokenPairVectorSource vsb =
                new WeightedTokenPairVectorSource(
                WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

        List<Weighted<TokenPair>> result = new ArrayList<Weighted<TokenPair>>();
        ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(result);
        ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                vsa, vsb, sink);

        instance.setInnerAlgorithm(NaiveApssTask.class);
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();

        while (instance.isExceptionTrapped()) {
            instance.throwTrappedException();
        }

        assertTrue(!result.isEmpty());
    }

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test
    public void testInverted() throws Exception {
        System.out.println("Testing " + subject + " Inverted");
        DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);


        WeightedTokenPairVectorSource vsa =
                new WeightedTokenPairVectorSource(
                WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

        WeightedTokenPairVectorSource vsb =
                new WeightedTokenPairVectorSource(
                WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));
        List<Weighted<TokenPair>> result = new ArrayList<Weighted<TokenPair>>();
        ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(result);
        ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                vsa, vsb, sink);

        instance.setInnerAlgorithm(InvertedApssTask.class);
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();
        while (instance.isExceptionTrapped()) {
            instance.throwTrappedException();
        }

        assertTrue(!result.isEmpty());
    }

    @Test
    public void compareNaiveInverted() throws Exception {
        System.out.println(
                "Testing " + subject + " comparing Naive and Inverted");



        List<Weighted<TokenPair>> naiveResults = new ArrayList<Weighted<TokenPair>>();
        List<Weighted<TokenPair>> invertedResults = new ArrayList<Weighted<TokenPair>>();

        {
            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);


            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));


            List<Weighted<TokenPair>> result = new ArrayList<Weighted<TokenPair>>();
            ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(result);
            ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(NaiveApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        {
            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);

            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));


            List<Weighted<TokenPair>> result = new ArrayList<Weighted<TokenPair>>();
            ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(result);
            ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(InvertedApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        Collections.sort(naiveResults);
        Collections.sort(invertedResults);

        assertEquals(naiveResults, invertedResults);
    }

    @Test
    public void compareNaive_Threaded_vs_NonThreaded() throws Exception {
        System.out.println(
                "Testing " + subject + " compare Naive Threaded vs Non-Threaded");


        List<Weighted<TokenPair>> threadedResults = new ArrayList<Weighted<TokenPair>>();
        List<Weighted<TokenPair>> nonThreadedResults = new ArrayList<Weighted<TokenPair>>();

        {
            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);


            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));


            ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(threadedResults);
            ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(NaiveApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        {
            NaiveApssTask<Tell> instance = new NaiveApssTask<Tell>();

            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);

            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            instance.setSourceA(vsa);
            instance.setSourceB(vsb);
            instance.setSink(ObjectIO.asSink(nonThreadedResults));
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        Collections.sort(threadedResults);
        Collections.sort(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }

    @Test
    public void compareInverted_Threaded_vs_NonThreaded() throws Exception {
        System.out.println(
                "Testing " + subject + " compare Naive Threaded vs Non-Threaded");

        List<Weighted<TokenPair>> threadedResults = new ArrayList<Weighted<TokenPair>>();
        List<Weighted<TokenPair>> nonThreadedResults = new ArrayList<Weighted<TokenPair>>();

        {
            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);

            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            ObjectSink<Weighted<TokenPair>> sink = ObjectIO.asSink(threadedResults);
            ThreadedApssTask<Tell> instance = new ThreadedApssTask<Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(InvertedApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        {
            InvertedApssTask<Tell> instance = new InvertedApssTask<Tell>();

            DoubleEnumeratingDeligate del = new DoubleEnumeratingDeligate(
                    Enumerating.DEFAULT_TYPE, false, false, null, null);

            WeightedTokenPairVectorSource vsa =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            WeightedTokenPairVectorSource vsb =
                    new WeightedTokenPairVectorSource(
                    WeightedTokenPairSource.open(
                    TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false));

            instance.setSourceA(vsa);
            instance.setSourceB(vsb);
            instance.setSink(ObjectIO.asSink(nonThreadedResults));
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionTrapped()) {
                instance.throwTrappedException();
            }
        }

        Collections.sort(threadedResults);
        Collections.sort(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }

}

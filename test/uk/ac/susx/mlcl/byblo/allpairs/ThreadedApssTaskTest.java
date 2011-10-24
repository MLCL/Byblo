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
package uk.ac.susx.mlcl.byblo.allpairs;

import java.io.File;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureSource;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureVectorSource;
import com.google.common.base.Predicate;
import uk.ac.susx.mlcl.byblo.measure.Proximity;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import java.util.Collections;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.byblo.measure.Jaccard;
import uk.ac.susx.mlcl.byblo.io.EntryPair;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.Byblo;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.io.Lexer;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.ExitTrapper.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ThreadedApssTaskTest {

    private static final String subject = ThreadedApssTask.class.getName();

    private static final Proximity MEASURE = new Jaccard();

    private static final Predicate<Weighted<EntryPair>> PAIR_FILTER =
            Weighted.greaterThanOrEqualTo(0.1);

    @Test(timeout = 1000)
    public void testCLI() throws Exception {
        String output = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_INPUT.getName() + ".sims").toString();
        String[] args = {
            "allpairs",
            "--input", TEST_FRUIT_ENTRY_FEATURES.toString(),
            "--input-entries", TEST_FRUIT_ENTRIES.toString(),
            "--input-features", TEST_FRUIT_FEATURES.toString(),
            "--output", output,
            "--measure", "Jaccard",};

        enableExistTrapping();
        Byblo.main(args);
        disableExitTrapping();


    }

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test(timeout = 1000)
    public void testNaive() throws Exception {
        System.out.println("Testing " + subject + " Naive");

        ObjectIndex<String> stringIndex = new ObjectIndex<String>();

        WeightedEntryFeatureVectorSource vsa =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

        WeightedEntryFeatureVectorSource vsb =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

        List<Weighted<EntryPair>> result = new ArrayList<Weighted<EntryPair>>();
        Sink<Weighted<EntryPair>> sink = IOUtil.asSink(result);
        ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                vsa, vsb, sink);

        instance.setInnerAlgorithm(NaiveApssTask.class);
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();

        while (instance.isExceptionThrown()) {
            instance.throwException();
        }

        assertTrue(!result.isEmpty());
    }

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test(timeout = 1000)
    public void testInverted() throws Exception {
        System.out.println("Testing " + subject + " Inverted");
        ObjectIndex<String> stringIndex = new ObjectIndex<String>();

        WeightedEntryFeatureVectorSource vsa =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

        WeightedEntryFeatureVectorSource vsb =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

        List<Weighted<EntryPair>> result = new ArrayList<Weighted<EntryPair>>();
        Sink<Weighted<EntryPair>> sink = IOUtil.asSink(result);
        ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                vsa, vsb, sink);

        instance.setInnerAlgorithm(InvertedApssTask.class);
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();
        while (instance.isExceptionThrown()) {
            instance.throwException();
        }

        assertTrue(!result.isEmpty());
    }

    @Test(timeout = 1000)
    public void compareNaiveInverted() throws Exception {
        System.out.println(
                "Testing " + subject + " comparing Naive and Inverted");



        List<Weighted<EntryPair>> naiveResults = new ArrayList<Weighted<EntryPair>>();
        List<Weighted<EntryPair>> invertedResults = new ArrayList<Weighted<EntryPair>>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));


            List<Weighted<EntryPair>> result = new ArrayList<Weighted<EntryPair>>();
            Sink<Weighted<EntryPair>> sink = IOUtil.asSink(result);
            ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(NaiveApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();
            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));


            List<Weighted<EntryPair>> result = new ArrayList<Weighted<EntryPair>>();
            Sink<Weighted<EntryPair>> sink = IOUtil.asSink(result);
            ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(InvertedApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        Collections.sort(naiveResults);
        Collections.sort(invertedResults);

        assertEquals(naiveResults, invertedResults);
    }

    @Test(timeout = 1000)
    public void compareNaive_Threaded_vs_NonThreaded() throws Exception {
        System.out.println(
                "Testing " + subject + " compare Naive Threaded vs Non-Threaded");


        List<Weighted<EntryPair>> threadedResults = new ArrayList<Weighted<EntryPair>>();
        List<Weighted<EntryPair>> nonThreadedResults = new ArrayList<Weighted<EntryPair>>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));


            Sink<Weighted<EntryPair>> sink = IOUtil.asSink(threadedResults);
            ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(NaiveApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        {
            NaiveApssTask<Lexer.Tell> instance = new NaiveApssTask<Lexer.Tell>();

            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            instance.setSourceA(vsa);
            instance.setSourceB(vsb);
            instance.setSink(IOUtil.asSink(nonThreadedResults));
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        Collections.sort(threadedResults);
        Collections.sort(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }

    @Test(timeout = 1000)
    public void compareInverted_Threaded_vs_NonThreaded() throws Exception {
        System.out.println(
                "Testing " + subject + " compare Naive Threaded vs Non-Threaded");

        List<Weighted<EntryPair>> threadedResults = new ArrayList<Weighted<EntryPair>>();
        List<Weighted<EntryPair>> nonThreadedResults = new ArrayList<Weighted<EntryPair>>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            Sink<Weighted<EntryPair>> sink = IOUtil.asSink(threadedResults);
            ThreadedApssTask<Lexer.Tell> instance = new ThreadedApssTask<Lexer.Tell>(
                    vsa, vsb, sink);

            instance.setInnerAlgorithm(InvertedApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        {
            InvertedApssTask<Lexer.Tell> instance = new InvertedApssTask<Lexer.Tell>();

            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    TEST_FRUIT_ENTRY_FEATURES, DEFAULT_CHARSET, stringIndex));

            instance.setSourceA(vsa);
            instance.setSourceB(vsb);
            instance.setSink(IOUtil.asSink(nonThreadedResults));
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();
            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        Collections.sort(threadedResults);
        Collections.sort(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }
}

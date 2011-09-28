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

import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureSource;
import uk.ac.susx.mlcl.byblo.io.WeightedEntryFeatureVectorSource;
import com.google.common.base.Predicate;
import uk.ac.susx.mlcl.byblo.measure.Proximity;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.ObjectIndex;
import java.util.Collections;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import java.io.File;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.measure.Jaccard;
import uk.ac.susx.mlcl.lib.collect.Pair;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 */
public class ThreadedApssTaskTest {

    private static final File FEATURES_FILE =
            new File("sampledata", "bnc-gramrels-fruit.features");

    private static final Charset CHARSET = IOUtil.DEFAULT_CHARSET;

    private static final Proximity MEASURE = new Jaccard();

    private static final Predicate<Pair> PAIR_FILTER =
            Pair.similarityGTE(0.1);

    public ThreadedApssTaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of runTask method, of class AbstractAPSS2.
     */
    @Test
    public void testRunTask_Naive() throws Exception {
        System.out.println("runTask Naive");

        ObjectIndex<String> stringIndex = new ObjectIndex<String>();

        WeightedEntryFeatureVectorSource vsa =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                FEATURES_FILE, CHARSET, stringIndex));

        WeightedEntryFeatureVectorSource vsb =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                FEATURES_FILE, CHARSET, stringIndex));

        List<Pair> result = new ArrayList<Pair>();
        Sink<Pair> sink = IOUtil.asSink(result);
        ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);


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
    @Test
    public void testRunTask_Inverted() throws Exception {
        System.out.println("runTask Inverted");
        ObjectIndex<String> stringIndex = new ObjectIndex<String>();

        WeightedEntryFeatureVectorSource vsa =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                FEATURES_FILE, CHARSET, stringIndex));

        WeightedEntryFeatureVectorSource vsb =
                new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                FEATURES_FILE, CHARSET, stringIndex));

        List<Pair> result = new ArrayList<Pair>();
        Sink<Pair> sink = IOUtil.asSink(result);
        ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);

        instance.setInnerAlgorithm(InvertedApssTask.class);
        instance.setMeasure(MEASURE);
        instance.setProducatePair(PAIR_FILTER);

        instance.run();
        while (instance.isExceptionThrown()) {
            instance.throwException();
        }

        assertTrue(!result.isEmpty());
    }

    @Test
    public void compareNaiveInverted() throws Exception {
        System.out.println("compare Naive and Inverted");



        List<Pair> naiveResults = new ArrayList<Pair>();
        List<Pair> invertedResults = new ArrayList<Pair>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));


            List<Pair> result = new ArrayList<Pair>();
            Sink<Pair> sink = IOUtil.asSink(result);
            ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);

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
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));


            List<Pair> result = new ArrayList<Pair>();
            Sink<Pair> sink = IOUtil.asSink(result);
            ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);

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

//        System.out.println(naiveResults);
//        System.out.println(invertedResults);

        assertEquals(naiveResults, invertedResults);
    }

    @Test
    public void compareNaive_Threaded_vs_NonThreaded() throws Exception {
        System.out.println("compareNaive_Threaded_vs_NonThreaded");


        List<Pair> threadedResults = new ArrayList<Pair>();
        List<Pair> nonThreadedResults = new ArrayList<Pair>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));


            Sink<Pair> sink = IOUtil.asSink(threadedResults);
            ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);

            instance.setInnerAlgorithm(NaiveApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        {
            NaiveApssTask instance = new NaiveApssTask();

            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));


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

//        System.out.println(threadedResults);
//        System.out.println(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }

    @Test
    public void compareInverted_Threaded_vs_NonThreaded() throws Exception {
        System.out.println("compareNaive_Threaded_vs_NonThreaded");

        List<Pair> threadedResults = new ArrayList<Pair>();
        List<Pair> nonThreadedResults = new ArrayList<Pair>();

        {
            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));


            Sink<Pair> sink = IOUtil.asSink(threadedResults);
            ThreadedApssTask instance = new ThreadedApssTask(vsa, vsb, sink);

            instance.setInnerAlgorithm(InvertedApssTask.class);
            instance.setMeasure(MEASURE);
            instance.setProducatePair(PAIR_FILTER);

            instance.run();

            while (instance.isExceptionThrown()) {
                instance.throwException();
            }
        }

        {
            InvertedApssTask instance = new InvertedApssTask();

            ObjectIndex<String> stringIndex = new ObjectIndex<String>();

            WeightedEntryFeatureVectorSource vsa =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));

            WeightedEntryFeatureVectorSource vsb =
                    new WeightedEntryFeatureVectorSource(new WeightedEntryFeatureSource(
                    FEATURES_FILE, CHARSET, stringIndex));



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

//        System.out.println(threadedResults);
//        System.out.println(nonThreadedResults);

        assertEquals(threadedResults, nonThreadedResults);
    }
}

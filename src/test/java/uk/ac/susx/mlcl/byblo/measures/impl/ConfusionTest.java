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
package uk.ac.susx.mlcl.byblo.measures.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.disableExitTrapping;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.enableExistTrapping;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ConfusionTest {

    static Confusion INSTANCE;

    static final double EPSILON = 0;

    static Random RANDOM;

    static List<Indexed<SparseDoubleVector>> FRUIT_EVENTS;

    @BeforeClass
    public static void setUpClass() throws Exception {
        INSTANCE = new Confusion();
        RANDOM = new Random(1234);

        final DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();

        // Load events
        final FastWeightedTokenPairVectorSource eventSrc =
                BybloIO.openEventsVectorSource(
                        TEST_FRUIT_EVENTS, DEFAULT_CHARSET, indexDelegate);
        FRUIT_EVENTS = new ArrayList<Indexed<SparseDoubleVector>>();
        int card = 0;
        while (eventSrc.hasNext()) {
            Indexed<SparseDoubleVector> v = eventSrc.read();
            FRUIT_EVENTS.add(v);
            card = Math.max(card, v.value().cardinality);
        }

        // Add a completely empty feature vector to test that works
        FRUIT_EVENTS.add(new Indexed<SparseDoubleVector>(
                Integer.MAX_VALUE, new SparseDoubleVector(card, 0)));

        if (eventSrc instanceof Closeable) {
            ((Closeable) eventSrc).close();
        }

        for (Indexed<SparseDoubleVector> v : FRUIT_EVENTS) {
            v.value().cardinality = card;
        }


        WeightedTokenSource featsSrc = BybloIO.openFeaturesSource(
                TEST_FRUIT_FEATURES, DEFAULT_CHARSET,
                EnumeratingDelegates.toSingleFeatures(indexDelegate));

        WeightedTokenSource.WTStatsSource featsStatSrc =
                new WeightedTokenSource.WTStatsSource(featsSrc);


        final double[] feats = AllPairsCommand.readAllAsArray(featsStatSrc);
        final double featsSum = featsStatSrc.getWeightSum();
        final int featusCard = featsStatSrc.getMaxId() + 1;

        INSTANCE.setFeatureCardinality(featusCard);
        INSTANCE.setFeatureMarginals(feats);
        INSTANCE.setGrandTotal(featsSum);


    }

    @Test
    public void testCLI() throws Exception {
        System.out.println("testCLI");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Confusion");

        deleteIfExist(output);

        try {
            enableExistTrapping();
            Tools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "confusion",
                    "--input", TEST_FRUIT_EVENTS.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output.toString()
            });
        } finally {
            disableExitTrapping();
        }


        assertTrue("Output file " + output + " does not exist.", output.exists());
        assertTrue("Output file " + output + " is empty.", output.length() > 0);
    }

    @Test
    public void testBothEmptyVectors() throws Exception {
        System.out.println("testBothEmptyVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 0);
        SparseDoubleVector B = new SparseDoubleVector(size, 0);
        double expect = 0;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testOneEmptyVector() throws Exception {
        System.out.println("testOneEmptyVector");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 0);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size; i++)
            B.set(i, RANDOM.nextDouble());

        double expect = 0;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }
//
//    @Test
//    public void testSizeOneVectors() throws Exception {
//        System.out.println("testSizeOneVectors");
//        int size = 100;
//        SparseDoubleVector A = new SparseDoubleVector(size, 1);
//        SparseDoubleVector B = new SparseDoubleVector(size, 1);
//        A.set(0, 1);
//        B.set(0, 1);
//        double expect = INSTANCE.getHomogeneityBound();
//        double actual = test(A, B);
//
//        assertEquals(expect, actual, EPSILON);
//    }

    //    @Test
//    public void testCardinalityOneVectors() throws Exception {
//        System.out.println("testCardinalityOneVectors");
//        SparseDoubleVector A = new SparseDoubleVector(1, 1);
//        SparseDoubleVector B = new SparseDoubleVector(1, 1);
//        A.set(0, 1);
//        B.set(0, 1);
//        double expect = 1;
//        double actual = test(A, B);
//
//        assertEquals(expect, actual, EPSILON);
//    }
//    @Test
//    public void testSizeTwoVectors() throws Exception {
//        System.out.println("testSizeTwoVectors");
//        int size = 100;
//        SparseDoubleVector A = new SparseDoubleVector(size, 2);
//        SparseDoubleVector B = new SparseDoubleVector(size, 2);
//        A.set(0, 1);
//        A.set(1, 1);
//        B.set(0, 2);
//        B.set(1, 2);
//        A.compact();
//        B.compact();
//        double expect = INSTANCE.getHomogeneityBound();
//        double actual = test(A, B);
//
//        assertEquals(expect, actual, EPSILON);
//    }
//    @Test
//    public void testCommutative() throws Exception {
//        System.out.println("testCommutative");
//        int size = 100;
//        SparseDoubleVector A = new SparseDoubleVector(size, size);
//        SparseDoubleVector B = new SparseDoubleVector(size, size);
//        for (int i = 0; i < size; i++) {
//            A.set(i, RANDOM.nextDouble());
//            B.set(i, RANDOM.nextDouble());
//        }
//
//        double expect = test(A, B);
//        double actual = test(B, A);
//        assertEquals(expect, actual, EPSILON);
//    }
//    @Test
//    public void testHomoginiety() throws Exception {
//        System.out.println("testHomoginiety");
//        int size = 100;
//        SparseDoubleVector A = new SparseDoubleVector(size, size);
//        SparseDoubleVector B = new SparseDoubleVector(size, size);
//        for (int i = 0; i < size; i++) {
//            double value = RANDOM.nextDouble();
//            A.set(i, value);
//            B.set(i, value);
//        }
//
//        double expect = INSTANCE.getHomogeneityBound();
//        double actual = test(A, B);
//
//        assertEquals(expect, actual, EPSILON);
//    }
    @Test
    public void testHeteroginiety() throws Exception {
        System.out.println("testHeteroginiety");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, size);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size / 2; i++) {
            A.set(i * 2, i);
            B.set(i * 2 + 1, i);
        }

        double expect = INSTANCE.getHeterogeneityBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testFruitData() throws Exception {
        System.out.println("testFruitData");
        int limit = 5;


        limit = Math.min(limit, FRUIT_EVENTS.size());

        final double[][] results = new double[limit][limit];
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                SparseDoubleVector A = FRUIT_EVENTS.get(i).value();
                SparseDoubleVector B = FRUIT_EVENTS.get(j).value();
                results[i][j] = test(A, B);
            }
        }
    }

    @Test
    public void testLargeCardinality() throws Exception {
        System.out.println("testLargeCardinality");
        final int size = 100;
        final SparseDoubleVector A = new SparseDoubleVector(
                Integer.MAX_VALUE, size);
        final SparseDoubleVector B = new SparseDoubleVector(
                Integer.MAX_VALUE, size);
        for (int i = 0; i < size; i++) {
            A.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
            B.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
        }

        test(A, B);
    }

    public static double test(SparseDoubleVector A, SparseDoubleVector B) {
        final double val = INSTANCE.similarity(A, B);
        assertFalse("Similarity is NaN" + " with measure " + INSTANCE,
                Double.isNaN(val));
        assertFalse("Similarity is " + val + " with measure " + INSTANCE,
                Double.isInfinite(val));

        final double min, max;
        if (INSTANCE.getHeterogeneityBound() < INSTANCE.getHomogeneityBound()) {
            min = INSTANCE.getHeterogeneityBound();
            max = INSTANCE.getHomogeneityBound();
        } else {
            min = INSTANCE.getHomogeneityBound();
            max = INSTANCE.getHeterogeneityBound();
        }
        assertTrue("expected similarity >= " + min + " but found " + val,
                val >= min);
        assertTrue("expected similarity <= " + max + " but found " + val,
                val <= max);


        if (INSTANCE.isCommutative()) {
            final double rev = INSTANCE.similarity(B, A);
            assertEquals("Measure is declared computative, but reversing "
                    + "operands results in a different score.", rev, val,
                    EPSILON);
        }

        return val;
    }
}

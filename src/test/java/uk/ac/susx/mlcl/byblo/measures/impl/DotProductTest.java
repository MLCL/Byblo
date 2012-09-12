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
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
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
public class DotProductTest {

    static DotProduct INSTANCE;

    static final double EPSILON = 0;

    static Random RANDOM;

    @BeforeClass
    public static void setUpClass() throws Exception {
        INSTANCE = new DotProduct();
        RANDOM = new Random(1234);
    }

    @Test
    public void testCLI() throws Exception {
        System.out.println("testCLI");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".DotProduct");
        deleteIfExist(output);

        try {
            enableExistTrapping();
            Tools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "dp",
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

    @Test
    public void testSizeOneVectors() throws Exception {
        System.out.println("testSizeOneVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 1);
        SparseDoubleVector B = new SparseDoubleVector(size, 1);
        A.set(0, 1);
        B.set(0, 1);
        double expect = 1;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testCardinalityOneVectors() throws Exception {
        System.out.println("testCardinalityOneVectors");
        SparseDoubleVector A = new SparseDoubleVector(1, 1);
        SparseDoubleVector B = new SparseDoubleVector(1, 1);
        A.set(0, 1);
        B.set(0, 1);
        double expect = 1;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testSizeTwoVectors() throws Exception {
        System.out.println("testSizeTwoVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 2);
        SparseDoubleVector B = new SparseDoubleVector(size, 2);
        A.set(0, 1);
        A.set(1, 1);
        B.set(0, 1);
        B.set(1, 1);
        double expect = 2;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testCommutative() throws Exception {
        System.out.println("testCommutative");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, size);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size; i++) {
            A.set(i, RANDOM.nextDouble());
            B.set(i, RANDOM.nextDouble());
        }

        double expect = test(A, B);
        double actual = test(B, A);
        assertEquals(expect, actual, EPSILON);
    }

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

        double expect = 0;
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testFruitData() throws Exception {
        System.out.println("testFruitData");
        int limit = 5;

        List<Indexed<SparseDoubleVector>> vecs = TestConstants.loadFruitVectors();

        limit = Math.min(limit, vecs.size());

        final double[][] results = new double[limit][limit];
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                SparseDoubleVector A = vecs.get(i).value();
                SparseDoubleVector B = vecs.get(j).value();
                results[i][j] = test(A, B);
            }
        }

        // triangular mirrors should be equal
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                assertEquals(results[i][j], results[j][i], EPSILON);
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

    public double test(SparseDoubleVector A, SparseDoubleVector B) {
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

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
package uk.ac.susx.mlcl.byblo.measures.v2.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.*;
import static uk.ac.susx.mlcl.TestConstants.DEFAULT_CHARSET;
import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_EVENTS;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Unit tests for {@link KendallsTau } proximity measure.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class KendallsTauTest {

    public KendallsTauTest() {
    }

    static KendallsTau INSTANCE;

    static final double EPSILON = 0;

    static Random RANDOM;

    @BeforeClass
    public static void setUpClass() throws Exception {
        INSTANCE = new KendallsTau();
        RANDOM = new Random(1234);
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

    @Test
    public void testBothEmptyVectors() throws Exception {
        System.out.println("testBothEmptyVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 0);
        SparseDoubleVector B = new SparseDoubleVector(size, 0);
        double expect = INSTANCE.getIndependenceBound();
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

        double expect = INSTANCE.getIndependenceBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testSizeOneVectors() throws Exception {
        System.out.println("testSizeOneVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 1);
        SparseDoubleVector B = new SparseDoubleVector(size, 1);
        A.set(0, RANDOM.nextDouble());
        B.set(0, RANDOM.nextDouble());
        double expect = INSTANCE.getHomogeneityBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testCardinalityOneVectors() throws Exception {
        System.out.println("testCardinalityOneVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(1, 1);
        SparseDoubleVector B = new SparseDoubleVector(1, 1);
        A.set(0, RANDOM.nextDouble());
        B.set(0, RANDOM.nextDouble());
        double expect = INSTANCE.getIndependenceBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testSizeTwoVectors() throws Exception {
        System.out.println("testSizeTwoVectors");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, 2);
        SparseDoubleVector B = new SparseDoubleVector(size, 2);
        A.set(0, RANDOM.nextDouble());
        A.set(1, 1 + RANDOM.nextDouble());
        B.set(0, RANDOM.nextDouble());
        B.set(1, 1 + RANDOM.nextDouble());
        double expect = INSTANCE.getHomogeneityBound();
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
    public void testHomoginiety() throws Exception {
        System.out.println("testHomoginiety");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, size);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size; i++) {
            double value = RANDOM.nextDouble();
            A.set(i, value);
            B.set(i, value);
        }

        double expect = INSTANCE.getHomogeneityBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testHeteroginiety() throws Exception {
        System.out.println("testHeteroginiety");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, size);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size; i++) {
            A.set(i, i);
            B.set(i, size - i);
        }

        double expect = INSTANCE.getHeterogeneityBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testIdependence() throws Exception {
        System.out.println("testIdependence");
        int size = 100;
        SparseDoubleVector A = new SparseDoubleVector(size, size);
        SparseDoubleVector B = new SparseDoubleVector(size, size);
        for (int i = 0; i < size / 2; i++) {
            A.set(i, i);
            B.set(i, size - i);
        }
        for (int i = size / 2 + 1; i < size; i++) {
            A.set(i, i);
            B.set(i, i);
        }
        double expect = INSTANCE.getIndependenceBound();
        double actual = test(A, B);

        assertEquals(expect, actual, EPSILON);
    }

    @Test
    public void testFruitData() throws Exception {
        System.out.println("testFruitData");
        int limit = 5;

        List<Indexed<SparseDoubleVector>> vecs = loadFruitVectors();

        limit = Math.min(limit, vecs.size());

        final double[][] results = new double[limit][limit];
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < limit; j++) {
                SparseDoubleVector A = vecs.get(i).value();
                SparseDoubleVector B = vecs.get(j).value();
                results[i][j] = test(A, B);
            }
        }

        // diagonals should all be +1
        for (int i = 0; i < limit; i++) {
            assertEquals(INSTANCE.getHomogeneityBound(), results[i][i], EPSILON);
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

        double expect = test(A, B);
    }

    @Test
    public void testCompareToNaiveImpl() throws Exception {
        System.out.println("testCompareToNaiveImpl");
        int size = 100;
        int repeats = 10;
        for (int i = 0; i < repeats; i++) {
            SparseDoubleVector A = new SparseDoubleVector(size * 2, size);
            SparseDoubleVector B = new SparseDoubleVector(size * 2, size);
            for (int j = 0; j < size; j++) {
                A.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
                B.set(RANDOM.nextInt(size * 2), RANDOM.nextDouble());
            }
            double expect = tauB_naive1(A, B);
            double actual = test(A, B);
            assertEquals(expect, actual, EPSILON);
        }
    }

    public double test(SparseDoubleVector A, SparseDoubleVector B) {
        final double val = INSTANCE.similarity(A, B);
        assertFalse("Similarity is NaN", Double.isNaN(val));
        assertFalse("Similarity is " + val, Double.isInfinite(val));
        assertTrue("Similarity < -1", val >= INSTANCE.getHeterogeneityBound());
        assertTrue("Similarity > +1", val <= INSTANCE.getHomogeneityBound());
        return val;
    }

    static double tauB_naive1(SparseDoubleVector A, SparseDoubleVector B) {
        assert A.cardinality == B.cardinality :
                "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;

        final int n = A.cardinality;
        long conc = 0;
        long ta = 0;
        long tb = 0;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                final double dA = A.get(i) - A.get(j);
                final double dB = B.get(i) - B.get(j);
                if (dA == 0)
                    ++ta;
                if (dB == 0)
                    ++tb;
                conc += Math.signum(dA) * Math.signum(dB);

            }
        }

        long n0 = (n * (n - 1)) >> 1;
        double denom = Math.sqrt((n0 - ta) * (n0 - tb));
//
//        System.out.printf("n0=%d, ti=%d, tj=%d, conc=%d, denom=%f, sim=%f%n",
//                          n0, ta, tb, conc, denom, conc / denom);

        return conc / denom;
    }

    static List<Indexed<SparseDoubleVector>> loadFruitVectors() throws IOException {

        final DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();
        final FastWeightedTokenPairVectorSource eventSrc =
                BybloIO.openEventsVectorSource(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, indexDeligate);
        final List<Indexed<SparseDoubleVector>> vecs =
                new ArrayList<Indexed<SparseDoubleVector>>();
        while (eventSrc.hasNext())
            vecs.add(eventSrc.read());

        if (eventSrc instanceof Closeable) {
            ((Closeable) eventSrc).close();
        }

        return vecs;
    }
//    
//    static double tauA_naive1(SparseDoubleVector A, SparseDoubleVector B) {
//        assert A.cardinality == B.cardinality :
//                "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;
//
//        final int n = A.cardinality;
//        double result = 0;
//        for (int i = 1; i < n; i++) {
//            for (int j = 0; j < i; j++) {
//                result += Math.signum(A.get(i) - A.get(j))
//                        * Math.signum(B.get(i) - B.get(j));
//            }
//        }
//        double denom = 0.5 * n * (n - 1);
//
//        return result / denom;
//    }
//
//    @Test
//    @Ignore
//    public void testThing() throws Exception {
//        System.out.println("test");
//
//        DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();
//        FastWeightedTokenPairVectorSource eventSrc =
//                BybloIO.openEventsVectorSource(
//                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, indexDeligate);
//
//        List<Indexed<SparseDoubleVector>> vecs =
//                new ArrayList<Indexed<SparseDoubleVector>>();
//        while (eventSrc.hasNext()) {
//            vecs.add(eventSrc.read());
//        }
//
//        // Calcualte the maximum cardinality of the vectors
//        int cardinality = 0;
//        for (Indexed<SparseDoubleVector> A : vecs) {
//            if (A.value().cardinality > cardinality)
//                cardinality = A.value().cardinality;
//        }
//
//        //Update all the vectors to have the same cardinality
//        for (Indexed<SparseDoubleVector> A : vecs) {
//            A.value().cardinality = cardinality;
//        }
////
////        double tau = naive_tauA(vecs.get(0).value(), vecs.get(0).value());
////
////        System.out.printf("%d %d %f%n", vecs.get(0).key(), vecs.get(0).key(),
////                          tau);
//
////             
////
////        tauB_naive1(vecs.get(0).value(), vecs.get(1).value());
////        tauB_naive3(vecs.get(0).value(), vecs.get(1).value());
//////
////        tauB_naive1(vecs.get(0).value(), vecs.get(0).value());
//        tauB_merge1(vecs.get(1).value(), vecs.get(0).value());
//
//        for (Indexed<SparseDoubleVector> A : vecs) {
//            for (Indexed<SparseDoubleVector> B : vecs) {
//
////
////                {
////                    System.out.println("imp1");
////
////                    final long t0 = System.currentTimeMillis();
////                    final double tau = tauB_naive1(A.value(), B.value());
////
////                    assertTrue("Tau expected in range [-1,+1] but found " + tau,
////                               tau >= -1 && tau <= +1);
////
////
////
////                    System.out.printf("%d %d %f   (%f seconds)%n",
////                                      A.key(), B.key(), tau,
////                                      (System.currentTimeMillis() - t0) / 1000.0);
////                }
//
////                {
////                    System.out.println("imp2");
////
////                    final long t0 = System.currentTimeMillis();
////                    final double tau = tauB_naive2(A.value(), B.value());
////
////                    assertTrue("Tau expected in range [-1,+1] but found " + tau,
////                               tau >= -1 && tau <= +1);
////
////
////
////                    System.out.printf("%d %d %f   (%f seconds)%n",
////                                      A.key(), B.key(), tau,
////                                      (System.currentTimeMillis() - t0) / 1000.0);
////                }
//
////                {
//
////                    final long t0 = System.currentTimeMillis();
//
////                    final double tau = new KendallsTauRC().proximity(A.value(), B.value());
////
////                    assertTrue("Tau expected in range [-1,+1] but found " + tau,
////                               tau >= -1 && tau <= +1);
////
////
////
////                    System.out.printf("imp3: %d %d %f   (%f seconds)%n",
////                                      A.key(), B.key(), tau,
////                                      (System.currentTimeMillis() - t0) / 1000.0);
//
////                }
////                
////                  {
////                 
////
//                final long t0 = System.currentTimeMillis();
////                    
////                    
////                    
////                final double tau = new TingDem(A.value(), B.value()).calc();
//                final double tau = new KendallsTau().proximity(A.value(), B.
//                        value());
////                    assertTrue("Tau expected in range [-1,+1] but found " + tau,
////                               tau >= -1 && tau <= +1);
////
////
//
//                System.out.printf("imp4: %d %d %f   (%f seconds)%n",
//                                  A.key(), B.key(), tau,
//                                  (System.currentTimeMillis() - t0) / 1000.0);
//
////                }
////
////                {
////
////                    final long t0 = System.currentTimeMillis();
////                    final double tau = tauB_merge1(A.value(), B.value());
////
////                    assertTrue("Tau expected in range [-1,+1] but found " + tau,
////                               tau >= -1 && tau <= +1);
////
////
////
////                    System.out.printf("%d %d %f   (%f seconds)%n",
////                                      A.key(), B.key(), tau,
////                                      (System.currentTimeMillis() - t0) / 1000.0);
////                }
//
//            }
//        }
//
////        System.out.println(vecs);
//
//
//    }
//
//    static final class TingDem {
//
//        private final SparseDoubleVector A;
//
//        private final SparseDoubleVector B;
//
//        private long cordance = 0;
//
//        private long aties = 0;
//
//        private long bties = 0;
//
//        private int intersectionSize = 0;
//
//        public TingDem(SparseDoubleVector A, SparseDoubleVector B) {
//            Checks.checkNotNull(A);
//            Checks.checkNotNull(B);
//            this.A = A;
//            this.B = B;
//        }
//
//        public double calc() {
//            assert A.cardinality == B.cardinality :
//                    "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;
//
//            final int N = A.cardinality;
//
//            final int L = A.size;
//            final int M = B.size;
//
//            calc1();
//
//            final int unionSize = (L + M) - intersectionSize;
//
//            // Features that don't occur in either vector are a similarity
//            // between the two sets. For each feature that they both have there
//            // should be an addition +2 to the sum.
//            // The relationship between these and disjoint features
//            cordance += ((N - unionSize) * intersectionSize);
//
//            // Outside of those in the union all elements are zero add all pairwise
//            // combinations to the ties counters.
//            aties += ((N - unionSize) * (N - unionSize - 1)) >> 1;
//            bties += ((N - unionSize) * (N - unionSize - 1)) >> 1;
//
//            // We also need to the add the cross-tries between zeros in union, and
//            // everything else
//            aties += ((unionSize - L) * (N - unionSize));
//            bties += ((unionSize - M) * (N - unionSize));
//
//            // Within the union minus the size of vector, all elements are zero so
//            // add all pairwise combinations
//            aties += ((unionSize - L) * (unionSize - L - 1)) >> 1;
//            bties += ((unionSize - M) * (unionSize - M - 1)) >> 1;
////            
//            final long n0 = (N * (N - 1)) >> 1;
//            final double denom = Math.sqrt((n0 - aties) * (n0 - bties));
//            final double sim = cordance / denom;
//
//
////            
////            System.out.printf("n0=%d, ti=%d, tj=%d, conc=%d, denom=%f, sim=%f%n",
////                              n0, aties, bties, cordance, denom,
////                              cordance / denom);
////            
//            return sim;
//        }
//
//        void calc1() {
//            int i = 0, j = 0;
//            while (i + j < A.size + B.size) {
//                if (i < A.size && (j == B.size || A.keys[i] < B.keys[j])) {
//                    inner(i, j, A.values[i], 0);
//                    ++i;
//                } else if (j < B.size && (i == A.size || B.keys[j] < A.keys[i])) {
//                    inner(i, j, 0, B.values[j]);
//                    ++j;
//                } else if (i < A.size && j < B.size) {
//                    inner(i, j, A.values[i], B.values[j]);
//                    ++intersectionSize;
//                    ++i;
//                    ++j;
//                }
//            }
//
//        }
//
//        private void inner(final int M, final int N,
//                           final double aiv, final double biv) {
//            int i = 0, j = 0;
//            while (i + j < M + N) {
//                if (i < M && (j == N || A.keys[i] < B.keys[j])) {
//                    if (A.values[i] != 0)
//                        concordance(aiv, biv, A.values[i], 0);
//                    ++i;
//                } else if (j < N && (i == M || B.keys[j] < A.keys[i])) {
//                    if (B.values[j] != 0)
//                        concordance(aiv, biv, 0, B.values[j]);
//                    ++j;
//                } else if (i < M && j < N) {
//                    concordance(aiv, biv, A.values[i], B.values[j]);
//                    ++i;
//                    ++j;
//                }
//            }
//        }
//
//        private void concordance(final double aiv, final double biv,
//                                 final double ajv, final double bjv) {
//            if (aiv == ajv)
//                ++this.aties;
//            if (biv == bjv)
//                ++this.bties;
//            this.cordance += signum(aiv - ajv)
//                    * signum(biv - bjv);
//        }
//    }
//
//
//
//    static double tauB_naive2(SparseDoubleVector A, SparseDoubleVector B) {
//        System.out.println("tauB_naive2");
//        assert A.cardinality == B.cardinality :
//                "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;
//        int n = A.cardinality;
//
//        int cordance = 0;
//        int intersectionSize = 0;
//        int unionSize = 0;
//        int ai = 0;
//        int bi = 0;
//
//        // Count ties
//        long ta = 0;
//        long tb = 0;
//
//        while (ai < A.size && bi < B.size) {
//            ++unionSize;
//            if (A.keys[ai] < B.keys[bi]) {
//                int aj = ai + 1;
//                int bj = bi;
//                while (aj < A.size && bj < B.size) {
//                    if (A.keys[aj] < B.keys[bj]) {
//                        if (A.values[ai] == A.values[aj])
//                            ++ta;
//                        ++aj;
//                    } else if (A.keys[aj] > B.keys[bj]) {
//                        --cordance;
//                        ++bj;
//                    } else {
//                        if (A.values[ai] < A.values[aj])
//                            ++cordance;
//                        else if (A.values[ai] > A.values[aj])
//                            --cordance;
//                        else
//                            ++ta;
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                cordance -= B.size - bj;
//                while (aj < A.size) {
//                    if (A.values[ai] == A.values[aj])
//                        ++ta;
//                    ++aj;
//                }
//                while (bj < B.size) {
//                    if (B.values[bi] == B.values[bj])
//                        ++tb;
//                    ++bj;
//                }
//                ++ai;
//            } else if (A.keys[ai] > B.keys[bi]) {
//                int aj = ai;
//                int bj = bi + 1;
//                while (aj < A.size && bj < B.size) {
//                    if (A.keys[aj] < B.keys[bj]) {
//                        --cordance;
//                        ++aj;
//                    } else if (A.keys[aj] > B.keys[bj]) {
//                        ++bj;
//                    } else {
//                        if (B.values[bi] < B.values[bj])
//                            ++cordance;
//                        else if (B.values[bi] > B.values[bj])
//                            --cordance;
//                        else
//                            ++tb;
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                cordance -= A.size - aj;
//                while (aj < A.size) {
//                    if (A.values[ai] == A.values[aj])
//                        ++ta;
//                    ++aj;
//                }
//                while (bj < B.size) {
//                    if (B.values[bi] == B.values[bj])
//                        ++tb;
//                    ++bj;
//                }
//                ++bi;
//            } else {
//                ++intersectionSize;
//                int aj = ai + 1;
//                int bj = bi + 1;
//                while (aj < A.size && bj < B.size) {
//                    if (A.keys[aj] < B.keys[bj]) {
//                        if (A.values[ai] < A.values[aj])
//                            --cordance;
//                        else if (A.values[ai] > A.values[aj])
//                            ++cordance;
//                        else
//                            ++ta;
//                        ++aj;
//                    } else if (A.keys[aj] > B.keys[bj]) {
//                        if (B.values[bi] < B.values[bj])
//                            --cordance;
//                        else if (B.values[bi] > B.values[bj])
//                            ++cordance;
//                        else
//                            ++tb;
//                        ++bj;
//                    } else {
//                        final double diff = (A.values[ai] - A.values[aj])
//                                * (B.values[bi] - B.values[bj]);
//                        if (diff < 0)
//                            --cordance;
//                        else if (diff > 0)
//                            ++cordance;
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                while (aj < A.size) {
//                    if (A.values[ai] < A.values[aj])
//                        --cordance;
//                    else if (A.values[ai] > A.values[aj])
//                        ++cordance;
//                    else
//                        ++ta;
//                    ++aj;
//                }
//                while (bj < B.size) {
//                    if (B.values[bi] < B.values[bj])
//                        --cordance;
//                    else if (B.values[bi] > B.values[bj])
//                        ++cordance;
//                    else
//                        ++tb;
//                    ++bj;
//                }
//                ++ai;
//                ++bi;
//            }
//        }
//        while (ai < A.size) {
//            ++unionSize;
//            int aj = ai + 1;
//            int bj = bi;
//            while (aj < A.size && bj < B.size) {
//                if (A.keys[aj] < B.keys[bj]) {
//                    ++aj;
//                } else if (A.keys[aj] > B.keys[bj]) {
//                    --cordance;
//                    ++bj;
//                } else {
//                    if (A.values[ai] < A.values[aj])
//                        ++cordance;
//                    else if (A.values[ai] > A.values[aj])
//                        --cordance;
//                    else
//                        ++ta;
//                    ++aj;
//                    ++bj;
//                }
//            }
//            cordance -= B.size - bj;
//            while (aj < A.size) {
//                if (A.values[ai] == A.values[aj])
//                    ++ta;
//                ++aj;
//            }
//            while (bj < B.size) {
//                if (B.values[bi] == B.values[bj])
//                    ++tb;
//                ++bj;
//            }
//            ++ai;
//        }
//        while (bi < B.size) {
//            ++unionSize;
//            int aj = ai;
//            int bj = bi + 1;
//            while (aj < A.size && bj < B.size) {
//                if (A.keys[aj] < B.keys[bj]) {
//                    --cordance;
//                    ++aj;
//                } else if (A.keys[aj] > B.keys[bj]) {
//                    ++bj;
//                } else {
//                    if (B.values[bi] < B.values[bj])
//                        ++cordance;
//                    else if (B.values[bi] > B.values[bj])
//                        --cordance;
//                    else
//                        ++tb;
//                    ++aj;
//                    ++bj;
//                }
//            }
//            cordance -= A.size - aj;
//            while (aj < A.size) {
//                if (A.values[ai] == A.values[aj])
//                    ++ta;
//                ++aj;
//            }
//            while (bj < B.size) {
//                if (B.values[bi] == B.values[bj])
//                    ++tb;
//                ++bj;
//            }
//            ++bi;
//        }
//
//        System.out.println("n=" + n + ", |A|=" + A.size);
//        System.out.println("n=" + n + ", |B|=" + B.size);
//        System.out.println(ta + " " + tb);
//        ta += ((n - A.size) * (n - A.size)) >> 1;
//        tb += ((n - B.size) * (n - B.size)) >> 1;
//        System.out.println(ta + " " + tb);
//
//        // Comparisons are only done in one direction so double the result
//        cordance <<= 1;
//
//
//        // Features that don't occur in either vector are a similarity
//        // between the two sets. For each feature that they both have there
//        // should be an addition +2 to the sum.
//        // The relationship between these and disjoint features
//        cordance += ((n - unionSize) * intersectionSize);
//        //
//
//        long n0 = (n * (n - 1)) >> 1;
//
//
//        double denom = Math.sqrt((n0 - ta) * (n0 - tb));
//        double sim = (double) (cordance) / denom;
//
//        System.out.printf("n0=%d, ti=%d, tj=%d, conc=%d, denom=%f%n",
//                          n0, ta, tb, cordance, denom);
//
//
//        return sim;
//    }
//
//    static double tauB_naive3(SparseDoubleVector A, SparseDoubleVector B) {
//        assert A.cardinality == B.cardinality :
//                "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;
//        final int totalSize = A.cardinality;
//        final int aSize = A.size;
//        final int bSize = B.size;
//
//        long cordance = 0;
//        long aties = 0;
//        long bties = 0;
//
//        int intersectionSize = 0;
//
//        int ai = 0, bi = 0;
//        while (ai + bi < aSize + bSize) {
//            if (ai < aSize && (bi == bSize || A.keys[ai] < B.keys[bi])) {
//                int aj = 0, bj = 0;
//                while (aj + bj < ai + bi) {
//                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
//                        if (A.values[ai] == A.values[aj])
//                            ++aties;
//                        ++aj;
//                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
//                        cordance += signum(A.values[ai])
//                                * signum(-B.values[bj]);
//                        ++bj;
//                    } else if (aj < ai && bj < bi) {
//                        if (A.values[ai] == A.values[aj])
//                            ++aties;
//                        cordance += signum(A.values[ai] - A.values[aj])
//                                * signum(-B.values[bj]);
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                ++ai;
//            } else if (bi < bSize && (ai == aSize || B.keys[bi] < A.keys[ai])) {
//                int aj = 0, bj = 0;
//                while (aj + bj < ai + bi) {
//                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
//                        cordance += signum(-A.values[aj])
//                                * signum(B.values[bi]);
//                        ++aj;
//                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
//                        if (B.values[bi] == B.values[bj])
//                            ++bties;
//                        ++bj;
//                    } else if (aj < ai && bj < bi) {
//                        if (B.values[bi] == B.values[bj])
//                            ++bties;
//                        cordance += signum(-A.values[aj])
//                                * signum(B.values[bi] - B.values[bj]);
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                ++bi;
//            } else if (ai < aSize && bi < bSize) {
//                int aj = 0, bj = 0;
//                while (aj + bj < ai + bi) {
//                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
//                        if (A.values[ai] == A.values[aj])
//                            ++aties;
//                        cordance += signum(A.values[ai] - A.values[aj])
//                                * signum(B.values[bi]);
//                        ++aj;
//                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
//                        if (B.values[bi] == B.values[bj])
//                            ++bties;
//                        cordance += signum(A.values[ai])
//                                * signum(B.values[bi] - B.values[bj]);
//                        ++bj;
//                    } else if (aj < ai && bj < bi) {
//                        if (A.values[ai] == A.values[aj])
//                            ++aties;
//                        if (B.values[bi] == B.values[bj])
//                            ++bties;
//                        cordance += signum(A.values[ai] - A.values[aj])
//                                * signum(B.values[bi] - B.values[bj]);
//                        ++aj;
//                        ++bj;
//                    }
//                }
//                ++intersectionSize;
//                ++ai;
//                ++bi;
//            }
//        }
//
//        final int unionSize = (aSize + bSize) - intersectionSize;
//
//        // Features that don't occur in either vector are a similarity
//        // between the two sets. For each feature that they both have there
//        // should be an addition +2 to the sum.
//        // The relationship between these and disjoint features
//        cordance += ((totalSize - unionSize) * intersectionSize);
//
//        // Outside of those in the union all elements are zero add all pairwise
//        // combinations to the ties counters.
//        aties += ((totalSize - unionSize) * (totalSize - unionSize - 1)) >> 1;
//        bties += ((totalSize - unionSize) * (totalSize - unionSize - 1)) >> 1;
//
//        // We also need to the add the cross-tries between zeros in union, and
//        // everything else
//        aties += ((unionSize - aSize) * (totalSize - unionSize));
//        bties += ((unionSize - bSize) * (totalSize - unionSize));
//
//        // Within the union minus the size of vector, all elements are zero so
//        // add all pairwise combinations
//        aties += ((unionSize - aSize) * (unionSize - aSize - 1)) >> 1;
//        bties += ((unionSize - bSize) * (unionSize - bSize - 1)) >> 1;
//
//        final long n0 = (totalSize * (totalSize - 1)) >> 1;
//        final double denom = Math.sqrt((n0 - aties) * (n0 - bties));
//        final double sim = cordance / denom;
////
////        System.out.printf(
////                "n0=%d, ti=%d, tj=%d, conc=%d, denom=%f, sim=%f%n",
////                n0, aties, bties, cordance, denom, sim);
//
//        return sim;
//    }
//
//    static double tauB_merge1(SparseDoubleVector A, SparseDoubleVector B) {
//        assert A.cardinality == B.cardinality :
//                "Cardinalities not equal " + A.cardinality + " != " + B.cardinality;
//        final int totalSize = A.cardinality;
//        final int aSize = A.size;
//        final int bSize = B.size;
//
//
//        final double[][] data = unionArray(A, B);
//
////        System.out.println(Arrays.deepToString(data));
//
//        sort(data);
//
//
//        final double[] x = new double[data.length];
//        final double[] y = new double[data.length];
//
//        for (int i = 0; i < data.length; i++) {
//            x[i] = data[i][0];
//            y[i] = data[i][1];
//        }
////
////        System.out.println(Arrays.toString(x));
////        System.out.println(Arrays.toString(y));
////
//
//
//        System.out.println(swaps(y));
//
//
//        return 0;
//    }
//
//    public static void sort(double[][] a) {
//        sort1(a, 0, a.length);
//    }
//
//    private static void sort1(double x[][], int off, int len) {
//        // Insertion sort on smallest arrays
//        if (len < 7) {
//            for (int i = off; i < len + off; i++)
//                for (int j = i; j > off && (x[j - 1][0] > x[j][0] || (x[j - 1][0] == x[j][0] && x[j - 1][1] > x[j][1])); j--)
//                    swap(x, j, j - 1);
//            return;
//        }
//
//        // Choose a partition element, v
//        int m = off + (len >> 1);       // Small arrays, middle element
//        if (len > 7) {
//            int l = off;
//            int n = off + len - 1;
//            if (len > 40) {        // Big arrays, pseudomedian of 9
//                int s = len / 8;
//                l = med3(x, l, l + s, l + 2 * s);
//                m = med3(x, m - s, m, m + s);
//                n = med3(x, n - 2 * s, n - s, n);
//            }
//            m = med3(x, l, m, n); // Mid-size, med of 3
//        }
//        double[] v = x[m];
//
//        // Establish Invariant: v* (<v)* (>v)* v*
//        int a = off, b = a, c = off + len - 1, d = c;
//        while (true) {
//            while (b <= c && (x[b][0] < v[0] || (x[b][0] == v[0] && x[b][1] <= v[1]))) {
//                if (x[b][0] == v[0])
//                    swap(x, a++, b);
//                b++;
//            }
//            while (c >= b && (x[c][0] > v[0] || (x[b][0] == v[0] && x[b][1] >= v[1]))) {
//                if (x[c][0] == v[0])
//                    swap(x, c, d--);
//                c--;
//            }
//            if (b > c)
//                break;
//            swap(x, b++, c--);
//        }
//
//        // Swap partition elements back to middle
//        int s, n = off + len;
//        s = Math.min(a - off, b - a);
//        vecswap(x, off, b - s, s);
//        s = Math.min(d - c, n - d - 1);
//        vecswap(x, b, n - s, s);
//
//        // Recursively sort non-partition-elements
//        if ((s = b - a) > 1)
//            sort1(x, off, s);
//        if ((s = d - c) > 1)
//            sort1(x, n - s, s);
//    }
//
//    /**
//     * Swaps x[a] with x[b].
//     */
//    private static void swap(double x[][], int a, int b) {
//        double[] t = x[a];
//        x[a] = x[b];
//        x[b] = t;
//    }
//
//    /**
//     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
//     */
//    private static void vecswap(double x[][], int a, int b, int n) {
//        for (int i = 0; i < n; i++, a++, b++)
//            swap(x, a, b);
//    }
//
//    /**
//     * Returns the index of the median of the three indexed doubles.
//     */
//    private static int med3(double x[][], int a, int b, int c) {
//        return (x[a][0] < x[b][0]
//                ? (x[b][0] < x[c][0] ? b : x[a][0] < x[c][0] ? c : a)
//                : (x[b][0] > x[c][0] ? b : x[a][0] > x[c][0] ? c : a));
//    }
//
////    static double[][] coSort(double[][] data, int left, int right) {
////
////        int mid = left + (right - left) / 2;
////
////        return coMerge(coSort(data, left, mid), coSort(data, mid + 1, right));
////    }
//////
////    static double[][] coMerge(double[][] a, double[][] b) {
////        double[][] out = new double[2][a[0].length];
////
////        int i = 0, j = 0, k = 0;
////        while (i < A.size && j < B.size) {
////            if (A.keys[i] < B.keys[j]) {
////                out[0][k] = A.values[i++];
////                out[1][k] = 0;
////                ++k;
////            } else if (B.keys[j] < A.keys[i]) {
////                out[0][k] = 0;
////                out[1][k] = B.values[j++];
////                ++k;
////            } else {
////                out[0][k] = A.values[i++];
////                out[1][k] = B.values[j++];
////                ++k;
////            }
////        }
////        while (i < A.size) {
////            out[0][k] = A.values[i++];
////            bOut[k] = 0;
////            ++k;
////        }
////        while (j < B.size) {
////            out[0][k] = 0;
////            out[1][k] = B.values[j++];
////            ++k;
////        }
////        return null;
////    }
////    private static final List<Int2DoubleMap.Entry> entryList(
////            SparseDoubleVector vector) {
////        final List<Int2DoubleMap.Entry> list =
////                new ArrayList<Int2DoubleMap.Entry>(vector.size);
////        for (int i = 0; i < vector.size; i++)
////            list.add(new AbstractInt2DoubleMap.BasicEntry(vector.keys[i],
////                                                          vector.values[i]));
////        return list;
////    }
////
////    private static final Comparator<Int2DoubleMap.Entry> VALUE_ORDER_ASC =
////            new Comparator<Int2DoubleMap.Entry>() {
////
////                @Override
////                public int compare(Int2DoubleMap.Entry a, Int2DoubleMap.Entry b) {
////                    return Double.compare(a.getDoubleValue(), b.getDoubleValue());
////                }
////            };
//    static long swaps(double[] y) {
//        if (y.length < 2)
//            return 0;
//        int mid = y.length / 2;
//
//        double[] left = Arrays.copyOfRange(y, 0, mid);
//        double[] right = Arrays.copyOfRange(y, mid, y.length);
//
//        return swaps(left) + swaps(right)
//                + mergeCountSwaps(sorted(left), sorted(right));
//    }
//
//    static double[] sorted(double[] arr) {
//        double[] copy = Arrays.copyOf(arr, arr.length);
//        Arrays.sort(copy);
//        return copy;
//    }
//
//    static long mergeCountSwaps(double[] L, double[] R) {
//
//        final int m = R.length;
//        final int n = L.length;// + m;
//
//        long nSwaps = 0;
//        int i = 0, j = 0;
//        while (i + j < n) {
//            if (i >= m || R[j] < L[i]) {
//                nSwaps += m - i;
//                ++j;
//            } else {
//                ++i;
//            }
//        }
//        return nSwaps;
//    }
//
//    static double[][] unionArray(SparseDoubleVector A, SparseDoubleVector B) {
//        final double[][] out = new double[A.size + B.size][2];
//
//        int i = 0, j = 0, k = 0;
//        while (i < A.size && j < B.size) {
//            if (A.keys[i] < B.keys[j]) {
//                out[k++][0] = A.values[i++];
//            } else if (B.keys[j] < A.keys[i]) {
//                out[k++][1] = B.values[j++];
//            } else {
//                out[k][0] = A.values[i++];
//                out[k][1] = B.values[j++];
//                ++k;
//            }
//        }
//        while (i < A.size)
//            out[k++][0] = A.values[i++];
//        while (j < B.size)
//            out[k++][1] = B.values[j++];
//
//        return out.length == k ? out : Arrays.copyOf(out, k);
//    }
}

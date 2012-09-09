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
package uk.ac.susx.mlcl.byblo.measures;

import org.junit.*;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.byblo.measures.impl.*;
import uk.ac.susx.mlcl.byblo.weighings.FeatureMarginalsCarrier;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;

/**
 * Perform a set of tests on ALL measures.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class MeasuresTest {

    public MeasuresTest() {
    }

    static final double EPSILON = 0.0000000000001;

    static Measure[] MEASURES = new Measure[]{
            new Confusion(),
            new Cosine(),
            new Dice(),
            new DotProduct(),
            new Hindle(),
            new Jaccard(),
            new JensenShannonDivergence(),
            new KendallsTau(),
            new KullbackLeiblerDivergence(),
            new LambdaDivergence(LambdaDivergence.DEFAULT_LAMBDA),
            new LeeSkewDivergence(LeeSkewDivergence.DEFAULT_ALPHA),
            new Lin(),
            new LpSpaceDistance(Double.NEGATIVE_INFINITY),
            new LpSpaceDistance(0),
            new LpSpaceDistance(1),
            new LpSpaceDistance(2),
            new LpSpaceDistance(3),
            new LpSpaceDistance(Double.POSITIVE_INFINITY),
            new Overlap(),
            new Precision(),
            new Recall(),
            new Weeds(Weeds.DEFAULT_BETA, Weeds.DEFAULT_GAMMA),
            new Weeds(0.00, 1),
            new Weeds(1.00, 0),
            new Weeds(0.75, 0),
            new Weeds(0.50, 0),
            new Weeds(0.25, 0),
            new Weeds(0.00, 0)
    };

    static Map<Measure, Weighting> WEIGHTINGS;

    static Random RANDOM;

    static List<Indexed<SparseDoubleVector>> FRUIT_EVENTS;

    @BeforeClass
    public static void setUpClass() throws Exception {
        RANDOM = new Random(0);


        WEIGHTINGS = new HashMap<Measure, Weighting>();
        for (Measure m : MEASURES) {
            WEIGHTINGS.put(m, m.getExpectedWeighting().newInstance());
        }

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


        double[] feats = AllPairsCommand.readAllAsArray(featsStatSrc);
        double featsSum = featsStatSrc.getWeightSum();
        int featusCard = featsStatSrc.getMaxId() + 1;

        for (Measure m : MEASURES) {

            if (m instanceof FeatureMarginalsCarrier) {
                ((FeatureMarginalsCarrier) m).setFeatureCardinality(featusCard);
                ((FeatureMarginalsCarrier) m).setFeatureMarginals(feats);
                ((FeatureMarginalsCarrier) m).setGrandTotal(featsSum);
            }

            Weighting w = WEIGHTINGS.get(m);

            if (w instanceof FeatureMarginalsCarrier) {
                ((FeatureMarginalsCarrier) w).setFeatureCardinality(featusCard);
                ((FeatureMarginalsCarrier) w).setFeatureMarginals(feats);
                ((FeatureMarginalsCarrier) w).setGrandTotal(featsSum);
            }
        }

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
    public void testFruitEverything() throws IOException {
        System.out.println("testFruitEverything");

        int repeats = 10;

        for (int r = 0; r < repeats; r++) {
            int i = RANDOM.nextInt(FRUIT_EVENTS.size());
            int j = RANDOM.nextInt(FRUIT_EVENTS.size());

            SparseDoubleVector A = FRUIT_EVENTS.get(i).value();
            SparseDoubleVector B = FRUIT_EVENTS.get(j).value();

            for (Measure m : MEASURES) {
                SparseDoubleVector wA = WEIGHTINGS.get(m).apply(A);
                SparseDoubleVector wB = WEIGHTINGS.get(m).apply(B);

                test(m, wA, wB);
            }
        }
    }

    @Test
    public void testFruitHomoginiety() throws IOException {
        System.out.println("testFruitHomoginiety");

        int repeats = 10;

        for (int r = 0; r < repeats; r++) {
            int i = RANDOM.nextInt(FRUIT_EVENTS.size());
//            System.out.println("repeat " + r + ": entries " + FRUIT_EVENTS.get(i).
//                    key() + " and " + FRUIT_EVENTS.get(i).key());
//
            SparseDoubleVector A = FRUIT_EVENTS.get(i).value();
            SparseDoubleVector B = FRUIT_EVENTS.get(i).value();

            for (Measure m : MEASURES) {
//                System.out.print("\tmeasure " + m + ": ");
                SparseDoubleVector wA = WEIGHTINGS.get(m).apply(A);
                SparseDoubleVector wB = WEIGHTINGS.get(m).apply(B);

                double result = test(m, wA, wB);

//                System.out.println(result + " " + (result == m.
//                                                   getHomogeneityBound()));

                if (!(m instanceof Hindle
                        || m instanceof Confusion
                        || m instanceof DotProduct)) {
                    assertEquals(m.toString(), m.getHomogeneityBound(), result, EPSILON);
                }

            }

        }
    }

    @Test
    public void testSizeZeroVector() throws IOException {
        System.out.println("testSizeZeroVector");

        int repeats = 10;

        for (int r = 0; r < repeats; r++) {
            int i = FRUIT_EVENTS.size() - 1;
            int j = RANDOM.nextInt(FRUIT_EVENTS.size());

            SparseDoubleVector A = FRUIT_EVENTS.get(i).value();
            SparseDoubleVector B = FRUIT_EVENTS.get(j).value();

            for (Measure m : MEASURES) {
                SparseDoubleVector wA = WEIGHTINGS.get(m).apply(A);
                SparseDoubleVector wB = WEIGHTINGS.get(m).apply(B);

                test(m, wA, wB);
            }

        }
    }

    public double test(Measure instance, SparseDoubleVector A,
                       SparseDoubleVector B) {
        final double val = instance.similarity(A, B);
        assertFalse("Similarity is NaN" + " with measure " + instance,
                Double.isNaN(val));
        assertFalse("Similarity is " + val + " with measure " + instance,
                Double.isInfinite(val));

        final double min, max;
        if (instance.getHeterogeneityBound() < instance.getHomogeneityBound()) {
            min = instance.getHeterogeneityBound();
            max = instance.getHomogeneityBound();
        } else {
            min = instance.getHomogeneityBound();
            max = instance.getHeterogeneityBound();
        }
        assertTrue(
                "expected similarity >= " + min + " but found " + val + " using measure " + instance,
                val >= min - EPSILON);
        assertTrue(
                "expected similarity <= " + max + " but found " + val + " using measure " + instance,
                val <= max + EPSILON);


        if (instance.isCommutative()) {
            final double rev = instance.similarity(B, A);
            assertEquals("Measure is declared computative, but reversing "
                    + "operands results in a different score.", rev, val,
                    EPSILON);
        }

        return val;
    }
}

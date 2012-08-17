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
package uk.ac.susx.mlcl.byblo.weighings;

import it.unimi.dsi.fastutil.ints.AbstractInt2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;
import java.io.Serializable;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.byblo.commands.AllPairsCommand;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDeligates;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.FastWeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenSource;
import uk.ac.susx.mlcl.byblo.weighings.impl.*;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 *
 *
 * @author hamish
 */
public class WeightingsTest {

    public WeightingsTest() {
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

    static Class<? extends Weighting>[] WEIGHT_CLASSES =
            (Class<? extends Weighting>[]) new Class<?>[]{
                NullWeighting.class,
                Constant.class,
                Power.class,
                Likelyhood.class,
                LogProduct.class,
                L2UnitVector.class,
                Rank.class,
                Step.class,
                DiceWeighting.class,
                TTest.class,
                LLR.class,
                PMI.class,
                PositivePMI.class,
                NormalisedPMI.class,
                WeightedPMI.class,
                SquaredPMI.class,
                ChiSquared.class,
                GeoMean.class};

    @Test
    public void testWeightingImpls() throws Exception {
        System.out.println("testWeightingImpls");
        DoubleEnumerating indexDeligate = new DoubleEnumeratingDeligate();

        // Load the feature contexts
        MarginalDistribution fmd = BybloIO.readFeaturesMarginalDistribution(
                TEST_FRUIT_FEATURES, DEFAULT_CHARSET,
                EnumeratingDeligates.toSingleFeatures(indexDeligate));

        // Load the entry contexts
        MarginalDistribution emd = BybloIO.readEntriesMarginalDistribution(
                TEST_FRUIT_ENTRIES, DEFAULT_CHARSET,
                EnumeratingDeligates.toSingleFeatures(indexDeligate));

        assertEquals("marginal distributions totals differ",
                     fmd.getFrequencySum(), emd.getFrequencySum(), 0.000001);

        // Load the events vectors

        // Instantiate weighting objects

        Weighting[] wgts = new Weighting[WEIGHT_CLASSES.length];
        for (int i = 0; i < WEIGHT_CLASSES.length; i++) {
            Weighting wgt = WEIGHT_CLASSES[i].newInstance();

            if (wgt instanceof FeatureMarginalsCarrier)
                ((FeatureMarginalsCarrier) wgt).setFeatureMarginals(fmd);

            if (wgt instanceof EntryMarginalsCarrier)
                ((EntryMarginalsCarrier) wgt).setEntryMarginals(emd);

            wgts[i] = wgt;
        }

        FastWeightedTokenPairVectorSource eventSrc =
                BybloIO.openEventsVectorSource(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, indexDeligate);

        List<Indexed<SparseDoubleVector>> vecs =
                new ArrayList<Indexed<SparseDoubleVector>>();
        while (eventSrc.hasNext())
            vecs.add(eventSrc.read());



        int[][] mtot = new int[WEIGHT_CLASSES.length][WEIGHT_CLASSES.length];

        for (int vecIdx = 0; vecIdx < vecs.size(); vecIdx++) {

            final int entryId = vecs.get(vecIdx).key();
            final SparseDoubleVector vector = vecs.get(vecIdx).value();

//            System.out.println(" > testing entry id " + entryId);

            SparseDoubleVector[] reweighted = new SparseDoubleVector[WEIGHT_CLASSES.length];
            for (int wgtIdx = 0; wgtIdx < WEIGHT_CLASSES.length; wgtIdx++) {
                Weighting weighting = wgts[wgtIdx];
//                System.out.println("weighting: " + wgt);

                SparseDoubleVector rew = weighting.apply(vector);
                reweighted[wgtIdx] = rew;

                if (!weighting.getClass().equals(NullWeighting.class))
                    assertNotSame(rew, vector);
                assertNotNull(rew);

                // Check there are no zero valued elements
                for (int i = 0; i < rew.size; i++) {
                    if (rew.values[i] == 0.0)
                        fail(String.
                                format(
                                "Found zero value in sparse vector %d at id %d with weighting scheme %s",
                                entryId, rew.keys[i], weighting.getClass()));
                    if (Double.isNaN(rew.values[i])) {
                        fail(String.
                                format(
                                "Found NaN value in sparse vector %d at id %d with weighting scheme %s",
                                entryId, rew.keys[i], weighting.getClass()));
                    }

                    if (rew.values[i] < weighting.getLowerBound() || rew.values[i] > weighting.
                            getUpperBound()) {
                        fail(String.
                                format(
                                "sparse vector %d at id %d has value %f outside range with weighting scheme %s",
                                entryId, rew.keys[i], rew.values[i], weighting.
                                getClass()));
                    }
                }


//                System.out.println(rew.size);
//                print(entryId, rew);
            }


            boolean[][] m = new boolean[WEIGHT_CLASSES.length][WEIGHT_CLASSES.length];
//
            for (int i = 0; i < WEIGHT_CLASSES.length; i++) {
                for (int j = 0; j < i; j++) {
                    m[i][j] = isMonotonic(reweighted[i], reweighted[j]);
                    mtot[i][j] += m[i][j] ? 1 : 0;
                    mtot[j][i] += m[i][j] ? 1 : 0;
                }
            }

        }

        System.out.println(String.format(
                "Weighting schemes monotonicity (out of %d tests):",
                vecs.size()));
        for (int i = 0; i < WEIGHT_CLASSES.length; i++) {
            System.out.printf("%20s ", WEIGHT_CLASSES[i].getSimpleName());

            for (int j = 0; j < WEIGHT_CLASSES.length; j++) {
                if (i == j)
                    System.out.print(" X ");
                else
                    System.out.printf("%2d ", mtot[i][j]);

            }
            System.out.println();
        }
        System.out.println();

    }

    boolean isMonotonic(SparseDoubleVector a, SparseDoubleVector b) {

        int[] keys = merge(a.keys, b.keys);

        List<Int2DoubleMap.Entry> alist = new ArrayList<Int2DoubleMap.Entry>();
        List<Int2DoubleMap.Entry> blist = new ArrayList<Int2DoubleMap.Entry>();

        for (int i = 0; i < keys.length; i++) {
            alist.add(new AbstractInt2DoubleMap.BasicEntry(keys[i], a.get(
                    keys[i])));
            blist.add(new AbstractInt2DoubleMap.BasicEntry(keys[i], b.get(
                    keys[i])));
        }

        Comparator<Int2DoubleMap.Entry> cmp = new Int2DoubleEntryValueThenKeyComparator();

        Collections.sort(alist, cmp);
        Collections.sort(blist, cmp);


        for (int i = 0; i < alist.size(); i++)
            if (alist.get(i).getIntKey() != blist.get(i).getIntKey())
                return false;

        return true;
    }

    int[] merge(int[] x, int[] y) {
        int[] z = new int[x.length + y.length];
        int i = 0, j = 0, k = 0;
        while (i < x.length && j < y.length) {
            if (x[i] < y[j])
                z[k++] = x[i++];
            else if (x[i] > y[j])
                z[k++] = y[j++];
            else {
                z[k++] = x[i++];
                j++;
            }
        }
        System.arraycopy(x, i, z, k, x.length - i);
        System.arraycopy(y, j, z, k, y.length - j);
        return Arrays.copyOf(z, k);
    }

    void print(int entryId, SparseDoubleVector vec) {
        StringBuilder sb = new StringBuilder();
        sb.append(entryId);
        sb.append(" => ");
        for (int i = 0; i < vec.size; i++) {
            sb.append(vec.keys[i]);
            sb.append(':');
            sb.append(String.format("%f", vec.values[i]));
            sb.append(' ');
        }
        System.out.println(sb);
    }

    private static class Int2DoubleEntryValueThenKeyComparator
            implements Comparator<Entry>, Serializable {

        private static final long serialVersionUID = 1L;

        private Int2DoubleEntryValueThenKeyComparator() {
        }

        @Override
        public int compare(Int2DoubleMap.Entry o1, Int2DoubleMap.Entry o2) {
            int c = Double.compare(o1.getDoubleValue(), o2.getDoubleValue());
            return c != 0 ? c : o1.getIntKey() - o2.getIntKey();
        }
    }
}

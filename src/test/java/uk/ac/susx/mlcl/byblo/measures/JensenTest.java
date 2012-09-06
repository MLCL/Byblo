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

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairVectorSource;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.ac.susx.mlcl.TestConstants.*;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.disableExitTrapping;
import static uk.ac.susx.mlcl.lib.test.ExitTrapper.enableExistTrapping;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class JensenTest {

    @Test
    public void testJensenCLI() throws Exception {
        System.out.println("Testing Jensen from main method.");

        File output = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Jensen");
        output.delete();

        try {
            enableExistTrapping();
            Tools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Jensen",
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
    public void testJensen_Symmetry() throws Exception {
        System.out.println("Testing Jensen symmetry.");

        File output1 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Jensen-1");
        File output2 = new File(TEST_OUTPUT_DIR, FRUIT_NAME + ".Jensen-2");
        output1.delete();
        output2.delete();

        try {
            enableExistTrapping();
            Tools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Jensen",
                    "--input", TEST_FRUIT_EVENTS.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output1.toString()
            });
            Tools.main(new String[]{
                    "allpairs",
                    "--charset", "UTF-8",
                    "--measure", "Jensen",
                    "--measure-reversed",
                    "--input", TEST_FRUIT_EVENTS.toString(),
                    "--input-features", TEST_FRUIT_FEATURES.toString(),
                    "--input-entries", TEST_FRUIT_ENTRIES.toString(),
                    "--output", output2.toString()
            });

        } finally {
            disableExitTrapping();
        }
//
//        assertTrue(WeightedTokenPairSource.equal(output1, output2,
//                                                 DEFAULT_CHARSET, false, false));
    }

    @Test
    public void testVsReference() throws Exception {

        Jensen instance = new Jensen();

        DoubleEnumeratingDelegate del = new DoubleEnumeratingDelegate(
                Enumerating.DEFAULT_TYPE, false, false, null, null);

        WeightedTokenPairSource mdbsa = WeightedTokenPairSource.open(
                TEST_FRUIT_EVENTS, DEFAULT_CHARSET, del, false, false);
        WeightedTokenPairVectorSource vsa = mdbsa.getVectorSource();

        List<Indexed<SparseDoubleVector>> x = new ArrayList<Indexed<SparseDoubleVector>>();
        uk.ac.susx.mlcl.lib.io.ObjectIO.copy(vsa, x);


        for(Indexed<SparseDoubleVector> a : x) {
            for(Indexed<SparseDoubleVector> b : x) {
                final double expected = referenceImplementaiton(a.value(), b.value());
                final double actual = instance.shared(a.value(), b.value());
//                System.out.printf("%2d %2d: %5.3f %5.3f %s%n", a.key(), b.key(), expected, actual, Math.abs(expected-actual) <= 0.000001 ? "Equal" : "");
                Assert.assertEquals(expected, actual,  0.000001);
            }
        }
    }
    public static double referenceImplementaiton(SparseDoubleVector A, SparseDoubleVector B) {
        double divergence = 0.0;

        int i = 0;
        int j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                final double q = A.values[i] / A.sum;
                divergence += 0.5 * q;
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                final double r = B.values[j] / B.sum;
                divergence += 0.5 * r;
                ++j;
            } else {
                final double q = A.values[i] / A.sum;
                final double r = B.values[j] / B.sum;
                final double logAvg = log2(0.5 * (q + r));
                divergence += 0.5 * q * (log2(q) - logAvg)
                        + 0.5 * r * (log2(r) - logAvg);
                ++i;
                ++j;
            }
        }
        while (i < A.size) {
            final double q = A.values[i] / A.sum;
            divergence += 0.5 * q;
            i++;
        }
        while (j < B.size) {
            final double r = B.values[j] / B.sum;
            divergence += 0.5 * r;
            j++;
        }

        return divergence;
    }

    /**
     * Constant to aid conversion to base 2 logarithms.
     * <p/>
     * Conceptually it doesn't really matter what base is used, but 2 is the
     * standard base for most information theoretic approaches.
     * <p/>
     * TODO: Move to mlcl-lib/MathUtil
     */
    public static final double LOG_2 = Math.log(2.0);

    /**
     * Return the base 2 logarithm of the parameter v.
     * <p/>
     * TODO: Move to mlcl-lib/MathUtil
     *
     * @param v some values
     * @return logarithm of the value
     */
    public static double log2(final double v) {
        return Math.log(v) / LOG_2;
    }

}

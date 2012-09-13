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

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.weighings.MarginalDistribution;

import java.io.IOException;

import static uk.ac.susx.mlcl.TestConstants.DEFAULT_CHARSET;
import static uk.ac.susx.mlcl.TestConstants.TEST_FRUIT_FEATURES;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ConfusionTest extends AbstractMeasureTest<Confusion> {

    @Override
    public Class<? extends Confusion> getImplementation() {
        return Confusion.class;
    }

    @Override
    public String getMeasureName() {
        return "confusion";
    }

    @Override
    public Confusion newInstance() {
        try {
            Confusion instance = super.newInstance();
            final DoubleEnumerating indexDelegate = new DoubleEnumeratingDelegate();
            MarginalDistribution fmd = BybloIO
                    .readFeaturesMarginalDistribution(TEST_FRUIT_FEATURES,
                            DEFAULT_CHARSET, EnumeratingDelegates
                            .toSingleFeatures(indexDelegate));
            instance.setFeatureMarginals(fmd);

            return instance;
        } catch (IOException e) {
            throw new AssertionError(e);
        }

    }

    @Test
    @Ignore
    @Override
    public void testHomoginiety() {
        throw new UnsupportedOperationException();
    }

    @Test
    @Ignore
    @Override
    public void testHomoginiety2() {
        throw new UnsupportedOperationException();
    }

    @Test
    @Ignore
    @Override
    public void testSizeOneVectors() {
        throw new UnsupportedOperationException();
    }

    @Test
    @Ignore
    @Override
    public void testSizeTwoVectors() {
        throw new UnsupportedOperationException();
    }

    @Test
    @Ignore
    @Override
    public void testCardinalityOneVectors() {
        throw new UnsupportedOperationException();
    }

    // @Test
    // @Override
    // public void testSizeOneVectors() {
    // System.out.println("testSizeOneVectors");
    // SparseDoubleVector A = ones(100, 1);
    // SparseDoubleVector B = ones(100, 1);
    // Confusion instance = newInstance();
    // instance.setFeatureMarginals(featureMarginals(A, B));
    // double expect = instance.getIndependenceBound();
    // double actual = similarity(instance, A, B);
    //
    // Assert.assertEquals(expect, actual, EPSILON);
    // }
    //
    // @Test
    // @Override
    // public void testSizeTwoVectors() {
    // System.out.println("testSizeTwoVectors");
    // SparseDoubleVector A = ones(100, 2);
    // SparseDoubleVector B = ones(100, 2);
    // Confusion instance = newInstance();
    // instance.setFeatureMarginals(featureMarginals(A, B));
    // double expect = instance.getIndependenceBound();
    // double actual = similarity(instance, A, B);
    //
    // Assert.assertEquals(expect, actual, EPSILON);
    // }
    //
    // @Test
    // @Override
    // public void testCardinalityOneVectors() {
    // System.out.println("testCardinalityOneVectors");
    // SparseDoubleVector A = ones(1, 1);
    // SparseDoubleVector B = ones(1, 1);
    // Confusion instance = newInstance();
    // instance.setFeatureMarginals(featureMarginals(A, B));
    // double expect = instance.getIndependenceBound();
    // double actual = similarity(instance, A, B);
    //
    // Assert.assertEquals(expect, actual, EPSILON);
    // }

    @Test
    @Override
    @Ignore
    public void testFruitIdentity() {
        throw new UnsupportedOperationException();
    }

    // @Test
    // @Override
    // public void testFruitData() throws IOException {
    // System.out.println("testFruitData");
    // int limit = 5;
    //
    // List<SparseDoubleVector> vecs = TestConstants.loadFruitVectors();
    //
    // limit = Math.min(limit, vecs.size());
    //
    // Confusion instance = newInstance();
    // if (instance instanceof FeatureMarginalsCarrier)
    // ((FeatureMarginalsCarrier) instance)
    // .setFeatureMarginals(featureMarginals(vecs));
    //
    // final double[][] results = new double[limit][limit];
    // for (int i = 0; i < limit; i++) {
    // for (int j = 0; j < limit; j++) {
    // SparseDoubleVector A = vecs.get(i);
    // SparseDoubleVector B = vecs.get(j);
    // results[i][j] = similarity(instance, A, B);
    // }
    // }
    //
    // if (instance.isCommutative()) {
    // // triangular mirrors should be equal
    // for (int i = 0; i < limit; i++) {
    // for (int j = 0; j < limit; j++) {
    // Assert.assertEquals(results[i][j], results[j][i], EPSILON);
    // }
    // }
    // }
    // }

}

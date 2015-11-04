/*
 * Copyright (c) 2010-2013, University of Sussex
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
    public void testHomogeneity() {
        throw new UnsupportedOperationException();
    }

    @Test
    @Ignore
    @Override
    public void testHomogeneity2() {
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

    @Test
    @Override
    @Ignore
    public void testFruitIdentity() {
        throw new UnsupportedOperationException();
    }


}

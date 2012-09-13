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

import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.weighings.FeatureMarginalsCarrier;
import uk.ac.susx.mlcl.byblo.weighings.MarginalDistribution;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositiveWeighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * Proximity measure computing the confusion probability between the given
 * vector pair.
 * <p/>
 * <h4>Notes:</h4>
 * <p/>
 * <ul>
 * <p/>
 * <li>The implementation of this measure is rather strange because it requires
 * contextual information in a way totally unlike any other measure. Normally
 * when contextual information is required, it can be handled during
 * pre-weighting, but this measure is the exception.</li>
 * <p/>
 * <li>Unlikely all other measures, confusion has the property that an entry is
 * not necessarily the most similar entry to itself.</li>
 * <p/>
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see JE Weeds (2003) "Measures and Applications of Lexical Distributional
 *      Similarity", which references (Sugawara, Nishimura, Toshioka, Okachi, &
 *      Kaneko, 1985; Essen & Steinbiss, 1992; Grishman & Sterling, 1993; Dagan et
 *      al., 1999; Lapata et al., 2001)
 * @see Essen, Ute and Volker Steinbiss. 1992. Co-occurrence smoothing for
 *      stochastic language modeling. In Proceedings of ICASSP, volume 1, pages
 *      161{164.
 * @see Sugawara, K., M. Nishimura, K. Toshioka, M. Okochi, and T. Kaneko. 1985.
 *      Isolated word recognition using hidden Markov models. In Proceedings of
 *      ICASSP, pages 1--4, Tampa, Florida. IEEE.
 * @see Grishman, Ralph and John Sterling. 1993. Smoothing of automatically
 *      generated selectional constraints. In Human Language Technology, pages
 *      254{259, San Francisco, California. Advanced Research Projects Agency,
 *      Software and Intelligent Systems Technology Oce, Morgan Kaufmann.
 */
@CheckReturnValue
public class Confusion implements Measure, FeatureMarginalsCarrier {

    @Nullable
    private MarginalDistribution featureMarginals = null;

    public Confusion() {
    }

    @Override
    public MarginalDistribution getFeatureMarginals() {
        if (featureMarginals == null)
            throw new IllegalStateException(
                    "marginals requested before they where set.");
        return featureMarginals;
    }

    @Override
    public void setFeatureMarginals(MarginalDistribution featureMarginals) {
        this.featureMarginals = featureMarginals;
    }

    @Override
    public boolean isFeatureMarginalsSet() {
        return featureMarginals != null;
    }

    @Override
    @CheckReturnValue
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        final double N = featureMarginals.getFrequencySum();

        double sum = 0.0;

        int i = 0;
        int j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                ++j;
            } else {
                final double pFEa = A.values[i] / A.sum;
                final double pFEb = B.values[j] / B.sum;
                final double pF = featureMarginals.getPrior(A.keys[i]);
                final double pEa = A.sum / N;
                if (pFEa * pFEb * pEa * pF > 0)
                    sum += pFEa * pFEb * pEa / pF;
                ++i;
                ++j;
            }
        }

        return sum;
    }

    @Override
    public double getHomogeneityBound() {
        return 1;
    }

    @Override
    public double getHeterogeneityBound() {
        return 0;
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return PositiveWeighting.class;
    }

    @Override
    public boolean isCommutative() {
        return false;
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass();
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Confusion";
    }
}

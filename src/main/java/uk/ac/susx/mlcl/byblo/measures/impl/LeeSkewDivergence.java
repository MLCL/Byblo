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

import java.io.Serializable;
import java.text.MessageFormat;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import static uk.ac.susx.mlcl.byblo.weighings.Weightings.log2;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositiveWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Distance measure that computes similarity using Lee's alpha-Skew divergence.
 *
 * Skew divergence is a variant of Kullback–Leibler (KL) divergence. KL
 * divergence is undefined for zero probabilities in the empirical distribution.
 * To resolve this problem divergence is measured from a mixed distribution;
 * computed as the weighted average of the two distributions. The alpha
 * parameter controls this weighting.
 *
 * Alpha takes a value in the range [0,1] exclusive, where 0 is entirely the
 * first distribution, 1 is entirely the second distribution, and 0.5 is the
 * average distribution. Note that alpha <em>must<em> not be exactly 0 or 1, or
 * it becomes undefined for zero estimates just like KL divergence.
 *
 * Not a true metric, but effectively a distance measure.
 *
 * Lillian Lee (2001) "On the Effectiveness of the Skew Divergence for
 * Statistical Language Analysis" Artificial Intelligence and Statistics 2001,
 * pp. 65--72, 2001
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class LeeSkewDivergence implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_ALPHA = 0.99;

    private double alpha;

    public LeeSkewDivergence() {
        setAlpha(DEFAULT_ALPHA);
    }

    public LeeSkewDivergence(double alpha) {
        setAlpha(alpha);
    }

    public final double getAlpha() {
        return alpha;
    }

    public final void setAlpha(double alpha) {
        Checks.checkNotNaN("alpha", alpha);
        Checks.checkFinite("alpha", alpha);
        Checks.checkRangeExcl("alpha", alpha, 0.0, 1.0);
        this.alpha = alpha;
    }

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        double divergence = 0.0;

        int i = 0;
        int j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                final double r = B.values[j] / B.sum;
                final double logAvg = log2((1 - alpha) * r);
                divergence += r * (log2(r) - logAvg);
                ++j;
            } else {
                final double q = A.values[i] / A.sum;
                final double r = B.values[j] / B.sum;
                final double logAvg = log2(alpha * q + (1 - alpha) * r);
                divergence += r * (log2(r) - logAvg);
                ++i;
                ++j;
            }
        }
        while (j < B.size) {
            final double r = B.values[j] / B.sum;
            final double logAvg = log2((1 - alpha) * r);
            divergence += r * (log2(r) - logAvg);
            j++;
        }

        return divergence;
    }

    @Override
    public boolean isCommutative() {
        return false;
    }

    @Override
    public double getHomogeneityBound() {
        return 0;
    }

    @Override
    public double getHeterogeneityBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return PositiveWeighting.class;
    }

    @Override
    public String toString() {
        return MessageFormat.format("LeeSkewDivergence[alpha={0}]", alpha);
    }
}

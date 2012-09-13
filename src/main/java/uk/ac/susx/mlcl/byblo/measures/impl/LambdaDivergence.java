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
import uk.ac.susx.mlcl.byblo.measures.Measures;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositiveWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import java.io.Serializable;

import static uk.ac.susx.mlcl.byblo.weighings.Weightings.log2;

/**
 * Distance measure that computes similarity as the lambda divergence.
 * <p/>
 * The lambda weighted average of KL divergences of two distribution Q and R:
 * <p/>
 * lambdaD(Q||R) = lambda * D(Q||M) + (1 - lambda) * D(R||M)
 * <p/>
 * where M is the the mixed distribution:
 * <p/>
 * M = lambda * Q + (1 - lambda) * R:
 * <p/>
 * For lambda = 0.5 we have the Jensen-Shannon Divergence.
 * <p/>
 * For lambda = 0 and lambda = 1 the divergence is always 0, which is
 * meaningless. Hence, lambda must be in the range 0 &lt; lambda &lt; 1.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
public final class LambdaDivergence implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    private static final double EPSILON = 1E-15;

    @Nonnegative
    public static final double DEFAULT_LAMBDA = 0.5;

    @Nonnegative
    private double lambda;

    public LambdaDivergence() {
        setLambda(DEFAULT_LAMBDA);
    }

    public LambdaDivergence(@Nonnegative double lambda) {
        setLambda(lambda);
    }

    @Nonnegative
    public final double getLambda() {
        return lambda;
    }

    public final void setLambda(@Nonnegative double lambda) {
        Checks.checkRangeExcl("lambda", lambda, 0.0, 1.0);
        this.lambda = lambda;
    }

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        double divergence = 0.0;

        int i = 0;
        int j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                final double q = A.values[i] / A.sum;
                divergence += lambda * q * (log2(q) - log2(lambda * q));
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                final double r = B.values[j] / B.sum;
                divergence += (1.0 - lambda) * r
                        * (log2(r) - log2((1.0 - lambda) * r));
                ++j;
            } else {
                final double q = A.values[i] / A.sum;
                final double r = B.values[j] / B.sum;
                final double logAvg = log2(lambda * q + (1.0 - lambda) * r);
                divergence += lambda * q * (log2(q) - logAvg)
                        + (1.0 - lambda) * r * (log2(r) - logAvg);
                ++i;
                ++j;
            }
        }
        while (i < A.size) {
            final double q = A.values[i] / A.sum;
            divergence += lambda * q * (log2(q) - log2(lambda * q));
            i++;
        }
        while (j < B.size) {
            final double r = B.values[j] / B.sum;
            divergence += (1.0 - lambda) * r * (log2(r) - log2(
                    (1.0 - lambda) * r));
            j++;
        }

        // The algorithm introduces a small amount of floating point error, which can lead to result slightly outside
        // the expected bounds. To correct for this we clamp results to the bound when they are within some small value.
        if(Math.abs(divergence - getHeterogeneityBound()) < EPSILON)
            divergence = getHeterogeneityBound();
        if(Math.abs(divergence - getHomogeneityBound()) < EPSILON)
            divergence = getHomogeneityBound();


        return divergence;
    }

    @Override
    public boolean isCommutative() {
        return Measures.epsilonEquals(lambda, 0.5);
    }

    @Override
    public double getHomogeneityBound() {
        return 0;
    }

    @Override
    public double getHeterogeneityBound() {
        return 1.0;
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return PositiveWeighting.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LambdaDivergence that = (LambdaDivergence) o;

        return Double.compare(that.lambda, lambda) == 0;

    }

    @Override
    public int hashCode() {
        long temp = lambda != +0.0d ? Double.doubleToLongBits(lambda) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "Lambda-Divergence{lambda=" + lambda + '}';
    }
}

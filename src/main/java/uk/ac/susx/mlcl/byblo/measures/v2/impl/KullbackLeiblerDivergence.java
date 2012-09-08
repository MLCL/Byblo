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

import java.io.Serializable;
import java.text.MessageFormat;
import uk.ac.susx.mlcl.byblo.measures.v2.Measure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import static uk.ac.susx.mlcl.byblo.weighings.Weightings.log2;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositiveWeighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Distance measure that computes similarity as the Kullback–Leibler divergence,
 * with Laplace smoothing.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class KullbackLeiblerDivergence implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default expected minimum dimensionality of vectors ({@value}) if not
     * explicitly set.
     */
    public static final int DEFAULT_MIN_CARDINALITY = 1;

    /**
     * Expected dimensionality of vectors.
     */
    private int minCardinality;

    /**
     * Construct a new instance of {@link KullbackLeiblerDivergence } similarity
     * measure.
     *
     * The <tt>minCardinality</tt> field is initialized to
     * {@link KullbackLeiblerDivergence#DEFAULT_MIN_CARDINALITY}, which is
     * {@value #DEFAULT_MIN_CARDINALITY}
     */
    public KullbackLeiblerDivergence() {
        this(DEFAULT_MIN_CARDINALITY);
    }

    /**
     * Construct new instance of {@link KullbackLeiblerDivergence } similarity
     * measure, initializing the expected dimensionality of vectors to
     * <tt>minCardinality</tt>.
     *
     * @param minCardinality expected dimensionality of vectors
     * @throws IllegalArgumentException when
     * <code>minCardinality</code> is negative
     */
    public KullbackLeiblerDivergence(final int minCardinality)
            throws IllegalArgumentException {
        setMinCardinality(minCardinality);
    }

    /**
     * Get the minimum (usually the actual) cardinality of vectors.
     *
     * @return expected dimensionality of vectors
     */
    public final int getMinCardinality() {
        return minCardinality;
    }

    /**
     * Set the minimum (usually the actual) cardinality of vectors.
     *
     * If the vector cardinality is known before hand, but is not set on the
     * vectors for some reason, then method can be used to set it globally.
     *
     * @param minCardinality expected dimensionality of vectors
     * @throws IllegalArgumentException when
     * <code>minCardinality</code> is negative
     */
    public final void setMinCardinality(int minCardinality)
            throws IllegalArgumentException {
        if (minCardinality <= 0)
            throw new IllegalArgumentException(MessageFormat.format(
                    "expecting minCardinality > 0, but found {0}",
                    minCardinality));
        this.minCardinality = minCardinality;
    }

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        final int N = Math.max(minCardinality,
                               Math.max(A.cardinality, B.cardinality));

        final double sumA = A.sum + N;
        final double sumB = B.sum + N;

        // The smoothed likelyhoods for zero frequency features
        final double q0 = 1.0 / (A.sum + N);
        final double r0 = 1.0 / (B.sum + N);
        final double log_q0 = log2(q0);
        final double log_r0 = log2(r0);

        double divergence = 0.0;

        int i = 0;
        int j = 0;
        int intersectionSize = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                final double q = (A.values[i] + 1) / sumA;
                divergence += q * (log2(q) - log_r0);
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                final double r = (B.values[j] + 1) / sumB;
                divergence += q0 * (log_q0 - log2(r));
                ++j;
            } else {
                final double q = (A.values[i] + 1) / sumA;
                final double r = (B.values[j] + 1) / sumB;
                divergence += q * (log2(q) - log2(r));
                ++i;
                ++j;
                ++intersectionSize;
            }
        }
        while (i < A.size) {
            final double q = (A.values[i] + 1) / sumA;
            divergence += q * (log2(q) - log_r0);
            i++;
        }
        while (j < B.size) {
            final double r = (B.values[j] + 1) / sumB;
            divergence += q0 * (log_q0 - log2(r));
            j++;
        }

        // Finally add all the divergence components for features that did not
        // appear in either distribution
        final int unionSize = A.size + B.size - intersectionSize;
        divergence += (N - unionSize) * q0 * (log_q0 - log_r0);

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
        return "KL-Divergence";
    }
}

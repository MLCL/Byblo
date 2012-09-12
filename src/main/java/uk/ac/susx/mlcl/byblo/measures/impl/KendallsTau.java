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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import java.io.Serializable;
import java.text.MessageFormat;

import static java.lang.Math.signum;
import static uk.ac.susx.mlcl.byblo.measures.Measures.epsilonEquals;

/**
 * Kendall's Tau Rank Correlation Coefficient measures the similarity of two
 * lists of ranked data as the proportion of pairs that are concordant between
 * the lists.
 * <p/>
 * <h4>Notes:</h4>
 * <p/>
 * <ul>
 * <p/>
 * <li>This implementation is the "Tau B" variant, which makes adjustments for
 * tied elements in a manner suitable for equal cardinality vectors. Identical
 * vectors will always produce a similarity of +1 irrespective of ties </li>
 * <p/>
 * <li> Vectors that contain only the same value (i.e no ranked data at all)
 * will be given a score of 0.</li>
 * <p/>
 * <li>The {@link KendallsTau } measure is extremely inefficient (quadratic in
 * the number of features). For large numbers of dense feature vectors it will
 * become prohibitively slow. There is a more efficient algorithm for this
 * measure but it's pretty complicated to implement for sparse vectors.</li>
 * <p/>
 * <li>Ideally the cardinality should be set correctly in vectors to get
 * accurate results, however this is not strictly required. In the event that
 * vector cardinalities differ, the larger value will be used.</li>
 * <p/>
 * </ul>
 * <p/>
 *
 * @author Hamish I A Morgan <tt>&lt;hamish.morgan@sussex.ac.uk&gt;</tt>
 * @see "A New Measure of Rank Correlation." Maurice Kendall (1938). Biometrika
 *      30 (1–2): 81–89."
 */
@CheckReturnValue
public final class KendallsTau
        implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(KendallsTau.class);

    /**
     * Default expected minimum dimensionality of vectors ({@value}) if not
     * explicitly set.
     */
    @Nonnegative
    public static final int DEFAULT_MIN_CARDINALITY = 1;

    /**
     * Expected dimensionality of vectors.
     */
    @Nonnegative
    private int minCardinality;

    /**
     * Construct a new instance of {@link KendallsTau } similarity measure.
     * <p/>
     * The <tt>minCardinality</tt> field is initialized to
     * {@link KendallsTau#DEFAULT_MIN_CARDINALITY}, which is {@value
     * #DEFAULT_MIN_CARDINALITY}
     */
    public KendallsTau() {
        this(DEFAULT_MIN_CARDINALITY);
    }

    /**
     * Construct new instance of {@link KendallsTau } similarity measure,
     * initializing the expected dimensionality of vectors to
     * <tt>minCardinality</tt>.
     * <p/>
     *
     * @param minCardinality expected dimensionality of vectors
     * @throws IllegalArgumentException when <code>minCardinality</code> is
     *                                  negative
     */
    public KendallsTau(@Nonnegative
                       final int minCardinality)
            throws IllegalArgumentException {
        setMinCardinality(minCardinality);

        if (LOG.isWarnEnabled())
            LOG.warn("The KendallsTau proximity measure is extremely "
                    + "inefficient (quadratic on the number of features). "
                    + "Consider using a different measure.");
    }

    /**
     * Get the minimum (usually the actual) cardinality of vectors.
     * <p/>
     *
     * @return expected dimensionality of vectors
     */
    @Nonnegative
    public final int getMinCardinality() {
        return minCardinality;
    }

    /**
     * Set the minimum (usually the actual) cardinality of vectors.
     * <p/>
     * If the vector cardinality is known before hand, but is not set on the
     * vectors for some reason, then method can be used to set it globally.
     * <p/>
     *
     * @param minCardinality expected dimensionality of vectors
     * @throws IllegalArgumentException when <code>minCardinality</code> is
     *                                  negative
     */
    public final void setMinCardinality(@Nonnegative int minCardinality)
            throws IllegalArgumentException {
        if (minCardinality <= 0)
            throw new IllegalArgumentException(MessageFormat.format(
                    "expecting minCardinality > 0, but found {0}",
                    minCardinality));
        this.minCardinality = minCardinality;
    }

    /**
     * Compute the rank correlation score between the vector operands. Larger
     * values indicate a greater degree of similarity. Specifically the
     * orientation of this measure is: -1 indicating total disagreement, +1
     * indicating total agreement, and ~0 indicating independence.
     * <p/>
     *
     * @param A first feature vector
     * @param B second feature vector
     * @return rank correlation of the feature vectors
     */
    @Override
    public double similarity(
            final SparseDoubleVector A,
            final SparseDoubleVector B) {

        final int N = Math.max(minCardinality,
                Math.max(A.cardinality, B.cardinality));

        final int L = A.size;
        final int M = B.size;

        long cordance = 0;
        long aties = 0;
        long bties = 0;
        int intersectionSize = 0;


        // A double merge operations (one inside the other) the get all pairwise
        // combinations of non-zero elements in both vectors. This has been 
        // implemented with more-compact but slightly slower merge method, 
        // without trailing accumulators, to help reduce the probability of 
        // errors.
        int ai = 0, bi = 0;
        while (ai + bi < L + M) {
            if (ai < L && (bi == M || A.keys[ai] < B.keys[bi])) {
                // A[i] = A.values[ai]
                // B[i] = 0
                int aj = 0, bj = 0;
                while (aj + bj < ai + bi) {
                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
                        // A[j] = A.values[aj]
                        // B[j] = 0
                        if (epsilonEquals(A.values[ai], A.values[aj], 0))
                            ++aties;
                        ++aj;
                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
                        // A[j] = 0
                        // B[j] = B.values[bj]
                        cordance += signum(A.values[ai]) * signum(-B.values[bj]);
                        ++bj;
                    } else if (aj < ai && bj < bi) {
                        // A[j] = A.values[aj]
                        // B[j] = B.values[bj]
                        if (epsilonEquals(A.values[ai], A.values[aj], 0))
                            ++aties;
                        else
                            cordance += signum(A.values[ai] - A.values[aj])
                                    * signum(-B.values[bj]);
                        ++aj;
                        ++bj;
                    } else {
                        throw new AssertionError();
                    }
                }
                ++ai;
            } else if (bi < M && (ai == L || B.keys[bi] < A.keys[ai])) {
                // A[i] = 0
                // B[i] = B.values[bi]
                int aj = 0, bj = 0;
                while (aj + bj < ai + bi) {
                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
                        // A[j] = A.values[aj]
                        // B[j] = 0
                        cordance += signum(-A.values[aj]) * signum(B.values[bi]);
                        ++aj;
                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
                        // A[j] = 0
                        // B[j] = B.values[bj]
                        if (epsilonEquals(B.values[bi], B.values[bj], 0))
                            ++bties;
                        ++bj;
                    } else if (aj < ai && bj < bi) {
                        // A[j] = A.values[aj]
                        // B[j] = B.values[bj]
                        if (epsilonEquals(B.values[bi], B.values[bj], 0))
                            ++bties;
                        else
                            cordance += signum(-A.values[aj])
                                    * signum(B.values[bi] - B.values[bj]);
                        ++aj;
                        ++bj;
                    } else {
                        throw new AssertionError();
                    }
                }
                ++bi;
            } else if (ai < L && bi < M) {
                // A[i] = A.values[ai]
                // B[i] = B.values[bi]
                int aj = 0, bj = 0;
                while (aj + bj < ai + bi) {
                    if (aj < ai && (bj == bi || A.keys[aj] < B.keys[bj])) {
                        // A[j] = A.values[aj]
                        // B[j] = 0
                        if (epsilonEquals(A.values[ai], A.values[aj], 0))
                            ++aties;
                        else
                            cordance += signum(A.values[ai] - A.values[aj])
                                    * signum(B.values[bi]);
                        ++aj;
                    } else if (bj < bi && (aj == ai || B.keys[bj] < A.keys[aj])) {
                        // A[j] = 0
                        // B[j] = B.values[bj]
                        if (epsilonEquals(B.values[bi], B.values[bj], 0))
                            ++bties;
                        else
                            cordance += signum(A.values[ai])
                                    * signum(B.values[bi] - B.values[bj]);
                        ++bj;
                    } else if (aj < ai && bj < bi) {
                        // A[j] = A.values[aj]
                        // B[j] = B.values[bj]
                        if (epsilonEquals(A.values[ai], A.values[aj], 0)) {
                            ++aties;
                            if (epsilonEquals(B.values[bi], B.values[bj], 0))
                                ++bties;
                        } else if (epsilonEquals(B.values[bi], B.values[bj], 0)) {
                            ++bties;
                        } else {
                            cordance += signum(A.values[ai] - A.values[aj])
                                    * signum(B.values[bi] - B.values[bj]);
                        }
                        ++aj;
                        ++bj;
                    } else {
                        throw new AssertionError();
                    }
                }
                ++intersectionSize;
                ++ai;
                ++bi;
            } else {
                throw new AssertionError();
            }
        }

        final int unionSize = (L + M) - intersectionSize;


        // At this point the numbers can overflow rather easily so move 
        // everything to double precision
        double d_cordance = cordance;
        double d_aties = aties;
        double d_bties = bties;

        // Features that don't occur in either vector are a similarity
        // between the two sets. For each feature that they both have there
        // should be an addition +2 to the sum.
        // The relationship between these and disjoint features
        d_cordance += ((N - unionSize) * (double) intersectionSize);

        // Outside of those in the union all elements are zero add all pairwise
        // combinations to the ties counters.
        d_aties += ((N - unionSize) * (double) (N - unionSize - 1)) / 2.0;
        d_bties += ((N - unionSize) * (double) (N - unionSize - 1)) / 2.0;

        // We also need to the add the cross-tries between zeros in union, and
        // everything else
        d_aties += ((unionSize - L) * (double) (N - unionSize));
        d_bties += ((unionSize - M) * (double) (N - unionSize));

        // Within the union minus the size of vector, all elements are zero so
        // add all pairwise combinations
        d_aties += ((unionSize - L) * (double) (unionSize - L - 1)) / 2.0;
        d_bties += ((unionSize - M) * (double) (unionSize - M - 1)) / 2.0;

        final double n0 = ((double) N * (N - 1)) / 2.0;

        final double denom = Math.sqrt((n0 - d_aties) * (n0 - d_bties));

        // Avoid divide-by-0 errors for uniform vectors
        final double sim = !epsilonEquals(d_cordance, 0)
                ? d_cordance / denom
                : 0;

        if (LOG.isInfoEnabled())
            LOG.trace(MessageFormat.format(
                    "n0={0}, ti={1}, tj={2}, conc={3}, denom={4}, sim={5}",
                    n0, d_aties, d_bties, d_cordance, denom, sim));

        return sim;
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public double getHomogeneityBound() {
        return +1.0;
    }

    /**
     * The rank correlation score indicating approximate independence between
     * the vectors. Vectors generated from a uniform random distribution are
     * likely to have a score close to this value.
     * <p/>
     * Considering pulling this up to the {@link Measure } interface.
     * <p/>
     *
     * @return score indicating no positive or negative correlation has been
     *         found.
     */
    public double getIndependenceBound() {
        return 0.0;
    }

    @Override
    public double getHeterogeneityBound() {
        return -1.0;
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return NullWeighting.class;
    }

    @Override
    public String toString() {
        return "Kendalls-Tau{minCardinality=" + minCardinality + '}';
    }

    public boolean equals(final KendallsTau other) {
        return this.minCardinality == other.minCardinality;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((KendallsTau) obj);
    }

    @Override
    public int hashCode() {
        return 79 * 5 + this.minCardinality;
    }
}

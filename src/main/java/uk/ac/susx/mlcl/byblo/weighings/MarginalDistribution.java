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

import com.google.common.base.Preconditions;
import uk.ac.susx.mlcl.byblo.measures.Measures;
import uk.ac.susx.mlcl.lib.collect.ArrayMath;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
public final class MarginalDistribution {

    /**
     * A mapping from event marginal id to frequency, stored as a double because
     * it may have been waited in a pre-processing stage.
     */
    private final double[] frequencies;

    /**
     * The total number of events, not just the number of unique event
     * marginals. This should be equal to the sum of all values in
     * {@link #frequencies}.
     */
    @Nonnegative
    private final double frequencySum;

    /**
     * The number of event marginals that are actually occurring more than once.
     * Due to pre-processing an event marginal may have been previously
     * filtered, resulting in this value being less than the cardinality.
     */
    @Nonnegative
    private final int nonZeroCardinality;

    /**
     * Full dependency injection constructor.
     * <p/>
     *
     * @param frequencies
     * @param frequencySum
     * @param nonZeroCardinality
     */
    public MarginalDistribution(
            double[] frequencies,
            @Nonnegative double frequencySum,
            @Nonnegative int nonZeroCardinality) {
        Preconditions.checkNotNull(frequencies, "frequencies");
        if (frequencySum < 0)
            throw new IllegalArgumentException("frequencySum is negative");
        if (nonZeroCardinality < 0)
            throw new IllegalArgumentException("nonZeroCardinality is negative");
        this.frequencies = Arrays.copyOf(frequencies, frequencies.length);
        this.frequencySum = frequencySum;
        this.nonZeroCardinality = nonZeroCardinality;
    }

    /**
     * Constructor that automatically calculates the sum and non-zero
     * cardinality from the provided frequency array.
     * <p/>
     *
     * @param frequencies
     */
    public MarginalDistribution(final double[] frequencies) {
        this(frequencies,
                ArrayMath.sum(frequencies),
                nonZeroCardinality(frequencies));
    }

    /**
     * Count the number of elements in the given array which are not zero.
     * <p/>
     * TODO: Move to mlcl-lib
     * <p/>
     *
     * @param frequencies
     * @return
     */
    @Nonnegative
    @CheckReturnValue
    private static int nonZeroCardinality(final double[] frequencies) {
        int nonZeroCardinality = 0;
        for (double f : frequencies)
            if (!Measures.epsilonEquals(f, 0, 0))
                ++nonZeroCardinality;
        return nonZeroCardinality;
    }

    /**
     * Get an array containing a mapping from id to frequency.
     * <p/>
     * Stored as a double because it may have been waited in a pre-processing
     * stage, and as an array because it's usually dense.
     * <p/>
     * Warning: This method copies the array (which may be very large) so care
     * should taken to either store the result, or use other accessors such as
     * {@link #getFrequency(int) }
     *
     * @return marginal scores for each event marginal
     */
    @CheckReturnValue
    public double[] getFrequencies() {
        return Arrays.copyOf(frequencies, frequencies.length);
    }

    /**
     * Return the frequency of a particular event marginal, denoted by the index
     * <code>i</code>.
     * <p/>
     *
     * @param index The event marginal id (dimension) to access
     * @return The frequency of that event marginal over the whole join
     *         distribution.
     */
    @CheckReturnValue
    @Nonnegative
    public double getFrequency(@Nonnegative int index) {
        return frequencies[index];
    }

    /**
     * @return
     */
    @CheckReturnValue
    @Nonnegative
    public double getFrequencySum() {
        return frequencySum;
    }

    /**
     * Get the number of marginal events that are actually occurring at least
     * once. Due to pre-processing a entry may have been previously filtered,
     * resulting in this value being less than than {@link #
     *
     * @return number of actually occurring feature types
     */
    @CheckReturnValue
    @Nonnegative
    public int getNonZeroCardinality() {
        return nonZeroCardinality;
    }

    /**
     * The number of unique features.
     * <p/>
     *
     * @return number of unique featuress.
     */
    @CheckReturnValue
    @Nonnegative
    public final int getCardinality() {
        return frequencies.length;
    }

    /**
     * Return the independent probability of a particular event marginal,
     * calculated as the frequency of the marginal divided by the sum of all
     * frequencies in the distribution.
     * <p/>
     *
     * @param index event marginal id (dimension)
     * @return independent prior probability of that event marginal occurring.
     */
    @CheckReturnValue
    @Nonnegative
    public final double getPrior(@Nonnegative final int index) {
        return frequencySum == 0 ? 0
                : frequencies[index] / frequencySum;
    }

    boolean equals(MarginalDistribution other) {
        if (!Arrays.equals(this.frequencies, other.frequencies))
            return false;
        if (Double.doubleToLongBits(this.frequencySum) != Double.
                doubleToLongBits(other.frequencySum))
            return false;
        if (this.nonZeroCardinality != other.nonZeroCardinality)
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((MarginalDistribution) obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Arrays.hashCode(this.frequencies);
        final long ftBits = Double.doubleToLongBits(this.frequencySum);
        hash = 23 * hash + (int) (ftBits ^ (ftBits >>> 32));
        hash = 23 * hash + this.nonZeroCardinality;
        return hash;
    }

    @Override
    public String toString() {
        return "MarginalDistribution{"
                + "frequencySum=" + frequencySum
                + ", nonZeroCardinality=" + nonZeroCardinality
                + '}';
    }
}

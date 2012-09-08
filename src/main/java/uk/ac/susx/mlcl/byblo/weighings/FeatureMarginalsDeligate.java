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

import java.util.Arrays;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FeatureMarginalsDeligate implements FeatureMarginalsCarrier {

    /**
     * A mapping from feature id to frequency, stored as a double because it may
     * have been waited in a pre-processing stage.
     */
    double[] featureMarginals;

    /**
     * The total number of features, not just the number of unique features.
     * This should be equal to the sum of all values in
     * {@link #featureMarginals}.
     */
    double featureTotal;

    /**
     * The number of features that are actually occurring more than once. Due to
     * pre-processing a feature may have been previously filtered, resulting in
     * this value being less than the cardinality. This could be described as
     * the non-zero cardinality.
     */
    long featureCardinality;

    @Override
    public final double[] getFeatureMarginals() {
        return featureMarginals;
    }

    @Override
    public final void setFeatureMarginals(double[] featureFrequencies) {
        if (featureFrequencies == null)
            throw new NullPointerException("featureFrequencies == null");
        this.featureMarginals = featureFrequencies;
    }

    /**
     * The total number of features, not just the number of unique features.
     *
     * @return sum of all feature frequencies.
     */
    @Override
    public final double getGrandTotal() {
        return featureTotal;
    }

    @Override
    public final void setGrandTotal(double featureFrequencySum) {
        if (Double.isNaN(featureFrequencySum))
            throw new IllegalArgumentException("featureFrequencySum is NaN");
        if (Double.isInfinite(featureFrequencySum))
            throw new IllegalArgumentException("featureFrequencySum is infinite");
        this.featureTotal = featureFrequencySum;
    }

    @Override
    public final long getFeatureCardinality() {
        return featureCardinality;
    }

    @Override
    public final void setFeatureCardinality(long occuringFeatureCount) {
        this.featureCardinality = occuringFeatureCount;
    }

    /**
     * The number of unique features.
     *
     * @return number of unique featuress.
     */
    public final int getFeatureCount() {
        return featureMarginals.length;
    }

    /**
     * Return the frequency of a particular feature, denoted by the index k.
     *
     * @param key The feature id (or dimension) to access
     * @return The frequency of that feature over the whole corpus.
     */
    public final double getFeatureMarginals(final int key) {
        return featureMarginals[key];
    }

    /**
     * Return the independent probability of a particular feature, calculated as
     * the frequency of the feature divided by the total number of features
     * observed over the corpus.
     *
     * @param key the feature id (or dimension)
     * @return independent probability of that feature occurring.
     */
    public final double getFeaturePrior(final int key) {
        return getFeatureMarginals(key) / featureTotal;
    }

    protected boolean equals(FeatureMarginalsDeligate other) {
        if (!Arrays.equals(this.featureMarginals, other.featureMarginals))
            return false;
        if (Double.doubleToLongBits(this.featureTotal) != Double.
                doubleToLongBits(other.featureTotal))
            return false;
        if (this.featureCardinality != other.featureCardinality)
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((FeatureMarginalsDeligate) obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Arrays.hashCode(this.featureMarginals);
        final long ftBits = Double.doubleToLongBits(this.featureTotal);
        hash = 23 * hash + (int) (ftBits ^ (ftBits >>> 32));
        hash = 23 * hash + (int) (this.featureCardinality ^ (this.featureCardinality >>> 32));
        return hash;
    }
}

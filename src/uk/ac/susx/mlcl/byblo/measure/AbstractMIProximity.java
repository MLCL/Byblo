/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.byblo.measure;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * AbstractMIProximity is a class that mutual information (MI) based proximity
 * measures can extend. Unlike more tradition measures (such as Jaccard) these
 * require global feature statistical information, which is held in
 * the class fields.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public abstract class AbstractMIProximity extends AbstractProximity {

    /**
     * A mapping from feature id to frequency, stored as a double because it
     * may have been waited in a pre-processing stage.
     */
    private double[] featureFrequencies;

    /**
     * The total number of features, not just the number of unique features.
     * This should be equal to the sum of all values in
     * {@link #featureFrequencies}.
     */
    private double featureFrequencySum;

    /**
     * The number of features that are actually occurring more than once.
     * Due to pre-processing a feature may have been previously filtered,
     * resulting in this value being less than the cardinality. This could
     * be described as the non-zero cardinality.
     */
    private long occuringFeatureCount;

    /**
     * 
     */
    public AbstractMIProximity() {
    }

    public void setFeatureFrequencies(double[] featureFrequencies) {
        if (featureFrequencies == null)
            throw new NullPointerException("featureFrequencies == null");
        this.featureFrequencies = featureFrequencies;
    }

    public void setFeatureFrequencySum(double featureFrequencySum) {
        if (Double.isNaN(featureFrequencySum))
            throw new IllegalArgumentException("featureFrequencySum is NaN");
        if (Double.isInfinite(featureFrequencySum))
            throw new IllegalArgumentException("featureFrequencySum is infinite");
        this.featureFrequencySum = featureFrequencySum;
    }

    public void setOccuringFeatureCount(int occuringFeatureCount) {
        this.occuringFeatureCount = occuringFeatureCount;
    }

    /**
     * The total number of features, not just the number of unique features.
     * 
     * @return sum of all feature frequencies.
     */
    public final double getFeatureFrequencySum() {
        return featureFrequencySum;
    }

    /**
     * The number of unique features.
     * @return number of unique featuress.
     */
    public final int getFeatureCount() {
        return featureFrequencies.length;
    }

    public long getOccuringFeatureCount() {
        return occuringFeatureCount;
    }

    /**
     * Return the frequency of a particular feature, denoted by the index k.
     *
     * @param k The feature id (or dimension) to access
     * @return The frequency of that feature over the whole corpus.
     */
    protected final double featureFreq(final int k) {
        return featureFrequencies[k];
    }

    /**
     * Return the independent probability of a particular feature, calculated
     * as the frequency of the feature divided by the total number of features
     * observed over the corpus.
     *
     * @param k the  feature id (or dimension)
     * @return independent probability of that feature occurring.
     */
    protected final double featurePrior(final int k) {
        return featureFreq(k) / featureFrequencySum;
    }

    /**
     * <p>Calculate the information content of the vector V at dimension i
     * with respect to the feature information held by this class.</p>
     *
     * <p>If only the positive information is required then use
     * posInf(null, featureCardinality) because it's faster.</p>
     *
     * @param V vector
     * @param i dimension (aka feature id) to calculate
     * @return information content of V at i
     */
    protected final double inf(final SparseDoubleVector V, final int i) {
        return Math.log((V.values[i] / V.sum) / featurePrior(V.keys[i]));
    }

    /**
     * Calculate the positive information given by the vector V at dimension i
     * with respect to the feature information held by this class. If the
     * information content is negative then 0 is returned.
     *
     * @param V vector
     * @param i dimension (aka feature id) to calculate
     * @return information content of V at i if positive, otherwise 0
     */
    protected final double posInf(final SparseDoubleVector V, final int i) {
        final double tmp = (V.values[i] / V.sum) / featurePrior(V.keys[i]);
        return tmp > 1 ? Math.log(tmp) : 0;
    }

    public boolean hasPosInf(final SparseDoubleVector V, final int i) {
        return prob(V, i) > featurePrior(V.keys[i]);
    }

    // Calculate if the features would both have positive
    // information content w.r.t the feature data.
    public boolean hasPosInf(final SparseDoubleVector A, final int i,
                             final SparseDoubleVector B, final int j) {
        final double pC = featurePrior(A.keys[i]);
        return prob(A, i) > pC && prob(B, j) > pC;
    }

    protected final double prob(final SparseDoubleVector V, final int k) {
        return V.values[k] / V.sum;
    }
}

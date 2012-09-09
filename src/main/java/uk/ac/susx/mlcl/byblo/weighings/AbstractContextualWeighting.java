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

/**
 * {@link AbstractContextualWeighting} is an abstract super class that combines
 * an {@link ElementwiseWeighting} scheme with the availability of feature
 * marginal scores via {@link FeatureMarginalsCarrier}.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractContextualWeighting
        extends AbstractElementwiseWeighting
        implements FeatureMarginalsCarrier {

    private final FeatureMarginalsDelegate featureMarginalsDelegate =
            new FeatureMarginalsDelegate();

    public AbstractContextualWeighting() {
    }

    @Override
    public final void setGrandTotal(double featureFrequencySum) {
        featureMarginalsDelegate.setGrandTotal(featureFrequencySum);
    }

    @Override
    public final void setFeatureMarginals(double[] featureFrequencies) {
        featureMarginalsDelegate.setFeatureMarginals(featureFrequencies);
    }

    @Override
    public final void setFeatureCardinality(long occuringFeatureCount) {
        featureMarginalsDelegate.setFeatureCardinality(occuringFeatureCount);
    }

    @Override
    public final double getGrandTotal() {
        return featureMarginalsDelegate.getGrandTotal();
    }

    @Override
    public final double[] getFeatureMarginals() {
        return featureMarginalsDelegate.getFeatureMarginals();
    }

    @Override
    public final long getFeatureCardinality() {
        return featureMarginalsDelegate.getFeatureCardinality();
    }

    public final int getFeatureCount() {
        return featureMarginalsDelegate.getFeatureCount();
    }

    protected final double getFeaturePrior(int key) {
        return featureMarginalsDelegate.getFeaturePrior(key);
    }

    protected final double getFeatureMarginal(int key) {
        return featureMarginalsDelegate.getFeatureMarginals(key);
    }

    protected boolean equals(AbstractContextualWeighting other) {
        if (this.featureMarginalsDelegate != other.featureMarginalsDelegate
                && (this.featureMarginalsDelegate == null
                || !this.featureMarginalsDelegate.equals(
                other.featureMarginalsDelegate)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((AbstractContextualWeighting) obj);
    }

    @Override
    public int hashCode() {
        return 89 * 7 + (this.featureMarginalsDelegate != null
                ? this.featureMarginalsDelegate.hashCode() : 0);
    }
}

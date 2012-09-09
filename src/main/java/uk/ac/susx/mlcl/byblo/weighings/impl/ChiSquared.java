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
package uk.ac.susx.mlcl.byblo.weighings.impl;

import uk.ac.susx.mlcl.byblo.weighings.AbstractContextualWeighting;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Serializable;

/**
 * {@link Weighting} that ranks features using the Chi-Squared hypothesis test.
 * Our null hypothesis (H0) is that the feature and entry are co-occurring by
 * chance, i.e that the probability of their joint distribution is approximately
 * equal to the product of their priors.
 * <p/>
 * This method is proposed in James Curran's PhD thesis "From Distributional to
 * Semantic Similarity." (2004) although the description contains errors so it's
 * a little hard to know exactly what was done here, especially since there are
 * numerous Chi-squared distribution based hypothesis tests.
 * <p/>
 * Described by Christopher D. Manningand and Hinrich Schutze (1999)
 * "Foundations of Statistical Natural Language Processing." MIT Press,
 * Cambridge, MA USA.
 * <p/>
 * Seems to be mostly monotonic with {@link SquaredPMI} and {@link GeoMean}
 * weighting schemes, although there are a few exceptions. Not sure if this is
 * down a real difference or just floating point errors.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ChiSquared
        extends AbstractContextualWeighting
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public double apply(SparseDoubleVector vector, int key, double value) {
        final double N = getFeatureMarginals().getFrequencySum();

        // The following are the value in contingency table:

        // weighting for the entry with the feature 
        final double O11 = value;
        // weighting for the entry without the feature 
        final double O12 = vector.sum - value;
        // weighting for all other entries with the feature 
        final double O21 = getFeatureMarginals().getFrequency(key) - value;
        // weighting for all other entries with all other feature 
        final double O22 = N - O11 - O12 - O21;

        return (N * (O11 * O22 - O12 - O21) * (O11 * O22 - O12 - O21))
                / ((O11 + O12) * (O11 + O21) * (O12 + O22) * (O21 + O22));
    }

    @Override
    public double getLowerBound() {
        return 0;
    }

    @Override
    public double getUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return super.equals((AbstractContextualWeighting) this);
    }

    @Override
    public int hashCode() {
        return 7;
    }
}

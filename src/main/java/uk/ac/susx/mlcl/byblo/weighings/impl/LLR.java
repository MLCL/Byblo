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
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import java.io.Serializable;

/**
 * {@link Weighting} that implements that re-weights features using the
 * log-likelihood-ratio hypothesis test. That is the log of ratio between the
 * null hypothesis (H0) and alternate hypothesis (Ha).
 * <p/>
 * H0: That the entry and feature occur together by change, measured as the
 * probability of the feature conditioned on all other entries.
 * <p/>
 * Ha: That the entry and feature are positively correlated, measured as the
 * conditional probability of the feature in the context of the entry.
 * <p/>
 * Described by Christopher D. Manningand and Hinrich Schutze (1999)
 * "Foundations of Statistical Natural Language Processing." MIT Press,
 * Cambridge, MA USA.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class LLR
        extends AbstractContextualWeighting
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public double apply(final SparseDoubleVector vector, int key, double value) {
        final double Ha = Weightings.log2(value / vector.sum);
        final double H0 = Weightings.log2((getFeatureMarginals().getFrequency(
                                           key) - value)
                / (getFeatureMarginals().getFrequencySum() - vector.sum));
        return 2 * (Ha - H0);
    }

    @Override
    public double getLowerBound() {
        return Double.NEGATIVE_INFINITY;
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
        return 23;
    }
}

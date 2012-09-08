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

import java.io.Serializable;
import uk.ac.susx.mlcl.byblo.weighings.AbstractContextualWeighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Squared PMI.
 *
 * \[ pmi^2(x,y) = \log( p(x,y)^2 / (p(x)p(y)) ) \]
 *
 * Orientation of the scores is 0, log p(x,y), -infinity; for maximal positive
 * correlation, no correlation, and maximal negative correlation
 *
 *
 * Proposed in: Daille, B.: Approche mixte pour l'extraction automatique de
 * terminologie: statistiquesicales et ltres linguistiques. PhD thesis,
 * Universite Paris 7
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class SquaredPMI
        extends AbstractContextualWeighting
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public double apply(SparseDoubleVector vector, int key, double value) {
        final double logProbability = Weightings.log2(value / getGrandTotal());
        final double logFeaturePrior = Weightings.log2(getFeaturePrior(key));
        final double logEntryPrior = Weightings.log2(
                vector.sum / getGrandTotal());

        return 2 * logProbability - (logEntryPrior + logFeaturePrior);
    }

    @Override
    public double getLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getUpperBound() {
        return 0;
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
        return 61;
    }
}

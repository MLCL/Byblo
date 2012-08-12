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
import uk.ac.susx.mlcl.byblo.measures.v2.Measure;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import static uk.ac.susx.mlcl.byblo.weighings.Weightings.log2;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * {@link Distance} measure that computes similarity as the Jensen-Shannon
 * divergence.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class JensenShannonDivergence implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        double divergence = 0.0;

        int i = 0;
        int j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                final double q = A.values[i] / A.sum;
                divergence += 0.5 * q;
                ++i;
            } else if (A.keys[i] > B.keys[j]) {
                final double r = B.values[j] / B.sum;
                divergence += 0.5 * r;
                ++j;
            } else {
                final double q = A.values[i] / A.sum;
                final double r = B.values[j] / B.sum;
                final double logAvg = log2(0.5 * (q + r));
                divergence += 0.5 * q * (log2(q) - logAvg)
                        + 0.5 * r * (log2(r) - logAvg);
                ++i;
                ++j;
            }
        }
        while (i < A.size) {
            final double q = A.values[i] / A.sum;
            divergence += 0.5 * q;
            i++;
        }
        while (j < B.size) {
            final double r = B.values[j] / B.sum;
            divergence += 0.5 * r;
            j++;
        }

        return divergence;
    }

    @Override
    public boolean isCommutative() {
        return true;
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
    public Weighting getExpectedWeighting() {
        return Weightings.positive();
    }

    @Override
    public String toString() {
        return "KL-Divergence";
    }
}

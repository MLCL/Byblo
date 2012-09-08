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
package uk.ac.susx.mlcl.byblo.measures;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Jensen-Shannon Divergence similarity measure, defined as a balanced average of KL-Divergences from the two entry
 * distributions to the average distribution of both.
 * <p/>
 * <pre>
 *  JSD(Q||R) = 0.5 KLD(Q||M) + 0.5 * KLD(R||M)
 *              where M = 0.5 * (Q + R)
 *        and KLD(X||M) = sum_i P(x_i) * log(P(x_i) / P(m_i))
 * </pre>
 * <p/>
 * Base-2 logarithms are used throughout. This is partly because it fits better with the information theoretic notion
 * of entropy as bits, and partly because it conveniently produces a score in the range 0 to 1.
 * <p/>
 * The output is reversed so it's inline with other proximity measures; so 1 indicates maximal similarity
 * (convergence) and 0 indicates maximal dissimilarity (divergence.
 *
 * @author David Sheldrick &lt;ds300@sussex.ac.uk&gt;
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @deprecated replaced by v2 measures and weightings
 */
public class Jensen extends AbstractProximity {

    @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        double divergence = 0;

        int i = 0, j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                divergence += A.values[i] / A.sum;
                i++;
            } else if (A.keys[i] > B.keys[j]) {
                divergence += B.values[j] / B.sum;
                j++;
            } else if (isFiltered(A.keys[i])) {
                i++;
                j++;
            } else {
                final double pA = A.values[i] / A.sum;
                final double pB = B.values[j] / B.sum;
                final double lpAvg = log2(pA + pB) - 1.;
                divergence += pA * (log2(pA) - lpAvg);
                divergence += pB * (log2(pB) - lpAvg);
                i++;
                j++;
            }
        }

        while (i < A.size) {
            divergence += A.values[i] / A.sum;
            i++;
        }

        while (j < B.size) {
            divergence += B.values[j] / B.sum;
            j++;
        }

        return divergence / 2.;
    }

    @Override
    public double left(SparseDoubleVector A) {
        return 0.;
    }

    @Override
    public double right(SparseDoubleVector B) {
        return 0.;
    }

    @Override
    public double combine(double shared, double left, double right) {
        // Low values indicate similarity so invert the result
        return 1. - shared;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public String toString() {
        return "Jensen{}";
    }

    private static final double LN2 = Math.log(2);

    private static double log2(double value) {
        return Math.log(value) / LN2;
    }

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Jensen-Shannon divergence
 * 
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Jensen extends AbstractProximity {

    private static final Log LOG = LogFactory.getLog(Jensen.class);

    private static final double LN2 = Math.log(2);

    public Jensen() {
        if (LOG.isWarnEnabled())
            LOG.warn("The JensenShannon proximity measure has not been "
                    + "thoughoughly test and is likely to contain bugs.");
    }

   @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        double compA = 0;
        double compB = 0;

        int i = 0, j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                compA += (A.values[i] / A.sum) * LN2;
                i++;
            } else if (A.keys[i] > B.keys[j]) {
                compB += (B.values[j] / B.sum) * LN2;
                j++;
            } else if (isFiltered(A.keys[i])) {
                i++;
                j++;
            } else {
                final double pA = A.values[i] / A.sum;
                final double pB = B.values[j] / B.sum;
                final double lpAvg = Math.log(pA + pB) - LN2;
                compA += pA * (Math.log(pA) - lpAvg);
                compB += pB * (Math.log(pB) - lpAvg);
                i++;
                j++;
            }
        }

        while (i < A.size) {
            compA += (A.values[i] / A.sum) * LN2;
            i++;
        }

        while (j < B.size) {
            compB += (B.values[j] / B.sum) * LN2;
            j++;
        }

        return (compA + compB) / 2;
    }

    @Override
    public double left(SparseDoubleVector A) {
        return 0d;
    }

    @Override
    public double right(SparseDoubleVector B) {
        return 0d;
    }

    @Override
    public double combine(double shared, double left, double right) {
        // Low values indicate similarity so invert the result
        return 1d / shared;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public String toString() {
        return "Jensen{}";
    }
}

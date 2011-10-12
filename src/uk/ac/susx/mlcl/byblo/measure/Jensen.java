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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Jensen-Shannon divergence
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class Jensen implements Proximity {

    private static final Log LOG = LogFactory.getLog(Jensen.class);

    private static final double LN2 = Math.log(2);

    public Jensen() {
        if (LOG.isWarnEnabled())
            LOG.warn("The JensenShannon proximity measure has been "
                    + "thoughoughly test and is likely to contain bugs.");
    }

    @Override
    public double shared(SparseDoubleVector Q, SparseDoubleVector R) {
        double comp = 0;

        int i = 0, j = 0;
        while (i < Q.size && j < R.size) {
            if (Q.keys[i] < R.keys[j]) {
                i++;
            } else if (Q.keys[i] > R.keys[j]) {
                j++;
            } else {
                final double Qprob = Q.values[i] / Q.sum;
                final double Rprob = R.values[j] / R.sum;
                final double avprob = Math.log(Qprob + Rprob) - LN2;
                comp += Qprob * (2 * Math.log(Qprob) - avprob - LN2)
                        + Rprob * (2 * Math.log(Rprob) - avprob - LN2);
                i++;
                j++;
            }
        }

        return comp;
    }

    @Override
    public double left(SparseDoubleVector Q) {
        double comp = 0;
        for (int i = 0; i < Q.size; i++) {
            final double Qprob = Q.values[i] / Q.sum;
            comp += Qprob * (-((Qprob - 1) * Math.log(Qprob) - Qprob * LN2));
        }
        return comp;
    }

    @Override
    public double right(SparseDoubleVector R) {
        return left(R);
    }

    @Override
    public double combine(double shared, double left, double right) {
        // Low values indicate similarity so invert the result
        return 1d / (0.5 * (shared + left + right));
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

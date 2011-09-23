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
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class Hindle extends AbstractMIProximity implements Proximity {

    @Override
    public double shared(SparseDoubleVector Q, SparseDoubleVector R) {
        double sim = 0;

        int i = 0, j = 0;
        while (i < Q.size && j < R.size) {
            if (Q.keys[i] < R.keys[j]) {

                i++;
            } else if (Q.keys[i] > R.keys[j]) {

                j++;
            } else { // Q.keys[i] == R.keys[j]
                final double Cprob = contextProb(Q.keys[i]);
                final double Qprob = prob(Q, i);
                if (Qprob > Cprob) {
                    final double Rprob = prob(R,j);
                    if (Rprob > Cprob) {
                        sim += Math.log(Math.min(Qprob / Cprob, Rprob / Cprob));
                    }
                }
                i++;
                j++;
            }
        }
        return sim;
    }

    @Override
    public double left(SparseDoubleVector Q) {
        return 0;
    }

    @Override
    public double right(SparseDoubleVector R) {
        return 0;
    }

    @Override
    public double combine(double shared, double left, double right) {
        return shared;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public String toString() {
        return "Hindle{}";
    }


}

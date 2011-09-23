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
package uk.ac.susx.mlcl.dttools.measure;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 *
 * sim(q,r) = sum((P(q_i) * P(r_i)) / P(c_i))
 *
 * Expected range: 0 to +inf
 * Measure type: proximity (0 = distinct, +inf = identical)
 *
 * @version 2nd December 2010
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class Confusion extends AbstractMIProximity implements Proximity {

    @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        double total = 0.0;

        int i = 0, j = 0;
        while (i < A.size && j < B.size) {
            if (A.keys[i] < B.keys[j]) {
                i++;
            } else if (A.keys[i] > B.keys[j]) {
                j++;
            } else { // Q.keys[i] == R.keys[j]
                total += (prob(A, i) * prob(B, j))
                        / contextProb(A.keys[i]);
                i++;
                j++;
            }
        }
        return total;
    }

    @Override
    public double left(SparseDoubleVector A) {
        return 0;
    }

    @Override
    public double right(SparseDoubleVector B) {
        return 0;
    }

    @Override
    public double combine(double shared, double left, double right) {
        return shared;
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }

    @Override
    public String toString() {
        return "Confusion{}";
    }
}
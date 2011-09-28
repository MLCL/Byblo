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
import java.util.logging.Logger;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class Lp implements Proximity {

    private static final Logger log = Logger.getLogger(Lp.class.getName());

    public static final double DEFAULT_P = 1;

    private double p;

    public Lp() {
        this.p = DEFAULT_P;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    @Override
    public double shared(SparseDoubleVector Q, SparseDoubleVector R) {
        double shared = 0;

        if (p == 0) {
            throw new UnsupportedOperationException("L_0 isn't implemented yet.");
        } else if (p == 1) {
            int i = 0, j = 0;
            while (i < Q.size && j < R.size) {
                if (Q.keys[i] < R.keys[j]) {
                    i++;
                } else if (Q.keys[i] > R.keys[j]) {
                    j++;
                } else { // Q.keys[i] == R.keys[j]
                    final double Qprob = Q.values[i] / Q.sum;
                    final double Rprob = R.values[j] / R.sum;
                    shared += Math.abs(Qprob - Rprob)
                            - Rprob
                            - Qprob;
                    i++;
                    j++;
                }
            }
        } else if (p == 2) {
            int i = 0, j = 0;
            while (i < Q.size && j < R.size) {
                if (Q.keys[i] < R.keys[j]) {
                    i++;
                } else if (Q.keys[i] > R.keys[j]) {
                    j++;
                } else {
                    final double Qprob = Q.values[i] / Q.sum;
                    final double Rprob = R.values[j] / R.sum;
                    shared += (Qprob - Rprob) * (Qprob - Rprob)
                            - Rprob * Rprob
                            - Qprob * Qprob;
                    i++;
                    j++;
                }
            }
        } else if (p == Double.POSITIVE_INFINITY) {
            int i = 0, j = 0;
            while (i < Q.size && j < R.size) {
                if (Q.keys[i] < R.keys[j]) {
                    shared = Math.max(shared, (Q.values[i] / Q.sum));
                    i++;
                } else if (Q.keys[i] > R.keys[j]) {
                    shared = Math.max(shared, (R.values[j] / R.sum));
                    j++;
                } else {
                    shared = Math.max(shared, Math.abs(
                            (Q.values[i] / Q.sum) - (R.values[j] / R.sum)));
                    i++;
                    j++;
                }
            }
            while (i < Q.size) {
                shared = Math.max(shared, (Q.values[i] / Q.sum));
                i++;
            }
            while (j < R.size) {
                shared = Math.max(shared, (R.values[j] / R.sum));
                j++;
            }
        } else {
            int i = 0, j = 0;
            while (i < Q.size && j < R.size) {
                if (Q.keys[i] < R.keys[j]) {
                    i++;
                } else if (Q.keys[i] > R.keys[j]) {
                    j++;
                } else {
                    final double Qprob = Q.values[i] / Q.sum;
                    final double Rprob = R.values[j] / R.sum;
                    shared += Math.pow(Math.abs(Qprob - Rprob), p)
                            - (Math.pow(Rprob, p) + Math.pow(Qprob, p));
                    i++;
                    j++;
                }
            }
        }
        return shared;
    }

    @Override
    public double left(SparseDoubleVector Q) {
        if (p == 0) {
            throw new UnsupportedOperationException("L_0 isn't implemented yet.");
        } else if (p == 1) {
            return 1;
        } else if (p == 2) {
            double left = 0;
            for (int i = 0; i < Q.size; i++) {
                left += (Q.values[i] / Q.sum) * (Q.values[i] / Q.sum);
            }
            return left;
        } else if (p == Double.POSITIVE_INFINITY) {
            return 0;
        } else {
            double left = 0;
            for (int i = 0; i < Q.size; i++) {
                left += Math.pow(Q.values[i] / Q.sum, p);
            }
            return left;
        }
    }

    @Override
    public double right(SparseDoubleVector R) {
        return left(R);
    }

    @Override
    public double combine(double shared, double left, double right) {
        // Low values indicate similarity results are inverted
        if (p == 0) {
            throw new UnsupportedOperationException("L_0 isn't implemented yet.");
        } else if (p == 1) {
            return 1d / (shared + left + right);
        } else if (p == 2) {
            return 1d / Math.sqrt(shared + left + right);
        } else if (p == Double.POSITIVE_INFINITY) {
            return 1d / shared;
        } else {
            return 1d / Math.pow(shared + left + right, 1 / p);
        }
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public String toString() {
        return "Lp{" + "p=" + p + '}';
    }
}

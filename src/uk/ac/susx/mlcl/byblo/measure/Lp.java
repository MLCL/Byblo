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
package uk.ac.susx.mlcl.byblo.measure;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * 
 * <h4>Notes</h4>
 * <ul>
 * <li>All results are inverted to produce proximities rather than distances. 
 * I.e Values are between 0 and 1 (inclusive) where 0 indicates infinite 
 * distance, and 1 indicates no distance at all.</li>
 * <li>L0 is defined as the L0 "norm" (with quotes) by David Donoho. This is
 * effectively the non-zero cardinality of the absolute difference between 
 * vectors.</li>
 * </ul>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class Lp extends AbstractProximity {

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
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        double shared = 0;

        if (p == 0) {
            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    j++;
                } else if (isFiltered(A.keys[i])) {
                    i++;
                    j++;
                } else { // Q.keys[i] == R.keys[j]
                    final double pA = A.values[i] / A.sum;
                    final double pB = B.values[j] / B.sum;
                    shared += Math.signum(Math.abs(pA - pB))
                            - Math.signum(pB)
                            - Math.signum(pA);
                    i++;
                    j++;
                }
            }
        } else if (p == 1) {
            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    j++;
                } else if (isFiltered(A.keys[i])) {
                    i++;
                    j++;
                } else { // Q.keys[i] == R.keys[j]
                    final double pA = A.values[i] / A.sum;
                    final double pB = B.values[j] / B.sum;
                    shared += Math.abs(pA - pB)
                            - pB
                            - pA;
                    i++;
                    j++;
                }
            }
        } else if (p == 2) {
            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    j++;
                } else if (isFiltered(A.keys[i])) {
                    i++;
                    j++;
                } else {
                    final double pA = A.values[i] / A.sum;
                    final double pB = B.values[j] / B.sum;
                    shared += (pA - pB) * (pA - pB)
                            - pB * pB
                            - pA * pA;
                    i++;
                    j++;
                }
            }
        } else if (p == Double.POSITIVE_INFINITY) {
            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    shared = Math.max(shared, (A.values[i] / A.sum));
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    shared = Math.max(shared, (B.values[j] / B.sum));
                    j++;
                } else if (isFiltered(A.keys[i])) {
                    shared = Math.max(shared, (A.values[i] / A.sum));
                    shared = Math.max(shared, (B.values[j] / B.sum));
                    i++;
                    j++;
                } else {
                    shared = Math.max(shared, Math.abs(
                            (A.values[i] / A.sum) - (B.values[j] / B.sum)));
                    i++;
                    j++;
                }
            }
            while (i < A.size) {
                shared = Math.max(shared, (A.values[i] / A.sum));
                i++;
            }
            while (j < B.size) {
                shared = Math.max(shared, (B.values[j] / B.sum));
                j++;
            }
        } else {
            int i = 0, j = 0;
            while (i < A.size && j < B.size) {
                if (A.keys[i] < B.keys[j]) {
                    i++;
                } else if (A.keys[i] > B.keys[j]) {
                    j++;
                } else if (isFiltered(A.keys[i])) {
                    i++;
                    j++;
                } else {
                    final double pA = A.values[i] / A.sum;
                    final double pB = B.values[j] / B.sum;
                    shared += Math.pow(Math.abs(pA - pB), p)
                            - (Math.pow(pB, p) + Math.pow(pA, p));
                    i++;
                    j++;
                }
            }
        }
        return shared;
    }

    @Override
    public double left(SparseDoubleVector A) {
        if (p == 0) {
            return A.size;
        } else if (p == 1) {
            return 1;
        } else if (p == 2) {
            double left = 0;
            for (int i = 0; i < A.size; i++) {
                left += (A.values[i] / A.sum) * (A.values[i] / A.sum);
            }
            return left;
        } else if (p == Double.POSITIVE_INFINITY) {
            return 0;
        } else {
            double left = 0;
            for (int i = 0; i < A.size; i++) {
                left += Math.pow(A.values[i] / A.sum, p);
            }
            return left;
        }
    }

    @Override
    public double right(SparseDoubleVector B) {
        return left(B);
    }

    @Override
    public double combine(double shared, double left, double right) {
        // results are inverted: High values indicate similarity 
        if (p == 0) {
            return 1d / (shared + left + right);
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

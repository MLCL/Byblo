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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * tau(q,r) = sum_i_j( sign((qi - qj)(ri - rj)) / (2 * |V|)  )
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
public class KendallTau implements Proximity {

    private static final Log log = LogFactory.getLog(KendallTau.class);

    private int numFeatures;

    public KendallTau() {
        this.numFeatures = 0;
        log.warn("The KendallTau proximity measure is extremely inefficient "
                + "(quadratic on the number of features). Consider using a "
                + "different measure.");
    }

    public final int getNumFeatures() {
        return numFeatures;
    }

    public final void setNumFeatures(int numFeatures) {
        if (numFeatures <= 0)
            throw new IllegalArgumentException("exepcting numFeatures to be > "
                    + "0, but found " + numFeatures);
        this.numFeatures = numFeatures;
    }

    private void checkState() {
        if (numFeatures <= 0)
            throw new IllegalStateException("exepcting numFeatures to be > 0, "
                    + "but found " + numFeatures);
    }

    @Override
    public double shared(SparseDoubleVector Q, SparseDoubleVector R) {
        checkState();

        int sum = 0;
        int intersectionSize = 0;
        int unionSize = 0;
        int qi = 0;
        int ri = 0;

        while (qi < Q.size && ri < R.size) {
            ++unionSize;
            if (Q.keys[qi] < R.keys[ri]) {
                int qj = qi + 1;
                int rj = ri;
                while (qj < Q.size && rj < R.size) {
                    if (Q.keys[qj] < R.keys[rj]) {
                        ++qj;
                    } else if (Q.keys[qj] > R.keys[rj]) {
                        --sum;
                        ++rj;
                    } else {
                        if (Q.values[qi] < Q.values[qj])
                            ++sum;
                        else if (Q.values[qi] > Q.values[qj])
                            --sum;
                        ++qj;
                        ++rj;
                    }
                }
                sum -= R.size - rj;
                ++qi;
            } else if (Q.keys[qi] > R.keys[ri]) {
                int qj = qi;
                int rj = ri + 1;
                while (qj < Q.size && rj < R.size) {
                    if (Q.keys[qj] < R.keys[rj]) {
                        --sum;
                        ++qj;
                    } else if (Q.keys[qj] > R.keys[rj]) {
                        ++rj;
                    } else {
                        if (R.values[ri] < R.values[rj])
                            ++sum;
                        else if (R.values[ri] > R.values[rj])
                            --sum;
                        ++qj;
                        ++rj;
                    }
                }
                sum -= Q.size - qj;
                ++ri;
            } else {
                ++intersectionSize;
                int qj = qi + 1;
                int rj = ri + 1;
                while (qj < Q.size && rj < R.size) {
                    if (Q.keys[qj] < R.keys[rj]) {
                        if (Q.values[qi] < Q.values[qj])
                            --sum;
                        else if (Q.values[qi] > Q.values[qj])
                            ++sum;
                        ++qj;
                    } else if (Q.keys[qj] > R.keys[rj]) {
                        if (R.values[ri] < R.values[rj])
                            --sum;
                        else if (R.values[ri] > R.values[rj])
                            ++sum;
                        ++rj;
                    } else {
                        final double diff = (Q.values[qi] - Q.values[qj])
                                * (R.values[ri] - R.values[rj]);
                        if (diff < 0)
                            --sum;
                        else if (diff > 0)
                            ++sum;
                        ++qj;
                        ++rj;
                    }
                }
                while (qj < Q.size) {
                    if (Q.values[qi] < Q.values[qj])
                        --sum;
                    else if (Q.values[qi] > Q.values[qj])
                        ++sum;
                    ++qj;
                }
                while (rj < R.size) {
                    if (R.values[ri] < R.values[rj])
                        --sum;
                    else if (R.values[ri] > R.values[rj])
                        ++sum;
                    ++rj;
                }
                ++qi;
                ++ri;
            }
        }
        while (qi < Q.size) {
            ++unionSize;
            int qj = qi + 1;
            int rj = ri;
            while (qj < Q.size && rj < R.size) {
                if (Q.keys[qj] < R.keys[rj]) {
                    ++qj;
                } else if (Q.keys[qj] > R.keys[rj]) {
                    --sum;
                    ++rj;
                } else {
                    if (Q.values[qi] < Q.values[qj])
                        ++sum;
                    else if (Q.values[qi] > Q.values[qj])
                        --sum;
                    ++qj;
                    ++rj;
                }
            }
            sum -= R.size - rj;
            ++qi;
        }
        while (ri < R.size) {
            ++unionSize;
            int qj = qi;
            int rj = ri + 1;
            while (qj < Q.size && rj < R.size) {
                if (Q.keys[qj] < R.keys[rj]) {
                    --sum;
                    ++qj;
                } else if (Q.keys[qj] > R.keys[rj]) {
                    ++rj;
                } else {
                    if (R.values[ri] < R.values[rj])
                        ++sum;
                    else if (R.values[ri] > R.values[rj])
                        --sum;
                    ++qj;
                    ++rj;
                }
            }
            sum -= Q.size - qj;
            ++ri;
        }

        // Comparisons are only done in one direction so double the result
        sum <<= 1;

        // Features that don't occur in either vector are a similarity
        // between the two sets. For each feature that they both have there
        // should be an addition +2 to the sum.
        // The relationship between these and disjoint features
        sum += 2 * ((numFeatures - unionSize) * intersectionSize);
        //
        double sim = (double) (sum) / (double) (numFeatures * (numFeatures - 1));

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
        return "KendallTau{}";
    }
}

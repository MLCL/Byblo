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
 * tau(q,r) = sum_i_j( sign((qi - qj)(ri - rj)) / (2 * |V|)  )
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class KendallTau extends AbstractProximity {

    private static final Log LOG = LogFactory.getLog(KendallTau.class);

    private int numFeatures;

    public KendallTau() {
        this.numFeatures = 0;
        if (LOG.isWarnEnabled())
            LOG.warn("The KendallTau proximity measure is extremely inefficient "
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
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        checkState();

        int sum = 0;
        int intersectionSize = 0;
        int unionSize = 0;
        int ai = 0;
        int bi = 0;

        while (ai < A.size && bi < B.size) {
            ++unionSize;
            if (A.keys[ai] < B.keys[bi]) {
                int aj = ai + 1;
                int bj = bi;
                while (aj < A.size && bj < B.size) {
                    if (A.keys[aj] < B.keys[bj]) {
                        ++aj;
                    } else if (A.keys[aj] > B.keys[bj]) {
                        --sum;
                        ++bj;
                    } else {
                        if (A.values[ai] < A.values[aj])
                            ++sum;
                        else if (A.values[ai] > A.values[aj])
                            --sum;
                        ++aj;
                        ++bj;
                    }
                }
                sum -= B.size - bj;
                ++ai;
            } else if (A.keys[ai] > B.keys[bi]) {
                int aj = ai;
                int bj = bi + 1;
                while (aj < A.size && bj < B.size) {
                    if (A.keys[aj] < B.keys[bj]) {
                        --sum;
                        ++aj;
                    } else if (A.keys[aj] > B.keys[bj]) {
                        ++bj;
                    } else {
                        if (B.values[bi] < B.values[bj])
                            ++sum;
                        else if (B.values[bi] > B.values[bj])
                            --sum;
                        ++aj;
                        ++bj;
                    }
                }
                sum -= A.size - aj;
                ++bi;
            } else {
                ++intersectionSize;
                int aj = ai + 1;
                int bj = bi + 1;
                while (aj < A.size && bj < B.size) {
                    if (A.keys[aj] < B.keys[bj]) {
                        if (A.values[ai] < A.values[aj])
                            --sum;
                        else if (A.values[ai] > A.values[aj])
                            ++sum;
                        ++aj;
                    } else if (A.keys[aj] > B.keys[bj]) {
                        if (B.values[bi] < B.values[bj])
                            --sum;
                        else if (B.values[bi] > B.values[bj])
                            ++sum;
                        ++bj;
                    } else {
                        final double diff = (A.values[ai] - A.values[aj])
                                * (B.values[bi] - B.values[bj]);
                        if (diff < 0)
                            --sum;
                        else if (diff > 0)
                            ++sum;
                        ++aj;
                        ++bj;
                    }
                }
                while (aj < A.size) {
                    if (A.values[ai] < A.values[aj])
                        --sum;
                    else if (A.values[ai] > A.values[aj])
                        ++sum;
                    ++aj;
                }
                while (bj < B.size) {
                    if (B.values[bi] < B.values[bj])
                        --sum;
                    else if (B.values[bi] > B.values[bj])
                        ++sum;
                    ++bj;
                }
                ++ai;
                ++bi;
            }
        }
        while (ai < A.size) {
            ++unionSize;
            int aj = ai + 1;
            int bj = bi;
            while (aj < A.size && bj < B.size) {
                if (A.keys[aj] < B.keys[bj]) {
                    ++aj;
                } else if (A.keys[aj] > B.keys[bj]) {
                    --sum;
                    ++bj;
                } else {
                    if (A.values[ai] < A.values[aj])
                        ++sum;
                    else if (A.values[ai] > A.values[aj])
                        --sum;
                    ++aj;
                    ++bj;
                }
            }
            sum -= B.size - bj;
            ++ai;
        }
        while (bi < B.size) {
            ++unionSize;
            int aj = ai;
            int bj = bi + 1;
            while (aj < A.size && bj < B.size) {
                if (A.keys[aj] < B.keys[bj]) {
                    --sum;
                    ++aj;
                } else if (A.keys[aj] > B.keys[bj]) {
                    ++bj;
                } else {
                    if (B.values[bi] < B.values[bj])
                        ++sum;
                    else if (B.values[bi] > B.values[bj])
                        --sum;
                    ++aj;
                    ++bj;
                }
            }
            sum -= A.size - aj;
            ++bi;
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
        return true;
    }

    @Override
    public String toString() {
        return "KendallTau{}";
    }
}

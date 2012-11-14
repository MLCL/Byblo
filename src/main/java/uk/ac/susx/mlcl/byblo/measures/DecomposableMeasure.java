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

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * <code>DecomposableMeasure</code> defines a Measure can be optimally extended
 * to break down internal operation into sub-functions.
 * <p/>
 * This abstract class has been expanded from {@link Measure} to allow for
 * pre-calculation of values that are dependant only on one vector.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
@CheckReturnValue
public abstract class DecomposableMeasure
        implements Measure {

    @Override
    public final double similarity(
            final SparseDoubleVector A,
            final SparseDoubleVector B) {
        return combine(shared(A, B), left(A), right(B));
    }

    /**
     * Calculate the similarity of the given vectors A and B. If possible this
     * should be based entirely upon shared features, while independent features
     * can be pre-calculated using {@link #left(SparseDoubleVector)} and
     * {@link #right(SparseDoubleVector)}. All three values are combined using
     * {@link DecomposableMeasure#combine(double, double, double)}.
     * <p/>
     *
     * @param A the first feature vector
     * @param B the second feature vector
     * @return portion of similarity measure dependent on both A and B
     */
    public abstract double shared(SparseDoubleVector A, SparseDoubleVector B);

    /**
     * Calculate some part of the similarity measure, based entirely on the
     * first feature vectors.
     * <p/>
     *
     * @param A first feature vector
     * @return pre-calculated result
     */
    public abstract double left(SparseDoubleVector A);

    /**
     * Calculate some part of the similarity measure, based entirely on the
     * second feature vectors.
     * <p/>
     *
     * @param B first feature vector
     * @return pre-calculated result
     */
    public abstract double right(SparseDoubleVector B);

    /**
     * Combine the the component results into a single similarity score.
     * <p/>
     *
     * @param shared component derived from both vectors
     * @param left   component derived from first vector
     * @param right  component derived from second vector
     * @return combined result
     */
    public abstract double combine(double shared, double left, double right);
}

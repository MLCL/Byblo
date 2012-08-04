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
package uk.ac.susx.mlcl.byblo.weighings;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Static utility class for various operation pertaining to {@link Weighting},
 * {@link SimpleWeighting}, and {@link ElementwiseWeighting}.
 *
 * TODO: There exist a number of weighting schemes based on feature cardinality,
 * which cannot be implemented because this information is not readily available.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Weightings {

    /**
     * Utility class cannot be instantiated.
     */
    private Weightings() {
    }

    /**
     * A no-op weighting that simply returns the feature vector unaltered.
     */
    public static final Weighting NULL_WEIGHTING = new Weighting() {

        @Override
        public SparseDoubleVector apply(SparseDoubleVector f) {
            return f;
        }

        @Override
        public double getUpperBound() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public double getLowerBound() {
            return Double.NEGATIVE_INFINITY;
        }
    };

    /**
     * Constant to aid conversion to base 2 logarithms.
     *
     * Conceptually it doesn't really matter what base is used, but 2 is the
     * standard base for most information theoretic approaches.
     *
     * TODO: Move to mlcl-lib/MathUtil
     */
    public static final double LOG_2 = Math.log(2.0);

    /**
     * Return the base 2 logarithm of the parameter v.
     *
     * TODO: Move to mlcl-lib/MathUtil
     *
     * @param v some values
     * @return logarithm of the value
     */
    public static double log2(final double v) {
        return Math.log(v) / LOG_2;
    }

    /**
     * The magnitude of the vector, i.e the L2 vector normal.
     *
     * TODO: Move to mlcl-lib/SparseDoubleVector
     *
     * @param vector
     * @return magnitude of vector
     */
    public static double length(SparseDoubleVector vector) {
        return Math.sqrt(lengthSquared(vector));
    }

    /**
     * Calculate the square of the L@ norm of vector.
     *
     * TODO: Move to mlcl-lib/SparseDoubleVector
     *
     * @param vector
     * @return magnitude squared of vector
     */
    public static double lengthSquared(SparseDoubleVector vector) {
        double lengthSq = 0;
        for (int i = 0; i < vector.size; i++)
            lengthSq += vector.values[i] * vector.values[i];
        return lengthSq;
    }

}

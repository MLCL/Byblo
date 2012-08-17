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
package uk.ac.susx.mlcl.byblo.weighings.impl;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * {@link Weighting} that normalizes the feature vector to an L2 unit vector, by
 * dividing by the normal.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
public final class L2UnitVector
        implements Weighting, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public final SparseDoubleVector apply(SparseDoubleVector from) {
        final SparseDoubleVector to = from.clone();
        double lenSq = lengthSquared(from);
        double sum = 0;
        for (int i = 0; i < from.size; i++) {
            to.values[i] = from.values[i] / lenSq;
            sum += to.values[i];
        }
        to.sum = sum;
        to.compact();
        return to;
    }

    @Override
    public double getLowerBound() {
        return 0.0;
    }

    @Override
    public double getUpperBound() {
        return 1.0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * The magnitude of the vector, i.e the L2 vector normal.
     * <p/>
     * TODO: Move to mlcl-lib/SparseDoubleVector
     * <p/>
     * @param vector
     * @return magnitude of vector
     */
    public static double length(SparseDoubleVector vector) {
        return Math.sqrt(lengthSquared(vector));
    }

    /**
     * Calculate the square of the L@ norm of vector.
     * <p/>
     * TODO: Move to mlcl-lib/SparseDoubleVector
     * <p/>
     * @param vector
     * @return magnitude squared of vector
     */
    public static double lengthSquared(SparseDoubleVector vector) {
        double lengthSq = 0;
        for (int i = 0; i < vector.size; i++)
            lengthSq += vector.values[i] * vector.values[i];
        return lengthSq;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 19;
    }
}

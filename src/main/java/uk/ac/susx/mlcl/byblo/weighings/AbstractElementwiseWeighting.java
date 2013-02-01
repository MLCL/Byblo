/*
 * Copyright (c) 2010-2013, University of Sussex
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

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * {@linkElementwiseWeighting} maps element-wise from a single feature input
 * weight to the output weight, but with addition contextual information
 * provided.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
@CheckReturnValue
public abstract class AbstractElementwiseWeighting implements Weighting {

    /**
     * Construct a new instance of {@link AbstractContextualWeighting}
     */
    protected AbstractElementwiseWeighting() {
    }

    @Override
    public final SparseDoubleVector apply(final SparseDoubleVector from) {
        if (from.size == 0)
            return new SparseDoubleVector(from.cardinality, 0);
        final SparseDoubleVector to = from.clone();
        double sum = 0;
        for (int i = 0; i < from.size; i++) {
            to.values[i] = apply(from, from.keys[i],
                    from.values[i]);
            sum += to.values[i];
        }
        to.sum = sum;
        to.compact();
        return to;
    }

    /**
     * Re-weighting the feature <tt>key</tt> with input weight <tt>value</tt>,
     * using contextual information provided by the whole feature
     * <tt>vector</tt> returning a new weighting
     * <p/>
     *
     * @param vector feature vector of a particular entry
     * @param key    enumerated feature index
     * @param value  feature input weighting
     * @return feature output weighting
     */
    protected abstract double apply(SparseDoubleVector vector,
                                    int key,
                                    double value);

    @Override
    public abstract double getLowerBound();

    @Override
    public abstract double getUpperBound();

}

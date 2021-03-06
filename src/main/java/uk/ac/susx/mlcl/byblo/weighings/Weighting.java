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

/**
 * Interface that defines a function mapping between features spaces.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
public interface Weighting {

    /**
     * Re-weight all the elements of feature vector <tt>from</tt>.
     * <p/>
     * If the implementation makes any changes to the input vector it should
     * first take a copy by calling {@link SparseDoubleVector#clone() }.
     * <p/>
     * If it is possible that any previously non-zero value has been re-weighted
     * to zero, the implementation should call
     * {@link SparseDoubleVector#compact() } on the output vector before
     * returning.
     * <p/>
     *
     * @param from vector to re-weight
     * @return Re-weighted vector.
     */
    SparseDoubleVector apply(SparseDoubleVector from);

    /**
     * Accessor to the minimum weighting this scheme will produce, assuming the
     * input vector contains only real valued positive values.
     * <p/>
     *
     * @return minimum possible weighting value
     */
    double getLowerBound();

    /**
     * Accessor to the maximum weighting this scheme will produce, assuming the
     * input vector contains only real valued positive values.
     * <p/>
     *
     * @return maximum possible weighting value
     */
    double getUpperBound();
}

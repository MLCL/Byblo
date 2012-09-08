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
package uk.ac.susx.mlcl.byblo.measures.v2;

import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * {@link Decomposable} defines an interface that measures can optimally
 * implement that breaks down their operation into sub-functions.
 *
 * This interface has been expanded from {@link Proximity} and {@link Distance}
 * (the usual single method interfaces) to allow for pre-calculation of values
 * that are dependant only on one vector.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Decomposable extends Measure {

    /**
     * Calculate the similarity of the given vectors A and B. If possible this
     * should be based entirely upon shared features, while independent features
     * can be pre-calculated using {@link #left(SparseDoubleVector)} and
     * {@link #right(SparseDoubleVector)}. All three values are combined using
     * {@link Proximity#combine(double, double, double)}.
     *
     * @param A the first feature vector
     * @param B the second feature vector
     * @return portion of similarity measure dependent on both A and B
     */
    double shared(SparseDoubleVector A, SparseDoubleVector B);

    double left(SparseDoubleVector A);

    double right(SparseDoubleVector B);

    double combine(double shared, double left, double right);
}

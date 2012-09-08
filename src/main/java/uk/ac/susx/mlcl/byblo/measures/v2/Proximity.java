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
 * {@link Proximity} defines a common interface that proximity based similarity
 * measure must implement. Proximity (as opposed to {@link Distance}) indicates
 * that similarity scores get larger as the vectors been compared become more
 * similar.
 *
 * In general a proximity score should be in the range [0,1] where 0 indicates
 * maximal similarity (homogeneity), and 1 indicates maximal dissimilarity
 * (heterogeneity). If this is not the case then the implementing class should
 * take care to provide the true lower and upper bounds using
 * {@link #getHomogeneityBound()} and {@link #getHeterogeneityBound() }
 * respectively.
 *
 * Historically, most proximity measures define a symmetric similarity space,
 * but this is not always the case. If this implementation defines a asymmetric
 * space (where for some x,y, sim(x,y) != sim(y,x)) then
 * {@link #isCommutative() } should be implemented to return false.
 *
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Proximity extends Measure {

    /**
     * Compute the similarity score between the vector operands as a proximity.
     * Larger values indicate a greater degree as similarity, as opposed to a
     * distance metric where scores tend towards zero as the similarity
     * increases.
     *
     * @param A first feature vector
     * @param B second feature vector
     * @return similarity of the feature vectors
     */
    double proximity(SparseDoubleVector A, SparseDoubleVector B);


  
}

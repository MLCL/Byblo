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

import javax.annotation.CheckReturnValue;
import uk.ac.susx.mlcl.byblo.measures.impl.Dice;
import uk.ac.susx.mlcl.byblo.measures.impl.Jaccard;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * Measure is a common super-interface to various similarity measure types.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public interface Measure {

    /**
     * Compute the similarity score between the vector operands.
     * <p/>
     * @param A first feature vector
     * @param B second feature vector
     * @return similarity of the feature vectors
     */
    @CheckReturnValue
    double similarity(SparseDoubleVector A, SparseDoubleVector B);

    /**
     * Gets the to similarity score that will be produced by this measure when
     * two vectors are identical. In the case of proximity scores this value
     * should usually be 0. For distance metrics it is usually +infinity,
     * <p/>
     * @return value indicating vectors are identical
     */
    @CheckReturnValue
    double getHomogeneityBound();

    /**
     * Gets the similarity score that will be produced by this measure when two
     * vectors could not be more dissimilar. In the case of proximity scores
     * this value should usually be 1 or +infinity. For distance metrics it is
     * usually 0,
     * <p/>
     * @return value indicating vectors are maximally dissimilar
     */
    @CheckReturnValue
    double getHeterogeneityBound();

    /**
     * Gets the {@link Weighting} scheme that should have been previously
     * applied to feature vectors before this {@link Measure} implementation is
     * used.
     * <p/>
     * Some measures can operate on pretty much any kind of vector (for example
     * geometric distance measures). Others require the vectors to take a
     * particular form. Set theoretic measures {@link Dice} and {@link Jaccard}
     * require feature weights to be positive, though not binary as one might
     * expect since multi-set generalizations are implemented. Lin and Weeds
     * measure expect PMI weighting.
     * <p/>
     * @return weighting scheme that should have been previously applied to
     *         vectors
     */
    @CheckReturnValue
    Class<? extends Weighting> getExpectedWeighting();

    /**
     * Whether or not the operands can be reversed without changing the
     * resultant similarity score.
     * <p/>
     * Traditionally, similarity measures define a symmetric space but this does
     * not have to be the case. Symmetric space must be defined by a commutative
     * similarity measure (where sim(x,y) = sim(y,x)). However, non-symmetric
     * spaces are possible. In these cases the measure should be marked as
     * non-commutative.
     * <p/>
     * In the case of distance measures, a commutative kernel will define a
     * <em>true</em> metric, while a non-commutative kernel will not. For
     * example {@link KLDivergence} is a distance measure while the
     * {@link LpSpace} variants are true metrics.
     * <p/>
     * @return true if the measure defines a symmetric space, false otherwise
     */
    @CheckReturnValue
    boolean isCommutative();
}

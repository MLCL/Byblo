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

/**
 *
 * Parameters
 *
 * β   γ  Special Case
 *
 * -   1  harmonic mean of precision and recall (F-score) (DiceMi)
 * β   0  weighted arithmetic mean of precision and recall
 * 1   0  precision 
 * 0   0  recall
 * 0.5 0  unweighted arithmetic mean
 *
 * <ul>
 * <li>Weeds, Julie, and David Weir. (December 2005) Co-occurrence Retrieval: A 
 * Flexible Framework for Lexical Distributional Similarity. Computational
 * Linguistics 31, no. 4: 439-475.</li>
 * </ul>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CrMi extends AbstractMIProximity {

    public static final double DEFAULT_BETA = 0.5;

    public static final double DEFAULT_GAMMA = 0.5;

    private final RecallMi recallMi;

    private double beta;

    private double gamma;

    public CrMi() {
        recallMi = new RecallMi();
        this.beta = DEFAULT_BETA;
        this.gamma = DEFAULT_GAMMA;
    }

    @Override
    public void setFeatureFrequencies(double[] contextFrequencies) {
        recallMi.setFeatureFrequencies(contextFrequencies);
        super.setFeatureFrequencies(contextFrequencies);
    }

    @Override
    public void setFeatureFrequencySum(double contextSum) {
        recallMi.setFeatureFrequencySum(contextSum);
        super.setFeatureFrequencySum(contextSum);
    }

    public final void setBeta(final double beta) {
        if (beta < 0 || beta > 1)
            throw new IllegalArgumentException(
                    "beta parameter expected in range 0 to 1, but found " + beta);
        this.beta = beta;
    }

    public final void setGamma(final double gamma) {
        if (gamma < 0 || gamma > 1)
            throw new IllegalArgumentException(
                    "beta parameter expected in range 0 to 1, but found " + gamma);
        this.gamma = gamma;
    }

    public final double getBeta() {
        return beta;
    }

    public final double getGamma() {
        return gamma;
    }

    @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        final double recall = recallMi.shared(A, B) / recallMi.left(A);
        final double precision = recallMi.shared(B, A) / recallMi.left(B);

        // arithmetic mean
        final double am = (beta * precision) + ((1 - beta) * recall);

        // harmonic mean (aka F1 score)
        final double hm = (precision + recall) != 0
                ? (2 * precision * recall) / (precision + recall)
                : 0;
        return gamma * hm + (1 - gamma) * am;
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
        // If gamma = 1.0 then only then harmonic component is used, hence the
        // measure is symmetric. Otherwise some portion of the arithmetic
        // component is used which is symmetric only when beta = 0.5.
        return gamma == 1.0 || beta == 0.5;
    }

    @Override
    public String toString() {
        return "CrMi{" + "beta=" + beta + ", gamma=" + gamma + '}';
    }
}

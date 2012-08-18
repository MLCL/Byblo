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
package uk.ac.susx.mlcl.byblo.measures.impl;

import java.io.Serializable;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import uk.ac.susx.mlcl.byblo.measures.Measure;
import uk.ac.susx.mlcl.byblo.measures.Measures;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.PositivePMI;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 *
 * Parameters
 * <p/>
 * β γ Special Case
 * <p/>
 * - 1 harmonic mean of precision and recall (F-score) (DiceMi) β 0 weighted
 * arithmetic mean of precision and recall 1 0 precision 0 0 recall 0.5 0
 * unweighted arithmetic mean
 * <p/>
 * <ul> <li>Weeds, Julie, and David Weir. (December 2005) Co-occurrence
 * Retrieval: A Flexible Framework for Lexical Distributional Similarity.
 * Computational Linguistics 31, no. 4: 439-475.</li> </ul>
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
public class Weeds implements Measure, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Recall RECALL = new Recall();

    private static final Precision PRECISION = new Precision();

    @Nonnegative
    public static final double DEFAULT_BETA = 0.5;

    @Nonnegative
    public static final double DEFAULT_GAMMA = 0.5;

    @Nonnegative
    private double beta;

    @Nonnegative
    private double gamma;

    public Weeds() {
        this(DEFAULT_BETA, DEFAULT_GAMMA);
    }

    public Weeds(@Nonnegative final double beta, @Nonnegative final double gamma) {
        setBeta(beta);
        setGamma(gamma);
    }

    public final void setBeta(@Nonnegative final double beta) {
        if (beta < 0.0 || beta > 1.0)
            throw new IllegalArgumentException(
                    "beta parameter expected in range 0 to 1, but found " + beta);
        this.beta = beta;
    }

    public final void setGamma(@Nonnegative final double gamma) {
        if (gamma < 0 || gamma > 1)
            throw new IllegalArgumentException(
                    "beta parameter expected in range 0 to 1, but found "
                    + gamma);
        this.gamma = gamma;
    }

    @Nonnegative
    public final double getBeta() {
        return beta;
    }

    @Nonnegative
    public final double getGamma() {
        return gamma;
    }

    @Override
    public double similarity(SparseDoubleVector A, SparseDoubleVector B) {
        assert beta >= 0 && beta <= 1;
        assert gamma >= 0 && gamma <= 1;

        final double recall = RECALL.similarity(A, B);
        final double precision = PRECISION.similarity(A, B);

        // arithmetic mean
        final double am = (beta * precision) + ((1 - beta) * recall);

        // harmonic mean (aka F1 score)
        final double hm = (precision + recall) != 0
                ? (2 * precision * recall) / (precision + recall)
                : 0;

        final double sim = gamma * hm + (1.0 - gamma) * am;

        assert sim >= 0.0 && sim <= 1.0;
        return sim;

    }

    @Override
    public boolean isCommutative() {
        // If gamma = 1.0 then only then harmonic component is used, hense the 
        // measure is symetric. Otherwise some portion of the arithimentic 
        // component is used which is symetric only when beta = 0.5.
        return Measures.epsilonEquals(gamma, 1.0)
                || Measures.epsilonEquals(beta, 0.5);
    }

    @Override
    public double getHomogeneityBound() {
        return 1.0;
    }

    @Override
    public double getHeterogeneityBound() {
        return 0.0;
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return PositivePMI.class;
    }

    @Override
    public String toString() {
        return "Weeds{" + "beta=" + beta + ", gamma=" + gamma + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Weeds other = (Weeds) obj;
        if (Double.doubleToLongBits(this.beta) != Double.doubleToLongBits(
                other.beta))
            return false;
        if (Double.doubleToLongBits(this.gamma) != Double.doubleToLongBits(
                other.gamma))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        final long betaBits = Double.doubleToLongBits(this.beta);
        final long gammaBits = Double.doubleToLongBits(this.gamma);
        hash = 37 * hash + (int) (betaBits ^ (betaBits >>> 32));
        hash = 37 * hash + (int) (gammaBits ^ (gammaBits >>> 32));
        return hash;
    }
}

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
package uk.ac.susx.mlcl.byblo.measures.v2.impl;

import java.io.Serializable;
import uk.ac.susx.mlcl.byblo.measures.v2.DecomposableMeasure;
import uk.ac.susx.mlcl.byblo.measures.v2.Measures;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * {@link ProximityMeasure} calculating proximity as the degree orthogonality
 * between vectors; the cosine of the angle between them.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Cosine extends DecomposableMeasure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public double shared(SparseDoubleVector A, SparseDoubleVector B) {
        return Measures.dotProduct(A, B);
    }

    @Override
    public double left(SparseDoubleVector A) {
        return Measures.lengthSquared(A);
    }

    @Override
    public double right(SparseDoubleVector B) {
        return Measures.lengthSquared(B);
    }

    @Override
    public double combine(double shared, double left, double right) {
        return (left == 0 || right == 0 || shared == 0) ? 0
                : shared / Math.sqrt(left * right);
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public double getHomogeneityBound() {
        return 1.0;
    }

    @Override
    public double getHeterogeneityBound() {
        return -1.0;
    }

    @Override
    public Weighting getExpectedWeighting() {
        return Weightings.none();
    }

    @Override
    public String toString() {
        return "Cosine";
    }
}

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

import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.Weightings;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * <code>ForwardingMeasure</code> is an abstract super-class to decorators of
 * <code>Measure</code> instances.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> type of Measure being decorated
 */
public abstract class ForwardingMeasure<T extends Measure>
        implements Measure {

    private final T deligate;

    /**
     * Constructor used by sub-classes.
     *
     * @param deligate The Measure being decorated.
     */
    protected ForwardingMeasure(final T deligate) {
        Checks.checkNotNull("deligate", deligate);
        this.deligate = deligate;
    }

    @Override
    public double similarity(
            final SparseDoubleVector A,
            final SparseDoubleVector B) {
        return deligate.similarity(A, B);
    }

    public final T getDelegate() {
        return deligate;
    }

    @Override
    public double getHomogeneityBound() {
        return deligate.getHomogeneityBound();
    }

    @Override
    public double getHeterogeneityBound() {
        return deligate.getHeterogeneityBound();
    }

    @Override
    public Weighting getExpectedWeighting() {
        return Weightings.none();
    }

    @Override
    public boolean isCommutative() {
        return deligate.isCommutative();
    }

    public boolean equals(ForwardingMeasure<?> other) {
        if (this.deligate != other.deligate
                && (this.deligate == null
                    || !this.deligate.equals(other.deligate)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((ForwardingMeasure) obj);
    }

    @Override
    public int hashCode() {
        return 79 * 5 + (deligate != null ? deligate.hashCode() : 0);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{deligate=" + deligate + '}';
    }
}

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
package uk.ac.susx.mlcl.byblo.measures;

import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * <code>ForwardingMeasure</code> is an abstract super-class to decorators of
 * <code>Measure</code> instances.
 *
 * @param <T> type of Measure being decorated
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Immutable
@CheckReturnValue
public abstract class ForwardingMeasure<T extends Measure>
        implements Measure {

    private final T delegate;

    /**
     * Constructor used by sub-classes.
     *
     * @param delegate The Measure being decorated.
     */
    ForwardingMeasure(final T delegate) {
        Checks.checkNotNull("delegate", delegate);
        this.delegate = delegate;
    }

    @Override
    public double similarity(
            final SparseDoubleVector A,
            final SparseDoubleVector B) {
        return delegate.similarity(A, B);
    }

    public final T getDelegate() {
        return delegate;
    }

    @Override
    public double getHomogeneityBound() {
        return delegate.getHomogeneityBound();
    }

    @Override
    public double getHeterogeneityBound() {
        return delegate.getHeterogeneityBound();
    }

    @Override
    public Class<? extends Weighting> getExpectedWeighting() {
        return NullWeighting.class;
    }

    @Override
    public boolean isCommutative() {
        return delegate.isCommutative();
    }

    boolean equals(ForwardingMeasure<?> other) {
        return !(this.delegate != other.delegate && (this.delegate == null || !this.delegate.equals(other.delegate)));
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || !(obj == null || getClass() != obj.getClass()) && equals((ForwardingMeasure) obj);
    }

    @Override
    public int hashCode() {
        return 79 * 5 + (delegate != null ? delegate.hashCode() : 0);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{delegate=" + delegate + '}';
    }
}

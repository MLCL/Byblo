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
package uk.ac.susx.mlcl.byblo.weighings.impl;

import uk.ac.susx.mlcl.byblo.weighings.AbstractSimpleWeighting;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;

import java.io.Serializable;

/**
 * {@link Weighting} that simply returns the value stored in the vector
 * parameter multiplied by some constant.
 * <p/>
 * Used when no re-weighting is required (i.e identity) or when some constant
 * scaFling is useful.
 * <p/>
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Constant
        extends AbstractSimpleWeighting
        implements Serializable {

    public static final double DEFAULT_FACTOR = 1;

    private static final long serialVersionUID = 1L;

    public double factor;

    public Constant(final double factor) {
        setFactor(factor);
    }

    public Constant() {
        setFactor(DEFAULT_FACTOR);
    }

    public final double getFactor() {
        return factor;
    }

    public final void setFactor(double factor) {
        this.factor = factor;
    }

    @Override
    public double apply(double value) {
        return factor * value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "factor=" + factor + '}';
    }

    @Override
    public double getUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    private boolean equals(Constant that) {
        if (Double.doubleToLongBits(this.factor)
                != Double.doubleToLongBits(that.factor))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((Constant) obj);
    }

    @Override
    public int hashCode() {
        final long fBits = Double.doubleToLongBits(this.factor);
        return (67 * 11 + (int) (fBits ^ (fBits >>> 32)));
    }
}

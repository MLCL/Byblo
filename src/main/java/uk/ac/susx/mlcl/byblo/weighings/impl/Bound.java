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
import java.text.MessageFormat;

/**
 * A {@link Weighting} bounds each feature weighting within a fixed range.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Bound
        extends AbstractSimpleWeighting
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final double DEFAULT_LOWER_BOUND = Double.NEGATIVE_INFINITY;

    public static final double DEFAULT_UPPER_BOUND = Double.POSITIVE_INFINITY;

    public double lowerBound;

    public double upperBound;

    public Bound() {
        setLowerBound(DEFAULT_LOWER_BOUND);
        setUpperBound(DEFAULT_UPPER_BOUND);
    }

    public Bound(double lowerBound, double upperBound) {
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
    }

    public void setLowerBound(final double lowerBound) {
        if (Double.isNaN(lowerBound))
            throw new IllegalArgumentException("lowerBound");
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(final double upperBound) {
        if (Double.isNaN(lowerBound))
            throw new IllegalArgumentException("upperBound");
        this.upperBound = upperBound;
    }

    @Override
    protected double apply(final double value) {
        if (value <= lowerBound)
            return lowerBound;
        else if (value >= upperBound)
            return upperBound;
        return value;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Bound{{1},{2}}",
                getLowerBound(), getUpperBound());
    }

    private boolean equals(Bound that) {
        if (Double.doubleToLongBits(this.lowerBound)
                != Double.doubleToLongBits(that.lowerBound))
            return false;
        if (Double.doubleToLongBits(this.upperBound)
                != Double.doubleToLongBits(that.upperBound))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((Bound) obj);
    }

    @Override
    public int hashCode() {
        final long lbBits = Double.doubleToLongBits(this.lowerBound);
        final long ubBits = Double.doubleToLongBits(this.upperBound);
        return 67 * (67 * 5 + (int) (lbBits ^ (lbBits >>> 32)))
                + (int) (ubBits ^ (ubBits >>> 32));
    }
}

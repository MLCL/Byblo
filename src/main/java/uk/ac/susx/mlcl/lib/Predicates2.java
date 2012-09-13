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
package uk.ac.susx.mlcl.lib;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.io.Serializable;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Predicates2 {

    private static Predicate<Double> lt(final double threshold) {
        if (Double.isNaN(threshold))
            return Predicates.alwaysFalse();
        else if (threshold == Double.NEGATIVE_INFINITY)
            return Predicates.alwaysFalse();
        else
            return new LessThan(threshold);
    }

    public static Predicate<Double> eq(final double value) {
        if (Double.isNaN(value))
            return Predicates.alwaysFalse();
        else
            return Predicates.equalTo(value);
    }

    public static Predicate<Double> lte(final double threshold) {
        if (Double.isNaN(threshold))
            return Predicates.alwaysFalse();
        else if (threshold == Double.POSITIVE_INFINITY)
            return Predicates.alwaysFalse();
        else
            return Predicates.not(gt(threshold));
    }

    private static Predicate<Double> gt(final double threshold) {
        if (Double.isNaN(threshold))
            return Predicates.alwaysFalse();
        else if (threshold == Double.POSITIVE_INFINITY)
            return Predicates.alwaysFalse();
        else
            return new GreaterThan(threshold);
    }

    public static Predicate<Double> gte(final double threshold) {
        if (Double.isNaN(threshold))
            return Predicates.alwaysFalse();
        else if (threshold == Double.NEGATIVE_INFINITY)
            return Predicates.alwaysFalse();
        else
            return Predicates.not(lt(threshold));
    }

    public static Predicate<Double> inRange(double min, double max) {
        if (min > max)
            return Predicates.alwaysFalse();
        if (min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY)
            return Predicates.alwaysTrue();
        if (Double.isNaN(min) || Double.isNaN(max))
            return Predicates.alwaysFalse();
        return new InRange(min, max);
    }

    private static class InRange implements Predicate<Double>, Serializable {

        private static final long serialVersionUID = 1L;

        private final double min;

        private final double max;

        public InRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean apply(Double input) {
            return input >= min && input <= max;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final InRange other = (InRange) obj;
            return Double.doubleToLongBits(this.min) == Double.doubleToLongBits(other.min) && Double.doubleToLongBits(this.max) == Double.doubleToLongBits(other.max);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(
                    this.min) >>> 32));
            hash = 67 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(
                    this.max) >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).
                    addValue(min + " to " + max).
                    toString();
        }

    }

    private static class GreaterThan implements Predicate<Double>, Serializable {

        private static final long serialVersionUID = 1L;

        private final double threshold;

        public GreaterThan(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean apply(Double input) {
            return input > threshold;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final GreaterThan other = (GreaterThan) obj;
            return Double.doubleToLongBits(this.threshold) == Double.doubleToLongBits(other.threshold);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.threshold) ^ (Double.doubleToLongBits(this.threshold) >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).
                    addValue(threshold).
                    toString();
        }

    }

    private static class LessThan implements Predicate<Double>, Serializable {

        private static final long serialVersionUID = 1L;

        private final double threshold;

        public LessThan(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean apply(Double input) {
            return input < threshold;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final GreaterThan other = (GreaterThan) obj;
            return Double.doubleToLongBits(this.threshold) == Double.doubleToLongBits(other.threshold);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.threshold) ^ (Double.doubleToLongBits(this.threshold) >>> 32));
            return hash;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).
                    addValue(threshold).
                    toString();
        }

    }

    private Predicates2() {
    }

}

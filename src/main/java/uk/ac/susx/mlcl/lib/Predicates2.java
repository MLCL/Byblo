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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Predicates2 {

    public static <T> Predicate<T> in(Collection<? extends T> target) {
        if (target.isEmpty())
            return alwaysFalse();
        else
            return new InPredicate<T>(target);
    }

    public static <A, B> Predicate<A> compose(
            Predicate<B> predicate, Function<A, ? extends B> function) {
        if (predicate.equals(alwaysFalse()))
            return alwaysFalse();
        else if (predicate.equals(alwaysTrue()))
            return alwaysTrue();
        else
            return Predicates.compose(predicate, function);
    }

    public static <T> Predicate<T> alwaysTrue() {
        return Predicates.alwaysTrue();
    }

    public static <T> Predicate<T> alwaysFalse() {
        return Predicates.alwaysFalse();
    }

    public static <T> Predicate<T> isNull() {
        return Predicates.isNull();
    }

    public static <T> Predicate<T> notNull() {
        return Predicates.notNull();
    }

    public static <T> Predicate<T> not(Predicate<T> predicate) {
        if (predicate.equals(alwaysFalse()))
            return alwaysTrue();
        else if (predicate.equals(alwaysFalse()))
            return alwaysTrue();
        else
            return Predicates.not(predicate);
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> and(
            Iterable<? extends Predicate<? super T>> components) {
        Set<Predicate<? super T>> result = new HashSet<Predicate<? super T>>();
        for (Predicate<? super T> predicate : components) {
            if (predicate.equals(alwaysFalse()))
                return alwaysFalse();
            else if (!predicate.equals(alwaysTrue()))
                result.add(Preconditions.checkNotNull(predicate));
        }
        if (result.isEmpty())
            return alwaysTrue();
        if (result.size() == 1)
            return (Predicate<T>) result.iterator().next();
        else
            return Predicates.and(components);
    }

    public static <T> Predicate<T> and(Predicate<? super T>... components) {
        return and(Arrays.asList(components));
    }

    public static <T> Predicate<T> and(
            Predicate<? super T> first, Predicate<? super T> second) {
        return and(Predicates2.<T>asList(first, second));
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> or(
            Iterable<? extends Predicate<? super T>> components) {
        Set<Predicate<? super T>> result = new HashSet<Predicate<? super T>>();
        for (Predicate<? super T> predicate : components) {
            if (predicate.equals(alwaysTrue()))
                return alwaysTrue();
            else if (!predicate.equals(alwaysFalse()))
                result.add(Preconditions.checkNotNull(predicate));
        }
        if (result.isEmpty())
            return alwaysFalse();
        if (result.size() == 1)
            return (Predicate<T>) result.iterator().next();
        else
            return Predicates.or(components);
    }

    public static <T> Predicate<T> or(Predicate<? super T>... components) {
        return or(Arrays.asList(components));
    }

    public static <T> Predicate<T> or(Predicate<? super T> first,
                                      Predicate<? super T> second) {
        return or(Predicates2.<T>asList(first, second));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<Predicate<? super T>> asList(
            Predicate<? super T> first, Predicate<? super T> second) {
        return Arrays.<Predicate<? super T>>asList(
                Preconditions.checkNotNull(first),
                Preconditions.checkNotNull(second));
    }

    public static <T> Predicate<T> equalTo(T target) {
        return Predicates.equalTo(target);
    }

    public static Predicate<Object> instanceOf(Class<?> clazz) {
        return Predicates.instanceOf(clazz);
    }

    public static Predicate<CharSequence> containsPattern(String pattern) {
        return Predicates.containsPattern(pattern);
    }

    public static Predicate<CharSequence> contains(Pattern pattern) {
        return Predicates.contains(pattern);
    }

    public static Predicate<Double> lt(final double threshold) {
        if (Double.isNaN(threshold))
            return alwaysFalse();
        else if (threshold == Double.NEGATIVE_INFINITY)
            return alwaysFalse();
        else
            return new LessThan(threshold);
    }

    public static Predicate<Double> eq(final double value) {
        if (Double.isNaN(value))
            return alwaysFalse();
        else
            return Predicates.equalTo(value);
    }

    public static Predicate<Double> lte(final double threshold) {
        if (Double.isNaN(threshold))
            return alwaysFalse();
        else if (threshold == Double.POSITIVE_INFINITY)
            return alwaysFalse();
        else
            return not(gt(threshold));
    }

    public static Predicate<Double> gt(final double threshold) {
        if (Double.isNaN(threshold))
            return alwaysFalse();
        else if (threshold == Double.POSITIVE_INFINITY)
            return alwaysFalse();
        else
            return new GreaterThan(threshold);
    }

    public static Predicate<Double> gte(final double threshold) {
        if (Double.isNaN(threshold))
            return alwaysFalse();
        else if (threshold == Double.NEGATIVE_INFINITY)
            return alwaysFalse();
        else
            return not(lt(threshold));
    }

    public static Predicate<Double> inRange(double min, double max) {
        if (min > max)
            return alwaysFalse();
        if (min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY)
            return alwaysTrue();
        if (Double.isNaN(min) || Double.isNaN(max))
            return alwaysFalse();
        return new InRange(min, max);
    }

    private static class InPredicate<T> implements Predicate<T>, Serializable {

        private static final long serialVersionUID = 0;

        private final Collection<?> target;

        private InPredicate(Collection<?> target) {
            this.target = Preconditions.checkNotNull(target);
        }

        @Override
        public boolean apply(T t) {
            try {
                return target.contains(t);
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InPredicate) {
                InPredicate<?> that = (InPredicate<?>) obj;
                return target.equals(that.target);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return target.hashCode();
        }

        @Override
        @SuppressWarnings("unchecked")
        public String toString() {
            if (target.size() <= 20)
                return "In(" + target + ")";
            else {
                Collection<T> tmp = new ArrayList<T>();
                int i = 0;
                Iterator<?> it = target.iterator();
                while (it.hasNext() && i++ < 20) {
                    tmp.add((T) it.next());
                }
                return "In(" + tmp + ", ...)";
            }
        }

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
            if (Double.doubleToLongBits(this.min) != Double.doubleToLongBits(
                    other.min))
                return false;
            if (Double.doubleToLongBits(this.max) != Double.doubleToLongBits(
                    other.max))
                return false;
            return true;
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
            if (Double.doubleToLongBits(this.threshold) != Double.doubleToLongBits(other.threshold))
                return false;
            return true;
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
            if (Double.doubleToLongBits(this.threshold) != Double.doubleToLongBits(other.threshold))
                return false;
            return true;
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

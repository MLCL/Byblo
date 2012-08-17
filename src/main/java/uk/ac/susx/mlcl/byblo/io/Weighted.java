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
package uk.ac.susx.mlcl.byblo.io;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import java.io.Serializable;
import java.util.Comparator;

/**
 * <tt>Weighted</tt> objects represent a weighting or frequency applied to some
 * discrete record.
 *
 * <p>Instances of <tt>Weighted</tt> are immutable.<p>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> Type of record being weighted
 */
public class Weighted<T> implements Serializable, Comparable<Weighted<T>> {

    private static final long serialVersionUID = 1L;

    double weight;

    T record;

    public Weighted(final T record, final double weight) {
        this.weight = weight;
        this.record = record;
    }

    /**
     * Constructor used during de-serialization.
     */
    protected Weighted() {
    }

    public double weight() {
        return weight;
    }

    public T record() {
        return record;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Note that only the <tt>entryId</tt> field is used for equality. I.e
     * two objects with the same <tt>entryId</tt>, but differing weights
     * <em>will</em> be consider equal.</p>
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return equals((Weighted<?>) obj);
    }

    public boolean equals(Weighted<?> other) {
        return record.equals(other.record());
    }

    @Override
    public int hashCode() {
        return record.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                addValue(record).add("weight", weight).toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Weighted<T> that) {
        if (record instanceof Comparable<?>) {
            return ((Comparable<T>) record).compareTo(that.record());
        } else {
            throw new RuntimeException(new UnsupportedOperationException(
                    "Comparable not implemented by inner object."));
        }
    }

    public static <T> Predicate<Weighted<T>> greaterThan(final double weight) {
        return new Predicate<Weighted<T>>() {
            @Override
            public boolean apply(Weighted<T> pair) {
                return pair != null && (pair.weight() > weight);
            }

            @Override
            public String toString() {
                return "weight>" + weight;
            }
        };
    }

    public static <T> Predicate<Weighted<T>> greaterThanOrEqualTo(
            final double weight) {
        return new Predicate<Weighted<T>>() {
            @Override
            public boolean apply(Weighted<T> pair) {
                return pair != null && (pair.weight() >= weight);
            }

            @Override
            public String toString() {
                return "weight>=" + weight;
            }
        };
    }

    public static <T> Predicate<Weighted<T>> equalTo(final double weight) {
        return new Predicate<Weighted<T>>() {
            @Override
            public boolean apply(Weighted<T> pair) {
                return pair != null && Math.abs(pair.weight() - weight) < 0.0000001;
            }

            @Override
            public String toString() {
                return "weight==" + weight;
            }
        };
    }

    public static <T> Predicate<Weighted<T>> lessThanOrEqualTo(
            final double weight) {
        return new Predicate<Weighted<T>>() {
            @Override
            public boolean apply(Weighted<T> pair) {
                return pair != null && (pair.weight() <= weight);
            }

            @Override
            public String toString() {
                return "weight<=" + weight;
            }
        };
    }

    public static <T> Predicate<Weighted<T>> lessThan(final double weight) {
        return new Predicate<Weighted<T>>() {
            @Override
            public boolean apply(Weighted<T> pair) {
                return pair != null && (pair.weight() < weight);
            }

            @Override
            public String toString() {
                return "weight<" + weight;
            }
        };
    }

    public static <T> Function<Weighted<T>, T> recordFunction() {
        return new Function<Weighted<T>, T>() {
            @Override
            public final T apply(final Weighted<T> input) {
                return input == null ? null : input.record();
            }

            @Override
            public final String toString() {
                return "Record";
            }
        };
    }

    public static <S> Comparator<Weighted<S>> weightOrder() {
        return new Comparator<Weighted<S>>() {
            @Override
            public int compare(Weighted<S> t, Weighted<S> t1) {
                return Double.compare(t.weight(), t1.weight());
            }
        };
    }

    public static <S> Comparator<Weighted<S>> recordOrder(
            final Comparator<S> inner) {
        return new Comparator<Weighted<S>>() {
            @Override
            public int compare(Weighted<S> t, Weighted<S> t1) {
                return inner.compare(t.record(), t1.record());
            }
        };
    }
}

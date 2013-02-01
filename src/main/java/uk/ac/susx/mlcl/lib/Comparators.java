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
package uk.ac.susx.mlcl.lib;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * A static utility class for creating, and manipulating Comparator objects.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Comparators {

    /**
     * Utility class can not be instantiated.
     */
    private Comparators() {
    }

    /**
     * Produce a new Comparator that is identical to comp, except that the
     * arguments are reversed. I.e compare(a,b) becomes compare(b,a). Hence, for
     * some comparator c reverse(c).compare(a,b) = -c.compare(a,b).
     *
     * @param <T> the type of object being compared
     * @param comp comparator to reverse the arguments of
     * @return argument reversed comparator
     */
    public static <T> Comparator<T> reverse(final Comparator<T> comp) {
        Checks.checkNotNull(comp);
        return (comp.getClass().equals(ReverseComparator.class))
               ? ((ReverseComparator<T>) comp).getInner()
               : new ReverseComparator<T>(comp);
    }

    /**
     * <p>Comparator decorator that reverses the operands.</p>
     *
     * <p>When used with a sorting operation this will typically reverse the
     * sort from ascending, to descending.</p>
     *
     * <p>Serialization is supported if, and only i, the encapsulated
     * {@link java.util.Comparator} is also serializable.</p>
     *
     * @param <T>
     * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
     */
    private static final class ReverseComparator<T>
            implements Comparator<T>, Serializable {

        public static final long serialVersionUID = 1L;

        private final Comparator<T> inner;

        private ReverseComparator(final Comparator<T> inner) {
            this.inner = inner;
        }

        @Override
        public int compare(final T o1, final T o2) {
            return inner.compare(o2, o1);
        }

        public Comparator<T> getInner() {
            return inner;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName());
            builder.append('{');
            builder.append("inner=").append(inner);
            builder.append('}');
            return builder.toString();
        }

    }

    /**
     * Produces a composite comparator that returns the comparison according to
     * the first argument if non-zero, otherwise fallback to the result of the
     * second comparator.
     *
     * @param <T> Type of object being compared
     * @param a first comparator
     * @param b second comparator, used if the first returns 0
     * @return composite fallback comparator
     */
    public static <T> Comparator<T> fallback(
            final Comparator<T> a, final Comparator<T> b) {
        return new Comparator<T>() {

            @Override
            public int compare(final T o1, final T o2) {
                final int c = a.compare(o1, o2);
                return c != 0 ? c : b.compare(o1, o2);
            }

        };
    }

    /**
     *
     * @param <T>
     * @param comparators
     * @return produce a comparator that backsoff to each listed comparator
     */
    public static <T> Comparator<T> fallback(
            final Comparator<T>... comparators) {
        Checks.checkNotNull("comparators", comparators);
        Checks.checkRangeIncl(comparators.length, 1, Integer.MAX_VALUE);
        Comparator<T> result = comparators[comparators.length - 1];
        for (int i = comparators.length - 2; i >= 0; i++)
            result = new FallbackComparator<T>(comparators[i], result);
        return result;
    }

    /**
     *
     * @param <T>
     * @param comparators
     * @return produce a comparator that backsoff to each listed comparator
     */
    public static <T> Comparator<T> fallback(
            final List<Comparator<T>> comparators) {
        Checks.checkNotNull("comparators", comparators);
        Comparator<T> result = comparators.get(comparators.size() - 1);
        for (int i = comparators.size() - 2; i >= 0; i++)
            result = new FallbackComparator<T>(comparators.get(i), result);
        return result;
    }

    /**
     *
     * @param <T>
     */
    private static class FallbackComparator<T>
            implements Comparator<T>, Serializable {

        public static final long serialVersionUID = 1L;

        private final Comparator<T> firstComparator;

        private final Comparator<T> secondComparator;

        public FallbackComparator(
                final Comparator<T> first, final Comparator<T> second) {
            this.firstComparator = first;
            this.secondComparator = second;
        }

        @Override
        public int compare(T o1, T o2) {
            final int c = firstComparator.compare(o1, o2);
            return c != 0 ? c : secondComparator.compare(o1, o2);
        }

        public Comparator<T> getFirstComparator() {
            return firstComparator;
        }

        public Comparator<T> getSecondComparator() {
            return secondComparator;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FallbackComparator<T> other = (FallbackComparator<T>) obj;
            if (this.firstComparator != other.firstComparator
                    && (this.firstComparator == null || !this.firstComparator.equals(other.firstComparator)))
                return false;
            if (this.secondComparator != other.secondComparator
                    && (this.secondComparator == null || !this.secondComparator.equals(other.secondComparator)))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + (this.firstComparator != null
                                ? this.firstComparator.hashCode() : 0);
            hash = 83 * hash + (this.secondComparator != null
                                ? this.secondComparator.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "FallbackComparator{"
                    + "firstComparator=" + firstComparator
                    + ", secondComparator=" + secondComparator + '}';
        }

    }

    /**
     * Return a comparator that uses the natural ordering of the Comparable
     * objects.
     *
     * @param <T> Type of object being compared
     * @return comparator object that uses it's operand's natural order
     */
    public static <T extends Comparable<T>> Comparator<T> naturalOrder() {
        return new Comparator<T>() {

            @Override
            public int compare(final T a, final T b) {
                return a.compareTo(b);
            }

        };
    }

    /**
     * Return a comparator that uses the natural ordering of objects, which are
     * assumed to implement Comparable.
     *
     * This unchecked conversion is not in the least bit safe. Probably need a
     * better way to hand this.
     *
     * @param <T> type of object being compared
     * @return comparator object that uses it's operand's natural order
     */
    public static <T> Comparator<T> naturalOrderIfPossible() {
        return new Comparator<T>() {

            /**
             * @throws ClassCastException when either argument does not
             * implement Comparable
             */
            @Override
            @SuppressWarnings("unchecked")
            public int compare(final T a, final T b) throws ClassCastException {
                return ((Comparable<T>) a).compareTo(b);
            }

        };
    }

    /**
     * Produce a comparator that compares strings, and other CharSequence
     * implementations, in a case-insensitive manner.
     *
     * Note that this is usually much faster than calling toLower on each string
     * before comparison, and uses less memory.
     *
     * @param <T>
     * @return a string comparator that ignores alphabetical case
     */
    public <T extends CharSequence> Comparator<T> caseInsensitiveStringComparator() {
        return new CaseInsensitiveStringComparator<T>();
    }

    /**
     * @see #caseInsensitiveStringComparator()
     * @param <T>
     */
    private static final class CaseInsensitiveStringComparator<T extends CharSequence>
            implements Comparator<T>, Serializable {

        public static final long serialVersionUID = 1L;

        private CaseInsensitiveStringComparator() {
        }

        @Override
        public int compare(final T a, final T b) {
            final int n = a.length();
            final int m = b.length();
            for (int i = 0, j = 0; i < n && j < m; i++, j++) {
                char c1 = a.charAt(i);
                char c2 = b.charAt(j);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n - m;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }

    }
}

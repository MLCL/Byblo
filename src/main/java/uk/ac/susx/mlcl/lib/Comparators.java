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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Comparators {

    private Comparators() {
    }

    public static <T> Comparator<T> reverse(final Comparator<T> comp) {
        return (comp.getClass().equals(ReverseComparator.class))
               ? ((ReverseComparator<T>) comp).getInner()
               : new ReverseComparator<T>(comp);
    }

    @SuppressWarnings("unchecked")
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

    public static <T extends Comparable<T>> Comparator<T> naturalOrder() {
        return new Comparator<T>() {

            @Override
            public int compare(final T a, final T b) {
                return a.compareTo(b);
            }

        };
    }

    /**
     * XXX: this unchecked conversion is not in the least bit safe. Probably
     * need a better way to hand this.
     *
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> naturalOrderIfPossible() {
        return new Comparator<T>() {

            @Override
            @SuppressWarnings("unchecked")
            public int compare(final T a, final T b) {
                return ((Comparable<T>) a).compareTo(b);
            }

        };
    }

    private static class FallbackComparator<T> extends AbstractList<Comparator<T>>
            implements Comparator<T>, Serializable, List<Comparator<T>> {

        public static final long serialVersionUID = 1L;

        private final List<Comparator<T>> innerComparators;

        private FallbackComparator() {
            innerComparators = new ArrayList<Comparator<T>>();
        }

        private FallbackComparator(Iterable<Comparator<T>> comps) {
            this();
            for (Comparator<T> c : comps)
                innerComparators.add(c);
        }

        private FallbackComparator(Comparator<T>... comps) {
            this(Arrays.asList(comps));
        }

        @Override
        public int compare(T t, T t1) {
            for (Comparator<T> comparator : innerComparators) {
                int c = comparator.compare(t, t1);
                if (c != 0)
                    return c;
            }

            return 0;
        }

        @Override
        public Comparator<T> get(int index) {
            return innerComparators.get(index);
        }

        @Override
        public int size() {
            return innerComparators.size();
        }

    }

    private static class CaseInsensitiveStringComparator<T extends CharSequence>
            implements Comparator<T>, Serializable {

        public static final long serialVersionUID = 1L;

        private CaseInsensitiveStringComparator() {
        }

        @Override
        public int compare(final T string1, final T string2) {
            final int n = string1.length();
            final int m = string2.length();
            for (int i = 0, j = 0; i < n && j < m; i++, j++) {
                char c1 = string1.charAt(i);
                char c2 = string2.charAt(j);
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

    /**
     * <p>Comparator decorator that reverses the operands.</p>
     *
     * <p>When used with a sorting operation this will typically reverse the
     * sort from ascending, to descending.</p>
     *
     * <p>Serialization is supported if, and only i, the encapsulated
     * {@link Comparator} is also serializable.</p>
     *
     * @param <T>
     * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
     */
    private static class ReverseComparator<T>
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
     * A string comparator that takes strings composed of three tab delimited
     * fields. The first two are strings and the third is a double-precision
     * floating point number. The comparator orders by first field ASCII order
     * ascending, then by third field numeric order descending.
     *
     * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
     * @deprecated Should no longer be used
     */
    @Deprecated
    public static class NeighbourComparator implements Comparator<String>, Serializable {

        private static final Log LOG = LogFactory.getLog(Comparators.class);

        public static final long serialVersionUID = 1L;

        public NeighbourComparator() {
        }

        @Override
        public int compare(String string1, String string2) {
            try {

                final int n = string1.length();
                final int m = string2.length();
                int i = 0;
                int j = 0;
                while (i < n && j < m) {
                    char c1 = string1.charAt(i);
                    char c2 = string2.charAt(j);


                    if (c1 != c2) {
                        return c1 - c2;
                    }
                    if (c1 == '\t' || c2 == '\t')
                        break;
                    i++;
                    j++;
                }


                // find the ends of the Entries
                while (i < n && string1.charAt(i) != '\t') {
                    i++;
                }
                while (j < m && string2.charAt(j) != '\t') {
                    j++;
                }

                i++;
                j++;

                // find the ends of the nieghbour words
                while (i < n && string1.charAt(i) != '\t') {
                    i++;
                }
                while (j < m && string2.charAt(j) != '\t') {
                    j++;
                }
                i++;
                j++;

                double num1 = Double.parseDouble(string1.substring(i));
                double num2 = Double.parseDouble(string2.substring(j));

                return -Double.compare(num1, num2);
            } catch (RuntimeException ex) {
                if (LOG.isErrorEnabled())
                    LOG.error(
                            "Caught exception when attempting to compare "
                            + "strings \"" + string1 + "\" and \"" + string2 + "\": " + ex,
                            ex);
                throw ex;
            }
        }

    }
}

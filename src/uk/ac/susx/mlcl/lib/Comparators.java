/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.lib;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author hamish
 */
public class Comparators {

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
        return new FallbackComparator<T>(a, b);
    }

    public static <T extends Comparable<T>> Comparator<T> naturalOrder() {
        return new Comparator<T>() {

            @Override
            public int compare(final T a, final T b) {
                return a.compareTo(b);
            }

        };
    }

    public static <T> Comparator<T> naturalOrderIfPossible() {
        return new Comparator<T>() {

            @Override
            public int compare(final T a, final T b) {
                return ((Comparable<T>)a).compareTo(b);
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
     * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
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
     * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
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

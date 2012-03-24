/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.tasks;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 *
 * @param <T>
 * @author hiam20
 */
public class FallbackComparator<T> implements Comparator<T>, Serializable {

    public static final long serialVersionUID = 1L;

    private Comparator<T>[] innerComparators;

    public FallbackComparator(Comparator<T>... comps) {
        innerComparators = comps;
    }

    @SuppressWarnings("unchecked")
    public FallbackComparator(Collection<Comparator> comps) {
        this((Comparator<T>[]) comps.toArray());
    }

    public Comparator<T>[] getInnerComparators() {
        return Arrays.copyOf(innerComparators, innerComparators.length);
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
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('{');
        builder.append("inner=").append(innerComparators);
        builder.append('}');
        return builder.toString();
    }

}

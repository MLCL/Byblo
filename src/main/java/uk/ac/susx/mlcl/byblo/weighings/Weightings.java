/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.weighings;

import java.util.Arrays;
import java.util.Collection;
import uk.ac.susx.mlcl.byblo.weighings.impl.Bound;
import uk.ac.susx.mlcl.byblo.weighings.impl.NullWeighting;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Factory class from some derivative {@link Weighting} implementations.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Weightings {

    /**
     * Static utility class should not ever be instantiated.
     */
    private Weightings() {
    }

    /**
     * Hold singleton instances that will be instantiated lazily, and
     * synchronously, the first time this name-space is accessed.
     */
    private static final class InstanceHolder {

        private InstanceHolder() {
        }

        private static final Weighting NULL_WEIGHTING =
                new NullWeighting();

        private static final Weighting POSITIVE_WEIGHTING =
                new Bound(0, Double.POSITIVE_INFINITY);

        private static final Weighting NEGATIVE_WEIGHTING =
                new Bound(Double.NEGATIVE_INFINITY, 0);

    }

    /**
     * @return A weighting scheme leaves vectors intact
     */
    public static Weighting none() {
        return InstanceHolder.NULL_WEIGHTING;
    }

    /**
     * @return A weighting scheme that lower bounds all weightings to 0
     */
    public static Weighting positive() {
        return InstanceHolder.POSITIVE_WEIGHTING;
    }

    /**
     * @return A weighting scheme that upper bounds all weightings to 0
     */
    public static Weighting negative() {
        return InstanceHolder.NEGATIVE_WEIGHTING;
    }

    /**
     * @param weightings weightings to compose
     * @return weighting scheme the runs the given weightings in sequence.
     */
    public static Weighting compose(Weighting... weightings) {
        Checks.checkNotNull("weightings", weightings);
        if (weightings.length == 0)
            throw new IllegalArgumentException("weightings is empty");
        if (weightings.length == 1)
            return weightings[0];
        else {
            return new CompositeWeighting(Arrays.asList(weightings));
        }
    }

    /**
     * @param weightings weightings to compose
     * @return weighting scheme the runs the given weightings in sequence.
     */
    public static Weighting compose(Collection<Weighting> weightings) {
        Checks.checkNotNull("weightings", weightings);
        if (weightings.isEmpty())
            throw new IllegalArgumentException("weightings is empty");
        if (weightings.size() == 1)
            return weightings.iterator().next();
        else {
            return new CompositeWeighting(weightings);
        }
    }

    /**
     * Constant to aid conversion to base 2 logarithms.
     *
     * Conceptually it doesn't really matter what base is used, but 2 is the
     * standard base for most information theoretic approaches.
     *
     * TODO: Move to mlcl-lib/MathUtil
     */
    public static final double LOG_2 = Math.log(2.0);

    /**
     * Return the base 2 logarithm of the parameter v.
     *
     * TODO: Move to mlcl-lib/MathUtil
     *
     * @param v some values
     * @return logarithm of the value
     */
    public static double log2(final double v) {
        return Math.log(v) / LOG_2;
    }
}

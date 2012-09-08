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

    private Weightings() {
    }

    /**
     * @return A weighting scheme leaves vectors intact
     */
    public static Weighting none() {
        return new NullWeighting();
    }

    /**
     * @return A weighting scheme that lower bounds all weightings to 0
     */
    public static Weighting positive() {
        return new Bound(0, Double.POSITIVE_INFINITY);
    }

    /**
     * @return A weighting scheme that upper bounds all weightings to 0
     */
    public static Weighting negative() {
        return new Bound(Double.NEGATIVE_INFINITY, 0);
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
     * @param weightings  weightings to compose
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

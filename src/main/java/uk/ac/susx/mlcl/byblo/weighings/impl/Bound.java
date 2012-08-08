/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.weighings.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import uk.ac.susx.mlcl.byblo.weighings.AbstractSimpleWeighting;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * A {@link Weighting} bounds each feature weighting within a fixed range.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Bound extends AbstractSimpleWeighting implements Serializable {

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
        Checks.checkNotNaN("lowerBound", lowerBound);
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(final double upperBound) {
        Checks.checkNotNaN("upperBound", upperBound);
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
}

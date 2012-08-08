/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.weighings.impl;

import java.io.Serializable;
import uk.ac.susx.mlcl.byblo.weighings.Weighting;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * A {@link Weighting} that does nothing; simply returning the feature vector
 * unmodified.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class NullWeighting
        implements Weighting, Serializable {

    private static final long serialVersionUID = 1L;

    public NullWeighting() {
    }

    @Override
    public SparseDoubleVector apply(SparseDoubleVector f) {
        return f;
    }

    @Override
    public double getLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double getUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 41;
    }
}

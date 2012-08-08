/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.byblo.weighings;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;

/**
 * A {@link Weighting} composed of one or more other weightings that will be
 * applied in sequence.
 *
 * Note that the weightings will only be applied to the feature vectors (the
 * conditional feature distributions), not on the marginal feature distribution.
 * This latter should be updated at the end of the sequence but not during.
 * Therefore it is unsafe to have a contextual weighting (implementing
 * {@link FeatureMarginalsCarrier}) in a {@link CompositeWeighting } unless it
 * is the first element. A warning will be printed if this is detected.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class CompositeWeighting implements Weighting, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(CompositeWeighting.class);

    private final List<Weighting> childWeightings;

    public CompositeWeighting(final Collection<Weighting> childWeightings) {
        Checks.checkNotNull("weightings", childWeightings);
        if (childWeightings.isEmpty())
            throw new IllegalArgumentException("Argument weightings is empty.");
        this.childWeightings = new ArrayList<Weighting>(childWeightings);
        for (int i = 1; i < this.childWeightings.size(); i++) {
            if (this.childWeightings.get(i) instanceof FeatureMarginalsCarrier
                    && LOG.isWarnEnabled()) {
                LOG.warn(MessageFormat.format(
                        "Composite weighting sequence contains contextual "
                        + "weighting ({0}) outside of the first position ({1})."
                        + " This is unlikely to produce the correct result "
                        + "because marginal distributions have not been updated.",
                        this.childWeightings.get(i).toString(), 0));
            }
        }
    }

    public Weighting[] getChildWeightings() {
        return childWeightings.toArray(new Weighting[0]);
    }

    @Override
    public SparseDoubleVector apply(final SparseDoubleVector from) {
        SparseDoubleVector vec = from;
        for (Weighting w : childWeightings) {
            vec = w.apply(vec);
        }
        return vec;
    }

    @Override
    public double getLowerBound() {
        return childWeightings.get(childWeightings.size() - 1).getLowerBound();
    }

    @Override
    public double getUpperBound() {
        return childWeightings.get(childWeightings.size() - 1).getUpperBound();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append('{');
        boolean first = true;
        for (Weighting child : childWeightings) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(child.toString());
        }
        sb.append('}');
        return sb.toString();
    }
}

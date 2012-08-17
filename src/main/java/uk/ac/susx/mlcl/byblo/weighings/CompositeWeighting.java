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
 * <p/>
 * Note that the weightings will only be applied to the feature vectors (the
 * conditional feature distributions), not on the marginal feature distribution.
 * This latter should be updated at the end of the sequence but not during.
 * Therefore it is unsafe to have a contextual weighting (implementing
 * {@link FeatureMarginalsCarrier}) in a {@link CompositeWeighting } unless it
 * is the first element. A warning will be printed if this is detected.
 * <p/>
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
                LOG.warn(MessageFormat.
                        format(
                        "Composite weighting sequence contains contextual "
                        + "weighting ({0}) outside of the first position ({1})."
                        + " This is unlikely to produce the correct result "
                        + "because marginal distributions have not been updated.",
                        this.childWeightings.get(i).toString(), 0));
            }
        }
    }

    public Weighting[] getChildWeightings() {
        return childWeightings.toArray(new Weighting[childWeightings.size()]);
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

    protected boolean equals(CompositeWeighting that) {
        if (this.childWeightings != that.childWeightings
                && (this.childWeightings == null
                    || !this.childWeightings.equals(that.childWeightings)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((CompositeWeighting) obj);
    }

    @Override
    public int hashCode() {
        return 73 * 79 + (this.childWeightings != null
                          ? this.childWeightings.hashCode()
                          : 0);
    }
}

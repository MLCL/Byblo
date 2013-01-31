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

import static com.google.common.base.Preconditions.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link AbstractContextualWeighting} is an abstract super class that combines an
 * {@link AbstractElementwiseWeighting} scheme with the availability of feature marginal scores via
 * {@link FeatureMarginalsCarrier}.
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@CheckReturnValue
public abstract class AbstractContextualWeighting
        extends AbstractElementwiseWeighting
        implements FeatureMarginalsCarrier {

    @Nullable
    private MarginalDistribution featureMarginals = null;

    protected AbstractContextualWeighting() {
    }

    @Override
    public MarginalDistribution getFeatureMarginals() {
        checkState(featureMarginals != null, "marginals requested before they where set.");
        return featureMarginals;
    }

    @Override
    public void setFeatureMarginals(@Nonnull MarginalDistribution featureMarginals) {
        this.featureMarginals = checkNotNull(featureMarginals, "featureMarginals");
    }

    @Override
    public boolean isFeatureMarginalsSet() {
        return featureMarginals != null;
    }

    protected boolean equals(AbstractContextualWeighting other) {
        return !(this.featureMarginals != other.featureMarginals
                 && (this.featureMarginals == null
                     || !this.featureMarginals.equals(
                     other.featureMarginals)));
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || !(obj == null || getClass() != obj.getClass()) && equals((AbstractContextualWeighting) obj);
    }

    @Override
    public int hashCode() {
        return 89 * 7 + (this.featureMarginals != null
                         ? this.featureMarginals.hashCode() : 0);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[featureMarginals="
                + getFeatureMarginals() + ", bounds=("
                + getLowerBound() + ", " + getUpperBound()
                + ")]";
    }
}

/*
 * Copyright (c) 2010-2011, University of Sussex
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
package uk.ac.susx.mlcl.byblo.io;

import com.google.common.base.Objects;
import java.io.Serializable;
import uk.ac.susx.mlcl.lib.ObjectIndex;

/**
 * <tt>EntryFeature</tt> objects represent a single instance of an 
 * entry/feature pair, i.e the occurrence of a feature with and entry. Typically
 * this will be associated with a frequency use to estimate the likelihood of
 * the feature in the entries context.
 * 
 * <p>Instances of <tt>EntryFeature</tt> are immutable.<p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class EntryFeature
        implements Serializable, Comparable<EntryFeature> {

    private static final long serialVersionUID = 1L;

    private int entryId;

    private int featureId;

    public EntryFeature(final int entryId, final int featureId) {
        this.entryId = entryId;
        this.featureId = featureId;
    }

    /**
     * Constructor used during de-serialization.
     */
    protected EntryFeature() {
    }

    public final int getEntryId() {
        return entryId;
    }

    public final int getFeatureId() {
        return featureId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("entryId", entryId).add("featureId", featureId).toString();
    }

    public String toString(ObjectIndex<String> entryIndex,
            ObjectIndex<String> featureIndex) {
        return Objects.toStringHelper(this).
                add("entry", entryIndex.get(entryId)).
                add("feature", featureIndex.get(featureId)).
                toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((EntryFeature) obj);
    }

    public boolean equals(EntryFeature other) {
        return this.getEntryId() == other.getEntryId()
                && this.getFeatureId() == other.getFeatureId();
    }

    @Override
    public int hashCode() {
        return 47 * (47 * 3 + this.entryId) + this.featureId;
    }

    @Override
    public int compareTo(EntryFeature that) {
        return this.getEntryId() != that.getEntryId()
                ? this.getEntryId() - that.getEntryId()
                : this.getFeatureId() - that.getFeatureId();
    }
}

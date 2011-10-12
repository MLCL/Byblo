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
import uk.ac.susx.mlcl.lib.ObjectIndex;

/**
 * <tt>WeightedEntryFeatureRecord</tt> objects represent a single instance of an 
 * entry/feature pair, associated with a weight. Typically the weight is a 
 * frequency use to estimate the likelihood of the feature in the entries 
 * context.
 *
 * <p>Instances of <tt>WeightedEntryFeatureRecord</tt> are immutable.<p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class WeightedEntryFeatureRecord
        extends EntryFeatureRecord {

    private static final long serialVersionUID = 1L;

    private double weight;

    public WeightedEntryFeatureRecord(
            final int entryId, final int featureId, final double weight) {
        super(entryId, featureId);
        this.weight = weight;
    }

    /**
     * Constructor used during de-serialization.
     */
    protected WeightedEntryFeatureRecord() {
        super();
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("entryId", getEntryId()).
                add("featureId", getFeatureId()).
                add("weight", getWeight()).
                toString();
    }

    public String toString(ObjectIndex<String> entryIndex,
            ObjectIndex<String> featureIndex) {
        return Objects.toStringHelper(this).
                add("entryId", entryIndex.get(getEntryId())).
                add("featureId", featureIndex.get(getFeatureId())).
                add("weight", getWeight()).
                toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * 
     * <p>Note that only the <tt>entryId</tt> and <tt>featureId</tt> fields are
     * used for equality. I.e two objects with the same ids, but differing 
     * weights <em>will</em> be consider equal.</p>
     * 
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals((EntryFeatureRecord) obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

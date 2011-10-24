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

/**
 * <tt>Feature</tt> objects represent a single instance of a feature used
 * to measure the similarity between thesaurus entries. A <tt>Feature</tt> 
 * consists of a <tt>featureId</tt>, and a <tt>weight</tt> estimated from the 
 * source corpus. The weighting is usually the features frequency, but it could 
 * be anything.
 * 
 * <p>Instances of <tt>Feature</tt> are immutable.<p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Feature 
    implements Serializable, Comparable<Feature> {

    private static final long serialVersionUID = 63617210012669024L;

    private int id;

    public Feature(final int featureId) {
        this.id = featureId;
    }

    /**
     * Constructor used during de-serialization.
     */
    protected Feature() {
    }


    public final int getId() {
        return id;
    }


    /**
     * Indicates whether some other object is "equal to" this one.
     * 
     * <p>Note that only the <tt>featureId</tt> field is used for equality. I.e 
     * two objects with the same <tt>featureId</tt>, but differing weights 
     * <em>will</em> be consider equal.</p>
     * 
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((Feature) obj);
    }
    
    public boolean equals(Feature other) {
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("id", id).toString();
    }

    @Override
    public int compareTo(Feature that) {
        return this.getId() - that.getId();
    }
}

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
package uk.ac.susx.mlcl.lib.collect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * An IdKey represents some object which has been assigned a unique id. It
 * contains the integer id value and the object itself. The id can then be used
 * for all reference purposes without having to hold the object itself in
 * memory (assuming of course an index has been stored.)
 *
 * It is assumed that a genuinely unique id has been assigned. All operations
 * comparing instances of this object are done only with respect to the id.
 *
 * Immutable
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T>
 */
public class Indexed<T> extends AbstractIndexed
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private final T value;

    /**
     * Cloning constructor.
     *
     * @param other IndexEntry to clone.
     */
    protected Indexed(final Indexed<T> other) {
        super(other);
        if (other.value == null)
            throw new NullPointerException("other.value == null");
        this.value = other.value;
    }

    /**
     * Full dependancy constructor.
     *
     * @param key    the unique integer id of this entry
     * @param value the valye of this entry
     */
    public Indexed(final int key, final T value) {
        super(key);
        if (value == null)
            throw new NullPointerException("value == null");
        this.value = value;
    }

    public final T value() {
        return value;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return this.key() == ((Indexed<?>) obj).key();
    }

    @Override
    public final int hashCode() {
        return this.key();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[id=").append(key());
        sb.append(", value=").append(value()).append(']');
        return sb.toString();
    }

    @Override
    public Indexed<T> clone() {
        return new Indexed<T>(this);
    }

    @Override
    protected final Object writeReplace() {
        return new Serializer<T>(this);
    }

    private static final class Serializer<T> extends AbstractIndexed.Serializer implements Externalizable {

        private static final long serialVersionUID = 1;

        private T value;

        public Serializer() {
        }

        public Serializer(final Indexed<T> entry) {
            super(entry);
            this.value = entry.value;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeObject(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            this.value = (T) in.readObject();
        }

        @Override
        protected final Object readResolve() {
            return new Indexed<T>(key, value);
        }
    }
}

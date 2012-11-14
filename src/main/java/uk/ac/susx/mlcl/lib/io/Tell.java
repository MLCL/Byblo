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
package uk.ac.susx.mlcl.lib.io;

import uk.ac.susx.mlcl.lib.Checks;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Class used to store information about a position in randomly accessible source, such that the source can return to
 * that position given only the data stored in Tell.
 * <p/>
 * this can be rather complex process since a particular Seekable Source may consist of a hierarchy of objects each
 * contributing some portion of the reading process. For example the base level may be FileChannel, which is buffered
 * into ByteBuffers, which needs to be decoded into characters, which tokenized by a lexer, follewed by parsing,
 * filtering, other processing. At each stage some information must be stored to return to some stored position.
 * <p/>
 * This implementation solves this by implementing an extremely light weight stack of data, which can be pushed to as
 * the Tell is passed up the hierarchy, then can be popped again as the position is passed back down.
 * <p/>
 * <p/>
 * Note that the class type of each object pushed is also stored to introduce a modicum of type safety.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Tell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> type;

    private final Object value;

    @Nullable
    private final Tell next;

    private Tell(Class<?> type, Object value, @Nullable Tell next) {
        Checks.checkNotNull(type);
        this.type = type;
        this.value = value;
        this.next = next;
    }

    public Tell(Class<?> type, Object value) {
        this(type, value, null);
    }

    /**
     * Cloning constructor, produces a shallow copy of the parameterised <code>Tell</code>.
     *
     * @param other object to clone
     */
    public Tell(Tell other) {
        this(other.type, other.value, other.next);

    }

    public final Tell next() {
        return next;
    }

    public final boolean hasNext() {
        return next != null;
    }

    public final <T> T value(Class<T> type) {
        return type.cast(value);
    }

    public final <T> Tell push(Class<T> type, T value) {
        return new Tell(type, value, this);
    }

    private Class<?> getType() {
        return type;
    }

    private Object getValue() {
        return value;
    }

    public boolean equals(Tell other) {
        if (this.type != other.getType() && (this.type == null || !this.type.equals(
                other.getType())))
            return false;
        if (this.value != other.getValue() && (this.value == null || !this.value.equals(other.getValue())))
            return false;
        if (this.next != other.next() && (this.next == null || !this.next.equals(
                other.next())))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && equals((Tell) obj);
    }

    @Override
    public int hashCode() {
        return 97 * (97 * (97 * 7
                + (this.type != null ? this.type.hashCode() : 0))
                + (this.value != null ? this.value.hashCode() : 0))
                + (this.next != null ? this.next.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Tell{" + "type=" + type.getSimpleName() + ", value=" + value + ", inner=" + next + '}';
    }

}

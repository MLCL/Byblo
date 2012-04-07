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

import java.io.Serializable;
import uk.ac.susx.mlcl.lib.Checks;

/**
 *
 * @author hamish
 */
public final class Tell implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> type;

    private final Object value;

    private final Tell next;

    private Tell(Class<?> type, Object value, Tell next) {
        Checks.checkNotNull(type);
        this.type = type;
        this.value = value;
        this.next = next;
    }

    public Tell(Class<?> type, Object value) {
        this(type, value, null);
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

    public boolean equals(Tell other) {
        if (this.type != other.type && (this.type == null || !this.type.equals(
                                        other.type)))
            return false;
        if (this.value != other.value && (this.value == null || !this.value.
                                          equals(other.value)))
            return false;
        if (this.next != other.next && (this.next == null || !this.next.equals(
                                        other.next)))
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

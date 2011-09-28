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
import java.text.ParseException;
import java.util.Comparator;

/**
 * Immutable object that holds a key/value pair
 * 
 * @author Hamish Morgan (Hamish.Morgan@sussex.ac.uk)
 */
public class IntEntry extends AbstractEntry implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    private final int value;

    protected IntEntry(IntEntry other) {
        super(other);
        this.value = other.value;
    }

    public IntEntry(int key, int value) {
        super(key);
        this.value = value;
    }

    public final int value() {
        return value;
    }

    @Override
    public String toString() {
        return key() + ":" + value;
    }

    public static IntEntry valueOf(final String str) throws ParseException {
        if(str == null)
            throw new NullPointerException("str == null");
        final int delim = str.indexOf(':');
        if (delim == -1)
            throw new ParseException("Delimiter not found.", delim);
        final int key = Integer.valueOf(str.substring(0, delim));
        final int value = Integer.valueOf(str.substring(delim + 1));
        return new IntEntry(key, value);
    }

    @Override
    protected IntEntry clone() {
        return new IntEntry(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final IntEntry other = (IntEntry) obj;
        return this.key() == other.key()
                ; //&& this.value == other.value;
    }

    @Override
    public int hashCode() {
        return 37 * (37 * 7 + this.key());// + this.value;
    }

    @Override
    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            extends AbstractEntry.Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;
        private int value;

        public Serializer() {
        }

        public Serializer(final IntEntry entry) {
            super(entry);
            this.value = entry.value;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(value);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            value = in.readInt();
        }

        @Override
        protected final Object readResolve() {
            return new IntEntry(key, value);
        }
    }
    public static final Comparator<IntEntry> KEY_VALUE_ASC = new Comparator<IntEntry>() {

        @Override
        public final int compare(final IntEntry a, final IntEntry b) {
            return a.key() < b.key() ? -1
                    : a.key() > b.key() ? 1
                    : a.value < b.value ? -1
                    : a.value > b.value ? 1
                    : 0;
        }
    };
    public static final Comparator<IntEntry> KEY_VALUE_DESC = new Comparator<IntEntry>() {

        @Override
        public final int compare(final IntEntry a, final IntEntry b) {
            return a.key() < b.key() ? 1
                    : a.key() > b.key() ? -1
                    : a.value < b.value ? 1
                    : a.value > b.value ? -1
                    : 0;
        }
    };
    public static final Comparator<IntEntry> VALUE_ASC = new Comparator<IntEntry>() {

        @Override
        public final int compare(final IntEntry a, final IntEntry b) {
            return a.value < b.value ? -1
                    : a.value > b.value ? 1
                    : 0;
        }
    };
    public static final Comparator<IntEntry> VALUE_DESC = new Comparator<IntEntry>() {

        @Override
        public final int compare(final IntEntry a, final IntEntry b) {
            return a.value < b.value ? 1
                    : a.value > b.value ? -1
                    : 0;
        }
    };
}

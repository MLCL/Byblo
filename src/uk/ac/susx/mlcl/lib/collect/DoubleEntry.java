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
 * @version 22nd October 2010
 */
public class DoubleEntry extends AbstractEntry implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    private final double value;

    protected DoubleEntry(DoubleEntry other) {
        super(other);
        this.value = other.value;
    }

    public DoubleEntry(int key, double value) {
        super(key);
        this.value = value;
    }

    public final double value() {
        return value;
    }

    @Override
    public String toString() {
        return key() + ":" + value;
    }

    public static DoubleEntry valueOf(final String str) throws ParseException {
        if(str == null)
            throw new NullPointerException("str == null");
        final int delim = str.indexOf(':');
        if (delim == -1)
            throw new ParseException("Delimiter not found.", delim);
        final int key = Integer.valueOf(str.substring(0, delim));
        final double value = Double.valueOf(str.substring(delim + 1));
        return new DoubleEntry(key, value);
    }

    @Override
    protected DoubleEntry clone() {
        return new DoubleEntry(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final DoubleEntry other = (DoubleEntry) obj;
        return this.key() == other.key()
                ;//&& this.value == other.value;
    }

    @Override
    public int hashCode() {
        return 37 * (37 * 7 + this.key());
//        int hash = 7;
//        hash = 47 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.
//                doubleToLongBits(this.value) >>> 32));
//        return hash;
    }

    @Override
    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            extends AbstractEntry.Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;
        private double value;

        public Serializer() {
        }

        public Serializer(final DoubleEntry entry) {
            super(entry);
            this.value = entry.value;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeDouble(value);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            value = in.readDouble();
        }

        @Override
        protected final Object readResolve() {
            return new DoubleEntry(key, value);
        }
    }
    public static final Comparator<DoubleEntry> KEY_VALUE_ASC = new Comparator<DoubleEntry>() {

        @Override
        public final int compare(final DoubleEntry a, final DoubleEntry b) {
            return a.key() < b.key() ? -1
                    : a.key() > b.key() ? 1
                    : Double.compare(a.value, b.value);
        }
    };
    public static final Comparator<DoubleEntry> KEY_VALUE_DESC = new Comparator<DoubleEntry>() {

        @Override
        public final int compare(final DoubleEntry a, final DoubleEntry b) {
            return a.key() < b.key() ? 1
                    : a.key() > b.key() ? -1
                    : -Double.compare(a.value, b.value);
        }
    };
    public static final Comparator<DoubleEntry> VALUE_ASC = new Comparator<DoubleEntry>() {

        @Override
        public final int compare(final DoubleEntry a, final DoubleEntry b) {
            return Double.compare(a.value, b.value);
        }
    };
    public static final Comparator<DoubleEntry> VALUE_DESC = new Comparator<DoubleEntry>() {

        @Override
        public final int compare(final DoubleEntry a, final DoubleEntry b) {
            return -Double.compare(a.value, b.value);
        }
    };
}

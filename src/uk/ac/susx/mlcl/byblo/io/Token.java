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
package uk.ac.susx.mlcl.byblo.io;

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import com.google.common.base.Objects;
import java.io.*;
import java.util.Comparator;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;

/**
 * <tt>Token</tt> objects represent a single instance of an indexed string.
 *
 * <p>Instances of <tt>Token</tt> are immutable.<p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class Token implements Serializable, Comparable<Token>, Cloneable {

    private static final long serialVersionUID = 2L;

    private static final Comparator<Token> NATURAL_ORDER = indexOrder();

    private final int id;

    public Token(final int id) {
        this.id = id;
    }

    protected Token(final Token other) {
        this.id = other.id();
    }

    public int id() {
        return id;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Note that only the <tt>entryId</tt> field is used for equality. I.e
     * two objects with the same <tt>entryId</tt>, but differing weights
     * <em>will</em> be consider equal.</p>
     *
     * @param obj the reference object with which to compare.
     * @return
     * <code>true</code> if this object is the same as the obj argument;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return equals((Token) obj);
    }

    public boolean equals(Token other) {
        return this.id() == other.id();
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("id", id).
                toString();
    }

    public String toString(Enumerator<String> stringIndex) {
        return Objects.toStringHelper(this).
                add("id", id).
                add("string", stringIndex.valueOf(id)).
                toString();
    }

    @Override
    public int compareTo(Token that) {
        return NATURAL_ORDER.compare(this, that);
    }

    @Override
    protected Token clone() throws CloneNotSupportedException {
        return new Token(this);
    }

    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;

        private Token token;

        public Serializer() {
        }

        public Serializer(final Token token) {
            if (token == null)
                throw new NullPointerException("token == null");
            this.token = token;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(token.id());
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            final int id = in.readInt();
            token = new Token(id);
        }

        protected final Object readResolve() {
            return token;
        }

    }

    public static Comparator<Token> indexOrder() {
        return new Comparator<Token>() {

            @Override
            public int compare(final Token a, final Token b) {
                return a.id() - b.id();
            }

        };
    }

    public static Comparator<Token> stringOrder(
            final SingleEnumerating idx) {
        return new Comparator<Token>() {

            @Override
            public int compare(final Token a, final Token b) {
                try {
                    String s1 = idx.getEnumerator().valueOf(a.id());
                    String s2 = idx.getEnumerator().valueOf(b.id());

                    assert s1 != null : "s1 ia null";
                    assert s2 != null : "s2 ia null";
                    return s1.compareTo(s2);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        };
    }

}

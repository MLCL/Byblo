/*
 * Copyright (c) 2010-2013, University of Sussex
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
import com.google.common.base.Predicate;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.lib.Checks;

import java.io.*;
import java.util.Comparator;

/**
 * TokenPair holds the unique ids of two indexed strings.
 * <p/>
 * <p>Instances of <tt>TokenPair</tt> are immutable.<p>
 * <p/>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class TokenPair implements
        Comparable<TokenPair>, Serializable, Cloneable {

    private static final long serialVersionUID = 3L;

    private static final Comparator<TokenPair> NATURAL_ORDER = indexOrder();

    /**
     * Indexed identifier of the first entry.
     */
    private final int id1;

    /**
     * Indexed identifier of the second entry.
     */
    private final int id2;

    /**
     * Constructor used during cloning. Sub-classes should implement a similar
     * constructor and call this one.
     * <p/>
     *
     * @param that
     * @throws NullPointerException     if the argument is null
     * @throws IllegalArgumentException
     */
    private TokenPair(final TokenPair that)
            throws NullPointerException, IllegalArgumentException {
        Checks.checkNotNull("that", that);
        this.id1 = that.id1();
        this.id2 = that.id2();
    }

    /**
     * Construct a new pair using the given arguments.
     * <p/>
     *
     * @param id1 Indexed identifier of the first item.
     * @param id2 Indexed identifier of the second item.
     */
    public TokenPair(final int id1, final int id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    /**
     * @return Indexed identifier of the first item.
     */
    public final int id1() {
        return id1;
    }

    /**
     * @return Indexed identifier of the second item.
     */
    public final int id2() {
        return id2;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("id1", id1).add("id2", id2).
                toString();
    }

    public String toString(Enumerator<String> stringIndex1,
                           Enumerator<String> stringIndex2) {
        return Objects.toStringHelper(this).
                add("1", stringIndex1.valueOf(id1)).add("2", stringIndex2.
                valueOf(
                        id2)).
                toString();
    }

    /**
     * Comparator that orders two pairs: first by the id1, then by id2.
     * <p/>
     * It is entirely consistent with equals() and hashCode().
     * <p/>
     *
     * @param that object to compare to
     * @return -1 if this < that, +1 if this > that, 0 otherwise
     * @throws NullPointerException if the argument is null
     */
    @Override
    public int compareTo(final TokenPair that)
            throws NullPointerException {
        Checks.checkNotNull("that", that);
        return NATURAL_ORDER.compare(this, that);
    }

    @Override
    protected TokenPair clone() throws CloneNotSupportedException {
        return new TokenPair(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && compareTo((TokenPair) obj) == 0;
    }

    @Override
    public int hashCode() {
        return 13 * this.id1 + this.id2;
    }

    final Object writeReplace() {
        return new Serializer(this);
    }

    public static final class Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;

        private TokenPair pair;

        public Serializer() {
        }

        Serializer(final TokenPair pair) {
            if (pair == null) {
                throw new NullPointerException("pair == null");
            }
            this.pair = pair;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(pair.id1());
            out.writeInt(pair.id2());
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            final int x_id = in.readInt();
            final int y_id = in.readInt();
            pair = new TokenPair(x_id, y_id);
        }

        final Object readResolve() {
            return pair;
        }
    }

    public static Predicate<TokenPair> identity() {
        return new Predicate<TokenPair>() {
            @Override
            public boolean apply(TokenPair input) {
                return input != null && input.id1() == input.id2();
            }

            @Override
            public String toString() {
                return "identity";
            }
        };
    }

    public static Comparator<TokenPair> indexOrder() {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(TokenPair a, TokenPair b) {
                int c = a.id1() - b.id1();
                return c != 0 ? c : a.id2() - b.id2();
            }
        };
    }

    public static Comparator<TokenPair> firstIndexOrder() {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(TokenPair a, TokenPair b) {
                return a.id1() - b.id1();
            }
        };
    }

    public static Comparator<TokenPair> secondIndexOrder() {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(TokenPair a, TokenPair b) {
                return a.id2() - b.id2();
            }
        };
    }

    public static Comparator<TokenPair> stringOrder(
            final DoubleEnumerating idx) {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(final TokenPair a, final TokenPair b) {
                try {
                    final Enumerator<String> en1 = idx.getEntryEnumerator();
                    int c = en1.valueOf(a.id1()).compareTo(en1.valueOf(b.id1()));
                    final Enumerator<String> en2 = idx.getFeatureEnumerator();
                    return c != 0 ? c
                            : en2.valueOf(a.id2()).compareTo(en2.
                            valueOf(b.id2()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public static Comparator<TokenPair> firstStringOrder(
            final SingleEnumerating idx) {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(final TokenPair a, final TokenPair b) {
                try {
                    final Enumerator<String> en = idx.getEnumerator();
                    return en.valueOf(a.id1()).compareTo(en.valueOf(b.id1()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public static Comparator<TokenPair> secondStringOrder(
            final SingleEnumerating idx) {
        return new Comparator<TokenPair>() {
            @Override
            public int compare(final TokenPair a, final TokenPair b) {
                try {
                    final Enumerator<String> en = idx.getEnumerator();
                    return en.valueOf(a.id2()).compareTo(en.valueOf(b.id2()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }
}

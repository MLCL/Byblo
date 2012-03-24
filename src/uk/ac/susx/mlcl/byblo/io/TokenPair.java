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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import java.io.*;
import java.util.Comparator;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;

/**
 * TokenPair holds the unique ids of two indexed strings.
 *
 * <p>Instances of <tt>TokenPair</tt> are immutable.<p>
 *
 * @author Hamish Morgan
 */
public class TokenPair implements
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
     *
     * @throws NullPointerException if the argument is null
     */
    protected TokenPair(final TokenPair that)
            throws NullPointerException, IllegalArgumentException {
        Checks.checkNotNull("that", that);
        this.id1 = that.id1();
        this.id2 = that.id2();
    }

    /**
     * Construct a new pair using the given arguments.
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
                add("1", stringIndex1.value(id1)).add("2", stringIndex2.value(
                id2)).
                toString();
    }

    /**
     * Comparator that orders two pairs: first by the id1, then by id2.
     *
     * It is entirely consistent with equals() and hashCode().
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

    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;

        private TokenPair pair;

        public Serializer() {
        }

        public Serializer(final TokenPair pair) {
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

        protected final Object readResolve() {
            return pair;
        }
    }

    public static Predicate<TokenPair> identity() {
        return new Predicate<TokenPair>() {

            @Override
            public boolean apply(TokenPair input) {
                return input.id1() == input.id2();
            }

            @Override
            public String toString() {
                return "identity";
            }
        };
    }
//
//    public static Comparator<Weighted<EntryPair>> ASYMMETRIC_COMPARATOR =
//            new Comparator<Weighted<EntryPair>>() {
//
//                @Override
//                public int compare(Weighted<EntryPair> a,
//                        Weighted<EntryPair> b) {
//                    return a.get().entry1Id < b.get().entry1Id ? -1
//                            : a.get().entry1Id > b.get().entry1Id ? 1
//                            : a.get().entry2Id < b.get().entry2Id ? -1
//                            : a.get().entry2Id > b.get().entry2Id ? 1
//                            : Double.compare(a.getWeight(), b.getWeight());
//                }
//            };
//
//    public static Comparator<Weighted<EntryPair>> SYMMETRIC_COMPARATOR =
//            new Comparator<Weighted<EntryPair>>() {
//
//                @Override
//                public int compare(Weighted<EntryPair> a,
//                        Weighted<EntryPair> b) {
//                    final int x1 = Math.min(a.get().entry1Id, a.get().entry2Id);
//                    final int x2 = Math.min(b.get().entry1Id, b.get().entry2Id);
//                    final int y1 = Math.max(a.get().entry1Id, a.get().entry2Id);
//                    final int y2 = Math.max(b.get().entry1Id, b.get().entry2Id);
//                    return x1 < x2 ? -1
//                            : x1 > x2 ? 1
//                            : y1 < y2 ? -1
//                            : y1 > y2 ? 1
//                            : Double.compare(a.getWeight(), b.getWeight());
//                }
//            };
//
//    public static final Comparator<TokenPair> ASYMMETRIC_KEY_COMPARATOR =
//            new Comparator<TokenPair>() {
//
//                @Override
//                public final int compare(final TokenPair a, final TokenPair b) {
//                    return a.id1() < b.id1() ? -1
//                            : a.id1() > b.id1() ? 1
//                            : a.id2() < b.id2() ? -1
//                            : a.id2() > b.id2() ? 1
//                            : 0;
//                }
//
//                @Override
//                public String toString() {
//                    return "ASYMMETRIC_KEY_COMPARATOR";
//                }
//            };
//
//    public static final Comparator<TokenPair> SYMMETRIC_KEY_COMPARATOR =
//            new Comparator<TokenPair>() {
//
//                @Override
//                public final int compare(final TokenPair a, final TokenPair b) {
//                    final int x1 = Math.min(a.id1(), a.id2());
//                    final int x2 = Math.min(b.id1(), b.id2());
//                    final int y1 = Math.max(a.id1(), a.id2());
//                    final int y2 = Math.max(b.id1(), b.id2());
//                    return x1 < x2 ? -1
//                            : x1 > x2 ? 1
//                            : y1 < y2 ? -1
//                            : y1 > y2 ? 1
//                            : 0;
//                }
//
//                @Override
//                public String toString() {
//                    return "SYMMETRIC_KEY_COMPARATOR";
//                }
//            };
////

    public static Comparator<TokenPair> indexOrder() {
        return new Comparator<TokenPair>() {

            @Override
            public int compare(TokenPair a, TokenPair b) {
                int c = a.id1() - b.id1();
                return c != 0 ? c : a.id2() - b.id2();
            }
        };
    }

    public static Comparator<TokenPair> stringOrder(
            final Function<Integer, String> encoder1,
            final Function<Integer, String> encoder2) {
        return new Comparator<TokenPair>() {

            @Override
            public int compare(final TokenPair a, final TokenPair b) {
                int c = encoder1.apply(a.id1()).compareTo(
                        encoder1.apply(b.id1()));
                return c != 0 ? c : encoder2.apply(a.id2()).compareTo(
                        encoder2.apply(b.id2()));
            }
        };
    }
}

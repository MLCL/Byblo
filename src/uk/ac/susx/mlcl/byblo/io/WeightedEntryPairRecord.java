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
import com.google.common.base.Predicate;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Comparator;

/**
 * WeightedEntryPairRecord hold the unique ids of two entries, along with a value 
 * indicating the weight of connections between them.
 *
 * <p>Instances of <tt>WeightedEntryPairRecord</tt> are immutable.<p>
 *
 * @author Hamish Morgan
 */
public class WeightedEntryPairRecord implements
        Comparable<WeightedEntryPairRecord>, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * Indexed identifier of the first entry.
     */
    private final int entry1Id;

    /**
     * Indexed identifier of the second entry.
     */
    private final int entry2Id;

    /**
     * The weight of connection between the two entry. Can be proximity,
     * similarity, distance or whatever is required.
     */
    private final double weight;

    /**
     * Constructor used during cloning. Sub-classes should implement a similar
     * constructor and call this one.
     *
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if argument weight is NaN
     */
    protected WeightedEntryPairRecord(final WeightedEntryPairRecord that)
            throws NullPointerException, IllegalArgumentException {
        if (that == null)
            throw new NullPointerException("that == null");
        if (Double.isNaN(that.weight))
            throw new IllegalArgumentException("proximity == NaN");

        this.entry1Id = that.entry1Id;
        this.entry2Id = that.entry2Id;
        this.weight = that.weight;
    }

    /**
     * Construct a new weighted pair using the given arguments.
     *
     * @param entry1Id Indexed identifier of the first item.
     * @param entry2Id Indexed identifier of the second item.
     * @param weight The weight of connection between the two items.
     * @throws IllegalArgumentException if argument weight is NaN
     */
    public WeightedEntryPairRecord(final int entry1Id, final int entry2Id,
            final double weight)
            throws IllegalArgumentException {
        if (Double.isNaN(weight))
            throw new IllegalArgumentException("weight == NaN");

        this.entry1Id = entry1Id;
        this.entry2Id = entry2Id;
        this.weight = weight;
    }

    /**
     * @return Indexed identifier of the first item.
     */
    public final int getEntry1Id() {
        return entry1Id;
    }

    /**
     * @return Indexed identifier of the second item.
     */
    public final int getEntry2Id() {
        return entry2Id;
    }

    /**
     * @return The weight of connection between the two items.
     */
    public final double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).
                add("id1", entry1Id).add("id2", entry2Id).
                add("weight", weight).toString();
    }

    /**
     * Comparator that orders two pairs: first by the entry1id, second by 
     * entry2id, and finally by weight.
     *
     * It is entirely consistent with equals() and hashCode().
     *
     * @param that object to compare to
     * @return -1 if this < that, +1 if this > that, 0 otherwise
     * @throws NullPointerException if the argument is null
     */
    @Override
    public int compareTo(final WeightedEntryPairRecord that)
            throws NullPointerException {
        if (that == null)
            throw new NullPointerException("that == null");
        return ASYMMETRIC_COMPARATOR.compare(this, that);
    }

    @Override
    protected WeightedEntryPairRecord clone() {
        return new WeightedEntryPairRecord(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && compareTo((WeightedEntryPairRecord) obj) == 0;
    }

    @Override
    public int hashCode() {
        final long bits = Double.doubleToLongBits(this.weight);
        return 13 * (13 * (13 * 3 + this.entry1Id) + this.entry2Id)
                + (int) (bits ^ (bits >>> 32));
    }

    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;

        private WeightedEntryPairRecord pair;

        public Serializer() {
        }

        public Serializer(final WeightedEntryPairRecord pair) {
            if (pair == null)
                throw new NullPointerException("pair == null");
            this.pair = pair;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(pair.entry1Id);
            out.writeInt(pair.entry2Id);
            out.writeDouble(pair.weight);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            final int x_id = in.readInt();
            final int y_id = in.readInt();
            final double weight = in.readDouble();
            if (Double.isNaN(weight))
                throw new IllegalArgumentException("proximity == NaN");
            pair = new WeightedEntryPairRecord(x_id, y_id, weight);
        }

        protected final Object readResolve() {
            return pair;
        }
    }

    public static Predicate<WeightedEntryPairRecord> identity() {
        return new Predicate<WeightedEntryPairRecord>() {

            @Override
            public boolean apply(WeightedEntryPairRecord input) {
                return input.getEntry1Id() == input.getEntry2Id();
            }
        };
    }

    public static Predicate<WeightedEntryPairRecord> similarityGTE(
            final double minSimilarity) {
        return new Predicate<WeightedEntryPairRecord>() {

            @Override
            public boolean apply(WeightedEntryPairRecord pair) {
                return (pair.getWeight() >= minSimilarity);
            }
        };
    }

    public static Predicate<WeightedEntryPairRecord> similarityLTE(
            final double maxSimilarity) {
        return new Predicate<WeightedEntryPairRecord>() {

            @Override
            public boolean apply(WeightedEntryPairRecord pair) {
                return (pair.getWeight() <= maxSimilarity);
            }
        };
    }

    public static Comparator<WeightedEntryPairRecord> ASYMMETRIC_COMPARATOR =
            new Comparator<WeightedEntryPairRecord>() {

                @Override
                public int compare(WeightedEntryPairRecord a,
                        WeightedEntryPairRecord b) {
                    return a.entry1Id < b.entry1Id ? -1
                            : a.entry1Id > b.entry1Id ? 1
                            : a.entry2Id < b.entry2Id ? -1
                            : a.entry2Id > b.entry2Id ? 1
                            : Double.compare(a.weight, b.weight);
                }
            };

    public static Comparator<WeightedEntryPairRecord> SYMMETRIC_COMPARATOR =
            new Comparator<WeightedEntryPairRecord>() {

                @Override
                public int compare(WeightedEntryPairRecord a,
                        WeightedEntryPairRecord b) {
                    final int x1 = Math.min(a.entry1Id, a.entry2Id);
                    final int x2 = Math.min(b.entry1Id, b.entry2Id);
                    final int y1 = Math.max(a.entry1Id, a.entry2Id);
                    final int y2 = Math.max(b.entry1Id, b.entry2Id);
                    return x1 < x2 ? -1
                            : x1 > x2 ? 1
                            : y1 < y2 ? -1
                            : y1 > y2 ? 1
                            : Double.compare(a.weight, b.weight);
                }
            };

    public static Comparator<WeightedEntryPairRecord> ASYMMETRIC_KEY_COMPARATOR =
            new Comparator<WeightedEntryPairRecord>() {

                @Override
                public int compare(WeightedEntryPairRecord a,
                        WeightedEntryPairRecord b) {
                    return a.entry1Id < b.entry1Id ? -1
                            : a.entry1Id > b.entry1Id ? 1
                            : a.entry2Id < b.entry2Id ? -1
                            : a.entry2Id > b.entry2Id ? 1
                            : 0;
                }
            };

    public static Comparator<WeightedEntryPairRecord> SYMMETRIC_KEY_COMPARATOR =
            new Comparator<WeightedEntryPairRecord>() {

                @Override
                public int compare(WeightedEntryPairRecord a,
                        WeightedEntryPairRecord b) {
                    final int x1 = Math.min(a.entry1Id, a.entry2Id);
                    final int x2 = Math.min(b.entry1Id, b.entry2Id);
                    final int y1 = Math.max(a.entry1Id, a.entry2Id);
                    final int y2 = Math.max(b.entry1Id, b.entry2Id);
                    return x1 < x2 ? -1
                            : x1 > x2 ? 1
                            : y1 < y2 ? -1
                            : y1 > y2 ? 1
                            : 0;
                }
            };

}

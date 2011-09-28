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

import com.google.common.base.Predicate;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Pair hold the uniue ids of two items, along with a value indicating
 * the weight of connections between them.
 *
 * Instances of Pair are immutable, though the fields are not
 *
 * @author Hamish Morgan
 */
public class Pair implements
        Comparable<Pair>, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * Indexed identifier of the first item.
     */
    private int x_id;

    /**
     * Indexed identifier of the second item.
     */
    private int y_id;

    /**
     * The weight of connection between the two items. Can be proximity,
     * similarity, distance or whatever is required.
     */
    private double weight;

    /**
     * Constructor used during de-serialization.
     */
    private Pair() {
    }

    /**
     * Constructor used during cloning. Sub-classes should implement a similar
     * constructor and call this one.
     *
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if argument weight is NaN
     */
    protected Pair(final Pair that)
            throws NullPointerException, IllegalArgumentException {
        if (that == null)
            throw new NullPointerException("that == null");
        if (Double.isNaN(that.weight))
            throw new IllegalArgumentException("proximity == NaN");

        this.x_id = that.x_id;
        this.y_id = that.y_id;
        this.weight = that.weight;
    }

    /**
     * Construct a new weighted pair using the given arguments.
     *
     * @param x_id Indexed identifier of the first item.
     * @param y_id Indexed identifier of the second item.
     * @param weight The weight of connection between the two items.
     * @throws IllegalArgumentException if argument weight is NaN
     */
    public Pair(final int x_id, final int y_id, final double weight)
            throws IllegalArgumentException {
        if (Double.isNaN(weight))
            throw new IllegalArgumentException("proximity == NaN");

        this.x_id = x_id;
        this.y_id = y_id;
        this.weight = weight;
    }

    /**
     * @return Indexed identifier of the first item.
     */
    public final int getXId() {
        return x_id;
    }

    /**
     * @return Indexed identifier of the second item.
     */
    public final int getYId() {
        return y_id;
    }

    /**
     * @return The weight of connection between the two items.
     */
    public final double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append('(');
        builder.append(x_id);
        builder.append(',');
        builder.append(y_id);
        builder.append(")=");
        builder.append(String.format("%g", weight));
        return builder.toString();
    }

    /**
     * Comparator that orders two pairs: first by the xId, second by yId, and
     * finally by weight.
     *
     * It is entirly consistent with equals() and hashCode().
     *
     * @param that object to compare to
     * @return -1 if this < that, +1 if this > that, 0 otherwise
     * @throws NullPointerException if the argument is null
     */
    @Override
    public int compareTo(final Pair that)
            throws NullPointerException {
        if (that == null)
            throw new NullPointerException("that == null");
        return ASYMMETRIC_COMPARATOR.compare(this, that);
    }

    @Override
    protected Pair clone() {
        return new Pair(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && getClass() == obj.getClass()
                && compareTo((Pair) obj) == 0;
    }

    @Override
    public int hashCode() {
        final long bits = Double.doubleToLongBits(this.weight);
        return 13 * (13 * (13 * 3 + this.x_id) + this.y_id)
                + (int) (bits ^ (bits >>> 32));
    }

    protected final Object writeReplace() {
        return new Serializer(this);
    }

    private static final class Serializer
            implements Externalizable {

        private static final long serialVersionUID = 1;

        private Pair pair;

        public Serializer() {
        }

        public Serializer(final Pair pair) {
            if (pair == null)
                throw new NullPointerException("pair == null");
            this.pair = pair;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(pair.x_id);
            out.writeInt(pair.y_id);
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
            pair = new Pair(x_id, y_id, weight);
        }

        protected final Object readResolve() {
            return pair;
        }
    }

    public static Predicate<Pair> identity() {
        return new Predicate<Pair>() {

            @Override
            public boolean apply(Pair input) {
                return input.getXId() == input.getYId();
            }
        };
    }


    public static Predicate<Pair> similarityGTE(
            final double minSimilarity) {
        return new Predicate<Pair>() {

            @Override
            public boolean apply(Pair pair) {
                return (pair.getWeight() >= minSimilarity);
            }
        };
    }

    public static Predicate<Pair> similarityLTE(
            final double maxSimilarity) {
        return new Predicate<Pair>() {

            @Override
            public boolean apply(Pair pair) {
                return (pair.getWeight() <= maxSimilarity);
            }
        };
    }

    public static Comparator<Pair> ASYMMETRIC_COMPARATOR = new Comparator<Pair>() {

        @Override
        public int compare(Pair a, Pair b) {
            return a.x_id < b.x_id ? -1
                    : a.x_id > b.x_id ? 1
                    : a.y_id < b.y_id ? -1
                    : a.y_id > b.y_id ? 1
                    : Double.compare(a.weight, b.weight);
        }
    };

    public static Comparator<Pair> SYMMETRIC_COMPARATOR = new Comparator<Pair>() {

        @Override
        public int compare(Pair a, Pair b) {
            final int x1 = Math.min(a.x_id, a.y_id);
            final int x2 = Math.min(b.x_id, b.y_id);
            final int y1 = Math.max(a.x_id, a.y_id);
            final int y2 = Math.max(b.x_id, b.y_id);
            return x1 < x2 ? -1
                    : x1 > x2 ? 1
                    : y1 < y2 ? -1
                    : y1 > y2 ? 1
                    : Double.compare(a.weight, b.weight);
        }
    };

    public static Comparator<Pair> ASYMMETRIC_KEY_COMPARATOR = new Comparator<Pair>() {

        @Override
        public int compare(Pair a, Pair b) {
            return a.x_id < b.x_id ? -1
                    : a.x_id > b.x_id ? 1
                    : a.y_id < b.y_id ? -1
                    : a.y_id > b.y_id ? 1
                    : 0;
        }
    };

    public static Comparator<Pair> SYMMETRIC_KEY_COMPARATOR = new Comparator<Pair>() {

        @Override
        public int compare(Pair a, Pair b) {
            final int x1 = Math.min(a.x_id, a.y_id);
            final int x2 = Math.min(b.x_id, b.y_id);
            final int y1 = Math.max(a.x_id, a.y_id);
            final int y2 = Math.max(b.x_id, b.y_id);
            return x1 < x2 ? -1
                    : x1 > x2 ? 1
                    : y1 < y2 ? -1
                    : y1 > y2 ? 1
                    : 0;
        }
    };

}

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Arrays;

/**
 * Store a sparse vector of double precision values as an ordered array non-zero
 * indices and an ordered array of values.
 *
 * This is an ideal representation for performing fast dot product, or
 * any other operation that ignores zero values, if the denisity is sufficiently
 * low. Other operations can be slower than a full vector because indicies must
 * be searched for.
 * 
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 */
public final class SparseDoubleVector
        extends SparseVector
        implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;
    //

    public double[] values;

    public double sum;

    public SparseDoubleVector(final int[] keys, final double[] values,
                              final int cardinality,
                              int size) {
        super(keys, cardinality, size);
        if (values == null)
            throw new NullPointerException("values == null");
        if (keys.length != values.length)
            throw new IllegalArgumentException("value.length != keys.length");

        this.values = values;
        sum = 0;
        for (int i = 0; i < size; i++) {
            sum += values[i];
        }
    }

    public SparseDoubleVector(final SparseDoubleVector other) {
        super(other);
        values = Arrays.copyOf(other.values, other.size);
        sum = other.sum;
    }

    public SparseDoubleVector(final int cardinality, int capacity) {
        super(cardinality, capacity);
        values = new double[keys.length];
        sum = 0;
    }

    public SparseDoubleVector(final int cardinality) {
        super(cardinality);
        values = new double[keys.length];
        sum = 0;
    }

    protected SparseDoubleVector() {
        values = new double[0];
        sum = 0;
    }

    private final void writeObject(final ObjectOutputStream out)
            throws IOException {
        compact();
        out.writeObject(values);
        out.writeDouble(sum);
    }

    private final void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        values = (double[]) in.readObject();
        if (keys.length != values.length)
            throw new IllegalArgumentException("value.length != keys.length");
        sum = in.readDouble();
    }

    @Override
    protected final void insureCapacity(final int requiredCapacity) {
        if (requiredCapacity > keys.length) {
            final int newCapacity = Math.max(requiredCapacity,
                                             (int) (keys.length * GROWTH_FACTOR) + 1);
            keys = Arrays.copyOf(keys, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    /**
     * Remove a given vector index (key) from the array if it exists. Return
     * it's index if it existed, or the insertion point if it didn't.
     *
     * @param key
     */
    @Override
    protected final void remove(final int key) {
        final int index = fetch(key);
        if (index >= 0) {
            sum -= values[index];
            System.arraycopy(keys, index + 1, keys, index, size - index);
            System.arraycopy(values, index + 1, values, index, size - index);
            size--;
        }
    }

    @Override
    public final void compact() {
        if (size < keys.length) {
            keys = Arrays.copyOf(keys, size);
            values = Arrays.copyOf(values, keys.length);
        }
    }

    public final double get(final int key) {
        if (key < 0 || key >= cardinality)
            throw new IndexOutOfBoundsException("key");
        final int index = fetch(key);
        return index < 0 ? 0d : values[index];
    }

    public final void set(final int key, final double value) {
//        checkElementIndex(key, cardinality);
        if (cardinality <= key) {
            cardinality = key + 1;
        }

        final int i = fetch(key);
        if (i >= 0) {
            sum -= values[i];
            values[i] = value;
            sum += values[i];
            return;

        }
        if (value == 0) {
            // index did not exists so was previously zero, and the new value
            // is zero so do nothing
            return;
        }

        insureCapacity(size + 1);

        final int insert = (-i) - 1;
        if (insert < size) {
            // the innsert point isn't the end so we must make space
            System.arraycopy(keys, insert, keys, insert + 1, size - insert);
            System.arraycopy(values, insert, values, insert + 1, size - insert);
        }
        keys[insert] = key;
        values[insert] = value;
        sum += values[insert];
        size++;
    }

    public final SparseDoubleVector slice(final int fromIndex, final int toIndex) {
        int key0 = fetch(fromIndex);
        if (key0 < 0)
            key0 = -key0 - 1;
        int key1 = fetch(toIndex);
        if (key1 < 0)
            key1 = -key1 - 1;
        final int[] newKeys = Arrays.copyOfRange(keys, key0, key1);
        final double[] newVals = Arrays.copyOfRange(values, key0, key1);
        return new SparseDoubleVector(newKeys, newVals, cardinality,
                                      newKeys.length);
    }

    public final SparseDoubleVector slice(final int fromIndex) {
        return slice(fromIndex, cardinality);
    }
//
//
//    public final  TIntDoubleIterator entryIterator() {
//        return new TIntDoubleEntryIterator(this, Direction.FORWARDS);
//    }
//
//    public final TIntDoubleIterator entryIterator(final Direction dir) {
//        return new TIntDoubleEntryIterator(this, dir);
//    }
//
//    private static class TIntDoubleEntryIterator implements TIntDoubleIterator {
//
//        private final SparseDoubleVector vector;
//        private final Direction dir;
//        private final int end;
//        private int index;
//
//        public TIntDoubleEntryIterator(final SparseDoubleVector vector,
//                final Direction dir) {
//            this.vector = vector;
//            this.dir = dir;
//            if (dir == Direction.FORWARDS) {
//                index = -1;
//                end = vector.size - 1;
//            } else if (dir == Direction.BACKWARDS) {
//                index = vector.size;
//                end = 0;
//            } else {
//                throw new IllegalArgumentException();
//            }
//        }
//
//        @Override
//        public final int key() {
//            return vector.keys[index];
//        }
//
//        @Override
//        public final double value() {
//            return vector.values[index];
//        }
//
//        @Override
//        public final double setValue(double val) {
//            final double prev = vector.values[index];
//            if (val == 0)
//                remove();
//            else
//                vector.values[index] = val;
//            return prev;
//        }
//
//        @Override
//        public final void advance() {
////            if (!hasNext())
////                throw new NoSuchElementException();
//            index += dir.step();
//        }
//
//        @Override
//        public final boolean hasNext() {
//            return index != end;
//        }
//
//        @Override
//        public final void remove() {
//            vector.remove(index);
//            index -= dir.step();
//        }
//    }
//
//    public final double dot(final SparseDoubleVector that) {
//        checkNotNull(that);
////        checkArgument(this.cardinality == that.cardinality, "Vectors must be the same size.");
//
//        if (this.size == 0 || that.size == 0) {
//            return 0;
//        }
//
//        double prod = 0;
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j]) {
//                prod += this.values[i] * that.values[j];
//                i++;
//                j++;
//            } else if (this.keys[i] < that.keys[j]) {
//                i++;
//            } else {
//                j++;
//            }
//        }
//        return prod;
//    }
//
//    public final double distance(final SparseDoubleVector that, double p) {
//        checkNotNull(that);
//
//        if (this.size == 0 || that.size == 0) {
//            return 0;
//        }
//
//        double distance = 0;
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j]) {
//                distance += Math.pow(Math.abs(this.values[i] - that.values[j]),
//                        p);
//                i++;
//                j++;
//            } else if (this.keys[i] < that.keys[j]) {
//                distance += Math.pow(this.values[i], p);
//                i++;
//            } else {
//                distance += Math.pow(that.values[i], p);
//                j++;
//            }
//        }
//        return Math.pow(distance, 1d / p);
//    }
//
//    public final SparseDoubleVector intersection(SparseDoubleVector that) {
//        checkNotNull(that);
//
//        if (this.size == 0 || that.size == 0) {
//            return new SparseDoubleVector(0);
//        }
//        final SparseDoubleVector min = new SparseDoubleVector(
//                Math.min(this.size, that.size));
//
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j]) {
//                min.set(this.keys[i], Math.min(this.values[i], that.values[j]));
//                i++;
//                j++;
//            } else if (this.keys[i] < that.keys[j]) {
//                i++;
//            } else {
//                j++;
//            }
//        }
//        min.compact();
//        return min;
//    }
//
//    public final double intersectionSum(SparseDoubleVector that) {
//        checkNotNull(that);
//
//        if (this.size == 0 || that.size == 0) {
//            return 0;
//        }
//        double intSize = 0;
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j])
//                intSize += Math.min(this.values[i++], that.values[j++]);
//            else if (this.keys[i] < that.keys[j])
//                i++;
//            else
//                j++;
//
//        }
//        return intSize;
//    }
//
//    public final SparseDoubleVector union(SparseDoubleVector that) {
//        checkNotNull(that);
//
//        if (this.size == 0 && that.size == 0) {
//            return new SparseDoubleVector(0);
//        }
//        final SparseDoubleVector max = new SparseDoubleVector(
//                this.size + that.size);
//
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j]) {
//                max.set(this.keys[i], Math.max(this.values[i], that.values[j]));
//                i++;
//                j++;
//            } else if (this.keys[i] < that.keys[j]) {
//                max.set(this.keys[i], this.values[i]);
//                i++;
//            } else {
//                max.set(this.keys[i], that.values[j]);
//                j++;
//            }
//        }
//        max.compact();
//        return max;
//    }
//
//    public final double unionSum(SparseDoubleVector that) {
//        checkNotNull(that);
//
//        if (this.size == 0 && that.size == 0) {
//            return 0;
//        }
//        double unionSize = 0;
//
//        int i = 0;
//        int j = 0;
//        while (i < this.size && j < that.size) {
//            if (this.keys[i] == that.keys[j])
//                unionSize += Math.max(this.values[i++], that.values[j++]);
//            else if (this.keys[i] < that.keys[j])
//                unionSize += this.values[i++];
//            else
//                unionSize += that.values[j++];
//        }
//        return unionSize;
//    }
//
//    public final double dot(final double[] arr) {
//        checkNotNull(arr);
////        checkArgument(this.cardinality == arr.length,
////                "Vectors must be the same size; expected" + cardinality + ", but found " + arr.length);
//
//        if (size == 0 || arr.length == 0) {
//            return 0;
//        }
//
//        double prod = 0;
//        for (int i = 0; i < size; i++) {
//            prod += values[i] * arr[keys[i]];
//        }
//        return prod;
//    }

//    public final double sum() {
//        return sum;
//        double sum = 0;
//        for (int i = 0; i < size; i++) {
//            sum += values[i];
//        }
//        return sum;
//    }
//
//    public final double min() {
//        double min = values[0];
//        for (int j = 1; j < size; j++) {
//            if (Double.isNaN(values[j])) {
//                return Double.NaN;
//            }
//            if (values[j] < min) {
//                min = values[j];
//            }
//        }
//        return (size < cardinality) ? Math.min(min, 0) : min;
//    }
//
//    public final double max() {
//        // Finds and returns max
//        double max = values[0];
//        for (int j = 1; j < size; j++) {
//            if (Double.isNaN(values[j])) {
//                return Double.NaN;
//            }
//            if (values[j] > max) {
//                max = values[j];
//            }
//        }
//        return (size < cardinality) ? Math.max(max, 0) : max;
//    }
    public final double magnitude() {
        double sqr_sum = 0;
        for (int i = 0; i < size; i++) {
            sqr_sum += values[i] * values[i];
        }
        return Math.sqrt(sqr_sum);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        SparseDoubleVector other = (SparseDoubleVector) obj;
        if (this.cardinality != other.cardinality || this.size != other.size)
            return false;
        for (int i = 0; i < size; i++) {
            if (this.keys[i] != other.keys[i])
                return false;
            if (Double.doubleToLongBits(this.values[i]) != Double.
                    doubleToLongBits(
                    other.values[i]))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        for (int i = 0; i < size; i++) {
            hash = 31 * hash + keys[i];
            long bits = Double.doubleToLongBits(values[i]);
            hash = 31 * hash + (int) (bits ^ (bits >>> 32));
        }
        hash = 19 * hash + this.cardinality;
        hash = 19 * hash + this.size;
        return hash;
    }

    @Override
    public SparseDoubleVector clone() {
        return new SparseDoubleVector(this);
    }

    public final double getNoEntryValue() {
        return 0;
    }

    public final boolean isEmpty() {
        return cardinality == 0;
    }

    public final boolean contains(final double entry) {
        for (int i = 0; i < size; i++) {
            if (values[i] == entry) {
                return true;
            }
        }
        return false;
    }
//
//    @Override
//    public String toString() {
//        final StringBuilder buf = new StringBuilder();
//        buf.append(super.toString());
//
//        final TIntDoubleIterator it = entryIterator();
//        if (it.hasNext()) {
//            it.advance();
//            buf.append(it.key());
//            buf.append(":");
//            buf.append(it.value());
//
//        }
//        while (it.hasNext()) {
//            it.advance();
//            buf.append(", ");
//            buf.append(it.key());
//            buf.append(":");
//            buf.append(it.value());
//        }
//        buf.append("}");
//
//        return buf.toString();
//    }
}

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
package uk.ac.susx.mlcl.lib.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Abstract super class defining common operation of list based sparse vector
 * representation for different primitive types.
 *
 * This is an ideal representation for performing fast dot product, or
 * any other operation that ignores zero values, if the denisity is sufficiently
 * low. Other operations can be slower than a full vector because indicies must
 * be searched for.
 *
 * Vector have a fixed dimentionality. Once constructed they cannot change from
 * their initialized cardinality.
 * 
 * TODO: There could potentially be a warnings about excess density that would
 *       cause the structure to become inefficient. This will first require
 *       an experiment to see what that density is. However this should not
 *       be implmented in this class which has no way of knowing what kind of
 *       warning would be appropriate, if at all. (Issue #27)
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class SparseVector implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Iterators and ForEach lambda actioners can opperate both forwards and
     * in reverse. These constants are used to denote which direction is
     * required.
     */
    public static enum Direction {

        FORWARDS(1), BACKWARDS(-1);
        private final int step;

        private Direction(final int step) {
            this.step = step;
        }

        public final int step() {
            return step;
        }
    }
    /**
     * If initial capacity is not provided at construction, then this
     * constant will be used.
     */
    protected static final int DEFAULT_CAPACITY = 10;
    /**
     * When the capacity is exausted, they storage will be expanded by the this
     * factor plus 1. For example if the store is 10 then it will become 16.
     */
    protected static final double GROWTH_FACTOR = 1.5d;
    /**
     * The dimentionality of the vector space and the maximum number of elements
     * in this structure.
     */
    public int cardinality;
    /**
     * An ordered array of key values. Each key reperents a dimention index that
     * holds a non-zero value.
     */
    public int[] keys;
    /**
     * The number of elements in keys[] that are currently used.
     */
    public int size;

    /**
     * Dependancy inject constructure for testing and other internal purposes.
     *
     * @param keys Array containing the ordered indices of all non-zero values.
     * @param cardinality The number of dimensions
     * @param size  The number of keys actually used.
     * @throws NullPointerException  when argument keys is null
     * @throws IndexOutOfBoundsException  if keys is smaller than cardinality, or
     *        keys is negative, or size is smaller that keys or size is
     *        negative, or the largest value in keys is greater than cardinality,
     *        or the keys contains a negative value.
     * @throws IllegalArgumentException if cardinality is negative
     */
    protected SparseVector(
            final int[] keys, final int cardinality, final int size)
            throws NullPointerException, IndexOutOfBoundsException,
            IllegalArgumentException {
        if(keys == null) throw new NullPointerException("keys == null");
        if(keys.length > cardinality)
            throw new IndexOutOfBoundsException("keys.length");
        if(size < 0 || size > keys.length)
            throw new IndexOutOfBoundsException("size");

        this.keys = keys;
        this.cardinality = cardinality;
        this.size = size;
    }

    /**
     * Cloning constructor for subclasses to use.
     *
     * @param other SparseVector to clone
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException
     */
    protected SparseVector(final SparseVector other) throws
            NullPointerException, IndexOutOfBoundsException,
            IllegalArgumentException {
        this(Arrays.copyOf(other.keys, other.size), other.cardinality, other.size);
    }

    /**
     * Null constructor for serialization. It does the minimum amount of work
     * to create a valid object.
     *
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException
     */
    protected SparseVector() throws IllegalArgumentException,
            IndexOutOfBoundsException, NullPointerException {
        this(new int[0], 0, 0);
    }

    /**
     *
     * @param cardinality
     * @param capacity
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException
     */
    protected SparseVector(final int cardinality, final int capacity)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        if(capacity < 0 || capacity > cardinality)
            throw new IndexOutOfBoundsException("capacity");

        this.keys = new int[capacity];
        this.cardinality = cardinality;
        this.size = 0;
    }

    /**
     *
     * @param cardinality
     * @throws IllegalArgumentException
     */
    protected SparseVector(final int cardinality)
            throws IllegalArgumentException {
        this(cardinality, Math.min(cardinality, DEFAULT_CAPACITY));
    }

    /**
     * The dimentionality of this vector. Not to be confused with the length
     * (magnitude) or the number of elements actually used.
     *
     * @return the dimentionality of the vector space
     */
    public final int cardinality() {
        return cardinality;
    }

    /**
     * The number of storage elements required to represent this vector. This
     * will be equal to the number of non-zero elements in the vector.
     *
     * @return number of storage elements required
     */
    public final int size() {
        return size;
    }

    /**
     * The number of storage elements available before the internal array must
     * be grown.
     *
     * @return store elements available.
     */
    public final int capacity() {
        return keys.length;
    }

    /**
     * The proportion of this vectors dimensions which contain non-zero values.
     * Where 0.0 indicates there are no non-zero elements, and 1.0 indicate that
     * every element is non-zero.
     *
     * Returns 0.0 for null (0 cardinality) vectors.
     *
     * @return proportion of usage as a value in the range 0.0 to 1.0
     */
    public final double density() {
        return empty() ? 0D : (double) size / (double) cardinality;
    }
    

    /**
     * Whether this vector contains any non-zero elements.
     *
     * @return false if there is a non-zero element, true otherwise.
     */
    public final boolean empty() {
        return cardinality() == 0;
    }
//
//    /**
//     * @return a forwards iterator over each non-zero element index.
//     */
//    public final TIntIterator indexIterator() {
//        return indexIterator(Direction.FORWARDS);
//    }
//
//    /**
//     * @param dir the direction to iterate in
//     * @return an iterator over each non-zero element index.
//     */
//    public final TIntIterator indexIterator(final Direction dir) {
//        checkNotNull(dir, "dir");
//        return new IndexIterator(this, dir);
//    }

    /**
     * Warning: This method returns a copy of keys so can be quite slow.
     *
     * @return a copy of the keys array.
     */
    @Deprecated
    public final int[] keys() {
        return Arrays.copyOf(keys, size);
    }

    public void compact() {
        if (size < keys.length) {
            keys = Arrays.copyOf(keys, size);
        }
    }

    protected final boolean contains(int key) {
        return fetch(key) >= 0 ? true : false;
    }

    protected final int fetch(final int index) {
        return Arrays.binarySearch(keys, 0, size, index);
    }

    protected void insureCapacity(final int requiredCapacity) {
        if (requiredCapacity > keys.length) {
            final int newCapacity = Math.max(requiredCapacity,
                    (int) (keys.length * GROWTH_FACTOR) + 1);
            keys = Arrays.copyOf(keys, newCapacity);
        }
    }

    /**
     * Ensure that the structure has the given index. If it does not then find
     * the insert point.
     *
     * @param key The vector index to create
     */
    protected final void store(final int key) {
        final int index = fetch(key);

        // index already exists
        if (index >= 0) {
            return;
        }

        final int insert = (-index) - 1;

        insureCapacity(size + 1);

        // the insert point isn't the end so we must make space by
        // shifting everything up
        if (insert < size) {
            System.arraycopy(keys, insert, keys, insert + 1, size - insert);
        }
        size++;
        keys[insert] = key;
    }

    /**
     * Remove a given vector index (key) from the array if it exists. Return
     * it's index if it existed, or the insertion point if it didn't.
     * 
     * @param key
     */
    protected void remove(final int key) {
        final int index = fetch(key);
        if (index >= 0) {
            System.arraycopy(keys, index + 1, keys, index, size - index);
            size--;
        }
    }

    private final void writeObject(final ObjectOutputStream out)
            throws IOException {
        // Compact before serialization so "used" is redundant
        compact();
        out.writeInt(cardinality);
        out.writeObject(keys);
    }

    private final void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        cardinality = in.readInt();
        keys = (int[]) in.readObject();
        if(keys == null)
            throw new NullPointerException("keys");
        if(keys.length < 0 || keys.length > cardinality)
            throw new IndexOutOfBoundsException("keys.length");
        size = keys.length;
    }


    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        buf.append(getClass().getSimpleName());
        buf.append("[cardinality=");
        buf.append(cardinality);
        buf.append("]");
        return buf.toString();
    }
}

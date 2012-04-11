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
package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.*;
import java.util.Map;
import java.util.Random;

/**
 *
 *
 *
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class Int2ObjectOpenHashBiMap<T>
        implements Serializable, RandomAccessableInt2ObjectBiMap<T> {

    private static final long serialVersionUID = 2L;

    private final RandomAccessableInt2ObjectOpenHashMap<T> forwards;

    private final RandomAccessableObject2IntOpenHashMap<T> backwards;

    protected Int2ObjectOpenHashBiMap(
            RandomAccessableInt2ObjectOpenHashMap<T> indexToObj,
            RandomAccessableObject2IntOpenHashMap<T> objToIndex) {
        this.forwards = indexToObj;
        this.backwards = objToIndex;
    }

    public Int2ObjectOpenHashBiMap(int expected, float f) {
        this.forwards = new RandomAccessableInt2ObjectOpenHashMap<T>(expected, f);
        this.backwards = new RandomAccessableObject2IntOpenHashMap<T>(expected, f);
        backwards.defaultReturnValue(-1);
        forwards.defaultReturnValue(null);
    }

    public Int2ObjectOpenHashBiMap(int expected) {
        this(expected, Object2IntOpenHashMap.DEFAULT_LOAD_FACTOR);
    }

    public Int2ObjectOpenHashBiMap() {
        this(Object2IntOpenHashMap.DEFAULT_INITIAL_SIZE);
    }

    @Override
    public T put(Integer k, T v) {
        if (backwards.containsKey(v))
            throw new IllegalArgumentException(
                    "given value is already bound to a different key in this bimap");
        backwards.put(v, k);
        return forwards.put(k, v);
    }

    @Override
    public T put(int k, T v) {
        backwards.put(v, k);
        return forwards.put(k, v);
    }

    @Override
    public T forcePut(Integer k, T v) {
        backwards.put(v, k);
        return forwards.put(k, v);
    }

    @Override
    public T forcePut(int k, T v) {
        backwards.put(v, k);
        return forwards.put(k, v);
    }

    @Override
    public int size() {
        return forwards.size();
    }

    @Override
    public boolean isEmpty() {
        return forwards.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forwards.containsKey(key);
    }

    @Override
    public boolean containsKey(int key) {
        return forwards.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backwards.containsKey(value);
    }

    @Override
    public T remove(Object key) {
        final T r = forwards.remove(key);
        backwards.remove(r);
        return r;
    }

    @Override
    public T remove(int key) {
        final T r = forwards.remove(key);
        backwards.remove(r);
        return r;
    }

    @Override
    public void clear() {
        forwards.clear();
        backwards.clear();
    }

    @Override
    public IntSet keySet() {
        return forwards.keySet();
    }

    @Override
    public T get(Object key) {
        return forwards.get(key);
    }

    @Override
    public T get(int key) {
        return forwards.get(key);
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends T> map) {
        for (Map.Entry<? extends Integer, ? extends T> e : map.entrySet())
            put(e.getKey(), e.getValue());
    }

    @Override
    public ObjectSet<T> values() {
        return backwards.keySet();
    }

    @Override
    public ObjectSet<Map.Entry<Integer, T>> entrySet() {
        return forwards.entrySet();
    }

    @Override
    public Int2ObjectMap.FastEntrySet<T> int2ObjectEntrySet() {
        return forwards.int2ObjectEntrySet();
    }

    @Override
    public void defaultReturnValue(T v) {
        forwards.defaultReturnValue(v);
    }

    @Override
    public T defaultReturnValue() {
        return forwards.defaultReturnValue();
    }

    @Override
    public RandomAccessableObject2IntBiMap<T> inverse() {
        return inverse;
    }

    @Override
    public Int2ObjectMap.Entry<T> randomEntry() {
        return forwards.randomEntry();
    }

    @Override
    public Int2ObjectMap.Entry<T> randomEntry(Random rand) {
        return forwards.randomEntry(rand);
    }

    @Override
    public Integer randomKey(Random rand) {
        return forwards.randomKey(rand);
    }

    @Override
    public Integer randomKey() {
        return forwards.randomKey();
    }

    @Override
    public int randomIntKey(Random rand) {
        return forwards.randomKey(rand);
    }

    @Override
    public int randomIntKey() {
        return forwards.randomKey();
    }

    @Override
    public T randomValue() {
        return forwards.randomValue();
    }

    @Override
    public T randomValue(Random rand) {
        return forwards.randomValue(rand);
    }

    private transient final RandomAccessableObject2IntBiMap<T> inverse = new RandomAccessableObject2IntBiMap<T>() {

        @Override
        public Integer put(T k, Integer v) {
            if (forwards.containsKey(v))
                throw new IllegalArgumentException(
                        "given value is already bound to a different key in this bimap");
            forwards.put(v, k);
            return backwards.put(k, v);
        }

        @Override
        public int put(T k, int v) {
            if (forwards.containsKey(v))
                throw new IllegalArgumentException(
                        "given value is already bound to a different key in this bimap");
            forwards.put(v, k);
            return backwards.put(k, v);
        }

        @Override
        public Integer forcePut(T k, Integer v) {
            forwards.put(v, k);
            return backwards.put(k, v);
        }

        @Override
        public int forcePut(T k, int v) {
            forwards.put(v, k);
            return backwards.put(k, v);
        }

        @Override
        public IntSet values() {
            return forwards.keySet();
        }

        @Override
        public int size() {
            return backwards.size();
        }

        @Override
        public boolean isEmpty() {
            return backwards.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return backwards.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return forwards.containsKey(value);
        }

        @Override
        public boolean containsValue(int value) {
            return forwards.containsKey(value);
        }

        @Override
        public Integer get(Object key) {
            return backwards.get(key);
        }

        @Override
        public Integer remove(Object key) {
            final int i = backwards.remove(key);
            forwards.remove(i);
            return i;
        }

        @Override
        public void clear() {
            forwards.clear();
            backwards.clear();
        }

        @Override
        public ObjectSet<T> keySet() {
            return backwards.keySet();
        }

        @Override
        public RandomAccessableInt2ObjectBiMap<T> inverse() {
            return Int2ObjectOpenHashBiMap.this;
        }

        @Override
        public ObjectSet<Object2IntMap.Entry<T>> object2IntEntrySet() {
            return backwards.object2IntEntrySet();
        }

        @Override
        public int getInt(Object o) {
            return backwards.getInt(o);
        }

        @Override
        public int removeInt(Object o) {
            return backwards.removeInt(o);
        }

        @Override
        public void defaultReturnValue(int i) {
            backwards.defaultReturnValue(i);
        }

        @Override
        public int defaultReturnValue() {
            return backwards.defaultReturnValue();
        }

        @Override
        public void putAll(Map<? extends T, ? extends Integer> map) {
            for (Map.Entry<? extends T, ? extends Integer> e : map.entrySet())
                put(e.getKey(), e.getValue());
        }

        @Override
        public ObjectSet<Map.Entry<T, Integer>> entrySet() {
            return backwards.entrySet();
        }

        @Override
        public Object2IntMap.Entry<T> randomEntry() {
            return backwards.randomEntry();
        }

        @Override
        public Object2IntMap.Entry<T> randomEntry(Random rand) {
            return backwards.randomEntry(rand);
        }

        @Override
        public T randomKey(Random rand) {
            return backwards.randomKey(rand);
        }

        @Override
        public T randomKey() {
            return backwards.randomKey();
        }

        @Override
        public Integer randomValue() {
            return backwards.randomValue();
        }

        @Override
        public Integer randomValue(Random rand) {
            return backwards.randomValue(rand);
        }

        @Override
        public int randomIntValue() {
            return backwards.randomValue();
        }

        @Override
        public int randomIntValue(Random rand) {
            return backwards.randomValue(rand);
        }

    };

    @Override
    public String toString() {
        return backwards.toString();
    }

    final Object writeReplace() {
        return new Serializer<T>(this);
    }

    static final class Serializer<T> implements Externalizable {

        private static final long serialVersionUID = 1;

        private Int2ObjectOpenHashBiMap<T> instance;

        Serializer() {
        }

        Serializer(final Int2ObjectOpenHashBiMap<T> se) {
            if (se == null) {
                throw new NullPointerException("se == null");
            }
            this.instance = se;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeObject(instance.forwards);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            RandomAccessableInt2ObjectOpenHashMap<T> forwards =
                    (RandomAccessableInt2ObjectOpenHashMap<T>) in.readObject();
            RandomAccessableObject2IntOpenHashMap<T> backwards =
                    new RandomAccessableObject2IntOpenHashMap<T>(forwards.size());
            for (int i = 0; i < forwards.size(); i++)
                backwards.put(forwards.get(i), i);
            this.instance = new Int2ObjectOpenHashBiMap<T>(forwards, backwards);
        }

        final Object readResolve() {
            return instance;
        }

    }
}

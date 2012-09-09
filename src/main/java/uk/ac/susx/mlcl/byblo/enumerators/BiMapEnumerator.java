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
package uk.ac.susx.mlcl.byblo.enumerators;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.collect.ForwardingBiMap;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple enumerator that delegates to BiMap for enumeration storage, and
 * assigns current max + 1 as the next key.
 *
 * @param <T> type of object being indexed.
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class BiMapEnumerator<T> implements Serializable, Enumerator<T> {

    private static final long serialVersionUID = 1L;

    private final BiMap<Integer, T> map;

    private final AtomicInteger nextId;

    /**
     * Dependency injection constructor to be used by subclasses only.
     * <p/>
     * Checks nextId with an assert only, so will accept invalid nextId's when
     * assertions are disabled.
     *
     * @param map    BiMap to delegate storage too.
     * @param nextId Integer value of key to assign to next added element.
     */
    protected BiMapEnumerator(
            final BiMap<Integer, T> map,
            final AtomicInteger nextId) {
        Checks.checkNotNull("map", map);
        Checks.checkNotNull("nextId", nextId);
        assert max(map) < nextId.get() : MessageFormat.format(
                "next id ({0}) is not greater than largest in map ({1})",
                nextId.get(), max(map));

        this.map = map;
        this.nextId = nextId;
    }

    public BiMapEnumerator(final BiMap<Integer, T> map) {
        this(map, new AtomicInteger(max(map) + 1));
    }

    protected BiMapEnumerator(Map<Integer, T> forwards,
                              Map<T, Integer> backwards) {
        Checks.checkNotNull("forwards", forwards);
        Checks.checkNotNull("backwards", backwards);
        assert forwards.size() == backwards.size();
        assert forwards.keySet().containsAll(backwards.values());
        assert backwards.keySet().containsAll(forwards.values());

        map = ForwardingBiMap.<Integer, T>create(forwards, backwards);
        nextId = new AtomicInteger(max(forwards));
    }

    public BiMapEnumerator() {
        this(new HashMap<Integer, T>(), new HashMap<T, Integer>());
    }

    public int getNextId() {
        return nextId.get();
    }

    public BiMap<Integer, T> getMap() {
        return Maps.unmodifiableBiMap(map);
    }

    @Override
    public final int indexOf(final T value) {
        Checks.checkNotNull("value", value);

        final Integer result = map.inverse().get(value);
        if (result == null) {
            final int id = nextId.getAndIncrement();
            put(id, value);
            return id;
        } else {
            return result;
        }

    }

    @Override
    public final T valueOf(final int index) {
        Checks.checkRangeIncl("index", index, 0, Integer.MAX_VALUE);

        final T value = map.get(index);
        assert value != null : MessageFormat.format("Enumerated value is null;"
                + " there is no item associated with index {0}.", index);
        return value;
    }

    protected void put(final int id, final T obj) {
        map.put(id, obj);
        if (nextId.get() <= id)
            nextId.set(id + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        return obj != null
                && getClass() != obj.getClass()
                && equals((BiMapEnumerator<?>) obj);
    }

    public boolean equals(BiMapEnumerator<?> other) {
        if (!nextId.equals(other.nextId) && (nextId == null || !nextId.equals(other.nextId)))
            return false;
        if (map != other.map && (map == null || !map.equals(other.map)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 29 * (29 * 7 + (map != null ? map.hashCode() : 0))
                + (nextId != null ? nextId.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "BiMapEnumerator{" + "map=" + map + ", nextId=" + nextId + '}';
    }

    @Override
    public Iterator<Map.Entry<Integer, T>> iterator() {
        return map.entrySet().iterator();
    }

    final Object writeReplace() {
        return new Serializer<T>(this);
    }

    public static final class Serializer<T> implements Externalizable {

        private static final long serialVersionUID = 1;

        private BiMapEnumerator<T> instance;

        public Serializer() {
        }

        Serializer(final BiMapEnumerator<T> instance) {
            if (instance == null) {
                throw new NullPointerException("se == null");
            }
            this.instance = instance;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(instance.nextId.get());
            out.writeObject(instance.map);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            AtomicInteger nextId = new AtomicInteger(0);
            nextId.set(in.readInt());
            @SuppressWarnings("unchecked")
            BiMap<Integer, T> map = (BiMap<Integer, T>) in.readObject();
            this.instance = new BiMapEnumerator<T>(map, nextId);
        }

        final Object readResolve() {
            return instance;
        }
    }

    private static <T> int max(final Map<Integer, T> map) {
        assert map != null : "map is null";
        if (map.isEmpty()) {
            return -1;
        } else if (map instanceof SortedMap) {
            final SortedMap<Integer, T> smap = ((SortedMap<Integer, T>) map);
            assert smap.lastKey() > smap.firstKey() :
                    "Expecting last key in sorted map to be greatest.";
            return smap.lastKey();
        } else {
            int max = -1;
            for (int key : map.keySet())
                if (max < key)
                    max = key;
            return max;
        }
    }
}

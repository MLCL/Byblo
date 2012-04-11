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
package uk.ac.susx.mlcl.lib;

import com.google.common.collect.BiMap;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import uk.ac.susx.mlcl.lib.collect.ForwardingBiMap;
import uk.ac.susx.mlcl.lib.collect.Int2ObjectBiMap;

/**
 * A simple of bimap for indexing complex objects (usually strings).
 *
 * @param <T> type of object being indexed.
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class BiMapEnumerator<T> implements Serializable, Enumerator<T> {

    private static final long serialVersionUID = 2L;

    private final BiMap<Integer, T> map;

    private final AtomicInteger nextId;

    protected BiMapEnumerator(
            BiMap<Integer, T> map,
            AtomicInteger nextId) {
        this.map = map;
        this.nextId = nextId;
    }

    protected BiMapEnumerator() {
        this(ForwardingBiMap.<Integer, T>create(
                new HashMap<Integer, T>(),
                new HashMap<T, Integer>()),
             new AtomicInteger(0));
    }

    protected synchronized void put(final int id, final T obj) {
         map.put(id, obj);
         if(nextId.get() <= id)
             nextId.set(id + 1);
    }

    public int getNextId() {
        return nextId.get();
    }
    

    @Override
    public synchronized final int indexOf(final T obj) {
        Checks.checkNotNull("obj", obj);
        if (!map.containsValue(obj))
            put(nextId.getAndIncrement(), obj);
        return map.inverse().get(obj);
    }

    @Override
    public synchronized final T valueOf(final int id) {
        Checks.checkRangeIncl("id", id, 0, Integer.MAX_VALUE);

        final T value = map.get(id);
        assert value != null : "value is null";
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BiMapEnumerator<T> other = (BiMapEnumerator<T>) obj;
        if (this.map != other.map && (this.map == null || !this.map.equals(other.map)))
            return false;
        if (this.nextId != other.nextId && (this.nextId == null || !this.nextId.equals(other.nextId)))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.map != null ? this.map.hashCode() : 0);
        hash = 29 * hash + (this.nextId != null ? this.nextId.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "MemoryBasedEnumerator{" + "map=" + map + ", nextId=" + nextId + '}';
    }

    @Override
    public Iterator<Map.Entry<Integer, T>> iterator() {
        return map.entrySet().iterator();
    }

    final Object writeReplace() {
        return new Serializer<T>(this);
    }

    static final class Serializer<T> implements Externalizable {

        private static final long serialVersionUID = 1;

        private BiMapEnumerator<T> se;

        Serializer() {
        }

        Serializer(final BiMapEnumerator<T> se) {
            if (se == null) {
                throw new NullPointerException("se == null");
            }
            this.se = se;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(se.nextId.get());
            out.writeObject(se.map);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            AtomicInteger nextId = new AtomicInteger(0);
            nextId.set(in.readInt());
            @SuppressWarnings("unchecked")
            Int2ObjectBiMap<T> map = (Int2ObjectBiMap<T>) in.readObject();
            this.se = new BiMapEnumerator<T>(map, nextId);
        }

        final Object readResolve() {
            return se;
        }

    }
}

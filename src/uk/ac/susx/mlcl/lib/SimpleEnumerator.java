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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.io.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple of bimap for indexing complex objects (usually strings).
 *
 * @param <T> type of object being indexed.
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class SimpleEnumerator<T> implements Serializable, Enumerator<T> {

    private static final long serialVersionUID = 2L;

    private final ObjectList<T> indexToObj;

    private final Object2IntMap<T> objToIndex;

    private final AtomicInteger nextId;

    protected SimpleEnumerator(ObjectList<T> indexToObj,
                               Object2IntMap<T> objToIndex, AtomicInteger nextId) {
        this.indexToObj = indexToObj;
        this.objToIndex = objToIndex;
        this.nextId = nextId;
    }

    protected SimpleEnumerator() {
        this(new ObjectArrayList<T>(), new Object2IntOpenHashMap<T>(),
             new AtomicInteger(0));
        objToIndex.defaultReturnValue(-1);
    }

    @Override
    public final int index(final T obj) {
        if (obj == null)
            throw new NullPointerException("obj is null");

        final int result = objToIndex.getInt(obj);
        return (result == objToIndex.defaultReturnValue())
                ? get0(obj) : result;
    }

    private synchronized int get0(final T obj) {
        if (obj == null)
            throw new NullPointerException("obj is null");
        if (objToIndex.containsKey(obj)) {
            return objToIndex.getInt(obj);
        } else {
            final int id = nextId.getAndIncrement();
            objToIndex.put(obj, id);
            indexToObj.add(obj);
            return id;
        }
    }

    @Override
    public final T value(final int id) {
        return indexToObj.get(id);
    }

    @Override
    public String toString() {
        return objToIndex.toString();
    }

    @Override
    public Iterator<Object2IntMap.Entry<T>> iterator() {
        return new Iterator<Entry<T>>() {

            int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return nextIndex < indexToObj.size();
            }

            @Override
            public Object2IntMap.Entry<T> next() {
                final Object2IntMap.Entry<T> e = new Object2IntMap.Entry<T>() {

                    final int index = nextIndex;

                    @Override
                    public int setValue(int i) {
                        throw new UnsupportedOperationException(
                                "Not supported yet.");
                    }

                    @Override
                    public int getIntValue() {
                        return index;
                    }

                    @Override
                    public T getKey() {
                        return indexToObj.get(index);
                    }

                    @Override
                    public Integer getValue() {
                        return index;
                    }

                    @Override
                    public Integer setValue(Integer v) {
                        throw new UnsupportedOperationException(
                                "Not supported yet.");
                    }
                };
                nextIndex++;
                return e;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    final Object writeReplace() {
        return new Serializer<T>(this);
    }

    static final class Serializer<T> implements Externalizable {

        private static final long serialVersionUID = 1;

        private SimpleEnumerator<T> se;

        Serializer() {
        }

        Serializer(final SimpleEnumerator<T> se) {
            if (se == null) {
                throw new NullPointerException("se == null");
            }
            this.se = se;
        }

        @Override
        public final void writeExternal(final ObjectOutput out)
                throws IOException {
            out.writeInt(se.nextId.get());
            out.writeObject(se.indexToObj);
        }

        @Override
        public final void readExternal(final ObjectInput in)
                throws IOException, ClassNotFoundException {
            AtomicInteger nextId = new AtomicInteger(0);
            nextId.set(in.readInt());
            @SuppressWarnings("unchecked")
            ObjectList<T> indexToObj = (ObjectList<T>) in.readObject();
            Object2IntMap<T> objToIndex = new Object2IntOpenHashMap<T>();
            for (int i = 0; i < indexToObj.size(); i++) {
                objToIndex.put(indexToObj.get(i), i);
            }
            this.se = new SimpleEnumerator<T>(indexToObj, objToIndex, nextId);
        }

        final Object readResolve() {
            return se;
        }
    }
}

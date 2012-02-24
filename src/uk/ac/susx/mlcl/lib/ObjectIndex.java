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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple of bimap for indexing complex objects (usually strings).
 *
 * @param <T> type of object being indexed.
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ObjectIndex<T> implements Serializable {

    private static final long serialVersionUID = 2L;

    private final ObjectList<T> indexToObj;

    private final Object2IntMap<T> objToIndex;

    private final AtomicInteger nextId = new AtomicInteger(0);

    public ObjectIndex() {
        indexToObj = new ObjectArrayList<T>();
        objToIndex = new Object2IntOpenHashMap<T>();
        objToIndex.defaultReturnValue(-1);
    }

    public final int get(final T obj) {
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

    public final T get(final int id) {
        return indexToObj.get(id);
    }

    private void writeObject(final ObjectOutputStream out)
            throws IOException {
        out.writeInt(nextId.get());
        out.writeObject(indexToObj);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        nextId.set(in.readInt());
        indexToObj.addAll((ObjectList<T>) in.readObject());
        for (int i = 0; i < indexToObj.size(); i++) {
            objToIndex.put(indexToObj.get(i), i);
        }
    }

    @Override
    public String toString() {
        return objToIndex.toString();
    }

}

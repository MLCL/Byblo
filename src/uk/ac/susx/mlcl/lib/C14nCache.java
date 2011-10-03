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
package uk.ac.susx.mlcl.lib;

import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * The {@link C14nCache} (canonicalisation cache) is used to reduce the number
 * of redundant copies of identical objects in memory.
 * <p>
 * It cases where many objects are being produced and stored, and there is high
 * probability of produced object being exact duplicates, it can be desirable
 * to cut down on memory usage by producing the same instance again, rather than
 * and equal copy.
 * </p><p>
 * Object passed as a parameter to the {@link C14nCache#cached(java.lang.Object)
 * cached()} method are either stored and returned, or, if an identical object
 * has been seen before, the second is discarded and the original is returned.
 * </p><p>
 * It works by holding weak references to stored objects, so objects that are
 * no longer referenced elsewhere may be subject to garbage collection. There
 * is therefore no need to clear the cache to free memory, simply insure that
 * unwanted objects are dereferenced elsewhere.
 * </p><p>
 * The object also implements base Set interface functionality, however this
 * should generally be avoided in normal usage. It is mostly there for debugging
 * information. Note that only immutable Set methods are implemented, so calls
 * to add() or remove() will result in an UnsupportedOperationException.
 * </p><p>
 * This object is not thread safe. In particular calls to {@link
 * C14nCache#cached(java.lang.Object) cached()} must be synchronised externally
 * or the behaviour will be undefined. Concurrent use of the Set interface
 * functionality is fail-fast and will result in a
 * {@link ConcurrentModificationException}.
 * </p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @param <T> The type of object that will be held here.
 */
public class C14nCache<T> extends AbstractSet<T> {

    /**
     * The storage where a WeakReference to objects are held for both the key
     * and value. This allows retrieval functionality as well as searching.
     */
    private final WeakHashMap<T, WeakReference<T>> inner;
    /**
     * The number of times an object has been requested and an identical copy
     * was found to already be held.
     */
    private long cacheHitCount;
    /**
     * The number of times an object has been requested but no copy was
     * already held, so it was stored.
     */
    private long cacheMissCount;

    /**
     * Construct a new instance of {@link C14nCache}.
     */
    public C14nCache() {
        inner = new WeakHashMap<T, WeakReference<T>>();
        cacheMissCount = 0;
        cacheHitCount = 0;
    }

    /**
     * Return the cached copy of argument obj if it has been seen before, or
     * store and return obj if it is new.
     * 
     * @param obj object to store and return a unique instance of
     * @return an equal but not necessarily identical instance of obj
     * @throws NullPointerException if argument obj is null
     */
    public T cached(T obj) throws NullPointerException {
        if (obj == null)
            throw new NullPointerException("obj == null");
        
        final WeakReference<T> cached = inner.get(obj);
        if (cached == null) {
            inner.put(obj, new WeakReference<T>(obj));
            ++cacheMissCount;
            return obj;
        } else {
            ++cacheHitCount;
            return cached.get();
        }
    }

    /**
     * Return number of times an object has been requested and an identical copy
     * was found to already be held.
     *
     * @return number of cache hits
     */
    public long getCacheHitCount() {
        return cacheHitCount;
    }

    /**
     * Return the he number of times an object has been requested but no copy
     * was already held, so it was stored.
     *
     * @return number of cache misses
     */
    public long getCacheMissCount() {
        return cacheMissCount;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return inner.containsKey((T) o);
    }

    @Override
    public Iterator<T> iterator() {
        return inner.keySet().iterator();
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public String toString() {
        return inner.keySet().toString();
    }
}

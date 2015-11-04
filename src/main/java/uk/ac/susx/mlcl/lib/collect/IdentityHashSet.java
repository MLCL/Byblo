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

import java.io.Serializable;
import java.util.*;

/**
 * A Set implementation, that uses object identity rather than equality to
 * compare elements.
 *
 * This implementation is backed by an IdentityHashMap, which is somewhat space
 * inefficient, but should be robust. See the IdentityHashMap documentation for
 * details on how that works.
 *
 * @author Hamish Morgan
 * @param <T> the type of elements maintained by this set
 */
public class IdentityHashSet<T> extends AbstractSet<T>
        implements Set<T>, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Object PRESENT = new Object();

    private IdentityHashMap<T, Object> inner;

    /**
     * Create a new IdentityHashSet object with default capacity.
     */
    public IdentityHashSet() {
        inner = new IdentityHashMap<T, Object>();
    }

    /**
     * Create a new IdentityHashSet object, filling it with the contents of the
     * given collection.
     *
     * @param collection
     */
    public IdentityHashSet(Collection<? extends T> collection) {
        inner = new IdentityHashMap<T, Object>(collection.size());
        addAll(collection);
    }

    /**
     *
     * @param expectedMaxSize
     * @throws IllegalArgumentException if expectedMaxSize is negative
     */
    public IdentityHashSet(int expectedMaxSize) {
        inner = new IdentityHashMap<T, Object>(expectedMaxSize);
    }

    @Override
    public boolean contains(Object o) {
        return inner.containsKey(o);
    }

    @Override
    public boolean add(T e) {
        return inner.put(e, PRESENT) == null;
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
    public void clear() {
        inner.clear();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return inner.remove(o) != null;
    }

    @Override
    public Object clone() {
        try {
            IdentityHashSet<T> newSet = (IdentityHashSet<T>) super.clone();
            newSet.inner = (IdentityHashMap<T, Object>) inner.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}

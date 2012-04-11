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

import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import uk.ac.susx.mlcl.lib.collect.Int2ObjectOpenHashBiMap;
import uk.ac.susx.mlcl.lib.collect.RandomAccessableInt2ObjectBiMap;

/**
 *
 *
 * @param <T> type of object being indexed.
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class BackoffEnumerator<T> implements Enumerator<T> {

    private static final long serialVersionUID = 2L;

    private static final Random random = new Random();

    private final int maxSize;

    private final RandomAccessableInt2ObjectBiMap<T>[] maps;

    private final Enumerator<T> backoff;

    protected BackoffEnumerator(
            int maxSize, Enumerator<T> backoff,
            RandomAccessableInt2ObjectBiMap<T> maps[]) {
        this.maxSize = maxSize;
        this.maps = maps;
        this.backoff = backoff;
    }

    protected BackoffEnumerator(int maxSize, Enumerator<T> backoff) {
        this(maxSize, backoff, BackoffEnumerator.<T>createMaps(2));
    }

    private static <T> RandomAccessableInt2ObjectBiMap<T>[] createMaps(int size) {
        RandomAccessableInt2ObjectBiMap<T>[] maps =
                (RandomAccessableInt2ObjectBiMap<T>[]) new RandomAccessableInt2ObjectBiMap[size];
        for (int i = 0; i < size; i++)
            maps[i] = new Int2ObjectOpenHashBiMap<T>();
        return maps;
    }

    @Override
    public synchronized final int indexOf(final T obj) {
        Checks.checkNotNull("obj", obj);
        int i = 0;
        int id = NULL_INDEX;

        while (i < maps.length && id == NULL_INDEX) {
            id = maps[i].inverse().getInt(obj);
            ++i;
        }

        if (id == NULL_INDEX)
            id = backoff.indexOf(obj);

        move(i, i - 1, id);
        return id;
    }

    @Override
    public synchronized final T valueOf(final int id) {
        Checks.checkRangeIncl("id", id, 0, Integer.MAX_VALUE);

        int i = 0;
        T value = null;

        while (i < maps.length && value == null) {
            value = maps[i].get(id);
            ++i;
        }
        if (value == null)
            value = backoff.valueOf(id);

        move(i, i - 1, id);
        assert value != null : "value is null";
        return value;
    }

    private void move(int from, int to, int id) {
        if (to == maps.length) {
            // Destination is the backoff, so only need to remove from the source
            maps[from].remove(id);
        } else {
            // Destination is the maps

            if (maps[to].size() > maxSize) {
                // Insufficient space in desintation sp demote a random element
                move(to, to + 1, maps[to].randomIntKey());
            }

            if (from == maps.length) {
                // Source is the backoff 
                maps[to].put(id, backoff.valueOf(id));
            } else {
                // Source is another map
                maps[to].put(id, maps[from].remove(id));
            }
        }
    }

    @Override
    public String toString() {
        return maps.toString();
    }

    @Override
    public Iterator<Map.Entry<Integer, T>> iterator() {
        return backoff.iterator();
    }

}

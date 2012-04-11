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
 * POSSIBILITY OF SUCH DAMAGE.To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.susx.mlcl.lib.collect;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author hiam20
 */
/**
 *
 * @param <T>
 */
public class RandomAccessableInt2ObjectOpenHashMap<T>
        extends Int2ObjectOpenHashMap<T>
        implements RandomAccessableInt2ObjectMap<T> {

    private static final long serialVersionUID = 1L;

    public RandomAccessableInt2ObjectOpenHashMap(int[] k, T[] v) {
        super(k, v);
    }

    public RandomAccessableInt2ObjectOpenHashMap(int[] k, T[] v, float f) {
        super(k, v, f);
    }

    public RandomAccessableInt2ObjectOpenHashMap(Int2ObjectMap<T> m) {
        super(m);
    }

    public RandomAccessableInt2ObjectOpenHashMap(Int2ObjectMap<T> m, float f) {
        super(m, f);
    }

    public RandomAccessableInt2ObjectOpenHashMap(Map<? extends Integer, ? extends T> m) {
        super(m);
    }

    public RandomAccessableInt2ObjectOpenHashMap(Map<? extends Integer, ? extends T> m, float f) {
        super(m, f);
    }

    public RandomAccessableInt2ObjectOpenHashMap() {
    }

    public RandomAccessableInt2ObjectOpenHashMap(int expected) {
        super(expected);
    }

    public RandomAccessableInt2ObjectOpenHashMap(int expected, float f) {
        super(expected, f);
    }

    @Override
    public Entry<T> randomEntry() {
        final int k = randomKey();
        return new BasicEntry<T>(k, get(k));
    }

    @Override
    public Entry<T> randomEntry(Random rand) {
        final int k = randomKey(rand);
        return new BasicEntry<T>(k, get(k));
    }

    @Override
    public T randomValue() {
        return get(randomKey());
    }

    @Override
    public T randomValue(Random rand) {
        return get(randomKey(rand));
    }

    @Override
    public Integer randomKey(Random rand) {
        return randomKey(rand.nextInt(n));
    }

    @Override
    public Integer randomKey() {
        return randomKey((int) Math.floor(Math.random() * n));
    }

    @Override
    public int randomIntKey(Random rand) {
        return randomKey(rand.nextInt(n));
    }

    @Override
    public int randomIntKey() {
        return randomKey((int) Math.floor(Math.random() * n));
    }

    private int randomKey(int rand) {
        if (isEmpty())
            throw new IllegalStateException("Can't access a random elements of an empty map.");
        int i = rand % n;
        while (!used[i])
            i = (i + 1) % n;
        return key[i];
    }

}

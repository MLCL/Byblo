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
package uk.ac.susx.mlcl.byblo.io;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 * Wraps a {@link WeightedEventsSource} to produce complete feature
 * vectors instead of just individual entry/feature records.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedTokenPairVectorSource
        implements SeekableObjectSource<Indexed<SparseDoubleVector>, Tell> {

    private final WeightedTokenPairSource inner;

    private Weighted<TokenPair> next;

    private Tell tell;

    public WeightedTokenPairVectorSource(
            WeightedTokenPairSource inner) throws IOException {
        this.inner = inner;
        tell = inner.position();
        next = null;
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext() || next != null;
    }

    @Override
    public Indexed<SparseDoubleVector> read() throws IOException {
        if (next == null) {
            readNext();
        }
        Int2DoubleMap features = new Int2DoubleOpenHashMap();
        Weighted<TokenPair> start = next;
        int cardinality = 0;
        do {
            features.put(next.record().id2(), next.weight());
            cardinality = Math.max(cardinality, next.record().id2() + 1);
            // XXX position() should not need to be called every iteration
            tell = inner.position();
            readNext();
        } while (next != null && next.record().id1() == start.record().
                id1());

        SparseDoubleVector v = toDoubleVector(features, cardinality);

        return new Indexed<SparseDoubleVector>(start.record().id1(), v);
    }

    @Override
    public void position(Tell offset) throws IOException {
        inner.position(offset);
        tell = offset;
        readNext();
    }

    @Override
    public Tell position() throws IOException {
        return tell;
    }

    private void readNext() throws IOException {
        try {
            next = inner.hasNext() ? inner.read() : null;
        } catch (CharacterCodingException e) {
            next = null;
            throw e;
        }
    }

    public static SparseDoubleVector toDoubleVector(Int2DoubleMap map,
                                                    int cardinality) {
        if (map == null) {
            throw new NullPointerException();
        }
        if (cardinality < 0) {
            throw new IllegalArgumentException();
        }

        List<Int2DoubleMap.Entry> entries = new ArrayList<Int2DoubleMap.Entry>(map.int2DoubleEntrySet());
        Collections.sort(entries, new Comparator<Int2DoubleMap.Entry>() {

            @Override
            public final int compare(final Int2DoubleMap.Entry t,
                                     final Int2DoubleMap.Entry t1) {
                return t.getIntKey() - t1.getIntKey();
            }

        });

        int[] keys = new int[entries.size()];
        double[] values = new double[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            keys[i] = entries.get(i).getIntKey();
            values[i] = entries.get(i).getDoubleValue();
        }

        SparseDoubleVector vec = new SparseDoubleVector(keys, values,
                                                        cardinality, keys.length);
        vec.compact();
        return vec;
    }

}

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

import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.*;

import javax.annotation.WillClose;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Wraps a (something) to produce complete feature
 * vectors instead of just individual entry/feature records.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FastWeightedTokenPairVectorSource
        implements SeekableObjectSource<Indexed<SparseDoubleVector>, Tell>,
        Closeable {

    private final SeekableDataSource inner;

    private int next_id1;

    private int next_id2;

    private double next_weight;

    private Tell tell;

    public FastWeightedTokenPairVectorSource(
            SeekableDataSource inner) throws IOException {
        this.inner = inner;
        tell = inner.position();
        next_id1 = -1;
        next_id2 = -1;
        next_weight = 0;
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.canRead() || next_id1 != -1;
    }

    @Override
    public Indexed<SparseDoubleVector> read() throws IOException {
        if (next_id1 == -1) {
            readNext();
        }
        Int2DoubleMap features = new Int2DoubleOpenHashMap();

        int id1 = next_id1;
//        Weighted<TokenPair> start = next;
        int cardinality = 0;
        do {
            features.put(next_id2, next_weight);
            cardinality = Math.max(cardinality, next_id2 + 1);
            // XXX position() should not need to be called every iteration
            tell = inner.position();
            readNext();
        } while (next_id1 != -1 && next_id1 == id1);

        SparseDoubleVector v = toDoubleVector(features, cardinality);

        return new Indexed<SparseDoubleVector>(id1, v);
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
            if (inner.canRead()) {
                next_id1 = inner.readInt();
                next_id2 = inner.readInt();
                next_weight = inner.readDouble();
                inner.endOfRecord();
            } else {
                next_id1 = -1;
            }
        } catch (CharacterCodingException e) {
            next_id1 = -1;
            throw e;
        }
    }

    @Override
    @WillClose
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
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

    public static FastWeightedTokenPairVectorSource open(
            File file, Charset charset, DoubleEnumerating idx, boolean skip1, boolean skip2)
            throws IOException {
        SeekableDataSource tsv = new TSV.Source(file, charset);


        if (skip1) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column != null && column == 0;
                }

            });
        }
        if (skip2) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column != null && (column + 1) % 2 == 0;
                }

            });
        }
        if (!idx.isEnumeratedEntries()) {
            tsv = Enumerated.enumerated(tsv, idx.getEntryEnumerator(),
                    new Predicate<Integer>() {

                        @Override
                        public boolean apply(Integer column) {
                            return column != null && column == 0;
                        }

                    });
        }

        if (!idx.isEnumeratedFeatures()) {
            tsv = Enumerated.enumerated(tsv, idx.getFeatureEnumerator(),
                    new Predicate<Integer>() {

                        @Override
                        public boolean apply(Integer column) {
                            return column != null && (column + 1) % 2 == 0;
                        }

                    });
        }

        tsv = Compact.compact(tsv, 3);

        return new FastWeightedTokenPairVectorSource(tsv);
    }

}

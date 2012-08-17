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
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.Compact;
import uk.ac.susx.mlcl.lib.io.DataSink;
import uk.ac.susx.mlcl.lib.io.Deltas;
import uk.ac.susx.mlcl.lib.io.Enumerated;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.TSV;

/**
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FastWeightedTokenPairVectorSink
        implements ObjectSink<Indexed<SparseDoubleVector>>, Flushable, Closeable {

    private final DataSink inner;

    public FastWeightedTokenPairVectorSink(DataSink inner) {
        this.inner = inner;
    }

    @Override
    public void write(Indexed<SparseDoubleVector> record) throws IOException {
        int entryId = record.key();
        SparseDoubleVector vec = record.value();
        for (int i = 0; i < vec.size; i++) {
            inner.writeInt(entryId);
            inner.writeInt(vec.keys[i]);
            inner.writeDouble(vec.values[i]);
            inner.endOfRecord();
        }
    }

    @Override
    public void flush() throws IOException {
        if (inner instanceof Flushable)
            ((Flushable) inner).flush();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    public static FastWeightedTokenPairVectorSink open(
            File file, Charset charset, DoubleEnumerating idx, boolean skip1, boolean skip2, boolean compact)
            throws IOException {
        DataSink tsv = new TSV.Sink(file, charset);


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

        if (compact)
            tsv = Compact.compact(tsv, 3);

        return new FastWeightedTokenPairVectorSink(tsv);
    }

}

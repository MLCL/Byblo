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
import uk.ac.susx.mlcl.lib.io.Compact;
import uk.ac.susx.mlcl.lib.io.DataSink;
import uk.ac.susx.mlcl.lib.io.Deltas;
import uk.ac.susx.mlcl.lib.io.Enumerated;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.TSV;

/**
 * An <tt>WeightedTokenPairSink</tt> object is used to store
 * {@link TokenPair} objects in a flat file.
 *
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each
 * {@link TokenPair} corresponds to a single TSV record; i.e one line per object
 * consisting of two entries, and their weight. In compact mode each TSV record
 * consists of a single entry followed by the second-entry/weight pairs from all
 * sequentially written
 * {@link WeightedTokenPairSink} objects that share the same first entry.</p>
 *
 * Verbose mode example:
 * <pre>
 *      entry1  entry1    weight1
 *      entry1  entry2    weight2
 *      entry2  entry3    weight3
 *      entry3  entry2    weight4
 *      enrty3  entry4    weight5
 *      enrty3  entry1    weight6
 * </pre>
 *
 * Equivalent compact mode example:
 * <pre>
 *      entry1  entry1    weight1 entry2    weight2
 *      entry2  entry3    weight3
 *      entry3  entry2    weight4 entry4    weight5 entry1    weight6
 * </pre>
 *
 * <p>Compact mode is the default behavior, since it can reduce file sizes by
 * approximately 50%, with corresponding reductions in I/O overhead.</p>
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedTokenPairSink
        implements ObjectSink<Weighted<TokenPair>>, Closeable, Flushable {

    private DataSink inner;

    public WeightedTokenPairSink(DataSink inner) {
        this.inner = inner;
    }

    @Override
    public void write(Weighted<TokenPair> record) throws IOException {
        inner.writeInt(record.record().id1());
        inner.writeInt(record.record().id2());
        inner.writeDouble(record.weight());
        inner.endOfRecord();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    @Override
    public void flush() throws IOException {
        if (inner instanceof Flushable)
            ((Flushable) inner).flush();
    }

    public static WeightedTokenPairSink open(
            File file, Charset charset, DoubleEnumerating idx, boolean skip1, boolean skip2, boolean compact)
            throws IOException {
        DataSink tsv = new TSV.Sink(file, charset);


        if (skip1) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }

            });
        }

        if (skip2) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return (column + 1) % 2 == 0;
                }

            });
        }

        if (!idx.isEnumeratedEntries()) {
            tsv = Enumerated.enumerated(tsv, idx.getEntryEnumerator(),
                                        new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }

            });
        }

        if (!idx.isEnumeratedFeatures()) {
            tsv = Enumerated.enumerated(tsv, idx.getFeatureEnumerator(),
                                        new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return (column + 1) % 2 == 0;
                }

            });
        }

        if (compact)
            tsv = Compact.compact(tsv, 3);

        return new WeightedTokenPairSink(tsv);
    }

}

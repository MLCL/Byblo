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

import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumeratingDeligate;
import com.google.common.base.Predicate;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerating;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * A <tt>WeightedTokenPairSource</tt> object is used to retrieve
 * {@link TokenPair} objects from a flat file.
 *
 * @param <P>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see WeightedEntryPairSink
 */
public class WeightedTokenPairSource
        implements SeekableSource<Weighted<TokenPair>, Tell>, Closeable {

    private final SeekableDataSource inner;

    public WeightedTokenPairSource(
            SeekableDataSource inner//, IndexDeligatePair indexDeligate
            )
            throws FileNotFoundException, IOException {

        this.inner = inner;
    }

    @Override
    public Weighted<TokenPair> read() throws IOException {
        final int id1 = inner.readInt();
        final int id2 = inner.readInt();
        final double weight = inner.readDouble();
        inner.endOfRecord();
        return new Weighted<TokenPair>(new TokenPair(id1, id2), weight);
    }

    public WeightedTokenPairVectorSource getVectorSource() throws IOException {
        return new WeightedTokenPairVectorSource(this);
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.canRead();
    }

    @Override
    public void position(Tell offset) throws IOException {
        inner.position(offset);
    }

    @Override
    public Tell position() throws IOException {
        return inner.position();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        DoubleEnumerating idx = new DoubleEnumeratingDeligate(Enumerating.DEFAULT_TYPE, false, false, null, null, false, false);
        final WeightedTokenPairSource srcA = WeightedTokenPairSource.open(
                a, charset, idx);
        final WeightedTokenPairSource srcB = WeightedTokenPairSource.open(
                b, charset, idx);

        List<Weighted<TokenPair>> listA = IOUtil.readAll(srcA);
        List<Weighted<TokenPair>> listB = IOUtil.readAll(srcB);
        Comparator<Weighted<TokenPair>> c =
                Comparators.fallback(
                Weighted.recordOrder(TokenPair.indexOrder()),
                Weighted.<TokenPair>weightOrder());
        Collections.sort(listA, c);
        Collections.sort(listB, c);
        return listA.equals(listB);
    }

    public static WeightedTokenPairSource open(
            File file, Charset charset, DoubleEnumerating idx)
            throws IOException {
        SeekableDataSource tsv = new TSV.Source(file, charset);


        if (idx.isEnumeratorSkipIndexed1()) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }

            });
        }
        if (idx.isEnumeratorSkipIndexed2()) {
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

        tsv = Compact.compact(tsv, 3);

        return new WeightedTokenPairSource(tsv);
    }

}

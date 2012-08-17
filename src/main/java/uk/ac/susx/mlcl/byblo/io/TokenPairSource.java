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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.byblo.enumerators.DoubleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;
import uk.ac.susx.mlcl.lib.io.Compact;
import uk.ac.susx.mlcl.lib.io.Deltas;
import uk.ac.susx.mlcl.lib.io.Enumerated;
import uk.ac.susx.mlcl.lib.io.SeekableDataSource;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.io.TSV;
import uk.ac.susx.mlcl.lib.io.Tell;

/**
 * An <tt>TokenPairSource</tt> object is used to retrieve
 * {@link TokenPair} objects from a flat file.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see TokenPairSink
 */
public class TokenPairSource
        implements SeekableObjectSource<TokenPair, Tell>, Closeable {

    private final SeekableDataSource inner;

    public TokenPairSource(SeekableDataSource inner)
            throws FileNotFoundException, IOException {
        this.inner = inner;
    }

    @Override
    public TokenPair read() throws IOException {
        try {
            final int id1 = inner.readInt();
            final int id2 = inner.readInt();
            inner.endOfRecord();
            return new TokenPair(id1, id2);
        } catch (Throwable ex) {
            throw new IOException("Error at position " + position(), ex);
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.canRead();
    }

    @Override
    public void position(Tell p) throws IOException {
        inner.position(p);
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

    public static TokenPairSource open(
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
                    return column != null && column > 0;
                }

            });
        }

        tsv = Compact.compact(tsv, 2);

        if (!idx.isEnumeratedEntries() || !idx.isEnumeratedFeatures()) {
            @SuppressWarnings("unchecked")
            Enumerator<String>[] enumerators = (Enumerator<String>[]) new Enumerator[2];
            if (!idx.isEnumeratedEntries())
                enumerators[0] = idx.getEntryEnumerator();
            if (!idx.isEnumeratedFeatures())
                enumerators[1] = idx.getFeatureEnumerator();
            tsv = Enumerated.enumerated(tsv, enumerators);
        }
        return new TokenPairSource(tsv);
    }

}

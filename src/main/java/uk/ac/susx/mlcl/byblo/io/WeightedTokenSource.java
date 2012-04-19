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

import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * An <tt>WeightedTokenSource</tt> object is used to retrieve {@link Token}
 * objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntrySink
 */
public class WeightedTokenSource
        implements SeekableSource<Weighted<Token>, Tell>, Closeable {

    private static final Log LOG = LogFactory.getLog(WeightedTokenSource.class);

    private final SeekableDataSource inner;

    public WeightedTokenSource(SeekableDataSource inner)
            throws FileNotFoundException, IOException {
        this.inner = inner;
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
    public Weighted<Token> read() throws IOException {
        final int tokenId = inner.readInt();
        final double weight = inner.readDouble();
        inner.endOfRecord();

        return new Weighted<Token>(new Token(tokenId), weight);

    }

    public static WeightedTokenSource open(
            File f, Charset charset, SingleEnumerating idx, boolean skip1) throws IOException {
        SeekableDataSource tsv = new TSV.Source(f, charset);

        if (skip1) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }

            });
        }

        tsv = Compact.compact(tsv, 2);

        if (!idx.isEnumerationEnabled())
            tsv = Enumerated.enumerated(tsv, idx.getEnumerator());
        return new WeightedTokenSource(tsv);
    }
//
//    public static boolean equal(File a, File b, Charset charset,
//                                boolean askip1, boolean bskip1) throws IOException {
//        SingleEnumerating idx = new SingleEnumeratingDeligate(EnumeratorType.JDBC, true, null);
//        final WeightedTokenSource srcA = open(a, charset, idx, askip1);
//        final WeightedTokenSource srcB = open(b, charset, idx, bskip1);
//
//        List<Weighted<Token>> listA = IOUtil.readAll(srcA);
//        List<Weighted<Token>> listB = IOUtil.readAll(srcB);
//        Comparator<Weighted<Token>> c = Comparators.fallback(
//                Weighted.recordOrder(Token.indexOrder()),
//                Weighted.<Token>weightOrder());
//        Collections.sort(listA, c);
//        Collections.sort(listB, c);
//        return listA.equals(listB);
//    }
//    
//    public static boolean equal(File a, File b, Charset charset,
//                                SingleEnumeratingDeligate aidx, SingleEnumeratingDeligate bidx,
//                                boolean askip1, boolean bskip1) throws IOException {
//        final WeightedTokenSource srcA = open(a, charset, aidx, askip1);
//        final WeightedTokenSource srcB = open(b, charset, bidx, bskip1);
//
//        List<Weighted<Token>> listA = IOUtil.readAll(srcA);
//        List<Weighted<Token>> listB = IOUtil.readAll(srcB);
//        Comparator<Weighted<Token>> c = Comparators.fallback(
//                Weighted.recordOrder(Token.indexOrder()),
//                Weighted.<Token>weightOrder());
//        Collections.sort(listA, c);
//        Collections.sort(listB, c);
//        return listA.equals(listB);
//    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.canRead();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    public static class WTStatsSource
            extends WeightStatsObjectSource<Token> {

        private int minId = 0;

        private int maxId = 0;

        public WTStatsSource(WeightedTokenSource inner) {
            super(inner);
        }

        public int getMaxId() {
            return maxId;
        }

        public int getMinId() {
            return minId;
        }

        @Override
        public Weighted<Token> read() throws IOException {
            final Weighted<Token> wt = super.read();
            minId = Math.min(minId, wt.record().id());
            maxId = Math.max(maxId, wt.record().id());
            return wt;
        }

    }
}

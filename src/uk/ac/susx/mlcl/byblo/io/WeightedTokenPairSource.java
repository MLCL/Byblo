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
import uk.ac.susx.mlcl.byblo.enumerators.Enumerator;
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

//    private IndexDeligatePair indexDeligate;
//
//    private int prev_id1 = 0;
//
//    private int prev_id2 = 0;
//
//    private boolean token1_continuation = false;
//
//    private long count = 0;
//
    private final SeekableDataSource inner;

    public WeightedTokenPairSource(
            SeekableDataSource inner//, IndexDeligatePair indexDeligate
            )
            throws FileNotFoundException, IOException {

        this.inner = inner;
//        this.indexDeligate = indexDeligate;
    }
//
//    public long getCount() {
//        return count;
//    }

    @Override
    public Weighted<TokenPair> read() throws IOException {
        final int id1 = inner.readInt();
        final int id2 = inner.readInt();
        final double weight = inner.readDouble();
        inner.endOfRecord();
        return new Weighted<TokenPair>(new TokenPair(id1, id2), weight);
//        
//        
//        final int tokenId1;
//        if (token1_continuation) {
//            tokenId1 = prev_id1;
//        } else {
//            tokenId1 = readToken1();
//            assert tokenId1 >= 0 : "Negative token 1 id read";
//            prev_id1 = tokenId1;
//            prev_id2 = 0;
//            token1_continuation = true;
//        }
//
//
//        if (!hasNext() || inner.isEndOfRecordNext()) {
//            inner.endOfRecord();
//            throw new IOException(
//                    "Found weighte entry pair record with second entries.");
//        }
//
//        final int tokenId2 = readToken2();
//        assert tokenId2 >= 0 : "Negative token 2 id read";
//        prev_id2 = tokenId2;
//        final double weight = readWeight();
//
//        final Weighted<TokenPair> record = new Weighted<TokenPair>(
//                new TokenPair(tokenId1, tokenId2), weight);
//        ++count;
//
//
//        if (inner.isEndOfRecordNext()) {
//            inner.endOfRecord();
//            token1_continuation = false;
//            prev_id2 = 0;
//        }
//
//        return record;
    }
//
//    protected int readToken1() throws IOException {
//        if (indexDeligate.isPreindexedTokens1()) {
//            if (indexDeligate.isSkipindexed1())
//                return prev_id1 + inner.readInt();
//            else
//                return inner.readInt();
//        } else
//            return indexDeligate.getEnumerator1().index(inner.readString());
//    }
//
//    protected int readToken2() throws IOException {
//        if (indexDeligate.isPreindexedTokens2()) {
//            if (indexDeligate.isSkipindexed2()) {
//                return prev_id2 + inner.readInt();
//            } else {
//                return inner.readInt();
//            }
//        } else
//            return indexDeligate.getEnumerator2().index(inner.readString());
//    }
//
//    protected double readWeight() throws IOException {
//        return inner.readDouble();
//    }

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
//        prev_id2 = offset.value(Integer.class);
//        offset = offset.next();
//        prev_id1 = offset.value(Integer.class);
//        offset = offset.next();
//        token1_continuation = offset.value(Boolean.class);
//        inner.position(offset.next());
//        
//        this.token1_continuation = p.isToken1_continuation();
//        this.prev_id1 = p.getPrev_id1();
//        this.prev_id2 = p.getPrev_id2();
//        inner.position(p.getInner());
    }

    @Override
    public Tell position() throws IOException {
        return inner.position();
//        return inner.position().
//                push(Boolean.class, token1_continuation).
//                push(Integer.class, prev_id1).
//                push(Integer.class, prev_id2);
//        
//        return new Tell<P>(inner.position(),
//                        token1_continuation, prev_id1, prev_id2);
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
//    public static final class Tell<P> {
//
//        private final P inner;
//
//        private final boolean token1_continuation;
//
//        private final int prev_id1;
//
//        private final int prev_id2;
//
//        public Tell(P inner, boolean token1_continuation, int prev_id1,
//                    int prev_id2) {
//            this.inner = inner;
//            this.token1_continuation = token1_continuation;
//            this.prev_id1 = prev_id1;
//            this.prev_id2 = prev_id2;
//        }
//
//        public P getInner() {
//            return inner;
//        }
//
//        public int getPrev_id1() {
//            return prev_id1;
//        }
//
//        public int getPrev_id2() {
//            return prev_id2;
//        }
//
//        public boolean isToken1_continuation() {
//            return token1_continuation;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj == null)
//                return false;
//            if (getClass() != obj.getClass())
//                return false;
//            final Tell other = (Tell) obj;
//            if (this.inner != other.inner && (this.inner == null || !this.inner.
//                                              equals(other.inner)))
//                return false;
//            if (this.prev_id1 != other.prev_id1)
//                return false;
//            if (this.prev_id2 != other.prev_id2)
//                return false;
//            return true;
//        }
//
//        @Override
//        public int hashCode() {
//            int hash = 7;
//            hash = 83 * hash + (this.inner != null ? this.inner.hashCode() : 0);
//            hash = 83 * hash + this.prev_id1;
//            hash = 83 * hash + this.prev_id2;
//            return hash;
//        }
//    }

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


//        
//        if (!idx.isPreindexedTokens1() || !idx.isPreindexedTokens2()) {
//            Enumerator<String>[] enumerators = (Enumerator<String>[]) new Enumerator[2];
//            if (!idx.isPreindexedTokens1())
//                enumerators[0] = idx.getEnumerator1();
//            if (!idx.isPreindexedTokens2())
//                enumerators[1] = idx.getEnumerator2();
//            tsv = Enumerated.enumerated(tsv, enumerators);
//        }

        return new WeightedTokenPairSource(tsv);
    }

}

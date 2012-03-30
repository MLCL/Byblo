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

import com.google.common.base.Function;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Lexer;
import uk.ac.susx.mlcl.lib.io.Lexer.Tell;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 * A <tt>WeightedTokenPairSource</tt> object is used to retrieve
 * {@link TokenPair} objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see WeightedEntryPairSink
 */
public class WeightedTokenPairSource
        implements SeekableSource<Weighted<TokenPair>, Lexer.Tell>, Closeable {

    private IndexDeligatePair indexDeligate;
//    
//    private final Function<String, Integer> tokenDecoder1;
//
//    private final Function<String, Integer> tokenDecoder2;

    private Weighted<TokenPair> previousRecord = null;

    private long count = 0;

    private final TSVSource inner;

    public WeightedTokenPairSource(
            TSVSource inner, IndexDeligatePair indexDeligate
//            Function<String, Integer> tokenDecoder1,
//            Function<String, Integer> tokenDecoder2
            )
            throws FileNotFoundException, IOException {

        this.inner = inner;
        this.indexDeligate = indexDeligate;
//        this.tokenDecoder1 = tokenDecoder1;
//        this.tokenDecoder2 = tokenDecoder2;
    }

//    public WeightedTokenPairSource(TSVSource inner)
//            throws FileNotFoundException, IOException {
//        this.inner = inner;
//        indexDeligate = new IndexDeligatePair();
////        this.tokenDecoder1 = Token.enumeratedDecoder();
////        this.tokenDecoder2 = Token.enumeratedDecoder();
//    }
//
//    public Function<String, Integer> getTokenDecoder2() {
//        return tokenDecoder2;
//    }
//
//    public Function<String, Integer> getTokenDecoder1() {
//        return tokenDecoder1;
//    }

    public long getCount() {
        return count;
    }

    @Override
    public Weighted<TokenPair> read() throws IOException {

        final int tokenId1 = readToken1();

        if (!hasNext() || inner.isEndOfRecordNext()) {
            inner.endOfRecord();
            throw new SingletonRecordException(
                    inner, "Found weighte entry pair record with second entries.");
        }

        final int tokenId2 = readToken2();
        final double weight = readWight();

        final Weighted<TokenPair> record = new Weighted<TokenPair>(
                new TokenPair(tokenId1, tokenId2), weight);
        ++count;

        if (inner.hasNext() && !inner.isEndOfRecordNext()) {
            previousRecord = record;
        }

        if (inner.hasNext() && inner.isEndOfRecordNext()) {
            inner.endOfRecord();
            previousRecord = null;
        }

        return record;
    }

    protected int readToken1() throws IOException {
        if (previousRecord == null) {
            if(indexDeligate.isPreindexedTokens1())
                return inner.readInt();
            else
                return indexDeligate.getEnumerator1().index(inner.readString());
//                indexDeligate.getDecoder1().apply(inner.readString());
//            else
//            return tokenDecoder1.apply(inner.readString());
        } else {
            return previousRecord.record().id1();
        }
    }

    protected int readToken2() throws IOException {
        if(indexDeligate.isPreindexedTokens2())
                return inner.readInt();
            else
                return indexDeligate.getEnumerator2().index(inner.readString());
        
//        return tokenDecoder2.apply(inner.readString());
    }

    protected double readWight() throws IOException {
        return inner.readDouble();
    }

    public WeightedTokenPairVectorSource getVectorSource() {
        return new WeightedTokenPairVectorSource(this);
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final Enumerator<String> stringIndex = Enumerators.newDefaultStringEnumerator();
        IndexDeligatePair idx = new IndexDeligatePair(false, false);
        final WeightedTokenPairSource srcA = new WeightedTokenPairSource(
                new TSVSource(a, charset),idx
//                Token.stringDecoder(stringIndex),
//                Token.stringDecoder(stringIndex)
                        );
        final WeightedTokenPairSource srcB = new WeightedTokenPairSource(
                new TSVSource(b, charset), idx
//                Token.stringDecoder(stringIndex),
                //                Token.stringDecoder(stringIndex)
                        );


        List<Weighted<TokenPair>> listA = IOUtil.readAll(srcA);
        List<Weighted<TokenPair>> listB = IOUtil.readAll(srcB);
        Comparator<Weighted<TokenPair>> c =
                Comparators.fallback(
                Weighted.recordOrder(TokenPair.indexOrder()),
                Weighted.<TokenPair>weightOrder());
        Collections.sort(listA, c);
        Collections.sort(listB, c);
        return listA.equals(listB);
//
//        boolean equal = true;
//        while (equal && srcA.hasNext() && srcB.hasNext()) {
//            final Weighted<TokenPair> recA = srcA.read();
//            final Weighted<TokenPair> recB = srcB.read();
//            equal = recA.record().id1() == recB.record().id1()
//                    && recA.record().id2() == recB.record().id2()
//                    && recA.weight() == recB.weight();
//        }
//        return equal && srcA.hasNext() == srcB.hasNext();
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    @Override
    public void position(Tell p) throws IOException {
        inner.position(p);
    }

    @Override
    public Tell position() throws IOException {
        return inner.position();
    }

    public double percentRead() throws IOException {
        return inner.percentRead();
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }

}

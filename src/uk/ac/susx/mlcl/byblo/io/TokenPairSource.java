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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * An <tt>TokenPairSource</tt> object is used to retrieve
 * {@link EntryFeature} objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntryFeatureSink
 */
public class TokenPairSource
        implements SeekableSource<TokenPair, Tell>, Closeable {

//    private IndexDeligatePair indexDeligate;
//    private TokenPair previousRecord = null;
//
//    private long count = 0;
    private final SeekableDataSource inner;

    public TokenPairSource(
            SeekableDataSource inner //            ,
            //            IndexDeligatePair indexDeligate
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
    public TokenPair read() throws IOException {
        final int id1 = inner.readInt();
        final int id2 = inner.readInt();
        inner.endOfRecord();
        return new TokenPair(id1, id2);




//        final int id1;
////        if (previousRecord == null) {
//        id1 = readToken1();
////        } else {
////            id1 = previousRecord.id1();
////        }
////
////        if (!hasNext() || inner.isEndOfRecordNext()) {
////            // Encountered an entry without any features. This is incoherent wrt
////            // the task at hand, but quite a common scenario in general feature
////            // extraction. Throw an exception which is caught for end user input
////            inner.endOfRecord();
////            throw new IOException("Found entry/feature record with no features.");
////        }
//
//        final int id2 = readToken2();
//        final TokenPair record = new TokenPair(id1, id2);
//
////        if (hasNext() && !inner.isEndOfRecordNext()) {
////            previousRecord = record;
////        }
////
////        if (hasNext() && inner.isEndOfRecordNext()) {
//        inner.endOfRecord();
////            previousRecord = null;
////        }
//
////        ++count;
//        return record;
    }

//    private int readToken1() throws CharacterCodingException, IOException {
//        return inner.readInt();
////        if (indexDeligate.isPreindexedTokens1())
////            return inner.readInt();
////        else
////            return indexDeligate.getEnumerator1().index(inner.readString());
//    }
//
//    private int readToken2() throws CharacterCodingException, IOException {
//        return inner.readInt();
////        if (indexDeligate.isPreindexedTokens2())
////            return inner.readInt();
////        else
////            return indexDeligate.getEnumerator2().index(inner.readString());
//    }
    public static boolean equal(File fileA, File fileB, Charset charset)
            throws IOException {
        final Enumerator<String> stringIndex = Enumerators.
                newDefaultStringEnumerator();
        IndexDeligatePair idx = new IndexDeligatePair(false, false, stringIndex,
                                                      stringIndex);
//        final TokenPairSource srcA = new TokenPairSource(
//                new TSVSource(fileA, charset), idx);
//        final TokenPairSource srcB = new TokenPairSource(
//                new TSVSource(fileB, charset), idx);
        final TokenPairSource srcA = open(fileA, charset, idx);
        final TokenPairSource srcB = open(fileB, charset, idx);


        List<TokenPair> listA = IOUtil.readAll(srcA);
        List<TokenPair> listB = IOUtil.readAll(srcB);
        Comparator<TokenPair> c = TokenPair.indexOrder();
        Collections.sort(listA, c);
        Collections.sort(listB, c);
        return listA.equals(listB);
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
            File file, Charset charset, IndexDeligatePair idx)
            throws IOException {
        SeekableDataSource tsv = new TSV.Source(file, charset);

        if (idx.isSkipIndexed1()) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }
            });
        }
        if (idx.isSkipIndexed2()) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column > 0;
                }
            });
        }

        tsv = Compact.compact(tsv, 2);
        if (!idx.isPreindexedTokens1() || !idx.isPreindexedTokens2()) {
            Enumerator<String>[] enumerators = (Enumerator<String>[]) new Enumerator[2];
            if (!idx.isPreindexedTokens1())
                enumerators[0] = idx.getEnumerator1();
            if (!idx.isPreindexedTokens2())
                enumerators[1] = idx.getEnumerator2();
            tsv = Enumerated.enumerated(tsv, enumerators);
        }
        return new TokenPairSource(tsv);
    }
}

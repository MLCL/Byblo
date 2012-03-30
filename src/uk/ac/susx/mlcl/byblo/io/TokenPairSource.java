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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.Lexer;
import uk.ac.susx.mlcl.lib.io.Lexer.Tell;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 * An <tt>TokenPairSource</tt> object is used to retrieve
 * {@link EntryFeature} objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntryFeatureSink
 */
public class TokenPairSource implements SeekableSource<TokenPair, Lexer.Tell>, Closeable {

    private static final Log LOG = LogFactory.getLog(TokenPairSource.class);

    private IndexDeligatePair indexDeligate;
//
//    private final Function<String, Integer> tokenDecoder1;
//
//    private final Function<String, Integer> tokenDecoder2;

    private TokenPair previousRecord = null;

    private long count = 0;

    private final TSVSource inner;

    public TokenPairSource(
            TSVSource inner,
            IndexDeligatePair indexDeligate)
            throws FileNotFoundException, IOException {
        this.inner = inner;
        this.indexDeligate = indexDeligate;
    }

//    public TokenPairSource(TSVSource inner)
//            throws FileNotFoundException, IOException {
//        this(inner, Token.enumeratedDecoder(), Token.enumeratedDecoder());
//    }
//
//    public Function<String, Integer> getTokenDecoder1() {
//        return tokenDecoder1;
//    }
//
//    public Function<String, Integer> getTokenDecoder2() {
//        return tokenDecoder2;
//    }
    public long getCount() {
        return count;
    }

    @Override
    public TokenPair read() throws IOException {
        final int id1;
        if (previousRecord == null) {
            id1 = readToken1();
        } else {
            id1 = previousRecord.id1();
        }

        if (!hasNext() || inner.isEndOfRecordNext()) {
            // Encountered an entry without any features. This is incoherent wrt
            // the task at hand, but quite a common scenario in general feature
            // extraction. Throw an exception which is caught for end user input
            inner.endOfRecord();
            throw new SingletonRecordException(inner,
                                               "Found entry/feature record with no features.");
        }

        final int id2 = readToken2();
        final TokenPair record = new TokenPair(id1, id2);

        if (hasNext() && !inner.isEndOfRecordNext()) {
            previousRecord = record;
        }

        if (hasNext() && inner.isEndOfRecordNext()) {
            inner.endOfRecord();
            previousRecord = null;
        }

        ++count;
        return record;
    }

    private int readToken1() throws CharacterCodingException, IOException {
        if (indexDeligate.isPreindexedTokens1())
            return inner.readInt();
        else
            return indexDeligate.getEnumerator1().index(inner.readString());
//                   
//        return tokenDecoder1.apply(inner.readString());
    }

    private int readToken2() throws CharacterCodingException, IOException {
        if (indexDeligate.isPreindexedTokens2())
            return inner.readInt();
        else
            return indexDeligate.getEnumerator2().index(inner.readString());
//        return tokenDecoder2.apply(inner.readString());
    }

    public static boolean equal(File fileA, File fileB, Charset charset)
            throws IOException {
        final Enumerator<String> stringIndex = Enumerators.newDefaultStringEnumerator();
        IndexDeligatePair idx = new IndexDeligatePair(false, false, stringIndex, stringIndex);
        final TokenPairSource srcA = new TokenPairSource(
                new TSVSource(fileA, charset), idx);
        final TokenPairSource srcB = new TokenPairSource(
                new TSVSource(fileB, charset), idx);


        List<TokenPair> listA = IOUtil.readAll(srcA);
        List<TokenPair> listB = IOUtil.readAll(srcB);
        Comparator<TokenPair> c = TokenPair.indexOrder();
        Collections.sort(listA, c);
        Collections.sort(listB, c);
        return listA.equals(listB);
//        
//        boolean equal = true;
//        while (equal && srcA.hasNext() && srcB.hasNext()) {
//            final TokenPair recA = srcA.read();
//            final TokenPair recB = srcB.read();
//
//            equal = equal
//                    && recA.id1() == recB.id1()
//                    && recA.id2() == recB.id2();
//
//            if (!equal) {
//                LOG.info(recA + " | " + recB);
//            }
//        }
//        equal = equal && srcA.hasNext() == srcB.hasNext();
//        return equal;
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

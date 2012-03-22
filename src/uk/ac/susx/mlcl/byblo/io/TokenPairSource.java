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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
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
public class TokenPairSource implements SeekableSource<TokenPair, Lexer.Tell> {

    private static final Log LOG = LogFactory.getLog(TokenPairSource.class);

    private final Enumerator<String> stringIndex1;

    private final Enumerator<String> stringIndex2;

    private TokenPair previousRecord = null;

    private long count = 0;

    private final TSVSource inner;

    public TokenPairSource(
            TSVSource inner,
            Enumerator<String> entryIndex,
            Enumerator<String> featureIndex)
            throws FileNotFoundException, IOException {
        this.inner = inner;
        if (entryIndex == null)
            throw new NullPointerException("stringIndex1 == null");
        if (featureIndex == null)
            throw new NullPointerException("stringIndex2 == null");
        this.stringIndex1 = entryIndex;
        this.stringIndex2 = featureIndex;
    }

    public TokenPairSource(TSVSource inner,
                           Enumerator<String> combinedIndex)
            throws FileNotFoundException, IOException {
        this(inner, combinedIndex, combinedIndex);
    }

    public TokenPairSource(TSVSource inner)
            throws FileNotFoundException, IOException {
        this(inner, new SimpleEnumerator<String>());
    }

    public Enumerator<String> getStringIndex1() {
        return stringIndex1;
    }

    public Enumerator<String> getStringIndex2() {
        return stringIndex2;
    }

    public boolean isIndexCombined() {
        return getStringIndex1() == getStringIndex2();
    }

    public long getCount() {
        return count;
    }

    @Override
    public TokenPair read() throws IOException {
        final int id1;
        if (previousRecord == null) {
            id1 = readHead();
            inner.parseValueDelimiter();
        } else {
            id1 = previousRecord.id1();
        }

        if (!hasNext() || inner.isDelimiterNext()) {
            // Encountered an entry without any features. This is incoherent wrt
            // the task at hand, but quite a common scenario in general feature
            // extraction. Throw an exception which is caught for end user input
            inner.parseRecordDelimiter();
            throw new SingletonRecordException(inner,
                                               "Found entry/feature record with no features.");
        }

        final int id2 = readTail();
        final TokenPair record = new TokenPair(
                id1, id2);

        if (hasNext() && inner.isValueDelimiterNext()) {
            inner.parseValueDelimiter();
            previousRecord = record;
        }

        if (hasNext() && inner.isRecordDelimiterNext()) {
            inner.parseRecordDelimiter();
            previousRecord = null;
        }

        ++count;
        return record;
    }

    protected final int readHead() throws IOException {
        return stringIndex1.index(inner.parseString());
    }

    protected final int readTail() throws IOException {
        return stringIndex2.index(inner.parseString());
    }

    public static boolean equal(File fileA, File fileB, Charset charset)
            throws IOException {
        final Enumerator<String> stringIndex = new SimpleEnumerator<String>();
        final TokenPairSource srcA = new TokenPairSource(
                new TSVSource(fileA, charset), stringIndex);
        final TokenPairSource srcB = new TokenPairSource(
                new TSVSource(fileB, charset), stringIndex);
        boolean equal = true;
        while (equal && srcA.hasNext() && srcB.hasNext()) {
            final TokenPair recA = srcA.read();
            final TokenPair recB = srcB.read();

            equal = equal
                    && recA.id1() == recB.id1()
                    && recA.id2() == recB.id2();

            if (!equal) {
                LOG.info(recA + " | " + recB);
            }
        }
        equal = equal && srcA.hasNext() == srcB.hasNext();
        return equal;
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

}

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
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
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
        implements SeekableSource<Weighted<TokenPair>, Lexer.Tell> {

    private final Enumerator<String> stringIndex1;

    private final Enumerator<String> stringIndex2;

    private Weighted<TokenPair> previousRecord = null;

    private long count = 0;

    private final TSVSource inner;

    public WeightedTokenPairSource(
            TSVSource inner,
            Enumerator<String> stringIndex1, Enumerator<String> stringIndex2)
            throws FileNotFoundException, IOException {
        this.inner = inner;
        if (stringIndex1 == null)
            throw new NullPointerException("entryIndex == null");
        this.stringIndex1 = stringIndex1;
        this.stringIndex2 = stringIndex2;
    }

    public WeightedTokenPairSource(
            TSVSource inner,
            Enumerator<String> stringIndex)
            throws FileNotFoundException, IOException {
        this(inner, stringIndex, stringIndex);
    }

    public WeightedTokenPairSource(TSVSource inner)
            throws FileNotFoundException, IOException {
        this(inner, new SimpleEnumerator<String>());
    }

    public final Enumerator<String> getStringIndex1() {
        return stringIndex1;
    }

    public final Enumerator<String> getStringIndex2() {
        return stringIndex2;
    }

    public boolean isIndexCombined() {
        return stringIndex1 == stringIndex2;
    }

    public long getCount() {
        return count;
    }

    @Override
    public Weighted<TokenPair> read() throws IOException {
        final int tokenId1;
        if (previousRecord == null) {
            tokenId1 = readEntry1();
            inner.parseValueDelimiter();
        } else {
            tokenId1 = previousRecord.record().id1();
        }

        if (!hasNext() || inner.isDelimiterNext()) {
            inner.parseRecordDelimiter();
            throw new SingletonRecordException(inner,
                                               "Found weighte entry pair record with second entries.");
        }

        final int tokenId2 = readEntry2();
        inner.parseValueDelimiter();
        final double weight = readWight();

        final Weighted<TokenPair> record = new Weighted<TokenPair>(
                new TokenPair(tokenId1, tokenId2), weight);
        ++count;

        if (inner.isValueDelimiterNext()) {
            inner.parseValueDelimiter();
            previousRecord = record;
        }

        if (inner.hasNext() && inner.isRecordDelimiterNext()) {
            inner.parseRecordDelimiter();
            previousRecord = null;
        }

        return record;
    }

    protected int readEntry1() throws IOException {
        return stringIndex1.index(inner.parseString());
    }

    protected int readEntry2() throws IOException {
        return stringIndex2.index(inner.parseString());
    }

    protected double readWight() throws IOException {
        return inner.parseDouble();
    }

    public WeightedTokenPairVectorSource getVectorSource() {
        return new WeightedTokenPairVectorSource(this);
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final SimpleEnumerator<String> stringIndex = new SimpleEnumerator<String>();
        final WeightedTokenPairSource srcA = new WeightedTokenPairSource(
                new TSVSource(a, charset), stringIndex);
        final WeightedTokenPairSource srcB = new WeightedTokenPairSource(
                new TSVSource(b, charset), stringIndex);
        boolean equal = true;
        while (equal && srcA.hasNext() && srcB.hasNext()) {
            final Weighted<TokenPair> recA = srcA.read();
            final Weighted<TokenPair> recB = srcB.read();
            equal = recA.record().id1() == recB.record().id1()
                    && recA.record().id2() == recB.record().id2()
                    && recA.weight() == recB.weight();
        }
        return equal && srcA.hasNext() == srcB.hasNext();
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

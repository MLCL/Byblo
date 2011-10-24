/*
 * Copyright (c) 2010-2011, University of Sussex
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

import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.io.AbstractTSVSource;
import uk.ac.susx.mlcl.lib.io.Source;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A <tt>WeightedTokenPairSource</tt> object is used to retrieve
 * {@link TokenPair} objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see WeightedEntryPairSink
 */
public class WeightedTokenPairSource
        extends AbstractTSVSource<Weighted<TokenPair>>
        implements Source<Weighted<TokenPair>> {

    private final ObjectIndex<String> stringIndex1;

    private final ObjectIndex<String> stringIndex2;

    private Weighted<TokenPair> previousRecord = null;

    private long count = 0;

    public WeightedTokenPairSource(
            File file, Charset charset,
            ObjectIndex<String> stringIndex1, ObjectIndex<String> stringIndex2)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (stringIndex1 == null)
            throw new NullPointerException("entryIndex == null");
        this.stringIndex1 = stringIndex1;
        this.stringIndex2 = stringIndex2;
    }

    public WeightedTokenPairSource(
            File file, Charset charset,
            ObjectIndex<String> stringIndex)
            throws FileNotFoundException, IOException {
        this(file, charset, stringIndex, stringIndex);
    }

    public WeightedTokenPairSource(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public final ObjectIndex<String> getStringIndex1() {
        return stringIndex1;
    }

    public final ObjectIndex<String> getStringIndex2() {
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
            parseValueDelimiter();
        } else {
            tokenId1 = previousRecord.get().id1();
        }

        if (!hasNext() || isDelimiterNext()) {
            parseRecordDelimiter();
            throw new SingletonRecordException(this,
                    "Found weighte entry pair record with second entries.");
        }

        final int tokenId2 = readEntry2();
        parseValueDelimiter();
        final double weight = readWight();

        final Weighted<TokenPair> record = new Weighted<TokenPair>(
                new TokenPair(tokenId1, tokenId2), weight);
        ++count;

        if (isValueDelimiterNext()) {
            parseValueDelimiter();
            previousRecord = record;
        }

        if (hasNext() && isRecordDelimiterNext()) {
            parseRecordDelimiter();
            previousRecord = null;
        }

        return record;
    }

    protected int readEntry1() throws IOException {
        return stringIndex1.get(parseString());
    }

    protected int readEntry2() throws IOException {
        return stringIndex2.get(parseString());
    }

    protected double readWight() throws IOException {
        return parseDouble();
    }

    public WeightedTokenPairVectorSource getVectorSource() {
        return new WeightedTokenPairVectorSource(this);
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final ObjectIndex<String> stringIndex = new ObjectIndex<String>();
        final WeightedTokenPairSource srcA = new WeightedTokenPairSource(a,
                charset,
                stringIndex);
        final WeightedTokenPairSource srcB = new WeightedTokenPairSource(b,
                charset,
                stringIndex);
        boolean equal = true;
        while (equal && srcA.hasNext() && srcB.hasNext()) {
            final Weighted<TokenPair> recA = srcA.read();
            final Weighted<TokenPair> recB = srcB.read();
            equal = recA.get().id1() == recB.get().id1()
                    && recA.get().id2() == recB.get().id2()
                    && recA.getWeight() == recB.getWeight();
        }
        return equal && srcA.hasNext() == srcB.hasNext();
    }
}

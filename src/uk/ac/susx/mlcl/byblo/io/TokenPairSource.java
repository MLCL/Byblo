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

import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.io.AbstractTSVSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An <tt>TokenPairSource</tt> object is used to retrieve 
 * {@link EntryFeature} objects from a flat file. 
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntryFeatureSink
 */
public class TokenPairSource extends AbstractTSVSource<TokenPair> {

    private static final Log LOG = LogFactory.getLog(TokenPairSource.class);

    private final ObjectIndex<String> stringIndex1;

    private final ObjectIndex<String> stringIndex2;

    private TokenPair previousRecord = null;
    
    private long count = 0;

    public TokenPairSource(
            File file, Charset charset,
            ObjectIndex<String> entryIndex,
            ObjectIndex<String> featureIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (entryIndex == null)
            throw new NullPointerException("stringIndex1 == null");
        if (featureIndex == null)
            throw new NullPointerException("stringIndex2 == null");
        this.stringIndex1 = entryIndex;
        this.stringIndex2 = featureIndex;
    }

    public TokenPairSource(File file, Charset charset,
            ObjectIndex<String> combinedIndex)
            throws FileNotFoundException, IOException {
        this(file, charset, combinedIndex, combinedIndex);
    }

    public TokenPairSource(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public ObjectIndex<String> getStringIndex1() {
        return stringIndex1;
    }

    public ObjectIndex<String> getStringIndex2() {
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
            parseValueDelimiter();
        } else {
            id1 = previousRecord.id1();
        }

        if (!hasNext() || isDelimiterNext()) {
            // Encountered an entry without any features. This is incoherent wrt
            // the task at hand, but quite a common scenario in general feature
            // extraction. Throw an exception which is caught for end user input
            parseRecordDelimiter();
            throw new SingletonRecordException(this,
                    "Found entry/feature record with no features.");
        }

        final int id2 = readTail();
        final TokenPair record = new TokenPair(
                id1, id2);

        if (hasNext() && isValueDelimiterNext()) {
            parseValueDelimiter();
            previousRecord = record;
        }

        if (hasNext() && isRecordDelimiterNext()) {
            parseRecordDelimiter();
            previousRecord = null;
        }

        ++count;
        return record;
    }

    protected final int readHead() throws IOException {
        return stringIndex1.get(parseString());
    }

    protected final int readTail() throws IOException {
        return stringIndex2.get(parseString());
    }

    public static boolean equal(File fileA, File fileB, Charset charset)
            throws IOException {
        final ObjectIndex<String> stringIndex = new ObjectIndex<String>();
        final TokenPairSource srcA = new TokenPairSource(
                fileA, charset, stringIndex);
        final TokenPairSource srcB = new TokenPairSource(
                fileB, charset, stringIndex);
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
}

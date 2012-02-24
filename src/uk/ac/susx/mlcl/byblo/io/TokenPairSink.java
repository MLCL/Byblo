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
import uk.ac.susx.mlcl.lib.io.AbstractTSVSink;
import uk.ac.susx.mlcl.lib.io.Sink;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * An <tt>TokenPairSink</tt> object is used to store 
 * {@link EntryFeature} objects in a flat file. 
 * 
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are 
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each {@link EntryFeature} 
 * corresponds to a single TSV record; i.e one line per object consisting of an
 * entry and a feature. In compact mode each TSV record consists of a single 
 * entry followed by the features from all sequentially written 
 * {@link EntryFeature} objects that share the same entry.</p>
 * 
 * Verbose mode example:
 * <pre>
 *      entry1  feature1
 *      entry1  feature2
 *      entry2  feature3
 *      entry3  feature2
 *      enrty3  feature4
 *      enrty3  feature1
 * </pre>
 * 
 * Equivalent compact mode example:
 * <pre>
 *      entry1  feature1 feature2
 *      entry2  feature3
 *      entry3  feature2 feature4 feature1
 * </pre>
 * 
 * <p>Compact mode is the default behavior, since it can reduce file sizes by 
 * approximately 50%, with corresponding reductions in I/O overhead.</p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class TokenPairSink extends AbstractTSVSink<TokenPair> {

    private final ObjectIndex<String> stringIndex1;
    private final ObjectIndex<String> stringIndex2;
    private boolean compactFormatEnabled = false;
    private TokenPair previousRecord = null;
    private long count = 0;

    public TokenPairSink(File file, Charset charset,
            ObjectIndex<String> stringIndex1, ObjectIndex<String> stringIndex2)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (stringIndex1 == null) {
            throw new NullPointerException("entryIndex == null");
        }
        if (stringIndex2 == null) {
            throw new NullPointerException("featureIndex == null");
        }
        this.stringIndex1 = stringIndex1;
        this.stringIndex2 = stringIndex2;
    }

    public TokenPairSink(File file, Charset charset,
            ObjectIndex<String> combinedIndex)
            throws FileNotFoundException, IOException {
        this(file, charset, combinedIndex, combinedIndex);
    }

    public TokenPairSink(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public final ObjectIndex<String> getStringIndex1() {
        return stringIndex1;
    }

    public ObjectIndex<String> getStringindex2() {
        return stringIndex2;
    }

    public boolean isIndexCombined() {
        return getStringindex2() == getStringIndex1();
    }

    public boolean isCompactFormatEnabled() {
        return compactFormatEnabled;
    }

    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
        this.compactFormatEnabled = compactFormatEnabled;
    }

    public long getCount() {
        return count;
    }

    @Override
    public void write(final TokenPair record) throws IOException {
        if (isCompactFormatEnabled()) {
            writeCompact(record);
        } else {
            writeVerbose(record);
        }
        ++count;
    }

    private void writeVerbose(final TokenPair record) throws IOException {
        write1(record.id1());
        writeValueDelimiter();
        write2(record.id2());
        writeRecordDelimiter();
    }

    private void writeCompact(final TokenPair record) throws IOException {
        if (previousRecord == null) {
            write1(record.id1());
        } else if (previousRecord.id1() != record.id1()) {
            writeRecordDelimiter();
            write1(record.id1());
        }
        writeValueDelimiter();
        write2(record.id2());
        previousRecord = record;
    }

    protected final void write1(final int entryId) throws IOException {
        writeString(stringIndex1.get(entryId));
    }

    protected final void write2(final int featureId) throws IOException {
        writeString(stringIndex2.get(featureId));
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null) {
            writeRecordDelimiter();
        }
        super.close();
    }
}

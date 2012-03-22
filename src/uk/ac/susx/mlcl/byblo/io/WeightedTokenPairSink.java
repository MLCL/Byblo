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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.text.DecimalFormat;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.TSVSink;

/**
 * An <tt>WeightedTokenPairSink</tt> object is used to store
 * {@link TokenPair} objects in a flat file.
 *
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each
 * {@link TokenPair} corresponds to a single TSV record; i.e one line per object
 * consisting of two entries, and their weight. In compact mode each TSV record
 * consists of a single entry followed by the second-entry/weight pairs from all
 * sequentially written
 * {@link WeightedEntryFeatureSink} objects that share the same first entry.</p>
 *
 * Verbose mode example:
 * <pre>
 *      entry1  entry1    weight1
 *      entry1  entry2    weight2
 *      entry2  entry3    weight3
 *      entry3  entry2    weight4
 *      enrty3  entry4    weight5
 *      enrty3  entry1    weight6
 * </pre>
 *
 * Equivalent compact mode example:
 * <pre>
 *      entry1  entry1    weight1 entry2    weight2
 *      entry2  entry3    weight3
 *      entry3  entry2    weight4 entry4    weight5 entry1    weight6
 * </pre>
 *
 * <p>Compact mode is the default behavior, since it can reduce file sizes by
 * approximately 50%, with corresponding reductions in I/O overhead.</p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedTokenPairSink implements Sink<Weighted<TokenPair>>, Closeable, Flushable {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private boolean compactFormatEnabled = false;

    private Weighted<TokenPair> previousRecord = null;

    private long count = 0;

    private TSVSink inner;

    private Enumerator<String> enumerator1;

    private Enumerator<String> enumerator2;

    public WeightedTokenPairSink(TSVSink inner, Enumerator<String> stringIndex1, Enumerator<String> stringIndex2) {
        this.inner = inner;
        this.enumerator1 = stringIndex1;
        this.enumerator2 = stringIndex2;
    }
    public WeightedTokenPairSink(TSVSink inner) {
        this.inner = inner;
        this.enumerator1 = null;
        this.enumerator2 = null;
    }

    public Enumerator<String> getEnumerator1() {
        return enumerator1;
    }

    public Enumerator<String> getEnumerator2() {
        return enumerator2;
    }

    public boolean isEnumeratorsCombined() {
        return getEnumerator1() == getEnumerator2();
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
    public void write(Weighted<TokenPair> record) throws IOException {
        if (isCompactFormatEnabled()) {
            writeCompact(record);
        } else {
            writeVerbose(record);
        }
        ++count;
    }

    private void writeVerbose(Weighted<TokenPair> record) throws IOException {
        writeString1(record.record().id1());
        writeString2(record.record().id2());
        writeWeight(record.weight());
        inner.endOfRecord();
    }

    private void writeCompact(final Weighted<TokenPair> record) throws IOException {
        if (previousRecord == null) {
            writeString1(record.record().id1());
        } else if (previousRecord.record().id1() != record.record().
                id1()) {
            inner.endOfRecord();
            writeString1(record.record().id1());
        }

        writeString2(record.record().id2());
        writeWeight(record.weight());
        previousRecord = record;
    }

    private void writeString1(int stringId) throws IOException {
        if (enumerator1 == null)
            inner.writeInt(stringId);
        else
            inner.writeString(enumerator1.value(stringId));
    }

    private void writeString2(int stringId) throws IOException {
        if (enumerator2 == null)
            inner.writeInt(stringId);
        else
            inner.writeString(enumerator2.value(stringId));
    }

    private void writeWeight(double weight) throws IOException {
        if (Double.compare((int) weight, weight) == 0) {
            inner.writeInt((int) weight);
        } else {
            inner.writeString(f.format(weight));
        }
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null) {
            inner.endOfRecord();
        }
        inner.close();
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
    }

}

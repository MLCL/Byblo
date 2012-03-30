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

    private IndexDeligatePair indexDeligate;

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private boolean compactFormatEnabled = true;

    private Weighted<TokenPair> previousRecord = null;

    private long count = 0;

    private TSVSink inner;
//
//    private final Function<Integer, String> tokenEncoder1;
//
//    private final Function<Integer, String> tokenEncoder2;

    public WeightedTokenPairSink(TSVSink inner, 
                                 IndexDeligatePair indexDeligate
//                                 Function<Integer, String> tokenEncoder1, Function<Integer, String> tokenEncoder2
            ) {
        this.inner = inner;
        this.indexDeligate = indexDeligate;
//        this.tokenEncoder1 = tokenEncoder1;
//        this.tokenEncoder2 = tokenEncoder2;
    }

//    public WeightedTokenPairSink(TSVSink inner) {
//        this.inner = inner;
//        this.tokenEncoder1 = Token.enumeratedEncoder();
//        this.tokenEncoder2 = Token.enumeratedEncoder();
//    }

//    public Function<Integer, String> getTokenEncoder1() {
//        return tokenEncoder1;
//    }
//
//    public Function<Integer, String> getTokenEncoder2() {
//        return tokenEncoder2;
//    }

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
        writeToken1(record.record().id1());
        writeToken2(record.record().id2());
        writeWeight(record.weight());
        inner.endOfRecord();
    }

    private void writeCompact(final Weighted<TokenPair> record) throws IOException {
        if (previousRecord == null) {
            writeToken1(record.record().id1());
        } else if (previousRecord.record().id1() != record.record().
                id1()) {
            inner.endOfRecord();
            writeToken1(record.record().id1());
        }

        writeToken2(record.record().id2());
        writeWeight(record.weight());
        previousRecord = record;
    }

    private void writeToken1(int tokenId) throws IOException {
        if(indexDeligate.isPreindexedTokens1())
            inner.writeInt(tokenId);
        else
        inner.writeString(indexDeligate.getEncoder1().apply(tokenId));
//        inner.writeString(tokenEncoder1.apply(tokenId));
    }

    private void writeToken2(int tokenId) throws IOException {
         if(indexDeligate.isPreindexedTokens2())
            inner.writeInt(tokenId);
        else
        inner.writeString(indexDeligate.getEncoder2().apply(tokenId));
//        inner.writeString(tokenEncoder2.apply(tokenId));
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

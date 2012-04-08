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
import java.io.*;
import java.nio.charset.Charset;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.io.*;

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
public class TokenPairSink implements Sink<TokenPair>, Closeable, Flushable {

//    private IndexDeligatePair indexDeligate;
//    private boolean compactFormatEnabled = true;
//    private TokenPair previousRecord = null;
//    private long count = 0;
    private final DataSink inner;

    public TokenPairSink(DataSink inner //, IndexDeligatePair indexDeligate
            )
            throws FileNotFoundException, IOException {
        this.inner = inner;
//        this.indexDeligate = indexDeligate;
    }
//
//    public boolean isCompactFormatEnabled() {
//        return compactFormatEnabled;
//    }
//
//    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
//        this.compactFormatEnabled = compactFormatEnabled;
//    }
//
//    public long getCount() {
//        return count;
//    }

    @Override
    public void write(final TokenPair record) throws IOException {
        inner.writeInt(record.id1());
        inner.writeInt(record.id2());
        inner.endOfRecord();
//        if (isCompactFormatEnabled()) {
//            writeCompact(record);
//        } else {
//            writeVerbose(record);
//        }
//        ++count;
    }

//    private void writeVerbose(final TokenPair record) throws IOException {
//        writeToken1(record.id1());
//        writeToken2(record.id2());
//        inner.endOfRecord();
//    }
//
//    private void writeCompact(final TokenPair record) throws IOException {
//        if (previousRecord == null) {
//            writeToken1(record.id1());
//        } else if (previousRecord.id1() != record.id1()) {
//            inner.endOfRecord();
//            writeToken1(record.id1());
//        }
//        writeToken2(record.id2());
//        previousRecord = record;
//    }
//
//    private void writeToken1(int stringId) throws IOException {
//        inner.writeInt(stringId);
////        if (indexDeligate.isPreindexedTokens1())
////            inner.writeInt(stringId);
////        else
////            inner.writeString(indexDeligate.getEnumerator1().value(stringId));
//    }
//
//    private void writeToken2(int stringId) throws IOException {
//        inner.writeInt(stringId);
////        if (indexDeligate.isPreindexedTokens2())
////            inner.writeInt(stringId);
////        else
////            inner.writeString(indexDeligate.getEnumerator2().value(stringId));
//    }
    @Override
    public void close() throws IOException {
//        if (isCompactFormatEnabled() && previousRecord != null) {
//            inner.endOfRecord();
//        }
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }

    @Override
    public void flush() throws IOException {
        if (inner instanceof Flushable)
            ((Flushable) inner).flush();
    }

    public static TokenPairSink open(
            File file, Charset charset, IndexDeligatePair idx, boolean compact)
            throws IOException {
        DataSink tsv = new TSV.Sink(file, charset);
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

        if (compact)
            tsv = Compact.compact(tsv, 2);
        
        if (!idx.isEnumerated1() || !idx.isEnumerated2()) {
            Enumerator<String>[] enumerators = (Enumerator<String>[]) new Enumerator[2];
            if (!idx.isEnumerated1())
                enumerators[0] = idx.getEnumerator1();
            if (!idx.isEnumerated2())
                enumerators[1] = idx.getEnumerator2();
            tsv = Enumerated.enumerated(tsv, enumerators);
        }
        return new TokenPairSink(tsv);
    }
}

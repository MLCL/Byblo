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
import java.io.*;
import java.text.DecimalFormat;
import sun.security.krb5.internal.crypto.EType;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.io.TSVSink;

/**
 * An <tt>WeightedTokenSink</tt> object is used to store {@link Token} objects
 * in a flat file.
 *
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each {@link Token}
 * corresponds to a single TSV record; i.e one line per object consisting of an
 * entry and it's weight. In compact mode each TSV record consists of a single
 * entry followed by the weights of all sequentially written {@link Token}
 * objects that share the same entry.</p>
 *
 * Verbose mode example:
 * <pre>
 *      entry1  weight1
 *      entry1  weight2
 *      entry2  weight3
 *      entry3  weight4
 *      enrty3  weight5
 *      enrty3  weight6
 * </pre>
 *
 * Equivalent compact mode example:
 * <pre>
 *      entry1  weight1 weight2
 *      entry2  weight3
 *      entry3  weight4 weight5 weight6
 * </pre>
 *
 * <p>Compact mode is the default behavior, since it can reduce file sizes by
 * approximately 50%, with corresponding reductions in I/O overhead.</p>
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedTokenSink implements Sink<Weighted<Token>>, Closeable, Flushable {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

//    private final Function<Integer, String> tokenEncoder;
    private IndexDeligate indexDeligate;

    private boolean compactFormatEnabled = true;

    private Weighted<Token> previousRecord = null;

    private long count = 0;

    private TSVSink inner;

    public WeightedTokenSink(TSVSink inner, IndexDeligate indexDeligate)
            throws FileNotFoundException, IOException {
        this.inner = inner;
        this.indexDeligate = indexDeligate;
    }

    public boolean isCompactFormatEnabled() {
        return compactFormatEnabled;
    }

    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
        this.compactFormatEnabled = compactFormatEnabled;
    }

    @Override
    public void write(final Weighted<Token> record) throws IOException {
        if (isCompactFormatEnabled()) {
            writeCompact(record);
        } else {
            writeVerbose(record);
        }
        ++count;
    }

    public long getCount() {
        return count;
    }

    private void writeVerbose(final Weighted<Token> record) throws IOException {
        writeString(record.record().id());
        writeWeight(record.weight());
        inner.endOfRecord();
    }

    private void writeCompact(final Weighted<Token> record) throws IOException {
        if (previousRecord == null) {
            writeString(record.record().id());
        } else if (previousRecord.record().id() != record.record().id()) {
            inner.endOfRecord();
            writeString(record.record().id());
        }
        writeWeight(record.weight());
        previousRecord = record;
    }

    private void writeString(int id) throws IOException {
        if (indexDeligate.isPreindexedTokens())
            inner.writeInt(id);
        else
            inner.writeString(indexDeligate.getEnumerator().value(id));
    }

    private void writeWeight(double weight) throws IOException {
        if (Double.compare((int) weight, weight) == 0) {
            inner.writeInt((int) weight);
        } else {
            inner.writeString(f.format(weight));
        }
    }

    @Override
    public void flush() throws IOException {
        inner.flush();
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null) {
            inner.endOfRecord();
        }
        inner.close();
    }

}

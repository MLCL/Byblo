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
import uk.ac.susx.mlcl.lib.io.AbstractTSVSink;
import uk.ac.susx.mlcl.lib.io.Sink;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 * An <tt>WeightedEntryPairSink</tt> object is used to store 
 * {@link WeightedEntryPairRecord} objects in a flat file. 
 * 
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are 
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each 
 * {@link WeightedEntryPairRecord} corresponds to a single TSV record; i.e one
 * line per object consisting of two entries, and their weight. In 
 * compact mode each TSV record consists of a single entry followed by the 
 * second-entry/weight pairs from all sequentially written 
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
public class WeightedEntryPairSink
        extends AbstractTSVSink<WeightedEntryPairRecord>
        implements Sink<WeightedEntryPairRecord> {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private final ObjectIndex<String> strIndexA;

    private final ObjectIndex<String> strIndexB;

    private boolean compactFormatEnabled = false;

    private WeightedEntryPairRecord previousRecord = null;

    public WeightedEntryPairSink(File file, Charset charset,
            ObjectIndex<String> strIndexA,
            ObjectIndex<String> strIndexB) throws IOException {
        super(file, charset);
        this.strIndexA = strIndexA;
        this.strIndexB = strIndexB;
    }

    public boolean isCompactFormatEnabled() {
        return compactFormatEnabled;
    }

    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
        this.compactFormatEnabled = compactFormatEnabled;
    }

    @Override
    public void write(WeightedEntryPairRecord record) throws IOException {
        if (isCompactFormatEnabled())
            writeCompact(record);
        else
            writeVerbose(record);
    }

    private void writeVerbose(WeightedEntryPairRecord record) throws IOException {
        writeEntryA(record.getEntry1Id());
        writeValueDelimiter();
        writeEntryB(record.getEntry2Id());
        writeValueDelimiter();
        writeWeight(record.getWeight());
        writeRecordDelimiter();
    }

    private void writeCompact(final WeightedEntryPairRecord record) throws IOException {
        if (previousRecord == null) {
            writeEntryA(record.getEntry1Id());
        } else if (previousRecord.getEntry1Id() != record.getEntry1Id()) {
            writeRecordDelimiter();
            writeEntryA(record.getEntry1Id());
        }

        writeValueDelimiter();
        writeEntryB(record.getEntry2Id());
        writeValueDelimiter();
        writeWeight(record.getWeight());
        previousRecord = record;
    }

    private void writeEntryA(int id) throws IOException {
        writeString(strIndexA.get(id));
    }

    private void writeEntryB(int id) throws IOException {
        writeString(strIndexB.get(id));
    }

    private void writeWeight(double weight) throws IOException {
        if (Double.compare((int) weight, weight) == 0)
            writeInt((int) weight);
        else
            writeString(f.format(weight));
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null)
            writeRecordDelimiter();
        super.close();
    }
}

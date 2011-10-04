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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class EntrySink extends AbstractTSVSink<EntryRecord>
        implements Sink<EntryRecord> {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private final ObjectIndex<String> stringIndex;

    private boolean compactFormatEnabled = true;

    private EntryRecord previousRecord = null;

    public EntrySink(File file, Charset charset, ObjectIndex<String> stringIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        this.stringIndex = stringIndex;
    }

    public boolean isCompactFormatEnabled() {
        return compactFormatEnabled;
    }

    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
        this.compactFormatEnabled = compactFormatEnabled;
    }

    @Override
    public void write(final EntryRecord record) throws IOException {
        if (isCompactFormatEnabled())
            writeCompact(record);
        else
            writeVerbose(record);
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null)
            writeRecordDelimiter();
        super.close();
    }

    private void writeVerbose(final EntryRecord record) throws IOException {
        writeEntry(record.getEntryId());
        writeValueDelimiter();
        writeWeight(record.getWeight());
        writeRecordDelimiter();
    }

    private void writeCompact(final EntryRecord record) throws IOException {
        if (previousRecord == null) {
            writeEntry(record.getEntryId());
        } else if (previousRecord.getEntryId() != record.getEntryId()) {
            writeRecordDelimiter();
            writeEntry(record.getEntryId());
        }
        writeValueDelimiter();
        writeWeight(record.getWeight());
        previousRecord = record;
    }

    private void writeEntry(int id) throws IOException {
        writeString(stringIndex.get(id));
    }

    private void writeWeight(double weight) throws IOException {
        if (Double.compare((int) weight, weight) == 0)
            super.writeInt((int) weight);
        else
            super.writeString(f.format(weight));
    }
}

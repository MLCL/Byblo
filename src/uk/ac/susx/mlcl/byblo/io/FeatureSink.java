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
 * An <tt>FeatureSink</tt> object is used to store {@link FeatureRecord} objects
 * in a flat file. 
 * 
 * <p>The basic file format is Tab-Separated-Values (TSV) where records are 
 * delimited by new-lines, and values are delimited by tabs. Two variants are
 * supported: verbose and compact. In verbose mode each {@link FeatureRecord} 
 * corresponds to a single TSV record; i.e one line per object consisting of an
 * feature and it's weight. In compact mode each TSV record consists of a single
 * feature followed by the weights of all sequentially written 
 * {@link FeatureRecord} objects that share the same feature.</p>
 * 
 * Verbose mode example:
 * <pre>
 *      feature1  weight1
 *      feature1  weight2
 *      feature2  weight3
 *      feature3  weight4
 *      feature3  weight5
 *      feature3  weight6
 * </pre>
 * 
 * Equivalent compact mode example:
 * <pre>
 *      feature1  weight1 weight2
 *      feature2  weight3
 *      feature3  weight4 weight5 weight6
 * </pre>
 * 
 * <p>Compact mode is the default behavior, since it can reduce file sizes by 
 * approximately 50%, with corresponding reductions in I/O overhead.</p>
 * 
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FeatureSink
        extends AbstractTSVSink<FeatureRecord>
        implements Sink<FeatureRecord> {

    private final ObjectIndex<String> stringIndex;

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");

    private boolean compactFormatEnabled = false;

    private FeatureRecord previousRecord = null;

    public FeatureSink(
            File file, Charset charset, ObjectIndex<String> stringIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);

        if (stringIndex == null)
            throw new NullPointerException("stringIndex == null");
        this.stringIndex = stringIndex;
    }

    public FeatureSink(File file, Charset charset) throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public boolean isCompactFormatEnabled() {
        return compactFormatEnabled;
    }

    public void setCompactFormatEnabled(boolean compactFormatEnabled) {
        this.compactFormatEnabled = compactFormatEnabled;
    }

    public final ObjectIndex<String> getStringIndex() {
        return stringIndex;
    }

    @Override
    public void write(FeatureRecord record) throws IOException {
        if (isCompactFormatEnabled())
            writeCompact(record);
        else
            writeVerbose(record);
    }

    private void writeVerbose(FeatureRecord record) throws IOException {
        writeFeature(record.getFeatureId());
        writeValueDelimiter();
        writeWeight(record.getWeight());
        writeRecordDelimiter();
    }

    private void writeCompact(FeatureRecord record) throws IOException {
        if (previousRecord == null) {
            writeFeature(record.getFeatureId());
        } else if (previousRecord.getFeatureId() != record.getFeatureId()) {
            writeRecordDelimiter();
            writeFeature(record.getFeatureId());
        }
        writeValueDelimiter();
        writeWeight(record.getWeight());
        previousRecord = record;
    }

    private void writeFeature(int id) throws IOException {
        writeString(stringIndex.get(id));
    }

    private void writeWeight(double weight) throws IOException {
        if (Double.compare((int) weight, weight) == 0)
            super.writeInt((int) weight);
        else
            super.writeString(f.format(weight));
    }

    @Override
    public void close() throws IOException {
        if (isCompactFormatEnabled() && previousRecord != null)
            writeRecordDelimiter();
        super.close();
    }
}

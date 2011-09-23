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
package uk.ac.susx.mlcl.dttools.io;

import uk.ac.susx.mlcl.lib.ObjectIndex;
import uk.ac.susx.mlcl.lib.io.AbstractTSVSink;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class FeatureSink
        extends AbstractTSVSink<FeatureEntry>
        implements uk.ac.susx.mlcl.lib.io.Sink<FeatureEntry> {

    private final DecimalFormat f = new DecimalFormat("###0.0#####;-###0.0#####");
    private final ObjectIndex<String> headIndex;

    private final ObjectIndex<String> contextIndex;

    public FeatureSink(File file, Charset charset,
            ObjectIndex<String> headIndex, ObjectIndex<String> contextIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (headIndex == null)
            throw new NullPointerException("headIndex == null");
        if (contextIndex == null)
            throw new NullPointerException("contextIndex == null");
        this.headIndex = headIndex;
        this.contextIndex = contextIndex;
    }

    public FeatureSink(File file, Charset charset,
            ObjectIndex<String> combinedIndex)
            throws FileNotFoundException, IOException {
        this(file, charset, combinedIndex, combinedIndex);
    }

    public FeatureSink(File file, Charset charset) throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public final ObjectIndex<String> getHeadIndex() {
        return headIndex;
    }

    public ObjectIndex<String> getContextIndex() {
        return contextIndex;
    }

    @Override
    public void write(FeatureEntry record) throws IOException {
        super.writeString(headIndex.get(record.getHeadId()));//record.getHead());
        super.writeValueDelimiter();
        super.writeString(contextIndex.get(record.getContextId()));//record.getContext());
        super.writeValueDelimiter();
        if (Double.compare((int) record.getWeight(), record.getWeight()) == 0)
            super.writeInt((int) record.getWeight());
        else
        super.writeString(f.format(record.getWeight()));
        super.writeRecordDelimiter();
    }
}

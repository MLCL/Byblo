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
import uk.ac.susx.mlcl.lib.io.AbstractTSVSource;
import uk.ac.susx.mlcl.lib.io.Source;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 */
public class FeatureSource
        extends AbstractTSVSource<FeatureEntry>
        implements Source<FeatureEntry> {

    private final ObjectIndex<String> headIndex;

    private final ObjectIndex<String> contextIndex;

    private long count = 0;

    public FeatureSource(File file, Charset charset,
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

    public FeatureSource(File file, Charset charset,
            ObjectIndex<String> combinedIndex)
            throws FileNotFoundException, IOException {
        this(file, charset, combinedIndex, combinedIndex);
    }

    public FeatureSource(File file, Charset charset) throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public long getCount() {
        return count;
    }


    public final ObjectIndex<String> getHeadIndex() {
        return headIndex;
    }

    public ObjectIndex<String> getContextIndex() {
        return contextIndex;
    }

    public FeatureVectorSource getVectorSource() {
        return new FeatureVectorSource(this);
    }

    public FeatureEntry read() throws IOException {
//        final String head = parseString();
//        final int head_id = stringIndex.get(head);
//        final String head = ;
        final int head_id = headIndex.get(parseString());
        parseValueDelimiter();
//        final String tail = parseString();
//        final int tail_id = stringIndex.get(tail);
//        final String tail = ;
        final int tail_id = contextIndex.get(parseString());
        parseValueDelimiter();
        final double weight = parseDouble();
        if (hasNext())
            parseRecordDelimiter();
        count++;
        return new FeatureEntry(//stringIndex.get(head_id),
                                //stringIndex.get(tail_id),
                                head_id,
                                tail_id,
                                weight);
    }
}

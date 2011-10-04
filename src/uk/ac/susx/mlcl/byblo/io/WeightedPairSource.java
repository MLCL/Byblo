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
import uk.ac.susx.mlcl.lib.collect.WeightedPair;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class WeightedPairSource
        extends AbstractTSVSource<WeightedPair>
        implements Source<WeightedPair> {

    private final ObjectIndex<String> entryIndex;

    private WeightedPair previousRecord = null;

    public WeightedPairSource(
            File file, Charset charset,
            ObjectIndex<String> entryIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (entryIndex == null)
            throw new NullPointerException("entryIndex == null");
        this.entryIndex = entryIndex;
    }

    public WeightedPairSource(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public final ObjectIndex<String> getEntryIndex() {
        return entryIndex;
    }

    @Override
    public WeightedPair read() throws IOException {
        final int entryId1;
        if (previousRecord == null) {
            entryId1 = readEntry();
            parseValueDelimiter();
        } else {
            entryId1 = previousRecord.getXId();
        }

        final int entryId2 = readEntry();
        parseValueDelimiter();
        final double weight = readWight();

        final WeightedPair record = new WeightedPair(
                entryId1, entryId2, weight);

        if (isValueDelimiterNext()) {
            parseValueDelimiter();
            previousRecord = record;
        } else if(hasNext()) {
            parseRecordDelimiter();
            previousRecord = null;
        }

        return record;
    }

    protected int readEntry() throws IOException {
        return entryIndex.get(parseString());
    }

    protected double readWight() throws IOException {
        return parseDouble();
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final ObjectIndex<String> stringIndex = new ObjectIndex<String>();
        final WeightedPairSource srcA = new WeightedPairSource(a, charset,
                stringIndex);
        final WeightedPairSource srcB = new WeightedPairSource(b, charset,
                stringIndex);
        boolean equal = true;
        while (equal && srcA.hasNext() && srcB.hasNext()) {
            final WeightedPair recA = srcA.read();
            final WeightedPair recB = srcB.read();
            equal = recA.getXId() == recB.getXId()
                    && recA.getYId() == recB.getYId()
                    && recA.getWeight() == recB.getWeight();
        }
        return equal && srcA.hasNext() == srcB.hasNext();
    }
}

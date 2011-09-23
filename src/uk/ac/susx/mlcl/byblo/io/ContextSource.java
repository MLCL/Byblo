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
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @version 27th March 2011
 */
public class ContextSource extends AbstractTSVSource<ContextEntry> {

    private static final Logger LOG = Logger.getLogger(ContextSource.class.
            getName());

    private final ObjectIndex<String> stringIndex;

    private double weightSum = 0;

    private int cardinality = 0;

    private long count = 0;

    public ContextSource(File file, Charset charset,
                          ObjectIndex<String> stringIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (stringIndex == null)
            throw new NullPointerException("stringIndex == null");
        this.stringIndex = stringIndex;
    }

    public ContextSource(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public ObjectIndex<String> getStringIndex() {
        return stringIndex;
    }

    @Override
    public ContextEntry read() throws IOException {
//        final String context = parseString();
//        final int context_id = stringIndex.get(context);
//        final String context = ;
        final int context_id = stringIndex.get(parseString());
        parseValueDelimiter();
        final double weight = parseDouble();
        if (hasNext())
            parseRecordDelimiter();
        cardinality = Math.max(cardinality, context_id + 1);
        weightSum += weight;
        ++count;
        return new ContextEntry(//context,
                context_id, weight);
    }

    public double getWeightSum() {
        return weightSum;
    }

    public int getCardinality() {
        return cardinality;
    }

    public long getCount() {
        return count;
    }

    public Int2DoubleMap readAll() throws IOException {
        Int2DoubleMap contextFrequenciesMap = new Int2DoubleOpenHashMap();
        while (this.hasNext()) {
            ContextEntry entry = this.read();
            if (contextFrequenciesMap.containsKey(entry.getId())) {
                // XXX: This shouldn't happen any-more, becaue perl is no longer used.
                //
                // If we encounter the same context more than once, it means
                // the previous stage has decided two strings are not-equal, 
                // but the corrent stage thinks are equals 
                // ... so we need to merge the records:

                final int id = entry.getId();
                final double oldFreq = contextFrequenciesMap.get(id);
                final double newFreq = oldFreq + entry.getWeight();

                LOG.log(Level.WARNING,
                        "Found duplicate context \"{0}\" (id={1}) in tailsdb "
                        + "file. Merging records. Old frequency = {2}, new "
                        + "frequency = {3}.",
                        new Object[]{stringIndex.get(id),
                        id, oldFreq, newFreq});

                contextFrequenciesMap.put(id, newFreq);
            }
            contextFrequenciesMap.put(entry.getId(), entry.getWeight());

        }
        return contextFrequenciesMap;
    }

    public double[] readAllAsArray() throws IOException {
        Int2DoubleMap tmp = readAll();
        double[] contextFreqs = new double[getCardinality()];
        ObjectIterator<Int2DoubleMap.Entry> it = tmp.int2DoubleEntrySet().
                iterator();
        while (it.hasNext()) {
            Int2DoubleMap.Entry entry = it.next();
            contextFreqs[entry.getIntKey()] = entry.getDoubleValue();
        }
        return contextFreqs;
    }
}

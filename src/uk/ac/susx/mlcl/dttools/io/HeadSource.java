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
public class HeadSource extends AbstractTSVSource<HeadEntry> {

    private static final Logger LOG = Logger.getLogger(HeadSource.class.
            getName());

    private final ObjectIndex<String> stringIndex;

    private double weightSum = 0;

    private double weightMax = 0;

    private int widthSum = 0;

    private int widthMax = 0;

    private int cardinality = 0;

    private int count = 0;

    public HeadSource(File file, Charset charset,
            ObjectIndex<String> stringIndex)
            throws FileNotFoundException, IOException {
        super(file, charset);
        if (stringIndex == null)
            throw new NullPointerException("stringIndex == null");
        this.stringIndex = stringIndex;
    }

    public HeadSource(File file, Charset charset)
            throws FileNotFoundException, IOException {
        this(file, charset, new ObjectIndex<String>());
    }

    public ObjectIndex<String> getStringIndex() {
        return stringIndex;
    }

    public double getWeightSum() {
        return weightSum;
    }

    public double getWeightMax() {
        return weightMax;
    }

    public int getWidthMax() {
        return widthMax;
    }

    public int getWidthSum() {
        return widthSum;
    }

    public int getCardinality() {
        return cardinality;
    }

    public int getCount() {
        return count;
    }

    public HeadEntry read() throws IOException {
//        final String head = parseString();
//        final int head_id = stringIndex.get(head);
        final int head_id = stringIndex.get(parseString());
        parseValueDelimiter();
//        final int index = parseInt();
//        parseValueDelimiter();
//        final int width = parseInt();
//        parseValueDelimiter();
        final double weight = parseDouble();
        if (hasNext())
            parseRecordDelimiter();
        cardinality = Math.max(cardinality, head_id + 1);
        weightSum += weight;
        weightMax = Math.max(weightMax, weight);
//        widthSum += width;
//        widthMax = Math.max(widthMax, width);
        ++count;
        return new HeadEntry(
//                head,
                head_id, 
//                index, width,
                weight);

    }

    public Int2DoubleMap readAll() throws IOException {
        Int2DoubleMap contextFrequenciesMap = new Int2DoubleOpenHashMap();
        while (this.hasNext()) {
            HeadEntry entry = this.read();
            if (contextFrequenciesMap.containsKey(entry.getHeadId())) {
                // If we encounter the same headword more than once, it means
                // the perl has decided two strings are not-equal, which java
                // thinks are equals ... so we need to merge the records:

                // TODO: Rewrite the perl script

//                String word = stringIndex.get(entry.getHeadId());//entry.getHeadword();
                final int id = entry.getHeadId();
                final double oldFreq = contextFrequenciesMap.get(id);
                final double newFreq = oldFreq + entry.getTotal();

                LOG.log(Level.WARNING,
                        "Found duplicate headword \"{0}\" (id={1}) in headsdb "
                        + "file. Merging records. Old frequency = {2}, new "
                        + "frequency = {3}.",
                        new Object[]{stringIndex.get(entry.getHeadId()), id, oldFreq, newFreq});

                contextFrequenciesMap.put(id, newFreq);

//                throw new AssertionError();
            }
            contextFrequenciesMap.put(entry.getHeadId(), entry.getTotal());
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

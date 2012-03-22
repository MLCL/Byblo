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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.Lexer;
import uk.ac.susx.mlcl.lib.io.Lexer.Tell;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 * An <tt>WeightedTokenSource</tt> object is used to retrieve {@link Token}
 * objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntrySink
 */
public class WeightedTokenSource implements SeekableSource<Weighted<Token>, Lexer.Tell> {

    private static final Log LOG = LogFactory.getLog(WeightedTokenSource.class);

    private final Enumerator<String> stringIndex;

    private double weightSum = 0;

    private double weightMax = 0;

    private int widthSum = 0;

    private int widthMax = 0;

    private int cardinality = 0;

    private int count = 0;

    private Weighted<Token> previousRecord = null;

    private final TSVSource inner;

    public WeightedTokenSource(TSVSource inner,
                               Enumerator<String> stringIndex)
            throws FileNotFoundException, IOException {
        this.inner = inner;
        if (stringIndex == null)
            throw new NullPointerException("stringIndex == null");
        this.stringIndex = stringIndex;
    }

    public WeightedTokenSource(TSVSource inner)
            throws FileNotFoundException, IOException {
        this(inner, new SimpleEnumerator<String>());
    }

    public Enumerator<String> getStringIndex() {
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

    @Override
    public void position(Tell offset) throws IOException {
        inner.position(offset);
        previousRecord = null;
    }

    @Override
    public Tell position() {
        return inner.position();
    }

    @Override
    public Weighted<Token> read() throws IOException {
        final int entryId;
        if (previousRecord == null) {
            entryId = stringIndex.index(inner.parseString());
            inner.parseValueDelimiter();
        } else {
            entryId = previousRecord.record().id();
        }

        if (!hasNext() || inner.isDelimiterNext()) {
            inner.parseRecordDelimiter();
            throw new SingletonRecordException(inner,
                                               "Found entry record with no weights.");
        }

        final double weight = inner.parseDouble();

        cardinality = Math.max(cardinality, entryId + 1);
        weightSum += weight;
        weightMax = Math.max(weightMax, weight);
        ++count;
        final Weighted<Token> record = new Weighted<Token>(new Token(entryId), weight);

        if (inner.isValueDelimiterNext()) {
            inner.parseValueDelimiter();
            previousRecord = record;
        }

        if (hasNext() && inner.isRecordDelimiterNext()) {
            inner.parseRecordDelimiter();
            previousRecord = null;
        }

        return record;
    }

    public Int2DoubleMap readAll() throws IOException {
        Int2DoubleMap entityFrequenciesMap = new Int2DoubleOpenHashMap();
        while (this.hasNext()) {
            Weighted<Token> entry = this.read();
            if (entityFrequenciesMap.containsKey(entry.record().id())) {
                // If we encounter the same Token more than once, it means
                // the perl has decided two strings are not-equal, which java
                // thinks are equal ... so we need to merge the records:

                // TODO: Not true any more.. remove this code?

                final int id = entry.record().id();
                final double oldFreq = entityFrequenciesMap.get(id);
                final double newFreq = oldFreq + entry.weight();

                if (LOG.isWarnEnabled())
                    LOG.warn("Found duplicate Entry \"" + stringIndex.value(entry.record().
                            id()) + "\" (id=" + id
                            + ") in entries file. Merging records. Old frequency = "
                            + oldFreq + ", new frequency = " + newFreq + ".");

                entityFrequenciesMap.put(id, newFreq);
            } else {
                entityFrequenciesMap.put(entry.record().id(), entry.weight());
            }
        }
        return entityFrequenciesMap;
    }

    public double[] readAllAsArray() throws IOException {
        Int2DoubleMap tmp = readAll();
        double[] entryFreqs = new double[getCardinality()];
        ObjectIterator<Int2DoubleMap.Entry> it = tmp.int2DoubleEntrySet().
                iterator();
        while (it.hasNext()) {
            Int2DoubleMap.Entry entry = it.next();
            entryFreqs[entry.getIntKey()] = entry.getDoubleValue();
        }
        return entryFreqs;
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final Enumerator<String> stringIndex = new SimpleEnumerator<String>();
        final WeightedTokenSource srcA = new WeightedTokenSource(new TSVSource(a, charset), stringIndex);
        final WeightedTokenSource srcB = new WeightedTokenSource(new TSVSource(b, charset), stringIndex);
        boolean equal = true;
        while (equal && srcA.hasNext() && srcB.hasNext()) {
            final Weighted<Token> recA = srcA.read();
            final Weighted<Token> recB = srcB.read();
            equal = recA.record().id() == recB.record().id()
                    && recA.weight() == recB.weight();
        }
        return equal && srcA.hasNext() == srcB.hasNext();
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.hasNext();
    }

    public double percentRead() throws IOException {
        return inner.percentRead();
    }
}

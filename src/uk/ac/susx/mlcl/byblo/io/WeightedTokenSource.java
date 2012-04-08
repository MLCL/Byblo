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

import com.google.common.base.Predicate;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * An <tt>WeightedTokenSource</tt> object is used to retrieve {@link Token}
 * objects from a flat file.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 * @see EntrySink
 */
public class WeightedTokenSource
        implements SeekableSource<Weighted<Token>, Tell>, Closeable {

    private static final Log LOG = LogFactory.getLog(WeightedTokenSource.class);

//    private IndexDeligate indexDeligate;
    private double weightSum = 0;

    private double weightMax = 0;

    private int widthSum = 0;

    private int widthMax = 0;

    private int cardinality = 0;

    private int count = 0;

    private Weighted<Token> previousRecord = null;

    private final SeekableDataSource inner;

    public WeightedTokenSource(SeekableDataSource inner //                             ,  IndexDeligate indexDeligate
            )
            throws FileNotFoundException, IOException {
        this.inner = inner;
//        this.indexDeligate = indexDeligate;
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
    public Tell position() throws IOException {
        return inner.position();
    }

    private int readTokenId() throws IOException {
        return inner.readInt();
//        if (indexDeligate.isPreindexedTokens()) {
//            return inner.readInt();
//        } else
//            return indexDeligate.getEnumerator().index(inner.readString());
    }

    @Override
    public Weighted<Token> read() throws IOException {
        final int tokenId;
        if (previousRecord == null) {
            tokenId = readTokenId();
        } else {
            tokenId = previousRecord.record().id();
        }

        if (!hasNext() || inner.isEndOfRecordNext()) {
            inner.endOfRecord();
            throw new IOException("Found entry record with no weights.");
        }

        final double weight = inner.readDouble();

        cardinality = Math.max(cardinality, tokenId + 1);
        weightSum += weight;
        weightMax = Math.max(weightMax, weight);
        ++count;
        final Weighted<Token> record = new Weighted<Token>(new Token(tokenId),
                                                           weight);

        if (inner.canRead() && !inner.isEndOfRecordNext()) {
            previousRecord = record;
        }

        if (hasNext() && inner.isEndOfRecordNext()) {
            inner.endOfRecord();
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
                    LOG.warn("Found duplicate Entry \""
                            //                            + (indexDeligate.getEnumerator().value(entry.record().id()))
                            + (entry.record().id())
                            + "\" (id=" + id
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

    public static WeightedTokenSource open(
            File f, Charset charset, IndexDeligate idx) throws IOException {
        SeekableDataSource tsv = new TSV.Source(f, charset);
        
        if (idx.isSkipIndexed1()) {
            tsv = Deltas.deltaInt(tsv, new Predicate<Integer>() {

                @Override
                public boolean apply(Integer column) {
                    return column == 0;
                }
            });
        }
        
        
        
        tsv = Compact.compact(tsv, 2);
        if (!idx.isPreindexedTokens())
            tsv = Enumerated.enumerated(tsv, idx.getEnumerator());
        return new WeightedTokenSource(tsv);
    }

    public static boolean equal(File a, File b, Charset charset) throws IOException {
        final Enumerator<String> stringIndex = Enumerators.
                newDefaultStringEnumerator();
        IndexDeligate idx = new IndexDeligate();
        final WeightedTokenSource srcA = open(a, charset, idx);
        final WeightedTokenSource srcB = open(b, charset, idx);

        List<Weighted<Token>> listA = IOUtil.readAll(srcA);
        List<Weighted<Token>> listB = IOUtil.readAll(srcB);
        Comparator<Weighted<Token>> c = Comparators.fallback(
                Weighted.recordOrder(Token.indexOrder()),
                Weighted.<Token>weightOrder());
        Collections.sort(listA, c);
        Collections.sort(listB, c);
        return listA.equals(listB);
    }

    @Override
    public boolean hasNext() throws IOException {
        return inner.canRead();
    }

    @Override
    public void close() throws IOException {
        if (inner instanceof Closeable)
            ((Closeable) inner).close();
    }
}

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
package uk.ac.susx.mlcl.byblo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.FallbackComparator;
import uk.ac.susx.mlcl.lib.tasks.ReverseComparator;

/**
 * Task that read in a file and produces the k-nearest-neighbors for each base
 * entry. Assumes the file is composed of entry, entry, weight triples that are
 * delimited by tabs.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class KnnTask extends SortTask.SimsSortTask {

    private static final Log LOG = LogFactory.getLog(KnnTask.class);

    @Parameter(names = {"-k"},
    description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnTask.DEFAULT_K;

    private Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.INDEX_ORDER);

    private Comparator<Weighted<TokenPair>> nearnessComparator =
            new ReverseComparator(Weighted.weightOrder());

    public KnnTask(File sourceFile, File destinationFile, Charset charset,
                   boolean preindexedTokens1, boolean preindexedTokens2, int k) {
        super(sourceFile, destinationFile, charset, preindexedTokens1, preindexedTokens2);
        setComparator(new FallbackComparator<Weighted<TokenPair>>(classComparator, nearnessComparator));
        this.k = k;
    }

    public Comparator<Weighted<TokenPair>> getClassComparator() {
        return classComparator;
    }

    public void setClassComparator(Comparator<Weighted<TokenPair>> classComparator) {
        this.classComparator = classComparator;
        setComparator(new FallbackComparator<Weighted<TokenPair>>(classComparator, nearnessComparator));
    }

    public Comparator<Weighted<TokenPair>> getNearnessComparator() {
        return nearnessComparator;
    }

    public void setNearnessComparator(Comparator<Weighted<TokenPair>> nearnessComparator) {
        this.nearnessComparator = nearnessComparator;
        setComparator(new FallbackComparator<Weighted<TokenPair>>(classComparator, nearnessComparator));
    }

    public final int getK() {
        return k;
    }

    public final void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k < 1");
        this.k = k;
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running memory K-Nearest-Neighbours from \"" + getSrcFile()
                    + "\" to \"" + getDstFile() + "\".");

        final Function<String, Integer> decoder1;
        final Function<String, Integer> decoder2;
        final Function<Integer, String> encoder1;
        final Function<Integer, String> encoder2;

        if (!isPreindexedTokens1()) {
            final Enumerator<String> entryIndex = new SimpleEnumerator<String>();

            decoder1 = Token.stringDecoder(entryIndex);
            encoder1 = Token.stringEncoder(entryIndex);
        } else {
            decoder1 = Token.enumeratedDecoder();
            encoder1 = Token.enumeratedEncoder();
        }

        if (!isPreindexedTokens2()) {
            final Enumerator<String> featureIndex = new SimpleEnumerator<String>();
            decoder2 = Token.stringDecoder(featureIndex);
            encoder2 = Token.stringEncoder(featureIndex);

        } else {
            decoder2 = Token.enumeratedDecoder();
            encoder2 = Token.enumeratedEncoder();
        }

        Source<Weighted<TokenPair>> src = new WeightedTokenPairSource(
                new TSVSource(getSrcFile(), getCharset()),
                decoder1, decoder2);

        final List<Weighted<TokenPair>> items = IOUtil.readAll(src);
        Collections.sort(items, getComparator());


        Sink<Weighted<TokenPair>> snk = new WeightedTokenPairSink(
                new TSVSink(getDstFile(), getCharset()),
                encoder1, encoder2);

        knnLines(items, snk);

        if (LOG.isInfoEnabled())
            LOG.info("Completed memory K-Nearest-Neighbours.");

    }

    protected void knnLines(
            Collection<Weighted<TokenPair>> in,
            Sink<Weighted<TokenPair>> out)
            throws IOException {
        
        Weighted<TokenPair> currentClass = null;
        int count = 0;
        for (Weighted<TokenPair> item : in) {
            if (classComparator.compare(item, currentClass) != 0) {
                currentClass = item;
                count = 1;
            } else {
                count++;
            }
            if (count <= k) {
                out.write(item);
            }
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("k", k);
    }

}

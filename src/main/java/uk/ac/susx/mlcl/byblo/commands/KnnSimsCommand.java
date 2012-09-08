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
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.enumerators.EnumeratingDelegates;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerObjectSink;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.KFirstReducingObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;

/**
 * Task that read in a file and produces the k-nearest-neighbors for each base
 * entry. Assumes the file is composed of entry, entry, weight triples that are
 * delimited by tabs.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public final class KnnSimsCommand extends SortEventsCommand {

    private static final Log LOG = LogFactory.getLog(KnnSimsCommand.class);

    @Parameter(names = {"-k"},
    description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnSimsCommand.DEFAULT_K;

    private Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.firstIndexOrder());

    private Comparator<Weighted<TokenPair>> nearnessComparator =
            Comparators.reverse(Weighted.<TokenPair>weightOrder());

    public KnnSimsCommand(File sourceFile, File destinationFile, Charset charset,
                          SingleEnumerating indexDelegate, int k) throws IOException {
        super(sourceFile, destinationFile, charset, EnumeratingDelegates.toPair(indexDelegate));
        super.setComparator(Comparators.fallback(
                classComparator, nearnessComparator));
        setK(k);
    }

    public KnnSimsCommand() {
        setK(100);
    }

    public Comparator<Weighted<TokenPair>> getCombinedComparator() {
        return Comparators.fallback(getClassComparator(), getNearnessComparator());
    }

    @Override
    public Comparator<Weighted<TokenPair>> getComparator() {
        return isReverse()
               ? Comparators.reverse(getCombinedComparator())
               : getCombinedComparator();
    }

    @Override
    @Deprecated
    public void setComparator(Comparator<Weighted<TokenPair>> comparator) {
        throw new UnsupportedOperationException(
                "Class and nearness comparators should be set instead.");
    }

    public Comparator<Weighted<TokenPair>> getClassComparator() {
        return classComparator;
    }

    public void setClassComparator(
            Comparator<Weighted<TokenPair>> classComparator) {
        this.classComparator = classComparator;
    }

    public Comparator<Weighted<TokenPair>> getNearnessComparator() {
        return nearnessComparator;
    }

    public void setNearnessComparator(
            Comparator<Weighted<TokenPair>> nearnessComparator) {
        this.nearnessComparator = nearnessComparator;
    }

    public final int getK() {
        return k;
    }

    public final void setK(int k) {
        if (k < 1)
            throw new IllegalArgumentException("k < 1");
        this.k = k;
    }

    private boolean first = false;

    @Override
    protected ObjectSource<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        final ObjectSource<Weighted<TokenPair>> src =
                first
                ? BybloIO.openSimsSource(file, getCharset(), getIndexDelegate())
                : BybloIO.openNeighboursSource(file, getCharset(), getIndexDelegate());
        first = true;
        return src;
    }

    @Override
    protected ObjectSink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return new KFirstReducingObjectSink<Weighted<TokenPair>>(
                new WeightSumReducerObjectSink<TokenPair>(
                BybloIO.openNeighboursSink(file, getCharset(), getIndexDelegate())),
                classComparator, k);

    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("k", k);
    }

}

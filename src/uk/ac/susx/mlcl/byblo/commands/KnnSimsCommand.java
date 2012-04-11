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
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 * Task that read in a file and produces the k-nearest-neighbors for each base
 * entry. Assumes the file is composed of entry, entry, weight triples that are
 * delimited by tabs.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class KnnSimsCommand extends SortWeightedTokenPairCommand {

    private static final Log LOG = LogFactory.getLog(KnnSimsCommand.class);

    @Parameter(names = {"-k"},
    description = "The maximum number of neighbours to produce per word.")
    private int k = ExternalKnnSimsCommand.DEFAULT_K;

    private Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.firstIndexOrder());

    private Comparator<Weighted<TokenPair>> nearnessComparator =
            Comparators.reverse(Weighted.<TokenPair>weightOrder());

    public KnnSimsCommand(File sourceFile, File destinationFile, Charset charset,
                          EnumeratorSingleBaring indexDeligate, int k) throws IOException {
        super(sourceFile, destinationFile, charset, EnumeratorDeligates.toPair(indexDeligate));
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

    @Override
    protected Sink<Weighted<TokenPair>> openSink(File file) throws FileNotFoundException, IOException {
        return new KFirstReducerSink<Weighted<TokenPair>>(
                super.openSink(file), classComparator, k);
    }
    
    
//
//    @Override
//    public void runCommand() throws Exception {
//        if (LOG.isInfoEnabled())
//            LOG.info("Running memory K-Nearest-Neighbours from \"" + getFilesDeligate().getSourceFile()
//                    + "\" to \"" + getFilesDeligate().getDestinationFile() + "\".");
//
//        Source<Weighted<TokenPair>> src = openSource(getFilesDeligate().getSourceFile());
//
////        final List<Weighted<TokenPair>> items = IOUtil.readAll(src);
////        Collections.sort(items, getComparator());
////
//        Sink<Weighted<TokenPair>> snk = openSink(getFilesDeligate().getDestinationFile());
//        snk = new KFirstReducerSink<Weighted<TokenPair>>(snk, classComparator, k);
//
//        SortTask<Weighted<TokenPair>> task = new SortTask<Weighted<TokenPair>>();
//        task.setComparator(getComparator());
//        task.setSource(src);
//        task.setSink(snk);
//        task.run();
//
//        while (task.isExceptionThrown())
//            task.throwException();
////        
////        KnnTask<Weighted<TokenPair>> task = new KnnTask<Weighted<TokenPair>>();
////        task.setSink(snk);
////        task.setSource(src);
////        task.setClassComparator(classComparator);
////        task.setNearnessComparator(nearnessComparator);
////        task.setK(k);
////
////        task.run();
////
////        if (task.isExceptionThrown())
////            task.throwException();
//
//        if (LOG.isInfoEnabled())
//            LOG.info("Completed memory K-Nearest-Neighbours.");
//
//    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("k", k);
    }

}

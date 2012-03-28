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
import com.google.common.base.Objects.ToStringHelper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import javax.naming.OperationNotSupportedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.KFirstReducerSink;
import uk.ac.susx.mlcl.byblo.tasks.Chunk;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.tasks.MergeTask;
import uk.ac.susx.mlcl.lib.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Comparators;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class ExternalKnnSimsCommand extends ExternalSortWeightedTokenPiarCommand {

    private static final Log LOG = LogFactory.getLog(ExternalKnnSimsCommand.class);

    public static final int DEFAULT_K = 100;

    @Parameter(names = {"-k"},
    description = "The number of neighbours to produce for each base entry.")
    int k = DEFAULT_K;

    Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.firstIndexOrder());

    Comparator<Weighted<TokenPair>> nearnessComparator =
            Comparators.reverse(Weighted.<TokenPair>weightOrder());

    public ExternalKnnSimsCommand(
            File sourceFile, File destinationFile, Charset charset,
            boolean preindexedTokens1, boolean preindexedTokens2,
            int k, int maxChunkSize) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        setMaxChunkSize(maxChunkSize);
        setK(k);
    }

    public ExternalKnnSimsCommand(
            File sourceFile, File destinationFile, Charset charset,
            boolean preindexedTokens1, boolean preindexedTokens2,
            int k) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        setK(k);
    }

    public ExternalKnnSimsCommand() {
        super();
    }

    @Override
    @Deprecated
    public void setComparator(Comparator<Weighted<TokenPair>> comparator) {
        throw new RuntimeException(new OperationNotSupportedException());
    }

    @Override
    @Deprecated
    public Comparator<Weighted<TokenPair>> getComparator() {
        return getCombinedComparator();
    }

    public Comparator<Weighted<TokenPair>> getCombinedComparator() {
        return Comparators.fallback(
                getClassComparator(),
                getNearnessComparator());
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
        if (k < 1) {
            throw new IllegalArgumentException("k < 1");
        }
        this.k = k;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
    }

    @Override
    protected void finaliseTask() throws Exception {
        super.finaliseTask();
    }
//
//    @Override
//    protected void runTask() throws Exception {
//
//        if (LOG.isInfoEnabled()) {
//            LOG.info("Running external K-Nearest-Neighbours from \""
//                    + getFileDeligate().getSourceFile()
//                    + "\" to \"" + getFileDeligate().getDestinationFile() + "\".");
//        }
//
//        map();
//        reduce();
//        finish();
//
//        if (LOG.isInfoEnabled()) {
//            LOG.info("Completed external K-Nearest-Neighbours.");
//        }
//
//    }

    @Override
    protected MergeTask<Weighted<TokenPair>> createMergeTask(File srcA, File srcB, File dst) throws IOException {
        MergeTask<Weighted<TokenPair>> task = super.createMergeTask(srcA, srcB, dst);
        task.setSink(new KFirstReducerSink<Weighted<TokenPair>>(
                task.getSink(), getClassComparator(), getK()));
        return task;
    }

    @Override
    protected SortTask<Weighted<TokenPair>> createSortTask(Chunk<Weighted<TokenPair>> chunk, File dst) throws IOException {
        SortTask<Weighted<TokenPair>> task = super.createSortTask(chunk, dst);
        task.setSink(new KFirstReducerSink<Weighted<TokenPair>>(
                task.getSink(), getClassComparator(), getK()));
        return task;
    }

//    private KnnTask<Weighted<TokenPair>> createKnnTask(File from, File to) throws IOException {
//        Checks.checkNotNull("from", from);
//        Checks.checkNotNull("to", to);
//
//        Source<Weighted<TokenPair>> src = openSource(from);
//        Sink<Weighted<TokenPair>> snk = openSink(to);
//
//        KnnTask<Weighted<TokenPair>> task = new KnnTask<Weighted<TokenPair>>();
//        task.setSink(snk);
//        task.setSource(src);
//        task.setClassComparator(getClassComparator());
//        task.setNearnessComparator(getNearnessComparator());
//        task.setK(k);
//
//        task.getProperties().setProperty(KEY_SRC_FILE, from.toString());
//        task.getProperties().setProperty(KEY_DST_FILE, to.toString());
//
//        return task;
//    }
//
//    @Override
//    protected void handleCompletedTask(Task task) throws Exception {
//        Checks.checkNotNull(task);
//        task.throwException();
//        Properties p = task.getProperties();
//        if (task instanceof MergeTask) {
//
//            File srca = new File(p.getProperty(KEY_SRC_FILE_A));
//            File srcb = new File(p.getProperty(KEY_SRC_FILE_B));
//            File dst = new File(p.getProperty(KEY_DST_FILE));
//
//            assert !srca.equals(dst);
//            assert !srcb.equals(dst);
//
//            submitTask(new DeleteTask(srca));
//            submitTask(new DeleteTask(srcb));
//            submitTask(createKnnTask(dst, dst));
//
//        } else if (task instanceof SortTask) {
//
//            File src = new File(p.getProperty(KEY_SRC_FILE));
//            File dst = new File(p.getProperty(KEY_DST_FILE));
//
//            assert src.equals(getFileDeligate().getSourceFile());
//
//            submitTask(createKnnTask(dst, dst));
//
//        } else if (task instanceof KnnTask) {
//
//            File src = new File(p.getProperty(KEY_SRC_FILE));
//            File dst = new File(p.getProperty(KEY_DST_FILE));
//            assert src.equals(dst);
//            queueMergeTask(dst);
//
//        } else if (task instanceof DeleteTask) {
//            // not a sausage
//        } else {
//            throw new AssertionError(
//                    "Task type " + task.getClass()
//                    + " should not have been queued.");
//        }
//    }
    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("k", k);
    }

}

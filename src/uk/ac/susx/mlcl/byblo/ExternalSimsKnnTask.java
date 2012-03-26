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
import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSink;
import uk.ac.susx.mlcl.byblo.io.WeightedTokenPairSource;
import uk.ac.susx.mlcl.byblo.tasks.KnnTask;
import uk.ac.susx.mlcl.byblo.tasks.MergeTask;
import uk.ac.susx.mlcl.byblo.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class ExternalSimsKnnTask extends ExternalSortTask.WeightedTokenPiarExternalSortTask {

    private static final Log LOG = LogFactory.getLog(ExternalSimsKnnTask.class);

    public static final int DEFAULT_K = 100;

    @Parameter(names = {"-k"},
    description = "The number of neighbours to produce for each base entry.")
    private int k = DEFAULT_K;

    private Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.indexOrder());

    private Comparator<Weighted<TokenPair>> nearnessComparator =
            Comparators.reverse(Weighted.<TokenPair>weightOrder());

    public ExternalSimsKnnTask(File sourceFile, File destinationFile,
                               Charset charset, int maxChunkSize, int k,
                               boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        setMaxChunkSize(maxChunkSize);
        updateCombinedComparator();
        setK(k);
    }

    public ExternalSimsKnnTask(File sourceFile, File destinationFile,
                               Charset charset, int k, boolean preindexedTokens1,
                               boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        updateCombinedComparator();
        setK(k);
    }

    public ExternalSimsKnnTask() {
        super();
        updateCombinedComparator();
    }

    public Comparator<Weighted<TokenPair>> getClassComparator() {
        return classComparator;
    }

    public void setClassComparator(
            Comparator<Weighted<TokenPair>> classComparator) {
        this.classComparator = classComparator;
        updateCombinedComparator();
    }

    public Comparator<Weighted<TokenPair>> getNearnessComparator() {
        return nearnessComparator;
    }

    public void setNearnessComparator(
            Comparator<Weighted<TokenPair>> nearnessComparator) {
        this.nearnessComparator = nearnessComparator;
        updateCombinedComparator();
    }

    private void updateCombinedComparator() {
        setComparator(Comparators.fallback(
                classComparator, nearnessComparator));
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        if (getComparator() == null) {
            throw new NullPointerException();
        }

    }

    @Override
    protected void finaliseTask() throws Exception {
        super.finaliseTask();
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
    protected void runTask() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info("Running external K-Nearest-Neighbours from \"" + fileDeligate.getSourceFile()
                    + "\" to \"" + fileDeligate.getDestinationFile() + "\".");
        }

        map();
        reduce();
        finish();


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed external K-Nearest-Neighbours.");
        }

    }

    protected Source<Weighted<TokenPair>> openSource(File file)
            throws FileNotFoundException, IOException {
        return new WeightedTokenPairSource(
                new TSVSource(file, getCharset()),
                indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
    }

    protected Sink<Weighted<TokenPair>> openSink(File file)
            throws FileNotFoundException, IOException {
        return new WeightSumReducerSink<TokenPair>(
                new WeightedTokenPairSink(
                new TSVSink(file, getCharset()),
                indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));
    }

    private void submitKnnTask(File from, File to) throws IOException {

        Source<Weighted<TokenPair>> src = openSource(from);
        Sink<Weighted<TokenPair>> snk = openSink(to);

        KnnTask<Weighted<TokenPair>> task = new KnnTask<Weighted<TokenPair>>();
        task.setSink(snk);
        task.setSource(src);
        task.setClassComparator(classComparator);
        task.setNearnessComparator(nearnessComparator);
        task.setK(k);

        task.getProperties().setProperty("srcFile", from.toString());
        task.getProperties().setProperty("dstFile", to.toString());

        submitTask(task);
    }

    @Override
    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();
        if (task instanceof MergeTask) {

            MergeTask mergeTask = (MergeTask) task;
            File srca = new File(mergeTask.getProperties().getProperty("srcFileA"));
            File srcb = new File(mergeTask.getProperties().getProperty("srcFileB"));
            File dst = new File(mergeTask.getProperties().getProperty("dstFile"));
            submitTask(new DeleteTask(srca));
            submitTask(new DeleteTask(srcb));
            submitKnnTask(dst, dst);

        } else if (task instanceof SortTask) {

            SortTask sortTask = (SortTask) task;
            File src = new File(sortTask.getProperties().getProperty("srcFile"));
            File dst = new File(sortTask.getProperties().getProperty("dstFile"));
            submitKnnTask(dst, dst);

        } else if (task instanceof KnnTask) {

            KnnTask knnTask = (KnnTask) task;
            File src = new File(knnTask.getProperties().getProperty("srcFile"));
            File dst = new File(knnTask.getProperties().getProperty("dstFile"));
            queueMergeTask(dst);

        } else if (task instanceof DeleteTask) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().add("k", k);
    }

}

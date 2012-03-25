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
import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Perform k-nearest-neighbours on a similarity file.")
public class ExternalKnnTask extends ExternalSortTask.SimsExternalSortTask {

    private static final Log LOG = LogFactory.getLog(ExternalKnnTask.class);

    public static final int DEFAULT_K = 100;

    @Parameter(names = {"-k"},
               description = "The number of neighbours to produce for each base entry.")
    private int k = DEFAULT_K;

    private Comparator<Weighted<TokenPair>> classComparator =
            Weighted.recordOrder(TokenPair.indexOrder());

    private Comparator<Weighted<TokenPair>> nearnessComparator =
            Comparators.reverse(Weighted.<TokenPair>weightOrder());

    public ExternalKnnTask(File sourceFile, File destinationFile,
                           Charset charset, int maxChunkSize, int k,
                           boolean preindexedTokens1, boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        setMaxChunkSize(maxChunkSize);
        updateCombinedComparator();
        setK(k);
    }

    public ExternalKnnTask(File sourceFile, File destinationFile,
                           Charset charset, int k, boolean preindexedTokens1,
                           boolean preindexedTokens2) {
        super(sourceFile, destinationFile, charset, preindexedTokens1,
              preindexedTokens2);
        updateCombinedComparator();
        setK(k);
    }

    public ExternalKnnTask() {
        super();
        updateCombinedComparator();
    }

    private Enumerator<String> index1 = null;

    private Enumerator<String> index2 = null;

    public Enumerator<String> getIndex1() {
        if (index1 == null)
            index1 = Enumerators.newDefaultStringEnumerator();
        return index1;
    }

    public void setIndex1(Enumerator<String> entryIndex) {
        this.index1 = entryIndex;
    }

    public Enumerator<String> getIndex2() {
        if (index2 == null)
            index2 = Enumerators.newDefaultStringEnumerator();
        return index2;
    }

    public void setIndex2(Enumerator<String> featureIndex) {
        this.index2 = featureIndex;
    }

    public final Function<String, Integer> getFeatureDecoder() {
        return isPreindexedTokens1()
                ? Token.enumeratedDecoder()
                : Token.stringDecoder(getIndex1());
    }

    public final Function<String, Integer> getEntryDecoder() {
        return isPreindexedTokens1()
                ? Token.enumeratedDecoder()
                : Token.stringDecoder(getIndex1());
    }

    public final Function<Integer, String> getFeatureEncoder() {
        return isPreindexedTokens2()
                ? Token.enumeratedEncoder()
                : Token.stringEncoder(getIndex2());
    }

    public final Function<Integer, String> getEntryEncoder() {
        return isPreindexedTokens2()
                ? Token.enumeratedEncoder()
                : Token.stringEncoder(getIndex2());
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
            LOG.info("Running external K-Nearest-Neighbours from \"" + getSrcFile()
                    + "\" to \"" + getDestFile() + "\".");
        }

        map();
        reduce();
        finish();


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed external K-Nearest-Neighbours.");
        }

    }

    @Override
    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();
        if (task.getClass().equals(MergeTask.SimsMergeTask.class)) {

            MergeTask.SimsMergeTask mergeTask = (MergeTask.SimsMergeTask) task;

            submitTask(new DeleteTask(mergeTask.getSourceFileA()));
            submitTask(new DeleteTask(mergeTask.getSourceFileB()));
            KnnTask knntask = new KnnTask(
                    mergeTask.getDestFile(),
                    mergeTask.getDestFile(),
                    mergeTask.getCharset(),
                    mergeTask.isPreindexedTokens1(),
                    mergeTask.isPreindexedTokens2(),
                    this.getK());
            knntask.setClassComparator(this.getClassComparator());
            knntask.setNearnessComparator(this.getNearnessComparator());
            knntask.setIndex1(mergeTask.getIndex1());
            knntask.setIndex2(mergeTask.getIndex2());
            submitTask(knntask);

        } else if (task.getClass().equals(SortTask.SimsSortTask.class)) {

            SortTask.SimsSortTask sortTask = (SortTask.SimsSortTask) task;
            KnnTask knntask = new KnnTask(
                    sortTask.getDstFile(),
                    sortTask.getDstFile(),
                    sortTask.getCharset(),
                    sortTask.isPreindexedTokens1(),
                    sortTask.isPreindexedTokens2(),
                    getK());
            sortTask.setComparator(getComparator());
            sortTask.setIndex1(sortTask.getIndex1());
            sortTask.setIndex2(sortTask.getIndex2());
            submitTask(knntask);

        } else if (task.getClass().equals(KnnTask.class)) {

            KnnTask knnTask = (KnnTask) task;
            queueMergeTask(knnTask.getDstFile());

        } else if (task.getClass().equals(DeleteTask.class)) {
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

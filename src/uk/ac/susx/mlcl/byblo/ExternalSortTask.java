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

import uk.ac.susx.mlcl.lib.tasks.TempFileFactoryConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.MergeTask.EntryFreqsMergeTask;
import uk.ac.susx.mlcl.byblo.MergeTask.EventFreqsMergeTask;
import uk.ac.susx.mlcl.byblo.MergeTask.EventMergeTask;
import uk.ac.susx.mlcl.byblo.MergeTask.FeatureFreqsMergeTask;
import uk.ac.susx.mlcl.byblo.MergeTask.SimsMergeTask;
import uk.ac.susx.mlcl.byblo.SortTask.EntryFreqsSortTask;
import uk.ac.susx.mlcl.byblo.SortTask.EventFreqsSortTask;
import uk.ac.susx.mlcl.byblo.SortTask.EventSortTask;
import uk.ac.susx.mlcl.byblo.SortTask.FeatureFreqsSortTask;
import uk.ac.susx.mlcl.byblo.SortTask.SimsSortTask;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.ReverseComparator;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class ExternalSortTask<T> extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalSortTask.class);

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines that will be read and sorted in RAM at one "
    + "time (per thread). Larger values increase memory usage and performace.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-i", "--input"},
    description = "Source file. If this argument is not given, or if it is \"-\", then stdin will be read.",
    validateWith = InputFileValidator.class,
    required = true)
    private File sourceFile;

    @Parameter(names = {"-o", "--output"},
    description = "Destination file. If this argument is not given, or if it is \"-\", then stdout will be written to.",
    validateWith = OutputFileValidator.class,
    required = true)
    private File destFile;

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory which will be used for storing temporary files.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();

    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding for reading and writing files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    private Comparator<T> comparator;

    private Queue<File> mergeQueue;

    public ExternalSortTask(File src, File dst, Charset charset,
                            Comparator<T> comparator,
                            int maxChunkSize) {
        this(src, dst, charset);
        setComparator(comparator);
        setMaxChunkSize(maxChunkSize);
    }

    public ExternalSortTask(File src, File dst, Charset charset) {
        setSourceFile(src);
        setDestinationFile(dst);
        setCharset(charset);
    }

    public ExternalSortTask() {
        super();
    }

    public final boolean isReverse() {
        return reverse;
    }

    public final void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public final Comparator<T> getComparator() {
        return isReverse() ? new ReverseComparator<T>(comparator) : comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        if (comparator == null) {
            throw new NullPointerException("comparator is null");
        }
        this.comparator = comparator;
    }

    public final int getMaxChunkSize() {
        return maxChunkSize;
    }

    public final void setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize < 1) {
            throw new IllegalArgumentException("maxChunkSize < 1");
        }
        this.maxChunkSize = maxChunkSize;
    }

    public final File getSrcFile() {
        return sourceFile;
    }

    public final File getDestFile() {
        return destFile;
    }

    public final void setSourceFile(final File sourceFile)
            throws NullPointerException {
        if (sourceFile == null) {
            throw new NullPointerException("sourceFile is null");
        }
        this.sourceFile = sourceFile;
    }

    public final void setDestinationFile(final File destFile)
            throws NullPointerException {
        if (destFile == null) {
            throw new NullPointerException("destinationFile is null");
        }
        this.destFile = destFile;
    }

    @Override
    protected void runTask() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Sorting file externally: from \"" + getSrcFile() + "\" to \"" + getDestFile() + "\".");
        }

        if (getComparator() == null) {
            throw new NullPointerException();
        }

        map();
        reduce();
        finish();


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed " + this + ".");
        }

    }

    protected void map() throws Exception {

        mergeQueue = new ArrayDeque<File>();

        BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);

        ChunkTask chunkTask = new ChunkTask(getSrcFile(), getCharset(),
                                            getMaxChunkSize());
        chunkTask.setDstFileQueue(chunkQueue);
        chunkTask.setChunkFileFactory(tempFileFactory);
        Future<ChunkTask> chunkFuture = submitTask(chunkTask);

        // Immidiately poll the chunk task so we can start handling other
        // completed tasks
        if (!getFutureQueue().poll().equals(chunkFuture)) {
            throw new AssertionError("Expecting ChunkTask on future queue.");
        }

        while (!chunkFuture.isDone() || !chunkQueue.isEmpty()) {
            if (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {

                handleCompletedTask(getFutureQueue().poll().get());

            } else if (!chunkQueue.isEmpty()) {

                File chunk = chunkQueue.take();

                submitTask(newSortTask(chunk, chunk));

            }

            // XXX: Nasty hack to stop it tight looping when both queues are empty
            Thread.sleep(1);
        }
        chunkTask.throwException();
    }

    protected void reduce() throws Exception {
        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
    }

    protected void handleCompletedTask(Task task) throws Exception {
        task.throwException();

        if (task.getClass().equals(SortTask.class)) {

            SortTask<T> sortTask = (SortTask<T>) task;
            queueMergeTask(sortTask.getDstFile());

        } else if (task.getClass().equals(MergeTask.class)) {

            MergeTask mergeTask = (MergeTask) task;
            queueMergeTask(mergeTask.getDestFile());
            submitTask(new DeleteTask(mergeTask.getSourceFileA()));
            submitTask(new DeleteTask(mergeTask.getSourceFileB()));

        } else if (task.getClass().equals(DeleteTask.class)) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }

    protected void finish() throws Exception {

        File finalMerge = mergeQueue.poll();
        new CopyTask(finalMerge, getDestFile()).runTask();
        new DeleteTask(finalMerge).runTask();
    }

    protected Future<MergeTask> queueMergeTask(File file) throws IOException {
        mergeQueue.add(file);
        if (mergeQueue.size() >= 2) {
            File result = tempFileFactory.createFile();
            MergeTask mergeTask = newMergeTask(mergeQueue.poll(), mergeQueue.poll(), result);
            return submitTask(mergeTask);
        } else {
            return null;
        }
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", sourceFile).
                add("out", destFile).
                add("chunkSize", maxChunkSize).
                add("temp", tempFileFactory).
                add("charset", charset);
    }

    abstract SortTask<T> newSortTask(File from, File to);

//    SortTask<T> newSortTask(File from, File to) {
//        SortTask<T> t = new SortTask<T>(from, to, getCharset(), getComparator());
//        return t;
//    }
    abstract MergeTask<T> newMergeTask(File from1, File from2, File to);
//
//    MergeTask<T> newMergeTask(File from1, File from2, File to) {
//        MergeTask<T> mergeTask = new MergeTask<T>(
//                from1, from2, to, getCharset());
//        mergeTask.setComparator(getComparator());
//        return mergeTask;
//    }

    public abstract static class OneTokenExternalSortTask<T> extends ExternalSortTask<T> {

        private static final Log LOG = LogFactory.getLog(SortTask.WeightedTokenSortTask.class);

        @Parameter(names = {"-p", "--preindexed"},
        description = "Whether tokens in the input events file are indexed.")
        private boolean preindexedTokens = false;

        public OneTokenExternalSortTask(
                File sourceFile, File destinationFile, Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset);
            setPreindexedTokens(preindexed);
        }

        public OneTokenExternalSortTask() {
        }

        public final boolean isPreindexedTokens() {
            return preindexedTokens;
        }

        public final void setPreindexedTokens(boolean preindexedTokens) {
            this.preindexedTokens = preindexedTokens;
        }

    }

    public abstract static class TwoTokenExternalSortTask<T> extends ExternalSortTask<T> {

        private static final Log LOG = LogFactory.getLog(SortTask.WeightedTokenSortTask.class);

        @Parameter(names = {"-p1", "--preindexed1"},
        description = "Whether tokens in the first column of the input file are indexed.")
        private boolean preindexedTokens1 = false;

        @Parameter(names = {"-p2", "--preindexed2"},
        description = "Whether entries in the second column of the input file are indexed.")
        private boolean preindexedTokens2 = false;

        public TwoTokenExternalSortTask(
                File sourceFile, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset);
            setPreindexedTokens1(preindexedTokens1);
            setPreindexedTokens2(preindexedTokens2);
        }

        public TwoTokenExternalSortTask() {
        }

        public final boolean isPreindexedTokens1() {
            return preindexedTokens1;
        }

        public final void setPreindexedTokens1(boolean preindexedTokens1) {
            this.preindexedTokens1 = preindexedTokens1;
        }

        public final boolean isPreindexedTokens2() {
            return preindexedTokens2;
        }

        public final void setPreindexedTokens2(boolean preindexedTokens2) {
            this.preindexedTokens2 = preindexedTokens2;
        }

    }

    public static class EntryFreqsExternalSortTask extends OneTokenExternalSortTask<Weighted<Token>> {

        public EntryFreqsExternalSortTask(File sourceFile, File destinationFile, Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset, preindexed);
        }

        public EntryFreqsExternalSortTask() {
        }

        @Override
        EntryFreqsSortTask newSortTask(File from, File to) {
            EntryFreqsSortTask t = new EntryFreqsSortTask(from, to, getCharset(), isPreindexedTokens());
            t.setComparator(getComparator());
            return t;
        }

        @Override
        EntryFreqsMergeTask newMergeTask(File from1, File from2, File to) {
            EntryFreqsMergeTask mergeTask = new EntryFreqsMergeTask(
                    from1, from2, to, getCharset(), isPreindexedTokens());
            mergeTask.setComparator(getComparator());
            return mergeTask;
        }

    }

    public static class FeatureFreqsExternalSortTask extends OneTokenExternalSortTask<Weighted<Token>> {

        public FeatureFreqsExternalSortTask(File sourceFile, File destinationFile, Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset, preindexed);
        }

        public FeatureFreqsExternalSortTask() {
        }

        @Override
        FeatureFreqsSortTask newSortTask(File from, File to) {
            FeatureFreqsSortTask t = new FeatureFreqsSortTask(from, to, getCharset(), isPreindexedTokens());
            t.setComparator(getComparator());
            return t;
        }

        @Override
        FeatureFreqsMergeTask newMergeTask(File from1, File from2, File to) {
            FeatureFreqsMergeTask mergeTask = new FeatureFreqsMergeTask(
                    from1, from2, to, getCharset(), isPreindexedTokens());
            mergeTask.setComparator(getComparator());
            return mergeTask;
        }

    }

    public static class EventFreqsExternalSortTask extends TwoTokenExternalSortTask<Weighted<TokenPair>> {

        public EventFreqsExternalSortTask(File sourceFile, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1, preindexedTokens2);
        }

        public EventFreqsExternalSortTask() {
        }

        @Override
        EventFreqsSortTask newSortTask(File from, File to) {
            EventFreqsSortTask t = new EventFreqsSortTask(from, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            t.setComparator(getComparator());
            return t;
        }

        @Override
        EventFreqsMergeTask newMergeTask(File from1, File from2, File to) {
            EventFreqsMergeTask mergeTask = new EventFreqsMergeTask(
                    from1, from2, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            mergeTask.setComparator(getComparator());
            return mergeTask;
        }

    }

    public static class EventExternalSortTask extends TwoTokenExternalSortTask<TokenPair> {

        public EventExternalSortTask(File sourceFile, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1, preindexedTokens2);
        }

        public EventExternalSortTask() {
        }

        @Override
        EventSortTask newSortTask(File from, File to) {
            EventSortTask t = new EventSortTask(from, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            t.setComparator(getComparator());
            return t;
        }

        @Override
        EventMergeTask newMergeTask(File from1, File from2, File to) {
            EventMergeTask mergeTask = new EventMergeTask(
                    from1, from2, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            mergeTask.setComparator(getComparator());
            return mergeTask;
        }

    }

    public static class SimsExternalSortTask extends TwoTokenExternalSortTask<Weighted<TokenPair>> {

        public SimsExternalSortTask(File sourceFile, File destinationFile, Charset charset, boolean preindexedTokens1, boolean preindexedTokens2) {
            super(sourceFile, destinationFile, charset, preindexedTokens1, preindexedTokens2);
        }

        public SimsExternalSortTask() {
        }

        @Override
        SimsSortTask newSortTask(File from, File to) {
            SimsSortTask t = new SimsSortTask(from, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            t.setComparator(getComparator());
            return t;
        }

        @Override
        SimsMergeTask newMergeTask(File from1, File from2, File to) {
            SimsMergeTask mergeTask = new SimsMergeTask(
                    from1, from2, to, getCharset(), isPreindexedTokens1(), isPreindexedTokens2());
            mergeTask.setComparator(getComparator());
            return mergeTask;
        }

    }
}

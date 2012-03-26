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

import uk.ac.susx.mlcl.byblo.tasks.MergeTask;
import uk.ac.susx.mlcl.lib.tasks.TempFileFactoryConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
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
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;

/**
 *
 * @param <T>
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

    @ParametersDelegate
    protected final FilePipeDeligate fileDeligate = new FilePipeDeligate();

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
        fileDeligate.setSourceFile(src);
        fileDeligate.setDestinationFile(dst);
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
        return isReverse() ? Comparators.reverse(comparator) : comparator;
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

    @Override
    protected void runTask() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Sorting file externally: from \""
                    + fileDeligate.getSourceFile() + "\" to \""
                    + fileDeligate.getDestinationFile() + "\".");
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

        ChunkTask chunkTask = new ChunkTask(fileDeligate.getSourceFile(),
                                            getCharset(),
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

                Source<T> source = openSource(chunk);
                Sink<T> sink = openSink(chunk);
                SortTask<T> task = new SortTask<T>(source, sink);
                task.setComparator(this.getComparator());

                task.getProperties().setProperty("srcFile", chunk.toString());
                task.getProperties().setProperty("dstFile", chunk.toString());
                submitTask(task);

            }

            // XXX: Nasty hack to stop it tight looping when both queues are empty
//            Thread.sleep(1);
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

        if (task instanceof SortTask) {

            @SuppressWarnings("unchecked")
            SortCommand<T> sortTask = (SortCommand<T>) task;
            queueMergeTask(new File(sortTask.getProperties().getProperty("dstFile")));

        } else if (task instanceof MergeTask) {

            @SuppressWarnings("unchecked")
            MergeCommand<T> mergeTask = (MergeCommand<T>) task;
            queueMergeTask(mergeTask.getDestFile());
            submitTask(new DeleteTask(mergeTask.getSourceFileA()));
            submitTask(new DeleteTask(mergeTask.getSourceFileB()));

        } else if (task instanceof DeleteTask) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }

    protected void finish() throws Exception {
        File finalMerge = mergeQueue.poll();
        new CopyCommand(finalMerge, fileDeligate.getDestinationFile()).runCommand();
        new DeleteTask(finalMerge).runTask();
    }

    protected Future<MergeTask<T>> queueMergeTask(File file) throws IOException, Exception {
        mergeQueue.add(file);
        if (mergeQueue.size() >= 2) {
            File srcA = mergeQueue.poll();
            File srcB = mergeQueue.poll();
            File dst = tempFileFactory.createFile();
            Source<T> source1 = openSource(srcA);
            Source<T> source2 = openSource(srcB);
            Sink<T> sink = openSink(dst);

            MergeTask<T> mergeTask =
                    new MergeTask<T>(source1, source2, sink);
            mergeTask.setComparator(this.getComparator());

            mergeTask.getProperties().setProperty("srcFileA", srcA.toString());
            mergeTask.getProperties().setProperty("srcFileB", srcB.toString());
            mergeTask.getProperties().setProperty("dstFile", dst.toString());

            return submitTask(mergeTask);
        } else {
            return null;
        }
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", fileDeligate.getSourceFile()).
                add("out", fileDeligate.getDestinationFile()).
                add("chunkSize", maxChunkSize).
                add("temp", tempFileFactory).
                add("charset", charset);
    }

    abstract Source<T> openSource(File file) throws IOException;

    abstract Sink<T> openSink(File file) throws IOException;

    public static class WeightedTokenExternalSortTask extends ExternalSortTask<Weighted<Token>> {

        @ParametersDelegate
        protected final SingleIndexDeligate indexDeligate = new SingleIndexDeligate();

        public WeightedTokenExternalSortTask(File sourceFile, File destinationFile,
                                             Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens(preindexed);
        }

        public WeightedTokenExternalSortTask() {
        }

        @Override
        Sink<Weighted<Token>> openSink(File file) throws IOException {
            return new WeightSumReducerSink<Token>(
                    new WeightedTokenSink(
                    new TSVSink(file, getCharset()),
                    indexDeligate.getEncoder()));
        }

        @Override
        Source<Weighted<Token>> openSource(File file) throws IOException {
            return new WeightedTokenSource(
                    new TSVSource(file, getCharset()),
                    indexDeligate.getDecoder());
        }

    }

    public static class WeightedTokenPiarExternalSortTask extends ExternalSortTask<Weighted<TokenPair>> {

        @ParametersDelegate
        protected final TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public WeightedTokenPiarExternalSortTask(File sourceFile, File destinationFile,
                                                 Charset charset, boolean preindexed1, boolean preindexed2) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens1(preindexed1);
            indexDeligate.setPreindexedTokens2(preindexed2);
        }

        public WeightedTokenPiarExternalSortTask() {
        }

        @Override
        Sink<Weighted<TokenPair>> openSink(File file) throws IOException {
            return new WeightSumReducerSink<TokenPair>(
                    new WeightedTokenPairSink(
                    new TSVSink(file, getCharset()),
                    indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));
        }

        @Override
        Source<Weighted<TokenPair>> openSource(File file) throws IOException {
            return new WeightedTokenPairSource(
                    new TSVSource(file, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
        }

    }

    public static class TokenPiarExternalSortTask extends ExternalSortTask<TokenPair> {

        @ParametersDelegate
        protected final TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

        public TokenPiarExternalSortTask(File sourceFile, File destinationFile,
                                         Charset charset, boolean preindexed1, boolean preindexed2) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens1(preindexed1);
            indexDeligate.setPreindexedTokens2(preindexed2);
        }

        public TokenPiarExternalSortTask() {
        }

        @Override
        Sink<TokenPair> openSink(File file) throws IOException {
            return new TokenPairSink(
                    new TSVSink(file, getCharset()),
                    indexDeligate.getEncoder1(), indexDeligate.getEncoder2());
        }

        @Override
        Source<TokenPair> openSource(File file) throws IOException {
            return new TokenPairSource(
                    new TSVSource(file, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());
        }

    }
}

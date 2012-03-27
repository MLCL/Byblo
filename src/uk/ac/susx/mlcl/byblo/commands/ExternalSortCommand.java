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

import uk.ac.susx.mlcl.byblo.tasks.DeleteTask;
import uk.ac.susx.mlcl.byblo.commands.IndexDeligateSingle;
import uk.ac.susx.mlcl.byblo.commands.IndexDeligatePair;
import uk.ac.susx.mlcl.byblo.commands.FilePipeDeligate;
import uk.ac.susx.mlcl.byblo.commands.CopyCommand;
import uk.ac.susx.mlcl.lib.tasks.MergeTask;
import uk.ac.susx.mlcl.lib.command.TempFileFactoryConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.io.FileFactory;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.ChunkTask;
import uk.ac.susx.mlcl.byblo.WeightSumReducerSink;
import uk.ac.susx.mlcl.byblo.allpairs.Chunk;
import uk.ac.susx.mlcl.byblo.allpairs.Chunker;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.lib.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.AbstractParallelCommandTask;

/**
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class ExternalSortCommand<T> extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalSortCommand.class);

    protected static final String KEY_SRC_FILE = "sort.src.file";

    protected static final String KEY_SRC_FILE_A = "sort.src.file.a";

    protected static final String KEY_SRC_FILE_B = "sort.src.file.b";

    protected static final String KEY_DST_FILE = "sort.dst.file";

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    private static final boolean DEBUG = true;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines that will be read and sorted in RAM at one "
    + "time (per thread). Larger values increase memory usage and performace.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @ParametersDelegate
    private final FilePipeDeligate fileDeligate = new FilePipeDeligate();

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory which will be used for storing temporary files.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    private Comparator<T> comparator;

    private Queue<File> mergeQueue;

    public ExternalSortCommand(File src, File dst, Charset charset,
                               Comparator<T> comparator,
                               int maxChunkSize) {
        this(src, dst, charset);
        setComparator(comparator);
        setMaxChunkSize(maxChunkSize);
    }

    public ExternalSortCommand(File src, File dst, Charset charset) {
        fileDeligate.setSourceFile(src);
        fileDeligate.setDestinationFile(dst);
        fileDeligate.setCharset(charset);
    }

    public ExternalSortCommand() {
        super();
    }

    public FilePipeDeligate getFileDeligate() {
        return fileDeligate;
    }

    public FileFactory getTempFileFactory() {
        return tempFileFactory;
    }

    public void setTempFileFactory(FileFactory tempFileFactory) {
        Checks.checkNotNull("tempFileFactory", tempFileFactory);
        this.tempFileFactory = tempFileFactory;
    }

    public final boolean isReverse() {
        return reverse;
    }

    public final void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public Comparator<T> getComparator() {
        return isReverse() ? Comparators.reverse(comparator) : comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        Checks.checkNotNull("comparator", comparator);
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
                    + getFileDeligate().getSourceFile() + "\" to \""
                    + getFileDeligate().getDestinationFile() + "\".");
        }

        if (getComparator() == null) {
            throw new NullPointerException();
        }


        mergeQueue = new ArrayDeque<File>();

//      
        final SeekableSource<T, ?> src = openSource(getFileDeligate().getSourceFile());
        final Chunker<T, ?> chunks = (Chunker<T, ?>) new Chunker(src, getMaxChunkSize());

        while (chunks.hasNext()) {
            Chunk<T> chunk = chunks.read();
            submitTask(createSortTask(chunk, getTempFileFactory().createFile()));
        }


        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
        File finalMerge = mergeQueue.poll();
        new CopyCommand(finalMerge, getFileDeligate().getDestinationFile()).runCommand();
        createDeleteTask(finalMerge).runTask();


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed " + this + ".");
        }

    }
//
//    protected void map() throws Exception {
//
//        mergeQueue = new ArrayDeque<File>();
//
////      
//        final SeekableSource<T, ?> src = openSource(getFileDeligate().getSourceFile());
//        final Chunker<T, ?> chunks = (Chunker<T, ?>) new Chunker(src, getMaxChunkSize());
//
//        while (chunks.hasNext()) {
//            Chunk<T> chunk = chunks.read();
//            submitTask(createSortTask(chunk, getTempFileFactory().createFile()));
//        }
//
////         while(!getFutureQueue().isEmpty()) {
////             if(getFutureQueue().peek().isDone()) {
////                 handleCompletedTask(getFutureQueue().poll().get());
////             }
////         }
//
////  BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);
//
////        ChunkTask chunkTask = new ChunkTask(
////                getFileDeligate().getSourceFile(),
////                getFileDeligate().getCharset(),
////                getMaxChunkSize());
////        chunkTask.setDstFileQueue(chunkQueue);
////        chunkTask.setChunkFileFactory(getTempFileFactory());
////        Future<ChunkTask> chunkFuture = submitTask(chunkTask);
//
//        // Immidiately poll the chunk task so we can start handling other
//        // completed tasks
////        if (!getFutureQueue().poll().equals(chunkFuture)) {
////            throw new AssertionError("Expecting ChunkTask on future queue.");
////        }
////
////        while (!chunkFuture.isDone() || !chunkQueue.isEmpty()) {
////            if (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {
////
////                handleCompletedTask(getFutureQueue().poll().get());
////
////            } else if (!chunkQueue.isEmpty()) {
////
////                File chunk = chunkQueue.take();
////
////                Source<T> source = openSource(chunk);
////                Sink<T> sink = openSink(chunk);
////                SortTask<T> task = new SortTask<T>(source, sink, this.getComparator());
////
////                task.getProperties().setProperty("srcFile", chunk.toString());
////                task.getProperties().setProperty("dstFile", chunk.toString());
////                submitTask(task);
////
////            }
////
////            // XXX: Nasty hack to stop it tight looping when both queues are empty
//////            Thread.sleep(1);
////        }
////        chunkTask.throwException();
//    }

//    protected void reduce() throws Exception {
//        while (!getFutureQueue().isEmpty()) {
//            Task task = getFutureQueue().poll().get();
//            handleCompletedTask(task);
//        }
//    }
    protected void handleCompletedTask(Task task) throws Exception {
        Checks.checkNotNull("task", task);
        task.throwException();
        final Properties p = task.getProperties();

        if (task instanceof SortTask) {

//            @SuppressWarnings("unchecked")
//            SortTask<T> sortTask = (SortTask<T>) task;
            queueMergeTask(new File(p.getProperty(KEY_DST_FILE)));

        } else if (task instanceof MergeTask) {

//            @SuppressWarnings("unchecked")
//            MergeTask<T> mergeTask = (MergeTask<T>) task;
            queueMergeTask(new File(p.getProperty(KEY_DST_FILE)));
            if (!DEBUG) {
                submitTask(createDeleteTask(new File(p.getProperty(KEY_SRC_FILE_A))));
                submitTask(createDeleteTask(new File(p.getProperty(KEY_SRC_FILE_B))));
            }

        } else if (task instanceof DeleteTask) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }
//
//    protected void finish() throws Exception {
//        File finalMerge = mergeQueue.poll();
//        new CopyCommand(finalMerge, getFileDeligate().getDestinationFile()).runCommand();
//        createDeleteTask(finalMerge).runTask();
//    }

    @Override
    protected <T extends Task> Future<T> submitTask(T task) {
        return super.submitTask(task);
    }

    protected void queueMergeTask(File file) throws IOException, Exception {
        Checks.checkNotNull("file", file);

        mergeQueue.add(file);
        if (mergeQueue.size() >= 2) {
            File srcA = mergeQueue.poll();
            File srcB = mergeQueue.poll();
            File dst = getTempFileFactory().createFile();
            submitTask(createMergeTask(srcA, srcB, dst));
        }
    }

    protected DeleteTask createDeleteTask(File file) {
        return new DeleteTask(file);
    }

    protected SortTask<T> createSortTask(Chunk<T> chunk, File dst) throws IOException {
        Sink<T> sink = openSink(dst);
        SortTask<T> task = new SortTask<T>();
        task.setSource(chunk);
        task.setSink(sink);
        task.setComparator(getComparator());

        task.getProperties().setProperty(KEY_SRC_FILE, getFileDeligate().getSourceFile().toString());
        task.getProperties().setProperty(KEY_DST_FILE, dst.toString());
        return task;
    }

    protected MergeTask<T> createMergeTask(File srcA, File srcB, File dst) throws IOException {
        Source<T> source1 = openSource(srcA);
        Source<T> source2 = openSource(srcB);
        Sink<T> sink = openSink(dst);

        MergeTask<T> mergeTask =
                new MergeTask<T>(source1, source2, sink);
        mergeTask.setComparator(this.getComparator());

        mergeTask.getProperties().setProperty(KEY_SRC_FILE_A, srcA.toString());
        mergeTask.getProperties().setProperty(KEY_SRC_FILE_B, srcB.toString());
        mergeTask.getProperties().setProperty(KEY_DST_FILE, dst.toString());

        return mergeTask;

    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", getFileDeligate().getSourceFile()).
                add("out", getFileDeligate().getDestinationFile()).
                add("chunkSize", maxChunkSize).
                add("temp", getTempFileFactory());
    }

    protected abstract SeekableSource<T, ?> openSource(File file) throws IOException;

    protected abstract Sink<T> openSink(File file) throws IOException;

    public static class WeightedTokenExternalSortTask extends ExternalSortCommand<Weighted<Token>> {

        @ParametersDelegate
        private final IndexDeligateSingle indexDeligate = new IndexDeligateSingle();

        public WeightedTokenExternalSortTask(File sourceFile, File destinationFile,
                                             Charset charset, boolean preindexed) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens(preindexed);
        }

        public WeightedTokenExternalSortTask() {
        }

        @Override
        protected Sink<Weighted<Token>> openSink(File file) throws IOException {
            return new WeightSumReducerSink<Token>(
                    new WeightedTokenSink(
                    new TSVSink(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getEncoder()));
        }

        @Override
        protected SeekableSource<Weighted<Token>, Lexer.Tell> openSource(File file) throws IOException {
            return new WeightedTokenSource(
                    new TSVSource(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getDecoder());
        }

        public IndexDeligateSingle getIndexDeligate() {
            return indexDeligate;
        }

    }

    public static class WeightedTokenPiarExternalSortCommand extends ExternalSortCommand<Weighted<TokenPair>> {

        @ParametersDelegate
        private final IndexDeligatePair indexDeligate = new IndexDeligatePair();

        public WeightedTokenPiarExternalSortCommand(File sourceFile, File destinationFile,
                                                    Charset charset, boolean preindexed1, boolean preindexed2) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens1(preindexed1);
            indexDeligate.setPreindexedTokens2(preindexed2);
        }

        public WeightedTokenPiarExternalSortCommand() {
        }

        @Override
        protected Sink<Weighted<TokenPair>> openSink(File file) throws IOException {
            return new WeightSumReducerSink<TokenPair>(
                    new WeightedTokenPairSink(
                    new TSVSink(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getEncoder1(), getIndexDeligate().getEncoder2()));
        }

        @Override
        protected SeekableSource<Weighted<TokenPair>, Lexer.Tell> openSource(File file) throws IOException {
            return new WeightedTokenPairSource(
                    new TSVSource(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getDecoder1(), getIndexDeligate().getDecoder2());
        }

        public IndexDeligatePair getIndexDeligate() {
            return indexDeligate;
        }

    }

    public static class TokenPiarExternalSortTask extends ExternalSortCommand<TokenPair> {

        @ParametersDelegate
        private final IndexDeligatePair indexDeligate = new IndexDeligatePair();

        public TokenPiarExternalSortTask(File sourceFile, File destinationFile,
                                         Charset charset, boolean preindexed1, boolean preindexed2) {
            super(sourceFile, destinationFile, charset);
            indexDeligate.setPreindexedTokens1(preindexed1);
            indexDeligate.setPreindexedTokens2(preindexed2);
        }

        public TokenPiarExternalSortTask() {
        }

        @Override
        protected Sink<TokenPair> openSink(File file) throws IOException {
            return new TokenPairSink(
                    new TSVSink(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getEncoder1(), getIndexDeligate().getEncoder2());
        }

        @Override
        protected SeekableSource<TokenPair, Lexer.Tell> openSource(File file) throws IOException {
            return new TokenPairSource(
                    new TSVSource(file, getFileDeligate().getCharset()),
                    getIndexDeligate().getDecoder1(), getIndexDeligate().getDecoder2());
        }

        public IndexDeligatePair getIndexDeligate() {
            return indexDeligate;
        }

    }
}

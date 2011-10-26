/*
 * Copyright (c) 2010-2011, University of Sussex
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
import uk.ac.susx.mlcl.lib.io.IOUtil;
import uk.ac.susx.mlcl.lib.io.TempFileFactory;
import uk.ac.susx.mlcl.lib.tasks.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public class ExternalSortTask extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalSortTask.class);
    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;
    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines that will be read and sorted in RAM at one time (per thread). Larger values increase memory usage and performace.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;
    @Parameter(names = {"-i", "--input"},
    description = "Source file. If this argument is not given, or if it is \"-\", then stdin will be read.")
    private File sourceFile = IOUtil.STDIN_FILE;
    @Parameter(names = {"-o", "--output"},
    description = "Destination file. If this argument is not given, or if it is \"-\", then stdout will be written to.")
    private File destFile = IOUtil.STDOUT_FILE;
    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory which will be used for storing temporary files.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();
    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding for reading and writing files.")
    private Charset charset = IOUtil.DEFAULT_CHARSET;
    private Comparator<String> comparator = new NeighbourComparator();
    private Queue<File> mergeQueue;

    public ExternalSortTask(File src, File dst, Charset charset,
            Comparator<String> comparator,
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

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public final Comparator<String> getComparator() {
        return comparator;
    }

    public final void setComparator(Comparator<String> comparator) {
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
                submitTask(new SortTask(chunk, chunk, getCharset(),
                        getComparator()));

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

            SortTask sortTask = (SortTask) task;
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
            MergeTask mergeTask = new MergeTask(
                    mergeQueue.poll(), mergeQueue.poll(), result, getCharset());
            mergeTask.setComparator(getComparator());
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
}

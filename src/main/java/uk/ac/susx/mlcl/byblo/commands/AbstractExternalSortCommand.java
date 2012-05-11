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
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects.ToStringHelper;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.concurrent.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.io.Chunk;
import uk.ac.susx.mlcl.lib.io.Chunker;
import uk.ac.susx.mlcl.lib.tasks.FileDeleteTask;
import uk.ac.susx.mlcl.lib.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.commands.FilePipeDelegate;
import uk.ac.susx.mlcl.lib.commands.TempFileFactoryConverter;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.*;

/**
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class AbstractExternalSortCommand<T> extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(
            AbstractExternalSortCommand.class);

    protected static final String KEY_SRC_FILE = "sort.src.file";

    protected static final String KEY_SRC_FILE_A = "sort.src.file.a";

    protected static final String KEY_SRC_FILE_B = "sort.src.file.b";

    protected static final String KEY_DST_FILE = "sort.dst.file";

    private static final boolean DEBUG = false;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines that will be read and sorted in RAM at one "
    + "time (per thread). Larger values increase memory usage and performace.")
    private int maxChunkSize = 1000000;

    @ParametersDelegate
    private final FilePipeDelegate fileDeligate = new FilePipeDelegate();

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory which will be used for storing temporary files.",
    converter = TempFileFactoryConverter.class)
    private FileFactory tempFileFactory = new TempFileFactory();

    @Parameter(names = {"-r", "--reverse"},
    description = "Reverse the result of comparisons.")
    private boolean reverse = false;

    private Comparator<T> comparator;

    private File[] nextFileToMerge;

    public AbstractExternalSortCommand(File src, File dst, Charset charset,
                                       Comparator<T> comparator,
                                       int maxChunkSize) {
        this(src, dst, charset);
        setComparator(comparator);
        setMaxChunkSize(maxChunkSize);
    }

    public AbstractExternalSortCommand(File src, File dst, Charset charset) {
        fileDeligate.setSourceFile(src);
        fileDeligate.setDestinationFile(dst);
        fileDeligate.setCharset(charset);
    }

    public AbstractExternalSortCommand() {
        super();
    }

    public FilePipeDelegate getFileDeligate() {
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

        nextFileToMerge = new File[64];

        final SeekableSource<T, ?> src = openSource(getFileDeligate().
                getSourceFile());
        final Source<Chunk<T>> chunks = Chunker.newInstance(
                src, getMaxChunkSize());

        while (chunks.hasNext()) {
            while (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {
                handleCompletedTask(getFutureQueue().poll().get());
            }

            Chunk<T> chunk = chunks.read();
            submitTask(createSortTask(chunk, getTempFileFactory().createFile()));
        }


        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }

        // Finally merge any remaining files up the stack
        // XXX ideal this should happen automatically.
        for (int i = 0; i < nextFileToMerge.length - 1; i++) {
            if (nextFileToMerge[i] == null) {

                continue;

            } else if (nextFileToMerge[i + 1] == null) {

                nextFileToMerge[i + 1] = nextFileToMerge[i];
                nextFileToMerge[i] = null;

            } else {
                File tmp = getTempFileFactory().createFile();
                ObjectMergeTask<T> mergeTask = createMergeTask(
                        nextFileToMerge[i], nextFileToMerge[i + 1], tmp);

                mergeTask.run();

                if (mergeTask.isExceptionTrapped())
                    mergeTask.throwTrappedException();
                if (mergeTask.getSink() instanceof Flushable)
                    ((Flushable) mergeTask.getSink()).flush();
                if (mergeTask.getSink() instanceof Closeable)
                    ((Closeable) mergeTask.getSink()).close();
                if (mergeTask.getSourceA() instanceof Closeable)
                    ((Closeable) mergeTask.getSourceA()).close();
                if (mergeTask.getSourceB() instanceof Closeable)
                    ((Closeable) mergeTask.getSourceB()).close();

                nextFileToMerge[i].delete();
                nextFileToMerge[i + 1].delete();
                nextFileToMerge[i] = null;
                nextFileToMerge[i + 1] = tmp;
            }

        }

        File finalMerge = nextFileToMerge[nextFileToMerge.length - 1];

        FileMoveTask moveTask = new FileMoveTask(
                finalMerge, getFileDeligate().getDestinationFile());
        moveTask.run();

        if (moveTask.isExceptionTrapped())
            moveTask.throwTrappedException();


        if (LOG.isInfoEnabled()) {
            LOG.info("Completed " + this + ".");
        }

    }

    protected void handleCompletedTask(Task task) throws Exception {
        Checks.checkNotNull("task", task);
        task.throwTrappedException();
//        final Properties 2p = task.getProperties();

        if (task.isExceptionTrapped())
            task.throwTrappedException();

        if (task instanceof ObjectSortTask) {

            ObjectSortTask<?> sortTask = (ObjectSortTask) task;
            if (sortTask.getSink() instanceof Flushable)
                ((Flushable) sortTask.getSink()).flush();
            if (sortTask.getSink() instanceof Closeable)
                ((Closeable) sortTask.getSink()).close();
            if (sortTask.getSource() instanceof Closeable)
                ((Closeable) sortTask.getSource()).close();
            queueMergeTask(new File(task.getProperty(KEY_DST_FILE)), 0);

        } else if (task instanceof ObjectMergeTask) {

            ObjectMergeTask<?> mergeTask = (ObjectMergeTask) task;
            if (mergeTask.getSink() instanceof Flushable)
                ((Flushable) mergeTask.getSink()).flush();
            if (mergeTask.getSink() instanceof Closeable)
                ((Closeable) mergeTask.getSink()).close();
            if (mergeTask.getSourceA() instanceof Closeable)
                ((Closeable) mergeTask.getSourceA()).close();
            if (mergeTask.getSourceB() instanceof Closeable)
                ((Closeable) mergeTask.getSourceB()).close();

            int depth = Integer.parseInt(mergeTask.getProperty("depth"));
            queueMergeTask(new File(task.getProperty(KEY_DST_FILE)), depth + 1);


            if (!DEBUG) {
                submitTask(createDeleteTask(new File(task.getProperty(
                        KEY_SRC_FILE_A))));
                submitTask(createDeleteTask(new File(task.getProperty(
                        KEY_SRC_FILE_B))));
            }

        } else if (task instanceof FileDeleteTask) {
            // not a sausage
        } else {
            throw new AssertionError(
                    "Task type " + task.getClass()
                    + " should not have been queued.");
        }
    }

    @Override
    protected <T extends Task> Future<T> submitTask(final T task) throws InterruptedException {
        return super.submitTask(task);
    }

    protected void queueMergeTask(File file, int depth) throws IOException, Exception {
        Checks.checkNotNull("file", file);

        if (nextFileToMerge[depth] == null) {

            nextFileToMerge[depth] = file;

        } else {

            File srcA = nextFileToMerge[depth];
            nextFileToMerge[depth] = null;
            File srcB = file;
            File dst = getTempFileFactory().createFile();
            ObjectMergeTask<T> mergeTask = createMergeTask(srcA, srcB, dst);
            mergeTask.setProperty("depth", Integer.toString(depth));
            submitTask(mergeTask);


        }
    }

    protected FileDeleteTask createDeleteTask(File file) {
        return new FileDeleteTask(file);
    }

    protected ObjectSortTask<T> createSortTask(Chunk<T> chunk, File dst) throws IOException {
        Sink<T> sink = openSink(dst);
        ObjectSortTask<T> task = new ObjectSortTask<T>();
        task.setSource(chunk);
        task.setSink(sink);
        task.setComparator(getComparator());

        task.setProperty(KEY_SRC_FILE, getFileDeligate().
                getSourceFile().toString());
        task.setProperty(KEY_DST_FILE, dst.toString());
        return task;
    }

    protected ObjectMergeTask<T> createMergeTask(File srcA, File srcB, File dst) throws IOException {
        Source<T> source1 = openSource(srcA);
        Source<T> source2 = openSource(srcB);
        Sink<T> sink = openSink(dst);

        ObjectMergeTask<T> mergeTask =
                new ObjectMergeTask<T>(source1, source2, sink);
        mergeTask.setComparator(this.getComparator());

        mergeTask.setProperty(KEY_SRC_FILE_A, srcA.toString());
        mergeTask.setProperty(KEY_SRC_FILE_B, srcB.toString());
        mergeTask.setProperty(KEY_DST_FILE, dst.toString());

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

    public final void setCharset(Charset charset) {
        fileDeligate.setCharset(charset);
    }

    public final Charset getCharset() {
        return fileDeligate.getCharset();
    }

    public final void setSourceFile(File sourceFile) throws NullPointerException {
        fileDeligate.setSourceFile(sourceFile);
    }

    public final void setDestinationFile(File destFile) throws NullPointerException {
        fileDeligate.setDestinationFile(destFile);
    }

    public final File getSourceFile() {
        return fileDeligate.getSourceFile();
    }

    public final File getDestinationFile() {
        return fileDeligate.getDestinationFile();
    }

    protected abstract SeekableSource<T, ?> openSource(File file) throws IOException;

    protected abstract Sink<T> openSink(File file) throws IOException;

}

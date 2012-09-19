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

package uk.ac.susx.mlcl.lib.tasks;

import com.google.common.base.Preconditions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Comparators;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.events.ProgressAggregate;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.*;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.text.MessageFormat;
import java.util.Comparator;

/**
 * An <code>ObjectStoreSortTask</code> takes reads all the data from an <code>ObjectStore</code>, sorts it, then writes
 * the results out to a second <code>ObjectStore</code>. Ordering is defined either by setting a comparator, or
 * alternatively the natural ordering of the object will be used if possible. The <code>from</code> and <code>to</code>
 * stores may safely reference the same store, using it supported both reading and writing.
 *
 * @param <T> Object type to be sorted
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@CheckReturnValue
@NotThreadSafe
public final class ObjectStoreExternalSortTask<T> extends AbstractParallelTask implements ProgressReporting {

    private static final Log LOG = LogFactory.getLog(ObjectStoreExternalSortTask.class);

    private final ProgressAggregate progress = new ProgressAggregate(this);

    @Nullable
    private ObjectStore<T, ?> input;

    @Nullable
    private ObjectStore<T, ?> output;

    @Nullable
    private Comparator<T> comparator;

    @Nullable
    private StoreFactory<ObjectStore<T, ?>> tempFactory;

    private final ObjectStore<T, ?>[] nextStoreToMerge = new ObjectStore[64];

    public ObjectStoreExternalSortTask(ObjectStore<T, ?> input, ObjectStore<T, ?> output, Comparator<T> comparator,
                                       StoreFactory<ObjectStore<T, ?>> tempFactory) {
        setInput(input);
        setTo(output);
        setComparator(comparator);
        setTempFactory(tempFactory);

    }

    public ObjectStoreExternalSortTask(ObjectStore<T, ?> from, ObjectStore<T, ?> to,
                                       StoreFactory<ObjectStore<T, ?>> tempFactory) {
        this(from, to, Comparators.<T>naturalOrderIfPossible(), tempFactory);
    }

    public ObjectStoreExternalSortTask() {
        input = null;
        output = null;
        tempFactory = null;
        setComparator(Comparators.<T>naturalOrderIfPossible());
    }

    public final Comparator<T> getComparator() {
        return comparator;
    }

    @Nullable
    public final ObjectStore<T, ?> getInput() {
        return input;
    }

    public final void setInput(final ObjectStore<T, ?> input) {
        Preconditions.checkNotNull(input, "input");
        Preconditions.checkArgument(input.isReadable(), "input is not readable");
        this.input = input;
    }

    @Nullable
    public final ObjectStore<T, ?> getOutput() {
        return output;
    }

    public final void setTo(final ObjectStore<T, ?> output) {
        Preconditions.checkNotNull(output, "output");
        Preconditions.checkArgument(output.isWritable(), "output is not writable");
        this.output = output;
    }

    public final void setComparator(final Comparator<T> comparator) {
        Preconditions.checkNotNull(comparator, "comparator");
        this.comparator = comparator;
    }

    @Nullable
    public final StoreFactory<ObjectStore<T, ?>> getTempFactory() {
        return tempFactory;
    }

    public final void setTempFactory(@Nullable StoreFactory<ObjectStore<T, ?>> tempFactory) {
        this.tempFactory = tempFactory;
    }


    @Override
    protected void runTask() throws Exception {
        checkState();

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.endAdjusting();

        final int maxChunkSize = estimateMaxChunkSize();
        LOG.info(MessageFormat.format("Estimated maximum chunk size: {0}", maxChunkSize));

        ObjectSource<T> source = null;
        try {
            source = getInput().openObjectSource();
            final ObjectSource<Chunk<T>> chunks = Chunker.newInstance(source, maxChunkSize);








        } finally {
            if (source != null && source.isOpen())
                source.close();
        }
//
//        FileMoveTask finalMoveTask = new FileMoveTask();
//        finalMoveTask.setDstFile(getFileDelegate().getDestinationFile());
//
//        progress.addChildProgressReporter(finalMoveTask);
//        progress.endAdjusting();
//
//        progress.startAdjusting();
//        while (chunks.hasNext()) {
//            clearCompleted();
//
//            Chunk<T> chunk = chunks.read();
//            submitTask(createSortTask(chunk));
//            progress.endAdjusting();
//            progress.startAdjusting();
//        }
//
//        clearCompletedNow();
//        progress.endAdjusting();
//        progress.startAdjusting();


    }
//
//    void clearCompleted(boolean block) throws Exception {
//        while (!getFutureQueue().isEmpty()) {
//            Task task = getFutureQueue().poll().get();
//            handleCompletedTask(task);
//        }
//    }
//
//    void clearCompletedNow() throws Exception {
//
//        List<Future<? extends Task>> completed = null;
//        for (Future<? extends Task> future : getFutureQueue()) {
//            if (future.isDone()) {
//                if (completed == null)
//                    completed = new ArrayList<Future<? extends Task>>();
//                completed.add(future);
//            }
//        }
//
//        if (completed != null && !completed.isEmpty()) {
//            getFutureQueue().removeAll(completed);
//            for (Future<? extends Task> future : completed) {
//                handleCompletedTask(future.get());
//            }
//        }
//
//    }
//
//
//    void handleCompletedTask(Task task) throws Exception {
//        Checks.checkNotNull("task", task);
//
//        if (task.isExceptionTrapped())
//            task.throwTrappedException();
//
//        if (task instanceof ObjectSortTask) {
//
//            ObjectSortTask<?> sortTask = (ObjectSortTask) task;
//            if (sortTask.getSink() instanceof Flushable)
//                ((Flushable) sortTask.getSink()).flush();
//            if (sortTask.getSink() instanceof Closeable)
//                ((Closeable) sortTask.getSink()).close();
//            if (sortTask.getSource() instanceof Closeable)
//                ((Closeable) sortTask.getSource()).close();
//            queueMergeTask(new File(task.getProperty(KEY_DST_FILE)), 0);
//
//        } else if (task instanceof ObjectMergeTask) {
//
//            ObjectMergeTask<?> mergeTask = (ObjectMergeTask) task;
//            if (mergeTask.getSink() instanceof Flushable)
//                ((Flushable) mergeTask.getSink()).flush();
//            if (mergeTask.getSink() instanceof Closeable)
//                ((Closeable) mergeTask.getSink()).close();
//            if (mergeTask.getSourceA() instanceof Closeable)
//                ((Closeable) mergeTask.getSourceA()).close();
//            if (mergeTask.getSourceB() instanceof Closeable)
//                ((Closeable) mergeTask.getSourceB()).close();
//
//            int depth = Integer.parseInt(mergeTask.getProperty("depth"));
//            queueMergeTask(new File(task.getProperty(KEY_DST_FILE)), depth + 1);
//
//
//            if (!DEBUG) {
//                submitTask(createDeleteTask(new File(task.getProperty(
//                        KEY_SRC_FILE_A))));
//                submitTask(createDeleteTask(new File(task.getProperty(
//                        KEY_SRC_FILE_B))));
//            }
//
//        } else if (task instanceof FileDeleteTask) {
//            // not a sausage
//        } else {
//            throw new AssertionError(
//                    "Task type " + task.getClass()
//                            + " should not have been queued.");
//        }
//    }
//
//    @Override
//    protected <T extends Task> Future<T> submitTask(final T task) throws InterruptedException {
//        return super.submitTask(task);
//    }
//
//    void queueMergeTask(File file, int depth) throws Exception {
//        Checks.checkNotNull("file", file);
//
//        if (nextStoreToMerge[depth] == null) {
//
//            nextStoreToMerge[depth] = file;
//
//        } else {
//
//            File srcA = nextStoreToMerge[depth];
//            nextStoreToMerge[depth] = null;
//            File dst = getTempFileFactory().createFile();
//            ObjectMergeTask<T> mergeTask = createMergeTask(srcA, file, dst);
//            mergeTask.setProperty("depth", Integer.toString(depth));
//            submitTask(mergeTask);
//
//
//        }
//    }
//
//    FileDeleteTask createDeleteTask(File file) {
//        FileDeleteTask task = new FileDeleteTask(file);
//        progress.addChildProgressReporter(task);
//        return task;
//    }
//
//    private final String KEY_DESTINATION_STORE = "destinationStore";
//
//    ObjectStoreSortTask<T> createSortTask(Chunk<T> chunk) throws IOException {
//
//        ObjectStore<T,?> from = new ObjectMemoryStore<T>(chunk);
//        ObjectStore<T,?> to = getTempFactory().newStore();
//
//        ObjectStoreSortTask<T> task = new ObjectStoreSortTask<T>();
//        task.setFrom(from);
//        task.setTo(to);
//        task.setComparator(getComparator());
//
//        progress.addChildProgressReporter(task);
//
//        return task;
//    }
//
//    ObjectMergeTask<T> createMergeTask(File srcA, File srcB, File dst) throws IOException {
//        ObjectSource<T> source1 = openSource(srcA);
//        ObjectSource<T> source2 = openSource(srcB);
//        ObjectSink<T> sink = openSink(dst);
//
//        ObjectMergeTask<T> mergeTask =
//                new ObjectMergeTask<T>(source1, source2, sink);
//        mergeTask.setComparator(this.getComparator());
//
//        mergeTask.setProperty(KEY_SRC_FILE_A, srcA.toString());
//        mergeTask.setProperty(KEY_SRC_FILE_B, srcB.toString());
//        mergeTask.setProperty(KEY_DST_FILE, dst.toString());
//
//        progress.addChildProgressReporter(mergeTask);
//
//        return mergeTask;
//
//    }

    @Override
    public String getName() {
        return "external-sort";
    }


    protected void checkState() {
        Preconditions.checkNotNull(getComparator(), "comparator");
        Preconditions.checkNotNull(getInput(), "from");
        Preconditions.checkArgument(getInput().isReadable(), "from is not readable");
        Preconditions.checkArgument(getInput().exists(), "from does not exist");
        Preconditions.checkNotNull(getOutput(), "output");
        Preconditions.checkArgument(getOutput().isWritable(), "output is not writable");
        Preconditions.checkNotNull(getTempFactory(), "tempFactory");
    }

    protected void updateProgress(final ProgressReporting.State state, final String message, final int percentComplete) {
        progress.startAdjusting();
        progress.setState(state);
        progress.setProgressPercent(percentComplete);
        progress.setMessage(message);
        progress.endAdjusting();
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    public ProgressReporting.State getState() {
        return progress.getState();
    }


    protected long getBytesPerObject() {
        return 1; // TODO: XXX
    }

    /**
     * Calculate a conservative guess at the maximum chunk size we can get away with given the available memory, and
     * number of simultaneous threads.
     * <p/>
     * History: In previous version the end user was expected to set this value, which obviously was a total disaster.
     * Most wouldn't both (because they didn't know what it was) and result was usually either code running too slowly,
     * or java running our of heap space.
     *
     * @return maximum number of events that should be loaded per worker
     */
    private int estimateMaxChunkSize() {
        // Start by at least trying to GC whatever junk is lying around
        System.gc();
        final long bytesAvailable = MiscUtil.freeMaxMemory();
        final int numTasks = (getNumThreads() + PRELOAD_SIZE);
        int chunkSize = (int) (bytesAvailable / (getBytesPerObject() * numTasks));

        int maxChunkSize = 5000000;
        // In some system we might expect a very large amount of available
        // memory. In this case we shouldn't set chunk size too large or it
        // will never parallelise.
        return Math.min(chunkSize, maxChunkSize);
    }
}

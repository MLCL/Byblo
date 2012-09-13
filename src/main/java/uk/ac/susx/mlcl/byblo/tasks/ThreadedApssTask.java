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
package uk.ac.susx.mlcl.byblo.tasks;

import com.google.common.base.Objects.ToStringHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.Chunk;
import uk.ac.susx.mlcl.lib.io.Chunker;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.tasks.Task;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * An all pairs similarity search implementation that parallelises another
 * implementation. This is achieved by breaking the work down into chunks that
 * are run concurrently.
 * <p/>
 *
 * @param <S> Type of "tell" object used to seek into the data source.
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ThreadedApssTask<S> extends NaiveApssTask<S> {

    private static final Log LOG = LogFactory.getLog(ThreadedApssTask.class);

    private Class<? extends NaiveApssTask> innerAlgorithm =
            InvertedApssTask.class;

    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    private int nThreads = DEFAULT_NUM_THREADS;

    private ExecutorService executor = null;

    private Queue<Future<? extends Task>> futureQueue = new ArrayDeque<Future<? extends Task>>();

    private Semaphore throttle;

    public ThreadedApssTask(
            SeekableObjectSource<Indexed<SparseDoubleVector>, S> A,
            SeekableObjectSource<Indexed<SparseDoubleVector>, S> B,
            ObjectSink<Weighted<TokenPair>> sink) {
        super(A, B, sink);
        setNumThreads(DEFAULT_NUM_THREADS);
    }

    public ThreadedApssTask() {
    }

    @Override
    protected void buildPreCalcs() throws IOException {
        // The super class runs this in during initialization, but we don't want
        // do to it here.
    }

    public Class<? extends NaiveApssTask> getInnerAlgorithm() {
        return innerAlgorithm;
    }

    public void setInnerAlgorithm(Class<? extends NaiveApssTask> innerAlgorithm) {
        this.innerAlgorithm = innerAlgorithm;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        executor = new ThreadPoolExecutor(
                nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        futureQueue = new ArrayDeque<Future<? extends Task>>();
        throttle = new Semaphore(getThrottleSize());
    }

    private int nChunks = 0;

    private int queuedCount = 0;

    private int completedCount = 0;

    @Override
    protected void runTask() throws Exception {

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage("Reading threaded all-pairs.");
        progress.endAdjusting();

        final int maxChunkSize = estimateChunkSize();
        if (LOG.isInfoEnabled()) {
            LOG.info("Chunk-size estimated as: " + maxChunkSize + " vectors per work unit.");
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Initialising chunker A.");
        }
        SeekableObjectSource<Chunk<Indexed<SparseDoubleVector>>, S> chunkerA =
                Chunker.newSeekableInstance(getSourceA(), maxChunkSize);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Initialising chunker B.");
        }
        SeekableObjectSource<Chunk<Indexed<SparseDoubleVector>>, S> chunkerB =
                Chunker.newSeekableInstance(getSourceB(), maxChunkSize);

        int i = 0;
        while (chunkerA.hasNext()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Reading chunk A" + i);
            }
            Chunk<Indexed<SparseDoubleVector>> chunkA = chunkerA.read();
            i++;
            chunkA.setName(Integer.toString(i));

            int j = 0;
            S restartPos = chunkerB.position();
            while (chunkerB.hasNext()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Reading chunk B" + j);
                }
                Chunk<Indexed<SparseDoubleVector>> chunkB = chunkerB.read();
                j++;
                chunkB.setName(Integer.toString(j));

                progress.startAdjusting();
                progress.setMessage(MessageFormat.format("Queueing chunk pair {0,number} and {1,number}", i, j));
                updateProgress();
                progress.endAdjusting();

                @SuppressWarnings("unchecked")
                NaiveApssTask<Integer> task = innerAlgorithm.newInstance();
                task.setSourceA(chunkA.clone());
                task.setSourceB(chunkB);
                task.setMeasure(getMeasure());
                task.setProducePair(getProducePair());
                task.setProcessRecord(getProcessRecord());
                task.setSink(getSink());
                task.setStats(getStats());
                task.setProperty("chunkPair", MessageFormat.format(
                        "{0,number} and {1,number}", i, j));
                queueTask(task);
                ++queuedCount;

                // retrieve the results
                clearCompleted(false);
            }

            nChunks = j;
            chunkerB.position(restartPos);
        }
        getExecutor().shutdown();

        clearCompleted(true);

        getExecutor().awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);

        progress.startAdjusting();
        progress.setState(State.COMPLETED);
        progress.setProgressPercent(90);
        progress.setMessage("Finished");
        progress.endAdjusting();
    }

    void updateProgress() {
        if (nChunks != 0) {
            double progress = (completedCount + queuedCount) / (double) (nChunks
                    * nChunks
                    * 2);
            this.progress.setProgressPercent((int) (100 * progress));
        }
    }

    void clearCompleted(boolean block) throws Exception {

        if (!block) {

            List<Future<? extends Task>> completed = null;
            for (Future<? extends Task> future : getFutureQueue()) {
                if (future.isDone()) {
                    Task t = future.get();
                    while (t.isExceptionTrapped()) {
                        t.throwTrappedException();
                    }
                    ++completedCount;

                    if (completed == null)
                        completed = new ArrayList<Future<? extends Task>>();
                    completed.add(future);

                    progress.startAdjusting();
                    progress.setMessage("Completed chunk pair " + t.getProperty(
                            "chunkPair"));
                    updateProgress();
                    progress.endAdjusting();
                }
            }
            if (completed != null && !completed.isEmpty())
                getFutureQueue().removeAll(completed);

        } else {

            while (!getFutureQueue().isEmpty()) {

                Future<? extends Task> completed = getFutureQueue().poll();
                Task t = completed.get();
                while (t.isExceptionTrapped()) {
                    t.throwTrappedException();
                }
                ++completedCount;

                progress.startAdjusting();
                progress.setMessage("Completed chunk pair " + t.getProperty(
                        "chunkPair"));
                updateProgress();
                progress.endAdjusting();
            }

        }


    }

    @Override
    protected void finaliseTask() throws Exception {
        if (getExecutor() != null) {
            getExecutor().shutdownNow();
        }
        super.finaliseTask();
    }

    <T extends Task> void queueTask(final T task) throws InterruptedException {
        if (task == null) {
            throw new NullPointerException("task is null");
        }

        throttle.acquire();
        final Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    progress.startAdjusting();
                    progress.setMessage("Starting chunk pair " + task.
                            getProperty("chunkPair"));
                    updateProgress();
                    progress.endAdjusting();

                    task.run();
                } finally {
                    throttle.release();
                }
            }
        };

        try {
            Future<T> future = getExecutor().submit(wrapper, task);
            if (!getFutureQueue().add(future)) {
                throw new AssertionError(MessageFormat.format(
                        "Failed to add future {0} to futureQueue, "
                                + "presumably because it already existed.", future));
            }
        } catch (RejectedExecutionException e) {
            throttle.release();
            throw e;
        } catch (RuntimeException e) {
            throttle.release();
            throw e;
        }
    }

    final int getNumThreads() {
        return nThreads;
    }

    synchronized final ExecutorService getExecutor() {
        return executor;
    }

    synchronized final Queue<Future<? extends Task>> getFutureQueue() {
        return futureQueue;
    }

    public final void setNumThreads(int nThreads) {
        if (nThreads < 1) {
            throw new IllegalArgumentException("nThreads < 1");
        }
        this.nThreads = nThreads;
    }

    private int getThrottleSize() {
        return getNumThreads() + 1;
    }


    private int estimateChunkSize() {

        // Maximum possible non-zero cardinality of any feature vector. In theory this is Integer.MAX_VALUE, through
        // with real data that bound never occurs since feature vectors are typically very sparse, especially if
        // filtering has been performed.
        // TODO: filtering stage should record the largest non-zero cardinality, which should be used for nFeatures.
        final double nFeatures = 10000;

        // number of concurrent worker units that can exist at one time
        final double nWorkUnits = getThrottleSize();

        // each worker has 2 chunks, but most of the time at least one of the chunks is shared
        final double pairMultiplier = (nWorkUnits + 1) / nWorkUnits;

        // theoretical number of bytes per feature is: 1 x int32 + 1 x double
        // note that arrays should be packed even on 64 bit platforms
        final double bytesPerFeature = 4 + 8;

        // It's a tad conservative to use free memory rather than total memory,
        // but we can't be sure what else is going on
        System.gc();
        final double availableMemory = MiscUtil.freeMaxMemory();

        double chunkSize = availableMemory / (nFeatures * nWorkUnits * pairMultiplier * bytesPerFeature);
        assert chunkSize > 0;

        // It's possible that we don't even enough memory for a single
        if (chunkSize < 1)
            chunkSize = 1;

        // Finally, we've only really calculated an upper bound. It's conceivable that the software is running in an
        // environment where it would be preferable not to just use all memory, just because it's there.
        chunkSize = Math.min(chunkSize, 4000);

        return (int) Math.floor(chunkSize);
    }

    public String getName() {
        return "threaded-allpairs";
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("innerAlgorithm", innerAlgorithm).
                add("nThreads", nThreads).
                add("executor", executor).
                add("futureQueue", futureQueue).
                add("throttle", throttle);
    }
}

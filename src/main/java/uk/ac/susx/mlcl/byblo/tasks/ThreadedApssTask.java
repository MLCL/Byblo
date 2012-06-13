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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.TokenPair;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.lib.io.Chunk;
import uk.ac.susx.mlcl.lib.io.Chunker;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.tasks.Task;

/**
 * An all pairs similarity search implementation that parallelises another
 * implementation. This is achieved by breaking the work down into chunks that
 * are run concurrently.
 *
 * @param <S> Type of "tell" object used to seek into the data source.
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ThreadedApssTask<S> extends NaiveApssTask<S> {

    private static final Log LOG = LogFactory.getLog(ThreadedApssTask.class);

    private Class<? extends NaiveApssTask> innerAlgorithm = InvertedApssTask.class;

    private static final int DEFAULT_NUM_THREADS =
            Runtime.getRuntime().availableProcessors() + 1;

    private int nThreads = DEFAULT_NUM_THREADS;

    private ExecutorService executor = null;

    private Queue<Future<? extends Task>> futureQueue =
            new ArrayDeque<Future<? extends Task>>();

    public static final int DEFAULT_MAX_CHUNK_SIZE = 1000;

    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

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

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    @Override
    protected void buildPrecalcs() throws IOException {
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
        throttle = new Semaphore(nThreads + 1);
    }

    int nChunks = 0;

    int queuedCount = 0;

    int completedCount = 0;

    @Override
    protected void runTask() throws Exception {

        progress.startAdjusting();
        progress.setState(State.RUNNING);
        progress.setMessage("Reading threaded all-pairs.");
        progress.endAdjusting();

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
                progress.setMessage(MessageFormat.format(
                        "Queueing chunk pair {0,number} and {1,number}", i, j));
                updateProgress();
                progress.endAdjusting();

                @SuppressWarnings("unchecked")
                NaiveApssTask<Integer> task = innerAlgorithm.newInstance();
                task.setSourceA(chunkA.clone());
                task.setSourceB(chunkB);
                task.setMeasure(getMeasure());
                task.setProducatePair(getProducatePair());
                task.setProcessRecord(getProcessRecord());
                task.setSink(getSink());
                task.setStats(getStats());
                task.setProperty("chunkPair", MessageFormat.format("{0,number} and {1,number}", i, j));
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
            double prog = (completedCount + queuedCount) / (double) (nChunks * nChunks * 2);
            progress.setProgressPercent((int) (100 * prog));
        }
    }

    void clearCompleted(boolean block) throws InterruptedException, ExecutionException, Exception {

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
                    progress.setMessage("Completed chunk pair " + t.getProperty("chunkPair"));
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
                progress.setMessage("Completed chunk pair " + t.getProperty("chunkPair"));
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

    protected <T extends Task> Future<T> queueTask(final T task) throws InterruptedException {
        if (task == null) {
            throw new NullPointerException("task is null");
        }

        throttle.acquire();
        final Runnable wrapper = new Runnable() {

            @Override
            public void run() {
                try {
                    progress.startAdjusting();
                    progress.setMessage("Starting chunk pair " + task.getProperty("chunkPair"));
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
            getFutureQueue().offer(future);
            return future;
        } catch (RejectedExecutionException e) {
            throttle.release();
            throw e;
        } catch (RuntimeException e) {
            throttle.release();
            throw e;
        }
    }

    public final int getNumThreads() {
        return nThreads;
    }

    protected synchronized final ExecutorService getExecutor() {
        return executor;
    }

    protected synchronized final Queue<Future<? extends Task>> getFutureQueue() {
        return futureQueue;
    }

    public final void setNumThreads(int nThreads) {
        if (nThreads < 1) {
            throw new IllegalArgumentException("nThreads < 1");
        }
        this.nThreads = nThreads;
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
                add("maxChunkSize", maxChunkSize).
                add("throttle", throttle);
    }

}

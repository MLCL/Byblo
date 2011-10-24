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
package uk.ac.susx.mlcl.byblo.allpairs;

import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.collect.Indexed;
import uk.ac.susx.mlcl.lib.collect.SparseDoubleVector;
import uk.ac.susx.mlcl.byblo.io.EntryPair;
import uk.ac.susx.mlcl.lib.io.SeekableSource;
import uk.ac.susx.mlcl.lib.io.Sink;
import uk.ac.susx.mlcl.lib.tasks.Task;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.Weighted;

/**
 * An all pairs similarity search implementation that parallelises another
 * implementation. This is achieved by breaking the work down into chunks that
 * are run concurrently.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ThreadedApssTask<S> extends NaiveApssTask<S> {

    private static final Log LOG = LogFactory.getLog(ThreadedApssTask.class);

    private Class<? extends NaiveApssTask> innerAlgorithm = InvertedApssTask.class;

    private static final int DEFAULT_NUM_THREADS =
            Runtime.getRuntime().availableProcessors() + 1;

    private int nThreads = DEFAULT_NUM_THREADS;

    private ExecutorService executor = null;

    private Queue<Future<? extends Task>> futureQueue =
            new ArrayDeque<Future<? extends Task>>();

    private int maxChunkSize = 500;

    private Semaphore throttle;

    public ThreadedApssTask(
            SeekableSource<Indexed<SparseDoubleVector>, S> A,
            SeekableSource<Indexed<SparseDoubleVector>, S> B,
            Sink<Weighted<EntryPair>> sink) {
        super(A, B, sink);
        setNumThreads(DEFAULT_NUM_THREADS);
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
        throttle = new Semaphore(nThreads * 2);
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running all-pairs similarity search: " + this.toString());
            if (LOG.isDebugEnabled())
                LOG.debug(MiscUtil.memoryInfoString());
        }

        Chunker<Indexed<SparseDoubleVector>, S> chunkerA =
                new Chunker<Indexed<SparseDoubleVector>, S>(
                getSourceA(), maxChunkSize);

        Chunker<Indexed<SparseDoubleVector>, S> chunkerB =
                new Chunker<Indexed<SparseDoubleVector>, S>(
                getSourceB(), maxChunkSize);

        int nChunks = 0;
        int i = 0;
        while (chunkerA.hasNext()) {
            Chunk<Indexed<SparseDoubleVector>> chunkA = chunkerA.read();
            i++;

            int j = 0;
            S restartPos = chunkerB.position();
            while (chunkerB.hasNext()) {
                Chunk<Indexed<SparseDoubleVector>> chunkB = chunkerB.read();
                j++;

                double complete = nChunks == 0 ? 0
                        : (double) (i * nChunks + j) / (double) (nChunks * nChunks);
                if (LOG.isDebugEnabled())
                    LOG.debug(
                            "Creating APSS task on chunk " + i + ":" + j + " (" + (complete == 0 ? "estimating " : complete * 100) + "% complete)");

                @SuppressWarnings("unchecked")
                NaiveApssTask<Integer> task = innerAlgorithm.newInstance();
                task.setSourceA(chunkA.clone());
                task.setSourceB(chunkB);
                task.setMeasure(getMeasure());
                task.setProducatePair(getProducatePair());
                task.setProcessRecord(getProcessRecord());
                task.setSink(getSink());
                task.setStats(getStats());

                queueTask(task);

                // retrieve the results
                while (getFutureQueue().peek().isDone()) {
                    Future<? extends Task> completed = getFutureQueue().poll();
                    Task t = completed.get();
                    while (t.isExceptionThrown()) {
                        t.throwException();
                    }
                }
            }
            nChunks = j + 1;
            chunkerB.position(restartPos);
        }
        getExecutor().shutdown();
        getExecutor().awaitTermination(1, TimeUnit.DAYS);

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed all-pairs similarity search: " + this);
            if (LOG.isDebugEnabled())
                LOG.debug(MiscUtil.memoryInfoString());
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
        if (task == null)
            throw new NullPointerException("task is null");

        throttle.acquire();
        final Runnable wrapper = new Runnable() {

            @Override
            public void run() {
                try {
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
        if (nThreads < 1)
            throw new IllegalArgumentException("nThreads < 1");
        this.nThreads = nThreads;
    }

    @Override
    public String toString() {
        return "ThreadedApssTask{"
                + "sourceA=" + getSourceA()
                + ", sourceB=" + getSourceB()
                + ", measure=" + getMeasure()
                + ", sink=" + getSink()
                + ", pairFilter=" + getProducatePair()
                + ", recordFilter=" + getProcessRecord()
                + ", stats=" + getStats()
                + ", precalcA=" + getPrecalcA()
                + ", precalcB=" + getPrecalcB()
                + ", innerAlgorithm=" + innerAlgorithm
                + ", nThreads=" + nThreads
                + ", executor=" + executor
                + ", futureQueue=" + futureQueue
                + ", maxChunkSize=" + maxChunkSize
                + ", throttle=" + throttle
                + '}';
    }
}

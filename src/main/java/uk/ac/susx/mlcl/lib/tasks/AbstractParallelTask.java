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

import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractParallelTask extends AbstractTask {

    private static final Log LOG = LogFactory.getLog(AbstractParallelTask.class);

    protected static final int DEFAULT_NUM_THREADS =
            Runtime.getRuntime().availableProcessors();

    private int numThreads = DEFAULT_NUM_THREADS;

    private ExecutorService executor = null;

    private Queue<Future<? extends Task>> futureQueue;

    private Semaphore throttle;

    public AbstractParallelTask() {
    }

    public void setNumThreads(int numThreads) {
        Checks.checkRangeIncl(numThreads, 1, Integer.MAX_VALUE);

        if (LOG.isWarnEnabled() && numThreads
                > Runtime.getRuntime().availableProcessors()) {
            LOG.
                    warn("numThreads (" + numThreads + ") > availableProcessors (" + Runtime.
                            getRuntime().
                            availableProcessors() + ")");
        }
        if (numThreads != this.numThreads) {
            this.numThreads = numThreads;
        }
    }

    public final int getNumThreads() {
        return numThreads;
    }

    private synchronized ExecutorService getExecutor() {
        if (executor == null) {
            // Create a new thread pool using an unbounded queue - throttling will
            // be handled by a semaphore
            final ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                    numThreads, numThreads,
                    1L, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>());

            tpe.setRejectedExecutionHandler(
                    new ThreadPoolExecutor.AbortPolicy());

            this.executor = Executors.unconfigurableExecutorService(tpe);
        }
        return executor;
    }

    @Override
    protected void initialiseTask() throws Exception {
        getExecutor();
        throttle = new Semaphore((int) (getNumThreads() * 1.5) + 1);
    }

    @Override
    protected void finaliseTask() throws Exception {
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(null, ex);
            }
            getExceptionDelegate().trapException(ex);
        } finally {
            if (!executor.isTerminated())
                executor.shutdownNow();
        }
    }

    protected synchronized final Queue<Future<? extends Task>> getFutureQueue() {
        if (futureQueue == null) {
            futureQueue = new ArrayDeque<Future<? extends Task>>();
        }
        return futureQueue;
    }

    protected <T extends Task> Future<T> submitTask(final T task) throws InterruptedException {
        Checks.checkNotNull("task", task);

        throttle.acquire();
        Runnable wrapper = new Runnable() {
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
            if (!getFutureQueue().add(future))
                throw new AssertionError(MessageFormat.
                        format("Failed to add future {0} to queue;"
                                + " queue already contained future.", future));
            return future;
        } catch (RejectedExecutionException e) {
            throttle.release();
            throw e;
        } catch (RuntimeException e) {
            throttle.release();
            throw e;
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("threads", getNumThreads()).
                add("executor", getExecutor()).
                add("futureQueue", getFutureQueue());
    }

    public boolean equals(AbstractParallelTask other) {
        if (!super.equals(other))
            return false;
        if (this.getNumThreads() != other.getNumThreads())
            return false;
        if (this.getExecutor() != other.getExecutor() && (this.getExecutor() == null || !this.
                getExecutor().
                equals(other.
                        getExecutor())))
            return false;
        if (this.futureQueue != other.futureQueue && (this.futureQueue == null || !this.futureQueue.
                equals(other.futureQueue)))
            return false;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return getClass() == obj.getClass() && equals((AbstractParallelTask) obj);
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 61 * hash + this.getNumThreads();
        hash = 61 * hash + this.getExecutor().hashCode();
        hash = 61 * hash + (this.futureQueue != null ? this.futureQueue.
                hashCode() : 0);
        return hash;
    }
}

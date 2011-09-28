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
package uk.ac.susx.mlcl.lib.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * This class is not currently used! The idea is that APSS should inherit from
 * the same type as other parallel tasks, but the design is a little different.
 * Worst still is that ThreadedApssTask inherits from NaiveApssTask so that
 * needs to be refactored before AbstractParallelTask can be integrated.
 *
 * @author Hamish Morgan (hamish.morgan@sussex.ac.uk)
 * @deprecated work in progress (or not in progress)
 */
@Deprecated
public abstract class AbstractParallelTask_APSS extends AbstractParallelTask {

    private static final Logger LOG = Logger.getLogger(
            AbstractParallelTask_APSS.class.getName());

    private int taskQueueSize = DEFAULT_NUM_THREADS;

    private Semaphore throttle;

    public AbstractParallelTask_APSS() {
    }

    public int getTaskQueueSize() {
        return taskQueueSize;
    }

    public void setTaskQueueSize(int taskQueueSize) {
        this.taskQueueSize = taskQueueSize;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        throttle = new Semaphore(getNumThreads() + taskQueueSize, true);
    }

    /**
     * Blocks until a queue slot is available.
     * 
     * @param task
     * @throws InterruptedException
     */
    protected <T extends Task> Future<T> submitTask(final Callable<T> task)
            throws InterruptedException {
        throttle.acquire();
        final Callable<T> wrapper = new Callable<T>() {

            public T call() throws Exception {
                try {
                    return task.call();
                } finally {
                    throttle.release();
                }
            }
        };

        try {
            Future<T> result = getExecutor().submit(wrapper);
            return result;
        } catch (RejectedExecutionException e) {
            throttle.release();
            throw e;
        }
    }
}

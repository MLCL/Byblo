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
import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class AbstractTask implements Task {

    private static final Log LOG = LogFactory.getLog(AbstractTask.class);

    private final Properties properties = new Properties();

    private Queue<Exception> exceptions = null;

    public enum State {

        STOPPED, INITIALISING, RUNNING, FINALISING

    }

    public State state = State.STOPPED;

    public AbstractTask() {
    }

    public Properties getProperties() {
        return properties;
    }

    protected abstract void initialiseTask() throws Exception;

    protected abstract void runTask() throws Exception;

    protected abstract void finaliseTask() throws Exception;

    @Override
    public final void run() {
        try {

            setState(State.INITIALISING);
            if (LOG.isDebugEnabled())
                LOG.debug("Initialising task: " + this);
            initialiseTask();


            setState(State.RUNNING);
            if (LOG.isDebugEnabled())
                LOG.debug("Running task: " + this);
            runTask();

        } catch (Exception ex) {
            catchException(ex);
        } catch (Throwable t) {
            catchException(new RuntimeException(t));
        } finally {
            try {

                setState(State.FINALISING);
                if (LOG.isDebugEnabled())
                    LOG.debug("Finalising task: " + this);
                finaliseTask();

            } catch (Exception ex) {
                catchException(ex);
            }

            setState(State.STOPPED);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Completed task: " + this);
            }
        }
    }

    public State getState() {
        return state;
    }

    private void setState(State state) {
        this.state = state;
    }

    protected final void catchException(Exception throwable) {
        if (LOG.isWarnEnabled())
            LOG.warn("Exception caught and queued.", throwable);
        if (exceptions == null)
            exceptions = new ArrayDeque<Exception>();
        exceptions.offer(throwable);
    }

    @Override
    public final Throwable getException() {
        return exceptions == null ? null : exceptions.poll();
    }

    @Override
    public final boolean isExceptionThrown() {
        return exceptions != null && !exceptions.isEmpty();
    }

    /**
     * Throws one exception that was caught during task execution. Repeated
     * calls to this method will throw the exception in first-in/first-out
     * order. When no exception remain this method does nothing.
     *
     * @throws Exception
     */
    @Override
    public final void throwException() throws Exception {
        if (isExceptionThrown())
            throw exceptions.poll();
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected Objects.ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this).
                add("state", getState()).
                add("exceptions", isExceptionThrown()).add("properties", getProperties());
    }

}

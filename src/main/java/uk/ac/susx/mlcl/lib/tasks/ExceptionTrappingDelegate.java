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
import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.Checks;

/**
 * Simple implementation of the ExceptionTrapping functionality, to be used as a
 * delegate by other classes.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public final class ExceptionTrappingDelegate
        implements ExceptionTrapping, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(
            ExceptionTrappingDelegate.class);

    /**
     * Store the queue of exception. Instantiated lazily on first access.
     */
    private volatile Queue<Exception> exceptionQueue = null;

    /**
     * Construct a new ExceptionTrapping delegate object, to be encapsulated by
     * other objects.
     */
    public ExceptionTrappingDelegate() {
    }

    private Queue<Exception> getExceptionQueue() {
        Queue<Exception> result = exceptionQueue;
        if (result == null)
            synchronized (this) {
                result = exceptionQueue;
                if (result == null) {
                    result = new LinkedBlockingDeque<Exception>();
                    exceptionQueue = result;
                }
            }
        return result;
    }

    /**
     * Called by the encapsulating class when an exception is caught.
     *
     * @param exception caught exception
     * @throws NullPointerException if exception is null
     */
    public final void trapException(final Exception exception)
            throws NullPointerException {
        Checks.checkNotNull(exception);
        if (LOG.isDebugEnabled())
            LOG.debug("Exception trapped.", exception);
        try {
            boolean success = getExceptionQueue().add(exception);
            assert success;
        } catch (IllegalStateException ex) {
            // caused by a failed add() to the queue, which should never happen
            // because the LinkedBlockingDeque is unbounded
            throw new AssertionError(ex);
        }
    }

    @Override
    public final boolean isExceptionTrapped() {
        return exceptionQueue != null
                && !getExceptionQueue().isEmpty();
    }

    @Override
    public final Exception getTrappedException() {
        return isExceptionTrapped() ? getExceptionQueue().poll() : null;
    }

    @Override
    public final void throwTrappedException() throws Exception {
        if (isExceptionTrapped())
            throw getTrappedException();
    }

    @Override
    public final String toString() {
        return Objects.toStringHelper(this).
                add("queue", getExceptionQueue()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        final ExceptionTrappingDelegate that = (ExceptionTrappingDelegate) obj;
        return this.getExceptionQueue() == that.getExceptionQueue()
                || (this.getExceptionQueue() != null && this.getExceptionQueue().
                    equals(that.getExceptionQueue()));
    }

    @Override
    public int hashCode() {
        assert this.getExceptionQueue() != null;
        return this.getExceptionQueue().hashCode();
    }
}
